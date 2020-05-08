 package com.rakuten.rit.roma.romac4j.pool;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.concurrent.TimeoutException;
 
 public class Connection extends Socket {
     private String nodeId;
     private String sendCmd;
     private InputStream is;
     private int bufferSize;
 
     public Connection() {
     }
 
     public void write(String cmd, String key, String opt, byte[] value,
             int casid) throws TimeoutException {
         sendCmd = null;
         if (cmd != null && cmd.length() != 0) {
             sendCmd = cmd;
         }
 
         if (key != null && key.length() != 0) {
             sendCmd = " " + key;
         }
 
         if (opt != null && opt.length() != 0) {
             sendCmd = " " + opt;
         }
 
         if (value != null && value.length != 0) {
             sendCmd = " " + value.length;
         }
 
         if (casid != -1) {
             sendCmd = " " + casid;
         }
     }
 
     public String getNodeId() {
         return nodeId;
     }
 
     public void setNodeId(String nodeId) {
         this.nodeId = nodeId;
     }
 
     public void setBufferSize(int bufferSize) {
         this.bufferSize = bufferSize;
     }
 
     public String readLine() {
         PrintWriter writer = null;
         // BufferedInputStream is = null;
 
         byte[] b = new byte[1];
         byte[] buff = new byte[bufferSize];
         int i = 0;
         try {
             writer = new PrintWriter(getOutputStream(), true);
 
             writer.write(sendCmd + "\r\n");
             writer.flush();
 
             is = new BufferedInputStream(getInputStream());
             while (true) {
                 if (i > bufferSize) {
                     throw new ArrayIndexOutOfBoundsException("Too much size.");
                 }
                 is.read(b, 0, 1);
                 if (b[0] == 0x0d) {
                     is.read(b, 0, 1);
                     if (b[0] == 0x0a)
                         break;
                 }
                 buff[i] = b[0];
                 i++;
             }
         } catch (Exception e) {
             // throw new Exception("Can't convert header.");
             e.printStackTrace();
         }
         return new String(buff, 0, i);
     }
 
     public byte[] readValue() {
         // BufferedInputStream is = null;
         int rtLen = 0;
         String str = readLine();
         if (sendCmd.equals("routingdump bin")) {
             rtLen = Integer.parseInt(str);
         } else {
             String[] header = str.split(" ");
             if (header.length == 4) {
                 rtLen = Integer.valueOf(header[3]);
             }
             // throw Exception???
         }
 
         // Initialize buffer
         byte[] b = new byte[bufferSize];
         byte[] buff = new byte[rtLen + 7];
        byte[] result = new byte[rtLen];
 
         int receiveCount = 0;
         int count = 0;
         try {
             while (receiveCount < rtLen + 7) {
                 count = is.read(b, 0, bufferSize);
                 System.arraycopy(b, 0, buff, receiveCount, count);
                 receiveCount += count;
             }
            System.arraycopy(buff, 0, result, 0, rtLen);
         } catch (IOException e) {
             // e.printStackTrace();
         }
        return result;
     }
 }
