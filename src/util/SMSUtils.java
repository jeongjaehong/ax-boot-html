package util;

import framework.db.ConnectionManager;
import framework.db.RecordSet;
import framework.db.SQLPreparedStatement;
import framework.db.SelectConditionObject;
import framework.util.JsonUtil;
import framework.util.StringUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SMSUtils {
	private static Log _logger = LogFactory.getLog(framework.db.SelectDaoSupport.class);

	final String send_url = "https://apis.aligo.in/send/";

	private static Log getLogger() {
		return _logger;
	}

	/**
	 * 발송 대표번호 및 Api 호출을 위한 URL등을 환경설정에서 조회한다.
	 * 
	 * @param connMgr
	 * @return
	 */
	private RecordSet getConfig(ConnectionManager connMgr) {
		try {

			SQLPreparedStatement pstmt = connMgr.createPrepareStatement("select channelid, userid, apikey, senderkey, sender, baseurl, send_url from dpms.talk_config where channelid = ? ");
			SelectConditionObject cond = new SelectConditionObject();

			cond.setObject("doorepms");

			pstmt.set(cond.getParameter());

			return pstmt.executeQuery();

		} catch (Exception e) {
			e.printStackTrace();
			getLogger().error(e);
			return null;
		}
	}

	/**
	 * 알리고 API를 이용하여 SMS, LMS를 발송한다.
	 * 
	 * @param connMgr
	 * @param sms
	 * @return
	 */
	public String sendSMS(ConnectionManager connMgr, Map<String, Object> sms) {
		Map<String, Object> result = new HashMap<String, Object>();

		try {
			RecordSet config = getConfig(connMgr);
			if (!config.nextRow()) {
				result.put("result_code", "-10");
				result.put("message", "SMS 환경설정 정보를 조회할 수 없습니다.");
				return JsonUtil.format(result);
			}

			sms.put("user_id", config.getString("userid"));
			sms.put("key", config.getString("apikey"));

			URL url = new URL(send_url);

			StringBuilder postData = new StringBuilder();
			for (Map.Entry<String, Object> param : sms.entrySet()) {
				if (postData.length() != 0) {
					postData.append('&');
				}
				postData.append(param.getKey());
				postData.append('=');
				postData.append(param.getValue());
			}
			byte[] postDataBytes = postData.toString().getBytes("UTF-8");

			getLogger().debug(postData.toString());

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

			String res = StringUtil.convertUTF8(response.toString());
			res = PortalUtil.uniToKor(res);

			getLogger().debug(response.toString());
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result_code", "-1");
			result.put("message", e.getMessage());
			return JsonUtil.format(result);
		}

	}

}
