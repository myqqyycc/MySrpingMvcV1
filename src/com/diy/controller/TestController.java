package com.diy.controller;

import com.diy.annotations.MyAutoWired;
import com.diy.annotations.MyController;
import com.diy.annotations.MyRequestMapping;
import com.diy.service.TestService;
import com.diy.service.TestService2;
import com.diy.service.TestService3;

@MyController
@MyRequestMapping("/test")
public class TestController {

    @MyAutoWired("")
    private TestService service;

    @MyAutoWired("testServiceImpl2")
    private TestService service2;

    @MyAutoWired
    private TestService2 service3;

    @MyAutoWired("testService2")
    private TestService2 service4;

    @MyAutoWired
    private TestService3 service5;

    @MyRequestMapping("tt")
    String test1() {
        return service2.test();
    }
}
