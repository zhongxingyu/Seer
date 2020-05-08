 /**
  *
  * SIROCCO
  * Copyright (C) 2011 France Telecom
  * Contact: sirocco@ow2.org
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  * USA
  *
  * $Id$
  *
  */
 package org.ow2.sirocco.apis.rest.cimi.manager;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.easymock.EasyMock;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiCloudEntryPoint;
 import org.ow2.sirocco.apis.rest.cimi.request.CimiContext;
 import org.ow2.sirocco.apis.rest.cimi.request.CimiContextImpl;
 import org.ow2.sirocco.apis.rest.cimi.request.CimiExpand;
 import org.ow2.sirocco.apis.rest.cimi.request.CimiRequest;
 import org.ow2.sirocco.apis.rest.cimi.request.CimiResponse;
 import org.ow2.sirocco.apis.rest.cimi.request.CimiSelect;
 import org.ow2.sirocco.apis.rest.cimi.request.RequestParams;
 import org.ow2.sirocco.apis.rest.cimi.utils.ConstantsPath;
 import org.ow2.sirocco.cloudmanager.core.api.ICredentialsManager;
 import org.ow2.sirocco.cloudmanager.core.api.IEventManager;
 import org.ow2.sirocco.cloudmanager.core.api.IJobManager;
 import org.ow2.sirocco.cloudmanager.core.api.IMachineImageManager;
 import org.ow2.sirocco.cloudmanager.core.api.IMachineManager;
 import org.ow2.sirocco.cloudmanager.core.api.INetworkManager;
 import org.ow2.sirocco.cloudmanager.core.api.ISystemManager;
 import org.ow2.sirocco.cloudmanager.core.api.IVolumeManager;
 import org.ow2.sirocco.cloudmanager.model.cimi.Address;
 import org.ow2.sirocco.cloudmanager.model.cimi.AddressTemplate;
 import org.ow2.sirocco.cloudmanager.model.cimi.CloudEntryPoint;
 import org.ow2.sirocco.cloudmanager.model.cimi.Credentials;
 import org.ow2.sirocco.cloudmanager.model.cimi.CredentialsTemplate;
 import org.ow2.sirocco.cloudmanager.model.cimi.ForwardingGroup;
 import org.ow2.sirocco.cloudmanager.model.cimi.ForwardingGroupTemplate;
 import org.ow2.sirocco.cloudmanager.model.cimi.Job;
 import org.ow2.sirocco.cloudmanager.model.cimi.Machine;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineConfiguration;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineImage;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineTemplate;
 import org.ow2.sirocco.cloudmanager.model.cimi.Network;
 import org.ow2.sirocco.cloudmanager.model.cimi.NetworkConfiguration;
 import org.ow2.sirocco.cloudmanager.model.cimi.NetworkPort;
 import org.ow2.sirocco.cloudmanager.model.cimi.NetworkPortConfiguration;
 import org.ow2.sirocco.cloudmanager.model.cimi.NetworkPortTemplate;
 import org.ow2.sirocco.cloudmanager.model.cimi.NetworkTemplate;
 import org.ow2.sirocco.cloudmanager.model.cimi.Volume;
 import org.ow2.sirocco.cloudmanager.model.cimi.VolumeConfiguration;
 import org.ow2.sirocco.cloudmanager.model.cimi.VolumeImage;
 import org.ow2.sirocco.cloudmanager.model.cimi.VolumeTemplate;
 import org.ow2.sirocco.cloudmanager.model.cimi.event.EventLog;
 import org.ow2.sirocco.cloudmanager.model.cimi.event.EventLogTemplate;
 import org.ow2.sirocco.cloudmanager.model.cimi.system.System;
 import org.ow2.sirocco.cloudmanager.model.cimi.system.SystemTemplate;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 /**
  * Basic tests "end to end" for managers Machine.
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"/context/managerContext.xml"})
 public class CimiManagersCloudEntryPointTest {
 
     @Autowired
     @Qualifier("ICredentialsManager")
     private ICredentialsManager serviceCredentials;
 
     @Autowired
     @Qualifier("IJobManager")
     private IJobManager serviceJob;
 
     @Autowired
     @Qualifier("IMachineManager")
     private IMachineManager serviceMachine;
 
     @Autowired
     @Qualifier("IMachineImageManager")
     private IMachineImageManager serviceMachineImage;
 
     @Autowired
     @Qualifier("ISystemManager")
     private ISystemManager serviceSystem;
 
     @Autowired
     @Qualifier("IVolumeManager")
     private IVolumeManager serviceVolume;
 
     @Autowired
     @Qualifier("INetworkManager")
     private INetworkManager serviceNetwork;
 
     @Autowired
     @Qualifier("IEventManager")
     private IEventManager serviceEvent;
 
     @Autowired
     @Qualifier("CimiManagerReadCloudEntryPoint")
     private CimiManager managerRead;
 
     private CimiRequest request;
 
     private CimiResponse response;
 
     private CimiContext context;
 
     /**
      * @throws java.lang.Exception
      */
     @Before
     public void setUp() throws Exception {
 
         this.request = new CimiRequest();
         this.request.setBaseUri("/");
         RequestParams header = new RequestParams();
         header.setCimiSelect(new CimiSelect());
         header.setCimiExpand(new CimiExpand());
         this.request.setParams(header);
 
         this.response = new CimiResponse();
         this.context = new CimiContextImpl(this.request, this.response);
     }
 
     /**
      * @throws java.lang.Exception
      */
     @After
     public void tearDown() throws Exception {
         EasyMock.reset(this.serviceCredentials);
         EasyMock.reset(this.serviceJob);
         EasyMock.reset(this.serviceMachine);
         EasyMock.reset(this.serviceMachineImage);
         EasyMock.reset(this.serviceSystem);
         EasyMock.reset(this.serviceVolume);
         EasyMock.reset(this.serviceNetwork);
         EasyMock.reset(this.serviceEvent);
     }
 
     @Test
     // TODO Others resources : Network, ...
     public void testRead() throws Exception {
         CloudEntryPoint cloud = new CloudEntryPoint();
         cloud.setId(10);
 
         // Credentials
         List<Credentials> credentialsCollection = new ArrayList<Credentials>();
         List<CredentialsTemplate> credentialsTemplateCollection = new ArrayList<CredentialsTemplate>();
         // Jobs
         List<Job> jobCollection = new ArrayList<Job>();
         // Machines
         List<Machine> machineCollection = new ArrayList<Machine>();
         List<MachineTemplate> machineTemplateCollection = new ArrayList<MachineTemplate>();
         List<MachineConfiguration> machineConfigurationCollection = new ArrayList<MachineConfiguration>();
         List<MachineImage> machineImageCollection = new ArrayList<MachineImage>();
         // Systems
         List<System> systemCollection = new ArrayList<System>();
         List<SystemTemplate> systemTemplateCollection = new ArrayList<SystemTemplate>();
         // Volumes
         List<Volume> volumeCollection = new ArrayList<Volume>();
         List<VolumeTemplate> volumeTemplateCollection = new ArrayList<VolumeTemplate>();
         List<VolumeConfiguration> volumeConfigurationCollection = new ArrayList<VolumeConfiguration>();
         List<VolumeImage> volumeImageCollection = new ArrayList<VolumeImage>();
         // Networks
         List<Network> networkCollection = new ArrayList<Network>();
         List<NetworkTemplate> networkTemplateCollection = new ArrayList<NetworkTemplate>();
         List<NetworkConfiguration> networkConfigurationCollection = new ArrayList<NetworkConfiguration>();
         // NetworkPorts
         List<NetworkPort> networkPortCollection = new ArrayList<NetworkPort>();
         List<NetworkPortTemplate> networkPortTemplateCollection = new ArrayList<NetworkPortTemplate>();
         List<NetworkPortConfiguration> networkPortConfigurationCollection = new ArrayList<NetworkPortConfiguration>();
         // Addresss
         List<Address> addressCollection = new ArrayList<Address>();
         List<AddressTemplate> addressTemplateCollection = new ArrayList<AddressTemplate>();
         // ForwardingGroups
         List<ForwardingGroup> forwardingGroupCollection = new ArrayList<ForwardingGroup>();
         List<ForwardingGroupTemplate> forwardingGroupTemplateCollection = new ArrayList<ForwardingGroupTemplate>();
         // EventLogs
         List<EventLog> eventLogCollection = new ArrayList<EventLog>();
         List<EventLogTemplate> eventLogTemplateCollection = new ArrayList<EventLogTemplate>();
 
         EasyMock.expect(this.serviceCredentials.getCredentials()).andReturn(credentialsCollection);
         EasyMock.expect(this.serviceCredentials.getCredentialsTemplates()).andReturn(credentialsTemplateCollection);
         EasyMock.replay(this.serviceCredentials);
 
         EasyMock.expect(this.serviceJob.getJobs()).andReturn(jobCollection);
         EasyMock.replay(this.serviceJob);
 
         EasyMock.expect(this.serviceMachine.getCloudEntryPoint()).andReturn(cloud);
         EasyMock.expect(this.serviceMachine.getMachines()).andReturn(machineCollection);
         EasyMock.expect(this.serviceMachine.getMachineTemplates()).andReturn(machineTemplateCollection);
         EasyMock.expect(this.serviceMachine.getMachineConfigurations()).andReturn(machineConfigurationCollection);
         EasyMock.replay(this.serviceMachine);
 
         EasyMock.expect(this.serviceMachineImage.getMachineImages()).andReturn(machineImageCollection);
         EasyMock.replay(this.serviceMachineImage);
 
         EasyMock.expect(this.serviceSystem.getSystems()).andReturn(systemCollection);
         EasyMock.expect(this.serviceSystem.getSystemTemplates()).andReturn(systemTemplateCollection);
         EasyMock.replay(this.serviceSystem);
 
         EasyMock.expect(this.serviceVolume.getVolumes()).andReturn(volumeCollection);
         EasyMock.expect(this.serviceVolume.getVolumeTemplates()).andReturn(volumeTemplateCollection);
         EasyMock.expect(this.serviceVolume.getVolumeConfigurations()).andReturn(volumeConfigurationCollection);
         EasyMock.expect(this.serviceVolume.getVolumeImages()).andReturn(volumeImageCollection);
         EasyMock.replay(this.serviceVolume);
 
         EasyMock.expect(this.serviceNetwork.getNetworks()).andReturn(networkCollection);
         EasyMock.expect(this.serviceNetwork.getNetworkTemplates()).andReturn(networkTemplateCollection);
         EasyMock.expect(this.serviceNetwork.getNetworkConfigurations()).andReturn(networkConfigurationCollection);
         EasyMock.expect(this.serviceNetwork.getNetworkPorts()).andReturn(networkPortCollection);
         EasyMock.expect(this.serviceNetwork.getNetworkPortTemplates()).andReturn(networkPortTemplateCollection);
         EasyMock.expect(this.serviceNetwork.getNetworkPortConfigurations()).andReturn(networkPortConfigurationCollection);
         EasyMock.expect(this.serviceNetwork.getAddresses()).andReturn(addressCollection);
         EasyMock.expect(this.serviceNetwork.getAddressTemplates()).andReturn(addressTemplateCollection);
         EasyMock.expect(this.serviceNetwork.getForwardingGroups()).andReturn(forwardingGroupCollection);
         EasyMock.expect(this.serviceNetwork.getForwardingGroupTemplates()).andReturn(forwardingGroupTemplateCollection);
         EasyMock.replay(this.serviceNetwork);
 
        // FIXME EventLog
        // EasyMock.expect(this.serviceEvent.getEventLogs()).andReturn(eventLogCollection);
        // EasyMock.expect(this.serviceEvent.getEventLogTemplates()).andReturn(eventLogTemplateCollection);
        // EasyMock.replay(this.serviceEvent);
 
         // this.request.setId("1");
         this.managerRead.execute(this.context);
 
         Assert.assertEquals(200, this.response.getStatus());
         CimiCloudEntryPoint cimiCloud = (CimiCloudEntryPoint) this.response.getCimiData();
 
         Assert.assertEquals(ConstantsPath.CLOUDENTRYPOINT_PATH, cimiCloud.getId());
 
         Assert.assertEquals(ConstantsPath.CREDENTIAL_PATH, cimiCloud.getCredentials().getHref());
         Assert.assertEquals(ConstantsPath.CREDENTIAL_TEMPLATE_PATH, cimiCloud.getCredentialTemplates().getHref());
 
         Assert.assertEquals(ConstantsPath.JOB_PATH, cimiCloud.getJobs().getHref());
 
         Assert.assertEquals(ConstantsPath.MACHINE_PATH, cimiCloud.getMachines().getHref());
         Assert.assertEquals(ConstantsPath.MACHINE_CONFIGURATION_PATH, cimiCloud.getMachineConfigs().getHref());
         Assert.assertEquals(ConstantsPath.MACHINE_IMAGE_PATH, cimiCloud.getMachineImages().getHref());
         Assert.assertEquals(ConstantsPath.MACHINE_TEMPLATE_PATH, cimiCloud.getMachineTemplates().getHref());
 
         Assert.assertEquals(ConstantsPath.SYSTEM_PATH, cimiCloud.getSystems().getHref());
         Assert.assertEquals(ConstantsPath.SYSTEM_TEMPLATE_PATH, cimiCloud.getSystemTemplates().getHref());
 
         Assert.assertEquals(ConstantsPath.VOLUME_PATH, cimiCloud.getVolumes().getHref());
         Assert.assertEquals(ConstantsPath.VOLUME_CONFIGURATION_PATH, cimiCloud.getVolumeConfigs().getHref());
         Assert.assertEquals(ConstantsPath.VOLUME_IMAGE_PATH, cimiCloud.getVolumeImages().getHref());
         Assert.assertEquals(ConstantsPath.VOLUME_TEMPLATE_PATH, cimiCloud.getVolumeTemplates().getHref());
 
         EasyMock.verify(this.serviceCredentials);
         EasyMock.verify(this.serviceJob);
         EasyMock.verify(this.serviceMachine);
         EasyMock.verify(this.serviceMachineImage);
         EasyMock.verify(this.serviceSystem);
         EasyMock.verify(this.serviceVolume);
     }
 
     // @Test
     // @Ignore
     // public void testReadWithCimiSelect() throws Exception {
     // // TODO
     // }
 }
