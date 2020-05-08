 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Date;
 
 public class AssemblyCodeGenerator {
 
     // Compiler identifier
     private final String COMPILER_IDENT = "WRC 1.0";
     // level of indentation for current code
     private int indent_level = 0;
     
     // Error Messages
     private static final String ERROR_IO_CLOSE     = "Unable to close fileWriter";
     private static final String ERROR_IO_CONSTRUCT = "Unable to construct FileWriter for file %s";
     private static final String ERROR_IO_WRITE     = "Unable to write to fileWriter";
 
     // FileWriter
     private FileWriter fileWriter;
     
     // Output file header
     private static final String FILE_HEADER = 
         "/*\n" +
         " * Generated %s\n" + 
         " */\n\n";
         
     //-------------------------------------------------------------------------
     //      Constructors
     //-------------------------------------------------------------------------
 
     public AssemblyCodeGenerator(String fileToWrite) {
         try {
             fileWriter = new FileWriter(fileToWrite);
             
             // write fileHeader with date/time stamp
             writeAssembly(FILE_HEADER, (new Date()).toString());
         } 
         catch (IOException e) {
             System.err.printf(ERROR_IO_CONSTRUCT, fileToWrite);
             e.printStackTrace();
             System.exit(1);
         }
     }
 
     //-------------------------------------------------------------------------
     //      decreaseIndent
     //-------------------------------------------------------------------------
     public void decreaseIndent() {
         indent_level--;
     }
     
     //-------------------------------------------------------------------------
     //      increaseIndent
     //-------------------------------------------------------------------------
     public void increaseIndent() {
         indent_level++;
     }
 
     //-------------------------------------------------------------------------
     //      dispose
     //-------------------------------------------------------------------------
     public void dispose() {
         // Close the filewriter
         try {
             fileWriter.close();
         } 
         catch (IOException e) {
             System.err.println(ERROR_IO_CLOSE);
             e.printStackTrace();
             System.exit(1);
         }
     }
 
     //-------------------------------------------------------------------------
     //      writeAssembly
     //-------------------------------------------------------------------------
     // params = String []
     public void writeAssembly(String template, String ... params) {
         StringBuilder asStmt = new StringBuilder();
         
         // Indent current line
         for (int i=0; i < indent_level; i++) {
             asStmt.append(SparcInstr.INDENTOR);
         }
         
         asStmt.append(String.format(template, (Object[])params));
         
         try {
             fileWriter.write(asStmt.toString());
         } 
         catch (IOException e) {
             System.err.println(ERROR_IO_WRITE);
             e.printStackTrace();
         }
     }
     
     //-------------------------------------------------------------------------
     //      example
     //-------------------------------------------------------------------------
     public void example() {
         AssemblyCodeGenerator myAsWriter = new AssemblyCodeGenerator("output.s");
 
         myAsWriter.increaseIndent();
         myAsWriter.writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, String.valueOf(4095), "%l0");
         myAsWriter.increaseIndent();
         myAsWriter.writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, String.valueOf(1024), "%l1");
         myAsWriter.decreaseIndent();
         
         myAsWriter.writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, String.valueOf(512), "%l2");
         
         myAsWriter.decreaseIndent();
         myAsWriter.dispose();
     }
 
     //-------------------------------------------------------------------------
     //      String Utility Functions
     //-------------------------------------------------------------------------
     public String quoted(String str)
     {
         return "\"" + str + "\"";
     }
 
     //-------------------------------------------------------------------------
     //
     //      Code Generation Functions
     //  
     //-------------------------------------------------------------------------
 
     //-------------------------------------------------------------------------
     //      DoProgramStart
     //-------------------------------------------------------------------------
     public void DoProgramStart(String filename)
     {
        writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.FILE_DIR, quoted(filename));
        writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.IDENT_DIR, quoted(COMPILER_IDENT));
     }
 
 
 
 
 }
