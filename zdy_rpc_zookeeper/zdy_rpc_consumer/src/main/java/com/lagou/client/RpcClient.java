package com.lagou.client;

import com.lagou.service.JSONSerializer;
import com.lagou.service.RpcEncoder;
import com.lagou.service.RpcRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RpcClient {
    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private RpcClientHandler rpcClientHandler = new RpcClientHandler();
    private String ip;
    private int port;

    public RpcClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public RpcClientHandler getRpcClientHandler() {
        return rpcClientHandler;
    }

    //初始化netty客户端
    public void initClient() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();

        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new RpcEncoder(RpcRequest.class, new JSONSerializer()));
                        pipeline.addLast(new StringDecoder());
                        pipeline.addLast(rpcClientHandler);
                    }
                });

        bootstrap.connect(ip, port).sync();
        System.out.println("启动客户端, ip:" + ip + ", port:" + port);
    }

    public Object send(RpcRequest request) throws ExecutionException, InterruptedException {
        rpcClientHandler.setPara(request);
        System.out.println("提供服务的ip:" + ip + ":" + port);
        return executor.submit(rpcClientHandler).get();
    }
}
