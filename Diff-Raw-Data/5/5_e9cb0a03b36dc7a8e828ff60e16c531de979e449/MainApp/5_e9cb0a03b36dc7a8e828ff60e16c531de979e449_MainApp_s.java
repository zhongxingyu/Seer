 package ru.artyomkomarov;
 
 import java.io.File;
 
 public class MainApp {
 	static long ans = 0;
 
     public static void main(String[] args) {
    	args = new String[2];
     	args[0] = "C:\\Users\\1\\Desktop\\";
    	args[1] = "false";
     	String name = "";
     	String mark = "false";
     	for(int i = 0; i < args.length - 1; i++) {
     		if(i == 0)name = args[i];
     		else name = name + " " + args[i]; 
     	}
     	mark = args[args.length - 1];
     	long startTime = System.currentTimeMillis();
     	if(mark.equals("true")) {
     		ThreadClass th = new ThreadClass(args[0]);
     		Thread newTh = new Thread(th);
     		newTh.start();
     		try {
     			newTh.join();
     		} catch (Exception e) {
     			e.printStackTrace();
     		}
     		System.out.println(th.res);
     	} else {
     		File[] files = new File(name).listFiles();
     		for(int i = 0; i < files.length; i++) {
     			if(files[i].isFile())ans += files[i].length();
     		}
     		System.out.println(ans);
     	}
     	long finishTime = System.currentTimeMillis();
     	System.out.println("Time mills: " + (finishTime - startTime));
     }
 }
