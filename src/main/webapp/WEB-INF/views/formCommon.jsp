<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
  <title><c:choose>
    <c:when test="${not empty post}">글 수정</c:when>
    <c:otherwise>글 작성</c:otherwise>
  </c:choose></title>
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
    input[readonly] {
      background-color: #eee;
      color: #666;
      border: 1px solid #bbb;
      cursor: not-allowed;
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

<h2 style="text-align:center;">
  <c:choose>
    <c:when test="${not empty post}">🛠️ 글 수정</c:when>
    <c:otherwise>✏️ 새 글 작성</c:otherwise>
  </c:choose>
</h2>

<form method="post" action="${pageContext.request.contextPath}/posts/<c:out value='${not empty post ? "update" : "save"}'/>">
  <c:if test="${not empty error}">
    <div class="error">${error}</div>
  </c:if>

  <c:if test="${not empty post}">
    <input type="hidden" name="postId" value="${post.postId}" />
  </c:if>

  <label for="title">제목</label>
  <input type="text" id="title" name="title"
         value="<c:out value='${post.title}'/>"
         required minlength="2" maxlength="200" />

  <label for="writer">작성자</label>
  <input type="text" id="writer" name="writer"
         value="<c:out value='${post.writer}'/>"
         <c:if test="${not empty post}">readonly</c:if>
         required minlength="1" maxlength="50" />

  <label for="content">내용</label>
  <textarea id="content" name="content" rows="6" required minlength="5"><c:out value='${post.content}'/></textarea>

  <label for="passphrase">
    <c:choose>
      <c:when test="${not empty post}">비밀번호(기존 비밀번호)</c:when>
      <c:otherwise>비밀번호</c:otherwise>
    </c:choose>
  </label>
  <input type="password" id="passphrase" name="passphrase" required minlength="4" maxlength="20" />

  <button type="submit">
    <c:choose>
      <c:when test="${not empty post}">수정</c:when>
      <c:otherwise>등록</c:otherwise>
    </c:choose>
  </button>
</form>

<div style="text-align:center;">
  <c:choose>
    <c:when test="${not empty post}">
      <a href="${pageContext.request.contextPath}/posts/detail?id=${post.postId}">🔙 상세로 돌아가기</a>
    </c:when>
    <c:otherwise>
      <a href="${pageContext.request.contextPath}/posts">📋 목록으로 돌아가기</a>
    </c:otherwise>
  </c:choose>
</div>

</body>
</html>
