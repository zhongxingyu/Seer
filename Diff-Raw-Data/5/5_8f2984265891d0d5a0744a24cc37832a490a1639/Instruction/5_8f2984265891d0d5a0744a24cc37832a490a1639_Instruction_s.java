 package mustache.core;
 
 import java.io.Serializable;
 
 /**
  * TODO doc
  * @author Dri
  */
 public abstract class Instruction implements Serializable {
 	private static final long serialVersionUID = 4440679549428243560L;
 	
 	Instruction() {}
 	
 	public static boolean isIndentation(String indentation) {
		for (int i = indentation.length(); --i > 0;) {
 			char c = indentation.charAt(i);
			if (c != ' ' && c != '\t') {
 				return false;
 			}
 		}
 		return true;
 	}
 }
