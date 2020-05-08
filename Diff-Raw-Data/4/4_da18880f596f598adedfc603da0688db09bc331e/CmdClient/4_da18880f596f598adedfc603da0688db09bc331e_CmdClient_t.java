 package com.razie.sdk.agent.webservice;
 
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 
import razie.assets.AssetLocation;

 import com.razie.agent.webservice.RazClientSocket;
 import com.razie.pub.base.log.Log;
 
 /**
  * helper to execute commands on a server
  * 
  * @author razvanc
  * @deprecated - should use ExecTelnet or ActionToInvoke etc
  */
 public class CmdClient {
     private AssetLocation             target;
     private String             hostname;
     private int                port;
     private String             lastResult   = "";
     public CmdClient(AssetLocation target) {
         this.target = target;
         if (!target.isRemote()) {
             throw new IllegalArgumentException("target must be remote");
         }
 
         hostname = target.getHost();
         port = Integer.parseInt(target.getPort());
     }
 
     public String execute(String cmd, String args) {
         try {
             RazClientSocket server = new RazClientSocket(hostname, port);
             server.setSoTimeout(30000);
 
             DataInputStream in = new DataInputStream(server.getInputStream());
             PrintStream out = new PrintStream(server.getOutputStream());
 
             out.println(cmd + " " + args);
             int brakets = 0;
             lastResult = "";
 
             // read until the end
             do {
                 String l = in.readLine();
 
                 if (l == null) {
                     lastResult += "NULL";
                     break;
                 } else if (l.equals("{ENDS}")) {
                     brakets = 0;
                 } else if (l.endsWith("{")) {
                     lastResult += lastResult.length() <= 0 ? l : "\n" + l;
                     brakets++;
                 } else if (l.endsWith("}")) {
                     lastResult += lastResult.length() <= 0 ? l : "\n" + l;
                     brakets--;
                 } else {
                     lastResult += lastResult.length() <= 0 ? l : "\n" + l;
                 }
             } while (brakets > 0);
 
             logger.log("REPLY <" + this.target + "> : " + lastResult);
 
         } catch (IOException ioe) {
             logger.log("IOException on socket listen: " + ioe);
             ioe.printStackTrace();
             return "ERROR";
         }
         return lastResult;
     }
 
     static final Log logger = Log.Factory.create(CmdClient.class.getName());
 }
