<%@page import="model.BoardBean"%>
<%@page import="model.BoardDAO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<% 	
	//해당 게시글 번호를 통하여 게시글을 수정
	int num = Integer.parseInt(request.getParameter("num").trim());
	//하나의 게시글에 대한 정보를 리턴
	BoardDAO dao = new BoardDAO();
	BoardBean bean  = dao.getOneUpdateBoard(num);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	<center>
	<h2>게시글 수정</h2>
	<form action="BoardUpdateProc.jsp" method="post">
		<table width="600" border="1" bgcolor="#7FE5FA">
			<tr height="40">
				<td align="center" width="120">작성자</td>
				<td align="center" width="180"><%= bean.getWriter() %></td>
				<td align="center" width="120">작성일</td>
				<td align="center" width="180"><%= bean.getReg_date() %></td>
			</tr>
			<tr height="40">
				<td align="center" width="120">제목</td>
				<td align="center" colspan="3">
					<input type="text" name="subject" size="60" value="<%= bean.getSubject() %>">
				</td>
			</tr>
			<tr height="40">
				<td align="center" width="120">비밀번호</td>
				<td align="center" colspan="3"><input type="password" name="password" size="60"></td>
			</tr>
			<tr height="40">
				<td align="center" width="120">글내용</td>
				<td width="480" colspan="3">
					<textarea rows="10" cols="65" name="content" align="left"><%=bean.getContent() %></textarea>
				</td>
			</tr>
			<tr height="40">
				<td colspan="4" align="center">
					<input type="hidden" name="num" value="<%=bean.getNum() %>">
					<input type="submit" value="글 수정"> &nbsp;&nbsp;
					<input type="button" onclick="location.href='BoardList.jsp'" value="전체 글보기">
				</td>
			</tr>
		</table>
	</form>
	</center>
</body>
</html>





