package com.git.service;

import com.git.dao.mapper.DemoMapper;
import com.git.dao.mapper.GuestMapper;
import com.git.dao.mapper.GuestSourceMapper;
import com.git.dao.pojo.*;
import com.git.thread.ThreadManager;
import com.git.threadlocal.ShardTableIdThreadLocal;
import com.git.utils.ListUtil;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DemoService {

    public static final Function<GuestSource, Guest> SOURCE_GUEST_FUNCTION = new Function<GuestSource, Guest>() {
        @Override
        public Guest apply(GuestSource guestSource) {
            Guest guest = new Guest();
            guest.setId(guestSource.getId());
            guest.setName(StringUtils.abbreviate(guestSource.getName(), 20));
            guest.setAddress(StringUtils.abbreviate(guestSource.getAddress(), 100));
            try {
                guest.setBirthday(DateUtils.parseDate(guestSource.getBirthday(), "yyyyMMdd"));
            } catch (ParseException e) {
            }
            guest.setCardNo(StringUtils.abbreviate(guestSource.getCtfid(), 20));
            guest.setSex(StringUtils.abbreviate(guestSource.getGender(), 6));
            guest.setHotelTel(StringUtils.abbreviate(guestSource.getTel(), 50));
            guest.setUserMobile(StringUtils.abbreviate(guestSource.getMobile(), 20));
            guest.setUserEmail(StringUtils.abbreviate(guestSource.getEmail(), 50));
            try {
                guest.setUpdateTime(DateUtils.parseDate(guestSource.getVersion(), "yyyy-MM-dd HH:mm:ss"));
            } catch (ParseException e) {
//                log.error("version解析异常, id:{}, version:{}", guestSource.getId(), guestSource.getVersion());
            }
            return guest;
        }
    };
    public static final int SIZE_ONE_TASK = 100;
    @Autowired
    private DemoMapper demoMapper;
    @Autowired
    private GuestMapper guestMapper;
    @Autowired
    private GuestSourceMapper guestSourceMapper;

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, readOnly = true)
    public Demo get(Integer id) {
        return demoMapper.selectByPrimaryKey(id);

    }

    public List<Demo> batchGet() {
        DemoExample demoExample = new DemoExample();

        RowBounds rowBounds = new RowBounds(0, 10);


        return demoMapper.selectByExampleWithRowbounds(demoExample, rowBounds);
    }


    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public Demo insert() {
        Demo demo = new Demo();
        demo.setStartDate(new Date());
        demoMapper.insert(demo);
        return demo;
    }

    /**
     * 初始化分表数据
     * 首次拉取, 都是where id > maxId limit 0, 1000;
     */
    public void init(Integer maxIdParam) {
        int maxId = 0;
        if (maxIdParam != null) {
            maxId = maxIdParam;
        }
        int threadSize = 200;
        int beforeCapacity = 0;

        while (true) {
            int pageSize = getPageSize(threadSize, SIZE_ONE_TASK);

            GuestSourceExample guestSourceExample = new GuestSourceExample();
            guestSourceExample.createCriteria().andIdGreaterThan(maxId);
            guestSourceExample.setOrderByClause("id limit " + pageSize);
            Stopwatch selectTime = Stopwatch.createStarted();
            List<GuestSource> guestSources = guestSourceMapper.selectByExampleWithBLOBs(guestSourceExample);
            long selectTimeLong = selectTime.elapsed(TimeUnit.MILLISECONDS);
            log.info("maxId:{}, guestSourcesSize:{}", maxId, guestSources.size());
            if (CollectionUtils.isEmpty(guestSources)) {
                log.info("执行结束!");
                break;
            }
            maxId = ListUtil.last(guestSources).getId();
            List<List<GuestSource>> partition = Lists.partition(guestSources, SIZE_ONE_TASK);
            final CountDownLatch countDownLatch = new CountDownLatch(partition.size());
            Stopwatch insertTime = Stopwatch.createStarted();
            log.info("开始提交任务, 任务数:{}, 每个任务:{}个", partition.size(), SIZE_ONE_TASK);
            for (final List<GuestSource> sources : partition) {
                // 最多有70个任务, 每个任务最多200条数据
                ThreadManager.COMMEN_POOL.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            List<Guest> guestList = Lists.transform(sources, SOURCE_GUEST_FUNCTION);
                            for (Guest guest : guestList) {
                                try {
                                    guestMapper.insertSelective(guest);
                                } catch (Exception e) {
                                    log.error("插入异常, guest:{}, msg:{}", guest, e.getMessage());
                                }
                            }
                        } catch (Exception e) {
                            log.error("执行异常", e);
                        } finally {
                            countDownLatch.countDown();
                        }
                    }
                });
            }

            try {
                boolean await = countDownLatch.await(10, TimeUnit.MINUTES);
                Preconditions.checkArgument(await, "计数器返回false");
                long insertTimeLong = insertTime.elapsed(TimeUnit.MILLISECONDS);
                int afterCapacity = (int) (pageSize * 1000 / (selectTimeLong + insertTimeLong));
                log.info("本次拉取出的结果都执行完毕, insertTimeLong:{}, 每秒吞吐量:{}", insertTimeLong, afterCapacity);

                // 设置下一次的线程数
                threadSize = getThreadSize(beforeCapacity, threadSize, afterCapacity);
                // 把本次吞吐量重置为上次吞吐量
                beforeCapacity = afterCapacity;
            } catch (InterruptedException e) {
                log.error("等待计数器被打断", e);
            }
        }
    }

    public int getThreadSize(int beforeCapacity, int beforeThreadSize, int afterCapacity) {
        int promote = afterCapacity - beforeCapacity;


        if (promote > 0) {
            log.info("吞吐量上升, 增加线程数, beforeCapacity:{}, afterCapacity:{}", beforeCapacity, afterCapacity);
            return beforeThreadSize + 1;
        }

        if (promote < 0) {
            log.info("吞吐量下降, 减少线程数, beforeCapacity:{}, afterCapacity:{}", beforeCapacity, afterCapacity);
            int tmp = beforeThreadSize - 1;
            return tmp <= 0 ? 1 : tmp;
        }
        return beforeThreadSize;
    }

    public int getPageSize(int threadSize, int sizeOneTask) {
        return threadSize * sizeOneTask;
    }

    public Set<Guest> search(String cardNo, String address, String userName) {
        final GuestExample guestExample = new GuestExample();
        GuestExample.Criteria criteria = guestExample.createCriteria();
        if (StringUtils.isNotBlank(cardNo)) {
            criteria.andCardNoLike(cardNo);
        }
        if (StringUtils.isNotBlank(address)) {
            criteria.andAddressLike(address);
        }
        if (StringUtils.isNotBlank(userName)) {
            criteria.andNameLike(userName);
        }


        final Set<Guest> guests = Sets.newConcurrentHashSet();
        final CountDownLatch countDownLatch = new CountDownLatch(100);
        for (int i = 0; i < 100; i++) {
            final int finalI = i;
            ThreadManager.COMMEN_POOL.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        log.info("开始执行第{}个表", finalI);
                        ShardTableIdThreadLocal.integerThreadLocal.set(finalI);
                        List<Guest> guests1 = guestMapper.selectByExample(guestExample);
                        log.info("第{}个表执行结果多少条{}", finalI, guests1.size());
                        guests.addAll(guests1);
                        ShardTableIdThreadLocal.integerThreadLocal.remove();
                        log.info("执行完第{}个表", finalI);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        countDownLatch.countDown();
                    }
                }
            });
        }
        try {
            boolean await = countDownLatch.await(20, TimeUnit.MINUTES);
            if (!await) {
                log.info("5分钟没有结束");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return guests;
    }
}
