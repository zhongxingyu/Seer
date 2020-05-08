 /*
  * Copyright (c) 2010,2011 --chris--
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  * 3. The name of the author may not be used to endorse or promote products
  *    derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR "AS IS" AND ANY EXPRESS OR
  * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package yeti.lang.compiler;
 
 
 import java.lang.reflect.Method;
 import junit.framework.TestCase;
 import org.yetidoc.*;
 
 /**
  *
  * @author Christian
  */
 public class YeticlDocMakerTest extends TestCase {
     
  
     public YeticlDocMakerTest(String testName) {
         super(testName);
     }
 
     protected void setUp() throws Exception {
         super.setUp();
     }
 
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
     /**
      * Test of docForSrcs method, of class YetiDocMaker.
      */
     public void testDocForSrcs() throws Exception {
         System.out.println("Path: " +(new java.io.File(".")).getAbsolutePath());
         //YetiDocModuleType[] mta = YetiDocMaker.docForSrcs(new String[]{}, new String[]{}, new String[]{"yeti/lang/compiler/repl.yeti"});
         YetiDocModuleType[] mta = YetiDocMaker.docForSrcs(new String[]{"src/testdocsrc/"}, new String[]{}, new String[]{"std.yeti"});
         assertEquals (mta.length,1);
         if(mta[0].getException() != null) {
             mta[0].getException().printStackTrace();
             throw mta[0].getException();
         }
     }
 
     public void testYetiDocForDir() throws Exception {
         YetiDocJavaApi.writeDocForDir(new String[]{},"src/testdocsrc","target/unittestdocssrc/yetidocfordir","");
     }
 
     public void testYetiDoc() throws Exception {
         YetiDocJavaApi.writeDocFor(null,new String[]{"src/testdocsrc"},
                 new String[]{"yeti/lang/std.yeti","some1.yeti"},
                 "target/unittestdocssrc/yetidoc","");
     }
 
     public void testApp() throws Exception {
         String[] args = new String[]{
           "-d","target/unittestdocssrc/app",
           "-sd","src/testdocsrc",
           "-fm","testApp",
           "yeti/lang/std.yeti","some1.yeti"
         };
 
         Class cl = this.getClass().getClassLoader().loadClass("org.yetidoc.yetidoc");
         Method m = cl.getMethod("main", String[].class);
         m.invoke(null,(Object)args);
     }
 
 
 }
