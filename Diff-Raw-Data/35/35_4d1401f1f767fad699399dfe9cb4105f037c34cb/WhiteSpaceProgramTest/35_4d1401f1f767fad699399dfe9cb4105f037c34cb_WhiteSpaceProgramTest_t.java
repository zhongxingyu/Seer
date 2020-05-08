 package com.github.mikiwiik;
 
 import org.junit.Test;
 
 import static junit.framework.Assert.assertEquals;
 
 public class WhiteSpaceProgramTest {
 
     @Test
     public void testPushSingleChar() {
         final String program = new WhiteSpaceProgram().stackPushChar('j').toString();
         assertEquals("SS(Stack IMP, push nr) S(pos nr) STTSTSTS(j) CR",
            "    \t\t \t \t \n\r", program);
     }
 
     @Test
     public void testPopSingleChar() {
         final String program = new WhiteSpaceProgram().ioPopChar().toString();
         assertEquals("[Tab][LF](I/O IMP), [Space][Space] Output the character at the top of the stack, CR",
                 "\t\n  \r", program);
     }
 
 }
