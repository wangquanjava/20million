package com.git.service;

import com.git.dao.mapper.DemoMapper;
import com.git.dao.mapper.GuestMapper;
import com.git.dao.mapper.GuestSourceMapper;
import com.git.dao.pojo.*;
import com.git.thread.ThreadManager;
import com.git.utils.ListUtil;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
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
                log.error("生日解析异常, id:{}, birthday:{}", guestSource.getId(), guestSource.getBirthday());
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
    public static final int SIZE_ONE_TASK = 200;
    // 相除就是线程数
    public static final int PAGE_SIZE = 100000;
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
        while (true) {
            GuestSourceExample guestSourceExample = new GuestSourceExample();
            guestSourceExample.createCriteria().andIdGreaterThan(maxId);
            guestSourceExample.setOrderByClause("id limit " + PAGE_SIZE);
            Stopwatch started1 = Stopwatch.createStarted();
            List<GuestSource> guestSources = guestSourceMapper.selectByExampleWithBLOBs(guestSourceExample);
            log.info("本次拉取任务耗时:{}", started1.elapsed(TimeUnit.MILLISECONDS));
            log.info("maxId:{}, guestSourcesSize:{}", maxId, guestSources.size());
            if (CollectionUtils.isEmpty(guestSources)) {
                log.info("执行结束!");
                break;
            }
            maxId = ListUtil.last(guestSources).getId();
            List<List<GuestSource>> partition = Lists.partition(guestSources, SIZE_ONE_TASK);
            final CountDownLatch countDownLatch = new CountDownLatch(partition.size());
            Stopwatch started = Stopwatch.createStarted();
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
                boolean await = countDownLatch.await(1, TimeUnit.MINUTES);
                Preconditions.checkArgument(await, "计数器返回false");
                log.info("本次拉取出的结果都执行完毕, 执行时间:{}", started.elapsed(TimeUnit.MILLISECONDS));
            } catch (InterruptedException e) {
                log.error("等待计数器被打断", e);
            }
        }
    }
}
