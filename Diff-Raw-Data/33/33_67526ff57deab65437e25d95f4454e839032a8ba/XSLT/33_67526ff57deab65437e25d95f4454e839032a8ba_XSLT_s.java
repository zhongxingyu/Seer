 /* $Id$
  *
  * The Summa project.
  * Copyright (C) 2005-2008  The State and University Library
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package dk.statsbiblioteket.util.xml;
 
 import dk.statsbiblioteket.util.qa.QAInfo;
 import dk.statsbiblioteket.util.Strings;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.Log;
 import org.w3c.dom.Document;
 import org.xml.sax.InputSource;
 
 import javax.xml.transform.*;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.transform.stream.StreamResult;
 import java.io.*;
 import java.net.URL;
 import java.net.MalformedURLException;
 import java.util.Map;
 import java.util.HashMap;
 
 /**
  * Helpers for transforming XML using XSLTs. All methods are Thread-safe,
  * as long as Threads do not share the same Transformer.
  * </p><p>
  * Most of the helpers have an option for ifnoring XML namespace. Setting this
  * to true strips namespaces from the input by doing a full DOM-parsing.
  * Besides being fairly expensive in terms of processing time and temporary
  * memory allocation, this is also a bad practice with regard to QA of the
  * input.
  */
 @QAInfo(level = QAInfo.Level.NORMAL,
         state = QAInfo.State.IN_DEVELOPMENT,
         author = "te")
 public class XSLT {
     private static Log log = LogFactory.getLog(XSLT.class);
 
     /**
      * Creates a new transformer based on the given XSLTLocation.
      * @param xslt the location of the XSLT.
      * @throws javax.xml.transform.TransformerException thrown if for some
      *         reason a Transformer could not be instantiated.
      *         This is normally due to problems with the {@code xslt} URL
      * @return a Transformer based on the given XSLT.
      * @see {@link #getLocalTransformer} for reusing Transformers.
      */
     public static Transformer createTransformer(URL xslt) throws
                                                           TransformerException {
 
         log.debug("Requesting and compiling XSLT from '" + xslt + "'");
 
         TransformerFactory tfactory = TransformerFactory.newInstance();
         InputStream in = null;
         Transformer transformer;
         try {
             if (xslt == null) {
                 throw new NullPointerException("xslt URL is null");
             }
             in = xslt.openStream();
             transformer = tfactory.newTransformer(
                     new StreamSource(in, xslt.toString()));
             
         } catch (MalformedURLException e) {
             throw new TransformerException(String.format(
                     "The URL to the XSLT is not a valid URL: '%s'",
                     xslt), e);
         } catch (IOException e) {
             throw new TransformerException(String.format(
                     "Unable to open the XSLT resource '%s'", xslt), e);
         } catch (TransformerConfigurationException e) {
             throw new TransformerException(String.format(
                     "Wrongly configured transformer for XSLT at '%s'",
                     xslt), e);
         } catch (TransformerException e) {
             throw new TransformerException(
                     "Unable to instantiate Transformer, a system configuration"
                     + " error?", e);
         } finally {
             try {
                 if (in != null) {
                     in.close();
                 }
             } catch (IOException e) {
                 log.warn("Non-fatal IOException while closing stream to '"
                          + xslt + "'");
             }
         }
         return transformer;
     }
 
 
     private static ThreadLocal<Map<String, Transformer>> localMapCache =
             createLocalMapCache();
     private static ThreadLocal<Map<String, Transformer>> createLocalMapCache() {
         return new ThreadLocal<Map<String, Transformer>>() {
             @Override
             protected Map<String, Transformer> initialValue() {
                 return new HashMap<String, Transformer>();
             }
         };
     }
 
     /**
      * Create or re-use a Transformer for the given xsltLocation.
      * The Transformer is {@link ThreadLocal}, so the method is thread-safe.
      * </p><p>
      * Warning: A list is maintained for all XSLTs so changes to the xslt will
      *          not be reflected. Call {@link #clearTransformerCache} to clear
      *          the list.
      * @param xslt the location of the XSLT.
      * @return a Transformer using the given XSLT.
      * @throws TransformerException if the Transformor could not be constructed.
      */
     public static Transformer getLocalTransformer(URL xslt)
                                                    throws TransformerException {
         return getLocalTransformer(xslt, null);
     }
 
     /**
      * Create or re-use a Transformer for the given xsltLocation.
      * The Transformer is {@link ThreadLocal}, so the method is thread-safe.
      * </p><p>
      * Warning: A list is maintained for all XSLTs so changes to the xslt will
      *          not be reflected. Call {@link #clearTransformerCache} to clear
      *          the list.
      * @param xslt the location of the XSLT.
      * @param parameters for the Transformer. The keys must be Strings.
      *        If the map is null, it will be ignored.
      * @return a Transformer using the given XSLT.
      * @throws TransformerException if the Transformor could not be constructed.
      */
     public static Transformer getLocalTransformer(URL xslt, Map parameters)
                                                    throws TransformerException {
         if (xslt == null) {
             throw new NullPointerException("The xslt was null");
         }
         Map<String, Transformer> map = localMapCache.get();
         Transformer transformer = map.get(xslt.toString());
         if (transformer == null) {
             transformer = createTransformer(xslt);
             map.put(xslt.toString(), transformer);
         }
         transformer.clearParameters(); // Is this safe? Any defaults lost?
         if (parameters != null) {
             for (Object entryObject: parameters.entrySet()) {
                 Map.Entry entry = (Map.Entry)entryObject;
                 transformer.setParameter((String)entry.getKey(), 
                                          entry.getValue());
             }
         }
         return transformer;
     }
 
     /**
      * Clears the cache used by {@link #getLocalTransformer(java.net.URL)}.
      * This is safe to call as it only affects performance. Clearing the cache
      * means that changes to underlying XSLTs will be reflected and that any
      * memory allocated for caching is freed.
      * </p><p>
      * Except for special cases, such as a huge number of different XSLTs,
      * the cache should only be cleared when the underlying XSLTs are changed.
      */
     public static void clearTransformerCache() {
         localMapCache = createLocalMapCache();
     }
 
     /* ******************** Transformer-calls below this ******************** */
 
     /**
      * Requests a cached ThreadLocal Transformer and performs the
      * transformation.
      * @param xslt the location of the XSLT to use.
      * @param in   the content to transform.
      * @return     the transformed content.
      * @throws TransformerException if the transformation failed.
      */
     public static String transform(URL xslt, String in)
                                                    throws TransformerException {
         return transform(xslt, in, null, false);
     }
 
     /**
      * Requests a cached ThreadLocal Transformer and performs the
      * transformation.
      * @param xslt the location of the XSLT to use.
      * @param in   the content to transform.
      * @param ignoreXMLNamespaces if true, namespaces in the input content will
      *        be stripped. This is not recommended, but a lot of XML and XSLTs
      *        does not match namespaces correctly. Setting this to true will
      *        have an impact on performance.
      * @return     the transformed content.
      * @throws TransformerException if the transformation failed.
      */
     public static String transform(URL xslt, String in,
                                    boolean ignoreXMLNamespaces)
                                                    throws TransformerException {
         return transform(xslt, in, null, ignoreXMLNamespaces);
     }
 
     /**
      * Requests a cached ThreadLocal Transformer and performs the
      * transformation.
      * @param xslt the location of the XSLT to use.
      * @param in   the content to transform.
      * @param parameters for the Transformer. The keys must be Strings.
      *        If the map is null, it will be ignored.
      * @return     the transformed content.
      * @throws TransformerException if the transformation failed.
      */
     public static String transform(URL xslt, String in, Map parameters)
                                                    throws TransformerException {
         return transform(xslt, in, parameters, false);
     }
 
     /**
      * Requests a cached ThreadLocal Transformer and performs the
      * transformation.
      * @param xslt the location of the XSLT to use.
      * @param in   the content to transform.
      * @param parameters for the Transformer. The keys must be Strings.
      *        If the map is null, it will be ignored.
      * @param ignoreXMLNamespaces if true, namespaces in the input content will
      *        be stripped. This is not recommended, but a lot of XML and XSLTs
      *        does not match namespaces correctly. Setting this to true will
      *        have an impact on performance.
      * @return     the transformed content.
      * @throws TransformerException if the transformation failed.
      */
     public static String transform(URL xslt, String in, Map parameters,
                                    boolean ignoreXMLNamespaces)
                                                    throws TransformerException {
         StringWriter sw = new StringWriter();
         if (!ignoreXMLNamespaces) {
             transform(xslt, new StringReader(in), sw, parameters);
         } else {
             Document dom = DOM.stringToDOM(in);
             transform(getLocalTransformer(xslt, parameters), dom, sw);
 
             /*
             Reader noNamespace = removeNamespaces(new StringReader(in));
             transform(getLocalTransformer(xslt, parameters), noNamespace, sw);
               */
         /*
             Reader noNamespace = new NamespaceRemover(new StringReader(in));
             transform(getLocalTransformer(xslt, parameters), noNamespace, sw);
             */
         }
         return sw.toString();
     }
 
     /**
      * Requests a cached ThreadLocal Transformer and performs the
      * transformation.
      * @param xslt the location of the XSLT to use.
      * @param in   the content to transform.
      * @param parameters for the Transformer. The keys must be Strings.
      *        If the map is null, it will be ignored.
      * @return     the transformed content.
      * @throws TransformerException if the transformation failed.
      */
     public static String transform(URL xslt, Reader in, Map parameters)
                                                    throws TransformerException {
         return transform(xslt, in, parameters, false);
     }
 
     /**
      * Requests a cached ThreadLocal Transformer and performs the
      * transformation.
      * @param xslt the location of the XSLT to use.
      * @param in   the content to transform.
      * @param parameters for the Transformer. The keys must be Strings.
      *        If the map is null, it will be ignored.
      * @param ignoreXMLNamespaces if true, namespaces in the input content will
      *        be stripped. This is not recommended, but a lot of XML and XSLTs
      *        does not match namespaces correctly. Setting this to true will
      *        have an impact on performance.
      * @return     the transformed content.
      * @throws TransformerException if the transformation failed.
      */
     public static String transform(URL xslt, Reader in, Map parameters,
                                    boolean ignoreXMLNamespaces)
                                                    throws TransformerException {
         StringWriter sw = new StringWriter();
         if (!ignoreXMLNamespaces) {
             transform(xslt, in, sw, parameters);
         } else {
             InputSource is = new InputSource();
             is.setCharacterStream(in);
             Document dom;
             try {
                 dom = DOM.stringToDOM(Strings.flush(in));
             } catch (IOException e) {
                 throw new TransformerException(
                         "Unable to convert Reader to String", e);
             }
             transform(getLocalTransformer(xslt, parameters), dom, sw);
         }
         return sw.toString();
     }
 
     /**
      * Requests a cached ThreadLocal Transformer and performs the
      * transformation.
      * @param xslt the location of the XSLT to use.
      * @param in   the content to transform.
      * @param parameters for the Transformer. The keys must be Strings.
      *        If the map is null, it will be ignored.
      * @return the transformed content. Note that the correct charset must be
      *         supplied to toString("charset") to get proper String results.
      *         The charset is specified by the XSLT.
      * @throws TransformerException if the transformation failed.
      */
     public static ByteArrayOutputStream transform(URL xslt, byte[] in,
                                                   Map parameters)
                                                    throws TransformerException {
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         transform(xslt, new ByteArrayInputStream(in), out, parameters);
         return out;
     }
 
     /**
      * Requests a cached ThreadLocal Transformer and performs the
      * transformation.
      * @param xslt the location of the XSLT to use.
      * @param in   the content to transform.
      * @param parameters for the Transformer. The keys must be Strings.
      *        If the map is null, it will be ignored.
      * @param ignoreXMLNamespaces if true, namespaces in the input content will
      *        be stripped. This is not recommended, but a lot of XML and XSLTs
      *        does not match namespaces correctly. Setting this to true will
      *        have an impact on performance.
      * @return the transformed content. Note that the correct charset must be
      *         supplied to toString("charset") to get proper String results.
      *         The charset is specified by the XSLT.
      * @throws TransformerException if the transformation failed.
      */
     public static ByteArrayOutputStream transform(
             URL xslt, byte[] in, Map parameters, boolean ignoreXMLNamespaces)
                                                    throws TransformerException {
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         if (!ignoreXMLNamespaces) {
             transform(xslt, new ByteArrayInputStream(in), out, parameters);
         } else {
             Document dom;
             dom = DOM.streamToDOM(new ByteArrayInputStream(in));
             transform(getLocalTransformer(xslt, parameters), dom, out);
         }
         return out;
     }
 
     /**
      * Requests a cached ThreadLocal Transformer and performs the
      * transformation.
      * @param xslt the location of the XSLT to use.
      * @param in   the content to transform.
      * @param parameters for the Transformer. The keys must be Strings.
      *        If the map is null, it will be ignored.
      * @return the transformed content. Note that the correct charset must be
      *         supplied to toString("charset") to get proper String results.
      *         The charset is specified by the XSLT.
      * @throws TransformerException if the transformation failed.
      */
     public static ByteArrayOutputStream transform(URL xslt, InputStream in,
                                                   Map parameters)
                                                    throws TransformerException {
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         transform(getLocalTransformer(xslt, parameters), in, out);
         return out;
     }
 
     /**
      * Requests a cached ThreadLocal Transformer and performs the
      * transformation.
      * @param xslt the location of the XSLT to use.
      * @param in   the content to transform.
      * @param parameters for the Transformer. The keys must be Strings.
      *        If the map is null, it will be ignored.
      * @param ignoreXMLNamespaces if true, namespaces in the input content will
      *        be stripped. This is not recommended, but a lot of XML and XSLTs
      *        does not match namespaces correctly. Setting this to true will
      *        have an impact on performance.
      * @return the transformed content. Note that the correct charset must be
      *         supplied to toString("charset") to get proper String results.
      *         The charset is specified by the XSLT.
      * @throws TransformerException if the transformation failed.
      */
     public static ByteArrayOutputStream transform(
             URL xslt, InputStream in, Map parameters,
             boolean ignoreXMLNamespaces) throws TransformerException {
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         if (!ignoreXMLNamespaces) {
             transform(getLocalTransformer(xslt, parameters), in, out);
         } else {
             Document dom;
             dom = DOM.streamToDOM(in);
             transform(getLocalTransformer(xslt, parameters), dom, out);
         }
         return out;
     }
 
     /**
      * Requests a cached ThreadLocal Transformer and performs the
      * transformation.
      * @param xslt the location of the XSLT to use.
      * @param dom   the content to transform.
      * @param parameters for the Transformer. The keys must be Strings.
      *        If the map is null, it will be ignored.
      * @return the transformed content. Note that the correct charset must be
      *         supplied to toString("charset") to get proper String results.
      *         The charset is specified by the XSLT.
      * @throws TransformerException if the transformation failed.
      */
     public static ByteArrayOutputStream transform(URL xslt, Document dom,
                                                   Map parameters)
                                                    throws TransformerException {
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         transform(getLocalTransformer(xslt, parameters), dom, out);
         return out;
     }
 
     /**
      * Requests a cached ThreadLocal Transformer and performs a transformation
      * from Stream to Stream.
      * @param xslt the location of the XSLT to use.
      * @param in   input Stream.
      * @param out  output Stream.
      * @param parameters for the Transformer. The keys must be Strings.
      *        If the map is null, it will be ignored.
      * @throws TransformerException if the transformation failed.
      */
     public static void transform(URL xslt, InputStream in, OutputStream out,
                                  Map parameters)
                                                     throws TransformerException{
         transform(getLocalTransformer(xslt, parameters), in, out);
     }
 
     /**
      * Requests a cached ThreadLocal Transformer and performs a transformation
      * from Reader to Writer.
      * @param xslt the location of the XSLT to use.
      * @param in   input.
      * @param out  output.
      * @param parameters for the Transformer. The keys must be Strings.
      *        If the map is null, it will be ignored.
      * @throws TransformerException if the transformation failed.
      */
     public static void transform(URL xslt, Reader in, Writer out,
                                  Map parameters)
                                                     throws TransformerException{
         transform(getLocalTransformer(xslt, parameters), in, out);
     }
 
 
     /* ********************* Resolved transformer below this ***************  */
 
 
     /**
      * Performs a transformation from Document to Stream with the transformer.
      * @param transformer probably retrieved by {@link #getLocalTransformer}.
      * @param dom input.
      * @param out output.
      * @throws TransformerException if the transformation failed.
      */
     public static void transform(Transformer transformer, Document dom,
                                  OutputStream out) throws TransformerException {
         transformer.transform(new DOMSource(dom), new StreamResult(out));
     }
 
     /**
      * Performs a transformation from Stream to Stream with the transformer.
      * @param transformer probably retrieved by {@link #getLocalTransformer}.
      * @param in          input Stream.
      * @param out         output Stream.
      * @throws TransformerException if the transformation failed.
      */
     public static void transform(Transformer transformer,
                                  InputStream in, OutputStream out) throws
                                                            TransformerException{
         transformer.transform(new StreamSource(in), new StreamResult(out));
     }
 
     /**
      * Performs a transformation from Reader to Writer with the transformer.
      * @param transformer probably retrieved by {@link #getLocalTransformer}.
      * @param in          input.
      * @param out         output.
      * @throws TransformerException if the transformation failed.
      */
     public static void transform(Transformer transformer, Reader in, Writer out)
                                                     throws TransformerException{
         transformer.transform(new StreamSource(in), new StreamResult(out));
     }
 
     /**
      * Performs a transformation from DOM to Writer with the transformer.
      * @param transformer probably retrieved by {@link #getLocalTransformer}.
      * @param dom input.
      * @param out output.
      * @throws TransformerException if the transformation failed.
      */
     public static void transform(Transformer transformer, Document dom,
                                  Writer out) throws TransformerException {
         transformer.transform(new DOMSource(dom), new StreamResult(out));
     }
 
     /* Using XSLT's to remove the namespaces is slower than DOM-parsing.
        Memory-usage has not been tested. 
      */
 
     static URL NAMESPACE_XSLT;
     static {
         NAMESPACE_XSLT =
                 Thread.currentThread().getContextClassLoader().getResource(
                         "dk/statsbiblioteket/util/xml/namespace_remover.xslt");
     }
     static ByteArrayOutputStream removeNamespaces(InputStream in) throws
                                                           TransformerException {
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         transform(NAMESPACE_XSLT, in, out, null);
         return out;
     }
 
     static Reader removeNamespaces(Reader in) throws TransformerException {
         StringWriter sw = new StringWriter();
         transform(NAMESPACE_XSLT, in, sw, null);
         return new StringReader(sw.toString());
     }
 }
