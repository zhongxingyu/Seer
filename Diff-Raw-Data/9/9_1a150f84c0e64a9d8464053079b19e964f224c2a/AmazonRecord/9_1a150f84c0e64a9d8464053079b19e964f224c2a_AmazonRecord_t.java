 package amazonEmotion;
 
 
 import org.apache.hadoop.io.Text;
 
 public class AmazonRecord {
 
 	//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 
 	String ID1;
 	String ID2;
 	String date;
 	int num1;
 	int num2;
 	double score;
 	String title;
 	String review;
 
 	public AmazonRecord(String inputString) throws IllegalArgumentException {
 		// CSV header (parsing the inputString is based on this):
 		// exchange, stock_symbol, date, stock_price_open, stock_price_high, stock_price_low,
 		// 		stock_price_close, stock_volume, stock_price_adj_close
		String[] attributes = inputString.split("\t", 8);
 
 		if (attributes.length != 8)
 			throw new IllegalArgumentException(String.format("Input string given did not have 8 values in CSV format, contained %i values", attributes.length));
 
 		try {
 			setID1(attributes[0]);
 			setID2(attributes[1]);
 			setDate(attributes[2]);
 			setNum1(Integer.parseInt(attributes[3]));
 			setNum2(Integer.parseInt(attributes[4]));
 			setScore(Double.parseDouble(attributes[5]));
 			setTitle(attributes[6]);
 			setReview(attributes[7]);
 		} catch (NumberFormatException e) {
 			throw new IllegalArgumentException("Input string contained an unknown number value that couldn't be parsed", e);
 		}
 	}
 
 	public AmazonRecord(Text inputText) throws IllegalArgumentException {
 		this(inputText.toString());
 	}
 
 	public String getID1() {
 		return ID1;
 	}
 
 	public void setID1(String id1) {
 		this.ID1 = id1;
 	}
 
 	public String getID2() {
 		return ID2;
 	}
 
 	public void setID2(String id2) {
 		this.ID2 = id2;
 	}
 
 	public String getDate() {
 		return date;
 	}
 
 	public void setDate(String date) {
 		this.date = date;
 	}
 
 	public double getNum1() {
 		return num1;
 	}
 
 	public void setNum1(int n1) {
 		this.num1 = n1;
 	}
 
 	public double getNum2() {
 		return num2;
 	}
 
 	public void setNum2(int n2) {
 		this.num2 = n2;
 	}
 
 	public double getScore() {
 		return score;
 	}
 
 	public void setScore(double scr) {
 		this.score = scr;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String t) {
 		this.title = t;
 	}
 
 	public String getReview() {
 		return review;
 	}
 
 	public void setReview(String r) {
 		this.review = r;
 	}
 
 	
 }
