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
 package org.ow2.sirocco.apis.rest.cimi.converter;
 
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiCloudEntryPoint;
 import org.ow2.sirocco.apis.rest.cimi.domain.CloudEntryPointAggregate;
 import org.ow2.sirocco.apis.rest.cimi.domain.collection.CimiCredentialCollection;
 import org.ow2.sirocco.apis.rest.cimi.domain.collection.CimiCredentialTemplateCollection;
 import org.ow2.sirocco.apis.rest.cimi.domain.collection.CimiJobCollection;
 import org.ow2.sirocco.apis.rest.cimi.domain.collection.CimiMachineCollection;
 import org.ow2.sirocco.apis.rest.cimi.domain.collection.CimiMachineConfigurationCollection;
 import org.ow2.sirocco.apis.rest.cimi.domain.collection.CimiMachineImageCollection;
 import org.ow2.sirocco.apis.rest.cimi.domain.collection.CimiMachineTemplateCollection;
 import org.ow2.sirocco.apis.rest.cimi.domain.collection.CimiSystemCollection;
 import org.ow2.sirocco.apis.rest.cimi.domain.collection.CimiSystemTemplateCollection;
 import org.ow2.sirocco.apis.rest.cimi.domain.collection.CimiVolumeCollection;
 import org.ow2.sirocco.apis.rest.cimi.domain.collection.CimiVolumeConfigurationCollection;
 import org.ow2.sirocco.apis.rest.cimi.domain.collection.CimiVolumeImageCollection;
 import org.ow2.sirocco.apis.rest.cimi.domain.collection.CimiVolumeTemplateCollection;
 import org.ow2.sirocco.apis.rest.cimi.request.CimiContext;
 import org.ow2.sirocco.cloudmanager.model.cimi.CloudEntryPoint;
 
 /**
  * Convert the data of the CIMI model and the service model in both directions.
  * <p>
  * Converted classes:
  * <ul>
  * <li>CIMI model: {@link CimiCloudEntryPoint}</li>
  * <li>Service model: List of {@link CloudEntryPoint}</li>
  * </ul>
  * </p>
  */
 public class CloudEntryPointConverter extends ObjectCommonConverter {
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.apis.rest.cimi.converter.CimiConverter#toCimi(org.ow2.sirocco.apis.rest.cimi.utils.CimiContextImpl,
      *      java.lang.Object)
      */
     @Override
     public Object toCimi(final CimiContext context, final Object dataService) {
         CimiCloudEntryPoint cimi = new CimiCloudEntryPoint();
         this.copyToCimi(context, dataService, cimi);
         return cimi;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.apis.rest.cimi.converter.CimiConverter#copyToCimi(org.ow2.sirocco.apis.rest.cimi.utils.CimiContextImpl,
      *      java.lang.Object, java.lang.Object)
      */
     @Override
     public void copyToCimi(final CimiContext context, final Object dataService, final Object dataCimi) {
         this.doCopyToCimi(context, (CloudEntryPointAggregate) dataService, (CimiCloudEntryPoint) dataCimi);
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.apis.rest.cimi.converter.CimiConverter#toService(org.ow2.sirocco.apis.rest.cimi.utils.CimiContextImpl,
      *      java.lang.Object)
      */
     @Override
     public Object toService(final CimiContext context, final Object dataCimi) {
         CloudEntryPoint service = new CloudEntryPoint();
         this.copyToService(context, dataCimi, service);
         return service;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.apis.rest.cimi.converter.CimiConverter#copyToService
      *      (org.ow2.sirocco.apis.rest.cimi.utils.CimiContextImpl,
      *      java.lang.Object, java.lang.Object)
      */
     @Override
     public void copyToService(final CimiContext context, final Object dataCimi, final Object dataService) {
         this.doCopyToService(context, (CimiCloudEntryPoint) dataCimi, (CloudEntryPoint) dataService);
     }
 
     /**
      * Copy data from a service object to a CIMI object.
      * 
      * @param context The current context
      * @param dataService Source service object
      * @param dataCimi Destination CIMI object
      */
     protected void doCopyToCimi(final CimiContext context, final CloudEntryPointAggregate dataService,
         final CimiCloudEntryPoint dataCimi) {
         this.fill(context, dataService, dataCimi);
         if ((true == context.mustBeExpanded(dataCimi)) || (true == context.mustBeReferenced(dataCimi))) {
             dataCimi.setBaseURI(context.getRequest().getBaseUri());
 
             dataCimi.setCredentials((CimiCredentialCollection) context.convertNextCimi(dataService.getCredentials(),
                 CimiCredentialCollection.class));
             dataCimi.setCredentialTemplates((CimiCredentialTemplateCollection) context.convertNextCimi(
                 dataService.getCredentialsTemplates(), CimiCredentialTemplateCollection.class));
 
             dataCimi.setJobs((CimiJobCollection) context.convertNextCimi(dataService.getJobs(), CimiJobCollection.class));
 
             dataCimi.setMachineConfigs((CimiMachineConfigurationCollection) context.convertNextCimi(
                 dataService.getMachineConfigs(), CimiMachineConfigurationCollection.class));
             dataCimi.setMachineImages((CimiMachineImageCollection) context.convertNextCimi(dataService.getMachineImages(),
                 CimiMachineImageCollection.class));
             dataCimi.setMachines((CimiMachineCollection) context.convertNextCimi(dataService.getMachines(),
                 CimiMachineCollection.class));
             dataCimi.setMachineTemplates((CimiMachineTemplateCollection) context.convertNextCimi(
                 dataService.getMachineTemplates(), CimiMachineTemplateCollection.class));
 
             dataCimi.setSystems((CimiSystemCollection) context.convertNextCimi(dataService.getSystems(),
                 CimiSystemCollection.class));
             dataCimi.setSystemTemplates((CimiSystemTemplateCollection) context.convertNextCimi(
                 dataService.getSystemTemplates(), CimiSystemTemplateCollection.class));
 
            dataCimi.setVolumeConfigs((CimiVolumeConfigurationCollection) context.convertNextCimi(
                 dataService.getVolumeConfigurations(), CimiVolumeConfigurationCollection.class));
             dataCimi.setVolumeImages((CimiVolumeImageCollection) context.convertNextCimi(dataService.getVolumeImages(),
                 CimiVolumeImageCollection.class));
             dataCimi.setVolumes((CimiVolumeCollection) context.convertNextCimi(dataService.getVolumes(),
                 CimiVolumeCollection.class));
             dataCimi.setVolumeTemplates((CimiVolumeTemplateCollection) context.convertNextCimi(
                 dataService.getVolumeTemplates(), CimiVolumeTemplateCollection.class));
         }
     }
 
     /**
      * Copy data from a CIMI object to a service object.
      * 
      * @param context The current context
      * @param dataCimi Source CIMI object
      * @param dataService Destination Service object
      */
     protected void doCopyToService(final CimiContext context, final CimiCloudEntryPoint dataCimi,
         final CloudEntryPoint dataService) {
         this.fill(context, dataCimi, dataService);
     }
 
 }
