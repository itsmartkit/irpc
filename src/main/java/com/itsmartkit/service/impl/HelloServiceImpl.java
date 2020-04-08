package com.itsmartkit.service.impl;

import com.itsmartkit.annotation.RpcService;
import com.itsmartkit.service.HelloService;

/**
 * @author cyj
 * @name HelloServiceImpl
 * @description TODO
 * @date 2020/4/7 14:52
 * Version 1.0
 */
@RpcService
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String msg) {
        return "1234: " + msg;
    }
}