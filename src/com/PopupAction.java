package com;

import framework.action.Action;
import framework.action.Box;
import framework.db.RecordSet;
import framework.util.JsonUtil;
import util.DBLog;
import util.PortalUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class PopupAction extends Action {

	private CodeDao codeDao = null;

	private CodeDao getCodeSelect(String service) throws Exception {
		if (codeDao == null || !codeDao.getService().equals(service)) {
			codeDao = new CodeDao(getConnectionManager(service));
			codeDao.setService(service);
		}
		return codeDao;
	}

	private PopupDao dao = null;

	private PopupDao getSelect(String service) throws Exception {
		if (dao == null || !dao.getService().equals(service)) {
			dao = new PopupDao(getConnectionManager(service));
			dao.setService(service);
		}
		return dao;
	}

	public void processSearch() {

		DBLog.actionLog(this.getRequest(), getConnectionManager("default"), this);

		try {
			Box box = this.getInput();

			getLogger().debug(box);

			RecordSet rs = getSelect("default").searchData(box);

			PortalUtil.setResult(this.getResponse(), rs, true);

		} catch (Exception e) {
			PortalUtil.setResult(this.getResponse(), -1, e.getMessage());

			getLogger().error(e);
			e.printStackTrace();
		}

	}

	/**
	 * 공통 팝업을 위한 쿼리 정의 내역을 조회한다.
	 */
	public void processPopupQuerySearch() {

		DBLog.actionLog(this.getRequest(), getConnectionManager("default"), this);

		try {
			Box box = this.getInput();

			getLogger().debug(box);

			RecordSet rs = getSelect("default").searchPopupQuery(box);

			PortalUtil.setResult(this.getResponse(), rs, true);

		} catch (Exception e) {
			PortalUtil.setResult(this.getResponse(), -1, e.getMessage());

			getLogger().error(e);
			e.printStackTrace();
		}

	}

	/**
	 * 공통 팝업을 위한 쿼리 정의 내역을 조회한다.
	 */
	public void processSavePopupQuery() {

		DBLog.actionLog(this.getRequest(), getConnectionManager("default"), this);
		HashMap<String, Object> map = null;
		try {

			LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");

			if (null == loginBean) {
				PortalUtil.setResult(this.getResponse(), -99, "다시 로그인 하십시오.");
				return;
			}

			map = new HashMap<String, Object>();
			Box box = this.getInput();

			ArrayList<HashMap<String, Object>> jarray = (ArrayList<HashMap<String, Object>>) JsonUtil.parse(box.getRawString("rows"));
			map = getSelect("default").savePopupQuery(jarray, loginBean);

			getLogger().debug("sp result=" + map);

			if (0 == (Integer) map.get("result")) {
				RecordSet rs = getSelect("default").searchPopupQuery(box);

				if (rs.nextRow()) {
					PortalUtil.setResult(this.getResponse(), rs);
				} else {

					PortalUtil.setResult(this.getResponse(), -10, "데이터가 존재하지 않습니다.");

				}

			} else {
				PortalUtil.setResult(this.getResponse(), -11, map.get("message") + "");

			}

		} catch (Exception e) {
			// dao에서 발생한 오류 메시지를 화면으로 전달.
			PortalUtil.setMessage(this.getResponse(), -12, e);

			getLogger().error(e);
			e.printStackTrace();
		}

	}

}
