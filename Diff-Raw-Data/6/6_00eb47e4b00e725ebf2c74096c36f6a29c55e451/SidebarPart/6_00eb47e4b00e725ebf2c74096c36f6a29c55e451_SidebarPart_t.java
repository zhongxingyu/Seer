 package de.hswt.hrm.main.ui.sidebarPart;
 
 import java.net.URL;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 
 import org.eclipse.e4.core.contexts.IEclipseContext;
 import org.eclipse.e4.ui.workbench.modeling.EPartService;
 import org.eclipse.e4.xwt.IConstants;
 import org.eclipse.e4.xwt.XWT;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 
 import de.hswt.hrm.main.ui.MPartSwitcher;
 
 
 public class SidebarPart {
     
 	@Inject
 	EPartService service;
 
     @PostConstruct
     public void postConstruct(Composite parent, IEclipseContext context) {
 
         URL url = SidebarPart.class.getClassLoader().getResource(
                 "de/hswt/hrm/main/ui/sidebarXwt/SideBar" + IConstants.XWT_EXTENSION_SUFFIX);
 
         try {
 
             // Obtain root element of the XWT file
             final Composite comp = (Composite) XWT.load(parent, url);
             
             ToolBar toolBar = (ToolBar)XWT.findElementByName(comp, "toolBar");
             setListenerForToolbar(toolBar);
             addIcons(toolBar);
         }
         catch (Exception e) {
         }
     }
     
     private void setListenerForToolbar(ToolBar bar){    	
     	((ToolItem) XWT.findElementByName(bar, "toPlaces")).addListener(SWT.Selection, new Listener() {			
 			@Override
 			public void handleEvent(Event event) {
 				MPartSwitcher.setPartVisible(service, MPartSwitcher.PLACES_ID);
 			}
 		});
     	((ToolItem) XWT.findElementByName(bar, "toPlants")).addListener(SWT.Selection, new Listener() {			
 			@Override
 			public void handleEvent(Event event) {
 				MPartSwitcher.setPartVisible(service, MPartSwitcher.PLANTS_ID);
 			}
 		});
     	((ToolItem) XWT.findElementByName(bar, "toCatalog")).addListener(SWT.Selection, new Listener() {			
 			@Override
 			public void handleEvent(Event event) {
 				MPartSwitcher.setPartVisible(service, MPartSwitcher.CATALOG_ID);
 			}
 		});
     	((ToolItem) XWT.findElementByName(bar, "toCategory")).addListener(SWT.Selection, new Listener() {			
 			@Override
 			public void handleEvent(Event event) {
 				MPartSwitcher.setPartVisible(service, MPartSwitcher.CATEGORY_ID);
 			}
 		});
     	((ToolItem) XWT.findElementByName(bar, "toMain")).addListener(SWT.Selection, new Listener() {			
 			@Override
 			public void handleEvent(Event event) {
 				MPartSwitcher.setPartVisible(service, MPartSwitcher.MAIN_ID);
 			}
 		});
     	((ToolItem) XWT.findElementByName(bar, "toContacts")).addListener(SWT.Selection, new Listener() {			
 			@Override
 			public void handleEvent(Event event) {
 				MPartSwitcher.setPartVisible(service, MPartSwitcher.CONTACTS_ID);
 			}
 		});   	
     	((ToolItem) XWT.findElementByName(bar, "toInspection")).addListener(SWT.Selection, new Listener() {			
 			@Override
 			public void handleEvent(Event event) {
 				MPartSwitcher.setPartVisible(service, MPartSwitcher.INSPECTION_ID);
 			}
 		});
     	((ToolItem) XWT.findElementByName(bar, "toOverall")).addListener(SWT.Selection, new Listener() {			
 			@Override
 			public void handleEvent(Event event) {
 				MPartSwitcher.setPartVisible(service, MPartSwitcher.OVERALL_ID);
 			}
 		});   	
     }
     
     private void addIcons(ToolBar bar) {
     	((ToolItem) XWT.findElementByName(bar, "toPlaces")).setImage(getImage("/resources/icons/places_32.bmp"));
     	((ToolItem) XWT.findElementByName(bar, "toPlants")).setImage(getImage("/resources/icons/plants_32.bmp"));
     	((ToolItem) XWT.findElementByName(bar, "toCatalog")).setImage(getImage("/resources/icons/catalogs_32.bmp"));
    	((ToolItem) XWT.findElementByName(bar, "toCategory")).setImage(getImage("/resources/icons/components_32.bmp"));
     	((ToolItem) XWT.findElementByName(bar, "toMain")).setImage(getImage("/resources/icons/home_32.bmp"));
     	((ToolItem) XWT.findElementByName(bar, "toContacts")).setImage(getImage("/resources/icons/contacts_32.bmp"));
     	((ToolItem) XWT.findElementByName(bar, "toInspection")).setImage(getImage("/resources/icons/reports_32.bmp"));
    	((ToolItem) XWT.findElementByName(bar, "toOverall")).setImage(getImage("/resources/icons/summary_32.bmp"));
    	
     }
     
     private Image getImage(String image) {
     		return ImageDescriptor.createFromFile(this.getClass(), image).createImage();
    	}
    
 }
