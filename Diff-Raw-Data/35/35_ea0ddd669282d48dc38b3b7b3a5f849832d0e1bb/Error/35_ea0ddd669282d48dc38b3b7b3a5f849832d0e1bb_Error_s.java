 package errors;
 
 public class Error {
 	
	
	private int systaxErrors = 0;
	public Error(int syntaxErrors) {
		this.systaxErrors = syntaxErrors;
 	}
 	
	public static void PrintError(String error){
		System.out.println("ERROR : " + error);
 	}
 	
 	/*
 	 * 	Scope Errors
 	 */
 
 	public static void VariableAlreadyDeclared(String id){
 		PrintError("Variable " +id+" Already Declared");
 	}
	
 }
