 // Copyright 2007, 2008, 2009 The Apache Software Foundation
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //     http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 
 package com.bluetangstudio.shared.jersey.services;
 
 import java.io.IOException;
 
 import javax.servlet.FilterChain;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.tapestry5.services.HttpServletRequestFilter;
 import org.apache.tapestry5.services.HttpServletRequestHandler;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.sun.jersey.spi.container.servlet.ServletContainer;
 
 /**
  * HttpServletRequestFilter that passes requests with a predefined prefix 
  * (see {@link JerseySymbols#REQUEST_PATH_PREFIX) to Jersey container.
  * 
  */
 public class JerseyRequestFilter implements HttpServletRequestFilter {
 
     private static final Logger LOG = LoggerFactory.getLogger(JerseyRequestFilter.class);
 
     private static final FilterChain END_OF_CHAIN = new EndOfChainFilerChain();
 
     private ServletContainer _jaxwsContainer;
 
     private String _pathPrefix;
 
     public JerseyRequestFilter(String pathPrefix, ServletContainer container) {
         _pathPrefix = pathPrefix;
         _jaxwsContainer = container;
 
     }
 
     @Override
     public boolean service(HttpServletRequest request, HttpServletResponse response, HttpServletRequestHandler handler)
             throws IOException {
 
        if (!request.getServletPath().startsWith(_pathPrefix)) {
             return handler.service(request, response);
         }
 
         try {
             _jaxwsContainer.doFilter(request, response, END_OF_CHAIN);
             return true;
         }
         catch (ServletException e) {
             LOG.info("{}", e);
             return false;
         }
     }
 
     private static final class EndOfChainFilerChain implements FilterChain {
 
         @Override
         public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
 
         }
     }
 
 }
