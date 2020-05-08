 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.wazari.view.servlet;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.logging.Logger;
import javax.annotation.security.RunAs;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import net.wazari.service.AlbumLocal;
 import net.wazari.service.ChoixLocal;
 import net.wazari.service.ConfigLocal;
 import net.wazari.service.ImageLocal;
 import net.wazari.service.PhotoLocal;
 import net.wazari.service.TagLocal;
 import net.wazari.service.ThemeLocal;
 import net.wazari.service.UserLocal;
 import net.wazari.service.WebPageLocal;
 import net.wazari.service.exception.WebAlbumsServiceException;
 import net.wazari.service.exchange.ViewSession;
 import net.wazari.service.exchange.ViewSession.Action;
 import net.wazari.service.exchange.ViewSession.ViewSessionLogin;
 import net.wazari.service.exchange.ViewSessionAlbum;
 import net.wazari.service.exchange.ViewSessionConfig;
 import net.wazari.service.exchange.ViewSessionImages;
 import net.wazari.service.exchange.ViewSessionPhoto;
 import net.wazari.service.exchange.ViewSessionTag;
 import net.wazari.util.XmlBuilder;
 import net.wazari.view.servlet.exchange.ViewSessionImpl;
 
 /**
  *
  * @author kevin
  */
 @Stateless
 public class DispatcherBean {
 
     private static final Logger log = Logger.getLogger(DispatcherBean.class.getCanonicalName());
     @EJB
     private ThemeLocal themeService;
     @EJB
     private UserLocal userService;
     @EJB
     private ChoixLocal choixService;
     @EJB
     private AlbumLocal albumService;
     @EJB
     private PhotoLocal photoService;
     @EJB
     private TagLocal tagService;
     @EJB
     private ImageLocal imageService;
     @EJB
     private ConfigLocal configService;
     @EJB
     private WebPageLocal webPageService;
 
     public enum Page {
 
         PHOTO, IMAGE, USER, ALBUM, CONFIG, CHOIX, TAGS, VOID, PERIODE, MAINT
     }
 
     public void treat(ServletContext context,
             Page page,
             HttpServletRequest request,
             HttpServletResponse response)
             throws IOException, ServletException 
     {
        request.logout();
        request.login("Admin", "admin") ;
         ViewSession vSession = new ViewSessionImpl(request, response, context);
         if (request.getParameter("logout") != null) {
             log.info("Logout and cleanup the session");
             request.logout();
             userService.cleanUpSession((ViewSessionLogin) vSession);
         }
         if (page != Page.USER) {
             log.info("Authenticated the session");
             request.authenticate(response) ;
         }
         log.info("============= " + page + " =============");
                 
         long debut = System.currentTimeMillis();
         request.setCharacterEncoding("UTF-8");
 
         XmlBuilder output = new XmlBuilder("root");
 
         String xslFile = null;
 
         boolean isWritten = false;
         boolean isComplete = false;
         try {
             xslFile = "static/Display.xsl";
             if (page == Page.USER) {
                 output.add(treatLogin(vSession, request)) ;
             } else if (page == Page.VOID) {
                 output.add(themeService.treatVOID(vSession));
             } else if (page == Page.MAINT) {
                 xslFile = "static/Empty.xsl";
                 //output.add(net.wazari.service.engine.Maint.treatMAINT(request));
             } else {
                 log.info("============= Login: " + request.getUserPrincipal() + " =============");
                 String special = request.getParameter("special");
                 if (special != null) {
                     log.info("Special XSL-style ("+special+")");
                     if ("RSS".equals(special)) {
                         xslFile = "static/Rss.xsl";
                     } else {
                         xslFile = "static/Empty.xsl";
                     }
                 }
                 log.fine("XSL-style"+xslFile);
                 //try to logon and set the theme
                 if (vSession.getTheme() == null) {
                     log.fine("Try to logon");
                     boolean ret = userService.logon((ViewSessionLogin) vSession, request);
                     log.finer("Logon result: "+ret);
                 }
                 //from here on, the theme must be saved
                 if (vSession.getTheme() == null){
                     if (special == null) {
                         log.finer("Not logged in, not a special page, display VOID page");
                         output.add(themeService.treatVOID(vSession));
                     } else {
                         isComplete = true;
                         output = new XmlBuilder("nothing");
                         log.finer("Not logged in, special request, nothing to display ...");
                     }
                 } else {
                     if (page == Page.CHOIX) {
                         if (special == null) {
                             log.finer("CHOIX page");
                             output.add(choixService.displayCHX(vSession));
                         } else {
                             log.finer("CHOIX special page");
                             output = choixService.displayChxScript(vSession);
                             isComplete = true;
                         }
                     } else if (page == Page.ALBUM) {
                         log.finer("ALBUM page");
                         output.add(albumService.treatALBM((ViewSessionAlbum) vSession));
                     } else if (page == Page.PHOTO) {
                         log.finer("PHOTO page");
                         output.add(photoService.treatPHOTO((ViewSessionPhoto) vSession));
                     } else if (page == Page.CONFIG) {
                         log.finer("CONFIG page");
                         output.add(configService.treatCONFIG((ViewSessionConfig) vSession));
                     } else if (page == Page.TAGS) {
                         log.finer("TAGS page");
                         output.add(tagService.treatTAGS((ViewSessionTag) vSession));
                     } else if (page == Page.IMAGE) {
                         log.finer("IMAGE page");
                         XmlBuilder ret = imageService.treatIMG((ViewSessionImages) vSession);
                         if (ret == null) {
                             isWritten = true;
                         } else {
                             output.add(ret);
                         }
                         log.fine("IMAGE written? "+isWritten);
                     } else {
                         log.finer("VOID page? ("+page+")");
                         output.add(themeService.treatVOID(vSession));
                     }
                 }
             }
             
             output.validate();
         } catch (WebAlbumsServiceException e) {
             e.printStackTrace();
             output.cancel();
         } 
         log.fine("============= Footer (written:"+isWritten+", complete:"+isComplete+")=============");
         long fin = System.currentTimeMillis();
         float time = ((float) (fin - debut) / 1000);
         if (!isWritten) {
             preventCaching(request, response);
 
             if (!isComplete) {
                 output.add(webPageService.xmlLogin(vSession));
                 output.add(webPageService.xmlAffichage(vSession));
 
                 XmlBuilder xmlStats = new XmlBuilder("stats");
                 output.add(xmlStats);
                 xmlStats.add("time", time);
 
             }
             doWrite(response, output, xslFile, isComplete, vSession);
         }
         log.info("============= " + page + ": " + time + " =============");
     }
 
     // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
 
     private static void doWrite(HttpServletResponse response, XmlBuilder output, String xslFile, boolean isComplete, ViewSession vSession) {
         response.setContentType("text/xml");
         try {
             PrintWriter sortie = response.getWriter();
 
             if (!isComplete) {
                 output.addHeader("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
                 output.addHeader("<!DOCTYPE xsl:stylesheet  [" +
                         "<!ENTITY auml   \"&#228;\" >" +
                         "<!ENTITY ouml   \"&#246;\" >" +
                         "<!ENTITY uuml   \"&#252;\" >" +
                         "<!ENTITY szlig  \"&#223;\" >" +
                         "<!ENTITY Auml   \"&#196;\" >" +
                         "<!ENTITY Ouml   \"&#214;\" >" +
                         "<!ENTITY Uuml   \"&#220;\" >" +
                         "<!ENTITY euml   \"&#235;\" >" +
                         "<!ENTITY ocirc  \"&#244;\" >" +
                         "<!ENTITY nbsp   \"&#160;\" >" +
                         "<!ENTITY Agrave \"&#192;\" >" +
                         "<!ENTITY Egrave \"&#200;\" >" +
                         "<!ENTITY Eacute \"&#201;\" >" +
                         "<!ENTITY Ecirc  \"&#202;\" >" +
                         "<!ENTITY egrave \"&#232;\" >" +
                         "<!ENTITY eacute \"&#233;\" >" +
                         "<!ENTITY ecirc  \"&#234;\" >" +
                         "<!ENTITY agrave \"&#224;\" >" +
                         "<!ENTITY iuml   \"&#239;\" >" +
                         "<!ENTITY ugrave \"&#249;\" >" +
                         "<!ENTITY ucirc  \"&#251;\" >" +
                         "<!ENTITY uuml   \"&#252;\" >" +
                         "<!ENTITY ccedil \"&#231;\" >" +
                         "<!ENTITY AElig  \"&#198;\" >" +
                         "<!ENTITY aelig  \"&#330;\" >" +
                         "<!ENTITY OElig  \"&#338;\" >" +
                         "<!ENTITY oelig  \"&#339;\" >" +
                         "<!ENTITY euro   \"&#8364;\">" +
                         "<!ENTITY laquo  \"&#171;\" >" +
                         "<!ENTITY raquo  \"&#187;\" >" +
                         "]>");
                 if (vSession.getConfiguration().wantsXsl()) {
                     output.addHeader("<?xml-stylesheet type=\"text/xsl\" href=\"" + xslFile + "\"?>");
                 }
             }
             sortie.println(output.toString());
 
             sortie.flush();
             sortie.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     protected static void preventCaching(HttpServletRequest request,
             HttpServletResponse response) {
         // see http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
         String protocol = request.getProtocol();
         if ("HTTP/1.0".equalsIgnoreCase(protocol)) {
             response.setHeader("Pragma", "no-cache");
         } else if ("HTTP/1.1".equalsIgnoreCase(protocol)) {
             response.setHeader("Cache-Control", "no-cache"); // "no-store" work also
         }
         response.setDateHeader("Expires", 0);
     }// </editor-fold>
 
     public static XmlBuilder treatLogin(ViewSession vSession, HttpServletRequest request) {
         XmlBuilder output = new XmlBuilder("userLogin");
         try {
             Action action = vSession.getAction();
             log.info("Action: " + action);
             boolean valid = false;
             if (Action.LOGIN == action) {
 
                 String userName = vSession.getUserName();
                 log.info("userName: " + userName);
                 if (userName == null) {
                     output.add("denied");
                     output.add("login");
                     return output;
                 }
 
                 String pass = vSession.getUserPass();
 
                 request.login(userName, pass);
                 output.add("valid");
                 log.info("authentication: " + valid);
             } else {
                 output.add("login");
             }
 
         } catch (javax.servlet.ServletException e) {
             output.add("denied");
             output.add("login");
 
         } finally {
             output.validate() ;
         }
         return output ;
     }
 }
