 package org.zezutom;
 
 /**
  * Hello world!
  *
  */
 public class App 
 {
     public String sayHello(String name) 
     {
	return "Hello " + name + ":-)";
     }
 
     public static void main( String[] args )
     {
 	final String name = (args.length == 0) ? "everyone" : args[0];
         System.out.println(new App().sayHello(name));
     }
 }
