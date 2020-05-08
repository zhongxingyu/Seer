 /*
  * Copyright 2002
  * Robert Forsman <thoth@purplefrog.com>
  */
 package com.purplefrog.flea2flea;
 
 import java.io.*;
 import java.util.*;
 import java.util.regex.*;
 
 import com.purplefrog.apachehttpcliches.*;
import com.purplefrog.httpcliches.*;
 import org.apache.http.*;
 import org.apache.http.entity.*;
 import org.apache.http.message.*;
 import org.apache.http.protocol.*;
 
 
 public class OutboundHandler
     implements HttpRequestHandler
 {
     protected Map offerings;
 
     protected Logger logger;
     public static final Pattern byteRangePattern = Pattern.compile("bytes=(\\d+)?-(\\d+)?");
 
 
     public OutboundHandler(Map offerings, Logger logger)
     {
         this.logger = logger;
         this.offerings = offerings;
     }
 
     public void handle(HttpRequest request, HttpResponse response, HttpContext httpContext)
     {
         String pathInContext = request.getRequestLine().getUri();
 
        String tag = HTMLTools.decodePercent(pathInContext);
        Offering of = (Offering) offerings.get(tag);
 
         EntityAndHeaders rval;
         {
             if (null == of) {
                 rval = EntityAndHeaders.plainPayload(404, "not found", "text/plain");
             } else {
                 String remoteAddr = ApacheHTTPCliches.remoteAddress(httpContext).getRemoteAddress().getHostAddress();
                 String method = request.getRequestLine().getMethod();
 
                 rval = computeResult(request, pathInContext, of, remoteAddr, method);
             }
         }
         rval.apply(response);
     }
 
     private EntityAndHeaders computeResult(HttpRequest request, String pathInContext, Offering of, String remoteAddr, String method)
     {
         if ( ! (isGet(method) || isHead(method)) ) {
 
             String payload = "This URL only supports GET and HEAD requests.\n";
             return new EntityAndHeaders(405, ApacheHTTPCliches.boringStringEntity(payload),
                 new BasicHeader("Allow", "GET, HEAD"));
 
         }
 
         logger.logOutbound(method, remoteAddr, pathInContext, of.resource);
 
         Header range_ = request.getFirstHeader("Range");
         String rangeHeader = range_==null ? null : range_.getValue();
         if (rangeHeader== null) {
             return new EntityAndHeaders(200, new FileEntity(of.resource, "application/binary"), DirectoryApacheHandler.ACCEPT_RANGES_BYTES);
         } else {
             return DirectoryApacheHandler.handleSubset(of.resource, "application/binary", rangeHeader, null);
         }
     }
 
     private static boolean isHead(String method)
     {
 	return "HEAD".equalsIgnoreCase(method);
     }
 
     private static boolean isGet(String method)
     {
 	return "GET".equalsIgnoreCase(method);
     }
 
 
 }
