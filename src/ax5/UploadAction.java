package ax5;

import com.LoginBean;
import framework.action.Action;
import framework.action.Box;
import framework.db.RecordSet;
import framework.util.StringUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import util.DBLog;
import util.PortalUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;


public class UploadAction extends Action {

    private static final int MAX_WIDTH = 1920;
    private static final int MAX_HEIGHT = 1080;

    // ZIP은 30MB 이하만 허용
    private static final long ZIP_MAX_BYTES = 30L * 1024L * 1024L; // 30MB

    // 허용(화이트리스트) 확장자: 업무용 문서/도면/영상/사진/오피스/캐드/포토샵 원본 등
    private static final Set<String> ALLOWED_EXT = new HashSet<String>(Arrays.asList(
            // documents
            "pdf", "hwp", "hwpx", "txt", "rtf",
            // office
            "doc", "docx", "xls", "xlsx", "ppt", "pptx", "csv",
            // images (svg 제외 권장)
            "jpg", "jpeg", "png", "gif", "bmp", "tif", "tiff", "webp",
            // videos
            "mp4", "mov", "avi", "mkv", "wmv", "m4v", "webm",
            // cad
            "dwg", "dxf", "dwf",
            // adobe
            "psd", "psb"
            // 필요 시: "zip"
    ));

    // 웹에서 실행/렌더링 위험이 큰 확장자(업로드 차단)
    private static final Set<String> BLOCKED_EXT = new HashSet<String>(Arrays.asList(
            "html", "htm", "shtml", "xhtml",
            "js", "mjs",
            "css",
            "svg",
            "xml", "json",
            "jsp", "jspx", "php", "phtml", "asp", "aspx",
            "exe", "dll", "bat", "cmd", "com", "sh",
            "jar", "war", "class"
    ));

    // String SAVE_ROOT = "/Users/nilriri/Downloads/Upload/temp/";
    String SAVE_ROOT = "/upload_data/ax5/";

    int fileSizeLimit = 100 * 1024 * 1024;
    String encoding = "utf-8";

    private UploadDao dao = null;

    private UploadDao getSelect(String service) throws Exception {
        if (dao == null || !dao.getService().equals(service)) {
            dao = new UploadDao(getConnectionManager(service));
            dao.setService(service);
        }
        return dao;
    }

    private static String lowerExt(String fileName) {
        String ext = FilenameUtils.getExtension(fileName);
        if (ext == null) return "";
        return ext.toLowerCase(Locale.ENGLISH).trim();
    }

    private static boolean looksLikeHtmlOrScript(byte[] buf, int len) {
        if (len <= 0) return false;
        String s = new String(buf, 0, len);
        String l = s.toLowerCase(Locale.ENGLISH);
        return l.contains("<html")
                || l.contains("<script")
                || l.contains("<iframe")
                || l.contains("<svg")
                || l.contains("javascript:")
                || l.contains("<meta")
                || l.contains("<body");
    }

    private static void deleteQuietly(File f) {
        try {
            if (f != null && f.exists()) f.delete();
        } catch (Exception ignore) {
        }
    }

