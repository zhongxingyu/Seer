 package com.bazaarvoice.oxen;
 
 /**
  * Created by steve.ohara
  * Date: 9/27/12 9:10 AM
  */
 
 import com.bazaarvoice.oxen.commands.OxParseProgram;
 import com.bazaarvoice.oxen.commands.OxProgram;
 import com.bazaarvoice.oxen.data.OxDirectReader;
 import com.bazaarvoice.oxen.data.OxReader;
 import com.bazaarvoice.oxen.data.OxXmlReader;
import org.testng.Assert;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintStream;
 
 public class OxReaderTest {
     private OxProgram _program;
 
     @DataProvider (name = "testFiles")
     public Object[][] fileData() {
         return new Object[][] {
                 { "BV_C1_ANSWER_2012-01-1_2012-01-2.xml", 258 },
                 { "BV_C1_QUESTION_2012-01-1_2012-01-2.xml", 77 },
                 { "BV_C1_STORY_2012-01-1_2012-01-2.xml", 0 },
                 { "BV_C1_COMMENTREVIEW_2012-01-1_2012-01-2.xml", 0 },
                 { "BV_C1_REVIEW_2012-01-1_2012-01-2.xml", 2 }
         };
     }
 
     private class HandleRow implements OxReader.RowProcessor {
         public void processRow() {
             _program.run();
         }
     }
 
     @Test (dataProvider = "testFiles")
     public void testDirect(String file, int expected) throws IOException {
         sharedTester(file, expected, new OxDirectReader());
     }
 
     @Test (dataProvider = "testFiles")
     public void testXml(String file, int expected) throws IOException {
         sharedTester(file, expected, new OxXmlReader());
     }
 
     private void sharedTester(String file, int expected, OxReader reader) throws IOException {
         String prefix = "/Users/steve.ohara/oxen/xml/";
 
         StringBuilder symbolText = new StringBuilder();
         BufferedReader symbolReader = new BufferedReader(new FileReader(prefix + "ids.txt"));
         String rec;
         while ((rec = symbolReader.readLine()) != null) {
             symbolText.append(rec).append("\n");
         }
 
         String programText = symbolText.toString() +
                 "if Client_ID = 235\n" +
                 "    print(Client_ID + ' ' + left(Content_Text,40))\n" +
                 "endif";
         OxParseProgram parser = new OxParseProgram();
         OxSymbolTable symbolTable = new OxSymbolTable();
         _program = parser.parse(programText, symbolTable);
 
         // Redirect output and run
         ByteArrayOutputStream pgmOutput = new ByteArrayOutputStream();
         PrintStream prtStream = new PrintStream(pgmOutput);
         System.setOut(prtStream);
         HandleRow handler = new HandleRow();
         reader.readFile(prefix + file, symbolTable, handler);
 
         // Check results
         int count = 0;
         for (char ch : pgmOutput.toString().toCharArray()) {
             if (ch == '\n') count++;
         }
         Assert.assertEquals(expected, count);
     }
 }
