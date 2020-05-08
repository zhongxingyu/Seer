 /**
  * Copyright (C) 2012 - 101loops.com <dev@101loops.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.crashnote.servlet.collect;
 
 import com.crashnote.core.model.data.DataArray;
 import com.crashnote.core.model.data.DataObject;
 import com.crashnote.servlet.config.ServletConfig;
 import com.crashnote.web.collect.RequestCollector;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.Collections;
 import java.util.Enumeration;
 
 /**
  * Collector to transform a HTTP request into a structured data format.
  */
 public class ServletRequestCollector
     extends RequestCollector<HttpServletRequest> {
 
     // SETUP ======================================================================================
 
     public ServletRequestCollector(final ServletConfig config) {
         super(config);
     }
 
 
     // SHARED =====================================================================================
 
     @Override
     protected DataObject collectReqBase(final HttpServletRequest req) {
         final DataObject data = createDataObj();
         {
             data.put("method", req.getMethod());
             data.put("url", req.getRequestURL().toString());
             addIP(data, req.getRemoteAddr());
         }
         return data;
     }
 
     @Override
     protected DataObject collectReqHeader(final HttpServletRequest req) {
         final DataObject data = createDataObj();
         {
             final Enumeration<?> names = req.getHeaderNames();
             while (names.hasMoreElements()) {
                 final String name = (String) names.nextElement();
                final Enumeration<?> header = req.getHeaders(name);
                 addHeader(data, name, header);
             }
         }
         return data;
     }
 
     @Override
     protected DataObject collectReqParams(final HttpServletRequest req) {
         final DataObject data = createDataObj();
         {
             final Enumeration<?> names = req.getParameterNames();
             while (names.hasMoreElements()) {
                 final String name = (String) names.nextElement();
                 final String[] values = req.getParameterValues(name);
                 if (values != null) {
                     if (values.length == 1) {
                         addParam(data, name, values[0]);
                     } else {
                         addParam(data, name, values);
                     }
                 }
             }
         }
         return data;
     }
 
 
     // INTERNAL ===================================================================================
 
     protected DataArray createDataArr(final Enumeration<?> values) {
         final DataArray arr = createDataArr();
         {
             while (values.hasMoreElements()) {
                 final Object val = values.nextElement();
                 arr.add(val);
             }
         }
         return arr;
     }
 
     protected void addHeader(final DataObject data, final String name, final Enumeration<?> values) {
         final DataArray arr = createDataArr(values);
         final int size = arr.size();
         if (size > 0) {
             if (size == 1)
                 data.put(name, arr.get(0));
             else
                 data.put(name, arr);
         }
     }
 }
