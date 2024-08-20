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
	private String mid; 		// 아이디
	private String mname; 		// 이름
	private String mpassword; 	// 비밀번호
	private String mphone;		// 전화번호
	private String maddress;	// 주소
	private String msex;		// 성별
	private String mrole;		// 사용자 권한(관리자 유무)
	
	public Member(Connection conn) {
        this.conn = conn;
    }
	//메뉴화면
	public void memberMenu() {
		while(!isLoggedIn) {
			System.out.println("-----------------------------------------------------------------------");
			System.out.println("============================ 미니 프로젝트 1차 =============================");
			System.out.println("-----------------------------------------------------------------------");
			System.out.println("메뉴: 1.회원가입 | 2.로그인 | 3.아이디 찾기 | 4.비밀번호 찾기  | 5.종료");
			System.out.print("메뉴선택: ");
			String menuNo = scanner.nextLine();
			System.out.println();
			switch(menuNo) {
				case "1" -> signup();		//회원가입
				case "2" -> signin();		//로그인
				case "3" -> findId();		//아이디 찾기
				case "4" -> findPassword();	//비밀번호 찾기
				case "5" -> exit();			//프로그램 종료
			}
		}
	}
	//게시판 -> 멤버 회원가입
	public void signup() {
		//정보 입력 받기
		System.out.println("====================  회원가입  =====================");
		//사용자가 입력한 아이디가 db에 존재하는지 먼저 확인 => 중복된 아이디를 가질 수 없음
		String inputMid;
		    while (true) {
	        System.out.print("아이디: ");
	        inputMid = scanner.nextLine();
	        //아이디 중복 확인 코드
	        String checkIdSql = "SELECT mid FROM membertable WHERE mid = ?";
	        boolean isExistingAccount = false;
	        boolean isAccountDisabled = false;
	        try (PreparedStatement checkIdPstmt = conn.prepareStatement(checkIdSql)) {
	            checkIdPstmt.setString(1, inputMid);
	            try (ResultSet rs = checkIdPstmt.executeQuery()) {
	                if (rs.next()) {
	                    isExistingAccount = true;
	                    // 아이디가 이미 존재하는 경우 활성화 상태를 확인
	                    String checkStatusSql = "SELECT menabled FROM membertable WHERE mid = ?";
	                    try (PreparedStatement checkStatusPstmt = conn.prepareStatement(checkStatusSql)) {
	                        checkStatusPstmt.setString(1, inputMid);
	                        try (ResultSet statusRs = checkStatusPstmt.executeQuery()) {
	                            if (statusRs.next() && statusRs.getInt("menabled") == 0) {
	                                isAccountDisabled = true; // 비활성화된 계정임
	                            }
	                        }
	                    }
	                }
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }

	        if (isExistingAccount) {
	            if (isAccountDisabled) {
	                System.out.println("비활성화된 계정이 발견되었습니다. 해당 계정을 활성화하려면 관리자에게 문의하십시오.");
	            } else {
	                System.out.println("이미 사용 중인 아이디입니다. 다른 아이디를 입력해주세요.");
	            }
	        } else {
	            this.setMid(inputMid); 	// 중복되지 않은 아이디를 설정
	            break; 					// 중복되지 않은 아이디인 경우 반복문 종료
	        }
	    }
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
		
		// 기본 사용자 권한 설정
	    if (this.getMrole() == null) {
	        this.setMrole("ROLE USER");  // mrole이 null일 경우 "ROLE USER"로 설정
	    }
		
		System.out.println("======================================================");
		//보조메뉴 출력
		System.out.println("-----------------------------------------------------------------------");
		System.out.println("1.회원가입 | 2.취소");
		System.out.print("메뉴선택: ");
		String menuNo = scanner.nextLine();
		if(menuNo.equals("1")) {
			//membertable 테이블에 게시물 정보 저장
			try {
				String sql = "" +
					"INSERT INTO membertable (mid, mpassword, mname, mphone, maddress, msex, mrole) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?)";
				PreparedStatement pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, this.getMid());
				pstmt.setString(2, this.getMpassword());
				pstmt.setString(3, this.getMname());
				pstmt.setString(4, this.getMphone());
				pstmt.setString(5, this.getMaddress());
				pstmt.setString(6, this.getMsex());
				pstmt.setString(7, this.getMrole());
				
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
	//회원 로그인
	public void signin() {
        // 사용자로부터 아이디와 비밀번호 입력 받기
        System.out.print("아이디 입력: ");
        this.mid = scanner.nextLine();
        System.out.print("비밀번호 입력: ");
        this.mpassword = scanner.nextLine();
        
		try {
			String sql = "SELECT mid, mrole FROM membertable WHERE mid=? AND mpassword=? AND menabled = 1";
			//PreparedStatement 얻기 및 값 지정
			PreparedStatement pstmt = conn.prepareStatement(sql);
	        pstmt.setString(1, this.mid);  			// 아이디 설정
	        pstmt.setString(2, this.mpassword);  	// 비밀번호 설정
			
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()) {						
				//멤버 계정이 있을 경우
				System.out.println("로그인 성공: " + rs.getString("mid"));
				isLoggedIn = true;  				// 로그인 성공시 상태 업데이트
				
				// 로그인한 사용자의 mrole 확인
	            String role = rs.getString("mrole");
	            if ("ROLE ADMIN".equals(role)) {
	            	//로그인 계정이 관리자인 경우
	                System.out.println("==== 알림 ==== : [관리자] 계정으로 로그인 되었습니다.");
	            } else {
	            	//로그인 계정이 사용자인 경우
	            	System.out.println("---- 알림 ---- : 일반 사용자 계정으로 로그인 되었습니다.");
	            }
	            // 로그인 기록 삽입 및 로그인 시간 업데이트
	            logLoginTime();
	            // 로그인 시간 출력
	            displayLoginTime();
	            // 로그인 후 메인 메뉴로 이동
	            Board board = new Board(conn, this);
	            board.mainMenu();
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
	
	// 로그인 시간 기록
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
	    boolean logUpdated = false;  // 로그아웃 업데이트 여부 확인
	    try {
	        // LOGRECORD 테이블의 로그아웃 시간 업데이트
	        String logOutSql = "UPDATE LOGRECORD SET LOGOUTTIME = systimestamp WHERE mid = ? AND LOGOUTTIME IS NULL";
	        try (PreparedStatement pstmt = conn.prepareStatement(logOutSql)) {
	            pstmt.setString(1, this.mid);
	            int rowsUpdated = pstmt.executeUpdate();
	            if (rowsUpdated > 0) {
	                logUpdated = true;
	            }
	        }

	        // membertable 테이블의 로그아웃 시간 갱신
	        String memberTableSql = "UPDATE membertable SET mlogoutdate = sysdate WHERE mid = ?";
	        try (PreparedStatement pstmt = conn.prepareStatement(memberTableSql)) {
	            pstmt.setString(1, this.mid);
	            int memberRowsUpdated = pstmt.executeUpdate();
	            if (memberRowsUpdated > 0) {
	                logUpdated = true;
	            }
	        }

	        // 로그아웃 시간 업데이트 여부 확인
	        if (logUpdated) {
	            System.out.println("로그아웃 시간 업데이트 성공");
	            displayLogoutTime();  	// 로그아웃 시간을 출력
	        } else {
	            System.out.println("로그아웃 시간 업데이트 실패");
	        }

	        this.isLoggedIn = false; 	// 로그아웃 상태로 변경

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	// 로그인 시간 기록
	public void logLogoutTime() throws SQLException {
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
	
	public void displayLogoutTime() throws SQLException {
	    String getTimeSql = "SELECT MLOGOUTDATE FROM membertable WHERE MID = ?";

	    try (PreparedStatement getTimePstmt = conn.prepareStatement(getTimeSql)) {
	        getTimePstmt.setString(1, this.mid);
	        try (ResultSet rs = getTimePstmt.executeQuery()) {
	            if (rs.next()) {
	            	Timestamp logoutTime = rs.getTimestamp("MLOGOUTDATE");
	                System.out.println(" 로그아웃 성공! 로그아웃 시간: " + logoutTime);
	            }
	        }
	    }
	}
	
    // 로그인 상태 확인
    public boolean isLoggedIn() {
    	 return mid != null && !mid.trim().isEmpty();
    }
    
    // 회원정보 수정
    public void updateMember() {
        System.out.print("비밀번호를 입력해주세요: ");
        String inputPassword = scanner.nextLine();

        // 비밀번호가 맞을 경우, 정보 수정
        if (inputPassword.equals(this.mpassword)) {
            System.out.println("수정할 정보를 입력하세요.");
            System.out.println("메뉴: 1.개인정보 수정 |  2.비밀번호 변경  |  3.돌아가기  ");
            System.out.print("메뉴선택: ");
			String menuNo = scanner.nextLine();
			System.out.println();
			switch(menuNo) {
				case "1" : 
					updateUser();			//회원정보 수정
					break;
				case "2" : 
					updateMpassword();		//비밀번호 변경
					break;
				
				case "3" : 
					memberMenu();			//돌아가기
					break;
				default  :
					System.out.println("올바른 접근이 아닙니다.");
					updateMember();
					break;
			}
        } else {
            System.out.println("비밀번호가 틀렸습니다. 잘못된 접근입니다.");
        }
    }
    
    // 회원정보 수정
    public void updateUser() {
        System.out.println("수정할 정보를 입력하세요.");
        System.out.print("새 전화번호: ");
        String newPhone = scanner.nextLine();
        System.out.print("새 주소: ");
        String newAddress = scanner.nextLine();

        // DB 업데이트
        try {
            String updateSql = "UPDATE membertable SET mphone=?, maddress=? WHERE mid=?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setString(1, newPhone);
                pstmt.setString(2, newAddress);
                pstmt.setString(3, this.mid);
                pstmt.executeUpdate();
                System.out.println("회원 정보가 수정되었습니다.");
                this.setMphone(newPhone);
                this.setMaddress(newAddress);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("회원 정보 수정 중 오류가 발생했습니다.");
        }
    }
    
    public void updateMpassword() {
    	// 현재 비밀번호 확인
        System.out.print("현재 비밀번호를 입력해주세요: ");
        String currentPassword = scanner.nextLine();

        // 현재 비밀번호가 맞는지 확인
        if (!currentPassword.equals(this.mpassword)) {
            System.out.println("현재 비밀번호가 틀렸습니다. 변경을 취소합니다.");
            return; // 비밀번호가 틀린 경우 메소드 종료
        }
        // 비밀번호 변경
        System.out.print("새 비밀번호를 입력해주세요: ");
        String newPassword = scanner.nextLine();
        System.out.print("새 비밀번호 확인: ");
        String confirmPassword = scanner.nextLine();
        // 새 비밀번호와 비밀번호 일치 확인
        if (newPassword.equals(confirmPassword)) {
            try {
                String updatePasswordSql = "UPDATE membertable SET mpassword=? WHERE mid=?";
                try (PreparedStatement pstmt = conn.prepareStatement(updatePasswordSql)) {
                    pstmt.setString(1, newPassword);
                    pstmt.setString(2, this.mid);
                    pstmt.executeUpdate();
                    System.out.println("비밀번호가 변경되었습니다. 다시 로그인 해주세요.");
                    this.mpassword = newPassword;  // 비밀번호 업데이트
                    // 로그아웃 후 회원 메뉴로 돌아가기
                    signout();  		// 로그아웃 메소드 호출
                    memberMenu();      	// 회원 메뉴로 돌아가기
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("비밀번호 변경 중 오류가 발생했습니다.");
            }
        } else {
            System.out.println("비밀번호가 일치하지 않습니다. 다시 시도해주세요.");
            updateMpassword();  // 비밀번호 변경 재시도
        }
    }
    
    //회원 탈퇴
    public void withdraw() {
        System.out.print("비밀번호를 입력해주세요: ");
        String inputPassword = scanner.nextLine();
        
        if (inputPassword.equals(this.mpassword)) {
        	// 탈퇴 확인
            System.out.print("정말 탈퇴하시겠습니까? (Y / N) : ");
            String confirm = scanner.nextLine();
            if(confirm.equals("Y")) {
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
	            System.out.println("회원 탈퇴가 취소되었습니다.");
	            memberMenu();
	        }
        } else {
            System.out.println("비밀번호가 틀렸습니다. 잘못된 접근입니다.");
            memberMenu();
        }
    }
    
    //아이디 찾기
    public void findId() {
    	//이름, 전화번호 입력받기
    	System.out.print("이름: ");
    	String mname = scanner.nextLine();
    	System.out.print("전화번호 입력: ");
    	String mphone = scanner.nextLine();
    	try {
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
    
    //비밀번호 찾기
    public void findPassword() {
    	//아이디, 이름, 전화번호 입력받기
    	System.out.print("아이디 입력: ");
    	String mid = scanner.nextLine();
    	System.out.print("이름 입력: ");
    	String mname = scanner.nextLine();
    	System.out.print("전화번호 입력: ");
    	String mphone = scanner.nextLine();
    	
    	try {
            String sql = "SELECT mpassword FROM membertable WHERE mid=? AND mname=? AND mphone=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, mid);  
            pstmt.setString(2, mname);
            pstmt.setString(3, mphone);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()) {                        
                // 비밀번호가 있는 경우
                System.out.println("찾은 비밀번호: " + rs.getString("mpassword"));
            } else {                           
                // 비밀번호를 찾지 못한 경우
                System.out.println("비밀번호를 찾을 수 없습니다.");
            }
            rs.close();
            pstmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    //프로그램 종료
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
    
}
