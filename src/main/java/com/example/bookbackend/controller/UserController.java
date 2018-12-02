package com.example.bookbackend.controller;

import com.example.bookbackend.mapper.UserMapper;
import com.example.bookbackend.bean.Message;
import com.example.bookbackend.bean.User;
import com.example.bookbackend.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;

import javax.mail.SendFailedException;
import java.util.List;

@EnableTransactionManagement  // 需要事务的时候加上
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private EmailService emailService;

    @PostMapping(path="/signUp")
    @Transactional(rollbackFor = Exception.class)//事物回滚
    public Message insert(@RequestBody User user){
        //Message message = new Message("105","注册成功！");
        try {
            userMapper.insert(user);
            sendMail(user.getEmail());
        }catch (DuplicateKeyException duplicateKeyException){
            return new Message("106","该邮箱已被注册！");
        }catch (SendFailedException e){
            System.out.println(e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();//回滚事物
            return new Message("107","无效邮箱！");
        }
        return new Message("105","注册成功！");
    }

    @GetMapping("/selectAll")
    public List<User> selectAll() {
        return userMapper.selectAll();
    }
    @GetMapping("/selectByEmail")
    public User selectByEmail(@RequestParam("email") String email){
        return userMapper.selectByEmail(email);
    }

    //@RequestMapping("/mail")
    public String sendMail(String email)throws SendFailedException{
        try {
            emailService.sendTemplateMail(email);
        }catch (Exception e){
            throw new SendFailedException();//发送失败抛出异常
        }
        return "发送成功";
    }

    @GetMapping("/activate")
    public String active(@RequestParam("email") String email){
        if(userMapper.activate(email)==1){
            return "success";
        }else return "false";
    }

    @PostMapping("/login")
    public Message login(@RequestParam("email") String email, @RequestParam("pwd") String pwd){
        User user = userMapper.selectByEmail(email);
        if (user == null){
            return new Message("101","不存在该用户！");
        }else if (user.getState() == 0){
            return new Message("102","该账号还未激活！");
        }else if (pwd.equals(user.getPwd())){
            return new Message("103","登录成功！");
        }else {
            return new Message("104","密码错误！");
        }
    }

    @PostMapping("/updateInfo")
    public Message updateInfo(@RequestBody User user){
        try{
            System.out.println(user.getName());
            userMapper.updateInfo(user);
        }catch (Exception e){
            e.printStackTrace();
            return new Message("109","更新失败！");
        }
        return new Message("108","更新成功！");
    }

}