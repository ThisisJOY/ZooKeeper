package com.lagou;

import com.lagou.service.NodeChangeListener;
import com.lagou.service.RpcRegistryHandler;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.List;

public class ZookeeperRpcRegistryHandler implements RpcRegistryHandler {
    private static final String ZK_PATH_SPLITTER = "/";
    private static final String ZK_PATH = "/rpc/com.lagou.service.UserService/provider";
    private static final List<NodeChangeListener> listenerList = new ArrayList<>();
    private static List<String> serviceList = new ArrayList<>();
    private CuratorFramework client;

    /**
     * 初始化Curator
     */
    public ZookeeperRpcRegistryHandler() {
        client = CuratorFrameworkFactory.newClient("127.0.0.1", new RetryNTimes(3, 1000));
        client.getConnectionStateListenable().addListener(new ConnectionStateListener() {

            @Override
            public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
                if (ConnectionState.CONNECTED.equals(connectionState)) {
                    System.out.println("注册中心连接成功");
                }
            }
        });
        client.start();

    }

    @Override
    public boolean register(String ip, int port) throws Exception {
        final Stat stat = client.checkExists().forPath(ZK_PATH);
        if (stat == null) {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(ZK_PATH);
        }

        // /rpc/com.lagou.service.UserService/provider/127.0.0.1:8990
        String instancePath = ZK_PATH + ZK_PATH_SPLITTER + ip + ":" + port;
        client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, "init".getBytes());
        return true;
    }

    @Override
    public List<String> discover() throws Exception {
        if (serviceList.isEmpty()) {
            System.out.println("首次从注册中心查找服务地址");
            serviceList = client.getChildren().forPath(ZK_PATH);
        }
        registerWatch();
        return serviceList;
    }

    private void registerWatch() throws Exception {
        PathChildrenCache watcher = new PathChildrenCache(client, ZK_PATH, true);
        watcher.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                serviceList = client.getChildren().forPath(ZK_PATH);
                listenerList.stream().forEach(nodeChangeListener -> {
                    System.out.println("节点变化，开始通知业务");
                    try {
                        nodeChangeListener.notify(serviceList, event);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
        watcher.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
    }

    @Override
    public void addListener(NodeChangeListener listener) {
        listenerList.add(listener);
    }
}
