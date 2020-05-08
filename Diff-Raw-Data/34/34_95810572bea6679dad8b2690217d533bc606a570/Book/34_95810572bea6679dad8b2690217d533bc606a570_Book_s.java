 package pppp;
 
 import com.example.base.BaseClass;
 import aaa;
 
 /**
  * book 【書籍マスタ】
  * テーブルの説明<br>
  * あいうえお<br>
  * かきくけこ<br>
  * 改行込みだよ
  *
  * @author test
  */
 public class Book extends BaseClass {
 /* SQL
SELECT book_id,book_name,price,book_type,want_flag,length9,length10,numeric41,insert_datetime,update_datetime
 FROM book;
 
INSERT INTO book(book_id,book_name,price,book_type,want_flag,length9,length10,numeric41,insert_datetime,update_datetime)
VALUES(?,?,?,?,?,?,?,?,?,?);
 
 UPDATE book
SET book_id=?,book_name=?,price=?,book_type=?,want_flag=?,length9=?,length10=?,numeric41=?,insert_datetime=?,update_datetime=?;
 */
 
 	private static final long serialVersionUID = 1L;
 
 	private Integer bookId;
 	private String bookName;
 	private Integer price;
 	private String bookType;
 	private Integer wantFlag;
 	private Integer length9;
 	private Long length10;
 	private BigDecimal numeric41;
 	private Timestamp insertDatetime;
 	private Timestamp updateDatetime;
 
 	/**
 	 * book_id 【書籍ID】の取得<br>
 	 * 【型】 INTEGER
 	 * 
 	 * @return bookId 【書籍ID】
 	 */
 	public Integer getBookId() {
 		return bookId;
 	}
 
 	/**
 	 * book_id 【書籍ID】のセット<br>
 	 * 【型】 INTEGER
 	 * 
 	 * @param bookId
 	 *             【書籍ID】
 	 */
 	public void setBookId(Integer bookId) {
 		this.bookId = bookId;
 	}
 
 	/**
 	 * book_name 【書名】の取得<br>
 	 * 【型】 VARCHAR(80)
 	 * 
 	 * @return bookName 【書名】
 	 */
 	public String getBookName() {
 		return bookName;
 	}
 
 	/**
 	 * book_name 【書名】のセット<br>
 	 * 【型】 VARCHAR(80)
 	 * 
 	 * @param bookName
 	 *             【書名】
 	 */
 	public void setBookName(String bookName) {
 		this.bookName = bookName;
 	}
 
 	/**
 	 * price 【価格】の取得<br>
 	 * 【型】 INTEGER
 	 * 
 	 * @return price 【価格】
 	 */
 	public Integer getPrice() {
 		return price;
 	}
 
 	/**
 	 * price 【価格】のセット<br>
 	 * 【型】 INTEGER
 	 * 
 	 * @param price
 	 *             【価格】
 	 */
 	public void setPrice(Integer price) {
 		this.price = price;
 	}
 
 	/**
 	 * book_type 【書籍種別】の取得<br>
 	 * 【型】 CHAR(1)
 	 *
 	 * 1: 小説<br>
 	 * 2: コミック<br>
 	 * 3: 実用書
 	 * 
 	 * @return bookType 【書籍種別】
 	 */
 	public String getBookType() {
 		return bookType;
 	}
 
 	/**
 	 * book_type 【書籍種別】のセット<br>
 	 * 【型】 CHAR(1)
 	 *
 	 * 1: 小説<br>
 	 * 2: コミック<br>
 	 * 3: 実用書
 	 * 
 	 * @param bookType
 	 *             【書籍種別】
 	 */
 	public void setBookType(String bookType) {
 		this.bookType = bookType;
 	}
 
 	/**
 	 * want_flag 【購入予定フラグ】の取得<br>
 	 * 【型】 NUMERIC(1)
 	 *
 	 * 1: 購入予定<br>
 	 * 0: 購入予定なし
 	 * 
 	 * @return wantFlag 【購入予定フラグ】
 	 */
 	public Integer getWantFlag() {
 		return wantFlag;
 	}
 
 	/**
 	 * want_flag 【購入予定フラグ】のセット<br>
 	 * 【型】 NUMERIC(1)
 	 *
 	 * 1: 購入予定<br>
 	 * 0: 購入予定なし
 	 * 
 	 * @param wantFlag
 	 *             【購入予定フラグ】
 	 */
 	public void setWantFlag(Integer wantFlag) {
 		this.wantFlag = wantFlag;
 	}
 
 	/**
 	 * length9 【長さ9】の取得<br>
 	 * 【型】 NUMERIC(9)
 	 * 
 	 * @return length9 【長さ9】
 	 */
 	public Integer getLength9() {
 		return length9;
 	}
 
 	/**
 	 * length9 【長さ9】のセット<br>
 	 * 【型】 NUMERIC(9)
 	 * 
 	 * @param length9
 	 *             【長さ9】
 	 */
 	public void setLength9(Integer length9) {
 		this.length9 = length9;
 	}
 
 	/**
 	 * length10 【長さ10】の取得<br>
 	 * 【型】 NUMERIC(10)
 	 * 
 	 * @return length10 【長さ10】
 	 */
 	public Long getLength10() {
 		return length10;
 	}
 
 	/**
 	 * length10 【長さ10】のセット<br>
 	 * 【型】 NUMERIC(10)
 	 * 
 	 * @param length10
 	 *             【長さ10】
 	 */
 	public void setLength10(Long length10) {
 		this.length10 = length10;
 	}
 
 	/**
 	 * numeric41 【小数】の取得<br>
 	 * 【型】 NUMERIC(4,1)
 	 * 
 	 * @return numeric41 【小数】
 	 */
 	public BigDecimal getNumeric41() {
 		return numeric41;
 	}
 
 	/**
 	 * numeric41 【小数】のセット<br>
 	 * 【型】 NUMERIC(4,1)
 	 * 
 	 * @param numeric41
 	 *             【小数】
 	 */
 	public void setNumeric41(BigDecimal numeric41) {
 		this.numeric41 = numeric41;
 	}
 
 	/**
 	 * insert_datetime 【登録日時】の取得<br>
 	 * 【型】 TIMESTAMP WITH TIME ZONE
 	 * 
 	 * @return insertDatetime 【登録日時】
 	 */
 	public Timestamp getInsertDatetime() {
 		return insertDatetime;
 	}
 
 	/**
 	 * insert_datetime 【登録日時】のセット<br>
 	 * 【型】 TIMESTAMP WITH TIME ZONE
 	 * 
 	 * @param insertDatetime
 	 *             【登録日時】
 	 */
 	public void setInsertDatetime(Timestamp insertDatetime) {
 		this.insertDatetime = insertDatetime;
 	}
 
 	/**
 	 * update_datetime 【更新登録日時】の取得<br>
 	 * 【型】 TIMESTAMP
 	 * 
 	 * @return updateDatetime 【更新登録日時】
 	 */
 	public Timestamp getUpdateDatetime() {
 		return updateDatetime;
 	}
 
 	/**
 	 * update_datetime 【更新登録日時】のセット<br>
 	 * 【型】 TIMESTAMP
 	 * 
 	 * @param updateDatetime
 	 *             【更新登録日時】
 	 */
 	public void setUpdateDatetime(Timestamp updateDatetime) {
 		this.updateDatetime = updateDatetime;
 	}
 
 }
