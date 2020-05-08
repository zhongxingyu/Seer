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
 
 package org.icemobile.samples.mobileshowcase.view.examples.device.camera;
 
 import org.icemobile.samples.mobileshowcase.util.FacesUtils;
 import org.icemobile.samples.mobileshowcase.view.examples.device.DeviceInput;
 import org.icemobile.samples.mobileshowcase.view.metadata.annotation.*;
 import org.icemobile.samples.mobileshowcase.view.metadata.context.ExampleImpl;
 
 import javax.annotation.PreDestroy;
 import javax.faces.application.Resource;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.event.ActionEvent;
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 @Destination(
         title = "example.device.camera.destination.title.short",
         titleExt = "example.device.camera.destination.title.long",
         titleBack = "example.device.camera.destination.title.back"
 )
 @Example(
         descriptionPath = "/WEB-INF/includes/examples/device/camera-desc.xhtml",
         examplePath = "/WEB-INF/includes/examples/device/camera-example.xhtml",
         resourcesPath = "/WEB-INF/includes/examples/example-resources.xhtml"
 )
 @ExampleResources(
         resources = {
                 // xhtml
                 @ExampleResource(type = ResourceType.xhtml,
                         title = "camera-example.xhtml",
                         resource = "/WEB-INF/includes/examples/device/camera-example.xhtml"),
                 // Java Source
                 @ExampleResource(type = ResourceType.java,
                         title = "CameraBean.java",
                         resource = "/WEB-INF/classes/org/icemobile/samples/mobileshowcase" +
                                 "/view/examples/device/camera/CameraBean.java")
         }
 )
 @ManagedBean(name = CameraBean.BEAN_NAME)
 @SessionScoped
 public class CameraBean extends ExampleImpl<CameraBean> implements
         Serializable {
 
     private static final Logger logger =
             Logger.getLogger(CameraBean.class.toString());
 
     public static final String BEAN_NAME = "cameraBean";
 
     public static final String FILE_KEY = "file";
     public static final String IMAGE_HEIGHT_KEY = "intHeight";
     public static final String IMAGE_WIDTH_KEY = "intWidth";
     public static final String RELATIVE_PATH_KEY = "relativePath";
     public static final String CONTENT_TYPE_KEY = "contentType";
 
     private Map cameraImage = new HashMap();
     private File cameraFile;
     // uploaded video will be stored as a resource.
     private Resource outputResource;
 
     // upload error message
     private String uploadMessage;
 
     private Integer maxHeight;
     private Integer maxWidth;
 
     public CameraBean() {
         super(CameraBean.class);
     }
 
     public void processUploadedImage(ActionEvent event) {
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
                     outputResource = DeviceInput.createResourceObject(
                         cameraFile, UUID.randomUUID().toString(),
                         (String) cameraImage.get(CONTENT_TYPE_KEY));
                 } catch (IOException ex) {
                     logger.warning("Error setting up video resource object");
                 }
                 uploadMessage = "Upload was successful";
                 return;
             }
         }else{
             // create error message for users.
             uploadMessage = "The uploaded image file could not be correctly processed.";
         }
         // a null/empty object is used in the page to hide the audio
         // component.
         outputResource = null;
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
 
     public Resource getOutputResource() {
         return outputResource;
     }
 
     public String getUploadMessage() {
         return uploadMessage;
     }
 
     public Integer getMaxHeight() {
         return maxHeight;
     }
 
     public void setMaxHeight(Integer height) {
         this.maxHeight = height;
     }
 
     public Integer getMaxWidth() {
         return maxWidth;
     }
 
     public void setMaxWidth(Integer width) {
         this.maxWidth = width;
     }
 }
