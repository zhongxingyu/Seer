 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gui.controlwidgets;
 
 import icarus.exceptions.ComponentFailedException;
 import icarus.exceptions.InvalidRodsException;
 import icarus.operatingsoftware.Components;
 import icarus.operatingsoftware.PlantControl;
 import java.awt.event.ActionEvent;
 import java.util.Observable;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JSlider;
 import javax.swing.JToggleButton;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 /**
  *
  * @author drm
  */
 public class ReactorControl extends ControlWidget implements ChangeListener {
 
     JSlider controlRodPosition;
     JToggleButton repairButton;
 
     public ReactorControl(PlantControl plant) {
         super(plant);
         addTitle("Reactor");
         controlRodPosition = new JSlider(JSlider.VERTICAL, 0, 100, 0);
         vbox.add(Align.centerVertical(controlRodPosition));
         repairButton = addToggleButton("Repair");        
     }
 
     @Override
     public void actionPerformed(ActionEvent event) {
         Object source = event.getSource();
         try {
             if (source == repairButton) {
                 plant.fix(Components.REACTOR);
 
             }
         } catch (Exception e) {
             Logger.getLogger(PumpControl.class.getName()).log(Level.SEVERE, null, e);
         }
     }
 
     @Override
     public void update(Observable o, Object o1) {
         if (o instanceof PlantControl) {
             PlantControl plantControl = (PlantControl)o;
             final boolean reactorIsFunctional = plantControl.functional(Components.REACTOR);
             repairButton.setEnabled(!reactorIsFunctional && !plantControl.fixUnderway());
             repairButton.setSelected(plantControl.isRepairing(Components.REACTOR));
             controlRodPosition.setValue(plantControl.rodHeight());
             controlRodPosition.setEnabled(reactorIsFunctional);
         }
     }
 
     @Override
     public void stateChanged(ChangeEvent event) {
         Object source = event.getSource();
         try {
             if (source == controlRodPosition) {
                 plant.movecontrolrods(controlRodPosition.getValue());
             }
         } catch (ComponentFailedException e) {
             Logger.getLogger(PumpControl.class.getName()).log(Level.SEVERE, null, e);
         } catch (InvalidRodsException e) {
             Logger.getLogger(PumpControl.class.getName()).log(Level.SEVERE, null, e);
         }
     }
 }
