 /*
  * Copyright (c) 2012, Mikael Svensson
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the copyright holder nor the names of the
  *       contributors of this software may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL MIKAEL SVENSSON BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package info.mikaelsvensson.devtools.xml.documentcreator;
 
 import enumeration.Fruit;
 import info.mikaelsvensson.devtools.doclet.xml.documentcreator.EnumDocumentCreator;
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.ParserConfigurationException;
 import java.io.IOException;
 import java.net.URISyntaxException;
 
 
 public class EnumDocumentCreatorTest extends AbstractDocumentCreatorTest {
     /**
      * Sample comment with a nice picture of a cloud: {@image resources/cloud.png}.
      * <p/>
      * Class:
      * {@embed class info.mikaelsvensson.devtools.ClassA}
      *
      * Result:
      * {@embed file resources/ClassA.standard.xml}
      */
     @Test
     public void testFruit() throws Exception {
         performTest(Fruit.class);
     }
 
    private void performTest(final Class<?> cls) throws IOException, URISyntaxException, SAXException, ParserConfigurationException {
         performTest(EnumDocumentCreator.NAME, cls, "-format.property." + EnumDocumentCreator.PARAMETER_CLASS_FOLDER, ".\\target\\classes");
     }
 
     @Override
     protected Node findClassElement(final Class cls, final Document doc) {
         return AbstractDocumentCreatorTest.findClassElementByQName(cls, doc, "enum", "qualified-name");
     }
 }
