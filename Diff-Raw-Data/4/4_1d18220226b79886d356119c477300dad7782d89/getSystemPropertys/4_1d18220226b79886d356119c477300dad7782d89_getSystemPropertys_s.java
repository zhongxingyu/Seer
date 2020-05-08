 package me.Crosant.System_Tools;
 
 public class getSystemPropertys {
 
 	public static void SystemPropertys() throws Exception
     {
 		
 		String Java_Version = System.getProperty("java.version");
 		String SystemOS = System.getProperty("os.name");
     	String SystemType = System.getProperty("os.arch");
     	String SystemVersion = System.getProperty("os.version");
     	String UserName = System.getProperty("user.name");
     	String UserDir = System.getProperty("user.home");
     	System.out.println("/*************************************************************\\");
     	Thread.sleep(500);
     	System.out.println("|                                                             |");
     	Thread.sleep(500);
     	System.out.println("|    Java Version:    "+ Java_Version + "                                |");
     	Thread.sleep(500);
     	System.out.println("|                                                             |");
     	Thread.sleep(500);
     	System.out.println("|    Operating System: "+ SystemOS + " " + SystemType + " Version: " + SystemVersion + "             |");
     	Thread.sleep(500);
     	System.out.println("|                                                             |");
     	Thread.sleep(500);
     	System.out.println("|    User: " + UserName + "                                            |");
     	Thread.sleep(500);
     	System.out.println("|                                                             |");
     	Thread.sleep(500);
     	System.out.println("|    Home: " + UserDir +"                                   |");
     	Thread.sleep(500);
     	System.out.println("|                                                             |");
     	Thread.sleep(500);
     	System.out.println(" ************************************************************* ");
 
     	
     }
 	
 }
