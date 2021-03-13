package com.west2wp.service;

import com.west2wp.dao.UserMapper;
<<<<<<< HEAD
=======
import com.west2wp.pojo.Feedback;
>>>>>>> 2017e29 (west2wp)
import com.west2wp.pojo.User;
import com.west2wp.pojo.FileData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.text.SimpleDateFormat;
import java.util.List;
<<<<<<< HEAD
=======
import java.util.UUID;
>>>>>>> 2017e29 (west2wp)

@Slf4j
@Service
public class UserServiceImpl implements UserService{

    private UserMapper userMapper;

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }


    @Override
    public User getUser(String username) {
        //根据用户名获取用户信息
        return userMapper.selectByUsername(username);
    }

    @Override
    public FileData getFileData(String url) {
        //根据url获取文件信息
        return userMapper.selectUserDataByUrl(url);
    }

    @Override
    public boolean login(User user) {
        //根据用户名在数据库中查询该用户的信息,如果账号和密码都正确则登录成功,返回登录结果
        User user1 = userMapper.selectByUsername(user.getUsername());
        if(user1 != null){
            return user1.getUsername().equals(user.getUsername()) && user1.getPassword().equals(user.getPassword());
        }
        return false;
    }

    @Override
    public String getPhotoUrl(String username) {
        //根据用户名获取图片的url
        return userMapper.selectUrlByUsername(username);
    }

    @Override
    public String selectUserByUrl(String url) {
        //根据url获取用户名
        return userMapper.selectUserByUrl(url);
    }

    @Override
    public void changePassword(String username, String password) {
        userMapper.updatePassword(username,password);
    }

    @Override
    public boolean uploadPhoto(User user) {
        //上传头像方法,先判断用户是否存在,不存在则无法更新头像返回false,存在则更新头像的url
        User user1 = userMapper.selectByUsername(user.getUsername());
        if(user1 == null){
            return false;
        }
        userMapper.updateUrlByUsername(user);
        return true;
    }

    @Override
    public User register(User user) {
        //注册方法,用户名没有重复则插入数据返回用户信息
        if(userMapper.selectByUsername(user.getUsername()) == null){
            userMapper.saveUser(user);
            //注册成功
            return user;
        }
        return null;
    }

    @Override
    public double judgeSpace(String username, double size) {
        //用户名不存在
        User user = userMapper.selectByUsername(username);
        if(user == null){
            return -1;
        }
        log.info(user.toString());
        //上传文件时判断是否超过容量的方法,返回容量差(单位MB)
        double sizes = userMapper.selectSpaceByUsername(username);
        return sizes - size/1024;
    }

    @Override
    public boolean judgeUrl(String username,String url){
        //根据url在数据库中查询用户名是否属实
        FileData fileData = userMapper.selectUserDataByUrl(url);
        if(fileData == null){
            log.warn("-----url有误-----");
            //url错误,返回false,不让下载
            return true;
        }
        log.info("-----正在验证用户名----");
<<<<<<< HEAD
        log.info(username + fileData.getUsername());
=======
>>>>>>> 2017e29 (west2wp)
        //判断用户名和url所属的用户名是否相符决定是否下载
        return !fileData.getUsername().equals(username);
    }

    @Override
    public void upload(String username, double space, FileData fileData) {
        //上传文件成功时,先更新用户云盘容量
        log.info("------正在更新云盘容量------");
        userMapper.updateSpaceByUsername(space,username);
        log.info("------更新云盘容量成功------");
        //再在数据库中添加对应的文件信息
        userMapper.saveFileData(fileData);
        log.info("-----添加对应文件信息成功----");
    }

    @Override
    public String createNewFile(String username,String Filename,String parent) {
        if(!parent.equals("/ck/data")){
            //在服务器(/ck/data)为根目录,不需要检测是否存在
            //先尝试获取父级文件夹
            FileData fileData = userMapper.getFileDataByParentFile(username,parent,"wjj");
            if(fileData == null){
                //父级文件夹不存在
                log.warn("----父级文件夹不存在,不存在文件夹:" + parent + "----");
                return "parentFileNotExit";
            }
        }
<<<<<<< HEAD
=======
        if(!parent.equals("/ck/data")){
            parent = parent.substring(0,parent.lastIndexOf('/'));
        }
>>>>>>> 2017e29 (west2wp)
        //获取同级文件夹的数据,循环判断新文件夹是否重名
        log.info("-----正在获取同级文件夹数据------");
        List<FileData> fileDataList = userMapper.getSameGradeFileNameByParentFile(username,parent,"wjj");
        log.info("------同级文件夹获取成功-------");
        if(fileDataList != null){
            log.info("-----同级文件夹不为空-----");
            for(FileData x:fileDataList){
<<<<<<< HEAD
                if(x.getUrl().equals(parent + '/' +Filename)){
                    log.warn("------文件夹重名,重名文件夹:" + x.getUrl() + "-----");
=======
                if(x.getFilename().equals(Filename)){
                    log.warn("------文件夹重名,重名文件夹:" + x.getFilename() + "-----");
>>>>>>> 2017e29 (west2wp)
                    return "fileNameRepeat";
                }
            }
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = df.format(System.currentTimeMillis());
        //生成FileData实例,把文件夹数据保存进数据库
<<<<<<< HEAD
        FileData fileData1 = new FileData(parent + '/' + Filename,username,Filename,parent,"wjj",0.0,currentTime);
=======
        FileData fileData1 = new FileData(parent + '/' + Filename + '/' + UUID.randomUUID(),username,Filename,parent,"wjj",0.0,currentTime);
>>>>>>> 2017e29 (west2wp)
        log.info("------正在把文件夹数据保存进数据库-----");
        userMapper.saveFileData(fileData1);
        log.info("------文件夹数据保存成功------");
        return "success";
    }

    @Override
    public boolean judgeParentFileExist(String username,String parentFile) {
        //根据用户名和父级文件路径判断父级文件夹是否存在
<<<<<<< HEAD
        if(parentFile.equals("/ck/data")){
=======
        System.out.println(parentFile);
        if(parentFile.equals("/ck/data")){
            //根目录特判
>>>>>>> 2017e29 (west2wp)
            return true;
        }
        return userMapper.getFileDataByParentFile(username,parentFile,"wjj") != null;
    }

    @Override
    public List<FileData> getFileInformation(String username,String parentFile){
        //根据用户名和父级文件路径获取该目录下所有文件信息
        return userMapper.getSameGradeFileDataByParentFile(username,parentFile);
    }

    @Override
    public List<FileData> getAllFavorFile(String username) {
        return userMapper.getAllFavorDataByUsername(username);
    }

    @Override
    public String addFavorFile(String username, String url) {
        //根据url检查文件是否存在
        FileData fileData = userMapper.selectUserDataByUrl(url);
<<<<<<< HEAD
=======
        log.info("-----正在验证收藏文件条件----");
>>>>>>> 2017e29 (west2wp)
        if(fileData == null){
            //文件不存在
            return "notExist";
        }
        if(!fileData.getUsername().equals(username)){
            //文件存在但是与用户不匹配
            return "userWrong";
        }
        if(fileData.getType().equals("wjj")){
            //文件是文件夹,暂时不支持收藏文件夹
            return "fileWrong";
        }
        //根据用户名和url获取收藏夹里是否有对应的文件
        FileData fileData1 = userMapper.selectFavorFile(username,url);
        if(fileData1 != null){
            //文件已被收藏,返回报错
            return "repeat";
        }
<<<<<<< HEAD
=======
        log.info("-----可以进行收藏-----");
>>>>>>> 2017e29 (west2wp)
        //获取当前系统时间
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = df.format(System.currentTimeMillis());
        //设置收藏时间
        fileData.setDate(currentTime);
        //将文件信息存入数据库
<<<<<<< HEAD
        userMapper.saveFavorFile(fileData);
=======
        log.info("-----正在将收藏文件存入数据库----");
        log.info(fileData.toString());
        userMapper.saveFavorFile(fileData);
        log.info("-------收藏文件成功-------");
>>>>>>> 2017e29 (west2wp)
        return "success";
    }

    @Override
    public String removeFavorFile(String username, String url) {
        //根据url检查文件是否存在
        FileData fileData = userMapper.selectUserDataByUrl(url);
        if(fileData == null){
            //文件不存在
            return "notExist";
        }
        if(!fileData.getUsername().equals(username)){
            //文件存在但是与用户不匹配
            return "userWrong";
        }
        //根据用户名和url获取收藏夹里是否有对应的文件
        FileData fileData1 = userMapper.selectFavorFile(username,url);
        if(fileData1 == null){
            //文件并没有被收藏
            return "notFavor";
        }
        //删除数据库里对应收藏文件数据
        userMapper.deleteFavorFileByUsernameAndUrl(username,url);
        return "success";
    }

    @Override
    public void deleteFile(String username, String url) {
        FileData fileData = userMapper.selectUserDataByUrl(url);
        //获取被删除文件的大小,由于之前已经判断了url的可行性,必不会为空
        double sizes = userMapper.getSizeByUrl(url);
        log.info("-----已获取被删除文件大小:" + sizes + "kb----");
        //删除数据库文件信息
        userMapper.deleteFileByUrlAndUsername(username,url);
        log.info("-----已删除数据库文件信息-----");
        //根据用户名获取剩余空间
        double space = userMapper.selectSpaceByUsername(username);
        log.info("----用户:" + username + ",剩余空间:" + space + "MB---");
        //更新用户剩余空间
        userMapper.updateSpaceByUsername(space + sizes/1024,username);
        log.info("-------更新用户剩余空间成功------");
        double space1 = userMapper.selectSpaceByUsername(username);
        log.info("----用户:" + username + ",剩余空间:" + space1 + "MB---");
        //如果删除的不是文件夹(文件夹不能收藏),删除对应的收藏信息
        if(!fileData.getType().equals("wjj")){
            //根据url删除该资源路径下所有在数据中收藏的文件信息
            userMapper.deleteFavorFileByUrl(url);
            log.info("-----收藏文件信息删除成功----");
        }
    }
<<<<<<< HEAD
=======

    @Override
    public String changeFilename(String username, String filename, String url) {
        FileData fileData = userMapper.selectUserDataByUrl(url);
        if(fileData == null){
            //url不存在
            log.warn("-----重名名失败,url不存在。url:" + url + "----");
            return "UrlWrong";
        }
        if(!fileData.getUsername().equals(username)){
            //url与用户不匹配
            log.warn("-----重命名失败,url与用户不匹配。url:" + url + " username:" + username);
            return "UserWrong";
        }
        //尝试获取同名文件
        FileData fileData1 = userMapper.selectFileDataByFilename(filename,username);
        if(fileData1 != null){
            //有同名文件重命名失败
            log.warn("----重命名失败,有同名文件,filename:" + filename + "-----");
            return "RepeatWrong";
        }
        log.info("----正在更新文件名,url:" + url + " filename:" + filename + "----");
        userMapper.updateFilenameByUrl(filename,url);
        FileData fileData2 = userMapper.selectFavorFile(username,url);
        if(fileData2 != null){
            //已被收藏
            userMapper.updateFavorNameByUrl(filename,url);
        }
        log.info("------重命名成功-----");
        return "success";
    }

    @Override
    public String saveFeedback(String username, String feedback) {
        User user = userMapper.selectByUsername(username);
        if(user == null){
            //用户不存在
            return "UserWrong";
        }
        //获取当前系统时间
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = df.format(System.currentTimeMillis());
        Feedback feedback1 = new Feedback(username,feedback,currentTime);
        log.info("-----正在保存用户反馈----");
        userMapper.saveFeedback(feedback1);
        log.info("-----用户反馈保存成功,用户:" + username + " 反馈:" + feedback);
        return "success";
    }
>>>>>>> 2017e29 (west2wp)
}