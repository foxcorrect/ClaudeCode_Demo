package com.wechat.match.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * OpenAPI配置类
 * 访问地址：http://localhost:8080/api/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Bean
    public OpenAPI wechatMatchOpenAPI() {
        Server localServer = new Server()
                .url("http://localhost:8080" + contextPath)
                .description("本地开发环境");

        Server devServer = new Server()
                .url("http://dev.wechat-match.com" + contextPath)
                .description("开发环境");

        Contact contact = new Contact()
                .name("微信匹配聊天系统")
                .email("support@wechat-match.com")
                .url("https://wechat-match.com");

        License mitLicense = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("微信小程序匹配聊天系统 API")
                .version("v1.0.0")
                .contact(contact)
                .description("微信小程序匹配聊天系统后端API接口文档")
                .license(mitLicense)
                .termsOfService("https://wechat-match.com/terms");

        return new OpenAPI()
                .info(info)
                .servers(Arrays.asList(localServer, devServer));
    }
}