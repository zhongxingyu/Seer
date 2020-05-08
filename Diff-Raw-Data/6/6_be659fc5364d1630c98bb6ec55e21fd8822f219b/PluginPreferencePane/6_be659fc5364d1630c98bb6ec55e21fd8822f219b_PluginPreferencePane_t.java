 package ch9k.plugins;
 
 import ch9k.core.I18n;
 import javax.swing.GroupLayout;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSpinner;
 import javax.swing.JButton;
 import javax.swing.JTextField;
 import javax.swing.JList;
 import javax.swing.JScrollPane;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SwingConstants;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import java.util.Set;
 
 /**
  * Preference pane for the plugins.
  */
 public class PluginPreferencePane
         extends JPanel implements ChangeListener {
     /**
      * The plugin manager.
      */
     private PluginManager manager;
 
     /**
      * Input field for url.
      */
     private final JTextField urlField;
 
     /**
      * Browse button.
      */
     private final JButton browseButton;
 
     /**
      * Install plugin button.
      */
     private JButton installPluginButton;
 
     /**
      * Plugin list.
      */
     private final JList pluginList;
 
     /**
      * Remove plugin button.
      */
     private final JButton removePluginButton;
 
     /**
      * Constructor.
      * @param settings Settings to manipulate.
      */
     public PluginPreferencePane(PluginManager manager) {
         this.manager = manager;
         GroupLayout layout = new GroupLayout(this);
 
         layout.setAutoCreateGaps(true);
         layout.setAutoCreateContainerGaps(true);
 
         JLabel urlLabel = new JLabel(I18n.get("ch9k.plugins", "set_url"));
         urlField = new JTextField();
         browseButton = new JButton(I18n.get("ch9k.plugins", "browse_file"));
         installPluginButton =
                 new JButton(I18n.get("ch9k.plugins", "install_plugin"));
         JLabel pluginListLabel =
                 new JLabel(I18n.get("ch9k.plugins", "plugin_list"));
         pluginList = new JList();
         JScrollPane pluginListScrollPane = new JScrollPane();
         pluginListScrollPane.getViewport().setView(pluginList);
         removePluginButton = new JButton(
                 I18n.get("ch9k.plugins", "remove_plugin"));
 
         layout.setHorizontalGroup(layout.createParallelGroup()
             .addComponent(urlLabel)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(urlField)
                 .addComponent(browseButton))
             .addComponent(installPluginButton)
             .addComponent(pluginListLabel)
             .addComponent(pluginListScrollPane)
             .addComponent(removePluginButton));
 
         layout.setVerticalGroup(layout.createSequentialGroup()
             .addComponent(urlLabel)
             .addGroup(layout.createParallelGroup()
                .addComponent(urlField, GroupLayout.PREFERRED_SIZE,
                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                 .addComponent(browseButton))
             .addComponent(installPluginButton)
             .addGap(20)
             .addComponent(pluginListLabel)
             .addComponent(pluginListScrollPane)
             .addComponent(removePluginButton));
 
         layout.linkSize(SwingConstants.HORIZONTAL,
                 installPluginButton, removePluginButton);
        layout.linkSize(SwingConstants.VERTICAL,
                urlField, browseButton);
 
         setLayout(layout);
 
         updatePluginList();
 
         manager.addChangeListener(this);
     }
 
     /**
      * Update the plugin list.
      */
     private void updatePluginList() {
         Set<String> names = manager.getPrettyNames().keySet();
         pluginList.setListData(names.toArray());
     }
 
     @Override
     public void stateChanged(ChangeEvent event) {
         updatePluginList();
     }
 }
