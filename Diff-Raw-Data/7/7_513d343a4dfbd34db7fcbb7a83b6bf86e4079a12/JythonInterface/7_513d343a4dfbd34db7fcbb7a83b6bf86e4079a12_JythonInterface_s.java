 package edu.berkeley.gamesman;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Arrays;
 import java.util.prefs.Preferences;
 
 import jline.ConsoleReader;
 import jline.Terminal;
 
 import org.python.core.PyObject;
 import org.python.util.InteractiveConsole;
 import org.python.util.JLineConsole;
 import org.python.util.ReadlineConsole;
 
 public final class JythonInterface {
 	
 	private enum Consoles {
 		dumb,readline,jline
 	}
 
 	/**
 	 * @param args
 	 * @throws IOException something bad happened
 	 */
 	public static void main(String[] args) throws Exception {
 		Preferences prefs = Preferences.userNodeForPackage(JythonInterface.class);
 		
 		String consoleName = prefs.get("console", "ask");
 		Consoles console = null;
 		
 		if(consoleName.equals("ask")){
 			System.out.print("What console would you like to use? "+Arrays.toString(Consoles.values())+": ");
 			System.out.flush();
 			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 			consoleName = br.readLine();
 			if((console = Consoles.valueOf(consoleName)) != null){
 				prefs.put("console", consoleName);
 			}
		}
 		InteractiveConsole rc = null;
 		switch(console){
 		case dumb:
 			rc = new InteractiveConsole();
 		case jline:
 			try {
 				ConsoleReader cr = new ConsoleReader();
 				System.out.print("Press enter to start gamesman-jython!\n");
 				if(cr.readLine() != null) //eclipse doesn't work with jline
 					rc = new JLineConsole();
 			} catch(Error e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			//if jline isn't working for some reason
 			rc = new InteractiveConsole() {
 				@Override
 				public String raw_input(PyObject prompt) {
 					return super.raw_input(prompt);
 				}
 			};
 		case readline:
 			rc = new ReadlineConsole();
 		}
 		
 		//this will let us put .py files in the junk directory, and things will just work =)
 		rc.exec("import sys");
 		addpath(rc, "jython_lib");
 		addpath(rc, "junk");
 		addpath(rc, "jobs");
 	
 		rc.exec("from Play import *");
 		rc.interact();
 	}
 	
 	private static void addpath(InteractiveConsole ic, String what){
 		String ud = System.getProperty("user.dir");
 		ic.exec(String.format("sys.path.append('%s/%s'); sys.path.append('%s/../%s');",
 				ud, what, ud, what));
 	}
 
 }
