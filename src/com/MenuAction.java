package com;

import framework.action.Action;
import framework.db.RecordSet;
import util.DBLog;
import util.PortalUtil;

import java.util.HashMap;

public class MenuAction extends Action {

	private MenuDao dao = null;

	private MenuDao getSelect(String service) throws Exception {
		if (dao == null || !dao.getService().equals(service)) {
			dao = new MenuDao(getConnectionManager(service));
			dao.setService(service);
		}
		return dao;
	}

	/**
	 * 프로그램 목록을 조회하여 메뉴를 구성한다.
	 */
	public void processLoadMenu() {
		try {
			DBLog.actionLog(this.getRequest(), getConnectionManager("default"), this);

			LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");
			long start_menu = getInput().getLong("start");

			if (loginBean == null) {
				PortalUtil.setResult(this.getResponse(), -99, "로그인 정보를 찾을 수 없습니다.");
			} else {

				try {
					getSelect("default").saveOpenLog(this.getInput(), loginBean);
				} catch (Exception e) {
					DBLog.errorLog(this.getRequest(), getConnectionManager("default"), e, this);
				}

				RecordSet rs = getSelect("default").selectMenuList(loginBean.getUserId(), start_menu);

				if (rs != null && rs.nextRow()) {
					PortalUtil.setResult(this.getResponse(), rs);
				} else {
					PortalUtil.setResult(this.getResponse(), -10, "사용 권한을 부여받은 프로그램이 없습니다.\n\n관리자에게 문의 하십시오.");
				}
			}

		} catch (Exception e) {
			// dao에서 발생한 오류 메시지를 화면으로 전달.
			PortalUtil.setMessage(this.getResponse(), -12, e);
			getLogger().error("processLoadMenu error", e);
		}
	}

	/**
	 * 즐겨찾는 메뉴로 지정된 프로그램 목록을 조회한다.
	 */
	public void processLoadFavorite() {
		try {
			DBLog.actionLog(this.getRequest(), getConnectionManager("default"), this);

			LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");
			long start_menu = getInput().getLong("start");

			if (loginBean == null) {
				PortalUtil.setResult(this.getResponse(), -99, "로그인 정보를 찾을 수 없습니다.");
			} else {

				RecordSet rs = getSelect("default").selectFavorite(loginBean.getUserId());

				if (rs != null && rs.nextRow()) {
					PortalUtil.setResult(this.getResponse(), rs);
				} else {
					PortalUtil.setResult(this.getResponse(), -10, "등록된 즐겨찾가 존재하지 않습니다.");
				}
			}

		} catch (Exception e) {
			// dao에서 발생한 오류 메시지를 화면으로 전달.
			PortalUtil.setMessage(this.getResponse(), -12, e);
			getLogger().error("processLoadFavorite error", e);
		}
	}

	public void processLoadTop5() {
		try {
			DBLog.actionLog(this.getRequest(), getConnectionManager("default"), this);

			LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");
			long start_menu = getInput().getLong("start");

			if (loginBean == null) {
				PortalUtil.setResult(this.getResponse(), -99, "로그인 정보를 찾을 수 없습니다.");
			} else {

				RecordSet rs = getSelect("default").selectTop5(loginBean.getUserId());

				if (rs != null && rs.nextRow()) {
					PortalUtil.setResult(this.getResponse(), rs);
				} else {
					PortalUtil.setResult(this.getResponse(), -10, "등록된 즐겨찾가 존재하지 않습니다.");
				}
			}

		} catch (Exception e) {
			// dao에서 발생한 오류 메시지를 화면으로 전달.
			PortalUtil.setMessage(this.getResponse(), -12, e);
			getLogger().error("processLoadFavorite error", e);
		}
	}

	/**
	 * 즐겨찾기를 등록하거나 해제한다.
	 */

	public void processToggleFavorite() {

		DBLog.actionLog(this.getRequest(), getConnectionManager("default"), this);
		HashMap<String, Object> map = null;
		try {

			LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");

			if (null == loginBean) {
				PortalUtil.setResult(this.getResponse(), -99, "다시 로그인 하십시오.");
				return;
			}

			map = new HashMap<String, Object>();
			int favorite_seq = this.getInput().getInteger("favorite_seq");
			int program_id = this.getInput().getInteger("program_id");

			map = getSelect("default").toggleFavorite(favorite_seq, program_id, loginBean);

			if (0 == (Integer) map.get("result")) {

				PortalUtil.setResult(this.getResponse(), 0, "succ");

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
