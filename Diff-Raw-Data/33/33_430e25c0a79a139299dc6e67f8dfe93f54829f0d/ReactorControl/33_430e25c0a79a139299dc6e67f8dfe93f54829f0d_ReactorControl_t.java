 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gui;
 
 import icarus.operatingsoftware.Components;
 import icarus.operatingsoftware.OperatingSoftware;
 import icarus.operatingsoftware.Plant;
 import icarus.operatingsoftware.PlantControl;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Observable;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.Box;
 import javax.swing.JLabel;
 import javax.swing.JSlider;
 import javax.swing.JToggleButton;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 /**
  *
  * @author drm
  */
 public class ReactorControl extends ControlWidget implements ActionListener, ChangeListener {
     JSlider controlRodPosition;
     JToggleButton repairButton;
     
     public ReactorControl (PlantControl plant) {
         super(plant);
         Box box = Box.createVerticalBox();        
         JLabel title = new JLabel("Reactor");
         controlRodPosition = new JSlider(JSlider.VERTICAL, 0, 100, 0);
         repairButton = new JToggleButton("Repair");        
         add(box);
         box.add(Align.left(title));
         box.add(Align.centerVertical(controlRodPosition));
         box.add(Box.createVerticalGlue());
         box.add(Align.centerVertical(repairButton));
         
         addActionListeners();
     }
     
     
 
     @Override
     public void actionPerformed(ActionEvent event) {
         Object source = event.getSource();
         try
         {
             if(source==repairButton)
             {
                 plant.fix(Components.REACTOR);
                           
             }
            
             
             
         }
         catch(Exception  e)
         {
             Logger.getLogger(PumpControl.class.getName()).log(Level.SEVERE, null, e);
         }
        plant.next();
     }
     
     @Override
     public void update(Observable o, Object o1) {
         if (o instanceof PlantControl) {
             PlantControl plantControl = (PlantControl)o;
             repairButton.setEnabled(!plantControl.functional(Components.REACTOR));
             repairButton.setSelected(plantControl.isRepairing(Components.REACTOR));
             controlRodPosition.setValue(plantControl.rodHeight());
         }
     }
 
     @Override
     public void stateChanged(ChangeEvent event) {
         Object source = event.getSource();
         try
         {
             if(source==controlRodPosition)
             {
                 plant.movecontrolrods(controlRodPosition.getValue());
             }
         }
         catch(Exception e)
         {
             Logger.getLogger(PumpControl.class.getName()).log(Level.SEVERE, null, e);
         }
     }
 
     private void addActionListeners() {
         repairButton.addActionListener(this);
         controlRodPosition.addChangeListener(this);
     }
 
     
 }
