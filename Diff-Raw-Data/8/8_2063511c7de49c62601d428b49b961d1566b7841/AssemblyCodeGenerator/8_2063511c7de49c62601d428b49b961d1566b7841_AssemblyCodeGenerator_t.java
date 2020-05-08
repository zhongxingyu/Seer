 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Date;
 
 public class AssemblyCodeGenerator {
 
     // 1) We will use this variable to denote the current level of indentation.
     //    For assembly there is usually only one or two levels of nesting.
     private int indent_level = 0;
     
     // 2) A collection of static final error messages.  This isn't so important in
     //    your project but is useful.  The static keyword means we only make 3 
     //    strings, shared across potentially multiple AssemblyCodeGenerator(s).
     //    It is Java convention to spell constant variables with upper casing.
     private static final String ERROR_IO_CLOSE     = "Unable to close fileWriter";
     private static final String ERROR_IO_CONSTRUCT = "Unable to construct FileWriter for file %s";
     private static final String ERROR_IO_WRITE     = "Unable to write to fileWriter";
 
     // 3) FileWriter is a basic IO class which can write basic types such as
     //    Strings to a file.  For performance you may want to look into the
     //    BufferedWriter class.
     private FileWriter fileWriter;
     
     // 4) This is a template for our file header.  It is very basic consisting only
     //    of a time stamp.
     private static final String FILE_HEADER = 
         "/*\n" +
         " * Generated %s\n" + 
         " */\n\n";
         
     // 5) This is the string we will use as an indentation seperator.  We are 
     //    encapsulating this seperator into one variable so we only need to change
     //    the initialization if we want to change our spacing to say 4 spaces for
     //    example.  Imagine if you simply used the literal "\t" in 500 places and
     //    then you decide you want to change it to 4 spaces!  Aside from regular 
     //    expressions you have a lot of work ahead of you.
     private static final String SEPARATOR = "    ";
     
     // 6) These are constant String templates that will be used for code
     //    generation.  It is nice to isolate these in one place or even in another
     //    file so that we can quickly make universal changes if needed.  You will
     //    notice that we could generate an entirely different language by simply
     //    changing the construct definitions.  I recommend defining all operations
     //    as well as formats.  Operations are things like add, mul, set, etc.
     //    Formats are like {OPERATION} {REG_1}, {REG_2}, {REG_3} etc.
 
     /*  MOVED THESE TO SparcWords.java */
     
     public AssemblyCodeGenerator(String fileToWrite) {
         try {
             fileWriter = new FileWriter(fileToWrite);
             
             // 7) Here we are making a call to writeAssembly to write our header with the
             //    current time.  writeAssembly explained later.
             writeAssembly(FILE_HEADER, (new Date()).toString());
         } catch (IOException e) {
             System.err.printf(ERROR_IO_CONSTRUCT, fileToWrite);
             e.printStackTrace();
             System.exit(1);
         }
     }
     
 
     // 8) These methods are used to increase or decrease our current indentation
     //    level.  You might ask why make a method for a simple inc/dec?  We are
     //    encapsulating the notion of adjusting indentation.  It just so happens
     //    that this current implementation is just a variable increment or 
     //    decrement, but who is to say that the operation won't be more advanced
     //    in the future.  Maybe we want to log a message everytime we increment
     //    or decrement indentation.  We wouldn't want to add the logging code
     //    everywhere we were incrementing the variable (if we didn't have the
     //    methods).
     public void decreaseIndent() {
         indent_level--;
     }
     
     public void dispose() {
         try {
             fileWriter.close();
         } catch (IOException e) {
             System.err.println(ERROR_IO_CLOSE);
             e.printStackTrace();
             System.exit(1);
         }
     }
     
     public void increaseIndent() {
         indent_level++;
     }
     
 
     
     // 9) This signature may look foreign to you.  What is says is that we have 
     //    public method named writeAssembly which takes as parameters a String
     //    followed by 1 or more strings.  This construct is called "VarArgs" and
     //    is a Java 1.5 feature.  This allows you to write one method which can
     //    be applied to any number of parameters.  This method simply takes in 
     //    a template and all the strings that will be substituted into the 
     //    template.  When you are actually in the method, the parameter 
     //    String ... params will be an array of strings.
     public void writeAssembly(String template, String ... params) {
         StringBuilder asStmt = new StringBuilder();
         
         // 10) This is where we use our indent_level.  We will indent indent_level levels
         //     of indentation.  That is an awkward sentence isn't it!  StringBuilder is
         //     an efficient class to build strings from concatentations.  If your 
         //     concatenations span multiple lines of code, using a StringBuilder can
         //     offer signifigant performance when compared to using the + operator.
         //     This topic can get fairly detailed, send me an email or come talk to me
         //     in the lab for more details.
         for (int i=0; i < indent_level; i++) {
             asStmt.append(SEPARATOR);
         }
         
         // 11) Here we are writing the message to file, notice we are using the 
         //     String.format method which takes a printf like format string followed
         //     by an array of Objects which are the parameters to the format string.
         asStmt.append(String.format(template, (Object[])params));
         
         try {
             fileWriter.write(asStmt.toString());
         } 
         catch (IOException e) {
             System.err.println(ERROR_IO_WRITE);
             e.printStackTrace();
         }
     }
     
     // 12) Main is just a small demo that will create a tiny assembly file in the
     //     current directory called "output.s".  This file doesn't compile and is
     //     not meant to.
     public static void main(String args[]) {
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
 
     ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     //
     //
     //  
     ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 
 
 }
