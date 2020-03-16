package com.diy.service.impl;

import com.diy.annotations.MyService;
import com.diy.service.TestService;

@MyService("testService1")
public class TestServiceImpl1 implements TestService {
    @Override
    public String test() {
        String result = "i am test " + this.getClass().getSimpleName();
        System.out.println(result);
        return result;
    }
}
