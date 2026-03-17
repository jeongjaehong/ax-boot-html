package com;

import framework.action.Action;
import framework.db.RecordSet;
import util.DBLog;
import util.PortalUtil;

import java.util.HashMap;

public class CodeAction extends Action {

	private CodeDao dao = null;

	private CodeDao getSelect(String service) throws Exception {
		if (dao == null || !dao.getService().equals(service)) {
			dao = new CodeDao(getConnectionManager(service));
			dao.setService(service);
		}
		return dao;
	}

	public void processLoadCode() {
		try {
			DBLog.actionLog(this.getRequest(), getConnectionManager("dpms"), this);

			String code = getInput().getString("code");

			LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");

			if (loginBean == null) {
				PortalUtil.setResult(this.getResponse(), -99, "로그인 정보를 찾을 수 없습니다.");
			} else {

				RecordSet rs = null;
				if ("GW_PUMTYPE".equals(code)) {
					rs = getSelect("dpms").selectGwPumTypeList();
				} else if ("TEAM_CODE".equals(code)) {
					rs = getSelect("dpms").selectTeamCodeList();
				} else if ("CMTONG".equals(code)) {
					rs = getSelect("dpms").selectVacationCodeList();
				} else if ("CDMIDL".equals(code)) {
					rs = getSelect("erp").selectItemMiddleCodeList();
				}

				if (rs != null && rs.nextRow()) {
					PortalUtil.setResult(this.getResponse(), rs);
				} else {
					PortalUtil.setResult(this.getResponse(), -10, "데이터가 존재하지 않습니다.");
				}
			}

		} catch (Exception e) {
			getLogger().error("processLoadCode error", e);
			PortalUtil.setResult(this.getResponse(), e.getMessage());
		}
	}

	public void processLoadCmtong() {
		try {
			DBLog.actionLog(this.getRequest(), getConnectionManager("dpms"), this);

			String tong_sect = getInput().getString("tong_sect");
			String tong_deta = getInput().getString("tong_deta");

			LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");

			if (loginBean == null) {
				PortalUtil.setResult(this.getResponse(), -99, "로그인 정보를 찾을 수 없습니다.");
			} else {

				RecordSet rs = null;
				rs = getSelect("erp").selectCmtongCodeList(tong_sect, tong_deta);

				if (rs != null && rs.nextRow()) {
					PortalUtil.setResult(this.getResponse(), rs);
				} else {
					PortalUtil.setResult(this.getResponse(), -10, "데이터가 존재하지 않습니다.");
				}
			}

		} catch (Exception e) {
			getLogger().error("processLoadCmtong error", e);
			PortalUtil.setResult(this.getResponse(), e.getMessage());
		}
	}

	public void processGetEndHollyDate() {
		try {
			DBLog.actionLog(this.getRequest(), getConnectionManager("dpms"), this);

			String start_date = getInput().getString("start_date");
			int days = getInput().getInteger("days");

			LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");

			if (loginBean == null) {
				PortalUtil.setResult(this.getResponse(), -99, "로그인 정보를 찾을 수 없습니다.");
			} else {

				HashMap<String, Object> map = getSelect("erp").GetEndHollyDate(start_date, days);

				PortalUtil.setResult(this.getResponse(), map, false);
			}

		} catch (Exception e) {
			getLogger().error("processGetEndHollyDate error", e);
			PortalUtil.setResult(this.getResponse(), e.getMessage());
		}
	}

	public void processVacationDupCheck() {
		try {
			DBLog.actionLog(this.getRequest(), getConnectionManager("dpms"), this);

			LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");

			if (loginBean == null) {
				PortalUtil.setResult(this.getResponse(), -99, "로그인 정보를 찾을 수 없습니다.");
			} else {

				HashMap<String, Object> map = getSelect("dpms").VacationDupCheck(getInput());

				PortalUtil.setResult(this.getResponse(), map, false);
			}

		} catch (Exception e) {
			getLogger().error("processGetEndHollyDate error", e);
			PortalUtil.setResult(this.getResponse(), e.getMessage());
		}
	}

	public void processLoadSavedApprovalLineList() {
		try {
			DBLog.actionLog(this.getRequest(), getConnectionManager("dpms"), this);

			LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");

			if (loginBean == null) {
				PortalUtil.setResult(this.getResponse(), -99, "로그인 정보를 찾을 수 없습니다.");
			} else {

				RecordSet rs = null;
				rs = getSelect("dpms").selectSavedApprovalLineList(loginBean.getUserId());

				if (rs != null && rs.nextRow()) {
					PortalUtil.setResult(this.getResponse(), rs);
				} else {
					PortalUtil.setResult(this.getResponse(), -10, "데이터가 존재하지 않습니다.");
				}
			}

		} catch (Exception e) {
			getLogger().error("processLoadSavedApprovalLineList error", e);
			PortalUtil.setResult(this.getResponse(), e.getMessage());
		}
	}

	public void processLoadMenuPath() {
		try {
			DBLog.actionLog(this.getRequest(), getConnectionManager("dpms"), this);

			LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");

			if (loginBean == null) {
				PortalUtil.setResult(this.getResponse(), -99, "로그인 정보를 찾을 수 없습니다.");
			} else {

				RecordSet rs = null;
				rs = getSelect("dpms").selectMenuPath(this.getInput());

				if (rs != null && rs.nextRow()) {
					PortalUtil.setResult(this.getResponse(), rs);
				} else {
					PortalUtil.setResult(this.getResponse(), -10, "두레포털");
				}
			}

		} catch (Exception e) {
			getLogger().error("processLoadSavedApprovalLineList error", e);
			PortalUtil.setResult(this.getResponse(), -10, "두레포털");
		}
	}

