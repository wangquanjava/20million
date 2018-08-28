package com.git.service;

import com.git.dao.mapper.DemoMapper;
import com.git.dao.pojo.Demo;
import com.git.dao.pojo.DemoExample;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class DemoService {

    @Autowired
    private DemoMapper demoMapper;

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
}
