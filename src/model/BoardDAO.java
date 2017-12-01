package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class BoardDAO {

	Connection con;
	PreparedStatement pstmt;
	ResultSet rs;
	
	//데이터 베이스의 커넥션풀을 사용하도록 설정하는 메소드
	public void getCon() {
		try {
			Context initctx = new InitialContext();
			Context envctx = (Context)initctx.lookup("java:comp/env");
			DataSource ds = (DataSource)envctx.lookup("jdbc/pool");
			//datasource
			con = ds.getConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//하나의 새로운 게시글이 넘어와서 저장되는 메소드
	public void insertBoard(BoardBean bean) {
		getCon();
		//빈클래스에 넘어오지 않았던 데이터들을 초기화 해주어야한다.
		int ref=0; //글 그룹을 의미 = 쿼리를 실행시켜서 가장 큰 ref값을 가져온 후 +1해줘야함
		int re_step=1; //새글이기에 = 부모글이기에
		int re_level=1;
		
		try {
			//가장 큰 ref값을 읽어오는 쿼리 준비
			String refsql = "SELECT max(ref) FROM BOARD";
			//쿼리실행 객체
			pstmt = con.prepareStatement(refsql);
			//쿼리 실행 후 결과물 리턴
			rs = pstmt.executeQuery();
			if(rs.next()) { //결과값이 있다면
				ref = rs.getInt(1)+1; //최대값에 +1을 더해서 글 그룹을 설정
			}
			//실제로 게시글 전체값을 테이블에 저장
			String sql = "INSERT INTO BOARD VALUES(board_seq.NEXTVAL,?,?,?,?,sysdate,?,?,?,0,?)";
			pstmt = con.prepareStatement(sql);
			//?에 값을 맵핑
			pstmt.setString(1, bean.getWriter());
			pstmt.setString(2, bean.getEmail());
			pstmt.setString(3, bean.getSubject());
			pstmt.setString(4, bean.getPassword());
			pstmt.setInt(5, ref);
			pstmt.setInt(6, re_step);
			pstmt.setInt(7, re_level);
			pstmt.setString(8, bean.getContent());
			//쿼리를 실행하시요
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(rs != null) rs.close();
				if(pstmt != null) pstmt.close();
				if(con != null) con.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
	
	//모든 게시글을 리턴해주는 메소드 작성
	public ArrayList<BoardBean> getAllBoard(int start, int end){
		//리턴할 객체 선언
		ArrayList<BoardBean> al = new ArrayList<>();
		getCon();
		try {
			//쿼리 준비 
			String sql = "SELECT * FROM (SELECT A.*, Rownum Rnum FROM (SELECT * FROM Board ORDER BY ref desc, re_step asc)A)"
					+ "WHERE Rnum >= ? and Rnum <= ?";
			//쿼리를 실행할 객체
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, start);
			pstmt.setInt(2, end);
			//쿼리 실행 후 결과 저장
			rs = pstmt.executeQuery();
			//데이터 개수가 몇개인지 모르기에 반복문을 이용하여 데이터를 추출
			while(rs.next()) {
				//데이터를 패키징(BoardBean클래스를 이용)
				BoardBean bean = new BoardBean();
				bean.setNum(rs.getInt(1));
				bean.setWriter(rs.getString(2));
				bean.setEmail(rs.getString(3));
				bean.setSubject(rs.getString(4));
				bean.setPassword(rs.getString(5));
				bean.setReg_date(rs.getDate(6).toString());
				bean.setRef(rs.getInt(7));
				bean.setRe_step(rs.getInt(8));
				bean.setRe_level(rs.getInt(9));
				bean.setReadcount(rs.getInt(10));
				bean.setContent(rs.getString(11));
				//패키징한 데이터를 리스트에 저장
				al.add(bean);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(rs != null) rs.close();
				if(pstmt != null) pstmt.close();
				if(con != null) con.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return al;
	}
	
	//BoardInfo때 하나의 게시글을 리턴하는 메소드
	public BoardBean getOneBoard(int num) {
		//리턴타입 선언
		BoardBean bean = new BoardBean();
		getCon();
		try {
			//조회수 증가 쿼리
			String readsql = "UPDATE BOARD SET readcount=readcount+1 WHERE num=?";
			pstmt = con.prepareStatement(readsql);
			pstmt.setInt(1, num);
			pstmt.executeUpdate();
			//쿼리준비
			String sql = "SELECT * FROM BOARD WHERE num=?";
			//쿼리 실행객체
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, num);
			//쿼리 실행 후 결과를 리턴
			rs = pstmt.executeQuery();
			if(rs.next()) {
				bean.setNum(rs.getInt(1));
				bean.setWriter(rs.getString(2));
				bean.setEmail(rs.getString(3));
				bean.setSubject(rs.getString(4));
				bean.setPassword(rs.getString(5));
				bean.setReg_date(rs.getDate(6).toString());
				bean.setRef(rs.getInt(7));
				bean.setRe_step(rs.getInt(8));
				bean.setRe_level(rs.getInt(9));
				bean.setReadcount(rs.getInt(10));
				bean.setContent(rs.getString(11));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(rs != null) rs.close();
				if(pstmt != null) pstmt.close();
				if(con != null) con.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return bean;
	}
	
	//답변글이 저장되는 메소드
	public void reWriteBoard(BoardBean bean) {
		//부모글그룹과 글레벨 글스탭을 읽어드림
		int ref = bean.getRef();
		int re_step = bean.getRe_step();
		int re_level = bean.getRe_level();
		
		getCon();
		
		try {
		//********핵심코드*********
			//부모글보다 큰 re_level의 값을 전부 1씩 증가시켜줌
			String levelsql = "UPDATE BOARD SET re_level=re_level+1 WHERE ref=? and re_level > ?";
			//쿼리실행객체 선언
			pstmt = con.prepareStatement(levelsql);
			pstmt.setInt(1, ref);
			pstmt.setInt(2, re_level);
			//쿼리 실행
			pstmt.executeUpdate();
			//답변글 데이터를 저장
			String sql = "INSERT INTO BOARD VALUES(board_seq.NEXTVAL,?,?,?,?,sysdate,?,?,?,0,?)";
			pstmt = con.prepareStatement(sql);
			//?에 값을 대입
			pstmt.setString(1, bean.getWriter());
			pstmt.setString(2, bean.getEmail());
			pstmt.setString(3, bean.getSubject());
			pstmt.setString(4, bean.getPassword());
			pstmt.setInt(5, ref); //부모의 ref값을 넣어줌
			pstmt.setInt(6, re_step+1); //답글이기에 부모글 re_step에 1을 더해줌
			pstmt.setInt(7, re_level+1); 
			pstmt.setString(8, bean.getContent());
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(pstmt != null) pstmt.close();
				if(con != null) con.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
	
	//BoardUpdate, Delete시 하나의 게시글 리턴
	public BoardBean getOneUpdateBoard(int num) {
		//리턴타입 선언
		BoardBean bean = new BoardBean();
		getCon();
		try {
			//쿼리준비
			String sql = "SELECT * FROM BOARD WHERE num=?";
			//쿼리 실행객체
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, num);
			//쿼리 실행 후 결과를 리턴
			rs = pstmt.executeQuery();
			if(rs.next()) {
				bean.setNum(rs.getInt(1));
				bean.setWriter(rs.getString(2));
				bean.setEmail(rs.getString(3));
				bean.setSubject(rs.getString(4));
				bean.setPassword(rs.getString(5));
				bean.setReg_date(rs.getDate(6).toString());
				bean.setRef(rs.getInt(7));
				bean.setRe_step(rs.getInt(8));
				bean.setRe_level(rs.getInt(9));
				bean.setReadcount(rs.getInt(10));
				bean.setContent(rs.getString(11));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(rs != null) rs.close();
				if(pstmt != null) pstmt.close();
				if(con != null) con.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return bean;
	}
	
	//update와 delete시 필요한 패스워스 값을 리턴해주는 메소드
	public String getPass(int num) {
		//리턴할 변수 객체 선언
		String pass = "";
		getCon();
		try {
			String sql = "SELECT password FROM BOARD WHERE num=?";
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, num);
			rs = pstmt.executeQuery();
			//패스워드값을 저장
			if(rs.next()) {
				pass = rs.getString(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(rs != null) rs.close();
				if(pstmt != null) pstmt.close();
				if(con != null) con.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return pass;
	}
	//하나의 게시글을 수정하는 메소드
	public void updateBoard(BoardBean bean) {
		getCon();
		try {
			//쿼리준비
			String sql = "UPDATE BOARD SET subject=?, content=? WHERE num=?";
			pstmt = con.prepareStatement(sql);
			//?값을 대입
			pstmt.setString(1, bean.getSubject());
			pstmt.setString(2, bean.getContent());
			pstmt.setInt(3, bean.getNum());
			
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}  finally {
			try {
				if(pstmt != null) pstmt.close();
				if(con != null) con.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
	//하나의 게시글을 삭제하는 메소드
	public void deleteBoard(int num) {
		getCon();
		try {
			//쿼리준비
			String sql = "DELETE FROM BOARD WHERE num=?";
			pstmt = con.prepareStatement(sql);
			//? 설정
			pstmt.setInt(1, num);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(pstmt != null) pstmt.close();
				if(con != null) con.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
	
	//전체 글의 갯수를 리턴하는 메소드
	public int getAllCount() {
		getCon();
		//게시글 전체수를 저장하는 변수
		int count=0;
		try {
			//쿼리준비
			String sql = "SELECT count(*) FROM BOARD";
			//쿼리를 실행할 객체 선언
			pstmt = con.prepareStatement(sql);
			//쿼리 실행 후 결과 리턴
			rs = pstmt.executeQuery();
			if(rs.next()) {
				count = rs.getInt(1); //전체 게시글 수
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(rs != null) rs.close();
				if(pstmt != null) pstmt.close();
				if(con != null) con.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return count;
	}
}



















