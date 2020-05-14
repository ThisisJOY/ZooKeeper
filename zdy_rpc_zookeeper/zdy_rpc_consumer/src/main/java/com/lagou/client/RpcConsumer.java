package com.lagou.client;

import com.lagou.service.NodeChangeListener;
import com.lagou.service.RpcRegistryHandler;
import com.lagou.service.RpcRequest;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RpcConsumer implements NodeChangeListener {

    private RpcRegistryHandler rpcRegistryHandler;
    private List<RpcClient> rpcClients = new ArrayList<>();

    public RpcConsumer(RpcRegistryHandler rpcRegistryHandler) throws Exception {
        this.rpcRegistryHandler = rpcRegistryHandler;
        final List<String> serviceList = rpcRegistryHandler.discover();
        for (int i = 0; i < serviceList.size(); i++) {
            String s = serviceList.get(i);
            // 127.0.0.1:8990
            final String hostName = s.substring(0, s.lastIndexOf(":"));
            final String port = s.substring(s.lastIndexOf(":") + 1);
            final RpcClient rpcClient = new RpcClient(hostName, Integer.parseInt(port));
            rpcClient.initClient();
            rpcClients.add(rpcClient);
        }
        rpcRegistryHandler.addListener(this);
    }

    public RpcRegistryHandler getRpcRegistryHandler() {
        return rpcRegistryHandler;
    }

    //创建一个代理对象
    public Object createProxy(final Class<?> serviceClass) {
        //借助JDK动态代理生成代理对象
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{serviceClass},
                new InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                        //封装
                        RpcRequest request = new RpcRequest();
                        final String requestId = UUID.randomUUID().toString();
                        final String className = method.getDeclaringClass().getName();
                        final String methodName = method.getName();
                        final Class<?>[] parameterTypes = method.getParameterTypes();

                        request.setRequestId(requestId);
                        request.setClassName(className);
                        request.setMethodName(methodName);
                        request.setParameterTypes(parameterTypes);
                        request.setParameters(args);

                        System.out.println("请求内容：" + request);

                        //去服务端请求数据
                        if (!rpcClients.isEmpty()) {
                            RpcClient rpcClient = rpcClients.get(0);
                            rpcClient.send(request);
                        }
                        return null;
                    }
                });
    }

    @Override
    public void notify(List<String> serviceList, PathChildrenCacheEvent event) throws InterruptedException {
        final PathChildrenCacheEvent.Type eventType = event.getType();
        final String path = event.getData().getPath();
        final String instance = path.substring(path.lastIndexOf("/") + 1);
        String[] address = instance.split(":");

        switch (eventType) {
            case CHILD_ADDED:
            case CONNECTION_RECONNECTED:
                final RpcClient rpcClient = new RpcClient(address[0], Integer.parseInt(address[1]));
                rpcClient.initClient();
                System.out.println("增加节点：" + instance);
                rpcClients.add(rpcClient);
                break;
            case CHILD_REMOVED:
            case CONNECTION_SUSPENDED:
            case CONNECTION_LOST:
                if (!rpcClients.isEmpty()) {
                    for (RpcClient item : rpcClients) {
                        if (item.getIp().equalsIgnoreCase(address[0]) && Integer.parseInt(address[1]) == item.getPort()) {
                            rpcClients.remove(item);
                            System.out.println("移除节点：" + instance);
                        }

                    }
                }
                break;
            default:
                break;
        }
    }
}
