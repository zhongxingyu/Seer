 import tcwi.TCWI;
 
 public class Web_ProjectInitializator {
 	private static final String VERSION = "0.0.3.1";
 	private static final String AUTHORS = "EifX & hulllemann";
 
 	public static void main(String[] args) {
		if(args.length!=6){
 			System.out.println("Help - Web_ProjectInitializator "+VERSION+" by "+AUTHORS);
 			System.out.println("----------------------------------------------------");
 			System.out.println("\nUsage: Web_ProjectInitializator [PROJECTPATH] [PROJECTNAME] [PROJECTFULLNAME] ");
 			System.out.println("                                [PROJECTVERSION] [PROJECTAUTHOR] [GLOBAL_SETTINGS]");
 			System.out.println("\n[PROJECTPATH]");
 			System.out.println("     Absolute Path for scan for TypeChef-Files\n");
 			System.out.println("[PROJECTNAME]");
			System.out.println("     Project shortcut-name. It must be unique!\n"); //TODO: Unique-Check!
 			System.out.println("[PROJECTFULLNAME]");
 			System.out.println("     Project name\n");
 			System.out.println("[PROJECTVERSION]");
 			System.out.println("     Project version\n");
 			System.out.println("[PROJECTAUTHOR]");
 			System.out.println("     Project author\n");
 			System.out.println("[GLOBAL_SETTINGS]");
 			System.out.println("     Absolute Path for the global_settings.xml\n     (include the name of the settings file)\n");
 		}else{
 			TCWI tcwi = new TCWI();
 			tcwi.initialisize(args[0], args[1], args[2], args[3], args[4], args[5]);
 		}
 	}
 
 }
