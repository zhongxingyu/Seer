 package cc.rylander.intellij.plugin.polopoly;
 
 import javax.swing.*;
 
 public class PolopolyInstallationConfigGUI {
   public JPanel rootComponent;
   public JLabel urlLabel;
   public JTextField url;
   public JLabel usernameLabel;
   public JTextField username;
   public JLabel passwordLabel;
     public JTextField password;
 
   public PolopolyInstallationConfigGUI() {
     urlLabel.setLabelFor(url);
     usernameLabel.setLabelFor(username);
     passwordLabel.setLabelFor(password);
   }
 
   public boolean isModified(String _url, String _username, String _password) {
    return url.getText() != null ? ! url.getText().equals(_url) : _url != null &&
        username.getText() != null ? ! username.getText().equals(_username) : _username != null &&
        password.getText() != null ? ! password.getText().equals(_password) : _password != null;
   }
 }
