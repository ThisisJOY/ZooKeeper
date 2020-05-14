package com.lagou;

import com.lagou.server.RpcServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServerBootstrap {

    public static void main(String[] args) throws Exception {

        SpringApplication.run(ServerBootstrap.class, args);
        int port = 8990;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        RpcServer.startServer("127.0.0.1", port);
    }

}
