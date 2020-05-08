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
 
 import static org.sonatype.sisu.siesta.common.SiestaMediaType.VND_NEXUS_VALIDATION_ERRORS_V1_JSON_TYPE;
 import static org.sonatype.sisu.siesta.common.SiestaMediaType.VND_NEXUS_VALIDATION_ERRORS_V1_XML_TYPE;
 
 import java.util.List;
 import javax.ws.rs.core.GenericEntity;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Variant;
 
 import org.sonatype.sisu.siesta.common.exceptions.ValidationErrorXO;
 
 /**
  * Support calls for exception mappers returning 400 with {@link ValidationErrorXO}s in body.
  *
  * @since 1.4
  */
 public abstract class ValidationErrorsExceptionMappersSupport<E extends Throwable>
     extends ExceptionMapperSupport<E>
 {
 
    private final List<Variant> variants;
 
     public ValidationErrorsExceptionMappersSupport()
     {
        variants = Variant.mediaTypes(
             VND_NEXUS_VALIDATION_ERRORS_V1_JSON_TYPE, VND_NEXUS_VALIDATION_ERRORS_V1_XML_TYPE
         ).add().build();
     }
 
     @Override
     protected Response convert( final E exception, final String id )
     {
        final Variant variant = getRequest().selectVariant( variants );

         final Response.ResponseBuilder builder = Response.status( getStatusCode( exception ) );
 
        if ( variant != null )
         {
             builder
                .type( variant.getMediaType() )
                 .entity(
                     new GenericEntity<List<ValidationErrorXO>>( getValidationErrors( exception ) )
                     {
                         @Override
                         public String toString()
                         {
                             return getEntity().toString();
                         }
                     }
                 );
         }
 
         return builder.build();
     }
 
     protected int getStatusCode( final E exception )
     {
         return Response.Status.BAD_REQUEST.getStatusCode();
     }
 
     protected abstract List<ValidationErrorXO> getValidationErrors( final E exception );
 
 }
