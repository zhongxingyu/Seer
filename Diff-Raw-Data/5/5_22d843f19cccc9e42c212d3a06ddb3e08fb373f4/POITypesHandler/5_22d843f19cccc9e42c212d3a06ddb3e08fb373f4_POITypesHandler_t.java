 package eo.frontend.httpserver;
 
 import java.lang.*;
 import java.util.*;
 import java.io.*;
 
 import javax.servlet.http.*;
 import javax.servlet.*;
 
 import org.eclipse.jetty.server.*;
 import org.eclipse.jetty.server.handler.*;
 
 import com.thoughtworks.xstream.XStream;
 
 // ================================================================================
 
 public class POITypesHandler implements DynamicHandler {
     private static class Type {
         public String name;
     }
 
 	private static class UserConstructor {
         Type[] types;
         int sid;
 
         UserConstructor() {
             try {
                 String[] type_names = Searcher.queryTypes();
                 types = new Type[type_names.length];
                 for (int i = 0; i < type_names.length; ++i) {
                     types[i] = new Type();
                     types[i].name = type_names[i];
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
 
     public Response handle(final Request request) {
         try {
             Response r = new Response();
             UserConstructor uc = new UserConstructor();
             if ((request.getParameterValues("sid") != null) && (!request.getParameterValues("sid")[0].equals(""))) {
                 int sid = Integer.parseInt(request.getParameterValues("sid")[0]);
                 uc.sid = sid;
                 r.result = uc;
                 r.aliases.put("user-constructor", UserConstructor.class);
                 r.aliases.put("sid", int.class);
                 r.aliases.put("poi-types", Type[].class);
                 r.aliases.put("type", Type.class);
                 r.aliases.put("value", String.class);
             } else {
                 Type[] t = uc.types;
                 r.result = t;
                 r.aliases.put("poi-types", Type[].class);
                 r.aliases.put("type", Type.class);
                 r.aliases.put("value", String.class);
             }

            /*
             String[] type_names = Searcher.queryTypes();
             Type[] t = new Type[type_names.length];
             for (int i = 0; i < type_names.length; ++i) {
                 t[i] = new Type();
                 t[i].name = type_names[i];
             }
             
 
             r.result = t;
             r.aliases.put("poi-types", Type[].class);
             r.aliases.put("type", Type.class);
             r.aliases.put("value", String.class);
            */
 
             return r;
         } catch (Exception e) {
             e.printStackTrace();
             return new Response();
         }
     }
 }
