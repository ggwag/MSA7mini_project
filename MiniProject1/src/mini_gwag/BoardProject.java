package mini_gwag;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class BoardProject {
	public static void main(String[] args) {
		Connection conn = null;
		Member member = null;
		try {
			//JDBC Driver 등록
		Class.forName("oracle.jdbc.OracleDriver");
		
		//연결하기
		conn = DriverManager.getConnection(
			"jdbc:oracle:thin:@localhost:1521/xe", 
			"user01", 
			"1004"
			);	
		
		member = new Member(conn);
		member.memberMenu();
		if (member.isLoggedIn()) {
			//로그인 성공시 게시판메뉴 표시
			Board board = new Board(conn, member);
			board.mainMenu();
		} else {
			System.out.println("잘못된 접근입니다.");
		}
		 // 연결 끊기
		 } catch (ClassNotFoundException e) {
		     e.printStackTrace();
		 } catch (SQLException e) {
		     e.printStackTrace();
		 } finally {
		     // Connection 객체 종료
		     if (conn != null) {
		         try {
		             conn.close();
		         } catch (SQLException e) {
		             e.printStackTrace();
		         }
		     }
		 }
	}
}
