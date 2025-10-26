<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
	<title>글 작성</title>
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

<h2 style="text-align:center;">✏️ 새 글 작성</h2>

<form method="post" action="${pageContext.request.contextPath}/posts/save">
	<c:if test="${not empty error}">
		<div class="error">${error}</div>
	</c:if>

	<label for="title">제목</label>
	<input type="text" id="title" name="title" required minlength="2" maxlength="200" />

	<label for="writer">작성자</label>
	<input type="text" id="writer" name="writer" required minlength="1" maxlength="50" />

	<label for="content">내용</label>
	<textarea id="content" name="content" rows="6" required minlength="5"></textarea>

	<label for="passphrase">비밀번호</label>
	<input type="password" id="passphrase" name="passphrase" required minlength="4" maxlength="20" />

	<button type="submit">등록</button>
</form>

<div style="text-align:center;">
	<a href="${pageContext.request.contextPath}/posts">📋 목록으로 돌아가기</a>
</div>

</body>
</html>
