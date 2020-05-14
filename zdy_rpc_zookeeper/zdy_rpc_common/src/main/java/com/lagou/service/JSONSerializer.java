package com.lagou.service;


import com.alibaba.fastjson.JSON;

//采用JSON的方式，定义JSONSerializer的实现类:（其他序列化方式，可以自行实现序列化接口）
public class JSONSerializer implements Serializer {

    public byte[] serialize(Object object) {
        return JSON.toJSONBytes(object);
    }

    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        return JSON.parseObject(bytes, clazz);
    }
}
