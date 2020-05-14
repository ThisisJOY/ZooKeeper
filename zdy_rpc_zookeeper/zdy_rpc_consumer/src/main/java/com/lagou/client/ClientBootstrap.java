package com.lagou.client;

import com.lagou.ZookeeperRpcRegistryHandler;
import com.lagou.service.RpcRegistryHandler;
import com.lagou.service.UserService;

public class ClientBootstrap {

    public static void main(String[] args) throws Exception {
        RpcRegistryHandler rpcRegistryHandler = new ZookeeperRpcRegistryHandler();
        RpcConsumer rpcConsumer = new RpcConsumer(rpcRegistryHandler);
        UserService userService = (UserService) rpcConsumer.createProxy(UserService.class);

        while (true){
            Thread.sleep(2000);
            userService.sayHello("are you ok?");
            System.out.println("已响应");
        }


    }




}
