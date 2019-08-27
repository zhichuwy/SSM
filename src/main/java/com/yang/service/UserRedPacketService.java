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

    /**
     * 乐观锁机制完成抢红包功能，并调用Dao层grabRedPacket保存抢红包信息
     *
     * @param redPacketId 红包编号
     * @param userId      抢红包用户编号  ！！！！！
     * @return 影响记录条数
     */
    int grabRedPacketForVersion(Long redPacketId, Long userId);

    //====================================================================

    int grabRedPacketForVersion_time(Long redPacketId, Long userId);//时间戳
    int grabRedPacketForVersion_times(Long redPacketId, Long userId);//重试次数

}
