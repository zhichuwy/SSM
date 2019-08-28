package com.yang.service.impl;

import com.yang.pojo.UserRedPacket;
import com.yang.service.RedisRedPacketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Service
public class RedisRedPacketServiceImpl implements RedisRedPacketService {

    private static final String PREFIX = "red_packet_list_";
    //每次取出200条，避免一次取出消耗太多内存
    private static final int TIME_SIZE = 200;

    @Autowired
    private RedisTemplate redisTemplate = null;

    @Autowired
    private DataSource dataSource = null;


    @Override
    @Async
    public void saveUserRedPacketByRedis(Long redPacketId, Double unitAmount) {
        System.err.println("开始保存数据");
        Long start = System.currentTimeMillis();
        //获取列表操作对象
        BoundListOperations ops = redisTemplate.boundListOps(PREFIX + redPacketId);
        Long size = ops.size();
        Long times = size % TIME_SIZE == 0 ? size / TIME_SIZE : size / TIME_SIZE + 1;

        int count = 0;

        List<UserRedPacket> userRedPacketList = new ArrayList<>(TIME_SIZE);
        for (int i = 0; i < times; i++) {
            //获取至多TIME_SIZE个抢红包信息
            List userIdList = null;
            if (i == 0) {
                userIdList = ops.range(i * TIME_SIZE, (i + 1) * TIME_SIZE);
            } else {
                userIdList = ops.range(i * TIME_SIZE + 1, (i + 1) * TIME_SIZE);
            }
            userRedPacketList.clear();

            //保存抢红包信息
            for (int j = 0; j < userIdList.size(); j++) {
                String args = userIdList.get(j).toString();
                String[] arr = args.split("-");//UserRedPacketServiceImpl：grabRedPacket时自定义传递格式
                String userIdStr = arr[0];
                String timeStr = arr[1];
                Long userId = Long.parseLong(userIdStr);
                Long time = Long.parseLong(timeStr);

                //生成抢红包信息
                UserRedPacket userRedPacket = new UserRedPacket();
                userRedPacket.setRedPacketId(redPacketId);
                userRedPacket.setUserId(userId);
                userRedPacket.setAmount(unitAmount);
                userRedPacket.setGrabTime(new Timestamp(time));
                userRedPacket.setNote("抢红包" + redPacketId);
                userRedPacketList.add(userRedPacket);
            }
            //插入抢红包信息
            count += executeBatch(userRedPacketList);
        }

        //删除Redis列表
        redisTemplate.delete(PREFIX + redPacketId);
        Long end = System.currentTimeMillis();
        System.err.println("保存数据结束，耗时：" + (end - start) + "ms，共保存" + count + "条记录");

    }


    /**
     * 使用JDBC批量处理Redis缓存数据
     *
     * @param userRedPacketList --抢红包列表
     * @return --抢红包插入数量
     */
    private int executeBatch(List<UserRedPacket> userRedPacketList) {

        Connection connection = null;
        Statement statement = null;
        int[] count;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            statement = connection.createStatement();

            for (UserRedPacket userRedPacket : userRedPacketList) {
                String sql1 = "update t_red_packet set stock=stock-1 where id=" + userRedPacket.getRedPacketId();

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                String sql2 = "insert into t_user_red_packet(red_packet_id,user_id,amount,grab_time,note) values ("
                        + userRedPacket.getRedPacketId() + ","
                        + userRedPacket.getUserId() + ","
                        + userRedPacket.getAmount() + ","
                        + "'" + dateFormat.format(userRedPacket.getGrabTime()) + "',"
                        + "'" + userRedPacket.getNote() + "'"
                        + ")";

                statement.addBatch(sql1);
                statement.addBatch(sql2);
            }
            //执行批量处理
            count = statement.executeBatch();//返回值int[]

            //提交事务
            connection.commit();

        } catch (SQLException e) {
            throw new RuntimeException("抢红包批量处理执行程序错误");
            //e.printStackTrace();
        } finally {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return count.length / 2;//update insert 批量处理
    }
}
