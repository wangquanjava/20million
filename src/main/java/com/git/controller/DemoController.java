package com.git.controller;

import com.git.dao.pojo.Demo;
import com.git.resp.BaseDataResponse;
import com.git.resp.BaseResponse;
import com.git.resp.ResultCode;
import com.git.service.DemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

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


}
