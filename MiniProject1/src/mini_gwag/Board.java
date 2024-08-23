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
	private int currentPage = 1; // 현재 페이지 번호
	
	//데이터베이스 연결, 로그인한 사용자의 정보를 Board에 전달하기 위해 생성
	public Board(Connection conn, Member member) {
		this.conn = conn;
        this.member = member;
	}
	
	//게시물 목록 및 페이징 처리
	public void list(int currentPage) {
		int page_size = 10; 								// 한 페이지에 출력할 게시물 수
		int startRow = (currentPage - 1) * page_size + 1;	// 현재 페이지 시작 행 번
	    int endRow = currentPage * page_size;				// 현재 페이지 종료 행 번호
		
		System.out.println();
		System.out.println("[게시물 목록]");
		System.out.println("-----------------------------------------------------------------------");
		System.out.println("[게시물번호]       [작성자]         [제목]             [조회수]          [작성날짜]");
		System.out.println("-----------------------------------------------------------------------");
		//boardtable에서 게시물 정보를 페이징처리해서 가져오기
		try {
			// 게시물 번호를 기준으로 내림차순 정렬 후 페이지 번호에 맞는 게시물 조회
			String listSql = "" +
					"select * from (" +
		            "select bno, bwriter, btitle, bcount, bdate, " +
		            "ROW_NUMBER() over (order by bno desc) as rnum " +
		            "from boardtable " +
		            ") where rnum between ? and ?";
			PreparedStatement pstmt = conn.prepareStatement(listSql);
			pstmt.setInt(1, startRow);
	        pstmt.setInt(2, endRow);
			ResultSet rs = pstmt.executeQuery();
			// 조회된 게시물 목록 출력
			while(rs.next()) {
				//rs에 직접 값을 가져와서 게시물 내용을 출력
				System.out.printf("%-16s%-16s%-16s%-16s%-16s\n", 
						rs.getInt("bno"), 
						rs.getString("bwriter"),
						rs.getString("btitle"),
						rs.getInt("bcount"),
						rs.getDate("bdate"));
			}
			rs.close();
			pstmt.close();
		} catch(SQLException e) {
			e.printStackTrace();
			exit();
		}
	    // 페이지 이동 메뉴
	    System.out.println("-----------------------------------------------------------------------");
	    System.out.println("현재 페이지: " + currentPage + " | 총 페이지 수: " + getTotalPageCount());
	    System.out.println("1. 이전 페이지");
	    System.out.println("2. 다음 페이지");
	    System.out.println("3. 특정 페이지 이동");
	    System.out.println("4. 게시물 상세보기");
	    System.out.println("5. 돌아가기");
	    System.out.print("메뉴 선택: ");
	    int menuNo = Integer.parseInt(scanner.nextLine());

	    // 페이지 이동 및 처리
	    switch (menuNo) {
	        case 1 -> {
	            if (currentPage > 1) {
	                list(currentPage - 1); 	// 이전 페이지
	            } else {
	                System.out.println("이전 페이지가 없습니다.");
	                list(currentPage); 		// 현재 페이지 표시
	            }
	            break;
	        }
	        case 2 -> {
	            if (currentPage * page_size < getTotalCount()) { 	// 총 게시물 수를 반환
	                list(currentPage + 1); 	// 다음 페이지
	            } else {
	                System.out.println("다음 페이지가 없습니다.");
	                list(currentPage); 		// 현재 페이지 표시
	            }
	            break;
	        }
	        case 3 -> {
	            System.out.print("이동할 페이지 번호: ");
	            int page = Integer.parseInt(scanner.nextLine());
	            if (page >= 1 && page <= getTotalPageCount()) { 	// 총 페이지 수를 반환하는 메소드
	                list(page); 			// 특정 페이지 이동
	            } else {
	                System.out.println("잘못된 페이지 번호입니다.");
	                list(currentPage); 		// 현재 페이지 다시 표시
	            }
	            break;
	        }
	        case 4 ->
	            // 게시물 상세보기
	            read();
	        case 5 ->
	            mainMenu();
	        default -> {
	            System.out.println("잘못된 선택입니다.");
	            list(currentPage); 			// 현재 페이지 다시 표시
	        }
	    }
	}
	
	// 전체 페이지 목록 보기
	private void showBoardList() {
	    list(currentPage);
	}
	
	// 총 게시물 수를 반환
	public int getTotalCount() {
	    int totalCount = 0;
	    try {
	    	//count(*) = 특정 테이블에 있는 전체 행의 수를 계산 (COUNT는 SQL에서 제공하는 집계 함수)
	        String countSql = "select count (*) from boardtable";
	        PreparedStatement pstmt = conn.prepareStatement(countSql);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            totalCount = rs.getInt(1);
	        }
	        rs.close();
	        pstmt.close();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return totalCount;
	}

	//전체 게시물 수를 기반으로 총 페이지 수를 반환
	public int getTotalPageCount() {
		int page_size = 10; //한 페이지에 출력할 게시물 수
	    return (int) Math.ceil((double) getTotalCount() / page_size);
	}
	
	// 게시물 메인메뉴 화면
	public void mainMenu() {
		while (true) { 
			// 메인 메뉴 반복
			System.out.println("---------------------------------------------------------------------------------------------");
			System.out.println("메뉴: 1.게시물 생성 |  2.게시물 목록  |  3.게시물 삭제 |  4.종료   | 5.로그아웃  |  6.개인정보수정   |   7.회원탈퇴  ");
			// 관리자인 경우에만 '8. 회원 목록' 옵션을 추가
			if ("ROLE ADMIN".equals(member.getMrole())) {
				System.out.println("    8. 회원 목록 조회");
			}
			System.out.print("메뉴선택: ");
			String menuNo = scanner.nextLine();
			System.out.println();
			System.out.println("---------------------------------------------------------------------------------------------");
			System.out.println();
			
			switch(menuNo) {
				case "1" -> create();				//게시물 생성
				case "2" -> showBoardList();		//게시물 목록
				case "3" -> delete();				//게시물 삭제
				case "4" -> exit();					//게시판 종료
				case "5" -> member.signout(); 		//회원 로그아웃
				case "6" -> member.updateMember();	//회원정보 수정
				case "7" -> member.withdraw();		//회원탈퇴
				case "8" -> {						//회원 목록 보기 (관리자 전용)
					if("ROLE ADMIN".equals(member.getMrole())) {
						member.showMemberList();
					} else {
						System.out.println("잘못된 접근입니다.");
					}
				}       
				default -> {
					System.out.println("잘못된 입력입니다.");
				}
			}
		}	
	}
	
	//게시물 생성
	public void create() {
		// 게시물 정보 입력 받기
		Board board = new Board(conn, member);
		System.out.println("-------------------------------------------------------------------");
		System.out.println("새 게시물 입력");
		System.out.print("제목: "); 	
		board.setBtitle(scanner.nextLine());
		System.out.print("내용: "); 	
		board.setBcontent(scanner.nextLine());
		System.out.println();
        // 로그인된 사용자의 mid를 작성자로 설정  
        String writer = member.getMid();
        
        // 보조메뉴 출력(게시물 생성 확인 문구)
		System.out.println("-------------------------------------------------------------------");
		System.out.println("게시물 생성: 1.확인 | 2.취소");
		System.out.print("메뉴선택: ");
		String menuNo = scanner.nextLine();
		System.out.println();
		System.out.println("-------------------------------------------------------------------");
		switch(menuNo) {
			case "1" -> {
				try {
					// boardtable 테이블에 게시물 정보 저장
					String createSql =
						"insert into boardtable (bno, btitle, bcontent, bwriter, bdate) " +
						"values (SEQ_BNO.NEXTVAL, ?, ?, ?, sysdate)";
					
					PreparedStatement pstmt = conn.prepareStatement(createSql);
					pstmt.setString(1, board.getBtitle());
					pstmt.setString(2, board.getBcontent());
					pstmt.setString(3, writer);
					pstmt.executeUpdate();
					System.out.println("----------------- 게시물이 등록되었습니다. ------------------");
					pstmt.close();
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("----------------- 게시물 생성 중 오류가 발생했습니다. -------------------");
					exit();
				}
			}
			case "2" ->
				System.out.println("게시물 생성이 취소되었습니다.");
			default ->
				System.out.println("잘못된 선택입니다.");
		}
		// 게시물 목록으로 돌아가기
		mainMenu();
	}
	
	// 게시물 상세보기
	public void read() {
		// 읽을 게시물의 번호를 입력 받기
		System.out.println("-------------------------------------------------------------------");
		System.out.println("게시물 읽기");
		System.out.print("게시물 번호: "); 	
		int bno = Integer.parseInt(scanner.nextLine());
		System.out.println();
		System.out.println("-------------------------------------------------------------------");
		// boardtable 테이블에서 해당 게시물을 가져와 출력
		try {
			String readSql = "" +
				"select bno, btitle, bcontent, bwriter, bdate, bcount " +
				"from boardtable " +
				"where bno=?";
			PreparedStatement pstmt = conn.prepareStatement(readSql);
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
				
		            board.increseBcount(); 		// 조회수 증가
		            updateBoardCount(board); 	// 조회수 업데이트
		            //직접적으로 db에서 데이터를 조회하는 것은 좋은 방법이 아니다.
//					System.out.println("번호: " + rs.getInt("bno"));
//					System.out.println("제목: " + rs.getString("btitle"));
//					System.out.println("내용: " + rs.getString("bcontent"));
//					System.out.println("작성자: " + rs.getString("bwriter"));
//					System.out.println("작성날짜: " + rs.getDate("bdate"));
//					System.out.println("조회수: " + rs.getInt("bcount"));
		            //게시물 상세정보
					System.out.println("------------------------------------------------------");
					System.out.println("번호: " + board.getBno());
					System.out.println("제목: " + board.getBtitle());
					System.out.println("내용: " + board.getBcontent());
					System.out.println("작성자: " + board.getBwriter());
					System.out.println("작성날짜: " + board.getBdate());
					System.out.println("조회수: " + board.getBcount());
					System.out.println("------------------------------------------------------");
					
					// 현재 로그인한 사용자의 아이디
		            String currentUserId = member.getMid();
		            // 사용자 권한 확인
		            boolean isAdmin = "ROLE ADMIN".equals(member.getMrole());
					// 보조메뉴 출력
					System.out.println("------------------------------------------------------");
					System.out.println("보조메뉴: 1.수정 | 2.삭제 | 3.돌아가기");
					System.out.print("메뉴선택: ");
					String menuNo = scanner.nextLine();
					System.out.println();
					System.out.println("------------------------------------------------------");
					System.out.println();
				
				 switch (menuNo) {
	                case "1" -> {
	                    // 게시물 수정 권한 확인
	                    if (currentUserId.equals(board.getBwriter())) {
	                        // 게시물 수정
	                        update(board);
	                        mainMenu();  // 수정 후 메인 메뉴로 돌아감
	                    } else {
	                        System.out.println("수정 권한이 없습니다. 작성자만 수정할 수 있습니다.");
	                    }
	                }
	                case "2" -> {
	                    // 게시물 삭제 권한 확인
	                    if (isAdmin || currentUserId.equals(board.getBwriter())) {
	                        // 게시물 삭제
	                        delete();
	                    } else {
	                        System.out.println("삭제 권한이 없습니다. 작성자 본인또는 관리자만 삭제할 수 있습니다.");
	                    }
	                    mainMenu();  // 삭제 후 메인 메뉴로 돌아감(권한이 없을 때도)
	                }
	                case "3" -> {
	                    // 메뉴화면으로 돌아가기
	                    mainMenu();
	                }
	                default -> {
	                    System.out.println("잘못된 접근입니다.");
	                    mainMenu();  // 잘못된 입력이 발생하면 메인 메뉴로 돌아감
	                }
	            }
	        } else {
	            System.out.println("게시물이 존재하지 않습니다.");
	            System.out.println("------------------------------------------------------");
	            mainMenu(); 
	        }
	        rs.close();
	        pstmt.close();
	    } catch (Exception e) {
	        System.out.println("데이터를 읽는 중 오류가 발생했습니다.");
	        e.printStackTrace();
	        exit();
	    }
		mainMenu();  // mainMenu() 호출을 반복하거나, 오류 발생 시 종료됨
	}
	
	//게시물 수정
	public void update(Board board) {
		//비밀번호 확인
		System.out.print("비밀번호를 입력해주세요: ");
		String inputMpassword = scanner.nextLine();
		
		//비밀번호가 맞다면 게시물 수정 내용 출력
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
		
			switch(menuNo) {
				case"1" -> {
					try {
						String updateSql = "" +
							"update boardtable set btitle=?, bcontent=?" +
							"where bno=?";
						PreparedStatement pstmt = conn.prepareStatement(updateSql);
						pstmt.setString(1, board.getBtitle());
						pstmt.setString(2, board.getBcontent());
						pstmt.setInt(3, board.getBno());
						pstmt.executeUpdate();
						pstmt.close();
						System.out.println("게시물 수정이 완료되었습니다.");
					} catch (Exception e) {
							e.printStackTrace();
					}
					return;
				}
				case"2" -> {
					System.out.println("게시물 수정을 취소했습니다.");
					return;
				}
				default ->	System.out.println("잘못된 선택입니다.");
			}
		} else {
			System.out.println("비밀번호가 틀렸습니다.");
			return;
		}
	}
	//게시물 삭제
	public void delete() {
		// 게시물 정보 삭제 bno를 찾아 삭제
		System.out.println("-------------------------------------------------------------------");
		System.out.println("게시물 삭제");
		System.out.println("삭제할 게시물 번호: ");
		int bno = Integer.parseInt(scanner.nextLine());
		
	    // 현재 로그인한 사용자의 아이디
		String currentUserMid = member.getMid();
	    String currentUserMpassword = member.getMpassword(); // 현재 로그인한 사용자의 비밀번호
	    boolean isAdmin = "ROLE ADMIN".equals(member.getMrole());
	    // 게시물의 작성자인지 조회
	    String writer = null;
	    
	    try {
	    	//조회문
	        String selectSql = "select bwriter from boardtable where bno=?";
	        PreparedStatement selectPstmt = conn.prepareStatement(selectSql);
	        selectPstmt.setInt(1, bno);
	        ResultSet rs = selectPstmt.executeQuery();
	        
	        if (rs.next()) {
	            writer = rs.getString("bwriter");
	        } else {
	            System.out.println("해당 번호의 게시물이 존재하지 않습니다.");
	            mainMenu();
	        }
	        rs.close();
	        selectPstmt.close();
	        // 삭제권한 체크
	        if (!isAdmin && !currentUserMid.equals(writer)) {
	            System.out.println("삭제 권한이 없습니다. 작성자 본인 또는 관리자만 삭제할 수 있습니다.");
	            return;
	        }
	        // 비밀번호 확인 (관리자는 생략)
	        if (!isAdmin) {
	            System.out.print("비밀번호 입력: ");
	            String passwordInput = scanner.nextLine();
	            if (!passwordInput.equals(currentUserMpassword)) {
	                System.out.println("비밀번호가 올바르지 않습니다. 게시물 삭제 권한이 없습니다.");
	                return;
	            }
	        }
	        // 삭제 확인
	        System.out.println("-------------------------------------------------------------------");
	        System.out.println("게시물을 삭제하시겠습니까?: 1.확인 | 2.취소");
	        System.out.print("메뉴선택: ");
	        String menuNo = scanner.nextLine();
	        System.out.println("-------------------------------------------------------------------");

	        switch (menuNo) {
	            case "1" -> { 
	            	// 게시물 삭제문
	                String deleteSql = "DELETE FROM boardtable WHERE bno=?";
	                try (PreparedStatement deletePstmt = conn.prepareStatement(deleteSql)) {
	                    deletePstmt.setInt(1, bno);
	                    int deleteResult = deletePstmt.executeUpdate();

	                    if (deleteResult > 0) {
	                        System.out.println("게시물이 성공적으로 삭제되었습니다.");
	                    } else {
	                        System.out.println("해당 번호의 게시물이 존재하지 않습니다.");
	                    }
	                } catch (SQLException e) {
	                    e.printStackTrace();
	                    System.out.println("게시물 삭제 중 오류가 발생했습니다.");
	                }
	            }

	            case "2" -> { // 게시물 삭제 취소
	                System.out.println("게시물 삭제가 취소되었습니다.");
	            }
	            default ->	System.out.println("잘못된 입력입니다.");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        System.out.println("게시물 조회 중 오류가 발생했습니다.");
	    } finally {
	        mainMenu();
	    }
	}
	
	// 게시판 종료
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
	
	// 게시물 조회수 업데이트
	public void updateBoardCount(Board board) {
	    try {
	        String updateCountSql = "update boardtable set bcount=? where bno=?";
	        PreparedStatement pstmt = conn.prepareStatement(updateCountSql);
	        pstmt.setInt(1, board.getBcount()); // 증가된 조회수
	        pstmt.setInt(2, board.getBno());
	        pstmt.executeUpdate();
	        pstmt.close();
	    } catch (SQLException e) {
	        System.out.println("조회수 업데이트 중 오류가 발생했습니다.");
	        e.printStackTrace();
	    }
	}
	
	// 게시물 조회수 증가 카운트
    public void increseBcount() {
        this.bcount++;
    }
    

    
}
