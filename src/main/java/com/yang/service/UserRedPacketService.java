package com.yang.service;


public interface UserRedPacketService {

    /**
     * grabRedPacket-函数命名可能有歧义
     * （Dao层grabRedPacket操作为保存抢红包信息，
     * Service层完成抢红包逻辑，并调用Dao层grabRedPacket保存抢红包信息）
     *
     * @param redPacketId 红包编号
     * @param userId      抢红包用户编号
     * @return 影响记录条数
     */
    int grabRedPacket(Long redPacketId, Long userId);

    //=========================For Version=================================

    /**
     * 乐观锁机制完成抢红包功能，并调用Dao层grabRedPacket保存抢红包信息
     *
     * @param redPacketId 红包编号
     * @param userId      抢红包用户编号  ！！！！！
     * @return 影响记录条数
     */
    int grabRedPacketForVersion(Long redPacketId, Long userId);

    int grabRedPacketForVersion_time(Long redPacketId, Long userId);//时间戳

    int grabRedPacketForVersion_times(Long redPacketId, Long userId);//重试次数

    //=========================By Redis======================================

    /**
     * 通过Redis实现抢红包
     *
     * @param redPacketId --红包编号
     * @param userId      --用户编号
     * @return 0--没有库存 失败
     * 1--成功，且不是最后一个红包
     * 2--成功，且是最后一个红包
     */
    Long grabRedPacketByRedis(Long redPacketId, Long userId); //Long

}
