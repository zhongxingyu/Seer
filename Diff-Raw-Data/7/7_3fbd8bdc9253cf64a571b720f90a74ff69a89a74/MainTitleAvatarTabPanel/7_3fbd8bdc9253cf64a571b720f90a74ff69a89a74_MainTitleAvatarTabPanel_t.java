 /*
  * Created On: July 31, 2006, 5:27 PM
  */
 package com.thinkparity.ophelia.browser.application.browser.display.avatar;
 
 import java.awt.Dimension;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GridBagConstraints;
 import java.awt.Point;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 import javax.swing.Icon;
 import javax.swing.JLabel;
 
 import com.thinkparity.ophelia.browser.Constants.Icons.BrowserTitle;
 import com.thinkparity.ophelia.browser.application.browser.BrowserConstants.Fonts;
 import com.thinkparity.ophelia.browser.application.browser.component.LabelFactory;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.MainTitleAvatar.TabId;
 import com.thinkparity.ophelia.browser.platform.action.Data;
 import com.thinkparity.ophelia.browser.platform.plugin.Plugin;
 import com.thinkparity.ophelia.browser.platform.plugin.PluginExtension;
 import com.thinkparity.ophelia.browser.platform.plugin.PluginId;
 import com.thinkparity.ophelia.browser.platform.plugin.PluginRegistry;
 import com.thinkparity.ophelia.browser.platform.plugin.extension.TabListExtension;
 import com.thinkparity.ophelia.browser.platform.plugin.extension.TabPanelExtension;
 
 
 /**
  * <b>Title:</b>thinkParity Main Title Tabs<br>
  * <b>Description:</b>The tabs in the title portion of the UI are a simple
  * panel with two images controlling the contexts.<br>
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 public class MainTitleAvatarTabPanel extends MainTitleAvatarAbstractPanel {
     
     /** @see java.io.Serializable */
     private static final long serialVersionUID = 1;
 
     /** A resource bundle. */
     private static final ResourceBundle BUNDLE;
 
     static {
         BUNDLE = ResourceBundle.getBundle("localization/JPanel_Messages");
     }
 
     /** A list of all core tabs. */
     private final Map<TabId, Tab> allTabs;
 
     /** A list of all plugin tabs. */
     private final Map<PluginExtension, Tab> pluginTabs;
 
     /** The selected tab. */
     private Tab selectedTab;
     
     /** Creates new form BrowserTitleTabs */
     public MainTitleAvatarTabPanel() {
         super();
         this.allTabs = new HashMap<TabId, Tab>();
         this.pluginTabs = new HashMap<PluginExtension, Tab>();
         initComponents();
         addMoveListener(this);
         new Resizer(getBrowser(), this, Boolean.FALSE, Resizer.ResizeEdges.LEFT);
         new Resizer(getBrowser(), containerJLabel, Boolean.FALSE, Resizer.ResizeEdges.LEFT);
     }
 
     /**
      * @see javax.swing.JComponent#paintChildren(java.awt.Graphics)
      */
     @Override
     protected void paintChildren(final Graphics g) {
         super.paintChildren(g);
         final Graphics2D g2 = (Graphics2D) g.create();
         try {
             g2.setFont(Fonts.DefaultFontBold);
             for (final Tab tab : allTabs.values()) {
                 tab.drawText(g2, g2.getFontMetrics());
             }
             for (final Tab tab : pluginTabs.values()) {
                 tab.drawText(g2, g2.getFontMetrics());
             }
         } finally {
             g2.dispose();
         }
     }
 
     /**
      * Initialize tabs for the plugins.
      *
      */
     void initPluginTabs(final PluginRegistry pluginRegistry) {
         final Plugin archivePlugin = pluginRegistry.getPlugin(PluginId.ARCHIVE);
         if (null != archivePlugin) {
             final TabPanelExtension archiveTab =
                 pluginRegistry.getTabPanelExtension(PluginId.ARCHIVE, "ArchiveTab");
             final GridBagConstraints gridBagConstraints = new GridBagConstraints();
             gridBagConstraints.gridx = 1;
             gridBagConstraints.gridy = 1;
             gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
             gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 0);
             add(createTab(archivePlugin, archiveTab, Boolean.FALSE).jLabel, gridBagConstraints);
         }
     }
 
     /**
      * Select a tab.
      * 
      * @param tabExtension
      *            A tab extension.
      */
     void selectTab(final TabListExtension tabListExtension) {
         this.selectedTab = pluginTabs.get(tabListExtension);
         reloadDisplay();
     }
 
     /**
      * Select a tab.
      * 
      * @param tabExtension
      *            A tab extension.
      */
     void selectTab(final TabPanelExtension tabPanelExtension) {
         this.selectedTab = pluginTabs.get(tabPanelExtension);
         reloadDisplay();
     }
 
     /**
      * Select a tab.
      * 
      * @param tabId
      *            A tab.
      */
     void selectTab(final TabId tabId) {
         this.selectedTab = allTabs.get(tabId);
         reloadDisplay();
     }
 
     /**
      * Create a plugin extension tab (not leftmost).
      * 
      * @param tabExtension
      *            A tab extension.
      * @return A <code>Tab</code>.
      */
     private Tab createTab(final Plugin plugin, final TabPanelExtension tabPanelExtension, Boolean leftmost) {
         final Tab tab = new Tab(tabPanelExtension, leftmost);
         pluginTabs.put(tabPanelExtension, tab);
         return tab;
     }
     
     /**
      * Create a tab.
      * 
      * @param tabId
      *            An enumerated tab <code>TabId</code>.
      * @param leftmost
      *            Indicates if this is the leftmost tab.
      * @return A <code>Tab</code>.
      */
     private Tab createTab(final TabId tabId, final Boolean leftmost) {
         allTabs.put(tabId, new Tab(tabId, leftmost));
         return allTabs.get(tabId);
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         containerJLabel = createTab(TabId.CONTAINER, Boolean.TRUE).jLabel;
         archiveJLabel = createTab(TabId.ARCHIVE, Boolean.FALSE).jLabel;
         contactJLabel = createTab(TabId.CONTACT, Boolean.FALSE).jLabel;
 
         fillTopJLabel = new javax.swing.JLabel();
         fillRightJLabel = new javax.swing.JLabel();
 
         setLayout(new java.awt.GridBagLayout());
 
         setOpaque(false);
         containerJLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/BrowserTitle_LeftmostTabSelected.png")));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
         add(containerJLabel, gridBagConstraints);
 
         archiveJLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/BrowserTitle_Tab.png")));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 0);
         add(archiveJLabel, gridBagConstraints);
 
         contactJLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/BrowserTitle_Tab.png")));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 0);
         add(contactJLabel, gridBagConstraints);
 
         fillTopJLabel.setFont(Fonts.DefaultFontBold);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 6, 2, 0);
         add(fillTopJLabel, gridBagConstraints);
 
         fillRightJLabel.setFont(Fonts.DefaultFontBold);
         fillRightJLabel.setMaximumSize(new java.awt.Dimension(100, 18));
         fillRightJLabel.setMinimumSize(new java.awt.Dimension(100, 18));
         fillRightJLabel.setPreferredSize(new java.awt.Dimension(100, 18));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 4;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
         gridBagConstraints.weightx = 1.0;
         add(fillRightJLabel, gridBagConstraints);
 
     }// </editor-fold>//GEN-END:initComponents
     
     /**
      * Reload the display.
      */
     private void reloadDisplay() {
         for (final Tab tab : pluginTabs.values()) {
             tab.jLabel.setIcon(getTabIcon(selectedTab.isLeftmost(), Boolean.FALSE, Boolean.FALSE));
         }
         for (final Tab tab : allTabs.values()) {
             tab.jLabel.setIcon(getTabIcon(selectedTab.isLeftmost(), Boolean.FALSE, Boolean.FALSE));
         }
         selectedTab.jLabel.setIcon(getTabIcon(selectedTab.isLeftmost(), Boolean.TRUE, Boolean.FALSE));
     }
     
     /**
      * Get the appropriate tab icon.
      */
     private Icon getTabIcon(final Boolean leftmost, final Boolean selected, final Boolean rollover) {
         final Icon icon;
         
         if (leftmost && selected) {
             icon = BrowserTitle.LEFTMOST_TAB_SELECTED;  
         } else if (selected) {
             icon = BrowserTitle.TAB_SELECTED;
         } else if (rollover) {
             icon = BrowserTitle.TAB_ROLLOVER;
         } else {
             icon = BrowserTitle.TAB;  
         }
         
         return icon;
     }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JLabel archiveJLabel;
     private javax.swing.JLabel contactJLabel;
     private javax.swing.JLabel containerJLabel;
     private javax.swing.JLabel fillRightJLabel;
     private javax.swing.JLabel fillTopJLabel;
     // End of variables declaration//GEN-END:variables
 
     /** A tab definition. */
     private final class Tab {
 
         /** The tab label. */
         private final JLabel jLabel;
 
         /** The tab text. */
         private final String jLabelText;
         
         // Flag to indicate if this is the leftmost tab.
         private Boolean leftmost;
         
         /**
          * Create Tab.
          * 
          * @param tabExtension
          *            A plugin tab extension
          * @param leftmost
          *            Indicate if this tab is the leftmost tab or not.
          */
         private Tab(final TabPanelExtension tabPanelExtension, final Boolean leftmost) {
             super();
             this.leftmost = leftmost;
             this.jLabel = LabelFactory.create(getTabIcon(leftmost, Boolean.FALSE, Boolean.FALSE));
             this.jLabelText = tabPanelExtension.getText();
             this.jLabel.addMouseListener(new MouseAdapter() {
                 @Override
                 public void mousePressed(final MouseEvent e) {
                     final Data data = (Data) ((Data) mainTitleAvatar.getInput()).clone();
                     data.unset(MainTitleAvatar.DataKey.TAB_ID);
                     data.set(MainTitleAvatar.DataKey.TAB_PANEL_EXTENSION, tabPanelExtension);
                     mainTitleAvatar.setInput(data);
                 }
                 @Override
                 public void mouseEntered(final MouseEvent e) {
                     if (!MainTitleAvatarTabPanel.Tab.this.equals(selectedTab)) {
                         jLabel.setIcon(getTabIcon(leftmost, Boolean.FALSE, Boolean.TRUE));
                     }
                 }
                 @Override
                 public void mouseExited(final MouseEvent e) {
                     if (!MainTitleAvatarTabPanel.Tab.this.equals(selectedTab)) {
                         jLabel.setIcon(getTabIcon(leftmost, Boolean.FALSE, Boolean.FALSE));
                     }
                 }
             });
             final Dimension minimumSize = jLabel.getMinimumSize();
             minimumSize.height = 25;
             minimumSize.width = 77;
             this.jLabel.setMinimumSize(minimumSize);
         }
 
         /**
          * Create Tab.
          * 
          * @param tabId
          *            A tab id.
          * @param leftmost
          *            Indicate if this tab is the leftmost tab or not.
          */
         private Tab(final TabId tabId, final Boolean leftmost) {
             super();
             this.leftmost = leftmost;
             this.jLabel = LabelFactory.create(getTabIcon(leftmost, Boolean.FALSE, Boolean.FALSE));
             this.jLabelText = BUNDLE.getString(
                     new StringBuffer("MAIN_TITLE.MainTitleAvatar$TabId.")
                             .append(tabId).toString());
             this.jLabel.addMouseListener(new MouseAdapter() {
                 @Override
                 public void mousePressed(final MouseEvent e) {
                     getBrowser().selectTab(tabId); 
                 }
                 @Override
                 public void mouseEntered(final MouseEvent e) {
                     if (!MainTitleAvatarTabPanel.Tab.this.equals(selectedTab)) {
                         jLabel.setIcon(getTabIcon(leftmost, Boolean.FALSE, Boolean.TRUE));
                     }
                 }
                 @Override
                 public void mouseExited(final MouseEvent e) {
                     if (!MainTitleAvatarTabPanel.Tab.this.equals(selectedTab)) {
                         jLabel.setIcon(getTabIcon(leftmost, Boolean.FALSE, Boolean.FALSE));
                     }
                 }
             });
             final Dimension minimumSize = jLabel.getMinimumSize();
             minimumSize.height = 25;
             minimumSize.width = 77;
             this.jLabel.setMinimumSize(minimumSize);
         }       
 
         /**
          * @return the leftmost
          */
         public Boolean isLeftmost() {
             return leftmost;
         }
 
         /**
          * Draw the tab's text.
          * 
          * @param g2
          *            The <code>Graphics2D</code>.
          * @param fm
          *            The <code>FontMetrics</code>.
          */
         private void drawText(final Graphics2D g2, final FontMetrics fm) {
             final Point labelLocation = jLabel.getLocation();
             final Dimension labelSize = jLabel.getSize();
             final int textWidth = fm.stringWidth(jLabelText);
             final int textHeight = fm.getHeight();
             g2.drawString(jLabelText,
                     labelLocation.x + (labelSize.width - textWidth) / 2,
                     labelLocation.y + (labelSize.height - textHeight) / 2 + 13);
         }
     }
 }
