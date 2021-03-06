package com.git.controller;

import com.git.dao.pojo.Demo;
import com.git.dao.pojo.Guest;
import com.git.resp.BaseDataResponse;
import com.git.resp.BaseResponse;
import com.git.resp.ResultCode;
import com.git.service.DemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Set;

@Controller
@ResponseBody
public class DemoController {
    @Autowired
    private DemoService demoService;

    @RequestMapping("get")
    public BaseResponse get(Integer id) {
        Demo demo = demoService.get(id);

        return new BaseDataResponse(ResultCode.SUCCESSFUL_CODE, demo);
    }

    @RequestMapping("insert")
    public BaseResponse insert() {
        Demo demo = demoService.insert();

        return new BaseDataResponse(ResultCode.SUCCESSFUL_CODE, demo);
    }

    @RequestMapping("batchGet")
    public BaseResponse batchGet() {
        List<Demo> demos = demoService.batchGet();

        return new BaseDataResponse(ResultCode.SUCCESSFUL_CODE, demos);
    }
    @RequestMapping("init")
    public BaseResponse init(Integer maxId) {
        demoService.init(maxId);

        return new BaseDataResponse(ResultCode.SUCCESSFUL_CODE);
    }
    @RequestMapping("search")
    public BaseResponse init(String cardNo, String address, String userName) {
        Set<Guest> search = demoService.search(cardNo, address, userName);

        return new BaseDataResponse(ResultCode.SUCCESSFUL_CODE, search);
    }


}
