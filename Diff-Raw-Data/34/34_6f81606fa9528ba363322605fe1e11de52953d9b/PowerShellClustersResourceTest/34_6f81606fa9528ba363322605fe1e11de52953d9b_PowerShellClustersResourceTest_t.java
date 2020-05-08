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
 
 import com.redhat.rhevm.api.model.Cluster;
 import com.redhat.rhevm.api.model.CPU;
 import com.redhat.rhevm.api.model.DataCenter;
 
 import org.junit.Test;
 
 
 public class PowerShellClustersResourceTest extends AbstractPowerShellCollectionResourceTest<Cluster, PowerShellClusterResource, PowerShellClustersResource> {
 
     private static final String CLUSTER_CPU = "xeon";
     private static final String DATA_CENTER_ID = "12345";
 
     private static final String SELECT_RETURN_EPILOG = "\ndatacenterid: " + DATA_CENTER_ID;
     private static final String ADD_RETURN_EPILOG = "\ndatacenterid: " + DATA_CENTER_ID;
 
     public PowerShellClustersResourceTest() {
 	super(new PowerShellClusterResource("0"), "clusters", "cluster");
     }
 
     @Test
     public void testList() throws Exception {
         verifyCollection(
             resource.list(setUpResourceExpectations(getSelectCommand(),
                                                     getSelectReturn(SELECT_RETURN_EPILOG),
                                                     3,
                                                     NAMES)).getClusters(),
             NAMES);
     }
 
     @Test
     public void testAdd() throws Exception {
         verifyResponse(
             resource.add(setUpResourceExpectations(getAddCommand(),
                                                    getAddReturn(ADD_RETURN_EPILOG),
                                                    1,
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
             (PowerShellClusterResource)resource.getClusterSubResource(setUpResourceExpectations(NOTHING, NOTHING, 0),
                                                                       Integer.toString(NEW_NAME.hashCode())),
             NEW_NAME);
     }
 
     protected PowerShellClustersResource getResource() {
 	return new PowerShellClustersResource();
     }
 
     protected void populateModel(Cluster cluster) {
         CPU cpu = new CPU();
         cpu.setId(CLUSTER_CPU);
         cluster.setCpu(cpu);
 
         DataCenter dataCenter = new DataCenter();
         dataCenter.setId(DATA_CENTER_ID);
         cluster.setDataCenter(dataCenter);
     }
 
     protected String getAddCommand() {
         StringBuilder buf = new StringBuilder();
 
         buf.append("$v = get-clustercompatibilityversions -datacenterid " + DATA_CENTER_ID + "\n");
 
         buf.append("add-cluster");
 
         buf.append(" -clustername " + NEW_NAME);
         buf.append(" -clustercpuname " + CLUSTER_CPU);
         buf.append(" -datacenterid " + DATA_CENTER_ID);
         buf.append(" -compatibilityversion $v");
 
 	return buf.toString();
     }
 }
