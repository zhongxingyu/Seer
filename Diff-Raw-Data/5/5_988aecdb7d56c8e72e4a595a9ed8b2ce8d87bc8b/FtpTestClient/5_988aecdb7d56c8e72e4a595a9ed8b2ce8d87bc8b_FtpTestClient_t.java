 /*
  ------------------------------
  Hermes FTP Server
  Copyright (c) 2006 Lars Behnke
  ------------------------------
 
  This file is part of Hermes FTP Server.
 
  Hermes FTP Server is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  Foobar is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with Foobar; if not, write to the Free Software
  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
  */
 
 package net.sf.hermesftp.client;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.StringTokenizer;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.net.ServerSocketFactory;
 
 import net.sf.hermesftp.common.FtpConstants;
 import net.sf.hermesftp.utils.NetUtils;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 // CHECKSTYLE:OFF
 /**
  * FTP test client for test purposes.
  * 
  * @author Lars Behnke
  */
 public class FtpTestClient {
 
     /**
      * On Linux ports below 1024 can only be bound by root.
      */
     private static final int TEST_FTP_PORT   = 2121;
 
     private static final int LOG_LINE_LENGTH = 80;
 
     private static Log       log             = LogFactory.getLog(FtpTestClient.class);
 
     private PrintWriter      out;
 
     private BufferedReader   in;
 
     private String           server;
 
     private InputStream      transIs;
 
     private OutputStream     transOut;
 
     private Socket           passiveModeSocket;
 
     private ServerSocket     activeModeServerSocket;
 
     private Socket           serverSocket;
 
     private StringBuffer     textBuffer;
 
     private byte[]           rawBuffer;
 
     private Object           lock            = new Object();
 
     /**
      * Returns the text data.
      * 
      * @return The text data.
      */
     public String getTextData() {
         return textBuffer.toString();
     }
 
     /**
      * Returns the raw data.
      * 
      * @return The text data.
      */
     public byte[] getRawData() {
         return rawBuffer;
     }
 
     /**
      * Opens a anonymous FTP connection.
      * 
      * @throws IOException Error on connection.
      */
     public void openConnection() throws IOException {
         openConnection("anonymous", "my@mail");
     }
 
     /**
      * Opens a FTP connection.
      * 
      * @param user The user name.
      * @param pass The user password.
      * @throws IOException Error on connection.
      */
     public void openConnection(String user, String pass) throws IOException {
         openConnection(null, user, pass, TEST_FTP_PORT);
 
     }
 
     /**
      * Closes the FTP connection.
      */
     public void closeConnection() {
         try {
             if (serverSocket != null) {
                 serverSocket.close();
             }
 
         } catch (IOException e) {
             log.debug(e.toString());
         }
         try {
             if (passiveModeSocket != null) {
                 passiveModeSocket.close();
             }
 
         } catch (IOException e) {
             log.debug(e.toString());
         }
     }
 
     /**
      * Opening a connection to the FTP server.
      * 
      * @param svr The server name. If null is passed, the local machine is used.
      * @param user The user name.
      * @param pass The user password.
      * @throws IOException Error on connection.
      */
     public void openConnection(String svr, String user, String pass) throws IOException {
         openConnection(svr, user, pass, TEST_FTP_PORT);
     }
 
     /**
      * Opening a connection to the FTP server.
      * 
      * @param svr The server name. If null is passed, the local machine is used.
      * @param user The user name.
      * @param pass The user password.
      * @param port FTP port.
      * @throws IOException Error on connection.
      */
     public void openConnection(String svr, String user, String pass, int port) throws IOException {
         this.server = svr;
 
        if (server == null || server.startsWith("127.0.0.")) {
            this.server = NetUtils.getMachineAddress().getHostAddress();
         }
         serverSocket = new Socket(server, port);
         in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
         out = new PrintWriter(serverSocket.getOutputStream(), true);
         getResponse();
         sendAndReceive("USER " + user);
         sendAndReceive("PASS " + pass);
     }
 
     public String openPassiveMode() throws IOException {
         sendCommand("PASV");
         String response = getResponse();
         int parentStart = response.lastIndexOf('(');
         int parentEnd = response.lastIndexOf(')');
 
         String pasv = response.substring(parentStart + 1, parentEnd);
         StringTokenizer st = new StringTokenizer(pasv, ",");
         int[] iPs = new int[8];
         for (int i = 0; st.hasMoreTokens(); i++) {
             iPs[i] = Integer.valueOf(st.nextToken()).intValue();
         }
         int port = (iPs[4] << FtpConstants.BYTE_LENGTH) + iPs[5];
 
         resetDataSockets();
         passiveModeSocket = new Socket(server, port);
         return response;
     }
 
     public String openActiveMode() throws IOException {
         InetAddress addr = NetUtils.getMachineAddress();
         String addrStr = addr.getHostAddress();
         ServerSocket sock = ServerSocketFactory.getDefault().createServerSocket(0, 1, addr);
         sock.setSoTimeout(10000);
 
         Pattern pattern = Pattern.compile("^([0-9]+)\\.([0-9]+)\\.([0-9]+)\\.([0-9]+)$");
         Matcher matcher = pattern.matcher(addrStr);
         if (!matcher.matches()) {
             throw new IOException("Invalid address: " + addrStr);
         }
         int p1 = (sock.getLocalPort() >>> FtpConstants.BYTE_LENGTH) & FtpConstants.BYTE_MASK;
         int p2 = sock.getLocalPort() & FtpConstants.BYTE_MASK;
 
         StringBuffer sb = new StringBuffer();
         sb.append("PORT ");
         sb.append(matcher.group(1));
         sb.append(",");
         sb.append(matcher.group(2));
         sb.append(",");
         sb.append(matcher.group(3));
         sb.append(",");
         sb.append(matcher.group(4));
         sb.append(",");
         sb.append(p1);
         sb.append(",");
         sb.append(p2);
 
         resetDataSockets();
         activeModeServerSocket = sock;
 
         sendCommand(sb.toString());
 
         //        
         // try {
         // passiveModeSocket = sock.accept();
         // } catch (RuntimeException e) {
         // throw new IOException("Accepting data channel failed");
         // }
 
         String response = getResponse();
 
         // if (passiveModeSocket != null) {
         // transIs = passiveModeSocket.getInputStream();
         // transOut = passiveModeSocket.getOutputStream();
         // }
 
         return response;
 
     }
 
     public String openExtendedPassiveMode() throws IOException {
         sendCommand("EPSV");
         String response = getResponse();
 
         Pattern pattern = Pattern.compile("^.*\\(\\|\\|\\|([0-9]+)\\|\\).*$");
         Matcher matcher = pattern.matcher(response);
         int port = 0;
         if (matcher.matches()) {
             port = Integer.parseInt(matcher.group(1));
         }
 
         resetDataSockets();
         passiveModeSocket = new Socket(server, port);
         return response;
     }
 
     private void initializeIOStreams() throws IOException {
         if (passiveModeSocket != null) {
             transIs = passiveModeSocket.getInputStream();
             transOut = passiveModeSocket.getOutputStream();
         } else if (activeModeServerSocket != null) {
             Socket socket = activeModeServerSocket.accept();
             transIs = socket.getInputStream();
             transOut = socket.getOutputStream();
         } else {
             throw new IOException("IO streams have not been initialized");
         }
     }
 
     private boolean isServerSocketAvailable() {
         return passiveModeSocket != null || activeModeServerSocket != null;
     }
 
     private void resetDataSockets() throws IOException {
         if (passiveModeSocket != null) {
             passiveModeSocket.close();
             passiveModeSocket = null;
         }
         if (activeModeServerSocket != null) {
             activeModeServerSocket.close();
             activeModeServerSocket = null;
         }
     }
 
     public String openExtendedActiveMode() throws IOException {
         StringBuffer params = new StringBuffer();
 
         params.append("|1|");
         InetAddress addr = NetUtils.getMachineAddress();
         ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(0, 1, addr);
         params.append(addr.getHostAddress());
         params.append("|");
         params.append(serverSocket.getLocalPort());
         params.append("|");
 
         sendCommand("EPRT " + params.toString());
         String response = getResponse();
 
         if (passiveModeSocket != null) {
             passiveModeSocket.close();
         }
         activeModeServerSocket = serverSocket;
         return response;
     }
 
     /**
      * Lists the content of the passed path.
      * 
      * @param f The path.
      * @return The content as string.
      * @throws IOException Error on data transfer.
      */
     public String list(String f) throws IOException {
         String response = null;
         if (!isServerSocketAvailable()) {
             openPassiveMode();
         }
         textBuffer = new StringBuffer();
         if (f == null) {
             sendCommand("LIST");
         } else {
             sendCommand("LIST " + f);
         }
         String res = getResponse();
 
         if (res.startsWith("150")) {
             initializeIOStreams();
         } else {
             return res;
         }
 
         BufferedReader in = new BufferedReader(new InputStreamReader(transIs, "ISO-8859-1"));
         TextReceiver l = new TextReceiver(in, false);
         synchronized (lock) {
             try {
                 new Thread(l).start();
                 lock.wait();
             } catch (InterruptedException e) {
                 log.error(e);
             }
         }
         resetDataSockets();
         response = getResponse();
         return response;
 
     }
 
     /**
      * Lists the current folder.
      * 
      * @return The content.
      * @throws IOException Error on data transfer.
      */
     public String list() throws IOException {
         return list(null);
     }
 
     /**
      * Retrieves a text file.
      * 
      * @param filename The filename.
      * @return The content of the text file.
      * @throws IOException Error on data transfer.
      */
     public String retrieveText(String filename) throws IOException {
         String response = null;
         sendAndReceive("TYPE A");
         if (!isServerSocketAvailable()) {
             openPassiveMode();
         }
         textBuffer = new StringBuffer();
         response = sendAndReceive("RETR " + filename);
         if (!response.startsWith("150")) {
             return response;
         }
         initializeIOStreams();
         BufferedReader in = new BufferedReader(new InputStreamReader(transIs, "ISO-8859-1"));
         TextReceiver l = new TextReceiver(in, false);
         synchronized (lock) {
             try {
                 new Thread(l).start();
 
                 lock.wait();
             } catch (InterruptedException e) {
                 log.error(e);
             }
         }
 
         response = getResponse();
         resetDataSockets();
         return response;
     }
 
     /**
      * Retrieves a text file.
      * 
      * @param filename The filename.
      * @return Size of file.
      * @throws IOException Error on data transfer.
      */
     public int retrieveBigText(String filename) throws IOException {
 
         String response;
         sendAndReceive("TYPE A");
         if (!isServerSocketAvailable()) {
             openPassiveMode();
         }
         textBuffer = new StringBuffer();
         response = sendAndReceive("RETR " + filename);
         if (response.startsWith("150")) {
             initializeIOStreams();
         } else {
             return 0;
         }
 
         BufferedReader in = new BufferedReader(new InputStreamReader(transIs, "ISO-8859-1"));
         TextReceiver l = new TextReceiver(in, true);
         synchronized (lock) {
             try {
                 new Thread(l).start();
                 lock.wait();
             } catch (InterruptedException e) {
                 log.error(e);
             }
         }
 
         getResponse();
         resetDataSockets();
         return l.getCount();
     }
 
     /**
      * Retrieves a raw data file.
      * 
      * @param filename The filename.
      * @return The content of the data file.
      * @throws IOException Error on data transfer.
      */
     public String retrieveRaw(String filename) throws IOException {
         String response;
         if (!isServerSocketAvailable()) {
             openPassiveMode();
         }
         rawBuffer = null;
         response = sendAndReceive("RETR " + filename);
         if (!response.startsWith("150")) {
             return response;
         }
         initializeIOStreams();
         RawReceiver l = new RawReceiver(transIs);
         new Thread(l).start();
         synchronized (lock) {
             try {
                 lock.wait();
             } catch (InterruptedException e) {
                 log.error(e);
             }
         }
 
         response = getResponse();
         resetDataSockets();
         return response;
     }
 
     /**
      * Stores a text file on the remote system.
      * 
      * @param filename The filename.
      * @param textToStore The text to be stored.
      * @return The response.
      * @throws IOException Error on data transfer.
      */
     public String storeText(String filename, String textToStore) throws IOException {
         return storeText(filename, textToStore, false);
     }
 
     /**
      * Appends text to an text file.
      * 
      * @param filename The filename.
      * @param textToStore The text to append.
      * @return The server response.
      * @throws IOException Error on data transfer.
      */
     public String appendText(String filename, String textToStore) throws IOException {
         return storeText(filename, textToStore, true);
     }
 
     private String storeText(String filename, String textToStore, boolean append) throws IOException {
         String response = null;
 
         sendAndReceive("TYPE A");
         if (!isServerSocketAvailable()) {
             openPassiveMode();
         }
 
         textBuffer = new StringBuffer();
         if (append) {
             sendCommand("APPE" + filename);
         } else {
             sendCommand("STOR " + filename);
         }
         response = getResponse();
         if (!response.startsWith("150")) {
             return response;
         }
         initializeIOStreams();
         BufferedWriter out = new BufferedWriter(new OutputStreamWriter(transOut, "ISO-8859-1"));
         TextSender l = new TextSender(out, textToStore);
         new Thread(l).start();
         synchronized (lock) {
             try {
                 lock.wait();
             } catch (InterruptedException e) {
                 log.error(e);
             }
         }
         response = getResponse();
         resetDataSockets();
         return response;
 
     }
 
     /**
      * Stores a file of a given size. The content is arbitrary.
      * 
      * @param filename Filename.
      * @param size Size of the file.
      * @return Response.
      * @throws IOException
      */
     public String storeBigText(String filename, int size) throws IOException {
         String response = null;
         // openPassiveMode();
         response = sendAndReceive("TYPE A");
         if (!isServerSocketAvailable()) {
             openPassiveMode();
         }
         textBuffer = new StringBuffer();
         sendCommand("STOR " + filename);
 
         response = getResponse();
         if (!response.startsWith("150")) {
             return response;
         }
         initializeIOStreams();
         BufferedWriter out = new BufferedWriter(new OutputStreamWriter(transOut, "ISO-8859-1"));
         TextSender l = new TextSender(out, size);
         new Thread(l).start();
         synchronized (lock) {
             try {
                 lock.wait();
             } catch (InterruptedException e) {
                 log.error(e);
             }
         }
         response = getResponse();
         resetDataSockets();
         return response;
 
     }
 
     /**
      * Stores a data file on the remote system.
      * 
      * @param filename The filename.
      * @param data The Data to be stored.
      * @return The response.
      * @throws IOException Error on data transfer.
      */
     public String storeRaw(String filename, byte[] data) throws IOException {
         return storeRaw(filename, data, false);
     }
 
     /**
      * Appends text to an data file.
      * 
      * @param filename The filename.
      * @param data The data to append.
      * @return The server response.
      * @throws IOException Error on data transfer.
      */
     public String appendRaw(String filename, byte[] data) throws IOException {
         return storeRaw(filename, data, true);
     }
 
     private String storeRaw(String filename, byte[] data, boolean append) throws IOException {
         String response = null;
         //response = sendAndReceive("TYPE I");
         if (!isServerSocketAvailable()) {
             openPassiveMode();
         }
         if (append) {
             sendCommand("APPE" + filename);
         } else {
             sendCommand("STOR " + filename);
         }
         response = getResponse();
         if (!response.startsWith("150")) {
             return response;
         }
         initializeIOStreams();
 
         BufferedOutputStream out = new BufferedOutputStream(transOut);
         RawSender l = new RawSender(out, data);
         synchronized (lock) {
             new Thread(l).start();
             try {
                 lock.wait();
             } catch (InterruptedException e) {
                 log.error(e);
             }
         }
         response = getResponse();
         resetDataSockets();
         return response;
 
     }
 
     /**
      * Sends a command string to the server.
      * 
      * @param cmd The command.
      * @return The server response.
      * @throws IOException Error on data transfer.
      */
     public String sendAndReceive(String cmd) throws IOException {
         sendCommand(cmd);
         return getResponse();
     }
 
     private String getResponse() throws IOException {
         return getResponse(in);
     }
 
     private String getResponse(BufferedReader in) throws IOException {
         StringBuffer sb = new StringBuffer();
         boolean done;
         do {
             String line = in.readLine();
             sb.append(line + "\n");
             int idx = 0;
             done = Character.isDigit(line.charAt(idx++)) && Character.isDigit(line.charAt(idx++))
                     && Character.isDigit(line.charAt(idx++)) && line.charAt(idx++) == ' ';
         } while (!done);
         return sb.toString().trim();
     }
 
     /**
      * Send command and wait for resonse.
      */
     private void sendCommand(String command) throws IOException {
         // log.info("-> " + command);
         out.println(command);
     }
 
     /**
      * Listens to server socket.
      * 
      * @author Lars Behnke
      */
     private class TextReceiver implements Runnable {
 
         private BufferedReader reader;
 
         private int            count;
 
         private boolean        countOnly;
 
         /**
          * Constructor.
          * 
          * @param reader The reader.
          * @param countOnly True, if only the text size matters.
          */
         public TextReceiver(BufferedReader reader, boolean countOnly) {
             this.countOnly = countOnly;
             this.reader = reader;
         }
 
         /**
          * {@inheritDoc}
          */
         public void run() {
             synchronized (lock) {
                 try {
                     char[] buffer = new char[4096];
                     int count;
 
                     while ((count = reader.read(buffer)) != -1) {
 
                         if (!countOnly) {
                             String logLine = new String(buffer, 0, count);
                             if (textBuffer.length() > 0) {
                                 textBuffer.append(System.getProperty("line.separator"));
                             }
                             textBuffer.append(logLine);
                             if (log.isTraceEnabled()) {
                                 if (count >= LOG_LINE_LENGTH) {
 
                                     logLine = logLine.substring(0, LOG_LINE_LENGTH) + " ["
                                             + (logLine.length() - LOG_LINE_LENGTH) + " chars more]";
                                 }
 
                                 log.trace("<==: " + logLine.trim());
                             }
                         }
                         this.count += count;
                     }
                     if (countOnly) {
                         log.trace("<==: " + this.count + " characters read");
                     }
                     reader.close();
 
                 } catch (IOException e) {
                     log.error(e);
                 }
                 lock.notifyAll();
             }
         }
 
         /**
          * @return Size of file.
          */
         public int getCount() {
             return count;
         }
     }
 
     /**
      * Sends data to server socket.
      * 
      * @author Lars Behnke
      */
     private class TextSender implements Runnable {
 
         private BufferedWriter writer;
 
         private String         textToSend;
 
         private int            textSize;
 
         /**
          * Constructor.
          * 
          * @param writer The writer.
          * @param textToSend Text to send.
          */
         public TextSender(BufferedWriter writer, String textToSend) {
             this.writer = writer;
             this.textToSend = textToSend;
             this.textSize = -1;
         }
 
         /**
          * Constructor.
          * 
          * @param writer The writer.
          * @param size Size of the test string.
          */
         public TextSender(BufferedWriter writer, int size) {
             this.writer = writer;
             this.textSize = size;
         }
 
         /**
          * @see java.lang.Runnable#run()
          */
         public void run() {
             synchronized (lock) {
                 try {
                     if (textSize == -1 && textToSend != null) {
                         if (log.isTraceEnabled()) {
                             String x;
                             if (textToSend.length() >= LOG_LINE_LENGTH) {
                                 x = textToSend.substring(0, LOG_LINE_LENGTH) + " ["
                                         + (textToSend.length() - LOG_LINE_LENGTH) + " chars more]";
                             } else {
                                 x = textToSend;
                             }
                             log.trace("==>: " + x.trim());
                         }
                         writer.write(textToSend);
                         writer.flush();
                     } else if (textSize >= 0) {
                         if (log.isTraceEnabled()) {
                             log.trace("==>: Sending test text, length: " + textSize);
                         }
                         for (int i = 0; i < textSize; i++) {
                             writer.write('X');
                         }
 
                     }
 
                     writer.close();
                 } catch (IOException e) {
                     log.error(e);
                 }
                 lock.notifyAll();
             }
         }
     }
 
     /**
      * Sends data to server socket.
      * 
      * @author Lars Behnke
      */
     private class RawSender implements Runnable {
 
         private BufferedOutputStream os;
 
         private byte[]               dataToSend;
 
         /**
          * Constructor.
          * 
          * @param os The output stream.
          * @param dataToSend Data to send.
          */
         public RawSender(BufferedOutputStream os, byte[] dataToSend) {
             this.os = os;
             this.dataToSend = dataToSend;
         }
 
         /**
          * @see java.lang.Runnable#run()
          */
         public void run() {
             synchronized (lock) {
                 try {
                     log.trace("==>: " + dataToSend.length + " bytes");
                     os.write(dataToSend);
                     os.flush();
                     os.close();
                 } catch (IOException e) {
                     log.error(e);
                 } finally {
                     lock.notifyAll();
                 }
             }
         }
     }
 
     /**
      * Raw data receiver that listens to server socket.
      * 
      * @author Lars Behnke
      */
     private class RawReceiver implements Runnable {
 
         private InputStream is;
 
         /**
          * Constructor.
          * 
          * @param is The input stream.
          */
         public RawReceiver(InputStream is) {
             this.is = is;
         }
 
         /**
          * {@inheritDoc}
          */
         public void run() {
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             byte[] buffer = new byte[1024];
             int count = 0;
             synchronized (lock) {
                 try {
 
                     while ((count = is.read(buffer)) >= 0) {
                         log.trace("<==: " + count + " bytes");
                         baos.write(buffer, 0, count);
                     }
                     rawBuffer = baos.toByteArray();
                 } catch (IOException e) {
                     log.error(e);
                 } finally {
                     lock.notifyAll();
                 }
             }
         }
     }
 
 }
 
 // CHECKSTYLE:ON
