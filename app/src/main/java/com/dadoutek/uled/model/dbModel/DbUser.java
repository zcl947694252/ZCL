package com.dadoutek.uled.model.dbModel;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * Created by hejiajun on 2018/5/16.
 */

@Entity
public class DbUser {
    @Id(autoincrement = true)
    private Long id;
    //头像
    private String avatar;
    //渠道id
    private String channel="uled";
    //用户邮箱
    private String email;
    //用户名
    private String name;
    //用户账号--切换区域以后用户账号变为mesname
    private String account;
    //用户手机号
    private String phone;
    //认证
    private String token;
    //密码
    private String password;
    //区域id
    private String last_region_id;
    //区域切换者id  在请求头加上键值对authorizer-user-id:80即可获取id为80用户的数据，如果请求头中缺少这对键值对，后台默认authorizer-user-id：自己用户的id。
    private String authorizer_user_id;
    //单点登录标识
    private String login_state_key;

    private  String last_authorizer_user_id;
    //切换区域后设备登录使用的账户
    private String controlMeshName;
    //切换区域后设备登录使用的密码
    private String controlMeshPwd;
    //lastGenMeshAddr设备地址唯一标识确保地址不重复
    private int lastGenMeshAddr;



    @Generated(hash = 298022711)
    public DbUser(Long id, String avatar, String channel, String email, String name, String account, String phone,
            String token, String password, String last_region_id, String authorizer_user_id,
            String login_state_key, String last_authorizer_user_id, String controlMeshName, String controlMeshPwd,
            int lastGenMeshAddr) {
        this.id = id;
        this.avatar = avatar;
        this.channel = channel;
        this.email = email;
        this.name = name;
        this.account = account;
        this.phone = phone;
        this.token = token;
        this.password = password;
        this.last_region_id = last_region_id;
        this.authorizer_user_id = authorizer_user_id;
        this.login_state_key = login_state_key;
        this.last_authorizer_user_id = last_authorizer_user_id;
        this.controlMeshName = controlMeshName;
        this.controlMeshPwd = controlMeshPwd;
        this.lastGenMeshAddr = lastGenMeshAddr;
    }
    @Generated(hash = 762027100)
    public DbUser() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getAvatar() {
        return this.avatar;
    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    public String getChannel() {
        return this.channel;
    }
    public void setChannel(String channel) {
        this.channel = channel;
    }
    public String getEmail() {
        return this.email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getAccount() {
        return this.account;
    }
    public void setAccount(String account) {
        this.account = account;
    }
    public String getPhone() {
        return this.phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getToken() {
        return this.token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public String getPassword() {
        return this.password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getLast_region_id() {
        return this.last_region_id;
    }
    public void setLast_region_id(String last_region_id) {
        this.last_region_id = last_region_id;
    }
    public String getAuthorizer_user_id() {
        return this.authorizer_user_id;
    }
    public void setAuthorizer_user_id(String authorizer_user_id) {
        this.authorizer_user_id = authorizer_user_id;
    }

    public String getLast_authorizer_user_id() {
        return last_authorizer_user_id;
    }

    public void setLast_authorizer_user_id(String last_authorizer_user_id) {
        this.last_authorizer_user_id = last_authorizer_user_id;
    }

    public String getLogin_state_key() {
        return this.login_state_key;
    }
    public void setLogin_state_key(String login_state_key) {
        this.login_state_key = login_state_key;
    }
    public String getControlMeshName() {
        return this.controlMeshName;
    }
    public void setControlMeshName(String controlMeshName) {
        this.controlMeshName = controlMeshName;
    }
    public String getControlMeshPwd() {
        return this.controlMeshPwd;
    }
    public void setControlMeshPwd(String controlMeshPwd) {
        this.controlMeshPwd = controlMeshPwd;
    }

    @Override
    public String toString() {
        return "DbUser{" +
                "id=" + id +
                ", avatar='" + avatar + '\'' +
                ", channel='" + channel + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", account='" + account + '\'' +
                ", phone='" + phone + '\'' +
                ", token='" + token + '\'' +
                ", password='" + password + '\'' +
                ", last_region_id='" + last_region_id + '\'' +
                ", authorizer_user_id='" + authorizer_user_id + '\'' +
                ", login_state_key='" + login_state_key + '\'' +
                ", last_authorizer_user_id='" + last_authorizer_user_id + '\'' +
                ", controlMeshName='" + controlMeshName + '\'' +
                ", controlMeshPwd='" + controlMeshPwd + '\'' +
                '}';
    }
    public int getLastGenMeshAddr() {
        return this.lastGenMeshAddr;
    }
    public void setLastGenMeshAddr(int lastGenMeshAddr) {
        this.lastGenMeshAddr = lastGenMeshAddr;
    }
}
