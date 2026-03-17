package com.wechat.match;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 微信匹配聊天系统启动类
 */
@SpringBootApplication
@EnableScheduling // 启用定时任务
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}