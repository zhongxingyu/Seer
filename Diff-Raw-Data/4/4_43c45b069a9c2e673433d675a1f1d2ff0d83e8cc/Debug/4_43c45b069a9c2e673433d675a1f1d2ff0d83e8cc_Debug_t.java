 import java.util.concurrent.ConcurrentLinkedQueue;
 public class Debug {
     public static boolean developerMode = false;
     
     private static int getLineNumber() {
 	return Thread.currentThread().getStackTrace()[3].getLineNumber();
     }
     private static String getClassName() {
 	return Thread.currentThread().getStackTrace()[3].getClassName();
     }
     private static String getMethodName() {
 	return Thread.currentThread().getStackTrace()[3].getMethodName();
     }
     public static void game(String string){
 	System.out.println("==> " + string);
     }
     public static void marker(String string){
 	System.out.println("############### " + string + " ###############");
     }
     public static void warn(String string){
 	if(developerMode){
 	    System.out.println("WARNING: " + getClassName() + ":" + getMethodName() + "():" + getLineNumber() + ": " + string);
 	}
 	else {
 	    System.out.println("WARNING: " + string);
 	}
     }
     public static void info(String string){
 	if(developerMode){
 	    System.out.println("INFO: " + getClassName() + ":" + getMethodName() + "():" + getLineNumber() + ": " + string);
 	}
 	else {
 	    System.out.println("INFO: " + string);
 	}
     }
     public static void error(String string){
 	System.out.println("ERROR: " + getClassName() + ":" + getMethodName() + "():" + getLineNumber() + ": " + string);
 	System.exit(1);
     }
     public static void debug(String string){
 	if(developerMode){
 	    System.out.println("DEBUG: " + getClassName() + ":" + getMethodName() + "():" + getLineNumber() + ": " + string);
 	}
     }
     public static void stub(String string){
 	if(developerMode){
 	    System.out.println("STUB: " + getClassName() + ":" + getMethodName() + "():" + getLineNumber() + ": " + string);
 	}
     }
     public static void highlight(String position, int r, int g, int b){
 	if(developerMode){
 	    GraphicsConnection.debugConnection.sendHighlight(position, r, g, b);
 	}
     }
     public static void guiMessage(String message){
	GraphicsConnection.debugConnection.sendMessage(message);
     }
     public static void printGamestats(ConcurrentLinkedQueue<AIConnection> globalClientsArg){
 	if(developerMode){
 	    System.out.println("####################### GAME STATS: #######################");
 	    for(AIConnection ai: globalClientsArg){
 		ai.printStats();
 	    }
 	    System.out.println("###########################################################");
 	}
     }
 
 }
