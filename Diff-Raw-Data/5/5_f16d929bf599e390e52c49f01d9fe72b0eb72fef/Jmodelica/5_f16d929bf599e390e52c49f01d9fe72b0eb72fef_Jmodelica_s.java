 package com.sri.straylight.socketserver;
 
 import java.net.URL;
 import java.util.List;
 import java.util.Properties;
 
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineFactory;
 import javax.script.ScriptEngineManager;
 import javax.script.ScriptException;
 
 import org.python.util.*;
 import org.python.core.*;
 import java.lang.ClassLoader;
 
 import java.lang.Runtime;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.InputStream;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.File;
 
 
 public class Jmodelica {
 	private static final String fmuFile = "testFMI.fmu";
 	
 	
 	
 	
 	public String run() {
 	
 		
 			String theOs = System.getProperty("os.name");
 			
 			String cmd;
 			String arg;
 			String directory;
 			
 			if (theOs.matches("Windows 7")) {
 				
 				cmd = "C:\\python\\Python2.7\\python.exe";
 				arg = "C:\\ProgramFiles\\JModelica.org-1.6\\test_bouncingBall.py";
 				directory = "C:\\ProgramFiles\\JModelica.org-1.6";
 				
 			} else {
 				
				cmd = "/usr/bin/python";
 				arg = "/opt/packages/jmodelica/jmodelica-install/test_bouncingBall.py";
 				directory = "/opt/packages/jmodelica/jmodelica-install";
 				
 			}
 			
		
 			ProcessBuilder pb = new ProcessBuilder(cmd, arg);
 			
 		    File workingDir = new File(directory);
 		    pb.directory(workingDir);
 		    
 		    String result = "no result";
 		    
 			try {
 			    Process p = pb.start(); 
 			    File dir = pb.directory();
 
 			    
 				try {
 			        int exitValue = p.waitFor();
 			        
 			        
 			        BufferedReader reader;
 					
 			        if (exitValue == 0) {
 			                reader = new BufferedReader( new InputStreamReader(p.getInputStream()) );
 			        } else {
 			                reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
 			        }
 			        
 			        
 			        StringBuffer sb = new StringBuffer();
 			        String temp = reader.readLine();
 			        while (temp != null) {
 			                sb.append(temp + "<br>\n");
 			                System.out.println(temp);
 			                temp = reader.readLine();
 			        }
 
 			        reader.close();
 			        result = sb.toString();
 					
 				}
 					catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			    
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			
 	
 	        return result;
 	}
 	
 	
 	
 	public void test3() {
 		
 		
 		//Runtime.getRuntime().exec(String command, String[] enviroment, File workingdir) 
 		
 		String s;
 		byte buff[] = new byte[1024];
 
 		try {
 			
 			String[] command = new String[] {"cmd.exe", "/c", "C:\\ProgramFiles\\JModelica.org-1.6\\ipconfig1.bat"};
 			Runtime rt = Runtime.getRuntime();
 			Process pr = rt.exec(command);
 			
 			
 			try {
 				pr.waitFor();
 				System.out.println(pr.exitValue());
 				
 				OutputStream os = pr.getOutputStream();
 				InputStream is = pr.getInputStream();
 				
 				System.out.println(os.toString()); 
 				
 				//os.
 				
 			   // String line;
 			    
 			   // while ((line = is.) != null)
 			     // System.out.println(line);
 				
 			}
 				catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 
 
 			
 			
 			
 			
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		
 		
 		//try {
 			//Runtime rt = Runtime.getRuntime();
 			//rt.exec("cd c:\\ProgramFiles\\JModelica.org-1.6");
 			//rt.exec("cmd /c c:\\ProgramFiles\\JModelica.org-1.6\\test_fmu.bat");
 			//try {
 				//Runtime.getRuntime().exec("c:\\ProgramFiles\\JModelica.org-1.6\\test_fmu.bat",null,"c:\\ProgramFiles\\JModelica.org-1.6");
 				//ProcessBuilder pb = new ProcessBuilder("cmd /c test_fmu.bat");
 				//pb.directory(new File("c:\\ProgramFiles\\JModelica.org-1.6"));
 				//Process p = pb.start();
 				//int exitStatus = rt.wait();
 				
 			//}
 			//catch (InterruptedException e) {
 			//	e.printStackTrace();
 			//}
 			
 		//} catch (IOException  e) {
 			// Error was detected, close the ChatWebSocket client side
 			//e.printStackTrace();
 			
 		//}
 		
 		
 	
 	}
 	
 	
 	
 	
 	//public void testJep() {
 		
 	//	try {
 		//	ClassLoader cl = this.getClass().getClassLoader();
 			
 			//Jep objJep = new Jep(true, "C:", cl);
 			
 /*
 			objJep.eval("print 'test 1'");
 			objJep.eval("print 'test 2'");
 			
 			objJep.eval("import sys");
 			objJep.eval("print sys.version_info");
 			*/
 			
 			//objJep.runScript("C:\\ProgramFiles\\JModelica.org-1.6\\test1.py");
 			//objJep.runScript("C:\\test1.py");
 			
 			//objJep.runScript("C:\\test1.py", cl);
 			
 			
 			//objJep.eval("import sys");
 			//objJep.eval("import os");
 			//objJep.eval("for p in sys.path:");
 			//objJep.eval("	print os.listdir( p )");
 			
 			//objJep.eval("os.putenv('JMODELICA_HOME', 'C:\\ProgramFiles\\JModelica.org-1.6')");
 			//objJep.eval("os.putenv('IPOPT_HOME', 'C:\\ProgramFiles\\JModelica.org-1.6\\Ipopt-MUMPS')");
 			//objJep.eval("os.putenv('SUNDIALS_HOME', 'C:\\ProgramFiles\\JModelica.org-1.6\\SUNDIALS')");
 			//objJep.eval("os.putenv('CPPAD_HOME', 'C:\\ProgramFiles\\JModelica.org-1.6\\CppAD')");
 			//objJep.eval("os.putenv('MINGW_HOME', 'C:\\ProgramFiles\\JModelica.org-1.6\\mingw')");
 			
 			//
 			
 			
 			    
 			    
 			    
 			    
 			//objJep.eval("help('modules')");
 			
 			
 	//	} catch (JepException ex) {
 		//	 ex.printStackTrace();
 		//}
 	//}
 	
 	
 	
 	public void test() {
 		
 
 		String path = "c:\test";
 
 		this.listEngines();
 		
 		PythonInterpreter interp;
 		
         ScriptEngineManager mgr = new ScriptEngineManager();
         
         List<ScriptEngineFactory> factoryList = mgr.getEngineFactories();
         Properties newProps = new Properties();
         
     	String pythonPath = "C:\\ProgramFiles\\jmodelicatest\\Python;C:\\ProgramFiles\\jmodelicatest\\CasADi";
     	newProps.setProperty("python.path", pythonPath);
     	
     	String pythonHome = "C:\\python\\Python2.7";
     	newProps.setProperty("python.home", pythonPath);
     	
     	
     	newProps.setProperty("jmodelica.home", "C:\\ProgramFiles\\jmodelicatest");
     	//JMODELICA_HOME
     	
     	
     	Properties properties = System.getProperties();
     	
     	PythonInterpreter.initialize(properties, newProps,
     	                             new String[] {""});
     	
     	
         ScriptEngine pyEngine = mgr.getEngineByName("python");
         
         try {
             pyEngine.eval("print \"Python - Hello, world!\"");
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         
         //return;
         		
    
         URL fileUrl = this.getClass().getClassLoader().getResource(fmuFile);
         String code = "myModel = FMUModel('" + fileUrl.getPath() + "')";
         
         try {
 
         	
         	pyEngine.eval("import os");
         	
         	pyEngine.eval("os.putenv('JMODELICA_HOME', 'C:\\ProgramFiles\\jmodelicatest')");
         	pyEngine.eval("os.putenv('IPOPT_HOME', 'C:\\ProgramFiles\\jmodelicatest\\Ipopt-MUMPS')");
         	pyEngine.eval("os.putenv('SUNDIALS_HOME', 'C:\\ProgramFiles\\jmodelicatest\\SUNDIALS')");
         	pyEngine.eval("os.putenv('CPPAD_HOME', 'C:\\ProgramFiles\\jmodelicatest\\CppAD')");
         	pyEngine.eval("os.putenv('MINGW_HOME', 'C:\\ProgramFiles\\jmodelicatest\\mingw')");
 
 
         	
         	//pyEngine.eval("import jpype");
         	 
             pyEngine.eval("from jmodelica.fmi import FMUModel");
             
             
            // pyEngine.eval(code);
             //pyEngine.eval("myModel.simulate()");
             
         } catch (Exception ex) {
             ex.printStackTrace();
         }
  		/**/
 
         
         
         
         
         
 	}
 	
 	
 
     private static void listEngines(){
         ScriptEngineManager mgr = new ScriptEngineManager();
         List<ScriptEngineFactory> factories =
                 mgr.getEngineFactories();
         for (ScriptEngineFactory factory: factories) {
             System.out.println("ScriptEngineFactory Info");
             String engName = factory.getEngineName();
             String engVersion = factory.getEngineVersion();
             String langName = factory.getLanguageName();
             String langVersion = factory.getLanguageVersion();
             System.out.printf("\tScript Engine: %s (%s)\n",
                     engName, engVersion);
             List<String> engNames = factory.getNames();
             for(String name: engNames) {
                 System.out.printf("\tEngine Alias: %s\n", name);
             }
             System.out.printf("\tLanguage: %s (%s)\n",
                     langName, langVersion);
         }
     }
 
 
 }
