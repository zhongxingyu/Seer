 /*
  * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
  *
  * This program is licensed to you under the Apache License Version 2.0,
  * and you may not use this file except in compliance with the Apache License Version 2.0.
  * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the Apache License Version 2.0 is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
  */
 package org.sonatype.sisu.siesta.server.internal.mappers;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.inject.Provider;
 import javax.inject.Singleton;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import org.sonatype.sisu.siesta.common.error.ErrorXO;
 import org.sonatype.sisu.siesta.server.ErrorExceptionMapperSupport;
 
 /**
  * Maps {@link WebApplicationException} to {@link WebApplicationException#getResponse()} status with a
  * {@link ErrorXO} body.
  *
  * @since 1.4
  */
 @Named
 @Singleton
 public class WebApplicationExceptionMapper
     extends ErrorExceptionMapperSupport<WebApplicationException>
 {
 
     @Inject
     private Provider<UriInfo> uriInfo;
 
     @Override
     protected int getStatusCode( final WebApplicationException exception )
     {
         return exception.getResponse().getStatus();
     }
 
     @Override
     protected String getMessage( final WebApplicationException exception )
     {
         if ( Response.Status.NOT_FOUND.getStatusCode() == exception.getResponse().getStatus() )
         {
             return "No resource available at '" + uriInfo.get().getPath() + "'";
         }
 
         final String message = super.getMessage( exception );
         if ( message == null )
         {
             final Response.Status status = Response.Status.fromStatusCode( exception.getResponse().getStatus() );
             if ( status != null )
             {
                 return status.getStatusCode() + " " + status.getReasonPhrase();
             }
         }
 
         return message;
     }
 }
