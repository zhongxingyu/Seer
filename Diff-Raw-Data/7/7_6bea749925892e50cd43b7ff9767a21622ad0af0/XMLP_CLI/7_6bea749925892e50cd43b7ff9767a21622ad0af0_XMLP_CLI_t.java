 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mlparch;
 
 /**
  *
  * @author john
  */
 public class XMLP_CLI {
 	public static void showHelp() {
 		System.out.println("XMLPatch XML patching utility");
 		System.out.println("Public domain code, released 2012");
 		System.out.println("Options:");
 		System.out.println("    -t <arg> - set target file for patch or query");
 		System.out.println("    -v - show this help");
 		System.out.println("    -? - show this help");
 		System.out.println("    --help - show this help");
 	}
 	public static void main(String[] args) throws Exception {
 		String targName = "xmlpatch.xml";
 		String packName = "extract";
 		String query = null;
 		int mode = 1; //0 == patch, 1 == query
 		
 		for (int i = 0; i < args.length; i++) {
 			String arg0 = args[i];
 			if (arg0.startsWith("--")) {
 				//long option
 				arg0 = arg0.substring(2);
 				
 				if (arg0.equals("help")) {
 					showHelp(); System.exit(0);
 				} else {
 					throw new IllegalArgumentException("Unrecognized long option: '"+arg0+"'.");
 				}
 			} else if (arg0.charAt(0) == '-') {
 				//short option
 				arg0 = arg0.substring(1);
 				
 				for (int j = 0; j < arg0.length(); j++) {
 					//short optiona may be stacked
 					char opt = arg0.charAt(j);
 					
 					switch (opt) {
 						case 'v':
 						case '?':
 							showHelp(); System.exit(0);
 						case 'q':
 							if (j!=arg0.length()-1)
								throw new IllegalArgumentException("'q' short option must be last in a stack!");
 							if (++i >= args.length)
								throw new IllegalArgumentException("Expected another bare argument after 'q'!");
 							query = args[i];
 							break;
 						default:
 							throw new IllegalArgumentException("Unrecognized short option: '"+opt+"'.");
 					}
 				}
 			} else {
 				//bare argument
 				throw new IllegalArgumentException("Unrecognized bare argument: '"+arg0+"'.");
 			}
 		}
 		
 		if (mode == 0) {
 			//patch mode
 			throw new UnsupportedOperationException();
 		} else {
 			//query mode	
 			System.out.println("Querying \""+targName+"\"...");
 				//String query = "/GameObjects/GameObject[@Category=\"Pony\"]/@ID";
 				//String query = "/GameObjects/GameObject[@Category=\"Pony_House\"]/Construction/@ConstructionTime";
 				XMLPatch patcher = new XMLPatch(targName);
 				patcher.applyPatch(query, new XMLPatch.PrintXMLActor());
 		}
 	}
 }
