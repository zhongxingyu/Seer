 package com.fornacif.osgi.manager.internal.application;
 
 import java.util.Collection;
 import java.util.Map;
 
 import javafx.application.Platform;
 import javafx.fxml.FXMLLoader;
 import javafx.scene.layout.Pane;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceEvent;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.hooks.service.EventListenerHook;
 import org.osgi.framework.hooks.service.ListenerHook.ListenerInfo;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class FXMLLoaderDelegator implements EventListenerHook {
 
 	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
 
 	private final String FXML_SERVICE_PROPERTY = "fxml";
 	private final String CSS_SERVICE_PROPERTY = "css";
 	
 	public FXMLLoaderDelegator(BundleContext bundleContext) {
 		try {
 			Collection<ServiceReference<Pane>> controllers = bundleContext.getServiceReferences(Pane.class, null);
 			for (ServiceReference<Pane> serviceReference : controllers) {
 				loadFXML(serviceReference);
 			}
 		} catch (InvalidSyntaxException e) {
 			LOGGER.error("Error getting Controller services", e);
 		}
 	}
 
 	@Override
 	public void event(ServiceEvent event, Map<BundleContext, Collection<ListenerInfo>> listeners) {
 		if (event.getType() == ServiceEvent.REGISTERED) {
 			ServiceReference<?> serviceReference = event.getServiceReference();
 			String[] objectClass = (String[]) serviceReference.getProperty(Constants.OBJECTCLASS);
 			for (String clazz : objectClass) {
 				if (Pane.class.getName().equals(clazz)) {
 					loadFXML(serviceReference);
 				}
 			}
 		}
 		
 	}
 
 	private void loadFXML(final ServiceReference<?> serviceReference) {
 		final String fxml = (String) serviceReference.getProperty(FXML_SERVICE_PROPERTY);
 		final String css = (String) serviceReference.getProperty(CSS_SERVICE_PROPERTY);
 		if (fxml != null) {
 			BundleContext bundleContext = serviceReference.getBundle().getBundleContext();
 			final Pane controller = (Pane) bundleContext.getService(serviceReference);
 			Platform.runLater(new Runnable() {
 				@Override
 				public void run() {
 					try {
 						FXMLLoader fxmlLoader = new FXMLLoader();
 						fxmlLoader.setClassLoader(controller.getClass().getClassLoader());
 						fxmlLoader.setRoot(controller);
 						fxmlLoader.setController(controller);
 						if (css != null) {
 							controller.getStylesheets().add(controller.getClass().getResource(css).toExternalForm());
 						}
 						fxmlLoader.load(controller.getClass().getResourceAsStream(fxml));
 					} catch (Exception e) {
 						LOGGER.error("Error loading FXML for service", e);
					}		
 				}
 			}); 
 		}	
 	}
 
 }
