 package ch9k.core.settings;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.GroupLayout;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 /**
  *
  * @author toon
  */
 public class ProxyPrefPane extends JPanel {
     private Settings settings;
     private JTextField proxyField;
 
     private JTextField proxyPortField;
 
     public ProxyPrefPane(final Settings settings) {
         this.settings = settings;
         
         proxyField = new JTextField();
         proxyField.setColumns(30);
         proxyField.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 settings.set("proxy", proxyField.getText());
             }
         });
         if(settings.get("proxy") != null && !settings.get("proxy").isEmpty()){
             proxyField.setText(settings.get("proxy"));
         }
         settings.addSettingsListener(new SettingsChangeListener() {
             public void settingsChanged(SettingsChangeEvent event) {
                 if(event.getKey().equals("proxy")) {
                     System.setProperty("http.proxyHost", event.getValue());
                 }
             }
         });
 
         proxyPortField = new JTextField();
         proxyPortField.setColumns(15);
 
         proxyPortField.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 settings.set("proxyport", proxyPortField.getText());
             }
         });
 
         if(settings.get("proxyport") != null && !settings.get("proxyport").isEmpty()) {
            proxyPortField.setText(settings.get("proxyport"));
         }
 
 
         settings.addSettingsListener(new SettingsChangeListener() {
             public void settingsChanged(SettingsChangeEvent event) {
                 if(event.getKey().equals("proxyport")) {
                     System.setProperty("http.proxyPort", event.getValue());
                 }
             }
         });
 
 
         JLabel proxyLabel = new JLabel("Proxy: ");
         JLabel portLabel = new JLabel("Port: ");
         GroupLayout layout = new GroupLayout(this);
         layout.setVerticalGroup(layout.createSequentialGroup()
                 .addGroup(layout.createParallelGroup()
                     .addComponent(proxyLabel)
                     .addComponent(proxyField)
                 ).addGroup(layout.createParallelGroup()
                     .addComponent(portLabel)
                     .addComponent(proxyPortField))
                 );
 
         layout.setHorizontalGroup(layout.createSequentialGroup()
                 .addGroup(layout.createParallelGroup()
                     .addComponent(proxyLabel)
                     .addComponent(portLabel)
                 ).addGroup(layout.createParallelGroup()
                     .addComponent(proxyField)
                     .addComponent(proxyPortField))
                 );
 
         setLayout(layout);
 
     }
 }
