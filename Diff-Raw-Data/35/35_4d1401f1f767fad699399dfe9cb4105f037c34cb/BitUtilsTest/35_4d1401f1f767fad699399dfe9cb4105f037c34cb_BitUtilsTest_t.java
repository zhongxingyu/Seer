 package com.github.mikiwiik;
 
 import com.google.common.collect.Lists;
 import org.junit.Test;
 
 import static junit.framework.Assert.assertEquals;
 
 public class BitUtilsTest {
 
     @Test
     public void testConvertingSingleCharacters() throws Exception {
         // LF:000001010
 
         assertEquals("j:01101010",
                 Lists.newArrayList(false,true,true,false,true,false,true,false),
                 BitUtils.asciiAsBinary("j"));
 
         assertEquals("H:01001000",
                 Lists.newArrayList(false,true,false,false, true,false,false,false),
                 BitUtils.asciiAsBinary("H"));
     }
     @Test
     public void testConvertingSingleChars() throws Exception {
         assertEquals("LF: 00001010    \t \t ",
            "     \t \t \n",
             BitUtils.charAsWspaceNumber('\n'));
 
         assertEquals("j: 01101010 \t\t \t \t ",
            "  \t\t \t \t \n",
             BitUtils.charAsWspaceNumber('j'));
 
         assertEquals("H: 01001000 \t  \t   ",
            "  \t  \t   \n",
             BitUtils.charAsWspaceNumber('H'));
 
         assertEquals("SPACE: 00100000  \t     ",
            "   \t     \n",
             BitUtils.charAsWspaceNumber(' '));
     }
 }
