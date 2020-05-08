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
 
 import javax.annotation.PreDestroy;
 import javax.annotation.Resource;
 import javax.inject.Inject;
 import javax.jms.Connection;
 import javax.jms.ConnectionFactory;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageConsumer;
 import javax.jms.MessageListener;
 import javax.jms.Session;
 import javax.jms.Topic;
 
 import org.ow2.sirocco.cloudmanager.core.api.IUserManager;
 import org.ow2.sirocco.cloudmanager.core.api.IdentityContext;
 import org.ow2.sirocco.cloudmanager.core.api.ResourceStateChangeEvent;
 import org.ow2.sirocco.cloudmanager.core.api.exception.CloudProviderException;
 import org.ow2.sirocco.cloudmanager.model.cimi.Machine;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineVolume;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineVolume.State;
 import org.ow2.sirocco.cloudmanager.model.cimi.Network;
 import org.ow2.sirocco.cloudmanager.model.cimi.Volume;
 import org.ow2.sirocco.cloudmanager.model.cimi.extension.Tenant;
 import org.ow2.sirocco.cloudmanager.model.cimi.extension.User;
 
 import com.vaadin.annotations.Push;
 import com.vaadin.annotations.Theme;
 import com.vaadin.cdi.CDIUI;
 import com.vaadin.data.Property;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Property.ValueChangeListener;
 import com.vaadin.server.ThemeResource;
 import com.vaadin.server.VaadinRequest;
 import com.vaadin.server.VaadinService;
 import com.vaadin.server.VaadinServletService;
 import com.vaadin.shared.communication.PushMode;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.ComboBox;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.HorizontalSplitPanel;
 import com.vaadin.ui.Image;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Notification;
 import com.vaadin.ui.Tree;
 import com.vaadin.ui.UI;
 import com.vaadin.ui.UIDetachedException;
 import com.vaadin.ui.VerticalLayout;
 
 @CDIUI
 @Theme("mytheme")
 @Push(PushMode.MANUAL)
 @SuppressWarnings("serial")
 public class MyUI extends UI implements MessageListener {
     private VerticalLayout inventoryContainer;
 
     @Inject
     private MachineView machineView;
 
     @Inject
     private MachineImageView machineImageView;
 
     @Inject
     private VolumeView volumeView;
 
     @Inject
     private NetworkView networkView;
 
     @Inject
     private CloudProviderView providerView;
 
     @Inject
     private KeyPairView keyPairView;
 
     @Inject
     private IUserManager userManager;
 
     @Inject
     private IdentityContext identityContext;
 
     @Resource(lookup = "jms/ResourceStateChangeTopic")
     private Topic resourceStateChangeTopic;
 
     @Resource
     private ConnectionFactory connectionFactory;
 
     private Session messagingSession;
 
     private MessageConsumer consumer;
 
     private Connection connection;
 
     private String userName;
 
     private String tenantId;
 
     @Override
     protected void init(final VaadinRequest request) {
         this.userName = request.getUserPrincipal().getName();
        this.tenantId = "1";
         this.identityContext.setUserName(this.userName);
        this.identityContext.setTenantId(this.tenantId);
 
         this.getPage().setTitle("Sirocco Dashboard");
         final VerticalLayout layout = new VerticalLayout();
         layout.setSizeFull();
         this.setContent(layout);
 
         // Top header *********************
         HorizontalLayout header = new HorizontalLayout();
         header.setMargin(true);
         header.setWidth("100%");
         header.setHeight("70px");
         header.setStyleName("topHeader");
 
         // logo
         Image image = new Image(null, new ThemeResource("img/sirocco_small_logo.png"));
         header.addComponent(image);
 
         // spacer
         Label spacer = new Label();
         spacer.setWidth("100%");
         header.addComponent(spacer);
         header.setExpandRatio(spacer, 1.0f);
 
         HorizontalLayout rightButtons = new HorizontalLayout();
         rightButtons.setStyleName("topHeader");
         rightButtons.setSpacing(true);
 
         this.userName = request.getUserPrincipal().getName();
         User user = null;
         try {
             user = this.userManager.getUserByUsername(this.userName);
         } catch (CloudProviderException e) {
             e.printStackTrace();
         }
 
         Label label = new Label("Tenant:");
         label.setStyleName("topHeaderLabel");
         rightButtons.addComponent(label);
         final ComboBox tenantSelect = new ComboBox();
         tenantSelect.setTextInputAllowed(false);
         tenantSelect.setNullSelectionAllowed(false);
         for (Tenant tenant : user.getTenants()) {
             tenantSelect.addItem(tenant.getName());
         }
         tenantSelect.setValue(user.getTenants().iterator().next().getName());
         tenantSelect.addValueChangeListener(new Property.ValueChangeListener() {
 
             @Override
             public void valueChange(final ValueChangeEvent event) {
                 Notification.show("Switching to tenant " + tenantSelect.getValue());
 
             }
         });
         tenantSelect.setImmediate(true);
         rightButtons.addComponent(tenantSelect);
 
         // logged user name
 
         label = new Label("Logged in as: " + this.userName);
         label.setStyleName("topHeaderLabel");
         rightButtons.addComponent(label);
 
         // sign out button
         Button button = new Button("Sign Out");
         // button.setStyleName(BaseTheme.BUTTON_LINK);
         button.addClickListener(new Button.ClickListener() {
             public void buttonClick(final ClickEvent event) {
                 MyUI.this.logout();
             }
         });
         rightButtons.addComponent(button);
 
         header.addComponent(rightButtons);
         layout.addComponent(header);
 
         // Split view
         HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
         splitPanel.setSizeFull();
         splitPanel.setFirstComponent(this.createLeftMenu());
 
         this.inventoryContainer = new VerticalLayout();
         this.inventoryContainer.setSizeFull();
 
         this.inventoryContainer.addComponent(this.machineView);
 
         splitPanel.setSecondComponent(this.inventoryContainer);
         splitPanel.setSplitPosition(15);
 
         layout.addComponent(splitPanel);
         layout.setExpandRatio(splitPanel, 1.0f);
 
         this.listenToNotifications();
 
     }
 
     void listenToNotifications() {
         String selector = "tenantId = " + "'" + this.tenantId + "'";
         try {
             if (this.consumer != null) {
                 this.consumer.close();
             }
             this.connection = this.connectionFactory.createConnection();
             this.messagingSession = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
             this.consumer = this.messagingSession.createConsumer(this.resourceStateChangeTopic, selector);
 
             this.connection.start();
 
             this.consumer.setMessageListener(this);
         } catch (JMSException e) {
             e.printStackTrace();
         }
     }
 
     @PreDestroy
     private void destroy() {
         if (this.consumer != null) {
             try {
                 this.consumer.close();
                 this.messagingSession.close();
                 this.connection.close();
             } catch (JMSException e) {
                 e.printStackTrace();
             }
         }
     }
 
     private static final String PROVIDERS_MENU_ITEM_ID = "Providers";
 
     private static final String COMPUTE_MENU_ITEM_ID = "Compute";
 
     private static final String INSTANCES_MENU_ITEM_ID = "Instances";
 
     private static final String IMAGES_MENU_ITEM_ID = "Images";
 
     private static final String STORAGE_MENU_ITEM_ID = "Block storage";
 
     private static final String VOLUMES_MENU_ITEM_ID = "Volumes";
 
     private static final String NETWORKING_MENU_ITEM_ID = "Networking";
 
     private static final String NETWORKS_MENU_ITEM_ID = "Networks";
 
     private static final String SECURITY_MENU_ITEM_ID = "Security";
 
     private static final String KEYPAIRS_MENU_ITEM_ID = "KeyPairs";
 
     Tree createLeftMenu() {
         final Tree resourceTree = new Tree("Root");
         resourceTree.setStyleName("myTree");
         resourceTree.setImmediate(true);
         resourceTree.setSizeFull();
         resourceTree.addItem(MyUI.PROVIDERS_MENU_ITEM_ID);
         resourceTree.setItemIcon(MyUI.PROVIDERS_MENU_ITEM_ID, new ThemeResource("img/cloud.png"));
         resourceTree.setChildrenAllowed(MyUI.PROVIDERS_MENU_ITEM_ID, false);
 
         // resourceTree.addItem(MyUI.COMPUTE_MENU_ITEM_ID);
 
         resourceTree.addItem(MyUI.INSTANCES_MENU_ITEM_ID);
         resourceTree.setItemIcon(MyUI.INSTANCES_MENU_ITEM_ID, new ThemeResource("img/server.png"));
         resourceTree.setChildrenAllowed(MyUI.INSTANCES_MENU_ITEM_ID, false);
         // resourceTree.setParent(MyUI.INSTANCES_MENU_ITEM_ID, MyUI.COMPUTE_MENU_ITEM_ID);
 
         resourceTree.addItem(MyUI.IMAGES_MENU_ITEM_ID);
         resourceTree.setItemIcon(MyUI.IMAGES_MENU_ITEM_ID, new ThemeResource("img/image.png"));
         resourceTree.setItemCaption(MyUI.IMAGES_MENU_ITEM_ID, "  Images");
         resourceTree.setChildrenAllowed(MyUI.IMAGES_MENU_ITEM_ID, false);
         // resourceTree.setParent(MyUI.IMAGES_MENU_ITEM_ID, MyUI.COMPUTE_MENU_ITEM_ID);
 
         // resourceTree.addItem(MyUI.STORAGE_MENU_ITEM_ID);
 
         resourceTree.addItem(MyUI.VOLUMES_MENU_ITEM_ID);
         resourceTree.setItemIcon(MyUI.VOLUMES_MENU_ITEM_ID, new ThemeResource("img/disk.png"));
         resourceTree.setItemCaption(MyUI.VOLUMES_MENU_ITEM_ID, "  Volumes");
         resourceTree.setChildrenAllowed(MyUI.VOLUMES_MENU_ITEM_ID, false);
         // resourceTree.setParent(MyUI.VOLUMES_MENU_ITEM_ID, MyUI.STORAGE_MENU_ITEM_ID);
 
         // resourceTree.addItem(MyUI.NETWORKING_MENU_ITEM_ID);
 
         resourceTree.addItem(MyUI.NETWORKS_MENU_ITEM_ID);
         resourceTree.setItemIcon(MyUI.NETWORKS_MENU_ITEM_ID, new ThemeResource("img/network.png"));
         resourceTree.setChildrenAllowed(MyUI.NETWORKS_MENU_ITEM_ID, false);
         // resourceTree.setParent(MyUI.NETWORKS_MENU_ITEM_ID, MyUI.NETWORKING_MENU_ITEM_ID);
 
         // resourceTree.addItem(MyUI.SECURITY_MENU_ITEM_ID);
 
         resourceTree.addItem(MyUI.KEYPAIRS_MENU_ITEM_ID);
         resourceTree.setItemIcon(MyUI.KEYPAIRS_MENU_ITEM_ID, new ThemeResource("img/key.png"));
         resourceTree.setChildrenAllowed(MyUI.KEYPAIRS_MENU_ITEM_ID, false);
         // resourceTree.setParent(MyUI.KEYPAIRS_MENU_ITEM_ID, MyUI.SECURITY_MENU_ITEM_ID);
 
         // resourceTree.expandItemsRecursively(MyUI.COMPUTE_MENU_ITEM_ID);
         // resourceTree.expandItemsRecursively(MyUI.STORAGE_MENU_ITEM_ID);
         // resourceTree.expandItemsRecursively(MyUI.NETWORKING_MENU_ITEM_ID);
         // resourceTree.expandItemsRecursively(MyUI.SECURITY_MENU_ITEM_ID);
 
         resourceTree.select(MyUI.INSTANCES_MENU_ITEM_ID);
 
         resourceTree.addValueChangeListener(new ValueChangeListener() {
             Object previous = null;
 
             @Override
             public void valueChange(final ValueChangeEvent event) {
                 if (resourceTree.getValue() != null) {
                     if (resourceTree.hasChildren(resourceTree.getValue())) {
                         resourceTree.setValue(this.previous);
                     } else {
                         this.previous = resourceTree.getValue();
                         switch ((String) resourceTree.getValue()) {
                         case PROVIDERS_MENU_ITEM_ID:
                             MyUI.this.inventoryContainer.replaceComponent(MyUI.this.inventoryContainer.getComponent(0),
                                 MyUI.this.providerView);
                             break;
                         case INSTANCES_MENU_ITEM_ID:
                             MyUI.this.inventoryContainer.replaceComponent(MyUI.this.inventoryContainer.getComponent(0),
                                 MyUI.this.machineView);
                             break;
                         case IMAGES_MENU_ITEM_ID:
                             MyUI.this.inventoryContainer.replaceComponent(MyUI.this.inventoryContainer.getComponent(0),
                                 MyUI.this.machineImageView);
                             break;
                         case VOLUMES_MENU_ITEM_ID:
                             MyUI.this.inventoryContainer.replaceComponent(MyUI.this.inventoryContainer.getComponent(0),
                                 MyUI.this.volumeView);
                             break;
                         case NETWORKS_MENU_ITEM_ID:
                             MyUI.this.inventoryContainer.replaceComponent(MyUI.this.inventoryContainer.getComponent(0),
                                 MyUI.this.networkView);
                             break;
                         case KEYPAIRS_MENU_ITEM_ID:
                             MyUI.this.inventoryContainer.replaceComponent(MyUI.this.inventoryContainer.getComponent(0),
                                 MyUI.this.keyPairView);
                             break;
                         }
                     }
                 }
             }
         });
         return resourceTree;
     }
 
     public String getUserName() {
         return this.userName;
     }
 
     public String getTenantId() {
         return this.tenantId;
     }
 
     @Override
     public void detach() {
         super.detach();
     }
 
     private void logout() {
         this.getUI().getSession().close();
         // UI.getCurrent().close();
 
         // Invalidate underlying session instead if login info is stored there
         VaadinService.getCurrentRequest().getWrappedSession().invalidate();
 
         // Redirect to avoid keeping the removed UI open in the browser
         this.getUI().getPage().setLocation(VaadinServletService.getCurrentServletRequest().getContextPath() + "/logout.jsp");
     }
 
     @Override
     public void onMessage(final Message message) {
         try {
             final ResourceStateChangeEvent event = message.getBody(ResourceStateChangeEvent.class);
             try {
                 this.access(new Runnable() {
                     @Override
                     public void run() {
 
                         if (event.getResource() instanceof Machine) {
                             Machine machine = (Machine) event.getResource();
                             if (!machine.getState().toString().endsWith("ING")) {
                                 Notification.show("Instance " + machine.getName() + " "
                                     + machine.getState().toString().toLowerCase(), Notification.Type.TRAY_NOTIFICATION);
                             }
                             MyUI.this.machineView.updateMachine(machine);
                         } else if (event.getResource() instanceof Volume) {
                             Volume volume = (Volume) event.getResource();
                             if (!volume.getState().toString().endsWith("ING")) {
                                 Notification.show("Volume " + volume.getName() + " "
                                     + volume.getState().toString().toLowerCase(), Notification.Type.TRAY_NOTIFICATION);
                             }
                             MyUI.this.volumeView.updateVolume(volume);
                         } else if (event.getResource() instanceof MachineVolume) {
                             MachineVolume machineVolume = (MachineVolume) event.getResource();
                             if (!machineVolume.getState().toString().endsWith("ING")) {
                                 String message;
                                 if (machineVolume.getState() == State.DELETED) {
                                     message = "detached";
                                 } else if (machineVolume.getState() == State.ATTACHED) {
                                     message = "attached";
                                 } else {
                                     message = "error";
                                 }
                                 Notification.show("Volume " + machineVolume.getVolume().getName() + " " + message,
                                     Notification.Type.TRAY_NOTIFICATION);
                             }
                             MyUI.this.volumeView.updateVolume(machineVolume.getVolume());
                         } else if (event.getResource() instanceof Network) {
                             Network network = (Network) event.getResource();
                             if (!network.getState().toString().endsWith("ING")) {
                                 Notification.show("Network " + network.getName() + " "
                                     + network.getState().toString().toLowerCase(), Notification.Type.TRAY_NOTIFICATION);
                             }
                             MyUI.this.networkView.updateNetwork(network);
                         }
                         MyUI.this.push();
                     }
                 });
             } catch (UIDetachedException e) {
             }
 
         } catch (JMSException e) {
             Notification.show("Unable to retrieve message. See server log");
             this.push();
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
 }
