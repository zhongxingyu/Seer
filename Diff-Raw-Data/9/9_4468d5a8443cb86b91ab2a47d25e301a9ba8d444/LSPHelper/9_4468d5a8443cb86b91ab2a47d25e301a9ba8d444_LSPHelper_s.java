 /*
  * Copyright (c) 2004, Mikael Stldal
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
 
 package nu.staldal.lsp;
 
 import java.io.*;
 import java.util.*;
 
 import org.xml.sax.*;
 import javax.xml.transform.*;
 import javax.xml.transform.sax.*;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.transform.stream.StreamResult;
 
 
 /**
  * Helper class for loading and executing an LSP pages and serialize 
  * the output to a byte stream.
  *<p>
  * This class should generally be considered as Singleton, usually only
  * one instance is required. An instance of this class is thread-safe,
  * except for the <code>set<em>XXX</em></code> methods which should not be 
  * invoked concurrently with any other method.
  */
 public class LSPHelper
 {
     private final ClassLoader classLoader;
     private final Map lspPages;
     private final Map stylesheets;
 	private final SAXTransformerFactory tfactory;
 
     private String htmlType = "text/html";
     private String xhtmlType = "text/html";
     private String xmlType = "text/xml";
     private String textType = "text/plain";	
 
     private String htmlEncoding = "iso-8859-1";
     private String xhtmlEncoding = "iso-8859-1";
     private String xmlEncoding = "UTF-8";
     private String textEncoding = "iso-8859-1";	
 	
     
 	/**
      * Create an LSPHelper.
      *     
      * @param classLoader   {@link java.lang.ClassLoader} 
      *          to load LSP pages and stylesheet source with
      */
     public LSPHelper(ClassLoader classLoader) 
 	{
         this.classLoader = classLoader;
 
 		this.lspPages = new HashMap();
 		this.stylesheets = new HashMap();
 
 		TransformerFactory tf = TransformerFactory.newInstance();
         if (!(tf.getFeature(SAXTransformerFactory.FEATURE)
               	&& tf.getFeature(StreamResult.FEATURE)))
         {
             throw new Error("The transformer factory "
                 + tf.getClass().getName() + " doesn't support SAX");
         }            
 		this.tfactory = (SAXTransformerFactory)tf;
 	}
 
     
     /**
      * Set the default media-type for HTML.
      *<p>
      * Default is "text/html", and this should not be changed. 
      */
     public void setHtmlType(String htmlType)
     {
         this.htmlType = htmlType;    
     }
 
     
     /**
      * Set the default media-type for XHTML.
      *<p>
      * Default is "text/html", but another reasonable choice is 
      * "application/xhtml+xml". 
      */
     public void setXhtmlType(String xhtmlType)
     {
         this.xhtmlType = xhtmlType;    
     }
     
 
     /**
      * Set the default media-type for XML.
      *<p>
      * Default is "text/xml", but another reasonable choice is 
      * "application/xml". 
      */
     public void setXmlType(String xmlType)
     {
         this.xmlType = xmlType;    
     }
     
 
     /**
      * Set the default media-type for TEXT.
      *<p>
      * Default is "text/plain". 
      */
     public void setTextType(String textType)
     {
         this.textType = textType;    
     }
 
     
     /**
      * Set the default character encoding for HTML.
      *<p>
      * Default is "iso-8859-1". 
      */
     public void setHtmlEncoding(String htmlEncoding)
     {
         this.htmlEncoding = htmlEncoding;    
     }
 
     
     /**
      * Set the default character encoding for XHTML.
      *<p>
      * Default is "iso-8859-1". 
      */
     public void setXhtmlEncoding(String xhtmlEncoding)
     {
         this.xhtmlEncoding = xhtmlEncoding;    
     }
     
 
     /**
      * Set the default character encoding for XML.
      *<p>
      * Default is "UTF-8". 
      */
     public void setXmlEncoding(String xmlEncoding)
     {
         this.xmlEncoding = xmlEncoding;    
     }
     
 
     /**
      * Set the default character encoding for TEXT.
      *<p>
      * Default is "iso-8859-1". 
      */
     public void setTextEncoding(String textEncoding)
     {
         this.textEncoding = textEncoding;    
     }
 
 
 	/**
 	 * Get an compiled XSLT stylesheet.
      *<p>
      * The compiled stylesheets are cached and reused. Stylesheet source
      * is loaded from the supplied {@link java.lang.ClassLoader}.      
 	 *
 	 * @param stylesheetName  the name of the XSLT stylesheet
 	 *
 	 * @return <code>null</code> if the given stylesheet is not found
      *
      * @throws TransformerConfigurationException  if the stylesheet cannot be compiled
 	 */
 	public synchronized Templates getStylesheet(String stylesheetName)
         throws TransformerConfigurationException
 	{
 		Templates compiledStylesheet = (Templates)stylesheets.get(stylesheetName);
 		
 		if (compiledStylesheet == null)
 		{
 			compiledStylesheet = loadStylesheet(stylesheetName);
 		}
 		
 		return compiledStylesheet;
 	}
 	
 	
 	/**
 	 * @return <code>null</code> if not found.
 	 */
 	private Templates loadStylesheet(String stylesheetName)
         throws TransformerConfigurationException
 	{
         java.net.URL stylesheetURL = classLoader.getResource(stylesheetName);
         if (stylesheetURL == null) return null;
         String stylesheetData = stylesheetURL.toExternalForm();
         
         StreamSource stylesheetSource = new StreamSource(stylesheetData);
         
         Templates compiledStylesheet = 
             tfactory.newTemplates(stylesheetSource);
         
         stylesheets.put(stylesheetName, compiledStylesheet);			
         
         return compiledStylesheet;            
 	}
 
 
 	/**
 	 * Get the {@link nu.staldal.lsp.LSPPage} instance for a given page name.
      *<p>
      * {@link nu.staldal.lsp.LSPPage} instances are cached and reused.      
 	 *
 	 * @param pageName  the name of the LSP page
 	 *
 	 * @return <code>null</code> if the given page is not found
      *
      * @throws InstantiationException  if the LSP page cannot be loaded
      * @throws IllegalAccessException  if the LSP page cannot be loaded
      * @throws VerifyError  if the LSP page is damaged 
 	 */
 	public synchronized LSPPage getPage(String pageName)
         throws InstantiationException, IllegalAccessException, VerifyError 
 	{
 		LSPPage page = (LSPPage)lspPages.get(pageName);
 		
 		if (page == null)
 		{
 			page = loadPage(pageName);
 		}
 		
 		return page;
 	}
 	
 	
 	/**
 	 * @return <code>null</code> if not found.
 	 */
 	private LSPPage loadPage(String pageName)
         throws InstantiationException, IllegalAccessException, VerifyError 
 	{
 		try {
 			Class pageClass = Class.forName("_LSP_"+pageName, true, 
 				classLoader);
 
 			LSPPage page = (LSPPage)pageClass.newInstance();
             
             fixOutputProperties(page.getOutputProperties());
 			
 			lspPages.put(pageName, page);			
 			
 		  	return page;            
 		}
 		catch (ClassNotFoundException e)
 		{
 			return null;
 		}				
 	}
 
 
     /**
      * Get Content-Type for the LSP page. If the LSP page specifies a 
      * stylesheet, the Content-Type for the stylesheet will be returned.
      *<p>
      * Includes encoding with the "charset" parameter.
      */
     public String getContentType(LSPPage thePage)
     {
         Properties outputProperties = thePage.getOutputProperties();
         String stylesheet = outputProperties.getProperty("stylesheet");
         if (stylesheet != null)
         {
             try {
                 Templates compiledStylesheet = getStylesheet(stylesheet);
                 if (compiledStylesheet != null)
                 {
                     return getContentType(compiledStylesheet);    
                 }
             }
             catch (TransformerConfigurationException ignore) {}
         }
         return getContentType(outputProperties);
     }
 
     
     /**
      * Get Content-Type for the compiled stylesheet.
      *<p>
      * Includes encoding with the "charset" parameter.
      */
     public String getContentType(Templates compiledStylesheet)
     {
         Properties outputProperties = compiledStylesheet.getOutputProperties();
         
         return getContentType(outputProperties);
     }
 
     
     private String getContentType(Properties outputProperties)
     {
         return outputProperties.getProperty(OutputKeys.MEDIA_TYPE)+
             "; charset="+outputProperties.getProperty(OutputKeys.ENCODING);
     }
     
 
 	/**
 	 * Executes an LSP page and serialize the result to an 
 	 * {@link java.io.OutputStream}. Uses any stylesheet specified in the 
      * LSP page. 
 	 *
  	 * @param thePage     the LSP page
 	 * @param lspParams   parameters to the LSP page
 	 * @param extContext  external context which will be passed to ExtLibs
 	 * @param out         the {@link java.io.OutputStream}
      *
      * @throws FileNotFoundException  if the stylesheet cannot be found
      * @throws SAXException  if any error occurs while executing the page
      * @throws IOException   if any I/O error occurs while executing the page
 	 */	
 	public void executePage(LSPPage thePage, Map lspParams, Object extContext,
 							OutputStream out)
 		throws FileNotFoundException, SAXException, IOException
 	{
 		ContentHandler sax;						
 		try {
             TransformerHandler th;
 
 			Properties outputProperties = thePage.getOutputProperties();
             String stylesheetName = outputProperties.getProperty("stylesheet");
             if (stylesheetName != null)
             {
                 Templates compiledStylesheet = getStylesheet(stylesheetName);
                 if (compiledStylesheet == null)                    
                     throw new FileNotFoundException(stylesheetName);
                 
                 outputProperties = compiledStylesheet.getOutputProperties();
 
                 th = tfactory.newTransformerHandler(
                     compiledStylesheet);
             }
             else
             {
                 th = tfactory.newTransformerHandler();
             }
 
 			th.setResult(new StreamResult(out));
 		
 			Transformer trans = th.getTransformer();
 			
             boolean isHtml = outputProperties.getProperty(OutputKeys.METHOD)
                 .equals("html");
 
 			trans.setOutputProperties(outputProperties);			
             
 			sax = new ContentHandlerFixer(th, !isHtml, isHtml);
 		}
 		catch (TransformerConfigurationException e)
 		{
 			throw new SAXException(e.getMessage());
 		}
 					
 		sax.startDocument();
 		thePage.execute(sax, lspParams, extContext);
 		sax.endDocument();
     }
 
 
 	/**
 	 * Executes an LSP page and transform the the result with an
      * XSLT stylesheet.
      *<p>
      * The output properties specified in the stylesheet will be used, 
      * and those specified in the LSP page will be ignored. Also, the default
      * output properties specified in this class will be ignored. Make sure
     * to specify the output method in the stylesheet using <xsl:output>.
 	 *
  	 * @param thePage             the LSP page
 	 * @param lspParams           parameters to the LSP page
 	 * @param extContext          external context which will be passed to ExtLibs
      * @param compiledStylesheet  the compiled XSLT stylesheet
 	 * @param out                 the {@link java.io.OutputStream}
      *
      * @throws SAXException  if any error occurs while executing the page
      * @throws IOException   if any I/O error occurs while executing the page
 	 */	
 	public void executePage(LSPPage thePage, Map lspParams, Object extContext,
 							Templates compiledStylesheet, OutputStream out)
 		throws SAXException, IOException
 	{
 		ContentHandler sax;						
 		try {
 			Properties outputProperties = 
                 compiledStylesheet.getOutputProperties();
 
 			TransformerHandler th = tfactory.newTransformerHandler(
                 compiledStylesheet);
 			th.setResult(new StreamResult(out));
 		
 			Transformer trans = th.getTransformer();
 			
             boolean isHtml = outputProperties.getProperty(OutputKeys.METHOD)
                 .equals("html");
 
 			trans.setOutputProperties(outputProperties);			
             
 			sax = new ContentHandlerFixer(th, !isHtml, isHtml);
 		}
 		catch (TransformerConfigurationException e)
 		{
 			throw new SAXException(e.getMessage());
 		}
 					
 		sax.startDocument();
 		thePage.execute(sax, lspParams, extContext);
 		sax.endDocument();
     }
 
 
     /**
      * Fix output properties for serializing LSP page output.
      * 
      * @param outputProperties  from {@link LSPPage#getOutputProperties}
      */
     private void fixOutputProperties(Properties outputProperties)
     {
         String method = outputProperties.getProperty(OutputKeys.METHOD);
 
         if (!outputProperties.containsKey(OutputKeys.INDENT)) 
             outputProperties.setProperty(OutputKeys.INDENT, "no");
         
         if (method.equals("html"))
         {
 			if (!outputProperties.containsKey(OutputKeys.MEDIA_TYPE)) 
                 outputProperties.setProperty(OutputKeys.MEDIA_TYPE, htmlType);
 			if (!outputProperties.containsKey(OutputKeys.ENCODING)) 
                 outputProperties.setProperty(OutputKeys.ENCODING, htmlEncoding); 
 			if (!outputProperties.containsKey(OutputKeys.DOCTYPE_PUBLIC)) 
                 outputProperties.setProperty(OutputKeys.DOCTYPE_PUBLIC,
                     "-//W3C//DTD HTML 4.01 Transitional//EN");
 			if (!outputProperties.containsKey(OutputKeys.DOCTYPE_SYSTEM)) 
                 outputProperties.setProperty(OutputKeys.DOCTYPE_SYSTEM,
                     "http://www.w3.org/TR/html4/loose.dtd");				
         }
         else if (method.equals("xhtml"))
         {
 			if (!outputProperties.containsKey(OutputKeys.MEDIA_TYPE)) 
                 outputProperties.setProperty(OutputKeys.MEDIA_TYPE, xhtmlType);
 			if (!outputProperties.containsKey(OutputKeys.ENCODING)) 
                 outputProperties.setProperty(OutputKeys.ENCODING, xhtmlEncoding); 
 			if (!outputProperties.containsKey(OutputKeys.DOCTYPE_PUBLIC)) 
                 outputProperties.setProperty(OutputKeys.DOCTYPE_PUBLIC,
                     "-//W3C//DTD XHTML 1.0 Transitional//EN");
 			if (!outputProperties.containsKey(OutputKeys.DOCTYPE_SYSTEM)) 
                 outputProperties.setProperty(OutputKeys.DOCTYPE_SYSTEM,
                     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");
         }
         else if (method.equals("xml"))
         {
 			if (!outputProperties.containsKey(OutputKeys.MEDIA_TYPE)) 
                 outputProperties.setProperty(OutputKeys.MEDIA_TYPE, xmlType);
 			if (!outputProperties.containsKey(OutputKeys.ENCODING)) 
                 outputProperties.setProperty(OutputKeys.ENCODING, xmlEncoding); 
         }
         else if (method.equals("text"))
         {
 			if (!outputProperties.containsKey(OutputKeys.MEDIA_TYPE)) 
                 outputProperties.setProperty(OutputKeys.MEDIA_TYPE, textType);
 			if (!outputProperties.containsKey(OutputKeys.ENCODING)) 
                 outputProperties.setProperty(OutputKeys.ENCODING, textEncoding); 
         }
     }
 	    
 }
 
