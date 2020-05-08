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
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiCloudEntryPoint;
 import org.ow2.sirocco.apis.rest.cimi.domain.CloudEntryPointAggregate;
 import org.ow2.sirocco.apis.rest.cimi.domain.ExchangeType;
 import org.ow2.sirocco.apis.rest.cimi.request.CimiContext;
 import org.ow2.sirocco.apis.rest.cimi.request.CimiContextImpl;
 import org.ow2.sirocco.apis.rest.cimi.request.CimiRequest;
 import org.ow2.sirocco.apis.rest.cimi.request.CimiResponse;
 import org.ow2.sirocco.cloudmanager.model.cimi.CloudEntryPoint;
 import org.ow2.sirocco.cloudmanager.model.cimi.CredentialsCollection;
 import org.ow2.sirocco.cloudmanager.model.cimi.CredentialsTemplateCollection;
 import org.ow2.sirocco.cloudmanager.model.cimi.JobCollection;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineCollection;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineConfigurationCollection;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineImageCollection;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineTemplateCollection;
 
 /**
  * Converters tests of CloudEntryPoint resources.
  */
 public class CloudEntryPointConverterTest {
 
     private CimiRequest request;
 
     private CimiContext context;
 
     @Before
     public void setUp() throws Exception {
 
         this.request = new CimiRequest();
         this.request.setBaseUri("http://www.test.org/");
         this.context = new CimiContextImpl(this.request, new CimiResponse());
     }
 
     @Test
     // TODO Volumes ...
     public void testCimiCloudEntryPoint() throws Exception {
         CimiCloudEntryPoint cimi;
         CloudEntryPointAggregate service;
 
         // Empty Service -> Cimi
         service = new CloudEntryPointAggregate(new CloudEntryPoint());
         cimi = (CimiCloudEntryPoint) this.context.convertToCimi(service, CimiCloudEntryPoint.class);
         Assert.assertEquals(this.request.getBaseUri(), cimi.getBaseURI());
         Assert.assertNull(cimi.getCredentials());
         Assert.assertNull(cimi.getCredentialsTemplates());
         Assert.assertNull(cimi.getJobs());
         Assert.assertNull(cimi.getJobTime());
         Assert.assertNull(cimi.getMachineConfigs());
         Assert.assertNull(cimi.getMachineImages());
         Assert.assertNull(cimi.getMachines());
         Assert.assertNull(cimi.getMachineTemplates());
         Assert.assertNull(cimi.getMachineTemplates());
 
         // // Full Service -> Cimi
         service = new CloudEntryPointAggregate(new CloudEntryPoint());
         service.setCredentials(new CredentialsCollection());
         service.setCredentialsTemplates(new CredentialsTemplateCollection());
         service.setJobs(new JobCollection());
         service.setMachineConfigs(new MachineConfigurationCollection());
         service.setMachineImages(new MachineImageCollection());
         service.setMachines(new MachineCollection());
         service.setMachineTemplates(new MachineTemplateCollection());
 
         cimi = (CimiCloudEntryPoint) this.context.convertToCimi(service, CimiCloudEntryPoint.class);
         Assert.assertEquals(this.request.getBaseUri(), cimi.getBaseURI());
        Assert.assertNull(cimi.getCredentials());
         Assert.assertEquals(this.request.getBaseUri() + ExchangeType.CredentialsCollection.getPathname(), cimi.getCredentials()
             .getHref());
         Assert.assertEquals(this.request.getBaseUri() + ExchangeType.CredentialsTemplateCollection.getPathname(), cimi
             .getCredentialsTemplates().getHref());
         Assert.assertEquals(this.request.getBaseUri() + ExchangeType.MachineConfigurationCollection.getPathname(), cimi
             .getMachineConfigs().getHref());
         Assert.assertEquals(this.request.getBaseUri() + ExchangeType.MachineImageCollection.getPathname(), cimi
             .getMachineImages().getHref());
         Assert.assertEquals(this.request.getBaseUri() + ExchangeType.MachineCollection.getPathname(), cimi.getMachines()
             .getHref());
         Assert.assertEquals(this.request.getBaseUri() + ExchangeType.MachineTemplateCollection.getPathname(), cimi
             .getMachineTemplates().getHref());
        Assert.assertEquals(this.request.getBaseUri() + ExchangeType.JobCollection.getPathname(), cimi.getJobs().getHref());

     }
 
 }
