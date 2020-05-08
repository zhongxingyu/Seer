 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package testing;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.net.ftp.FTP;
 import org.apache.commons.net.ftp.FTPClient;
 import org.apache.commons.net.ftp.FTPFile;
 
 /**
  *
  * @author Quality of Service
  */
 public class FTPClientTesting {
 
     private static void listFromServerFTP() throws Exception {
         FTPClient client = new FTPClient();
 
         client.setUseEPSVwithIPv4(true);
 
         client.connect(getIPAddress());
         client.login("adminroot", "adminroot");
 
         String[] names = client.listNames();
 
         for (String name : names) {
             System.out.println("Name: " + name);
         }
 
         //FTPFile[] ftpFiles = client.listFiles();
        FTPFile[] ftpFiles = client.mlistDir("/");
 
         for (FTPFile ff : ftpFiles) {
             if (ff.getType() == FTPFile.FILE_TYPE) {
                 System.out.println("File Name: " + ff.getName() + "  Type: " + ff.getType()
                         + ";;;; " + FileUtils.byteCountToDisplaySize(ff.getSize()));
             } else {
                 System.out.println("Directory Name: " + ff.getName() + "  Type: " + ff.getType()
                         + ";;;; " + FileUtils.byteCountToDisplaySize(ff.getSize()));
             }
         }
         client.logout();
         client.disconnect();
     }
 
     private static void downloadFromServerFTP() throws SocketException, IOException {
         FTPClient client = new FTPClient();
         FileOutputStream fos = null;
 
         client.connect("localhost");
         client.login("adminroot", "adminroot");
 
         String local = "D:\\Proyectos en NetBeans 9\\neuromancerV1\\FTPServer2\\RSAPublic.key";
         String remote = "RSAPublic.key";
 
         fos = new FileOutputStream(local);
         client.retrieveFile(remote, fos);
 
         client.noop();
 
         fos.close();
         client.logout();
         client.disconnect();
     }
 
     private static void uploadToServerFTP() throws SocketException, IOException {
         FTPClient client = new FTPClient();
         FileInputStream fis = null;
 
         client.connect("localhost");
         client.login("adminroot", "adminroot");
 
         String local = ".\\FTPServer2\\UPLOADER.txt";
         String remote = "UPLOADER.txt";
 
         client.setFileTransferMode(FTP.BINARY_FILE_TYPE);
 
         fis = new FileInputStream(local);
         client.storeFile(remote, fis);
 
         client.noop();
 
         fis.close();
         client.disconnect();
     }
 
     private static String getIPAddress() throws UnknownHostException {
         InetAddress localhost = InetAddress.getByName("QoS-PC");
 
         return localhost.getHostAddress();
     }
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws Exception {
 
        listFromServerFTP();
        //downloadFromServerFTP();
         //uploadToServerFTP();
     }
 }
