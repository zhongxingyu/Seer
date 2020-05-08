 /*
  * Copyright (c) 2001-2005, Mikael Stldal
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions 
  * are met:
  * 
  * 1. Redistributions of source code must retain the above copyright 
  * notice, this list of conditions and the following disclaimer.
  * 
  * 2. Redistributions in binary form must reproduce the above copyright 
  * notice, this list of conditions and the following disclaimer in the 
  * documentation and/or other materials provided with the distribution.
  * 
  * 3. Neither the name of the author nor the names of its contributors 
  * may be used to endorse or promote products derived from this software 
  * without specific prior written permission. 
  * 
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
  * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR 
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY 
  * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * 
  * 
  * Note: This is known as "the modified BSD license". It's an approved 
  * Open Source and Free Software license, see 
  * http://www.opensource.org/licenses/ 
  * and
  * http://www.gnu.org/philosophy/license-list.html
  */
 
 package nu.staldal.lagoon.producer;
 
 import java.io.*;
 import java.util.Properties;
 
 import javax.xml.transform.*;
 import javax.xml.transform.sax.*;
 import javax.xml.transform.stream.StreamResult;
 import org.xml.sax.*;
 
 import nu.staldal.lagoon.core.*;
 import nu.staldal.xmlutil.ContentHandlerFixer;
 
 
 public class XMLFormatter extends Format
 {
 	private SAXTransformerFactory tfactory;
 	private Properties outputProperties;
 	private boolean isHTML;
 	
     public void init() throws LagoonException
     {
 		TransformerFactory tf = TransformerFactory.newInstance();
         if (!(tf.getFeature(SAXTransformerFactory.FEATURE)
               	&& tf.getFeature(StreamResult.FEATURE)))
         {
             throw new LagoonException("The transformer factory "
                 + tf.getClass().getName() + " doesn't support SAX");
         }
             
 		tfactory = (SAXTransformerFactory)tf;
 
         outputProperties = new Properties();
 
         String method = getParam("method");
         if (method == null)
             method = "XML";
 
         int _html;
         String html = getParam("html");
         if (html == null)
             _html = 1;
         else if (html.equals("transitional"))
             _html = 1;
         else if (html.equals("frameset"))
             _html = 2;
         else if (html.equals("strict"))
             _html = 3;
         else
             throw new LagoonException("Unknown html variant");
 
 		isHTML = false;
 			
         if (method.equals("XML"))
         {
 			outputProperties.setProperty(OutputKeys.METHOD, "xml");
 			outputProperties.setProperty(OutputKeys.ENCODING, "UTF-8");
         }
         else if (method.equals("HTML"))			
         {
 			isHTML = true;
 			outputProperties.setProperty(OutputKeys.METHOD, "html");
             switch (_html)
             {
             case 1:
 				outputProperties.setProperty(OutputKeys.DOCTYPE_PUBLIC,
 					"-//W3C//DTD HTML 4.01 Transitional//EN");
 				outputProperties.setProperty(OutputKeys.DOCTYPE_SYSTEM,
                    	"http://www.w3.org/TR/html4/loose.dtd");
                 break;
             case 2:
 				outputProperties.setProperty(OutputKeys.DOCTYPE_PUBLIC,
 					"-//W3C//DTD HTML 4.01 Frameset//EN");
 				outputProperties.setProperty(OutputKeys.DOCTYPE_SYSTEM,
                    	"http://www.w3.org/TR/html4/frameset.dtd");
                 break;
             case 3: 
 				outputProperties.setProperty(OutputKeys.DOCTYPE_PUBLIC,
 					"-//W3C//DTD HTML 4.01//EN");
 				outputProperties.setProperty(OutputKeys.DOCTYPE_SYSTEM,
                    	"http://www.w3.org/TR/html4/strict.dtd");
                 break;
             }
 			outputProperties.setProperty(OutputKeys.ENCODING, "iso-8859-1");
         }
         else if (method.equals("XHTML"))
         {
 			outputProperties.setProperty(OutputKeys.METHOD, "xhtml");
             switch (_html)
             {
             case 1: 
 				outputProperties.setProperty(OutputKeys.DOCTYPE_PUBLIC,
 					"-//W3C//DTD XHTML 1.0 Transitional//EN");
 				outputProperties.setProperty(OutputKeys.DOCTYPE_SYSTEM,
                     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");
                 break;
             case 2: 
 				outputProperties.setProperty(OutputKeys.DOCTYPE_PUBLIC,				
 					"-//W3C//DTD XHTML 1.0 Frameset//EN");
 				outputProperties.setProperty(OutputKeys.DOCTYPE_SYSTEM,
                    	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd");
                 break;
             case 3: 
 				outputProperties.setProperty(OutputKeys.DOCTYPE_PUBLIC,
 					"-//W3C//DTD XHTML 1.0 Strict//EN");
 				outputProperties.setProperty(OutputKeys.DOCTYPE_SYSTEM,
                 	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd");
                 break;
             }
 			outputProperties.setProperty(OutputKeys.ENCODING, "iso-8859-1");
         }
         else if (method.equals("TEXT"))
         {
 			outputProperties.setProperty(OutputKeys.METHOD, "text");
 			outputProperties.setProperty(OutputKeys.ENCODING, "iso-8859-1");
         }
         else
             throw new LagoonException("Unknown serializing method");
 
         String enc = getParam("encoding");
         if (enc != null) 
 			outputProperties.setProperty(OutputKeys.ENCODING, enc);
 
         String docPub = getParam("doctype-public");
 		if (docPub != null)
 			outputProperties.setProperty(OutputKeys.DOCTYPE_PUBLIC, docPub);
 			
         String docSys = getParam("doctype-system");
         if (docSys != null)
 			outputProperties.setProperty(OutputKeys.DOCTYPE_SYSTEM, docSys);
 
         String indent = getParam("indent");
         if (indent != null)
 			outputProperties.setProperty(OutputKeys.INDENT, "yes");
 		else
 			outputProperties.setProperty(OutputKeys.INDENT, "no");
         
         String omitXmlDeclaration = getParam("omit-xml-declaration");
         if (omitXmlDeclaration != null)
 			outputProperties.setProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
 		else
 			outputProperties.setProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
             
     }
 
     public void start(OutputStream bytes, Target target)
         throws IOException, SAXException
     {
 		try {
			TransformerHandler th = tfactory.newTransformerHandler();
			th.setResult(new StreamResult(bytes));
		
			Transformer trans = th.getTransformer();
			trans.setOutputProperties(outputProperties);
				
         	getNext().start(new ContentHandlerFixer(th, !isHTML, isHTML), target);
 		}
 		catch (TransformerConfigurationException e)
 		{
 			throw new LagoonException(e.getMessage());
 		}
 	}
 
     public boolean hasBeenUpdated(long when)
         throws LagoonException, IOException
     {
         return getNext().hasBeenUpdated(when);
     }
 
 }
