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
 
 package edu.rpi.cct.webdav.servlet.common;
 
 import edu.rpi.cct.webdav.servlet.shared.WebdavBadRequest;
 import edu.rpi.cct.webdav.servlet.shared.WebdavException;
 import edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf;
 import edu.rpi.cct.webdav.servlet.shared.WebdavNsNode;
 import edu.rpi.cct.webdav.servlet.shared.WebdavProperty;
 import edu.rpi.cct.webdav.servlet.shared.WebdavStatusCode;
 import edu.rpi.cct.webdav.servlet.shared.WebdavNsNode.PropertyTagEntry;
 import edu.rpi.sss.util.xml.XmlUtil;
 import edu.rpi.sss.util.xml.tagdefs.WebdavTags;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import java.util.Collection;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /** Class called to handle PROPFIND
  *
  *   @author Mike Douglass   douglm@rpi.edu
  */
 public class PropFindMethod extends MethodBase {
   /**
    */
   public static class PropRequest {
     /** */
     public static enum ReqType {
       /** */
       prop,
       /** */
       propName,
       /** */
       propAll
     }
 
     /** */
     public ReqType reqType;
 
     PropRequest(ReqType reqType)  {
       this.reqType = reqType;
     }
 
     /** For the prop element we build a Collection of WebdavProperty
      */
     public Collection<WebdavProperty> props;
   }
 
   private PropRequest parsedReq;
 
   /* (non-Javadoc)
    * @see edu.rpi.cct.webdav.servlet.common.MethodBase#init()
    */
   public void init() {
   }
 
   public void doMethod(HttpServletRequest req,
                        HttpServletResponse resp) throws WebdavException {
     if (debug) {
       trace("PropFindMethod: doMethod");
     }
 
     Document doc = parseContent(req, resp);
 
     if (doc == null) {
       // Treat as allprop request
       parsedReq = new PropRequest(PropRequest.ReqType.propAll);
     }
 
     if (doc != null) {
       processDoc(doc);
     }
 
     int depth = Headers.depth(req);
     if (depth == Headers.depthNone) {
       depth = Headers.depthInfinity;
     }
 
     if (debug) {
       trace("PropFindMethod: depth=" + depth);
       if (parsedReq != null) {
         trace("                type=" + parsedReq.reqType);
       }
     }
 
     processResp(req, resp, depth);
   }
 
   /* ====================================================================
    *                   Private methods
    * ==================================================================== */
 
   private void processDoc(Document doc) throws WebdavException {
     try {
       Element root = doc.getDocumentElement();
 
       if (!XmlUtil.nodeMatches(root, WebdavTags.propfind)) {
         throw new WebdavBadRequest();
       }
 
       Element curnode = getOnlyChild(root);
 
       String ns = curnode.getNamespaceURI();
 
       addNs(ns);
 
       if (debug) {
         String nm = curnode.getLocalName();
 
         trace("reqtype: " + nm + " ns: " + ns);
       }
 
       parsedReq = tryPropRequest(curnode);
     } catch (WebdavException wde) {
       throw wde;
     } catch (Throwable t) {
       System.err.println(t.getMessage());
       if (debug) {
         t.printStackTrace();
       }
 
       throw new WebdavException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 
   /** See if the current node represents a valid propfind element
    * and return with a request if so. Otherwise return null.
    *
    * @param nd
    * @return PropRequest
    * @throws WebdavException
    */
   public PropRequest tryPropRequest(Node nd) throws WebdavException {
     if (XmlUtil.nodeMatches(nd, WebdavTags.allprop)) {
       return new PropRequest(PropRequest.ReqType.propAll);
     }
 
     if (XmlUtil.nodeMatches(nd, WebdavTags.prop)) {
       return parseProps(nd);
     }
 
     if (XmlUtil.nodeMatches(nd, WebdavTags.propname)) {
       return new PropRequest(PropRequest.ReqType.propName);
     }
 
     return null;
   }
 
   /** Just a list of property names in any namespace.
    *
    * @param nd
    * @return PropRequest
    * @throws WebdavException
    */
   public PropRequest parseProps(Node nd) throws WebdavException {
     PropRequest pr = new PropRequest(PropRequest.ReqType.prop);
     pr.props = getNsIntf().parseProp(nd);
 
     return pr;
   }
 
   /**
    * @param req
    * @param resp
    * @param depth
    * @throws WebdavException
    */
   public void processResp(HttpServletRequest req,
                           HttpServletResponse resp,
                           int depth) throws WebdavException {
     resp.setStatus(WebdavStatusCode.SC_MULTI_STATUS);
     resp.setContentType("text/xml; charset=UTF-8");
 
     startEmit(resp);
 
     String resourceUri = getResourceUri(req);
     if (debug) {
       trace("About to get node at " + resourceUri);
     }
 
     WebdavNsNode node = getNsIntf().getNode(resourceUri,
                                             WebdavNsIntf.existanceMust,
                                             WebdavNsIntf.nodeTypeUnknown);
 
     openTag(WebdavTags.multistatus);
 
     if (node == null) {
       openTag(WebdavTags.response);
 
      // XXX missing href
      openTag(WebdavTags.propstat);
       addStatus(HttpServletResponse.SC_NOT_FOUND, null);
      closeTag(WebdavTags.propstat);
 
       closeTag(WebdavTags.response);
     } else {
       doNodeAndChildren(node, 0, depth);
     }
 
     closeTag(WebdavTags.multistatus);
 
     flush();
   }
 
   /** Generate response for a PROPFIND for the current node, then for the children.
    *
    * @param node
    * @param pr
    * @throws WebdavException
    */
   public void doNodeProperties(WebdavNsNode node,
                                 PropRequest pr) throws WebdavException {
     node.generateHref(xml);
 
     if ((pr == null) || (!node.getExists())) {
       openTag(WebdavTags.propstat);
       addStatus(node.getStatus(), null);
       closeTag(WebdavTags.propstat);
       return;
     }
 
     if ((pr.reqType == PropRequest.ReqType.propName) ||
         (pr.reqType == PropRequest.ReqType.propAll)) {
       openTag(WebdavTags.propstat);
 
       if (debug) {
         trace("doNodeProperties type=" + pr.reqType);
       }
 
       if (pr.reqType == PropRequest.ReqType.propName) {
         doPropNames(node);
       } else if (pr.reqType == PropRequest.ReqType.propAll) {
         doPropAll(node);
       }
       addStatus(node.getStatus(), null);
 
       closeTag(WebdavTags.propstat);
 
       return;
     }
 
     if (pr.reqType != PropRequest.ReqType.prop) {
       throw new WebdavBadRequest();
     }
 
     // Named properties
 
     doPropFind(node, pr.props);
   }
 
   private void doNodeAndChildren(WebdavNsNode node,
                                  int curDepth,
                                  int maxDepth) throws WebdavException {
     openTag(WebdavTags.response);
 
     doNodeProperties(node, parsedReq);
 
     closeTag(WebdavTags.response);
 
     flush();
 
     curDepth++;
 
     if (curDepth > maxDepth) {
       return;
     }
 
     for (WebdavNsNode child: getNsIntf().getChildren(node)) {
       doNodeAndChildren(child, curDepth, maxDepth);
     }
   }
 
   /* Build the response for a single node for a propnames request
    */
   private void doPropNames(WebdavNsNode node) throws WebdavException {
     openTag(WebdavTags.prop);
 
     for (PropertyTagEntry pte: node.getPropertyNames()) {
       if (pte.inPropAll) {
         emptyTag(pte.tag);
       }
     }
 
     closeTag(WebdavTags.prop);
   }
 
   /* Build the response for a single node for an allprop request
    */
   private int doPropAll(WebdavNsNode node) throws WebdavException {
     WebdavNsIntf intf = getNsIntf();
 
     openTag(WebdavTags.prop);
 
     doLockDiscovery(node);
 
     String sl = getNsIntf().getSupportedLocks();
 
     if (sl != null) {
       property(WebdavTags.supportedlock, sl);
     }
 
     for (PropertyTagEntry pte: node.getPropertyNames()) {
       if (pte.inPropAll) {
         intf.generatePropValue(node, new WebdavProperty(pte.tag, null), true);
       }
     }
 
     closeTag(WebdavTags.prop);
 
     return HttpServletResponse.SC_OK;
   }
 
   /* Build the lockdiscovery response for a single node
    */
   private void doLockDiscovery(WebdavNsNode node) throws WebdavException {
   }
 }
 
