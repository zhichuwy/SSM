package com.yang.service;

public interface RedisRedPacketService {

    /**
     * 保存Redis抢红包列表
     * @param redPacketId --红包编号
     * @param unitAmount --红包金额
     */

    void saveUserRedPacketByRedis(Long redPacketId, Double unitAmount);

}

