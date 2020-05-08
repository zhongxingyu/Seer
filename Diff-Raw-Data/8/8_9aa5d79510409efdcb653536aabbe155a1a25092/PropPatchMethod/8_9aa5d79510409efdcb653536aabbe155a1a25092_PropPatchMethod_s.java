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
 
 import edu.rpi.cct.webdav.servlet.shared.WebdavBadRequest;
 import edu.rpi.cct.webdav.servlet.shared.WebdavException;
 import edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf;
 import edu.rpi.cct.webdav.servlet.shared.WebdavNsNode;
 import edu.rpi.cct.webdav.servlet.shared.WebdavStatusCode;
 import edu.rpi.cct.webdav.servlet.shared.WebdavNsNode.SetPropertyResult;
 import edu.rpi.sss.util.xml.QName;
 import edu.rpi.sss.util.xml.tagdefs.CaldavTags;
 import edu.rpi.sss.util.xml.tagdefs.WebdavTags;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /** Class called to handle PROPPATCH
  *
  *   @author Mike Douglass   douglm@rpi.edu
  */
 public class PropPatchMethod extends MethodBase {
   /* (non-Javadoc)
    * @see edu.rpi.cct.webdav.servlet.common.MethodBase#init()
    */
   public void init() {
   }
 
   public void doMethod(HttpServletRequest req,
                         HttpServletResponse resp) throws WebdavException {
     if (debug) {
       trace("PropPatchMethod: doMethod");
     }
 
     /* Parse any content */
     Document doc = parseContent(req, resp);
 
     /* Create the node */
     String resourceUri = getResourceUri(req);
 
     WebdavNsNode node = getNsIntf().getNode(resourceUri,
                                             WebdavNsIntf.existanceMust,
                                             WebdavNsIntf.nodeTypeUnknown);
 
     if (doc != null) {
      processDoc(req, resp, doc, node, WebdavTags.propertyUpdate, false);
     }
   }
 
   /** List of properties to set
    *
    */
   public static class PropertySetList extends ArrayList<Element> {
   }
 
   /** List of properties to remove
    *
    */
   public static class PropertyRemoveList extends ArrayList<Element> {
   }
 
   /* ====================================================================
    *                   Protected methods
    * ==================================================================== */
 
   protected boolean processDoc(HttpServletRequest req,
                                HttpServletResponse resp,
                                Document doc,
                                WebdavNsNode node,
                                QName expectedRoot,
                                boolean onlySet) throws WebdavException {
     try {
       Element root = doc.getDocumentElement();
 
       if (!expectedRoot.nodeMatches(root)) {
         throw new WebdavBadRequest();
       }
 
       Collection<? extends Collection<Element>> setRemoveList = processUpdate(root);
       Collection<SetPropertyResult> failures = new ArrayList<SetPropertyResult>();
       Collection<SetPropertyResult> successes = new ArrayList<SetPropertyResult>();
 
       for (Collection<Element> sr: setRemoveList) {
         boolean setting = sr instanceof PropPatchMethod.PropertySetList;
 
         // XXX - possibly inadequate
         /* It's possible changes would conflict, so a later change may
          * invalidate an earlier change.
          */
 
         for (Element prop: sr) {
           SetPropertyResult spr = new SetPropertyResult(prop);
 
           boolean recognized;
 
           if (setting) {
             recognized = node.setProperty(prop, spr);
           } else {
             if (onlySet) {
               throw new WebdavBadRequest();
             }
             recognized = node.removeProperty(prop, spr);
           }
 
           if (!recognized) {
             spr.status = HttpServletResponse.SC_NOT_FOUND;
           }
 
           if (spr.status != HttpServletResponse.SC_OK) {
             failures.add(spr);
           } else {
             successes.add(spr);
           }
         }
       }
 
       if (failures.isEmpty()) {
        node.update();

         /* No response for success */
         return true;
       }
 
       /* Fail whole request for any failure */
       startEmit(resp);
 
       resp.setStatus(WebdavStatusCode.SC_MULTI_STATUS);
       resp.setContentType("text/xml; charset=UTF-8");
 
       openTag(WebdavTags.multistatus);
 
       openTag(WebdavTags.response);
       node.generateHref(xml);
       for (SetPropertyResult spr: failures) {
         openTag(WebdavTags.propstat);
         openTag(WebdavTags.prop);
         emptyTag(spr.prop);
         closeTag(WebdavTags.prop);
         addStatus(spr.status, spr.message);
         closeTag(WebdavTags.propstat);
       }
       for (SetPropertyResult spr: successes) {
         openTag(WebdavTags.propstat);
         openTag(WebdavTags.prop);
         emptyTag(spr.prop);
         closeTag(WebdavTags.prop);
         addStatus(WebdavStatusCode.SC_FAILED_DEPENDENCY, "Failed Dependency");
         closeTag(WebdavTags.propstat);
       }
       closeTag(WebdavTags.response);
       closeTag(WebdavTags.multistatus);
 
       flush();
 
       return false;
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
 
   /** The given node should contain zero or more set or remove child elements.
    *
    * <p>Each set element should contain zero or more property tags with values.
    *
    * <p>Each remove element should contain zero or more empty property tags.
    *
    * <p>The returned Collection contains zero or more PropertySetList or
    * PropertyRemoveList entries.
    *
    * @param node
    * @return Collection
    * @throws WebdavException
    */
   protected Collection<? extends Collection<Element>> processUpdate(Element node) throws WebdavException {
     ArrayList<Collection<Element>> res = new ArrayList<Collection<Element>>();
 
     try {
       Element[] children = getChildrenArray(node);
 
       for (int i = 0; i < children.length; i++) {
         Element srnode = children[i]; // set or remove
         PropertySetList plist;
 
         Element propnode = getOnlyChild(srnode);
 
         if (!WebdavTags.prop.nodeMatches(propnode)) {
           throw new WebdavBadRequest();
         }
 
         if (WebdavTags.set.nodeMatches(srnode)) {
           plist = new PropertySetList();
 
           processPlist(plist, propnode, false);
         } else if (WebdavTags.remove.nodeMatches(srnode)) {
           plist = new PropertySetList();
 
           processPlist(plist, propnode, true);
         } else {
           throw new WebdavBadRequest();
         }
 
         res.add(plist);
       }
     } catch (WebdavException wde) {
       throw wde;
     } catch (Throwable t) {
       System.err.println(t.getMessage());
       if (debug) {
         t.printStackTrace();
       }
 
       throw new WebdavException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
 
     return res;
   }
 
   /* ====================================================================
    *                   Private methods
    * ==================================================================== */
 
   /* Process the node which should contain either empty elements for remove or
    * elements with or without values for remove==false
    */
   private void processPlist(Collection<Element> plist, Element node,
                             boolean remove) throws WebdavException {
     Element[] props = getChildrenArray(node);
 
     for (int i = 0; i < props.length; i++) {
       Element prop = props[i];
 
       if (CaldavTags.supportedCalendarComponentSet.nodeMatches(prop)) {
         // XXX Need to do something
       } else if (remove && !isEmpty(prop)) {
         throw new WebdavBadRequest();
       }
 
       plist.add(prop);
 
       /* if (debug) {
         trace("reqtype: " + prop.getLocalName() +
               " ns: " + prop.getNamespaceURI() +
               " value: " + value);
       } */
     }
   }
 }
 
