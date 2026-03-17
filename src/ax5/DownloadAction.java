package ax5;

import com.mysql.jdbc.StringUtils;
import framework.action.Action;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.FilenameUtils;
import util.PortalUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;

public class DownloadAction extends Action {

	String SAVE_DIR = "/upload_data/ax5/";
	String SAVE_URL = "/upload_data/ax5/";

	int fileSizeLimit = 50 * 1000 * 1000;
	String encoding = "utf-8";

	public void processInit() {

		getLogger().debug("\nStart DownloadAction ...\n");

		String fileName = getInput().getString("fileName");
		String context_root = getSession().getServletContext().getRealPath("/");
		String fileNameOrg = "";

		try {
			getRequest().setCharacterEncoding("UTF-8");
			fileName = new String(fileName.getBytes("UTF-8"), "UTF-8");

			File file = null;

			if (StringUtils.startsWithIgnoreCase(fileName, "http")) {
				getLogger().debug("fileName=" + fileName);
				getLogger().debug("decode fileName=" + PortalUtil.encoding(fileName));

				// String url = URLEncoder.encode(fileName, "UTF-8");

				this.getResponse().sendRedirect(PortalUtil.encoding(fileName));
				// this.getResponse().sendRedirect(url);
				return;
			} else {
				file = new File(context_root, fileName);
			}

			if (file.isFile()) {
				fileNameOrg = FilenameUtils.getName(file.getName());

				int bytes = (int) file.length();
				String header = getRequest().getHeader("User-Agent");

				if (header.contains("MSIE") || header.contains("Trident")) {
					fileNameOrg = URLEncoder.encode(fileNameOrg, "UTF-8").replaceAll("\\+", "%20");
					getResponse().setHeader("Content-Disposition", "attachment;filename=" + fileNameOrg + ";");
				} else if (header.contains("Chrome")) {
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < fileNameOrg.length(); i++) {
						char c = fileNameOrg.charAt(i);
						if (c > '~') {
							sb.append(URLEncoder.encode("" + c, "UTF-8"));
						} else {
							sb.append(c);
						}
					}

					getResponse().setHeader("Content-Disposition", "attachment; filename=\"" + sb.toString() + "\"");
				} else {
					fileNameOrg = new String(fileNameOrg.getBytes("UTF-8"), "ISO-8859-1");
					getResponse().setHeader("Content-Disposition", "attachment; filename=\"" + fileNameOrg + "\"");
				}

				// 출처: https://aljjabaegi.tistory.com/340 [알짜배기 프로그래머]
				// String mimetype = "application/x-msdownload";
				// getResponse().setContentType(mimetype);

				getResponse().setContentType("application/download; UTF-8");
				getResponse().setContentLength(bytes);
				getResponse().setHeader("Content-Type", "application/octet-stream");
				getResponse().setHeader("Content-Transfer-Encoding", "binary;");
				getResponse().setHeader("Pragma", "no-cache;");
				getResponse().setHeader("Expires", "-1;");

				BufferedInputStream fin = new BufferedInputStream(new FileInputStream(file));
				BufferedOutputStream outs = new BufferedOutputStream(getResponse().getOutputStream());

				byte[] readByte = new byte[4096];
				try {
					while ((bytes = fin.read(readByte)) > 0) {
						outs.write(readByte, 0, bytes);
						outs.flush();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				} finally {
					outs.close();
					fin.close();
				}
			} else {
				getLogger().debug("not found : " + context_root + fileName);
				// PortalUtil.setResult(getResponse(), -1, "File Not Found!!!");

				getResponse().sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "File Not Found!!!");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
