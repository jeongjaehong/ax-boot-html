package util;

import com.LoginBean;
import framework.action.Action;
import framework.action.Box;
import framework.config.Configuration;
import framework.db.ConnectionManager;
import framework.db.SQLPreparedStatement;
import framework.db.SelectConditionObject;
import framework.util.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

public class DBLog {
	// call pkg_log.mobile ( p_loglevel, p_message, p_executesql, p_ipaddress,
	// p_useragent, p_modulename, p_actionname, p_userid, p_username )
	private static String logSQL = "call pkg_log.was_log (?, ?, ?, ?, ?, ?, ?, ?, ? )";
	// p_errcode IN INTEGER , p_errmsg IN VARCHAR2 , p_sqlstr IN VARCHAR2:= NULL ,
	// p_logcls IN VARCHAR2:= NULL , p_enterid IN VARCHAR2:= NULL );

	private static Log _logger = LogFactory.getLog(DBLog.class);

	public static void actionLog(HttpServletRequest request, ConnectionManager connMgr, Action caller) {
		getLogger().debug("\n Start Action logging...");
		dbLogging("action", request, connMgr, null, caller, false);
	}

	private static void dbLogging(String level, HttpServletRequest request, ConnectionManager connMgr, Exception exception, Action caller, boolean isStackTrace) {
		try {

			LoginBean l_session = (LoginBean) request.getSession().getAttribute("loginBean");
			String l_moduleName = Configuration.getInstance().getString("appinfo.module") + "/" + caller.getClass().getSimpleName();
			String userid = "SYSLOG";
			String username = "SystemLoger";
			if (l_session != null) {
				userid = l_session.getUserId();
				username = l_session.getUserName();
			}

			String l_actionName = null;
			if (request.getParameter("action") != null) {
				l_actionName = request.getParameter("action");
			} else {
				l_actionName = "init";
			}

			String l_errorMsg = "";
			if (exception == null) {
				l_errorMsg = request.getHeader("referer");
			} else {
				if (isStackTrace) {
					// 오류메시지 문자열 저장
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw, true);
					exception.printStackTrace(pw);
					l_errorMsg = sw.toString();
					pw.close();
					sw.close();
				} else {
					l_errorMsg = StringUtil.nullToBlankString(exception.getMessage());
				}
			}

			String l_sqltext = Box.getBox(request).toString();
			// 로그메시지 디비에 저장
			SQLPreparedStatement pstmt = connMgr.createPrepareStatement(logSQL);
			SelectConditionObject cond = new SelectConditionObject();

			cond.setObject(level); // p_loglevel
			cond.setObject(l_errorMsg.length() >= 3000 ? l_errorMsg.substring(0, 3000) : l_errorMsg); // p_message
			cond.setObject(l_sqltext.length() >= 3000 ? l_sqltext.substring(0, 3000) : l_sqltext); // p_executesql
			cond.setObject(request.getRemoteAddr()); // p_ipaddress
			cond.setObject(request.getHeader("user-agent")); // p_useragent

			cond.setObject(l_moduleName); // p_modulename
			cond.setObject(l_actionName); // p_actionname
			cond.setObject(userid); // p_userid
			cond.setObject(username); // p_username

			pstmt.set(cond.getParameter());

			//pstmt.executeUpdate();
			//connMgr.commit();
			pstmt.close();

		} catch (Exception e) {
			connMgr.rollback();
			e.printStackTrace();
			getLogger().error(e);
		}
	}

	public static void errorLog(HttpServletRequest request, ConnectionManager connMgr, Exception exception, Action caller) {
		getLogger().debug("errorLog");
		dbLogging("error", request, connMgr, exception, caller, true);
	}

	private static Log getLogger() {
		return DBLog._logger;
	}
}
