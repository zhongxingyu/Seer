 import java.io.*;
 import org.antlr.runtime.*;
 import javax.swing.*;
 
 
 public class LispIntRun {
 
 	public static void main(String[] args) throws Exception {
		// Reads the specified name of the file through an input dialog box
                 String SourceFile= JOptionPane.showInputDialog(null, "Enter source file name:");
                 SourceFile =SourceFile.trim();
                 if(SourceFile.equals("")|| SourceFile==null){
                     System.exit(0);
                 } else {
                 LispIntGUI LispGUI = new LispIntGUI(SourceFile);
 		LispGUI.setVisible(true);
                 }
                 
                CharStream cs= new ANTLRFileStream(args[0]);
 		LispLexer lexer = new LispLexer (cs);
             
                 CommonTokenStream tokens = 
 		   new CommonTokenStream(lexer);
 		LispParser parser = new LispParser(tokens);
 		parser.prog();
 	}
         
         
 }
         
         
 
  
