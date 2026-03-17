package com;

import framework.action.Box;
import framework.db.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class CodeManageDao extends SelectDaoSupport {
	private String service = "";

	public CodeManageDao(ConnectionManager mgr) {
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
	 * 공통코드 관리 - 공통코드 테이블 목록 - 조회
	 * 
	 * @param box
	 * @return
	 * @throws SQLException
	 */
	public RecordSet searchCodeManageHead(Box box) throws SQLException {
		SQLCallableStatement cstmt = null;
		SelectConditionObject cond = new SelectConditionObject();

		StringBuilder query = new StringBuilder();
		query.append("{call pkg_admin.p_code_manage_head_r ( ?/* search_keyword*/, ? /*ref cursor*/, ?/* ret code */, ? /*ret msg*/ )}");

		cstmt = getConnectionManager().createCallableStatement(query.toString());

		cond.setObject(box.getString("search_keyword"));

		cstmt.set(cond.getParameter());
		return cstmt.executeQuery();
	}

	/**
	 * 공통코드 관리 - 공통코드 테이블 목록 - 저장
	 * 
	 * @return
	 * @throws SQLException
	 */
	public HashMap<String, Object> saveCodeManageHead(ArrayList<HashMap<String, Object>> jarray, LoginBean loginBean) throws SQLException {

		HashMap<String, Object> result = new HashMap<String, Object>();
		SelectConditionObject cond = null;

		SQLCallableStatement cstmt = null;

		if (getLogger().isDebugEnabled()) {
			// 서버로 전달된 수정된 행에 대한 정보를 콘솔 로그로 출력해 본다.
			for (HashMap<String, Object> map : jarray) {
				getLogger().debug("map=" + map);
			}
		}

		StringBuilder query = new StringBuilder();

		query.append("{call pkg_admin.p_code_manage_head_u (? /* table_name */, ? /*comments*/, ?/* ret code */, ? /*ret msg*/ )}");

		cstmt = getConnectionManager().createCallableStatement(query.toString());

		for (HashMap<String, Object> map : jarray) {

			// for 루프에서 변수 초기화.
			cond = new SelectConditionObject();

			cond.setObject(map.get("table_name"));
			cond.setObject(map.get("comments"));

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

		}

		// 오류 없이 실행된 경우 커밋한다.
		getConnectionManager().commit();

		return result;
	}

	/**
	 * 공통코드 관리 - 코드목록 - 조회
	 * 
	 * @param box
	 * @return
	 * @throws SQLException
	 */
	public RecordSet searchCodeManageDetail(Box box) throws SQLException {
		SQLCallableStatement cstmt = null;
		SelectConditionObject cond = new SelectConditionObject();

		StringBuilder query = new StringBuilder();
		query.append("{call pkg_admin.p_code_manage_detail_r ( ? /*table_name*/ , ?/* search_keyword*/, ?/* gubn */ , ? /*ref cursor*/, ?/* ret code */, ? /*ret msg*/ )}");

		cstmt = getConnectionManager().createCallableStatement(query.toString());

		cond.setObject(box.getString("table_name"));
		cond.setObject(box.getString("search_keyword"));
		cond.setObject(box.getString("gubn"));

		cstmt.set(cond.getParameter());
		return cstmt.executeQuery();
	}

	/**
	 * 공통코드 관리 - 코드목록 - 저장
	 * 
	 * @return
	 * @throws SQLException
	 */
	public HashMap<String, Object> saveCodeManage(ArrayList<HashMap<String, Object>> jarray, LoginBean loginBean) throws SQLException {

		HashMap<String, Object> result = new HashMap<String, Object>();
		SelectConditionObject cond = null;

		SQLCallableStatement cstmt = null;

		if (getLogger().isDebugEnabled()) {
			// 서버로 전달된 수정된 행에 대한 정보를 콘솔 로그로 출력해 본다.
			for (HashMap<String, Object> map : jarray) {
				getLogger().debug("map=" + map);
			}
		}

		StringBuilder query = new StringBuilder();

		query.append("{call pkg_admin.p_code_manage_cu (? /* table_name */, ? /*column_name*/, ? /* description */, ? /* use_yn */, ? /*i_sort_order*/, ? /* registrant */, ?/* ret code */, ? /*ret msg*/ )}");

		cstmt = getConnectionManager().createCallableStatement(query.toString());

		for (HashMap<String, Object> map : jarray) {

			// for 루프에서 변수 초기화.
			cond = new SelectConditionObject();

			cond.setObject(map.get("table_name"));
			cond.setObject(map.get("column_name"));
			cond.setObject(map.get("description"));
			cond.setObject(map.get("use_yn"));
			cond.setObject(map.get("i_sort_order"));
			cond.setObject(loginBean.getUserId() + "");

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

		}

		// 오류 없이 실행된 경우 커밋한다.
		getConnectionManager().commit();

		return result;
	}

	/**
	 * 공통코드 관리 - 코드목록 - 삭제
	 * 
	 * @return
	 * @throws SQLException
	 */
	public HashMap<String, Object> deleteCodeManage(ArrayList<HashMap<String, Object>> jarray, LoginBean loginBean) throws SQLException {

		HashMap<String, Object> result = new HashMap<String, Object>();
		SelectConditionObject cond = null;

		SQLCallableStatement cstmt = null;

		if (getLogger().isDebugEnabled()) {
			// 서버로 전달된 수정된 행에 대한 정보를 콘솔 로그로 출력해 본다.
			for (HashMap<String, Object> map : jarray) {
				getLogger().debug("map=" + map);
			}
		}

		StringBuilder query = new StringBuilder();
		query.append("{call pkg_admin.p_code_manage_d (? /* table_name */, ? /* column_name */, ? /*  ret_code */, ? /*ret_msg*/ )}");

		cstmt = getConnectionManager().createCallableStatement(query.toString());

		for (HashMap<String, Object> map : jarray) {

			// for 루프에서 변수 초기화.
			cond = new SelectConditionObject();

			cond.setObject(map.get("table_name"));
			cond.setObject(map.get("column_name"));

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

		}

		// 오류 없이 실행된 경우 커밋한다.
		getConnectionManager().commit();

		return result;
	}

}
