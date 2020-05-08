 package com.github.mikiwiik;
 
 public class WhiteSpaceProgram {
 
     final static private String CR = "\r";
 
     //[Space]	Stack Manipulation
     final static private String IMP_STACK = " ";
     // [Space]	Number	Push the number onto the stack
     final static private String STACK_PUSH_NR = " ";
 
     //[LF]	Flow Control
    final static private String IMP_FLOW = "\n";
    //[LF][LF]	-	End the program
    final static private String FLOW_END_PROGRAM = "\n\n";
 
     //[Tab][LF]	I/O
     final static private String IMP_IO = "\t\n";
     final static private String IO_POP_CHAR = "  ";
 
     private final StringBuilder program = new StringBuilder();
 
     /**
      * Push given char to stack as positive wspace number
      * @param character
      * @return
      */
     public WhiteSpaceProgram stackPushChar(char character) {
         final String charAsWspace = WhiteSpaceUtils.charAsNumber(character);
         program.append(IMP_STACK)
             .append(STACK_PUSH_NR)
             .append(charAsWspace)
             .append(CR);
         return this;
     }
 
     /**
      * Output the character at the top of the stack.
      * @return
      */
     public WhiteSpaceProgram ioPopChar() {
         program.append(IMP_IO)
             .append(IO_POP_CHAR)
             .append(CR);
         return this;
     }
 
     /**
      * [LF][LF]	-	End the program
      * @return
      */
     public WhiteSpaceProgram flowEndProgram() {
        program.append(IMP_FLOW).append(FLOW_END_PROGRAM).append(CR);
         return this;
     }
 
     /**
      * @return The current program as a String.
      */
     @Override
     public String toString() {
         return program.toString();
     }
 }
