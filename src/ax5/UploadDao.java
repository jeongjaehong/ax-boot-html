package ax5;

import com.LoginBean;
import framework.action.Box;
import framework.db.*;
import framework.util.StringUtil;
import jakarta.servlet.http.HttpSession;
import org.apache.axis.utils.StringUtils;
import util.PortalUtil;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class UploadDao extends SelectDaoSupport {
    private String service = "";

    public UploadDao(ConnectionManager mgr) {
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

    // 첨부파일 정보 저장
    // "[{
    // "fileSize":"152434",
    // "saveName":"105883.사용자.접수하기.01.png",
    // "name":"105883.사용자.접수하기.01.png",
    // "thumbUrl":"/upload_data/ax5/temp/105883.사용자.접수하기.01.png",
    // "download":"/upload_data/ax5/temp/105883.사용자.접수하기.01.png",
    // "type":"png","@i":0,"@first":true},{"fileSize":"131621",
    // "saveName":"105883.사용자.초기화면.png",
    // "name":"105883.사용자.초기화면.png","thumbUrl":
    // "/upload_data/ax5/temp/105883.사용자.초기화면.png",
    // "download":"/upload_data/ax5/temp/105883.사용자.초기화면.png",
    // "type":"png",
    // "@i":1,
    // "@first":false}, ...]"

    public HashMap<String, Object> insertAttachFiles(HashMap<String, Object> fileMap, String user_id, HttpSession session, String host) throws Exception {

        long attach_seq = -1;
        StringBuilder query = new StringBuilder("");
        SQLPreparedStatement pstmt = null;
        SelectConditionObject cond = new SelectConditionObject();

        HashMap<String, Object> result = new HashMap<String, Object>();

        try {

            if ("default".equals(fileMap.get("target_db"))) {
                RecordSet rs = select(" select SEQ_ATTACH.NEXTVAL as attach_seq from dual ");
                if (rs.nextRow()) {
                    attach_seq = rs.getLong("attach_seq");
                } else {
                    throw new Exception(" 첨부파일 순번을 생성할 수 없습니다. ");
                }

                query = new StringBuilder();
                query.append("\n/* " + this.getClass().toString() + ".insertAttachFiles() */\n");
                query.append(" insert into " + fileMap.get("target_table"));
                query.append(" 	( attach_seq, " + fileMap.get("target_column") + ", attach_ext, primary_yn, original_name, attach_name, attach_url, attach_size, attach_group, registrant, registration_date )  ");
                query.append(" values  ");
                query.append(" 	( ? /*attach_seq*/, ?/*fk*/, lower(?)/*attach_ext*/, nvl( ?, 'No')/*primary_yn*/, ?/*original_name*/, ?/*attach_name*/, ?/*attach_url*/, ?/*attach_size*/, nvl(?, 'normal')/*attach_group*/, ?/*registrant*/, sysdate ) ");


            } else {
                this.getConnectionManager().createStatement("insert into xtendus.lgs_seq_attach (target_table) values ('" + fileMap.get("target_table") + "')").executeUpdate();

                RecordSet rs = select("SELECT IDENT_CURRENT('xtendus.lgs_seq_attach') AS attach_seq ");
                if (rs.nextRow()) {
                    attach_seq = rs.getLong("attach_seq");
                } else {
                    throw new Exception(" 첨부파일 순번을 생성할 수 없습니다. ");
                }

                query = new StringBuilder();
                query.append("\n/* " + this.getClass().toString() + ".insertAttachFiles() */\n");
                query.append(" insert into " + fileMap.get("target_table"));
                query.append(" 	( attach_seq, " + fileMap.get("target_column") + ", attach_ext, primary_yn, original_name, attach_name, attach_url, attach_size, attach_group, registrant, registration_date )  ");
                query.append(" values  ");
                query.append(" 	( ? /*attach_seq*/, ?/*fk*/, lower(?)/*attach_ext*/, isnull( ?, 'No')/*primary_yn*/, ?/*original_name*/, ?/*attach_name*/, ?/*attach_url*/, ?/*attach_size*/, isnull(?, 'normal')/*attach_group*/, ?/*registrant*/, getdate() ) ");


            }


            pstmt = this.getConnectionManager().createPrepareStatement(query.toString());


            cond.setObject(attach_seq);
            long fk = (Long) fileMap.get(fileMap.get("target_column"));
            if (fk <= 0) {
                cond.setObject(null);
            } else {
                cond.setObject(fk);
            }
            cond.setObject(fileMap.get("ext"));
            cond.setObject(fileMap.get("primary"));
            cond.setObject(PortalUtil.convertFileNameForMac(fileMap.get("name").toString(), false));
            cond.setObject(PortalUtil.convertFileNameForMac(fileMap.get("savename").toString(), false));
            cond.setObject(PortalUtil.convertFileNameForMac(fileMap.get("download").toString(), false));
            long size = 0;
            try {
                size = Long.parseLong(fileMap.get("filesize").toString());
            } catch (Exception e) {
                size = 0;
            }
            cond.setObject(size);
            cond.setObject(fileMap.get("attach_group"));
            cond.setObject(user_id);

            pstmt.set(cond.getParameter());

            pstmt.executeUpdate();

            result.put("attach_seq", attach_seq);
            result.put("result", 0);
            result.put("message", "정상 처리 되었습니다.");

        } catch (Exception e) {
            result.put("result", -1);
            result.put("attach_seq", attach_seq);
            result.put("message", e.getMessage());

            getLogger().error("insertAttachFiles error", e);

            getLogger().error("insertAttachFiles files=" + fileMap.toString());
            getLogger().error("error next_docu_id = " + attach_seq);
            getLogger().error("error query = " + query.toString());
        }

        return result;
    }

    public HashMap<String, Object> deleteAttachFilesInfo(ArrayList<HashMap<String, Object>> jarray, HttpSession session, String host) throws UnsupportedEncodingException {
        HashMap<String, Object> result = new HashMap<String, Object>();
        String context_root = session.getServletContext().getRealPath("/");
        for (HashMap<String, Object> map : jarray) {

            String deletefile = map.get("download").toString();

            deletefile = new String(deletefile.getBytes("UTF-8"), "UTF-8");

            File file = new File(context_root, deletefile);
            getLogger().debug("\ndelete file info=" + map);
            result = deleteAttachFilesInfo(map, session, host);
            if ((Integer) result.get("result") < 0) {
                this.getConnectionManager().rollback();
                return result;
            } else {
                if (file.delete()) {
                    getLogger().debug("\nAttach file deleted=" + file.getPath() + file.getName());
                } else {
                    this.getConnectionManager().rollback();
                    result.put("result", -1);
                    result.put("message", "첨부파일 삭제중 오류가 발생하였습니다.");
                    return result;
                }
            }

        }

        return result;
    }

    public HashMap<String, Object> deleteAttachFilesInfo(HashMap<String, Object> fileMap, HttpSession session, String host) {

        long attach_seq = -1;
        StringBuilder query = new StringBuilder("");
        SQLPreparedStatement pstmt = null;
        SelectConditionObject cond = new SelectConditionObject();

        HashMap<String, Object> result = new HashMap<String, Object>();

        try {

            query = new StringBuilder();
            query.append(" delete from  " + fileMap.get("target_table"));
            query.append(" 	where attach_seq = ?  ");

            pstmt = this.getConnectionManager().createPrepareStatement(query.toString());

            try {
                attach_seq = Long.parseLong(fileMap.get("attach_seq").toString());
            } catch (Exception e) {
                getLogger().error(e);
            }

            cond.setObject(attach_seq);

            pstmt.set(cond.getParameter());

            pstmt.executeUpdate();

            result.put("result", 0);
            result.put("attach_seq", attach_seq);
            result.put("message", "정상 처리 되었습니다.");

        } catch (Exception e) {

            result.put("result", -1);
            result.put("attach_seq", attach_seq);
            result.put("message", e.getMessage());

            getLogger().error("deleteAttachFilesInfo error", e);

            getLogger().error("deleteAttachFilesInfo files=" + fileMap.toString());
            getLogger().error("error next_docu_id = " + attach_seq);
            getLogger().error("error query = " + query.toString());
        }

        return result;

    }

    public RecordSet selectAttachFiles(String p_target_db, String p_target_table, String p_attach_group, String p_target_column, String p_target_id, String p_search_ext) throws SQLException {
        SelectConditionObject cond = new SelectConditionObject();
        StringBuilder query = new StringBuilder();

        query.append("\n/* " + this.getClass().toString() + ".selectAttachFiles() */\n");

        query.append(" select  ");
        query.append("      (case when row_number() over(order by t.attach_seq) = 1 then 'true' else 'false' end)  as \"@first\" ");
        query.append("     , row_number() over(order by t.attach_seq) as \"@i\" ");
        query.append("     , t.attach_seq ");
        query.append("     , '" + p_target_db + "' as target_db ");
        query.append("     , '" + p_target_table + "' as target_table ");
        query.append("     , '" + p_target_column + "' as target_column ");
        query.append("     , t." + p_target_column + "  ");
        query.append("     , (case when t.primary_yn = 'Yes' then 'checked' else ' ' end) as \"primary\" ");
        query.append("     , t.original_name as \"name\" ");
        query.append("     , t.attach_name as \"saveName\" ");
        query.append("     , t.attach_ext as \"ext\" ");
        query.append("     , t.attach_url as \"thumbUrl\"  ");
        query.append("     , t.attach_url as \"download\"  ");
        query.append("     , t.attach_size as \"fileSize\" ");
        query.append("     , t.registrant ");
        query.append("     , t.attach_group ");
        if("logis".equals(p_target_db)) {
            query.append("     , xtendus.f_name( t.registrant ) as user_name ");
        }else{
            query.append("     , f_name( t.registrant ) as user_name ");
        }
        query.append("     , t.registration_date ");
        query.append("     , '" + p_search_ext + "' as search_ext ");
        query.append("  from " + p_target_table + " t ");
        query.append(" where 1=1 ");

        //query.append(" where t." + p_target_column + " =  ? ");
        //cond.setObject(p_target_id);

        query.append(" and t." + p_target_column + " in (0  ");
        String ids[] = p_target_id.split(",");
        for (String id : ids) {
            query.append(",?");
            if(StringUtil.isEmpty(id)) id = "0";
            cond.setObject(Long.parseLong(id));
        }
        query.append(" ) ");

        if (StringUtil.isNotEmpty(p_attach_group)) {
            query.append(" and t.attach_group like  ? ");
            cond.setObject(p_attach_group);
        }

        if ("*".equals(p_search_ext) || StringUtils.isEmpty(p_search_ext)) {
            //
        } else {
            String extentions[] = p_search_ext.split("\\|");

            query.append(" and  t.attach_ext in ( '*' ");
            for (String ext : extentions) {
                query.append(", '" + ext.trim().toLowerCase() + "'");
            }
            query.append(" ) ");

        }

        getLogger().debug(query.toString());

        return select(query.toString(), cond.getParameter());
    }

    public RecordSet selectAllAttachFiles(Box box) throws SQLException {
        SelectConditionObject cond = new SelectConditionObject();
        StringBuilder query = new StringBuilder();

        query.append("\n/* " + this.getClass().toString() + ".selectAllAttachFiles() */\n");
        query.append(" select  ");
        query.append("     v.* ");
        query.append("     , f_attach_target_name(v.reference_table, v.target_id) as target_name ");
        query.append("     , u.user_name as registration_name ");
        query.append("  from vw_all_attachs v ");
        query.append("  left join users u on u.user_id = v.registrant  ");
        query.append(" where 1 = 1 ");

        if (StringUtil.isNotEmpty(box.getString("attach_group"))) {
            query.append(" and v.attach_group like  ? ");
            cond.setObject(box.getString("attach_group"));
        }
        query.append(" order by v.attach_seq desc ");

        return select(query.toString(), cond.getParameter());
    }

    public boolean changePrimaryFile(String p_target_table, String p_attach_group, String p_target_column, String p_target_id, long p_attach_seq) throws SQLException {
        SelectConditionObject cond = new SelectConditionObject();
        StringBuilder query = new StringBuilder();
        SQLPreparedStatement stmt = null;

        query.append("\n/* " + this.getClass().toString() + ".selectAttachFiles() */\n");
        query.append(" update " + p_target_table + "   ");
        query.append("   set primary_yn =  case when attach_seq =  ? then 'Yes' else 'No' end  ");
        cond.setObject(p_attach_seq);
        query.append(" where " + p_target_column + " =  cast(? as NUMERIC) ");
        cond.setObject(p_target_id);
        query.append(" and attach_group =  ? ");
        cond.setObject(p_attach_group);

        getLogger().debug(query.toString());

        stmt = getConnectionManager().createPrepareStatement(query.toString());

        stmt.set(cond.getParameter());
        stmt.executeUpdate();
        return true;

    }

    public HashMap<String, Object> bindingAttach4targetTable(ArrayList<HashMap<String, Object>> jarray, LoginBean loginBean, long target_id) throws SQLException {
        return bindingAttach4targetTable(null, jarray, null, target_id);
    }

    public HashMap<String, Object> bindingAttach4targetTable(HttpSession session, ArrayList<HashMap<String, Object>> jarray, LoginBean loginBean, long target_id) throws SQLException {
        SelectConditionObject cond = null;
        StringBuilder query = null;
        SQLBatchPreparedStatement stmt = null;
        HashMap<String, Object> result = new HashMap<String, Object>();

        if (getLogger().isDebugEnabled()) {
            // 서버로 전달된 수정된 행에 대한 정보를 콘솔 로그로 출력해 본다.
            for (HashMap<String, Object> map : jarray) {
                getLogger().debug("map=" + map);
            }
        }

        for (HashMap<String, Object> map : jarray) {

            cond = new SelectConditionObject();
            if (stmt == null) {
                query = new StringBuilder();
                query.append("\n/* " + this.getClass().toString() + ".bindingAttach4targetTable() */\n");
                query.append(" update " + map.get("target_table") + "   ");
                query.append("   set " + map.get("target_column") + " =  cast(? as NUMERIC) ");

                if ("help_attach".equals(map.get("target_table")) && target_id > 0 && "mrd".equals((map.get("ext") + "").toLowerCase())) {
                    // 온라인 도움말 파일인 경우. mrd파일명을 새로 생성한다.
                    query.append("   , help_name = 'dpms/help_' || trim(?) ||'_' || attach_seq ||  '.' || attach_ext  ");
                }

                query.append(" where attach_seq =  cast(? as NUMERIC) ");
                stmt = getConnectionManager().createBatchPrepareStatement(query.toString());
            }

            if (target_id > 0) {
                /* 각 업무별 Action에서 binding을 위해 호출할때, target id 가 있을 경우 해당 Target으로 연결 시킨다. */
                cond.setObject(target_id);
            } else {
                cond.setObject(map.get("target_id"));
            }

            if ("help_attach".equals(map.get("target_table")) && target_id > 0 && "mrd".equals((map.get("ext") + "").toLowerCase())) {
                cond.setObject(target_id);
            }

            cond.setObject(map.get("attach_seq"));

            stmt.addBatch(cond.getParameter());

            if ("help_attach".equals(map.get("target_table")) && "mrd".equals((map.get("ext") + "").toLowerCase()) && target_id > 0 && StringUtil.isNotEmpty(map.get("thumburl") + "")) {
                String target = "dpms\\help_" + target_id + "_" + map.get("attach_seq") + ".mrd";
                String source = map.get("thumburl") + "";
                copyOnlineHelp(session, source, target);
            }

        }

        getLogger().debug(query.toString());
        stmt.executeBatch();

        // 결과 코드와 메시지를 가져온다.
        result.put("result", 0);
        result.put("message", "succ");

        return result;

    }

    /**
     * MRD (Report Designer 파일)의 경우 ReportingServer의 /mrd 디렉토리로 복사한다.
     *
     * @param session
     * @param source
     * @param target
     */
    private void copyOnlineHelp(HttpSession session, String source, String target) {
        try {
            String report_server = "\\\\100.100.100.1\\www\\ReportingServer\\mrd\\";
            String context_root = session.getServletContext().getRealPath("/");
            getLogger().debug(context_root);

            File sFile = new File(context_root + source);
            getLogger().debug(sFile.getAbsolutePath());

            File tFile = new File(report_server + target);
            getLogger().debug(tFile.getAbsolutePath());

            framework.util.FileUtil.copyFile(sFile, tFile);

        } catch (Exception e) {
            getLogger().error(e);
            e.printStackTrace();
        }

    }

}
