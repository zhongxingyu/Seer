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
 
 import java.util.concurrent.Executor;
 
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.model.Action;
 import com.redhat.rhevm.api.model.Cluster;
 import com.redhat.rhevm.api.model.VM;
 import com.redhat.rhevm.api.resource.DevicesResource;
import com.redhat.rhevm.api.resource.SnapshotsResource;
 import com.redhat.rhevm.api.resource.VmResource;
 import com.redhat.rhevm.api.common.util.JAXBHelper;
 import com.redhat.rhevm.api.common.util.LinkHelper;
 import com.redhat.rhevm.api.mock.model.MockVmStatus;
 
 
 public class MockVmResource extends AbstractMockResource<VM> implements VmResource {
     /* FIXME: would like to do:
      * private @Context UriInfo uriInfo;
      */
 
     @SuppressWarnings("unused")
     private MockVmStatus status;
 
     /**
      * Package-protected ctor, never needs to be instantiated by JAX-RS framework.
      *
      * @param vm       encapsulated VM
      * @param executor executor used for asynchronous actions
      */
     public MockVmResource(String id, Executor executor) {
         super(id, executor);
     }
 
     // FIXME: this needs to be atomic
     public void updateModel(VM vm) {
         // update writable fields only
         if (vm.isSetName()) {
             getModel().setName(vm.getName());
         }
         if (vm.isSetDescription()) {
             getModel().setDescription(vm.getDescription());
         }
         if (vm.isSetCluster()) {
             Cluster cluster = new Cluster();
             cluster.setId(vm.getCluster().getId());
             getModel().setCluster(cluster);
         }
     }
 
     public VM addLinks() {
         return LinkHelper.addLinks(JAXBHelper.clone("vm", VM.class, getModel()));
     }
 
     /* FIXME: kill uriInfo param, make href auto-generated? */
     @Override
     public VM get(UriInfo uriInfo) {
         return addLinks();
     }
 
     @Override
     public VM update(UriInfo uriInfo, VM vm) {
         validateUpdate(vm);
         updateModel(vm);
         return addLinks();
     }
 
     protected String[] getStrictlyImmutable() {
         return addStrictlyImmutable("type");
     }
 
     @Override
     public Response start(UriInfo uriInfo, Action action) {
         return doAction(uriInfo, new VmStatusSetter(action, MockVmStatus.UP));
     }
 
 
     @Override
     public Response stop(UriInfo uriInfo, Action action) {
         return doAction(uriInfo, new VmStatusSetter(action, MockVmStatus.DOWN));
 
     }
 
     @Override
     public Response shutdown(UriInfo uriInfo, Action action) {
         return doAction(uriInfo, new VmStatusSetter(action, MockVmStatus.DOWN));
     }
 
     @Override
     public Response suspend(UriInfo uriInfo, Action action) {
         return doAction(uriInfo, new DoNothingTask(action));
     }
 
     @Override
     public Response detach(UriInfo uriInfo, Action action) {
         return doAction(uriInfo, new DoNothingTask(action));
     }
 
     @Override
     public Response migrate(UriInfo uriInfo, Action action) {
         String migrateToHostId = action.getHost().getId();
         return doAction(uriInfo, new DoNothingTask(action));
     }
 
     private class VmStatusSetter extends AbstractActionTask {
         private MockVmStatus status;
         VmStatusSetter(Action action, MockVmStatus status) {
             super(action, "VM status failed with {0}");
             this.status = status;
         }
         public void execute() {
             if (status.equals(MockVmResource.this.status)) {
                 throw new IllegalStateException("VM status already: " + status);
             }
             MockVmResource.this.status = status;
         }
     }
 
    @Override public DevicesResource   getCdRomsResource()    { return null; }
    @Override public DevicesResource   getDisksResource()     { return null; }
    @Override public DevicesResource   getNicsResource()      { return null; }
    @Override public SnapshotsResource getSnapshotsResource() { return null; }
 }
