package mini_gwag;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class BoardProject {
	//필드
//	private Scanner scanner = new Scanner(System.in);
//	private Connection conn = null;
//	private Member member;
//	//생성자
//	public BoardProject(Connection conn, Member member) {
//		this.conn = conn;
//        this.member = member;
//	}
	
//	//Method	
//	public void list() {
//		//타이틀 및 컬럼명 출력
//		System.out.println();
//		System.out.println("[게시물 목록]");
//		System.out.println("-----------------------------------------------------------------------");
//		System.out.printf("%-6s%-12s%-16s%-40s\n", "게시물번호", "작성자", "날짜", "제목");
//		System.out.println("-----------------------------------------------------------------------");
//		
//		//boads 테이블에서 게시물 정보를 가져와서 출력하기
//		try {
//			String sql = "" +
//				"SELECT bno, btitle, bcontent, bwriter, bdate " +
//				"FROM boardtable " + 
//				"ORDER BY bno DESC";
//			PreparedStatement pstmt = conn.prepareStatement(sql);
//			ResultSet rs = pstmt.executeQuery();
//			while(rs.next()) {		
//				Board board = new Board();
//				board.setBno(rs.getInt("bno"));
//				board.setBtitle(rs.getString("btitle"));
//				board.setBcontent(rs.getString("bcontent"));
//				board.setBwriter(rs.getString("bwriter"));
//				board.setBdate(rs.getDate("bdate"));
//				System.out.printf("%-6s%-12s%-16s%-40s \n", 
//						board.getBno(), 
//						board.getBwriter(),
//						board.getBdate(),
//						board.getBtitle());
//			}
//			rs.close();
//			pstmt.close();
//		} catch(SQLException e) {
//			e.printStackTrace();
//			exit();
//		}
//		
//		//메인 메뉴 출력
//		mainMenu();
//	}
//	
//	public void mainMenu() {
//		System.out.println();
//		System.out.println("-----------------------------------------------------------------------");
//		System.out.println("메뉴: 1.게시물 생성 |  2.게시물 보기  |  3.게시물 삭제 |  4.종료   | 5.로그아웃  |  6.개인정보수정   |   7.회원탈퇴  ");
//		System.out.print("메뉴선택: ");
//		String menuNo = scanner.nextLine();
//		System.out.println();
//		switch(menuNo) {
//			case "1" -> create();
//			case "2" -> read();
//			case "3" -> clear();
//			case "4" -> exit();
//			case "5" -> member.signout();
//			case "6" -> member.updateMember();
//			case "7" -> member.withdraw();
//		}
//	}	
//	
//	public void create() {
//		//입력 받기
//		Board board = new Board();
//		System.out.println("새 게시물 입력");
//		System.out.print("제목: "); 	
//		board.setBtitle(scanner.nextLine());
//		System.out.print("내용: "); 	
//		board.setBcontent(scanner.nextLine());
//		
//        // 로그인된 사용자의 mid를 작성자로 설정
//        String writer = member.getMid();
//		
//		//보조메뉴 출력
//		System.out.println("-----------------------------------------------------------------------");
//		System.out.println("게시물 생성: 1.확인 | 2.취소");
//		System.out.print("메뉴선택: ");
//		String menuNo = scanner.nextLine();
//		if(menuNo.equals("1")) {
//			//boardtable 테이블에 게시물 정보 저장
//			try {
//				String sql =
//					"INSERT INTO boardtable (bno, btitle, bcontent, bwriter, bdate) " +
//					"VALUES (SEQ_BNO.NEXTVAL, ?, ?, ?, sysdate)";
//				PreparedStatement pstmt = conn.prepareStatement(sql);
//				pstmt.setString(1, board.getBtitle());
//				pstmt.setString(2, board.getBcontent());
//				pstmt.setString(3, writer);
//				pstmt.executeUpdate();
//				System.out.println("게시물이 등록되었습니다.");
//				pstmt.close();
//			} catch (Exception e) {
//				e.printStackTrace();
//				System.out.println("게시물 생성 중 오류가 발생했습니다.");
//				exit();
//			}
//		}
//		
//		//게시물 목록으로 돌아가기
//		mainMenu();
//	}
//	
//	public void read() {
//		//입력 받기
//		System.out.println("게시물 읽기");
//		System.out.print("게시물 번호: "); 	
//		int bno = Integer.parseInt(scanner.nextLine());
//		
//		//boardtable 테이블에서 해당 게시물을 가져와 출력
//		try {
//			String sql = "" +
//				"SELECT bno, btitle, bcontent, bwriter, bdate, bcount " +
//				"FROM boardtable " +
//				"WHERE bno=?";
//			PreparedStatement pstmt = conn.prepareStatement(sql);
//			pstmt.setInt(1, bno);
//			ResultSet rs = pstmt.executeQuery();
//			if(rs.next()) {
//				Board board = new Board();
////				board.setBno(rs.getInt("bno"));
////				board.setBtitle(rs.getString("btitle"));
////				board.setBcontent(rs.getString("bcontent"));
////				board.setBwriter(rs.getString("bwriter"));
////				board.setBdate(rs.getDate("bdate"));
////				board.setBcount(rs.getInt("bcount"));
//				
//				System.out.println("------------------------------------------------------");
//				System.out.println("번호: " + board.getBno());
//				System.out.println("제목: " + board.getBtitle());
//				System.out.println("내용: " + board.getBcontent());
//				System.out.println("작성자: " + board.getBwriter());
//				System.out.println("작성날짜: " + board.getBdate());
//				System.out.println("조회수: " + board.getBcount());
//				//보조메뉴 출력
//				System.out.println("------------------------------------------------------");
//				System.out.println("보조메뉴: 1.수정 | 2.삭제 | 3.돌아가기");
//				System.out.print("메뉴선택: ");
//				String menuNo = scanner.nextLine();
//				System.out.println();
//				
//				if(menuNo.equals("1")) {
//					update(board);
//				} else if(menuNo.equals("2")) {
//					delete(board);
//				} else if(menuNo.equals("3")) {
//					mainMenu();
//				} else {
//					System.out.println("잘못된 접근입니다. 프로그램을 종료합니다.");
//					exit();
//				}
//			}
//			rs.close();
//			pstmt.close();
//		} catch (Exception e) {
//			System.out.println("데이터를 읽는 중 오류가 발생했습니다. 관리자에게 문의하세요.");
//			e.printStackTrace();
//			exit();
//		}
//		
//		//게시물 목록 출력
//		list();
//	}
//	
//	public void update(Board board) {
//		//수정 내용
//		System.out.println("수정 내용 입력");
//		System.out.print("제목: "); 	
//		board.setBtitle(scanner.nextLine());
//		System.out.print("내용: "); 	
//		board.setBcontent(scanner.nextLine());
//		
//		//보조메뉴 출력
//		System.out.println("-------------------------------------------------------------------");
//		System.out.println("보조메뉴: 1.Ok | 2.Cancel");
//		System.out.print("메뉴선택: ");
//		String menuNo = scanner.nextLine();
//		if(menuNo.equals("1")) {
//			//boardtable 테이블에서 게시물 정보 수정
//			try {
//				String sql = "" +
//					"UPDATE boardtable SET btitle=?, bcontent=?, bwriter=? " +
//					"WHERE bno=?";
//				PreparedStatement pstmt = conn.prepareStatement(sql);
//				pstmt.setString(1, board.getBtitle());
//				pstmt.setString(2, board.getBcontent());
//				pstmt.setString(3, board.getBwriter());
//				pstmt.setInt(4, board.getBno());
//				pstmt.executeUpdate();
//				pstmt.close();
//			} catch (Exception e) {
//				e.printStackTrace();
//				exit();
//			}
//		}
//		
//		//게시물 목록 출력
//		list();
//	}
//	
//	public void delete(Board board) {
//		//boardtable 테이블에 게시물 정보 삭제
//		try {
//			String sql = "DELETE FROM boardtable WHERE bno=?";
//			PreparedStatement pstmt = conn.prepareStatement(sql);
//			pstmt.setInt(1, board.getBno());
//			pstmt.executeUpdate();
//			pstmt.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//			exit();
//		}
//		
//		//게시물 목록 출력		
//		list();
//	}
//	
//	public void clear() {
//		System.out.println("게시물 삭제");
//		System.out.println("-------------------------------------------------------------------");
//		System.out.println("보조메뉴: 1.Ok | 2.Cancel");
//		System.out.print("메뉴선택: ");
//		String menuNo = scanner.nextLine();
//		if(menuNo.equals("1")) {
//			//boardtable 테이블에 게시물 정보 전체 삭제
//			try {
//				String sql = "TRUNCATE TABLE boardtable";
//				PreparedStatement pstmt = conn.prepareStatement(sql);
//				pstmt.executeUpdate();
//				pstmt.close();
//			} catch (Exception e) {
//				e.printStackTrace();
//				exit();
//			}
//		}
//			
//		//게시물 목록 출력
//		list();
//	}
//	
//	public void exit() {
//		if(conn != null) {
//			try {
//				conn.close();
//			} catch (SQLException e) {
//			}
//		}
//		System.out.println("** 게시판 종료 **");
//		System.exit(0);
//	}
	
	public static void main(String[] args) {
//		BoardProject BoardProject = new BoardProject();
//		BoardProject.list();
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
		
//		//회원 조회(로그인)
		if (member.isLoggedIn()) {
			//로그인 성공시 게시판메뉴 표시
			Board board = new Board(conn, member);
			board.mainMenu();
		} else {
			System.out.println("잘못된 접근입니다.");
//			member.memberMenu();
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
