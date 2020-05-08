 //: "The contents of this file are subject to the Mozilla Public License
 //: Version 1.1 (the "License"); you may not use this file except in
 //: compliance with the License. You may obtain a copy of the License at
 //: http://www.mozilla.org/MPL/
 //:
 //: Software distributed under the License is distributed on an "AS IS"
 //: basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 //: License for the specific language governing rights and limitations
 //: under the License.
 //:
 //: The Original Code is Guanxi (http://www.guanxi.uhi.ac.uk).
 //:
 //: The Initial Developer of the Original Code is Alistair Young alistair@smo.uhi.ac.uk.
 //: Portions created by SMO WWW Development Group are Copyright (C) 2005 SMO WWW Development Group.
 //: All Rights Reserved.
 //:
 /* CVS Header
    $Id$
    $Log$
   Revision 1.5  2005/05/23 08:41:04  alistairskye
   Updated buildIDPList() to fix idp-list display bug

    Revision 1.4  2005/05/04 13:16:36  alistairskye
    Changed to use org.Guanxi.SAMUEL.Exception package
 
    Revision 1.3  2005/05/02 10:58:13  alistairskye
    Changed to use latest version of org.Guanxi.SAMUEL.Utils.XUtils.getIterator()
 
    Revision 1.2  2005/04/15 10:05:56  alistairskye
    License updated
 
 */
 
 package org.Guanxi.WAYF;
 
 import org.Guanxi.Common.Utils;
 import org.Guanxi.SAMUEL.Utils.ParserPool;
 import org.Guanxi.SAMUEL.Exception.ParserPoolException;
 import org.Guanxi.SAMUEL.Exception.ParseException;
 import org.Guanxi.SAMUEL.Utils.XUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.traversal.NodeIterator;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.ServletException;
 import java.io.IOException;
 import java.io.File;
 import java.util.Hashtable;
 import java.util.Enumeration;
 
 /**
  * <font size=5><b></b></font>
  *
  * @author Alistair Young alistair@smo.uhi.ac.uk
  */
 public class WAYF extends HttpServlet {
   public void init() throws ServletException {
   }
 
   public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     doGet(request, response);
   }
 
   public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     getServletContext().setAttribute("idpList", buildIDPList());
 
     // Are we getting a form submission from the WAYF list?
     if (request.getParameter("mode") != null) {
       if (request.getParameter("mode").equalsIgnoreCase("dispatch")) {
         // Base IdP URL...
         String idpURL = request.getParameter("idp");
         // ...and add on the Shibboleth specific parameters
         String name, value;
         boolean firstShibbParam = true;
         Hashtable requestParams = Utils.getRequestParameters(request);
         Enumeration e = requestParams.keys();
         while (e.hasMoreElements()) {
           // Get a parameter from the request
           name = (String)e.nextElement();
           value = (String)requestParams.get(name);
 
           // If it's a Shibboleth parameter, remove the marker the JSP added...
           if (name.indexOf("shibb_") != -1) {
             name = name.replaceAll("shibb_", "");
 
             // ...and add it to the IdP's URL
             if (firstShibbParam) {
               idpURL += "?";
               firstShibbParam = false;
             }
             else
               idpURL += "&";
             idpURL += name + "=" + value;
           }
         }
         response.sendRedirect(idpURL);
       }
     }
     else {
       // Initial forward to the WAYF page from a Shibboleth SP GET request
       request.getRequestDispatcher("/WEB-INF/jsp/wayf.jsp").forward(request, response);
     }
   }
 
   private Hashtable buildIDPList() {
     Hashtable sites = new Hashtable();
 
     // Get an XML parser from the pool
     ParserPool parser = null;
     try {
       parser = ParserPool.getInstance();
     }
     catch(ParserPoolException ppe) {
       sites.put("error", ppe.getMessage());
       return sites;
     }
 
     // Build the path to the sites XML file...
     String sitesFile = getServletContext().getRealPath(getServletConfig().getInitParameter("sitesFile"));
 
     // ...and parse it
     Document sitesDoc = null;
     try {
       sitesDoc = parser.parse(new File(sitesFile));
     }
     catch(ParseException pe) {
       sites.put("error", pe.getMessage());
       return sites;
     }
 
     // Build up the list of IdPs we recognise
     XUtils xUtils = XUtils.getInstance();
     Element idpElement = null;
    NodeIterator ni = xUtils.getIterator(sitesDoc.getDocumentElement(), "", sitesDoc.getDocumentElement(), "idp-list");
     while ((idpElement = (Element)ni.nextNode()) != null) {
       NamedNodeMap idpAttrs = idpElement.getAttributes();
       sites.put(idpAttrs.getNamedItem("name").getNodeValue(), idpAttrs.getNamedItem("url").getNodeValue());
     }
 
     return sites;
   }
 }
