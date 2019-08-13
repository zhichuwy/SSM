package com.yang.dao;

import com.yang.pojo.RedPacket;
import org.springframework.stereotype.Repository;

@Repository
public interface RedPacketDao {
    /**
     * 获取红包信息
     *
     * @param id 红包id
     * @return 红包具体信息
     */
    RedPacket getRedPacket(Long id);


    /**
     * 扣减红包数量
     *
     * @param id 红包id
     * @return 更新纪录数
     */
    int decreaseRedPacket(Long id);
}
