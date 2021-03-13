package com.west2wp.service;

import com.west2wp.pojo.FileData;
import com.west2wp.pojo.User;

import java.util.List;

public interface UserService {
    /**
     * 注册用户方法:由注册界面获取生成user对象实例,调用queryUserByUsername方法
     * 判断用户是否存在,若存在调用saveUser方法在数据库中保存用户信息
     * @param user   注册时用户实例
     */
    boolean login(User user);

    /**
     * 用户登录方法:由登录界面获取生成user对象实例,调用queryUserByUsernameAndPassword方法
     * 判断用户账号密码是否正确,若正确登录成功跳转页面,否则登录失败出现账户名或者密码错误提示
     * @param user   登录时用户实例(仅有username和password)
     */
    User register(User user);

    /**
     * 根据用户名上传头像,并把头像的url存入数据库
     * @param user  用户
     * @return   是否成功
     */
    boolean uploadPhoto(User user);

    /**
     * 根据用户名获取用户头像
     * @param username  用户名
     * @return   头像url
     */
    String getPhotoUrl(String username);

    /**
     * 根据用户名在数据库中查询剩余空间和文件空间比较判断是否能存
     * @param username  用户名
     * @param size    当前文件大小
     * @return    返回文件大小之差
     */
    double judgeSpace(String username,double size);

    /**
     * 根据用户名获取用户数据
     * @param username  用户名
     * @return    用户实例
     */
    User getUser(String username);


    /**
     * 根据url获取文件信息
     * @param url   资源路径
     * @return   文件实例
     */
    FileData getFileData(String url);


    /**
     * 根据url获取FileData中的用户名
     * @param url 资源路径
     * @return   用户名
     */
    String selectUserByUrl(String url);

    /**
     * 根据url判断该资源是否存在且属于该用户
     * @param username  用户名
     * @param url  资源路径
     * @return    下载请求是否通过
     */
    boolean judgeUrl(String username,String url);

    /**
     * 上传方法,将新文件信息存入数据库,同时更新用户空间
     * @param username    用户名
     * @param space   容量
     * @param fileData   文件信息
     */
    void upload(String username, double space, FileData fileData);

    /**
     * 根据用户名,文件名,父级文件创建新的文件
     * @param username  用户名
     * @param Filename  文件名
     * @param parent    父级文件路径
     * @return  创建状态
     */
    String createNewFile(String username,String Filename,String parent);

    /**
     * 根据用户名,父级文件路径判断父级文件是否存在
     * @param username   用户名
     * @param parentFile   父级文件路径
     * @return    是否存在父级文件路径
     */
    boolean judgeParentFileExist(String username,String parentFile);

    /**
     * 根据用户名和url删除数据库中用户数据,同时更新用户网盘容量
     * @param username  用户名
     * @param url   资源路径
     */
    void deleteFile(String username,String url);

    /**
     * 根据用户名和父级文件夹路径获取该目录下所有文件信息
     * @param username    用户名
     * @param parentFile    父级文件夹
     * @return    目录下所有文件夹信息
     */
    List<FileData> getFileInformation(String username,String parentFile);

    /**
     * 根据用户名更改用户密码
     * @param username  用户名
     * @param password  新密码
     */
    void changePassword(String username,String password);

    /**
     * 根据用户名和文件url添加收藏
     * @param username  用户名
     * @param url  资源路径
     * @return  添加收藏状态
     */
    String addFavorFile(String username,String url);

    /**
     * 根据用户名和文件url移除收藏
     * @param username  用户名
     * @param url    资源路径
     * @return   移除收藏状态
     */
    String removeFavorFile(String username,String url);

<<<<<<< HEAD
    List<FileData> getAllFavorFile(String username);
=======
    /**
     * 根据用户名获取用户收藏夹内容
     * @param username    用户名
     * @return   收藏夹内容
     */
    List<FileData> getAllFavorFile(String username);

    /**
     * 根据用户名 url 重命名文件
     * @param username  用户名
     * @param Filename  新文件名
     * @param url  资源路径
     * @return  重命名状态
     */
    String changeFilename(String username,String Filename,String url);

    /**
     * 根据用户名和小作文保存用户反馈
     * @param username   用户名
     * @param feedback   小作文
     * @return  保存状态
     */
    String saveFeedback(String username,String feedback);
>>>>>>> 2017e29 (west2wp)
}