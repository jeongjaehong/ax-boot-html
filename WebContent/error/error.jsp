<%@ include file="/common/_page.jsp" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.apache.commons.text.StringEscapeUtils" %>
<%@ page import="java.util.Optional" %>

<%
    request.setCharacterEncoding("UTF-8");

    Optional<String> simpleMsg = Optional.ofNullable((String) request.getAttribute("errorMessage"))
        .or(() -> Optional.ofNullable(request.getParameter("msg")));

    Optional<String> detailMsg = Optional.ofNullable((String) request.getAttribute("errorDetail"))
        .or(() -> Optional.ofNullable(request.getParameter("detail")));

    Throwable exception = (Throwable) request.getAttribute("exception");
    
    if (detailMsg.isEmpty() && exception != null) {
        detailMsg = Optional.ofNullable(exception.getMessage());
    }

    // 기본 에러 메시지 (null 또는 공백인 경우)
    String displayMessage = simpleMsg.filter(msg -> !msg.trim().isEmpty()).orElse("요청 처리 중 에러가 발생했습니다.");
%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>오류</title>
    <style>
        body {
            margin: 0;
            font-family: Arial, "Malgun Gothic", sans-serif;
            background-color: #f6f7f9;
            color: #222;
        }
        .wrap {
            max-width: 720px;
            margin: 60px auto;
            padding: 0 16px;
        }
        .card {
            background: #fff;
            border: 1px solid #e6e8ee;
            border-radius: 8px;
            padding: 22px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
        }
        .title {
            font-size: 20px;
            font-weight: 700;
            margin-bottom: 14px;
        }
        .msg {
            margin-bottom: 14px;
            line-height: 1.6;
        }
        .toggle {
            display: inline-block;
            color: #0b5bd3;
            cursor: pointer;
            text-decoration: underline;
            margin-bottom: 10px;
        }
        .detail {
            display: none;
            background-color: #0f172a;
            color: #e2e8f0;
            border-radius: 8px;
            padding: 12px;
            overflow: auto;
            max-height: 260px;
            font-size: 14px;
            line-height: 1.4;
            white-space: pre-wrap;
            border: 1px solid #111827;
        }
        .btn-row {
            margin-top: 18px;
            text-align: right;
        }
        .btn {
            background-color: #16a34a;
            border: none;
            border-radius: 6px;
            color: #fff;
            font-size: 14px;
            padding: 10px 16px;
            cursor: pointer;
            margin-right: 10px;
        }
        .btn:hover {
            background-color: #15803d;
        }
        .hint {
            margin-top: 12px;
            color: #6b7280;
            font-size: 12px;
        }
        nav {
            display: flex;
            justify-content: space-between;
            margin-top: 20px;
        }
        nav a {
            text-decoration: none;
            color: #0b5bd3;
            font-weight: 600;
        }
        nav a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
<div class="wrap">
    <div class="card">
        <h1 class="title">오류가 발생했습니다.</h1>
        <p class="msg"><c:out value="${errorMessage != null ? errorMessage : displayMessage}" /></p>

        <span id="toggleDetail" class="toggle">상세 보기</span>
        <pre id="detailBox" class="detail">
<%= detailMsg
        .filter(msg -> msg.length() <= 2048)
        .map(StringEscapeUtils::escapeHtml4)
        .orElse("상세 정보가 없습니다.") %>
        </pre>

        <div class="btn-row">
            <button type="button" class="btn" id="okBtn">확인</button>
        </div>
        <div class="hint">확인을 누르면 첫 화면으로 이동합니다.</div>

        <nav>
            <div>
                <a class="btn" href="/"><i class="fa fa-home"></i> 첫 화면으로 이동</a>
                <a class="btn" href="mailto:sis@doore.co.kr"><i class="fa fa-envelope-o"></i> 관리자에게 문의하기</a>
            </div>
        </nav>
    </div>
</div>

<script>
    (function () {
        const toggleDetail = document.getElementById('toggleDetail');
        const detailBox = document.getElementById('detailBox');
        const okBtn = document.getElementById('okBtn');

        // 토글 상세보기
        let isDetailsVisible = false;
        toggleDetail.addEventListener('click', () => {
            isDetailsVisible = !isDetailsVisible;
            detailBox.style.display = isDetailsVisible ? 'block' : 'none';
            toggleDetail.textContent = isDetailsVisible ? '상세 보기 닫기' : '상세 보기';
        });

        // 확인 버튼 클릭 시 홈으로 이동
        okBtn.addEventListener('click', () => {
            window.location.href = '/';
        });
    })();
</script>
</body>
</html>