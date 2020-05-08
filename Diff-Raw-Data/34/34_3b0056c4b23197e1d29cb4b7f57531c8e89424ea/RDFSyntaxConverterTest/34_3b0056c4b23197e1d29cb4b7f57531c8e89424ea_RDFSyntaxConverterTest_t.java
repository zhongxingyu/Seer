 /**
  * Copyright (c) 2012  Jose Cruz-Toledo
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.semanticscience.converter;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  * @author  Jose Cruz-Toledo
  *
  */
 public class RDFSyntaxConverterTest {
 
 	private static File inputFile;
 	private static  File outputFile;
 	private static RDFSyntaxConverter rsc;
 	private static RDFSyntaxConverter rsc2;
 	@BeforeClass
 	public static void setup(){
		inputFile = new File("/tmp/input");
		outputFile = new File("/tmp/output");
 		try {
 			rsc = new RDFSyntaxConverter(inputFile, outputFile);
 		} catch (NullPointerException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
		/*try {
 			rsc2 = new RDFSyntaxConverter(inputFile, outputFile, "N-TRIPLE");
 		} catch (NullPointerException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
		}*/
 	}
 	
 	@AfterClass
 	public static void teardown(){
 		
 	}
 
 	@Test
 	public void test() {
 		
 	}
 
 }
