 package mypack;
 
 import mylibs.Calculator;
 
 public class MainClass {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		System.out.println("this is just a test");
		int res=0,resm=0, x = 10, y = 5;
 		res = Calculator.sub(x, y);
		resm = Calculator.mult(x,y);
 		System.out.println("result is: "+res);
		System.out.println("result is: "+resm);
 	}
 
 }
