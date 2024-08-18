package mini_gwag;

import java.util.Date;

import lombok.Data;

@Data
public class Board {
	private int bno;			// 게시물번호
	private String btitle;	// 제목
	private String bcontent;	// 내용
	private String bwriter; 	// 글쓴이
	private Date bdate;	// 작성일시


	public void print() {
		System.out.printf("%4d|%-27s|%s\n", bno, btitle,bdate);
	}

	public void detailView() {
		System.out.println("게시물 번호 : " + bno);
		System.out.println("게시물 제목 : " + btitle);
		System.out.println("게시물 내용 : " + bcontent);
		System.out.println("작성일시 : " + bdate);
		System.out.println();
	}


}
