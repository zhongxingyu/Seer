 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.io.Writer;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.xml.transform.URIResolver;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Templates;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.xins.common.collections.InvalidPropertyValueException;
 import org.xins.common.collections.MissingRequiredPropertyException;
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.io.FastStringWriter;
 import org.xins.common.manageable.InitializationException;
 
 /**
  * XSLT calling convention.
  *
  * @version $Revision$ $Date$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  */
 class XSLTCallingConvention extends StandardCallingConvention {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
    * The runtime property name that defines if the templates should be cached.
     */
    public final static String TEMPLATES_CACHE_PROPERTY = "templates.cache";
 
    /**
     * The runtime property name that defines the base directory of the XSLT
     * templates.
     */
    public final static String TEMPLATES_LOCATION_PROPERTY = "templates.callingconvention.source";
 
    /**
    * The input parameter that specifies the location of the XSLT template to
     * use.
     */
    public final static String TEMPLATE_PARAMETER = "_template";
 
    /**
     * The input parameter used to clear the template cache.
     */
    public final static String CLEAR_TEMPLATE_CACHE_PARAMETER = "_cleartemplatecache";
 
    /**
     * Cache for the templates.
     */
    private final static Map TEMPLATE_CACHE = new HashMap();
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>XSLTCallingConvention</code> object.
     */
    XSLTCallingConvention() {
 
       // Creates the transformer factory
       _factory = TransformerFactory.newInstance();
       _factory.setURIResolver(new XsltURIResolver());
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * Flag that indicates whether the templates should be cached.
     */
    private boolean _cacheTemplates = true;
 
    /**
     * Location of the XSLT transformation Style Sheet.
     */
    private String _baseXSLTDir;
 
    /**
     * The XSLT transformer.
     */
    private final TransformerFactory _factory;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    protected void initImpl(PropertyReader runtimeProperties)
    throws MissingRequiredPropertyException,
           InvalidPropertyValueException,
           InitializationException {
 
       _cacheTemplates = "false".equals(
          runtimeProperties.get(TEMPLATES_CACHE_PROPERTY));
 
       // Get the base directory of the Style Sheet
       // e.g. http://xslt.mycompany.com/myapi/
       // then the XSLT file must have the function names.
       _baseXSLTDir = runtimeProperties.get(TEMPLATES_LOCATION_PROPERTY);
 
       // Relative URLs use the user directory as base dir.
       if (_baseXSLTDir == null) {
          try {
             _baseXSLTDir = new File(System.getProperty("user.dir")).toURL().toString();
          } catch (IOException ioe) {
             // Ignore
          }
       } else if (_baseXSLTDir.indexOf("://") == -1) {
          try {
             String userDir = new File(System.getProperty("user.dir")).toURL().toString();
             _baseXSLTDir = userDir + _baseXSLTDir;
          } catch (IOException ioe) {
             // Ignore
          }
       }
    }
 
    protected void convertResultImpl(FunctionResult      xinsResult,
                                     HttpServletResponse httpResponse,
                                     HttpServletRequest  httpRequest)
    throws IOException {
 
       // Send the XML output to the stream and flush
       FastStringWriter xmlOutput = new FastStringWriter();
       CallResultOutputter.output(xmlOutput, RESPONSE_ENCODING, xinsResult, false);
       xmlOutput.close();
 
       String xsltLocation = httpRequest.getParameter(TEMPLATE_PARAMETER);
       if (xsltLocation == null) {
          xsltLocation = _baseXSLTDir + httpRequest.getParameter("_function") + ".xslt";
       }
       try {
          Templates t = null;
          if ("true".equals(httpRequest.getParameter(CLEAR_TEMPLATE_CACHE_PARAMETER))) {
             TEMPLATE_CACHE.clear();
             PrintWriter out = httpResponse.getWriter();
             out.write("Done.");
             out.close();
             return;
          }
          if (!_cacheTemplates && TEMPLATE_CACHE.containsKey(xsltLocation)) {
             t = (Templates) TEMPLATE_CACHE.get(xsltLocation);
          } else {
             t = _factory.newTemplates(_factory.getURIResolver().resolve(xsltLocation, _baseXSLTDir));
             if (_cacheTemplates) {
                TEMPLATE_CACHE.put(xsltLocation, t);
             }
          }
          Transformer xformer = t.newTransformer();
          Source source = new StreamSource(new StringReader(xmlOutput.toString()));
          Writer buffer = new FastStringWriter(1024);
          Result result = new StreamResult(buffer);
          xformer.transform(source, result);
 
          PrintWriter out = httpResponse.getWriter();
 
          // Determine the MIME type for the output.
          String mimeType = t.getOutputProperties().getProperty("media-type");
          if (mimeType == null) {
             String method = t.getOutputProperties().getProperty("method");
             if ("xml".equals(method)) {
                mimeType = "text/xml";
             } else if ("html".equals(method)) {
                mimeType = "text/html";
             } else if ("text".equals(method)) {
                mimeType = "text/plain";
             }
          }
          String encoding = t.getOutputProperties().getProperty("encoding");
          if (mimeType != null && encoding != null) {
             mimeType += ";charset=" + encoding;
          }
          if (mimeType != null) {
             httpResponse.setContentType(mimeType);
          }
 
          httpResponse.setStatus(HttpServletResponse.SC_OK);
          out.print(buffer.toString());
          out.close();
       } catch (Exception ex) {
          ex.printStackTrace();
          throw new IOException(ex.getMessage());
       }
    }
 
    /**
     * Class used to revolved URL locations when an SLT file refers to another XSLT file using a relative URL.
     */
    class XsltURIResolver implements URIResolver {
 
       /**
        * The previous base URL if any.
        */
       private String _base;
 
       /**
        * Revolve a hyperlink reference.
        *
        * @param href
        *    The hyperlink to resolve.
        * @param base
        *    The base URI in effect when the href attribute was encountered.
        *
        * @return
        *    A Source object, or <code>null</code> if the href cannot be resolved,
        *    and the processor should try to resolve the URI itself.
        *
        * @throws TransformerException
        *    If an error occurs when trying to resolve the URI.
        */
       public Source resolve(String href, String base) throws TransformerException {
          if (base == null) {
             base = _base;
          } else if (!base.endsWith("/")) {
             base += '/';
          }
          _base = base;
          String url = null;
          if (href.indexOf(":/") == -1) {
             url = base + href;
          } else {
             url = href;
             _base = href.substring(0, href.lastIndexOf('/') + 1);
          }
          try {
             return new StreamSource(new URL(url).openStream());
          } catch (IOException ioe) {
             ioe.printStackTrace();
             throw new TransformerException(ioe);
          }
       }
    }
 }
