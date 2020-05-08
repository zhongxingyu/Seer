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
 package org.ow2.sirocco.apis.rest.cimi.domain;
 
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.codehaus.jackson.map.annotate.JsonSerialize;
 import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
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
 
 /**
  * Class CloudEntryPoint.
  * <p>
  */
 @XmlRootElement(name = "CloudEntryPoint")
 @JsonSerialize(include = Inclusion.NON_NULL)
 public class CimiCloudEntryPoint extends CimiObjectCommonAbstract {
 
     /** Serial number */
     private static final long serialVersionUID = 1L;
 
     /**
      * Field "baseURI".
      */
     private String baseURI;
 
     /**
      * Field "credentials".
      */
     private CimiCredentialCollection credentials;
 
     /**
      * Field "credentialTemplates".
      */
     private CimiCredentialTemplateCollection credentialTemplates;
 
     /**
      * Field "jobs".
      */
     private CimiJobCollection jobs;
 
     /**
      * Field "machineConfigs".
      */
     private CimiMachineConfigurationCollection machineConfigs;
 
     /**
      * Field "machineImages".
      */
     private CimiMachineImageCollection machineImages;
 
     /**
      * Field "machines".
      */
     private CimiMachineCollection machines;
 
     /**
      * Field "machineTemplates".
      */
     private CimiMachineTemplateCollection machineTemplates;
 
     /**
      * Field "volumeConfigurations".
      */
     private CimiVolumeConfigurationCollection volumeConfigurations;
 
     /**
      * Field "volumeImages".
      */
     private CimiVolumeImageCollection volumeImages;
 
     /**
      * Field "volumes".
      */
     private CimiVolumeCollection volumes;
 
     /**
      * Field "volumeTemplates".
      */
     private CimiVolumeTemplateCollection volumeTemplates;
 
     /**
      * Field "systems".
      */
     private CimiSystemCollection systems;
 
     /**
      * Field "systemTemplates".
      */
     private CimiSystemTemplateCollection systemTemplates;
 
     /**
      * Field "jobTime".
      */
     private Integer jobTime;
 
     /**
      * Default constructor.
      */
     public CimiCloudEntryPoint() {
         super();
     }
 
     /**
      * Parameterized constructor.
      * 
      * @param href The reference
      */
     public CimiCloudEntryPoint(final String href) {
         super(href);
     }
 
     /**
      * Return the value of field "baseURI".
      * 
      * @return the baseURI
      */
     public String getBaseURI() {
         return this.baseURI;
     }
 
     /**
      * Set the value of field "baseURI".
      * 
      * @param baseURI the baseURI to set
      */
     public void setBaseURI(final String baseURI) {
         this.baseURI = baseURI;
     }
 
     /**
      * Return the value of field "credentials".
      * 
      * @return The value
      */
     public CimiCredentialCollection getCredentials() {
         return this.credentials;
     }
 
     /**
      * Set the value of field "credentials".
      * 
      * @param Credentials The value
      */
     public void setCredentials(final CimiCredentialCollection credentials) {
         this.credentials = credentials;
     }
 
     /**
      * Return the value of field "credentialTemplates".
      * 
      * @return The value
      */
     public CimiCredentialTemplateCollection getCredentialTemplates() {
         return this.credentialTemplates;
     }
 
     /**
      * Set the value of field "credentialTemplates".
      * 
      * @param credentialTemplates The value
      */
     public void setCredentialTemplates(final CimiCredentialTemplateCollection credentialTemplates) {
         this.credentialTemplates = credentialTemplates;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.apis.rest.cimi.domain.CimiExchange#getExchangeType()
      */
     @Override
     @XmlTransient
     @JsonIgnore
     public ExchangeType getExchangeType() {
         return ExchangeType.CloudEntryPoint;
     }
 
     /**
      * Return the value of field "jobs".
      * 
      * @return The value
      */
     public CimiJobCollection getJobs() {
         return this.jobs;
     }
 
     /**
      * Set the value of field "jobs".
      * 
      * @param jobs The value
      */
     public void setJobs(final CimiJobCollection jobs) {
         this.jobs = jobs;
     }
 
     /**
      * Return the value of field "jobTime".
      * 
      * @return The value
      */
     @Deprecated
     public Integer getJobTime() {
         return this.jobTime;
     }
 
     /**
      * Set the value of field "jobTime".
      * 
      * @param jobTime The value
      */
     @Deprecated
     public void setJobTime(final Integer jobTime) {
         this.jobTime = jobTime;
     }
 
     /**
      * Return the value of field "machineConfigs".
      * 
      * @return The value
      */
     public CimiMachineConfigurationCollection getMachineConfigs() {
         return this.machineConfigs;
     }
 
     /**
      * Set the value of field "machineConfigs".
      * 
      * @param machineConfigs The value
      */
     public void setMachineConfigs(final CimiMachineConfigurationCollection machineConfigs) {
         this.machineConfigs = machineConfigs;
     }
 
     /**
      * Return the value of field "machineImages".
      * 
      * @return The value
      */
     public CimiMachineImageCollection getMachineImages() {
         return this.machineImages;
     }
 
     /**
      * Set the value of field "machineImages".
      * 
      * @param machineImages The value
      */
     public void setMachineImages(final CimiMachineImageCollection machineImages) {
         this.machineImages = machineImages;
     }
 
     /**
      * Return the value of field "machines".
      * 
      * @return The value
      */
     public CimiMachineCollection getMachines() {
         return this.machines;
     }
 
     /**
      * Set the value of field "machines".
      * 
      * @param machines The value
      */
     public void setMachines(final CimiMachineCollection machines) {
         this.machines = machines;
     }
 
     /**
      * Return the value of field "machineTemplates".
      * 
      * @return The value
      */
     public CimiMachineTemplateCollection getMachineTemplates() {
         return this.machineTemplates;
     }
 
     /**
      * Set the value of field "machineTemplates".
      * 
      * @param machineTemplates The value
      */
     public void setMachineTemplates(final CimiMachineTemplateCollection machineTemplates) {
         this.machineTemplates = machineTemplates;
     }
 
     /**
      * Return the value of field "volumeConfigurations".
      * 
      * @return The value
      */
     public CimiVolumeConfigurationCollection getVolumeConfigs() {
         return this.volumeConfigurations;
     }
 
     /**
      * Set the value of field "volumeConfigurations".
      * 
      * @param volumeConfigurations The value
      */
    public void setVolumeConfigurations(final CimiVolumeConfigurationCollection volumeConfigurations) {
         this.volumeConfigurations = volumeConfigurations;
     }
 
     /**
      * Return the value of field "volumeImages".
      * 
      * @return The value
      */
     public CimiVolumeImageCollection getVolumeImages() {
         return this.volumeImages;
     }
 
     /**
      * Set the value of field "volumeImages".
      * 
      * @param volumeImages The value
      */
     public void setVolumeImages(final CimiVolumeImageCollection volumeImages) {
         this.volumeImages = volumeImages;
     }
 
     /**
      * Return the value of field "volumes".
      * 
      * @return The value
      */
     public CimiVolumeCollection getVolumes() {
         return this.volumes;
     }
 
     /**
      * Set the value of field "volumes".
      * 
      * @param volumes The value
      */
     public void setVolumes(final CimiVolumeCollection volumes) {
         this.volumes = volumes;
     }
 
     /**
      * Return the value of field "volumeTemplates".
      * 
      * @return The value
      */
     public CimiVolumeTemplateCollection getVolumeTemplates() {
         return this.volumeTemplates;
     }
 
     /**
      * Set the value of field "volumeTemplates".
      * 
      * @param volumeTemplates The value
      */
     public void setVolumeTemplates(final CimiVolumeTemplateCollection volumeTemplates) {
         this.volumeTemplates = volumeTemplates;
     }
 
     /**
      * Return the value of field "systems".
      * 
      * @return The value
      */
     public CimiSystemCollection getSystems() {
         return this.systems;
     }
 
     /**
      * Set the value of field "systems".
      * 
      * @param systems The value
      */
     public void setSystems(final CimiSystemCollection systems) {
         this.systems = systems;
     }
 
     /**
      * Return the value of field "systemTemplates".
      * 
      * @return The value
      */
     public CimiSystemTemplateCollection getSystemTemplates() {
         return this.systemTemplates;
     }
 
     /**
      * Set the value of field "systemTemplates".
      * 
      * @param systemTemplates The value
      */
     public void setSystemTemplates(final CimiSystemTemplateCollection systemTemplates) {
         this.systemTemplates = systemTemplates;
     }
 
 }
