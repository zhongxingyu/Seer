 package gui;
 
 import icarus.exceptions.AlreadyAtStateException;
 import icarus.exceptions.ComponentFailedException;
 import icarus.exceptions.FixAlreadyUnderwayException;
 import icarus.exceptions.InvalidComponentException;
 import icarus.exceptions.InvalidPumpException;
 import icarus.exceptions.NoFixNeededException;
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
 import javax.swing.JToggleButton;
 
 /**
  *
  * @author drm
  */
 public class PumpControl extends ControlWidget implements ActionListener {
     int pumpNumber;
     JToggleButton onButton;
     JToggleButton offButton;
     JToggleButton repairButton;
     public PumpControl (PlantControl plant, int pumpNumber) {
         super(plant);
         this.pumpNumber = pumpNumber;
         Box box = Box.createVerticalBox();
         JLabel title = new JLabel("Pump " + pumpNumber);
         onButton = new JToggleButton("On");
         offButton = new JToggleButton("Off" + " "); //Java sucks
         repairButton = new JToggleButton("Repair");
         add(box);
         box.add(Align.left(title));
         box.add(Align.centerVertical(onButton));
         box.add(Align.centerVertical(offButton));
         box.add(Box.createVerticalGlue());
         box.add(Align.centerVertical(repairButton));
         
         addActionListeners();
     }
 
 
     private void addActionListeners()
     {
         onButton.addActionListener(this);
         offButton.addActionListener(this);
         repairButton.addActionListener(this);
     }
     
     @Override
     public void actionPerformed(ActionEvent event) {
         
         
         Object source = event.getSource();
         try
         {
             if(source==onButton)
             {
                 plant.turnOn(pumpNumber);
             }
             else if(source==offButton)
             {
                 plant.turnOff(pumpNumber);
             }
             else if(source==repairButton)
             {
                 plant.fix(Components.WATERPUMP, pumpNumber);
             }
            plant.next();
         }
         catch(Exception  e)
         {
             Logger.getLogger(PumpControl.class.getName()).log(Level.SEVERE, null, e);
         }
       
     }
     
     @Override
     public void update(Observable o, Object o1) {
         if (o instanceof PlantControl) {
             PlantControl plantControl = (PlantControl)o;
             onButton.setSelected(plantControl.isWaterPumpActive(pumpNumber));
             offButton.setSelected(!plantControl.isWaterPumpActive(pumpNumber) &&
                                   plantControl.functional(Components.WATERPUMP, pumpNumber));
             
             onButton.setEnabled(!plantControl.isWaterPumpActive(pumpNumber));
             offButton.setEnabled(plantControl.isWaterPumpActive(pumpNumber) && 
                                  plantControl.functional(Components.WATERPUMP, pumpNumber));
             
             repairButton.setEnabled(!plantControl.functional(Components.WATERPUMP, pumpNumber));
             repairButton.setSelected(plantControl.isRepairing(Components.WATERPUMP, pumpNumber));
         }
     }
 
     
 }
