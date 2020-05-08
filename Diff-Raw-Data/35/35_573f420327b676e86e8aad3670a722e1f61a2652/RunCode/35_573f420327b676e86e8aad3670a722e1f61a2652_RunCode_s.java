 package com.compilation;
 
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.junit.runner.JUnitCore;
 import org.junit.runner.Result;
 import org.junit.runner.notification.Failure;
 
 import com.google.gson.Gson;
 import com.junit4.Listener;
 import com.modle.Assets;
 import com.modle.ResponseModle;
 
 public class RunCode {
 	public static String load_code(String input, String rule){
 		// 1.创建需要动态编译的代码字符串
 		String nr = "\r\n"; //回车
 		return 
 				
 				" import com.compilation.MyInterface;" + nr +
 				" import com.junit4.TestDescription;" + nr +
 				
 				" import org.junit.Assert; " + nr +
 				" import org.junit.Test; " + nr +
 				" import org.junit.runner.RunWith; " + nr +
 				" import org.junit.runners.JUnit4; " + nr +
 				
 				" import java.util.Iterator;" + nr +
 				" import java.util.Set;" + nr +
 				" import org.junit.runner.JUnitCore;" + nr +
 				" import org.junit.runner.Result;" + nr +
 				
 				" @RunWith(JUnit4.class) " + nr +
 				" public class  InputTest implements MyInterface{" + nr + 
 					" public void sayHello(){System.out.println(666);}" + nr +
 					 rule + nr +
 				" }" + nr +
 				
 				" class RuleTest{" + nr + 
 					input + nr +
 				" }";
 	}
 	
 	public static String run(String fullClassName,String error){
 		System.out.println("=================2=====================");
 		JUnitCore core = new org.junit.runner.JUnitCore();
 		core.addListener(new Listener());
 		int i = 0;
 		List<Assets> assets_list = new ArrayList<Assets>();
 		try {
			Class clz = new MyClassLoader().loadClass(fullClassName);
 			MyInterface myObj = (MyInterface) clz.newInstance();
 //			myObj.sayHello();
 			Result result = core.run(myObj.getClass());
 			
 			Set<String> set = Listener.test_map.keySet();
 			Iterator<String> it = set.iterator();
 			
 			while(it.hasNext()){
 				String name = it.next();
 
 				String doc = Listener.test_map_doc.get(name) == null ?  "":Listener.test_map_doc.get(name);
 				
 				boolean is_success = Listener.test_map.get(name);
 				Failure failure = Listener.test_map_error.get(name);
 				String exception = "";
 				if (!is_success) {
 					exception = failure.getException().getClass() == AssertionError.class ? "":failure.getException().toString();
 				}
 				
 				i += is_success ?  0:1;
 				
 				Assets assets = new Assets(doc, is_success, exception);
 				assets_list.add(assets);
 			}
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 			i += 1;
 			error += e.getMessage();
 			System.out.println("=================3=====================");
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 			i += 1;
 			error += e.getMessage();
 			System.out.println("=================4=====================");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			i += 1;
			error += e.getMessage();
			System.out.println("=================5=====================");
 		}
 		System.out.println("=================6=====================");
 		boolean success = i==0;
 		ResponseModle responseModle = new ResponseModle(error, success, assets_list);
 		Gson gson = new Gson();
 		String responseModle_json = gson.toJson(responseModle);
 		System.out.println(responseModle_json);
 		return responseModle_json;
 	}
 	
 	
 	public static String thread(long threadId,String input, String rule){
 		String fullClassName = "InputTest";
 		String complilation_error = "";
 		try {
 			new MyClassCompiler(threadId, fullClassName, RunCode.load_code(input, rule)).compile();
 		} catch (URISyntaxException e) {
 			System.out.println("=================1=====================");
 			e.printStackTrace();
 			complilation_error = e.getMessage();
 		}
 		
 		return RunCode.run(fullClassName,complilation_error);
 	}
 	
 }
