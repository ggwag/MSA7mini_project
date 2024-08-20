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
		//멤버 객체 생성 및 메뉴 표시하기
		member = new Member(conn);
		member.memberMenu();
		// 연결 끊기
		} catch (ClassNotFoundException e) {
		    e.printStackTrace();
		} catch (SQLException e) {
		    e.printStackTrace();
		} 
	}
}
