 /*
  * Copyright 2011 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.jayway.jaxrs.hateoas.core.jersey;
 
 import com.jayway.jaxrs.hateoas.web.RequestContext;
 import com.sun.jersey.spi.container.ContainerRequest;
 import com.sun.jersey.spi.container.ContainerRequestFilter;
 import com.sun.jersey.spi.container.ContainerResponse;
 import com.sun.jersey.spi.container.ContainerResponseFilter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.ws.rs.core.UriBuilder;
 
 /**
  * @author Mattias Hellborg Arthursson
  * @author Kalle Stenflo
  */
 public class JerseyHateoasContextFilter implements ContainerRequestFilter, ContainerResponseFilter {
 
     private static final Logger log = LoggerFactory.getLogger(JerseyHateoasContextFilter.class);
 
     @Override
     public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
 
         log.debug("container response filter");
 
        RequestContext.clearRequestContext();

         return response;
     }
 
     @Override
     public ContainerRequest filter(final ContainerRequest request) {
 
         log.debug("container request filter");
         log.debug("request.getAbsolutePath : " + request.getAbsolutePath());
         log.debug("request.getBaseUri : " + request.getBaseUri());
 
         RequestContext ctx = new RequestContext(UriBuilder.fromUri(request.getBaseUri()), request.getHeaderValue(RequestContext.HATEOAS_OPTIONS_HEADER));
 
         RequestContext.setRequestContext(ctx);
 
         return request;
     }
 }
