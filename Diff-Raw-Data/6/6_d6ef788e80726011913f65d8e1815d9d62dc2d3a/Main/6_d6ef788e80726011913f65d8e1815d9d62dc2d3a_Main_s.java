 /*
  * Copyright 2009 IT Mill Ltd.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.thingtrack.konekti.view.web.workbench;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.SocketException;
 import java.util.List;
 import java.util.Locale;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 
 import org.dellroad.stuff.vaadin.SpringContextApplication;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.web.context.ConfigurableWebApplicationContext;
 
 import com.thingtrack.konekti.domain.Configuration;
 import com.thingtrack.konekti.domain.MenuCommandResource;
 import com.thingtrack.konekti.domain.MenuFolderResource;
 import com.thingtrack.konekti.domain.MenuResource;
 import com.thingtrack.konekti.domain.MenuWorkbench;
 import com.thingtrack.konekti.domain.User;
 import com.thingtrack.konekti.service.api.ConfigurationService;
 import com.thingtrack.konekti.service.api.MenuWorkbenchService;
 import com.thingtrack.konekti.service.api.UserService;
 import com.thingtrack.konekti.service.security.SecurityService;
 import com.thingtrack.konekti.view.addon.ui.SliderView;
 import com.thingtrack.konekti.view.kernel.IMetadataModuleServiceListener;
 import com.thingtrack.konekti.view.kernel.IModuleService;
 import com.thingtrack.konekti.view.kernel.IWorkbenchContext;
 import com.thingtrack.konekti.view.kernel.MetadataModule;
 import com.thingtrack.konekti.view.kernel.ui.layout.IViewChangeListener;
 import com.thingtrack.konekti.view.kernel.ui.layout.IViewContainer;
 import com.thingtrack.konekti.view.kernel.ui.layout.LOCATION;
 import com.thingtrack.konekti.view.kernel.ui.layout.ViewEvent;
 import com.thingtrack.konekti.view.layout.KonektiLayout;
 import com.thingtrack.konekti.view.web.workbench.ui.MenuManager;
 import com.thingtrack.konekti.view.web.workbench.ui.ResourceManager;
 import com.thingtrack.konekti.view.web.workbench.ui.ResourceManager.Resource;
 import com.thingtrack.konekti.view.web.workbench.ui.ToolbarManager;
 import com.thingtrack.konekti.view.web.workbench.ui.WorkbenchContext;
 import com.thingtrack.konekti.view.web.form.field.LocaleField;
 import com.thingtrack.konekti.view.addon.ui.ErrorViewForm;
 
 import com.vaadin.terminal.StreamResource;
 import com.vaadin.terminal.StreamResource.StreamSource;
 import com.vaadin.terminal.Terminal;
 import com.vaadin.ui.MenuBar.Command;
 import com.vaadin.ui.MenuBar.MenuItem;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window;
 
 /**
  * The Application's "main" class
  */
 @SuppressWarnings("serial")
 
 public class Main extends SpringContextApplication implements IMetadataModuleServiceListener, IViewChangeListener {
 	private final static Logger logger = Logger.getLogger(SpringContextApplication.class.getName());
     
 	@Autowired
 	private IModuleService moduleService;
 
 	@Autowired
 	private MenuWorkbenchService menuWorkbenchService;
 	
 	@Autowired
 	private SecurityService securityService;
 	
 	@Autowired
 	private UserService userService;
 
 	@Autowired
 	private ConfigurationService configurationService;
 	
 	@Autowired
 	private ResourceManager resourceManager;
 
 	@Autowired
 	private ToolbarManager toolbarManager;
 
 	@Autowired
 	private MenuManager menuManager;
 	
 	@Autowired
 	private KonektiLayout konektiLayout;
 
 	private Window window;
 	
 	private SliderView sliderView;
 
 	private IWorkbenchContext workbenchContext;
 	
 	private String name;
 	private String version;
 	private String logoInit;
 	private boolean demo;
 	private boolean jira;
 	
 	@Override
 	protected void initSpringApplication(ConfigurableWebApplicationContext arg0) {
 		// set konekti theme
 		setTheme("konekti");
 
 		// get global konekti configuration
 		getConfiguration();
 		
 		window = new Window(name);
 		
 		window.setStyleName("background");
 		setMainWindow(window);
 
 		// set full size and none margin for the default window layout
 		VerticalLayout mainLayout = (VerticalLayout) window.getContent();
 		mainLayout.setSizeFull();
 		mainLayout.setMargin(false);
 		
 		// Create the views
 		createViews();
 
 		// add Konekti Layout
 		mainLayout.addComponent(sliderView);
 
 		// add module listener to list bundle install/uninstall
 		moduleService.addListener(this);
 			
 		// jira issue collector button
 		if (jira) {
 			StringBuffer buffer = new StringBuffer();
 			buffer.append("jQuery.ajax({");
			buffer.append(" url: \"https://thingtrack.atlassian.net/s/en_US-yjzsxs-1988229788/6080/65/1.4.0-m2/_/download/batch/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector-embededjs/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector-embededjs.js?collectorId=ba205bee\"");
 			buffer.append(",type: \"get\"");
 			buffer.append(",cache: true");
 			buffer.append(",dataType: \"script\"});");
 			getMainWindow().executeJavaScript(buffer.toString()); 
 		}
						
 	}
 
 	private void getConfiguration() {
 		Configuration configuration = null;
 		try {
 			configuration = configurationService.getByTag(Configuration.TAG.NAME.name());
 			name = configuration.getValue();
 		} catch (Exception e) {
 			name = "KONEKTI";
 		}	
 		
 		try {
 			configuration = configurationService.getByTag(Configuration.TAG.VERSION.name());
 			version = configuration.getValue();
 		} catch (Exception e) {
 			version = "0.0.1.SNAPSHOT";
 		}	
 		
 		try {
 			configuration = configurationService.getByTag(Configuration.TAG.LOGO_INIT.name());
 			logoInit = configuration.getValue();
 		} catch (Exception e) {
 			logoInit = "logo_konekti_init.png";
 		}	
 		
 		try {
 			configuration = configurationService.getByTag(Configuration.TAG.DEMO.name());
 			demo = Boolean.parseBoolean(configuration.getValue());
 		} catch (Exception e) {
 			demo = false;
 		}
 		
 		try {
 			configuration = configurationService.getByTag("JIRA");
 			jira = Boolean.parseBoolean(configuration.getValue());
 		} catch (Exception e) {
 			jira = false;
 		}
 			
 	}
 	
 	private void createViews() {
 		sliderView = new SliderView();
 
 		sliderView.addListener(this);
 
 		sliderView.addView(new SecurityAccessView(securityService, userService, sliderView, version, logoInit, demo));		
 		sliderView.addView(new WorkbenchView(konektiLayout, sliderView));
 	}
 
 	protected void initMenuManager() {
 		try {
 			// // STEP 1: construct vertical menu
 			List<MenuWorkbench> menuResources = menuWorkbenchService.getAll();
 
 			// get the first menu
 			MenuWorkbench menuDefault = menuResources.get(0);
 
 			for (MenuFolderResource menuFolderResource : menuDefault
 					.getMenuFolderResource()) {
 				// add new header menu item
 				MenuItem headMenuItem = konektiLayout.getMenuLayout()
 						.addMenuItem(
 								menuFolderResource.getCaption(),
 								getIcon(menuFolderResource.getIcon(),
 										menuFolderResource.getCaption()), null);
 
 				// recursive menu manage
 				getMenu(menuFolderResource, headMenuItem);
 			}
 
 		} catch (Exception e) {
 			e.getMessage();
 		}
 	}
 
 	private void getMenu(MenuFolderResource menuFolderResource, MenuItem itemParentId) {
 		
 		for (final MenuResource menuResource : menuFolderResource.getMenuResources()) {
 			if (menuResource instanceof MenuFolderResource) {
 				// add new header menu item
 				MenuItem headMenuItem = konektiLayout.getMenuLayout().addMenuItem(
 								menuResource.getCaption(),
 								getIcon(menuResource.getIcon(),menuResource.getCaption()),
 								itemParentId, null);
 
 				// recursive menu manage
 				getMenu((MenuFolderResource) menuResource, headMenuItem);
 			} else {
 				if (((MenuCommandResource) menuResource).getType() == MenuCommandResource.TYPE.SEPARATOR)
 					itemParentId.addSeparator();
 				else {
 					// default menu command
 					Command defaultCommand = new Command() {
 						@Override
 						public void menuSelected(MenuItem selectedItem) {
 							window.showNotification(
 									"Module Warning",
 									"The Module: "
 											+ ((MenuCommandResource) menuResource)
 													.getModuleId()
 											+ "["
 											+ ((MenuCommandResource) menuResource)
 													.getModuleVersion()
 											+ "] is not deploy",
 									Window.Notification.TYPE_WARNING_MESSAGE);
 
 						}
 					};
 
 					// create the new menu item; recover commanda data
 					String symbolicName = ((MenuCommandResource) menuResource).getModuleId();
 					String version = ((MenuCommandResource) menuResource).getModuleVersion();
 
 					String id = symbolicName + "#" + version;
 					String caption = ((MenuCommandResource) menuResource).getCaption();
 					String hint = ((MenuCommandResource) menuResource).getHint();
 					
 					boolean autoStart = ((MenuCommandResource) menuResource).isAutostart();
 					boolean closeable = ((MenuCommandResource) menuResource).isClosable();
 					
 					com.vaadin.terminal.Resource resource = null;
 					if (((MenuCommandResource) menuResource) != null)
 						resource = getIcon(((MenuCommandResource) menuResource).getIcon(), caption);
 
 					// create the new menu item
 					MenuItem headMenuItem = konektiLayout.getMenuLayout().addMenuItem(caption, hint, resource, itemParentId, defaultCommand);
 
 					// add command to the menu manager list
 					if (((MenuCommandResource) menuResource).getLocation().name().equals("TOP"))
 						resourceManager.addResource(id, caption, resource, LOCATION.TOP, autoStart, closeable, null, headMenuItem, null);
 					else if (((MenuCommandResource) menuResource).getLocation().name().equals("LEFT"))
 						resourceManager.addResource(id, caption, resource, LOCATION.LEFT, autoStart, closeable, null, headMenuItem, null);
 					else if (((MenuCommandResource) menuResource).getLocation().name().equals("CENTER"))
 						resourceManager.addResource(id, caption, resource, LOCATION.CENTER, autoStart, closeable, null, headMenuItem, null);
 					else if (((MenuCommandResource) menuResource).getLocation().name().equals("RIGHT"))
 						resourceManager.addResource(id, caption, resource, LOCATION.RIGHT, autoStart, closeable, null, headMenuItem, null);
 					else if (((MenuCommandResource) menuResource).getLocation().name().equals("BOTTON"))
 						resourceManager.addResource(id, caption, resource, LOCATION.BOTTON, autoStart, closeable, null, headMenuItem, null);
 
 					// set module payload if is register in service registry
 					MetadataModule metadataModule = moduleService.get(
 							symbolicName, version);
 
 					if (metadataModule != null)
 						try {
 							setModuleResource(metadataModule);
 						} catch (Exception e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 				}
 			}
 		}
 	}
 
 	private StreamResource getIcon(final byte[] byteResource, String name) {
 		if (byteResource == null)
 			return null;
 
 		// create an instance of our stream source.
 		StreamSource imagesource = new ModuleResource(byteResource);
 
 		// Create a resource that uses the stream source and give it a name.
 		// The constructor will automatically register the resource in the
 		// application.
 		StreamResource imageresource = new StreamResource(imagesource, name
 				+ ".png", this);
 
 		return imageresource;
 	}
 
 	private class ModuleResource implements StreamResource.StreamSource {
 		private byte[] resource = null;
 		private ByteArrayOutputStream imagebuffer = null;
 
 		public ModuleResource(byte[] resource) {
 			this.resource = resource;
 
 		}
 
 		@Override
 		public InputStream getStream() {
 			if (resource != null) {
 				InputStream in = new ByteArrayInputStream(resource);
 				BufferedImage bImageFromConvert = null;
 
 				try {
 					bImageFromConvert = ImageIO.read(in);
 					imagebuffer = new ByteArrayOutputStream();
 
 					ImageIO.write(bImageFromConvert, "png", imagebuffer);
 				} catch (IOException e) {
 					e.printStackTrace();
 
 				}
 
 				return new ByteArrayInputStream(imagebuffer.toByteArray());
 			}
 
 			return null;
 		}
 
 	}
 
 	private void setModuleResource(MetadataModule metadataModule)
 			throws Exception {
 		final Resource resource = resourceManager.getResource(metadataModule
 				.getId() + "#" + metadataModule.getVersion());
 
 		if (resource != null) {
 
 			IViewContainer viewComponent = metadataModule.getModule()
 					.createViewComponent(workbenchContext);
 						
 			viewComponent.addListener((IViewChangeListener) toolbarManager);
 
 			resource.setComponentView(viewComponent);
 
 			// set command (handler) to item menu
 			resource.getMenu().setCommand(new Command() {
 				@Override
 				public void menuSelected(MenuItem selectedItem) {
 					konektiLayout.addModule(resource.getId(),
 							resource.getCaption(), resource.getComponentView(), resource.isCloseable(),
 							resource.getResource(), resource.getLocation());
 
 				}
 			});
 
 			// autostart if the flag is active
 			if (resource.isAutoStart())
 				konektiLayout.addModule(resource.getId(),
 						resource.getCaption(), resource.getComponentView(), resource.isCloseable(),
 						resource.getResource(), resource.getLocation());
 		}
 	}
 
 	@Override
 	public void metadataModuleRegistered(IModuleService source,
 			MetadataModule metadataModule) {
 		try {
 			setModuleResource(metadataModule);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	@Override
 	public void metadataModuleUnregistered(IModuleService source,
 			final MetadataModule metadataModule) {
 		String id = metadataModule.getId() + "#" + metadataModule.getVersion();
 
 		Resource resource = resourceManager.getResource(id);
 
 		if (resource != null) {
 			// remove the bundle tab resource if it's opened
 			try {
 				konektiLayout.removeModule(resource.getId());
 				resource.setTab(null);
 			} catch (Exception e) {
 				e.getMessage();
 			}
 
 			// set default command to the bundle menu resource
 			Command defaultCommand = new Command() {
 				@Override
 				public void menuSelected(MenuItem selectedItem) {
 					window.showNotification("Module Warning",
 							"The Module: " + metadataModule.getId() + "["
 									+ metadataModule.getVersion()
 									+ "] is not deploy",
 							Window.Notification.TYPE_WARNING_MESSAGE);
 
 				}
 			};
 			resource.getMenu().setCommand(defaultCommand);
 
 			// reset the bundle component resource
 			resource.setComponentView(null);
 
 		}
 
 	}
 
 	@Override
 	public void close() {
 		moduleService.removeListener(this);
 		super.close();
 
 	}
 
 	@Override
 	public void viewChanged(ViewEvent arg0) {
 
 		if (arg0.getViewFrom() instanceof SecurityAccessView) {
 
 			SecurityAccessView securityAccessView = (SecurityAccessView) arg0.getViewFrom();
 			try {
 				
 				
 				User loggedUser = userService.getByUsername(securityAccessView.getGrantedUser().getUsername());
 				loadWorkbenchContext(loggedUser);
 				
 				WorkbenchView workbenchView =  (WorkbenchView) arg0.getViewTo();
 				workbenchView.setLoggedUser(loggedUser);
 								
 				menuManager.setUser(loggedUser);
 				
 				// construct the menu from scratch
 				initMenuManager();
 				
 			} catch (Exception e) {
 				throw new RuntimeException(e);
 			}
 		}
 
 		if (arg0.getViewFrom() instanceof WorkbenchView) {
 
 		}
 	}
 
 	private void loadWorkbenchContext(User user) throws Exception {
 		Locale defaultLocale = null;
 	
 		// create locale for employee agent
 		String[] localeParams = user.getDefaultLocale().split(LocaleField.LOCALE_SEPARATOR);
 			
 		String language = localeParams[0];
 		String country = localeParams[1];
 		
 		defaultLocale = new Locale(language, country);
 		
 		// initialize active Organization tree
 		user.setActiveOrganization(user.getDefaultOrganization());
 		user.setActiveLocation(user.getDefaultLocation());
 		user.setActiveArea(user.getDefaultArea());
 		user.setActiveLocale(defaultLocale);
 		
 		workbenchContext = new WorkbenchContext(
 				user,	
 				menuManager,
 				toolbarManager,
 				resourceManager);
 	}
 	
 	@Override
 	public void terminalError(Terminal.ErrorEvent event) {
 	    // Call the default implementation.
 	    //super.terminalError(event);
 	    
         final Throwable t = event.getThrowable();
         if (t instanceof SocketException) {
             // Most likely client browser closed socket
             logger.info("SocketException in CommunicationManager."
                     + " Most likely client (browser) closed socket.");
             return;
         }
         
         // show default error view form
         ErrorViewForm errorViewForm;
         if (t.getCause() != null)
         	errorViewForm = new ErrorViewForm(t.getCause().getMessage(), t.getCause().getCause(), getMainWindow());
         else
         	errorViewForm = new ErrorViewForm(null, null, getMainWindow());
         
         errorViewForm.show();
         
         // also print the error on console
         logger.log(Level.SEVERE, "Terminal error:", t);
 
 	}
 }
