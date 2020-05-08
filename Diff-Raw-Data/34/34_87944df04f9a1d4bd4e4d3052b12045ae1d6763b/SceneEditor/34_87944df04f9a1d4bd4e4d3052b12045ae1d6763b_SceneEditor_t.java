 package scene1;
 import java.awt.*;
 import java.awt.geom.*;
 import java.awt.event.*;
 import javax.swing.*;
 
 /**
    A program that allows users to edit a scene composed
    of items.
 */
 public class SceneEditor
 {
    public static void main(String[] args)
    {
       JFrame frame = new JFrame();
       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
       final SceneComponent scene = new SceneComponent();
     
       //begin messing with actions -- STEP 2
       AddObjectAction addCarAction = new AddObjectAction(scene, AddObjectAction.ADD_CAR);
       AddObjectAction addHouseAction = new AddObjectAction(scene, AddObjectAction.ADD_HOUSE);
       RemoveObjectAction removeAction = new RemoveObjectAction(scene);
       
       addCarAction.addOpposite(removeAction);
       addHouseAction.addOpposite(removeAction);
      addCarAction.addOtherAdder(addHouseAction);
      addHouseAction.addOtherAdder(addCarAction);
      
       removeAction.addOpposite(addCarAction);
       removeAction.addOpposite(addHouseAction);
       
       addCarAction.setEnabled(true);
       addHouseAction.setEnabled(true);
       removeAction.setEnabled(false);
       
       //end setting up actions -- STEP 2
       
       JButton houseButton = new JButton(addHouseAction);
       houseButton.setText("Add House");
       
 
       JButton carButton = new JButton(addCarAction);
       carButton.setText("Add Car");
       
       JButton removeButton = new JButton(removeAction);
       removeButton.setText("Remove Selected");
       
       
       JPanel buttons = new JPanel();
       buttons.add(houseButton);
       buttons.add(carButton);
       buttons.add(removeButton);
 
       frame.add(scene, BorderLayout.CENTER);
       frame.add(buttons, BorderLayout.NORTH);
       frame.setSize(1440, 900);
       frame.setVisible(true);
    }
 }
 
 
