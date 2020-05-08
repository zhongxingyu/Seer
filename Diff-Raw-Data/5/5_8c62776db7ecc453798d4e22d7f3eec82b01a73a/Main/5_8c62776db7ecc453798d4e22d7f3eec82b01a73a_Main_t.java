 package lea;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.Map;
 import lea.syntax.*;
 
 import java_cup.runtime.Symbol;
 
 public class Main 
 {
     public static FunctionTable fctTable = new FunctionTable();
     public static ConstantTable constTable = new ConstantTable();
     public static TypeTable typeTable = new TypeTable();
     public static NativeFunctionTable nativeFctTable = new NativeFunctionTable();
     public static SyntaxTree currentNode = null;	//Le noeud courant dans lequel on est rendu
     private static boolean hasCompileErrors = false;
     private static LeaLexer myLex;
     
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 
 			System.out.println("Lea compiler initialized.");
 			
			//Genere la liste des fonctions natives du langage
 			nativeFctTable.generateList();
 
 			FileReader file;
 			try 
 			{
 				file = new FileReader(args[0]);
 			    myLex = new LeaLexer(file);
 			    LeaParser myP = new LeaParser(myLex);
 			    
 			    Symbol result=null;
 			    try 
 			    {
 					result=myP.parse();
 					try 
 					{
 						if(!hasCompileErrors)
 						{
 							//CODE POUR LAETITIA
 						}
 						
 						//Generation des .dot
 						for (Map.Entry<String, FunctionInfo> entry : fctTable.entrySet())
 						{
						    entry.getValue().getSyntaxTree().toDot("data/"+entry.getKey());
 						}
 					}	
 					catch (Exception e) 
 					{
 					    System.out.println("result error");
 					}
 			    }
 			    catch (Exception e) 
 			    {
 					System.out.println("parse error...");
 					e.printStackTrace();
 			    }    
 			} 
 			catch (FileNotFoundException e) 
 			{
 				System.err.println("File not found!");
 			}
 		} 
 		catch (Exception e) 
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	//Le level c'est :
 	// 0 : WARNING
 	// 1 : ERROR
 	// 2 : FATAL ERROR
 	public static void printError(String message, int level)
 	{
 		String outputMess;
 		
 		switch(level)
 		{
 		case 0:
 			outputMess = "WARNING (ligne "+myLex.getLine()+", colonne "+myLex.getColumn()+") : " + message;
 			break;
 		case 1:
 			outputMess = "ERROR (ligne "+myLex.getLine()+", colonne "+myLex.getColumn()+") : " + message;
 			hasCompileErrors = true;
 			break;
 		case 2:
 			outputMess = "FATAL ERROR (ligne "+myLex.getLine()+", colonne "+myLex.getColumn()+") : " + message;
 			hasCompileErrors = true;
 			break;
 		default:
 			outputMess = message;
 			break;
 		}
 		
 		System.out.println(outputMess);
 	}
 }
