 package errors;
 
 public class Error {
 	
 	public enum Type{
		DECLARED, NOTDECLARED, TYPE
 	}
 	
 	public static void PrintError(String error, Type t){
 		switch (t) {
		case DECLARED:
 			System.out.println("Variable " + error + " has already been declared");
 			break;
		case NOTDECLARED:
 			System.out.println("Variable " + error + " has not been declared");
 			break;
 		case TYPE:
 			System.out.println("Incorrect type" + error);
 			break;
 		}
 
  
 		
 	}
 	
 	/*
 	 * 	Scope Errors
 	 */
 <<<<<<< HEAD
 
 	public static void VariableAlreadyDeclared(String id){
 		PrintError("Variable " +id+" Already Declared");
 =======
 /*
 	public static void VariableAlreadyDeclared(){
 		PrintError("Variable Already Declared");
 >>>>>>> Error
 	}
 	*/
 }
