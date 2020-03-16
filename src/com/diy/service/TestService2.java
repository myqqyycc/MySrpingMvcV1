package com.diy.service;

import com.diy.annotations.MyService;

@MyService
public class TestService2 {

    public String test2() {
        String result = "i am test " + this.getClass().getSimpleName();
        System.out.println(result);
        return result;
    }

}
