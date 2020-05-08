 package com.arlandis.Responses.FileResponses;
 
import com.arlandis.Responses.FileResponses.FileResponse;
 import com.arlandis.interfaces.Request;
 import com.arlandis.interfaces.ResourceRetriever;
import com.arlandis.interfaces.Response;
 
 public class BmpFileResponse extends FileResponse {
 
     public BmpFileResponse(Request request, ResourceRetriever retriever) {
         super(request, retriever);
     }
 
     public String contentType(){
         return "Content-type: image/bmp";
     }
 }
