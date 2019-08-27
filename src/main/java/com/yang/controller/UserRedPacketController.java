package com.yang.controller;

import com.yang.service.UserRedPacketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/userRedPacket")
public class UserRedPacketController {

    @Autowired
    private UserRedPacketService userRedPacketService = null;

    @RequestMapping(value = "/grabRedPacket")
    @ResponseBody
    public Map<String, Object> grabRedPacket(Long redPacketId, Long userId) {
        //抢红包 最初版本 & 悲观锁机制(redPacketDao.getRedPacketForUpdate)
        int result = userRedPacketService.grabRedPacket(redPacketId, userId);
        Map<String, Object> retMap = new HashMap<>();
        boolean flag = result > 0;
        retMap.put("success", flag);
        retMap.put("message", flag ? "抢红包成功" : "抢红包失败");
        return retMap;
    }

    //=========================乐观锁机制============================

    @RequestMapping(value = "/grabRedPacketForVersion")
    @ResponseBody
    public Map<String, Object> grabRedPacketForVersion(Long redPacketId, Long userId) {
        //抢红包 乐观锁机制
        int result = userRedPacketService.grabRedPacketForVersion(redPacketId, userId);
        Map<String, Object> retMap = new HashMap<>();
        boolean flag = result > 0;
        retMap.put("success", flag);
        retMap.put("message", flag ? "抢红包成功" : "抢红包失败");
        return retMap;
    }
    //=========================乐观锁机制============================

    //=============================乐观锁机制(重入-重试次数)==========================
    @RequestMapping(value = "/grabRedPacketForVersion_times")
    @ResponseBody
    public Map<String, Object> grabRedPacketForVersion_times(Long redPacketId, Long userId) {
        //抢红包 乐观锁机制
        int result = userRedPacketService.grabRedPacketForVersion_times(redPacketId, userId);
        Map<String, Object> retMap = new HashMap<>();
        boolean flag = result > 0;
        retMap.put("success", flag);
        retMap.put("message", flag ? "抢红包成功" : "抢红包失败");
        return retMap;
    }

    //=============================乐观锁机制(重入-重试次数)==========================

}
