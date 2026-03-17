package ax5;

import com.LoginBean;
import framework.action.Action;
import framework.action.Box;
import framework.util.JsonUtil;
import framework.util.StringUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import util.DBLog;
import util.PortalUtil;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class DeleteAction extends Action {

    String SAVE_DIR = "/upload_data/ax5/";
    String SAVE_URL = "/upload_data/ax5/";

    int fileSizeLimit = 50 * 1000 * 1000;
    String encoding = "utf-8";

    private UploadDao dao = null;

    private UploadDao getSelect(String service) throws Exception {
        if (dao == null || !dao.getService().equals(service)) {
            dao = new UploadDao(getConnectionManager(service));
            dao.setService(service);
        }
        return dao;
    }

    public void processInit() {

        getLogger().debug("\nStart processDelete ...\n");

        HashMap<String, Object> map = new HashMap<String, Object>();
        String context_root = getSession().getServletContext().getRealPath("/");

        try {
            getRequest().setCharacterEncoding("UTF-8");

            getLogger().debug(this.getInput());

            InputStream inputStream = getRequest().getInputStream();
            getLogger().debug(inputStream);

            JSONParser jsonParser = new JSONParser();
            InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");

            getLogger().debug(reader);

            JSONArray jsonArray = (JSONArray) jsonParser.parse(reader);

            getLogger().debug(jsonArray);

            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject obj = (JSONObject) jsonArray.get(i);

                getLogger().debug(JsonUtil.stringify(obj));

                /*
                 * attach_seq : "32" building_id : "0" download :
                 * "/upload_data/ax5/2023-04/조견표2.txt" ext : "txt" filesize : "83380" name :
                 * "조견표2.txt" savename : "조견표2.txt" target_column : "building_id"
                 * target_table : "building_attach" thumburl :
                 * "/upload_data/ax5/2023-04/조견표2.txt"
                 */

                map.put("attach_seq", obj.get("attach_seq"));
                map.put("download", obj.get("download"));
                map.put("target_db", obj.get("target_db"));
                map.put("target_table", obj.get("target_table"));
                map.put("target_column", obj.get("target_column"));

                String deletefile = obj.get("download").toString();

                deletefile = new String(deletefile.getBytes("UTF-8"), "UTF-8");

                File file = new File(context_root, deletefile);

                if (file.exists()) {
                    if (file.isFile()) {

                        if (applyDocumentDB(map)) {
                            getLogger().debug(file.getPath() + " deleted.");

                            if (file.delete()) {

                                PortalUtil.setResult(getResponse(), 0, "success");
                                return;

                            } else {
                                getLogger().debug("삭제중 오류가 발생하였습니다.");
                                PortalUtil.sendError(this.getResponse(), HttpServletResponse.SC_OK, "삭제중 오류가 발생하였습니다.");
                                return;
                            }

                        } else {
                            PortalUtil.sendError(getResponse(), -1, "fail");
                            return;
                        }

                    } else {
                        getLogger().debug("해당파일은 삭제할 수 없습니다.");
                        PortalUtil.sendError(this.getResponse(), HttpServletResponse.SC_OK, "해당파일은 삭제할 수 없습니다.");
                        return;
                    }
                } else {
                    applyDocumentDB(map);
                    getLogger().debug("해당파일은 이미 삭제 되었거나 서버에 존재하지 않습니다.\\n필요한 경우 다시 첨부하십시오.");
                    PortalUtil.setResult(this.getResponse(), "해당파일은 이미 삭제 되었거나 서버에 존재하지 않습니다.\n필요한 경우 다시 첨부하십시오.");
                    return;
                }

            }

        } catch (Exception ex) {
            PortalUtil.sendError(this.getResponse(), ex.getMessage());
            ex.printStackTrace();
        }
    }

    public boolean applyDocumentDB(HashMap<String, Object> map) {
        LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");
        try {

            HashMap<String, Object> result = new HashMap<String, Object>();
            String host = "http://" + getRequest().getHeader("host");
            String target_db = "dpms";

            if (map.containsKey("target_db") && StringUtil.isNotEmpty(map.get("target_db").toString())) {
                target_db = (String) map.get("target_db");
            }

            result = getSelect(target_db).deleteAttachFilesInfo(map, this.getSession(), host);
            if (0 != (Integer) result.get("result")) {
                getLogger().debug("Rollback=" + result);
                this.getConnectionManager(target_db).rollback();
                return false;

            } else {

                this.getConnectionManager(target_db).commit();
                return true;
            }

        } catch (Exception e) {
            this.getConnectionManager("dpms").rollback();
            DBLog.errorLog(getRequest(), getConnectionManager("dpms"), e, this);
            return false;
        }
    }

    public void processDeleteAll() {

        getLogger().debug("\nStart processDeleteAll ...\n");

        String context_root = getSession().getServletContext().getRealPath("/");
        HashMap<String, Object> map = new HashMap<String, Object>();

        try {
            getRequest().setCharacterEncoding("UTF-8");

            InputStream inputStream = getRequest().getInputStream();
            getLogger().debug(inputStream);

            JSONParser jsonParser = new JSONParser();
            InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");

            getLogger().debug(reader);

            JSONArray jsonArray = (JSONArray) jsonParser.parse(reader);

            getLogger().debug(jsonArray);

            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject obj = (JSONObject) jsonArray.get(i);

                getLogger().debug(JsonUtil.stringify(obj));
                map = new HashMap<String, Object>();

                map.put("attach_seq", obj.get("attach_seq"));
                map.put("download", obj.get("download"));
                map.put("target_db", obj.get("target_db"));
                map.put("target_table", obj.get("target_table"));
                map.put("target_column", obj.get("target_column"));

                String deletefile = obj.get("download").toString();

                deletefile = new String(deletefile.getBytes("UTF-8"), "UTF-8");
                File file = new File(context_root, deletefile);

                if (file.exists()) {
                    if (file.isFile()) {
                        if (file.delete()) {
                            // returnResult(this.getResponse(), "Delete success.");
                            applyDocumentDB(map);
                            getLogger().debug(file.getPath() + " deleted.");
                        } else {
                            PortalUtil.Sleep(300);
                            // 삭제를 다시한번 시도한다.
                            file = new File(context_root, deletefile);
                            if (file.exists()) {
                                if (file.delete()) {
                                    // returnResult(this.getResponse(), "Delete success.");
                                    applyDocumentDB(map);
                                    getLogger().debug(file.getPath() + " deleted.");
                                } else {
                                    PortalUtil.sendError(this.getResponse(), HttpServletResponse.SC_OK, "삭제중 오류가 발생하였습니다.");
                                    return;
                                }
                            }
                        }
                    } else {
                        PortalUtil.sendError(this.getResponse(), HttpServletResponse.SC_OK, "요청건은 파일이 아니므로 삭제할 수 없습니다.");
                        return;

                    }
                } else {
                    PortalUtil.sendError(this.getResponse(), HttpServletResponse.SC_OK, "해당파일이 존재하지 않아 삭제할 수 없습니다.");
                    return;
                }

            }

            PortalUtil.setResult(getResponse(), "success");

        } catch (Exception ex) {
            PortalUtil.sendError(this.getResponse(), ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void processDeleteAttachFile() {

        getLogger().debug("\nStart processDeleteAttachFile ...\n");

        String context_root = getSession().getServletContext().getRealPath("/");

        try {

            Box box = this.getInput();

            ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) JsonUtil.parse(box.getRawString("rows"));

            for (HashMap<String, Object> map : list) {

                String deletefile = map.get("download") + "";

                deletefile = new String(deletefile.getBytes("UTF-8"), "UTF-8");
                File file = new File(context_root, deletefile);

                if (file.exists()) {
                    if (file.isFile()) {
                        if (file.delete()) {
                            // returnResult(this.getResponse(), "Delete success.");
                            applyDocumentDB(map);
                            getLogger().debug(file.getPath() + " deleted.");
                        } else {
                            PortalUtil.Sleep(300);
                            // 삭제를 다시한번 시도한다.
                            file = new File(context_root, deletefile);
                            if (file.exists()) {
                                if (file.delete()) {
                                    // returnResult(this.getResponse(), "Delete success.");
                                    applyDocumentDB(map);
                                    getLogger().debug(file.getPath() + " deleted.");
                                } else {
                                    PortalUtil.setResult(this.getResponse(), "삭제중 오류가 발생하였습니다.");
                                    return;
                                }
                            }
                        }
                    } else {
                        PortalUtil.setResult(this.getResponse(), "요청건은 파일이 아니므로  삭제할 수 없습니다.");
                        return;

                    }
                } else {
                    if (applyDocumentDB(map)) {
                        getLogger().debug(map.get("download") + " Not found & DB info remove complete.");
                    } else {
                        PortalUtil.setResult(this.getResponse(), "첨부파일 DB정도 제거중 오류 발생. \n 해당파일이 존재하지 않아 삭제할 수 없습니다.");
                        return;
                    }
                }

            } // for

            PortalUtil.setResult(getResponse(), "success");

        } catch (Exception ex) {
            PortalUtil.sendError(this.getResponse(), ex.getMessage());
            ex.printStackTrace();
        }
    }

}
