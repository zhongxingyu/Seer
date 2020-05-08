 /**
  * EditorWindow
  * <p>
  * The window for the Airport editor
  * 
  * @author Serjoscha Bassauer
  */
 package de.bwv_aachen.dijkstra.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Arrays;
 import java.util.Map;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.WindowConstants;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import org.joda.time.Duration;
 
 import de.bwv_aachen.dijkstra.controller.Controller;
 import de.bwv_aachen.dijkstra.helpers.DateHelper;
 import de.bwv_aachen.dijkstra.model.Airport;
 import de.bwv_aachen.dijkstra.model.Connection;
 
 @SuppressWarnings("serial")
 public class EditorWindow extends View  implements ActionListener, ListSelectionListener {
 
     // Beans
     JPanel          connectionsContainer;
     JList<Airport>  locationJList;
     
     JButton lAdd;
     JButton lRem;
     JButton rAdd;
     JButton rRem;
     
     // Helper Window(s)
     EditorWindow_AirportSelector airportSel;
     
     // Model(s)
     DefaultListModel<Airport> lm = new DefaultListModel<>();
 
     public EditorWindow(Controller c) {
         super(c);
         
         // generate Airport List Model
         for(Airport ca: controller.getModel().getAirportList().values()) { // assign every location to the jList Model
             this.lm.addElement(ca);
         }
     }
 
     public void draw() {
         super.getContentPane().removeAll(); // making this function being able to repaint the mainwindow
         super.setTitle("Bearbeiten");
         super.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
         super.setResizable(false);
 
         super.setLayout(new GridLayout(1, 2, 10, 0));
 
         ((JComponent) getContentPane()).setBorder(BorderFactory.createMatteBorder(4, 4, 4, 4, Color.LIGHT_GRAY));
 
         // Build the UI Elems
         //locationJList = new JList<Airport>(locations); // this will create a jlist without an model -> completly unusable
         locationJList = new JList<Airport>(lm);
         connectionsContainer = new JPanel();
         
         // Container for the left and the right side
         JPanel leftContainer = new JPanel();
         leftContainer.setLayout(new BorderLayout());
         JPanel rightContainer = new JPanel();
         rightContainer.setLayout(new BorderLayout());
         
         // Buttons
         this.lAdd = new JButton("+");
         this.lAdd.setActionCommand("lAdd");
         this.lRem = new JButton("-");
         this.lRem.setActionCommand("lRem");
         this.rAdd = new JButton("+");
         this.rAdd.setActionCommand("rAdd");
         this.rAdd.setEnabled(false);
         this.rRem = new JButton("-");
         this.rRem.setActionCommand("rRem");
         this.rRem.setEnabled(false);
         
         // Container for the buttons
         JPanel lButtons = new JPanel();
         lButtons.setLayout(new FlowLayout());
         JPanel rButtons = new JPanel();
         rButtons.setLayout(new FlowLayout());
         
         // Add buttons to container
         lButtons.add(lAdd);
         lButtons.add(lRem);
         
         rButtons.add(rAdd);
         rButtons.add(rRem);
 
         // Add ActionListening
         //locationJList.addMouseListener(this);
         locationJList.addListSelectionListener(this);
         this.lAdd.addActionListener(this);
         this.rAdd.addActionListener(this);
         this.lRem.addActionListener(this);
         this.rRem.addActionListener(this);
         
         // Add lists and buttons to the correct jpanel
         leftContainer.add(locationJList, BorderLayout.CENTER);
         leftContainer.add(lButtons, BorderLayout.SOUTH);
         
         rightContainer.add(connectionsContainer, BorderLayout.CENTER);
         rightContainer.add(rButtons, BorderLayout.SOUTH);
         
         // Add elems (panels) to frame
         super.getContentPane().add(leftContainer);
         super.getContentPane().add(rightContainer);
 
         // Do the rest for displaying the window
         super.pack();
         super.setLocationRelativeTo(null); // center the frame
 
         // Show the window
         super.setVisible(true);
     }
 
     public void actionPerformed(ActionEvent e) {
         //JButton button = (JButton)e.getSource();
         switch(e.getActionCommand()){
             case "lAdd": // add FROM/source airport
                 String input = JOptionPane.showInputDialog("Name des Flughafens:");
                 if(input != null) { // prevents some nullpointer exceptions (which would not take any effect for the program, but disturbed me)
                     if(!input.equals("")) {
                         DefaultListModel<Airport> lm = (DefaultListModel<Airport>)this.locationJList.getModel();
                         
                         Airport nAp = new Airport(lm.lastElement().getId()+1, input); // create an temp airport that will later be assigned as connection
                         
                         nAp.getConnections().put(new Airport(1l, "Frankfurt"), new Connection(new Duration(1338))); // TEST!!
                         nAp.getConnections().put(new Airport(1l, "Frankfurt"), new Connection(new Duration(1338)));
                         
                         lm.addElement(nAp); // add the String as given Airport to the JList Model
                     }
                 }
             break;
             
             case "lRem":
                 lm.remove(this.locationJList.getSelectedIndex());
             break;
             
             case "rAdd":
                 // Show our self made selection box modal
                 this.airportSel = new EditorWindow_AirportSelector(controller, this);
                 this.airportSel.draw();
             break;
             
             case "approveAPselection":
                 //if()
                 int elem = this.lm.indexOf(locationJList.getSelectedValue());
                 Airport ap = this.lm.get(elem);
                 ap.getConnections().put(new Airport(2l, this.airportSel.getSelection().getName()), new Connection(new Duration(1339)));
                 this.airportSel.dispose();
             break;
             
             case "selectBoxChanged":
                 System.out.println("changed!");
             break;
         }
         int selection = this.locationJList.getSelectedIndex(); // repainting makes the form lose its selection so lets manually save and restore them
         this.draw(); // repaint
         this.locationJList.setSelectedIndex(selection);
     }
 
     public void valueChanged(ListSelectionEvent e) {     
         Object[] airportList = controller.getModel().getAirportList().values().toArray();
     /**
      * Triggered as soon as the list selection changes in any way
      */
         
         // first enable the action buttons
         this.rAdd.setEnabled(true);
         this.rRem.setEnabled(true);
         
         int elem = this.lm.indexOf(locationJList.getSelectedValue());
         Airport ap = this.lm.get(elem); // the object of type Airport that has been chosen from the list
 
         // Render Form
         connectionsContainer.removeAll();
         
        if(ap.getConnections().size() == 0)
            return;
         
         connectionsContainer.setLayout(new GridLayout(ap.getConnections().size(), 2));
 
         for (Map.Entry<Airport, Connection> entry : ap.getConnections().entrySet()) {
             JComboBox<Object> apSelect = new JComboBox<Object>(airportList);
             apSelect.setSelectedIndex(Arrays.binarySearch(airportList, entry.getKey()));
             apSelect.addActionListener(this);
             apSelect.setActionCommand("selectBoxChanged");
             connectionsContainer.add(apSelect);
             connectionsContainer.add(new JTextField(DateHelper.INSTANCE.durationToString(entry.getValue().getDuration())));
         }
 
         pack();
         this.repaint();
     }
 
 }
