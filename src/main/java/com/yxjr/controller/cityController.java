package com.yxjr.controller;

import com.yxjr.common.UploadCommon;
import com.yxjr.common.UploadConfig;
import com.yxjr.domian.ServerConfig;
import com.yxjr.domian.UploadPackage;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Controller
@RequestMapping("/")
public class cityController {

    @Autowired
    UploadConfig uploadConfig;
    //日志
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @GetMapping()
    String city() {
        return "index";
    }

    /**
     * 获取所有日志列表
     */
    @PostMapping("/getLogList")
    @ResponseBody
    public JSONObject getLogList(){
        String pathName=System.getProperty("user.dir")+uploadConfig.getLogPath();//读取日志存放位置
        File file=new File(pathName);
        JSONObject res = new JSONObject();
        try {
            File[] tempfile_list=file.listFiles();
            if(tempfile_list.length==0){
                res.put("code",-1);
                res.put("msg","无日志信息");
                return res;
            }
//            InetAddress address = InetAddress.getLocalHost();   //获取的是本地的IP地址 //PC-20140317PXKX/192.168.0.121
//            String hostAddress = address.getHostAddress();      //192.168.0.121
            //获取下载的ip和端口
            UploadCommon uploadCommon=new UploadCommon();
            ServerConfig serverConfig=uploadCommon.getYmlServer();
            String ip=serverConfig.getIp();
            String port=serverConfig.getPort();
            JSONObject logRes = new JSONObject();
            for(File devic:tempfile_list){
                if(devic.isDirectory()){                        // isDirectory判断是否是文件夹 isfile判断是否是文件
                    String Code=devic.getName();
                    File[] log_list=devic.listFiles();
                    if(log_list.length==0){
                        continue;
                    }
                     List<Map<String, String>> logList = new ArrayList<Map<String, String>>();
                    Map<String,String> deviceLog=new HashMap<>();
                    for(File log:log_list){
                        String logName=log.getName();
                        String url="http://"+ip+":"+port+"/download?fileFullName="+pathName+"/"+Code+"/"+logName;
                        deviceLog.put("logName",logName);
                        deviceLog.put("url",url);
                        logList.add(deviceLog);
                    }
                    logRes.put(Code,logList);
                }
            }
            res.put("code",200);
            res.put("msg","查询成功");
            res.put("date",logRes);
            System.out.println(res);
            return res;
        }catch (Exception e){
            System.out.println(e);
            res.put("code",-1);
            res.put("msg",e);
            return res;
        }
    }



    /**
     * 获取对应设备的日志列表
     * @param
     */
    @PostMapping("/getDevLogList")
    @ResponseBody
    public JSONObject getDevLogList(String devId){
        String pathName=uploadConfig.getLogPath()+devId;
        File file=new File(pathName);//读取日志存放位置
        JSONObject res = new JSONObject();
        try {
            File[] tempfile_list=file.listFiles();
            if(tempfile_list.length==0){
                res.put("code",-1);
                res.put("msg","无日志信息");
                return res;
            }
            //获取下载的ip和端口
            UploadCommon uploadCommon=new UploadCommon();
            ServerConfig serverConfig=uploadCommon.getYmlServer();
            String ip=serverConfig.getIp();
            String port=serverConfig.getPort();
            List<Map<String,String>> logList = new ArrayList<Map<String,String>>();
            for(File devic:tempfile_list){
                if(devic.isFile()){// isDirectory判断是否是文件夹 isfile判断是否是文件
                    Map<String,String> log=new HashMap<>();
                    String logName=devic.getName();
                    String url="http://"+ip+":"+port+"/download?fileFullName="+pathName+"/"+logName;
                    log.put("logName",logName);
                    log.put("url",url);
                    logList.add(log);
                }
            }
            res.put("code",200);
            res.put("msg","查询成功");
            res.put("date",logList);
            System.out.println(res);
            return res;
        }catch (Exception e){
            System.out.println(e);
            res.put("code",-1);
            res.put("msg",e);
            return res;
        }
    }

