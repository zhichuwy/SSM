package com.yang.dao;

import com.yang.pojo.UserRedPacket;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRedPacketDao {
    /**
     * 插入抢红包信息
     *
     * @param userRedPacket 抢红包信息
     * @return 影响记录数
     */
    int grapRedPacket(UserRedPacket userRedPacket);

}
