 /**
  * The Subversion Authentication Parse Module (SAPM for short).
  *
  * Copyright (c) 2010, 2011 by SoftwareEntwicklung Beratung Schulung (SoEBeS)
  * Copyright (c) 2010, 2011 by Karl Heinz Marbaise
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
  *
  * The License can viewed online under http://www.gnu.org/licenses/gpl.html
  * If you have any questions about the Software or about the license
  * just write an email to license@soebes.de
  */
 package com.soebes.subversion.sapm;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 
 import org.antlr.runtime.ANTLRInputStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.tree.Tree;
 import org.testng.annotations.Test;
 
 import com.soebes.subversion.sapm.parser.SAFPLexer;
 import com.soebes.subversion.sapm.parser.SAFPParser;
 
 /**
  *
  *
  *<pre>
  * [/] = r
  *
  * [aliases]
  * harry = CN=Harold Hacker,OU=Engineers,DC=red-bean,DC=com
  * sally = CN=Sally Swatterbug,OU=Engineers,DC=red-bean,DC=com
  * joe = CN=Gerald I. Joseph,OU=Engineers,DC=red-bean,DC=com
  *
  * [groups]
  * calc-developers = &harry, &sally, &joe
  * paint-developers = &frank,&sally, &jane
  * everyone = @calc-developers, @paint-developers
  *
  * [groups]
  * calc-developers = harry, sally, joe
 * paint-developers = frank, sally, jane
 * everyone = @calc-developers, @paint-developers
  *
  * [calc:/projects/calc]
  * @calc-developers = rw
  *
  * [paint:/projects/paint] jane = r
  * @paint-developers = rw
  * </pre>
  */
 public class AppTest extends TestBase {
 
     @Test
     public void testReadOne() throws IOException, RecognitionException {
         FileInputStream fis = new FileInputStream(
                 getFileResource("/svnaccess-1.conf"));
         ANTLRInputStream stream = new ANTLRInputStream(fis);
         SAFPLexer lexer = new SAFPLexer(stream);
         CommonTokenStream tokens = new CommonTokenStream(lexer);
         SAFPParser parser = new SAFPParser(tokens);
         parser.prog();
     }
 
     @Test
     public void testReadTwo() throws IOException, RecognitionException {
         FileInputStream fis = new FileInputStream(
                 getFileResource("/svnaccess-2.conf"));
         ANTLRInputStream stream = new ANTLRInputStream(fis);
         SAFPLexer lexer = new SAFPLexer(stream);
         CommonTokenStream tokens = new CommonTokenStream(lexer);
         SAFPParser parser = new SAFPParser(tokens);
         parser.prog();
 
     }
 
     @Test
     public void testReadThree() throws IOException, RecognitionException {
         FileInputStream fis = new FileInputStream(
                 getFileResource("/svnaccess-3.conf"));
         ANTLRInputStream stream = new ANTLRInputStream(fis);
         SAFPLexer lexer = new SAFPLexer(stream);
         CommonTokenStream tokens = new CommonTokenStream(lexer);
         SAFPParser parser = new SAFPParser(tokens);
         parser.prog();
     }
 
     @Test
     public void testReadAST() throws IOException, RecognitionException {
         FileInputStream fis = new FileInputStream(
                 getFileResource("/svnaccess-3.conf"));
         ANTLRInputStream stream = new ANTLRInputStream(fis);
         SAFPLexer lexer = new SAFPLexer(stream);
         CommonTokenStream tokens = new CommonTokenStream(lexer);
         SAFPParser parser = new SAFPParser(tokens);
         SAFPParser.prog_return result = parser.prog();
         Tree t = (Tree) result.getTree();
         System.out.println("AST:" + t.toStringTree());
     }
 
 }
