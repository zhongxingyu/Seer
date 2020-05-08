 /*
  Copyright (c) 2000-2005 University of Washington.  All rights reserved.
 
  Redistribution and use of this distribution in source and binary forms,
  with or without modification, are permitted provided that:
 
    The above copyright notice and this permission notice appear in
    all copies and supporting documentation;
 
    The name, identifiers, and trademarks of the University of Washington
    are not used in advertising or publicity without the express prior
    written permission of the University of Washington;
 
    Recipients acknowledge that this distribution is made available as a
    research courtesy, "as is", potentially with defects, without
    any obligation on the part of the University of Washington to
    provide support, services, or repair;
 
    THE UNIVERSITY OF WASHINGTON DISCLAIMS ALL WARRANTIES, EXPRESS OR
    IMPLIED, WITH REGARD TO THIS SOFTWARE, INCLUDING WITHOUT LIMITATION
    ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
    PARTICULAR PURPOSE, AND IN NO EVENT SHALL THE UNIVERSITY OF
    WASHINGTON BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL
    DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR
    PROFITS, WHETHER IN AN ACTION OF CONTRACT, TORT (INCLUDING
    NEGLIGENCE) OR STRICT LIABILITY, ARISING OUT OF OR IN CONNECTION WITH
    THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 /* **********************************************************************
     Copyright 2005 Rensselaer Polytechnic Institute. All worldwide rights reserved.
 
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
 
 import edu.rpi.cct.webdav.servlet.common.WebdavUtils;
 import edu.rpi.cmt.access.AccessXmlUtil;
 import edu.rpi.cmt.access.PrivilegeSet;
 import edu.rpi.cmt.access.Acl.CurrentAccess;
 import edu.rpi.sss.util.xml.QName;
 import edu.rpi.sss.util.xml.XmlEmit;
 import edu.rpi.sss.util.xml.tagdefs.WebdavTags;
 
 import java.io.Reader;
 import java.io.Serializable;
 import java.io.StringReader;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 
 import org.apache.log4j.Logger;
 import org.w3c.dom.Element;
 
 /** Represents a node in the underlying namespace for which this
  * servlet is acting as a gateway. This could be a file system, a set of
  * dynamically created objects or some sort of CMS for example.
  *
  *   @author Mike Douglass   douglm@rpi.edu
  */
 public abstract class WebdavNsNode implements Serializable {
   protected boolean debug;
 
   /** Does the resource exist? */
   protected boolean exists = true;
 
   private transient Logger log;
 
   /** Uri of the node. These are relative to the root of the namespace this
    * interface represents and should start with a "/". For example, if this
    * namespace is part of a file system starting at uabc in /var/local/uabc
    * and we are referring to a directory x at /var/local/uabc/a/x then the
    * uri should be /a/x/
    */
   protected String uri;
 
   /** Suitable for display
    */
   //protected String name;
 
   protected String path;
 
   /** True if this node is a collection
    */
   protected boolean collection;
 
   /** True if this node is a user
    */
   protected boolean userPrincipal;
 
   /** True if this node is a group
    */
   protected boolean groupPrincipal;
 
   /** True if GET is allowed
    */
   protected boolean allowsGet;
 
   /** Can be set to indicate some sort of abnormal condition on this node,
    * e.g. no access.
    */
   protected int status = HttpServletResponse.SC_OK;
 
   private final static HashMap<QName, PropertyTagEntry> propertyNames =
     new HashMap<QName, PropertyTagEntry>();
 
   private final static Collection<QName> supportedReports = new ArrayList<QName>();
 
   /** */
   public static final class PropertyTagEntry {
     /** */
     public QName tag;
     /** */
     public boolean inPropAll = false;
 
     /**
      * @param tag
      */
     public PropertyTagEntry(QName tag) {
       this.tag = tag;
     }
 
     /**
      * @param tag
      * @param inPropAll
      */
     public PropertyTagEntry(QName tag, boolean inPropAll) {
       this.tag = tag;
       this.inPropAll = inPropAll;
     }
   }
 
   static {
     addPropEntry(propertyNames, WebdavTags.acl);
     // addPropEntry(propertyNames, WebdavTags.aclRestrictons, false);
     addPropEntry(propertyNames, WebdavTags.creationdate, true);
     addPropEntry(propertyNames, WebdavTags.currentUserPrivilegeSet);
     addPropEntry(propertyNames, WebdavTags.displayname, true);
     addPropEntry(propertyNames, WebdavTags.getcontentlanguage, true);
     addPropEntry(propertyNames, WebdavTags.getcontentlength, true);
     addPropEntry(propertyNames, WebdavTags.getcontenttype, true);
     addPropEntry(propertyNames, WebdavTags.getetag, true);
     addPropEntry(propertyNames, WebdavTags.getlastmodified, true);
     //addPropEntry(propertyNames, WebdavTags.group, false);
     //addPropEntry(propertyNames, WebdavTags.inheritedAclSet, false);
     addPropEntry(propertyNames, WebdavTags.owner);
     //addPropEntry(propertyNames, WebdavTags.principalCollectionSet, false);
     addPropEntry(propertyNames, WebdavTags.principalURL);
     addPropEntry(propertyNames, WebdavTags.resourcetype, true);
     addPropEntry(propertyNames, WebdavTags.supportedPrivilegeSet);
 
     /* Supported reports */
 
     supportedReports.add(WebdavTags.expandProperty);          // Version
     supportedReports.add(WebdavTags.aclPrincipalPropSet);     // Acl
     supportedReports.add(WebdavTags.principalMatch);          // Acl
     supportedReports.add(WebdavTags.principalPropertySearch); // Acl
   }
 
   /* ....................................................................
    *                   Alias fields
    * .................................................................... */
 
   /** true if this is an alias
    */
   protected boolean alias;
 
   protected String targetUri;
 
   protected UrlHandler urlHandler;
 
 
   /** Prefix or unprefix urls - or not depending on internal state
    *
    * @author douglm
    */
   public static class UrlHandler {
     private String urlPrefix;
 
     private boolean relative;
 
     private String context;
 
     /** If relative we assume urls are relative to the host + port.
      * Internally we need to strip off the host + port + context.
      *
      * @param req
      * @param relative
      * @throws WebdavException
      */
     public UrlHandler(HttpServletRequest req,
                       boolean relative) throws WebdavException {
       this.relative = relative;
 
       try {
         urlPrefix = req.getRequestURL().toString();
 
         int pos = urlPrefix.indexOf(req.getContextPath());
 
         if (pos > 0) {
           urlPrefix = urlPrefix.substring(0, pos);
         }
 
         context = req.getContextPath();
         if ((context == null) || (context.equals("."))) {
           context = "";
         }
       } catch (Throwable t) {
         Logger.getLogger(WebdavUtils.class).warn(
             "Unable to get url from " + req);
         throw new WebdavException(t);
       }
     }
 
     /** Return an appropriately prefixed url. The parameter url will be
      * absolute or relative. If relative it may be prefixed with the context
      * path which we need to remove.
      *
      * <p>We're doing this because some clients don't handle absolute urls
      * (a violation of the spec)
      *
      * @param val
      * @return String
      * @throws WebdavException
      */
     public String prefix(String val) throws WebdavException {
       try {
         if (val.startsWith("mailto:")) {
           return val;
         }
 
         String enc = new URI(null, null, val, null).toString();
         enc = new URI(enc).toASCIIString();  // XXX ???????
 
         StringBuilder sb = new StringBuilder();
 
         if (!relative) {
           sb.append(getUrlPrefix());
          sb.append("/");
           sb.append(context);
         } else {
           if (!context.startsWith("/")) {
             sb.append("/");
           }
           sb.append(context);
 
         }
 
         if (!enc.startsWith("/")) {
           if ((sb.length() == 0) || (sb.charAt(sb.length() - 1) != '/')) {
             sb.append("/");
           }
         }
 
         sb.append(enc);
 
         return sb.toString();
       } catch (Throwable t) {
         throw new WebdavException(t);
       }
     }
 
     /** Remove any vestige of the host, port or context
      *
      * @param val
      * @return String
      * @throws WebdavException
      */
     public String unprefix(String val) throws WebdavException {
       if (val.startsWith(getUrlPrefix())) {
         val = val.substring(getUrlPrefix().length());
       }
 
       if (val.startsWith(context)) {
         val = val.substring(context.length());
       }
 
       return val;
     }
 
     /**
      * @return String url prefix (host + port, no context)
      */
     public String getUrlPrefix() {
       return urlPrefix;
     }
   }
 
   /** Constructor
    *
    * @param urlHandler - needed for building hrefs.
    * @param path - resource path
    * @param collection - true if this is a collection
    * @param debug
    */
   public WebdavNsNode(UrlHandler urlHandler, String path,
                       boolean collection, boolean debug) {
     this.urlHandler = urlHandler;
     this.path = path;
     this.collection = collection;
     this.debug = debug;
   }
 
   /* ====================================================================
    *                   Abstract methods
    * ==================================================================== */
 
   /** Get the current access granted to this principal for this node.
    *
    * @return CurrentAccess
    * @throws WebdavException
    */
   public abstract CurrentAccess getCurrentAccess() throws WebdavException;
 
   /** Update this node after changes.
    *
    * @throws WebdavException
    */
   public abstract void update() throws WebdavException;
 
   /** Result from setting or removing property
    *
    */
   public static class SetPropertyResult {
     /** */
     public Element prop;
     /** */
     public int status = HttpServletResponse.SC_OK;
     /** */
     public String message;
 
     /**
      * @param prop
      */
     public SetPropertyResult(Element prop) {
       this.prop = prop;
     }
   }
 
   /** Trailing "/" on uri?
    *
    * @return boolean
    */
   public abstract boolean trailSlash();
 
   /**
    * @return String
    */
   public String getPath() {
     return path;
   }
 
   /* ====================================================================
    *                   Property methods
    * ==================================================================== */
 
   /**
    * @param xml
    * @throws WebdavException
    */
   public void generateHref(XmlEmit xml) throws WebdavException {
     try {
       generateUrl(xml, WebdavTags.href, uri);
 //      String url = getUrlPrefix() + new URI(getEncodedUri()).toASCIIString();
 //      xml.property(WebdavTags.href, url);
     } catch (WebdavException wde) {
       throw wde;
     } catch (Throwable t) {
       throw new WebdavException(t);
     }
   }
 
   /**
    * @param xml
    * @param uri
    * @throws WebdavException
    */
   public void generateHref(XmlEmit xml, String uri) throws WebdavException {
     generateUrl(xml, WebdavTags.href, uri);
   }
 
   /**
    * @param xml
    * @param tag
    * @param uri
    * @throws WebdavException
    */
   public void generateUrl(XmlEmit xml, QName tag, String uri) throws WebdavException {
     try {
       /*
       String enc = new URI(null, null, uri, null).toString();
       enc = new URI(enc).toASCIIString();  // XXX ???????
 
       StringBuilder sb = new StringBuilder();
 
       if (!relativeUrls) {
         sb.append(getUrlPrefix());
       }
 
 //      if (!enc.startsWith("/")) {
 //        sb.append("/");
 //      }
       if (getExists()) {
         if (enc.endsWith("/")) {
           if (!trailSlash()) {
             enc = enc.substring(0, enc.length() - 1);
           }
         } else {
           if (trailSlash()) {
             enc = enc + "/";
           }
         }
       }
 
       if (!enc.startsWith("/")) {
         if ((sb.length() == 0) || (sb.charAt(sb.length() - 1) != '/')) {
           sb.append("/");
         }
       }
 
       sb.append(enc);
       xml.property(tag, sb.toString());
       */
       String prefixed = urlHandler.prefix(uri);
 
       if (getExists()) {
         if (prefixed.endsWith("/")) {
           if (!trailSlash()) {
             prefixed = prefixed.substring(0, prefixed.length() - 1);
           }
         } else {
           if (trailSlash()) {
             prefixed = prefixed + "/";
           }
         }
       }
 
       xml.property(tag, prefixed);
     } catch (Throwable t) {
       throw new WebdavException(t);
     }
   }
 
   /**
    */
   public static class PropVal {
     /**
      */
     public boolean notFound;
 
     /**
      */
     public String val;
   }
 
   /** Remove the given property for this node.
    *
    * @param val   Element defining property to remove
    * @param spr   Holds reult of removing property
    * @return boolean  true if property recognized.
    * @throws WebdavException
    */
   public boolean removeProperty(Element val,
                                 SetPropertyResult spr) throws WebdavException {
     try {
       if (WebdavTags.getetag.nodeMatches(val)) {
         spr.status = HttpServletResponse.SC_FORBIDDEN;
         return true;
       }
 
       if (WebdavTags.getlastmodified.nodeMatches(val)) {
         spr.status = HttpServletResponse.SC_FORBIDDEN;
         return true;
       }
 
       return false;
     } catch (Throwable t) {
       throw new WebdavException(t);
     }
   }
 
   /** Set the given property for this node.
    *
    * @param val   Element defining property to set
    * @param spr   Holds reult of removing property
    * @return boolean  true if property recognized.
    * @throws WebdavException
    */
   public boolean setProperty(Element val,
                              SetPropertyResult spr) throws WebdavException {
     try {
       if (WebdavTags.getetag.nodeMatches(val)) {
         spr.status = HttpServletResponse.SC_FORBIDDEN;
         return true;
       }
 
       if (WebdavTags.getlastmodified.nodeMatches(val)) {
         spr.status = HttpServletResponse.SC_FORBIDDEN;
         return true;
       }
 
       return false;
     } catch (Throwable t) {
       throw new WebdavException(t);
     }
   }
 
   /** Return true if a call to generatePropertyValue will return a value.
    *
    * @param tag
    * @return boolean
    */
   public boolean knownProperty(QName tag) {
     return propertyNames.get(tag) != null;
   }
 
   /** Emit the property indicated by the tag.
    *
    * @param tag  QName defining property
    * @param intf WebdavNsIntf
    * @param allProp    true if we're doing allprop
    * @return boolean   true if emitted
    * @throws WebdavException
    */
   public boolean generatePropertyValue(QName tag,
                                        WebdavNsIntf intf,
                                        boolean allProp) throws WebdavException {
     String ns = tag.getNamespaceURI();
     XmlEmit xml = intf.getXmlEmit();
 
     if (!ns.equals(WebdavTags.namespace)) {
       // Not ours
 
       return false;
     }
 
     try {
       if (tag.equals(WebdavTags.acl)) {
         // access 5.4
         intf.emitAcl(this);
         return true;
       }
 
       if (tag.equals(WebdavTags.creationdate)) {
         // dav 13.1
 
         String val = getCreDate();
         if (val == null) {
           return true;
         }
 
         xml.property(tag, val);
         return true;
       }
 
       if (tag.equals(WebdavTags.currentUserPrivilegeSet)) {
         // access 5.3
         CurrentAccess ca = getCurrentAccess();
         if (ca == null) {
           xml.emptyTag(tag);
           return true;
         }
 
         PrivilegeSet ps = ca.privileges;
         char[] privileges = ps.getPrivileges();
 
         AccessXmlUtil.emitCurrentPrivSet(xml,
                                          intf.getAccessUtil().getPrivTags(),
                                          privileges);
 
         return true;
       }
 
       if (tag.equals(WebdavTags.displayname)) {
         // dav 13.2
         xml.property(tag, getDisplayname());
 
         return true;
       }
 
       if (tag.equals(WebdavTags.getcontentlanguage)) {
         // dav 13.3
         if (!getAllowsGet()) {
           return true;
         }
         xml.property(tag, String.valueOf(getContentLang()));
         return true;
       }
 
       if (tag.equals(WebdavTags.getcontentlength)) {
         // dav 13.4
         if (!getAllowsGet()) {
           xml.property(tag, "0");
           return true;
         }
         xml.property(tag, String.valueOf(getContentLen()));
         return true;
       }
 
       if (tag.equals(WebdavTags.getcontenttype)) {
         // dav 13.5
         if (!getAllowsGet()) {
           return true;
         }
 
         String val = getContentType();
         if (val == null) {
           return true;
         }
 
         xml.property(tag, val);
         return true;
       }
 
       if (tag.equals(WebdavTags.getetag)) {
         // dav 13.6
         xml.property(tag, getEtagValue(true));
         return true;
       }
 
       if (tag.equals(WebdavTags.getlastmodified)) {
         // dav 13.7
         String val = getLastmodDate();
         if (val == null) {
           return true;
         }
 
         xml.property(tag, val);
         return true;
       }
 
       if (tag.equals(WebdavTags.owner)) {
         // access 5.1
         xml.openTag(tag);
         xml.property(WebdavTags.href, intf.makeUserHref(getOwner()));
         xml.closeTag(tag);
 
         return true;
       }
 
       if (tag.equals(WebdavTags.principalURL)) {
         xml.openTag(tag);
         generateUrl(xml, WebdavTags.href, getEncodedUri());
         xml.closeTag(tag);
 
         return true;
       }
 
       if (tag.equals(WebdavTags.resourcetype)) {
         // dav 13.9
         if (!isPrincipal() && !isCollection()) {
           xml.emptyTag(tag);
           return true;
         }
 
         xml.openTag(tag);
 
         if (isPrincipal()) {
           xml.emptyTag(WebdavTags.principal);
         }
 
         if (isCollection()) {
           xml.emptyTag(WebdavTags.collection);
         }
 
         xml.closeTag(tag);
         return true;
       }
 
       if (tag.equals(WebdavTags.supportedPrivilegeSet)) {
         // access 5.2
         intf.getAccessUtil().emitSupportedPrivSet();
         return true;
       }
 
       if (tag.equals(WebdavTags.supportedReportSet)) {
         // versioning
         intf.emitSupportedReportSet(this);
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
 
   /** This method is called before each setter/getter takes any action.
    * It allows the concrete implementation to defer some expensive
    * operation to just before the first call.
    *
    * @param content     boolean flag indicating if this is a content related
    *                    property - that is a property which requires fetching
    *                    and/or rendering the content
    * @throws WebdavException
    */
   public void init(boolean content) throws WebdavException {
   }
 
   /** Return true if this represents a principal
    *
    * @return boolean
    * @throws WebdavException
    */
   public boolean isPrincipal() throws WebdavException {
     return userPrincipal || groupPrincipal;
   }
 
   /** Return a set of PropertyTagEntry defining properties this node supports.
    *
    * @return Collection of PropertyTagEntry
    * @throws WebdavException
    */
   public Collection<PropertyTagEntry> getPropertyNames() throws WebdavException {
     if (!isPrincipal()) {
       return propertyNames.values();
     }
 
     Collection<PropertyTagEntry> res = new ArrayList<PropertyTagEntry>();
 
     res.addAll(propertyNames.values());
 
     return res;
   }
 
   /** Return a set of Qname defining reports this node supports.
    *
    * @return Collection of QName
    * @throws WebdavException
    */
   public Collection<QName> getSupportedReports() throws WebdavException {
     Collection<QName> res = new ArrayList<QName>();
     res.addAll(supportedReports);
 
     return res;
   }
 
   /**
    * @param val  boolean true if node exists
    * @throws WebdavException
    */
   public void setExists(boolean val) throws WebdavException {
     exists = val;
   }
 
   /**
    * @return boolean true if node exists
    * @throws WebdavException
    */
   public boolean getExists() throws WebdavException {
     return exists;
   }
 
   /** Set uri
    *
    * @param val
    * @throws WebdavException
    */
   public void setUri(String val) throws WebdavException {
     init(false);
     uri = val;
   }
 
   /** Get uri
    *
    * @return String uri
    * @throws WebdavException
    */
   public String getUri() throws WebdavException {
     init(false);
     return uri;
   }
 
   /**
    * @return String encoded uri
    * @throws WebdavException
    */
   public String getEncodedUri() throws WebdavException {
     try {
       return new URI(null, null, getUri(), null).toString();
     } catch (Throwable t) {
       if (debug) {
         error(t);
       }
       throw new WebdavBadRequest();
     }
   }
 
   /**
    * @return boolean true for a collection
    * @throws WebdavException
    */
   public boolean isCollection() throws WebdavException {
     return collection;
   }
 
   /**
    * @param val boolean true if node allows get
    * @throws WebdavException
    */
   public void setAllowsGet(boolean val) throws WebdavException {
     allowsGet = val;
   }
 
   /**
    * @return true if node allows get
    * @throws WebdavException
    */
   public boolean getAllowsGet() throws WebdavException {
     return allowsGet;
   }
 
   /**
    * @param val in status
    */
   public void setStatus(int val) {
     status = val;
   }
 
   /**
    * @return int sttaus
    */
   public int getStatus() {
     return status;
   }
 
   /**
    * @param val
    * @throws WebdavException
    */
   public void setAlias(boolean val) throws WebdavException {
     init(false);
     alias = val;
   }
 
   /**
    * @return boolean true if an alias
    * @throws WebdavException
    */
   public boolean getAlias() throws WebdavException {
     init(false);
     return alias;
   }
 
   /**
    * @param val
    * @throws WebdavException
    */
   public void setTargetUri(String val) throws WebdavException {
     init(false);
     targetUri = val;
   }
 
   /**
    * @return String uri
    * @throws WebdavException
    */
   public String getTargetUri() throws WebdavException {
     init(false);
     return targetUri;
   }
 
   /** Return a collection of property objects
    *
    * <p>Default is to return an empty Collection
    *
    * @param ns      String interface namespace.
    * @return Collection (possibly empty) of WebdavProperty objects
    * @throws WebdavException
    */
   public Collection<WebdavProperty> getProperties(String ns) throws WebdavException {
     return new ArrayList<WebdavProperty>();
   }
 
   /** Returns an InputStream for the content.
    *
    * @return Reader       A reader for the content.
    * @throws WebdavException
    */
   public Reader getContent() throws WebdavException {
     String cont = getContentString();
 
     if (cont == null) {
       return null;
     }
 
     return new StringReader(cont);
   }
 
   /** Return string content
    *
    * @return String       content.
    * @throws WebdavException
    */
   public String getContentString() throws WebdavException {
     return null;
   }
 
   /* ====================================================================
    *                   Required webdav properties
    * ==================================================================== */
 
   /**
    * @return String lang
    * @throws WebdavException
    */
   public abstract String getContentLang() throws WebdavException;
 
   /**
    * @return int content length
    * @throws WebdavException
    */
   public abstract int getContentLen() throws WebdavException;
 
   /** A content type of null implies no content (or we don't know)
    *
    * @return String content type
    * @throws WebdavException
    */
   public abstract String getContentType() throws WebdavException;
 
   /**
    * @return String credate
    * @throws WebdavException
    */
   public abstract String getCreDate() throws WebdavException;
 
   /**
    * @return String name
    * @throws WebdavException
    */
   public abstract String getDisplayname() throws WebdavException;
 
   /** Entity tags are defined in RFC2068 - they are supposed to provide some
    * sort of indication the data has changed - e.g. a checksum.
    * <p>There are weak and strong tags
    *
    * <p>This methods should return a suitable value for that tag.
    *
    * @param strong
    * @return String
    * @throws WebdavException
    */
   public abstract String getEtagValue(boolean strong) throws WebdavException;
 
   /**
    * @return String last mod date
    * @throws WebdavException
    */
   public abstract String getLastmodDate() throws WebdavException;
 
   /** Should return a value suitable for WebdavNsIntf.makeUserHref
    *
    * @return String owner
    * @throws WebdavException
    */
   public abstract String getOwner() throws WebdavException;
 
   /* ********************************************************************
    *                        Protected methods
    * ******************************************************************** */
 
   protected Logger getLogger() {
     if (log == null) {
       log = Logger.getLogger(this.getClass());
     }
 
     return log;
   }
 
   protected void error(Throwable t) {
     getLogger().error(this, t);
   }
 
   protected void warn(String msg) {
     getLogger().warn(msg);
   }
 
   protected void debugMsg(String msg) {
     getLogger().debug(msg);
   }
 
   protected void logIt(String msg) {
     getLogger().info(msg);
   }
 
   protected static void addPropEntry(HashMap<QName, PropertyTagEntry> propertyNames,
                                      QName tag) {
     propertyNames.put(tag, new PropertyTagEntry(tag));
   }
 
   protected static void addPropEntry(HashMap<QName, PropertyTagEntry> propertyNames,
                                      QName tag, boolean inAllProp) {
     propertyNames.put(tag, new PropertyTagEntry(tag, inAllProp));
   }
 
   /* ********************************************************************
    *                        Object methods
    * ******************************************************************** */
 
   public int hashCode() {
     return uri.hashCode();
   }
 
   public boolean equals(Object o) {
     if (o == this) {
       return true;
     }
 
     if (!(o instanceof WebdavNsNode)) {
       return false;
     }
 
     WebdavNsNode that = (WebdavNsNode)o;
 
     return uri.equals(that.uri);
   }
 }
