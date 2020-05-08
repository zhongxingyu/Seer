 package de.hswt.hrm.plant.ui.wizard;
 
 import java.net.URL;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.eclipse.e4.xwt.IConstants;
 import org.eclipse.e4.xwt.XWT;
 import org.eclipse.e4.xwt.forms.XWTForms;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Text;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Optional;
 
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.place.model.Place;
 import de.hswt.hrm.place.service.PlaceService;
 import de.hswt.hrm.plant.model.Plant;
 
 public class PlantWizardPageOne extends WizardPage {
 
     private static final Logger LOG = LoggerFactory.getLogger(PlantWizardPageOne.class);
     
     private Composite container;
     private Optional<Plant> plant;
     private Collection<Place> places;
     
     public PlantWizardPageOne(String title, Optional<Plant> plant) {
         super(title);
         this.plant = plant;
         setDescription(createDescription());
     }
     
     private String createDescription() {
         if (plant.isPresent()) {
             return "Anlage bearbeiten";
         }
         return "Neue Anlage erstellen";
     }
 
     public void createControl(Composite parent) {
         URL url = PlantWizardPageOne.class.getClassLoader().getResource(
                 "de/hswt/hrm/plant/ui/xwt/PlantWizardWindow" + IConstants.XWT_EXTENSION_SUFFIX);
         try {
             container = (Composite) XWTForms.load(parent, url);
         }
         catch (Exception e) {
             LOG.error("An error occured: ", e);
         }
         if (this.plant.isPresent()) {
             updateFields(container);
         }
         setControl(container);
         initPlaces();
         setKeyListener();
         setPageComplete(false);
     }
     
     private void initPlaces() {
         Combo combo = (Combo) XWT.findElementByName(container, "place");
         try {
             places = PlaceService.findAll();
             for (Iterator<Place> i=places.iterator(); i.hasNext();) {
                 String placeName = i.next().getPlaceName();
                 combo.add(placeName);
             }
         } catch (DatabaseException e) {
             LOG.error("An Error occured: ", e);
         }        
     }
     
     public Optional<Place> getSelectedPlace() {
         Place selectedPlace = null;
         Combo combo = (Combo) XWT.findElementByName(container, "place");
         String selectedPlaceName = combo.getItem(combo.getSelectionIndex());
        for (Iterator<Place> i=places.iterator(); i.hasNext();) {
           if (selectedPlaceName.equals(i.next().getPlaceName())){
               selectedPlace = i.next();
            }
         }
         return Optional.fromNullable(selectedPlace);
     }
         
     private void updateFields(Composite c) {
         Plant p = plant.get();
         Text t = (Text) XWT.findElementByName(c, "description");
         t.setText(p.getDescription());
         Combo combo = (Combo) XWT.findElementByName(container, "place");
         String placeName = p.getPlace().get().getPlaceName();
         combo.select(combo.indexOf(placeName));
         //TODO nextInspection / inspectionIntervall ?
         //TODO scheme
         
         t = (Text) XWT.findElementByName(c, "manufactor");
         t.setText(p.getManufactor().or(""));
         t = (Text) XWT.findElementByName(c, "constructionYear");
         t.setText(p.getConstructionYear().orNull().toString());
         t = (Text) XWT.findElementByName(c, "type");
         t.setText(p.getType().or(""));
         t = (Text) XWT.findElementByName(c, "airPerformance");
         t.setText(p.getAirPerformance().or(""));
         t = (Text) XWT.findElementByName(c, "motorPower");
         t.setText(p.getMotorPower().or(""));
         t = (Text) XWT.findElementByName(c, "ventilatorPerformance");
         t.setText(p.getVentilatorPerformance().or(""));
         t = (Text) XWT.findElementByName(c, "motorRPM");
         t.setText(p.getMotorRpm().or(""));
         t = (Text) XWT.findElementByName(c, "current");
         t.setText(p.getCurrent().or(""));
         t = (Text) XWT.findElementByName(c, "voltage");
         t.setText(p.getVoltage().or(""));
         t = (Text) XWT.findElementByName(c, "note");
         t.setText(p.getNote().or(""));
     }    
     
     public HashMap<String, Text> getMandatoryWidgets() {
         HashMap<String, Text> widgets = new HashMap<String, Text>();
         widgets.put("description", (Text) XWT.findElementByName(container, "description"));
         //TODO place - mandatory?
         //TODO nextInspection / inspectionIntervall ?
         widgets.put("inspectionIntervall", (Text) XWT.findElementByName(container, "inspectionIntervall"));
         //TODO scheme
         return widgets;
     }
     
     public HashMap<String, Text> getOptionalWidgets() {
         HashMap<String, Text> widgets = new HashMap<String, Text>();
         widgets.put("manufactor", (Text) XWT.findElementByName(container, "manufactor"));
         widgets.put("constructionYear", (Text) XWT.findElementByName(container, "constructionYear"));
         widgets.put("type", (Text) XWT.findElementByName(container, "type"));
         widgets.put("airPerformance", (Text) XWT.findElementByName(container, "airPerformance"));
         widgets.put("motorPower", (Text) XWT.findElementByName(container, "motorPower"));
         widgets.put("ventilatorPerformance", (Text) XWT.findElementByName(container, "ventilatorPerformance"));
         widgets.put("motorRPM", (Text) XWT.findElementByName(container, "motorRPM"));
         widgets.put("current", (Text) XWT.findElementByName(container, "current"));
         widgets.put("voltage", (Text) XWT.findElementByName(container, "voltage"));
         widgets.put("note", (Text) XWT.findElementByName(container, "note"));
         return widgets;
     }
     
     @Override
     public boolean isPageComplete() {
         for (Text textField : getMandatoryWidgets().values()) {
             if (textField.getText().length() == 0) {
                 return false;
             }
         }
         Combo combo = (Combo) XWT.findElementByName(container, "place");
         if (combo.getSelectionIndex()==-1){
             return false;
         }
         return true;
     }
     
     public void setKeyListener() {
         HashMap<String, Text> widgets = getMandatoryWidgets();
         for (Text text : widgets.values()) {
 
             text.addKeyListener(new KeyListener() {
 
                 @Override
                 public void keyPressed(KeyEvent e) {
                 }
 
                 @Override
                 public void keyReleased(KeyEvent e) {
                     getWizard().getContainer().updateButtons();
                 }
             });
         }
     }
     
 }
