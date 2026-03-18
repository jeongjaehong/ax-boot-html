package util;

import com.LoginBean;
import framework.action.Box;
import framework.config.Configuration;
import framework.db.RecordSet;
import framework.util.JsonUtil;
import framework.util.RDUtil;
import framework.util.StringUtil;
import framework.util.XmlUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class PortalUtil {
    private static Log _logger = LogFactory.getLog(framework.db.SelectDaoSupport.class);

    public static final String WIFI_DEVICE_NUMBER = "01099999999";

    private static Log getLogger() {
        return _logger;
    }

    public static String getAdminPhone() {

        Configuration config = framework.config.Configuration.getInstance();

        return config.getString("admin.phone.number");

    }

    public static String getAutoLoginPhones() {

        Configuration config = framework.config.Configuration.getInstance();

        return config.getString("auto.login.number");

    }

    public static HashMap<String, String> getAdminPhones() {

        Configuration config = framework.config.Configuration.getInstance();

        String phones[] = config.getString("admin.phone.list").split(",");

        HashMap<String, String> result = new HashMap<String, String>();
        result.put(getAdminPhone(), "A");
        for (int i = 0; i < phones.length; i++) {
            result.put(phones[i], "T");
        }
        return result;
    }

    public static HashMap<String, String> getBidPhones() {

        Configuration config = framework.config.Configuration.getInstance();

        String phones[] = config.getString("bid.phone.list").split(",");

        HashMap<String, String> result = new HashMap<String, String>();
        for (int i = 0; i < phones.length; i++) {
            result.put(phones[i], "T");
        }
        return result;
    }

    public static HashMap<String, String> getAdminIPList() {

        Configuration config = framework.config.Configuration.getInstance();

        String ip[] = config.getString("admin.ip.list").split(",");

        HashMap<String, String> result = new HashMap<String, String>();
        for (int i = 0; i < ip.length; i++) {
            result.put(ip[i], "X");
        }
        return result;
    }

    public static boolean isAdminIP(String userip) {

        try {
            Configuration config = framework.config.Configuration.getInstance();

            String ip[] = config.getString("admin.ip.list").split(",");
            String chk_ip[] = StringUtil.tokenFn(userip, ".");

            String check_ip = chk_ip[0] + "." + chk_ip[1] + "." + chk_ip[2] + ".*";

            for (int i = 0; i < ip.length; i++) {
                if (StringUtils.equals(ip[i], check_ip) || StringUtils.equals(ip[i], userip))
                    return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getClientVersion() {

        Configuration config = framework.config.Configuration.getInstance();

        return config.getString("android.client.verion");

    }

    public static String getBidSystemHost(String work_plac) {

        Configuration config = framework.config.Configuration.getInstance();

        return config.getString("bid_system_host_" + work_plac.charAt(0));

    }

    public static String getBidSystemKey(String work_plac) {

        Configuration config = framework.config.Configuration.getInstance();

        return config.getString("bid_system_key_" + work_plac.charAt(0));

    }

    public static String getTranmanagerClientVersion() {

        Configuration config = framework.config.Configuration.getInstance();

        return config.getString("tranmanager.client.verion");

    }

    public static String getGcmApiKey() {

        Configuration config = framework.config.Configuration.getInstance();

        return config.getString("gcm.api.key");

    }

    public static String getPhotoFlolder(String ServerIP) {

        Configuration config = framework.config.Configuration.getInstance();

        return config.getString("photo." + ServerIP + ".folder");

    }

    public static String getSignFlolder(String ServerIP) {

        Configuration config = framework.config.Configuration.getInstance();

        return config.getString("sign." + ServerIP + ".folder");

    }

    public static String getGwHost(String ServerIP) {
        try {
            Configuration config = framework.config.Configuration.getInstance();

            return config.getString("gw." + ServerIP.replaceAll(":", ".") + ".host");
        } catch (Exception e) {
            return "";
        }

    }

    public static String getAppInfo(String key) {

        Configuration config = framework.config.Configuration.getInstance();

        return config.getString("appinfo." + key);

    }

    public static String getNTSPdfFlolder(String ServerIP) {

        Configuration config = framework.config.Configuration.getInstance();

        return config.getString("ntspdf." + ServerIP + ".folder");

    }

    public static String getXLSFlolder(String ServerIP) {

        Configuration config = framework.config.Configuration.getInstance();

        return config.getString("xls." + ServerIP + ".folder");

    }

    public static String getCertPath(String ServerIP) {

        Configuration config = framework.config.Configuration.getInstance();

        return config.getString("cert." + ServerIP + ".path");

    }

    public static String getCertPassword(String ServerIP) {

        Configuration config = framework.config.Configuration.getInstance();

        return config.getString("cert." + ServerIP + ".password");

    }

    public static String getServerAlias() {

        Configuration config = framework.config.Configuration.getInstance();

        return config.getString("server.alias.docroot");

    }

    public static String getUserKey() {
        Configuration config = framework.config.Configuration.getInstance();

        return config.getString("user_key");
    }

    public static String getToken(String email, String password) throws IOException {
        // Create the post data
        // Requires a field with the email and the password
        StringBuilder builder = new StringBuilder();
        builder.append("Email=").append(email);
        builder.append("&Passwd=").append(password);
        builder.append("&accountType=GOOGLE");
        builder.append("&source=onecallserver");
        builder.append("&service=ac2dm");

        // Setup the Http Post
        byte[] data = builder.toString().getBytes();
        URL url = new URL("https://www.google.com/accounts/ClientLogin");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setUseCaches(false);
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty("Content-Length", Integer.toString(data.length));

        // Issue the HTTP POST request
        OutputStream output = con.getOutputStream();
        output.write(data);
        output.close();

        // Read the response
        BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String line = null;
        String auth_key = null;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("Auth=")) {
                auth_key = line.substring(5);
            }
        }

        int responseCode = con.getResponseCode();
        getLogger().debug("getToken responseCode----------->>>" + responseCode);

        // Finally get the authentication token
        // To something useful with it
        return auth_key;
    }

    public static boolean isDebug() {

        Configuration config = framework.config.Configuration.getInstance();

        return config.getBoolean("server.debug.status");

    }

    public static void setResult(HttpServletResponse response, int result) {
        setResult(response, result, result >= 0 ? "Succ" : "Fail");
    }

    public static void setResult(HttpServletResponse response, int result, String msg) {
        // 화면에서 JSON.parse(response.data)에서 오류가 발생하지 않도록 json array 형식으로 기본값 설정.
        setResult(response, result, msg, "data", "[]");
    }

    public static void setResult2HTML(HttpServletResponse response, int result, String msg, boolean html5) {
        if (html5) {
            // 화면에서 JSON.parse(response.data)에서 오류가 발생하지 않도록 json array 형식으로 기본값 설정.
            setResult(response, result, msg, "data", "[]");
        } else {
            response.setContentType("text/html; charset=utf-8");
            try {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void setResult(HttpServletResponse response, int result, String msg, String key, String data) {
        response.setContentType("text/json; charset=utf-8");
        // response.setContentType("text/html; charset=euc-kr");
        try {

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("result", result);
            map.put("message", msg);
            map.put(key, data);

            String json = JsonUtil.format(map);
            // 모바일 앱에서 에러메시지 표시를 위해서..
            // SC_INTERNAL_SERVER_ERROR 코드로 리턴할 경우 모바일 앱에서 메시지를 제대로 표시하지 못함.
            // response.setStatus(HttpServletResponse.SC_OK);
            // pc용 화면에서 데이터 없음의 경우 -10 코드를 사용하며, SC_INTERNAL_SERVER_ERROR 로 리턴이 되어야 ,ajax
            // error 루틴에서 메시지를 표시하도록 되어 있음.
            if (result > -10) {
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            response.getWriter().write(json);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void sendError(HttpServletResponse response, String msg) {
        sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
    }

    public static void sendError(HttpServletResponse response, int ret, String msg) {
        response.setContentType("text/json; charset=utf-8");
        // response.setContentType("text/html; charset=euc-kr");
        try {

            Map<String, Object> error = new HashMap<String, Object>();
            error.put("code", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            error.put("message", msg);

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("error", JsonUtil.parse(JsonUtil.format(error)));

            String json = JsonUtil.format(map);
            // 모바일 앱에서 에러메시지 표시를 위해서..
            // SC_INTERNAL_SERVER_ERROR 코드로 리턴할 경우 모바일 앱에서 메시지를 제대로 표시하지 못함.
            // response.setStatus(HttpServletResponse.SC_OK);

            response.setStatus(ret);

            response.getWriter().write(json);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void setResult(HttpServletResponse response, RecordSet rs) {
        setResult(response, rs, "json", "utf-8");
    }

    public static void setResult(HttpServletResponse response, RecordSet rs, boolean setMeta) {

        if (setMeta) {

            ArrayList<Map<String, String>> meta = new ArrayList<Map<String, String>>();

            for (int col = 0; col < rs.getColumnCount(); col++) {
                Map<String, String> rsmt = new HashMap<String, String>();

                rsmt.put("column_name", rs.getColumns()[col]);
                rsmt.put("data_type", rs.getColumnsInfo()[col]);
                rsmt.put("column_size", rs.getColumnsSize()[col] + "");

                meta.add(rsmt);
            }

            setResult(response, rs, "json", "utf-8", meta);

        } else {
            setResult(response, rs, "json", "utf-8");
        }

    }

    public static void setResult(HttpServletResponse response, HashMap map, boolean lowerkey) {
        if (lowerkey) {
            setResult(response, map);
        } else {
            setResult(response, map2json(map), "json", "utf-8");
        }
    }

    public static void setResult(HttpServletResponse response, Map map) {
        setResult(response, JsonUtil.format(map), "json", "utf-8");
    }

    // JSonUtil.format()함수가 hashmap key값을 무조건 소문자로 바꿔서...ㅡㅡ;
    public static String map2json(HashMap<String, Object> map) {

        StringBuffer result = new StringBuffer();

        for (String key : map.keySet()) {
            result.append("\"" + key + "\":\"" + map.get(key).toString() + "\",");
        }

        return "[{" + StringUtils.removeEnd(result.toString(), ",") + "}]";
    }

    public static void setResult(HttpServletResponse response, RecordSet rs, String type, String charset) {
        ArrayList<Map<String, String>> meta = new ArrayList<Map<String, String>>();
        setResult(response, rs, type, charset, meta);
    }

    public static void setResult(HttpServletResponse response, RecordSet rs, String type, String charset, ArrayList<Map<String, String>> meta) {
        if (charset == null || "".equals(charset)) {
            charset = "charset=utf-8";
        } else if (!"".equals(charset)) {
            charset = "charset=" + charset;
        }

        try {
            String formatRS = "";

            if ("json".equals(type)) {
                response.setContentType("text/json; " + charset);

                Map<String, Object> map = new HashMap<String, Object>();
                map.put("result", 0);
                map.put("message", "success");
                map.put("data", JsonUtil.format(rs));
                if (meta.size() > 0)
                    map.put("meta", JsonUtil.stringify(meta));

                formatRS = JsonUtil.format(map);
            } else if ("xml".equals(type)) {
                response.setContentType("text/xml; " + charset);
                formatRS = XmlUtil.format(rs);
            } else if ("RD".equals(type)) {
                response.setContentType("text/plan; " + charset);
                formatRS = RDUtil.format(rs);
            } else {
                response.setContentType("text/json; " + charset);
                formatRS = JsonUtil.format(rs);
            }

            setResult(response, formatRS, type, charset);

        } catch (Exception e) {
            setResult(response, -1, e.getMessage());
        }
    }

    private static String sha256(String msg) throws NoSuchAlgorithmException {

        try {
            MessageDigest mda = MessageDigest.getInstance("SHA-256");
            byte[] digesta = mda.digest(msg.getBytes());
            // BASE64Encoder encoder = new BASE64Encoder();
            // return encoder.encode(digesta);

            return Base64.getEncoder().encodeToString(digesta);
        } catch (Exception e) {
            return "";
        }

    }

    /**
     * Function ScP(pdata) if pdata <> "" then Set CrossCert =
     * Server.CreateObject("AxCrossCert2.CrossCert2") ' 1 : MD5 ' 2 : SHA1 ' 3 :
     * SHA256 ' response.write CrossCert.Hash(1,"123") ' response.write "<br>
     * " ' response.write CrossCert.Hash(2,"123") ' response.write "<br>
     * " ' response.write CrossCert.Hash(3,"123") keyvalue =
     * CrossCert.Hash(3,userkey) ' 한번 꼬아주기..
     * <p>
     * hsahpwd = CrossCert.Hash(3,pdata & keyvalue) hsahpwd =
     * CrossCert.Hash(3,hsahpwd) Set CrossCert = Nothing end if ScP = hsahpwd End
     * Function
     *
     * @param data
     * @return
     */
    public static String ScP(String data) {

        try {

            String userkey = "lks#@doore^^&&##";

            _logger.debug("userkey=" + sha256(userkey));
            _logger.debug("data+userkey=" + data + sha256(userkey));
            _logger.debug("pwd=" + sha256(sha256(data + sha256(userkey))));

            return sha256(sha256(data + sha256(userkey)));

        } catch (Exception e) {
            return data;
        }

    }

    public static void setResult(HttpServletResponse response, String data, String type, String charset) {
        if (charset == null || "".equals(charset)) {
            charset = "charset=utf-8";
        } else if (!"".equals(charset) && !StringUtils.contains(charset, "charset")) {
            charset = "charset=" + charset;
        }

        try {
            response.setStatus(HttpServletResponse.SC_OK);

            if ("json".equals(type)) {
                response.setContentType("text/json; " + charset);
            } else if ("xml".equals(type)) {
                response.setContentType("text/xml; " + charset);
            } else if ("RD".equals(type)) {
                response.setContentType("text/plan; " + charset);
            } else {
                response.setContentType("text/json; " + charset);
            }
            // getLogger().debug("\nBefore");
            // getLogger().debug(data);

            // StringUtils.replace(data, "\n", "");
            data = StringUtils.replace(data, "\b", ""); /* oracle에서 ascii(substr(컬럼명, 1, 1)) = 8 이라고 나와서 ascii문자표에서 확인 후 \b 백스페이스 제거. */
            // StringUtils.replace(data, "\", "");

            // data = st.replaceAll("\\s+",""); 안됨.
            // data = StringUtils.deleteWhitespace(data); 안됨.
            // StringUtils.replace(formatRS, "\\\\\\\\", "^");
            // StringUtils.replace(formatRS, "\\\\\\", "*");
            // StringUtils.replace(formatRS, "\\\\", "$");

            response.getWriter().write(data);

            // getLogger().debug("\nAfter");
            // getLogger().debug(data);
        } catch (Exception e) {
            e.printStackTrace();
            setResult(response, e.getMessage());
        }
    }

    public static void setResult(HttpServletResponse response, String msg) {
        setResult(response, -1, msg);
    }

    public static void setSuccess(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/plain");
        response.setContentLength(0);
    }

    public static void Sleep(int ms) {
        try {
            Thread.sleep(ms); // 1초 대기
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void copyFileUsingStream(String root, String srcName, String destName) throws IOException {
        File source = new File(root + srcName);
        File target = new File(root + destName);

        FileUtils.moveFile(source, target);

        Sleep(300);
        if (source.exists()) {
            FileUtils.moveFile(source, target);
        }
    }

    /**
     * url에 포함된 한글만 인코딩 하여 리턴한다.
     *
     * @param url
     * @return 출처: https://cofs.tistory.com/264 [CofS]
     */
    public static String encoding(String url) {
        char[] txtChar = url.toCharArray();
        for (int j = 0; j < txtChar.length; j++) {
            if (txtChar[j] >= '\uAC00' && txtChar[j] <= '\uD7A3') {
                String targetText = String.valueOf(txtChar[j]);
                try {
                    url = url.replace(targetText, URLEncoder.encode(targetText, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        return url;
    }

    public static String getSMSPhone() {

        Configuration config = framework.config.Configuration.getInstance();

        return config.getString("sms.sender.number");
    }

    public static String getSMSID() {
        Configuration config = framework.config.Configuration.getInstance();

        return config.getString("sms.sender.id");
    }

    public static String getSMSPWD() {
        Configuration config = framework.config.Configuration.getInstance();

        return config.getString("sms.sender.password");
    }

    public static String getClientIp(HttpServletRequest request) {

        String ip = "";

        try {
            ip = request.getHeader("X-Forwarded-For");

            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
        } catch (Exception e) {
            ip = "error";
        }

        return ip;
    }

    public static String getServerIp() {
        String ip = "";
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("doore.co.kr", 80));
            ip = socket.getLocalAddress().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
            ip = "127.0.0.1";
        }

        return ip;
    }

    public static void setMessage(HttpServletResponse response, int i, Exception e) {

        if (e instanceof SQLException) {
            PortalUtil.setMessage(response, -13, (SQLException) e);
        } else {
            StringBuffer b = new StringBuffer(e.getClass().toString());
            int step = 0;
            for (StackTraceElement s : e.getStackTrace()) {
                b.append(s.toString() + "\n");
                if (step++ > 3)
                    break;
            }
            PortalUtil.setResult(response, -14, b.toString());
        }

    }

    public static void setMessage(HttpServletResponse response, int error, SQLException e) {
        String src = e.getMessage();
        setMessage(response, error, e, src);
    }

    public static void setMessage(HttpServletResponse response, int error, SQLException e, String src) {


        String msgsource[] = src.split("\n");

        StringBuffer userMsg = new StringBuffer();

        for (String msg : msgsource) {
            if (msg.startsWith("ORA-") || msg.startsWith("ErrorMessage") || msg.startsWith("SQLException")) {
                String msgs[] = msg.split(":");

                if (msgs.length > 2) {
                    for (int i = 1; i < msgs.length; i++) {
                        if (StringUtils.isNotBlank(msgs[i]) && !msgs[i].startsWith("ORA-") && !msgs[i].startsWith("ErrorMessage") && !msgs[i].startsWith("SQLException")) {
                            userMsg.append(msgs[i]);
                        }
                    }
                } else if (msgs.length == 2) {
                    if (StringUtils.isNotBlank(msgs[1]) && !msgs[1].startsWith("ORA-") && !msgs[1].startsWith("ErrorMessage") && !msgs[1].startsWith("SQLException")) {
                        userMsg.append(msgs[1]);
                    }
                } else {
                    userMsg.append("\nLen=" + msgs.length + "***" + msg);
                }

            } else if (StringUtils.isNotBlank(msg) && !msg.startsWith("ErrorCode") && !msg.startsWith("SQL")) {

                userMsg.append("" + msg + "");
            }
        }

        if ("".equals(userMsg.toString())) {
            userMsg.append(src);
        } else {
            userMsg.append("\n\n\n\n\n\n\n");
            userMsg.append("<hr style='width:40%;border:1px solid gray;float:left;'>");
            userMsg.append("<a style='margin-top:-7px;' href='#' onclick='if(\"none\" == $(\"#_sql_message\").css(\"display\")){$(\"#_sql_message\").show();}else{$(\"#_sql_message\").hide();}' ><label>More...</label></a>");
            userMsg.append("<hr style='width:40%;border:1px solid gray;float:right;'><br><br>");
            userMsg.append("<div id='_sql_message' style='display:none;'>" + src + "</div>");
        }

        PortalUtil.setResult(response, -9, userMsg.toString());

    }

    /**
     * 맥 OS의 한글 인코딩 방식을 고려한 파일명 보정 [출처][JAVA] 맥(Mac OS)에서 파일 업로드 시 파일명 자모음 분리되는
     * 경우(자소분리) 처리방법|작성자 착한흑곰
     *
     * @param fileName
     * @param convertToMacFileName
     * @return
     */
    public static String convertFileNameForMac(String fileName, boolean convertToMacFileName) {
        if (fileName == null) {
            fileName = "";
        }

        // 맥은 NFD(조합형), 윈도우는 NFC(완성형) 방식을 사용한다.
        if (convertToMacFileName) {
            // 윈도우 파일명 => 맥 파일명
            if (Normalizer.isNormalized(fileName, Normalizer.Form.NFC)) {
                fileName = Normalizer.normalize(fileName, Normalizer.Form.NFD);
            }
        } else {
            // 맥 파일명 => 윈도우 파일명 (맥에서 파일 업로드 시 사용)
            if (Normalizer.isNormalized(fileName, Normalizer.Form.NFD)) {
                fileName = Normalizer.normalize(fileName, Normalizer.Form.NFC);
            }
        }

        return fileName;
    }

    /**
     * 문자열을 길이에 맞춰 인코딩 한다.
     *
     * @param src
     * @param size
     * @return
     */
    public static String encodeBase64(String src, int size) {
        src = paddingLeft(src, size);
        return new String(org.apache.commons.codec.binary.Base64.encodeBase64(src.getBytes()));
    }

    public static String encodeBase64(String src) {
        return new String(org.apache.commons.codec.binary.Base64.encodeBase64(src.getBytes()));
    }

    public static String decodeBase64(String src) {
        return new String(org.apache.commons.codec.binary.Base64.decodeBase64(src.getBytes()));
    }

    public static String paddingLeft(String str, int size) {
        byte result[] = new byte[size];
        if (str.length() >= result.length) {
            return str;
        }

        getLogger().debug("Result Length=" + result.length);
        int j = 0;
        for (int i = 0; i < result.length; i++) {

            getLogger().debug("Result " + i + " =" + new String(result));

            if (i < (size - str.length())) {
                result[i] = '0';
            } else {
                result[i] = (byte) str.charAt(j++);
            }
        }

        return new String(result);

    }

    /**
     * 알림을 이메일로 발송할 경수 수신자 정보를 이용하여 발신메일 본문을 생성한다.
     *
     * @param session
     * @param map
     * @param loginBean
     * @param target
     * @return
     */
    public static HashMap<String, Object> makeMailContents(HttpSession session, HashMap<String, Object> map, LoginBean loginBean, String target) {

        StringBuilder message = new StringBuilder();
        BufferedReader bufferedReader = null;
        HashMap<String, Object> retMap = new HashMap<String, Object>();
        String result = "";
        try {
            getLogger().debug("\nE-Mail Source = " + map);

            String context_root = session.getServletContext().getRealPath("/");

            String temp_path = context_root + "/_assets/templates/" + map.get("letter_type") + "4mail.html";
            if (!new File(temp_path).exists()) {
                // 해당 공문유형의 메일 템플릿이 없을 경우, 기본 템플릿으로 다시 확인한다.
                temp_path = context_root + "/_assets/templates/default4mail.html";
            }

            if (!new File(temp_path).exists()) {
                // 템플릿이 없는 경우.
                retMap.put("result", -1);
                retMap.put("message", "공문 종류에 맞는 메일 템플릿이 존재하지 않습니다. \n" + temp_path);
            } else {

                bufferedReader = new BufferedReader(new FileReader(temp_path), 16 * 1024);

                String buf = "";
                while ((buf = bufferedReader.readLine()) != null) {
                    message.append(buf + "\n");
                }

                result = message.toString();
                getLogger().debug("\nE-Mail Template = " + result);

                result = result.replace("{$lessee_name}", map.get("lessee_name") + "");
                result = result.replace("{$subject}", map.get("subject") + "");

                result = result.replace("{$send_date}", map.get("send_date") + "");
                result = result.replace("{$send_user}", loginBean.getDeptName() + " " + loginBean.getUserName() + " " + loginBean.getPosition() + " ( Phone : " + loginBean.getPhoneNumber() + " )");

                String key = PortalUtil.encodeBase64(map.get("lessee_id") + "", 10);
                String target_url = "https://dpms.doore.co.kr:9443/lessee-login.jsp?key=" + key;
                String unsubscribe_url = "https://dpms.doore.co.kr:9443/unsubscribe/mail.jsp?key=lessee_id&value=" + map.get("lessee_id") + "&target=" + target;
                result = result.replace("{$target_url}", target_url);
                result = result.replace("{$unsubscribe_url}", unsubscribe_url);

                getLogger().debug(result);

                retMap.put("result", 0);
                retMap.put("message", result);
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            retMap.put("result", -1);
            retMap.put("message", e.getMessage());
            return retMap;
        } finally {
            try {
                if (bufferedReader != null) bufferedReader.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return retMap;

    }

    public static HashMap<String, Object> makeMailContents(HttpSession session, Box box, LoginBean loginBean, String target) {

        StringBuilder message = new StringBuilder();
        BufferedReader bufferedReader = null;
        HashMap<String, Object> retMap = new HashMap<String, Object>();
        String result = "";
        try {
            getLogger().debug("\nE-Mail Source = " + box);

            String context_root = session.getServletContext().getRealPath("/");

            String temp_path = context_root + "/_assets/templates/" + box.getString("letter_type") + "4mail.html";
            if (!new File(temp_path).exists()) {
                // 해당 공문유형의 메일 템플릿이 없을 경우, 기본 템플릿으로 다시 확인한다.
                temp_path = context_root + "/_assets/templates/default4mail.html";
            }

            if (!new File(temp_path).exists()) {
                // 템플릿이 없는 경우.
                retMap.put("result", -1);
                retMap.put("message", "공문 종류에 맞는 메일 템플릿이 존재하지 않습니다. \n" + temp_path);
            } else {

                bufferedReader = new BufferedReader(new FileReader(temp_path), 16 * 1024);

                String buf = "";
                while ((buf = bufferedReader.readLine()) != null) {
                    message.append(buf + "\n");
                }

                result = message.toString();
                getLogger().debug("\nE-Mail Template = " + result);

                result = result.replace("{$owner_name}", box.getString("owner_name") + "");
                result = result.replace("{$subject}", box.getString("subject") + "");

                result = result.replace("{$send_date}", box.getString("send_date") + "");
                result = result.replace("{$send_user}", loginBean.getDeptName() + " " + loginBean.getUserName() + " " + loginBean.getPosition() + " ( Phone : " + loginBean.getPhoneNumber() + " )");

                String target_url = box.getString("link");
                String unsubscribe_url = "https://dpms.doore.co.kr:9443/unsubscribe/mail.jsp?key=owner_id&value=" + box.getLong("owner_id") + "&target=" + target;
                result = result.replace("{$target_url}", target_url);
                result = result.replace("{$unsubscribe_url}", unsubscribe_url);

                getLogger().debug(result);

                retMap.put("result", 0);
                retMap.put("message", result);
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            retMap.put("result", -1);
            retMap.put("message", e.getMessage());
            return retMap;
        } finally {
            try {
                if (bufferedReader != null) bufferedReader.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return retMap;

    }

    /**
     * 알리고에서 수신한 유니코드 문자열을 한글로 변환한다.
     *
     * @param uni
     * @return
     */
    public static String uniToKor(String uni) {
        StringBuffer result = new StringBuffer();

        for (int i = 0; i < uni.length(); i++) {
            if (uni.charAt(i) == '\\' && uni.charAt(i + 1) == 'u') {
                Character c = (char) Integer.parseInt(uni.substring(i + 2, i + 6), 16);
                result.append(c);
                i += 5;
            } else {
                result.append(uni.charAt(i));
            }
        }
        return result.toString();
    }

    /**
     * 한글을 유니코드 문자열로 변환한다.
     *
     * @param kor
     * @return
     */
    public static String korToUni(String kor) {
        StringBuffer result = new StringBuffer();

        for (int i = 0; i < kor.length(); i++) {
            int cd = kor.codePointAt(i);
            if (cd < 128) {
                result.append(String.format("%c", cd));
            } else {
                result.append(String.format("\\u%04x", cd));
            }
        }
        return result.toString();
    }


    public static String getCompanyName(String key) {
        try {
            Configuration config = framework.config.Configuration.getInstance();
            return config.getString("appinfo." + key);
        } catch (Exception e) {
            return "";
        }
    }
}
