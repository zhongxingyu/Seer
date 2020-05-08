 package com.psddev.dari.util;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpServletResponseWrapper;
 
 public class HeaderResponse extends HttpServletResponseWrapper {
 
     private final Map<String, Collection<String>> headers = new LinkedHashMap<String, Collection<String>>();
 
     public HeaderResponse(HttpServletResponse response) {
         super(response);
     }
 
     @Override
     public void addHeader(String name, String value) {
         Collection<String> values = headers.get(name);
 
         if (values == null) {
             values = new ArrayList<String>();
             headers.put(name, values);
         }
 
         values.add(value);
 
         super.addHeader(name, value);
     }
 
     @Override
     public void setHeader(String name, String value) {
         Collection<String> values = new ArrayList<String>();
 
         headers.put(name, values);
         values.add(value);
 
         super.setHeader(name, value);
     }
 
    @Override
    public void setResponse(ServletResponse response) {
        // Don't allow the delegate change to prevent Tomcat from following
        // SRV.8.3 in the Java servlet specification.
    }

     public String getHeader(String name) {
         Collection<String> values = headers.get(name);
 
         if (values != null) {
             for (String value : values) {
                 return value;
             }
         }
 
         return null;
     }
 
     public Collection<String> getHeaderNames() {
         return new ArrayList<String>(headers.keySet());
     }
 
     public Collection<String> getHeaders(String name) {
         Collection<String> copy = new ArrayList<String>();
         Collection<String> values = headers.get(name);
 
         if (values != null) {
             copy.addAll(values);
         }
 
         return copy;
     }
 }
