package mini_gwag;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Scanner;

import lombok.Data;

@Data
public class Member {
	Scanner scanner = new Scanner(System.in);
	Connection conn;
	private boolean isLoggedIn; // 로그인 상태를 저장할 변수
	private String mid; 
	private String mname; 
	private String mpassword; 
	private String mphone;
	private String maddress;
	private String msex;
	
	public String getMid() {
	    return mid;
	}
	
	public Member(Connection conn) {
        this.conn = conn;
    }
	
	public void memberMenu() {
		boolean loggedIn = false;
		while(!loggedIn) {
			System.out.println("-----------------------------------------------------------------------");
			System.out.println("=========================== 미니 프로젝트 1차 =============================");
			System.out.println("-----------------------------------------------------------------------");
			System.out.println("메뉴: 1.회원가입 | 2.로그인 | 3.아이디 찾기 | 4.비밀번호 찾기  | 5.종료");
			System.out.print("메뉴선택: ");
			String menuNo = scanner.nextLine();
			System.out.println();
			switch(menuNo) {
				case "1" -> signup();
				case "2" -> {
					signin();
					if(this.isLoggedIn) {
						loggedIn = true;
					}
				}
				case "3" -> findId();
	//			case "4" -> findpassword();
				case "5" -> exit();
			}
		}
	}
	public void exit() {
		if(conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
			}
		}
		System.out.println("=============================  프로그램 종료 ==============================");
		System.exit(0);
	}
	//게시판 -> 멤버 회원가입
	public void signup() {
		//입력 받기
		System.out.println("======회원가입======");
		System.out.print("아이디: "); 	
		this.setMid(scanner.nextLine());
		System.out.print("비밀번호: "); 	
		this.setMpassword(scanner.nextLine());
		System.out.print("이름: "); 	
		this.setMname(scanner.nextLine());
		System.out.print("전화번호: "); 	
		this.setMphone(scanner.nextLine());
		System.out.print("주소: "); 	
		this.setMaddress(scanner.nextLine());
		System.out.print("성별: "); 	
		this.setMsex(scanner.nextLine());
		
		//보조메뉴 출력
		System.out.println("-----------------------------------------------------------------------");
		System.out.println("1.회원가입 | 2.취소");
		System.out.print("메뉴선택: ");
		String menuNo = scanner.nextLine();
		if(menuNo.equals("1")) {
			//membertable 테이블에 게시물 정보 저장
			try {
				String sql = "" +
					"INSERT INTO membertable (mid, mpassword, mname, mphone, maddress, msex) " +
					"VALUES (?, ?, ?, ?, ? ,?)";
				PreparedStatement pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, this.getMid());
				pstmt.setString(2, this.getMpassword());
				pstmt.setString(3, this.getMname());
				pstmt.setString(4, this.getMphone());
				pstmt.setString(5, this.getMaddress());
				pstmt.setString(6, this.getMsex());
				
				pstmt.executeUpdate();
				System.out.println("회원가입 성공.");
				//다시 멤버메뉴화면으로 돌아가기
				memberMenu();
				pstmt.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("회원가입이 취소되었습니다.");
			//다시 멤버메뉴화면으로 돌아가기
			memberMenu();
		}
	}
	//게시판 -> 멤버 로그인
	public void signin() {
		//회원 조회(로그인)
        // 사용자로부터 아이디와 비밀번호 입력 받기
        System.out.print("아이디 입력: ");
        this.mid = scanner.nextLine();
        System.out.print("비밀번호 입력: ");
        this.mpassword = scanner.nextLine();
        
		try {
			//SQL문 작성
			String sql = "SELECT mid FROM membertable WHERE mid=? AND mpassword=? AND menabled = 1";
			//PreparedStatement 얻기 및 값 지정
			PreparedStatement pstmt = conn.prepareStatement(sql);
			
	        pstmt.setString(1, this.mid);  // 아이디 설정
	        pstmt.setString(2, this.mpassword);  // 비밀번호 설정
			
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()) {						
				//멤버 계정이 있을 경우
				System.out.println("로그인 성공: " + rs.getString("mid"));
				this.isLoggedIn = true;  	// 로그인 성공시 상태 업데이트
	            // 로그인 기록 삽입 및 로그인 시간 업데이트
	            logLoginTime();
	            // 로그인 시간 출력
	            displayLoginTime();
			} else {                           
				//계정이 없거나 아이디, 비밀번호가 틀릴 경우
				System.out.println("로그인 실패: 잘못된 아이디 또는 비밀번호 입니다.");
				this.isLoggedIn = false;	// 로그인 실패시 상태 업데이트
				memberMenu();
			}
			rs.close();
			pstmt.close();
		} catch (Exception e) {
			e.printStackTrace();
			this.isLoggedIn = false;
		}
	}
	
	// 로그인 시간 기록 및 업데이트
	public void logLoginTime() throws SQLException {
	    String logSql = "INSERT INTO LOGRECORD (mid, LOGINTIME, LOGOUTTIME) VALUES (?, systimestamp, null)";
	    String updateSql = "UPDATE membertable SET MLOGINDATE = systimestamp WHERE mid = ?";
	    
	    try (PreparedStatement logPstmt = conn.prepareStatement(logSql);
	         PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {

	        logPstmt.setString(1, this.mid);
	        logPstmt.executeUpdate();

	        updatePstmt.setString(1, this.mid);
	        updatePstmt.executeUpdate();
	    }
	}

	// 로그인 시간 출력
	public void displayLoginTime() throws SQLException {
	    String getTimeSql = "SELECT LOGINTIME FROM (SELECT LOGINTIME FROM LOGRECORD WHERE MID = ? ORDER BY LOGINTIME DESC) WHERE ROWNUM = 1";

	    try (PreparedStatement getTimePstmt = conn.prepareStatement(getTimeSql)) {
	        getTimePstmt.setString(1, this.mid);
	        try (ResultSet rs = getTimePstmt.executeQuery()) {
	            if (rs.next()) {
	                Timestamp loginTime = rs.getTimestamp("LOGINTIME");
	                System.out.println("로그인 성공! 로그인 시간: " + loginTime);
	            }
	        }
	    }
	}
	
	public void signout() {
	    try {
	        // 로그아웃 시간 업데이트
	        String logOutSql = "UPDATE LOGRECORD SET LOGOUTTIME = systimestamp WHERE mid = ? AND LOGOUTTIME IS NULL";
	        try (PreparedStatement pstmt = conn.prepareStatement(logOutSql)) {
	            pstmt.setString(1, this.mid);
	            int rowsUpdated = pstmt.executeUpdate();

	            if (rowsUpdated > 0) {
	                System.out.println("로그아웃 완료");
	                displayLogoutTime();
	            } else {
	                System.out.println("로그아웃 시간 업데이트 실패");
	            }
	        }
	        
	        this.isLoggedIn = false; // 로그아웃 상태로 변경

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public void displayLogoutTime() throws SQLException {
	    String getTimeSql = "SELECT LOGOUTTIME FROM (SELECT LOGOUTTIME FROM LOGRECORD WHERE MID = ? ORDER BY LOGOUTTIME DESC) WHERE ROWNUM = 1";

	    try (PreparedStatement getTimePstmt = conn.prepareStatement(getTimeSql)) {
	        getTimePstmt.setString(1, this.mid);
	        try (ResultSet rs = getTimePstmt.executeQuery()) {
	            if (rs.next()) {
	                Timestamp logoutTime = rs.getTimestamp("LOGOUTTIME");
	                System.out.println(" 로그아웃 성공! 로그아웃 시간: " + logoutTime);
	            }
	        }
	    }
	}
	
    // 로그인 상태 확인
    public boolean isLoggedIn() {
    	 return mid != null && !mid.trim().isEmpty();
    }
    
    
    //회원정보 수정
    public void updateMember() {
        System.out.print("비밀번호를 입력해주세요: ");
        String inputPassword = scanner.nextLine();

        // 비밀번호가 맞을 경우, 정보 수정
        if (inputPassword.equals(this.mpassword)) {
            System.out.println("수정할 정보를 입력하세요.");
            System.out.print("전화번호: ");
            this.setMphone(scanner.nextLine());
            System.out.print("주소: ");
            this.setMaddress(scanner.nextLine());
            
            // 회원 정보 업데이트
            try {
                String updateSql = "UPDATE membertable SET mphone=?, maddress=? WHERE mid=?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                    pstmt.setString(2, this.getMphone());
                    pstmt.setString(3, this.getMaddress());
                    pstmt.setString(5, this.getMid());
                    pstmt.executeUpdate();
                    System.out.println("회원 정보가 수정되었습니다.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("비밀번호가 틀렸습니다. 잘못된 접근입니다.");
        }
    }

    public void withdraw() {
        System.out.print("비밀번호를 입력해주세요: ");
        String inputPassword = scanner.nextLine();

        if (inputPassword.equals(this.mpassword)) {
            try {
            	// 회원을 비활성화 상태로 변경
                String deleteSql = "UPDATE membertable SET menabled  = 0 WHERE mid=?";
                try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                    pstmt.setString(1, this.getMid());
                    pstmt.executeUpdate();
                    System.out.println("회원 탈퇴가 완료되었습니다.");
                    memberMenu();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("비밀번호가 틀렸습니다. 잘못된 접근입니다.");
        }
    }
    
    public void findId() {
    	//이름, 전화번호 입력받기
    	System.out.println("이름: ");
    	String mname = scanner.nextLine();
    	System.out.print("전화번호 입력: ");
    	String mphone = scanner.nextLine();
    	try {
			// SQL문 작성
			String sql = "SELECT mid FROM membertable WHERE mname=? AND mphone=?";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, mname);  
			pstmt.setString(2, mphone);  
			
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()) {                        
			    // 아이디가 있는 경우
			    System.out.println("아이디 : " + rs.getString("mid"));
			} else {                           
			    // 아이디가 없는 경우
			    System.out.println("아이디를 찾을 수 없습니다.");
			    }
			    rs.close();
			    pstmt.close();
			} catch (Exception e) {
			    e.printStackTrace();
			}
    }
    
    
}
