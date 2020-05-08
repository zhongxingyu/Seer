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
 package org.ow2.sirocco.cimi.server.manager;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.ow2.sirocco.cimi.domain.CimiAddress;
 import org.ow2.sirocco.cimi.domain.CimiAddressCreate;
 import org.ow2.sirocco.cimi.domain.CimiAddressTemplate;
 import org.ow2.sirocco.cimi.domain.CimiComponentDescriptor;
 import org.ow2.sirocco.cimi.domain.CimiCredential;
 import org.ow2.sirocco.cimi.domain.CimiCredentialCreate;
 import org.ow2.sirocco.cimi.domain.CimiCredentialTemplate;
 import org.ow2.sirocco.cimi.domain.CimiDataCommon;
 import org.ow2.sirocco.cimi.domain.CimiDiskConfiguration;
 import org.ow2.sirocco.cimi.domain.CimiEventLogCreate;
 import org.ow2.sirocco.cimi.domain.CimiEventLogTemplate;
 import org.ow2.sirocco.cimi.domain.CimiForwardingGroup;
 import org.ow2.sirocco.cimi.domain.CimiForwardingGroupCreate;
 import org.ow2.sirocco.cimi.domain.CimiForwardingGroupNetwork;
 import org.ow2.sirocco.cimi.domain.CimiForwardingGroupTemplate;
 import org.ow2.sirocco.cimi.domain.CimiMachine;
 import org.ow2.sirocco.cimi.domain.CimiMachineConfiguration;
 import org.ow2.sirocco.cimi.domain.CimiMachineCreate;
 import org.ow2.sirocco.cimi.domain.CimiMachineDisk;
 import org.ow2.sirocco.cimi.domain.CimiMachineImage;
 import org.ow2.sirocco.cimi.domain.CimiMachineNetworkInterface;
 import org.ow2.sirocco.cimi.domain.CimiMachineNetworkInterfaceAddress;
 import org.ow2.sirocco.cimi.domain.CimiMachineTemplate;
 import org.ow2.sirocco.cimi.domain.CimiMachineTemplateNetworkInterface;
 import org.ow2.sirocco.cimi.domain.CimiMachineTemplateVolume;
 import org.ow2.sirocco.cimi.domain.CimiMachineTemplateVolumeTemplate;
 import org.ow2.sirocco.cimi.domain.CimiMachineVolume;
 import org.ow2.sirocco.cimi.domain.CimiNetwork;
 import org.ow2.sirocco.cimi.domain.CimiNetworkConfiguration;
 import org.ow2.sirocco.cimi.domain.CimiNetworkCreate;
 import org.ow2.sirocco.cimi.domain.CimiNetworkNetworkPort;
 import org.ow2.sirocco.cimi.domain.CimiNetworkPort;
 import org.ow2.sirocco.cimi.domain.CimiNetworkPortConfiguration;
 import org.ow2.sirocco.cimi.domain.CimiNetworkPortCreate;
 import org.ow2.sirocco.cimi.domain.CimiNetworkPortTemplate;
 import org.ow2.sirocco.cimi.domain.CimiNetworkTemplate;
 import org.ow2.sirocco.cimi.domain.CimiObjectCommon;
 import org.ow2.sirocco.cimi.domain.CimiResource;
 import org.ow2.sirocco.cimi.domain.CimiSystem;
 import org.ow2.sirocco.cimi.domain.CimiSystemAddress;
 import org.ow2.sirocco.cimi.domain.CimiSystemCreate;
 import org.ow2.sirocco.cimi.domain.CimiSystemCredential;
 import org.ow2.sirocco.cimi.domain.CimiSystemForwardingGroup;
 import org.ow2.sirocco.cimi.domain.CimiSystemMachine;
 import org.ow2.sirocco.cimi.domain.CimiSystemNetwork;
 import org.ow2.sirocco.cimi.domain.CimiSystemNetworkPort;
 import org.ow2.sirocco.cimi.domain.CimiSystemSystem;
 import org.ow2.sirocco.cimi.domain.CimiSystemTemplate;
 import org.ow2.sirocco.cimi.domain.CimiSystemVolume;
 import org.ow2.sirocco.cimi.domain.CimiVolume;
 import org.ow2.sirocco.cimi.domain.CimiVolumeConfiguration;
 import org.ow2.sirocco.cimi.domain.CimiVolumeCreate;
 import org.ow2.sirocco.cimi.domain.CimiVolumeImage;
 import org.ow2.sirocco.cimi.domain.CimiVolumeTemplate;
 import org.ow2.sirocco.cimi.domain.CimiVolumeVolumeImage;
 import org.ow2.sirocco.cimi.domain.ExchangeType;
 import org.ow2.sirocco.cimi.domain.collection.CimiCollection;
 import org.ow2.sirocco.cimi.server.configuration.ConfigurationException;
 import org.ow2.sirocco.cimi.server.converter.PathHelper;
 import org.ow2.sirocco.cimi.server.request.CimiContext;
 import org.ow2.sirocco.cimi.server.request.IdRequest;
 import org.ow2.sirocco.cloudmanager.core.api.ICredentialsManager;
 import org.ow2.sirocco.cloudmanager.core.api.IEventManager;
 import org.ow2.sirocco.cloudmanager.core.api.IMachineImageManager;
 import org.ow2.sirocco.cloudmanager.core.api.IMachineManager;
 import org.ow2.sirocco.cloudmanager.core.api.INetworkManager;
 import org.ow2.sirocco.cloudmanager.core.api.ISystemManager;
 import org.ow2.sirocco.cloudmanager.core.api.IVolumeManager;
 import org.ow2.sirocco.cloudmanager.core.api.QueryResult;
 import org.ow2.sirocco.cloudmanager.core.api.exception.ResourceNotFoundException;
 import org.ow2.sirocco.cloudmanager.model.cimi.AddressTemplate;
 import org.ow2.sirocco.cloudmanager.model.cimi.CloudCollectionItem;
 import org.ow2.sirocco.cloudmanager.model.cimi.Credentials;
 import org.ow2.sirocco.cloudmanager.model.cimi.CredentialsTemplate;
 import org.ow2.sirocco.cloudmanager.model.cimi.ForwardingGroup;
 import org.ow2.sirocco.cloudmanager.model.cimi.ForwardingGroupNetwork;
 import org.ow2.sirocco.cloudmanager.model.cimi.ForwardingGroupTemplate;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineConfiguration;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineDisk;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineImage;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineNetworkInterface;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineNetworkInterfaceAddress;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineTemplate;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineVolume;
 import org.ow2.sirocco.cloudmanager.model.cimi.Network;
 import org.ow2.sirocco.cloudmanager.model.cimi.NetworkConfiguration;
 import org.ow2.sirocco.cloudmanager.model.cimi.NetworkNetworkPort;
 import org.ow2.sirocco.cloudmanager.model.cimi.NetworkPortConfiguration;
 import org.ow2.sirocco.cloudmanager.model.cimi.NetworkPortTemplate;
 import org.ow2.sirocco.cloudmanager.model.cimi.NetworkTemplate;
 import org.ow2.sirocco.cloudmanager.model.cimi.Volume;
 import org.ow2.sirocco.cloudmanager.model.cimi.VolumeConfiguration;
 import org.ow2.sirocco.cloudmanager.model.cimi.VolumeImage;
 import org.ow2.sirocco.cloudmanager.model.cimi.VolumeTemplate;
 import org.ow2.sirocco.cloudmanager.model.cimi.VolumeVolumeImage;
 import org.ow2.sirocco.cloudmanager.model.cimi.event.EventLogTemplate;
 import org.ow2.sirocco.cloudmanager.model.cimi.system.SystemTemplate;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Component;
 
 /**
  * Implementation of a helper to get complete resource passed by reference or by
  * value during its creation
  */
 @Component("MergeReferenceHelper")
 public class MergeReferenceHelperImpl implements MergeReferenceHelper {
 
     @Autowired
     @Qualifier("ICredentialsManager")
     private ICredentialsManager managerCredentials;
 
     @Autowired
     @Qualifier("IMachineImageManager")
     private IMachineImageManager managerMachineImage;
 
     @Autowired
     @Qualifier("IMachineManager")
     private IMachineManager managerMachine;
 
     @Autowired
     @Qualifier("INetworkManager")
     private INetworkManager managerNetwork;
 
     @Autowired
     @Qualifier("IEventManager")
     private IEventManager managerEvent;
 
     @Autowired
     @Qualifier("IVolumeManager")
     private IVolumeManager managerVolume;
 
     @Autowired
     @Qualifier("ISystemManager")
     private ISystemManager managerSystem;
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiMachineImage)
      */
     @Override
     public void merge(final CimiContext context, final CimiMachineImage cimi) throws Exception {
         if (true == cimi.hasReference()) {
             MachineImage dataService = this.managerMachineImage.getMachineImageById(PathHelper.extractIdString(cimi.getHref()));
             CimiMachineImage cimiRef = (CimiMachineImage) context.convertToFullCimi(dataService, CimiMachineImage.class);
             this.merge(cimiRef, cimi);
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiMachineConfiguration)
      */
     @Override
     public void merge(final CimiContext context, final CimiMachineConfiguration cimi) throws Exception {
         if (true == cimi.hasReference()) {
             MachineConfiguration dataService = this.managerMachine.getMachineConfigurationById(PathHelper.extractIdString(cimi
                 .getHref()));
             CimiMachineConfiguration cimiRef = (CimiMachineConfiguration) context.convertToFullCimi(dataService,
                 CimiMachineConfiguration.class);
             this.merge(cimiRef, cimi);
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiCredentialCreate)
      */
     @Override
     public void merge(final CimiContext context, final CimiCredentialCreate cimi) throws Exception {
         this.merge(context, cimi.getCredentialTemplate());
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiCredentialTemplate)
      */
     @Override
     public void merge(final CimiContext context, final CimiCredentialTemplate cimi) throws Exception {
         if (true == cimi.hasReference()) {
             CredentialsTemplate dataService = this.managerCredentials.getCredentialsTemplateById(PathHelper
                 .extractIdString(cimi.getHref()));
             CimiCredentialTemplate cimiRef = (CimiCredentialTemplate) context.convertToFullCimi(dataService,
                 CimiCredentialTemplate.class);
             this.merge(cimiRef, cimi);
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiCredential)
      */
     @Override
     public void merge(final CimiContext context, final CimiCredential cimi) throws Exception {
         if (true == cimi.hasReference()) {
             Credentials dataService = this.managerCredentials.getCredentialsById(PathHelper.extractIdString(cimi.getHref()));
             CimiCredential cimiRef = (CimiCredential) context.convertToFullCimi(dataService, CimiCredential.class);
             this.merge(cimiRef, cimi);
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiMachineCreate)
      */
     @Override
     public void merge(final CimiContext context, final CimiMachineCreate cimi) throws Exception {
         this.merge(context, cimi.getMachineTemplate());
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiMachineDisk)
      */
     @Override
     public void merge(final CimiContext context, final CimiMachineDisk cimi) throws Exception {
         if (true == cimi.hasReference()) {
             MachineDisk dataService = this.managerMachine.getDiskFromMachine(context.getRequest().getIdParent(),
                 PathHelper.extractIdString(cimi.getHref()));
             CimiMachineDisk cimiRef = (CimiMachineDisk) context.convertToFullCimi(dataService, CimiMachineDisk.class);
             this.merge(cimiRef, cimi);
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiMachineVolume)
      */
     @Override
     public void merge(final CimiContext context, final CimiMachineVolume cimi) throws Exception {
         if (true == cimi.hasReference()) {
             MachineVolume dataService = this.managerMachine.getVolumeFromMachine(context.getRequest().getIdParent(),
                 PathHelper.extractIdString(cimi.getHref()));
             CimiMachineVolume cimiRef = (CimiMachineVolume) context.convertToFullCimi(dataService, CimiMachineVolume.class);
             this.merge(cimiRef, cimi);
         } else {
             this.merge(context, cimi.getVolume());
         }
     }
 
     @Override
     public void merge(final CimiContext context, final CimiVolume cimi) throws Exception {
         if (true == cimi.hasReference()) {
             Volume dataService = this.managerVolume.getVolumeById(PathHelper.extractIdString(cimi.getHref()));
             CimiVolume cimiRef = (CimiVolume) context.convertToFullCimi(dataService, CimiVolume.class);
             this.merge(cimiRef, cimi);
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiMachineNetworkInterface)
      */
     @Override
     public void merge(final CimiContext context, final CimiMachineNetworkInterface cimi) throws Exception {
         if (true == cimi.hasReference()) {
             MachineNetworkInterface dataService = this.managerMachine.getNetworkInterfaceFromMachine(context.getRequest()
                 .getIdParent(), PathHelper.extractIdString(cimi.getHref()));
             CimiMachineNetworkInterface cimiRef = (CimiMachineNetworkInterface) context.convertToFullCimi(dataService,
                 CimiMachineNetworkInterface.class);
             this.merge(cimiRef, cimi);
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiMachineNetworkInterfaceAddress)
      */
     @Override
     public void merge(final CimiContext context, final CimiMachineNetworkInterfaceAddress cimi) throws Exception {
         if (true == cimi.hasReference()) {
             MachineNetworkInterfaceAddress dataService = null;
             QueryResult<MachineNetworkInterfaceAddress> results = this.managerMachine.getMachineNetworkInterfaceAddresses(
                 context.getRequest().getIds().getId(IdRequest.Type.RESOURCE_GRAND_PARENT), context.getRequest().getIdParent(),
                 -1, -1, null, null);
             if (null != results.getItems()) {
                 Integer id = Integer.valueOf(context.getRequest().getId());
                 for (MachineNetworkInterfaceAddress item : results.getItems()) {
                     if (id == item.getId()) {
                         dataService = item;
                         break;
                     }
                 }
                 if (null == dataService) {
                     throw new ResourceNotFoundException();
                 }
                 CimiMachineNetworkInterfaceAddress cimiRef = (CimiMachineNetworkInterfaceAddress) context.convertToFullCimi(
                     dataService, CimiMachineNetworkInterfaceAddress.class);
                 this.merge(cimiRef, cimi);
             }
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiMachineTemplate)
      */
     @Override
     public void merge(final CimiContext context, final CimiMachineTemplate cimi) throws Exception {
         if (true == cimi.hasReference()) {
             MachineTemplate dataService = this.managerMachine
                 .getMachineTemplateById(PathHelper.extractIdString(cimi.getHref()));
             CimiMachineTemplate cimiRef = (CimiMachineTemplate) context.convertToFullCimi(dataService,
                 CimiMachineTemplate.class);
             this.merge(cimiRef, cimi);
         } else {
             if (null != cimi.getCredential()) {
                 this.merge(context, cimi.getCredential());
             }
             if (null != cimi.getEventLogTemplate()) {
                 this.merge(context, cimi.getEventLogTemplate());
             }
             if (null != cimi.getMachineConfig()) {
                 this.merge(context, cimi.getMachineConfig());
             }
             if (null != cimi.getMachineImage()) {
                 this.merge(context, cimi.getMachineImage());
             }
             // NetworkInterface : nothing to do because none ID in
             // CimiMachineTemplateNetworkInterface
            if (null != cimi.getNetworkInterfaces()) {
                for (CimiMachineTemplateNetworkInterface nic : cimi.getNetworkInterfaces()) {
                    if (nic.getNetwork() != null) {
                        this.merge(context, nic.getNetwork());
                    }
                }
            }
             if (null != cimi.getVolumes()) {
                 for (CimiMachineTemplateVolume item : cimi.getListVolumes()) {
                     this.merge(context, item);
                 }
             }
             if (null != cimi.getVolumeTemplates()) {
                 for (CimiMachineTemplateVolumeTemplate item : cimi.getListVolumeTemplates()) {
                     this.merge(context, item);
                 }
             }
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiVolumeCreate)
      */
     @Override
     public void merge(final CimiContext context, final CimiVolumeCreate cimi) throws Exception {
         this.merge(context, cimi.getVolumeTemplate());
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiVolumeConfiguration)
      */
     @Override
     public void merge(final CimiContext context, final CimiVolumeConfiguration cimi) throws Exception {
         if (true == cimi.hasReference()) {
             VolumeConfiguration dataService = this.managerVolume.getVolumeConfigurationById(PathHelper.extractIdString(cimi
                 .getHref()));
             CimiVolumeConfiguration cimiRef = (CimiVolumeConfiguration) context.convertToFullCimi(dataService,
                 CimiVolumeConfiguration.class);
             this.merge(cimiRef, cimi);
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiVolumeImage)
      */
     @Override
     public void merge(final CimiContext context, final CimiVolumeImage cimi) throws Exception {
         if (true == cimi.hasReference()) {
             VolumeImage dataService = this.managerVolume.getVolumeImageById(PathHelper.extractIdString(cimi.getHref()));
             CimiVolumeImage cimiRef = (CimiVolumeImage) context.convertToFullCimi(dataService, CimiVolumeImage.class);
             this.merge(cimiRef, cimi);
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiVolumeTemplate)
      */
     @Override
     public void merge(final CimiContext context, final CimiVolumeTemplate cimi) throws Exception {
         if (true == cimi.hasReference()) {
             VolumeTemplate dataService = this.managerVolume.getVolumeTemplateById(PathHelper.extractIdString(cimi.getHref()));
             CimiVolumeTemplate cimiRef = (CimiVolumeTemplate) context.convertToFullCimi(dataService, CimiVolumeTemplate.class);
             this.merge(cimiRef, cimi);
         } else {
             if (null != cimi.getEventLogTemplate()) {
                 this.merge(context, cimi.getEventLogTemplate());
             }
             if (null != cimi.getVolumeConfig()) {
                 this.merge(context, cimi.getVolumeConfig());
             }
             if (null != cimi.getVolumeImage()) {
                 this.merge(context, cimi.getVolumeImage());
             }
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiVolumeVolumeImage)
      */
     @Override
     public void merge(final CimiContext context, final CimiVolumeVolumeImage cimi) throws Exception {
         if (true == cimi.hasReference()) {
             VolumeVolumeImage dataService = this.managerVolume.getVolumeImageFromVolume(context.getRequest().getIdParent(),
                 PathHelper.extractIdString(cimi.getHref()));
             CimiVolumeVolumeImage cimiRef = (CimiVolumeVolumeImage) context.convertToFullCimi(dataService,
                 CimiVolumeVolumeImage.class);
             this.merge(cimiRef, cimi);
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiSystemCreate)
      */
     @Override
     public void merge(final CimiContext context, final CimiSystemCreate cimi) throws Exception {
         this.merge(context, cimi.getSystemTemplate());
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiSystemTemplate)
      */
     @Override
     public void merge(final CimiContext context, final CimiSystemTemplate cimi) throws Exception {
         if (true == cimi.hasReference()) {
             SystemTemplate dataService = this.managerSystem.getSystemTemplateById(PathHelper.extractIdString(cimi.getHref()));
             CimiSystemTemplate cimiRef = (CimiSystemTemplate) context.convertToFullCimi(dataService, CimiSystemTemplate.class);
             this.merge(cimiRef, cimi);
         } else {
             if (null != cimi.getListComponentDescriptors()) {
                 for (CimiComponentDescriptor component : cimi.getListComponentDescriptors()) {
                     switch (component.getComponent().getExchangeType()) {
                     case CredentialTemplate:
                         this.merge(context, component.getCredentialTemplate());
                         break;
                     case MachineTemplate:
                         this.merge(context, component.getMachineTemplate());
                         break;
                     case SystemTemplate:
                         this.merge(context, component.getSystemTemplate());
                         break;
                     case VolumeTemplate:
                         this.merge(context, component.getVolumeTemplate());
                         break;
                     default:
                         throw new ConfigurationException("This type [" + component.getComponent().getExchangeType().toString()
                             + "] is unknown. Merging is impossible.");
                     }
                 }
             }
             if (null != cimi.getEventLogTemplate()) {
                 this.merge(context, cimi.getEventLogTemplate());
             }
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiSystemCredential)
      */
     @Override
     public void merge(final CimiContext context, final CimiSystemCredential cimi) throws Exception {
         if (true == cimi.hasReference()) {
             CloudCollectionItem dataService = this.managerSystem.getEntityFromSystem(context.getRequest().getIdParent(),
                 PathHelper.extractIdString(cimi.getHref()));
             CimiSystemCredential cimiRef = (CimiSystemCredential) context.convertToFullCimi(dataService,
                 CimiSystemCredential.class);
             this.merge(cimiRef, cimi);
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiSystemMachine)
      */
     @Override
     public void merge(final CimiContext context, final CimiSystemMachine cimi) throws Exception {
         if (true == cimi.hasReference()) {
             CloudCollectionItem dataService = this.managerSystem.getEntityFromSystem(context.getRequest().getIdParent(),
                 PathHelper.extractIdString(cimi.getHref()));
             CimiSystemMachine cimiRef = (CimiSystemMachine) context.convertToFullCimi(dataService, CimiSystemMachine.class);
             this.merge(cimiRef, cimi);
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiSystemSystem)
      */
     @Override
     public void merge(final CimiContext context, final CimiSystemSystem cimi) throws Exception {
         if (true == cimi.hasReference()) {
             CloudCollectionItem dataService = this.managerSystem.getEntityFromSystem(context.getRequest().getIdParent(),
                 PathHelper.extractIdString(cimi.getHref()));
             CimiSystemSystem cimiRef = (CimiSystemSystem) context.convertToFullCimi(dataService, CimiSystemSystem.class);
             this.merge(cimiRef, cimi);
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiSystemVolume)
      */
     @Override
     public void merge(final CimiContext context, final CimiSystemVolume cimi) throws Exception {
         if (true == cimi.hasReference()) {
             CloudCollectionItem dataService = this.managerSystem.getEntityFromSystem(context.getRequest().getIdParent(),
                 PathHelper.extractIdString(cimi.getHref()));
             CimiSystemVolume cimiRef = (CimiSystemVolume) context.convertToFullCimi(dataService, CimiSystemVolume.class);
             this.merge(cimiRef, cimi);
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiSystemAddress)
      */
     @Override
     public void merge(final CimiContext context, final CimiSystemAddress cimi) throws Exception {
         if (true == cimi.hasReference()) {
             CloudCollectionItem dataService = this.managerSystem.getEntityFromSystem(context.getRequest().getIdParent(),
                 PathHelper.extractIdString(cimi.getHref()));
             CimiSystemAddress cimiRef = (CimiSystemAddress) context.convertToFullCimi(dataService, CimiSystemAddress.class);
             this.merge(cimiRef, cimi);
         }
 
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiSystemForwardingGroup)
      */
     @Override
     public void merge(final CimiContext context, final CimiSystemForwardingGroup cimi) throws Exception {
         if (true == cimi.hasReference()) {
             CloudCollectionItem dataService = this.managerSystem.getEntityFromSystem(context.getRequest().getIdParent(),
                 PathHelper.extractIdString(cimi.getHref()));
             CimiSystemForwardingGroup cimiRef = (CimiSystemForwardingGroup) context.convertToFullCimi(dataService,
                 CimiSystemForwardingGroup.class);
             this.merge(cimiRef, cimi);
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiSystemNetwork)
      */
     @Override
     public void merge(final CimiContext context, final CimiSystemNetwork cimi) throws Exception {
         if (true == cimi.hasReference()) {
             CloudCollectionItem dataService = this.managerSystem.getEntityFromSystem(context.getRequest().getIdParent(),
                 PathHelper.extractIdString(cimi.getHref()));
             CimiSystemNetwork cimiRef = (CimiSystemNetwork) context.convertToFullCimi(dataService, CimiSystemNetwork.class);
             this.merge(cimiRef, cimi);
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiSystemNetworkPort)
      */
     @Override
     public void merge(final CimiContext context, final CimiSystemNetworkPort cimi) throws Exception {
         if (true == cimi.hasReference()) {
             CloudCollectionItem dataService = this.managerSystem.getEntityFromSystem(context.getRequest().getIdParent(),
                 PathHelper.extractIdString(cimi.getHref()));
             CimiSystemNetworkPort cimiRef = (CimiSystemNetworkPort) context.convertToFullCimi(dataService,
                 CimiSystemNetworkPort.class);
             this.merge(cimiRef, cimi);
         }
     }
 
     /**
      * Merge the reference of a resource only if necessary.
      * 
      * @param context The working context
      * @param cimi The resource with values or reference
      * @throws Exception If error in call service
      */
     protected void merge(final CimiContext context, final CimiNetwork cimi) throws Exception {
         if (true == cimi.hasReference()) {
             Network dataService = this.managerNetwork.getNetworkById(PathHelper.extractIdString(cimi.getHref()));
             CimiNetwork cimiRef = (CimiNetwork) context.convertToFullCimi(dataService, CimiNetwork.class);
             this.merge(cimiRef, cimi);
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiNetworkNetworkPort)
      */
     @Override
     public void merge(final CimiContext context, final CimiNetworkNetworkPort cimi) throws Exception {
         if (true == cimi.hasReference()) {
             NetworkNetworkPort dataService = this.managerNetwork.getNetworkPortFromNetwork(context.getRequest().getIdParent(),
                 PathHelper.extractIdString(cimi.getHref()));
             CimiNetworkNetworkPort cimiRef = (CimiNetworkNetworkPort) context.convertToFullCimi(dataService,
                 CimiNetworkNetworkPort.class);
             this.merge(cimiRef, cimi);
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiNetworkCreate)
      */
     @Override
     public void merge(final CimiContext context, final CimiNetworkCreate cimi) throws Exception {
         this.merge(context, cimi.getNetworkTemplate());
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiNetworkConfiguration)
      */
     @Override
     public void merge(final CimiContext context, final CimiNetworkConfiguration cimi) throws Exception {
         if (true == cimi.hasReference()) {
             NetworkConfiguration dataService = this.managerNetwork.getNetworkConfigurationById(PathHelper.extractIdString(cimi
                 .getHref()));
             CimiNetworkConfiguration cimiRef = (CimiNetworkConfiguration) context.convertToFullCimi(dataService,
                 CimiNetworkConfiguration.class);
             this.merge(cimiRef, cimi);
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiNetworkTemplate)
      */
     @Override
     public void merge(final CimiContext context, final CimiNetworkTemplate cimi) throws Exception {
         if (true == cimi.hasReference()) {
             NetworkTemplate dataService = this.managerNetwork
                 .getNetworkTemplateById(PathHelper.extractIdString(cimi.getHref()));
             CimiNetworkTemplate cimiRef = (CimiNetworkTemplate) context.convertToFullCimi(dataService,
                 CimiNetworkTemplate.class);
             this.merge(cimiRef, cimi);
         } else {
             if (null != cimi.getEventLogTemplate()) {
                 this.merge(context, cimi.getEventLogTemplate());
             }
             if (null != cimi.getForwardingGroup()) {
                 this.merge(context, cimi.getForwardingGroup());
             }
             if (null != cimi.getNetworkConfig()) {
                 this.merge(context, cimi.getNetworkConfig());
             }
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiNetworkPortCreate)
      */
     @Override
     public void merge(final CimiContext context, final CimiNetworkPortCreate cimi) throws Exception {
         this.merge(context, cimi.getNetworkPortTemplate());
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiNetworkPortConfiguration)
      */
     @Override
     public void merge(final CimiContext context, final CimiNetworkPortConfiguration cimi) throws Exception {
         if (true == cimi.hasReference()) {
             NetworkPortConfiguration dataService = this.managerNetwork.getNetworkPortConfigurationById(PathHelper
                 .extractIdString(cimi.getHref()));
             CimiNetworkPortConfiguration cimiRef = (CimiNetworkPortConfiguration) context.convertToFullCimi(dataService,
                 CimiNetworkPortConfiguration.class);
             this.merge(cimiRef, cimi);
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiNetworkPortTemplate)
      */
     @Override
     public void merge(final CimiContext context, final CimiNetworkPortTemplate cimi) throws Exception {
         if (true == cimi.hasReference()) {
             NetworkPortTemplate dataService = this.managerNetwork.getNetworkPortTemplateById(PathHelper.extractIdString(cimi
                 .getHref()));
             CimiNetworkPortTemplate cimiRef = (CimiNetworkPortTemplate) context.convertToFullCimi(dataService,
                 CimiNetworkPortTemplate.class);
             this.merge(cimiRef, cimi);
         } else {
             if (null != cimi.getEventLogTemplate()) {
                 this.merge(context, cimi.getEventLogTemplate());
             }
             if (null != cimi.getNetwork()) {
                 this.merge(context, cimi.getNetwork());
             }
             if (null != cimi.getNetworkPortConfig()) {
                 this.merge(context, cimi.getNetworkPortConfig());
             }
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiAddressCreate)
      */
     @Override
     public void merge(final CimiContext context, final CimiAddressCreate cimi) throws Exception {
         this.merge(context, cimi.getAddressTemplate());
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiAddressTemplate)
      */
     @Override
     public void merge(final CimiContext context, final CimiAddressTemplate cimi) throws Exception {
         if (true == cimi.hasReference()) {
             AddressTemplate dataService = this.managerNetwork
                 .getAddressTemplateById(PathHelper.extractIdString(cimi.getHref()));
             CimiAddressTemplate cimiRef = (CimiAddressTemplate) context.convertToFullCimi(dataService,
                 CimiAddressTemplate.class);
             this.merge(cimiRef, cimi);
         } else {
             if (null != cimi.getNetwork()) {
                 this.merge(context, cimi.getNetwork());
             }
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiForwardingGroupCreate)
      */
     @Override
     public void merge(final CimiContext context, final CimiForwardingGroupCreate cimi) throws Exception {
         this.merge(context, cimi.getForwardingGroupTemplate());
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiForwardingGroupTemplate)
      */
     @Override
     public void merge(final CimiContext context, final CimiForwardingGroupTemplate cimi) throws Exception {
         if (true == cimi.hasReference()) {
             ForwardingGroupTemplate dataService = this.managerNetwork.getForwardingGroupTemplateById(PathHelper
                 .extractIdString(cimi.getHref()));
             CimiForwardingGroupTemplate cimiRef = (CimiForwardingGroupTemplate) context.convertToFullCimi(dataService,
                 CimiForwardingGroupTemplate.class);
             this.merge(cimiRef, cimi);
         } else {
             if (null != cimi.getNetworks()) {
                 for (CimiNetwork item : cimi.getListNetworks()) {
                     this.merge(context, item);
                 }
             }
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiForwardingGroupNetwork)
      */
     @Override
     public void merge(final CimiContext context, final CimiForwardingGroupNetwork cimi) throws Exception {
         if (true == cimi.hasReference()) {
             ForwardingGroupNetwork dataService = this.managerNetwork.getNetworkFromForwardingGroup(context.getRequest()
                 .getIdParent(), PathHelper.extractIdString(cimi.getHref()));
             CimiForwardingGroupNetwork cimiRef = (CimiForwardingGroupNetwork) context.convertToFullCimi(dataService,
                 CimiForwardingGroupNetwork.class);
             this.merge(cimiRef, cimi);
         }
     }
 
     /**
      * Merge the reference of a resource only if necessary.
      * 
      * @param context The working context
      * @param cimi The resource with values or reference
      * @throws Exception If error in call service
      */
     protected void merge(final CimiContext context, final CimiForwardingGroup cimi) throws Exception {
         if (true == cimi.hasReference()) {
             ForwardingGroup dataService = this.managerNetwork
                 .getForwardingGroupById(PathHelper.extractIdString(cimi.getHref()));
             CimiForwardingGroup cimiRef = (CimiForwardingGroup) context.convertToFullCimi(dataService,
                 CimiForwardingGroup.class);
             this.merge(cimiRef, cimi);
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiEventLogCreate)
      */
     @Override
     public void merge(final CimiContext context, final CimiEventLogCreate cimi) throws Exception {
         this.merge(context, cimi.getEventLogTemplate());
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper#merge(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      org.ow2.sirocco.cimi.domain.CimiEventLogTemplate)
      */
     @Override
     public void merge(final CimiContext context, final CimiEventLogTemplate cimi) throws Exception {
         if (true == cimi.hasReference()) {
             EventLogTemplate dataService = this.managerEvent
                 .getEventLogTemplateById(PathHelper.extractIdString(cimi.getHref()));
             CimiEventLogTemplate cimiRef = (CimiEventLogTemplate) context.convertToFullCimi(dataService,
                 CimiEventLogTemplate.class);
             this.merge(cimiRef, cimi);
         }
     }
 
     /**
      * Make a map by ExchangeType and ComponentDescriptors with all components
      * of SystemTemplate.
      * 
      * @param cimi The SystemTemplate
      * @return The map
      */
     protected Map<ExchangeType, CimiComponentDescriptor> makeMapComponents(final CimiSystemTemplate cimi) {
         Map<ExchangeType, CimiComponentDescriptor> map = new HashMap<ExchangeType, CimiComponentDescriptor>();
         if (null != cimi.getListComponentDescriptors()) {
             for (CimiComponentDescriptor cimiComponent : cimi.getListComponentDescriptors()) {
                 map.put(cimiComponent.getComponent().getExchangeType(), cimiComponent);
             }
         }
         return map;
     }
 
     /**
      * Merge SystemTemplate resource data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiSystemTemplate cimiRef, final CimiSystemTemplate cimi) {
         if (null != cimiRef) {
             // Merge common data
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getEventLogTemplate()) {
                 cimi.setEventLogTemplate(cimiRef.getEventLogTemplate());
             } else {
                 this.merge(cimiRef.getEventLogTemplate(), cimi.getEventLogTemplate());
             }
 
             if (null == cimi.getComponentDescriptors()) {
                 cimi.setComponentDescriptors(cimiRef.getComponentDescriptors());
             }
         }
     }
 
     /**
      * Merge CimiComponentDescriptor data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiComponentDescriptor cimiRef, final CimiComponentDescriptor cimi) {
         if (null != cimiRef) {
             this.mergeDataCommon(cimiRef, cimi);
             if (null == cimi.getQuantity()) {
                 cimi.setQuantity(cimiRef.getQuantity());
             }
             if (null == cimi.getType()) {
                 cimi.setType(cimiRef.getType());
             }
             if (null == cimi.getComponent()) {
                 cimi.setComponent(cimiRef.getComponent());
             } else {
                 for (ExchangeType type : CimiComponentDescriptor.TYPE_DESCRIPTORS) {
                     switch (type) {
                     case CredentialTemplate:
                         this.merge(cimiRef.getCredentialTemplate(), cimi.getCredentialTemplate());
                         break;
                     case MachineTemplate:
                         this.merge(cimiRef.getMachineTemplate(), cimi.getMachineTemplate());
                         break;
                     case SystemTemplate:
                         this.merge(cimiRef.getSystemTemplate(), cimi.getSystemTemplate());
                         break;
                     case VolumeTemplate:
                         this.merge(cimiRef.getVolumeTemplate(), cimi.getVolumeTemplate());
                         break;
                     default:
                         throw new ConfigurationException("This type is unknown : " + type.toString());
                     }
                 }
             }
         }
     }
 
     /**
      * Merge MachineTemplate resource data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiMachineTemplate cimiRef, final CimiMachineTemplate cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getCredential()) {
                 cimi.setCredential(cimiRef.getCredential());
             } else {
                 this.merge(cimiRef.getCredential(), cimi.getCredential());
             }
             if (null == cimi.getEventLogTemplate()) {
                 cimi.setEventLogTemplate(cimiRef.getEventLogTemplate());
             } else {
                 this.merge(cimiRef.getEventLogTemplate(), cimi.getEventLogTemplate());
             }
             if (null == cimi.getInitialState()) {
                 cimi.setInitialState(cimiRef.getInitialState());
             }
             if (null == cimi.getMachineConfig()) {
                 cimi.setMachineConfig(cimiRef.getMachineConfig());
             } else {
                 this.merge(cimiRef.getMachineConfig(), cimi.getMachineConfig());
             }
             if (null == cimi.getMachineImage()) {
                 cimi.setMachineImage(cimiRef.getMachineImage());
             } else {
                 this.merge(cimiRef.getMachineImage(), cimi.getMachineImage());
             }
             if (null == cimi.getNetworkInterfaces()) {
                 cimi.setNetworkInterfaces(cimiRef.getNetworkInterfaces());
             } else {
                 this.mergeMTNI(cimiRef, cimi);
             }
             if (null == cimi.getUserData()) {
                 cimi.setUserData(cimiRef.getUserData());
             }
             if (null == cimi.getVolumes()) {
                 cimi.setVolumes(cimiRef.getVolumes());
             } else {
                 this.merge(cimiRef.getListVolumes(), cimi.getListVolumes());
             }
             if (null == cimi.getVolumeTemplates()) {
                 cimi.setVolumeTemplates(cimiRef.getVolumeTemplates());
             } else {
                 this.merge(cimiRef.getListVolumeTemplates(), cimi.getListVolumeTemplates());
             }
         }
     }
 
     /**
      * Merge the reference of a resource only if necessary.
      * 
      * @param context The working context
      * @param cimi The resource with values or reference
      * @throws Exception If error in call service
      */
     protected void merge(final CimiContext context, final CimiMachineTemplateVolume cimi) throws Exception {
         if (true == cimi.hasReference()) {
             Volume dataService = this.managerVolume.getVolumeById(PathHelper.extractIdString(cimi.getHref()));
             CimiVolume cimiRef = (CimiVolume) context.convertToFullCimi(dataService, CimiVolume.class);
             this.merge(cimiRef, cimi);
         }
     }
 
     /**
      * Merge the reference of a resource only if necessary.
      * 
      * @param context The working context
      * @param cimi The resource with values or reference
      * @throws Exception If error in call service
      */
     protected void merge(final CimiContext context, final CimiMachineTemplateVolumeTemplate cimi) throws Exception {
         if (true == cimi.hasReference()) {
             VolumeTemplate dataService = this.managerVolume.getVolumeTemplateById(PathHelper.extractIdString(cimi.getHref()));
             CimiVolumeTemplate cimiRef = (CimiVolumeTemplate) context.convertToFullCimi(dataService, CimiVolumeTemplate.class);
             this.merge(cimiRef, cimi);
         }
     }
 
     /**
      * Merge Volume resource data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiVolume cimiRef, final CimiVolume cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getBootable()) {
                 cimi.setBootable(cimiRef.getBootable());
             }
             if (null == cimi.getCapacity()) {
                 cimi.setCapacity(cimiRef.getCapacity());
             }
             if (null == cimi.getImages()) {
                 cimi.setImages(cimiRef.getImages());
             } else {
                 this.merge(cimiRef.getImages(), cimi.getImages());
             }
             if (null == cimi.getState()) {
                 cimi.setState(cimiRef.getState());
             }
             if (null == cimi.getType()) {
                 cimi.setType(cimiRef.getType());
             }
         }
     }
 
     /**
      * Merge VolumeConfiguration resource data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiVolumeConfiguration cimiRef, final CimiVolumeConfiguration cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getCapacity()) {
                 cimi.setCapacity(cimiRef.getCapacity());
             }
             if (null == cimi.getFormat()) {
                 cimi.setFormat(cimiRef.getFormat());
             }
             if (null == cimi.getType()) {
                 cimi.setType(cimiRef.getType());
             }
         }
     }
 
     /**
      * Merge VolumeImage resource data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiVolumeImage cimiRef, final CimiVolumeImage cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getBootable()) {
                 cimi.setBootable(cimiRef.getBootable());
             }
             if (null == cimi.getImageLocation()) {
                 cimi.setImageLocation(cimiRef.getImageLocation());
             }
             if (null == cimi.getState()) {
                 cimi.setState(cimiRef.getState());
             }
         }
     }
 
     /**
      * Merge VolumeVolumeImage resource data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiVolumeVolumeImage cimiRef, final CimiVolumeVolumeImage cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getVolumeImage()) {
                 cimi.setVolumeImage(cimiRef.getVolumeImage());
             } else {
                 this.merge(cimiRef.getVolumeImage(), cimi.getVolumeImage());
             }
         }
     }
 
     /**
      * Merge VolumeTemplate resource data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiVolumeTemplate cimiRef, final CimiVolumeTemplate cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getEventLogTemplate()) {
                 cimi.setEventLogTemplate(cimiRef.getEventLogTemplate());
             } else {
                 this.merge(cimiRef.getEventLogTemplate(), cimi.getEventLogTemplate());
             }
             if (null == cimi.getVolumeConfig()) {
                 cimi.setVolumeConfig(cimiRef.getVolumeConfig());
             } else {
                 this.merge(cimiRef.getVolumeConfig(), cimi.getVolumeConfig());
             }
             if (null == cimi.getVolumeImage()) {
                 cimi.setVolumeImage(cimiRef.getVolumeImage());
             } else {
                 this.merge(cimiRef.getVolumeImage(), cimi.getVolumeImage());
             }
         }
     }
 
     /**
      * Merge CimiCollection<E extends CimiResource> resource data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected <E extends CimiResource> void merge(final CimiCollection<E> cimiRef, final CimiCollection<E> cimi) {
         if (null != cimiRef) {
             if (null != cimiRef.getCollection()) {
                 this.merge(cimiRef.getCollection(), cimi.getCollection());
             }
         }
     }
 
     /**
      * Merge List<E extends CimiResource> resource data.
      * 
      * @param <E> CimiResource
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected <E extends CimiResource> void merge(final List<E> cimiRef, final List<E> cimi) {
         if (null != cimiRef) {
             for (E item : cimiRef) {
                 if (false == this.containsId(item.getId(), cimi)) {
                     cimi.add(item);
                 }
             }
         }
     }
 
     private boolean containsId(final String id, final Collection<? extends CimiResource> collection) {
         boolean contains = false;
         if (null != collection) {
             for (CimiResource item : collection) {
                 if (true == id.equals(item.getId())) {
                     contains = true;
                     break;
                 }
             }
         }
         return contains;
     }
 
     /**
      * Merge List<MachineTemplateNetworkInterface> resource data.
      * <p>
      * Because there is none identifier, all references are added
      * </p>
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void mergeMTNI(final CimiMachineTemplate cimiRef, final CimiMachineTemplate cimi) {
         List<CimiMachineTemplateNetworkInterface> mtni = cimi.getListNetworkInterfaces();
         List<CimiMachineTemplateNetworkInterface> mtniRef = cimiRef.getListNetworkInterfaces();
         if (null != mtniRef) {
             if (null == mtni) {
                 mtni = new ArrayList<CimiMachineTemplateNetworkInterface>();
                 cimi.setListNetworkInterfaces(mtni);
             }
             for (CimiMachineTemplateNetworkInterface item : mtniRef) {
                 mtni.add(item);
             }
         }
     }
 
     /**
      * Merge Credential resource data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiCredential cimiRef, final CimiCredential cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getExtensionAttribute("key")) {
                 cimi.addExtensionAttribute("key", cimiRef.getExtensionAttribute("key"));
             }
             if (null == cimi.getExtensionAttribute("password")) {
                 cimi.addExtensionAttribute("password", cimiRef.getExtensionAttribute("password"));
             }
             if (null == cimi.getExtensionAttribute("userName")) {
                 cimi.addExtensionAttribute("userName", cimiRef.getExtensionAttribute("userName"));
             }
         }
     }
 
     /**
      * Merge MachineImage resource data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiMachineImage cimiRef, final CimiMachineImage cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getImageLocation()) {
                 cimi.setImageLocation(cimiRef.getImageLocation());
             }
             if (null == cimi.getRelatedImage()) {
                 cimi.setRelatedImage(cimiRef.getRelatedImage());
             }
         }
     }
 
     /**
      * Merge MachineConfiguration resource data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiMachineConfiguration cimiRef, final CimiMachineConfiguration cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getCpu()) {
                 cimi.setCpu(cimiRef.getCpu());
             }
             if (null == cimi.getMemory()) {
                 cimi.setMemory(cimiRef.getMemory());
             }
             if ((null != cimiRef.getDisks()) && (cimiRef.getDisks().length > 0)) {
                 if (null == cimi.getDisks()) {
                     cimi.setDisks(cimiRef.getDisks());
                 } else {
                     List<CimiDiskConfiguration> list = new ArrayList<CimiDiskConfiguration>();
                     for (CimiDiskConfiguration disk : cimiRef.getDisks()) {
                         list.add(disk);
                     }
                     for (CimiDiskConfiguration disk : cimi.getDisks()) {
                         list.add(disk);
                     }
                     cimi.setDisks(list.toArray(new CimiDiskConfiguration[list.size()]));
                 }
             }
         }
     }
 
     /**
      * Merge common data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void mergeObjectCommon(final CimiObjectCommon cimiRef, final CimiObjectCommon cimi) {
         this.mergeDataCommon(cimiRef, cimi);
         if (null == cimi.getId()) {
             cimi.setId(cimiRef.getId());
         }
         if (null == cimi.getResourceURI()) {
             cimi.setResourceURI(cimiRef.getResourceURI());
         }
     }
 
     /**
      * Merge common data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void mergeDataCommon(final CimiDataCommon cimiRef, final CimiDataCommon cimi) {
         if (null == cimi.getDescription()) {
             cimi.setDescription(cimiRef.getDescription());
         }
         if (null == cimi.getName()) {
             cimi.setName(cimiRef.getName());
         }
         if (null == cimi.getProperties()) {
             if ((null != cimiRef.getProperties()) && (cimiRef.getProperties().size() > 0)) {
                 cimi.setProperties(cimiRef.getProperties());
             }
         } else {
             Map<String, String> cimiProps = cimi.getProperties();
             if (null != cimiRef.getProperties()) {
                 for (Entry<String, String> entryRef : cimiRef.getProperties().entrySet()) {
                     if (false == cimiProps.containsKey(entryRef.getKey())) {
                         cimiProps.put(entryRef.getKey(), entryRef.getValue());
                     }
                 }
             }
         }
     }
 
     /**
      * Merge Machine resource data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiMachine cimiRef, final CimiMachine cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getCpu()) {
                 cimi.setCpu(cimiRef.getCpu());
             }
             if (null == cimi.getCpuArch()) {
                 cimi.setCpuArch(cimiRef.getCpuArch());
             }
             if (null == cimi.getMemory()) {
                 cimi.setMemory(cimiRef.getMemory());
             }
             if (null == cimi.getDisks()) {
                 cimi.setDisks(cimiRef.getDisks());
             } else {
                 this.merge(cimiRef.getDisks(), cimi.getDisks());
             }
             if (null == cimi.getNetworkInterfaces()) {
                 cimi.setNetworkInterfaces(cimiRef.getNetworkInterfaces());
             } else {
                 this.merge(cimiRef.getNetworkInterfaces(), cimi.getNetworkInterfaces());
             }
             if (null == cimi.getVolumes()) {
                 cimi.setVolumes(cimiRef.getVolumes());
             } else {
                 this.merge(cimiRef.getVolumes(), cimi.getVolumes());
             }
             if (null == cimi.getState()) {
                 cimi.setState(cimiRef.getState());
             }
         }
     }
 
     /**
      * Merge machine disk data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiMachineDisk cimiRef, final CimiMachineDisk cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getCapacity()) {
                 cimi.setCapacity(cimiRef.getCapacity());
             }
             if (null == cimi.getInitialLocation()) {
                 cimi.setInitialLocation(cimiRef.getInitialLocation());
             }
         }
     }
 
     /**
      * Merge machine volume data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiMachineVolume cimiRef, final CimiMachineVolume cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getInitialLocation()) {
                 cimi.setInitialLocation(cimiRef.getInitialLocation());
             }
             if (null == cimi.getVolume()) {
                 cimi.setVolume(cimiRef.getVolume());
             } else {
                 this.merge(cimiRef.getVolume(), cimi.getVolume());
             }
         }
     }
 
     /**
      * Merge machine network data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiMachineNetworkInterface cimiRef, final CimiMachineNetworkInterface cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getAddresses()) {
                 cimi.setAddresses(cimiRef.getAddresses());
             } else {
                 this.merge(cimiRef.getAddresses(), cimi.getAddresses());
             }
             if (null == cimi.getMacAddress()) {
                 cimi.setMacAddress(cimiRef.getMacAddress());
             }
             if (null == cimi.getMtu()) {
                 cimi.setMtu(cimiRef.getMtu());
             }
             if (null == cimi.getState()) {
                 cimi.setState(cimiRef.getState());
             }
             if (null == cimi.getNetwork()) {
                 cimi.setNetwork(cimiRef.getNetwork());
             } else {
                 this.merge(cimiRef.getNetwork(), cimi.getNetwork());
             }
             if (null == cimi.getNetworkPort()) {
                 cimi.setNetworkPort(cimiRef.getNetworkPort());
             } else {
                 this.merge(cimiRef.getNetworkPort(), cimi.getNetworkPort());
             }
         }
     }
 
     /**
      * Merge machine network data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiMachineNetworkInterfaceAddress cimiRef, final CimiMachineNetworkInterfaceAddress cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getAddress()) {
                 cimi.setAddress(cimiRef.getAddress());
             } else {
                 this.merge(cimiRef.getAddress(), cimi.getAddress());
             }
         }
     }
 
     /**
      * Merge System resource data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiSystem cimiRef, final CimiSystem cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getCredentials()) {
                 cimi.setCredentials(cimiRef.getCredentials());
             } else {
                 this.merge(cimiRef.getCredentials(), cimi.getCredentials());
             }
             if (null == cimi.getMachines()) {
                 cimi.setMachines(cimiRef.getMachines());
             } else {
                 this.merge(cimiRef.getMachines(), cimi.getMachines());
             }
             if (null == cimi.getSystems()) {
                 cimi.setSystems(cimiRef.getSystems());
             } else {
                 this.merge(cimiRef.getSystems(), cimi.getSystems());
             }
             if (null == cimi.getVolumes()) {
                 cimi.setVolumes(cimiRef.getVolumes());
             } else {
                 this.merge(cimiRef.getVolumes(), cimi.getVolumes());
             }
             if (null == cimi.getState()) {
                 cimi.setState(cimiRef.getState());
             }
         }
     }
 
     /**
      * Merge system credential data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiSystemCredential cimiRef, final CimiSystemCredential cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getCredential()) {
                 cimi.setCredential(cimiRef.getCredential());
             } else {
                 this.merge(cimiRef.getCredential(), cimi.getCredential());
             }
         }
     }
 
     /**
      * Merge system machine data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiSystemMachine cimiRef, final CimiSystemMachine cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getMachine()) {
                 cimi.setMachine(cimiRef.getMachine());
             } else {
                 this.merge(cimiRef.getMachine(), cimi.getMachine());
             }
         }
     }
 
     /**
      * Merge system system data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiSystemSystem cimiRef, final CimiSystemSystem cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getSystem()) {
                 cimi.setSystem(cimiRef.getSystem());
             } else {
                 this.merge(cimiRef.getSystem(), cimi.getSystem());
             }
         }
     }
 
     /**
      * Merge system volume data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiSystemVolume cimiRef, final CimiSystemVolume cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getVolume()) {
                 cimi.setVolume(cimiRef.getVolume());
             } else {
                 this.merge(cimiRef.getVolume(), cimi.getVolume());
             }
         }
     }
 
     /**
      * Merge system Address data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiSystemAddress cimiRef, final CimiSystemAddress cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getAddress()) {
                 cimi.setAddress(cimiRef.getAddress());
             } else {
                 this.merge(cimiRef.getAddress(), cimi.getAddress());
             }
         }
     }
 
     /**
      * Merge system ForwardingGroup data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiSystemForwardingGroup cimiRef, final CimiSystemForwardingGroup cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getForwardingGroup()) {
                 cimi.setForwardingGroup(cimiRef.getForwardingGroup());
             } else {
                 this.merge(cimiRef.getForwardingGroup(), cimi.getForwardingGroup());
             }
         }
     }
 
     /**
      * Merge system Network data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiSystemNetwork cimiRef, final CimiSystemNetwork cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getNetwork()) {
                 cimi.setNetwork(cimiRef.getNetwork());
             } else {
                 this.merge(cimiRef.getNetwork(), cimi.getNetwork());
             }
         }
     }
 
     /**
      * Merge system NetworkPort data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiSystemNetworkPort cimiRef, final CimiSystemNetworkPort cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getNetworkPort()) {
                 cimi.setNetworkPort(cimiRef.getNetworkPort());
             } else {
                 this.merge(cimiRef.getNetworkPort(), cimi.getNetworkPort());
             }
         }
     }
 
     /**
      * Merge Address resource data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiAddress cimiRef, final CimiAddress cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getAllocation()) {
                 cimi.setAllocation(cimiRef.getAllocation());
             }
             if (null == cimi.getDefaultGateway()) {
                 cimi.setDefaultGateway(cimiRef.getDefaultGateway());
             }
             if (null == cimi.getDns()) {
                 cimi.setDns(cimiRef.getDns());
             }
             if (null == cimi.getHostname()) {
                 cimi.setHostname(cimiRef.getHostname());
             }
             if (null == cimi.getIp()) {
                 cimi.setIp(cimiRef.getIp());
             }
             if (null == cimi.getMask()) {
                 cimi.setMask(cimiRef.getMask());
             }
             if (null == cimi.getNetwork()) {
                 cimi.setNetwork(cimiRef.getNetwork());
             } else {
                 this.merge(cimiRef.getNetwork(), cimi.getNetwork());
             }
             if (null == cimi.getProtocol()) {
                 cimi.setProtocol(cimiRef.getProtocol());
             }
             if (null == cimi.getResource()) {
                 cimi.setResource(cimiRef.getResource());
             }
         }
     }
 
     /**
      * Merge AddressTemplate resource data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiAddressTemplate cimiRef, final CimiAddressTemplate cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getAllocation()) {
                 cimi.setAllocation(cimiRef.getAllocation());
             }
             if (null == cimi.getDefaultGateway()) {
                 cimi.setDefaultGateway(cimiRef.getDefaultGateway());
             }
             if (null == cimi.getDns()) {
                 cimi.setDns(cimiRef.getDns());
             }
             if (null == cimi.getHostname()) {
                 cimi.setHostname(cimiRef.getHostname());
             }
             if (null == cimi.getIp()) {
                 cimi.setIp(cimiRef.getIp());
             }
             if (null == cimi.getMask()) {
                 cimi.setMask(cimiRef.getMask());
             }
             if (null == cimi.getNetwork()) {
                 cimi.setNetwork(cimiRef.getNetwork());
             } else {
                 this.merge(cimiRef.getNetwork(), cimi.getNetwork());
             }
             if (null == cimi.getProtocol()) {
                 cimi.setProtocol(cimiRef.getProtocol());
             }
         }
     }
 
     /**
      * Merge Network resource data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiNetwork cimiRef, final CimiNetwork cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getClassOfService()) {
                 cimi.setClassOfService(cimiRef.getClassOfService());
             }
             if (null == cimi.getForwardingGroup()) {
                 cimi.setForwardingGroup(cimiRef.getForwardingGroup());
             } else {
                 this.merge(cimiRef.getForwardingGroup(), cimi.getForwardingGroup());
             }
             if (null == cimi.getMtu()) {
                 cimi.setMtu(cimiRef.getMtu());
             }
             if (null == cimi.getNetworkPorts()) {
                 cimi.setNetworkPorts(cimiRef.getNetworkPorts());
             } else {
                 this.merge(cimiRef.getNetworkPorts(), cimi.getNetworkPorts());
             }
             if (null == cimi.getNetworkType()) {
                 cimi.setNetworkType(cimiRef.getNetworkType());
             }
             if (null == cimi.getState()) {
                 cimi.setState(cimiRef.getState());
             }
         }
     }
 
     /**
      * Merge NetworkNetworkPort resource data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiNetworkNetworkPort cimiRef, final CimiNetworkNetworkPort cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getNetworkPort()) {
                 cimi.setNetworkPort(cimiRef.getNetworkPort());
             } else {
                 this.merge(cimiRef.getNetworkPort(), cimi.getNetworkPort());
             }
         }
     }
 
     /**
      * Merge NetworkConfiguration resource data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiNetworkConfiguration cimiRef, final CimiNetworkConfiguration cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getClassOfService()) {
                 cimi.setClassOfService(cimiRef.getClassOfService());
             }
             if (null == cimi.getMtu()) {
                 cimi.setMtu(cimiRef.getMtu());
             }
             if (null == cimi.getNetworkType()) {
                 cimi.setNetworkType(cimiRef.getNetworkType());
             }
         }
     }
 
     /**
      * Merge NetworkTemplate resource data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiNetworkTemplate cimiRef, final CimiNetworkTemplate cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getEventLogTemplate()) {
                 cimi.setEventLogTemplate(cimiRef.getEventLogTemplate());
             } else {
                 this.merge(cimiRef.getEventLogTemplate(), cimi.getEventLogTemplate());
             }
             if (null == cimi.getForwardingGroup()) {
                 cimi.setForwardingGroup(cimiRef.getForwardingGroup());
             } else {
                 this.merge(cimiRef.getForwardingGroup(), cimi.getForwardingGroup());
             }
             if (null == cimi.getNetworkConfig()) {
                 cimi.setNetworkConfig(cimiRef.getNetworkConfig());
             } else {
                 this.merge(cimiRef.getNetworkConfig(), cimi.getNetworkConfig());
             }
         }
     }
 
     /**
      * Merge NetworkPort resource data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiNetworkPort cimiRef, final CimiNetworkPort cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getClassOfService()) {
                 cimi.setClassOfService(cimiRef.getClassOfService());
             }
             if (null == cimi.getNetwork()) {
                 cimi.setNetwork(cimiRef.getNetwork());
             } else {
                 this.merge(cimiRef.getNetwork(), cimi.getNetwork());
             }
             if (null == cimi.getPortType()) {
                 cimi.setPortType(cimiRef.getPortType());
             }
             if (null == cimi.getState()) {
                 cimi.setState(cimiRef.getState());
             }
         }
     }
 
     /**
      * Merge NetworkPortConfiguration resource data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiNetworkPortConfiguration cimiRef, final CimiNetworkPortConfiguration cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getClassOfService()) {
                 cimi.setClassOfService(cimiRef.getClassOfService());
             }
             if (null == cimi.getPortType()) {
                 cimi.setPortType(cimiRef.getPortType());
             }
         }
     }
 
     /**
      * Merge NetworkPortTemplate resource data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiNetworkPortTemplate cimiRef, final CimiNetworkPortTemplate cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getEventLogTemplate()) {
                 cimi.setEventLogTemplate(cimiRef.getEventLogTemplate());
             } else {
                 this.merge(cimiRef.getEventLogTemplate(), cimi.getEventLogTemplate());
             }
             if (null == cimi.getNetwork()) {
                 cimi.setNetwork(cimiRef.getNetwork());
             } else {
                 this.merge(cimiRef.getNetwork(), cimi.getNetwork());
             }
             if (null == cimi.getNetworkPortConfig()) {
                 cimi.setNetworkPortConfig(cimiRef.getNetworkPortConfig());
             } else {
                 this.merge(cimiRef.getNetworkPortConfig(), cimi.getNetworkPortConfig());
             }
         }
     }
 
     /**
      * Merge ForwardingGroup resource data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiForwardingGroup cimiRef, final CimiForwardingGroup cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getNetworks()) {
                 cimi.setNetworks(cimiRef.getNetworks());
             } else {
                 this.merge(cimiRef.getNetworks(), cimi.getNetworks());
             }
         }
     }
 
     /**
      * Merge ForwardingGroupTemplate resource data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiForwardingGroupTemplate cimiRef, final CimiForwardingGroupTemplate cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getNetworks()) {
                 cimi.setNetworks(cimiRef.getNetworks());
             } else {
                 this.merge(cimiRef.getListNetworks(), cimi.getListNetworks());
             }
         }
     }
 
     /**
      * Merge ForwardingGroupNetwork resource data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiForwardingGroupNetwork cimiRef, final CimiForwardingGroupNetwork cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getNetwork()) {
                 cimi.setNetwork(cimiRef.getNetwork());
             } else {
                 this.merge(cimiRef.getNetwork(), cimi.getNetwork());
             }
         }
     }
 
     /**
      * Merge EventLogTemplate resource data.
      * 
      * @param cimiRef Source to merge
      * @param cimi Merged destination
      */
     protected void merge(final CimiEventLogTemplate cimiRef, final CimiEventLogTemplate cimi) {
         if (null != cimiRef) {
             this.mergeObjectCommon(cimiRef, cimi);
             if (null == cimi.getPersistence()) {
                 cimi.setPersistence(cimiRef.getPersistence());
             }
             if (null == cimi.getTargetResource()) {
                 cimi.setTargetResource(cimiRef.getTargetResource());
             }
         }
     }
 }
