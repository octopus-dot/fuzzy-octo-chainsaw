package com.news.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.news.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
