package util;

import com.LoginBean;
import framework.action.Action;
import framework.db.ConnectionManager;
import framework.db.RecordSet;
import framework.db.SQLCallableStatement;
import framework.db.SelectConditionObject;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GrantUtil {
	private static String grantSQL = "call pkg_system.p_menu_grant (?, ?, ?, ?, ? )";

	private static Log _logger = LogFactory.getLog(Action.class);

	private static Log getLogger() {
		return GrantUtil._logger;
	}

	public static String getAuthority(HttpServletRequest request, ConnectionManager connMgr) {
		String result = "";
		String userid = "";
		try {

			//TODO: 임시로 localhost 테스트 중 오류 발생되지 않도록 리턴.
			//if("0:0:0:0:0:0:0:1".equals(request.getLocalAddr())) return "저장";

			String referer = request.getHeader("referer");
			String path[] = referer.split("/");
			String program = path[path.length - 1];
			path = program.split("\\?");
			if (path.length > 0) {
				program = path[0];
			}

			LoginBean l_session = (LoginBean) request.getSession().getAttribute("loginBean");

			if (l_session != null) {
				userid = l_session.getUserId();

				SQLCallableStatement pstmt = connMgr.createCallableStatement(grantSQL);
				SelectConditionObject cond = new SelectConditionObject();

				cond.setObject(userid); // p_userid
				cond.setObject(program); // p_username

				pstmt.set(cond.getParameter());

				RecordSet rs = pstmt.executeQuery();
				if (rs.nextRow()) {
					result =  rs.getString("authority");
					getLogger().debug("\nDB Result " + result);
				} else {
					result = "Not found " + program;
				}
			}else{
				getLogger().debug("\n Not login ");
				result = program;
			}

		} catch (Exception e) {
			e.printStackTrace();
			getLogger().error(e);
			result = e.getMessage();
		}
		getLogger().debug("\n" + userid + " Authority is " + result);
		return result;
	}
}
