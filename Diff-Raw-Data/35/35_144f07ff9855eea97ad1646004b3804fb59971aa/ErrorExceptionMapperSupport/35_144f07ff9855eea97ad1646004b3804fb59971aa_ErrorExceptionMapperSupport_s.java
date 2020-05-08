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
 package org.sonatype.sisu.siesta.server;
 
 import static org.sonatype.sisu.siesta.common.SiestaMediaType.VND_NEXUS_ERROR_V1_JSON_TYPE;
 import static org.sonatype.sisu.siesta.common.SiestaMediaType.VND_NEXUS_ERROR_V1_XML_TYPE;
 
 import java.util.List;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Variant;
 import javax.ws.rs.ext.ExceptionMapper;
 
 import org.sonatype.sisu.siesta.common.exceptions.ErrorXO;
 
 /**
  * Support for {@link ExceptionMapper} implementations.
  *
  * @since 1.0
  */
 public abstract class ErrorExceptionMapperSupport<E extends Throwable>
     extends ExceptionMapperSupport<E>
 {
 
    private final List<Variant> variants;
 
     public ErrorExceptionMapperSupport()
     {
        variants = Variant.mediaTypes(
             VND_NEXUS_ERROR_V1_JSON_TYPE, VND_NEXUS_ERROR_V1_XML_TYPE
         ).add().build();
     }
 
     protected Response convert( final E exception, final String id )
     {
        final Variant variant = getRequest().selectVariant( variants );

         final Response.ResponseBuilder builder = Response.status( getStatusCode( exception ) );
 
        if ( variant != null )
         {
             builder
                .type( variant.getMediaType() )
                 .entity( new ErrorXO().withId( id ).withMessage( getMessage( exception ) ) );
         }
 
         return builder.build();
     }
 
     protected String getMessage( final E exception )
     {
         return exception.getMessage();
     }
 
     protected int getStatusCode( final E exception )
     {
         return Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
     }
 
 }
