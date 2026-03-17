package com.wechat.match.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wechat.match.entity.MatchRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 匹配记录表 Mapper 接口
 */
@Mapper
public interface MatchRecordMapper extends BaseMapper<MatchRecord> {
}