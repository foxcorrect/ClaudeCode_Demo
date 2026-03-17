package com.wechat.match.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wechat.match.entity.WeChatRelation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 微信关系表 Mapper 接口
 */
@Mapper
public interface WeChatRelationMapper extends BaseMapper<WeChatRelation> {
}