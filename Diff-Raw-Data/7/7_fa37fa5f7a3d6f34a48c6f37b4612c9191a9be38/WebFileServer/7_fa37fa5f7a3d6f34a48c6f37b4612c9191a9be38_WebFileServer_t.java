 /**
  * Copyright (C) 2000 - 2012 Silverpeas
  *
  * This program is free software: you can redistribute it and/or modify it under the terms of the
  * GNU Affero General Public License as published by the Free Software Foundation, either version 3
  * of the License, or (at your option) any later version.
  *
  * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
  * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
  * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
  * text describing the FLOSS exception, and it is also available here:
  * "http://www.silverpeas.org/legal/licensing"
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License along with this program.
  * If not, see <http://www.gnu.org/licenses/>.
  */
 package com.silverpeas.tags.servlets;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.StringReader;
 import java.rmi.RemoteException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.silverpeas.attachment.AttachmentServiceFactory;
 import org.silverpeas.attachment.model.SimpleDocument;
 import org.silverpeas.attachment.model.SimpleDocumentPK;
 import org.silverpeas.util.Charsets;
 
 import com.silverpeas.gallery.control.ejb.GalleryBm;
 import com.silverpeas.gallery.model.GalleryRuntimeException;
 import com.silverpeas.gallery.model.PhotoDetail;
 import com.silverpeas.gallery.model.PhotoPK;
 import com.silverpeas.tags.authentication.AuthenticationManager;
 import com.silverpeas.tags.util.Admin;
 import com.silverpeas.util.FileUtil;
 import com.silverpeas.util.StringUtil;
 import com.silverpeas.util.web.servlet.RestRequest;
 
 import com.stratelia.silverpeas.silvertrace.SilverTrace;
 import com.stratelia.webactiv.util.EJBUtilitaire;
 import com.stratelia.webactiv.util.JNDINames;
 import com.stratelia.webactiv.util.ResourceLocator;
 import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 
 public class WebFileServer extends HttpServlet {
 
   private static final long serialVersionUID = 1L;
   private Admin admin = null;
 
   @Override
   public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException,
       IOException {
     SilverTrace.info("peasUtil", "OnlineFileServer.doPost", "root.MSG_GEN_ENTER_METHOD");
     RestRequest restRequest = new RestRequest(req, "");
     String userId = AuthenticationManager.getUserId(req);
     String componentId = restRequest.getElementValue("componentId");
     if (!StringUtil.isDefined(componentId)) {
       componentId = restRequest.getElementValue("ComponentId"); // forward compatibility
     }
     if (getAdmin().isUserAllowed(userId, componentId)) {
       OnlineFile file = getWantedFile(restRequest);
       if (file != null) {
         display(res, file);
         return;
       }
       displayWarningHtmlCode(res);
     } else {
       displayError(res, userId, componentId);
     }
   }
 
   public OnlineFile getFileFromOldURL(RestRequest restRequest) throws RemoteException {
     SilverTrace.info("peasUtil", "WebFileServer.doPost", "root.MSG_GEN_ENTER_METHOD");
     OnlineFile file = null;
 
     String mimeType = restRequest.getElementValue("MimeType");
     String sourceFile = restRequest.getElementValue("SourceFile");
     String directory = restRequest.getElementValue("Directory");
     String componentId = restRequest.getElementValue("ComponentId");
     String imageId = restRequest.getElementValue("ImageId");
 
     String attachmentId = restRequest.getWebRequest().getParameter("attachmentId");
     if (StringUtil.isDefined(attachmentId)) {
       SimpleDocument attachment = AttachmentServiceFactory.getAttachmentService().
           searchDocumentById(
           new SimpleDocumentPK(attachmentId), null);
       if (attachment != null) {
         file = new OnlineAttachment(attachment);
       }
     } else if (StringUtil.isDefined(imageId)) {
       PhotoDetail image = getGalleryBm().getPhoto(new PhotoPK(imageId, componentId));
       mimeType = "image/jpg";
       boolean useOriginal = Boolean.parseBoolean(restRequest.getElementValue("UseOriginal"));
       if (useOriginal) {
         sourceFile = image.getImageName();
       } else {
         sourceFile = image.getId() + "_preview.jpg";
       }
       directory = "image" + image.getId();
       file = new OnlineFile(mimeType, sourceFile, directory, componentId);
     } else {
       file = new OnlineFile(mimeType, sourceFile, directory, componentId);
     }
     return file;
   }
 
   protected OnlineFile getWantedFile(RestRequest restRequest)
       throws RemoteException {
     OnlineFile file = getWantedAttachment(restRequest);
     if (file == null) {
       file = getFileFromOldURL(restRequest);
     }
     return file;
   }
 
   protected OnlineFile getWantedAttachment(RestRequest restRequest) {
     String componentId = restRequest.getElementValue("componentId");
     if (!StringUtil.isDefined(componentId)) {
       componentId = restRequest.getElementValue("ComponentId");
     }
     OnlineFile file = null;
     String attachmentId = restRequest.getElementValue("attachmentId");
     String language = restRequest.getElementValue("lang");
     if (StringUtil.isDefined(attachmentId)) {
       SimpleDocumentPK pk;
       if (StringUtil.isDefined(componentId)) {
         pk = new SimpleDocumentPK(attachmentId, componentId);
       } else {
         pk = new SimpleDocumentPK(attachmentId);
       }
       SimpleDocument attachment = AttachmentServiceFactory.getAttachmentService().
           searchDocumentById(pk, language);
       if (attachment != null) {
         file = new OnlineAttachment(attachment);
       }
     }
     return file;
   }
 
   /**
    * This method writes the result of the preview action.
    *
    * @param res - The HttpServletResponse where the html code is write
    * @param htmlFilePath - the canonical path of the html document generated by the parser tools. if
    * this String is null that an exception had been catched the html document generated is empty !!
    * also, we display a warning html page
    */
   private void display(HttpServletResponse response, OnlineFile file) throws IOException {
     File realFile = file.getContentFile();
     if (!realFile.exists() && !realFile.isFile()) {
       displayWarningHtmlCode(response);
       return;
     }
     SilverTrace.info("peasUtil", "OnlineFileServer.display()", "root.MSG_GEN_ENTER_METHOD",
         " htmlFilePath " + realFile.getPath());
     try {
       response.setContentType(FileUtil.getMimeType(realFile.getName()));
       response.setHeader("Content-Length", String.valueOf(realFile.length()));
       FileUtils.copyFile(realFile, response.getOutputStream());
     } catch (Exception e) {
       SilverTrace.warn("peasUtil", "OnlineFileServer.doPost", "root.EX_CANT_READ_FILE", "file name="
           + realFile.getPath());
       displayWarningHtmlCode(response);
     }
   }
 
   private void displayWarningHtmlCode(HttpServletResponse res) throws IOException {
     OutputStream out2 = res.getOutputStream();
     ResourceLocator resourceLocator = new ResourceLocator(
         "org.silverpeas.util.peasUtil.multiLang.fileServerBundle", "");
     try {
       out2.write(resourceLocator.getString("warning").getBytes(Charsets.UTF_8));
     } catch (Exception e) {
       SilverTrace.warn("peasUtil", "OnlineFileServer.displayWarningHtmlCode",
           "root.EX_CANT_READ_FILE", "warning properties");
     } finally {
       IOUtils.closeQuietly(out2);
     }
   }
 
   private void displayError(HttpServletResponse res, String userId, String componentId)
       throws IOException {
     SilverTrace.info("peasUtil", "WebFileServer.displayError()", "root.MSG_GEN_ENTER_METHOD");
 
     res.setContentType("text/html");
     OutputStream out2 = res.getOutputStream();
     int read;
 
     StringBuilder message = new StringBuilder(255);
     message.append("<HTML>");
     message.append("<BODY>");
    message.append("<h1>Erreur ... </h1>");
    message.append("Le service a rencontr&eacute; un probl&egrave;me inattendu pendant le traitement de votre demande <br/>");
     message.append("</BODY>");
     message.append("</HTML>");
 
     StringReader reader = new StringReader(message.toString());
 
     try {
       read = reader.read();
       while (read != -1) {
         out2.write(read); // writes bytes into the response
         read = reader.read();
       }
     } catch (Exception e) {
       SilverTrace.warn("peasUtil", "WebFileServer.displayError", "root.EX_CANT_READ_FILE");
     } finally {
       // we must close the in and out streams
       try {
         out2.close();
       } catch (Exception e) {
         SilverTrace.warn("peasUtil", "WebFileServer.displayError", "root.EX_CANT_READ_FILE",
             "close failed");
       }
     }
   }
 
   private Admin getAdmin() {
     if (admin == null) {
       admin = new Admin();
     }
     return admin;
   }
 
   private GalleryBm getGalleryBm() {
     try {
       return EJBUtilitaire.getEJBObjectRef(JNDINames.GALLERYBM_EJBHOME, GalleryBm.class);
     } catch (Exception e) {
       throw new GalleryRuntimeException("WebFileServer.getGalleryBm",
           SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
     }
   }
 }
