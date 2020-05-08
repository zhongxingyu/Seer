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
 package org.sonatype.sisu.siesta.testsuite;
 
 import static com.sun.jersey.api.client.ClientResponse.Status;
 import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
 import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.notNullValue;
 import static org.sonatype.sisu.siesta.common.SiestaMediaType.VND_ERROR_V1_JSON_TYPE;
 import static org.sonatype.sisu.siesta.common.SiestaMediaType.VND_ERROR_V1_XML_TYPE;
 
 import javax.ws.rs.core.MediaType;
 
 import org.junit.Test;
 import org.sonatype.sisu.siesta.common.error.ErrorXO;
 import org.sonatype.sisu.siesta.testsuite.support.SiestaTestSupport;
 import com.sun.jersey.api.client.ClientResponse;
 
 /**
  * Tests related to media type/body returned in case of divers server errors.
  *
  * @since 1.4
  */
 public class ErrorsTest
     extends SiestaTestSupport
 {
 
     @Test
     public void throwObjectNotFoundException_XML()
         throws Exception
     {
         throwException(
             "ObjectNotFoundException", Status.NOT_FOUND, APPLICATION_XML_TYPE, VND_ERROR_V1_XML_TYPE
         );
     }
 
     @Test
     public void throwObjectNotFoundException_JSON()
         throws Exception
     {
         throwException(
             "ObjectNotFoundException", Status.NOT_FOUND, APPLICATION_JSON_TYPE, VND_ERROR_V1_JSON_TYPE
         );
     }
 
     @Test
     public void throwBadRequestException_XML()
         throws Exception
     {
         throwException(
             "BadRequestException", Status.BAD_REQUEST, APPLICATION_XML_TYPE, VND_ERROR_V1_XML_TYPE
         );
     }
 
     @Test
     public void throwBadRequestException_JSON()
         throws Exception
     {
         throwException(
             "BadRequestException", Status.BAD_REQUEST, APPLICATION_JSON_TYPE, VND_ERROR_V1_JSON_TYPE
         );
     }
 
     @Test
     public void inexistentUri()
         throws Exception
     {
         final ClientResponse response = client().resource( url( "some/unknown/path" ) )
             .accept( APPLICATION_JSON_TYPE, VND_ERROR_V1_JSON_TYPE )
             .get( ClientResponse.class );
 
         assertThat( response.getClientResponseStatus(), is( equalTo( Status.NOT_FOUND ) ) );
         assertThat( response.getType(), is( equalTo( VND_ERROR_V1_JSON_TYPE ) ) );
 
         final ErrorXO error = response.getEntity( ErrorXO.class );
         assertThat( error, is( notNullValue() ) );
         assertThat( error.getId(), is( notNullValue() ) );
         assertThat( error.getMessage(), is( "No resource available at 'some/unknown/path'" ) );
     }
 
    @Test
    public void methodNotAllowed405()
        throws Exception
    {
        final ClientResponse response = client().resource( url( "errors" ) )
            .accept( APPLICATION_JSON_TYPE, VND_ERROR_V1_JSON_TYPE )
            .delete( ClientResponse.class );

        assertThat( response.getClientResponseStatus(), is( equalTo( Status.METHOD_NOT_ALLOWED ) ) );
        assertThat( response.getType(), is( equalTo( VND_ERROR_V1_JSON_TYPE ) ) );

        final ErrorXO error = response.getEntity( ErrorXO.class );
        assertThat( error, is( notNullValue() ) );
        assertThat( error.getId(), is( notNullValue() ) );
        assertThat( error.getMessage(), is( "DELETE method not allowed on resource 'errors'" ) );
    }

     public void throwException( final String exceptionType, final Status expectedStatus, final MediaType... mediaTypes )
         throws Exception
     {
         final ClientResponse response = client().resource( url( "errors/" + exceptionType ) )
             .type( mediaTypes[0] )
             .accept( mediaTypes )
             .get( ClientResponse.class );
 
         assertThat( response.getClientResponseStatus(), is( equalTo( expectedStatus ) ) );
         assertThat( response.getType(), is( equalTo( mediaTypes[1] ) ) );
 
         final ErrorXO error = response.getEntity( ErrorXO.class );
         assertThat( error, is( notNullValue() ) );
         assertThat( error.getId(), is( notNullValue() ) );
         assertThat( error.getMessage(), is( exceptionType ) );
     }
 
 }
