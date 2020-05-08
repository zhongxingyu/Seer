 package de.hswt.hrm.plant.ui.wizard;
 
 import java.util.HashMap;
 
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.swt.widgets.Text;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Optional;
 
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.database.exception.SaveException;
 import de.hswt.hrm.plant.model.Plant;
 import de.hswt.hrm.plant.service.PlantService;
 
 public class PlantWizard extends Wizard {
     
     private static final Logger LOG = LoggerFactory.getLogger(PlantWizard.class);
     private PlantWizardPageOne first;
     private Optional<Plant> plant;
 
     public PlantWizard(Optional<Plant> plant) {
         this.plant = plant;
         first = new PlantWizardPageOne("Erste Seite", plant);
         
         if (plant.isPresent()) {
             setWindowTitle("Anlage bearbeiten");
         } else {
             setWindowTitle("Neue Anlage erstellen");
         } 
     }
 
     @Override
     public void addPages() {
         addPage(first);
     }
     
     @Override
     public boolean canFinish() {
         return first.isPageComplete();
     }
 
     @Override
     public boolean performFinish() {
         if (plant.isPresent()) {
             return editExistingPlant();
         } else {
             return insertNewPlant();
         }
     }
     
     private boolean editExistingPlant() {
         Plant p = this.plant.get();
         try {
             // Update plant from DB
             p = PlantService.findById(p.getId());
             // Set values to fields from WizardPage
             p = setValues(plant);
             // Update plant in DB
             PlantService.update(p);
             plant = Optional.of(p);
         } catch (DatabaseException e) {
             LOG.error("An error occured", e);
         }
         return true;
     }
     
     private boolean insertNewPlant() {
         Plant p = setValues(Optional.<Plant>absent());
         try {
             plant = Optional.of(PlantService.insert(p));
         } catch (SaveException e) {
             LOG.error("Could not save Element: "+ plant +"into Database", e);
         }
         return true;
     }
     
     private Plant setValues(Optional<Plant> p) {
         HashMap<String, Text> mandatoryWidgets = first.getMandatoryWidgets();
         String description = mandatoryWidgets.get("description").getText();
         //TODO place - mandatory?
         //TODO nextInspection / inspectionIntervall?
         String inspectionIntervall = mandatoryWidgets.get("inspectionIntervall").getText();
         //TODO scheme
         
         HashMap<String, Text> optionalWidgets = first.getOptionalWidgets();
         String manufactor = optionalWidgets.get("manufactor").getText();
         String constructionYear = optionalWidgets.get("constructionYear").getText();
         String type = optionalWidgets.get("type").getText();
         String airPerformance = optionalWidgets.get("airPerformance").getText();
         String motorPower = optionalWidgets.get("motorPower").getText();
         String ventilatorPerformance = optionalWidgets.get("ventilatorPerformance").getText();
         String motorRPM = optionalWidgets.get("motorRPM").getText();
         String current = optionalWidgets.get("current").getText();
         String voltage = optionalWidgets.get("voltage").getText();
         String note = optionalWidgets.get("note").getText();
         
         Plant plant;
         if (p.isPresent()) {
             plant = p.get();
             plant.setDescription(description);
             //TODO place? nextInspection?
             plant.setInspectionInterval(Integer.parseInt(inspectionIntervall));
             //TODO scheme
         } else {
             plant = new Plant(Integer.parseInt(inspectionIntervall), description);
             //TODO place? nextInspection?
             //TODO scheme
         }
         plant.setManufactor(manufactor);
        plant.setConstructionYear(Integer.parseInt(constructionYear));
         plant.setType(type);
         plant.setAirPerformance(airPerformance);
         plant.setMotorPower(motorPower);
         plant.setVentilatorPerformance(ventilatorPerformance);
         plant.setMotorRpm(motorRPM);
         plant.setCurrent(current);
         plant.setVoltage(voltage);
         plant.setNote(note);
         
         return plant;
     }
     
     public Optional<Plant> getPlant() {
         return plant;
     }
 
 }
