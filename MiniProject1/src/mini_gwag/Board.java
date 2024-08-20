package mini_gwag;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Scanner;

import lombok.Data;

@Data
public class Board {
	private int bno;			// 게시물번호
	private String btitle;		// 제목
	private String bcontent;	// 내용
	private String bwriter; 	// 작성자
	private int bcount;			// 조회수
	private Date bdate;			// 작성일시
	private Scanner scanner = new Scanner(System.in);
	private Connection conn = null;
	private Member member;
		
	//게시물 메인메뉴 화면
	public void mainMenu() {
		System.out.println("---------------------------------------------------------------------------------------------");
		System.out.println("메뉴: 1.게시물 생성 |  2.게시물 보기  |  3.게시물 삭제 |  4.종료   | 5.로그아웃  |  6.개인정보수정   |   7.회원탈퇴  ");
		System.out.print("메뉴선택: ");
		String menuNo = scanner.nextLine();
		System.out.println();
		System.out.println("---------------------------------------------------------------------------------------------");
		System.out.println();
		switch(menuNo) {
			case "1" -> create();				//게시물 생성
			case "2" -> read();					//게시물 읽기
			case "3" -> delete();				//게시물 삭제
			case "4" -> exit();					//게시판 종료
			case "5" -> member.signout(); 		//회원 로그아웃
			case "6" -> member.updateMember();	//회원정보 수정
			case "7" -> member.withdraw();		//회원탈퇴
			default -> System.out.println("잘못된 입력입니다.");
		}
	}	
	
	public Board(Connection conn, Member member) {
		this.conn = conn;
        this.member = member;
	}
	
