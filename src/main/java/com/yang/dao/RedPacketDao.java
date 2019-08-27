package com.yang.dao;

import com.yang.pojo.RedPacket;
import org.apache.ibatis.annotations.Param;
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

    //################ 悲观锁机制 ##################
    RedPacket getRedPacketForUpdate(Long id);
    //################ 悲观锁机制 ##################


    //====================================================

    /**
     * 扣减红包数量
     *
     * @param id 红包id
     * @return 更新纪录数
     */
    int decreaseRedPacket(Long id);


    //################ 乐观锁机制 ##################

    /**
     * 注意 Service层  函数参数含义
     * grabRedPacketForVersion(Long redPacketId, Long userId);
     *
     * @param id      红包id
     * @param version 版本号 ！！！！！
     * @return 影响记录数 0：更新失败， 1：更新成功
     */
    //nested exception is org.apache.ibatis.binding.BindingException:
    // Parameter 'id' not found.

    int decreaseRedPacketForVersion(@Param("id") Long id,
                                    @Param("version") Integer version);
    //################ 乐观锁机制 ##################

}
