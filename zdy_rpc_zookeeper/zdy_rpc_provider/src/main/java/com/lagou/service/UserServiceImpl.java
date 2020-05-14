package com.lagou.service;

import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    public String sayHello(String word) {
        System.out.println("调用成功--参数 " + word);
        return "调用成功--参数 " + word;
    }

}
