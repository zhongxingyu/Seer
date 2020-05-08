 package com.vylgin.midiwow;
 
 import java.awt.AWTException;
 import java.awt.Robot;
 import java.awt.Toolkit;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.sound.midi.MidiDevice;
 import javax.sound.midi.MidiMessage;
 import javax.sound.midi.MidiSystem;
 import javax.sound.midi.MidiUnavailableException;
 import javax.sound.midi.Receiver;
 import javax.sound.midi.ShortMessage;
 import javax.sound.midi.Transmitter;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListModel;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The main frame at which there are widgets
  * @author vylgin
  */
 public class MainWindow extends JFrame {
     private static final Pattern patternGameName = Pattern.compile("[a-zA-Z\\d\\s]+");
     private static final String gameKeysDirName = GameKeys.getGameKeysDirName();
     private static final String gameKeysFileExtension = GameKeys.getGameKeysFileExtension();
     private static final String dirSeparator = System.getProperty("file.separator");
     private static final String propertiesDirPath = "." + dirSeparator + gameKeysDirName;
     private static final File dirProperties = new File(propertiesDirPath);
     
     private static final String readyStatusBarText = "Ready.";
     private static final String createGameKeysText = "Create new Game Keys profile.";
     private static final String saveGameKeysText = "Save this Game Keys profile.";
     private static final String saveAsGameKeysText = "Save As this Game Keys profile.";
     private static final String deleteGameKeysText = "Delete this Game Keys profile.";
     
     private static Logger log = LoggerFactory.getLogger(MainWindow.class.getName());
     
     private final DefaultListModel listModel = new DefaultListModel();
     private DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<String>();
     
     private static enum MidiKeyEvent {
         KEY_PRESS_EVENT,
         KEY_RELEASE_EVENT
     }
         
     /**
      * 
      * @return this
      */
     public MainWindow getFrame() {
         return this;
     }
     
     /**
      * Creates new form MainWindow
      */
     public MainWindow() {
         log.info("Creating Main Window.");
        initComponents();
        
         if (!dirProperties.exists()) {
             createDefaultGameKeys();
         }
        
         selectGameKeysComboBox.setModel(comboBoxModel);
         initializeGameKeysComboBox();
         
         setStatusBarText(readyStatusBarText);
         log.info("Main Window created.");
     }
     
     /**
      * Set vector of midi devices to devices combo box.
      * @param midiDevices vector of midi devices.
      */
     public void setMidiDeviceNames(Vector<MidiDevice.Info> midiDevices) {
         midiDevicesComboBox.setModel(new DefaultComboBoxModel<MidiDevice.Info>(midiDevices));
         midiMessagesList.setModel(listModel);
         log.debug("Devices \"{}\" insert in devices combo box.", midiDevices.toString());
     }
     
     public void setStatusBarText(String text) {
         statusLabel.setText(text);
     }
     
     public String getReadyStatusBarText() {
         return readyStatusBarText;
     }
     
     private void createDefaultGameKeys() {
         log.info("Creating default Game Keys.");
         dirProperties.mkdir();
         String defaultGameKeysName = "Default Game Keys";
         GameKeys gameKeys = GameKeys.getInstance();
         gameKeys.createEmptyKeys(defaultGameKeysName);
         if (gameKeys.saveKeys(defaultGameKeysName)) {
             String message = "Default Game Keys created.";
             setStatusBarText(message);
             log.info(message);
         } else {
             String message = "Default Game Keys don't created.";
             setStatusBarText(message);
             log.error(message);
         }
     }
     
     private void initializeGameKeysComboBox() {
         log.info("Initializing Game Keys combo box.");
         for (String file : dirProperties.list()) {
             int i = file.lastIndexOf('.');
             String gameName = file.substring(0, i);
             String fileExtension = file.substring(i, file.length());
             if (fileExtension.equals(this.gameKeysFileExtension)) {
                 selectGameKeysComboBox.addItem(gameName);
                 log.debug("Added \"{}\" Game Keys in combo box.", gameName);
             }
         }
         log.info("Game Keys initialized.");
     }
     
     private void loadSelectedGameKeys() {
         log.info("Loading selected Game Keys.");
         String selectedGameKeys = (String) selectGameKeysComboBox.getSelectedItem();
         GameKeys gameKeys = GameKeys.getInstance();
         gameKeys.loadKeys(selectedGameKeys);
         log.info("Loaded selected \"{}\" Game Keys.", selectedGameKeys);
     }
 
     private void setBacklightNotEmptyKeys() {
         log.debug("Backlight not empty keys begining.");
         VirtualMidiKeyboard vmk = (VirtualMidiKeyboard) pianoKeysPanel;
         vmk.clearBacklight();
         vmk.backlightNotEmptyKeys();
         log.debug("Backlight not empty keys ended.");
     }
     
     private void createGameKeys() {
         log.info("Creating Game Keys.");
         String gameName = JOptionPane.showInputDialog(this, "Input new Game Keys (English letters and numbers):");
         Matcher matcher = patternGameName.matcher(gameName);
         if (matcher.matches() && comboBoxModel.getIndexOf(gameName) == -1) {
             GameKeys gameKeys = GameKeys.getInstance();
             if (gameKeys.createEmptyKeys(gameName)) {
                 selectGameKeysComboBox.addItem(gameName);
                 selectGameKeysComboBox.setSelectedItem(gameName);
                 setBacklightNotEmptyKeys();
                 
                 String message = String.format("\"%s\" Game Keys created.", gameName);
                 setStatusBarText(message);
                 log.info(message);
             } else {
                 String message = String.format("\"%s\" Game Keys don't created.", gameName);
                 setStatusBarText(message);
                 log.info(message);
             }
         } else {
             String message = String.format( "\"%s\" Game Keys don't created. Use English letters and numbers in name.", gameName);
             JOptionPane.showMessageDialog(this, message);
             setStatusBarText(message);
             log.info(message);
         }
     }
     
     private void saveGameKeys() {
         log.info("Saving Game Keys.");
         if (selectGameKeysComboBox.getSelectedItem() != null) {
             GameKeys gameKeys = GameKeys.getInstance();
             if (gameKeys.saveKeys((String) selectGameKeysComboBox.getSelectedItem())) {
                 String message = String.format("\"%s\" Game Keys saved.", (String) selectGameKeysComboBox.getSelectedItem());
                 setStatusBarText(message);
                 log.info(message);
             } else {
                 String message = String.format("\"%s\" Game Keys don't saved.", (String) selectGameKeysComboBox.getSelectedItem());
                 setStatusBarText(message);
                 JOptionPane.showMessageDialog(this, message);
                 log.info(message);
             }
         }
     }
     
     private void saveAsGameKeys() {
         log.info("Saving Game Keys as.");
         String gameName = JOptionPane.showInputDialog(this, "Input new Game Keys (English letters and numbers):");
         Matcher matcher = patternGameName.matcher(gameName);
         String oldGameName = (String) selectGameKeysComboBox.getSelectedItem();
         if (matcher.matches() && comboBoxModel.getIndexOf(gameName) == -1) {
             GameKeys gameKeys = GameKeys.getInstance();
             if (gameKeys.saveKeys(gameName)) {
                 selectGameKeysComboBox.addItem(gameName);
                 selectGameKeysComboBox.setSelectedItem(gameName);
                 setBacklightNotEmptyKeys();
                 
                 String message = String.format("\"%s\" Game Keys saved from \"%s\" GameKeys.", gameName, oldGameName);
                 setStatusBarText(message);
                 log.info(message);
             } else {
                 String message = String.format("\"%s\" Game Keys don't saved from \"%s\" GameKeys.", gameName, oldGameName);
                 setStatusBarText(message);
                 log.info(message);
             }
         } else {
             String message = String.format(
                     "\"%s\" Game Keys don't saved from \"%s\" GameKeys. Use English letters and numbers in name.", 
                     gameName, oldGameName);
             JOptionPane.showMessageDialog(this, message);
             setStatusBarText(message);
             log.info(message);
         }
     }
     
     private void deleteGameKeys() {
         log.info("Deleting Game Keys.");
         GameKeys gameKeys = GameKeys.getInstance();
         String deletedGameName = (String) selectGameKeysComboBox.getSelectedItem();
         if (gameKeys.deleteKeys(deletedGameName)) {
             selectGameKeysComboBox.removeItemAt(selectGameKeysComboBox.getSelectedIndex());
             setBacklightNotEmptyKeys();  
             
             String message = String.format("\"%s\" Game Keys deleted.", deletedGameName);
             setStatusBarText(message);
             log.info(message);
         } else {
             String message = String.format("\"%s\" Game Keys don't deleted.", deletedGameName);
             JOptionPane.showMessageDialog(this, message);
             setStatusBarText(message);
             log.info(message);
         }
     }
     
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         midiDevicesLabel = new javax.swing.JLabel();
         midiDevicesComboBox = new javax.swing.JComboBox();
         selectMidiDeviceButton = new javax.swing.JButton();
         infoSelectedDeviceLabel = new javax.swing.JLabel();
         jScrollPane1 = new javax.swing.JScrollPane();
         midiMessagesList = new javax.swing.JList();
         jScrollPane2 = new javax.swing.JScrollPane();
         pianoKeysPanel = new VirtualMidiKeyboard();
         selectGameKeysComboBox = new javax.swing.JComboBox();
         saveGameKeysButton = new javax.swing.JButton();
         createGameKeysButton = new javax.swing.JButton();
         deleteGameKeysButton = new javax.swing.JButton();
         saveAsButton = new javax.swing.JButton();
         jSeparator2 = new javax.swing.JSeparator();
         jLabel1 = new javax.swing.JLabel();
         statusPanel = new javax.swing.JPanel();
         statusLabel = new javax.swing.JLabel();
         menuBar = new javax.swing.JMenuBar();
         fileMenu = new javax.swing.JMenu();
         exitMenuItem = new javax.swing.JMenuItem();
         gameKeysMenu = new javax.swing.JMenu();
         createMenuItem = new javax.swing.JMenuItem();
         saveMenuItem = new javax.swing.JMenuItem();
         saveAsMenuItem = new javax.swing.JMenuItem();
         deleteMenuItem = new javax.swing.JMenuItem();
         helpMenu = new javax.swing.JMenu();
         documentationMenuItem = new javax.swing.JMenuItem();
         jSeparator1 = new javax.swing.JPopupMenu.Separator();
         aboutMidiWoWMenuItem = new javax.swing.JMenuItem();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("MidiWoW");
 
         midiDevicesLabel.setText("Midi Devices:");
 
         midiDevicesComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
         midiDevicesComboBox.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseEntered(java.awt.event.MouseEvent evt) {
                 midiDevicesComboBoxMouseEntered(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 midiDevicesComboBoxMouseExited(evt);
             }
         });
 
         selectMidiDeviceButton.setText("Select Device");
         selectMidiDeviceButton.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseEntered(java.awt.event.MouseEvent evt) {
                 selectMidiDeviceButtonMouseEntered(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 selectMidiDeviceButtonMouseExited(evt);
             }
         });
         selectMidiDeviceButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 selectMidiDeviceButtonActionPerformed(evt);
             }
         });
 
         infoSelectedDeviceLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         infoSelectedDeviceLabel.setText("Midi device don't select");
 
         midiMessagesList.setModel(new javax.swing.AbstractListModel() {
             String[] strings = { "" };
             public int getSize() { return strings.length; }
             public Object getElementAt(int i) { return strings[i]; }
         }
     );
     jScrollPane1.setViewportView(midiMessagesList);
 
     jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
 
     pianoKeysPanel.setBackground(new java.awt.Color(255, 255, 255));
 
     javax.swing.GroupLayout pianoKeysPanelLayout = new javax.swing.GroupLayout(pianoKeysPanel);
     pianoKeysPanel.setLayout(pianoKeysPanelLayout);
     pianoKeysPanelLayout.setHorizontalGroup(
         pianoKeysPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGap(0, 790, Short.MAX_VALUE)
     );
     pianoKeysPanelLayout.setVerticalGroup(
         pianoKeysPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGap(0, 121, Short.MAX_VALUE)
     );
 
     jScrollPane2.setViewportView(pianoKeysPanel);
 
     selectGameKeysComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
     selectGameKeysComboBox.addMouseListener(new java.awt.event.MouseAdapter() {
         public void mouseEntered(java.awt.event.MouseEvent evt) {
             selectGameKeysComboBoxMouseEntered(evt);
         }
         public void mouseExited(java.awt.event.MouseEvent evt) {
             selectGameKeysComboBoxMouseExited(evt);
         }
     });
     selectGameKeysComboBox.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             selectGameKeysComboBoxActionPerformed(evt);
         }
     });
 
     saveGameKeysButton.setText("Save");
     saveGameKeysButton.addMouseListener(new java.awt.event.MouseAdapter() {
         public void mouseEntered(java.awt.event.MouseEvent evt) {
             saveGameKeysButtonMouseEntered(evt);
         }
         public void mouseExited(java.awt.event.MouseEvent evt) {
             saveGameKeysButtonMouseExited(evt);
         }
     });
     saveGameKeysButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             saveGameKeysButtonActionPerformed(evt);
         }
     });
 
     createGameKeysButton.setText("Create");
     createGameKeysButton.addMouseListener(new java.awt.event.MouseAdapter() {
         public void mouseEntered(java.awt.event.MouseEvent evt) {
             createGameKeysButtonMouseEntered(evt);
         }
         public void mouseExited(java.awt.event.MouseEvent evt) {
             createGameKeysButtonMouseExited(evt);
         }
     });
     createGameKeysButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             createGameKeysButtonActionPerformed(evt);
         }
     });
 
     deleteGameKeysButton.setText("Delete");
     deleteGameKeysButton.addMouseListener(new java.awt.event.MouseAdapter() {
         public void mouseEntered(java.awt.event.MouseEvent evt) {
             deleteGameKeysButtonMouseEntered(evt);
         }
         public void mouseExited(java.awt.event.MouseEvent evt) {
             deleteGameKeysButtonMouseExited(evt);
         }
     });
     deleteGameKeysButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             deleteGameKeysButtonActionPerformed(evt);
         }
     });
 
     saveAsButton.setText("Save As...");
     saveAsButton.addMouseListener(new java.awt.event.MouseAdapter() {
         public void mouseEntered(java.awt.event.MouseEvent evt) {
             saveAsButtonMouseEntered(evt);
         }
         public void mouseExited(java.awt.event.MouseEvent evt) {
             saveAsButtonMouseExited(evt);
         }
     });
     saveAsButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             saveAsButtonActionPerformed(evt);
         }
     });
 
     jLabel1.setText("Game Keys:");
 
     statusPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
 
     javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
     statusPanel.setLayout(statusPanelLayout);
     statusPanelLayout.setHorizontalGroup(
         statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(statusPanelLayout.createSequentialGroup()
             .addContainerGap()
             .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
     );
     statusPanelLayout.setVerticalGroup(
         statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(statusLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 12, Short.MAX_VALUE)
     );
 
     fileMenu.setText("File");
     fileMenu.addMouseListener(new java.awt.event.MouseAdapter() {
         public void mouseEntered(java.awt.event.MouseEvent evt) {
             fileMenuMouseEntered(evt);
         }
         public void mouseExited(java.awt.event.MouseEvent evt) {
             fileMenuMouseExited(evt);
         }
     });
 
     exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
     exitMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/cross.png"))); // NOI18N
     exitMenuItem.setText("Exit");
     exitMenuItem.addMouseListener(new java.awt.event.MouseAdapter() {
         public void mouseEntered(java.awt.event.MouseEvent evt) {
             exitMenuItemMouseEntered(evt);
         }
         public void mouseExited(java.awt.event.MouseEvent evt) {
             exitMenuItemMouseExited(evt);
         }
     });
     exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             exitMenuItemActionPerformed(evt);
         }
     });
     fileMenu.add(exitMenuItem);
 
     menuBar.add(fileMenu);
 
     gameKeysMenu.setText("Game Keys");
     gameKeysMenu.addMouseListener(new java.awt.event.MouseAdapter() {
         public void mouseEntered(java.awt.event.MouseEvent evt) {
             gameKeysMenuMouseEntered(evt);
         }
         public void mouseExited(java.awt.event.MouseEvent evt) {
             gameKeysMenuMouseExited(evt);
         }
     });
 
     createMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
     createMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/add.png"))); // NOI18N
     createMenuItem.setText("Create");
     createMenuItem.addMouseListener(new java.awt.event.MouseAdapter() {
         public void mouseEntered(java.awt.event.MouseEvent evt) {
             createMenuItemMouseEntered(evt);
         }
         public void mouseExited(java.awt.event.MouseEvent evt) {
             createMenuItemMouseExited(evt);
         }
     });
     createMenuItem.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             createMenuItemActionPerformed(evt);
         }
     });
     gameKeysMenu.add(createMenuItem);
 
     saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
     saveMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/page_save.png"))); // NOI18N
     saveMenuItem.setText("Save");
     saveMenuItem.addMouseListener(new java.awt.event.MouseAdapter() {
         public void mouseEntered(java.awt.event.MouseEvent evt) {
             saveMenuItemMouseEntered(evt);
         }
         public void mouseExited(java.awt.event.MouseEvent evt) {
             saveMenuItemMouseExited(evt);
         }
     });
     saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             saveMenuItemActionPerformed(evt);
         }
     });
     gameKeysMenu.add(saveMenuItem);
 
     saveAsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
     saveAsMenuItem.setText("Save As...");
     saveAsMenuItem.addMouseListener(new java.awt.event.MouseAdapter() {
         public void mouseEntered(java.awt.event.MouseEvent evt) {
             saveAsMenuItemMouseEntered(evt);
         }
         public void mouseExited(java.awt.event.MouseEvent evt) {
             saveAsMenuItemMouseExited(evt);
         }
     });
     saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             saveAsMenuItemActionPerformed(evt);
         }
     });
     gameKeysMenu.add(saveAsMenuItem);
 
     deleteMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
     deleteMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/delete.png"))); // NOI18N
     deleteMenuItem.setText("Delete");
     deleteMenuItem.addMouseListener(new java.awt.event.MouseAdapter() {
         public void mouseEntered(java.awt.event.MouseEvent evt) {
             deleteMenuItemMouseEntered(evt);
         }
         public void mouseExited(java.awt.event.MouseEvent evt) {
             deleteMenuItemMouseExited(evt);
         }
     });
     deleteMenuItem.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             deleteMenuItemActionPerformed(evt);
         }
     });
     gameKeysMenu.add(deleteMenuItem);
 
     menuBar.add(gameKeysMenu);
 
     helpMenu.setText("Help");
     helpMenu.addMouseListener(new java.awt.event.MouseAdapter() {
         public void mouseEntered(java.awt.event.MouseEvent evt) {
             helpMenuMouseEntered(evt);
         }
         public void mouseExited(java.awt.event.MouseEvent evt) {
             helpMenuMouseExited(evt);
         }
     });
 
     documentationMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
     documentationMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help.png"))); // NOI18N
     documentationMenuItem.setText("Documentation");
     documentationMenuItem.addMouseListener(new java.awt.event.MouseAdapter() {
         public void mouseEntered(java.awt.event.MouseEvent evt) {
             documentationMenuItemMouseEntered(evt);
         }
         public void mouseExited(java.awt.event.MouseEvent evt) {
             documentationMenuItemMouseExited(evt);
         }
     });
     helpMenu.add(documentationMenuItem);
     helpMenu.add(jSeparator1);
 
     aboutMidiWoWMenuItem.setText("About MidiWoW");
     aboutMidiWoWMenuItem.addMouseListener(new java.awt.event.MouseAdapter() {
         public void mouseEntered(java.awt.event.MouseEvent evt) {
             aboutMidiWoWMenuItemMouseEntered(evt);
         }
         public void mouseExited(java.awt.event.MouseEvent evt) {
             aboutMidiWoWMenuItemMouseExited(evt);
         }
     });
     helpMenu.add(aboutMidiWoWMenuItem);
 
     menuBar.add(helpMenu);
 
     setJMenuBar(menuBar);
 
     javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
     getContentPane().setLayout(layout);
     layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(infoSelectedDeviceLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
             .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                 .addComponent(statusPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGroup(layout.createSequentialGroup()
                     .addContainerGap()
                     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addGroup(layout.createSequentialGroup()
                             .addComponent(midiDevicesLabel)
                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                             .addComponent(midiDevicesComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                             .addComponent(selectMidiDeviceButton))
                         .addGroup(layout.createSequentialGroup()
                             .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                             .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                 .addGroup(layout.createSequentialGroup()
                                     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                         .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.LEADING)
                                         .addGroup(layout.createSequentialGroup()
                                             .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                             .addComponent(selectGameKeysComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE)
                                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                             .addComponent(createGameKeysButton)
                                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                             .addComponent(saveGameKeysButton)
                                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                             .addComponent(saveAsButton)
                                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                             .addComponent(deleteGameKeysButton)))
                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 112, Short.MAX_VALUE)))))))
             .addContainerGap())
     );
     layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
             .addContainerGap()
             .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(midiDevicesLabel)
                 .addComponent(midiDevicesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(selectMidiDeviceButton))
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addComponent(infoSelectedDeviceLabel)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
             .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                 .addGroup(layout.createSequentialGroup()
                     .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGap(2, 2, 2)
                     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(selectGameKeysComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(createGameKeysButton)
                         .addComponent(deleteGameKeysButton)
                         .addComponent(saveGameKeysButton)
                         .addComponent(saveAsButton)
                         .addComponent(jLabel1))
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                     .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addComponent(jScrollPane1))
             .addGap(18, 18, 18)
             .addComponent(statusPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
     );
 
     pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void selectMidiDeviceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectMidiDeviceButtonActionPerformed
         try {
             log.info("Opening midi device");
             
             MidiDevice device = MidiSystem.getMidiDevice((MidiDevice.Info) midiDevicesComboBox.getSelectedItem());
             Transmitter trans = device.getTransmitter();
             trans.setReceiver(new MidiInputReceiver());
 
             if (!device.isOpen()) {
                 device.open();
                 String message = String.format("Midi device \"%s\" was opened", device.getDeviceInfo());
                 infoSelectedDeviceLabel.setText(message);
                 setStatusBarText(message);
                 log.info(message);
             }
         } catch (MidiUnavailableException e1) {
             String message = String.format("Midi device \"%s\" was don't opened", midiDevicesComboBox.getSelectedItem());
             infoSelectedDeviceLabel.setText(message);
             log.info(message);
         }
     }//GEN-LAST:event_selectMidiDeviceButtonActionPerformed
 
     private void selectGameKeysComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectGameKeysComboBoxActionPerformed
         if (selectGameKeysComboBox.getSelectedItem() != null) {
             loadSelectedGameKeys();
             setBacklightNotEmptyKeys();
         }
     }//GEN-LAST:event_selectGameKeysComboBoxActionPerformed
 
     private void saveGameKeysButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveGameKeysButtonActionPerformed
         saveGameKeys();
     }//GEN-LAST:event_saveGameKeysButtonActionPerformed
 
     private void createGameKeysButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createGameKeysButtonActionPerformed
         createGameKeys();
     }//GEN-LAST:event_createGameKeysButtonActionPerformed
 
     private void deleteGameKeysButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteGameKeysButtonActionPerformed
         deleteGameKeys();
     }//GEN-LAST:event_deleteGameKeysButtonActionPerformed
 
     private void saveAsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsButtonActionPerformed
         saveAsGameKeys();
     }//GEN-LAST:event_saveAsButtonActionPerformed
 
     private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
         WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
         Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);    
         log.info("Exited program from main menu.");
     }//GEN-LAST:event_exitMenuItemActionPerformed
 
     private void createMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createMenuItemActionPerformed
         createGameKeys();
     }//GEN-LAST:event_createMenuItemActionPerformed
 
     private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuItemActionPerformed
         saveGameKeys();
     }//GEN-LAST:event_saveMenuItemActionPerformed
 
     private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsMenuItemActionPerformed
         saveAsGameKeys();
     }//GEN-LAST:event_saveAsMenuItemActionPerformed
 
     private void deleteMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteMenuItemActionPerformed
         deleteGameKeys();
     }//GEN-LAST:event_deleteMenuItemActionPerformed
 
     private void midiDevicesComboBoxMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_midiDevicesComboBoxMouseEntered
         setStatusBarText("Change midi device.");
     }//GEN-LAST:event_midiDevicesComboBoxMouseEntered
 
     private void midiDevicesComboBoxMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_midiDevicesComboBoxMouseExited
         setStatusBarText(readyStatusBarText);
     }//GEN-LAST:event_midiDevicesComboBoxMouseExited
 
     private void selectMidiDeviceButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_selectMidiDeviceButtonMouseEntered
         setStatusBarText("Select midi device.");
     }//GEN-LAST:event_selectMidiDeviceButtonMouseEntered
 
     private void selectMidiDeviceButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_selectMidiDeviceButtonMouseExited
         setStatusBarText(readyStatusBarText);
     }//GEN-LAST:event_selectMidiDeviceButtonMouseExited
 
     private void selectGameKeysComboBoxMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_selectGameKeysComboBoxMouseEntered
         setStatusBarText("Change Game Keys profile.");
     }//GEN-LAST:event_selectGameKeysComboBoxMouseEntered
 
     private void selectGameKeysComboBoxMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_selectGameKeysComboBoxMouseExited
         setStatusBarText(readyStatusBarText);
     }//GEN-LAST:event_selectGameKeysComboBoxMouseExited
 
     private void createGameKeysButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_createGameKeysButtonMouseEntered
         setStatusBarText(createGameKeysText);
     }//GEN-LAST:event_createGameKeysButtonMouseEntered
 
     private void createGameKeysButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_createGameKeysButtonMouseExited
         setStatusBarText(readyStatusBarText);
     }//GEN-LAST:event_createGameKeysButtonMouseExited
 
     private void saveGameKeysButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveGameKeysButtonMouseEntered
         setStatusBarText(saveGameKeysText);
     }//GEN-LAST:event_saveGameKeysButtonMouseEntered
 
     private void saveGameKeysButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveGameKeysButtonMouseExited
         setStatusBarText(readyStatusBarText);
     }//GEN-LAST:event_saveGameKeysButtonMouseExited
 
     private void saveAsButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveAsButtonMouseEntered
         setStatusBarText(saveAsGameKeysText);
     }//GEN-LAST:event_saveAsButtonMouseEntered
 
     private void saveAsButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveAsButtonMouseExited
         setStatusBarText(readyStatusBarText);
     }//GEN-LAST:event_saveAsButtonMouseExited
 
     private void deleteGameKeysButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteGameKeysButtonMouseEntered
         setStatusBarText(deleteGameKeysText);
     }//GEN-LAST:event_deleteGameKeysButtonMouseEntered
 
     private void deleteGameKeysButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteGameKeysButtonMouseExited
         setStatusBarText(readyStatusBarText);
     }//GEN-LAST:event_deleteGameKeysButtonMouseExited
 
     private void exitMenuItemMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exitMenuItemMouseEntered
         setStatusBarText("Close this program.");
     }//GEN-LAST:event_exitMenuItemMouseEntered
 
     private void exitMenuItemMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exitMenuItemMouseExited
         setStatusBarText(readyStatusBarText);
     }//GEN-LAST:event_exitMenuItemMouseExited
 
     private void createMenuItemMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_createMenuItemMouseEntered
         setStatusBarText(createGameKeysText);
     }//GEN-LAST:event_createMenuItemMouseEntered
 
     private void createMenuItemMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_createMenuItemMouseExited
         setStatusBarText(readyStatusBarText);
     }//GEN-LAST:event_createMenuItemMouseExited
 
     private void saveMenuItemMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveMenuItemMouseEntered
         setStatusBarText(saveGameKeysText);
     }//GEN-LAST:event_saveMenuItemMouseEntered
 
     private void saveMenuItemMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveMenuItemMouseExited
         setStatusBarText(readyStatusBarText);
     }//GEN-LAST:event_saveMenuItemMouseExited
 
     private void saveAsMenuItemMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveAsMenuItemMouseEntered
         setStatusBarText(saveAsGameKeysText);
     }//GEN-LAST:event_saveAsMenuItemMouseEntered
 
     private void saveAsMenuItemMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveAsMenuItemMouseExited
         setStatusBarText(readyStatusBarText);
     }//GEN-LAST:event_saveAsMenuItemMouseExited
 
     private void deleteMenuItemMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteMenuItemMouseEntered
         setStatusBarText(deleteGameKeysText);
     }//GEN-LAST:event_deleteMenuItemMouseEntered
 
     private void deleteMenuItemMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteMenuItemMouseExited
         setStatusBarText(readyStatusBarText);
     }//GEN-LAST:event_deleteMenuItemMouseExited
 
     private void documentationMenuItemMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_documentationMenuItemMouseEntered
         setStatusBarText("View documentation.");
     }//GEN-LAST:event_documentationMenuItemMouseEntered
 
     private void documentationMenuItemMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_documentationMenuItemMouseExited
         setStatusBarText(readyStatusBarText);
     }//GEN-LAST:event_documentationMenuItemMouseExited
 
     private void aboutMidiWoWMenuItemMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_aboutMidiWoWMenuItemMouseEntered
         setStatusBarText("View information about MidiWoW program.");
     }//GEN-LAST:event_aboutMidiWoWMenuItemMouseEntered
 
     private void aboutMidiWoWMenuItemMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_aboutMidiWoWMenuItemMouseExited
         setStatusBarText(readyStatusBarText);
     }//GEN-LAST:event_aboutMidiWoWMenuItemMouseExited
 
     private void fileMenuMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fileMenuMouseEntered
         setStatusBarText("Open File menu.");
     }//GEN-LAST:event_fileMenuMouseEntered
 
     private void fileMenuMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fileMenuMouseExited
         setStatusBarText(readyStatusBarText);
     }//GEN-LAST:event_fileMenuMouseExited
 
     private void gameKeysMenuMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_gameKeysMenuMouseEntered
         setStatusBarText("Open Game Keys menu.");
     }//GEN-LAST:event_gameKeysMenuMouseEntered
 
     private void gameKeysMenuMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_gameKeysMenuMouseExited
         setStatusBarText(readyStatusBarText);
     }//GEN-LAST:event_gameKeysMenuMouseExited
 
     private void helpMenuMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_helpMenuMouseEntered
         setStatusBarText("Open Help menu");
     }//GEN-LAST:event_helpMenuMouseEntered
 
     private void helpMenuMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_helpMenuMouseExited
         setStatusBarText(readyStatusBarText);
     }//GEN-LAST:event_helpMenuMouseExited
         
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JMenuItem aboutMidiWoWMenuItem;
     private javax.swing.JButton createGameKeysButton;
     private javax.swing.JMenuItem createMenuItem;
     private javax.swing.JButton deleteGameKeysButton;
     private javax.swing.JMenuItem deleteMenuItem;
     private javax.swing.JMenuItem documentationMenuItem;
     private javax.swing.JMenuItem exitMenuItem;
     private javax.swing.JMenu fileMenu;
     private javax.swing.JMenu gameKeysMenu;
     private javax.swing.JMenu helpMenu;
     private javax.swing.JLabel infoSelectedDeviceLabel;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JPopupMenu.Separator jSeparator1;
     private javax.swing.JSeparator jSeparator2;
     private javax.swing.JMenuBar menuBar;
     private javax.swing.JComboBox midiDevicesComboBox;
     private javax.swing.JLabel midiDevicesLabel;
     private javax.swing.JList midiMessagesList;
     private javax.swing.JPanel pianoKeysPanel;
     private javax.swing.JButton saveAsButton;
     private javax.swing.JMenuItem saveAsMenuItem;
     private javax.swing.JButton saveGameKeysButton;
     private javax.swing.JMenuItem saveMenuItem;
     private javax.swing.JComboBox selectGameKeysComboBox;
     private javax.swing.JButton selectMidiDeviceButton;
     private javax.swing.JLabel statusLabel;
     private javax.swing.JPanel statusPanel;
     // End of variables declaration//GEN-END:variables
 
     /**
     * The handler of the received messages
     * @author vylgin
     */
     public class MidiInputReceiver implements Receiver {      
         private static final int SYSTEM_MIDI_SIGNAL = 248;
         private static final int NOTE_ON_MIDI_SIGNAL = 144;
         private static final int NOTE_OFF_MIDI_SIGNAL = 128;
         
         @Override
         public void send(MidiMessage message, long timeStamp) {
             ShortMessage event = (ShortMessage) message;
 
             if (event.getStatus() != SYSTEM_MIDI_SIGNAL) {
                 log.debug("Sending midi message.");
                 if (listModel.getSize() >= 8) {
                     listModel.remove(0);
                 }
                 
                 VirtualMidiKeyboard vmk = (VirtualMidiKeyboard) pianoKeysPanel;
                 
                 if (event.getStatus() == NOTE_ON_MIDI_SIGNAL) {
                     vmk.backlightOnKey(event.getData1());
                 }
 
                 if (event.getStatus() == NOTE_OFF_MIDI_SIGNAL) {
                     vmk.backlightOffKey(event.getData1());
                 }
                 
                 bindKeys(event.getStatus(), event.getData1());
                 log.debug("Midi message sended");
             }
         }
 
         @Override
         public void close() {
         }
    
         private void showErrorKeyEmitInfo(ArrayList<Integer> keysEventList, MidiKeyEvent midiKeyEvent) {
             StringBuilder message = new StringBuilder("Keys error ");
             
             switch (midiKeyEvent) {
                 case KEY_PRESS_EVENT:
                     message.append("pressed");
                     break;
                 case KEY_RELEASE_EVENT:
                     message.append("released");
                     break;                    
             }
 
             message.append(": ");
 
             for (int key : keysEventList) {
                 if (key != GameKeys.getEmptyNote()) {
                     message.append(KeyEvent.getKeyText(key));
                     message.append(" ");
                 }
             }
             
             message.append("(keys names). Codes: ");
             message.append(keysEventList.toString());
             
             JOptionPane.showMessageDialog(MainWindow.this, message);
             setStatusBarText(message.toString());
             log.error(message.toString());
         }
 
         private void eventKeyEmit(ArrayList<Integer> keysEventList, MidiKeyEvent midiKeyEvent) {
             log.debug("Begin event key emit.");
             switch (midiKeyEvent) {
                 case KEY_PRESS_EVENT:
                     try {
                         Robot robot = new Robot();
                         for (int key : keysEventList) {
                             if (key != GameKeys.getEmptyNote()) {
                                 robot.keyPress(key);
                                 String message = String.format("Pressed key:", KeyEvent.getKeyText(key));
                                 listModel.addElement(message);
                                 log.debug(message);
                             }
                         }                        
                     } catch (AWTException e) {
                         showErrorKeyEmitInfo(keysEventList, midiKeyEvent);
                     }
                     break;
                 case KEY_RELEASE_EVENT:
                     try {
                         Robot robot = new Robot();
                         for (int key : keysEventList) {
                             if (key != GameKeys.getEmptyNote()) {
                                 robot.keyRelease(key); 
                                 String message = String.format("Released key: ", KeyEvent.getKeyText(key));
                                 listModel.addElement(message);
                                 log.debug(message);
                             }
                         }                        
                     } catch (AWTException e) {
                         showErrorKeyEmitInfo(keysEventList, midiKeyEvent);
                     }
                     break;
                 default:
                     midiMessagesList.repaint();
             }
             log.debug("Ended event key emit.");
         }
         
         private void bindKeys(int statusMidiKey, int numberMidiKeyEvent) {
             log.debug("Begin bind keys.");
             GameKeys gameKeys = GameKeys.getInstance();
             ArrayList<Integer> listKeys = gameKeys.getKeyboardKeys(numberMidiKeyEvent);
             switch (statusMidiKey) {
                 case NOTE_ON_MIDI_SIGNAL:
                     eventKeyEmit(listKeys, MidiKeyEvent.KEY_PRESS_EVENT);
                     break;
                 case NOTE_OFF_MIDI_SIGNAL:
                     eventKeyEmit(listKeys, MidiKeyEvent.KEY_RELEASE_EVENT);
                     break;
             }
             log.debug("Ended bind kyes.");
         }
     }
 }
