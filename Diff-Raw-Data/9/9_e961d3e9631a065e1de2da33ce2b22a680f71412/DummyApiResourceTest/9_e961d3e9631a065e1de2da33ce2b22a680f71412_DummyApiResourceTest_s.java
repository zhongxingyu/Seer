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
 
 import java.util.ArrayList;
 import java.util.List;
 import org.junit.Test;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response;
 import org.jboss.resteasy.client.ClientResponse;
 import com.redhat.rhevm.api.model.Link;
 
 public class DummyApiResourceTest extends DummyTestBase {
     @Test
     public void testEntryPoint() throws Exception {
        ClientResponse response = api.head();
 
         assertEquals(Response.Status.Family.SUCCESSFUL, response.getResponseStatus().getFamily());
         assertEquals(Response.Status.OK, response.getResponseStatus());
 
         MultivaluedMap<String, String> headers = response.getHeaders();
 
         List<String> linkHeaders = headers.get("Link");
 
         assertNotNull(linkHeaders);
 
         List<Link> links = new ArrayList<Link>();
 
         for (String s : linkHeaders) {
             /* FIXME: WTF are the headers concatenated into a single string? */
             for (String t : s.split(",")) {
                 Link l = Link.valueOf(t);
                 assertEquals(t, l.toString());
                 links.add(l);
             }
         }
 
         assertEquals(2, links.size());
         assertEquals("hosts", links.get(0).getRel());
         assertEquals("vms", links.get(1).getRel());
     }
 }
