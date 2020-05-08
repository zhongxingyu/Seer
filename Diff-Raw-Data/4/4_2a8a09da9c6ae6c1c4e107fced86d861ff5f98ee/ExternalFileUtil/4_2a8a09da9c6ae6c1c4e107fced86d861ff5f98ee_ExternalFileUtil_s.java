 package no.uninett.agora.AgoraMobile;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.util.Log;
 
 import org.apache.cordova.api.CallbackContext;
 import org.apache.cordova.api.CordovaPlugin;
 import org.json.JSONArray;
 import org.json.JSONException;
 
 /**
  * Created by Brian Chen on 7/9/13.
  * Phonegap Plugin for Cookie Management
  */
 public class ExternalFileUtil extends CordovaPlugin {
     @Override
     public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
         if (action.equals("openWith")) {
             Log.v("AgoraMobilePlugin","external open file function");
             String path = args.getString(0);
             this.openWith(path,callbackContext);
             return true;
         }
         return false;
     }
 
     private void openWith(final String path, final CallbackContext callbackContext){
         try{
             cordova.getActivity().runOnUiThread(new Runnable() {
                 public void run() {
                     // Create URI
                     Uri uri = Uri.parse(path);
 
                     Intent intent = null;
                     // Check what kind of file you are trying to open, by comparing the url with extensions.
                     // When the if condition is matched, plugin sets the correct intent (mime) type,
                     // so Android knew what application to use to open the file
 
                     if (path.contains(".doc") || path.contains(".docx")) {
                         // Word document
                         intent = new Intent(Intent.ACTION_VIEW);
                         intent.setDataAndType(uri, "application/msword");
                     } else if(path.contains(".pdf")) {
                         // PDF file
                         intent = new Intent(Intent.ACTION_VIEW);
                         intent.setDataAndType(uri, "application/pdf");
                     } else if(path.contains(".ppt") || path.contains(".pptx")) {
                         // Powerpoint file
                         intent = new Intent(Intent.ACTION_VIEW);
                         intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
                     } else if(path.contains(".xls") || path.contains(".xlsx")) {
                         // Excel file
                         intent = new Intent(Intent.ACTION_VIEW);
                         intent.setDataAndType(uri, "application/vnd.ms-excel");
                     } else if(path.contains(".rtf")) {
                         // RTF file
                         intent = new Intent(Intent.ACTION_VIEW);
                         intent.setDataAndType(uri, "application/rtf");
                     } else if(path.contains(".wav")) {
                         // WAV audio file
                         intent = new Intent(Intent.ACTION_VIEW);
                         intent.setDataAndType(uri, "audio/x-wav");
                     } else if(path.contains(".gif")) {
                         // GIF file
                         intent = new Intent(Intent.ACTION_VIEW);
                         intent.setDataAndType(uri, "image/gif");
                     } else if(path.contains(".jpg") || path.contains(".jpeg")) {
                         // JPG file
                         intent = new Intent(Intent.ACTION_VIEW);
                         intent.setDataAndType(uri, "image/jpeg");
                     } else if(path.contains(".txt")) {
                         // Text file
                         intent = new Intent(Intent.ACTION_VIEW);
                         intent.setDataAndType(uri, "text/plain");
                     } else if(path.contains(".mpg") || path.contains(".mpeg") || path.contains(".mpe") || path.contains(".mp4") || path.contains(".avi")) {
                         // Video files
                         intent = new Intent(Intent.ACTION_VIEW);
                         intent.setDataAndType(uri, "video/*");
                     }else if(path.contains(".sh")){
                         //script files
                         intent = new Intent(Intent.ACTION_VIEW);
                         intent.setDataAndType(uri, "text/plain");
                     }else if(path.contains(".html") || path.contains(".htm")){
                         //html files
                         intent = new Intent(Intent.ACTION_VIEW);
                         intent.setDataAndType(uri, "text/html");
                     }else if(path.contains(".xml") || path.contains(".rss")){
                         //xml files
                         intent = new Intent(Intent.ACTION_VIEW);
                         intent.setDataAndType(uri, "application/rss+xml");
                     }else if(path.contains(".js")){
                         //js files
                         intent = new Intent(Intent.ACTION_VIEW);
                         intent.setDataAndType(uri, "application/javascript");
                     }else if(path.contains(".json")){
                         //json files
                         intent = new Intent(Intent.ACTION_VIEW);
                         intent.setDataAndType(uri, "application/json");
                     }else if(path.contains(".png")){
                         //png files
                         intent = new Intent(Intent.ACTION_VIEW);
                         intent.setDataAndType(uri, "image/png");
                     }else if(path.contains(".class")){
                         //java class files
                         intent = new Intent(Intent.ACTION_VIEW);
                         intent.setDataAndType(uri, "application/java-vm");
                     }else if(path.contains(".jar")){
                         //jar files
                         intent = new Intent(Intent.ACTION_VIEW);
                         intent.setDataAndType(uri, "application/java-archive");
                     }else if(path.contains(".gtar")){
                         //gtar files
                         intent = new Intent(Intent.ACTION_VIEW);
                         intent.setDataAndType(uri, "application/x-gtar");
                     }else if(path.contains(".tar")){
                         //tar files
                         intent = new Intent(Intent.ACTION_VIEW);
                         intent.setDataAndType(uri, "application/x-tar");
                     }else if(path.contains(".css")){
                         //css files
                         intent = new Intent(Intent.ACTION_VIEW);
                         intent.setDataAndType(uri, "text/css");
                     }else if(path.contains(".7z")){
                         //.7z files
                         intent = new Intent(Intent.ACTION_VIEW);
                         intent.setDataAndType(uri, "application/x-7z-compressed");
                     }else if(path.contains(".swf")){
                         //flash files
                         intent = new Intent(Intent.ACTION_VIEW);
                         intent.setDataAndType(uri, "application/x-shockwave-flash");
                     }else if(path.contains(".zip")){
                         //zip files
                         intent = new Intent(Intent.ACTION_VIEW);
                         intent.setDataAndType(uri, "application/zip");
                     }
 
                     //if you want you can also define the intent type for any other file
 
                     //additionally use else clause below, to manage other unknown extensions
                     //in this case, Android will show all applications installed on the device
                     //so you can choose which application to use
 
                     else {
                         intent = new Intent(Intent.ACTION_VIEW);
                         intent.setDataAndType(uri, "*/*");
                     }
 
                     try{
                         cordova.getActivity().startActivity(intent);
                     }catch (Exception ex){
                         callbackContext.error("external open file failed");
                     }
 
                     callbackContext.success("external open file success"); // Thread-safe.
                 }
             });
         }catch (Exception ex){
             callbackContext.error("external open file failed");
         }
     }
 }
