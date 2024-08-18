package mini_gwag;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class BoardProject {
	private static List<Board> list = new ArrayList<>();
	static Scanner scanner = new Scanner(System.in);
	private Connection conn;
	
	//Constructor
	public BoardProject() {
		try {
			//JDBC Driver 등록
			Class.forName("oracle.jdbc.OracleDriver");
			
			//연결하기
			conn = DriverManager.getConnection(
				"jdbc:oracle:thin:@localhost:1521/orcl", 
				"java", 
				"oracle"
			);
		} catch(Exception e) {
			e.printStackTrace();
			exit();
		}
	}
	// 게시물 목록
	public static void list() {
		System.out.println("목록");
		System.out.println("번호|       제목                     | 작성일시");
//		      for (Board board : list) {
//		         board.print();
//		      }
//		      list.stream().forEach(board -> board.print());
		list.stream().forEach(Board::print);
	}

	// 게시물 번호 선택 보기
	public static void view() {
		System.out.print("상세보기를 원하는 게시물번호?");
		String strNo = scanner.nextLine();
		try {
			final int no = Integer.parseInt(strNo);
//		         for (Boardz board : list) {
//		            if (board.getNo() == no) {
//		               //게시물 상세 정보 출력 
//		               detailView(board);
//		               return;
//		            }
//		         }
//		         System.out.println("게시물 번호가 존재하지 않습니다");

		list.stream().filter(board -> board.getBno() == no)
					 .findFirst().ifPresentOrElse(board -> detailView(board),
					 () -> System.out.println("게시물 번호가 존재하지 않습니다"));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// 게시물 내용 상세보기
	public static void detailView(Board board) {
		System.out.println("게시물 상세보기");
		board.detailView();

		while (true) {
			System.out.println("1. 삭제 ");
			System.out.println("2. 수정");
			System.out.println("3. 이전메뉴로");
			System.out.println("원하는 메뉴 ?");
			String menuNo = scanner.nextLine();
			switch (menuNo) {
			case "1":
				// 삭제
				delete(board);
				return;
			case "2":
				// 수정
//						System.out.println("수정작업 구현중..");
				try {
					update(board);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case "3":
				// 이전으로
				return;
			default:
				System.out.println("메뉴를 잘못 입력하셨습니다");
				break;
			}
		}

	}

	// 게시물 삭제
	public static void delete(Board board) {
		list.remove(board);
		try {
			saveData();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 게시물 생성
	public static void insert(String btitle, String bcontent) throws Exception {
		list.add(new Board(btitle, bcontent));
		saveData();
		System.out.println("신규 게시물이 등록되었습니다.");
	}

	// 게시물 생성 폼
	public static void insertForm() throws Exception {
		System.out.println("등록화면");
		String title;
		String content;
		String menu;
		System.out.print("제목 : ");
		title = scanner.nextLine();
		System.out.print("내용 : ");
		content = scanner.nextLine();

		while (true) {
			System.out.println("1. 저장 ");
			System.out.println("2. 취소하고 이전메뉴로");
			System.out.println("원하는 메뉴 ?");
			String menuNo = scanner.nextLine();
			switch (menuNo) {
			case "1":
				// 등록
				insert(title, content);
				return;
			case "2":
				// 이전으로
				return;
			default:
				System.out.println("메뉴를 잘못 입력하셨습니다");
				break;
			}
		}
	}

	// 등록된 게시물 DB 경로와 데이터 가져오기
	public static void loadData() throws Exception {
		String path = BoardProject.class.getResource("board.db").getPath();
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
		list = (List<Board>) in.readObject();
		in.close();
	}

	// 게시물 DB에 입력내용 저장
	public static void saveData() throws Exception {
		String path = BoardProject.class.getResource("board.db").getPath();
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path));
		out.writeObject(list);
		out.close();
	}
	
	// 단편적인 게시물 수정 -> 현재 프로그램이 실행되어 있을 때만 수정한 내용이 보이고, 다시 실행한 경우 원래의 DB에 저장된 값의 목록을 나타냄
	public static void update(Board board) {
		System.out.println("제목: ");
		board.setBtitle(scanner.nextLine());
		System.out.println("내용: ");
		board.setBcontent(scanner.nextLine());
	}
	//게시물 나가기
	public void exit() {
		if(conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
			}
		}
		System.out.println("** 게시판 종료 **");
		System.exit(0);
		
	}
	
	// DB 파일 로딩 (rollback, load)
	public static void rollback() throws Exception {
		String path = BoardProject.class.getResource("board.db").getPath();
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
		list = (List<Board>) in.readObject();
	}
	
	// DB 파일 저장 (commit, save)
	public static void commit() {
		try {
			saveData();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 마지막으로 작성된 게시물 Id값 얻기
	public static int getLastBoardId() {
		return list.get(list.size() - 1).getBno();
	}

	// 실행문
	public static void main(String[] args) throws Exception {
		// 파일에서 게시물 목록을 읽는다
		loadData();
		if (list.size() > 0) {
			Board.setNextId(getLastBoardId() + 1);
		}

		while (true) {
			System.out.println("1. 목록");
			System.out.println("2. 상세보기");
			System.out.println("3. 등록");
			System.out.println("4. DB파일로딩(rollback, load)");
			System.out.println("5. DB파일저장(commit, save)");
			System.out.println("6. 종료");
			String cmd = scanner.nextLine();
			switch (cmd) {
			case "6":
				// 다시 한번 저장할 것
				// 517 페이지
				System.out.println("프로그램 종료");
				System.exit(0);
				break;
			case "1":
				// 목록으로 이동하여 작업 할 것
				list();
				break;
			case "2":
				// 상세보기 이동하여 작업 할 것
				view();
				break;
			case "3":
				// 등록화면으로 이동 작업 할 것
				insertForm();
				break;
			case "4":
				rollback();
				break;
			case "5":
			commit();
			break;
		}
	}
				
				
	}

}
