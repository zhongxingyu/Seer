 /*
  * Copyright © 2010 Red Hat, Inc.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package com.redhat.rhevm.api.dummy.resource;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.HEAD;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MultivaluedMap;
 
 import org.jboss.resteasy.client.ClientResponse;
 import org.jboss.resteasy.client.ProxyFactory;
 import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
 import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
 import org.jboss.resteasy.spi.ResteasyProviderFactory;
 
 import com.redhat.rhevm.api.model.Link;
 import com.redhat.rhevm.api.model.VM;
 import com.redhat.rhevm.api.model.VMs;
 import com.redhat.rhevm.api.resource.MediaType;
 
 public class DummyTestBase extends Assert {
     private final String host = "localhost";
     private final int port = 8989;
     private final String uri = String.format("http://%s:%d/", host, port);
 
     private TJWSEmbeddedJaxrsServer server = new TJWSEmbeddedJaxrsServer();
 
     /* FIXME:
      * unify the client and server interfaces
      *
      * one interesting thing, though, is that if the client uses
      * an @Path("/vms") annotated interface, it is hardcoding
      * knowledge of the URI structure ... and we don't want that
      */
 
     @Path("/")
     protected interface ApiResource {
        @HEAD public ClientResponse head();
     }
     protected static ApiResource api;
 
     @Path("/")
     @Produces(MediaType.APPLICATION_XML)
     protected interface VmsResource {
         @GET public VMs list();
         @GET @Path("{id}") public VM get(@PathParam("id") String id);
     }
 
     protected VmsResource createVmsResource(String uri) {
         return ProxyFactory.create(VmsResource.class, uri);
     }
 
     protected Link getEntryPoint(String rel) {
         MultivaluedMap<String, String> headers = api.head().getHeaders();
         for (String s : headers.get("Link")) {
             /* FIXME: WTF are the headers concatenated into a single string? */
             for (String t : s.split(",")) {
                 Link link = Link.valueOf(t);
                 if (rel.equals(link.getRel()))
                     return link;
             }
         }
         throw new RuntimeException("No '" + rel + "' Link header found");
     }
 
     @Before
     public void setup() throws Exception {
         server.setPort(port);
         server.start();
         server.getDeployment().getDispatcher().getRegistry().addPerRequestResource(DummyApiResource.class);
         server.getDeployment().getDispatcher().getRegistry().addPerRequestResource(DummyVmsResource.class);
 
         RegisterBuiltin.register(ResteasyProviderFactory.getInstance());
 
         api = ProxyFactory.create(ApiResource.class, uri.toString());
     }
 
     @After
     public void destroy() throws Exception {
         server.stop();
     }
 }
