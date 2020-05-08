 /*
  * Copyright (c) 2011, ICEsoft Technologies Canada Corp.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, 
  * are permitted provided that the following conditions are met:
  * 
  * Redistributions of source code must retain the above copyright notice, 
  * this list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright notice, 
  * this list of conditions and the following disclaimer in the documentation 
  * and/or other materials provided with the distribution.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
  * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS
  * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
  * POSSIBILITY OF SUCH DAMAGE.
  * 
  */ 
 package org.icemobile.client.blackberry.script.upload;
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 
 import javax.microedition.io.Connector;
 import javax.microedition.io.file.FileConnection;
 
 import org.icemobile.client.blackberry.ICEmobileContainer;
 import org.icemobile.client.blackberry.http.HttpRequestHandler;
 import org.icemobile.client.blackberry.http.MultipartPostData;
 import org.icemobile.client.blackberry.utils.NameValuePair;
 import org.icemobile.client.blackberry.utils.HttpUtils;
 import org.icemobile.client.blackberry.utils.ResultHolder;
 import org.icemobile.client.blackberry.utils.UploadUtilities;
 
 import net.rim.device.api.script.ScriptableFunction;
 import net.rim.device.api.system.EventLogger;
 import net.rim.device.api.ui.UiApplication;
 
 /**
  * A fileUpload javascript accessable extension that is AJAX aware. 
  *
  */
 public class AjaxUpload extends ScriptableFunction {
 
     private ICEmobileContainer mContainer;
     private HttpRequestHandler mHandler; 
     private String mResult; 
     private static int uploadIndex;
 
     public AjaxUpload( ICEmobileContainer container ) { 
 
         mContainer = container; 
         mHandler = new HttpRequestHandler(container);
     }
 
     public Object invoke( Object thiz, Object[] args) { 
 
         if (args.length != 1) { 
             ICEmobileContainer.ERROR( "ajax.upload - wrong number of arguments:" + args.length);
             return Boolean.FALSE; 
         }
         String serializedForm = (String) args[0];
 
         //boolean gotValue = true;
         //String[] result;
         //		MultipartEntity content = new MultipartEntity();
         final MultipartPostData content = new MultipartPostData();
         final long startTime = System.currentTimeMillis();
         
         try {
 //            ICEmobileContainer.DEBUG("ajax.upload - Serialized form:\n" + serializedForm);
             NameValuePair[] params = UploadUtilities.getNameValuePairs(serializedForm, "=", "&");
             //Log.e("ICEutil", "NVP=" + params.length);
             for (int i=0; i<params.length; i++) {
                 if (params[i].getName().indexOf("file") > -1) {
                     String filename = HttpUtils.URLdecode(params[i].getValue());
                     ICEmobileContainer.DEBUG("ajax.upload - File part encountered: " + filename);
 
                     if (filename.indexOf(".jpg")> -1 ) { 
                         byte[] imageBytes;
                         InputStream theImage;
                         FileConnection fconn = (FileConnection) Connector.open(filename);
 
                         imageBytes = new byte[(int) fconn.fileSize()];
                         theImage = fconn.openInputStream();
                         theImage.read(imageBytes);
                         ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream( imageBytes.length );
                         byteArrayOutputStream.write( imageBytes );
 
                         byteArrayOutputStream.flush();
                         byteArrayOutputStream.close();		
 
                         String name = HttpUtils.URLdecode(params[i].getName()); 
                         String imageVar = byteArrayOutputStream.toString(); 
                         content.appendImagePart(name,
                                 filename, 
                                 imageVar );
                     } else if (filename.indexOf(".amr") > -1) {
 
                         byte[] clipBytes;
                         InputStream theClip;
 
                         // Audio encoder insists on having "file:// already in the storage location, while image 
                         // control can't have it. 
                         FileConnection fconn = (FileConnection) Connector.open(filename);
                         clipBytes = new byte[(int) fconn.fileSize()];
                         theClip = fconn.openInputStream();
                         theClip.read(clipBytes);
                         ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream( clipBytes.length );
                         byteArrayOutputStream.write( clipBytes );
 
                         byteArrayOutputStream.flush();
                         byteArrayOutputStream.close();		
 
                         String name = HttpUtils.URLdecode(params[i].getName()); 
                         String audioVar = byteArrayOutputStream.toString(); 
                         content.appendAudioPart(name,
                                 filename, 
                                 audioVar );
                     } else if (filename.indexOf(".3gp") > -1) { 
                         byte[] videoBytes;
                         InputStream theVideo;
                         FileConnection fconn = (FileConnection) Connector.open(filename);
 
                         videoBytes = new byte[(int) fconn.fileSize()];
                         theVideo = fconn.openInputStream();
                         theVideo.read(videoBytes);
                         ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream( videoBytes.length );
                         byteArrayOutputStream.write( videoBytes );
 
                         byteArrayOutputStream.flush();
                         byteArrayOutputStream.close();		
 
                         String name = HttpUtils.URLdecode(params[i].getName()); 
                         String videoVar = byteArrayOutputStream.toString(); 
                         content.appendVideoPart(name,
                                 filename, 
                                 videoVar );
                     }
 
                 } else {
                     if (params[i].getName() == null || params[i].getName().equals("")) { 
                         continue;
                     }
                     content.append( HttpUtils.URLdecode(params[i].getName()), HttpUtils.URLdecode(params[i].getValue()));
                 }
             }
 
            
             final DisposableResultHolder drh = new DisposableResultHolder();
             UiApplication.getUiApplication().invokeLater( new Runnable() { 
                 public void run() { 
 
                     String response = mHandler.processAJAXRequest( mContainer.getCurrentURL(), null, content );
                    ICEmobileContainer.TIME(startTime, "ajax.upload FormUpload");
                     ICEmobileContainer.DEBUG("ajax.upload - Raw response: " + response);
                    drh.setResult(response);
                    if (response != null) { 
                        mContainer.processResult( "AjaxUpload_" + uploadIndex++,  drh);
                     }
                 }
             });
 
 
         } catch (Exception e) { 
             ICEmobileContainer.ERROR ("ajax.upload - Upload exception: " + e);
             return Boolean.FALSE;
         }
         return Boolean.TRUE;
     }
 
    
     class DisposableResultHolder implements ResultHolder { 
 
         private String result; 
         
         public void setResult(String result) { 
             if (result != null) { 
                 this.result = new String (result); 
             } else { 
                 ICEmobileContainer.ERROR("Null result in ajaxUpload" );
             }
         }
         
         public String getResult() { 
             return this.result; 
         }
     }
 
 }
