 /**
  * Mainwindow
  * <p>
  * The main window
  * 
  * @author Serjoscha Bassauer
  */
 
 package de.bwv_aachen.dijkstra.gui;
 
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowFocusListener;
 import java.awt.event.WindowListener;
 import java.awt.event.WindowStateListener;
 import java.io.File;
 import java.util.Vector;
 import java.util.regex.Pattern;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 
 import de.bwv_aachen.dijkstra.controller.Controller;
 import de.bwv_aachen.dijkstra.helpers.JSONFileChooser;
 import de.bwv_aachen.dijkstra.model.Airport;
 import de.bwv_aachen.dijkstra.model.ListDataModel;
 
 @SuppressWarnings("serial")
 public class Mainwindow extends View implements ActionListener {
 
     private JComboBox<Object>     leftList;
     private JComboBox<Object>     rightList;
     boolean                       file_changed;
     private File                  connectionFile;
     private final JSONFileChooser chooser = new JSONFileChooser();
 
     public Mainwindow(Controller c) {
         super(c);
         connectionFile = controller.getDefaultConnectionFile();
         file_changed = true;
         
         
         //Every time when the Mainwindow gains the focus, redraw it
         this.addWindowFocusListener(new WindowFocusListener() {
             @Override
             public void windowGainedFocus(WindowEvent e) { 
                 draw();   
             }
 
             @Override
             public void windowLostFocus(WindowEvent e) {
             }
         });
     }
 
     @Override
     public void draw() {
         super.getContentPane().removeAll(); // making this function being able
                                             // to repaint the mainwindow
         super.setTitle("Dijkstra");
         super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         super.setResizable(false);
 
         super.setLayout(new GridLayout(3, 2, 2, 2));
 
         ((JComponent) getContentPane()).setBorder(BorderFactory
                 .createMatteBorder(4, 4, 4, 4, Color.LIGHT_GRAY));
 
         if (file_changed) {
             try {
                 Controller.INSTANCE.readFile(connectionFile);
             }
             catch (Exception e) {
                 JOptionPane.showMessageDialog(this,
                        "Die Datei konnte nicht ge�ffnet werden", "Fehler",
                         JOptionPane.ERROR_MESSAGE);
             }
 
             file_changed = false;
         }
 
         ListDataModel model = controller.getModel();
         Object[] locations = model.getAirportList().values().toArray();
 
         // Create the lists
         leftList  = new JComboBox<>(locations);
         rightList = new JComboBox<>(locations);
         rightList.setSelectedIndex(1); // just for a more professional
                                        // impression of the program -> mark
                                        // the second element in the second
                                        // list as active
 
         // Create a button for being able to submit the action
         JButton startAction = new JButton("Berechnen");
         startAction.setActionCommand("calc");
         startAction.addActionListener(this);
 
         // Add elements to the frame
         super.add(new JLabel("Start"));
         super.add(leftList);
         super.add(new JLabel("Ziel"));
         super.add(rightList);
         super.add(startAction);
         super.add(new JLabel());
 
         // Create a menu for various actions
         JMenuBar menuBar = new JMenuBar();
         super.setJMenuBar(menuBar);
 
         // Define Menu Cats
         JMenu fileMenu = new JMenu("Datei");
         menuBar.add(fileMenu);
         JMenu infoMenu = new JMenu("Info");
         menuBar.add(infoMenu);
 
         // Define Menu Items within the cats
        JMenuItem openFile = new JMenuItem("�ffnen...");
         openFile.addActionListener(this);
         openFile.setActionCommand("loadFile");
         fileMenu.add(openFile);
 
         JMenuItem editFile = new JMenuItem("Bearbeiten...");
         editFile.addActionListener(this);
         editFile.setActionCommand("editFile");
         fileMenu.add(editFile);
 
         JMenuItem saveFile = new JMenuItem("Speichern unter...");
         saveFile.addActionListener(this);
         saveFile.setActionCommand("saveFile");
         fileMenu.add(saveFile);
 
         // Info menu
         JMenuItem fileInfo = new JMenuItem(connectionFile.getAbsolutePath()
                 .toString());
         fileInfo.setEnabled(false);
         JMenuItem textLabelFileInfo = new JMenuItem(
                 "Momentan ge�ffnete Datei:");
         textLabelFileInfo.setEnabled(false);
         infoMenu.add(textLabelFileInfo);
         infoMenu.add(fileInfo);
 
         // Do the rest for displaying the window
         super.pack();
         super.setLocationRelativeTo(null); // center the frame
 
         // Show the window
         super.setVisible(true);
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         switch (e.getActionCommand()) {
         case "calc": {
             Airport from = (Airport) leftList.getSelectedItem();
             Airport to = (Airport) rightList.getSelectedItem();
 
             Vector<Vector<Object>> test = Controller.INSTANCE.getModel()
                     .getRoute(from, to);
 
             new ConnectionTableWindow(test).draw();
             break;
         }
 
         case "loadFile": {
             chooser.setDialogType(JFileChooser.OPEN_DIALOG);
             if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                 connectionFile = new File(chooser.getSelectedFile()
                         .getAbsolutePath()); // set the new file to parse before
                                              // redrawing the gui with new
                                              // data
                 file_changed = true;
 //                draw(); // repaint the gui!
             }
             break;
         }
 
         case "editFile": {
             EditorWindow ew = new EditorWindow(controller);            
             ew.draw();
             break;
         }
 
         case "saveFile": {
             File oldFile = connectionFile;
             chooser.setDialogType(JFileChooser.SAVE_DIALOG);
             if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
 
                 String path = chooser.getSelectedFile().getAbsolutePath();
 
                 // Add file ending if missing
                 if (!Pattern.matches(".*\\.json", path)) {
                     path = path.concat(".json");
                 }
 
                 connectionFile = new File(path);
 
                 try {
                     Controller.INSTANCE.writeFile(connectionFile);
                     file_changed = true;
                 }
                 catch (Exception ex) {
                     // TODO Show an error message depending on the thrown
                     // exception
                     connectionFile = oldFile;
                     JOptionPane.showMessageDialog(this,
                             "Die Datei konnte nicht gespeichert werden",
                             "Fehler", JOptionPane.ERROR_MESSAGE);
                 }
             }
             break;
         }
         }
 
     }
 
 }
