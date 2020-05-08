 /*
  * @author <a href="wehrens@aei.mpg.de">Oliver Wehrens</a>
  * @versiob $Id$
  */
 
 package org.gridlab.gridsphere.portlets.core;
 
 import org.gridlab.gridsphere.event.ActionEvent;
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.impl.GuestUser;
 import org.gridlab.gridsphere.portlet.impl.SportletUser;
 import org.gridlab.gridsphere.portlet.impl.SportletUserImpl;
 import org.gridlab.gridsphere.portlet.impl.PortletProperties;
 import org.gridlab.gridsphere.portlet.service.PortletServiceNotFoundException;
 import org.gridlab.gridsphere.portlet.service.PortletServiceUnavailableException;
 import org.gridlab.gridsphere.portletcontainer.GridSphereProperties;
 import org.gridlab.gridsphere.services.user.UserManagerService;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 
 import javax.servlet.UnavailableException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.URL;
 import java.net.MalformedURLException;
 import java.util.Vector;
 import java.util.List;
 import java.util.Iterator;
 
 public class RSSPortlet extends AbstractPortlet {
 
     String _url = "http://diveintomark.org/xml/rss.xml";
     long _lastFetched = 0;
     int _fetch_interval = 5;        // in minutes
     Document RSSFeed = new Document(new Element("rss"));
 
 
     public void init(PortletConfig config) throws UnavailableException {
         super.init(config);
         System.err.println("init() in RSSPortlet");
     }
 
     private Document getRSSFeed(String url) {
         Document doc = new Document(new Element("rss"));
         try {
             SAXBuilder builder = new SAXBuilder(false);
             URL feedurl = new URL(url);
             doc = builder.build(feedurl);
             _lastFetched = System.currentTimeMillis();
             log.info("Fetched rss feed from "+url);
         } catch (MalformedURLException e) {
         } catch (JDOMException e) {
         }
 
         return doc;
 
     }
 
     public void actionPerformed(ActionEvent evt) {
         PortletAction _action = evt.getAction();
 
         if (_action instanceof DefaultPortletAction) {
             DefaultPortletAction action = (DefaultPortletAction) _action;
             PortletRequest req = evt.getPortletRequest();
            _url = (String) req.getParameter("rss_url");
             _lastFetched = 0;
 
         }
     }
 
     public void doView(PortletRequest request, PortletResponse response) throws PortletException, IOException {
         PrintWriter out = response.getWriter();
 
         if (System.currentTimeMillis()-_lastFetched > (_fetch_interval*60*100)) {
             RSSFeed = getRSSFeed(_url);
         }
         Element root = RSSFeed.getRootElement();
 
         out.println("RSSFeed Name :"+root.getChild("channel").getChild("title").getText());
         List items = root.getChild("channel").getChildren("item");
         Iterator it = items.iterator();
         out.println("<ul>");
         while (it.hasNext()) {
             Element item = (Element)it.next();
             String title = item.getChild("title").getText();
             String link = item.getChild("link").getText();
             String desc = item.getChild("description").getText();
             out.println("<li><a target=\"_new\" href=\""+link+"\">"+title+"</a><br/>"+desc+"</li>");
         }
         out.println("</ul>");
 
     }
 
     public void doEdit(PortletRequest request, PortletResponse response) throws PortletException, IOException {
         PrintWriter out = response.getWriter();
         PortletURI pictureURI = response.createURI();
         DefaultPortletAction defAction = new DefaultPortletAction(PortletProperties.ACTION_PERFORMED);
         pictureURI.addAction(defAction);
        request.setAttribute("rss_url", pictureURI.toString());
        getPortletConfig().getContext().include("/jsp/rss.jsp", request, response);
     }
 
     public void doHelp(PortletRequest request, PortletResponse response) throws PortletException, IOException {
         doView(request,response);
     }
 
 
 }
