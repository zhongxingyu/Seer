 /**
  *
  * SIROCCO
  * Copyright (C) 2013 France Telecom
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
  */
 package org.ow2.sirocco.cloudmanager;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.ow2.sirocco.cloudmanager.NetworkView.NetworkBean;
 import org.ow2.sirocco.cloudmanager.core.api.ICloudProviderManager;
 import org.ow2.sirocco.cloudmanager.core.api.INetworkManager;
 import org.ow2.sirocco.cloudmanager.core.api.exception.CloudProviderException;
 import org.ow2.sirocco.cloudmanager.model.cimi.Job;
 import org.ow2.sirocco.cloudmanager.model.cimi.Network;
 import org.ow2.sirocco.cloudmanager.model.cimi.Network.Type;
 import org.ow2.sirocco.cloudmanager.model.cimi.NetworkConfiguration;
 import org.ow2.sirocco.cloudmanager.model.cimi.NetworkCreate;
 import org.ow2.sirocco.cloudmanager.model.cimi.NetworkTemplate;
import org.ow2.sirocco.cloudmanager.model.cimi.SubnetConfig;
 import org.ow2.sirocco.cloudmanager.model.cimi.extension.CloudProviderAccount;
 import org.vaadin.teemu.wizards.Wizard;
 import org.vaadin.teemu.wizards.WizardStep;
 import org.vaadin.teemu.wizards.event.WizardCancelledEvent;
 import org.vaadin.teemu.wizards.event.WizardCompletedEvent;
 import org.vaadin.teemu.wizards.event.WizardProgressListener;
 import org.vaadin.teemu.wizards.event.WizardStepActivationEvent;
 import org.vaadin.teemu.wizards.event.WizardStepSetChangedEvent;
 
 import com.vaadin.cdi.UIScoped;
 import com.vaadin.data.Property;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.CheckBox;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.FormLayout;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.UI;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window;
 
 import de.steinwedel.messagebox.ButtonId;
 import de.steinwedel.messagebox.Icon;
 import de.steinwedel.messagebox.MessageBox;
 
 @UIScoped
 @SuppressWarnings("serial")
 public class NetworkCreationWizard extends Window implements WizardProgressListener {
     private NetworkView networkView;
 
     private Wizard wizard;
 
     private Util.PlacementStep placementStep;
 
     private Util.MetadataStep metadataStep;
 
     private SubnetStep subnetStep;
 
     @Inject
     private ICloudProviderManager providerManager;
 
     @Inject
     private INetworkManager networkManager;
 
     public NetworkCreationWizard() {
         super("Network Creation");
         this.center();
         this.setClosable(false);
         this.setModal(true);
         this.setResizable(false);
 
         VerticalLayout content = new VerticalLayout();
         content.setMargin(true);
         this.wizard = new Wizard();
         this.wizard.addListener(this);
         this.wizard.addStep(this.placementStep = new Util.PlacementStep(this.wizard), "placement");
         this.wizard.addStep(this.metadataStep = new Util.MetadataStep(this.wizard), "metadata");
         this.wizard.addStep(this.subnetStep = new SubnetStep(), "subnet");
         this.wizard.setHeight("300px");
         this.wizard.setWidth("560px");
 
         content.addComponent(this.wizard);
         content.setComponentAlignment(this.wizard, Alignment.TOP_CENTER);
         this.setContent(content);
     }
 
     public boolean init(final NetworkView networkView) {
         this.networkView = networkView;
         this.wizard.setUriFragmentEnabled(false);
         this.wizard.activateStep(this.placementStep);
         String tenantId = ((MyUI) UI.getCurrent()).getTenantId();
 
         this.placementStep.providerBox.removeAllItems();
         try {
             this.placementStep.setProviderManager(this.providerManager);
             for (CloudProviderAccount providerAccount : this.providerManager.getCloudProviderAccountsByTenant(tenantId)) {
                 this.placementStep.providerBox.addItem(providerAccount.getUuid());
                 this.placementStep.providerBox.setItemCaption(providerAccount.getUuid(), providerAccount.getCloudProvider()
                     .getDescription());
             }
         } catch (CloudProviderException e) {
             Util.diplayErrorMessageBox("Internal error", e);
         }
         if (this.placementStep.providerBox.getItemIds().isEmpty()) {
             MessageBox.showPlain(Icon.ERROR, "No providers", "First add cloud providers", ButtonId.OK);
             return false;
         }
 
         this.metadataStep.nameField.setValue("");
         this.metadataStep.descriptionField.setValue("");
         return true;
     }
 
     @Override
     public void activeStepChanged(final WizardStepActivationEvent event) {
         if (event.getActivatedStep() == this.metadataStep) {
             this.metadataStep.nameField.focus();
         }
     }
 
     @Override
     public void stepSetChanged(final WizardStepSetChangedEvent event) {
     }
 
     @Override
     public void wizardCompleted(final WizardCompletedEvent event) {
         this.close();
 
         NetworkCreate networkCreate = new NetworkCreate();
         networkCreate.setProperties(new HashMap<String, String>());
 
         try {
             String accountId = (String) this.placementStep.providerBox.getValue();
             networkCreate.setProviderAccountId(accountId);
             networkCreate.setLocation((String) this.placementStep.locationBox.getValue());
             networkCreate.setName(this.metadataStep.nameField.getValue());
             networkCreate.setDescription(this.metadataStep.descriptionField.getValue());
             if (networkCreate.getDescription().isEmpty()) {
                 networkCreate.setDescription(null);
             }
 
             NetworkTemplate networkTemplate = new NetworkTemplate();
             NetworkConfiguration networkConfig = new NetworkConfiguration();
             networkConfig.setNetworkType(Type.PRIVATE);
            SubnetConfig subnet = new SubnetConfig();
             subnet.setName(this.subnetStep.nameField.getValue());
             subnet.setCidr(this.subnetStep.cidrField.getValue());
             subnet.setEnableDhcp(this.subnetStep.enableDhcp.getValue());
            List<SubnetConfig> subnets = Collections.singletonList(subnet);
             networkConfig.setSubnets(subnets);
             networkTemplate.setNetworkConfig(networkConfig);
 
             networkCreate.setNetworkTemplate(networkTemplate);
 
             Job job = this.networkManager.createNetwork(networkCreate);
             Network newNetwork = (Network) job.getAffectedResources().get(0);
 
             this.networkView.networks.addBeanAt(0, new NetworkBean(newNetwork));
 
         } catch (CloudProviderException e) {
             Util.diplayErrorMessageBox("Cannot create network", e);
         }
     }
 
     @Override
     public void wizardCancelled(final WizardCancelledEvent event) {
         this.close();
     }
 
     private class SubnetStep implements WizardStep {
         FormLayout content;
 
         TextField nameField;
 
         TextField cidrField;
 
         CheckBox enableDhcp;
 
         SubnetStep() {
             this.content = new FormLayout();
             this.content.setSizeFull();
             this.content.setMargin(true);
 
             this.nameField = new TextField("Name");
             this.nameField.setImmediate(true);
             this.nameField.setRequired(true);
             this.content.addComponent(this.nameField);
             this.nameField.addValueChangeListener(new Property.ValueChangeListener() {
 
                 @Override
                 public void valueChange(final ValueChangeEvent event) {
                     NetworkCreationWizard.this.wizard.updateButtons();
                 }
             });
 
             this.cidrField = new TextField("CIDR");
             this.cidrField.setImmediate(true);
             this.cidrField.setRequired(true);
             this.content.addComponent(this.cidrField);
             this.cidrField.addValueChangeListener(new Property.ValueChangeListener() {
 
                 @Override
                 public void valueChange(final ValueChangeEvent event) {
                     NetworkCreationWizard.this.wizard.updateButtons();
                 }
             });
 
             this.enableDhcp = new CheckBox("Enable DHCP");
             this.enableDhcp.setValue(true);
             this.content.addComponent(this.enableDhcp);
         }
 
         @Override
         public String getCaption() {
             return "Subnet";
         }
 
         @Override
         public Component getContent() {
             return this.content;
         }
 
         @Override
         public boolean onAdvance() {
             return !this.nameField.getValue().isEmpty() && this.cidrField.getValue() != null;
             // TODO parse and check cidr field
         }
 
         @Override
         public boolean onBack() {
             return true;
         }
 
     }
 
 }