    /**
     * 上传日志
     * @param file
     * @param request
     * @return
     */
    @PostMapping("/uploadLog")
    @ResponseBody
    public JSONObject uploadLog(MultipartFile file ,String devId, HttpServletRequest request) {
        JSONObject res = new JSONObject();
        res.put("code",1);
        try {
            logger.info("file的文件名为："+file.getOriginalFilename()+",社保编号为："+devId);
            String pathString = null;
            if(devId==null || devId==""){
                res.put("code",-1);
                res.put("msg","未收到设备编号");
                return res;
            }
            String packPath=uploadConfig.getLogPath()+devId;//上传路径
            String fileName=file.getOriginalFilename();//文件名
            if(file!=null) {
                ///home/weblogic/yxjrApp
                pathString = packPath+"/" + fileName;//new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "_" +
            }else{
                res.put("code",-1);
                res.put("msg","未收到上传日志");
                return res;
            }
            //包创建
            File pack=new File(packPath);
            if(!pack.getParentFile().exists()){
                pack.getParentFile().mkdirs();
            }
            //文件创建
            File files=new File(pathString);
            //打印查看上传路径
            logger.info("pathString为："+pathString);
            if(!files.getParentFile().exists()){
                files.getParentFile().mkdirs();
            }
            file.transferTo(files);
            return res;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            res.put("code",-2);
            res.put("msg","上传文件失败");
            return res;
        }
    }




    /**
     * 上传更新包
     * @param file
     * @param request
     * @return
     */
    @PostMapping("/upload")
    @ResponseBody
    public JSONObject uploadSource(MultipartFile file , HttpServletRequest request) {
        JSONObject res = new JSONObject();
        logger.info("开始文件上传");
        try {
            res.put("code",1);
            String pathString = null;
            UploadCommon updateCommon=new UploadCommon();
            String fileName=file.getOriginalFilename();//文件名
            logger.info("文件名为["+fileName+"]");
            if(file!=null) {
//                pathString ="/home/weblogic/yxjrApp"+ uploadConfig.getUploadpacketPath() + fileName;
                pathString =uploadConfig.getUploadpacketPath() + fileName;
            }else{
                logger.error("未接受上传文件");
                res.put("code",-1);
                res.put("msg","未收到上传文件");
                return res;
            }
            //匹配上传的文件格式
            String pattern = "[0-9]+\\.[0-9]+\\.[0-9]+\\.exe";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(fileName);
            System.out.println(m.matches());
            if( !m.matches()){
                logger.error("文件名的格式不对，文件名：fileName：["+fileName+"]");
                res.put("code",-1);
                res.put("msg","文件名的格式不对,其中包含文字或其他，文件名："+fileName);
                return res;
            }

            logger.info("开始修改文件更新路径和版本");
            File files = new File(pathString);
            //打印查看上传路径
            logger.info("上次路径为："+pathString);
            //更新yml的包路径和地址
            UploadPackage update = new UploadPackage();
            String url = pathString;
            update.setUrl(url);
            update.setCityCode("440000");       //默认全省，后续修改
            update.setPackageName(fileName);    //文件名
            update.setVersion(fileName.substring(0,fileName.lastIndexOf("."))); //版本号
            int ret=updateCommon.updateYamlFile(update);
            if(ret<0){
                logger.error("文件上传失败");
                res.put("code",-2);
                res.put("msg","上传文件失败");
                return res;
            }
            if(!files.getParentFile().exists()){
                files.getParentFile().mkdirs();
            }
            FileUtils.copyInputStreamToFile(file.getInputStream(), files);
//            file.transferTo(files);
            logger.info("文件上传成功");
            return res;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.error("上传文件失败"+e);
            res.put("code",-2);
            res.put("msg","上传文件失败+"+e);
            res.put("uploadConfig",this.uploadConfig);
            res.put("getUploadpacketPath",uploadConfig.getUploadpacketPath());
            return res;
        }
    }





    //保存更新
    @ResponseBody
    @PostMapping("/save")
    public int save(UploadPackage update){
        UploadCommon updateCommon=new UploadCommon();
        try {
            int ret=updateCommon.updateYamlFile(update);
            if(ret>0){
              return 1;
            }
            return 0;
        }catch (Exception e){
            int coad = -1;
            String msg = "上传更新包版本号冲突！";
            return 0;
        }
    }

