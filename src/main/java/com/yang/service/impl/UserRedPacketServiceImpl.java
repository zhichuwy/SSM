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

    private static final int FAILED = 0;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED)
    public int grapRedPacket(Long redPacketId, Long userId) {
        //获取红包信息
        RedPacket redPacket = redPacketDao.getRedPacket(redPacketId);

        //当前红包库存 > 0
        if (redPacket.getStock() > 0) {
            redPacketDao.decreaseRedPacket(redPacketId);
            UserRedPacket userRedPacket = new UserRedPacket();
            userRedPacket.setRedPacketId(redPacketId);
            userRedPacket.setUserId(userId);
            userRedPacket.setAmount(redPacket.getUnitAmount());
            userRedPacket.setNote("抢红包" + redPacketId);

            //插入抢红包信息
            int result = userRedPacketDao.grapRedPacket(userRedPacket);
            return result;
        }

        return FAILED;
    }
}
