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
 package com.redhat.rhevm.api.powershell.resource;
 
 import com.redhat.rhevm.api.model.VM;
 
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;

 import org.junit.Test;
 
 public class PowerShellVmsResourceTest extends AbstractPowerShellCollectionResourceTest<VM, PowerShellVmResource, PowerShellVmsResource> {
 
     private static final String ADD_COMMAND_PROLOG =
         "$templ = get-template -templateid template_1\n";
     private static final String ADD_COMMAND_EPILOG =
         "-templateobject $templ -hostclusterid hostcluster_1";
 
     public PowerShellVmsResourceTest() {
         super(new PowerShellVmResource("0", null), "vms", "vm");
     }
 
     @Test
     public void testList() throws Exception {
         verifyCollection(
             resource.list(setUpResourceExpectations(getSelectCommand(), getSelectReturn(), NAMES)).getVMs(),
             NAMES);
     }
 
     @Test
     public void testAdd() throws Exception {
         verifyResponse(
             resource.add(setUpResourceExpectations(ADD_COMMAND_PROLOG + getAddCommand() + ADD_COMMAND_EPILOG,
                                                    getAddReturn(),
                                                    NEW_NAME),
                          getModel(NEW_NAME)),
             NEW_NAME);
     }
 
     @Test
     public void testRemove() throws Exception {
         setUpResourceExpectations(getRemoveCommand(), null);
         resource.remove(Integer.toString(NAMES[1].hashCode()));
     }
 
     @Test
     public void testGetSubResource() throws Exception {
         verifyResource(
             (PowerShellVmResource)resource.getVmSubResource(setUpResourceExpectations(NOTHING, NOTHING, 0),
                                                             Integer.toString(NEW_NAME.hashCode())),
             NEW_NAME);
     }
 
     protected PowerShellVmsResource getResource() {
         return new PowerShellVmsResource();
     }
 
     protected void populateModel(VM vm) {
         vm.setTemplateId("template_1");
         vm.setClusterId("hostcluster_1");
     }
 }
