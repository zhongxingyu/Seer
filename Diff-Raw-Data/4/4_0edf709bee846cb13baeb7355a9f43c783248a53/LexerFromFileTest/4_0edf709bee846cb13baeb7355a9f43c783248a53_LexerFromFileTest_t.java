 /**
  * Lisp Subinterpreter, an interpreter for a sublanguage of Lisp
  * Copyright (C) 2011  Meisam Fathi Salmi <fathi@cse.ohio-state.edu>
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package edu.osu.cse.meisam.interpreter;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Vector;
 
 import junit.framework.Assert;
 import junit.framework.TestCase;
 import edu.osu.cse.meisam.interpreter.tokens.EOF;
 import edu.osu.cse.meisam.interpreter.tokens.Token;
 
 /**
  * This class provides a framework for testing the program by reading the input
  * from test files
  * 
  * @author Meisam Fathi Salmi <fathi@cse.ohio-state.edu>
  * 
  */
 public class LexerFromFileTest extends TestCase {
 
     private static final int DEFAULT_BUFFER_SIZE = 1000;
 
     private static final String TEST_DIR = "testfiles";
 
     private static final String SMOKE_TEST_FILE_NAME = "smoke-test.input";
 
     private static final File SMOKE_TEST_FILE_PATH = new File(
             LexerFromFileTest.TEST_DIR + File.separator
                     + LexerFromFileTest.SMOKE_TEST_FILE_NAME);
 
     private File[] getFiles(final String dir) {
         final File directory = new File(dir);
         return directory.listFiles();
     }
 
     protected Collection getPassingTestFiles() {
         final File[] allFiles = getFiles(LexerFromFileTest.TEST_DIR);
         final Vector testFiles = new Vector(allFiles.length / 2);
 
         for (int i = 0; i < allFiles.length; i++) {
             if (isPassingTestFile(allFiles[i])) {
                 testFiles.add(allFiles[i]);
             }
         }
         return testFiles;
     }
 
     protected Collection getFailingTestFiles() {
         final File[] allFiles = getFiles(LexerFromFileTest.TEST_DIR);
         final Vector testFiles = new Vector(allFiles.length / 2);
 
         for (int i = 0; i < allFiles.length; i++) {
             if (isFailingTestFile(allFiles[i])) {
                 testFiles.add(allFiles[i]);
             }
         }
         return testFiles;
     }
 
     private boolean isLexerExpectedFile(final File fileName) {
         return fileName.getName().endsWith(".lexer");
     }
 
     private boolean isTestFile(final File fileName) {
         return fileName.getName().endsWith(".input");
     }
 
     private boolean isPassingTestFile(final File fileName) {
         return fileName.getName().startsWith("passing-test")
                 && fileName.getName().endsWith(".input");
     }
 
     private boolean isFailingTestFile(final File fileName) {
         return fileName.getName().startsWith("failing-test-")
                 && fileName.getName().endsWith(".input");
     }
 
     protected String readfromFile(final File file) throws IOException {
         final Reader input = new FileReader(file);
         final StringBuffer fileContent = new StringBuffer(
                 LexerFromFileTest.DEFAULT_BUFFER_SIZE);
 
         final char[] buffer = new char[LexerFromFileTest.DEFAULT_BUFFER_SIZE];
         int n = 0;
         while (-1 != (n = input.read(buffer))) {
             fileContent.append(buffer, 0, n);
         }
        input.close();
         return fileContent.toString();
     }
 
     public void testSmokeListTestFile() {
         String s;
         try {
             s = readfromFile(LexerFromFileTest.SMOKE_TEST_FILE_PATH);
             // the value inside the file should be exactly the name of the file
             Assert.assertEquals(LexerFromFileTest.SMOKE_TEST_FILE_NAME, s);
         } catch (final IOException e) {
             fail(e.getMessage());
         }
     }
 
     public void testLexerByPassingTests() {
         final Collection testFiles = getPassingTestFiles();
         for (final Iterator iterator = testFiles.iterator(); iterator.hasNext();) {
             verifyAllTokens(iterator);
         }
     }
 
     private void verifyAllTokens(final Iterator iterator) {
         try {
             final File testFile = (File) iterator.next();
             final String fileContent = readfromFile(testFile);
             final InputProvider inputProvider = new StringInputProvider(
                     fileContent);
             final Lexer lexer = new Lexer(inputProvider);
             try {
 
                 final String pathname = getLexerExpectedOutputFileName(testFile);
 
                 final File lexerExpected = new File(pathname);
                 final BufferedReader reader = new BufferedReader(
                         new FileReader(lexerExpected));
                 Token token = null;
                 int line = 0;
                 do {
                     token = lexer.nextToken();
                     if (token instanceof EOF) {
                         break;
                     }
                     final String tokenName = reader.readLine();
                     line++;
                     Assert.assertEquals(testFile.getName() + ":" + line,
                             tokenName, token.getClass().getSimpleName());
 
                     final String tokenLexval = reader.readLine();
                     line++;
                     Assert.assertEquals(testFile.getName() + "@ Line " + line,
                             tokenLexval, token.getLexval());
                 } while (!(token instanceof EOF));
 
                reader.close();

             } catch (final LexerExeption ex) {
                 fail("In " + testFile + ": " + ex.getMessage());
             }
 
         } catch (final IOException e) {
             fail(e.getMessage());
         }
     }
 
     private String getLexerExpectedOutputFileName(final File testFile) {
         final String pathname = testFile.getAbsolutePath().replace("input",
                 "lexer");
         return pathname;
     }
 
     public void testLexerByFailingTests() {
         final Collection testFiles = getFailingTestFiles();
         for (final Iterator iterator = testFiles.iterator(); iterator.hasNext();) {
             try {
                 final File testFile = (File) iterator.next();
                 String fileContent;
                 fileContent = readfromFile(testFile);
                 final InputProvider inputProvider = new StringInputProvider(
                         fileContent);
                 final Lexer lexer = new Lexer(inputProvider);
                 try {
                     passOverAllTokens(lexer);
                     fail(testFile + " should've fail but it didn't");
                 } catch (final LexerExeption ex) {
                     // good job, you raised an exception
                 }
 
             } catch (final IOException e) {
                 fail(e.getMessage());
             }
         }
     }
 
     private void passOverAllTokens(final Lexer lexer) {
         Token token = null;
         do {
             token = lexer.nextToken();
         } while (!(token instanceof EOF));
     }
 
 }
