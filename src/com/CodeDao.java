package com;

import com.mysql.jdbc.StringUtils;
import framework.action.Box;
import framework.db.*;
import framework.util.JsonUtil;
import framework.util.StringUtil;
import util.PortalUtil;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CodeDao extends SelectDaoSupport {
	private String service = "";

	public CodeDao(ConnectionManager mgr) {
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

	public RecordSet selectGwPumTypeList() throws SQLException {
		SelectConditionObject cond = new SelectConditionObject();
		StringBuilder query = new StringBuilder();

		query.append("\n/* " + this.getClass().toString() + ".selectGwPumTypeList() */\n");
		query.append(" select ");
		query.append("    type_id as \"_code\" ");
		query.append("   ,type_name as \"_name\" ");
		query.append("   ,type_description as \"_description\" ");
		query.append(" from gw_pumtype ");
		query.append(" where 1=1 ");
		query.append("  and dele_gubn = '0' ");
		query.append(" order by display_order ");

		getLogger().debug(query.toString());

		return select(query.toString(), cond.getParameter());
	}

	public RecordSet selectMenuPath(Box box) throws SQLException {
		SelectConditionObject cond = new SelectConditionObject();
		StringBuilder query = new StringBuilder();

		query.append("\n/* " + this.getClass().toString() + ".selectMenuPath() */\n");

		query.append(" select  ");
		query.append("  level, p.* ");
		query.append(" from programs p ");
		query.append(" start with program_id = ? ");
		query.append(" connect by prior parent_id = program_id ");
		query.append(" order by level desc ");

		if (StringUtils.isNullOrEmpty(box.getString("select_menu"))) {
			cond.setObject(box.getInteger("start_menu"));
		} else if (StringUtils.isNullOrEmpty(box.getString("start_menu"))) {
			cond.setObject(null);
		} else {
			cond.setObject(box.getInteger("select_menu"));
		}

		return select(query.toString(), cond.getParameter());
	}

	public RecordSet selectTeamCodeList() throws SQLException {
		SelectConditionObject cond = new SelectConditionObject();
		StringBuilder query = new StringBuilder();

		query.append("\n/* " + this.getClass().toString() + ".selectTeamCodeList() */\n");
		query.append(" 	 select  ");
		query.append(" 	   distinct  ");
		query.append(" 	   d.team_code as \"_code\" ");
		query.append(" 	 , d.team_code, d.team_name ");
		query.append(" 	 , d.work_plac, d.work_name ");
		query.append(" 	 , d.saupbu_code, d.saupbu_name ");
		query.append(" 	 , d.work_name || ' > ' || d.saupbu_name || ' > ' || d.team_name as \"_name\" ");
		query.append(" 	 , (case when u.part_code is not null then 'Y' else 'N' end ) as myteam ");
		query.append(" 	 from v_cmdept  d ");
		query.append(" 	    left join dr_user u on d.part_code = u.part_code ");
		query.append(" 			 and u.user_id = '1201305169' ");
		query.append(" 			 where team_code  in   ");
		query.append(" 			   (   ");
		query.append(" 			      select   team_code   ");
		query.append(" 			      from  dr_user x ,  v_cmdept  y  ");
		query.append(" 			      where   x.part_code = y.part_code  ");
		query.append(" 			      and use_mode = 0  ");
		query.append(" 			   )  ");
		query.append(" 			 order by d.work_plac , d.saupbu_code , d.team_code    ");

		getLogger().debug(query.toString());

		return select(query.toString(), cond.getParameter());
	}

	public RecordSet selectSavedApprovalLineList(String p_userid) throws SQLException {
		SelectConditionObject cond = new SelectConditionObject();
		StringBuilder query = new StringBuilder();

		query.append("\n/* " + this.getClass().toString() + ".selectSavedApprovalLineList() */\n");
		query.append(" select ");
		query.append("     h.app_id  as \"_code\" ");
		query.append("     ,h.app_line_nm as \"_name\" ");
		query.append("     ,h.app_id ");
		query.append("     ,h.app_line_nm ");
		query.append("     ,h.user_id ");
		query.append("     ,(case when d.app_user_id = ? then 'Y' else 'N' end) as is_writer ");
		cond.setObject(p_userid);
		query.append("     ,h.is_default ");
		query.append("     ,d.index_id ");
		query.append("     ,d.app_id ");
		query.append("     ,d.app_orderby ");
		query.append("     ,d.app_user_id ");
		query.append("     ,(case when h.user_id = d.app_user_id then '기안' else d.app_sort end) as app_sort ");
		query.append("     ,d.app_user_nm ");
		query.append("     ,d.app_men_level ");
		query.append("     ,portal.fu_teaminfo(u.part_code, 'name' ) as team_name ");
		query.append("   from ");
		query.append("     gw_app_line_detail d inner join gw_app_line h ");
		query.append("       on h.app_id = d.app_id ");
		query.append("     left join dr_user u on u.user_id = d.app_user_id ");
		query.append(" where ");
		query.append("   h.user_id = ? ");
		cond.setObject(p_userid);
		query.append("   order by h.is_default desc, h.app_id asc ");
		query.append("   ,d.index_id desc ");
		// query.append(" ,d.app_orderby desc ");

		getLogger().debug(query.toString());

		return select(query.toString(), cond.getParameter());
	}

	public String getDateTimeString(String prefix) throws SQLException {
		RecordSet rs = null;
		try {

			rs = select("select to_char(sysdate, 'yymmddhh24miss') as seqno from dual");

			if (rs.nextRow()) {
				return prefix + rs.getString("seqno");
			} else {
				return prefix + "00000000000000";
			}
		} catch (Exception e) {
			return prefix + "00000000000000";
		}
	}

	public long getSequence(String sequence) throws SQLException {
		RecordSet rs = null;
		try {

			rs = select("select " + sequence + ".nextval as seqno from dual");

			if (rs.nextRow()) {
				return rs.getLong("seqno");

			} else {
				return 0;
			}
		} catch (Exception e) {
			return 0;
		}
	}

	public long getSequenceCurrent(String sequence) throws SQLException {
		RecordSet rs = null;
		try {

			rs = select("select " + sequence + ".currval as seqno from dual");

			if (rs.nextRow()) {
				return rs.getLong("seqno");

			} else {
				return 0;
			}
		} catch (Exception e) {
			return 0;
		}
	}

	public String getCMSEQU(String table_id, String prefix) {

		String base_yymm = new SimpleDateFormat("yyyyMM").format(new Date());

		return getCMSEQU(table_id, base_yymm, prefix);

	}

	public String getCMSEQU(String table_id) {

		String base_yymm = new SimpleDateFormat("yyyyMM").format(new Date());

		return getCMSEQU(table_id, base_yymm, "");

	}

	public String getCMSEQU(String table_id, String base_yymm, String prefix) {

		RecordSet rs;
		try {

			rs = select("select fu_cmsequ( '" + table_id + "', '" + base_yymm + "' , '" + prefix + "' ) as seqno from dual ");

			if (rs.nextRow()) {
				return rs.getString("seqno");

			} else {
				return "0000000000";
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "0000000000";
		}

	}

	public HashMap saveApprovalLine(Box box, String p_userid) throws SQLException {

		StringBuilder query = null;

		ConnectionManager con = this.getConnectionManager();

		PreparedStatement pstmt = null;
		int i = 1;
		int ret = -1;
		HashMap<String, Object> result = new HashMap<String, Object>();
		try {
			con.setAutoCommit(false);

			long app_id = 0;

			if ("Y".equals(box.getString("is_default"))) {
				// 새로 등록된 결재라인이 기본값으로 설정되면 본인이 소유한 모든 결재라인 정보의 기본값 설정을 제거한다.
				query = new StringBuilder();
				query.append("\n/* " + this.getClass().toString() + ".saveApprovalLine() */\n");
				query.append(" update gw_app_line ");
				query.append(" set is_default = 'N'  ");
				query.append(" where user_id = ? ");

				pstmt = con.getConnection().prepareStatement(query.toString());

				i = 1;
				pstmt.setObject(i++, p_userid);
				ret = pstmt.executeUpdate();
			}

			if (0 == box.getLong("app_id")) {
				// 새로운 결재라인을 등록한다.
				// TODO : 구시스템에서 더이상 신규결재라인 사용하지 않도록 하고...
				// 기존 시스템이 sequence를 사용하지 않고 있음. ㅡㅡ;
				// TR_GW_APP_LINE_SEQ 트리거를 추가해서 시퀀스에서 자동으로 id값 채번하도록 했음.
				// app_id = getSequence("gw_app_line_seq");

				query = new StringBuilder();
				query.append("\n/* " + this.getClass().toString() + ".saveApprovalLine() */\n");
				query.append(" insert into gw_app_line ");
				query.append(" 	( app_line_nm, user_id, is_default)  ");
				query.append(" values  ");
				query.append(" 	( ?, ?, ? ) ");

				pstmt = con.getConnection().prepareStatement(query.toString());

				i = 1;
				// pstmt.setObject(i++, app_id);
				pstmt.setObject(i++, box.getString("app_line_nm"));
				pstmt.setObject(i++, p_userid);
				pstmt.setObject(i++, box.getString("is_default"));
				ret = pstmt.executeUpdate();

				app_id = getSequenceCurrent("gw_app_line_seq");
				getLogger().debug("saveApprovalLine new next app_id=" + app_id);

			} else {
				// 기존 번호를 가져온다.
				app_id = box.getLong("app_id");
				// 변경된 정보를 갱신한다.
				query = new StringBuilder();
				query.append("\n/* " + this.getClass().toString() + ".saveApprovalLine() */\n");
				query.append(" update gw_app_line ");
				query.append(" set app_line_nm = ?, is_default = ? ");
				query.append(" where app_id = ?  ");
				query.append(" and user_id = ? ");

				pstmt = con.getConnection().prepareStatement(query.toString());

				i = 1;
				pstmt.setObject(i++, box.getString("app_line_nm"));
				pstmt.setObject(i++, box.getString("is_default"));
				pstmt.setObject(i++, app_id);
				pstmt.setObject(i++, p_userid);
				ret = pstmt.executeUpdate();

				// 기존 라인 정보는 삭제하고 다시 등록한다.
				query = new StringBuilder();
				query.append("\n/* " + this.getClass().toString() + ".saveApprovalLine() */\n");
				query.append(" delete from gw_app_line_detail ");
				query.append(" where app_id = ?  ");

				pstmt = con.getConnection().prepareStatement(query.toString());

				i = 1;
				pstmt.setObject(i++, app_id);
				ret = pstmt.executeUpdate();

			}

			if (ret >= 1) {

				query = new StringBuilder();
				query.append("\n/* " + this.getClass().toString() + ".saveApprovalLine() */\n");
				query.append(" insert into gw_app_line_detail ");
				query.append(" 	(index_id, app_id, app_orderby, app_user_id, app_sort, app_user_nm, app_men_level)  ");
				// app_men_level, app_user_nm 직급과 성명란이 기존 자료가 꼬여 있음...
				query.append(" 	select gw_app_line_detail_seq.nextval, ?, ?, ?, ?, u.men_level, u.user_nm  ");
				query.append(" 	from dr_user u  ");
				query.append(" 	where u.user_id = ?  ");

				pstmt = con.getConnection().prepareStatement(query.toString());

				ArrayList<HashMap<String, Object>> l = (ArrayList<HashMap<String, Object>>) JsonUtil.parse(box.getRawString("json_data"));

				// {"docu_app_orderby":"5","docu_app_user_id":"1200704050","docu_app_sort":"일반결재"}

				for (HashMap<String, Object> map : l) {

					i = 1;
					pstmt.setObject(i++, app_id);
					pstmt.setObject(i++, map.get("docu_app_orderby"));
					pstmt.setObject(i++, map.get("docu_app_user_id"));
					pstmt.setObject(i++, map.get("docu_app_sort"));
					pstmt.setObject(i++, map.get("docu_app_user_id"));

					pstmt.executeUpdate();
				}

				result.put("result", 0);
				result.put("app_id", app_id);
				result.put("message", "결재라인이 저장 되었습니다.");

				con.commit();

			} else {
				result.put("result", -1);
				result.put("message", "결재라인이 저장되지 않았습니다.");

				con.rollback();

			}

		} catch (Exception e) {
			con.rollback();

			result.put("result", -1);
			result.put("message", e.getMessage());

			getLogger().error("saveApprovalLine error", e);
		} finally {
			if (pstmt != null)
				pstmt.close();
		}

		return result;

	}

	public HashMap deleteApprovalLine(Box box, String p_userid) throws SQLException {

		StringBuilder query = null;

		ConnectionManager con = this.getConnectionManager();

		PreparedStatement pstmt = null;
		int i = 1;
		int ret = -1;
		HashMap<String, Object> result = new HashMap<String, Object>();
		try {
			con.setAutoCommit(false);

			long app_id = 0;

			// 기존 번호를 가져온다.
			app_id = box.getLong("app_id");

			// 상세정보를 먼저 삭제한다.
			query = new StringBuilder();
			query.append("\n/* " + this.getClass().toString() + ".deleteApprovalLine() */\n");
			query.append(" delete from gw_app_line_detail ");
			query.append(" where app_id = ?  ");

			pstmt = con.getConnection().prepareStatement(query.toString());

			i = 1;
			pstmt.setObject(i++, app_id);
			ret = pstmt.executeUpdate();

			// 헤더 정보를 삭제한다.
			query = new StringBuilder();
			query.append("\n/* " + this.getClass().toString() + ".deleteApprovalLine() */\n");
			query.append(" delete from gw_app_line ");
			query.append(" where app_id = ?  ");

			pstmt = con.getConnection().prepareStatement(query.toString());

			i = 1;
			pstmt.setObject(i++, app_id);
			ret = pstmt.executeUpdate();

			result.put("result", 0);
			result.put("message", "결재라인이 삭제 되었습니다.");

			con.commit();

		} catch (Exception e) {
			con.rollback();

			result.put("result", -1);
			result.put("message", e.getMessage());

			e.printStackTrace();
		} finally {
			if (pstmt != null)
				pstmt.close();
		}

		return result;

	}

	public RecordSet selectVacationCodeList() {

		try {
			StringBuilder query = new StringBuilder();

			query.append("\n/* " + this.getClass().toString() + ".selectVacationCodeList() */\n");
			query.append(" select  ");
			query.append("      tong_deta as \"_code\" ");
			query.append("     ,tong_1nam as \"_name\" ");
			query.append(" from cmtong ");
			query.append(" where 1=1 ");
			query.append(" and dele_gubn = '1' ");
			query.append(" and tong_sect = '621' ");
			query.append(" and tong_deta not in ('31', '02', '05') ");
			query.append(" order by tong_1num ");

			getLogger().debug(query.toString());

			return select(query.toString());
		} catch (SQLException e) {
			getLogger().error(e);
			;
			return null;
		}

	}

	public RecordSet selectItemMiddleCodeList() {

		try {
			StringBuilder query = new StringBuilder();

			query.append("\n/* " + this.getClass().toString() + ".selectItemMiddleCodeList() */\n");
			query.append(" SELECT    ");
			query.append("     '''' || min(midl_code) || ''' and ''' || max(midl_code) || '''' as \"_code\" ");
			query.append("     ,midl_name as \"_name\" ");
			query.append(" from CDMIDL ");
			query.append(" where 1=1 ");
			query.append(" group by midl_name  ");
			query.append(" order by 1 ");

			getLogger().debug(query.toString());

			return select(query.toString());
		} catch (SQLException e) {
			getLogger().error(e);
			;
			return null;
		}

	}

	public HashMap<String, Object> GetEndHollyDate(String start_date, int days) {

		SelectConditionObject cond = new SelectConditionObject();
		StringBuilder query = null;
		HashMap<String, Object> result = new HashMap<String, Object>();

		try {

			query = new StringBuilder();
			query.append("\n/* " + this.getClass().toString() + ".GetEndHollyDate() */\n");
			query.append(" select to_char(to_date(drerp.f_holiday_date(?, ? ), 'yyyymmdd'),'yyyy-mm-dd') as end_date from dual   ");

			cond.setObject(StringUtil.replaceStr(start_date, "-", ""));
			cond.setObject(days);

			RecordSet rs = select(query.toString(), cond.getParameter());

			if (rs.nextRow()) {
				result.put("result", 0);
				result.put("end_date", rs.getString("end_date"));
			} else {
				result.put("result", -1);
				result.put("message", "ERP로 부터 휴가종료일 정보를 조회할 수 없습니다.");
				result.put("end_date", start_date);
			}

		} catch (Exception e) {

			result.put("result", -1);
			result.put("message", e.getMessage());
			result.put("end_date", start_date);

			e.printStackTrace();
		}

		return result;

	}

	public HashMap<String, Object> VacationDupCheck(Box box) {

		SelectConditionObject cond = new SelectConditionObject();
		StringBuilder query = null;
		HashMap<String, Object> result = new HashMap<String, Object>();

		try {

			query = new StringBuilder();
			query.append("\n/* " + this.getClass().toString() + ".VacationDupCheck() */\n");

			query.append(" select (case when a.docu_base_state = '0' then ' [결재중] ' else '' end) || docu_title || (case when count(*)over()-1 > 1 then '외 ' || to_char(count(*)over() - 1)||'건' else '' end) as docu_title ");
			query.append(" , a.va_start_date, a.va_end_date ");
			query.append(" from gw_docu_base_temp a ");
			query.append(" where 1 = 1 ");
			query.append(" and a.docu_base_state in ( 0 , 2 )  ");
			query.append(" and a.pum_id = 1 ");

			if (!StringUtils.isNullOrEmpty("docu_id")) {
				// 수정중일때는 같은 문서아이디인 경우를 제외한 다른 문서번호의 휴가계가 존재하는지 검사하기 위해서.
				query.append(" and a.docu_id <> ? ");
				cond.setObject(box.getLong("docu_id"));
			}

			// 휴가기간이 중복되는 휴가내역이 있는지 확인한다.
			query.append(" and (  ? between a.va_start_date and a.va_end_date ");
			cond.setObject(box.getString("start_date"));
			query.append(" or  ? between a.va_start_date and a.va_end_date ");
			cond.setObject(box.getString("end_date"));
			query.append(" )    ");

			query.append(" and a.dele_gubn = 0    ");
			query.append(" and a.va_sabun = ? ");
			cond.setObject(box.getString("empl_code"));
			query.append(" order by docu_base_state ");

			RecordSet rs = select(query.toString(), cond.getParameter());

			if (rs.nextRow()) {
				result.put("result", 1);
				result.put("docu_title", rs.getString("docu_title"));
				result.put("va_start_date", rs.getString("va_start_date"));
				result.put("va_end_date", rs.getString("va_end_date"));
			} else {
				result.put("result", 0);
				result.put("message", "해당기간에 중복된 휴가계는 없습니다.");
			}

		} catch (Exception e) {

			result.put("result", -10);
			result.put("message", e.getMessage());

			e.printStackTrace();
		}

		return result;

	}

	public String selectBidCompanyInfo(String saup_numb, String column_name) throws SQLException, ColumnNotFoundException {
		SelectConditionObject cond = new SelectConditionObject();
		StringBuilder query = new StringBuilder();

		query.append("\n/* " + this.getClass().toString() + ".selectBidCompanyInfo() */\n");
		query.append(" select top 1 ");
		query.append("      c.co_cd ");
		query.append("     ,c.co_nm ");
		query.append("     ,c.saup_numb ");
		query.append("     ,c.use_mode ");
		query.append("     ,c.co_isrt_wdate ");
		query.append("     ,c.updt_dt ");
		query.append("     ,c.co_phone ");
		query.append("     ,c.Wcust_kind ");
		query.append("     ,c.Wcust_upte ");
		query.append("     ,c.damdang_tel ");
		query.append("     ,c.damdang_email ");
		query.append("     ,c.saup_gubn ");
		query.append("     ,u.ceo ");
		query.append("     ,u.post ");
		query.append("     ,u.addr ");
		query.append("     ,u.addr_detail ");
		query.append("   from xtendus.bid_company c ");
		query.append("     inner join xtendus.bid_user u on c.co_cd = u.co_cd  ");
		query.append("   where 1 = 1 ");
		query.append("     and  c.saup_numb = replace(?, '-', '') ");
		cond.setObject(saup_numb);

		getLogger().debug(query.toString());

		RecordSet rs = select(query.toString(), cond.getParameter());

		if (rs.nextRow()) {
			return rs.getString(column_name);
		}
		return saup_numb;
	}

	public String selectBidCompanyInfobyCoCd(String co_cd, String column_name) throws SQLException, ColumnNotFoundException {
		SelectConditionObject cond = new SelectConditionObject();
		StringBuilder query = new StringBuilder();

		query.append("\n/* " + this.getClass().toString() + ".selectBidCompanyInfo() */\n");
		query.append(" select top 1 ");
		query.append("      c.co_nm ");
		query.append("     ,c.saup_numb ");
		query.append("     ,c.use_mode ");
		query.append("     ,c.co_isrt_wdate ");
		query.append("     ,c.updt_dt ");
		query.append("     ,c.co_phone ");
		query.append("     ,c.Wcust_kind ");
		query.append("     ,c.Wcust_upte ");
		query.append("     ,c.damdang_tel ");
		query.append("     ,c.damdang_email ");
		query.append("     ,c.saup_gubn ");
		query.append("     ,u.ceo ");
		query.append("     ,u.post ");
		query.append("     ,u.addr ");
		query.append("     ,u.addr_detail ");
		query.append("   from xtendus.bid_company c ");
		query.append("     inner join xtendus.bid_user u on c.co_cd = u.co_cd  ");
		query.append("   where 1 = 1 ");
		query.append("     and  c.co_cd = ? ");
		cond.setObject(co_cd);

		getLogger().debug(query.toString());

		RecordSet rs = select(query.toString(), cond.getParameter());

		if (rs.nextRow()) {
			return rs.getString(column_name);
		}
		return co_cd;
	}

	public RecordSet selectCityName(Box box, String p_userid) throws SQLException {

		StringBuilder query = null;

		query = new StringBuilder();
		query.append("\n/* " + this.getClass().toString() + ".selectCityName() */\n");
		query.append(" select city_name from comerp.mv_city_name  ");
		query.append(" order by post_code  ");

		return this.select(query.toString());

	}

	public RecordSet selectGuName(Box box, String p_userid) throws SQLException {

		SelectConditionObject cond = new SelectConditionObject();
		StringBuilder query = null;

		query = new StringBuilder();
		query.append("\n/* " + this.getClass().toString() + ".selectGuName() */\n");
		query.append(" select city_name, trim(gu_name) as gu_name from comerp.mv_gu_name  ");
		query.append(" where city_name like nvl(? || '%', '%')  ");
		cond.setObject(box.getString("city_name"));
		query.append(" order by city_name, gu_name  ");

		return this.select(query.toString(), cond.getParameter());

	}

	public RecordSet selectDongName(Box box, String p_userid) throws SQLException {

		SelectConditionObject cond = new SelectConditionObject();
		StringBuilder query = null;

		query = new StringBuilder();
		query.append("\n/* " + this.getClass().toString() + ".selectDongName() */\n");
		query.append(" select city_name, trim(gu_name) as gu_name, trim(dong_name) as dong_name, trim(nat_dong_name) as nat_dong_name from comerp.mv_dong_name  ");
		query.append(" where city_name like nvl(? || '%', '%')  ");
		cond.setObject(box.getString("city_name"));
		query.append(" and gu_name like nvl(? || '%', '%')  ");
		cond.setObject(box.getString("gu_name"));
		query.append(" order by city_name, gu_name, nvl(trim(dong_name), nat_dong_name)  ");

		return this.select(query.toString(), cond.getParameter());

	}

	public RecordSet selectDoroName(Box box, String p_userid) throws SQLException {

		SelectConditionObject cond = new SelectConditionObject();
		StringBuilder query = null;

		query = new StringBuilder();
		query.append("\n/* " + this.getClass().toString() + ".selectDoroName() */\n");
		query.append(" select city_name,  gu_name, dong_name, nat_dong_name, doro_name  ");
		query.append(" from comerp.postdata ");
		query.append(" where 1=1 ");
		query.append(" and gubn_code = 'NEW' ");

		if (!StringUtils.isNullOrEmpty(box.getString("city_name"))) {
			query.append(" and city_name like ? || '%' ");
			cond.setObject(box.getString("city_name"));
		}

		if (!StringUtils.isNullOrEmpty(box.getString("gu_name"))) {
			query.append(" and gu_name like ? || '%' ");
			cond.setObject(box.getString("gu_name"));
		}

		if (!StringUtils.isNullOrEmpty(box.getString("dong_name"))) {
			query.append(" and nvl(dong_name, nat_dong_name) like ? || '%' ");
			cond.setObject(box.getString("dong_name"));
		}

		if (!StringUtils.isNullOrEmpty(box.getString("doro_name"))) {
			query.append(" and doro_name like ? || '%' ");
			cond.setObject(box.getString("doro_name"));
		}

		query.append(" group by city_name, gu_name, dong_name, nat_dong_name, doro_name  ");
		query.append(" order by city_name, gu_name, nvl(dong_name, nat_dong_name), doro_name  ");

		return this.select(query.toString(), cond.getParameter());

	}

	public RecordSet selectAddressList(Box box, String p_userid) throws SQLException {

		SelectConditionObject cond = new SelectConditionObject();
		StringBuilder query = null;

		query = new StringBuilder();
		query.append("\n/* " + this.getClass().toString() + ".selectGuName() */\n");
		query.append(" select post_code, city_name, gu_name, doro_name, main_code, sub_code, bdng_name, dong_name, nat_dong_name  ");
		query.append(" from comerp.postdata ");
		query.append(" where 1=1 ");
		query.append(" and gubn_code = 'NEW' ");
		if (!StringUtils.isNullOrEmpty(box.getString("city_name"))) {
			query.append(" and city_name like ? || '%' ");
			cond.setObject(box.getString("city_name"));
		}
		if (!StringUtils.isNullOrEmpty(box.getString("gu_name"))) {
			query.append(" and gu_name like ? || '%' ");
			cond.setObject(box.getString("gu_name"));
		}
		if (!StringUtils.isNullOrEmpty(box.getString("dong_name"))) {
			query.append(" and (dong_name like ? || '%' or nat_dong_name like ? || '%') ");
			cond.setObject(box.getString("dong_name"));
			cond.setObject(box.getString("dong_name"));
		}
		if (!StringUtils.isNullOrEmpty(box.getString("doro_name"))) {
			query.append(" and doro_name like ? || '%' ");
			cond.setObject(box.getString("doro_name"));
		}

		if (!StringUtils.isNullOrEmpty(box.getString("key_word"))) {

			query.append(" and  ( ");
			query.append("   doro_name || ' ' || main_code like ? || '%' ");
			cond.setObject(box.getString("key_word"));

			query.append(" or bdng_name like  '%' || ? || '%' ");
			cond.setObject(box.getString("key_word"));

			query.append(" or gu_name || ' ' || bdng_name like  ? || '%' ");
			cond.setObject(box.getString("key_word"));

			query.append(" or nvl(dong_name, nat_dong_name)|| ' ' || bdng_name like  ? || '%' ");
			cond.setObject(box.getString("key_word"));
			query.append(" )");
		}

		if (StringUtils.isNullOrEmpty(box.getString("dong_name")) && StringUtils.isNullOrEmpty(box.getString("doro_name"))) {
			//   읍면동 이나 도로명이 선택되지 않은 상태에서는 최대 1000개 까지만 조회하도록 한다.
			query.append(" and rownum <= 1000 ");
		}

		query.append(" order by city_name, gu_name, dong_name, nat_dong_name, doro_name  ");

		return this.select(query.toString(), cond.getParameter());

	}

	public RecordSet selectCyberSinmungo(String p_userid) throws SQLException {

		SelectConditionObject cond = new SelectConditionObject();
		StringBuilder query = null;

		query = new StringBuilder();
		query.append("\n/* " + this.getClass().toString() + ".selectCyberSinmungo() */\n");

		query.append(" select count(*) as jebo_cnt    ");
		query.append("  FROM Xtendus.EA_bbs_CyberSinmungo g   ");
		query.append("  where 1=1   ");
		query.append("  and g.del_gubn = 'N'   ");
		query.append("  and g.tb_date > ( getdate() - 90 )   ");
		query.append("  and not exists (   ");
		query.append("     select 1    ");
		query.append("     from EA_bbs_CyberSinmungo_comment c    ");
		query.append("     where c.tb_idx = g.tb_idx   ");
		query.append("     and c.mem_id = ?)   ");

		cond.setObject(p_userid);

		return this.select(query.toString(), cond.getParameter());

	}

	public boolean isAllow4ClientIp(String ip) {

		boolean ret = false;
		try {
			SelectConditionObject cond = new SelectConditionObject();
			StringBuilder query = new StringBuilder();
			query.append("\n/* " + this.getClass().toString() + ".selectClientIpList() */\n");
			query.append(" select count(*) as cnt  ");
			query.append("  from gw_ipcheck    ");
			query.append("  where ? like ip_area || '%' ");
			cond.setObject(ip);

			RecordSet rs = this.select(query.toString(), cond.getParameter());

			if (rs.nextRow()) {
				ret = rs.getInt("cnt") > 0;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ret;

	}

	/**
	 * 해당 사용자에게 사용이 허가된 프로그램인지 검사.
	 *
	 * @param program_id
	 * @param user_id
	 * @return
	 */
	public boolean isAllowProgram4User(long program_id, String user_id) {

		boolean ret = false;
		try {
			SelectConditionObject cond = new SelectConditionObject();
			StringBuilder query = new StringBuilder();

			query.append("\n/* " + this.getClass().toString() + ".isAllowProgram4User() */\n");
			query.append(" select count(*) as cnt ");
			query.append(" from ( ");
			query.append("     select ur.user_id ");
			query.append("     from user_roles ur ");
			query.append("         inner join role_programs rp on ur.role_id = rp.role_id ");
			query.append("     where rp.program_id = ? ");
			cond.setObject(program_id);
			query.append("     and ur.user_id = ? ");
			cond.setObject(user_id);
			query.append("     union all     ");
			query.append("     select up.user_id ");
			query.append("     from user_programs up ");
			query.append("     where up.program_id = ? ");
			cond.setObject(program_id);
			query.append("     and up.user_id = ? ");
			cond.setObject(user_id);
			query.append(" ) ");

			RecordSet rs = this.select(query.toString(), cond.getParameter());

			if (rs.nextRow()) {
				ret = rs.getInt("cnt") > 0;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ret;

	}

	/**
	 * 주민번호 검사.
	 *
	 * @param va_empl_code
	 * @param va_sodk_year
	 * @param va_famy_jumn
	 * @return
	 */
	public boolean selectJumnCheck(String va_empl_code, String va_sodk_year, String va_famy_jumn) {

		try {
			SelectConditionObject cond = new SelectConditionObject();
			StringBuilder query = new StringBuilder();

			query.append("\n/* " + this.getClass().toString() + ".selectJumnCheck() */\n");
			query.append(" select count(*) as cnt from (  ");
			query.append(" select s.d(b.jumn_numb) as jumn_numb ");
			query.append(" from insodk_main a inner join inmast b on a.empl_code = b.empl_code ");
			query.append("  where  a.empl_code = ? ");
			cond.setObject(va_empl_code);
			query.append("         and a.sodk_year = ? ");
			cond.setObject(va_sodk_year);
			query.append("         and s.d(b.jumn_numb) = ? ");
			cond.setObject(va_famy_jumn);
			query.append("  union all ");
			query.append(" select s.d(famy_jumn)as famy_jumn ");
			query.append(" from insodk_famy ");
			query.append("  where  empl_code = ? ");
			cond.setObject(va_empl_code);
			query.append("         and sodk_year = ? ");
			cond.setObject(va_sodk_year);
			query.append("         and s.d(famy_jumn) = ? ");
			cond.setObject(va_famy_jumn);
			query.append("  ) x  ");

			RecordSet rs = this.select(query.toString(), cond.getParameter());

			if (rs.nextRow()) {
				return rs.getInt("cnt") > 0;
			} else {
				return false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}

	/**
	 * 공통코드정보를 조회한다.
	 *
	 * @param tong_sect
	 * @param tong_deta
	 * @return
	 */
	public RecordSet selectCmtongCodeList(String tong_sect, String tong_deta) {

		try {
			SelectConditionObject cond = new SelectConditionObject();
			StringBuilder query = new StringBuilder();

			query.append("\n/* " + this.getClass().toString() + ".selectVacationCodeList() */\n");
			query.append(" select  ");
			query.append("      tong_deta as \"_code\" ");
			query.append("     ,tong_1nam as \"_name\" ");
			query.append("     ,tong_deta ");
			query.append("     ,tong_1nam ");
			query.append(" from cmtong ");
			query.append(" where 1=1 ");
			query.append(" and dele_gubn = '1' ");
			query.append(" and tong_sect = ? ");
			cond.setObject(tong_sect);

			if (!StringUtils.isNullOrEmpty(tong_deta)) {
				query.append(" and tong_deta =  ? ");
				cond.setObject(tong_deta);
			}
			query.append(" order by tong_1num ");

			return select(query.toString(), cond.getParameter());
		} catch (SQLException e) {
			getLogger().error(e);
			return null;
		}

	}

	/**
	 * 네이버 웍스 연동을 위한 환경설정 정보를 조회한다.
	 *
	 * @return
	 */
	public Map<String, String> selectNaverApiConfig() {

		HashMap<String, String> result = new HashMap<String, String>();
		SelectConditionObject cond = new SelectConditionObject();
		StringBuilder query = new StringBuilder();
		try {

			String server = PortalUtil.getServerIp();

			query.append("\n/* " + this.getClass().toString() + ".selectNaverApiConfig() */\n");
			query.append(" select ");
			query.append("    * ");
			query.append(" from Naver_api ");
			query.append(" where 1=1 ");
			query.append("  and ( server_ip like '%' || ? || '%' ");
			cond.setObject(server);
			query.append("  or  ? like server_ip ) ");
			cond.setObject(server);

			RecordSet rs = select(query.toString(), cond.getParameter());

			while (rs.nextRow()) {
				result.put(rs.getString("key_name"), rs.getString("key_value"));
			}

		} catch (SQLException e) {
		}
		return result;

	}

	/**
	 * 공문 유형을 전달하고, 맵핑된 알림톡 템플릿 아이디를 리턴 받는다. 필요한 경우 letter_type_list 에 template_id
	 * 컬럼을 추가해서 확장해야 함. 현재는 view 스크립트에서 hard coding되어 있음.
	 *
	 * @param letter_type
	 * @return
	 */
	public String getLetterTemplateMapping(String letter_type) {
		try {

			SQLPreparedStatement pstmt = this.getConnectionManager().createPrepareStatement("select *  from dpms.v_letter_type_list where letter_type = ? ");
			SelectConditionObject cond = new SelectConditionObject();

			cond.setObject(letter_type);

			pstmt.set(cond.getParameter());

			RecordSet rs = pstmt.executeQuery();
			if (rs.nextRow()) {
				return rs.getString("template_id");
			} else {
				return "";
			}

		} catch (Exception e) {
			e.printStackTrace();
			getLogger().error(e);
			return "";
		}
	}

	/**
	 * 알리고에 등록된 알림전송용 대표번호를 환경설정 정보에서 조회한다.
	 *
	 * @param channel_id
	 * @return
	 */
	public String getSMSSendPhone(String channel_id) {
		try {

			SelectConditionObject cond = new SelectConditionObject();

			cond.setObject(channel_id);

			RecordSet rs = select("select *  from dpms.talk_config where channelid = ? ", cond.getParameter());

			if (rs.nextRow()) {
				return rs.getString("sender");
			} else {
				return "";
			}

		} catch (Exception e) {
			e.printStackTrace();
			getLogger().error(e);
			return "";
		}
	}

}