	public void create() {
		//입력 받기
		Board board = new Board(conn, member);
		System.out.println("-------------------------------------------------------------------");
		System.out.println("새 게시물 입력");
		System.out.print("제목: "); 	
		board.setBtitle(scanner.nextLine());
		System.out.print("내용: "); 	
		board.setBcontent(scanner.nextLine());
		System.out.println();
		System.out.println("-------------------------------------------------------------------");
        // 로그인된 사용자의 mid를 작성자로 설정
        String writer = member.getMid();
		//보조메뉴 출력
		System.out.println("-------------------------------------------------------------------");
		System.out.println("게시물 생성: 1.확인 | 2.취소");
		System.out.print("메뉴선택: ");
		String menuNo = scanner.nextLine();
		System.out.println();
		System.out.println("-------------------------------------------------------------------");
		if(menuNo.equals("1")) {
			//boardtable 테이블에 게시물 정보 저장
			try {
				String sql =
					"INSERT INTO boardtable (bno, btitle, bcontent, bwriter, bdate) " +
					"VALUES (SEQ_BNO.NEXTVAL, ?, ?, ?, sysdate)";
				PreparedStatement pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, board.getBtitle());
				pstmt.setString(2, board.getBcontent());
				pstmt.setString(3, writer);
				pstmt.executeUpdate();
				System.out.println("------ 게시물이 등록되었습니다. ------");
				pstmt.close();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("------ 게시물 생성 중 오류가 발생했습니다. ------");
				exit();
			}
		}
		//게시물 목록으로 돌아가기
		mainMenu();
	}
	
	//게시물 내용 읽기
	public void read() {
		//입력 받기
		System.out.println("-------------------------------------------------------------------");
		System.out.println("게시물 읽기");
		System.out.print("게시물 번호: "); 	
		int bno = Integer.parseInt(scanner.nextLine());
		System.out.println();
		System.out.println("-------------------------------------------------------------------");
		//boardtable 테이블에서 해당 게시물을 가져와 출력
		try {
			String sql = "" +
				"SELECT bno, btitle, bcontent, bwriter, bdate, bcount " +
				"FROM boardtable " +
				"WHERE bno=?";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, bno);
			ResultSet rs = pstmt.executeQuery();
			
			if(rs.next()) {
				Board board = new Board(conn, member);
					board.setBno(rs.getInt("bno"));
					board.setBtitle(rs.getString("btitle"));
					board.setBcontent(rs.getString("bcontent"));
					board.setBwriter(rs.getString("bwriter"));
					board.setBdate(rs.getDate("bdate"));
					board.setBcount(rs.getInt("bcount"));
				
					System.out.println("------------------------------------------------------");
					System.out.println("번호: " + board.getBno());
					System.out.println("제목: " + board.getBtitle());
					System.out.println("내용: " + board.getBcontent());
					System.out.println("작성자: " + board.getBwriter());
					System.out.println("작성날짜: " + board.getBdate());
					System.out.println("조회수: " + board.getBcount());
					System.out.println("------------------------------------------------------");
					//보조메뉴 출력
					System.out.println("------------------------------------------------------");
					System.out.println("보조메뉴: 1.수정 | 2.삭제 | 3.돌아가기");
					System.out.print("메뉴선택: ");
					String menuNo = scanner.nextLine();
					System.out.println();
					System.out.println("------------------------------------------------------");
					System.out.println();
				
				if(menuNo.equals("1")) {
					//게시물 수정
					update(board);
				} else if(menuNo.equals("2")) {
					//게시물 삭제
					delete();
				} else if(menuNo.equals("3")) {
					//메뉴화면으로 돌아가기
					mainMenu();
				} else {
					System.out.println("잘못된 접근입니다. 프로그램을 종료합니다.");
					exit();
				}
			}
			rs.close();
			pstmt.close();
		} catch (Exception e) {
			System.out.println("데이터를 읽는 중 오류가 발생했습니다. 관리자에게 문의하세요.");
			e.printStackTrace();
			exit();
		}
	}
	
	public void update(Board board) {
		System.out.print("비밀번호를 입력해주세요: ");
		String inputMpassword = scanner.nextLine();
		//수정 내용
		if(inputMpassword.equals(member.getMpassword())) {
			System.out.println("-------------------------------------------------------------------");
			System.out.println("수정 내용 입력");
			System.out.print("제목: "); 	
			board.setBtitle(scanner.nextLine());
			System.out.print("내용: "); 	
			board.setBcontent(scanner.nextLine());
			System.out.println();
			System.out.println("-------------------------------------------------------------------");
			//선택메뉴 출력
			System.out.println("-------------------------------------------------------------------");
			System.out.println("수정메뉴: 1.수정확인 | 2.수정취소");
			System.out.print("메뉴선택: ");
			String menuNo = scanner.nextLine();
			System.out.println();
			System.out.println("-------------------------------------------------------------------");
		
			if(menuNo.equals("1")) {
				//boardtable 테이블에 게시물 정보 수정
				try {
					String sql = "" +
						"UPDATE boardtable SET btitle=?, bcontent=?, bwriter=? " +
						"WHERE bno=?";
					PreparedStatement pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, board.getBtitle());
					pstmt.setString(2, board.getBcontent());
					pstmt.setString(3, board.getBwriter());
					pstmt.setInt(4, board.getBno());
					pstmt.executeUpdate();
					pstmt.close();
				} catch (Exception e) {
					e.printStackTrace();
					exit();
				}
			}
		} else {
			System.out.println("비밀번호가 틀렸습니다.");
		}
	}
	
	public void delete() {
		//boardtable 테이블에 게시물 정보 삭제 bno를 찾아 게시물 삭제
		System.out.println("-------------------------------------------------------------------");
		System.out.println("게시물 삭제");
		System.out.println("삭제할 게시물 번호: ");
		int bno = Integer.parseInt(scanner.nextLine());
		System.out.println("-------------------------------------------------------------------");
		System.out.println("게시물을 삭제하시겠습니까?: 1.확인 | 2.취소");
		System.out.print("메뉴선택: ");
		System.out.println();
		System.out.println("-------------------------------------------------------------------");
		String menuNo = scanner.nextLine();
		//삭제를 확인한 경우(1 입력했을 경우)
		if(menuNo.equals("1")) {
			try {
				//delete쿼리문 실행
				String sql = "DELETE FROM boardtable WHERE bno=?";
				PreparedStatement pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, bno);
				int deleteResult = pstmt.executeUpdate();
				pstmt.close();
				 // 삭제 결과 출력
	            if (deleteResult > 0) {
	                System.out.println("게시물이 성공적으로 삭제되었습니다.");
	            } else {
	                System.out.println("해당 번호의 게시물이 존재하지 않습니다.");
	            }
			} catch (Exception e) {
				e.printStackTrace();
				exit();
			}
		} else {
	        //삭제를 취소한 경우
	        System.out.println("게시물 삭제가 취소되었습니다.");
	    }
	}
	
	//게시판 종료
	public void exit() {
		if(conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
			}
		}
		System.out.println("------------------------------ 게시판 종료 ---------------------------------");
		System.exit(0);
	}
	
    public void incrementBcount() {
        this.bcount++;
    }
    
}
