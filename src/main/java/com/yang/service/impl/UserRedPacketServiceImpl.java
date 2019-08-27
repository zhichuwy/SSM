package com.yang.service.impl;

import com.yang.dao.RedPacketDao;
import com.yang.dao.UserRedPacketDao;
import com.yang.pojo.RedPacket;
import com.yang.pojo.UserRedPacket;
import com.yang.service.UserRedPacketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserRedPacketServiceImpl implements UserRedPacketService {

    @Autowired
    private RedPacketDao redPacketDao = null;

    @Autowired
    private UserRedPacketDao userRedPacketDao = null;

    UserRedPacket userRedPacket = null;

    private static final int FAILED = 0;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED)
    public int grabRedPacket(Long redPacketId, Long userId) {

        //获取红包信息
        //RedPacket redPacket = redPacketDao.getRedPacket(redPacketId);

        //################ 悲观锁机制 ##################
        RedPacket redPacket = redPacketDao.getRedPacketForUpdate(redPacketId);
        //################ 悲观锁机制 ##################


        //当前红包库存 > 0
        if (redPacket.getStock() > 0) {
            redPacketDao.decreaseRedPacket(redPacketId);
            UserRedPacket userRedPacket = new UserRedPacket();
            userRedPacket.setRedPacketId(redPacketId);
            userRedPacket.setUserId(userId);
            userRedPacket.setAmount(redPacket.getUnitAmount());
            userRedPacket.setNote("抢红包" + redPacketId);

            //插入抢红包信息
            int result = userRedPacketDao.grabRedPacket(userRedPacket);
            return result;
        }
        return FAILED;
    }

    //=============================乐观锁机制(不重入-start)===================================

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED)
    public int grabRedPacketForVersion(Long redPacketId, Long userId) {

        //获取红包信息，注意Version值   getRedPacket(乐观锁不需要加行更新锁)
        RedPacket redPacket = redPacketDao.getRedPacket(redPacketId);

        //当前红包库存 > 0
        if (redPacket.getStock() > 0) {
            //再次传入线程保存的version旧值给SQL判断(通过起初读取的version更新库存和版本号)
            // 是否有其他线程修改过数据
            int update = redPacketDao.decreaseRedPacketForVersion(redPacketId,
                    redPacket.getVersion());
            //update影响记录数 0：更新失败(其他线程修改过version,抢红包失败)， 1：更新成功
            if (update == 0) {
                return FAILED;
            }

            UserRedPacket userRedPacket = new UserRedPacket();
            userRedPacket.setRedPacketId(redPacketId);
            userRedPacket.setUserId(userId);
            userRedPacket.setAmount(redPacket.getUnitAmount());
            userRedPacket.setNote("抢红包" + redPacketId);

            //插入抢红包信息
            int result = userRedPacketDao.grabRedPacket(userRedPacket);
            return result;
        }
        //失败返回
        return FAILED;
    }

    //=============================乐观锁机制(不重入-end)===================================

    //=============================乐观锁机制(重入-时间戳-start)=============================
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED)
    public int grabRedPacketForVersion_time(Long redPacketId, Long userId) {

        //记录开始时间
        long start = System.currentTimeMillis();

        //无限循环，重试成功or时间满100ms退出
        while (true) {

            //获取循环当前时间
            long end = System.currentTimeMillis();
            //超过100ms，返回失败
            if (end - start > 100) {
                return FAILED;
            }

            //获取红包信息，注意Version值   getRedPacket(乐观锁不需要加行更新锁)
            RedPacket redPacket = redPacketDao.getRedPacket(redPacketId);

            //当前红包库存 > 0
            if (redPacket.getStock() > 0) {
                //再次传入线程保存的version旧值给SQL判断(通过起初读取的version更新库存和版本号)
                // 是否有其他线程修改过数据
                int update = redPacketDao.decreaseRedPacketForVersion(redPacketId,
                        redPacket.getVersion());
                //update影响记录数 0：更新失败(其他线程修改过version,抢红包失败)， 1：更新成功
                if (update == 0) {
                    continue;
                }

                UserRedPacket userRedPacket = new UserRedPacket();
                userRedPacket.setRedPacketId(redPacketId);
                userRedPacket.setUserId(userId);
                userRedPacket.setAmount(redPacket.getUnitAmount());
                userRedPacket.setNote("抢红包" + redPacketId);

                //插入抢红包信息
                int result = userRedPacketDao.grabRedPacket(userRedPacket);
                return result;
            }
            //失败返回
            return FAILED;
        }
    }

    //=============================乐观锁机制(重入-时间戳-end)=============================


    //=============================乐观锁机制(重入-重试次数-start)==========================
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED)
    public int grabRedPacketForVersion_times(Long redPacketId, Long userId) {

        for (int i = 0; i < 3; i++) {
            //获取红包信息，注意Version值   getRedPacket(乐观锁不需要加行更新锁)
            RedPacket redPacket = redPacketDao.getRedPacket(redPacketId);

            //当前红包库存 > 0
            if (redPacket.getStock() > 0) {
                //再次传入线程保存的version旧值给SQL判断(通过起初读取的version更新库存和版本号)
                // 是否有其他线程修改过数据
                int update = redPacketDao.decreaseRedPacketForVersion(redPacketId,
                        redPacket.getVersion());
                //update影响记录数 0：更新失败(其他线程修改过version,抢红包失败)， 1：更新成功
                if (update == 0) {
                    continue;
                }

                UserRedPacket userRedPacket = new UserRedPacket();
                userRedPacket.setRedPacketId(redPacketId);
                userRedPacket.setUserId(userId);
                userRedPacket.setAmount(redPacket.getUnitAmount());
                userRedPacket.setNote("抢红包" + redPacketId);

                //插入抢红包信息
                int result = userRedPacketDao.grabRedPacket(userRedPacket);
                return result;
            }
        }
        //失败返回
        return FAILED;
    }
}
//=============================乐观锁机制(重入-重试次数-end)==========================

