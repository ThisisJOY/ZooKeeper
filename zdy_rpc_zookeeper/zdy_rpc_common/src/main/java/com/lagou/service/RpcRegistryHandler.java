package com.lagou.service;

import java.util.List;

public interface RpcRegistryHandler {
    /**
     * 服务注册
     *
     * @param ip
     * @param port
     * @return
     */
    boolean register(String ip, int port) throws Exception;

    /**
     * 服务发现
     */
    List<String> discover() throws Exception;

    /**
     * 启动监听
     */
    void addListener(NodeChangeListener listener);

}
