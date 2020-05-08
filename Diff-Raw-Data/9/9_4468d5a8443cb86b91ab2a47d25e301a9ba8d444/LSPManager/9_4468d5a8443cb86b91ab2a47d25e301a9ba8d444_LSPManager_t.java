 /*
  * Copyright (c) 2003-2004, Mikael Stldal
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
 
 package nu.staldal.lsp.servlet;
 
 import java.io.*;
 import java.util.*;
 
 import javax.servlet.*;
 
 import org.xml.sax.*;
 import javax.xml.transform.Templates;
 import javax.xml.transform.TransformerConfigurationException;
 
 import nu.staldal.lsp.*;
 
 
 /**
  * Handle execution of LSP pages in a Servlet environment.
  * This class is thread-safe.
  *
  * There is one instance of LSPManager per {@link javax.servlet.ServletContext},
  * use the {@link #getInstance} method to obtain it.
  */
 public class LSPManager
 {
 	private final ServletContext context;
 	private final LSPHelper helper;
 	
 	
 	/**
 	 * Obtain the the LSPManager instance for the given 
 	 * {@link javax.servlet.ServletContext}. Creates a new instance 
 	 * if nessecary.
 	 *
 	 * @param  context  the {@link javax.servlet.ServletContext}
 	 * @param  servletClassLoader  the {@link java.lang.ClassLoader} 
 	 *                             used to load LSPPages, use
 	 *                             <code>getClass().getClassLoader()</code> 
 	 *                             on the Servlet
 	 *
 	 * @return  the LSPManager instance for the given {@link javax.servlet.ServletContext} 
 	 */
 	public static LSPManager getInstance(ServletContext context, 
 										 ClassLoader servletClassLoader)
 	{
 		LSPManager manager = 
 			(LSPManager)context.getAttribute(LSPManager.class.getName());
 		
 		if (manager == null)
 		{
 			manager = new LSPManager(context, servletClassLoader);
 			context.setAttribute(LSPManager.class.getName(), manager);
 		}
 		
 		return manager;
 	}
 	
 	
 	private LSPManager(ServletContext context, ClassLoader servletClassLoader)
 	{
 		this.context = context;
 		this.helper = new LSPHelper(servletClassLoader);
 	}
 
 	
 	/**
 	 * Get the {@link nu.staldal.lsp.LSPPage} instance for a given page name.
 	 *
 	 * @param pageName  the name of the LSP page
 	 *
 	 * @return <code>null</code> if the given page name is not found
 	 */
 	public LSPPage getPage(String pageName)
 	{
         try {
             return helper.getPage(pageName);
         }				
 		catch (InstantiationException e)
 		{
 			context.log("Invalid LSP page: " + pageName, e);
 			return null;
 		}				
 		catch (IllegalAccessException e)
 		{
 			context.log("Invalid LSP page: " + pageName, e);
 			return null;
 		}				
 		catch (VerifyError e)
 		{
 			context.log("Invalid LSP page: " + pageName, e);
 			return null;
 	 	}				
 	}
 
 
 	/**
 	 * Executes an LSP page and write the result to a 
 	 * {@link javax.servlet.ServletResponse}. Uses any stylesheet 
      * specified in the LSP page. 
 	 *
  	 * @param thePage     the LSP page
 	 * @param lspParams   parameters to the LSP page
 	 * @param response    the {@link javax.servlet.ServletResponse}
      *
      * @throws SAXException  if any error occurs while executing the page
      * @throws IOException   if any I/O error occurs while executing the page
 	 */	
 	public void executePage(LSPPage thePage, Map lspParams, 
 							ServletResponse response)
 		throws SAXException, IOException
 	{		
         response.setContentType(helper.getContentType(thePage));
             
         OutputStream out = response.getOutputStream();        
         helper.executePage(thePage, lspParams, context, out);
     }
 		
 	
 	/**
 	 * Executes an LSP page and transform the the result with an
      * XSLT stylesheet.
      *<p>
      * The output properties specified in the stylesheet will be used, 
      * and those specified in the LSP page will be ignored. Make sure
     * to specify the output method in the stylesheet using &lt;xsl:output&gt;.
 	 *
  	 * @param thePage         the LSP page
 	 * @param lspParams       parameters to the LSP page
      * @param stylesheetName  the XSLT stylesheet
 	 * @param response        the {@link javax.servlet.ServletResponse}
      *
      * @throws SAXException     if any error occurs while executing the page
      * @throws FileNotFoundException  if the stylesheet cannot be found
      * @throws IOException      if any I/O error occurs while executing the page
      * @throws TransformerConfigurationException  if the stylesheet cannot be compiled
 	 */	
 	public void executePage(LSPPage thePage, Map lspParams, 
 							String stylesheetName, 
                             ServletResponse response)
 		throws SAXException, FileNotFoundException, IOException, 
             TransformerConfigurationException
 	{
         Templates compiledStylesheet = helper.getStylesheet(stylesheetName);
         if (compiledStylesheet == null)
             throw new FileNotFoundException(stylesheetName);
 		
         response.setContentType(helper.getContentType(compiledStylesheet));
             
         OutputStream out = response.getOutputStream();        
         helper.executePage(thePage, lspParams, context, compiledStylesheet, out);
     }
 
 
 	/**
 	 * Get a {@link javax.servlet.RequestDispatcher} for a given page name.
 	 *
 	 * The attributes in the {@link javax.servlet.ServletRequest} object
 	 * will be used as parameters to the LSP page.
      *
  	 * @param pageName  the name of the LSP page
 	 *
 	 * @return <code>null</code> if the LSP page cannot be found
 	 */
 	public RequestDispatcher getRequestDispatcher(String pageName) 
 	{
 		LSPPage page = getPage(pageName);
 			
 		if (page == null)
 		{
 			context.log("Unable to find LSP page: " + pageName);
 			return null;
 		}
 		
 		return new LSPRequestDispatcher(this, page);
 	}
 
 }
 