	public void processSaveApprovalLine() {
		try {


			DBLog.actionLog(this.getRequest(), getConnectionManager("dpms"), this);

			LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");

			if (loginBean == null) {
				PortalUtil.setResult(this.getResponse(), -99, "로그인 정보를 찾을 수 없습니다.");
			} else {

				HashMap map = null;
				if ("delete".equals(getInput().getString("command"))) {
					map = getSelect("dpms").deleteApprovalLine(this.getInput(), loginBean.getUserId());

				} else {
					map = getSelect("dpms").saveApprovalLine(this.getInput(), loginBean.getUserId());
				}

				PortalUtil.setResult(this.getResponse(), map);
			}

		} catch (Exception e) {
			getLogger().error("processSaveApprovalLine error", e);
			PortalUtil.setResult(this.getResponse(), e.getMessage());
		}
	}

	public void processLoadCityName() {
		try {
			DBLog.actionLog(this.getRequest(), getConnectionManager("dpms"), this);

			LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");

			if (loginBean == null) {
				PortalUtil.setResult(this.getResponse(), -99, "로그인 정보를 찾을 수 없습니다.");
			} else {

				RecordSet rs = getSelect("erp").selectCityName(this.getInput(), loginBean.getUserId());
				PortalUtil.setResult(this.getResponse(), rs);
			}

		} catch (Exception e) {
			getLogger().error("processSaveApprovalLine error", e);
			PortalUtil.setResult(this.getResponse(), e.getMessage());
		}
	}

	public void processLoadGuName() {
		try {
			DBLog.actionLog(this.getRequest(), getConnectionManager("dpms"), this);

			LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");

			if (loginBean == null) {
				PortalUtil.setResult(this.getResponse(), -99, "로그인 정보를 찾을 수 없습니다.");
			} else {

				RecordSet rs = getSelect("erp").selectGuName(this.getInput(), loginBean.getUserId());
				PortalUtil.setResult(this.getResponse(), rs);
			}

		} catch (Exception e) {
			getLogger().error("processSaveApprovalLine error", e);
			PortalUtil.setResult(this.getResponse(), e.getMessage());
		}
	}

	public void processLoadDongName() {
		try {
			DBLog.actionLog(this.getRequest(), getConnectionManager("dpms"), this);

			LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");

			if (loginBean == null) {
				PortalUtil.setResult(this.getResponse(), -99, "로그인 정보를 찾을 수 없습니다.");
			} else {

				RecordSet rs = getSelect("erp").selectDongName(this.getInput(), loginBean.getUserId());
				PortalUtil.setResult(this.getResponse(), rs);
			}

		} catch (Exception e) {
			getLogger().error("processSaveApprovalLine error", e);
			PortalUtil.setResult(this.getResponse(), e.getMessage());
		}
	}

	public void processLoadDoroName() {
		try {
			DBLog.actionLog(this.getRequest(), getConnectionManager("dpms"), this);

			LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");

			if (loginBean == null) {
				PortalUtil.setResult(this.getResponse(), -99, "로그인 정보를 찾을 수 없습니다.");
			} else {

				RecordSet rs = getSelect("erp").selectDoroName(this.getInput(), loginBean.getUserId());
				PortalUtil.setResult(this.getResponse(), rs);
			}

		} catch (Exception e) {
			getLogger().error("processSaveApprovalLine error", e);
			PortalUtil.setResult(this.getResponse(), e.getMessage());
		}
	}

	public void processLoadAddressList() {
		try {
			DBLog.actionLog(this.getRequest(), getConnectionManager("dpms"), this);

			LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");

			if (loginBean == null) {
				PortalUtil.setResult(this.getResponse(), -99, "로그인 정보를 찾을 수 없습니다.");
			} else {

				RecordSet rs = getSelect("erp").selectAddressList(this.getInput(), loginBean.getUserId());
				PortalUtil.setResult(this.getResponse(), rs);
			}

		} catch (Exception e) {
			getLogger().error("processSaveApprovalLine error", e);
			PortalUtil.setResult(this.getResponse(), e.getMessage());
		}
	}

	public void processGetCyberSinmungo() {
		try {
			DBLog.actionLog(this.getRequest(), getConnectionManager("dpms"), this);

			LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");

			if (loginBean == null) {
				PortalUtil.setResult(this.getResponse(), -99, "로그인 정보를 찾을 수 없습니다.");
			} else {

				RecordSet rs = getSelect("logis").selectCyberSinmungo(loginBean.getUserId());
				PortalUtil.setResult(this.getResponse(), rs);
			}

		} catch (Exception e) {
			getLogger().error("processGetCyberSinmungo error", e);
			PortalUtil.setResult(this.getResponse(), e.getMessage());
		}
	}

	public void processJumnCheck() {
		try {
			DBLog.actionLog(this.getRequest(), getConnectionManager("erp"), this);

			LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");

			if (loginBean == null) {
				PortalUtil.setResult(this.getResponse(), -99, "로그인 정보를 찾을 수 없습니다.");
			} else {

				boolean ret = getSelect("erp").selectJumnCheck(loginBean.getEmplCode(), getInput().getString("sodk_year"), getInput().getString("famy_jumn"));

				if (ret) {
					PortalUtil.setResult(this.getResponse(), 0, "succ");
				} else {
					PortalUtil.setResult(this.getResponse(), 0, "fail");
				}
			}

		} catch (Exception e) {
			getLogger().error("processJumnCheck error", e);
			PortalUtil.setResult(this.getResponse(), e.getMessage());
		}
	}

}
