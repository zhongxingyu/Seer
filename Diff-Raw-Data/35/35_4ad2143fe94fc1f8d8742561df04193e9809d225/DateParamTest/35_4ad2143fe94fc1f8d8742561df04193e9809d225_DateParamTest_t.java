/**
 * Copyright (C) 2012 Ness Computing, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package com.nesscomputing.jersey.types;
 
 import static org.junit.Assert.assertEquals;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.UriBuilder;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Inject;
 import com.google.inject.Key;
 import com.google.inject.servlet.GuiceFilter;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import com.nesscomputing.config.Config;
 import com.nesscomputing.httpclient.HttpClient;
 import com.nesscomputing.httpclient.response.StringContentConverter;
 import com.nesscomputing.jersey.ServerBaseModule;
 import com.nesscomputing.jersey.types.DateParam;
 import com.nesscomputing.lifecycle.junit.LifecycleRule;
 import com.nesscomputing.lifecycle.junit.LifecycleRunner;
 import com.nesscomputing.lifecycle.junit.LifecycleStatement;
 import com.nesscomputing.testing.IntegrationTestRule;
 import com.nesscomputing.testing.IntegrationTestRuleBuilder;
 import com.nesscomputing.testing.lessio.AllowDNSResolution;
 import com.nesscomputing.testing.lessio.AllowNetworkAccess;
 import com.nesscomputing.testing.tweaked.TweakedModule;
 
 @AllowNetworkAccess(endpoints= {"127.0.0.1:*"})
 @AllowDNSResolution
 @RunWith(LifecycleRunner.class)
 public class DateParamTest
 {
     private static final String DATE_TEST_SERVICE_NAME = "datetest";
 
     @LifecycleRule
     public final LifecycleStatement lifecycleRule = LifecycleStatement.serviceDiscoveryLifecycle();
 
     private UriBuilder uriBuilder;
 
     @Rule
     public IntegrationTestRule test = IntegrationTestRuleBuilder.defaultBuilder()
         .addService(DATE_TEST_SERVICE_NAME, TweakedModule.forServiceModule(DateToLongWadlModule.class))
         .addTestCaseModules(lifecycleRule.getLifecycleModule())
         .build(this);
 
     @Inject
     private HttpClient httpClient = null;
 
     private GuiceFilter guiceFilter = null;
 
     @Before
     public void setUp()
     {
         guiceFilter = test.exposeBinding(DATE_TEST_SERVICE_NAME, Key.get(GuiceFilter.class));
         uriBuilder = UriBuilder.fromUri(test.locateService(DATE_TEST_SERVICE_NAME)).path("/date");
     }
 
     @After
     public void tearDown()
     {
         Assert.assertNotNull(guiceFilter);
         guiceFilter.destroy();
     }
 
     @Test
     public void testDateLong() throws Exception
     {
         DateTime when = new DateTime(1000);
         assertEquals(when.getMillis(),
                 Long.parseLong(httpClient.get(
                         uriBuilder.queryParam("date", when.getMillis()).build(),
                         StringContentConverter.DEFAULT_RESPONSE_HANDLER).perform()));
     }
 
     @Test
     public void testDateString() throws Exception
     {
         DateTime when = new DateTime(1000);
         assertEquals(when.getMillis(),
                 Long.parseLong(httpClient.get(
                         uriBuilder.queryParam("date", when.toString()).build(),
                         StringContentConverter.DEFAULT_RESPONSE_HANDLER).perform()));
     }
 
     @Test
     public void testDateTZString() throws Exception
     {
         DateTime when = new DateTime(1000);
         assertEquals(when.getMillis(),
                 Long.parseLong(httpClient.get(
                         uriBuilder.queryParam("date", when.withZone(DateTimeZone.forID("America/Los_Angeles")).toString()).build(),
                         StringContentConverter.DEFAULT_RESPONSE_HANDLER).perform()));
     }
 
     @Test
     public void testNull() throws Exception
     {
         assertEquals("asdf", httpClient.get(
                         uriBuilder.build(),
                         StringContentConverter.DEFAULT_RESPONSE_HANDLER).perform());
     }
 
     @Test
     public void testNegativeDate() throws Exception
     {
         DateTime when = new DateTime(-1000);
         assertEquals(when.getMillis(),
                 Long.parseLong(httpClient.get(
                         uriBuilder.queryParam("date", when.getMillis()).build(),
                         StringContentConverter.DEFAULT_RESPONSE_HANDLER).perform()));
     }
 
     public static class DateToLongWadlModule extends AbstractModule {
         private final Config config;
 
         public DateToLongWadlModule(Config config)
         {
             this.config = config;
         }
 
         @Override
         protected void configure() {
             install (new ServerBaseModule(config));
             bind (DateToLongResource.class);
         }
     }
 
     @Path("/date")
     @Produces(MediaType.TEXT_PLAIN)
     public static class DateToLongResource
     {
         @GET
         public String dateToLong(@QueryParam("date") DateParam dateTime)
         {
             if (DateParam.getDateTime(dateTime) == null) {
                 return "asdf";
             }
             return Long.toString(DateParam.getDateTime(dateTime).getMillis());
         }
     }
 }
