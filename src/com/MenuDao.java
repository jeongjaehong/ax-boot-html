package com;

import framework.action.Box;
import framework.db.*;

import java.sql.SQLException;
import java.util.HashMap;

public class MenuDao extends SelectDaoSupport {
	private String service = "";

	public MenuDao(ConnectionManager mgr) {
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
	 * 화면 열람 로그를 등록한다 .
	 * 
	 * @param box
	 * @param loginBean
	 * @return
	 * @throws SQLException
	 */
	public HashMap<String, Object> saveOpenLog(Box box, LoginBean loginBean) throws SQLException {

		HashMap<String, Object> result = new HashMap<String, Object>();
		SelectConditionObject cond = null;

		SQLCallableStatement cstmt = null;

		StringBuilder query = new StringBuilder();
		query.append("{call pkg_admin.p_program_open_log_c (?, ?, ?, ? )}");

		cstmt = getConnectionManager().createCallableStatement(query.toString());

		// for 루프에서 변수 초기화.
		cond = new SelectConditionObject();

		cond.setObject(box.getString("pathname"));
		cond.setObject(loginBean.getUserId() + "");

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

	/**
	 * 선택된 상위 프로그램 아이디를 이용하여 프로그램 목록을 조회하여 리턴한다.
	 * 
	 * @param p_userid
	 * @param p_startmenu
	 * @return
	 * @throws SQLException
	 */
	public RecordSet selectMenuList(String p_userid, long p_startmenu) throws SQLException {

		SQLCallableStatement cstmt = null;
		SelectConditionObject cond = new SelectConditionObject();

		StringBuilder query = new StringBuilder();
		query.append("{call pkg_system.p_menu_r (? /*user_id*/, ? /*start_id*/, ? /*ref cursor*/, ?/* ret code */, ? /*ret msg*/ )}");

		cstmt = getConnectionManager().createCallableStatement(query.toString());

		cond.setObject(p_userid);
		cond.setObject(p_startmenu);

		cstmt.set(cond.getParameter());
		return cstmt.executeQuery();

	}

	/**
	 * 등록된 즐겨찾기를 조회한다.
	 * 
	 * @param p_userid
	 * @param p_startmenu
	 * @return
	 * @throws SQLException
	 */
	public RecordSet selectFavorite(String p_userid) throws SQLException {

		SQLCallableStatement cstmt = null;
		SelectConditionObject cond = new SelectConditionObject();

		StringBuilder query = new StringBuilder();
		query.append("{call pkg_system.p_favorite_r (? /*user_id*/, ? /*ref cursor*/, ?/* ret code */, ? /*ret msg*/ )}");

		cstmt = getConnectionManager().createCallableStatement(query.toString());

		cond.setObject(p_userid);

		cstmt.set(cond.getParameter());
		return cstmt.executeQuery();

	}

	public RecordSet selectTop5(String p_userid) throws SQLException {

		SQLCallableStatement cstmt = null;
		SelectConditionObject cond = new SelectConditionObject();

		StringBuilder query = new StringBuilder();
		query.append("{call pkg_system.p_topmenu_r (? /*user_id*/, ? /*ref cursor*/, ?/* ret code */, ? /*ret msg*/ )}");

		cstmt = getConnectionManager().createCallableStatement(query.toString());

		cond.setObject(p_userid);

		cstmt.set(cond.getParameter());
		return cstmt.executeQuery();

	}

	/**
	 * 즐겨찾기를 등록하거나, 삭제한다.
	 * 
	 * @param favorite_seq
	 * @param program_id
	 * @param loginBean
	 * @return
	 * @throws SQLException
	 */
	public HashMap<String, Object> toggleFavorite(int favorite_seq, int program_id, LoginBean loginBean) throws SQLException {

		HashMap<String, Object> result = new HashMap<String, Object>();
		SelectConditionObject cond = null;

		SQLCallableStatement cstmt = null;

		StringBuilder query = new StringBuilder();
		query.append("{call pkg_admin.p_favorites_cd (?, ?, ?, ?, ? )}");

		cstmt = getConnectionManager().createCallableStatement(query.toString());

		cond = new SelectConditionObject();

		cond.setObject(favorite_seq);
		cond.setObject(loginBean.getUserId() + "");
		cond.setObject(program_id);

		cstmt.clearParam(); // 기존 파라미터 값을 초기화 한다.
		cstmt.set(cond.getParameter()); // 새로운 값을 대입한다.

		getLogger().debug("cond=" + cond);
		getLogger().debug("param size=" + cond.getParameter().length);

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
