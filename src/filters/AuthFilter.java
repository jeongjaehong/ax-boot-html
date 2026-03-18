package filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * 인증 필터(AuthFilter)에 대한 구현 클래스입니다.
 * 이 필터는 HTTP 요청 경로에 따라 인증 여부를 확인하고,
 * 적절한 액션을 수행하여 접근 권한을 제어합니다.
 */
public class AuthFilter implements Filter {

    private static final Log _logger = LogFactory.getLog(AuthFilter.class);

    private Log getLogger() {
        return _logger;
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // 필요 시 초기화 작업 수행
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            throw new ServletException("ServletRequest 및 ServletResponse는 반드시 HttpServletRequest와 HttpServletResponse 타입이어야 합니다.");
        }

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String ctx = req.getContextPath();
        String path = req.getRequestURI().substring(ctx.length());

        getLogger().debug("접속 요청된 경로: " + path);

        // 로그인 없이 허용할 경로들
        if (isAllowedPath(path)) {
            getLogger().debug("로그인 없이 접속이 허용된 경로: " + path);
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        Object loginBean = (session == null ? null : session.getAttribute("loginBean"));

        if (loginBean == null) {
            getLogger().debug("로그인이 필요합니다: " + path);
            res.sendRedirect(ctx + "/login.jsp");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // 필요 시 정리 작업 수행
    }

    private boolean isAllowedPath(String path) {
        return path.equals("/") ||
               path.equals("/login.jsp") ||
               path.endsWith(".css") ||
               path.endsWith(".js") || 
               path.endsWith(".png") || 
               path.endsWith(".jpg") ||
               path.startsWith("/error/") ||
               path.startsWith("/_assets/") ||
               path.startsWith("/common/") ||
               path.startsWith("/Login.do");
    }
}
