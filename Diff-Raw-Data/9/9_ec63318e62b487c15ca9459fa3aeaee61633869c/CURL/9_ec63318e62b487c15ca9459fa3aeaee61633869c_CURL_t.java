 package hu.juranyi.zsolt.heritrixremote.communication;
 
 import java.io.File;
 
 /**
  *
  * @author Zsolt Jurányi
  */
 public class CURL implements IRESTClient {
 
     private String targetURL;
     private String usernameAndPassword;
     private String data;
     private File file;
     private String response;
 
     @Override
     public void setTargetURL(String targetURL) {
         this.targetURL = targetURL;
     }
 
     @Override
     public void setUsernameAndPassword(String usernameAndPassword) {
         this.usernameAndPassword = usernameAndPassword;
     }
 
     @Override
     public void setMethod(String method) {
         // not needed, cURL will handle this
     }
 
     @Override
     public void setData(String data) {
         this.data = data;
     }
 
     @Override
     public void setFileToSend(File file) {
         this.file = file;
     }
 
     @Override
     public void request() throws Exception {
        String curl = "curl DATA -k -u AUTH --anyauth --location URL -3";
         curl = curl.replace("AUTH", usernameAndPassword).replace("URL", targetURL);
         if (null != data) {
             curl = curl.replace("DATA", "-d " + data);
         } else if (null != file) {
             curl = curl.replace("DATA", "-T " + file.getAbsolutePath());
         } else {
             curl = curl.replace("DATA ", "");
         }
 
         ShellExec shellExec = new ShellExec(curl);
         shellExec.exec();
         response = shellExec.getStdout();
     }
 
     @Override
     public String getResponse() {
         return response;
     }
 }
