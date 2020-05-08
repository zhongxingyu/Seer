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
 
 package edu.rpi.cct.webdav.servlet.common;
 
 import org.bedework.davdefs.WebdavTags;
 
 import edu.rpi.cct.webdav.servlet.shared.WebdavBadRequest;
 import edu.rpi.cct.webdav.servlet.shared.WebdavException;
 import edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf;
 import edu.rpi.cct.webdav.servlet.shared.WebdavNsNode;
 import edu.rpi.cct.webdav.servlet.shared.WebdavProperty;
 import edu.rpi.cct.webdav.servlet.shared.WebdavStatusCode;
 import edu.rpi.cct.webdav.servlet.shared.WebdavNsNode.PropertyTagEntry;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import java.util.Collection;
 import java.util.Iterator;
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
     // ENUM
     //private static final int reqPropNone = 0;
     private static final int reqProp = 1;
     private static final int reqPropName = 2;
     private static final int reqPropAll = 3;
 
     int reqType;
 
     PropRequest(int reqType)  {
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
       parsedReq = new PropRequest(PropRequest.reqPropAll);
     }
 
     if (doc != null) {
       processDoc(doc);
     }
 
     int depth = Headers.depth(req);
     if (depth == Headers.depthNone) {
       depth = Headers.depthInfinity;
     }
 
     if (debug) {
       trace("PropFindMethod: depth=" + depth + " type=" + parsedReq.reqType);
     }
 
     startEmit(resp);
 
     processResp(req, resp, depth);
   }
 
   /* ====================================================================
    *                   Private methods
    * ==================================================================== */
 
   private void processDoc(Document doc) throws WebdavException {
     try {
       Element root = doc.getDocumentElement();
 
       if (!WebdavTags.propfind.nodeMatches(root)) {
         throw new WebdavBadRequest();
       }
 
       Element[] children = getChildren(root);
 
       for (int i = 0; i < children.length; i++) {
         Element curnode = children[i];
 
         String nm = curnode.getLocalName();
         String ns = curnode.getNamespaceURI();
 
         if (debug) {
           trace("reqtype: " + nm + " ns: " + ns);
         }
 
         parsedReq = tryPropRequest(curnode);
         if (parsedReq != null) {
           break;
         }
       }
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
     if (WebdavTags.allprop.nodeMatches(nd)) {
       return new PropRequest(PropRequest.reqPropAll);
     }
 
     if (WebdavTags.prop.nodeMatches(nd)) {
       return parseProps(nd);
     }
 
     if (WebdavTags.propname.nodeMatches(nd)) {
       return new PropRequest(PropRequest.reqPropName);
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
     PropRequest pr = new PropRequest(PropRequest.reqProp);
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
 
     String resourceUri = getResourceUri(req);
     if (debug) {
       trace("About to get node at " + resourceUri);
     }
 
     WebdavNsNode node = getNsIntf().getNode(resourceUri,
                                             WebdavNsIntf.existanceMust,
                                             WebdavNsIntf.nodeTypeUnknown);
 
     openTag(WebdavTags.multistatus);
 
     if (node != null) {
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
     addHref(node);
 
     openTag(WebdavTags.propstat);
 
     if ((pr != null) && (node.getExists())) {
       if (debug) {
         trace("doNodePropertiest type=" + pr.reqType);
       }
 
       if (pr.reqType == PropRequest.reqProp) {
         doPropFind(node, pr);
       } else if (pr.reqType == PropRequest.reqPropName) {
         doPropNames(node);
       } else if (pr.reqType == PropRequest.reqPropAll) {
         doPropAll(node);
       }
     }
 
     addStatus(node.getStatus());
 
     closeTag(WebdavTags.propstat);
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
 
     Iterator children = getNsIntf().getChildren(node);
 
     while (children.hasNext()) {
       WebdavNsNode child = (WebdavNsNode)children.next();
 
       doNodeAndChildren(child, curDepth, maxDepth);
     }
   }
 
   /** Build the response for a single node for a propfind request
    *
    * @param node
    * @param preq
    * @throws WebdavException
    */
   private void doPropFind(WebdavNsNode node, PropRequest preq) throws WebdavException {
     WebdavNsIntf intf = getNsIntf();
 
     openTag(WebdavTags.prop);
 
     for (WebdavProperty pr: preq.props) {
       addNs(pr.getTag().getNamespaceURI());
       intf.generatePropValue(node, pr, false);
     }
 
     closeTag(WebdavTags.prop);
   }
 
   /* Build the response for a single node for a propnames request
    */
   private void doPropNames(WebdavNsNode node) throws WebdavException {
     Iterator it = node.getPropertyNames().iterator();
 
     openTag(WebdavTags.prop);
 
     while (it.hasNext()) {
       PropertyTagEntry pte = (PropertyTagEntry)it.next();
 
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
 
     return HttpServletResponse.SC_OK;
   }
 
   /* Build the lockdiscovery response for a single node
    */
   private void doLockDiscovery(WebdavNsNode node) throws WebdavException {
   }
 }
 
