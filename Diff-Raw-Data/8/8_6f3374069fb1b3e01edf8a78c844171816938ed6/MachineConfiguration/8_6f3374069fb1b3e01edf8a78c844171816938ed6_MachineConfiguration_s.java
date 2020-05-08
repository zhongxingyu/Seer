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
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
  * USA
  *
  *  $Id$
  *
  */
 
 package org.ow2.sirocco.apis.rest.cimi.sdk;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiCapacity;
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiCpu;
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiDiskConfiguration;
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiMachineConfiguration;
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiMachineConfigurationCollection;
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiMemory;
 import org.ow2.sirocco.apis.rest.cimi.utils.ConstantsPath;
 
 public class MachineConfiguration extends Resource<CimiMachineConfiguration> {
     public static class Disk {
         public int capacity;
 
         public String format;
 
         public String initialLocation;
 
         static Disk from(final CimiDiskConfiguration diskConfig) {
             Disk disk = new Disk();
             disk.capacity = diskConfig.getCapacity().getQuantity();
             disk.format = diskConfig.getFormat();
            disk.initialLocation = diskConfig.getAttachmentPoint();
             return disk;
         }
     }
 
     public MachineConfiguration() {
         super(null, new CimiMachineConfiguration());
     }
 
     public MachineConfiguration(final CimiClient cimiClient, final String id) {
         super(cimiClient, new CimiMachineConfiguration());
         this.cimiObject.setHref(id);
         this.cimiObject.setId(id);
     }
 
     MachineConfiguration(final CimiClient cimiClient, final CimiMachineConfiguration cimiObject) {
         super(cimiClient, cimiObject);
     }
 
     public int getCpu() {
         return this.cimiObject.getCpu().getNumberVirtualCpus();
     }
 
     public void setCpu(final int cpu) {
         CimiCpu cpuInfo = new CimiCpu();
         cpuInfo.setNumberVirtualCpus(cpu);
         cpuInfo.setFrequency(1.0f);
         cpuInfo.setUnits("gigahertz");
         this.cimiObject.setCpu(cpuInfo);
     }
 
     public int getMemory() {
         return this.cimiObject.getMemory().getQuantity();
     }
 
     public void setMemory(final int memory) {
         CimiMemory memoryInfo = new CimiMemory();
         memoryInfo.setQuantity(memory);
         memoryInfo.setUnits("mebibyte");
         this.cimiObject.setMemory(memoryInfo);
     }
 
     public Disk[] getDisks() {
         Disk[] disks = new Disk[this.cimiObject.getDisks().length];
         for (int i = 0; i < disks.length; i++) {
             disks[i] = Disk.from(this.cimiObject.getDisks()[i]);
         }
         return disks;
     }
 
     public void setDisks(final Disk[] disks) {
         CimiDiskConfiguration diskConfigs[] = new CimiDiskConfiguration[disks.length];
         for (int i = 0; i < disks.length; i++) {
             diskConfigs[i] = new CimiDiskConfiguration();
             CimiCapacity diskCapacity = new CimiCapacity();
             diskCapacity.setUnits("gigabyte");
             diskCapacity.setQuantity(disks[i].capacity);
             diskConfigs[i].setCapacity(diskCapacity);
             if (disks[i].format != null) {
                 diskConfigs[i].setFormat(disks[i].format);
             } else {
                 diskConfigs[i].setFormat("");
             }
             if (disks[i].initialLocation != null) {
                diskConfigs[i].setAttachmentPoint(disks[i].initialLocation);
             } else {
                diskConfigs[i].setAttachmentPoint("");
             }
         }
         this.cimiObject.setDisks(diskConfigs);
     }
 
     public void delete() throws CimiException {
         this.cimiClient.deleteRequest(this.cimiClient.extractPath(this.getId()));
     }
 
     public static MachineConfiguration createMachineConfiguration(final CimiClient client,
         final MachineConfiguration machineConfig) throws CimiException {
         CimiMachineConfiguration cimiObject = client.postRequest(ConstantsPath.MACHINE_CONFIGURATION_PATH,
             machineConfig.cimiObject, CimiMachineConfiguration.class);
         return new MachineConfiguration(client, cimiObject);
     }
 
     public static List<MachineConfiguration> getMachineConfigurations(final CimiClient client) throws CimiException {
         CimiMachineConfigurationCollection machineConfigCollection = client.getRequest(
             client.extractPath(client.cloudEntryPoint.getMachineConfigs().getHref()), CimiMachineConfigurationCollection.class);
 
         List<MachineConfiguration> result = new ArrayList<MachineConfiguration>();
 
         if (machineConfigCollection.getCollection() != null) {
             for (CimiMachineConfiguration cimiMachineConfig : machineConfigCollection.getCollection().getArray()) {
                 result.add(MachineConfiguration.getMachineConfigurationByReference(client, cimiMachineConfig.getHref()));
             }
         }
         return result;
     }
 
     public static MachineConfiguration getMachineConfigurationByReference(final CimiClient client, final String ref)
         throws CimiException {
         return new MachineConfiguration(client, client.getCimiObjectByReference(ref, CimiMachineConfiguration.class));
     }
 
     public static MachineConfiguration getMachineConfigurationById(final CimiClient client, final String id)
         throws CimiException {
         String path = client.getMachineConfigurationsPath() + "/" + id;
         return new MachineConfiguration(client, client.getCimiObjectByReference(path, CimiMachineConfiguration.class));
     }
 
 }
