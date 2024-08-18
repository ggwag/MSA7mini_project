package mini_gwag;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

class Board implements Serializable {
	private static final long serialVersionUID = 1L;
	private static int next_id = 1;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private int bno;			// 게시물번호
	private String btitle;	// 제목
	private String bcontent;	// 내용
	private Date bdate;	// 작성일시

	private static int getNextId() {
		return next_id++;
	}

	public static void setNextId(int next_id) {
		Board.next_id = next_id;
	}

	public Board(String btitle, String bcontent) {
		this.bno = getNextId();
		this.btitle = btitle;
		this.bcontent = bcontent;
		this.bdate = new Date();
	}

	public int getBno() {
		return bno;
	}

	public void setBno(int bno) {
		this.bno = bno;
	}

	public String getBtitle() {
		return btitle;
	}

	public void setBtitle(String btitle) {
		this.btitle = btitle;
	}

	public String getBcontent() {
		return bcontent;
	}

	public void setBcontent(String bcontent) {
		this.bcontent = bcontent;
	}

	public Date getBdate() {
		return bdate;
	}

	public void setBdate(Date bdate) {
		this.bdate = bdate;
	}

	public void print() {
		System.out.printf("%4d|%-27s|%s\n", bno, btitle, sdf.format(bdate));
	}

	public void detailView() {
		System.out.println("게시물 번호 : " + bno);
		System.out.println("게시물 제목 : " + btitle);
		System.out.println("게시물 내용 : " + bcontent);
		System.out.println("작성일시 : " + sdf.format(bdate));
		System.out.println();
	}

}
