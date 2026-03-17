package com;

import framework.action.Box;
import framework.db.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class PopupDao extends SelectDaoSupport {
	private String service = "";

	public PopupDao(ConnectionManager mgr) {
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
	 * 팝업키를 이용하여 등록된 쿼리를 파싱해서 쿼리 실행결과를 리턴한다.
	 * 
	 * @param box
	 * @return
	 * @throws SQLException
	 */
	public RecordSet searchData(Box box) throws SQLException {

		SQLCallableStatement cstmt = null;
		SelectConditionObject cond = new SelectConditionObject();

		StringBuilder query = new StringBuilder();

		if (box.containsKey("refkey3")) {
			query.append("{call pkg_popup.p_search (? /*popup_key*/, ? /*keyword*/, ? /*refkey*/, ? /*refkey2*/,  ? /*refkey3*/, ? /*ref cursor*/, ?/* ret code */, ? /*ret msg*/ )}");
		} else if (box.containsKey("refkey2")) {
			query.append("{call pkg_popup.p_search (? /*popup_key*/, ? /*keyword*/, ? /*refkey*/, ? /*refkey2*/, ? /*ref cursor*/, ?/* ret code */, ? /*ret msg*/ )}");
		} else {
			query.append("{call pkg_popup.p_search (? /*popup_key*/, ? /*keyword*/, ? /*refkey*/, ? /*ref cursor*/, ?/* ret code */, ? /*ret msg*/ )}");
		}
		cstmt = getConnectionManager().createCallableStatement(query.toString());

		cond.setObject(box.getString("popup_key"));
		cond.setObject(box.getString("keyword"));
		cond.setObject(box.getString("refkey"));
		if (box.containsKey("refkey2")) {
			cond.setObject(box.getString("refkey2"));
		}
		if (box.containsKey("refkey3")) {
			cond.setObject(box.getString("refkey3"));
		}
		cstmt.set(cond.getParameter());
		return cstmt.executeQuery();
	}

	/**
	 * 팝업화면 또는 드롭다운리스트 박스의 컨텐츠를 생성하기 위해 등록된 쿼리를 조회한다.
	 * 
	 * @param box
	 * @return
	 * @throws SQLException
	 */
	public RecordSet searchPopupQuery(Box box) throws SQLException {
		SQLCallableStatement cstmt = null;
		SelectConditionObject cond = new SelectConditionObject();

		StringBuilder query = new StringBuilder();
		query.append("{call pkg_popup.p_popup_query_r (? /*keyword*/, ? /*ref cursor*/, ?/* ret code */, ? /*ret msg*/ )}");

		cstmt = getConnectionManager().createCallableStatement(query.toString());

		cond.setObject(box.getString("keyword"));

		cstmt.set(cond.getParameter());
		return cstmt.executeQuery();
	}

	/**
	 * 
	 * @param jarray
	 * @param loginBean
	 * @return
	 * @throws SQLException
	 */
	public HashMap<String, Object> savePopupQuery(ArrayList<HashMap<String, Object>> jarray, LoginBean loginBean) throws SQLException {

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
		query.append("{call pkg_popup.p_popup_query_cu (?, ?, ?, ?, ?, ?, ? )}");

		cstmt = getConnectionManager().createCallableStatement(query.toString());

		for (HashMap<String, Object> map : jarray) {

			// for 루프에서 변수 초기화.
			cond = new SelectConditionObject();

			cond.setObject(map.get("popup_key"));
			cond.setObject(map.get("description"));
			cond.setObject(map.get("query"));
			cond.setObject(map.get("use_yn"));
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

}
