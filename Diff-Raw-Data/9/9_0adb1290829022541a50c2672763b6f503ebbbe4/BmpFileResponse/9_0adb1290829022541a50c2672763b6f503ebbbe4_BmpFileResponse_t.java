 package com.arlandis.Responses.FileResponses;
 
 import com.arlandis.interfaces.Request;
 import com.arlandis.interfaces.ResourceRetriever;
 
 public class BmpFileResponse extends FileResponse {
 
     public BmpFileResponse(Request request, ResourceRetriever retriever) {
         super(request, retriever);
     }
 
     public String contentType(){
         return "Content-type: image/bmp";
     }
 }
