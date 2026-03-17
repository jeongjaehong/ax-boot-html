package com;

import framework.action.Box;
import framework.db.*;
import framework.util.StringUtil;

import java.sql.SQLException;
import java.util.HashMap;

public class LoginDao extends SelectDaoSupport {
	private String service = "";

	public LoginDao(ConnectionManager mgr) {
		super(mgr);
	}

	/**
	 * @return the service
	 */
	public String getService() {
		return service;
	}

	/**
	 * @param service the service to set
	 */
	public void setService(String service) {
		this.service = service;
	}

	/**
	 * 로그인을 위한 사용자 정보를 조회한다.
	 * 
	 * @param p_userid
	 * @param p_password
	 * @return
	 * @throws SQLException
	 */
	public RecordSet selectUserInfobyUserid(String p_userid, String p_password) throws SQLException {
		SQLCallableStatement cstmt = null;
		SelectConditionObject cond = new SelectConditionObject();

		StringBuilder query = new StringBuilder();
		query.append("{call pkg_system.p_users_r(? /* p_userid*/, ? /* p_password*/, ? /*ref cursor*/, ?/* ret code */, ? /*ret msg*/ )}");

		cstmt = getConnectionManager().createCallableStatement(query.toString());

		cond.setObject(p_userid);
		cond.setObject(p_password);

		cstmt.set(cond.getParameter());
		return cstmt.executeQuery();

	}

	public RecordSet selectUserInfobyUserName(String p_userid, String p_usernm, String p_password) throws SQLException {
		SelectConditionObject cond = new SelectConditionObject();
		StringBuilder query = new StringBuilder();

		query.append("\n/* " + this.getClass().toString() + ".selectUserInfobyUserName() */\n");
		query.append(" SELECT  ");
		query.append("      u.user_id as user_id ");
		query.append("      ,u.dept_id as dept_id ");
		query.append("      ,u.empl_code as empl_code ");
		query.append("      ,u.user_name as user_name ");
		query.append(" 		,? as password ");
		cond.setObject(p_password);
		query.append("      ,u.last_modify_date as last_modify_date ");
		query.append("      ,p.dept_name as dept_name ");
		query.append("      ,u.mobile_number as mobile_number ");
		query.append("      ,u.phone_number as phone_number ");
		query.append("      ,u.email as email ");
		query.append("      ,u.position as position ");
		query.append("      ,u.duty as duty ");
		query.append("      ,u.user_type as user_type ");
		query.append("      ,u.entry_date as entry_date ");
		query.append("      ,u.departure_date as departure_date ");
		query.append("      ,u.job as job ");
		query.append("      ,u.employ_type as employ_type ");
		query.append("      ,case when u.password = s.p(?) or u.password = ? or sso.key = s.p(?) then 'Y' else 'N' end as pwdchk ");
		cond.setObject(p_password);
		cond.setObject(p_password);
		cond.setObject(p_password);
		query.append("      ,case when sso.key = s.p(?) then 'Y' else 'N' end as ssochk ");
		cond.setObject(p_password);
		query.append("      ,sso.notify_target  ");
		query.append("      ,u.login_allow_yn as login_allow_yn ");
		query.append("      ,u.nw_mail_pwd as nw_mail_pwd ");
		query.append(" FROM  users u ");
		query.append(" left join department d  ON u.dept_id = d.dept_id  AND d.use_yn = 'Yes' ");
		query.append(" left join department p  ON d.i_parent_dept = p.dept_id  AND p.use_yn = 'Yes' ");
		query.append(" cross join  (SELECT KEY_VALUE AS key, server_ip || '@doore.co.kr' as notify_target FROM NAVER_API WHERE KEY_NAME = 'SSO') sso ");
		query.append(" WHERE  1 = 1 ");
		if (StringUtil.isNotEmpty(p_userid)) {
			query.append(" AND u.user_id = ?  ");
			cond.setObject(p_userid);
		} else {
			query.append(" AND ( u.user_name like '%' || ? || '%'  ");
			cond.setObject(p_usernm);
			query.append(" OR p.dept_name like '%' || ? || '%' ) ");
			cond.setObject(p_usernm);
		}

		getLogger().debug(query.toString());

		return select(query.toString(), cond.getParameter());
	}

	public RecordSet searchLoginInfo(Box box) throws SQLException {
		SelectConditionObject cond = new SelectConditionObject();
		StringBuilder query = new StringBuilder();

		query.append("\n/* " + this.getClass().toString() + ".searchLoginInfo() */\n");
		query.append(" SELECT  ");
		query.append("      u.user_id as user_id ");
		query.append("      ,u.empl_code as empl_code ");
		query.append("      ,u.user_name as user_nm ");
		query.append("      ,u.password as password ");
		query.append(" FROM  users u ");
		query.append(" WHERE  1 = 1 ");

		// 재입사자 또는 동명2인의 경우 1, 2, A, B처럼 본명뒤에 별칭을 입력하므로... like검색을 수행한다.
		query.append(" AND u.user_name like '%' || ? || '%' ");
		cond.setObject(box.getString("condition1"));

		query.append(" AND replace(u.mobile_number, '-', '') = ? ");
		cond.setObject(box.getString("condition2"));

		return select(query.toString(), cond.getParameter());
	}

