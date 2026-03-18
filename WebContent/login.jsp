<%@ include file="/common/_page.jsp" %>
<%
    if (session.getAttribute("loginBean") != null) {
        response.sendRedirect(request.getContextPath() + "/main.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="ko">

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
    <meta name="viewport" content="width=device-width, user-scalable=no, initial-scale=1, maximum-scale=1, minimum-scale=1" />
    <meta name="apple-mobile-web-app-capable" content="yes">
    <title>AXBoot :: Advanced Web Application Development Framework</title>
    <link rel="shortcut icon" href="<%=request.getContextPath()%>/assets/favicon.ico" type="image/x-icon" />
    <link rel="icon" href="<%=request.getContextPath()%>/assets/favicon.ico" type="image/x-icon" />
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/assets/css/axboot.css" />
    <!--[if lt IE 10]>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/assets/css/axboot-01.css"/>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/assets/css/axboot-02.css"/>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/assets/css/axboot-03.css"/>
    <![endif]-->
    <script type="text/javascript">
        var CONTEXT_PATH = "<%=request.getContextPath()%>";
        //todo: SCRIPT_SESSION 을 받아와야함
        var SCRIPT_SESSION = (function(json) {
            return json;
        })({
            "userCd": null,
            "userNm": null,
            "locale": null,
            "timeZone": null,
            "dateFormat": null,
            "login": false,
            "details": {},
            "dateTimeFormat": "null null",
            "timeFormat": null
        });

    </script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/js/plugins.min.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/js/axboot/dist/axboot.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/axboot.config.js"></script>
    <style>
        .ax-body.login {
            background: url(<%=request.getContextPath()%>/assets/images/login-bg.jpg) center center;
            background-size: cover;
            color: #ccc;
        }
    </style>
    <script>
        /*
         todo:
         로그인 여부 파악 후 분기
         requireSession 값 확인
         */

        axboot.requireSession('a_x_b_a_a_t_k');


    </script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/js/axboot/dist/good-words.js"></script>
</head>

<body class="ax-body login">
<table style="width:100%;height:100%;">
    <tr>
        <td align="center" valign="middle">
            <div>
                <img src="<%=request.getContextPath()%>/assets/images/login-logo.png" class="img-logo" />
            </div>
            <div class="panel">
                <div class="panel-heading">Input your ID and Password</div>
                <div class="panel-body">
                    <form id="login-form" name="login-form" class="" method="post" action="<%=request.getContextPath()%>/Login.do?action=Login" autocomplete="off">
                        <input type="hidden" name="action" value="Login" />
                        <div class="form-group">
                            <label for="userCd"><i class="cqc-new-message"></i> ID</label>
                            <input type="text" id="userCd" name="user_id" class="form-control ime-false" />
                        </div>
                        <div class="form-group">
                            <label for="userPs"><i class="cqc-key"></i> Password</label>
                            <input type="password" id="userPs" name="password" class="form-control ime-false" />
                        </div>
                        <div class="form-group" style="margin-top: 6px;">
                            <label style="font-weight: normal;">
                                <input type="checkbox" id="rememberId" name="rememberId" />아이디 저장
                            </label>
                        </div>
                        <input type="hidden" name="" value="" />
                        <div class="ax-padding-box" style="text-align: right;">
                            <button type="submit" class="btn">&nbsp;&nbsp;Login&nbsp;&nbsp;</button>
                        </div>
                    </form>
                </div>
                <ul class="list-group">
                    <li class="list-group-item">
                        <a href="#">Find ID</a> &nbsp; &nbsp;
                        <a href="#">Find Password</a>
                    </li>
                </ul>
            </div>
            <div class="txt-copyrights">
                AXBOOT 2.0.0 - Web Application Framework © 2010-2016
            </div>
            <div class="txt-good-words" id="good_words">
            </div>
        </td>
    </tr>
</table>
<script type="text/javascript">
    var fnObj = {
        pageStart: function() {
            try {
                if (typeof goodWords !== "undefined") {
                    $("#good_words").html(goodWords.get());
                }
            } catch (e) {
                // ignore
            }
            var savedId = null;
            if (typeof ax5 !== "undefined" && ax5.util) {
                savedId = ax5.util.getCookie("saved_userid");
            }
            if (savedId && String(savedId).trim() !== "") {
                $("#userCd").val(savedId);
                $("#rememberId").prop("checked", true);
                $("#userPs").focus();
            }
        },
        login: function() {
            if ($("#rememberId").is(":checked")) {
                if (typeof ax5 !== "undefined" && ax5.util) {
                    ax5.util.setCookie("saved_userid", $("#userCd").val(), 365, { path: "/" });
                }
            } else {
                if (typeof ax5 !== "undefined" && ax5.util) {
                    ax5.util.setCookie("saved_userid", "", -1, { path: "/" });
                }
            }
            var handleLoginResponse = function(res) {
                if (res && res.error) {
                    if (res.error.message == "Unauthorized") {
                        alert("로그인에 실패 하였습니다. 계정정보를 확인하세요");
                    } else {
                        alert(res.error.message);
                    }
                    return;
                } else {
                    if (res && res.redirectUrl) {
                        location.href = res.redirectUrl;
                    } else {
                        location.reload();
                    }
                }
            };
            axboot.ajax({
                type: "POST",
                url: "<%=request.getContextPath()%>/Login.do?action=Login",
                data: $("#login-form").serialize(),
                dataType: "json",
                callback: function(res) {
                    handleLoginResponse(res);
                },
                options: {
                    nomask: false,
                    apiType: "login",
                    contentType: "application/x-www-form-urlencoded; charset=UTF-8",
                    onError: function(err) {
                        handleLoginResponse({ error: err });
                    }
                }
            });
            return false;
        }
    };
    $(function() {
        if (fnObj && fnObj.pageStart) {
            fnObj.pageStart();
        }
        if ($("#login-form").length) {
            $("#login-form").on("submit", function(e) {
                e.preventDefault();
                return fnObj.login();
            });
        }
    });
</script>
</body>

</html>
