 // Copyright (C) 2006 Google Inc.
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //      http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 package prettify;
 
 import prettify.parser.Util;
 import prettify.parser.Job;
 import prettify.parser.Prettify;
 import java.util.ListIterator;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.ArrayList;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.List;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  * Tests are adapted from JavaScript Prettify.
  * @author Chan Wai Shing <cws1989@gmail.com>
  */
 public class PrettifyTest {
 
   static {
     // set debug mode
     System.setProperty("PrettifyDebugMode", "true");
   }
   protected final String packagePath = "test/" + this.getClass().getCanonicalName().replace('.', '/') + "/";
   protected Prettify prettify;
 
   public PrettifyTest() {
   }
 
   protected static String getClassName() {
     return new Object() {
     }.getClass().getEnclosingClass().getName();
   }
 
   @BeforeClass
   public static void setUpClass() throws Exception {
     System.out.println("***** " + getClassName() + " *****");
   }
 
   @AfterClass
   public static void tearDownClass() throws Exception {
     System.out.println("******************************\r\n");
   }
 
   @Before
   public void setUp() {
     prettify = new Prettify();
   }
 
   @After
   public void tearDown() {
   }
 
   /**
    * Do test. The Java Prettify line numbering is different from JavaScript 
    * Prettify. If the result is adapted from JavaScript Prettify tests and 
    * that {@code resultText} have line numbering, set {@code removeNewLines} 
    * to true.
    * 
    * @param extension the file name of the source file
    * @param code the source code
    * @param removeJSLineNumbering true to remove new line and combine 
    * identical adjacent tag
    * 
    * @throws IOException error occurred when reading source or result file
    */
   public void test(String extension, String code, boolean removeJSLineNumbering) throws IOException {
     String source = new String(readFile(new File(packagePath + "source/" + code + ".txt")), "UTF-8");
     Job job = new Job(0, source);
     prettify.langHandlerForExtension(extension, source).decorate(job);
     List<Object> decorations = removeJSLineNumbering ? removeNewLines(job.getDecorations(), source) : job.getDecorations();
     List<Object> compare = readResult(new String(readFile(new File(packagePath + "result/" + code + ".txt")), "UTF-8").replace("&lt;", "<").replace("&gt;", ">").replace("&nbsp;", " ").replace("&amp;", "&"), removeJSLineNumbering);
 //        assertArrayEquals(code + "\n" + compare + "\n" + decorations, compare.toArray(), decorations.toArray()); // for debug
     assertArrayEquals(code, compare.toArray(), decorations.toArray());
   }
 
   @Test
   public void testBatch() throws IOException {
     System.out.println("+++++ testBatch +++++");
 
     test(null, "bash", false);
     test("sh", "bash_lang", true);
     test(null, "java", false);
     test("java", "java_lang", true);
     test(null, "C", false);
     test("c", "C_lang", false);
     test(null, "Cpp", false);
     test("cpp", "Cpp_lang", false);
     test(null, "javascript", false);
     test(null, "perl", false);
     test(null, "python", false);
     test("py", "python_lang", false);
     test(null, "PHP", false);
     test("coffee", "coffee", false);
     test("css", "css", false);
     test("go", "go", false);
     test(null, "html", false);
     test(null, "htmlXmp", false);
     test("html", "html_lang", false);
     test(null, "issue12", false);
     test("js", "issue12_lang", false);
     test(null, "issue14a", false);
     test(null, "issue14b", false);
     test(null, "issue20", false);
     test(null, "issue21", false);
     test("lua", "issue24", false);
     test("vb", "issue27", false);
     test("hs", "issue30", false);
     test("ml", "issue33", false);
     test(null, "issue4", false);
     test("el", "issue42", false);
     test(null, "issue45", false);
     test(null, "issue79", false);
     test(null, "issue8", false);
     test("java", "issue84", false);
     test(null, "issue92", false);
     test("cs", "issue93", false);
     test(null, "misc1", false);
     test("proto", "proto", false);
     test("scala", "scala", false);
     test("vhdl", "vhdl", false);
     test("wiki", "wiki", false);
     test(null, "xhtml", false);
     test(null, "xml", false);
     test(null, "xsl", false);
     test("yaml", "yaml1", false);
     test("yaml", "yaml2", false);
   }
 
   /**
    * Read the whole file and return the content in byte array.
    * @param file the file to read
    * @return the content of the file in byte array
    * @throws IOException error occurred when reading the content from the 
    * file
    */
   public static byte[] readFile(File file) throws IOException {
     if (file == null) {
       throw new NullPointerException("argument 'file' cannot be null");
     }
 
     long fileLength = file.length();
     byte[] content = new byte[(int) fileLength];
 
     FileInputStream fin = null;
     try {
       fin = new FileInputStream(file);
 
       int byteRead = 0, cumulateByteRead = 0;
       while ((byteRead = fin.read(content, cumulateByteRead, content.length - cumulateByteRead)) != -1) {
         cumulateByteRead += byteRead;
         if (cumulateByteRead >= fileLength) {
           break;
         }
       }
 
       if (cumulateByteRead != fileLength) {
         throw new IOException("The total number of bytes read does not match the file size. Actual file size: " + fileLength + ", bytes read: " + cumulateByteRead + ", path: " + file.getAbsolutePath());
       }
     } finally {
       if (fin != null) {
         try {
           fin.close();
         } catch (IOException ex) {
         }
       }
     }
 
     return content;
   }
 
   /**
    * Determine the positions of new line characters in {@code source} and 
    * adjust the position value in {@code decorations}. Finally, it will 
    * combine adjacent identical tags.
    * 
    * @param decorations see {@link prettify.Job#decorations}
    * @param source the source code in string
    * 
    * @return the adjusted list of {@code decorations}
    */
   public static List<Object> removeNewLines(List<Object> decorations, String source) {
     if (decorations == null) {
       throw new NullPointerException("argument 'decorations' cannot be null");
     }
     if (source == null) {
       throw new NullPointerException("argument 'source' cannot be null");
     }
 
     List<Object> _decorations = new ArrayList<Object>(decorations);
 
     StringBuffer sb = new StringBuffer();
     Pattern pattern = Pattern.compile("[\r\n]");
     Matcher matcher = pattern.matcher(source);
     while (matcher.find()) {
       matcher.appendReplacement(sb, "");
 
       int posStart = sb.length();
       ListIterator<Object> iterator = _decorations.listIterator();
       while (iterator.hasNext()) {
         Integer pos = (Integer) iterator.next();
         if (pos > posStart) {
           iterator.set(pos - 1);
         }
         iterator.next();
       }
     }
 //        matcher.appendTail(sb);
 
     return Util.removeDuplicates(_decorations, source);
   }
 
   /**
    * Parse the {@code resultText}. The Java Prettify line numbering is 
    * different from JavaScript Prettify. If the result is adapted from 
    * JavaScript Prettify tests and that {@code resultText} have line 
    * numbering, set {@code removeNewLines} to true. Also, the 
    * {@code resultText} should not escape html special characters
    * 
    * @param resultText the result in string
    * @param removeJSLineNumbering true to remove new line and combine identical 
    * adjacent tag
    * 
    * @return the parsed result with format similar to 
    * {@link prettify.Job#decorations}
    */
   public static List<Object> readResult(String resultText, boolean removeJSLineNumbering) {
     if (resultText == null) {
       throw new NullPointerException("argument 'resultText' cannot be null");
     }
     String _resultText = resultText;
 
     List<Object> returnList = new ArrayList<Object>();
 
     if (removeJSLineNumbering) {
       _resultText = _resultText.replaceAll("[\r\n]", "");
 
       StringBuffer sb = new StringBuffer();
       Pattern pattern = Pattern.compile("(`[A-Z]{3})([^`]+?)`END\\1([^`]+?)`END", Pattern.MULTILINE | Pattern.DOTALL);
       Matcher matcher = pattern.matcher(_resultText);
       while (matcher.find()) {
         matcher.appendReplacement(sb, "");
         sb.append(matcher.group(1)).append(matcher.group(2)).append(matcher.group(3)).append("`END");
       }
       matcher.appendTail(sb);
 
       _resultText = sb.toString();
     }
 
     int count = 0;
     Pattern pattern = Pattern.compile("`([A-Z]{3})(.+?)`END", Pattern.MULTILINE | Pattern.DOTALL);
     Matcher matcher = pattern.matcher(_resultText);
     while (matcher.find()) {
       returnList.add(count);
       returnList.add(matcher.group(1).toLowerCase());
       count += matcher.group(2).length();
     }
 
     returnList = Util.removeDuplicates(returnList, _resultText);
 
     // for debug
 //        StringBuffer sb = new StringBuffer();
 //        matcher = pattern.matcher(resultText);
 //        while (matcher.find()) {
 //            matcher.appendReplacement(sb, "");
 //            sb.append(matcher.group(2));
 //        }
 //        matcher.appendTail(sb);
 //        String plainCode = sb.toString();
 //
 //        System.out.println(plainCode);
 //
 //        for (int i = 0, iEnd = returnList.size(); i < iEnd; i += 2) {
 //            int end = i + 2 < iEnd ? (Integer) returnList.get(i + 2) : plainCode.length();
 //            System.out.println(returnList.get(i) + ": " + returnList.get(i + 1) + ": " + plainCode.substring((Integer) returnList.get(i), end));
 //        }
 
     return returnList;
   }
 }
