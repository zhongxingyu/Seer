package javapuzzlers.src;
 
 import java.math.BigDecimal;
 
 /**
  * @author: Jay
  * @date: Nov 6, 2013
  * 
  * @description: 顾客使用2美元，买了1.10美元物品，打印出应该给的找零。
  * 
  * 并不是所有的小数都能用二进制浮点数表示（如1.1就不能被精确表示为一个double），二进制浮点数对于货币计算非常不合适。
  * 在需要精确答案的地方，要避免使用float和double；对于货币计算，要使用int、long或BigDecimal.
  * 精确小数运算可以使用BigDecimal，但一定要注意用BigDecimal(String)构造器，不要使用BigDecimal(double)。
  * 
  */
 public class Change {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		ChangeA();
 		ChangeAW();
 		ChangeB();
 		ChangeC();
 		ChangeW();
 	}
 	
 	// this method can work fine. Note that it uses string constructor of BigDecimal.
 	public static void ChangeA() {
 		System.out.println(new BigDecimal("2.00")
 							.subtract(new BigDecimal("1.10")) + " dollars");
 	}
 	
 	// this method doesn't work as we expected.
 	public static void ChangeAW() {
 		System.out.println(new BigDecimal("2.00")
 		.subtract(new BigDecimal(1.10)) + " dollars");
 	}
 	
 	// this method can work fine.  It need to translate from dollars to cents.
 	public static void ChangeB() {
 		System.out.println((200 - 110) + " cents");
 	}
 	
 	// this method can work but it's not a poor solution.
 	public static void ChangeC() {
 		System.out.printf("%.2f dollars\n", 2.00-1.10);
 	}
 	
 	// this method doesn't work as we expected.	
 	public static void ChangeW() {
 		System.out.println((2.00 - 1.10) + " dollars");
 	}
 
 }
