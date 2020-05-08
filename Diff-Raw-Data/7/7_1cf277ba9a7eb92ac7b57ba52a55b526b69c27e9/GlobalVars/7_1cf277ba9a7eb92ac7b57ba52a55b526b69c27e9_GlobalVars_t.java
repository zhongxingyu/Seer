 package main.java.globals;
 
 
 public class GlobalVars {
 	private static String nodeName = null;
 	public static final String DIRECTORY_NODE = "directory";
 	public static final String getNodeName(){
 		return nodeName;
 	}
 	public static final void setNodeName(String nodeName){
 		if(GlobalVars.nodeName == null) {
 			if(nodeName.getBytes().length < 16){
 				GlobalVars.nodeName = nodeName;
 			}else throw new RuntimeException("cannot have a node name greater then 16 bytes");
 		}
 	}
 	public static final String directoryHost = "ordirectory.herokuapp.com";
 	public static final int directoryPort = 8080;
 	public static final int serverPort = 8081;
 	public static final int terminalPort = 8082;
 	
 }
