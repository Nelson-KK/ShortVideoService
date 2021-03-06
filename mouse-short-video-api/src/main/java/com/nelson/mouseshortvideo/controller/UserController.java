package com.nelson.mouseshortvideo.controller;

import com.alibaba.fastjson.JSON;
import com.nelson.mouseshortvideo.pojo.Users;
import com.nelson.mouseshortvideo.service.UserService;
import com.nelson.mouseshortvideo.utils.MD5Utils;
import com.nelson.mouseshortvideo.utils.MouseShortVideoResult;
import com.nelson.mouseshortvideo.vo.UserVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.catalina.User;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sun.nio.ch.IOUtil;

import java.io.*;
import java.util.UUID;

@RestController
@Api(value = "用户相关业务接口")
public class UserController extends BasicController {
    @Autowired
    private UserService mUserService;

    @PostMapping("/query")
    @ApiOperation("查询用户信息")
    @ApiImplicitParam(name="userId", value="用户id", required=true,
            dataType="String", paramType="query")
    public MouseShortVideoResult query(String userId){
        if(StringUtils.isBlank(userId)){
            return MouseShortVideoResult.errorMsg("用户ID不能为空");
        }
        Users user = mUserService.query(userId);
        if(user != null){
            UserVo userVO = new UserVo();
            BeanUtils.copyProperties(user, userVO);
            return MouseShortVideoResult.ok(userVO);
        } else {
            return MouseShortVideoResult.errorMsg("无该用户");
        }
    }


    @PostMapping("/user/uploadFaceImage")
    @ApiOperation("上传头像")
    public String uploadFaceIcon(String userId, @RequestParam("file") MultipartFile[] files) {
        if (StringUtils.isBlank(userId)) {
            return MouseShortVideoResult.errorMsg("用户ID不能为空").toJsonString();
        }
        FileOutputStream fos = null;
        InputStream fis = null;
        String uploadPathDB;
        try {
            uploadPathDB = "/"+userId + "/face/";
            if (files != null && files.length > 0) {
                String filename = files[0].getOriginalFilename();
                if (StringUtils.isNotBlank(filename)) {
                    uploadPathDB += filename;
                    // 文件上传最终保存炉具
                    String finalFacePath = FILE_SPACE + uploadPathDB;
                    // 设置数据保存的路径
                    File outFile = new File(finalFacePath);
                    if (outFile.getParentFile() != null || !outFile.getParentFile().isDirectory()) {
                        // 创建文件夹
                        outFile.getParentFile().mkdirs();
                    }
                    fos = new FileOutputStream(outFile);
                    fis = files[0].getInputStream();
                    IOUtils.copy(fis, fos);
                }
            } else {
                return MouseShortVideoResult.errorMsg("上传失败").toJsonString();
            }
        } catch (Exception e) {
            return MouseShortVideoResult.errorMsg("上传失败").toJsonString();
        } finally {
            try {
                if (fos != null) {
                    fos.flush();
                    fos.close();
                }
            } catch (Exception e) {

            }
        }
        Users user = new Users();
        user.setId(userId);
        user.setFaceImage(uploadPathDB);
        mUserService.updateUser(user);
        user.setPassword("");
        return MouseShortVideoResult.ok(user).toJsonString();
    }
}
