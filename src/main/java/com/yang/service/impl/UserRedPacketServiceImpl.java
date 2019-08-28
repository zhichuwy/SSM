package com.yang.service.impl;

import com.yang.dao.RedPacketDao;
import com.yang.dao.UserRedPacketDao;
import com.yang.pojo.RedPacket;
import com.yang.pojo.UserRedPacket;
import com.yang.service.RedPacketService;
import com.yang.service.RedisRedPacketService;
import com.yang.service.UserRedPacketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

@Service
public class UserRedPacketServiceImpl implements UserRedPacketService {

    @Autowired
    private RedPacketDao redPacketDao = null;

    @Autowired
    private UserRedPacketDao userRedPacketDao = null;


    private static final int FAILED = 0;

    //=============================Redis==========================
    @Autowired
    private RedisTemplate redisTemplate = null;
    @Autowired
    private RedisRedPacketService redisRedPacketService = null;

    //Lua脚本
    String script = "local listKey = 'red_packet_list_'..KEYS[1] \n" +
            "local redPacket = 'red_packet_'..KEYS[1] \n" +
            "local stock = tonumber(redis.call('hget',redPacket,'stock')) \n" +
            "if stock <= 0 then return 0 end \n" +
            "stock = stock - 1 \n" +
            "redis.call('hset',redPacket,'stock',tostring(stock)) \n" +
            "redis.call('rpush',listKey,ARGV[1]) \n" +
            "if stock == 0 then return 2 end \n" +
            "return 1 \n";

    //缓存Lua脚本后，保存Redis返回的32位SHA1编码，使用SHA1编码执行缓存的Lua脚本
    String sha1 = null;

    //=============================Redis==========================


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
    //=============================乐观锁机制(重入-重试次数-end)==========================

    //=============================Redis-start==========================

    @Override
    public Long grabRedPacketByRedis(Long redPacketId, Long userId) {

        //当前抢红包用户和日期信息
        String args = userId + "-" + System.currentTimeMillis();
        Long result = null;

        //获取底层redis操作对象
        Jedis jedis = (Jedis) redisTemplate.getConnectionFactory().getConnection().getNativeConnection();

        try {
            //加载脚本，得到SHA1
            if (sha1 == null) {
                sha1 = jedis.scriptLoad(script);
            }

            //执行脚本
            Object res = jedis.evalsha(sha1, 1, redPacketId + "", args);

            result = (Long) res;


            //hset red_packet_5 stock 1000
            //hset red_packet_5 unit_amount 10
            //返回2，最后一个红包  异步保存抢红包信息到DB
            if (result == 2) {
                String unitAmountStr = jedis.hget("red_packet_" + redPacketId, "unit_amount");
                //触发保存数据库操作
                Double unitAmount = Double.parseDouble(unitAmountStr);
                System.err.println("thread_name = " + Thread.currentThread().getName());
                redisRedPacketService.saveUserRedPacketByRedis(redPacketId, unitAmount);
            }
        } finally {
            //关闭jedis
            if (jedis != null && jedis.isConnected()) {
                jedis.close();
            }
        }

        return result;
    }

    //=============================Redis-end==========================
}

