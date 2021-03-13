package com.west2wp.dao;

<<<<<<< HEAD
=======
import com.west2wp.pojo.Feedback;
>>>>>>> 2017e29 (west2wp)
import com.west2wp.pojo.User;
import com.west2wp.pojo.FileData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;


import java.util.List;

@Mapper
@Repository
public interface UserMapper {

    //保存用户
    void saveUser(User user);

    //保存上传的文件
    void saveFileData(FileData fileData);

    //保存用户收藏文件
    void saveFavorFile(FileData fileData);

<<<<<<< HEAD
=======
    //保存用户反馈信息
    void saveFeedback(Feedback feedback);

>>>>>>> 2017e29 (west2wp)
    //根据用户名从数据库中获取用户信息
    User selectByUsername(String username);

    //根据用户名获取用户的url
    String selectUrlByUsername(String username);

    //根据用户名修改用户头像url
    void updateUrlByUsername(User user);

    //根据用户名获取该用户剩余空间
    double selectSpaceByUsername(String username);

    //根据url获取用户名
    String selectUserByUrl(String url);

    //根据url获取该文件的文件信息
    FileData selectUserDataByUrl(String url);

    //根据url获取文件大小
    double getSizeByUrl(String url);

<<<<<<< HEAD
=======
    //根据文件名获取文件信息(不允许重复名字,至多有一个)
    FileData selectFileDataByFilename(@Param("filename") String filename,@Param("username") String username);

>>>>>>> 2017e29 (west2wp)
    //根据用户名和url获取收藏文件信息
    FileData selectFavorFile(@Param("username") String username,@Param("url") String url);

    //根据用户名更新用户密码
    void updatePassword(@Param("username") String username,@Param("password") String password);

    //根据用户名更新用户网盘容量
    void updateSpaceByUsername(@Param("space") double space,@Param("username") String username);

<<<<<<< HEAD
=======
    //根据url更新用户的文件名
    void updateFilenameByUrl(@Param("filename") String filename,@Param("url") String url);

    //根据url更新用户收藏文件夹名
    void updateFavorNameByUrl(@Param("filename") String filename,@Param("url") String url);

>>>>>>> 2017e29 (west2wp)
    //根据用户名,父级文件,文件类型获取文件数据(专门用来负责文件夹的判断,文件夹不重名,所以只会返回至多一个)
    FileData getFileDataByParentFile(@Param("username") String username,@Param("parent") String parent,@Param("type") String type);

    //根据用户名,用户父级文件,获取同级文件中所有文件夹的数据,来判断新建文件夹是否重名
    List<FileData> getSameGradeFileNameByParentFile(@Param("username") String username,@Param("parent") String parent,@Param("type") String type);

    //获取同级文件中所有文件
    List<FileData> getSameGradeFileDataByParentFile(@Param("username") String username,@Param("parent") String parent);

    //根据用户名获取用户所有的收藏文件
    List<FileData> getAllFavorDataByUsername(@Param("username") String username);

    //根据用户名和url删除数据库中的文件信息
    void deleteFileByUrlAndUsername(@Param("username") String username,@Param("url") String url);

    //根据用户用户名和url删除数据库中对应的收藏文件
    void deleteFavorFileByUsernameAndUrl(@Param("username") String username,@Param("url") String url);

    //根据用户url删除数据库中对应的收藏文件(适用于上传文件被删除时,对用户的收藏文件也同时作删除)
    void deleteFavorFileByUrl(String url);
}