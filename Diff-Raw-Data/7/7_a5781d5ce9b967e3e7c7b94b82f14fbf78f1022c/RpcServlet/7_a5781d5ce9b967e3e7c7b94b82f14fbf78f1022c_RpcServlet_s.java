 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.jposse;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import java.io.IOException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.slf4j.LoggerFactory;
 
 
 /**
  *
  * @author jposse
  */
 class JsonReq {
     String[] params;
     String id;
     String method;
 }
 
 
 class JsonResp {
     String id;
    String result;
     String error;
 }
 
 public class RpcServlet extends HttpServlet {
 
    public static final org.slf4j.Logger log = LoggerFactory.getLogger("com.jposse.RpcServlet");
         
         @Override
         protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
             response.setContentType("application/json");
             Gson gson = new GsonBuilder().serializeNulls().create();
             StringBuilder sb = new StringBuilder();
             String retval;
             while ((retval = request.getReader().readLine())!=null) {
                 sb.append(retval);
             }
             log.debug("HttpServletRequest is {}", sb.toString());
             JsonReq jreq = gson.fromJson(sb.toString(), JsonReq.class);
             if (jreq.method.equalsIgnoreCase("getaddress")) {
                 JsonResp jresp = new JsonResp();
                 jresp.result = WalletService.getAddress();
                 jresp.id = jreq.id;
                 jresp.error=null;
                 log.info("getaddress returning via HttpServletResponse: {}", gson.toJson(jresp));
                 response.getWriter().write(gson.toJson(jresp));
             } else if (jreq.method.equalsIgnoreCase("listaddresses")) {
                 JsonResp jresp = new JsonResp();
                jresp.result = gson.toJson(WalletService.listAddresses());
                 jresp.id = jreq.id;
                 log.info("listaddresses returning via HttpServletResponse: {}", gson.toJson(jresp));
                 response.getWriter().write(gson.toJson(jresp));
             } else {
                 log.info("Unknown method called: {}", jreq.method);
                 JsonResp jresp = new JsonResp();
                 jresp.id = jreq.id;
                 jresp.result = "";
                 jresp.error = "Unknown Method";
                 response.getWriter().write(gson.toJson(jresp));
             }
         }
 }
