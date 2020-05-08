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
 
 import java.text.MessageFormat;
 
 import com.redhat.rhevm.api.model.DataCenter;
 import com.redhat.rhevm.api.model.StorageType;
 
 import org.junit.Test;
 
 public class PowerShellDataCentersResourceTest extends AbstractPowerShellCollectionResourceTest<DataCenter, PowerShellDataCenterResource, PowerShellDataCentersResource> {
 
     private static final String SELECT_RETURN_EPILOG = "\ntype: ISCSI";
     private static final String ADD_COMMAND_EPILOG =
         "-type ISCSI";
     private static final String ADD_RETURN_EPILOG = "\ntype: ISCSI";
     private static final String GET_STORAGE_COMMAND =
         "get-storagedomain -datacenterid ";
     private static final String GET_STORAGE_RETURN =
         "storagedomainid: {0} \n name: {1}\ndomaintype: ISO \nstatus: ACTIVE\nsharedstatus: ACTIVE\ntype: ISCSI \n\n";
 
     public PowerShellDataCentersResourceTest() {
         super(new PowerShellDataCenterResource("0", null), "datacenters", "datacenter");
     }
 
     @Test
     public void testList() throws Exception {
         String [] commands = { getSelectCommand(),
                                GET_STORAGE_COMMAND + NAMES[0].hashCode(),
                                GET_STORAGE_COMMAND + NAMES[1].hashCode(),
                                GET_STORAGE_COMMAND + NAMES[2].hashCode() };
         String [] returns =  { getSelectReturn(SELECT_RETURN_EPILOG),
                                MessageFormat.format(GET_STORAGE_RETURN, "mimas".hashCode(), "mimas"),
                                MessageFormat.format(GET_STORAGE_RETURN, "dione".hashCode(), "dione"),
                                MessageFormat.format(GET_STORAGE_RETURN, "titan".hashCode(), "titan") };
          verifyCollection(
             resource.list(setUpResourceExpectations(commands, returns, 3, NAMES)).getDataCenters(),
             NAMES);
     }
 
     @Test
     public void testAdd() throws Exception {
        String [] commands = { getAddCommand() + ADD_COMMAND_EPILOG,
                            GET_STORAGE_COMMAND + NEW_NAME.hashCode()};
         String [] returns =  { getAddReturn(ADD_RETURN_EPILOG),
                            MessageFormat.format(GET_STORAGE_RETURN, "rhea".hashCode(), "rhea") };
         verifyResponse(
             resource.add(setUpResourceExpectations(commands, returns, 1, NEW_NAME), getModel(NEW_NAME)),
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
             (PowerShellDataCenterResource)resource.getDataCenterSubResource(setUpResourceExpectations(NOTHING, NOTHING, 0),
                                                                         Integer.toString(NEW_NAME.hashCode())),
             NEW_NAME);
     }
 
     protected PowerShellDataCentersResource getResource() {
         return new PowerShellDataCentersResource();
     }
 
     protected void populateModel(DataCenter dataCenter) {
         dataCenter.setStorageType(StorageType.ISCSI);
     }
 }
