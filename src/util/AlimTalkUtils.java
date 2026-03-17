package util;

import framework.action.Box;
import framework.db.ConnectionManager;
import framework.db.RecordSet;
import framework.db.SQLPreparedStatement;
import framework.db.SelectConditionObject;
import framework.util.StringUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AlimTalkUtils {
	private static Log _logger = LogFactory.getLog(framework.db.SelectDaoSupport.class);

	private static Log getLogger() {
		return _logger;
	}

	private String senderKey = "";
	private String sender = "";
	private String send_url = "";
	private String apiKey = "";
	private String userId = "";

	private RecordSet getConfig(ConnectionManager connMgr, String channel_id) {
		try {

			SQLPreparedStatement pstmt = connMgr.createPrepareStatement("select channelid, userid, apikey, senderkey, sender, baseurl, send_url from dpms.talk_config where channelid = ? ");
			SelectConditionObject cond = new SelectConditionObject();

			cond.setObject(channel_id);

			pstmt.set(cond.getParameter());

			return pstmt.executeQuery();

		} catch (Exception e) {
			e.printStackTrace();
			getLogger().error(e);
			return null;
		}
	}

	private RecordSet getTemplate(ConnectionManager connMgr, String template_id) {
		try {

			SQLPreparedStatement pstmt = connMgr.createPrepareStatement("select template_id, channelid, subject, message, target_name, target_url from dpms.talk_template where template_id = ? ");
			SelectConditionObject cond = new SelectConditionObject();

			cond.setObject(template_id);

			pstmt.set(cond.getParameter());

			return pstmt.executeQuery();

		} catch (Exception e) {
			e.printStackTrace();
			getLogger().error(e);
			return null;
		}
	}

	private RecordSet getMapping(ConnectionManager connMgr, String template_id) {
		try {

			SQLPreparedStatement pstmt = connMgr.createPrepareStatement("select template_id, variable, field_name from dpms.variable_mapping where template_id = ? ");
			SelectConditionObject cond = new SelectConditionObject();

			cond.setObject(template_id);

			pstmt.set(cond.getParameter());

			return pstmt.executeQuery();

		} catch (Exception e) {
			e.printStackTrace();
			getLogger().error(e);
			return null;
		}
	}

	/**
	 * 알리고 토큰 생성
	 * 
	 * @param
	 * @return
	 */
	private String createToken(ConnectionManager connMgr, String channel_id) throws Exception {

		RecordSet config = getConfig(connMgr, channel_id);
		if (config.nextRow()) {
			// getLogger().debug(JsonUtil.format(config));
		} else {
			return null;
		}
		// String baseUrl = "https://kakaoapi.aligo.in/akv10/token/create/30/m";
		String baseUrl = config.getString("baseurl");

		StringBuilder urlBuilder = new StringBuilder(baseUrl); /* URL */
		urlBuilder.append("?apikey=" + config.getString("apikey"));
		urlBuilder.append("&userid=" + config.getString("userid"));

		this.apiKey = config.getString("apikey");
		this.userId = config.getString("userid");
		this.senderKey = config.getString("senderkey");
		this.sender = config.getString("sender");
		this.send_url = config.getString("send_url");

		getLogger().debug("\nSend URL1=" + this.send_url);
		getLogger().debug(urlBuilder.toString());
		URL url = new URL(urlBuilder.toString());

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-type", "application/json");
		System.out.println("Response code: " + con.getResponseCode());

		BufferedReader rd;
		if (con.getResponseCode() >= 200 && con.getResponseCode() <= 300) {
			rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
		} else {
			rd = new BufferedReader(new InputStreamReader(con.getErrorStream()));
		}
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = rd.readLine()) != null) {
			sb.append(line);
		}
		rd.close();
		con.disconnect();

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObj = (JSONObject) jsonParser.parse(sb.toString());

		return (String) jsonObj.get("urlencode");
	}

	/**
	 * 알림톡 발송
	 * 
	 * @param repairEntity
	 * @param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Box sendAlimTalk(Box repairEntity, ConnectionManager connMgr) throws Exception {

		String token = this.createToken(connMgr, repairEntity.getString("channel_id") + "");
		getLogger().debug("\nToken=" + token);
		getLogger().debug("\nSend URL=" + this.send_url);
		URL url = new URL(this.send_url);

		getLogger().debug(repairEntity.toString());
		RecordSet template = getTemplate(connMgr, repairEntity.getString("template_id"));
		if (template.nextRow()) {
			// getLogger().debug(JsonUtil.format(template));
		} else {
			repairEntity.put("send_result", "템플릿을 조회할 수 없습니다. (Template ID : " + repairEntity.getString("template_id" + ")"));
			return repairEntity;
		}

		String result = "";
		String buttonName = "";
		String subjectTitle = "";
		String tplCode = "";
		// String siteLink = "https://dpms.doore.co.kr:9443/lessee-login.jsp";
		String siteLink = template.getString("target_url");
		String trRequesterNum = (repairEntity.getString("send_target")).replaceAll("-", ""); // 연락처

		JSONObject button_info_detail = new JSONObject();
		JSONArray button_info = new JSONArray();
		JSONObject button = new JSONObject();

		buttonName = template.getString("target_name");// "임차인 로그인";
		subjectTitle = template.getString("subject");// "임차인 공문 발행 안내";
		tplCode = repairEntity.getString("template_id");// "TP_1430";

		String message = template.getString("message");
		getLogger().debug("\nOriginal Message=" + message);

		RecordSet mapping = getMapping(connMgr, repairEntity.getString("template_id"));

		while (mapping.nextRow()) {
			getLogger().debug(mapping.getString("variable") + " => " + repairEntity.getString(mapping.getString("field_name") + "") + " ; Field=" + mapping.getString("field_name"));
			message = StringUtil.replaceStr(message, mapping.getString("variable"), repairEntity.getString(mapping.getString("field_name") + ""));
		}

		getLogger().debug("\nFormating Message=" + message);
		repairEntity.put("contents", message);




		Map<String, Object> params = new HashMap<String, Object>();
		params.put("apikey", apiKey);
		params.put("userid", userId);
		params.put("senderkey", senderKey);
		params.put("sender", sender);
		params.put("token", token);
		params.put("tpl_code", tplCode);
		params.put("receiver_1", trRequesterNum);
		params.put("subject_1", subjectTitle);
		params.put("message_1", message);


		if("doorepms".equals(repairEntity.getString("channel_id")) ) {
			button_info_detail.put("name", "채널 추가");
			button_info_detail.put("linkType", "AC");
			button_info_detail.put("linkTypeName", "채널 추가");
			button_info_detail.put("linkM", "");
			button_info_detail.put("linkP", "");
			button_info.add(button_info_detail);
			button.put("button", button_info);
			if(  StringUtil.isNotEmpty(buttonName) && StringUtil.isNotEmpty(siteLink) ) {
				button_info_detail = new JSONObject();
				button_info_detail.put("name", buttonName);
				button_info_detail.put("linkType", "WL");
				button_info_detail.put("linkTypeName", "웹링크");
				button_info_detail.put("linkM", siteLink);
				button_info_detail.put("linkP", siteLink);
				button_info.add(button_info_detail);
				button.put("button", button_info);
			}
			params.put("button_1", button);
		}else if("doorefms".equals(repairEntity.getString("channel_id")) ) {
			if(  StringUtil.isNotEmpty(buttonName) && StringUtil.isNotEmpty(siteLink) ) {
				button_info_detail = new JSONObject();
				button_info_detail.put("name", buttonName);
				button_info_detail.put("linkType", "WL");
				button_info_detail.put("linkTypeName", "웹링크");
				button_info_detail.put("linkM", siteLink);
				button_info_detail.put("linkP", siteLink);
				button_info.add(button_info_detail);
				button.put("button", button_info);
				params.put("button_1", button);
			}
		}



		StringBuilder postData = new StringBuilder();
		for (Map.Entry<String, Object> param : params.entrySet()) {
			if (postData.length() != 0)
				postData.append('&');
			postData.append(param.getKey());
			postData.append('=');
			postData.append(param.getValue());
		}
		byte[] postDataBytes = postData.toString().getBytes("UTF-8");

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
		con.setDoOutput(true);
		con.getOutputStream().write(postDataBytes);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		result = StringUtil.convertUTF8(response.toString());

		result = PortalUtil.uniToKor(result);
		repairEntity.put("send_result", result);
		return repairEntity;
	}

	public String getTalkTemplate(Box repairEntity, ConnectionManager connMgr) throws Exception {

		String result = "";

		String token = this.createToken(connMgr, repairEntity.getString("channel_id") + "");
		getLogger().debug("\nToken=" + token);

		URL url = new URL("https://kakaoapi.aligo.in/akv10/template/list/");

		getLogger().debug(repairEntity.toString());

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("apikey", apiKey);
		params.put("userid", userId);
		params.put("senderkey", senderKey);
		params.put("token", token);
		params.put("tpl_code", repairEntity.getString("template_id"));

		StringBuilder postData = new StringBuilder();
		for (Map.Entry<String, Object> param : params.entrySet()) {
			if (postData.length() != 0)
				postData.append('&');
			postData.append(param.getKey());
			postData.append('=');
			postData.append(param.getValue());
		}
		byte[] postDataBytes = postData.toString().getBytes("UTF-8");

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
		con.setDoOutput(true);
		con.getOutputStream().write(postDataBytes);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		result = StringUtil.convertUTF8(response.toString());

		result = PortalUtil.uniToKor(result);
		return result;
	}

}
