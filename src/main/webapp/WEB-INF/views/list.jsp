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
	<title>게시글 목록</title>
	<style>
		table {
			width: 80%;
			border-collapse: collapse;
			margin: 20px auto;
		}
		th, td {
			border: 1px solid #ccc;
			padding: 8px;
			text-align: center;
		}
		th {
			background-color: #f2f2f2;
		}
		a {
			text-decoration: none;
			color: #333;
		}
	</style>
</head>
<body>
<h1 style="text-align:center;">📋 게시글 목록</h1>

<table>
	<thead>
	<tr>
		<th>번호</th>
		<th>제목</th>
		<th>작성자</th>
		<th>작성일</th>
	</tr>
	</thead>
	<tbody>
	<c:forEach var="post" items="${dtoList}" varStatus="status">
		<tr>
			<td>${status.index + 1}</td>
			<td>
				<a href="${pageContext.request.contextPath}/posts/detail?id=${post.postId}">
						${post.title}
				</a>
			</td>
			<td>${post.writer}</td>
			<td>${post.createdAt}</td>
		</tr>
	</c:forEach>
	</tbody>
</table>

<div style="text-align:center;">
	<a href="${pageContext.request.contextPath}/posts/new">✏️ 새 글 작성</a>
</div>
</body>
</html>
