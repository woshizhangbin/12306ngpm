package org.ng12306.tpms;

// 标准票池服务器需要实现的接口
public interface ITpServer {
    // 启动服务器
    void start() throws Exception;

    // 关闭停止服务器
    void stop() throws Exception;
}
