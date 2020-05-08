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
 package org.ow2.sirocco.cimi.server.manager.machine;
 
 import javax.inject.Inject;
 
 import org.ow2.sirocco.cimi.domain.CimiMachineCreate;
 import org.ow2.sirocco.cimi.server.manager.CimiManagerCreateAbstract;
 import org.ow2.sirocco.cimi.server.manager.MergeReferenceHelper;
 import org.ow2.sirocco.cimi.server.request.CimiContext;
 import org.ow2.sirocco.cloudmanager.core.api.IMachineImageManager;
 import org.ow2.sirocco.cloudmanager.core.api.IMachineManager;
 import org.ow2.sirocco.cloudmanager.core.api.INetworkManager;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineConfiguration;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineCreate;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineImage;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineTemplateNetworkInterface;
 import org.ow2.sirocco.cloudmanager.model.cimi.Network;
 
 /**
  * Manage CREATE request of Machine.
  */
 @org.ow2.sirocco.cimi.server.manager.Manager("CimiManagerCreateMachine")
 public class CimiManagerCreateMachine extends CimiManagerCreateAbstract {
     @Inject
     private MergeReferenceHelper mergeReference;
 
     @Inject
     private IMachineManager manager;
 
     @Inject
     private IMachineImageManager imageManager;
 
     @Inject
     private INetworkManager networkManager;
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.CimiManagerAbstract#callService(org.ow2.sirocco.cimi.server.request.CimiContext,
      *      java.lang.Object)
      */
     @Override
     protected Object callService(final CimiContext context, final Object dataService) throws Exception {
         MachineCreate create = (MachineCreate) dataService;
         if (create.getMachineTemplate().getMachineImage() != null
             && create.getMachineTemplate().getMachineImage().getUuid() != null) {
             MachineImage image = this.imageManager.getMachineImageByUuid(create.getMachineTemplate().getMachineImage()
                 .getUuid());
            create.getMachineTemplate().setMachineImage(image);
         }
         if (create.getMachineTemplate().getMachineConfig() != null
             && create.getMachineTemplate().getMachineConfig().getUuid() != null) {
             MachineConfiguration config = this.manager.getMachineConfigurationByUuid(create.getMachineTemplate()
                 .getMachineConfig().getUuid());
            create.getMachineTemplate().setMachineConfig(config);
         }
         if (create.getMachineTemplate().getNetworkInterfaces() != null) {
             for (MachineTemplateNetworkInterface nic : create.getMachineTemplate().getNetworkInterfaces()) {
                 if (nic.getNetwork() != null) {
                     Network net = this.networkManager.getNetworkByUuid(nic.getNetwork().getUuid());
                     nic.setNetwork(net);
                 }
             }
         }
 
         return this.manager.createMachine(create);
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.server.manager.CimiManagerAbstract#beforeConvertToDataService(org.ow2.sirocco.cimi.server.request.CimiContext)
      */
     @Override
     protected void beforeConvertToDataService(final CimiContext context) throws Exception {
         this.mergeReference.merge(context, (CimiMachineCreate) context.getRequest().getCimiData());
     }
 
 }
