package com.diy.service.impl;

import com.diy.annotations.MyService;
import com.diy.service.TestService;
import com.diy.service.TestService3;

@MyService
public class TestServiceImpl3 implements TestService3 {
    @Override
    public String test() {
        String result = "i am test " + this.getClass().getSimpleName();
        System.out.println(result);
        return result;
    }
}
