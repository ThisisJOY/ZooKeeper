package com.lagou.handler;

import com.lagou.service.RpcRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.beans.BeansException;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;

@Component
public class RpcServerHandler extends ChannelInboundHandlerAdapter implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        RpcServerHandler.applicationContext = applicationContext;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        RpcRequest msg1 = (RpcRequest) msg;
        Object handler = handler(msg1);
        ctx.writeAndFlush("success");


        // 判断是否符合约定，符合则调用本地方法，返回数据
        // msg:  UserService#sayHello#are you ok?
//        if(msg.toString().startsWith("UserService")){
//            UserServiceImpl userService = new UserServiceImpl();
//            String result = userService.sayHello(msg.toString().substring(msg.toString().lastIndexOf("#") + 1));
//            ctx.writeAndFlush(result);
//        }


    }

    private Object handler(RpcRequest request) throws ClassNotFoundException, InvocationTargetException {
        final Class<?> clazz = Class.forName(request.getClassName());
        final Object serviceBean = applicationContext.getBean(clazz);

        final String methodName = request.getMethodName();
        final Object[] parameters = request.getParameters();
        final Class<?>[] parameterTypes = request.getParameterTypes();

        final FastClass fastClass = FastClass.create(clazz);
        final FastMethod fastMethod = fastClass.getMethod(methodName, parameterTypes);

        return fastMethod.invoke(serviceBean, parameters);
    }
}
