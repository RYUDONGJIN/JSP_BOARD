<%@page import="model.BoardDAO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<% request.setCharacterEncoding("UTF-8"); %>
<!-- 사용자데이터를 읽어드리는 빈클래스 설정 -->
<jsp:useBean id="boardBean" class="model.BoardBean">
	<jsp:setProperty name="boardBean" property="*"/>
</jsp:useBean>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	<%
		//데이터 베이스에 연결
		BoardDAO bdao = new BoardDAO();
		//해당 게시글의 패스워드값을 얻어옴
		String pass = bdao.getPass(boardBean.getNum());
		//기존 패스워드 값과 update시 작성했던 password값이 같은지 비요
		if(pass.equals(boardBean.getPassword())){
			//데이터 수정하는 메소드 호출
			bdao.updateBoard(boardBean);
			//수정이 완료되면 전체 게시글 보기
			response.sendRedirect("BoardList.jsp");
		} else { //패스워드가 틀리다면
	%>
			<script type="text/javascript">
				alert('패스워드가 일치하지 않습니다. 다시 확인 후 수정해주세요');
				history.go(-1);
			</script>
	<%
		}
	%>
</body>
</html>