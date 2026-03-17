package com;

import framework.action.Box;

import java.io.Serializable;
import java.util.Date;

public class LoginBean implements Serializable {

    private static final long serialVersionUID = 8757965692813992190L;

    private String str_UserId = ""; // 두레포털 사용자 아이디
    private String str_EmplCode = ""; // 사원번호
    private String str_UserName = ""; // 사용자 이름
    private String str_Positionen = ""; // 직급
    private String str_DeptCode = ""; // 사용자가 소속된 부서코드
    private String str_DeptName = ""; // 사용자가 소속된 부서명
    private String str_PhoneNumber = ""; // 사용자 전화번호

    private String str_Password = ""; // 사용자 UI에서 입력된 비밀번호
    private String str_UserEmail = ""; // 전자우편주소
    private String str_NaverworksMailPassword = ""; // 네이버 웍스 외부앱용 비밀번호.
    private Date LoginTime;
    private String companyName = ""; // 사업장명칭

    private String gw_host = "";

    public LoginBean() {
        // 클래스 변수 초기화
        this.str_UserId = "";
        this.str_EmplCode = "";
        this.str_UserName = "";
        this.str_DeptCode = "";
        this.str_DeptName = "";
        this.str_Password = "";
        this.str_UserEmail = "";
        this.str_NaverworksMailPassword = "";
        this.LoginTime = new Date();
    }

    public String toString() {
        Box buf = new Box("");
        buf.put("user_id", this.getUserId());
        buf.put("empl_code", this.getEmplCode());
        buf.put("user_name", this.getUserName());
        buf.put("email", this.getStr_UserEmail());
        buf.put("nw_mail_pwd", this.getNaverworksMailPassword());
        return buf.toString();
    }

    public String getDeptCode() {
        return this.str_DeptCode;
    }

    public String getDeptName() {
        return this.str_DeptName;
    }

    public String getPassword() {
        return this.str_Password;
    }

    public String getPhoneNumber() {
        return str_PhoneNumber;
    }

    public String getUserId() {
        return this.str_UserId == null ? "" : this.str_UserId;
    }

    public String getUserName() {
        return this.str_UserName;
    }

    public void setDeptCode(String str_DeptCode) {
        this.str_DeptCode = str_DeptCode;
    }

    public void setDeptName(String str_DeptName) {
        this.str_DeptName = str_DeptName;
    }

    public void setPassword(String str_Password) {
        this.str_Password = str_Password;
    }

    public void setPhoneNumber(String phonenumber) {
        this.str_PhoneNumber = phonenumber;
    }

    public void setUserId(String str_UserId) {
        this.str_UserId = str_UserId;
    }

    public void setUserName(String str_UserName) {
        this.str_UserName = str_UserName;
    }

    public String getEmplCode() {
        return str_EmplCode;
    }

    public void setEmplCode(String str_Empl_Code) {
        this.str_EmplCode = str_Empl_Code;
    }

    public String getPosition() {
        return str_Positionen;
    }

    public void setPosition(String str_Position) {
        this.str_Positionen = str_Position;
    }

    public String getStr_UserEmail() {
        return str_UserEmail;
    }

    public void setStr_UserEmail(String str_UserEmail) {
        this.str_UserEmail = str_UserEmail;
    }

    public Date getLoginTime() {
        return LoginTime;
    }

    public String getGwHost() {
        return gw_host;
    }

    public void setGwHost(String gw_host) {
        this.gw_host = gw_host;
    }

    public String getNaverworksMailPassword() {
        return str_NaverworksMailPassword;
    }

    public void setNaverworksMailPassword(String str_NaverworksMailPassword) {
        this.str_NaverworksMailPassword = str_NaverworksMailPassword;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
