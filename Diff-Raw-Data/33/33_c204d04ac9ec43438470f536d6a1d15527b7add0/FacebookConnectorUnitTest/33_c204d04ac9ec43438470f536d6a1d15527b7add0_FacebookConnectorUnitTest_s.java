 /**
  * Mule Facebook Cloud Connector
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.module.facebook;
 
 import static org.junit.Assert.*;
 import static org.mockito.Matchers.*;
 import static org.mockito.Mockito.*;
 
 import java.net.URI;
 import java.util.Map;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 
 /**
  * Test Driver for the connector
  */
 public class FacebookConnectorUnitTest
 {
     private FacebookConnector connector;
     private Client client;
     private WebResource resource;
 
     private static String responseJSON = "{\"id\": \"4\",\"name\": \"Mark Zuckerberg\",\"first_name\": \"Mark\",\"last_name\": \"Zuckerberg\",\"link\": \"https://www.facebook.com/zuck\",\"username\": \"zuck\",\"gender\": \"male\",\"locale\": \"en_US\"}";
 
     @Before
     public void setup()
     {
         connector = new FacebookConnector();
         client = mock(Client.class);
         connector.setClient(client);
         resource = mock(WebResource.class);
         when(client.resource((URI) anyObject())).thenReturn(resource);
         when(resource.queryParam(eq(anyString()), "")).thenReturn(resource);
         when(resource.get(String.class)).thenReturn(responseJSON);
         when(resource.post(String.class, eq(anyObject()))).thenReturn(responseJSON);
     }
 
     @Test
     public void testGetAlbum() throws Exception
     {
         final Map<String, Object> res = connector.getAlbum("test", "");
         assertNotNull(res);
     }
     
     @Test
     public void testGetApplication() throws Exception
     {
         final Map<String, Object> res = connector.getApplication("");
         assertNotNull(res);
     }
     
     @Test
     public void testGetEvent() throws Exception
     {
         final Map<String, Object> res = connector.getEvent("", "");
         assertNotNull(res);
     }
     
     @Test
     public void testGetGroup() throws Exception
     {
         final Map<String, Object> res = connector.getGroup("", "");
         assertNotNull(res);
     }
     
     @Test
     public void testGetLink() throws Exception
     {
         final Map<String, Object> res = connector.getLink("", "");
         assertNotNull(res);
     }
     
     @Test
     public void testGetNote() throws Exception
     {
         final Map<String, Object> res = connector.getNote("", "");
         assertNotNull(res);
     }
     
     @Test
     public void testGetPage() throws Exception
     {
         final Map<String, Object> res = connector.getPage("", "");
         assertNotNull(res);
     }
     
     @Test
     public void testGetPhoto() throws Exception
     {
         final Map<String, Object> res = connector.getPhoto("", "");
         assertNotNull(res);
     }
     
     @Test
     public void testGetPost() throws Exception
     {
         final Map<String, Object> res = connector.getPost("", "");
         assertNotNull(res);
     }
     
     @Test
     public void testGetStatus() throws Exception
     {
         final Map<String, Object> res = connector.getStatus("", "", "");
         assertNotNull(res);
     }
     
     @Test
     public void testGetUser() throws Exception
     {
         final Map<String, Object> res = connector.getUser("", "");
         assertNotNull(res);
     }
     
     @Test
     public void testGetVideo() throws Exception
     {
        final Map<String, Object> res = connector.getVideo("", "");
         assertNotNull(res);
     }
     
 }
