package com;

import framework.action.Action;
import framework.action.Box;
import framework.db.RecordSet;
import framework.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import util.DBLog;
import util.PortalUtil;
import util.SMSUtils;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class LoginAction extends Action {

    private LoginDao dao = null;

    private LoginDao getSelect(String service) throws Exception {
        if (dao == null || !dao.getService().equals(service)) {
            dao = new LoginDao(getConnectionManager(service));
            dao.setService(service);
        }
        return dao;
    }

    private CodeDao codeDao = null;

    private CodeDao getCodeSelect(String service) throws Exception {
        if (codeDao == null || !codeDao.getService().equals(service)) {
            codeDao = new CodeDao(getConnectionManager(service));
            codeDao.setService(service);
        }
        return codeDao;
    }

    /**
     * 로그인 성고, 실패 여부를 기록한다.
     */
    public void loginLogWrite(String p_userid, String p_status) {
        try {
            String loginIp = getRequest().getRemoteAddr();
            String userAgent = getRequest().getHeader("user-agent");

            getSelect("dpms").saveLoginLog(p_userid, p_status, userAgent, loginIp);

            this.getConnectionManager("dpms").commit();

        } catch (Exception e) {
            getLogger().error(e);
            e.printStackTrace();
        }
    }

    /**
     * 로그인을 처리한다.
     */
    public void processLogin() {
        DBLog.actionLog(this.getRequest(), getConnectionManager("dpms"), this);

        try {

            String userid = getInput().getString("user_id");
            String password = getInput().getString("password");
            LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");

            if (loginBean != null) {
                PortalUtil.setResult(this.getResponse(), 0, "메인 화면으로 이동합니다.");
            } else {
                RecordSet rs = getSelect("dpms").selectUserInfobyUserid(userid, password);

                if (rs.nextRow()) {

                    loginBean = new LoginBean();

                    if (!"Yes".equals(rs.getString("login_allow_yn"))) {
                        loginLogWrite(userid, "Bloking");
                        PortalUtil.setResult(this.getResponse(), "관리자에 의해 로그인이 차단되어 있습니다.");

                    } else {
                        if ("Yes".equals(rs.getString("pwdchk"))) {
                            setLoginBean(loginBean, rs);

                            setSession(loginBean);

                            loginLogWrite(loginBean.getUserId(), "Login");

                            PortalUtil.setResult(this.getResponse(), 0, "succ");
                        } else {
                            loginLogWrite(userid, "Password Fail");

                            PortalUtil.setResult(this.getResponse(), "사용자 비밀번호를 확인 하십시오.");
                        }
                    }
                } else {
                    loginLogWrite(userid, "ID Fail");

                    PortalUtil.setResult(this.getResponse(), "사용자 아이디를 확인 하십시오.");
                }

            }

        } catch (Exception e) {
            // dao에서 발생한 오류 메시지를 화면으로 전달.
            PortalUtil.setMessage(this.getResponse(), -12, e);

            getLogger().error(e);
            e.printStackTrace();
        }

    }


    public void processSwitchUser() {
        try {
            DBLog.actionLog(this.getRequest(), getConnectionManager("dpms"), this);

            String userid = getInput().getString("user_id");
            String usernm = getInput().getString("user_nm");
            String password = getInput().getString("password");
            LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");

            RecordSet rs = null;
            if (this.getInput().containsKey("user_id")) {
                rs = getSelect("dpms").selectUserInfobyUserName(userid, usernm, password);
            } else {
                rs = getSelect("dpms").selectUserInfobyUserName("", usernm, password);
            }

            if (rs.nextRow()) {

                if ("Y".equals(rs.getString("pwdchk"))) {

                    loginBean = new LoginBean();
                    if (rs.getRowCount() > 1) {
                        PortalUtil.setResult(this.getResponse(), rs);
                    } else {

                        String ssochk = rs.getString("ssochk");
                        String target = rs.getString("notify_target");

                        setLoginBean(loginBean, rs);
                        setSession(loginBean);
                        PortalUtil.setResult(this.getResponse(), rs);

                    }

                } else {
                    PortalUtil.setResult(this.getResponse(), "사용자 비밀번호를 확인 후 재시도 하십시오.");
                }

            } else {
                PortalUtil.setResult(this.getResponse(), "사용자 아이디(사원번호)를 확인 하십시오.");
            }

        } catch (Exception e) {
            getLogger().error("processSwitchUser error", e);
            PortalUtil.setResult(this.getResponse(), e.getMessage());
        }
    }


    /**
     * 임차인 로그인을 처리한다.
     */
    public void processLesseeLogin() {
        DBLog.actionLog(this.getRequest(), getConnectionManager("dpms"), this);

        try {

            Box box = this.getInput();
            box.put("user_key", box.getString("lessee_id"));
            box.put("lessee_id", PortalUtil.decodeBase64(box.getString("lessee_id")));

            String password = getInput().getString("password");
            LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");

            if (loginBean != null && box.getString("lessee_id").equals(loginBean.getUserId())) {
                PortalUtil.setResult(this.getResponse(), 0, "메인 화면으로 이동합니다.");
            } else {
                RecordSet rs = getSelect("dpms").selectLesseeInfo(box);

                if (rs.nextRow()) {

                    if ("Y".equals(rs.getString("pw_check"))) {

                        loginBean = new LoginBean();

                        setLoginBean(loginBean, rs);

                        setSession(loginBean);

                        loginLogWrite(loginBean.getUserId(), "Login");

                        PortalUtil.setResult(this.getResponse(), 0, "succ");
                    } else {
                        loginLogWrite(box.getString("lessee_id"), "Lessee Password Fail");

                        PortalUtil.setResult(this.getResponse(), -1, "비밀번호를 확인 하십시오.");
                    }

                } else {
                    loginLogWrite(box.getString("lessee_id"), "Lessee ID Fail");

                    PortalUtil.setResult(this.getResponse(), -2, "임차인 식별코드를 확인 하십시오.");
                }

            }

        } catch (Exception e) {
            // dao에서 발생한 오류 메시지를 화면으로 전달.
            PortalUtil.setMessage(this.getResponse(), -12, e);

            getLogger().error(e);
            e.printStackTrace();
        }

    }

    public void processOwnerLogin() {
        DBLog.actionLog(this.getRequest(), getConnectionManager("dpms"), this);

        try {

            Box box = this.getInput();

            LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");

            if (loginBean != null && box.getString("id").equals(loginBean.getUserId())) {
                PortalUtil.setResult(this.getResponse(), 0, "메인 화면으로 이동합니다.");
            } else {
                RecordSet rs = getSelect("dpms").selectOwnerInfo(box);

                if (rs.nextRow()) {

                    if ("Y".equals(rs.getString("pw_check"))) {

                        loginBean = new LoginBean();

                        setLoginBean(loginBean, rs);

                        setSession(loginBean);

                        loginLogWrite(loginBean.getUserId(), "Login");

                        PortalUtil.setResult(this.getResponse(), 0, "succ");
                    } else {
                        loginLogWrite(box.getString("id"), "Owner Password Fail");

                        PortalUtil.setResult(this.getResponse(), -1, "비밀번호를 확인 하십시오.");
                    }

                } else {
                    loginLogWrite(box.getString("id"), "Owner ID Fail");

                    PortalUtil.setResult(this.getResponse(), -2, "아이디를 확인 하십시오.");
                }

            }

        } catch (Exception e) {
            // dao에서 발생한 오류 메시지를 화면으로 전달.
            PortalUtil.setMessage(this.getResponse(), -12, e);

            getLogger().error(e);
            e.printStackTrace();
        }

    }

    public void processFindOwnerID() {
        DBLog.actionLog(this.getRequest(), getConnectionManager("dpms"), this);

        try {

            Box box = this.getInput();

            LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");

            if (loginBean != null && box.getString("owner_name").equals(loginBean.getUserName())) {
                PortalUtil.setResult(this.getResponse(), 0, "이미 로그인 되어 있습니다.");
            } else {
                RecordSet rs = getSelect("dpms").selectOwnerID(box);

                StringBuilder sb = new StringBuilder("<pre style='text-align:left;'> 다음중 하나를 사용하여 로그인 하십시오.\n\n");
                int idx = 0;
                if (rs.nextRow()) {
                    idx++;
                    sb.append("\t위탁자 성명 : ").append(rs.getString("owner_name")).append("\n");
                    sb.append("\t위탁자 상호 : ").append(rs.getString("corp_name")).append("\n");
                    sb.append("\t위탁자 빌딩 : ").append(rs.getString("building_name")).append("\n");
                    if (idx > 1) {
                        sb.append("----------------------------\n");
                    }
                } else {
                    PortalUtil.setResult(this.getResponse(), 0, "조건에 맞는 사용자 아이디를 찾을 수 없습니다.");
                    return;
                }
                sb.append("</pre>");
                PortalUtil.setResult(this.getResponse(), 0, sb.toString());

            }

        } catch (Exception e) {
            // dao에서 발생한 오류 메시지를 화면으로 전달.
            PortalUtil.setMessage(this.getResponse(), -12, e);

            getLogger().error(e);
            e.printStackTrace();
        }

    }


    public void processResetOwnerPW() {
        DBLog.actionLog(this.getRequest(), getConnectionManager("dpms"), this);

        try {

            Box box = this.getInput();

            LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");
            HashMap<String, Object> map = new HashMap<String, Object>();

            if (loginBean != null && box.getString("owner_name").equals(loginBean.getUserName())) {
                PortalUtil.setResult(this.getResponse(), 0, "이미 로그인 되어 있습니다.");
            } else {

                boolean ret = getSelect("dpms").initOwnerPassword(box);

                if (ret) {

                    /*
                     * // 두레포털 SMS 서비스 이용. SendSmsServiceDo sevice = new SendSmsServiceDo(); // 실제
                     * 시스템 운영시 사용할 함수. // 테스트 중에는 sms를 관리자에게만 보낸다. String p_phonenumber =
                     * box.getString("condition2"); p_phonenumber = p_phonenumber.replace("+82",
                     * "0"); sevice.sendMessage(null, 2, p_phonenumber, PortalUtil.getSMSPhone(),
                     * "DPMS 비밀번호가 [ " + pwd.toString() + " ]로 변경 되었습니다.");
                     */

                    // 알리고 SMS API 이용 방식.
                    SMSUtils sms = new SMSUtils();
                    HashMap<String, Object> msg = new HashMap<String, Object>();
                    msg.put("msg", "비밀번호가 [ " + box.getString("new_password") + " ]로 초기화 되었습니다."); // 메시지내용.
                    msg.put("receiver", box.getString("personal_mobile")); // 수신번호
                    msg.put("destination", box.getString("user_id") + "|사용자"); // 수신인 %고객명% 치환
                    msg.put("sender", getCodeSelect("dpms").getSMSSendPhone("doorepms")); // 발신번호 - 알리고에 등록된 대표 발신버호만 발송 가능.
                    msg.put("rdate", ""); // 예약일자 - 20161004 : 2016-10-04일기준
                    msg.put("rtime", ""); // 예약시간 - 1930 : 오후 7시30분
                    msg.put("testmode_yn", "N"); // Y 인경우 실제문자 전송X , 자동취소(환불) 처리
                    msg.put("title", "비밀번호 초기화"); // LMS, MMS 제목 (미입력시 본문중 44Byte 또는 엔터 구분자 첫라인)

                    String retStr = sms.sendSMS(getConnectionManager("dpms"), msg);
                    getLogger().debug(retStr);

                    map.put("result", 0);
                    map.put("message", "임시 비밀번호를 SMS로 발송하였습니다.\n임시비밀번호를 이용하여 로그인 하십시오.\n임시비밀번호를 수신하지 못하신 경우에는 담당자에게 휴대전화번호 변경을 요청후 재시도 하십시오.");
                } else {
                    map.put("result", -1);
                    map.put("message", "비밀번호를 초기화 할수 없습니다.\n아이디와 전화번호를 확인 후 재시도 하십시오.");
                }
                PortalUtil.setResult(this.getResponse(), map);


            }

        } catch (Exception e) {
            // dao에서 발생한 오류 메시지를 화면으로 전달.
            PortalUtil.setMessage(this.getResponse(), -12, e);

            getLogger().error(e);
            e.printStackTrace();
        }

    }


    public void processChangeOwnerPW() {
        DBLog.actionLog(this.getRequest(), getConnectionManager("dpms"), this);

        try {

            Box box = this.getInput();

            LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");
            HashMap<String, Object> map = new HashMap<String, Object>();

            if (loginBean == null) {
                PortalUtil.setResult(this.getResponse(), 0, "로그인 되어 있지 않습니다.");
            } else {
                box.put("user_id", loginBean.getUserId());

                boolean ret = getSelect("dpms").changeOwnerPassword(box);

                if (ret) {
                    map.put("result", 0);
                    map.put("message", "비밀번호가 변경되었습니다. \n변경된 비밀번호를 이용하여 로그인 하십시오.");
                } else {
                    map.put("result", -1);
                    map.put("message", "비밀번호를 변경할 수 없습니다.\n기존 비빌번호를 확인 후 재시도 하십시오.");
                }
                PortalUtil.setResult(this.getResponse(), map);


            }

        } catch (Exception e) {
            // dao에서 발생한 오류 메시지를 화면으로 전달.
            PortalUtil.setMessage(this.getResponse(), -12, e);

            getLogger().error(e);
            e.printStackTrace();
        }

    }


    public void processIsConnected() {

        try {

            LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");

            if (loginBean == null) {
                PortalUtil.setResult(this.getResponse(), -99, "Logout");
            } else {
                PortalUtil.setResult(this.getResponse(), 0, DateUtil.toString(loginBean.getLoginTime(), "yyyy-MM-dd HH:mm:ss"));
            }

        } catch (Exception e) {
            PortalUtil.setResult(this.getResponse(), -99, "Logout");
        }

    }

    public void processSearchLoginInfo() {
        try {
            DBLog.actionLog(this.getRequest(), getConnectionManager("dpms"), this);

            Box box = this.getInput();

            HashMap<String, Object> map = new HashMap<String, Object>();
            RecordSet rs = null;

            if ("id".equals(box.getString("chk"))) {

                rs = getSelect("dpms").searchLoginInfo(box);

                if (rs.nextRow()) {

                    map.put("result", 0);
                    map.put("message", "아이디는 \"" + rs.getString("user_id") + "\"입니다.");
                    PortalUtil.setResult(this.getResponse(), map);

                } else {

                    map.put("result", -1);
                    map.put("message", "성명과 전화번호를 확인하여 주십시오.\n휴대전화 번호가 변경된 경우에는 인사담당자에게 변경 요청후 재시도 하십시오.");
                    PortalUtil.setResult(this.getResponse(), map);

                }

            } else if ("pass".equals(box.getString("chk"))) {

                String str = "1,q,2,a,z,4,w,V,B,7,N,j,6,6,m,6,i,5,k,6,o,2,l,4,p,Q,7,W,8,E,9,R,1,g,2,b,4,y,5,T,6,Y,7,Z,8,X,9,C,8,9,U,7,I,O,6,7,s,x,5,P,6,M,1,2,f,v,6,t,1,h,2,n,3,u,4,A,5,S,6,D,7,F,8,G,3,4,5,e,8,d,9,c,r,H,4,J,5,K,3,L,0,a,z,4,w,V,2,B,3,N,j,m,5,i,4,k,6,o,7,l,p,Q,6,W,3,E,2,R,8,g,9,b,2,y,5,T,4,Y,6,Z,8,X,4,C,8,9,U,I,O,6,7,s,x,P,M,1,2,f,v,t,5,h,6,n,3,u,2,A,1,S,2,D,3,F,4,G,3,4,5,e,5,d,6,c,7,r,8,H,9,J,0,K,L,0";
                String[] rdata = StringUtils.split(str, ",");
                StringBuilder pwd = new StringBuilder();

                for (int i = 1; i <= 6; i++) {
                    int n = (int) (Math.random() * 190) + 1;

                    pwd.append(rdata[n]);
                }

                boolean ret = getSelect("dpms").initPassword(box, pwd.toString());

                if (ret) {

                    /*
                     * // 두레포털 SMS 서비스 이용. SendSmsServiceDo sevice = new SendSmsServiceDo(); // 실제
                     * 시스템 운영시 사용할 함수. // 테스트 중에는 sms를 관리자에게만 보낸다. String p_phonenumber =
                     * box.getString("condition2"); p_phonenumber = p_phonenumber.replace("+82",
                     * "0"); sevice.sendMessage(null, 2, p_phonenumber, PortalUtil.getSMSPhone(),
                     * "DPMS 비밀번호가 [ " + pwd.toString() + " ]로 변경 되었습니다.");
                     */

                    // 알리고 SMS API 이용 방식.
                    SMSUtils sms = new SMSUtils();
                    HashMap<String, Object> msg = new HashMap<String, Object>();
                    msg.put("msg", "비밀번호가 [ " + pwd.toString() + " ]로 초기화 되었습니다."); // 메시지내용.
                    msg.put("receiver", box.getString("condition2")); // 수신번호
                    msg.put("destination", box.getString("condition2") + "|사용자"); // 수신인 %고객명% 치환
                    msg.put("sender", getCodeSelect("dpms").getSMSSendPhone("doorepms")); // 발신번호 - 알리고에 등록된 대표 발신버호만 발송 가능.
                    msg.put("rdate", ""); // 예약일자 - 20161004 : 2016-10-04일기준
                    msg.put("rtime", ""); // 예약시간 - 1930 : 오후 7시30분
                    msg.put("testmode_yn", "N"); // Y 인경우 실제문자 전송X , 자동취소(환불) 처리
                    msg.put("title", "비밀번호 초기화"); // LMS, MMS 제목 (미입력시 본문중 44Byte 또는 엔터 구분자 첫라인)

                    String retStr = sms.sendSMS(getConnectionManager("dpms"), msg);
                    getLogger().debug(retStr);

                    map.put("result", 0);
                    map.put("message", "임시 비밀번호를 SMS로 발송하였습니다.\n임시비밀번호를 이용하여 로그인 하십시오.\n임시비밀번호를 수신하지 못하신 경우에는 담당자에게 휴대전화번호 변경을 요청후 재시도 하십시오.");
                } else {
                    map.put("result", -1);
                    map.put("message", "비밀번호를 초기화 할수 없습니다.\n아이디와 전화번호를 확인 후 재시도 하십시오.");
                }
                PortalUtil.setResult(this.getResponse(), map);

            }

        } catch (Exception e) {
            getLogger().error("processSearchLoginInfo login error", e);
            PortalUtil.setResult(this.getResponse(), e.getMessage());
        }
    }

    /**
     * 로그아웃 처리한다. 세션에서 로그인 정보를 제거하고 로그인 페이지로 이동한다.
     */
    public void processWebLogout() {
        try {
            DBLog.actionLog(this.getRequest(), getConnectionManager("dpms"), this);
            getLogger().debug("processWebLogout ");

            LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");

            this.setSessionAttribute("loginBean", null);
            this.setSessionAttribute("voc_request_phone", null);
            this.setSessionAttribute("vocGuestLogin", null);

            Enumeration<String> enumeration= getSession().getAttributeNames();
            while(enumeration.hasMoreElements()){
                String sessionName= enumeration.nextElement();
                setSessionAttribute(sessionName, null);
            }

            loginLogWrite(loginBean.getUserId(), "Logout");

            PortalUtil.setResult(this.getResponse(), 0, "LogOut 되었습니다.");
        } catch (Exception e) {
            getLogger().error("processWebLogout login error", e);
            PortalUtil.setResult(this.getResponse(), e.getMessage());
        }
    }

    /***********************************************************************************************************/
    private void setLoginBean(LoginBean loginBean, RecordSet rsParam) {
        try {
            DBLog.actionLog(this.getRequest(), getConnectionManager("dpms"), this);

            rsParam.moveRow(0);
            while (rsParam.nextRow()) {

                getLogger().debug("setLoginBean ");

                loginBean.setUserId(rsParam.getString("user_id"));
                loginBean.setEmplCode(rsParam.getString("empl_code"));
                loginBean.setUserName(rsParam.getString("user_name"));
                loginBean.setPassword(rsParam.getString("password"));

                loginBean.setPosition(rsParam.getString("position"));
                loginBean.setPhoneNumber(rsParam.getString("mobile_number"));

                loginBean.setDeptCode(rsParam.getString("dept_id"));
                loginBean.setDeptName(rsParam.getString("dept_name"));
                loginBean.setStr_UserEmail(rsParam.getString("email"));
                loginBean.setNaverworksMailPassword(rsParam.getString("nw_mail_pwd"));

                String server_ip = this.getRequest().getLocalAddr();
                getLogger().debug("server_ip=" + server_ip);
                String host = PortalUtil.getGwHost(server_ip);
                getLogger().debug("host=" + host);
                loginBean.setGwHost(host);

                loginBean.setCompanyName("두레시닝(주)");

            }
        } catch (Exception e) {
            getLogger().error("setLoginBean error", e);
        }
    }

    private void setSession(LoginBean loginBean) {
        getLogger().debug("setSession 1 ");
        // int sessionAliveTime = 60 * 60 * 2; // 2시간(세션)
        int sessionAliveTime = 60 * 60; // 60분.

        setSession(loginBean, sessionAliveTime);
    }

    private void setSession(LoginBean loginBean, int sessionAliveTime) {
        try {
            DBLog.actionLog(this.getRequest(), getConnectionManager("dpms"), this);

            getLogger().debug("setSession 2 ");

            setSessionAttribute("loginBean", loginBean);

            Map<String, String> nw_config = getCodeSelect("dpms").selectNaverApiConfig();
            setSessionAttribute("nwConfig", nw_config);

            getSession().setMaxInactiveInterval(sessionAliveTime);
        } catch (Exception e) {
            getLogger().error("setSession error", e);
        }
    }

}
