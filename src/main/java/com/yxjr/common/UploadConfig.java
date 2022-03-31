package com.yxjr.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;


@Configuration
public class UploadConfig {
    //客户端端口号
    @Value(value = "${server.port}")
    private  int port;
    //服务器存放更新包的文件夹路径
    @Value(value ="${uploadpacketPath}" )
    private String uploadpacketPath;
    //日志存放路径
    @Value(value = "${logPath}")
    private String logPath;

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUploadpacketPath() {
        return uploadpacketPath;
    }

    public void setUploadpacketPath(String uploadpacketPath) {
        this.uploadpacketPath = uploadpacketPath;
    }
}
