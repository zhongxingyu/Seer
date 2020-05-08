 // Assembler.java
 // by Thea Gab
// Compile: (from directory with assembler folder)
//  javac assembler/*.java
// Usage: (from directory with assembler folder)
//  java assembler/Assembler <filename.asm>
 
 package assembler;
 
 import java.io.*;
 import java.util.StringTokenizer;
 import assembler.Parser;
 import assembler.SymbolTable;
 
 public class Assembler {
 
   public static void main(String[] args) throws IOException{
     // Check input arguments
     // Check that file is proper file type
     if (args.length != 1 || args[0].endsWith(".asm") != true) {
       System.err.println("Usage: java Assembler <Prog.asm>");
       System.exit(1);
     }
 
     // Save input file name
     String inputFile = args[0];
 
     // Parse out filename for hack file
     String fileName = inputFile.substring(0, inputFile.length()-4) + ".hack";
 
     // Create file stream for output (***add testing for if file exists already, ask
     //    user before overwriting)
     BufferedWriter hack = new BufferedWriter(new FileWriter(fileName));
 
     // Create file stream for input
     BufferedReader asm = null;
     try {
       asm = new BufferedReader(new FileReader(inputFile));
 
     }
     catch (FileNotFoundException e) {
       System.err.println(inputFile + ": does not exist or cannot be read");
       System.exit(1);
     }
 
     Parser parser = null;
 
     try {
       parser = new Parser(asm);
     }
     catch (IOException e) {
       System.err.println(inputFile + ": error while reading file");
     }
 
     // Close file stream
     asm.close();
 
     // Create symbol table and do first pass (saving the labels into the symbol table)
     SymbolTable symbolTable = new SymbolTable();
     parser.firstPass(symbolTable);
 
     // Parse the first line of the file and keep parsing until
     // end of file reached
     String command = parser.parseCommand(symbolTable);
 
     // Check for comments
     if (command != null) {
       hack.write(command);
       hack.newLine();
     }
 
 
     while (parser.hasMoreCommands()) {
       parser.advance();
       command = parser.parseCommand(symbolTable);
       if (command != null) {
         hack.write(command);
         hack.newLine();
       }
 
     }
 
     // Close file stream
     hack.close();
 
     System.out.println("Output written to " + fileName);
   }
 }
