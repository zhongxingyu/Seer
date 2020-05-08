 /*
  * Created by Dmitry Miroshnichenko 01-12-2012. Copyright Mail.Ru Group 2012.
  * All rights reserved.
  */
 package ru.mail.plugins.imageloader.servlets;
 
 import java.awt.Graphics2D;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Iterator;
 import java.util.List;
 import javax.imageio.ImageIO;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.httpclient.util.URIUtil;
 import org.apache.log4j.Logger;
 import ru.mail.plugins.imageloader.common.Utils;
 import ru.mail.plugins.imageloader.settings.PluginSettingsManager;
 import com.atlassian.crowd.embedded.api.User;
 import com.atlassian.jira.component.ComponentAccessor;
 import com.atlassian.jira.config.util.JiraHome;
 import com.atlassian.jira.security.JiraAuthenticationContext;
 import com.atlassian.jira.security.Permissions;
 
 /**
  * Plug-In servlet for storing images.
  */
 public class MailIssueImageLoaderServlet
     extends HttpServlet
 {
     /**
      * Unique ID.
      */
     private static final long serialVersionUID = 8296716621463126487L;
 
     /**
      * Logger.
      */
     private final Logger log = Logger.getLogger(MailIssueImageLoaderServlet.class);
 
     /**
      * Plug-In data.
      */
     private final PluginSettingsManager pluginSettingsManager;
 
     /**
      * Constructor.
      */
     public MailIssueImageLoaderServlet(
         PluginSettingsManager pluginSettingsManager)
     {
         this.pluginSettingsManager = pluginSettingsManager;
     }
 
     @Override
     protected void doGet(
         HttpServletRequest req,
         HttpServletResponse resp)
     throws ServletException, IOException
     {
         String projectName = req.getParameter("projectname");
         String issueTypeName = req.getParameter("issuetypename");
         String avatarSrc = req.getParameter("avatarSrc");
 
         if (!Utils.isValidStr(projectName) || issueTypeName == null)
         {
             resp.sendRedirect(avatarSrc);
             return;
         }
 
        projectName = URIUtil.decode(projectName);
        issueTypeName = URIUtil.decode(issueTypeName);

         JiraAuthenticationContext jiraContext = ComponentAccessor.getJiraAuthenticationContext();
         User user = jiraContext.getLoggedInUser();
         if (user == null)
         {
             resp.sendRedirect(avatarSrc);
             return;
         }
 
         String imageName = pluginSettingsManager.getIssueTypeImage(projectName, issueTypeName);
         if (imageName == null)
         {
             resp.sendRedirect(avatarSrc);
             return;
         }
 
         File jiraHome = ComponentAccessor.getComponent(JiraHome.class).getHome();
         File jiraHomeImg = new File(jiraHome, "images");
         File filePath = new File(jiraHomeImg, imageName);
 
         if (!filePath.exists())
         {
             resp.sendRedirect(avatarSrc);
             return;
         }
 
         OutputStream out = resp.getOutputStream();
         FileInputStream in = new FileInputStream(filePath);
         int size = in.available();
         byte[] content = new byte[size];
         in.read(content);
         out.write(content);
         in.close();
         out.flush();
         out.close();
     }
 
     @Override
     protected void doPost(
         HttpServletRequest req,
         HttpServletResponse resp)
     throws ServletException, IOException
     {
         String projectName = req.getParameter("projectname");
         String issueTypeName = req.getParameter("issuetypename");
 
         if (!Utils.isValidStr(projectName) || issueTypeName == null)
         {
             log.error("MailIssueImageLoaderServlet::doPost - Incorrect input parameters");
             resp.sendError(404);
             return;
         }
 
         JiraAuthenticationContext jiraContext = ComponentAccessor.getJiraAuthenticationContext();
         User user = jiraContext.getLoggedInUser();
         if (user == null)
         {
             resp.sendRedirect(Utils.getBaseUrl(req));
             return;
         }
 
         if (!ComponentAccessor.getPermissionManager().hasPermission(Permissions.ADMINISTER, user))
         {
             log.error("MailIssueImageLoaderServlet::doPost - No permissions");
             resp.sendError(403, jiraContext.getI18nHelper().getText("mailru.image.loader.admin.error.permissions"));
             return;
         }
 
         boolean isMultipart = ServletFileUpload.isMultipartContent(req);
         if (!isMultipart)
         {
             log.error("MailIssueImageLoaderServlet::doPost - Incorrect request type");
             resp.sendError(403, jiraContext.getI18nHelper().getText("mailru.image.loader.admin.error.invalidtype"));
             return;
         }
 
         DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
         ServletFileUpload upload = new ServletFileUpload(fileItemFactory);
 
         @SuppressWarnings("rawtypes")
         List items = null;
         try
         {
             items = upload.parseRequest(req);
         }
         catch (FileUploadException e)
         {
             log.error("MailIssueImageLoaderServlet::doPost - Error occured during uploading file");
             resp.sendError(400, jiraContext.getI18nHelper().getText("mailru.image.loader.admin.error.noitems"));
             return;
         }
 
         File jiraHome = ComponentAccessor.getComponent(JiraHome.class).getHome();
         File jiraHomeImg = new File(jiraHome, "images");
         if (!jiraHomeImg.exists())
         {
             jiraHomeImg.mkdirs();
         }
         @SuppressWarnings("rawtypes")
         Iterator iter = items.iterator();
         while (iter.hasNext())
         {
             FileItem item = (FileItem) iter.next();
 
             if (!item.isFormField())
             {
                 String fileName = projectName + "_" + issueTypeName + "_" + item.getName();
                 File file = new File(jiraHomeImg, fileName);
                 try
                 {
                     if ("".equals(item.getName())) //--> delete icon
                     {
                         String oldFileName = pluginSettingsManager.getIssueTypeImage(projectName, issueTypeName);
                         if (oldFileName != null)
                         {
                             File oldFile = new File(jiraHomeImg, oldFileName);
                             if (oldFile.exists())
                             {
                                 oldFile.delete();
                             }
                             pluginSettingsManager.setIssueTypeImage(null, projectName, issueTypeName);
                         }
                     }
                     else //--> add icon
                     {
                         item.write(file);
                         scale(file.getAbsolutePath(), 48, 48, file.getAbsolutePath());
                         String oldFileName = pluginSettingsManager.getIssueTypeImage(projectName, issueTypeName);
                         if (oldFileName != null)
                         {
                             File oldFile = new File(jiraHomeImg, oldFileName);
                             if (oldFile.exists())
                             {
                                 oldFile.delete();
                             }
                         }
                         pluginSettingsManager.setIssueTypeImage(fileName, projectName, issueTypeName);
                     }
                 }
                 catch (Exception e)
                 {
                     log.error("MailIssueImageLoaderServlet::doPost - Error occured during writing file to disk");
                     resp.sendError(500, jiraContext.getI18nHelper().getText("mailru.image.loader.admin.error.cantwrite"));
                     return;
                 }
             }
         }
         String referrer = req.getHeader("referer");
         resp.sendRedirect(referrer);
     }
 
     /**
      * Scale image.
      */
     private void scale(
         String src,
         int width,
         int height,
         String dest)
     throws IOException
     {
         BufferedImage bsrc = ImageIO.read(new File(src));
         BufferedImage bdest = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
         Graphics2D g = bdest.createGraphics();
         AffineTransform at = AffineTransform.getScaleInstance((double)width/bsrc.getWidth(), (double)height/bsrc.getHeight());
         g.drawRenderedImage(bsrc,at);
         ImageIO.write(bdest, "PNG", new File(dest));
     }
 }
