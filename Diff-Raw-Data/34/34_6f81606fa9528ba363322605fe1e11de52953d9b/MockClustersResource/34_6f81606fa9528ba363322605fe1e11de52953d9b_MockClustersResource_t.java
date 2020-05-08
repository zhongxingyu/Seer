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
 package com.redhat.rhevm.api.mock.resource;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.model.DataCenter;
 import com.redhat.rhevm.api.model.Cluster;
 import com.redhat.rhevm.api.model.Clusters;
 import com.redhat.rhevm.api.model.CPU;
 import com.redhat.rhevm.api.resource.ClusterResource;
 import com.redhat.rhevm.api.resource.ClustersResource;
 
 import static com.redhat.rhevm.api.mock.resource.AbstractMockResource.allocateId;
 
 
 public class MockClustersResource extends AbstractMockCollectionResource implements ClustersResource {
 
     private static Map<String, MockClusterResource> clusters =
         Collections.synchronizedMap(new HashMap<String, MockClusterResource>());
 
     public void populate() {
         synchronized (clusters) {
             while (clusters.size() < 2) {
                 MockClusterResource resource = new MockClusterResource(allocateId(Cluster.class), getExecutor());
                 resource.getModel().setName("cluster" + resource.getModel().getId());
                 CPU cpu = new CPU();
                 cpu.setId((clusters.size() % 2) == 0 ? "Intel Xeon" : "AMD Opteron G1");
                 resource.getModel().setCpu(cpu);
                 DataCenter dataCenter = new DataCenter();
                 dataCenter.setId(resource.getModel().getId());
                 resource.getModel().setDataCenter(dataCenter);
                 clusters.put(resource.getModel().getId(), resource);
             }
         }
     }
 
     public static String getHref(UriBuilder baseUriBuilder, String id) {
         return baseUriBuilder.clone().path("clusters").path(id).build().toString();
     }
 
     @Override
     public Clusters list(UriInfo uriInfo) {
         Clusters ret = new Clusters();
 
         for (MockClusterResource cluster : clusters.values()) {
             String id = cluster.getModel().getId();
             UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().path(id);
             ret.getClusters().add(cluster.addLinks(uriInfo, uriBuilder));
         }
 
         return ret;
     }
 
     @Override
     public Response add(UriInfo uriInfo, Cluster cluster) {
         MockClusterResource resource = new MockClusterResource(allocateId(Cluster.class), getExecutor());
 
         resource.updateModel(cluster);
 
         String id = resource.getId();
         clusters.put(id, resource);
 
         UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().path(id);
 
         cluster = resource.addLinks(uriInfo, uriBuilder);
 
         return Response.created(uriBuilder.build()).entity(cluster).build();
     }
 
     @Override
     public void remove(String id) {
         clusters.remove(id);
     }
 
     @Override
     public ClusterResource getClusterSubResource(UriInfo uriInfo, String id) {
         return clusters.get(id);
     }
 }
