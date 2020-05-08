 package com.codecanaan;
 
 import groovy.lang.Binding;
 import groovy.lang.GroovyShell;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 
 import java.security.Key;
 import javax.crypto.Cipher;
 import javax.crypto.spec.SecretKeySpec;
 import org.apache.commons.codec.binary.Hex;
 
 public class ScriptLoader
 {
 	public ScriptLoader() {
 	}
 
 	public static void main(String args[])
 	{
 		System.setSecurityManager(new UnsafeSecurityManager());
 
 		ScriptLoader loader = new ScriptLoader();
 		
 		try {
			if ("groovy".equals(System.getProperty("javaws.core.script.type"))) {
				loader.loadGroovy(System.getProperty("javaws.core.script.url"));
 			}
 			else {
 				loader.load(
					System.getProperty("javaws.core.script.type"),
					System.getProperty("javaws.core.script.url")
 				);
 			}
 		}
 		catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		
 		//System.exit(0);
 	}
 
 	public void load(String scriptType, String scriptURL) throws Exception {
 	    //從網址取得 Scripting 資料
 		URL url = new URL(scriptURL);
 		URLConnection conn = url.openConnection();
 		BufferedReader reader = new BufferedReader(
 			new InputStreamReader(conn.getInputStream(), "UTF-8")
 		);
 
         //讀取成字串
 		StringBuilder builder = new StringBuilder();
 		String line;
         while ((line = reader.readLine()) != null) {
             builder.append(line);
         }
 		reader.close();
         String script = builder.toString();
 
         //使用 AES 演算法解密
 		Key key = new SecretKeySpec("thebestsecretkey".getBytes(), "AES");
         Cipher c = Cipher.getInstance("AES");
         c.init(Cipher.DECRYPT_MODE, key);
         byte[] decValue = c.doFinal(Hex.decodeHex(script.toCharArray()));
         script = new String(decValue, "UTF-8");
 
 		ScriptEngineManager factory = new ScriptEngineManager();
 		ScriptEngine engine = factory.getEngineByName(scriptType);
 		engine.put("loader", this);
 		engine.put("engine", engine);
 		engine.eval(script);
 	}
 
 	private GroovyShell shell = null;
 
 	public void loadGroovy(String scriptURL) throws Exception {
 		System.out.println("Load Groovy " + scriptURL);
 
 		URL url = new URL(scriptURL);
 		URLConnection conn = url.openConnection();
 		BufferedReader reader = new BufferedReader(
 			new InputStreamReader(conn.getInputStream(), "UTF-8")
 		);
 		
 		//讀取成字串
 		StringBuilder builder = new StringBuilder();
 		String line;
         while ((line = reader.readLine()) != null) {
             builder.append(line);
         }
         reader.close();
         String script = builder.toString();
         
         //使用 AES 演算法解密
 		Key key = new SecretKeySpec("thebestsecretkey".getBytes(), "AES");
         Cipher c = Cipher.getInstance("AES");
         c.init(Cipher.DECRYPT_MODE, key);
         byte[] decValue = c.doFinal(Hex.decodeHex(script.toCharArray()));
         script = new String(decValue, "UTF-8");
 
 		if (shell==null) {
 			Binding binding = new Binding();
 			binding.setVariable("loader", this);
 			shell = new GroovyShell(binding);
 			binding.setVariable("shell", shell);
 		}
 		shell.evaluate(script);
 	}
 }
