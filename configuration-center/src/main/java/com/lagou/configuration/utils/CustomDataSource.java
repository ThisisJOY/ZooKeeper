package com.lagou.configuration.utils;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.RetryNTimes;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomDataSource {

    private static final String CONFIG_PREFIX = "/config";
    private static final String SPLITTER = "/";
    private static CuratorFramework client;
    private static Map<String, String> configMap = new HashMap<>();
    private static ThreadLocal<DruidDataSource> threadLocal = new ThreadLocal<>();

    public static void init() throws Exception {
        client = CuratorFrameworkFactory.newClient("127.0.0.1", new RetryNTimes(3, 1000));
        client.start();
        getConfig();
        initDataSource();
        startListener();
    }

    //获取ZK配置
    private static void getConfig() throws Exception {
        final List<String> childrenNames = client.getChildren().forPath(CONFIG_PREFIX);
        for (String childrenName : childrenNames) {
            final String value = new String(client.getData().forPath(CONFIG_PREFIX + SPLITTER + childrenName));
            configMap.put(childrenName, value);
        }
    }

    private static void initDataSource() {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setDriverClassName(configMap.get("driverClassName"));
        druidDataSource.setUrl(configMap.get("url"));
        druidDataSource.setUsername(configMap.get("username"));
        druidDataSource.setPassword(configMap.get("password"));
        threadLocal.set(druidDataSource);
    }

    private static void startListener() throws Exception {
        PathChildrenCache watcher = new PathChildrenCache(client, CONFIG_PREFIX, true);
        watcher.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
                String path = event.getData().getPath();
                System.out.println("该节点数据产生变更：" + new String(event.getData().getData()));

                if (path.startsWith(CONFIG_PREFIX)) {
                    String key = path.replace(CONFIG_PREFIX + SPLITTER, "");
                    //子节点新增或变更时 更新缓存信息
                    if (PathChildrenCacheEvent.Type.CHILD_UPDATED.equals(event.getType())) {
                        System.out.println("更新配置信息");
                        configMap.put(key, new String(event.getData().getData()));
                        //释放之前的连接池，创建新的连接池
                        if (threadLocal.get() != null) {
                            threadLocal.get().close();
                        }
                        initDataSource();
                        System.out.println("更新连接池..." + threadLocal.get().getUrl());
                    }
                }
            }
        });

        System.out.println("监听启动");
        watcher.start();

    }

    public static Connection getConnection() throws Exception {
        return threadLocal.get().getConnection();
    }
}
