package com.wechat.match.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

/**
 * MyBatis Plus 元数据处理器
 * 用于自动填充创建时间和更新时间
 */
@Component
public class MybatisMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 插入时自动填充创建时间和更新时间
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());

        // 对于其他有创建时间的表
        if (metaObject.hasSetter("matchTime")) {
            this.strictInsertFill(metaObject, "matchTime", LocalDateTime.class, LocalDateTime.now());
        }
        if (metaObject.hasSetter("sendTime")) {
            this.strictInsertFill(metaObject, "sendTime", LocalDateTime.class, LocalDateTime.now());
        }
        if (metaObject.hasSetter("exchangeTime")) {
            this.strictInsertFill(metaObject, "exchangeTime", LocalDateTime.class, LocalDateTime.now());
        }
        if (metaObject.hasSetter("expireTime")) {
            // 过期时间设置为7天后
            LocalDateTime expireTime = LocalDateTime.now().plusDays(7);
            this.strictInsertFill(metaObject, "expireTime", LocalDateTime.class, expireTime);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时自动填充更新时间
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}