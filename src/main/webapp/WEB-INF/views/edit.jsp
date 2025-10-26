<%--
  Created by IntelliJ IDEA.
  User: user
  Date: 2025. 10. 26.
  Time: 오후 12:20
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
  <title>글 수정</title>
  <style>
    form {
      width: 500px;
      margin: 30px auto;
      padding: 20px;
      border: 1px solid #ccc;
      background-color: #f9f9f9;
    }
    label {
      display: block;
      margin-top: 10px;
    }
    input[type="text"], input[type="password"], textarea {
      width: 100%;
      padding: 8px;
      margin-top: 5px;
    }
    button {
      margin-top: 15px;
      padding: 10px 20px;
    }
    .error {
      color: red;
      margin-bottom: 10px;
    }
  </style>
</head>
<body>

<h2 style="text-align:center;">🛠️ 글 수정</h2>

<form method="post" action="${pageContext.request.contextPath}/posts/update">
  <c:if test="${not empty error}">
    <div class="error">${error}</div>
  </c:if>

  <input type="hidden" name="postId" value="${post.postId}" />

  <label for="title">제목</label>
  <input type="text" id="title" name="title" value="${post.title}" required minlength="2" maxlength="200" />

  <label for="writer">작성자</label>
  <input type="text" id="writer" name="writer" value="${post.writer}" readonly />

  <label for="content">내용</label>
  <textarea id="content" name="content" rows="6" required minlength="5">${post.content}</textarea>

  <label for="passphrase">비밀번호(기존 비밀번호)</label>
  <input type="password" id="passphrase" name="passphrase" required minlength="4" maxlength="20" />

  <button type="submit">수정</button>
</form>

<div style="text-align:center;">
  <a href="${pageContext.request.contextPath}/posts/detail?id=${post.postId}">🔙 상세로 돌아가기</a>
</div>

</body>
</html>
