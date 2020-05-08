 package filescript;
 import Parser.*;
 
 import java.util.Iterator;
 import java.util.List;
 
 //import java.io.FilePermission;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 
 
 //import actions.Action;
 
 import myFileScriptExceptions.ParsingException;
 import myFileScriptExceptions.RunException;
 import fileManager.*;
 public class MyFileScript {
 
 private static boolean validation(String[] args){
 	//	if 
 		
 		//check here if user enter file name
 		//check if filename is legal? maybe dont need a check here;
 		return true;
 	}
 	
 	/**
 	 * @param args
 	 * @throws InvocationTargetException 
 	 * @throws IllegalAccessException 
 	 * @throws InstantiationException 
 	 * @throws IOException 
 	 * @throws SecurityException 
 	 * @throws IllegalArgumentException 
 	 * @throws ParsingException 
 	 */
 public static void main(String[] args) throws ParsingException, IllegalArgumentException, SecurityException, IOException, InstantiationException, IllegalAccessException, InvocationTargetException {
 		// TODO Auto-generated method stub
 		
 
 			try {
 
 				MyFileScriptParser scriptParser = new MyFileScriptParser();
 				
 				List<Script> scripts = scriptParser.parseFile(args[1]);
				if (scripts.size() == 0)
				{
					quit();
				}
 				FileManager fm = new FileManager(args[0]);
 				
 				Script script;
 				for (Iterator <Script>i1 = scripts.iterator(); i1.hasNext() ;)
 				{
 					script =  i1.next();
 					script.runScript(fm);
 					
 				}
 
 			} catch (ParsingException e) {
 				
 				quit();
 			} 
 			catch (RunException e) {
 				
 				quit();
 			} 
 			
 
 		
 		
 		
 	}
 
 	private static void quit()
 	{
 		System.err.println("Error");
 		System.exit(-1);
 	}
 
 }
