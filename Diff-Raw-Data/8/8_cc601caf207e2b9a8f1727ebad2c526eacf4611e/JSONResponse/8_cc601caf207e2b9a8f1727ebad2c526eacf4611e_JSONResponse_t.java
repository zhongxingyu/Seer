 package ch.bazaruto;
 
 import java.io.InputStream;
 
 public class JSONResponse extends Response {
    
    {
        mimeType = NanoHTTPD.MIME_JSON;
    }
    
     public JSONResponse() {
         this.data = toInputStream("");
     }
     
     public JSONResponse(InputStream data) {
         this.data = data;
     }
     
     public JSONResponse(String json) {
         this.data = toInputStream(json);
     }
     
     public JSONResponse(InputStream data, String status) {
         this.data = data;
         this.status = status;
     }
 
     public JSONResponse(String json, String status) {
         this.data = toInputStream(json);
         this.status = status;
     }
 }
