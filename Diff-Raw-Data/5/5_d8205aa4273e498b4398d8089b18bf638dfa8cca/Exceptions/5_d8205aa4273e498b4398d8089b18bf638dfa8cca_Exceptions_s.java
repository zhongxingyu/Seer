 package tcwi.exception;
 
 import java.util.HashMap;
 
 public class Exceptions {
 	private HashMap<Integer, String> exception;
 	
 	public Exceptions(){
 		exception = new HashMap<Integer, String>();
 		exception.put(1, "ERROR 1: Can't read the setting-file");
 		exception.put(2, "ERROR 2: Error during parse the setting-file. Is it malformed?");
 		exception.put(3, "ERROR 3: Can't generate and write the javascript file.");
 		exception.put(4, "ERROR 4: Can't write the setting-file.");
 		exception.put(5, "ERROR 5: Project name already set!");
 		exception.put(6, "ERROR 6: Can't access the given path. Maybe you do not have the rights for it?");
 		exception.put(7, "ERROR 7: File / Path doesn't exist.");
 		exception.put(8, "ERROR 8: Can't read / write the given file.");
 		exception.put(9, "ERROR 9: The 2 given projects are not from the same type!");
 		exception.put(10, "ERROR 10: The project has provided over one million times. Delete old projects of this type!");
 		exception.put(11, "ERROR 11: Error in parsing the .project-File. It's an CompareFile. Wrong format?");
 		exception.put(12, "ERROR 12: Wrong project-type!");
 		exception.put(13, "ERROR 13: Wrong count of parameters!");
 	}
 	
 	public void throwException(int i, Exception e, boolean isExit, String pathInformation){
 		if(pathInformation!=""){
 			System.err.println(exception.get(i) + " The Path is: " + pathInformation);
 		}else{
 			System.err.println(exception.get(i));
 		}
 		System.err.println();
 		if(e!=null){
 			e.printStackTrace();
 		}
 		if(isExit){
 			System.exit(i);
 		}
 	}
 }
