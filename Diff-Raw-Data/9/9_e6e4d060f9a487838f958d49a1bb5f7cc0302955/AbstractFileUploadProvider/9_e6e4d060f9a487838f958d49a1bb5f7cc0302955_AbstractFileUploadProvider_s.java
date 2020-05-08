 /* 
  * This file is part of the Echo File Transfer Library (hereinafter "EFTL").
  * Copyright (C) 2002-2005 NextApp, Inc.
  *
  * Version: MPL 1.1/GPL 2.0/LGPL 2.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * Alternatively, the contents of this file may be used under the terms of
  * either the GNU General Public License Version 2 or later (the "GPL"), or
  * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  * in which case the provisions of the GPL or the LGPL are applicable instead
  * of those above. If you wish to allow use of your version of this file only
  * under the terms of either the GPL or the LGPL, and not to allow others to
  * use your version of this file under the terms of the MPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL or the LGPL. If you do not delete
  * the provisions above, a recipient may use your version of this file under
  * the terms of any one of the MPL, the GPL or the LGPL.
  */
 
 package nextapp.echo2.webcontainer.filetransfer;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 
 import nextapp.echo2.app.filetransfer.UploadSelect;
 
 /**
  * Base abstract class containing common implementations
  */
 public abstract class AbstractFileUploadProvider 
 implements MultipartUploadSPI, Serializable {
     
     protected static final String TEMPDIR_PATH = System.getProperty("java.io.tmpdir", ".");
     protected static final File   TEMPDIR_FILE = new File(TEMPDIR_PATH);
     
     private static final int  DEFAULT_UPLOAD_LIMIT  = 128 * 1024 * 1024; // 128 MB
     private static final File DEFAULT_DISK_CACHE_LOCATION = TEMPDIR_FILE;
     private static final int  DEFAULT_MEMORY_CACHE_THRESHOLD = 16 * 1024;       // 16 KB
    
     /**
      * @see nextapp.echo2.webcontainer.filetransfer.MultipartUploadSPI#supportsDiskCaching()
      */
     public boolean supportsDiskCaching() {
         return true;
     }
 
     /**
      * @see nextapp.echo2.webcontainer.filetransfer.MultipartUploadSPI#supportsFileUploadSizeLimit()
      */
     public boolean supportsFileUploadSizeLimit() {
         return true;
     }
 
     /**
      * @see nextapp.echo2.webcontainer.filetransfer.MultipartUploadSPI#getDiskCacheLocation()
      */
     public File getDiskCacheLocation() throws IOException,
             UnsupportedOperationException {
         return DEFAULT_DISK_CACHE_LOCATION;
     }
     
     /**
      * @see nextapp.echo2.webcontainer.filetransfer.MultipartUploadSPI#getFileUploadSizeLimit()
      */
     public int getFileUploadSizeLimit() throws UnsupportedOperationException {
         return DEFAULT_UPLOAD_LIMIT;
     }
     
     /**
      * @see nextapp.echo2.webcontainer.filetransfer.MultipartUploadSPI#getMemoryCacheThreshold()
      */
     public int getMemoryCacheThreshold() throws UnsupportedOperationException {
         return DEFAULT_MEMORY_CACHE_THRESHOLD;
     }
         
     public HttpServletRequest getWrappedRequest(HttpServletRequest request) 
     throws IOException, ServletException {
         return request;
     }
     
     protected File writeTempFile(InputStream in, UploadSelect uploadSelect) throws IOException {
         File file = new File(TEMPDIR_PATH + "/" + System.currentTimeMillis() + uploadSelect.getRenderId() + ".tmp");
         FileOutputStream out = new FileOutputStream(file);
         
        int byteOffset = 0;
         byte[] buffer = new byte[1024];
         
        while(byteOffset >= 0) {
            byteOffset = in.read(buffer);
            out.write(buffer);
         }
         
         in.close();
         out.flush();
         out.close();
         
         return file;
     }
 
 }
