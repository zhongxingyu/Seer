 package ch07.ex02;
 
 public class Literal {
 	public static boolean mBoolean = true;
 	public static char mChar = 'A';
 	public static byte mMaxByte = Byte.MAX_VALUE;
 	public static byte mMinByte = Byte.MIN_VALUE;
 	public static short mMaxShort = Short.MAX_VALUE;
 	public static short mMinShort = Short.MIN_VALUE;
 	public static int mMaxInt = Integer.MAX_VALUE;
 	public static int mMinInt = Integer.MIN_VALUE;
 	public static long mMaxLong = Long.MAX_VALUE;
 	public static long mMinLong = Long.MIN_VALUE;
 	public static float mMaxFloat = Float.MAX_VALUE;
 	public static float mMinFloat = Float.MIN_VALUE;
 	public static double mMaxDouble = Double.MAX_VALUE;
 	public static double mMinDouble = Double.MIN_VALUE;
 	
 	public static void main(String[]args) {
 		// ftHgl̏o
 		System.out.println(mBoolean);
 		System.out.println(mChar);
 		System.out.println("MaxByte: " + mMaxByte);
 		System.out.println("MinByte: " + mMinByte);
 		System.out.println("MaxShort: " + mMaxShort);
 		System.out.println("MinShort: " + mMinShort);
 		System.out.println("MaxInt: " + mMaxInt);
 		System.out.println("MinInt: " + mMinInt);
 		System.out.println("MaxLong: " + mMaxLong);
 		System.out.println("MinLong: " + mMinLong);
 		System.out.println("MaxFloat: " + mMaxFloat);
 		System.out.println("MinFloat: " + mMinFloat);
 		System.out.println("MaxDouble: " + mMaxDouble);
 		System.out.println("MinDouble: " + mMinDouble);
 		
 		// Int^ɕϊo
 		int aInt = 0;
 		System.out.println(aInt = (int)mMaxByte);
 		System.out.println(aInt = (int)mMinByte);
 		System.out.println(aInt = (int)mMaxShort);
 		System.out.println(aInt = (int)mMinShort);
 		System.out.println(aInt = (int)mMaxInt);
 		System.out.println(aInt = (int)mMinInt);
 		System.out.println(aInt = (int)mMaxLong); //NG -1
 		System.out.println(aInt = (int)mMinLong); //NG 0
 		System.out.println(aInt = (int)mMaxFloat); //NG 2147483647
 		System.out.println(aInt = (int)mMinFloat); //NG 0
 		System.out.println(aInt = (int)mMaxDouble); //NG 2147483647
 		System.out.println(aInt = (int)mMinDouble); //NG 0
 		
 		// Long^ɕϊo
 		long aLong = 0;
 		System.out.println(aLong = (long)mMaxByte);
 		System.out.println(aLong = (long)mMinByte);
 		System.out.println(aLong = (long)mMaxShort);
 		System.out.println(aLong = (long)mMinShort);
 		System.out.println(aLong = (long)mMaxInt);
 		System.out.println(aLong = (long)mMinInt);
 		System.out.println(aLong = (long)mMaxLong);
 		System.out.println(aLong = (long)mMinLong);
 		System.out.println(aLong = (long)mMaxFloat); //NG 9223372036854775807
 		System.out.println(aLong = (long)mMinFloat); //NG 0
 		System.out.println(aLong = (long)mMaxDouble); //NG 9223372036854775807
 		System.out.println(aLong = (long)mMinDouble); //NG 0
 		
 		// Float^ɕϊo
 		float aFloat = 0;
 		System.out.println(aFloat = (float)mMaxByte);
 		System.out.println(aFloat = (float)mMinByte);
 		System.out.println(aFloat = (float)mMaxShort);
 		System.out.println(aFloat = (float)mMinShort);
 		System.out.println(aFloat = (float)mMaxInt);
 		System.out.println(aFloat = (float)mMinInt);
 		System.out.println(aFloat = (float)mMaxLong);
 		System.out.println(aFloat = (float)mMinLong);
 		System.out.println(aFloat = (float)mMaxFloat);
 		System.out.println(aFloat = (float)mMinFloat);
 		System.out.println(aFloat = (float)mMaxDouble); //NG Infinity
 		System.out.println(aFloat = (float)mMinDouble); //NG 0.0
 		
 		// Double^ɕϊo
 		double aDouble = 0;
 		System.out.println(aDouble = (double)mMaxByte);
 		System.out.println(aDouble = (double)mMinByte);
 		System.out.println(aDouble = (double)mMaxShort);
 		System.out.println(aDouble = (double)mMinShort);
 		System.out.println(aDouble = (double)mMaxInt);
 		System.out.println(aDouble = (double)mMinInt);
 		System.out.println(aDouble = (double)mMaxLong);
 		System.out.println(aDouble = (double)mMinLong);
 		System.out.println(aDouble = (double)mMaxFloat);
 		System.out.println(aDouble = (double)mMinFloat);
		System.out.println(aDouble = (double)mMaxDouble); //NG Infinity
		System.out.println(aDouble = (double)mMinDouble); //NG 0.0
 	}
 }
