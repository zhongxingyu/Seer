 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.abada.web.util;
 
 /*
  * #%L
  * Abada Commons
  * %%
  * Copyright (C) 2013 Abada Servicios Desarrollo (investigacion@abadasoft.com)
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as
  * published by the Free Software Foundation, either version 3 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/gpl-3.0.html>.
  * #L%
  */
 
 import com.abada.utils.Constants;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 import javax.servlet.http.HttpServletRequest;
 import org.springframework.http.HttpMethod;
 
 /**
  * Utilities for Controllers and URLs
  * @author katsu
  * @author david
  */
 public class URL {
     /**
      * Return a map with all parameters in the httpServletRequest
      * @param request
      * @return 
      */
     public static  Map<String,Object> parseRequest(HttpServletRequest request){
         Map<String,Object> result=new HashMap<String, Object>();
         String name;
        String [] aux;
         Enumeration enu=request.getParameterNames();
         while (enu.hasMoreElements()){
             name=(String) enu.nextElement();
            aux=request.getParameterValues(name);
            if (aux.length==1)
                result.put(name, aux[0]);
            else
                result.put(name, aux);
         }
         return result;
     }
     
     private static final int LONG_BUFFER = 1024;
 
     /**
      * Return a get parameters to append with an url
      * @param map
      * @return 
      */
     public static String paramUrlGrid(Map<String, Object> params,HttpMethod method) throws UnsupportedEncodingException {
         StringBuilder param = new StringBuilder(LONG_BUFFER);
         boolean thisquestion = false;
         StringBuilder va;
         for (String key : params.keySet()) {
             if (params.get(key) != null) {
                 va = new StringBuilder();
                 if (!thisquestion) {
                     if (method==null || method==HttpMethod.GET)
                         va.append(Constants.QUESTION_MARK);
                     thisquestion = true;
                 } else {
                     va.append(Constants.AMPERSAND);
                 }
                 param.append(va);
                 param.append(key).append(Constants.EQUALS_SIGN).append(URLEncoder.encode(params.get(key).toString(), Constants.UTF8));
             }
         }
 
         return param.toString();                
     }
     
     /**
      * Return a get parameters to append with an url
      * @param map
      * @return 
      */
     public static String paramUrlGrid(Map<String, Object> params) throws UnsupportedEncodingException {
         return paramUrlGrid(params, HttpMethod.GET);
     }
 }
