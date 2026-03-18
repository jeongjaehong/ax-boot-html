package com;

import framework.action.Action;
import framework.db.RecordSet;
import jakarta.servlet.http.Cookie;
import util.DBLog;
import util.PortalUtil;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class LoginAction extends Action {

    private LoginDao dao = null;
    private static final String LOGIN_SERVICE = "default";

    private LoginDao getSelect(String service) throws Exception {
        if (dao == null || !dao.getService().equals(service)) {
            dao = new LoginDao(getConnectionManager(service));
            dao.setService(service);
        }
        return dao;
    }

    /**
     * 로그인을 처리한다.
     */
    public void processLogin() {
        try {
            getLogger().debug("processLogin Start");
            try {
                DBLog.actionLog(this.getRequest(), getConnectionManager(LOGIN_SERVICE), this);
            } catch (Exception e) {
                getLogger().warn("DBLog.actionLog failed", e);
            }

            String userid = getInput().getString("user_id");
            String password = getInput().getString("password");
            LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");
            String redirectUrl = this.getRequest().getContextPath() + "/main.jsp";
            boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(this.getRequest().getHeader("X-Requested-With"));
            boolean rememberId = "on".equalsIgnoreCase(this.getRequest().getParameter("rememberId"));

            getLogger().debug("processLogin called: user_id=" + userid);

            if (loginBean != null) {
                if (isAjax) {
                    PortalUtil.setResult(this.getResponse(), 0, "메인 화면으로 이동합니다.", "redirectUrl", redirectUrl);
                } else {
                    this.getResponse().sendRedirect(redirectUrl);
                }
            } else {
                RecordSet rs = getSelect(LOGIN_SERVICE).selectUserInfobyUserid(userid, password);

                if (rs.nextRow()) {

                    loginBean = new LoginBean();

                    if (!"Yes".equals(rs.getString("login_allow_yn"))) {
                        PortalUtil.setResult(this.getResponse(), "관리자에 의해 로그인이 차단되어 있습니다.");

                    } else {
                        if ("Yes".equals(rs.getString("pwdchk"))) {
                            setLoginBean(loginBean, rs);

                            setSession(loginBean);
                            setRememberIdCookie(userid, rememberId);

                            if (isAjax) {
                                PortalUtil.setResult(this.getResponse(), 0, "succ", "redirectUrl", redirectUrl);
                            } else {
                                this.getResponse().sendRedirect(redirectUrl);
                            }
                        } else {

                            PortalUtil.setResult(this.getResponse(), "사용자 비밀번호를 확인 하십시오.");
                        }
                    }
                } else {

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



    /***********************************************************************************************************/
    private void setLoginBean(LoginBean loginBean, RecordSet rsParam) {
        try {
            DBLog.actionLog(this.getRequest(), getConnectionManager("default"), this);

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

                loginBean.setCompanyName(PortalUtil.getCompanyName("company_name"));

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
            DBLog.actionLog(this.getRequest(), getConnectionManager("default"), this);

            getLogger().debug("setSession 2 ");

            setSessionAttribute("loginBean", loginBean);

            getSession().setMaxInactiveInterval(sessionAliveTime);
        } catch (Exception e) {
            getLogger().error("setSession error", e);
        }
    }

    private void setRememberIdCookie(String userid, boolean rememberId) {
        try {
            String encodedUserId = URLEncoder.encode(userid == null ? "" : userid, StandardCharsets.UTF_8);
            int maxAge = rememberId ? 60 * 60 * 24 * 365 : 0;
            Cookie cookie = new Cookie("saved_userid", encodedUserId);
            cookie.setPath("/");
            cookie.setMaxAge(maxAge);
            this.getResponse().addCookie(cookie);
        } catch (Exception e) {
            getLogger().warn("setRememberIdCookie failed", e);
        }
    }

}
