package com.generator.dao;

import com.generator.model.UserRedPacket;
import com.generator.model.UserRedPacketExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserRedPacketMapper {
    long countByExample(UserRedPacketExample example);

    int deleteByExample(UserRedPacketExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(UserRedPacket record);

    int insertSelective(UserRedPacket record);

    List<UserRedPacket> selectByExample(UserRedPacketExample example);

    UserRedPacket selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") UserRedPacket record, @Param("example") UserRedPacketExample example);

    int updateByExample(@Param("record") UserRedPacket record, @Param("example") UserRedPacketExample example);

    int updateByPrimaryKeySelective(UserRedPacket record);

    int updateByPrimaryKey(UserRedPacket record);
}