    /**
     * 获取版本号  ：目前默认为获取广东全省，后续需要分区再修改
     * @return
     */
    @ResponseBody
    @GetMapping("/queryVersion")
    public JSONObject getVersion(){
        logger.info("机器获取版本号");
        UploadCommon uploadCommon=new UploadCommon();
        JSONObject res = new JSONObject();
        try {
            UploadPackage uploadPackage =uploadCommon.getYmlFile("440000");//广东全省
//            InetAddress address = InetAddress.getLocalHost();//获取的是本地的IP地址 //PC-20140317PXKX/192.168.0.121
//            String hostAddress = address.getHostAddress();//192.168.0.121
            ServerConfig serverConfig=uploadCommon.getYmlServer();
            String ip=serverConfig.getIp();
            String port=serverConfig.getPort();
            uploadPackage.setUrl("http://"+ip+":"+ port+"/download/App_v"+uploadPackage.getVersion()+".exe");
            res.put("AppVersion",uploadPackage.getVersion());
            res.put("SPVersion","1.0.0");
            res.put("SpDownLoadFile","http://"+ip+":"+ port+"/download/SP_v1.0.0");
            res.put("AppDownLoadFile",uploadPackage.getUrl());
            logger.info("机器获取版本号和下载地址成功,AppVersion:["+uploadPackage.getVersion()+"],AppDownLoadFile:["+uploadPackage.getUrl()+"]");
            return res;
        }catch (Exception e){
            logger.error("获取版本出错，错误原因:"+e);
            res.put("retCode",-1);
            return res;
        }
    }

    /**
     * 更新包下载
     *
     * @param request
     * @param response
     * @return
     * @throws UnsupportedEncodingException
     */
    @ResponseBody
    @RequestMapping("/download/{fileFullName}")
    public String downloadFile(HttpServletRequest request,
                          HttpServletResponse response, String fileFullName) throws UnsupportedEncodingException {
        logger.info("设备开始下载更新包");
        UploadCommon uploadCommon=new UploadCommon();
        UploadPackage uploadPackage=uploadCommon.getYmlFile("440000");//默认广东全省
        String FullPath =  uploadPackage.getUrl();//将文件的统一储存路径和文件名拼接得到文件全路径
        File packetFile = new File(FullPath);
        String fileName = packetFile.getName(); //下载的文件名
        File file = new File(FullPath);
        // 如果文件名存在，则进行下载
        if (file.exists()) {
            // 配置文件下载
            response.setHeader("content-type", "application/octet-stream");
            response.setContentType("application/octet-stream");
            // 下载文件能正常显示中文
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            // 实现文件下载
            byte[] buffer = new byte[1024];
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            try {
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                OutputStream os = response.getOutputStream();
                int i = bis.read(buffer);
                while (i != -1) {
                    os.write(buffer, 0, i);
                    i = bis.read(buffer);
                }
                logger.info("下载更新包成功");
            } catch (Exception e) {
                logger.error("下载更新包失败"+e);
            } finally {
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {//对应文件不存在
            logger.error("设备想要下载的更新包不存在");
            String s = "{\"code\":-1,\"msg\":\"文件不存在\"}";
            return s;
        }
        return null;
    }

    /**
     * 日志下载
     *
     * @param request
     * @param response
     * @return
     * @throws UnsupportedEncodingException
     */
    @RequestMapping("/download")
    public String downloadLog(HttpServletRequest request,
                               HttpServletResponse response, String fileFullName) throws UnsupportedEncodingException {
        logger.info("下载日志");
        String FullPath = fileFullName;
        File packetFile = new File(FullPath);
        String fileName = packetFile.getName(); //下载的文件名
        File file = new File(FullPath);
        // 如果文件名存在，则进行下载
        if (file.exists()) {
            // 配置文件下载
            response.setHeader("content-type", "application/octet-stream");
            response.setContentType("application/octet-stream");
            // 下载文件能正常显示中文
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            // 实现文件下载
            byte[] buffer = new byte[1024];
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            try {
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                OutputStream os = response.getOutputStream();
                int i = bis.read(buffer);
                while (i != -1) {
                    os.write(buffer, 0, i);
                    i = bis.read(buffer);
                }
                logger.info("下载更新包成功");
                System.out.println("下载更新包成功！");
            } catch (Exception e) {
                logger.error("下载更新包失败");
                System.out.println("下载更新包失败!");
            } finally {
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {//对应文件不存在
            logger.error("设备想要下载的日志不存在");
            String s = "{\"code\":-1,\"msg\":\"文件不存在\"}";
            return s;
        }
        return null;
    }

}
