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
 
 import java.util.concurrent.Executor;
 
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.common.resource.UriInfoProvider;
 import com.redhat.rhevm.api.model.DataCenter;
 import com.redhat.rhevm.api.model.Fault;
 import com.redhat.rhevm.api.model.IP;
 import com.redhat.rhevm.api.model.Network;
 
 import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
 import com.redhat.rhevm.api.powershell.util.PowerShellParser;
 import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
 
 import org.junit.Test;
 
 import static org.easymock.classextension.EasyMock.expect;
 
 import static org.powermock.api.easymock.PowerMock.createMock;
 import static org.powermock.api.easymock.PowerMock.mockStatic;
 import static org.powermock.api.easymock.PowerMock.replayAll;
 
 
 public class PowerShellNetworkResourceTest extends AbstractPowerShellResourceTest<Network, PowerShellNetworkResource> {
 
     private static final String NETWORK_NAME = "rhevm";
     private static final String NETWORK_ID = Integer.toString(NETWORK_NAME.hashCode());
     private static final String DATA_CENTER_ID = PowerShellNetworksResourceTest.DATA_CENTER_ID;
 
     private static final String GET_COMMAND = "$n = get-networks;foreach ($i in $n) {  if ($i.networkid -eq \"" + NETWORK_ID + "\") {    $i  }}";
    private static final String UPDATE_COMMAND_PREFIX = "foreach ($i in $n) { if ($i.networkid -eq \"" + NETWORK_ID + "\") { ";
     private static final String UPDATE_COMMAND_SUFFIX = "update-network -networkobject $i -datacenterid $i.datacenterid } }";
     private static final String UPDATE_NAME_COMMAND = UPDATE_COMMAND_PREFIX + "$i.name = \"eris\"; " + UPDATE_COMMAND_SUFFIX;
     private static final String UPDATE_IP_COMMAND = UPDATE_COMMAND_PREFIX + "$i.address = \"172.31.0.110\"; " + UPDATE_COMMAND_SUFFIX;
     private static final String DISABLE_STP_COMMAND = UPDATE_COMMAND_PREFIX + "$i.stp = $false; " + UPDATE_COMMAND_SUFFIX;
 
     protected PowerShellNetworkResource getResource(Executor executor, PowerShellPoolMap poolMap, PowerShellParser parser, UriInfoProvider uriProvider) {
         return new PowerShellNetworkResource(NETWORK_ID, executor, uriProvider, poolMap, parser);
     }
 
     protected String formatNetwork(String name) {
         return formatXmlReturn("network",
                                new String[] { name },
                                new String[] { "" },
                                PowerShellNetworksResourceTest.extraArgs);
     }
 
     @Test
     public void testGet() throws Exception {
         setUriInfo(setUpNetworkExpectations(GET_COMMAND,
                                             formatNetwork(NETWORK_NAME),
                                             NETWORK_NAME));
         verifyNetwork(resource.get(), NETWORK_NAME);
     }
 
     @Test
     public void testUpdateName() throws Exception {
         setUriInfo(setUpNetworkExpectations(UPDATE_NAME_COMMAND,
                                             formatNetwork("eris"),
                                             "eris"));
         Network network = new Network();
         network.setName("eris");
         verifyNetwork(resource.update(network), "eris");
     }
 
     @Test
     public void testUpdateIp() throws Exception {
         setUriInfo(setUpNetworkExpectations(UPDATE_IP_COMMAND,
                                             formatNetwork("eris"),
                                             "eris"));
         Network network = new Network();
         network.setIp(new IP());
         network.getIp().setAddress("172.31.0.110");
         verifyNetwork(resource.update(network), "eris");
     }
 
     @Test
     public void testDisableStp() throws Exception {
         setUriInfo(setUpNetworkExpectations(DISABLE_STP_COMMAND,
                                             formatNetwork("eris"),
                                             "eris"));
         Network network = new Network();
         network.setStp(false);
         verifyNetwork(resource.update(network), "eris");
     }
 
     @Test
     public void testBadUpdate() throws Exception {
         try {
             setUriInfo(createMock(UriInfo.class));
             replayAll();
             resource.update(getNetwork("98765", "eris"));
             fail("expected WebApplicationException on bad update");
         } catch (WebApplicationException wae) {
             verifyUpdateException(wae);
         }
     }
 
     private UriInfo setUpNetworkExpectations(String command, String ret, String name) throws Exception {
         mockStatic(PowerShellCmd.class);
         expect(PowerShellCmd.runCommand(setUpPoolExpectations(), command)).andReturn(ret);
         UriInfo uriInfo = setUpBasicUriExpectations();
         replayAll();
         return uriInfo;
     }
 
     private Network getNetwork(String name) {
         return getNetwork(NETWORK_ID, name);
     }
 
     private Network getNetwork(String id, String name) {
         Network network = new Network();
         network.setId(id);
         network.setName(name);
         DataCenter dataCenter = new DataCenter();
         dataCenter.setId(DATA_CENTER_ID);
         network.setDataCenter(dataCenter);
         return network;
     }
 
     private void verifyNetwork(Network network, String name) {
         assertNotNull(network);
         assertEquals(network.getId(), Integer.toString(name.hashCode()));
         assertEquals(network.getName(), name);
         assertNotNull(network.getDataCenter());
         assertEquals(network.getDataCenter().getId(), DATA_CENTER_ID);
         verifyLinks(network);
     }
 
     private void verifyUpdateException(WebApplicationException wae) {
         assertEquals(409, wae.getResponse().getStatus());
         Fault fault = (Fault)wae.getResponse().getEntity();
         assertNotNull(fault);
         assertEquals("Broken immutability constraint", fault.getReason());
         assertEquals("Attempt to set immutable field: id", fault.getDetail());
     }
 }
