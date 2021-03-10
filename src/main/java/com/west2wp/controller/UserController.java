package com.west2wp.controller;


import com.alibaba.fastjson.JSONObject;
import com.west2wp.config.JWTInterceptor;
import com.west2wp.pojo.FileData;
import com.west2wp.pojo.User;
import com.west2wp.service.MailService;
import com.west2wp.service.UserService;
import com.west2wp.utils.JWTUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


@RestController
public class UserController {

    private UserService userService;

    private MailService mailService;

    private JWTUtils jwtUtils;

    //redis数据库一直出bug,改用线程安全的ConcurrentHashMap存邮箱验证时用户名验证码对
    private final ConcurrentHashMap<String,String> verificationCodeMap = new ConcurrentHashMap<>();

    //日志,一开始没用注解,后面懒得改了~
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setJwtUtils(JWTUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Autowired
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    //用户注册接口
    @RequestMapping("/register")
    public JSONObject register(@RequestParam("username") String username, @RequestParam("password") String password,@RequestParam("email") String email){
        logger.info("----正在注册,用户名:" + username + " 密码:" + password + " 邮箱:" + email);
        JSONObject jsonObject = new JSONObject();
        User user = new User(username,password,email,null,1024);
        //根据用户名读取数据库中信息,若用户名已存在返回null
        User user1 = userService.register(user);
        if(user1 != null){
            logger.info("----注册成功----");
            logger.info("注册用户:" + user1.toString());
            jsonObject.put("RegisterStatus",true);
        }else{
            logger.info("----注册失败----");
            jsonObject.put("RegisterStatus",false);
            return jsonObject;
        }
        return jsonObject;
    }

    //用户登录接口
    @RequestMapping("/login")
    public JSONObject login(@RequestParam("username") String username, @RequestParam("password") String password){
        JSONObject jsonObject = new JSONObject();
        User user = new User(username,password,null,null,0);
        logger.info("正在尝试登录用户:" + username);
        if(userService.login(user)){
            //创建token字符串
            String token = jwtUtils.createToken(username,password);
            //把token存入哈希表
            JWTInterceptor.tokenCodeMap.put(username,token);
            logger.info("登录成功,用户:" + username);
            jsonObject.put("LoginStatus",true);
            jsonObject.put("token",token);
        }else {
            //用户名或者密码错误
            logger.warn("登录失败,用户名或密码错误,username:" + username + " password:" + password);
            jsonObject.put("LoginStatus",false);
            return jsonObject;
        }
        User user1 = userService.getUser(username);
        jsonObject.put("user",user1);
        return jsonObject;
    }

    //用户注销接口
    @RequestMapping("/logout")
    public String logout(@RequestParam("username") String username){
        //删除对应的token
        logger.info("----正在注销用户:" + username + "----");
        JWTInterceptor.tokenCodeMap.remove(username);
        logger.info("---注销成功!---");
        return "success";
    }

    //修改密码时发送验证码接口
    @RequestMapping("/sendEmail")
    public JSONObject sendEmail(@RequestParam("username") String username){
        JSONObject jsonObject = new JSONObject();
        User user = userService.getUser(username);
        if(user == null){
            //如果不存在用户返回用户错误
            jsonObject.put("SendEmailStatus","UserWrong");
            return jsonObject;
        }
        String randomString = UUID.randomUUID().toString();
        //5位随机验证码
        String yzm = randomString.substring(0,5);
        //同一个用户多次申请,根据哈希表原理,会覆盖原来的验证码
        verificationCodeMap.put(username,yzm);
        //发送验证码
        mailService.sendEmail(user.getEmail(),yzm);
        jsonObject.put("SendEmailStatus","success");
        return jsonObject;
    }

    //修改密码接口
    @RequestMapping("/changePassword")
    public JSONObject changePassword(@RequestParam("username")String username,@RequestParam("password") String password,@RequestParam("newPassword") String newPassword,@RequestParam("yzm") String yzm){
        JSONObject jsonObject = new JSONObject();
        logger.info("-----正在尝试更改密码,用户:" + username + "----");
        User user = userService.getUser(username);
        if(user == null){
            //用户不存在
            jsonObject.put("ChangePasswordStatus","UserWrong");
            logger.warn("-----要修改密码的用户不存在,用户:" + username + "----");
            return jsonObject;
        }
        if(!user.getPassword().equals(password)){
            //用户输入的密码和和原密码不一致
            jsonObject.put("ChangePasswordStatus","PasswordWrong");
            logger.warn("----输入密码和原密码不一致,用户:" + username + " 原密码:" + user.getPassword() + " 输入密码:" + password);
            return jsonObject;
        }
        //判断验证码是否正确
        if(verificationCodeMap.get(username) != null){
            if(verificationCodeMap.get(username).equals(yzm)){
                //已发送验证码且验证码正确
                logger.info("------验证码正确,用户:" + username + "----");
                //删去对应验证码键值对(每个验证码只能用一次)
                verificationCodeMap.remove(username);
            }else{
                //验证码错误
                logger.warn("------验证码错误,用户:" + username + " 原验证码:" + verificationCodeMap.get(username) + " 验证码:" + yzm);
                jsonObject.put("ChangePasswordStatus","YzmWrong");
                return jsonObject;
            }
        }else{
            //验证码不存在
            jsonObject.put("ChangePasswordStatus","YzmWrong");
            logger.warn("------验证码不存在,用户:" + username + "-----");
            return jsonObject;
        }
        //验证码正确后,修改数据库中对应用户的密码
        userService.changePassword(username,newPassword);
        logger.info("-----修改密码成功!用户:" + username + " 原密码:" + password + " 新密码:" + newPassword);
        jsonObject.put("ChangePasswordStatus","success");
        return jsonObject;
    }

    //上传文件方法(单个文件,文件类型不限)
    @RequestMapping("/upload")
    public JSONObject fileUpload(@RequestParam("file") MultipartFile file,@RequestParam("username") String username,@RequestParam("parentFile") String parentFile){
        String username1 = username.substring(1,username.length() - 1);
        String parentFile1 = parentFile.substring(1,parentFile.length() - 1);
        logger.info("用户:" + username1 + " 正在上传文件");
        long currentTime1 = System.currentTimeMillis();
        JSONObject jsonObject = new JSONObject();
        if(file.isEmpty()){
            //空文件返回失败
            logger.warn("------上传文件失败:文件为空------");
            jsonObject.put("UploadStatus","EmptyWrong");
            jsonObject.put("UploadCostTime",-1);
            return jsonObject;
        }
        //获取文件大小:单位kb
        double size = (double)file.getSize()/1024;
        logger.info("------当前文件大小:" + size + "KB-----");
        //剩余云盘空间大小
        double remainSpace = userService.judgeSpace(username1,size);
        if(remainSpace <= 0){
            //云盘存不下或者用户名有误,报错
            jsonObject.put("UploadStatus","FullOrUserFWrong");
            jsonObject.put("UploadCostTime",-1);
            return jsonObject;
        }
        //获取当前系统时间
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = df.format(System.currentTimeMillis());
        //获取文件名
        String fileName = file.getOriginalFilename();
        if(fileName == null){
            logger.warn("-----上传文件失败:文件名为空------");
            jsonObject.put("UploadStatus","FileNameWrong");
            jsonObject.put("UploadCostTime",-1);
            return jsonObject;
        }
        //获取后缀
        String suffixName = fileName.substring(fileName.lastIndexOf(".") + 1);
        //对文件格式进行限制,暂时支持以下文件上传,同时在application.properties中限制单个文件上传上限200MB
        if(!(suffixName.equals("jpg")||suffixName.equals("jpeg")||suffixName.equals("mp3") || suffixName.equals("mp4") || suffixName.equals("png") || suffixName.equals("flac"))){
            logger.info("----文件格式不符合要求,文件格式:" + suffixName + " 大小:" + size + "KB");
            jsonObject.put("UploadStatus","FileTypeWrong");
            jsonObject.put("UploadCostTime",-1);
            return jsonObject;
        }
        //新文件名
        String FileNewName = UUID.randomUUID().toString() + fileName;
        //文件路径
        String url = parentFile1 + "/" + FileNewName;
        logger.info("------新文件url:" + url + "-----");
        //文件数据实例
        FileData fileData = new FileData(url,username1,FileNewName,parentFile1,suffixName,size,currentTime);
        //实际存储地址还是/ck/data/+文件名,但是数据库中存储路径是url
        File newFile = new File("/ck/data/" + FileNewName);
        try{
            //保存文件
            logger.info("------正在尝试上传文件-----");
            file.transferTo(newFile);
            logger.info("-------文件上传成功！------");
        }catch (IOException e){
            e.printStackTrace();
            logger.warn("-----上传文件失败-----");
            jsonObject.put("UploadStatus","InternetWrong");
            jsonObject.put("UploadCostTime",-1);
            return jsonObject;
        }
        long costTime = System.currentTimeMillis() - currentTime1;
        //更新数据库方法:更新用户网盘容量,同时向数据库插入新文件数据
        userService.upload(username1,remainSpace,fileData);
        jsonObject.put("UploadStatus","success");
        //加入上传时间,给前端获取
        jsonObject.put("UploadCostTime",costTime);
        logger.info("-----上传文件成功,用时:" + costTime + "ms-----");
        return jsonObject;
    }

    //下载文件方法
    @RequestMapping("/download")
    public JSONObject download(@RequestParam("username") String username,@RequestParam("url") String url,HttpServletResponse response){
        JSONObject jsonObject = new JSONObject();
        if(userService.judgeUrl(username, url)){
            //判断没有通过说明用户不符或者资源不存在
            jsonObject.put("DownloadStatus","UserWrong");
            return jsonObject;
        }
        logger.info("-----用户名和url验证成功-----");
        //获取文件格式
        //设置文件路径
        File file = new File(url);
        //获取上传前一刻时间
        long currentTime = System.currentTimeMillis();
        if(file.exists()){
            response.setContentType("UTF-8");
            logger.info("-----文件存在-----");
            //设置下载
            //response.setContentType("application/force-download");
            response.setContentType("application/octet-stream");
            response.setContentLength((int)file.length());
            //设置文件名,解决中文乱码问题
            response.setHeader("Content-Disposition","attachment;fileName=" + URLEncoder.encode(url, StandardCharsets.UTF_8));
            byte[] buffer = new byte[1024];
            FileInputStream fileInputStream = null;
            BufferedInputStream bufferedInputStream = null;
            try{
                fileInputStream = new FileInputStream(file);
                bufferedInputStream = new BufferedInputStream(fileInputStream);
                OutputStream outputStream = response.getOutputStream();
                int k = bufferedInputStream.read(buffer);
                logger.info("------正在进行数据传输-------");
                while(k != -1){
                    outputStream.write(buffer,0,k);
                    outputStream.flush();
                    k = bufferedInputStream.read(buffer);
                }
            }catch (Exception e){
                e.printStackTrace();
                logger.warn("-----下载文件出现异常-----");
            }finally {
                if(bufferedInputStream != null){
                    try{
                        bufferedInputStream.close();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
                if(fileInputStream != null){
                    try{
                        fileInputStream.close();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }else{
            logger.warn("----文件不存在----");
            jsonObject.put("DownloadStatus","FileWrong");
        }
        long costTime = System.currentTimeMillis() - currentTime;
        logger.info("-----下载文件成功,用时:" + costTime + "ms-----");
        jsonObject.put("DownloadStatus","success");
        //把下载用时返回给前端显示
        jsonObject.put("DownloadCostTime",costTime);
        return jsonObject;
    }

    //删除文件夹方法,可以一次性删除里面所有文件,同时删除数据库对应文件数据,更新用户对应网盘容量,但是暂时不支持文件夹嵌套文件夹的删除
    @RequestMapping("/deleteFile")
    public JSONObject deleteFile(@RequestParam("username") String username,@RequestParam("url") String url){
        JSONObject jsonObject = new JSONObject();
        if(userService.judgeUrl(username,url)){
            //先判断url是否存在且与用户匹配
            jsonObject.put("DeleteFileStatus","UrlWrong");
            logger.warn("-----文件夹删除失败,url与用户不匹配-----");
            return jsonObject;
        }
        //前面judgeUrl判断过文件是否存在,所以这里不用判断是否为空
        FileData fileData = userService.getFileData(url);
        if(!fileData.getType().equals("wjj")){
            //url对应资源不是文件夹
            jsonObject.put("DeleteFileStatus","FileWrong");
            logger.info("-----文件夹删除失败,url对应资源不是文件夹,url资源:" + fileData.getType() + "-----");
            return jsonObject;
        }
        //获取文件夹内的所有文件内容
        List<FileData> fileDataList = userService.getFileInformation(username,url);
        for(FileData x:fileDataList){
            if(x.getType().equals("wjj")){
                //有文件夹,无法删除,返回FileWrong
                jsonObject.put("DeleteFileStatus","FileWrong");
                return jsonObject;
            }
        }
        logger.info("-------正在开始删除文件夹-------");
        for(FileData x:fileDataList){
            //因为在服务器存储中所有文件都放在一个文件夹里,所以获得文件名
            String fileUrl = "/ck/data/" + x.getFilename();
            File file = new File(fileUrl);
            if(file.exists()){
                if(file.delete()){
                    //删除成功时,释放网盘资源,同时修正用户网盘容量
                    logger.info("-----文件删除成功!用户:" + username +" 服务器url:" + fileUrl + "-----");
                    userService.deleteFile(username,x.getUrl());
                }else{
                    logger.warn("-----文件删除失败,服务器内部错误----");
                    jsonObject.put("DeleteFileStatus","DeleteWrong");
                    return jsonObject;
                }
            }else{
                logger.warn("-----文件不存在删除失败,不存在文件:" + fileUrl + "-----");
                jsonObject.put("DeleteFileStatus","FileWrong");
                return jsonObject;
            }
        }
        //删除完所有内部文件,最后删除文件夹
        userService.deleteFile(username,url);
        logger.info("----删除文件夹成功,用户:" + username + " url:" + url + "-----");
        jsonObject.put("DeleteFileStatus","success");
        return jsonObject;
    }

    //删除普通文件方法,根据url和用户名删除对应文件,同时删除数据库中对应数据,增加用户剩余网盘空间
    @RequestMapping("/delete")
    public JSONObject delete(@RequestParam("username") String username,@RequestParam("url") String url){
        JSONObject jsonObject = new JSONObject();
        if(userService.judgeUrl(username, url)){
            //先判断url是否存在且与用户名匹配
            jsonObject.put("DeleteStatus","UrlWrong");
            logger.warn("-----文件删除失败,url与用户不匹配-----");
            return jsonObject;
        }
        FileData fileData = userService.getFileData(url);
        //服务器内部文件路径
        String fileUrl = "/ck/data/" + fileData.getFilename();
        File file = new File(fileUrl);
        //删除服务器对应文件
        if(file.exists()){
            if(file.delete()){
                logger.info("-----文件删除成功!用户:" + username + " 服务器url:" + fileUrl + "-----");
            }else{
                logger.warn("-----文件删除失败,服务器内部错误----");
                jsonObject.put("DeleteStatus","DeleteWrong");
                return jsonObject;
            }
        }else{
            logger.warn("-----文件不存在删除失败,不存在文件:" + fileUrl + "-----");
            jsonObject.put("DeleteStatus","FileWrong");
            return jsonObject;
        }
        //删除数据库里对应的url,更新用户剩余网盘空间
        userService.deleteFile(username,url);
        logger.info("----删除文件成功,用户:" + username + " url:" + url);
        jsonObject.put("DeleteStatus","success");
        return jsonObject;
    }

    //创建新文件夹的方法
    @RequestMapping("/createNewFile")
    public JSONObject createNewFile(@RequestParam("username") String username,@RequestParam("Filename") String Filename,@RequestParam("parentFile") String parentFile){
        JSONObject jsonObject = new JSONObject();
        if(Filename == null){
            //文件夹名不能为空
            jsonObject.put("createNewFileStatus","FileNameWrong");
            return jsonObject;
        }
        //调用service层创建新文件夹
        String status = userService.createNewFile(username,Filename,parentFile);
        if(status.equals("parentFileNotExit")){
            //文件夹的父级不存在
            jsonObject.put("createNewFileStatus","ParentFileWrong");
            logger.info("-----文件夹父级不存在,文件夹父级:" + parentFile + "-----");
            return jsonObject;
        }else if(status.equals("fileNameRepeat")){
            //文件夹在当前目录下重名
            jsonObject.put("createNewFileStatus","RepeatWrong");
            logger.info("------文件夹重名,创建失败,文件夹位置:" + parentFile + '/' + Filename + "-----");
            return jsonObject;
        }
        //成功创建文件夹
        jsonObject.put("createNewFileStatus","success");
        logger.info("------成功创建新文件夹,文件夹位置:" + parentFile + '/' + Filename + "------");
        return jsonObject;
    }

    //获取当前目录下文件数据,以json数据返回给安卓端
    @RequestMapping("/getFileInformation")
    public JSONObject getFileInformation(@RequestParam("username") String username,@RequestParam("parentFile") String parentFile){
        logger.info("----父级文件:" + parentFile + "-----");
        JSONObject jsonObject = new JSONObject();
        if(!userService.judgeParentFileExist(username,parentFile)){
            jsonObject.put("getFileInformationStatus","ParentFileWrong");
            logger.warn("------获取文件数据失败:父级文件夹不存在----");
            return jsonObject;
        }
        List<FileData> fileDataList = userService.getFileInformation(username,parentFile);
        jsonObject.put("getFileInformationStatus","success");
        //把获得的fileDataList存入json
        jsonObject.put("FileDataList",fileDataList);
        logger.info("----获取文件数据成功,文件夹:" + parentFile + "-----");
        return jsonObject;
    }
    //上传头像方法
    @RequestMapping("/photoUpload")
    public JSONObject photoUpload(@RequestParam("file") MultipartFile file,@RequestParam("username") String username){
        String username1 = username.substring(1,username.length() - 1);
        logger.info("------正在上传头像-------");
        logger.info("username:" + username1);
        JSONObject jsonObject = new JSONObject();
        if(file.isEmpty()){
            //空文件返回失败
            logger.warn("-----文件为空!-----");
            jsonObject.put("PhotoUploadStatus","EmptyWrong");
            return jsonObject;
        }
        //获取文件名
        String fileName = file.getOriginalFilename();
        if(fileName == null){
            logger.warn("-----上传文件失败:文件名为空------");
            jsonObject.put("PhotoUploadStatus","FileNameWrong");
            return jsonObject;
        }
        logger.info("文件名:" + fileName);
        //获取后缀
        String suffixName = fileName.substring(fileName.lastIndexOf(".") + 1);
        logger.info("文件后缀:" + suffixName);
        //上传后文件路径
        String filePath = "/ck/photo/" + fileName;
        logger.info("文件上传后路径:" + filePath);
        //新文件名(UUID防止重复)
        String FileNewName = UUID.randomUUID().toString() + fileName;
        logger.info("新文件名:" + FileNewName);
        if(!(suffixName.equals("jpg") || suffixName.equals("jpeg") || suffixName.equals("png"))){
            //文件格式不符返回失败
            jsonObject.put("PhotoUploadStatus","TypeWrong");
            return jsonObject;
        }
        //封装上传文件位置的全路径
        File photoFile = new File("/ck/photo/" + FileNewName);
        try{
            //保存文件
            file.transferTo(photoFile);
        }catch (IOException e){
            e.printStackTrace();
            logger.warn("-----上传头像失败-----");
            jsonObject.put("PhotoUploadStatus","InternetWrong");
            return jsonObject;
        }
        //保存图片url
        String url = "/ck/photo/" + FileNewName;
        logger.info("图片url:" + url);
        User user = new User(username1,null,null,url,0);
        if(!userService.uploadPhoto(user)){
            jsonObject.put("PhotoUploadStatus","UserWrong");
            return jsonObject;
        }
        jsonObject.put("PhotoUploadStatus","success");
        logger.info("------上传头像成功-----");
        return jsonObject;
    }

    //根据用户名获取头像的url方法
    @RequestMapping("/getPhoto")
    public String getPhoto(@RequestParam("username") String username,HttpServletResponse response){
        logger.info("------正在获取图片-------");
        //根据用户名获取图片url,若没有设置头像返回null
        String url = userService.getPhotoUrl(username);
        if(url != null){
            //设置文件路径
            File file = new File(url);
            if(file.exists()){
                response.setContentType("UTF-8");
                //设置下载
                //response.setContentType("application/force-download");
                response.setContentType("application/octet-stream");
                response.setContentLength((int)file.length());
                //设置文件名,解决中文乱码
                response.setHeader("Content-Disposition","attachment;fileName=" + URLEncoder.encode(url, StandardCharsets.UTF_8));
                byte[] buffer = new byte[1024];
                FileInputStream fileInputStream = null;
                BufferedInputStream bufferedInputStream = null;
                logger.info("------正在尝试获取头像-----");
                try{
                    fileInputStream = new FileInputStream(file);
                    bufferedInputStream = new BufferedInputStream(fileInputStream);
                    OutputStream outputStream = response.getOutputStream();
                    int i = bufferedInputStream.read(buffer);
                    while(i != -1) {
                        outputStream.write(buffer, 0, i);
                        outputStream.flush();
                        i = bufferedInputStream.read(buffer);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    logger.warn("------获取头像失败------");
                }finally {
                    if(bufferedInputStream != null){
                        try{
                            bufferedInputStream.close();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                    if(fileInputStream != null){
                        try{
                            fileInputStream.close();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }
            }else{
                logger.warn("-----url错误-----");
                return "wrong";
            }
        }else{
            logger.warn("----url不存在---");
            return "wrong";
        }
        logger.info("----获取头像成功-----");
        return "success";
    }

    //添加文件进收藏夹方法
    @RequestMapping("/addFavor")
    public JSONObject addFavor(@RequestParam("username") String username,@RequestParam("url") String url){
        JSONObject jsonObject = new JSONObject();
        //添加收藏
        String status = userService.addFavorFile(username,url);
        switch (status) {
            case "notExist":
                //文件不存在
                logger.warn("------添加收藏失败,文件不存在.文件url:" + url + "----");
                jsonObject.put("addFavorStatus", "ExistWrong");
                return jsonObject;
            case "userWrong":
                //文件不属于该用户
                logger.warn("-----添加收藏失败,文件不属于该用户.文件url:" + url + " 用户:" + username + "---");
                jsonObject.put("addFavorStatus", "UserWrong");
                return jsonObject;
            case "fileWrong":
                //文件是文件夹,不能收藏
                logger.warn("----添加收藏失败,文件属于文件夹,不能收藏---");
                jsonObject.put("addFavorStatus", "FileWrong");
                return jsonObject;
            case "repeat":
                //文件重复收藏
                logger.warn("----添加收藏失败,该文件已被收藏,文件url:" + url + "-----");
                jsonObject.put("addFavorStatus", "RepeatWrong");
                return jsonObject;
            default:
                //成功收藏
                logger.info("----成功收藏文件,文件url:" + url + " 用户:" + username + "----");
                jsonObject.put("addFavorStatus", "success");
                break;
        }
        return jsonObject;
    }

    //把文件移除收藏夹方法
    @RequestMapping("/removeFavor")
    public JSONObject removeFavor(@RequestParam("username") String username,@RequestParam("url") String url){
        JSONObject jsonObject = new JSONObject();
        //移除收藏
        String status = userService.removeFavorFile(username,url);
        switch (status) {
            case "notExist":
                //要移除的文件不存在
                logger.warn("-----移除收藏文件失败,该文件不存在,文件url:" + url + "-----");
                jsonObject.put("removeFavorStatus", "ExistWrong");
                return jsonObject;
            case "userWrong":
                //文件不属于该用户
                logger.warn("-----移除收藏文件失败,文件不属于该用户,文件url:" + url + " 用户:" + username + "-----");
                jsonObject.put("removeFavorStatus", "UserWrong");
                return jsonObject;
            case "notFavor":
                //文件并未被收藏
                logger.warn("-----移除收藏文件失败,文件并未被收藏,文件url:" + url + " 用户:" + username + "-----");
                jsonObject.put("removeFavorStatus", "FileWrong");
                return jsonObject;
            default:
                //移除收藏成功
                logger.info("----移除收藏文件成功,文件url:" + url + " 用户:" + username + "-----");
                jsonObject.put("removeFavorStatus", "success");
                break;
        }
        return jsonObject;
    }

    //根据用户名返回用户所有收藏文件
    @RequestMapping("/getFavor")
    public JSONObject getFavor(@RequestParam("username") String username){
        JSONObject jsonObject = new JSONObject();
        //先判断用户是否存在
        User user = userService.getUser(username);
        if(user == null){
            //用户不存在
            logger.warn("----获取用户收藏文件失败,用户不存在,用户:" + username + "---");
            jsonObject.put("getFavorFileStatus","UserWrong");
            return jsonObject;
        }
        //用户存在,获取他所有收藏的文件
        List<FileData> fileDataList = userService.getAllFavorFile(username);
        jsonObject.put("FavorFileList",fileDataList);
        jsonObject.put("getFavorFileStatus","success");
        logger.info("----获取用户收藏文件成功,用户:" + username + "-----");
        logger.info("----用户收藏文件如下:" + fileDataList.toString() + "----");
        return jsonObject;
    }

    //一个小小的测试接口啦~
    @RequestMapping("hello")
    public String hello(@RequestParam("username") String username){
        logger.info("hello" + username);
        logger.info(verificationCodeMap.toString());
        logger.info(JWTInterceptor.tokenCodeMap.toString());
        return "ycy yyds";
    }
}