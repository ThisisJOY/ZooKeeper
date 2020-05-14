package com.lagou.service;

import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;

import java.util.List;

public interface NodeChangeListener {

    void notify(List<String> serviceList, PathChildrenCacheEvent event) throws InterruptedException;
}
