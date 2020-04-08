package com.itsmartkit;

import com.itsmartkit.annotation.Reference;
import com.itsmartkit.service.HelloService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author cyj
 * @name HelloServiceTest
 * @description TODO
 * @date 2020/4/7 15:00
 * Version 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class HelloServiceTest {

    @Reference
    HelloService helloService;

    @Test
    public void helloTest(){
        String result = helloService.hello("feizi");
        System.out.println("============> result: " + result);
    }
}