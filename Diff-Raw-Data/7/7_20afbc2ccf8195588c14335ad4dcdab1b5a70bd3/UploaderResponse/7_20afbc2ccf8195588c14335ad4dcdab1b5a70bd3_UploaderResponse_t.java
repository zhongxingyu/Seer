 /*
  * Copyright (c) 2005 Aetrion LLC.
  */
 
 package com.aetrion.flickr.uploader;
 
 import java.util.Collection;
 
 import com.aetrion.flickr.Response;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Text;
 
 /**
  * @author Anthony Eden
  */
 public class UploaderResponse implements Response {
 
     private String status;
     private String photoId;
     private String errorCode;
     private String errorMessage;
 
     public void parse(Document document) {
         Element rspElement = document.getDocumentElement();
         status = rspElement.getAttribute("stat");
         if ("ok".equals(status)) {
             Element photoIdElement = (Element) rspElement.getElementsByTagName("photoid").item(0);
             photoId = ((Text)photoIdElement.getFirstChild()).getData();
         } else {
            Element errElement = (Element) rspElement.getElementsByTagName("err").item(0);
            errorCode = errElement.getAttribute("code");
            errorMessage = errElement.getAttribute("msg");
         }
     }
 
     public String getStatus() {
         return status;
     }
 
     public String getPhotoId() {
         return photoId;
     }
 
     public boolean isError() {
         return errorMessage != null;
     }
 
     public String getErrorCode() {
         return errorCode;
     }
 
     public String getErrorMessage() {
         return errorMessage;
     }
 
     /* (non-Javadoc)
      * @see com.aetrion.flickr.Response#getPayload()
      */
     public Element getPayload() {
         // TODO Auto-generated method stub
         return null;
     }
 
     /* (non-Javadoc)
      * @see com.aetrion.flickr.Response#getPayloadCollection()
      */
     public Collection getPayloadCollection() {
         // TODO Auto-generated method stub
         return null;
     }
     
     
 }
