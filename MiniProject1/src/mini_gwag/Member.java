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
	
	private Board board;
	public Member(Connection conn) {
        this.conn = conn;
    }
	
	//메뉴 화면
	public void memberMenu() {
		while(!isLoggedIn) {
			System.out.println("-----------------------------------------------------------------------");
			System.out.println("============================ 미니 프로젝트 1차 =============================");
			System.out.println("-----------------------------------------------------------------------");
			System.out.println("메뉴: 1.회원가입 | 2.로그인 | 3.아이디 찾기 | 4.비밀번호 찾기  | 5.종료");
			System.out.print("메뉴선택: ");
			String menuNo = scanner.nextLine();
			System.out.println("-----------------------------------------------------------------------");
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
		//회원의 정보 입력 받기
		System.out.println("====================  회원가입  =====================");
		//사용자가 입력한 아이디가 db에 존재하는지 먼저 확인 => 중복된 아이디를 가질 수 없음
		String inputMid;
		    while (true) {
	        System.out.print("아이디: ");
	        inputMid = scanner.nextLine();
	        //아이디 중복 확인 코드
	        String checkIdSql = "select mid from membertable where mid = ?";
	        boolean isExistingAccount = false;
	        boolean isAccountDisabled = false;
	        try (PreparedStatement checkIdPstmt = conn.prepareStatement(checkIdSql)) {
	            checkIdPstmt.setString(1, inputMid);
	            try (ResultSet rs = checkIdPstmt.executeQuery()) {
	                if (rs.next()) {
	                    isExistingAccount = true;
	                    // 아이디가 이미 존재하는 경우 계정 활성화 상태를 확인
	                    String checkStatusSql = "select menabled from membertable where mid = ?";
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
	        this.setMrole("ROLE USER");  // mrole이 null일 경우 기본값인 "ROLE USER"로 설정
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
					"insert into membertable (mid, mpassword, mname, mphone, maddress, msex, mrole) " +
					"values (?, ?, ?, ?, ?, ?, ?)";
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
				//다시 메뉴화면으로 돌아가기
				memberMenu();
				pstmt.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("회원가입이 취소되었습니다.");
			//다시 메뉴화면으로 돌아가기
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
			String sql = "select mid, mrole from membertable where mid=? and mpassword=? and menabled = 1";
			PreparedStatement pstmt = conn.prepareStatement(sql);
	        pstmt.setString(1, this.mid);  			// 아이디 설정
	        pstmt.setString(2, this.mpassword);  	// 비밀번호 설정
			
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()) {						
				//멤버 계정이 있을 경우 로그인 성공
				System.out.println("로그인 성공: " + rs.getString("mid"));
				isLoggedIn = true;  				// 로그인 성공시 상태 업데이트
				
				// 로그인한 사용자의 mrole(권한) 확인
	            String role = rs.getString("mrole");
	            // 로그인 후 member 객체에 역할 저장
	            this.mrole = role; // mrole 필드에 역할 설정
	            
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
				//계정이 없거나 아이디, 비밀번호가 틀릴 경우 = 로그인 실패
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
	    String logSql = "insert into logrecord (mid, logintime, logouttime) values (?, systimestamp, null)";
	    String updateSql = "update membertable set mlogindate = systimestamp where mid = ?";
	    
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
	    String getTimeSql = "select logintime from (select logintime from logrecord where mid = ? order by logintime desc) where ROWNUM = 1";

	    try (PreparedStatement getTimePstmt = conn.prepareStatement(getTimeSql)) {
	        getTimePstmt.setString(1, this.mid);
	        try (ResultSet rs = getTimePstmt.executeQuery()) {
	            if (rs.next()) {
	                Timestamp loginTime = rs.getTimestamp("logintime");
	                System.out.println("로그인 성공! 로그인 시간: " + loginTime);
	            }
	        }
	    }
	}
	
	//로그아웃
	public void signout() {
	    boolean logUpdated = false;  // 로그아웃 업데이트 여부 확인
	    try {
	        // LOGRECORD 테이블의 로그아웃 시간 업데이트
	        String logOutSql = "update logrecord set logouttime = systimestamp where mid = ? and logouttime IS NULL";
	        try (PreparedStatement pstmt = conn.prepareStatement(logOutSql)) {
	            pstmt.setString(1, this.mid);
	            int rowsUpdated = pstmt.executeUpdate();
	            if (rowsUpdated > 0) {
	                logUpdated = true;
	            }
	        }

	        // membertable 테이블의 로그아웃 시간 갱신
	        String memberTableSql = "update membertable set mlogoutdate = sysdate where mid = ?";
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
	        this.mid = null;           // 아이디 초기화
	        this.mpassword = null;     // 비밀번호 초기화
	        this.mrole = null;         // 권한 초기화
	        System.out.println("로그아웃이 완료되었습니다.");
	        memberMenu();
	        
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	// 로그인 시간 기록
	public void logLogoutTime() throws SQLException {
	    String logSql = "insert into logrecord (mid, logintime, logouttime) values (?, systimestamp, null)";
	    String updateSql = "update membertable set mlogindate = systimestamp where mid = ?";
	    
	    try (PreparedStatement logPstmt = conn.prepareStatement(logSql);
	         PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {

	         logPstmt.setString(1, this.mid);
	         logPstmt.executeUpdate();

	         updatePstmt.setString(1, this.mid);
	         updatePstmt.executeUpdate();
	    }
	}
	
	public void displayLogoutTime() throws SQLException {
	    String getTimeSql = "select mlogoutdate from membertable where mid = ?";

	    try (PreparedStatement getTimePstmt = conn.prepareStatement(getTimeSql)) {
	        getTimePstmt.setString(1, this.mid);
	        try (ResultSet rs = getTimePstmt.executeQuery()) {
	            if (rs.next()) {
	            	Timestamp logoutTime = rs.getTimestamp("mlogoutdate");
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
        while (true) {
            System.out.print("비밀번호를 입력해주세요: ");
            String inputPassword = scanner.nextLine();

            // 비밀번호가 맞을 경우, 정보 수정
            if (inputPassword.equals(this.mpassword)) {
                System.out.println("수정할 정보를 입력하세요.");
                System.out.println("메뉴: 1. 개인정보 수정 | 2. 비밀번호 변경 | 3. 돌아가기");
                System.out.print("메뉴선택: ");
                String menuNo = scanner.nextLine();
                System.out.println();

                switch (menuNo) {
                    case "1":
                        updateUser();            // 회원정보 수정
                        break;
                    case "2":
                        updateMpassword();       // 비밀번호 변경
                        break;
                    case "3":
                    	//돌아가기
                        return;                  // 메서드 종료
                    default:
                        System.out.println("올바른 접근이 아닙니다.");
                        break; // 잘못된 접근일 경우 루프 계속
                }
            } else {
                System.out.println("비밀번호가 틀렸습니다. 다시 시도해주세요.");
            }
        }
    }
    
    // 회원정보 수정
    public void updateUser() {
        System.out.println("수정할 정보를 입력하세요.");
        System.out.print("새 전화번호: ");
        String newPhone = scanner.nextLine();
        System.out.print("새 주소: ");
        String newAddress = scanner.nextLine();

        // DB에 수정내용 업데이트
        try {
            String updateSql = "update membertable set mphone=?, maddress=? where mid=?";
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

        // 현재 비밀번호가 맞는지 확인
    	System.out.print("현재 비밀번호를 입력해주세요: ");
    	String currentPassword = scanner.nextLine();
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
                String updatePasswordSql = "update membertable set mpassword=? where mid=?";
                try (PreparedStatement pstmt = conn.prepareStatement(updatePasswordSql)) {
                    pstmt.setString(1, newPassword);
                    pstmt.setString(2, this.mid);
                    pstmt.executeUpdate();
                    System.out.println("비밀번호가 변경되었습니다. 다시 로그인 해주세요.");
                    this.mpassword = newPassword; 	// 비밀번호 업데이트
                    // 로그아웃 후 회원 메뉴로 돌아가기
                    signout();  					// 로그아웃 메소드 호출
                    memberMenu();      				// 회원 메뉴로 돌아가기
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("비밀번호 변경 중 오류가 발생했습니다.");
            }
        } else {
            System.out.println("비밀번호가 일치하지 않습니다. 다시 시도해주세요.");
            updateMpassword();  					// 비밀번호 변경 재시도
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
	                String deleteSql = "update membertable set menabled  = 0 where mid=?";
	                try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
	                    pstmt.setString(1, this.getMid());
	                    pstmt.executeUpdate();
	                    System.out.println("회원 탈퇴가 완료되었습니다.");
	                    isLoggedIn = false; // 로그인 상태 업데이트
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
			String sql = "select mid from membertable where mname=? and mphone=?";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, mname);  
			pstmt.setString(2, mphone);  
			
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()) {                        
			    // 아이디가 있는 경우
			    System.out.println("아이디 발견 : " + rs.getString("mid"));
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
            String sql = "select mpassword from membertable where mid=? and mname=? and mphone=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, mid);  
            pstmt.setString(2, mname);
            pstmt.setString(3, mphone);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()) {                        
                // 비밀번호가 있는 경우
                System.out.println("찾은 비밀번호 : " + rs.getString("mpassword"));
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
    
    //관리자용 회원목록 보기
    public void showMemberList() {
    	if ("ROLE ADMIN".equals(getMrole())) { 			// 관리자인지 확인
            int pageSize = 10;  									// 한 페이지에 보여줄 회원 수
            int currentPage = 1; 								// 현재 페이지 초기값

            while (true) {
                System.out.println("============= 회원 목록 =============");
                System.out.println( "페이지[ " + currentPage + " ]");
                try {
	            	String sql = "" +
                        "select * from (" +
                        "  select mid, mname, mphone, maddress, msex, menabled, " +
                        "  ROW_NUMBER() over (order by mid asc) as rnum " +
                        "  from membertable " +
                        ") where rnum between ? and ?";
                	
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, (currentPage - 1) * pageSize + 1); // 시작 행 번호
                    pstmt.setInt(2, currentPage * pageSize); // 종료 행 번호
                    ResultSet rs = pstmt.executeQuery();

                    // 결과가 없을 경우 체크
                    boolean hasResult = false;
                    
                    while (rs.next()) {
                    	hasResult = true;
                        String mid = rs.getString("mid");
                        String mname = rs.getString("mname");
                        String mphone = rs.getString("mphone");
                        String maddress = rs.getString("maddress");
                        String msex = rs.getString("msex");
                        String menabled = rs.getString("menabled");
                        
                        System.out.println("아이디: " + mid);
                        System.out.println("이름: " + mname);
                        System.out.println("전화번호: " + mphone);
                        System.out.println("주소: " + maddress);
                        System.out.println("성별: " + msex);
                        System.out.println("활성화 상태: " + menabled);
                        System.out.println("---------------------------------");
                        
                    }
                    if (!hasResult) {
                        System.out.println("더 이상 회원 목록이 없습니다.");
                    }
                    rs.close();
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                System.out.println("===========================================");

                // 다음 페이지, 이전 페이지 또는 종료 선택
                System.out.println("1. 이전 페이지 | 2. 다음 페이지 |  3. 회원 비활성화  |  4. 회원 활성화  |  5. 돌아가기  |  6. 종료  ");
                System.out.print("선택: ");
                String choice = scanner.nextLine();

                switch (choice) {
                case "1":
                	// 페이지 이동
                    if (currentPage > 1) {
                        currentPage--;
                    } else {
                        System.out.println("이전 페이지가 없습니다.");
                    }
                    break;
                case "2":
                    if (currentPage * pageSize < board.getTotalPageCount()) {
                        currentPage++;
                    } else {
                        System.out.println("다음 페이지가 없습니다.");
                    }
                    break;
                case "3":
                    // 회원비활성화 시키기(관리자)
                	deactiveMember();
                    break;
                case "4":
                    // 회원 활성화 시키기(관리자)
                	activeMember();
                    break;
                case "5":
                    // 메뉴화면 돌아가기
                	board.mainMenu();
                    break;
                case "6":
                    // 종료
                    return;
                default:
                    System.out.println("잘못된 입력입니다.");
                    break;
                }
          }
       } else {
              System.out.println("회원 목록 보기를 사용할 수 있는 권한이 없습니다.");
         }
    }
    
	//회원탈퇴(비활성화)
	public void deactiveMember() {
	    System.out.print("비활성화할 회원 아이디 입력: ");
	    String memberMid = scanner.nextLine();

	    try {
	        String sql = "UPDATE membertable SET menabled = 0 WHERE mid = ?";
	        PreparedStatement pstmt = conn.prepareStatement(sql);
	        pstmt.setString(1, memberMid);
	        int rowsAffected = pstmt.executeUpdate();

	        if (rowsAffected > 0) {
	            System.out.println("회원 비활성화 성공: " + memberMid);
	        } else {
	            System.out.println("비활성화 실패: 회원을 찾을 수 없습니다.");
	        }
	        pstmt.close();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	//회원 활성화(비활성화 상태인 회원 활성화)
	public void activeMember() {
	    System.out.print("활성화할 회원 아이디 입력: ");
	    String memberMid = scanner.nextLine();

	    try {
	        String sql = "UPDATE membertable SET menabled = 1 WHERE mid = ?";
	        PreparedStatement pstmt = conn.prepareStatement(sql);
	        pstmt.setString(1, memberMid);
	        int rowsAffected = pstmt.executeUpdate();

	        if (rowsAffected > 0) {
	            System.out.println("회원 활성화 성공: " + memberMid);
	        } else {
	            System.out.println("활성화 실패: 회원을 찾을 수 없습니다.");
	        }
	        pstmt.close();
	    } catch (SQLException e) {
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
    
    //회원목록을 게시판에 나타내위해 관리자인지 확인
    public String getMrole() {
        return this.mrole;
    }
    
    public boolean isAdmin() {
        return "ROLE ADMIN".equals(this.getMrole());
    }
    
}
