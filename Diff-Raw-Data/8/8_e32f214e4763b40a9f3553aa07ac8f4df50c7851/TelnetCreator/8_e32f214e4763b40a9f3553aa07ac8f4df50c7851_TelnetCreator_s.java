 package com.tngtech.internal.telnet;
 
 import com.tngtech.internal.plug.PlugConfig;
 import com.tngtech.internal.plugclient.NetioPlugClient;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.Scanner;
 
 public class TelnetCreator {
     public AsynchronousTelnetClient getAsynchronousTelnetClient(PlugConfig config) {
         return new AsynchronousTelnetClient(this, config);
     }
 
     public SynchronousTelnetClient getSynchronousTelnetClient(PlugConfig config) {
         return new SynchronousTelnetClient(getAsynchronousTelnetClient(config));
     }
 
     public NetioPlugClient getNetIoPlugClient(PlugConfig config) {
         return new NetioPlugClient(getSynchronousTelnetClient(config), config);
     }
 
     public Thread getThread(Runnable runnable) {
         return new Thread(runnable);
     }
 
     public Socket getSocket(String hostName, int port) {
         try {
             return new Socket(hostName, port);
         } catch (IOException e) {
             throw new IllegalStateException(e);
         }
     }
 
     public Scanner getSocketScanner(Socket socket) {
         return new Scanner(getSocketInputStream(socket));
     }
 
     public InputStream getSocketInputStream(Socket socket) {
         try {
             return socket.getInputStream();
         } catch (IOException e) {
             throw new IllegalStateException(e);
         }
     }
 
     public PrintWriter getSocketWriter(Socket socket) {
        return new PrintWriter(getSocketOutputStream(socket));
     }
 
     public OutputStream getSocketOutputStream(Socket socket) {
         try {
             return socket.getOutputStream();
         } catch (IOException e) {
             throw new IllegalStateException(e);
         }
     }
 }
