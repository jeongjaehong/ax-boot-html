package filters;

import framework.filter.AccessLogFilter;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Enumeration;

public class MyAccessLogFilter extends AccessLogFilter {
	private static Log _logger = LogFactory.getLog(MyAccessLogFilter.class);

	private Log getLogger() {
		return _logger;
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest httpReq = (HttpServletRequest) req;
		HttpServletResponse httpRes = (HttpServletResponse) res;

		if (getLogger().isDebugEnabled()) {
			getLogger().debug("DebugEnabled is true");
			getLogger().debug("nUserAgent is " + httpReq.getHeader("user-agent"));
			// this.writeHeader(httpReq);
			// this.writeAttribute(httpReq);
			this.writeParameter(httpReq);
		}else{
			getLogger().debug ("DebugEnabled is false");
		}

		//super.doFilter(req, res, chain);
		chain.doFilter(req, res);

	}

	private void writeParameter(HttpServletRequest req) {

		getLogger().debug("===========Parammeter Begin================");

		for (Object obj : req.getParameterMap().keySet()) {
			String key = (String) obj;
			String value = null;
			Object o = req.getParameterValues(key);
			if (o == null) {
				value = "";
			} else {
				int length = Array.getLength(o);
				if (length == 0) {
					value = "";
				} else if (length == 1) {
					Object item = Array.get(o, 0);
					if (item == null) {
						value = "";
					} else {
						value = item.toString();
					}
				} else {
					StringBuilder valueBuf = new StringBuilder();
					valueBuf.append("[");
					for (int j = 0; j < length; j++) {
						Object item = Array.get(o, j);
						if (item != null) {
							valueBuf.append(item.toString());
						}
						if (j < length - 1) {
							valueBuf.append(",");
						}
					}
					valueBuf.append("]");
					value = valueBuf.toString();
				}
			}

			getLogger().debug(key + "=" + value);
		}

	}

	private void writeHeader(HttpServletRequest req) {

		getLogger().debug("===========Header Block================");

		Enumeration<String> header = req.getHeaderNames();
		while (header.hasMoreElements()) {
			String key = (String) header.nextElement();
			String value = null;
			Object o = req.getHeader(key);
			if (o == null) {
				value = "";
			} else {
				value = (String) o;
			}

			getLogger().debug(key + "=" + value);
		}

		getLogger().debug("===========Header ETC Begin================");

		getLogger().debug(req.getMethod());
		getLogger().debug(req.getPathInfo());
		getLogger().debug(req.getPathTranslated());
		getLogger().debug(req.getProtocol());
		getLogger().debug(req.getQueryString());
		getLogger().debug(req.getRemoteUser());
		getLogger().debug(req.toString());

	}

	private void writeAttribute(HttpServletRequest req) {

		getLogger().debug("===========Attribute Begin================");
		Enumeration<String> Attr = req.getAttributeNames();
		while (Attr.hasMoreElements()) {
			String key = (String) Attr.nextElement();
			String value = null;
			Object o = req.getAttribute(key);
			if (o == null) {
				value = "";
			} else {
				int length = Array.getLength(o);
				if (length == 0) {
					value = "";
				} else if (length == 1) {
					Object item = Array.get(o, 0);
					if (item == null) {
						value = "";
					} else {
						value = item.toString();
					}
				} else {
					StringBuilder valueBuf = new StringBuilder();
					valueBuf.append("[");
					for (int j = 0; j < length; j++) {
						Object item = Array.get(o, j);
						if (item != null) {
							valueBuf.append(item.toString());
						}
						if (j < length - 1) {
							valueBuf.append(",");
						}
					}
					valueBuf.append("]");
					value = valueBuf.toString();
				}
			}

			getLogger().debug(key + "=" + value);
		}

	}

}
