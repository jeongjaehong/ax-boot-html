package com;

import framework.action.Action;
import framework.action.Box;
import framework.db.RecordSet;
import framework.util.JsonUtil;
import util.DBLog;
import util.GrantUtil;
import util.PortalUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class CodeManageAction extends Action {

	private CodeManageDao dao = null;

	private CodeManageDao getSelect(String service) throws Exception {
		if (dao == null || !dao.getService().equals(service)) {
			dao = new CodeManageDao(getConnectionManager(service));
			dao.setService(service);
		}
		return dao;
	}

	public void processInit() {
		PortalUtil.setResult(this.getResponse(), 0, "class initialize.");

	}

	/**
	 * 공통코드 관리 - 공통코드 테이블 목록 - 조회
	 */
	public void processSearchCodeManageHead() {

		DBLog.actionLog(this.getRequest(), getConnectionManager("default"), this);

		try {
			
			String auhority = GrantUtil.getAuthority(getRequest(), getConnectionManager("default"));		
			if(!"읽기".equals(auhority) && !"저장".equals(auhority)) {
				PortalUtil.setResult(this.getResponse(), -1, "권한이 없습니다.\n" + auhority);
				return;
			}
			
			Box box = this.getInput();

			getLogger().debug(box);

			RecordSet rs = getSelect("default").searchCodeManageHead(box);

			PortalUtil.setResult(this.getResponse(), rs, true);

		} catch (Exception e) {
			PortalUtil.setResult(this.getResponse(), -1, e.getMessage());

			getLogger().error(e);
			e.printStackTrace();
		}

	}

	/**
	 * 공통코드 관리 - 공통코드 테이블 목록 - 저장
	 */
	public void processSaveCodeManageHead() {

		DBLog.actionLog(this.getRequest(), getConnectionManager("default"), this);
		HashMap<String, Object> map = null;
		try {

			String auhority = GrantUtil.getAuthority(getRequest(), getConnectionManager("default"));		
			if( !"저장".equals(auhority)) {
				PortalUtil.setResult(this.getResponse(), -1, "저장 권한이 없습니다.");
				return;
			}


			LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");

			if (null == loginBean) {
				PortalUtil.setResult(this.getResponse(), -99, "다시 로그인 하십시오.");
				return;
			}

			map = new HashMap<String, Object>();
			Box box = this.getInput();

			ArrayList<HashMap<String, Object>> jarray = (ArrayList<HashMap<String, Object>>) JsonUtil.parse(box.getRawString("rows"));
			map = getSelect("default").saveCodeManageHead(jarray, loginBean);

			getLogger().debug("sp result=" + map);

			PortalUtil.setResult(this.getResponse(), 0, "저장 되었습니다.");

		} catch (Exception e) {
			// dao에서 발생한 오류 메시지를 화면으로 전달.
			PortalUtil.setMessage(this.getResponse(), -12, e);

			getLogger().error(e);
			e.printStackTrace();
		}

	}

	/**
	 * 공통코드 관리 - 코드목록 - 조회
	 */
	public void processSearchCodeManageDetail() {

		DBLog.actionLog(this.getRequest(), getConnectionManager("default"), this);

		try {
			
			String auhority = GrantUtil.getAuthority(getRequest(), getConnectionManager("default"));		
			if(!"읽기".equals(auhority) && !"저장".equals(auhority)) {
				PortalUtil.setResult(this.getResponse(), -1, "권한이 없습니다.\n" + auhority);
				return;
			}
			
			Box box = this.getInput();

			getLogger().debug(box);

			RecordSet rs = getSelect("default").searchCodeManageDetail(box);

			PortalUtil.setResult(this.getResponse(), rs, true);

		} catch (Exception e) {
			PortalUtil.setResult(this.getResponse(), -1, e.getMessage());

			getLogger().error(e);
			e.printStackTrace();
		}

	}

	/**
	 * 공통코드 관리 - 코드목록 - 저장
	 */

	public void processSaveCodeManage() {

		DBLog.actionLog(this.getRequest(), getConnectionManager("default"), this);
		HashMap<String, Object> map = null;
		try {

			String auhority = GrantUtil.getAuthority(getRequest(), getConnectionManager("default"));		
			if( !"저장".equals(auhority)) {
				PortalUtil.setResult(this.getResponse(), -1, "저장 권한이 없습니다.");
				return;
			}


			LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");

			if (null == loginBean) {
				PortalUtil.setResult(this.getResponse(), -99, "다시 로그인 하십시오.");
				return;
			}

			map = new HashMap<String, Object>();
			Box box = this.getInput();

			ArrayList<HashMap<String, Object>> jarray = (ArrayList<HashMap<String, Object>>) JsonUtil.parse(box.getRawString("rows"));
			map = getSelect("default").saveCodeManage(jarray, loginBean);

			getLogger().debug("sp result=" + map);
			PortalUtil.setResult(this.getResponse(), 0, "저장 되었습니다.");

		} catch (Exception e) {
			// dao에서 발생한 오류 메시지를 화면으로 전달.
			PortalUtil.setMessage(this.getResponse(), -12, e);

			getLogger().error(e);
			e.printStackTrace();
		}

	}

	/**
	 * 공통코드 관리 - 코드목록 - 삭제
	 */
	public void processDeleteCodeManage() {

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
			map = getSelect("default").deleteCodeManage(jarray, loginBean);

			getLogger().debug("sp result=" + map);

			PortalUtil.setResult(this.getResponse(), 0, "삭제 되었습니다.");

		} catch (Exception e) {
			// dao에서 발생한 오류 메시지를 화면으로 전달.
			PortalUtil.setMessage(this.getResponse(), -12, e);

			getLogger().error(e);
			e.printStackTrace();
		}

	}

}
