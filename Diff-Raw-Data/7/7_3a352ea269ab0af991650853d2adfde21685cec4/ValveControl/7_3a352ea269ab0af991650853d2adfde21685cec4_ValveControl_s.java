 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gui;
 
import icarus.exceptions.InvalidValveException;
import icarus.operatingsoftware.Components;
import icarus.operatingsoftware.OperatingSoftware;
import icarus.operatingsoftware.Plant;
 import icarus.operatingsoftware.PlantControl;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Observable;
 import javax.swing.Box;
 import javax.swing.JLabel;
 import javax.swing.JToggleButton;
 
 /**
  *
  * @author drm
  */
 public class ValveControl extends ControlWidget implements ActionListener {
     int valveNumber;
     JToggleButton openButton;
     JToggleButton closeButton;
     
     public ValveControl(PlantControl plant, int valveNumber) {
         super(plant);
         this.valveNumber = valveNumber;
         Box box = Box.createVerticalBox();
         JLabel title = new JLabel("Valve " + valveNumber);
         openButton = new JToggleButton("Open");
         closeButton = new JToggleButton("Close");
         add(box);
         box.add(Align.left(title));
         box.add(Align.centerVertical(openButton));
         box.add(Align.centerVertical(closeButton));
         
         
         addActionListeners();
     }
 
 
     private void addActionListeners()
     {
         openButton.addActionListener(this);
         closeButton.addActionListener(this);
         
     }
     
     @Override
     public void actionPerformed(ActionEvent event) {
         
         
         Object source = event.getSource();
         try
         {
             if(source==openButton)
             {
                 plant.open(valveNumber);
             }
             else if(source==closeButton)
             {
                 plant.close(valveNumber);
             }
             
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
             openButton.setSelected(plantControl.isValveOpened(valveNumber));
             closeButton.setSelected(!plantControl.isValveOpened(valveNumber));
             
             openButton.setEnabled(!plantControl.isValveOpened(valveNumber));
             closeButton.setEnabled(plantControl.isValveOpened(valveNumber));
         }
     }
 }
