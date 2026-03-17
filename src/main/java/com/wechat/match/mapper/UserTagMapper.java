package com.wechat.match.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wechat.match.entity.UserTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户标签关联表 Mapper 接口
 */
@Mapper
public interface UserTagMapper extends BaseMapper<UserTag> {

    /**
     * 根据用户ID查询用户标签列表（包含标签信息）
     */
    @Select("SELECT ut.*, t.name as tag_name, t.description as tag_description, t.category as tag_category, t.type as tag_type " +
            "FROM user_tag ut " +
            "LEFT JOIN tag t ON ut.tag_id = t.id " +
            "WHERE ut.user_id = #{userId} AND t.status = 1 " +
            "ORDER BY ut.weight DESC, ut.created_at DESC")
    List<UserTag> selectUserTagsWithDetail(Long userId);

    /**
     * 根据用户ID查询标签ID列表
     */
    @Select("SELECT tag_id FROM user_tag WHERE user_id = #{userId} ORDER BY weight DESC")
    List<Long> selectTagIdsByUserId(Long userId);

    /**
     * 根据标签ID查询用户ID列表
     */
    @Select("SELECT user_id FROM user_tag WHERE tag_id = #{tagId}")
    List<Long> selectUserIdsByTagId(Long tagId);
}