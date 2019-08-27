<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!<!doctype html>
<!<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
    <title>参数</title>
    <!-- 加载Query文件 -->
    <script type="text/javascript" src="https://code.jquery.com/jquery-3.2.0.js"></script>

    <script type="text/javascript">
        $(document).ready(function () {
            // 模拟20000个异步请求，进行并发
            var max = 20000;
            for (var i = 1; i <= max; i++) {
                //jQuery的post请求，异步请求
                $.post({

                    // 读已提交
                    // url: "./userRedPacket/grabRedPacket.do?redPacketId=1&userId=" + i,


                    // 读已提交 悲观控制机制
                    //url: "./userRedPacket/grabRedPacket.do?redPacketId=2&userId=" + i,


                    // 读已提交 乐观控制机制
                    //url: "./userRedPacket/grabRedPacketForVersion.do?redPacketId=3&userId=" + i,


                    // 读已提交 乐观控制机制-重入-限制3次
                    url: "./userRedPacket/grabRedPacketForVersion_times.do?redPacketId=4&userId=" + i,

                    success: function (result) {

                    }
                });
            }
        })
    </script>
</head>
<body>

</body>
</html>