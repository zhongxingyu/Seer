 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.github.swingomatic.server;
 
 import com.github.swingomatic.http.Server;
 import com.github.swingomatic.message.ApplicationCommand;
 import com.github.swingomatic.message.ComponentInfo;
 import com.sun.java.accessibility.util.EventQueueMonitor;
 import com.sun.java.accessibility.util.GUIInitializedListener;
 import com.sun.java.accessibility.util.SwingEventMonitor;
 import com.sun.java.accessibility.util.Translator;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Window;
 import java.awt.event.*;
 import java.io.File;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.util.ArrayList;
 import java.util.List;
 import javax.accessibility.Accessible;
 import javax.accessibility.AccessibleContext;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import javax.swing.JTree;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.tree.TreePath;
 import org.apache.log4j.Logger;
 
 public class Swingomatic implements
         ActionListener, KeyListener, GUIInitializedListener, DocumentListener {
 
     private JTree accessibleTree;
     private static int depth = 0;
     private static final int MAX_DEPTH = 100;
     private static Logger logger = Logger.getLogger(Swingomatic.class);
     private static int SERVER_COUNT = 2;
     private ServerSocket serverSocket;
     private List servers = new ArrayList(0);
 
     public static void main(String s[]) {
         Swingomatic app = new Swingomatic();
     }
 
     public Swingomatic() {
         initializeLogDirectory();
         initialize();
         logger.debug("Swingomatic server....");
     }
 
     private void initialize() {
         EventQueueMonitor.addGUIInitializedListener(this);
         createGUI();
         int listenPort = 8088;
         String listenAddress = "127.0.0.1";
         //TODO: get listen address and port port from parameter file
         logger.debug("Trying to bind to localhost on port " + Integer.toString(listenPort) + "...");
         try {
             //make a ServerSocket and bind it to given listenPort,
             serverSocket = new ServerSocket(listenPort, 1024);
         } catch (IOException ex) {
             logger.error(ex.getMessage());
         }
         
         for (int i = 0; i < SERVER_COUNT; i++) {
             servers.add(new ServerObserver(this, serverSocket, listenAddress));
         }
     }
 
     private static void initializeLogDirectory() {
         try {
             File logDir = new File("./logs");
             if (!logDir.exists()) {
                 logDir.mkdir();
             }
         } catch (Exception e) {
             logger.error(e.getMessage());
         }
     }
 
     public String getUsage() {
         String result = "Usage:\r\n"
                 + "list-components\r\n"
                 + "execute<XML>";
         return result;
     }
 
     public boolean executeCommand(ApplicationCommand ac) {
         boolean ok = ac.getComponents() != null;
         int i = 0;
         while (ok && (i < ac.getComponents().size())) {
             logger.debug("processing component " + i);
             if (ac.getComponents().get(i) instanceof ComponentInfo) {
                 ComponentInfo ci = (ComponentInfo) ac.getComponents().get(i);
                 ok = processComponent(ci);
                 if (!ok) {
                     ac.setResult("Not completed: " + i + " " + ci.getName());
                 }
             } else {
                 ac.setResult("Error: " + i);
                 ok = false;
             }
             i++;
         }
         return ok;
     }
 
     private boolean processComponent(ComponentInfo componentInfo) {
         logger.debug("processing component: " + componentInfo.toString());
         boolean ok = false;
         Window[] wins = EventQueueMonitor.getTopLevelWindows();
         ComponentObject root = new ComponentObject("Component Tree");
         //TODO: delay/retries
         if (componentInfo.getDelay() > 0) {
             try {
                 Thread.sleep(componentInfo.getDelay());
             } catch (InterruptedException ex) {
                 logger.debug(ex.getMessage());
             }
         }
         for (int i = 0; i < wins.length; i++) {
             ok = processComponentNodes(wins[i], root, componentInfo);
             if (ok) {
                 break;
             }
         }
         logger.debug("end processCompent - result: " + ok);
         return ok;
     }
 
     public void actionPerformed(ActionEvent e) {
         logger.debug("ActionEvent: " + e.getActionCommand());
     }
 
     public void keyTyped(KeyEvent e) {
         //logger.debug("Key typed KeyEvent: " + e.getKeyChar());
     }
 
     public void keyPressed(KeyEvent e) {
     }
 
     public void keyReleased(KeyEvent e) {
     }
 
     public void guiInitialized() {
         createGUI();
     }
 
     private void createGUI() {
         WindowListener l = new WindowAdapter() {
 
             public void windowClosing(WindowEvent e) {
                 System.exit(0);
             }
         };
         listComponents();
 
         accessibleTree = createAccessibleTree();
 
         SwingEventMonitor.addKeyListener(this);
         SwingEventMonitor.addDocumentListener(this);
         SwingEventMonitor.addActionListener(this);
 //        SwingEventMonitor.addPropertyChangeListener(new PropertyChangeListener() {
 //
 //            public void propertyChange(PropertyChangeEvent evt) {
 //                logger.debug("PropertyChange: " + evt.getPropertyName());
 //            }
 //        });
 //        SwingEventMonitor.addChangeListener(new ChangeListener() {
 //
 //            public void stateChanged(ChangeEvent e) {
 //                logger.debug("StateChanged: " + e.getSource().toString());
 //            }
 //        });
         SwingEventMonitor.addFocusListener(new FocusListener() {
 
             public void focusGained(FocusEvent e) {
                 if ("class com.ams.momentum.widgets.ComboBox".equals(e.getComponent().getClass().toString())) {
                     listComponents();
                 }
                 logger.debug("focusGained: " + e.getComponent().getClass().toString());
             }
 
             public void focusLost(FocusEvent e) {
             }
         });
         SwingEventMonitor.addContainerListener(new ContainerListener() {
 
             public void componentAdded(ContainerEvent e) {
                 if ("seeThroughGlassPane".equals(e.getChild().getName())
                         || "null.glassPane".equals(e.getChild().getName())) {
                     listComponents();
 
                 }
                 logger.debug("ComponentAdded: " + e.getChild().getName());
             }
 
             public void componentRemoved(ContainerEvent e) {
             }
         });
     }
 
     public ApplicationCommand listComponents() {
         ApplicationCommand ac = new ApplicationCommand();
         ac.setResult("OK");
         ac.setComponents(new ArrayList(0));
         Window[] wins = EventQueueMonitor.getTopLevelWindows();
         ComponentObject root = new ComponentObject("Component Tree");
         for (int i = 0; i < wins.length; i++) {
             addComponentNodes(wins[i], root, ac.getComponents());
         }
         return ac;
     }
 
     private List addComponentNodes(Component c, ComponentObject root, List result) {
         ComponentObject me;
         me = new ComponentObject(c);
         root.add(me);
         if (c instanceof Container) {
             int count = ((Container) c).getComponentCount();
             for (int i = 0; i < count; i++) {
                 Component comp = ((Container) c).getComponent(i);
                 ComponentInfo componentInfo = new ComponentInfo(comp.getName(), comp.getClass().toString());
                 result.add(componentInfo);
                 componentInfo.setxCoordinate(comp.getX());
                 componentInfo.setyCoordinate(comp.getY());
                 //TODO: add ToolTipText when available               
                 if (comp instanceof JTextField) {
                     JTextField jTextField = (JTextField) comp;
                     componentInfo.setToolTipText(jTextField.getToolTipText());
                 }
                 if (comp instanceof JLabel) {
                     JLabel jLabel = (JLabel) comp;
                     ((ComponentInfo) result.get(result.size() - 1)).setText(jLabel.getText());
                     if (jLabel.getLabelFor() != null) {
                         Component ofComponent = jLabel.getLabelFor();
                         result.add(new ComponentInfo(ofComponent.getName(),
                                 ofComponent.getClass().toString(),
                                 jLabel.getText(),
                                 ""));
                     }
 //                    if ("Document Title:".equals(jLabel.getText())) {
 //                        if (jLabel.getLabelFor() != null) {
 //                            ((JTextField) jLabel.getLabelFor()).setText("hello----");
 //                        }
 //                    }
                 }
                 addComponentNodes(((Container) c).getComponent(i), me, result);
             }
         }
         return result;
     }
 
     private boolean processComponentNodes(Component c, ComponentObject root,
             ComponentInfo componentInfo) {
         boolean result = false;
         ComponentObject me;
         me = new ComponentObject(c);
         root.add(me);
         if (c instanceof Container) {
             int count = ((Container) c).getComponentCount();
            for (int i = 0; i < count; i++) {
                 Component comp = ((Container) c).getComponent(i);
                 logger.debug("processCompoentNodes: " + comp.getClass().toString());
                 if ((componentInfo.getOfLabel() != null) && (comp instanceof JLabel)) {
                     logger.debug("ofLabel and JLabel found");
                     JLabel jLabel = (JLabel) comp;
                     if ((jLabel.getLabelFor() != null) && (jLabel.getText().equals(componentInfo.getOfLabel()))) {
                         logger.debug("ofLabel match found: " + componentInfo.getOfLabel());
                         logger.debug("label is for: " + jLabel.getLabelFor().getClass().toString());
                         if (jLabel.getLabelFor() instanceof JTextField) {
                             ((JTextField) jLabel.getLabelFor()).setText(componentInfo.getText());
                            return true;
                         }
                     }
                 }
 //                if (!comp.getClass().toString().equals(componentInfo.getClazz())) {
 //                }
                 result = processComponentNodes(((Container) c).getComponent(i), me, componentInfo);
             }
         }
         // if we reach this point, we have not found the component and therefore
         // cannot process 
         return result;
     }
 
     private JTree createAccessibleTree() {
         final JTree t;
         Window[] wins = EventQueueMonitor.getTopLevelWindows();
 
         AccessibleObject root = new AccessibleObject("Accessible Tree");
         Accessible accessible;
         for (int i = 0; i < wins.length; i++) {
             accessible = Translator.getAccessible(wins[i]);
             if (accessible != null) {
                 addAccessibleNodes(accessible, root);
             }
         }
 
         t = new JTree(root);
 
         MouseListener ml = new MouseAdapter() {
 
             public void mousePressed(MouseEvent e) {
                 if (e.getModifiers() == InputEvent.BUTTON3_MASK) {
                     TreePath selPath = t.getPathForLocation(e.getX(), e.getY());
                     if (selPath != null) {
                         Object o = selPath.getLastPathComponent();
                         if (o instanceof AccessibleObject) {
                             Accessible a = ((AccessibleObject) o).getAccessible();
                         }
                     }
                 }
             }
         };
 
         t.addMouseListener(ml);
         return t;
     }
 
     private void addAccessibleNodes(Accessible a, AccessibleObject root) {
 
         if (depth >= MAX_DEPTH) {
             return;
         }
         depth++;
 
         AccessibleObject me;
         me = new AccessibleObject(a);
         root.add(me);
 
         AccessibleContext ac = me.getAccessible().getAccessibleContext();
         if (ac != null) {
             Accessible ap = ac.getAccessibleParent();
             if (ap != root.getAccessible()) {
                 logger.debug("Accessible parent of " + a
                         + " should be " + root.getAccessible() + " but it is "
                         + ap);
             }
             int count = ac.getAccessibleChildrenCount();
             for (int i = 0; i < count; i++) {
                 addAccessibleNodes(ac.getAccessibleChild(i), me);
             }
         }
         depth--;
     }
 
     public void insertUpdate(DocumentEvent e) {
         //logger.debug("DocumentEvent insertUpdate" + e.toString());
     }
 
     public void removeUpdate(DocumentEvent e) {
         //logger.debug("DocumentEvent removeUpdate" + e.getDocument().toString());
     }
 
     public void changedUpdate(DocumentEvent e) {
         //logger.debug("DocumentEvent changedUpdate" + e.toString());
     }
 }