	public RecordSet selectLesseeInfo(Box box) throws SQLException {
		SelectConditionObject cond = new SelectConditionObject();
		StringBuilder query = new StringBuilder();

		query.append("\n/* " + this.getClass().toString() + ".selectLesseeInfo() */\n");
		query.append(" SELECT  ");
		query.append("      u.lessee_id as user_id ");
		query.append("      ,u.lessee_id as empl_code ");
		query.append("      ,u.lessee_name as user_name ");
		query.append("      ,'********' as password ");
		query.append("      ,personal_mobile as mobile_number ");
		query.append("      ,personal_email as email ");
		query.append("    ,( case when  replace(?, '-', '') in ( u.personal_number, u.corp_number ) or  replace(?, '-', '') in ( u.personal_number, u.corp_number )  then 'Y' else 'N' end) as pw_check ");
		cond.setObject(box.getString("user_key"));
		cond.setObject(box.getString("lessee_id"));
		query.append(" FROM  lessee u ");
		query.append(" WHERE  1 = 1  ");
		query.append("    AND ( replace(?, '-', '') in (u.personal_number, u.corp_number) ");
		cond.setObject(box.getString("user_key"));
		query.append("    or replace(?, '-', '') in (u.personal_number, u.corp_number )  ");
		cond.setObject(box.getString("lessee_id"));
		query.append("  ) ");

		return select(query.toString(), cond.getParameter());
	}


	public RecordSet selectOwnerInfo(Box box) throws SQLException {
		SelectConditionObject cond = new SelectConditionObject();
		StringBuilder query = new StringBuilder();

		query.append("\n/* " + this.getClass().toString() + ".selectOwnerInfo() */\n");
		query.append(" SELECT  ");
		query.append("       u.owner_id as user_id ");
		query.append("      ,u.owner_id as empl_code ");
		query.append("      ,b.building_name as user_name ");
		query.append("      ,'********' as password ");
		query.append("      ,o.personal_mobile as mobile_number ");
		query.append("      ,o.personal_email as email ");

		query.append("    ,( case when s.p(replace(?,'-','')) in ( u.password,  s.p(o.personal_number), s.p(o.corp_number) ) ");
		cond.setObject(box.getString("pw"));

		query.append("  then 'Y' else 'N' end ) as pw_check ");

		query.append(" FROM building_owner u ");
		query.append(" 		inner join building b on b.building_id = u.building_id  ");
		query.append(" 		inner join owners o on o.owner_id = u.owner_id  ");
		query.append(" WHERE  1 = 1  ");
		query.append("    AND ( replace(b.building_name,' ','') = replace(?,' ','')  ");
		cond.setObject(box.getString("id"));
		query.append("    or replace(o.owner_name,' ','') = replace(?,' ','')  ");
		cond.setObject(box.getString("id"));
		query.append("    or replace(o.corp_name,' ','') = replace(?,' ','')  ");
		cond.setObject(box.getString("id"));
		query.append("  ) ");

		getLogger().debug(query.toString());

		return select(query.toString(), cond.getParameter());
	}

	public RecordSet selectOwnerID(Box box) throws SQLException {
		SelectConditionObject cond = new SelectConditionObject();
		StringBuilder query = new StringBuilder();

		query.append("\n/* " + this.getClass().toString() + ".selectOwnerID() */\n");
		query.append(" SELECT  ");
		query.append("       o.owner_name as owner_name ");
		query.append("      ,o.corp_name as corp_name ");
		query.append("      ,b.building_name as building_name ");
		query.append(" FROM building_owner u ");
		query.append(" 		inner join building b on b.building_id = u.building_id  ");
		query.append(" 		inner join owners o on o.owner_id = u.owner_id  ");
		query.append(" WHERE  1 = 1  ");
		query.append("    AND o.owner_name = ?   ");
		cond.setObject(box.getString("owner_name"));
		query.append("    AND o.personal_mobile = replace(?,'-','')  ");
		cond.setObject(box.getString("personal_mobile"));
		query.append("    AND replace(?,'-','') in (o.personal_number, o.corp_number)  ");
		cond.setObject(box.getString("corp_number"));

		getLogger().debug(query.toString());

		return select(query.toString(), cond.getParameter());
	}

