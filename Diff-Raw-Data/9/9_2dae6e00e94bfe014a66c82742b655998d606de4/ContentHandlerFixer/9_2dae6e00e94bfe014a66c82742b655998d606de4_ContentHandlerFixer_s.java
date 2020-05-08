 /*
  * Copyright (c) 2001-2002, Mikael Stldal
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
 
 package nu.staldal.xmlutil;
 
 import java.util.*;
 
 import org.xml.sax.*;
 import org.xml.sax.helpers.*;
 
 
 /**
  * A filter to add missing qName parameters to SAX2 ContentHandler
  * events for elements and attributes.
  */
 public class ContentHandlerFixer implements ContentHandler
 {
 	private static final boolean DEBUG = false;
 
     private final boolean nsDecl;
 	private final ContentHandler ch;
     
 	private NamespaceSupport nsSup;
     private boolean contextPushed;
 	private int prefixNum;
 
 
 	/**
 	 * Constructs a filter.
 	 *
 	 * @param ch  the SAX2 ContentHandler to fire events on.
 	 */
     public ContentHandlerFixer(ContentHandler ch)
     {
 		this(ch, false);
     }
 
 
 	/**
 	 * Constructs a filter.
 	 *
 	 * @param ch  the SAX2 ContentHandler to fire events on.
 	 * @param nsDecl  add namespace declarations as explicit 'xmlns' attributes.
 	 */
     public ContentHandlerFixer(ContentHandler ch, boolean nsDecl)
     {
 		this.ch = ch;
         this.nsDecl = nsDecl;
 
         nsSup = new NamespaceSupport();
         contextPushed = false;
 		prefixNum = 0;
 		if (DEBUG) System.out.println("New ContentHandlerFixer");
     }
 	
 	
 	private String genPrefix()
 	{
 		return "ns" + (++prefixNum);
 	}
 	
 	
     // ContentHandler implementation
 
     public void setDocumentLocator(Locator locator)
     {
         // ch.setDocumentLocator(locator);
     }
 
     public void startDocument()
         throws SAXException
     {
 		if (DEBUG) System.out.println("startDocument");
         ch.startDocument();
     }
 
     public void endDocument()
         throws SAXException
     {
 		if (DEBUG) System.out.println("endDocument");
         ch.endDocument();
     }
 
     public void startElement(String namespaceURI, String localName,
                              String qname, Attributes atts)
         throws SAXException
     {
 		if (DEBUG) System.out.println("startElement("+namespaceURI+
             ','+localName+','+qname+')');
 
         if (!contextPushed)
         {
             nsSup.pushContext();
         }
         contextPushed = false;
 
         String name;
         if (qname != null && qname.length() > 0)
             name = qname;
         else
         {
             String prefix = nsSup.getPrefix(namespaceURI);
             if (prefix == null)
             {
				if ((namespaceURI == null) || (namespaceURI.length() < 1))
 				{
 					prefix = "";
 				}
 				else
 				{
 					String defaultURI = nsSup.getURI("");
 	                if ((defaultURI != null) && defaultURI.equals(namespaceURI))
 	                    prefix = ""; // default namespace
 	                else
 					{
 	                    prefix = genPrefix();
 						nsSup.declarePrefix(prefix, namespaceURI);
 					}
 			 	}
             }
             name = ((prefix.length() == 0) ? "" : (prefix + ':')) + localName;
         }
 
         AttributesImpl newAtts = new AttributesImpl();
         for (int i = 0; i < atts.getLength(); i++)
         {
             String aname = atts.getQName(i);
             if ((aname == null) || (aname.length() == 0))
             {
                 String uri = atts.getURI(i);
                 String alocalName = atts.getLocalName(i);
                 if (uri.length() == 0)
                 {
                     aname = alocalName;
                 }
                 else
                 {
                     String prefix = nsSup.getPrefix(uri);
                     if (prefix == null)
 					{
                        	prefix = genPrefix();
 						nsSup.declarePrefix(prefix, namespaceURI);
 					}
                     aname = prefix + ':' + alocalName;
                 }
             }
             newAtts.addAttribute(atts.getURI(i), atts.getLocalName(i), aname, 
 				atts.getType(i), atts.getValue(i));
         }
 
 		if (nsDecl)
 		{
 			for (Enumeration e = nsSup.getDeclaredPrefixes(); e.hasMoreElements(); )
 			{
 				String prefix = (String)e.nextElement();
 				String uri = nsSup.getURI(prefix);
 				if (prefix.length() == 0)
 				{
 					newAtts.addAttribute("", "xmlns", "xmlns", "CDATA", uri);
 				}
 				else
 				{
 					newAtts.addAttribute("", "xmlns", "xmlns:"+prefix, "CDATA", uri);
 				}
 			}
 		}
 
         for (Enumeration e = nsSup.getDeclaredPrefixes(); e.hasMoreElements(); )
         {
             String prefix = (String)e.nextElement();
             String uri = nsSup.getURI(prefix);
 			
 			if (DEBUG) System.out.println("prefix=" + prefix + "  uri=" + uri);
 			
 			ch.startPrefixMapping(prefix, uri);
         }
 						
         ch.startElement(namespaceURI, localName, name, newAtts);
     }
 
     public void endElement(String namespaceURI, String localName,
                            String qname)
         throws SAXException
     {
 		if (DEBUG) System.out.println("endElement("+namespaceURI+','+
             localName+','+qname+')');
 
         String name;
         if (qname != null && qname.length() > 0)
             name = qname;
         else
         {
             String prefix = nsSup.getPrefix(namespaceURI);
             if (prefix == null)
             {
				if ((namespaceURI == null) || (namespaceURI.length() < 1))
 				{
 					prefix = "";
 				}
 				else
 				{
 					String defaultURI = nsSup.getURI("");
 	                if ((defaultURI != null) && defaultURI.equals(namespaceURI))
 	                    prefix = ""; // default namespace
 	                else
 					{
 						throw new Error("No prefix for " + namespaceURI);
 					}	                    
 			 	}
             }
             name = ((prefix.length() == 0) ? "" : (prefix + ':')) + localName;
         }
 
         ch.endElement(namespaceURI, localName, name);
 
         for (Enumeration e = nsSup.getDeclaredPrefixes(); e.hasMoreElements(); )
         {
             String prefix = (String)e.nextElement();
 			ch.endPrefixMapping(prefix);
         }
 
         nsSup.popContext();
     }
 
     public void startPrefixMapping(String prefix, String uri)
         throws SAXException
     {
 		if (DEBUG) System.out.println("startPrefixMapping("+
 			((prefix.length() == 0) ? "<default>" : prefix) +','+uri+')');
 
         if (!contextPushed)
         {
             nsSup.pushContext();
             contextPushed = true;
         }
 
         nsSup.declarePrefix(prefix, uri);
     }
 
     public void endPrefixMapping(String prefix)
         throws SAXException
     {
 		if (DEBUG) System.out.println("endPrefixMapping("+
 			((prefix.length() == 0) ? "<default>" : prefix)+')');
     }
 
     public void characters(char[] chars, int start, int length)
         throws SAXException
     {
         ch.characters(chars, start, length);
     }
 
     public void ignorableWhitespace(char[] chars, int start, int length)
         throws SAXException
     {
         ch.ignorableWhitespace(chars, start, length);
     }
 
     public void processingInstruction(String target, String data)
         throws SAXException
     {
 		if (DEBUG) System.out.println("processingInstruction("+target+','+
 			data+')');
 
         ch.processingInstruction(target, data);
     }
 
     public void skippedEntity(String name)
         throws SAXException
     {
 		if (DEBUG) System.out.println("skippedEntity("+name+')');
 
         ch.skippedEntity(name);
     }
 
 }
