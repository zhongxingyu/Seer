 package edu.wustl.cab2b.client.ui.mainframe;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.net.URL;
 import java.text.DateFormat;
 import java.util.Date;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JEditorPane;
 import javax.swing.JLabel;
 
 import org.jdesktop.swingx.HorizontalLayout;
 import org.jdesktop.swingx.JXFrame;
 import org.jdesktop.swingx.JXPanel;
 
 import edu.wustl.cab2b.client.ui.MainSearchPanel;
 import edu.wustl.cab2b.client.ui.WindowUtilities;
 import edu.wustl.cab2b.client.ui.controls.Cab2bButton;
 import edu.wustl.cab2b.client.ui.controls.Cab2bHyperlink;
 import edu.wustl.cab2b.client.ui.controls.Cab2bLabel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.util.CommonUtils;
 import edu.wustl.common.util.logger.Logger;
 
 /**
  * This class creates a glassPane over the icons and adds tab-buttons to the
  * panel
  * 
  * @author hrishikesh_rajpathak
  * 
  */
 class GlobalNavigationGlassPane extends JComponent implements ActionListener {
     Cab2bLabel loggedInUserLabel;
 
     private ClassLoader loader = this.getClass().getClassLoader();
 
     private URL[] tabsImagesUnPressed = { loader.getResource("home_tab.gif"), loader.getResource("searchdata_tab.gif"), loader.getResource("experiment_tab.gif") };
 
     private URL[] tabsImagesPressed = { loader.getResource("home_MO_tab.gif"), loader.getResource("searchdata_MO_tab.gif"), loader.getResource("experiment_MO_tab.gif") };
 
     private JButton[] tabButtons = new Cab2bButton[3];
 
     public Color navigationButtonBgColorSelected = Color.WHITE;
 
     private JXPanel tabsPanel;
 
     public MainFrame mainFrame;
 
     public JXFrame frame;
 
     private JLabel middleLabel;
 
     private JLabel rightLabel;
     
     private JButton lastSelectedTab;
 
     private static final long serialVersionUID = 1L;
 
     public GlobalNavigationGlassPane(
             JLabel leftLabel,
             JLabel middleLabel,
             JLabel rightLabel,
             MainFrame mainFrame,
             JXFrame frame) {
         this.frame = mainFrame;
         this.mainFrame = mainFrame;
         this.middleLabel = middleLabel;
         this.rightLabel = rightLabel;
         initUI();
 
     }
 
     /**
      * Initialize the UI
      */
     private void initUI() {
         middleLabel.setLayout(new GridBagLayout());
         GridBagConstraints gbc = new GridBagConstraints();
 
         Cab2bPanel topMiddlePanel = new Cab2bPanel();
         topMiddlePanel.setPreferredSize(new Dimension(300, 40));
         topMiddlePanel.setMinimumSize(new Dimension(300, 40));
         topMiddlePanel.setMaximumSize(new Dimension(300, 40));
         topMiddlePanel.setOpaque(false);
 
         tabsPanel = new Cab2bPanel();
         tabsPanel.setPreferredSize(new Dimension(300, 30));
         tabsPanel.setMinimumSize(new Dimension(300, 30));
         tabsPanel.setMaximumSize(new Dimension(300, 30));
         tabsPanel.setOpaque(false);
         tabsPanel.setLayout(new HorizontalLayout(10));
 
         for (int i = 0; i < 3; i++) {
             ImageIcon icon = new ImageIcon(tabsImagesUnPressed[i]);
             tabButtons[i] = new Cab2bButton();
             tabButtons[i].setPreferredSize(new Dimension(85, 22));
             tabButtons[i].setIcon(icon);
             tabButtons[i].setBorder(null);
             tabButtons[i].addActionListener(this);
             tabsPanel.add(tabButtons[i]);
         }
         
         ImageIcon icon = new ImageIcon(tabsImagesPressed[0]);
         tabButtons[0].setIcon(icon);
         gbc.gridx = 3;
         gbc.gridy = 0;
         gbc.gridwidth = 3;
         gbc.gridheight = 1;
         gbc.fill = GridBagConstraints.VERTICAL;
         middleLabel.add(topMiddlePanel, gbc);
 
         gbc.gridx = 3;
         gbc.gridy = 1;
         gbc.gridwidth = 3;
         gbc.gridheight = 1;
         gbc.fill = GridBagConstraints.VERTICAL;
         middleLabel.add(tabsPanel, gbc);
 
         rightLabel.setLayout(new GridBagLayout());
 
         gbc.gridx = 0;
         gbc.gridy = 0;
         gbc.gridwidth = 2;
         gbc.gridheight = 2;
         gbc.weightx = 1.5;
         gbc.fill = GridBagConstraints.VERTICAL;
         rightLabel.add(new JLabel(""), gbc);
 
         Date date = new Date();
         loggedInUserLabel = new Cab2bLabel("Robert Lloyd");
         loggedInUserLabel.setFont(new Font("Arial", Font.BOLD, 12));
         Cab2bLabel dateLabel = new Cab2bLabel(DateFormat.getDateInstance(DateFormat.LONG).format(date).toString());
         dateLabel.setForeground(Color.WHITE);
         Cab2bHyperlink logOutHyperLink = new Cab2bHyperlink();
        logOutHyperLink.setClickedHyperlinkColor(Color.WHITE);
         logOutHyperLink.setUnclickedHyperlinkColor(Color.WHITE);
         logOutHyperLink.setText("Logout");
         logOutHyperLink.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 System.out.println("Clicked on logOut Link");
             }
         });
 
         Cab2bHyperlink mySettingHyperlInk = new Cab2bHyperlink();
        mySettingHyperlInk.setClickedHyperlinkColor(Color.WHITE);
         mySettingHyperlInk.setUnclickedHyperlinkColor(Color.WHITE);
         mySettingHyperlInk.setText("MySettings");
         mySettingHyperlInk.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 System.out.println("Clicked on My-Settings Link");
             }
         });
 
         loggedInUserLabel.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
         loggedInUserLabel.setForeground(Color.WHITE);
 
         JLabel line = new JLabel("|");
         line.setForeground(Color.WHITE);
         Cab2bPanel linkPanel = new Cab2bPanel();
 
         JLabel label = new JLabel(" ");
         linkPanel.add("br", label);
         linkPanel.add("tab ", loggedInUserLabel);
         linkPanel.add("br ", label);
         linkPanel.add("tab ", dateLabel);
         linkPanel.add("br ", logOutHyperLink);
         linkPanel.add(line);
         linkPanel.add(mySettingHyperlInk);
         linkPanel.setOpaque(false);
 
         gbc.gridx = 2;
         gbc.gridy = 0;
         gbc.gridwidth = 2;
         gbc.gridheight = 2;
         gbc.weightx = 0.1;
         gbc.fill = GridBagConstraints.VERTICAL;
         rightLabel.add(linkPanel, gbc);
 
         lastSelectedTab = tabButtons[0];
         this.repaint();
     }
 
     public void actionPerformed(ActionEvent e) {
         Logger.out.debug("Global Nagigation Panel Button");
         JButton button = (JButton) e.getSource();
         if (button.equals(tabButtons[0])) {
             tabButtons[0].setIcon(new ImageIcon(tabsImagesPressed[0]));
             tabButtons[1].setIcon(new ImageIcon(tabsImagesUnPressed[1]));
             tabButtons[2].setIcon(new ImageIcon(tabsImagesUnPressed[2]));
             if (this.frame instanceof MainFrame) {
                 MainFrame mainframePanel = (MainFrame) this.frame;
                 mainframePanel.setHomeWelcomePanel();
                 Logger.out.debug("Global Nagigation Panel Home Button");
             }            
         } else if (button.equals(tabButtons[1])) {
             tabButtons[0].setIcon(new ImageIcon(tabsImagesUnPressed[0]));
             tabButtons[1].setIcon(new ImageIcon(tabsImagesPressed[1]));
             tabButtons[2].setIcon(new ImageIcon(tabsImagesUnPressed[2]));
             GlobalNavigationPanel.mainSearchPanel = new MainSearchPanel();
             Dimension relDimension = CommonUtils.getRelativeDimension(MainFrame.mainframeScreenDimesion, 0.90f,
                                                                       0.85f);
             GlobalNavigationPanel.mainSearchPanel.setPreferredSize(relDimension);
             GlobalNavigationPanel.mainSearchPanel.setSize(relDimension);
 
             // Update the variable for latest screen dimension from the
             // toolkit, this is to handle the situations where
             // application is started and then screen resolution is
             // changed, but the variable stiil holds old resolution
             // size.
             MainFrame.mainframeScreenDimesion = Toolkit.getDefaultToolkit().getScreenSize();
             Dimension dimension = MainFrame.mainframeScreenDimesion;
             WindowUtilities.showInDialog(mainFrame, GlobalNavigationPanel.mainSearchPanel, "Search Data for Experiment",
                                          new Dimension((int) (dimension.width * 0.90),
                                                  (int) (dimension.height * 0.85)), true, true);
 
             MainSearchPanel.getDataList().clear();
             GlobalNavigationPanel.mainSearchPanel = null;
 
             // Set the Home tab as pressed and Search tab as unpressed
             if(lastSelectedTab != null && lastSelectedTab.equals(tabButtons[0])) {
                 lastSelectedTab.setIcon(new ImageIcon(tabsImagesPressed[0]));
             } else if(lastSelectedTab != null && lastSelectedTab.equals(tabButtons[2])) {
                 lastSelectedTab.setIcon(new ImageIcon(tabsImagesPressed[2]));
             }
             tabButtons[1].setIcon(new ImageIcon(tabsImagesUnPressed[1]));
         } else if (button.equals(tabButtons[2])) {
             tabButtons[0].setIcon(new ImageIcon(tabsImagesUnPressed[0]));
             tabButtons[1].setIcon(new ImageIcon(tabsImagesUnPressed[1]));
             tabButtons[2].setIcon(new ImageIcon(tabsImagesPressed[2]));
             MainFrame mainframePanel = (MainFrame) this.frame;
             mainframePanel.setOpenExperimentWelcomePanel();
         }
         lastSelectedTab = button;
 
         this.updateUI();
         this.repaint();
     }
 
 }
