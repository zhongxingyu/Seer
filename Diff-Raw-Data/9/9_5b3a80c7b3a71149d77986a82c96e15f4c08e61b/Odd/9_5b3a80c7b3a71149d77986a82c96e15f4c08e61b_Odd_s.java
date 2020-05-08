package javapuzzlers.src;
 
 /*
  * @author: Jay
  * @date: Nov 5, 2013
  * @description: check an integer is an odd number or not.	
  * 
  * Java中取余操作符(%)，对于任意的int数值a和非零的int数值b，都满足恒等式： (a / b) * b + (a % b) == a 
  * 该恒等式具有正确的含义，但是当与Java的截尾整数整除操作符相结合时，就有：
  * 		当取余操作返回一个非零的结果时，它与左操作数具有相同的正负符号。
  */
 
 public class Odd {
 	
 	//  this can work.
 	public static boolean isOddA(int i) {
 		return (i % 2) != 0;
 	}
 	
 	//	this one is more effective.
 	public static boolean isOddB(int i) {
 		return (i & 1) != 0;
 	}
 	
 	//	this method is not right.
 	public static boolean isOddW(int i) {
 		return (i %2) == 1;
 	}
 }
