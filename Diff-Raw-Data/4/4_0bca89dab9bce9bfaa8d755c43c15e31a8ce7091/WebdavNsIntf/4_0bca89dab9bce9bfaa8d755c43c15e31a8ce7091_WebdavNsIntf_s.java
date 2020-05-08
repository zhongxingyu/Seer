 /* **********************************************************************
     Copyright 2008 Rensselaer Polytechnic Institute. All worldwide rights reserved.
 
     Redistribution and use of this distribution in source and binary forms,
     with or without modification, are permitted provided that:
        The above copyright notice and this permission notice appear in all
         copies and supporting documentation;
 
         The name, identifiers, and trademarks of Rensselaer Polytechnic
         Institute are not used in advertising or publicity without the
         express prior written permission of Rensselaer Polytechnic Institute;
 
     DISCLAIMER: The software is distributed" AS IS" without any express or
     implied warranty, including but not limited to, any implied warranties
     of merchantability or fitness for a particular purpose or any warrant)'
     of non-infringement of any current or pending patent rights. The authors
     of the software make no representations about the suitability of this
     software for any particular purpose. The entire risk as to the quality
     and performance of the software is with the user. Should the software
     prove defective, the user assumes the cost of all necessary servicing,
     repair or correction. In particular, neither Rensselaer Polytechnic
     Institute, nor the authors of the software are liable for any indirect,
     special, consequential, or incidental damages related to the software,
     to the maximum extent the law permits.
 */
 
 package edu.rpi.cct.webdav.servlet.shared;
 
 import edu.rpi.sss.util.xml.XmlEmit;
 import edu.rpi.sss.util.xml.XmlUtil;
 import edu.rpi.sss.util.xml.tagdefs.WebdavTags;
 import edu.rpi.cct.webdav.servlet.common.AccessUtil;
 import edu.rpi.cct.webdav.servlet.common.MethodBase;
 import edu.rpi.cct.webdav.servlet.common.WebdavServlet;
 import edu.rpi.cct.webdav.servlet.common.WebdavUtils;
 import edu.rpi.cct.webdav.servlet.common.MethodBase.MethodInfo;
 import edu.rpi.cmt.access.Acl;
 
 import java.io.InputStream;
 import java.io.Reader;
 import java.io.Serializable;
 import java.net.URI;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.namespace.QName;
 
 import org.apache.log4j.Logger;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 /** This acts as an interface to the underlying namespace for which this
  * servlet is acting as a gateway. This could be a file system, a set of
  * dynamically created objects or some sort of CMS for example.
  *
  * A namespace consists of a number of nodes which may be containers for
  * other nodes or leaf nodes.
  *
  * All nodes are considered identical in their capabilities, that is, a
  * non-terminal node might contain content.
  *
  * Some nodes are aliases of other nodes (e.g. symlinks in a unix file
  * system). By default these aliases will be followed.
  *
  *   @author Mike Douglass
  */
 public abstract class WebdavNsIntf implements Serializable {
   protected boolean debug;
 
   protected transient Logger log;
 
   protected static class SessCt {
     int sessNum;
   }
 
   /** Mostly to distinguish trace entries */
   protected static volatile SessCt session = new SessCt();
   protected int sessNum;
 
   protected WebdavServlet servlet;
 
   protected Properties props;
 
   private HttpServletRequest req;
 
   /* User associated with session */
   protected String account;
   protected boolean anonymous;
 
   protected boolean dumpContent;
 
   protected XmlEmit xml;
 
   /** Table of methods - set by servlet
    */
   protected HashMap<String, MethodInfo> methods;
 
   /** Table of created methods
   private HashMap<String, MethodBase> createdMethods = new HashMap<String, MethodBase>();
    */
 
   /** Should we return ok status in multistatus?
    */
   boolean returnMultistatusOk = true;
 
   private String urlPrefix;
 
   /** Called before any other method is called to allow initialisation to
    * take place at the first or subsequent requests
    *
    * @param servlet
    * @param req
    * @param props
    * @param debug
    * @param methods    HashMap   table of method info
    * @param dumpContent
    * @throws WebdavException
    */
   public void init(WebdavServlet servlet,
                    HttpServletRequest req,
                    Properties props,
                    boolean debug,
                    HashMap<String, MethodInfo> methods,
                    boolean dumpContent) throws WebdavException {
     this.servlet = servlet;
     this.req = req;
     this.props = props;
     this.xml = new XmlEmit();
     this.debug = debug;
     this.methods = methods;
     this.dumpContent = dumpContent;
 
     synchronized (session) {
       session.sessNum++;
       sessNum = session.sessNum;
     }
 
     account = req.getRemoteUser();
     anonymous = (account == null) || (account.length() == 0);
     urlPrefix = WebdavUtils.getUrlPrefix(req);
 
     addNamespace(xml);
   }
 
   /**
    * @return String
    */
   public String getAccount() {
     return account;
   }
 
   /**
    * @return XmlEmit xmlemitter
    */
   public XmlEmit getXmlEmit() {
     return xml;
   }
 
   /**
    * @return HttpServletRequest
    */
   public HttpServletRequest getRequest() {
     return req;
   }
 
   /** Return DAV header
    *
    * @param node
    * @return  String
    * @throws WebdavException
    */
   public String getDavHeader(WebdavNsNode node) throws WebdavException {
     return "1, access-control, extended-mkcol";
   }
 
   /** Get an object suitable for use in parsing acls and generating access.
    *
    * @return AccessUtil implementation.
    * @throws WebdavException
    */
   public abstract AccessUtil getAccessUtil() throws WebdavException;
 
   /** Return true if we can PUT this resource/entity
   *
   * @param node
   * @return  boolean
   * @throws WebdavException
   */
  public abstract boolean canPut(WebdavNsNode node) throws WebdavException;
 
   /**
    * @return Collection of method names.
    */
   public Collection<String> getMethodNames() {
     return methods.keySet();
   }
 
   /** Return the named initialised method or null if no such method or the
    * method requires authentication and we are anonymous
    *
    * @param name  name
    * @return MethodBase object or null
    * @throws WebdavException
    */
   public MethodBase getMethod(String name) throws WebdavException {
     name = name.toUpperCase();
 
     /*
     MethodBase mb = createdMethods.get(name);
     if (mb != null) {
       return mb;
     }
 
     MethodInfo mi = methods.get(name);
 
     if ((mi == null) || getAnonymous() && mi.getRequiresAuth()) {
       return null;
     }
 
     try {
       mb = (MethodBase)mi.getMethodClass().newInstance();
 
       mb.init(this, debug, dumpContent);
 
       createdMethods.put(name, mb);
 
       return mb;
     } catch (Throwable t) {
       if (debug) {
         error(t);
       }
       throw new WebdavException(t);
     }
     */
     MethodInfo mi = methods.get(name);
 
     if ((mi == null) || getAnonymous() && mi.getRequiresAuth()) {
       return null;
     }
 
     try {
       MethodBase mb = (MethodBase)mi.getMethodClass().newInstance();
 
       mb.init(this, debug, dumpContent);
 
       return mb;
     } catch (Throwable t) {
       if (debug) {
         error(t);
       }
       throw new WebdavException(t);
     }
   }
 
   /**
    * @return boolean true for anon access
    */
   public boolean getAnonymous() {
     return anonymous;
   }
 
   /** Return the part of the href referring to the actual entity, e.g. <br/>
    * for http://localhost/ucaldav/user/caluser/calendar/2656-uwcal-demouwcalendar@mysite.edu.ics
    *
    * <br/>user/caluser/calendar/2656-uwcal-demouwcalendar@mysite.edu.ics
    *
    * @param href
    * @return String
    * @throws WebdavException
    */
   public String getUri(String href) throws WebdavException {
     try {
       if (href == null) {
         throw new WebdavException("bad URI " + href);
       }
 
       String context = req.getContextPath();
 
       if (href.startsWith(context)) {
         return href.substring(context.length());
       }
 
       URL url = new URL(href);
 
       String path = url.getPath();
 
       if ((path == null) || (path.length() <= 1)) {
         return path;
       }
 
       if (context == null) {
         return path;
       }
 
       if (path.indexOf(context) != 0){
         return path;
       }
 
       int pos = context.length();
 
       if (path.length() == pos) {
         return "";
       }
 
       if (path.charAt(pos) != '/') {
         throw new WebdavException("bad URI " + href);
       }
 
       return path.substring(pos);
     } catch (Throwable t) {
       if (debug) {
         error(t);
       }
       throw new WebdavException(t);
     }
   }
 
   /**
    * @return WebdavServlet
    */
   public WebdavServlet getServlet() {
     return servlet;
   }
 
   /**
    * @return boolean
    */
   public boolean getReturnMultistatusOk() {
     return returnMultistatusOk;
   }
 
   /** Add any namespaces for xml tag names in requests and responses.
    * An abbreviation will be supplied by the servlet.
 
    * The name should be globally unique in a global sense so don't return
    * something like "RPI:"
    *
    * <p>Something more like "http://ahost.rpi.edu/webdav/"
    *
    * @param xml
    * @throws WebdavException
    */
   public void addNamespace(XmlEmit xml) throws WebdavException {
     try {
       xml.addNs(WebdavTags.namespace);
     } catch (Throwable t) {
       throw new WebdavException(t);
     }
   }
 
   /** Return true if the system disallows directory browsing.
    *
    * @return boolean
    * @throws WebdavException
    */
   public abstract boolean getDirectoryBrowsingDisallowed() throws WebdavException;
 
   /** Called on the way out to allow resources to be freed.
    *
    * @throws WebdavException
    */
   public abstract void close() throws WebdavException;
 
   /** Returns the supported locks for the supportedlock property.
    *
    * <p>To ensure these will work always provide the full namespace "DAV:"
    * for example, the result for supported exclusive and shared write locks
    * would be the string
    *
    *  "&lt;DAV:lockentry&gt;" +
    *  "  &lt;DAV:lockscope&gt;&lt;DAV:exclusive/&gt;&lt;DAV:/lockscope&gt;" +
    *  "  &lt;DAV:locktype&gt;&lt;DAV:write/&gt;&lt;DAV:/locktype&gt;" +
    *  "&lt;DAV:/lockentry&gt;" +
    *  "&lt;DAV:lockentry&gt;" +
    *  "  &lt;DAV:lockscope&gt;&lt;DAV:shared/&gt;&lt;DAV:/lockscope&gt;" +
    *  "&lt;DAV:/lockentry&gt;"
    *
    * @return String response
    */
   public abstract String getSupportedLocks();
 
   /** Returns true if the namespace supports access control
    *
    * @return boolean
    */
   public abstract boolean getAccessControl();
 
   //ENUM
 
   /** Must not exist */
   public static final int existanceNot = 0;
 
   /** Must exist. */
   public static final int existanceMust = 1;
 
   /** We know it exists. */
   public static final int existanceDoesExist = 2;
 
   /** May exist */
   public static final int existanceMay = 3;
 
   //ENUM
 
   /** Must be collection */
   public static final int nodeTypeCollection = 0;
 
   /** Must be entity. */
   public static final int nodeTypeEntity = 1;
 
   /** Must be a principal. */
   public static final int nodeTypePrincipal = 2;
 
   /** Unknown. */
   public static final int nodeTypeUnknown = 3;
 
   /** Retrieves a node by uri, following any links.
    *
    * @param uri              String decoded uri of the node to retrieve
    * @param existance        Say's something about the state of existance
    * @param nodeType         Say's something about the type of node
    * @return WebdavNsNode    node specified by the URI or the node aliased by
    *                         the node at the URI.
    * @throws WebdavException
    */
   public abstract WebdavNsNode getNode(String uri,
                                        int existance,
                                        int nodeType)
       throws WebdavException;
 
   /** Stores/updates an object.
    *
    * @param node             node in question
    * @throws WebdavException
    */
   public abstract void putNode(WebdavNsNode node)
       throws WebdavException;
 
   /** Deletes a node from the namespace.
    *
    * @param node             node in question
    * @throws WebdavException
    */
   public abstract void delete(WebdavNsNode node)
       throws WebdavException;
 
   /** Returns the immediate children of a node.
    *
    * @param node             node in question
    * @return Collection      of WebdavNsNode children
    * @throws WebdavException
    */
   public abstract Collection<WebdavNsNode> getChildren(WebdavNsNode node)
       throws WebdavException;
 
   /** Returns the parent of a node.
    *
    * @param node             node in question
    * @return WebdavNsNode    node's parent, or null if the specified node
    *                         is the root
    * @throws WebdavException
    */
   public abstract WebdavNsNode getParent(WebdavNsNode node)
       throws WebdavException;
 
   /** Returns a Reader for the content.
    *
    * @param node             node in question
    * @return Reader          A reader for the content.
    * @throws WebdavException
    */
   public abstract Reader getContent(WebdavNsNode node)
       throws WebdavException;
 
   /** Returns an InputStream for the binary content.
    *
    * @param node             node in question
    * @return inputStream     A stream for the content.
    * @throws WebdavException
    */
   public abstract InputStream getBinaryContent(WebdavNsNode node)
       throws WebdavException;
 
   /** Result for putContent
    */
   public static class PutContentResult {
     /** Same node or new node for creation */
     public WebdavNsNode node;
 
     /** True if created */
     public boolean created;
   }
 
   /** Set the content from a Reader
    *
    * @param node              node in question.
    * @param contentTypePars   null or values from content-type header
    * @param contentRdr        Reader for content
    * @param create            true if this is a probably creation
    * @param ifEtag            if non-null etag must match
    * @return PutContentResult result of creating
    * @throws WebdavException
    */
   public abstract PutContentResult putContent(WebdavNsNode node,
                                               String[] contentTypePars,
                                               Reader contentRdr,
                                               boolean create,
                                               String ifEtag)
       throws WebdavException;
 
   /** Set the content from a Stream
    *
    * @param node              node in question.
    * @param contentTypePars   null or values from content-type header
    * @param contentStream     Stream for content
    * @param create            true if this is a probably creation
    * @param ifEtag            if non-null etag must match
    * @return PutContentResult result of creating
    * @throws WebdavException
    */
   public abstract PutContentResult putBinaryContent(WebdavNsNode node,
                                                     String[] contentTypePars,
                                               InputStream contentStream,
                                               boolean create,
                                               String ifEtag)
       throws WebdavException;
 
   /** Create a new node.
    *
    * @param node             node to create with new uri set
    * @throws WebdavException
    */
   public abstract void create(WebdavNsNode node)
       throws WebdavException;
 
   /** Creates an alias to another node.
    *
    * @param alias       alias node that should be created with uri and
    *                    targetUri set
    * @throws WebdavException
    */
   public abstract void createAlias(WebdavNsNode alias)
       throws WebdavException;
 
   /** Throw an exception if we don't want the content for mkcol.
    *
    * @param req       HttpServletRequest
    * @throws WebdavException
    */
   public abstract void acceptMkcolContent(HttpServletRequest req)
       throws WebdavException;
 
   /** Create an empty collection at the given location. Status is set on return
    *
    * @param req       HttpServletRequest
    * @param resp      HttpServletResponse
    * @param node      node to create
    * @throws WebdavException
    */
   public abstract void makeCollection(HttpServletRequest req,
                                       HttpServletResponse resp,
                                       WebdavNsNode node)
       throws WebdavException;
 
   /** Copy or move a resource at the given location to another location.
    * Status is set on return
    *
    * @param req       HttpServletRequest
    * @param resp      HttpServletResponse
    * @param from      Source
    * @param to        Destination
    * @param copy      true for copying
    * @param overwrite true to overwrite destination
    * @param depth     0 for entity, infinity for collection.
    * @throws WebdavException
    */
   public abstract void copyMove(HttpServletRequest req,
                                 HttpServletResponse resp,
                                 WebdavNsNode from,
                                 WebdavNsNode to,
                                 boolean copy,
                                 boolean overwrite,
                                 int depth) throws WebdavException;
 
   /** Handle a special resource uri for GET.
    * Status is set on return
    *
    * @param req       HttpServletRequest
    * @param resp      HttpServletResponse
    * @param resourceUri
    * @return boolean true if it was a special uri and is processed
    * @throws WebdavException
    */
   public abstract boolean specialUri(HttpServletRequest req,
                                      HttpServletResponse resp,
                                      String resourceUri) throws WebdavException;
 
   /* ====================================================================
    *                  Access methods
    * ==================================================================== */
 
   /** Given a PrincipalMatchReport returns a Collection of matching nodes.
    *
    * @param resourceUri - url to base search on.
    * @param principalUrl - url of principal or null for current user
    * @return Collection of WebdavNsNode
    * @throws WebdavException
    */
   public abstract Collection<WebdavNsNode> getGroups(String resourceUri,
                                                      String principalUrl)
           throws WebdavException;
 
   /** Given a uri returns a Collection of uris that allow search operations on
    * principals for that resource.
    *
    * @param resourceUri
    * @return Collection of String
    * @throws WebdavException
    */
   public abstract Collection<String> getPrincipalCollectionSet(String resourceUri)
          throws WebdavException;
 
   /** Given a PrincipalPropertySearch returns a Collection of matching nodes.
    *
    * @param resourceUri
    * @param pps Collection of PrincipalPropertySearch
    * @return Collection of WebdavNsNode
    * @throws WebdavException
    */
   public abstract Collection<? extends WebdavNsNode> getPrincipals(String resourceUri,
                                            PrincipalPropertySearch pps)
           throws WebdavException;
 
   /**
    * @param id
    * @return String href
    * @throws WebdavException
    */
   public abstract String makeUserHref(String id) throws WebdavException;
 
   /** Object class passed around as we parse access.
    */
   public static class AclInfo {
     /** uri of object */
     public String what;
 
     /** Set non-null if error occurred -- see Acl 8.1.1 */
     public QName errorTag;
 
     /** The resulting Acl */
     public Acl acl;
 
     /** Constructor
      *
      * @param uri
      */
     public AclInfo(String uri) {
       what = uri;
     }
   }
 
   /**
    * @param ainfo
    * @throws WebdavException
    */
   public abstract void updateAccess(AclInfo ainfo) throws WebdavException;
 
   /**
    * @param node
    * @throws WebdavException
    */
   public abstract void emitAcl(WebdavNsNode node) throws WebdavException;
 
   /** Return all the hrefs found in the access for th egiven node.
    *
    * @param node
    * @return Collection of hrefs.
    * @throws WebdavException
    */
   public abstract Collection<String> getAclPrincipalInfo(WebdavNsNode node)
           throws WebdavException;
 
   /* ====================================================================
    *                Property value methods
    * ==================================================================== */
 
   /**
    * @param node
    * @throws WebdavException
    */
   public void emitSupportedReportSet(WebdavNsNode node) throws WebdavException {
     try {
       xml.openTag(WebdavTags.supportedReportSet);
 
       Collection<QName> supportedReports = node.getSupportedReports();
 
       for (QName qn: supportedReports) {
         xml.openTag(WebdavTags.supportedReport);
         xml.openTag(WebdavTags.report);
         xml.emptyTag(qn);
         xml.closeTag(WebdavTags.report);
         xml.closeTag(WebdavTags.supportedReport);
       }
       xml.closeTag(WebdavTags.supportedReportSet);
     } catch (Throwable t) {
       throw new WebdavException(t);
     }
   }
 
   /** Open a propstat response.
    *
    * @throws WebdavException
    */
   public void openPropstat() throws WebdavException {
     try {
       xml.openTag(WebdavTags.propstat);
       xml.openTag(WebdavTags.prop);
     } catch (Throwable t) {
       throw new WebdavException(t);
     }
   }
 
   /** Close a propstat response with given result.
    *
    * @param status
    * @throws WebdavException
    */
   public void closePropstat(int status) throws WebdavException {
     try {
       xml.closeTag(WebdavTags.prop);
 
       if ((status != HttpServletResponse.SC_OK) ||
           getReturnMultistatusOk()) {
         xml.property(WebdavTags.status, "HTTP/1.1 " + status + " " +
                      WebdavStatusCode.getMessage(status));
       }
 
       xml.closeTag(WebdavTags.propstat);
     } catch (Throwable t) {
       throw new WebdavException(t);
     }
   }
 
   /** Close a propstat response with an ok result.
    *
    * @throws WebdavException
    */
   public void closePropstat() throws WebdavException {
     closePropstat(HttpServletResponse.SC_OK);
   }
 
   /** Parse a <prop> list of property names in any namespace.
    *
    * @param nd
    * @return Collection
    * @throws WebdavException
    */
   public Collection<WebdavProperty> parseProp(Node nd) throws WebdavException {
     Collection<WebdavProperty> props = new ArrayList<WebdavProperty>();
 
     Element[] children = getChildren(nd);
 
     for (int i = 0; i < children.length; i++) {
       Element propnode = children[i];
       String ns = propnode.getNamespaceURI();
 
       xml.addNs(ns);
 
       WebdavProperty prop = makeProp(propnode);
 
       if (debug) {
         trace("prop: " + prop.getTag());
       }
 
       props.add(prop);
     }
 
     return props;
   }
 
   /** Override this to create namespace specific property objects.
    *
    * @param propnode
    * @return WebdavProperty
    * @throws WebdavException
    */
   public WebdavProperty makeProp(Element propnode) throws WebdavException {
     return new WebdavProperty(new QName(propnode.getNamespaceURI(),
                                         propnode.getLocalName()),
                                         null);
   }
 
   /** Properties we can process */
   private static final QName[] knownProperties = {
     //    WebdavTags.lockdiscovery,
     //    WebdavTags.source,
     //    WebdavTags.supportedlock,
     //    WebdavTags.aclRestrictions,
     //    WebdavTags.inheritedAclSet,
     WebdavTags.principalCollectionSet,
   };
 
   /** Return true if a call to generatePropValue will return a value.
    *
    * @param node
    * @param pr
    * @return boolean
    */
   public boolean knownProperty(WebdavNsNode node,
                                WebdavProperty pr) {
     QName tag = pr.getTag();
 
     for (int i = 0; i < knownProperties.length; i++) {
       if (tag.equals(knownProperties[i])) {
         return true;
       }
     }
 
     /* Try the node for a value */
 
     return node.knownProperty(tag);
   }
 
   /** Generate a response for a single webdav property. This should be overrriden
    * to handle other namespaces.
    *
    * @param node
    * @param pr
    * @param allProp   true if we're doing allprop
    * @return boolean false for unknown (or unset)
    * @throws WebdavException
    */
   public boolean generatePropValue(WebdavNsNode node,
                                    WebdavProperty pr,
                                    boolean allProp) throws WebdavException {
     QName tag = pr.getTag();
     String ns = tag.getNamespaceURI();
 
     try {
       /* Deal with webdav properties */
       if (!ns.equals(WebdavTags.namespace)) {
         // Not ours
         //xml.emptyTag(tag);
         return false;
       }
 
       if (tag.equals(WebdavTags.lockdiscovery)) {
         // dav 13.8
         //xml.emptyTag(tag);
         return false;
       }
 
       if (tag.equals(WebdavTags.source)) {
         // dav 13.10
         //xml.emptyTag(tag);
         return false;
       }
 
       if (tag.equals(WebdavTags.supportedlock)) {
         // dav 13.11
         //xml.emptyTag(tag);
         return false;
       }
 
       if (tag.equals(WebdavTags.aclRestrictions)) {
         // access 5.5
         return false;
       }
 
       if (tag.equals(WebdavTags.inheritedAclSet)) {
         // access 5.6
         return false;
       }
 
       if (tag.equals(WebdavTags.principalCollectionSet)) {
         // access 5.7
         xml.openTag(WebdavTags.principalCollectionSet);
 
         for (String s: getPrincipalCollectionSet(node.getUri())) {
           xml.property(WebdavTags.href, s);
         }
 
         xml.closeTag(WebdavTags.principalCollectionSet);
         return true;
       }
 
       /* Try the node for a value */
 
       if (node.generatePropertyValue(tag, this, allProp)) {
         // Generated by node
         return true;
       }
 
       // Not known
       return false;
     } catch (WebdavException wde) {
       throw wde;
     } catch (Throwable t) {
       throw new WebdavException(t);
     }
   }
 
   /** Return the complete URL describing the location of the object
    * represented by the node
    *
    * @param node             node in question
    * @return String      url
    * @throws WebdavException
    */
   public String getLocation(WebdavNsNode node) throws WebdavException {
     try {
       if (debug) {
         trace("Get url " + urlPrefix + node.getEncodedUri());
       }
 
       String url = urlPrefix + new URI(node.getEncodedUri()).toASCIIString();
 
       if (url.endsWith("/")) {
         if (!node.trailSlash()) {
           url = url.substring(0, url.length() - 1);
         }
       } else {
         if (node.trailSlash()) {
           url = url + "/";
         }
       }
       return url;
     } catch (WebdavException wde) {
       throw wde;
     } catch (Throwable t) {
       throw new WebdavException(t);
     }
   }
 
   /**
    * @param status
    * @throws WebdavException
    */
   public void addStatus(int status) throws WebdavException {
     try {
       xml.property(WebdavTags.status, "HTTP/1.1 " + status + " " +
                WebdavStatusCode.getMessage(status));
     } catch (Throwable t) {
       throw new WebdavException(t);
     }
   }
 
   /** Return a path, beginning with a "/", after "." and ".." are removed.
    * If the parameter path attempts to go above the root we return null.
    *
    * Other than the backslash thing why not use URI?
    *
    * @param path      String path to be fixed
    * @return String   fixed path
    * @throws WebdavException
    */
   public static String fixPath(String path) throws WebdavException {
     if (path == null) {
       return null;
     }
 
     String decoded;
     try {
       decoded = URLDecoder.decode(path, "UTF8");
     } catch (Throwable t) {
       throw new WebdavBadRequest("bad path: " + path);
     }
 
     if (decoded == null) {
       return (null);
     }
 
     /** Make any backslashes into forward slashes.
      */
     if (decoded.indexOf('\\') >= 0) {
       decoded = decoded.replace('\\', '/');
     }
 
     /** Ensure a leading '/'
      */
     if (!decoded.startsWith("/")) {
       decoded = "/" + decoded;
     }
 
     /** Remove all instances of '//'.
      */
     while (decoded.indexOf("//") >= 0) {
       decoded = decoded.replaceAll("//", "/");
     }
 
     if (decoded.indexOf("/.") < 0) {
       return decoded;
     }
 
     /** Somewhere we may have /./ or /../
      */
 
     StringTokenizer st = new StringTokenizer(decoded, "/");
 
     ArrayList<String> al = new ArrayList<String>();
     while (st.hasMoreTokens()) {
       String s = st.nextToken();
 
       if (s.equals(".")) {
         // ignore
       } else if (s.equals("..")) {
         // Back up 1
         if (al.size() == 0) {
           // back too far
           return null;
         }
 
         al.remove(al.size() - 1);
       } else {
         al.add(s);
       }
     }
 
     /** Reconstruct */
     StringBuffer sb = new StringBuffer();
     for (String s: al) {
       sb.append('/');
       sb.append(s);
     }
 
     return sb.toString();
   }
 
   /* ====================================================================
    *                   XmlUtil wrappers
    * ==================================================================== */
 
   /** Get all the children if any
    *
    * @param nd
    * @return array of Element
    * @throws WebdavException
    */
   public Element[] getChildren(Node nd) throws WebdavException {
     try {
       return XmlUtil.getElementsArray(nd);
     } catch (Throwable t) {
       if (debug) {
         getLogger().error(this, t);
       }
 
       throw new WebdavBadRequest();
     }
   }
 
   /** We expect a single child
    *
    * @param nd
    * @return Element
    * @throws WebdavException
    */
   public Element getOnlyChild(Node nd) throws WebdavException {
     try {
       return XmlUtil.getOnlyElement(nd);
     } catch (Throwable t) {
       if (debug) {
         getLogger().error(this, t);
       }
 
       throw new WebdavBadRequest();
     }
   }
 
   /**
    * @param el
    * @return String
    * @throws WebdavException
    */
   public String getElementContent(Element el) throws WebdavException {
     try {
       return XmlUtil.getElementContent(el);
     } catch (Throwable t) {
       if (debug) {
         getLogger().error(this, t);
       }
 
       throw new WebdavBadRequest();
     }
   }
 
   /* ====================================================================
    *                        Protected methods
    * ==================================================================== */
 
   protected Logger getLogger() {
     if (log == null) {
       log = Logger.getLogger(this.getClass());
     }
 
     return log;
   }
 
   protected void trace(String msg) {
     getLogger().debug("[" + sessNum + "] " + msg);
   }
 
   protected void debugMsg(String msg) {
     getLogger().debug("[" + sessNum + "] " + msg);
   }
 
   protected void warn(String msg) {
     getLogger().warn("[" + sessNum + "] " + msg);
   }
 
   protected void error(Throwable t) {
     getLogger().error(this, t);
   }
 
   protected void logIt(String msg) {
     getLogger().info("[" + sessNum + "] " + msg);
   }
 }
