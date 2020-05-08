 
 package com.quartercode.qcutil.net;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.Serializable;
 import java.net.HttpURLConnection;
 import java.net.ProtocolException;
 import java.net.URL;
 import java.util.Scanner;
 import com.quartercode.qcutil.Progressable;
 import com.quartercode.qcutil.io.File;
 import com.quartercode.qcutil.utility.ObjectUtil;
 
 public class NetConnection implements Serializable, Comparable<NetConnection> {
 
     private static final long serialVersionUID = 5762665241921899355L;
 
     protected URL             url;
 
     public NetConnection(final URL url) {
 
         super();
 
         setURL(url);
     }
 
     public URL getUrl() {
 
         return url;
     }
 
     public void setURL(final URL url) {
 
         this.url = url;
     }
 
     public void download(final File destination, final Progressable progress) throws FileNotFoundException, ProtocolException, IOException {
 
         if (progress != null) {
             final String[] parts = url.getFile().split("/");
             progress.setProgressStatus("Downloading " + parts[parts.length - 1]);
         }
 
         destination.getParentFile().mkdirs();
 
         final OutputStream outputStream = new FileOutputStream(destination);
         outputStream.flush();
 
         final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
         connection.setRequestMethod("GET");
         connection.connect();
 
         final byte[] tempBuffer = new byte[4096];
 
         final InputStream inputStream = connection.getInputStream();
         final double fileSize = connection.getContentLength();
         int counter;
         while ( (counter = inputStream.read(tempBuffer)) > 0) {
             outputStream.write(tempBuffer, 0, counter);
             outputStream.flush();
 
             final double destinationFileSize = destination.length();
             final double downloaded = destinationFileSize / fileSize;
 
             if (progress != null) {
                 progress.setProgressPercent((int) (downloaded * 100));
             }
         }
 
         inputStream.close();
         outputStream.close();
     }
 
     public void downloadOriginalName(final File destinationFolder, final Progressable progressable) throws FileNotFoundException, ProtocolException, IOException {
 
         download(new File(destinationFolder, url.getFile().split("/")[url.getFile().split("/").length - 1]), progressable);
     }
 
     public void downloadOriginalPath(final File destinationFolder, final Progressable progressable) throws FileNotFoundException, ProtocolException, IOException {
 
         download(new File(destinationFolder, url.getFile()), progressable);
     }
 
     public String getContent(final Progressable progressable) throws IOException {
 
         String fileContent = "";
         final InputStream inputStream = url.openStream();
        final Scanner scanner = new Scanner(inputStream);
        fileContent = scanner.useDelimiter("\\Z").next();
        scanner.close();
         inputStream.close();
 
         return fileContent;
     }
 
     public void touch(final Progressable progressable) throws IOException {
 
         url.openStream().close();
     }
 
     @Override
     public int compareTo(final NetConnection o) {
 
         return url.toString().compareTo(o.url.toString());
     }
 
     @Override
     public int hashCode() {
 
         final int prime = 31;
         int result = 1;
         result = prime * result + (url == null ? 0 : url.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(final Object obj) {
 
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final NetConnection other = (NetConnection) obj;
         if (url == null) {
             if (other.url != null) {
                 return false;
             }
         } else if (!url.equals(other.url)) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
 
         return ObjectUtil.generateObjectStringWithNames(this, "url");
     }
 
 }
