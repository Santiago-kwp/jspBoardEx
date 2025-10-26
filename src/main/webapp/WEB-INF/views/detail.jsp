<%--
  Created by IntelliJ IDEA.
  User: a
  Date: 2025-10-23
  Time: 오후 7:33
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
	<title>게시글 상세</title>
	<style>
		.container {
			width: 600px;
			margin: 30px auto;
			padding: 20px;
			border: 1px solid #ccc;
			background-color: #f9f9f9;
		}
		.field {
			margin-bottom: 15px;
		}
		.label {
			font-weight: bold;
		}
		.buttons {
			text-align: center;
			margin-top: 20px;
		}
		.buttons button {
			margin: 0 10px;
			padding: 8px 16px;
			background-color: #eee;
			border: 1px solid #ccc;
			color: #333;
			cursor: pointer;
			font-size: 14px;
		}
		.buttons button:hover {
			background-color: #ddd;
		}
		.error {
			color: red;
			margin-bottom: 10px;
			text-align: center;
		}
	</style>
	<script>
		function confirmDelete() {
			const pass = prompt("게시글을 삭제하려면 비밀번호를 입력하세요:");
			if (pass !== null && pass.trim().length > 0) {
				document.getElementById("deletePassphrase").value = pass;
				document.getElementById("deleteForm").submit();
			}
		}
	</script>
</head>
<body>

<div class="container">
	<h2>📄 게시글 상세</h2>
	<c:if test="${not empty error}">
		<div class="error">${error}</div>
	</c:if>

	<div class="field">
		<span class="label">제목:</span> ${post.title}
	</div>

	<div class="field">
		<span class="label">작성자:</span> ${post.writer}
	</div>

	<div class="field">
		<span class="label">작성일:</span> ${post.createdAt}
	</div>

	<div class="field">
		<span class="label">내용:</span><br/>
		<pre>${post.content}</pre>
	</div>

	<div class="buttons">
		<form method="get" action="${pageContext.request.contextPath}/posts/edit" style="display:inline;">
			<input type="hidden" name="id" value="${post.postId}" />
			<button type="submit">✏️ 수정</button>
		</form>

		<form id="deleteForm" method="post" action="${pageContext.request.contextPath}/posts/delete" style="display:inline;">
			<input type="hidden" name="id" value="${post.postId}" />
			<input type="hidden" name="passphrase" id="deletePassphrase" />
			<button type="button" onclick="confirmDelete()">🗑️ 삭제</button>
		</form>

		<form method="get" action="${pageContext.request.contextPath}/posts" style="display:inline;">
			<button type="submit">📋 목록</button>
		</form>
	</div>
</div>


</body>
</html>
