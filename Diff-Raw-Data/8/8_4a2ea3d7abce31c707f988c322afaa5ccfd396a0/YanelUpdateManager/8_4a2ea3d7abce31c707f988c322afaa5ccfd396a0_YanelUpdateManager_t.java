 /*
 * Copyright 2007 Wyona
  */
 
 package org.wyona.yanel.impl.resources.yanelupdatemanager;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Enumeration;
 
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.sax.SAXResult;
 import javax.xml.transform.sax.SAXTransformerFactory;
 import javax.xml.transform.sax.TransformerHandler;
 import javax.xml.transform.stream.StreamSource;
 
 import org.apache.log4j.Category;
 import org.wyona.yanel.core.Resource;
 import org.wyona.yanel.core.api.attributes.ViewableV2;
 import org.wyona.yanel.core.attributes.viewable.View;
 import org.wyona.yanel.core.attributes.viewable.ViewDescriptor;
 import org.wyona.yanel.core.serialization.SerializerFactory;
 import org.wyona.yanel.core.source.ResourceResolver;
 import org.wyona.yanel.core.transformation.I18nTransformer2;
 import org.wyona.yanel.core.transformation.XIncludeTransformer;
 import org.wyona.yanel.core.util.PathUtil;
 import org.xml.sax.InputSource;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Iterator;
 import java.io.File;
 import java.net.URL;
 
 import org.wyona.yanel.impl.resources.updatefinder.utils.*;
 
 /**
  *
  */
 public class YanelUpdateManager extends Resource implements ViewableV2 {
 
     private static Category log = Category.getInstance(YanelUpdateManager.class);
     private String defaultLanguage;
     private String language = null;
     
     /**
      *
      */
     public YanelUpdateManager() {
     }
     
     /**
      * 
      */
     public boolean exists() {
         return true;
     }
 
     /**
      * 
      */
     public long getSize() {
         return -1;
     }
 
     /**
      * 
      */
     public String getMimeType(String viewId) {
         if (viewId != null && viewId.equals("source"))
             return "application/xml";
         return "application/xhtml+xml";
     }
 
     /**
      * 
      */
     public View getView(String viewId) {
         View view = new View();
         String mimeType = getMimeType(viewId);
         view.setMimeType(mimeType);
 
         try {
             org.wyona.yarep.core.Repository repo = getRealm().getRepository();
 
             if (viewId != null && viewId.equals("source")) {
                 view.setInputStream(new java.io.StringBufferInputStream(getScreen()));
                 view.setMimeType("application/xml");
                 return view;
             }
 
             /*String[] xsltPath = getXSLTPath(getPath());
             if (xsltPath != null) {
 
                 // create reader:
                 XMLReader xmlReader = XMLReaderFactory.createXMLReader();
                 CatalogResolver catalogResolver = new CatalogResolver();
                 xmlReader.setEntityResolver(catalogResolver);
 
                 // create xslt transformer:
                 SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
 
                 TransformerHandler[] xsltHandlers = new TransformerHandler[xsltPath.length];
                 for (int i = 0; i < xsltPath.length; i++) {
                     xsltHandlers[i] = tf.newTransformerHandler(new StreamSource(repo.getNode(xsltPath[i])
                             .getInputStream()));
                     xsltHandlers[i].getTransformer().setParameter("yanel.path.name",
                             PathUtil.getName(getPath()));
                     xsltHandlers[i].getTransformer().setParameter("yanel.path", getPath());
                     xsltHandlers[i].getTransformer().setParameter("yanel.back2context",
                             PathUtil.backToContext(realm, getPath()));
                     xsltHandlers[i].getTransformer().setParameter("yarep.back2realm",
                             PathUtil.backToRealm(getPath()));
 
                     xsltHandlers[i].getTransformer().setParameter("language",
                             getRequestedLanguage());
                 }
 
                 // create i18n transformer:
                 I18nTransformer2 i18nTransformer = new I18nTransformer2("global",
                         getRequestedLanguage(),
                         getRealm().getDefaultLanguage());
                 i18nTransformer.setEntityResolver(catalogResolver);
 
                 // create xinclude transformer:
                 XIncludeTransformer xIncludeTransformer = new XIncludeTransformer();
                 ResourceResolver resolver = new ResourceResolver(this);
                 xIncludeTransformer.setResolver(resolver);
 
                 // create serializer:
                 Serializer serializer = SerializerFactory.getSerializer(SerializerFactory.XHTML_STRICT);
                 ByteArrayOutputStream baos = new ByteArrayOutputStream();
 
                 // chain everything together (create a pipeline):
                 xmlReader.setContentHandler(xsltHandlers[0]);
                 for (int i = 0; i < xsltHandlers.length - 1; i++) {
                     xsltHandlers[i].setResult(new SAXResult(xsltHandlers[i + 1]));
                 }
                 xsltHandlers[xsltHandlers.length - 1].setResult(new SAXResult(xIncludeTransformer));
                 xIncludeTransformer.setResult(new SAXResult(i18nTransformer));
                 i18nTransformer.setResult(new SAXResult(serializer.asContentHandler()));
                 serializer.setOutputStream(baos);
 
                 // execute pipeline:
                 xmlReader.parse(new InputSource(new java.io.StringBufferInputStream(getScreen())));
 
                 // write result into view:
                 view.setInputStream(new ByteArrayInputStream(baos.toByteArray()));
                 return view;
             } else {*/
                 log.debug("Mime-Type: " + mimeType);
                 view.setInputStream(new java.io.StringBufferInputStream(getScreen()));
                 return view;
             //}
         } catch (Exception e) {
             log.error(e + " (" + getPath() + ", " + getRealm() + ")", e);
         }
 
         view.setInputStream(new java.io.StringBufferInputStream(getScreen()));
         return view;
     }    
     
     /**
      * 
      */
     public ViewDescriptor[] getViewDescriptors() {
         ViewDescriptor[] vd = new ViewDescriptor[2];
         vd[0] = new ViewDescriptor("default");
         vd[0].setMimeType(getMimeType(null));
         vd[1] = new ViewDescriptor("source");
         vd[1].setMimeType(getMimeType("source"));
         return vd;
     }
     
     /**
      * 
      */
     private String getScreen() {
         StringBuffer sbContent = new StringBuffer();
         //StringBuffer sbHeader = new StringBuffer();
         Enumeration parameters = request.getParameterNames();
         if (!parameters.hasMoreElements()) {
             plainRequest(sbContent);
         } else {
             if (request.getParameter("updatelink") != null) {
                 if (request.getParameter("updateconfirmed") == null){
                     getUpdateConfirmScreen(sbContent);                    
                 } else {
                     getUpdateScreen(sbContent);
                 }
             } else {
                 log.info("Fallback ...");
                 plainRequest(sbContent);
             }
         }
 
         StringBuffer sb = new StringBuffer("<?xml version=\"1.0\"?>");
         sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
         sb.append("<head><title>Yanel Updater</title>");
         sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + PathUtil.backToRealm(getPath()) + "css/updater.css\"/>");
         //sb.append(sbHeader);
         sb.append("</head>");
         sb.append("<body>");
         sb.append("<img src=\"" + PathUtil.backToRealm(getPath()) + "img/yanel_updater.png\" alt=\"yanel updater\" id=\"title\"/>");
         sb.append(sbContent);
         sb.append("</body>");
         sb.append("</html>");
         return sb.toString();
     }
     
     private void plainRequest(StringBuffer sb) {
         sb.append("<p>");
         sb.append("Installed versions:");
         sb.append("</p>");
         TomcatContextHandler tomcatContextHandler = null;
         Map contextAndWebapp = null;
         try {
             tomcatContextHandler = new TomcatContextHandler(request);
             contextAndWebapp = tomcatContextHandler.getContextAndWebapp();
         } catch (Exception e) {
             log.error(e.getMessage(), e);
             sb.append("<p>Lookup for context and webabs failed. Exception: " + e.getMessage() + "</p>");
         }
 
         sb.append("<table class=\"sortable\">");
         sb.append("<thead>");
         sb.append("<tr><th>Context</th><th>Webapp</th></tr>");
         sb.append("</thead>");
         sb.append("<tbody>");
         Iterator iterator = contextAndWebapp.keySet().iterator();
         
        if (!iterator.hasNext()) sb.append("<tr><td colspan=\"2\">No Yanel contexts found!</td></tr>");
         while (iterator.hasNext()) {
             String context = (String) iterator.next();
             String webapp = (String) contextAndWebapp.get(context);
            sb.append("<tr><td><a href=\"" + "http://" + request.getServerName() + ":" + request.getServerPort() + "/" + context.replaceAll("/", "") + "/\">" + context + "</a></td><td>" + webapp + "</td></tr>");
         }
         sb.append("</tbody>");
         sb.append("</table>");
     }
     
     private void getUpdateConfirmScreen(StringBuffer htmlBodyContent) {
         try {
             InstallInfo installInfo = new InstallInfo(request, request.getParameter("requestingwebapp"));
             UpdateInfo updateInfo = new UpdateInfo(installInfo.getUpdateURL(), installInfo);
             
             TomcatContextHandler tomcatContextHandler = new TomcatContextHandler(request);
             
             HashMap versionDetails = updateInfo.getUpdateVersionDetail("updateLink", request.getParameter("updatelink"));
             String version = (String) versionDetails.get("version");
             String revision = (String) versionDetails.get("revision");
             String id = (String) versionDetails.get("id");
             
             htmlBodyContent.append("<p>The Updater is ready to install " + id + "-v-" + version + "-r-" + revision  + "</p>");
             htmlBodyContent.append("<p>Do you want to continue?</p>");
             htmlBodyContent.append("<p>");
             htmlBodyContent.append("<form method=\"post\" action=\".\">");
             htmlBodyContent.append("<input type=\"submit\" name=\"button\" value=\"YES\"/>");
             htmlBodyContent.append("<input type=\"hidden\" name=\"updateconfirmed\" value=\"updateconfirmed\"/>");
             htmlBodyContent.append("<input type=\"hidden\" name=\"updatelink\" value=\"" + request.getParameter("updatelink") + "\"/>");
             htmlBodyContent.append("<input type=\"hidden\" name=\"requestingwebapp\" value=\"" + request.getParameter("requestingwebapp") + "\"/>");
             htmlBodyContent.append("</form>");
             htmlBodyContent.append("<form method=\"post\" action=\".\">");
             htmlBodyContent.append("<input type=\"submit\" name=\"button\" value=\"Cancel\"></input>");
             htmlBodyContent.append("</form>");
             htmlBodyContent.append("</p>");
         }  catch (Exception e) {
             log.error(e.getMessage(), e);
             htmlBodyContent.append("<p>An error occoured. Exception: " + e.getMessage() + "</p>");
         }
     }
     
     private void getUpdateScreen(StringBuffer sb) {
         try {
             InstallInfo installInfo = new InstallInfo(request, request.getParameter("requestingwebapp"));
             UpdateInfo updateInfo = new UpdateInfo(installInfo.getUpdateURL(), installInfo);
             
             String destDir = request.getSession().getServletContext().getRealPath(".")
                     + File.separator + "..";
             WarFetcher warFetcher = new WarFetcher(request, request.getParameter("updatelink"), destDir);
 
             HashMap versionDetails = updateInfo.getUpdateVersionDetail("updateLink",
                     request.getParameter("updatelink"));
             String version = (String) versionDetails.get("version");
             String revision = (String) versionDetails.get("revision");
             String id = (String) versionDetails.get("id");
 
             warFetcher.fetch();
 
             //here should the merging of the conf, realms etc happen
             try {
                 reConfigureNewFromOld(installInfo, versionDetails, destDir);
             } catch (Exception e) {
                 log.error(e.getMessage(), e);
                 sb.append("<p>Update partly failed. Exception: " + e.getMessage() + "</p>");
             }
             
             
             TomcatContextHandler tomcatContextHandler = new TomcatContextHandler(request);
             tomcatContextHandler.setContext(id + "-v-" + version + "-r-" + revision, id + "-v-" + version + "-r-" + revision);
             String pathToUpdater = "http://" + request.getServerName() + ":"
                     + request.getServerPort() + "/" + id + "-v-" + version + "-r-" + revision + "/";
 
             sb.append("<p>");
             sb.append("Update done. <br/>");
             plainRequest(sb);
             sb.append("</p>");
         } catch (Exception e) {
             log.error(e.getMessage(), e);
             sb.append("<p>Update failed. Exception: " + e.getMessage() + "</p>");
         }
     }
     
     /**
      * Get XSLT path
      */
     private String[] getXSLTPath(String path) throws Exception {
         String[] xsltPath = getResourceConfigProperties("xslt");
         if (xsltPath != null)
             return xsltPath;
         log.info("No XSLT Path within: " + path);
         return null;
     }
     
     private void reConfigureNewFromOld(InstallInfo installInfo, HashMap versionDetails, String destDir) throws Exception{
         //TODO copy is not really sufficient better do a kind of merge
         ArrayList protectedFiles = installInfo.getProtectedFiles();
         String srcDirectoryPath =  destDir + File.separator + installInfo.getId() + "-v-" + installInfo.getVersion() + "-r-" + installInfo.getRevision() + File.separator;
         String dstDirectoryPath =  destDir + File.separator + (String) versionDetails.get("id") + "-v-" + (String) versionDetails.get("version") + "-r-" + (String) versionDetails.get("revision") + File.separator;
         
         try {
             for (int i = 0; i < protectedFiles.size(); i++) {
                 copy(new File(srcDirectoryPath + protectedFiles.get(i)), new File(dstDirectoryPath + protectedFiles.get(i)) );
             }
         } catch (Exception e) {
             log.error("Copy of configuration failed");
             throw new Exception("Copy of configuration failed");
         }
     }
     
     private void copy(File src, File dst) throws IOException {
         InputStream in = new FileInputStream(src);
         OutputStream out = new FileOutputStream(dst);
         // Transfer bytes from in to out
         byte[] buf = new byte[1024];
         int len;
         while ((len = in.read(buf)) > 0) {
             out.write(buf, 0, len);
         }
         in.close();
         out.close();
     }
 }
