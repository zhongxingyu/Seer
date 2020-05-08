 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icemobile.samples.mobileshowcase.view.examples.device.reality;
 
 import org.icemobile.samples.mobileshowcase.util.FacesUtils;
 import org.icemobile.samples.mobileshowcase.view.examples.device.DeviceInput;
 import org.icemobile.samples.mobileshowcase.view.metadata.annotation.*;
 import org.icemobile.samples.mobileshowcase.view.metadata.context.ExampleImpl;
 import org.icefaces.application.ResourceRegistry;
 
 import javax.annotation.PreDestroy;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ExternalContext;
 import javax.faces.application.Resource;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.event.ActionEvent;
 import java.io.File;
 import java.io.FileOutputStream;
 import javax.imageio.ImageIO;
 import java.awt.geom.AffineTransform;
 import java.awt.image.AffineTransformOp;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.List;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 import java.net.URLEncoder;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 @Destination(
         title = "example.device.reality.destination.title.short",
         titleExt = "example.device.reality.destination.title.long",
         titleBack = "example.device.reality.destination.title.back"
 )
 @Example(
         descriptionPath = "/WEB-INF/includes/examples/device/reality-desc.xhtml",
         examplePath = "/WEB-INF/includes/examples/device/reality-example.xhtml",
         resourcesPath = "/WEB-INF/includes/examples/example-resources.xhtml"
 )
 @ExampleResources(
         resources = {
                 // xhtml
                 @ExampleResource(type = ResourceType.xhtml,
                         title = "reality-example.xhtml",
                         resource = "/WEB-INF/includes/examples/device/reality-example.xhtml"),
                 // Java Source
                 @ExampleResource(type = ResourceType.java,
                         title = "RealityBean.java",
                         resource = "/WEB-INF/classes/org/icemobile/samples/mobileshowcase" +
                                 "/view/examples/device/reality/RealityBean.java")
         }
 )
 @ManagedBean(name = RealityBean.BEAN_NAME)
 @SessionScoped
 public class RealityBean extends ExampleImpl<RealityBean> implements
         Serializable {
 
     private static final Logger logger =
             Logger.getLogger(RealityBean.class.toString());
 
     public static final String BEAN_NAME = "realityBean";
 
     public static final String FILE_KEY = "file";
     public static final String RELATIVE_PATH_KEY = "relativePath";
     public static final String CONTENT_TYPE_KEY = "contentType";
 
     private Map cameraImage = new HashMap();
     private File cameraFile;
     // uploaded video will be stored as a resource.
     private Resource outputResource;
     private String resourcePath;
     private String imagePath;
     private String selection;
     private String label;
     private double latitude = 0.0;
     private double longitude = 0.0;
     HashMap<String,RealityMessage> messages = new HashMap();
     static int THUMBSIZE = 128;
 
     // upload error message
     private String uploadMessage;
 
     public RealityBean() {
         super(RealityBean.class);
     }
 
     public void processUploadedImage(ActionEvent event) {
         outputResource = null;
         if (cameraImage != null &&
                 cameraImage.get("contentType") != null &&
                 ((String)cameraImage.get("contentType")).startsWith("image")) {
             // clean up previously upload file
             if (cameraFile != null){
                 disposeResources();
             }
             cameraFile = (File)cameraImage.get(FILE_KEY);
             if (cameraFile != null) {
                 // copy the bytes into the resource object.
                 try {
                     scaleImage(cameraFile);
                     outputResource = DeviceInput.createResourceObject(
                         cameraFile, UUID.randomUUID().toString(),
                         (String) cameraImage.get(CONTENT_TYPE_KEY));
                     resourcePath = ResourceRegistry
                             .addSessionResource(outputResource);
                 } catch (IOException ex) {
                     logger.warning("Error setting up video resource object");
                 }
                 RealityMessage message = new RealityMessage();
                 message.setTitle(label);
                 message.setLocation(latitude, longitude);
                 message.setFileName(resourcePath);
                 messages.put(label, message);
                 uploadMessage = "Locations marked: " + messages.size();
             }
         } else  {
             // create error message for users.
             uploadMessage = "The image upload failed.";
         }
     }
 
     @PreDestroy
     public void disposeResources(){
         boolean success = cameraFile.delete();
         if (!success && logger.isLoggable(Level.FINE)){
             logger.fine("Could not dispose of media file" + cameraFile.getAbsolutePath());
         }
     }
 
     public void setCameraImage(Map cameraImage) {
 
         this.cameraImage = cameraImage;
 
     }
 
     public Map getCameraImage() {
         return cameraImage;
     }
 
     public String getImage() {
         return imagePath;
     }
 
     public String getUploadMessage() {
         return uploadMessage;
     }
 
     public void setLabel(String label) {
         this.label = label;
     }
 
     public String getLabel() {
         return label;
     }
 
     public void setLatitude(double latitude) {
         this.latitude = latitude;
     }
 
     public double getLatitude() {
         return latitude;
     }
 
     public void setLongitude(double longitude) {
         this.longitude = longitude;
     }
 
     public double getLongitude() {
         return longitude;
     }
 
     public String getSelection()  {
         return selection;
     }
 
     public void setSelection(String selection)  {
        RealityMessage msg = messages.get(selection);
		if( msg != null ){
			this.selection = selection;
			imagePath = msg.getFileName();
		}  
     }
 
     public Collection getMessages()  {
         return messages.values();
     }
 
     public String getBaseURL()  {
         FacesContext facesContext = FacesContext.getCurrentInstance();
         ExternalContext externalContext = facesContext.getExternalContext();
         String serverName = externalContext.getRequestHeaderMap()
                 .get("x-forwarded-host");
         if (null == serverName) {
             serverName = externalContext.getRequestServerName() + ":" + 
             externalContext.getRequestServerPort();
         }
         String url = externalContext.getRequestScheme() + "://" + serverName;
         return url;
     }
 
     private void scaleImage(File photoFile) throws IOException  {
 
         if (null == photoFile) {
             return;
         }
 
         BufferedImage image = ImageIO.read(photoFile);
         // scale the original file into a small thumbNail and the other
         // into a 1 megapixelish sized image.
         int width = image.getWidth();
         int height = image.getHeight();
 
         // create the thumbnail
         AffineTransform tx = new AffineTransform();
         //default image type creates nonstandard all black jpg file
         BufferedImage thumbNailImage = 
                 new BufferedImage(THUMBSIZE, THUMBSIZE, 
                         BufferedImage.TYPE_3BYTE_BGR);
         double imageScale = calculateImageScale(THUMBSIZE, width, height);
         tx.scale(imageScale, imageScale);
         AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
         op.filter(image, thumbNailImage);
 
         // clean up the original image.
         image.flush();
 
         writeImage(thumbNailImage, photoFile);
 
     }
 
     private double calculateImageScale(double intendedSize, int width, int height) {
         double scaleHeight = height / intendedSize;
         // change the algorithm, so height is always the same
         return 1 / scaleHeight;
     }
 
     private void writeImage(BufferedImage image, File imageFile)
             throws IOException {
         FileOutputStream fs = new FileOutputStream(imageFile);
         ImageIO.write(image, "jpg", fs);
         fs.close();
     }
 
 }
 
