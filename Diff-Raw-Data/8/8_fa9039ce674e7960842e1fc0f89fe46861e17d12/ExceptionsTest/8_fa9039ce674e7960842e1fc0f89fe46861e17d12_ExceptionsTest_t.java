 /**
  * Copyright 2012 Terremark Worldwide Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.terremark.exception;
 
 import java.util.Date;
 
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.junit.Test;
 
 import com.terremark.AbstractTestBase;
 import com.terremark.TerremarkFactory;
 import com.terremark.annotations.Order;
 import com.terremark.config.ContentType;
 import com.terremark.config.PropertiesBuilder;
 import com.terremark.config.Version;
 import com.terremark.impl.TerremarkClientImpl;
 import com.terremark.impl.VersionAndDateProvider;
 
 /**
  * TODO
  *
  * @author <a href="mailto:spasam@terremark.com">Seshu Pasam</a>
  */
 public class ExceptionsTest extends AbstractTestBase {
     /**
      * TODO
      *
      * @throws Exception
      */
     @Order(1)
     @Test(expected = AuthenticationDeniedException.class)
     public void testAuthenticationDenied() throws Exception {
         final PropertiesBuilder props = new PropertiesBuilder().setEndPoint(ENDPOINT_URL).setAccessKey("Invalid key")
                         .setAPIVersion(VERSION).setContentType(CONTENT_TYPE).setPrivateKey("Invalid private key")
                         .setHttpClient(new DefaultHttpClient());
         client = TerremarkFactory.getClient(props);
 
         getOrganizations();
     }
 
     /**
      * TODO
      *
      * @throws Exception
      */
     @Order(2)
     @Test(expected = InvalidRequestException.class)
     public void testClockSkew() throws Exception {
         final PropertiesBuilder props = new PropertiesBuilder().setEndPoint(ENDPOINT_URL).setAccessKey(ACCESS_KEY)
                         .setAPIVersion(VERSION).setContentType(CONTENT_TYPE).setPrivateKey(PRIVATE_KEY)
                         .setHttpClient(new DefaultHttpClient());
         client = TerremarkFactory.getClient(props);
 
         // Get the version and date provider and intentionally skew the clock by 10 mins
         VersionAndDateProvider vdp = ((TerremarkClientImpl) client).getVersionAndDateProvider();
         vdp.calculateClockSkew(new Date(System.currentTimeMillis() - 10 * 60 * 1000L));
 
         getOrganizations();
     }
 
     /**
      * TODO
      *
      * @throws Exception
      */
     @Order(3)
     @Test(expected = InvalidRequestException.class)
     public void testInvalidVersion() throws Exception {
         final PropertiesBuilder props = new PropertiesBuilder().setEndPoint(ENDPOINT_URL).setAccessKey(ACCESS_KEY)
                         .setAPIVersion(VERSION).setContentType(CONTENT_TYPE).setPrivateKey(PRIVATE_KEY)
                         .setHttpClient(new DefaultHttpClient());
         client = TerremarkFactory.getClient(props);
 
         // Get the version and date provider and intentionally set the version to invalid value
         VersionAndDateProvider vdp = ((TerremarkClientImpl) client).getVersionAndDateProvider();
         vdp.setVersion("Invalid version");
 
         getOrganizations();
     }
 
     /**
      * TODO
      *
      * @throws Exception
      */
     @Order(4)
     @Test(expected = NotFoundException.class)
     public void testObjectNotFound() throws Exception {
         final PropertiesBuilder props = new PropertiesBuilder().setEndPoint(ENDPOINT_URL).setAccessKey(ACCESS_KEY)
                         .setAPIVersion(VERSION).setContentType(CONTENT_TYPE).setPrivateKey(PRIVATE_KEY)
                         .setHttpClient(new DefaultHttpClient());
         client = TerremarkFactory.getClient(props);
 
         client.getOrganizationHandler().getOrganizationByID("0");
     }
 
     /**
      * This is not a valid test. Since we are using JSON and expecting a runtime exception. Ideally, this test should
      * pass, but currently failing because JSON is not enabled in 2.12 environment.
      *
      * @throws Exception
      */
     @Order(5)
    @Test(expected = TerremarkException.class)
     public void testJson() throws Exception {
         final PropertiesBuilder props = new PropertiesBuilder().setEndPoint(ENDPOINT_URL).setAccessKey(ACCESS_KEY)
                         .setAPIVersion(VERSION).setContentType(ContentType.JSON).setPrivateKey(PRIVATE_KEY)
                         .setHttpClient(new DefaultHttpClient());
         client = TerremarkFactory.getClient(props);
 
         getOrganizations();
     }
 
     /**
      * TODO
      *
      * @throws Exception
      */
     @Order(6)
     @Test(expected = RequestFailedException.class)
     public void testRequestFailed() throws Exception {
         final PropertiesBuilder props = new PropertiesBuilder().setEndPoint(ENDPOINT_URL).setAccessKey(ACCESS_KEY)
                         .setAPIVersion(VERSION).setContentType(ContentType.XML).setPrivateKey(PRIVATE_KEY)
                         .setHttpClient(new DefaultHttpClient());
         client = TerremarkFactory.getClient(props);
 
         getEnvironmentId();
 
         // NOTE: This call will not fail in Livespec testing because we can always activate a public IP address
         client.getNetworkHandler().publicIPActivate(environmentId);
 
         // This should fail in Livespec environment
         client.getComputePoolHandler().enableMemoryBurst("1");
     }
 
     /**
      * TODO
      *
      * @throws Exception
      */
     @Order(7)
     @Test(expected = NotImplementedException.class)
     public void testNotImplemented() throws Exception {
         final PropertiesBuilder props = new PropertiesBuilder().setEndPoint(ENDPOINT_URL).setAccessKey(ACCESS_KEY)
                         .setAPIVersion(Version.VERSION_2_11).setContentType(ContentType.XML).setPrivateKey(PRIVATE_KEY)
                         .setHttpClient(new DefaultHttpClient());
         client = TerremarkFactory.getClient(props);
 
         client.getComputePoolHandler().virtualMachineToolsInstall("1");
     }
 
     /**
      * TODO
      *
      * @throws Exception
      */
     @Order(8)
     @Test(expected = NotFoundException.class)
     public void testInvalidSshKey() throws Exception {
         final PropertiesBuilder props = new PropertiesBuilder().setEndPoint(ENDPOINT_URL).setAccessKey(ACCESS_KEY)
                         .setAPIVersion(Version.VERSION_2_11).setContentType(ContentType.XML).setPrivateKey(PRIVATE_KEY)
                         .setHttpClient(new DefaultHttpClient());
         client = TerremarkFactory.getClient(props);
 
         client.getOrganizationHandler().getSSHKeyByID("abc");
     }
}
