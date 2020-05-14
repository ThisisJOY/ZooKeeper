package com.lagou.server;

import com.lagou.ZookeeperRpcRegistryHandler;
import com.lagou.handler.RpcServerHandler;
import com.lagou.service.JSONSerializer;
import com.lagou.service.RpcDecoder;
import com.lagou.service.RpcRequest;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.stereotype.Service;

@Service
public class RpcServer {

    public static void startServer(String ip, int port) throws Exception {

        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new StringEncoder());
                        pipeline.addLast(new RpcDecoder(RpcRequest.class, new JSONSerializer()));
                        pipeline.addLast(new RpcServerHandler());

                    }
                });
        final ChannelFuture sync = serverBootstrap.bind(ip, port).sync();
        System.out.println("==============开始注册==============");
        register(ip, port);
        System.out.println("启动成功: ip:" + ip + ", port:" + port);
        sync.channel().closeFuture().sync();
    }

    public static void register(String ip, int port) throws Exception {
        final ZookeeperRpcRegistryHandler zookeeperRpcRegistryHandler = new ZookeeperRpcRegistryHandler();
        zookeeperRpcRegistryHandler.register(ip, port);
    }
}