    private static void assertSafeUpload(File savedFile, String originalName, String contentType) throws Exception {
        String ext = lowerExt(originalName);

        // 0) 허용 확장자만 통과(화이트리스트)
        if (ext.length() == 0 || !ALLOWED_EXT.contains(ext)) {
            throw new Exception("허용되지 않은 파일 확장자입니다. (" + ext + ")");
        }

        // 파일은 30MB 초과 업로드 금지
        //if ("zip".equals(ext)) {
        //    long size = (savedFile != null ? savedFile.length() : 0);
        //    if (size > ZIP_MAX_BYTES) {
        //        throw new Exception("파일은 30MB를 초과하여 업로드할 수 없습니다.");
        //    }
        //}

        // 1) 확장자 기반 1차 차단
        if (BLOCKED_EXT.contains(ext)) {
            throw new Exception("보안상 업로드할 수 없는 파일 형식입니다. (" + ext + ")");
        }

        // 2) Content-Type이 명확히 HTML/SVG/JS 계열이면 차단(클라이언트가 속일 수 있으나 2차 힌트로 유효)
        if (contentType != null) {
            String ct = contentType.toLowerCase(Locale.ENGLISH);
            if (ct.contains("text/html") || ct.contains("image/svg") || ct.contains("application/javascript") || ct.contains("text/javascript")) {
                throw new Exception("보안상 업로드할 수 없는 파일 형식입니다. (" + contentType + ")");
            }
        }

        // 3) 파일 헤더 일부를 읽어 HTML/SVG/Script 흔적이 있으면 차단(위장 업로드 방지용)
        InputStream in = null;
        try {
            in = new FileInputStream(savedFile);
            byte[] head = new byte[8192];
            int n = in.read(head);
            if (looksLikeHtmlOrScript(head, n)) {
                throw new Exception("보안상 업로드할 수 없는 파일 내용입니다.");
            }
        } finally {
            try {
                if (in != null) in.close();
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * 중복된 파일명을 확인하고 새로운 고유 파일명을 생성합니다.
     * 예: example.jpg -> example(1).jpg -> example(2).jpg
     */
    private String generateUniqueFileName(File uploadDir, String originalFileName) {
        File file = new File(uploadDir, originalFileName);
        
        // 파일이 존재하지 않으면 원본 파일명 반환
        if (!file.exists()) {
            getLogger().debug("파일명이 중복되지 않음: " + originalFileName);
            return originalFileName;
        }

        // 파일명 분해 (기본명.확장자)
        String baseName = FilenameUtils.getBaseName(originalFileName);
        String extension = FilenameUtils.getExtension(originalFileName);

        // 새로운 파일명 생성
        int count = 1;
        String newFileName;
        while (true) {
            if (extension != null && !extension.isEmpty()) {
                newFileName = baseName + "(" + count + ")." + extension;
            } else {
                newFileName = baseName + "(" + count + ")";
            }
            
            file = new File(uploadDir, newFileName);
            if (!file.exists()) {
                getLogger().debug("중복 파일명 감지 - 새로운 파일명 생성: " + originalFileName + " -> " + newFileName);
                return newFileName;
            }
            count++;
        }
    }

    public void processInit() {
        getLogger().debug("\nStart Ax5Upload Uploading...\n");

        HashMap<String, Object> map = new HashMap<String, Object>();
        try {
            LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");
            String request_phone = (String) getSessionAttribute("voc_request_phone");
            String user_id = "";

            if (null == loginBean && StringUtil.isEmpty(request_phone)) {
                user_id = PortalUtil.getClientIp(this.getRequest());
            } else if (null == loginBean && StringUtil.isNotEmpty(request_phone)) {
                user_id = request_phone;
            } else if (null != loginBean) {
                user_id = loginBean.getUserId();
            } else {
                user_id = "guest";
            }

            String context_root = getSession().getServletContext().getRealPath("/");
            String save_path = SAVE_ROOT + new SimpleDateFormat("yyyy-MM").format(new Date()) + "/";

            // 디렉토리 생성
            File uploadDir = new File(context_root + save_path);
            if (!uploadDir.exists() && !uploadDir.mkdirs()) {
                throw new IOException("Upload directory could not be created: " + uploadDir.getAbsolutePath());
            }

            // multipart/form-data 인지 확인
            if (!JakartaServletFileUpload.isMultipartContent(this.getRequest())) {
                throw new Exception("Form must be multipart");
            }

            long fileSize = 0;
            String saveFileName = null;
            String originalFileName = null;

            getLogger().debug("context_root=" + context_root);

            try {
                long uploadsize = Long.parseLong(getRequest().getHeader("content-length"));
                if (uploadsize > fileSizeLimit) {
                    throw new Exception("업로드 파일의 크기가 너무 커서 업로드할 수 없습니다.");
                }
            } catch (Exception e) {
                throw new Exception("업로드 파일의 사이즈를 확인할 수 없습니다.");
            }

            // Jakarta 버전 빌더 사용
            DiskFileItemFactory factory = DiskFileItemFactory.builder()
                    .setPath(uploadDir.toPath())
                    .get();

            JakartaServletFileUpload upload = new JakartaServletFileUpload(factory);
            upload.setHeaderCharset(StandardCharsets.UTF_8);
            upload.setSizeMax(fileSizeLimit);

            // multipart 데이터 파싱
            List<FileItem> formItems = upload.parseRequest(this.getRequest());

            String target_db = "default";
            String target_table = null;
            String attach_group = null;
            String target_column = null;
            long new_key = 0;

            for (FileItem item : formItems) {
                if (item.isFormField()) {
                    // form-field 처리
                    String fieldName = item.getFieldName();
                    String fieldValue = item.getString();

                    getLogger().debug("fieldName=" + fieldName + ", fieldValue=" + fieldValue);

                    if ("target_db".equals(fieldName)) {
                        target_db = fieldValue;
                    } else if ("target_table".equals(fieldName)) {
                        target_table = fieldValue;
                    } else if ("attach_group".equals(fieldName)) {
                        attach_group = fieldValue;
                    } else if ("target_column".equals(fieldName)) {
                        target_column = fieldValue;
                    } else if (target_column != null && target_column.equals(fieldName)) {
                        new_key = StringUtil.toLong(fieldValue);
                    }
                } else {
                    // 파일 처리
                    originalFileName = item.getName();
                    fileSize = item.getSize();
                    
                    if (fileSize > 0) {
                        // 중복 파일명 처리
                        saveFileName = generateUniqueFileName(uploadDir, FilenameUtils.getName(originalFileName));
                        getLogger().debug("최종 저장 파일명: " + saveFileName);

                        String filePath = context_root + save_path + saveFileName;
                        File storeFile = new File(filePath);
                        item.write(storeFile.toPath()); // 파일 저장

                        // 업로드 파일 보안 검사 (웹 실행 가능 파일 차단)
                        try {
                            String contentType = null;
                            try {
                                contentType = item.getContentType();
                            } catch (Exception ignore) {
                            }
                            assertSafeUpload(storeFile, saveFileName, contentType);
                        } catch (Exception ex) {
                            deleteQuietly(storeFile);
                            throw ex;
                        }

                        resizeImage(storeFile); // 이미지 리사이즈 처리

                        getLogger().debug("파일 저장 완료: " + saveFileName + ", 크기: " + fileSize);

                    } else {
                        throw new Exception("-ERR: File Size 0");
                    }
                }
            }

            getLogger().debug("Form in target table=" + target_table + ", column=" + target_column);

            map.put("name", saveFileName);
            map.put("savename", saveFileName);
            map.put("ext", FilenameUtils.getExtension(saveFileName));
            map.put("filesize", fileSize);
            map.put("download", save_path + saveFileName);
            map.put("thumburl", save_path + saveFileName);

            map.put("target_db", target_db);
            map.put("target_table", target_table);
            map.put("attach_group", attach_group);
            map.put("target_column", target_column);
            map.put(target_column, new_key);

            getLogger().debug(map);

            int attach_seq = applyDB(map, user_id);

            if (attach_seq > 0) {
                map.put("attach_seq", attach_seq);
                PortalUtil.setResult(this.getResponse(), map, false);
            } else {
                PortalUtil.setResult(this.getResponse(), -1, "첨부파일을 업로드 할 수 없습니다.");
            }

        } catch (Exception e) {
            getLogger().error(e);
            PortalUtil.sendError(this.getResponse(), e.getMessage() + "\n" + e.toString());
            DBLog.errorLog(getRequest(), getConnectionManager("default"), e, this);
            e.printStackTrace();
        }
    }

    public void processSaveScreenShot() throws IOException {
        String binaryData = this.getInput().getRawString("image");
        String saveFileName = this.getInput().getString("file_name");
        String masterFileName = this.getInput().getString("master_file_name");
        FileOutputStream stream = null;
        try {

            LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");
            if (null == loginBean) {
                throw new Exception("세션이 만료되었습니다. \n다시 로그인 후 시도 하십시오.\n");
            }

            String context_root = getSession().getServletContext().getRealPath("/");
            String save_path = SAVE_ROOT + "screen_shot/";

            String targetDir = context_root;
            for (String s : save_path.split("/")) {
                targetDir += "/" + s;

                File targetDirectory = new File(targetDir);
                if (!targetDirectory.exists()) {
                    targetDirectory.mkdir();
                }
            }

            System.out.println("binary file   " + binaryData);
            if (binaryData == null || binaryData.trim().equals("")) {
                throw new Exception();
            }

            binaryData = binaryData.replaceAll("data:image/jpeg;base64,", "");
            byte[] file = Base64.decodeBase64(binaryData.getBytes());

            // 중복 파일명 처리
            File uploadDirFile = new File(targetDir);
            saveFileName = generateUniqueFileName(uploadDirFile, saveFileName);

            File target = new File(context_root + save_path + saveFileName);
            stream = new FileOutputStream(target);
            stream.write(file);
            stream.close();

            // 기준년월을 제외한 이름으로 대표 이미지를 생성한다.
            try {
                if (StringUtil.isNotEmpty(masterFileName)) {
                    File src = new File(context_root + save_path + saveFileName);
                    resizeImage(src);
                    framework.util.FileUtil.copyFile(src, new File(context_root + save_path + masterFileName));
                }
            } catch (Exception e) {
                e.printStackTrace();
                getLogger().error(e);
            }
            getLogger().debug("\n캡처 저장 - " + context_root + save_path + saveFileName);

            PortalUtil.setResult(this.getResponse(), 0, "Success.");
        } catch (Exception e) {
            getLogger().error(e);
            PortalUtil.setResult(this.getResponse(), -1, e.getMessage());
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    public int applyDB(HashMap<String, Object> fileMap, String user_id) throws Exception {
        HashMap<String, Object> map = new HashMap<String, Object>();

        String host = "http://" + getRequest().getHeader("host");
        String target_db = "default";
        if (fileMap.containsKey("target_db") && StringUtil.isNotEmpty(fileMap.get("target_db").toString())) {
            target_db = fileMap.get("target_db") + "";
        }
        map = getSelect(target_db).insertAttachFiles(fileMap, user_id, this.getSession(), host);

        if (0 != (Integer) map.get("result")) {
            getLogger().debug("Rollback=" + map);
            this.getConnectionManager(target_db).rollback();
            throw new Exception(map.toString());
        }

        this.getConnectionManager(target_db).commit();
        return Integer.parseInt(map.get("attach_seq").toString());
    }

    public void processChangePrimary() {
        String target_db = "default";
        try {
            DBLog.actionLog(this.getRequest(), getConnectionManager("default"), this);

            LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");
            String voc_request_phone = (String) getSessionAttribute("voc_request_phone");

            if (getInput().containsKey("target_db")) {
                target_db = getInput().get("target_db") + "";
            }
            String target_table = getInput().getString("target_table");
            String attach_group = getInput().getString("attach_group");
            String target_column = getInput().getString("target_column");

            String target_id = getInput().getString(target_column);
            long attach_seq = getInput().getLong("attach_seq");
            String search_ext = getInput().getString("search_ext");

            if (loginBean == null && StringUtil.isEmpty(voc_request_phone)) {
                PortalUtil.setResult(this.getResponse(), -99, "로그인 정보를 찾을 수 없습니다.");
            } else {

                if (getSelect(target_db).changePrimaryFile(target_table, attach_group, target_column, target_id, attach_seq)) {
                    this.getConnectionManager(target_db).commit();
                    RecordSet rs = getSelect(target_db).selectAttachFiles(target_db, target_table, attach_group, target_column, target_id, search_ext);

                    if (rs != null && rs.nextRow()) {
                        PortalUtil.setResult(this.getResponse(), rs);
                    } else {
                        PortalUtil.setResult(this.getResponse(), -9, "Primary 파일 적용중 오류가 발생하였습니다.");
                    }
                } else {
                    this.getConnectionManager(target_db).rollback();
                }
            }

        } catch (Exception e) {
            this.getConnectionManager(target_db).rollback();
            getLogger().error("processLoadAttachFiles error", e);
            PortalUtil.setResult(this.getResponse(), e.getMessage());
        }
    }

    /**
     * 첨부된 파일 목록을 조회한다.
     */
    public void processLoadAttachFiles() {
        try {
            DBLog.actionLog(this.getRequest(), getConnectionManager("default"), this);

            LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");
            String voc_request_phone = (String) getSessionAttribute("voc_request_phone");

            String target_db = "default";
            if (getInput().containsKey("target_db")) {
                target_db = getInput().get("target_db") + "";
            }
            String target_table = getInput().getString("target_table");
            String attach_group = getInput().getString("attach_group");
            String target_column = getInput().getString("target_column");
            String target_id = getInput().getString(target_column);
            // String search_ext = java.net.URLDecoder.decode(
            // getInput().getString("search_ext"), "UTF-8");
            String search_ext = getInput().getString("search_ext");

            if (loginBean == null && StringUtil.isEmpty(voc_request_phone)) {
                PortalUtil.setResult(this.getResponse(), -99, "로그인 정보를 찾을 수 없습니다. - LoadAttachFiles()");
            } else {

                RecordSet rs = getSelect(target_db).selectAttachFiles(target_db, target_table, attach_group, target_column, target_id, search_ext);

                if (rs != null && rs.nextRow()) {
                    PortalUtil.setResult(this.getResponse(), rs);
                } else {
                    PortalUtil.setResult(this.getResponse(), -9, "첨부파일이 존재하지 않습니다.");
                }
            }

        } catch (Exception e) {
            getLogger().error("processLoadAttachFiles error", e);
            PortalUtil.setResult(this.getResponse(), e.getMessage());
        }
    }

    public void processLoadAllAttachFiles() {
        try {
            DBLog.actionLog(this.getRequest(), getConnectionManager("default"), this);

            LoginBean loginBean = (LoginBean) getSessionAttribute("loginBean");
            String voc_request_phone = (String) getSessionAttribute("voc_request_phone");

            Box box = getInput();

            if (loginBean == null && StringUtil.isEmpty(voc_request_phone)) {
                PortalUtil.setResult(this.getResponse(), -99, "로그인 정보를 찾을 수 없습니다.");
            } else {

                String target_db = "default";
                if (box.containsKey("target_db")) {
                    target_db = box.get("target_db") + "";
                }
                RecordSet rs = getSelect(target_db).selectAllAttachFiles(box);

                if (rs != null && rs.nextRow()) {
                    PortalUtil.setResult(this.getResponse(), rs);
                } else {
                    PortalUtil.setResult(this.getResponse(), -9, "첨부파일이 존재하지 않습니다.");
                }
            }

        } catch (Exception e) {
            getLogger().error("processLoadAttachFiles error", e);
            PortalUtil.setResult(this.getResponse(), e.getMessage());
        }
    }


    private void resizeImage(final File fileEntry) throws IOException {
        try {
            BufferedImage originalImage = ImageIO.read(new File(fileEntry.getPath()));
            if (originalImage == null) {
                getLogger().error("Not a Image " + fileEntry.getPath());
                return;
            }
            int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();

            int height = originalImage.getHeight();
            int width = originalImage.getWidth();

            if (height > MAX_HEIGHT || width > MAX_WIDTH) {
                BufferedImage resizeImageHintJpg = resizeImageWithHint(originalImage, type);
                ImageIO.write(resizeImageHintJpg, "jpg", new File(fileEntry.getPath()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BufferedImage resizeImageWithHint(BufferedImage originalImage, int type) {
        int height = originalImage.getHeight();
        int width = originalImage.getWidth();

        getLogger().debug("Resize : " + width + " x " + height + " to " + (width * MAX_HEIGHT) / height + " x " + MAX_HEIGHT);

        BufferedImage resizedImage = new BufferedImage((width * MAX_HEIGHT) / height, MAX_HEIGHT, type);

        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, (width * MAX_HEIGHT) / height, MAX_HEIGHT, null);
        g.dispose();
        g.setComposite(AlphaComposite.Src);

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        return resizedImage;
    }


}
