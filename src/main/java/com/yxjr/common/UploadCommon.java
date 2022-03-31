package com.yxjr.common;


import com.yxjr.domian.ServerConfig;
import com.yxjr.domian.UploadPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.Map;


public class UploadCommon {

    //日志
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public ServerConfig getYmlServer(){
        logger.info("下载");
        ServerConfig serverConfig=new ServerConfig();
//        String src =File.separator+"home"+File.separator+"weblogic"+File.separator+"yxjrApp"+File.separator+"config.yml";
        String src ="src\\main\\resources\\config.yml";
        // linux环境"/home/weblogic/yxjrApp/config.yml" 开发环境"src\main\resources\config.yml"
        Yaml yaml = new Yaml();
        //层级map变量
        Map<String, Object> resultMap;
        try {
            //读取yaml文件，默认返回根目录结构
            resultMap = (Map<String, Object>) yaml.load(new FileInputStream(new File(src)));
            //get出所有城市节点数据
            String ip =  resultMap.get("serverIp").toString();
            String port= resultMap.get("serverPort").toString();
            //get出对应城市节点数据
            serverConfig.setIp(ip);
            serverConfig.setPort(port);
            logger.info("获取成功");
            return serverConfig;
        }catch (Exception e){
            logger.error("获取失败"+e);
            e.printStackTrace();
           return serverConfig;
        }
    }

    public UploadPackage getYmlFile(String cityCode){
        logger.info("获取更新包的路径和更新包名，版本：["+cityCode+"]");
        UploadPackage uploadPackage =new UploadPackage();
        // linux环境"/home/weblogic/yxjrApp/config.yml" 开发环境"src\main\resources\config.yml"
//        String configYml=File.separator+"home"+File.separator+"weblogic"+File.separator+"yxjrApp"+File.separator+"config.yml";
        String configYml="src\\main\\resources\\config.yml";
        Yaml yaml = new Yaml();
        //层级map变量
        Map<String, Object> cityMap,updateMap,resultMap;
        try {
            //读取yaml文件，默认返回根目录结构
            resultMap = (Map<String, Object>) yaml.load(new FileInputStream(new File(configYml)));
            //get出所有城市节点数据
            cityMap = (Map<String, Object>) resultMap.get("city");
            //get出对应城市节点数据
            String cityCodeA = "A_" +cityCode;
            updateMap = (Map<String, Object>) cityMap.get(cityCodeA);
            //更新包名
            uploadPackage.setPackageName(updateMap.get("packageName").toString());
            //城市名
            uploadPackage.setName(updateMap.get("name").toString());
            //城市代码
            uploadPackage.setCityCode(cityCode);
            //版本
            uploadPackage.setVersion(updateMap.get("version").toString());
            //地址
            uploadPackage.setUrl(updateMap.get("url").toString());
            logger.info("获取更新包路径和版本号成功：["+String.valueOf(uploadPackage)+"]");
            return uploadPackage;
        }catch (Exception e){
            logger.error("获取更新包路径和版本失败");
            e.printStackTrace();
            return uploadPackage;
        }
    }

    //更新城市对应的版本，更新包路径，更新包名
    public int updateYamlFile(UploadPackage uploadPackage) {
        logger.info("修改更新包的路径和更新包名，版本：["+String.valueOf(uploadPackage)+"]");
         //linux环境"/home/weblogic/yxjrApp/config.yml" 开发环境"src\main\resources\config.yml"
//        String configYml=File.separator+"home"+File.separator+"weblogic"+File.separator+"yxjrApp"+File.separator+"config.yml";
        String configYml="src\\main\\resources\\config.yml";
        Yaml yaml = new Yaml();
        FileWriter fileWriter = null;
        //层级map变量
        Map<String, Object> cityMap,updateMap,resultMap;
        try {
            //读取yaml文件，默认返回根目录结构
            resultMap = (Map<String, Object>) yaml.load(new FileInputStream(new File(configYml)));
            //get出所有城市节点数据
            cityMap = (Map<String, Object>) resultMap.get("city");
            //get出对应城市节点数据
            String cityCodeA="A_"+ uploadPackage.getCityCode();
            updateMap = (Map<String, Object>) cityMap.get(cityCodeA);
            //修改对应城市的包名，版本，url地址
            updateMap.put("version", uploadPackage.getVersion());
            updateMap.put("packageName", uploadPackage.getPackageName());
            updateMap.put("url", uploadPackage.getUrl());
            //字符输出
            fileWriter = new FileWriter(new File(configYml));
            //用yaml方法把map结构格式化为yaml文件结构
            fileWriter.write(yaml.dumpAsMap(resultMap));
            //刷新
            fileWriter.flush();
            //关闭流
            fileWriter.close();
            logger.info("修改成功");
            return 1;
        } catch (Exception e) {
            logger.error("修改失败"+e);
            e.printStackTrace();
            return -1;
        }
    }
}
