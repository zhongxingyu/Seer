 package org.sonar.ide.ui;
 
 import com.jgoodies.forms.builder.DefaultFormBuilder;
 import com.jgoodies.forms.layout.FormLayout;
 
 import javax.swing.*;
 import java.util.Properties;
 
 /**
  * @author Evgeny Mandrikov
  */
 public class SonarConfigPanel extends AbstractConfigPanel {
   private final JTextField host;
   private final JTextField username;
   private final JPasswordField password;
 
   public SonarConfigPanel() {
     super();
 
     host = new JTextField();
     username = new JTextField();
     password = new JPasswordField();
     JButton testConnection = new JButton("Test connection");
     testConnection.setEnabled(false);
 
     DefaultFormBuilder formBuilder = new DefaultFormBuilder(new FormLayout(""));
     formBuilder.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
     formBuilder.appendColumn("right:pref");
     formBuilder.appendColumn("3dlu");
     formBuilder.appendColumn("fill:p:g");
    formBuilder.appendColumn("3dlu");
    formBuilder.appendColumn("pref");
     formBuilder.append("Host:", host);
    formBuilder.append(testConnection);
     formBuilder.append("Username:", username);
    formBuilder.nextRow();
     formBuilder.append("Password:", password);
 
     add(formBuilder.getPanel());
   }
 
   @Override
   public boolean isModified() {
     // TODO
     return true;
   }
 
   public Properties getProperties() {
     Properties properties = new Properties();
     properties.setProperty("host", host.getText());
     properties.setProperty("username", username.getText());
     properties.setProperty("password", new String(password.getPassword()));
     return properties;
   }
 }
