package com;

import framework.action.Action;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class LogoutAction extends Action {
    public void processLogout() throws IOException {
        HttpSession session = getSession(false);
        if (session != null) {
            session.invalidate();
        }
        getResponse().sendRedirect(getRequest().getContextPath() + "/login.jsp");
    }
}
