package com.yang.dao;

import com.yang.pojo.UserRedPacket;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRedPacketDao {
    /**
     * 插入抢红包信息（命名不合理--实际功能为插入信息）
     *
     * @param userRedPacket 抢红包信息
     * @return 影响记录数
     */
    int grabRedPacket(UserRedPacket userRedPacket);

}