	public boolean initOwnerPassword(Box box) {
		int rscnt = 0;
		try {
			SelectConditionObject cond = new SelectConditionObject();

			StringBuilder query = null;

			query = new StringBuilder();
			query.append("\n/* " + this.getClass().toString() + ".initOwnerPassword() */\n");
			query.append("update building_owner t ");
			query.append(" set t.password = s.p(?) ");
			cond.setObject(box.getString("new_password"));
			query.append(" where 1=1 ");
			query.append("   and exists ( select 1 from owners o  ");
			query.append("        inner join building_owner u on o.owner_id = u.owner_id  ");
			query.append("        inner join building b on u.building_id = b.building_id   ");
			query.append("       where (o.owner_name = ?   ");
			cond.setObject(box.getString("user_id"));
			query.append("       or o.corp_name = ?   ");
			cond.setObject(box.getString("user_id"));
			query.append("       or b.building_name = ? )   ");
			cond.setObject(box.getString("user_id"));
			query.append("       and o.personal_mobile = replace(?, '-', '')   ");
			cond.setObject(box.getString("personal_mobile"));
			query.append("       and t.building_id = u.building_id   ");
			query.append("       and t.owner_id = u.owner_id   ");
			query.append("   ) /*exists*/  ");

			SQLPreparedStatement pstmt = getConnectionManager().createPrepareStatement(query.toString());
			pstmt.set(cond.getParameter());

			rscnt = pstmt.executeUpdate();

			getConnectionManager().commit();

			pstmt.close();
		} catch (SQLException e) {
			getConnectionManager().rollback();
			e.printStackTrace();
			return false;
		}

		return rscnt > 0;

	}


	public boolean changeOwnerPassword(Box box) {
		int rscnt = 0;
		try {
			SelectConditionObject cond = new SelectConditionObject();

			StringBuilder query = null;

			query = new StringBuilder();
			query.append("\n/* " + this.getClass().toString() + ".changeOwnerPassword() */\n");
			query.append("update building_owner t ");
			query.append(" set t.password = s.p(?) ");
			cond.setObject(box.getString("new_password"));
			query.append(" where 1=1 ");
			query.append("   and exists ( select 1 from owners o  ");
			query.append("        inner join building_owner u on o.owner_id = u.owner_id  ");
			query.append("        inner join building b on u.building_id = b.building_id   ");
			query.append("       where o.owner_id = ?   ");
			cond.setObject(box.getLong("user_id"));
			query.append("       and t.building_id = u.building_id   ");
			query.append("       and t.owner_id = u.owner_id   ");
			query.append("       and (t.password = s.p(?) or t.password is null)   ");
			cond.setObject(box.getString("old_password"));
			query.append("   ) /*exists*/  ");

			SQLPreparedStatement pstmt = getConnectionManager().createPrepareStatement(query.toString());
			pstmt.set(cond.getParameter());

			rscnt = pstmt.executeUpdate();

			getConnectionManager().commit();

			pstmt.close();
		} catch (SQLException e) {
			getConnectionManager().rollback();
			e.printStackTrace();
			return false;
		}

		return rscnt > 0;

	}


	public boolean initPassword(Box box, String new_pwd) {
		int rscnt = 0;
		try {
			SelectConditionObject cond = null;
			StringBuilder query = null;

			query = new StringBuilder();
			query.append("\n/* " + this.getClass().toString() + ".initPassword() */\n");
			query.append("update users ");
			query.append(" set password = s.p(?) ");
			query.append(" where 1=1 ");
			query.append("   and user_id = ? ");
			query.append("   and replace(mobile_number, '-', '') = ? ");

			SQLPreparedStatement pstmt = getConnectionManager().createPrepareStatement(query.toString());

			cond = new SelectConditionObject();
			cond.setObject(new_pwd);
			cond.setObject(box.getString("condition1"));
			cond.setObject(box.getString("condition2"));

			pstmt.set(cond.getParameter());

			rscnt = pstmt.executeUpdate();

			getConnectionManager().commit();

			pstmt.close();
		} catch (SQLException e) {
			getConnectionManager().rollback();
			e.printStackTrace();
			return false;
		}

		return rscnt > 0;

	}

	/**
	 * 로그인 정보를 기록한다.
	 * 
	 * @return
	 * @throws SQLException
	 */
	public HashMap<String, Object> saveLoginLog(String p_userid, String p_status, String p_agent, String p_ip) throws SQLException {

		HashMap<String, Object> result = new HashMap<String, Object>();
		SelectConditionObject cond = null;

		SQLCallableStatement cstmt = null;

		StringBuilder query = new StringBuilder();
		query.append("{call pkg_system.p_login_log_c (?, ?, ?, ?, ?, ? )}");

		cstmt = getConnectionManager().createCallableStatement(query.toString());

		// for 루프에서 변수 초기화.
		cond = new SelectConditionObject();

		cond.setObject(p_userid);
		cond.setObject(p_status);
		cond.setObject(p_agent);
		cond.setObject(p_ip);

		cstmt.clearParam(); // 기존 파라미터 값을 초기화 한다.
		cstmt.set(cond.getParameter()); // 새로운 값을 대입한다.

		// 쿼리를 실행한다.
		cstmt.executeUpdate();

		// 결과 코드와 메시지를 가져온다.
		result.put("result", cstmt.getRetCode());
		result.put("message", cstmt.getRetMessage());

		if (cstmt.getRetCode() < 0) {
			// 프로시져 호출 결과가 오류인 경우 롤백하고, 리턴한다.
			getConnectionManager().rollback();

			return result;
		}

		// 오류 없이 실행된 경우 커밋한다.
		getConnectionManager().commit();

		return result;
	}

}
