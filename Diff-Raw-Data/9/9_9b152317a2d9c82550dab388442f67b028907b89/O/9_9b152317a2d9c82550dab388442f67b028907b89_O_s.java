 package com.hk.util;
 
 public class O {
 
 	private static long lastms = 0;
 	public static void o(String s)
 	{
 		long now = System.currentTimeMillis();
 		if ((now - lastms) > 3000)
			System.out.println ("\r\n\r\n"+ (new java.util.Date().toString())+ " **********************************\r\n");
 		lastms = now;
 		System.out.println (s);
 	}
 
	public static void oc(Object o)
 	{
		System.out.println ("object class hk is: " + o.getClass().getName() );
		System.out.println ("object class hk2 is: " + o.getClass().getName() );
 	}
 	
 	public static void or(String s, Throwable t)
 	{
 		O.o(s +  " [" + t.getMessage() + "]");
 		t.printStackTrace();
 	}
 }
