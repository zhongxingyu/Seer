 /*
  * The MIT License
  *
  * Copyright 2013 Gravidence.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.gravidence.gravifon.filter;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import java.io.IOException;
 import javax.ws.rs.container.ContainerRequestContext;
 import javax.ws.rs.container.ContainerResponseContext;
 import javax.ws.rs.container.ContainerResponseFilter;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.ext.Provider;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 /**
  * Service log response filter.<p>
  * Logs HTTP status code, reason phrase and entity if presented.<p>
  * Consumes <code>application/json</code> media type entities only.
  * 
  * @author Maksim Liauchuk <maksim_liauchuk@fastmail.fm>
  */
 @Provider
 public class LogServiceResponseFilter implements ContainerResponseFilter {
     
     private static final Logger LOGGER = LoggerFactory.getLogger(LogServiceResponseFilter.class);
     
     /**
      * JSON data binding instance.
      */
     @Autowired
     private ObjectMapper objectMapper;
 
     @Override
     public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
         if (LOGGER.isDebugEnabled()) {
             String entity = FilterUtils.NO_ENTITY;
             
             if (responseContext.hasEntity()) {
                if (MediaType.APPLICATION_JSON_TYPE.isCompatible(responseContext.getMediaType())) {
                     entity = FilterUtils.jsonEntityToString(objectMapper, responseContext.getEntity(), LOGGER);
                 } else {
                     entity = FilterUtils.unsupportedEntityToString(responseContext.getMediaType());
                 }
             }
             
             LOGGER.debug("Service response:\n[{}] {}\n{}", responseContext.getStatus(),
                     responseContext.getStatusInfo().getReasonPhrase(), entity);
         }
     }
     
 }
