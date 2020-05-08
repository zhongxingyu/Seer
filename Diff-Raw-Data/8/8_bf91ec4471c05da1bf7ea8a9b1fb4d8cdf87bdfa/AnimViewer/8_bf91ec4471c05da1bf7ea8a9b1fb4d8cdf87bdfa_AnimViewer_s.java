 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package animviewer;
 
 import components.RoundButton;
 import plugins.AnimPlugin;
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.StandardCopyOption;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListModel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JSlider;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JToolBar;
 import javax.swing.JTree;
 import javax.swing.ListCellRenderer;
 import javax.swing.UIManager;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.MutableTreeNode;
 import javax.swing.tree.TreePath;
 import plugins.AnimPluginManager;
 import updater.Updater;
 
 /**
  *
  * @author kirrie
  */
 public class AnimViewer {
     
     private static final Logger LOG = Logger.getLogger(AnimViewer.class.getClass().getName());
     private static final String defName = "AnimViewer";    
     private static final String defW = "800", defH = "600";
     
     private static final String noPropErr = "Could not find property file with config --> default values used";
     private static final String noLookErr = "We cannot set %s look for the app";    
     private static final String infoRemove = "List of loaded plugins were resetted\n";
     private static final String uptoDate = "Your app is up-to date";
     private static final String newVersion = "New version is available. Do you want to download?";
     private static final String newVersionTitle = "New version available";
     private static final String noConnection = "Connection was interrupted";
     
     private static final String versionFormat = "%s - version %s by Surko";
     
     private static final String app_version = "app_version";
     
     public static Properties prop;
     public static AnimPluginManager plugManager;
     public static AnimPlugin activePlugin; 
     public static AnimPlugin selectedPlugin;
     public static String version;
     
     public static int animState = 0;
     public static final int START = 1;
     public static final int STOP = 0;
     public static final int PAUSE = 2;
     
     public static int fpsMin = 0,fpsMax = 30,fpsInit = 0;
     public static int w,h;
     public static JFrame animViewerFrame;
     public static JPanel panel1,panel2;
     public static JMenuBar animMenuBar;
     public static JScrollPane animScrollList;
     public static JTree animList;
     public static MutableTreeNode rootNode;
     public static JToolBar animToolbar;
     public static JPanel animPanel;
     public static JSlider animSlider;
     public static JScrollPane animScrollText;
     public static JTextArea animText;    
     
     public static JFrame animViewerUpdateFrame;
     public static JPopupMenu versionPopup;
     public static JTable versionList;
     
     static class WindowListener implements ComponentListener {
 
         @Override
         public void componentResized(ComponentEvent e) {                        
             updateLengths();
         }
 
         @Override
         public void componentMoved(ComponentEvent e) {           
         }
         @Override
         public void componentShown(ComponentEvent e) {            
             //updateLengths();
         }
         @Override
         public void componentHidden(ComponentEvent e) {            
         }
         
     }             
     
     public static void updateLengths() {
         if (animToolbar != null) {
             animToolbar.setPreferredSize(new Dimension(animViewerFrame.getContentPane().getWidth(),34));
             animToolbar.setSize(new Dimension(animViewerFrame.getContentPane().getWidth(),34));
         }
         if (animPanel != null) {
             animPanel.setPreferredSize(new Dimension(animViewerFrame.getContentPane().getWidth()-100,
                     animViewerFrame.getContentPane().getHeight()-170));            
                       
         }
         if (animScrollList != null) {
             animScrollList.setPreferredSize(new Dimension(100,
                     animViewerFrame.getContentPane().getHeight()-170)); 
             animScrollList.setSize(new Dimension(100,
                     animViewerFrame.getContentPane().getHeight()-170)); 
                         
         }
         if (animScrollText != null) {            
             animScrollText.setPreferredSize(new Dimension(animScrollText.getParent().getWidth(),100));     
             animScrollText.setSize(new Dimension(animScrollText.getParent().getWidth(),100));
         }
         
         if (animSlider != null) {            
             animSlider.setPreferredSize(new Dimension(animSlider.getParent().getWidth(), 30));                                
                                           
         }        
     }
     
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         
         prop = new Properties();
         try {       
             InputStream propIn;                        
             propIn = AnimViewer.class.getResourceAsStream("/properties/viewer.properties");             
             prop.load(propIn);                                    
         } catch(Exception e) {            
             LOG.log(Level.WARNING, noPropErr);
         }
         plugManager = new AnimPluginManager();
         
         w = Integer.parseInt(prop.getProperty("app_width", defW));
         h = Integer.parseInt(prop.getProperty("app_height", defH));
         version = prop.getProperty(app_version,"0");
         
         JFrame.setDefaultLookAndFeelDecorated(true);
         animViewerFrame = new JFrame(String.format(versionFormat,prop.getProperty("app_name", defName), version));        
         animViewerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         animViewerFrame.setPreferredSize(new Dimension(w, h));
         rootNode = new DefaultMutableTreeNode("Plugins");
                 
         initMenuBar(); 
         initToolbar();
         initList();
         initSlider();
         initGraphicsPane();
         initTextArea();
         
         panel1 = new JPanel(new BorderLayout());
         panel1.add(animToolbar, BorderLayout.PAGE_START);
         panel1.add(animScrollList, BorderLayout.LINE_START);
         panel1.add(animPanel, BorderLayout.LINE_END);
         panel2 = new JPanel(new BorderLayout());        
         panel2.add(animSlider, BorderLayout.PAGE_START);
         panel2.add(animScrollText, BorderLayout.PAGE_END);
         
         animViewerFrame.setJMenuBar(animMenuBar);                                                
         animViewerFrame.getContentPane().add(panel1, BorderLayout.PAGE_START);
         animViewerFrame.getContentPane().add(panel2,BorderLayout.PAGE_END);                    
                 
         animViewerFrame.pack();                                                      
         animViewerFrame.addComponentListener(new WindowListener());
         updateLengths();        
         
         animViewerFrame.setVisible(true);    
         System.out.println(animToolbar.getComponentAtIndex(1).getWidth());
                 
     }
     
     private static void initLookandFeel() {
         try {
             UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
         } catch (Exception e) {
             LOG.log(Level.WARNING,noLookErr);            
         }
     }
     
     private static void initMenuBar() {
         animMenuBar = new JMenuBar();
         
         // Vytvorene nove menu
         JMenu menu = new JMenu("File");        
         // Pridanie tlacidla na pridanie pluginov
         JMenuItem item = new JMenuItem("Add Plugin");
         item.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 JFileChooser chooser = new JFileChooser();                
                 FileNameExtensionFilter filter = new FileNameExtensionFilter("JAR files", "jar");
                 chooser.setFileFilter(filter);
                 int retValue = chooser.showOpenDialog(animViewerFrame);
                 
                 if (retValue == JFileChooser.APPROVE_OPTION) {
                     DefaultTreeModel model = (DefaultTreeModel) animList.getModel();                    
                     model.insertNodeInto(new DefaultMutableTreeNode(plugManager.addPlugin(chooser.getSelectedFile())),rootNode,
                             model.getChildCount(rootNode));
                     model.reload();
                 }
             }
         });
         menu.add(item);
         // Pridanie tlacidla na vymazanie vsetkych pluginov
         item = new JMenuItem("Remove All");
         item.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 animList.setModel(new DefaultTreeModel(rootNode));
                 plugManager.removeAll();
                 animText.append(infoRemove);
             }
         });
         menu.add(item);
         // Pridanie separatoru
         menu.add(new JSeparator());
         //Pridanie tlacidla na ukoncenie
         item = new JMenuItem("Exit");
         item.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 System.exit(0);
             }
         });
         menu.add(item);
         
         // Pridanie celeho jedneho menu do menu baru
         animMenuBar.add(menu);
         
         // Created menu item help
         menu = new JMenu("Help");
         // Created menu item Check for updates
         item = new JMenuItem("Check for Updates");
         item.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {                
                 Updater updater = new Updater(prop);  
                 try {
                     if (updater.getLatestVersion().equals(version)) {
                         JOptionPane.showMessageDialog(animViewerFrame, uptoDate);
                     } else {
                         int answer = JOptionPane.showConfirmDialog(animViewerFrame, newVersion,newVersionTitle,JOptionPane.YES_NO_OPTION);
                         if (answer == -1) {
                             return;
                         } else {
                             animText.append("Downloading... \n");                            
                         }
                     }
                 } catch (Exception ex) {
                     JOptionPane.showMessageDialog(animViewerFrame, noConnection);
                     animText.append(noConnection);
                 }
             }
         });
         menu.add(item);
         
         // Version List
         item = new JMenuItem("List all Versions");
         item.addActionListener(new ActionListener() {            
             @Override
             public void actionPerformed(ActionEvent e) {
                 animViewerUpdateFrame = new JFrame("Version List");
                 animViewerUpdateFrame.setSize(new Dimension(400,300));
                 animViewerUpdateFrame.setMinimumSize(new Dimension(400,300));
                 animViewerUpdateFrame.setPreferredSize(new Dimension(400,300));
                 animViewerUpdateFrame.setResizable(false);
                                 
                 versionPopup = new JPopupMenu();                
                 
                 final Updater updater = new Updater(prop);  
                                                         
                 try {
                     // Get all releases from servers
                     Map<String,String> versionHistory = updater.getAllReleases();                    
                     // Initializing of versionList
                     versionList = new JTable();   
                     
                     // Initialization of table model with not editable cells
                     DefaultTableModel versionModel = new DefaultTableModel() {
                         @Override
                         public boolean isCellEditable(int row, int column) {
                             return false;                            
                         }
                     };                     
                     versionModel.setColumnIdentifiers(new String[] {"version","history"});                                        
                     for (Entry<String,String> entry : versionHistory.entrySet()) {                        
                         versionModel.addRow(new String[] {entry.getKey(), entry.getValue()});
                     }                                                                    
                     
                     // Set model for version list
                     versionList.setModel(versionModel);
                     versionList.addMouseListener(new MouseAdapter() {
                         @Override
                         public void mouseClicked(MouseEvent e) {
                             if (e.getButton() == 3) {                                                            
                                 versionPopup.show(e.getComponent(), e.getX(), e.getY());
                             }
                         }                        
                     });
                     
                     // Set download menu item for downloading new versions
                     JMenuItem menuItem = new JMenuItem("Download");
                     menuItem.addActionListener(new ActionListener() {
                         @Override
                         public void actionPerformed(ActionEvent e) {
                             if (versionList.getSelectedRow() >=0) {
                                 animText.append(String.format("Downloading AnimViewer version %s\n", versionList.getValueAt(versionList.getSelectedRow(), 0)));
                                 if (updater.download((String)versionList.getValueAt(versionList.getSelectedRow(), 0))) {
                                     animText.append("Downloading was succesful\n");
                                     int ret = JOptionPane.showConfirmDialog(animViewerFrame, "Do you want to run it right now?", "Run new Version", JOptionPane.OK_CANCEL_OPTION);                                     
                                     if (ret == 2) {                                       
                                     } else {
                                         try {                                                                                        
                                             Runtime.getRuntime().exec("cmd /c start update.bat");
                                             System.exit(0);
                                         } catch (IOException ex) {
                                             LOG.log(Level.SEVERE, null, ex);
                                         }
                                     }
                                 } else {
                                     animText.append("Downloading was not succesful\n");
                                 }                             
                             }
                         }
                     });
                     versionPopup.add(menuItem);
                     versionPopup.add(new JSeparator());
                     // Set cancel button for doing nothing
                     menuItem = new JMenuItem("Cancel");
                     versionPopup.add(menuItem);
                     
                     // Set JScrollPane with JTable
                     JScrollPane versionScroll = new JScrollPane(versionList,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);                    
                     versionScroll.setPreferredSize(new Dimension(400,300));
                     versionScroll.setSize(versionScroll.getPreferredSize());                    
                     versionScroll.setBorder(BorderFactory.createEmptyBorder(5, 5, 15, 15));
                     versionScroll.setColumnHeaderView(new JLabel("Versions"));
                     // Add JScrollPane with components to Frame
                     animViewerUpdateFrame.getContentPane().add(versionScroll);   
                     animViewerUpdateFrame.pack();
                     animViewerUpdateFrame.setVisible(true); 
                 } catch (Exception ex) {
                     JOptionPane.showMessageDialog(animViewerFrame, noConnection);
                     animText.append(noConnection);                    
                 }                                                   
             }
         });
         menu.add(item);
         
         // Pridanie help okna do menu baru
         animMenuBar.add(menu);
     }
 
     //<editor-fold defaultstate="collapsed" desc=" Inicializacie ">
     private static void initToolbar() {
         try {
             animToolbar = new JToolBar();              
             RoundButton btn;
             // Play button
             URL url = AnimViewer.class.getResource("/resources/play.png");                
             Image icon = new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);
             icon.getGraphics().drawImage(ImageIO.read(url).getScaledInstance(32, 32, Image.SCALE_DEFAULT),0,0,null);                        
             btn = new RoundButton(icon);   
             btn.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     switch (animState) {
                         case PAUSE :
                             animText.append(" Animation resumes \n");
                             animState = START;  
                             break;
                         case STOP :                             
                             // Testovanie na oznacenie animacie
                             if (selectedPlugin != null && activePlugin == null) {                                                                
                                 animState = START;
                                 activePlugin = selectedPlugin;                                     
                                 Thread t = new Thread(activePlugin);
                                 t.start();
                                 animText.append(" Animation started  \n");
                                 return;
                             }                           
                             if (selectedPlugin == null) {
                                 animText.append(" There is no selected animation to run \n");
                             }
                             break;
                         case START :
                             animText.append(" Animation is already running. To change it please select another one \n");
                             break;
                     }                                      
                 }
             });
             animToolbar.add(btn);
             // Pause Button
             url = AnimViewer.class.getResource("/resources/pause.png");
             icon = new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);
             icon.getGraphics().drawImage(ImageIO.read(url).getScaledInstance(32, 32, Image.SCALE_DEFAULT),0,0,null);                        
             btn = new RoundButton(icon);     
             btn.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     if (animState == START) {
                         animText.append(" Animation was paused \n");
                         animState = PAUSE;
                     } else {
                         animText.append(" Animation has to run to pause it \n");
                     }  
                 }
             });
             animToolbar.add(btn);
             // Stop button
             url = AnimViewer.class.getResource("/resources/stop.png");
             icon = new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);
             icon.getGraphics().drawImage(ImageIO.read(url).getScaledInstance(32, 32, Image.SCALE_DEFAULT),0,0,null);                        
             btn = new RoundButton(icon); 
             btn.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     if (animState != STOP) {
                         animText.append(" Animation is halting \n");  
                         animState = STOP;
                         if (activePlugin != null) {
                             activePlugin.exit();
                         }
                         activePlugin = null;
                         animText.append(" Animation was stopped \n");                        
                     } else {
                         animText.append(" Animation has to run to stop it\n");
                     }                
                 }
             });
             animToolbar.add(btn);
         } catch (IOException ex) {
             Logger.getLogger(AnimViewer.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     private static void initSlider() {
         animSlider = new JSlider(JSlider.HORIZONTAL,fpsMin,fpsMax, fpsInit);                
         animSlider.setMinorTickSpacing(1);
         animSlider.setPaintTicks(true);                        
     }
     
     private static void initList() {
         animList = new JTree();        
         animList.addMouseListener(new MouseAdapter() {
 
             @Override
             public void mouseClicked(MouseEvent e) {
                 TreePath tp = animList.getPathForLocation(e.getX(), e.getY());
                 if (tp == null) {
                     animList.clearSelection();
                     animPanel.removeAll();
                     animPanel.updateUI();
                     return;
                 }
                 if (tp.getLastPathComponent() != rootNode) {
                     selectedPlugin = (AnimPlugin) ((DefaultMutableTreeNode) tp.getLastPathComponent()).getUserObject();
                     selectedPlugin.setPanel(animPanel);
                     animPanel.updateUI();
                 }
             }
             
         });
         DefaultTreeModel animModel = new DefaultTreeModel(rootNode);        
         animList.setModel(animModel);        
         animScrollList = new JScrollPane(animList,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
     }
     
     private static void initGraphicsPane() {
         animPanel = new JPanel();                 
     }
     
     private static void initTextArea() {
         animText = new JTextArea();              
         animText.setEditable(false);        
         animScrollText = new JScrollPane(animText,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);                                
     }
     // </editor-fold>
 }
