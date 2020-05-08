 /*******************************************************************************
  * Copyright (c) 2012, All Rights Reserved.
  * 
  * Generation Challenge Programme (GCP)
  * 
  * 
  * This software is licensed for use under the terms of the GNU General Public
  * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
  * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
  * 
  *******************************************************************************/
 package org.generationcp.ibpworkbench.actions;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.generationcp.ibpworkbench.IBPWorkbenchApplication;
 import org.generationcp.ibpworkbench.comp.form.AddLocationForm;
 import org.generationcp.ibpworkbench.comp.project.create.ProjectLocationsComponent;
 import org.generationcp.ibpworkbench.comp.window.AddLocationsWindow;
 import org.generationcp.ibpworkbench.model.LocationModel;
 import org.generationcp.middleware.pojos.Location;
 import org.generationcp.middleware.pojos.User;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Configurable;
 
 import com.vaadin.data.util.BeanItem;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 
 /**
  * 
  * @author Jeffrey Morales
  * 
  */
 
 @Configurable
 public class SaveNewLocationAction implements ClickListener {
     
     private static final Logger LOG = LoggerFactory.getLogger(SaveNewLocationAction.class);
     private static final long serialVersionUID = 1L;
    
     private AddLocationForm newLocationForm;
     
     private AddLocationsWindow window;
     
 	private ProjectLocationsComponent projectLocationsComponent;
     
     public SaveNewLocationAction(AddLocationForm newLocationForm, AddLocationsWindow window, ProjectLocationsComponent projectLocationsComponent) {
         this.newLocationForm = newLocationForm;
         this.window = window;
         this.projectLocationsComponent=projectLocationsComponent;
 
     }
     
     
     @Override
     public void buttonClick(ClickEvent event) {
     	newLocationForm.commit();
 
         @SuppressWarnings("unchecked")
         BeanItem<LocationModel> locationBean = (BeanItem<LocationModel>) newLocationForm.getItemDataSource();
         LocationModel location = locationBean.getBean();
         
         newLocationForm.commit();
         
         IBPWorkbenchApplication app = IBPWorkbenchApplication.get();
         
         if (!app.getSessionData().getUniqueLocations().contains(location.getLocationName())){
         
         	app.getSessionData().getUniqueLocations().add(location.getLocationName());
         	
        	Integer nextKey = app.getSessionData().getProjectLocationData().keySet().size();
         	
         	nextKey = nextKey*-1;
         	
         	LocationModel newLocation = new LocationModel();
         	
         	newLocation.setLocationName(location.getLocationName());
         	newLocation.setLocationAbbreviation(location.getLocationAbbreviation());
         	newLocation.setLocationId(nextKey);
         
             app.getSessionData().getProjectLocationData().put(nextKey, newLocation);
             
             LOG.info(app.getSessionData().getProjectLocationData().toString());
          
             newLocationForm.commit();
             
             Location newLoc=new Location();
             newLoc.setLocid(newLocation.getLocationId());
         	newLoc.setLname(newLocation.getLocationName());
         	newLoc.setLabbr(newLocation.getLocationAbbreviation());
 
             projectLocationsComponent.getSelect().addItem(newLoc);
             projectLocationsComponent.getSelect().setItemCaption(newLoc, newLoc.getLname());
         	
             projectLocationsComponent.getSelect().select(newLoc);
             projectLocationsComponent.getSelect().setValue(newLoc);
 
             newLocation = null;
             window.getParent().removeWindow(window);
         
         }
         
     }
 }
