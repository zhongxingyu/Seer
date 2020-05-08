 /* ********************************************************************
     Licensed to Jasig under one or more contributor license
     agreements. See the NOTICE file distributed with this work
     for additional information regarding copyright ownership.
     Jasig licenses this file to you under the Apache License,
     Version 2.0 (the "License"); you may not use this file
     except in compliance with the License. You may obtain a
     copy of the License at:
 
     http://www.apache.org/licenses/LICENSE-2.0
 
     Unless required by applicable law or agreed to in writing,
     software distributed under the License is distributed on
     an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
     KIND, either express or implied. See the License for the
     specific language governing permissions and limitations
     under the License.
 */
 package org.bedework.util.dav;
 
 import org.bedework.util.http.BasicHttpClient;
 import org.bedework.util.xml.XmlEmit;
 import org.bedework.util.xml.XmlUtil;
 import org.bedework.util.xml.tagdefs.WebdavTags;
 
 import org.apache.http.Header;
 import org.apache.http.message.BasicHeader;
 import org.apache.http.protocol.HTTP;
 import org.apache.log4j.Logger;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.xml.sax.InputSource;
 
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Serializable;
 import java.io.StringWriter;
 import java.net.URI;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.namespace.QName;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 /** Helper for DAV interactions
 *
 * @author Mike Douglass  douglm - rpi.edu
 */
 public class DavUtil implements Serializable {
   protected boolean debug;
 
   private transient Logger log;
 
   /** */
   public static final Header depth0 = new BasicHeader("depth", "0");
   /** */
   public static final Header depth1 = new BasicHeader("depth", "1");
   /** */
   public static final Header depthinf = new BasicHeader("depth", "infinity");
 
   /**
    */
   public DavUtil() {
     debug = getLogger().isDebugEnabled();
   }
 
   /** Represents the child of a collection
    *
    * @author Mike Douglass
    */
   public static class DavChild implements Comparable<DavChild>  {
     /** The href */
     public String uri;
 
     /** Always requested */
     public String displayName;
 
     /** Always requested */
     public boolean isCollection;
 
     /** Same order as supplied properties */
     public Collection<DavProp> propVals = new ArrayList<DavProp>();
 
     @Override
     public int compareTo(final DavChild that) {
       if (isCollection != that.isCollection) {
         if (!isCollection) {
           return -1;
         }
 
         return 1;
       }
 
       if (displayName == null) {
         return -1;
       }
 
       if (that.displayName == null) {
         return 1;
       }
 
       return displayName.compareTo(that.displayName);
     }
   }
 
   /** Represents a property
    *
    * @author Mike Douglass
    */
   public static class DavProp {
     /** */
     public QName name;
     /** */
     public String content;
     /** */
     public int status;
   }
 
   /** partially parsed propstat response element
    *
    * @author douglm
    */
   public static class PropstatElement {
     /** */
     public Collection<Element> props;
 
     /** */
     public int status;
 
     /** May be null */
     public String responseDescription;
   }
 
   /** partially parsed multi-status response element
    *
    * @author douglm
    */
   public static class MultiStatusResponseElement {
     /** */
     public String href;
 
     /** */
     public List<PropstatElement> propstats = new ArrayList<PropstatElement>();
 
     /** May be null */
     public String responseDescription;
   }
 
   /** partially parsed multi-status response
    *
    * @author douglm
    */
   public static class MultiStatusResponse {
     /** */
     public List<MultiStatusResponseElement> responses =
       new ArrayList<MultiStatusResponseElement>();
 
     /** May be null */
     public String responseDescription;
   }
 
   /**
    * @param in
    * @return Collection<DavChild>
    * @throws Throwable
    */
   public MultiStatusResponse getMultiStatusResponse(final InputStream in) throws Throwable {
     MultiStatusResponse res = new MultiStatusResponse();
 
     Document doc = parseContent(in);
 
     Element root = doc.getDocumentElement();
 
     /*    <!ELEMENT multistatus (response+, responsedescription?) > */
 
     expect(root, WebdavTags.multistatus);
 
     Collection<Element> responses = getChildren(root);
 
     int count = 0; // validity
     for (Element resp: responses) {
       count++;
 
       if (XmlUtil.nodeMatches(resp, WebdavTags.responseDescription)) {
         // Has to be last
         if (responses.size() > count) {
           throw new Exception("Bad multstatus Expected " +
               "(response+, responsedescription?)");
         }
 
         res.responseDescription = getElementContent(resp);
         continue;
       }
 
       if (!XmlUtil.nodeMatches(resp, WebdavTags.response)) {
         throw new Exception("Bad multstatus Expected " +
             "(response+, responsedescription?) found " + resp);
       }
 
       /*    <!ELEMENT response (href, ((href*, status)|(propstat+)),
                           responsedescription?) >
        */
       MultiStatusResponseElement msre = new MultiStatusResponseElement();
       res.responses.add(msre);
 
       Iterator<Element> elit = getChildren(resp).iterator();
 
       Node nd = elit.next();
 
       if (!XmlUtil.nodeMatches(nd, WebdavTags.href)) {
         throw new Exception("Bad response. Expected href found " + nd);
       }
 
       msre.href = getElementContent((Element)nd);
 
       while (elit.hasNext()) {
         nd = elit.next();
 
         if (!XmlUtil.nodeMatches(nd, WebdavTags.propstat)) {
           throw new Exception("Bad response. Expected propstat found " + nd);
         }
 
         /*    <!ELEMENT propstat (prop, status, responsedescription?) > */
 
         PropstatElement pse = new PropstatElement();
         msre.propstats.add(pse);
 
         Iterator<Element> propstatit = getChildren(nd).iterator();
         Node propnd = propstatit.next();
 
         if (!XmlUtil.nodeMatches(propnd, WebdavTags.prop)) {
           throw new Exception("Bad response. Expected prop found " + propnd);
         }
 
         if (!propstatit.hasNext()) {
           throw new Exception("Bad response. Expected propstat/status");
         }
 
         pse.status = httpStatus(propstatit.next());
 
         if (propstatit.hasNext()) {
           Node rdesc = propstatit.next();
 
           if (!XmlUtil.nodeMatches(rdesc, WebdavTags.responseDescription)) {
             throw new Exception("Bad response, expected null or " +
                 "responsedescription. Found: " + rdesc);
           }
 
           pse.responseDescription = getElementContent(resp);
         }
 
         /* process each property with this status */
 
         pse.props = getChildren(propnd);
       }
     }
 
     return res;
   }
 
   /** Return the DavChild element for the targeted node.
    *
    * @param cl
    * @param path
    * @param props   null for a default set
    * @return DavChild or null for not found
    * @throws Throwable
    */
   public DavChild getProps(final BasicHttpClient cl,
                            final String path,
                            final Collection<QName> props) throws Throwable {
     Collection<Element> responses = propfind(cl, normalizePath(path), props,
                                              depth0);
 
     if (responses == null) {
       // status 400
       return null;
     }
 
     DavChild dc = null;
 
     int count = 0; // validity
     for (Element resp: responses) {
       count++;
 
       if (XmlUtil.nodeMatches(resp, WebdavTags.responseDescription)) {
         // Has to be last
         if (responses.size() > count) {
           throw new Exception("Bad multstatus Expected " +
               "(response+, responsedescription?)");
         }
 
         continue;
       }
 
       if (!XmlUtil.nodeMatches(resp, WebdavTags.response)) {
         throw new Exception("Bad multstatus Expected " +
             "(response+, responsedescription?) found " + resp);
       }
 
       if (dc != null){
         throw new Exception("Bad multstatus Expected only 1 response");
       }
 
       dc = makeDavResponse(resp);
     }
 
     return dc;
   }
 
   /**
    * @param cl
    * @param parentPath
    * @param props   null for a default set
    * @return Collection<DavChild>
    * @throws Throwable
    */
   public Collection<DavChild> getChildrenUrls(final BasicHttpClient cl,
                                               final String parentPath,
                                               final Collection<QName> props) throws Throwable {
     String path = normalizePath(parentPath);
 
     URI parentURI = new URI(path);
 
     Collection<Element> responses = propfind(cl, path, props, depth1);
 
     Collection<DavChild> result = new ArrayList<DavChild>();
 
     int count = 0; // validity
     for (Element resp: responses) {
       count++;
 
       if (XmlUtil.nodeMatches(resp, WebdavTags.responseDescription)) {
         // Has to be last
         if (responses.size() > count) {
           throw new Exception("Bad multstatus Expected " +
               "(response+, responsedescription?)");
         }
 
         continue;
       }
 
       if (!XmlUtil.nodeMatches(resp, WebdavTags.response)) {
         throw new Exception("Bad multstatus Expected " +
             "(response+, responsedescription?) found " + resp);
       }
 
       DavChild dc = makeDavResponse(resp);
 
       /* We get the collection back as well - check for it and skip it. */
       URI childURI = new URI(dc.uri);
 
       if (parentURI.getPath().equals(childURI.getPath())) {
         continue;
       }
 
       result.add(dc);
     }
 
     return result;
   }
 
   /**
    * @param cl
    * @param path
    * @param props   null for a default set
    * @param depthHeader
    * @return Collection<Element> from multi-status response
    * @throws Throwable
    */
   public Collection<Element> propfind(final BasicHttpClient cl,
                                       final String path,
                                       final Collection<QName> props,
                                       final Header depthHeader) throws Throwable {
     StringWriter sw = new StringWriter();
     XmlEmit xml = new XmlEmit();
 
     xml.startEmit(sw);
 
     xml.openTag(WebdavTags.propfind);
     xml.openTag(WebdavTags.prop);
     xml.emptyTag(WebdavTags.displayname);
     xml.emptyTag(WebdavTags.resourcetype);
 
     if (props != null) {
       for (QName pr: props) {
         if (pr.equals(WebdavTags.displayname)) {
           continue;
         }
 
         if (pr.equals(WebdavTags.resourcetype)) {
           continue;
         }
 
         xml.emptyTag(pr);
       }
     }
 
     xml.closeTag(WebdavTags.prop);
     xml.closeTag(WebdavTags.propfind);
 
     byte[] content = sw.toString().getBytes();
 
     Header[] hdr = {depthHeader};
     int res = cl.sendRequest("PROPFIND", path,
                              hdr,
                              "text/xml", // contentType,
                              content.length, // contentLen,
                              content);
 
     int SC_MULTI_STATUS = 207; // not defined for some reason
     if (res != SC_MULTI_STATUS) {
       if (debug) {
         debugMsg("Got response " + res + " for path " + path);
       }
 
      if (res == HttpServletResponse.SC_NOT_FOUND) {
        return null;
      }

       throw new Exception("Got response " + res + " for path " + path);
     }
 
     Document doc = parseContent(cl.getResponseBodyAsStream());
 
     Element root = doc.getDocumentElement();
 
     /*    <!ELEMENT multistatus (response+, responsedescription?) > */
 
     expect(root, WebdavTags.multistatus);
 
     return getChildren(root);
   }
 
   /* ====================================================================
    *                   XmlUtil wrappers
    * ==================================================================== */
 
   /** Parse the content, and return the DOM representation.
    *
    * @param in         content as stream
    * @return Document  Parsed body or null for no body
    * @exception Throwable Some error occurred.
    */
   private Document parseContent(final InputStream in) throws Throwable {
     DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     factory.setNamespaceAware(true);
 
     DocumentBuilder builder = factory.newDocumentBuilder();
 
     return builder.parse(new InputSource(new InputStreamReader(in)));
   }
 
   /**
    * @param nd
    * @return Collection<Element>
    * @throws Throwable
    */
   public static Collection<Element> getChildren(final Node nd) throws Throwable {
     try {
       return XmlUtil.getElements(nd);
     } catch (Throwable t) {
       //if (debug) {
       //  getLogger().error(this, t);
       //}
 
       throw new Exception(t.getMessage());
     }
   }
 
   /**
    * @param nd
    * @return Element[]
    * @throws Throwable
    */
   public static Element[] getChildrenArray(final Node nd) throws Throwable {
     try {
       return XmlUtil.getElementsArray(nd);
     } catch (Throwable t) {
       //if (debug) {
       //  getLogger().error(this, t);
       //}
 
       throw new Exception(t.getMessage());
     }
   }
 
   /**
    * @param nd
    * @return Element
    * @throws Throwable
    */
   public static Element getOnlyChild(final Node nd) throws Throwable {
     try {
       return XmlUtil.getOnlyElement(nd);
     } catch (Throwable t) {
       //if (debug) {
       //  getLogger().error(this, t);
       //}
 
       throw new Exception(t.getMessage());
     }
   }
 
   /**
    * @param el
    * @return String
    * @throws Throwable
    */
   public static String getElementContent(final Element el) throws Throwable {
     try {
       return XmlUtil.getElementContent(el);
     } catch (Throwable t) {
       //if (debug) {
       //  getLogger().error(this, t);
       //}
 
       throw new Exception(t.getMessage());
     }
   }
 
   /**
    * @param el
    * @return boolean
    * @throws Throwable
    */
   public static boolean isEmpty(final Element el) throws Throwable {
     try {
       return XmlUtil.isEmpty(el);
     } catch (Throwable t) {
       //if (debug) {
       //  getLogger().error(this, t);
       //}
 
       throw new Exception(t.getMessage());
     }
   }
 
   /**
    * @param el
    * @param tag
    * @throws Throwable
    */
   public static void expect(final Element el,
                             final QName tag) throws Throwable {
     if (!XmlUtil.nodeMatches(el, tag)) {
       throw new Exception("Expected " + tag);
     }
   }
 
   /**
    * @param el
    * @return int status
    * @throws Throwable
    */
   public static int httpStatus(final Element el) throws Throwable {
     if (!XmlUtil.nodeMatches(el, WebdavTags.status)) {
       throw new Exception("Bad response. Expected status found " + el);
     }
 
     String s = getElementContent(el);
 
     if (s == null) {
       throw new Exception("Bad http status. Found null");
     }
 
     try {
       int start = s.indexOf(" ");
       int end = s.indexOf(" ", start + 1);
 
       if (end < 0) {
         return Integer.valueOf(s.substring(start));
       }
 
       return Integer.valueOf(s.substring(start + 1, end));
     } catch (Throwable t) {
       throw new Exception("Bad http status. Found " + s);
     }
   }
 
   /*
   private static class Response implements DavResp {
 //    private boolean debug;
 
     DavIo client;
 
     Response(final DavIo client, final boolean debug) throws DavioException {
       this.client = client;
 //      this.debug = debug;
     }
 
     @Override
     public int getRespCode() throws DavioException {
       return client.getStatusCode();
     }
 
     @Override
     public String getContentType() throws DavioException {
       return client.getResponseContentType();
     }
 
     @Override
     public long getContentLength() throws DavioException {
       return client.getResponseContentLength();
     }
 
     @Override
     public String getCharset() throws DavioException {
       return client.getResponseCharSet();
     }
 
     @Override
     public InputStream getContentStream() throws DavioException {
       return client.getResponseBodyAsStream();
     }
 
     @Override
     public String getResponseBodyAsString() throws DavioException {
       return client.getResponseBodyAsString();
     }
 
     @Override
     public Header getResponseHeader(final String name) throws DavioException {
       return client.getResponseHeader(name);
     }
 
     @Override
     public String getResponseHeaderValue(final String name) throws DavioException {
       return client.getResponseHeaderValue(name);
     }
 
     @Override
     public void close() {
       try {
         client.release();
       } catch (Throwable t) {
         //error(t)
       }
     }
   }
 */
 
   private DavChild makeDavResponse(final Element resp) throws Throwable {
     /*    <!ELEMENT response (href, ((href*, status)|(propstat+)),
           responsedescription?) >
      */
     Iterator<Element> elit = getChildren(resp).iterator();
 
     Node nd = elit.next();
 
     DavChild dc = new DavChild();
 
     if (!XmlUtil.nodeMatches(nd, WebdavTags.href)) {
       throw new Exception("Bad response. Expected href found " + nd);
     }
 
     dc.uri = URLDecoder.decode(getElementContent((Element)nd),
                                HTTP.UTF_8); // href should be escaped
 
     while (elit.hasNext()) {
       nd = elit.next();
 
       if (!XmlUtil.nodeMatches(nd, WebdavTags.propstat)) {
         throw new Exception("Bad response. Expected propstat found " + nd);
       }
 
       /*    <!ELEMENT propstat (prop, status, responsedescription?) > */
 
       Iterator<Element> propstatit = getChildren(nd).iterator();
       Node propnd = propstatit.next();
 
       if (!XmlUtil.nodeMatches(propnd, WebdavTags.prop)) {
         throw new Exception("Bad response. Expected prop found " + propnd);
       }
 
       if (!propstatit.hasNext()) {
         throw new Exception("Bad response. Expected propstat/status");
       }
 
       int st = httpStatus(propstatit.next());
 
       if (propstatit.hasNext()) {
         Node rdesc = propstatit.next();
 
         if (!XmlUtil.nodeMatches(rdesc, WebdavTags.responseDescription)) {
           throw new Exception("Bad response, expected null or " +
               "responsedescription. Found: " + rdesc);
         }
       }
 
       /* process each property with this status */
 
       Collection<Element> respProps = getChildren(propnd);
 
       for (Element pr: respProps) {
         /* XXX This needs fixing to handle content that is xml
          */
         if (XmlUtil.nodeMatches(pr, WebdavTags.resourcetype)) {
           Collection<Element> rtypeProps = getChildren(pr);
 
           for (Element rtpr: rtypeProps) {
             if (XmlUtil.nodeMatches(rtpr, WebdavTags.collection)) {
               dc.isCollection = true;
               break;
             }
           }
         } else {
           DavProp dp = new DavProp();
 
           dc.propVals.add(dp);
 
           dp.name = new QName(pr.getNamespaceURI(), pr.getLocalName());
           dp.status = st;
           dp.content = getElementContent(pr);
 
           if (XmlUtil.nodeMatches(pr, WebdavTags.displayname)) {
             dc.displayName = dp.content;
           }
         }
       }
     }
 
     return dc;
   }
 
   private String normalizePath(final String path) {
     if (!path.endsWith("/")) {
       return path + "/";
     }
 
     return path;
   }
 
   /** ===================================================================
    *                   Logging methods
    *  =================================================================== */
 
   /**
    * @return Logger
    */
   protected Logger getLogger() {
     if (log == null) {
       log = Logger.getLogger(this.getClass());
     }
 
     return log;
   }
 
   protected void debugMsg(final String msg) {
     getLogger().debug(msg);
   }
 
   protected void trace(final String msg) {
     getLogger().debug(msg);
   }
 }
