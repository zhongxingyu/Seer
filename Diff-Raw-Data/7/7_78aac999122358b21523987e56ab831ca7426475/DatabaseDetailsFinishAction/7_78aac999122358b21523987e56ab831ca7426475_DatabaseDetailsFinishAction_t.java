 package de.aidger.controller.actions;
 
 import static de.aidger.utils.Translation._;
 import de.aidger.model.Runtime;
 import de.aidger.view.UI;
 import de.aidger.view.Wizard;
 import de.aidger.view.wizard.DatabaseDetails;
 
 import java.awt.event.ActionEvent;
 import javax.swing.JComponent;
 import javax.swing.AbstractAction;
 
 /**
  * This action saves the username before advancing.
  *
  * @author rmbl
  */
 public class DatabaseDetailsFinishAction extends AbstractAction {
 
     public void actionPerformed(ActionEvent e) {
         Wizard dlg = (Wizard) ((JComponent) e.getSource())
             .getTopLevelAncestor();
 
         String type = Runtime.getInstance().getOption("database-type");
         DatabaseDetails dtls = ((DatabaseDetails) dlg.getCurrentPanel());
 
         Runtime.getInstance().setOption("database-driver", dtls.getDriver());
         String uri;
         if (type.equals("0")) {
             if (dtls.getHost().isEmpty()) {
                 UI.displayError(_("The path can't be empty."));
                 e.setSource(null);
                 return;
             } 
 
             uri = "jdbc:derby:" + dtls.getHost() + ";create=true";
         } else if (type.equals("1")) {
             if (dtls.getHost().isEmpty() || dtls.getPort().equals(0) || dtls.getDatabase().isEmpty()) {
                 UI.displayError(_("The hostname, port and database can't be empty."));
                 e.setSource(null);
                 return;
             }
             uri = "jdbc:mysql://" + dtls.getHost() + ":" + dtls.getPort() + "/" + dtls.getDatabase();
             if (!dtls.getUsername().isEmpty()) {
                uri += "?user=" + dtls.getUsername() + "&password=" + dtls.getPassword()
                		+ "&autoReconnect=true";
            } else {
            	uri += "?autoReconnect=true";
            }            
         } else {
             if (dtls.getHost().isEmpty()) {
                 UI.displayError(_("The JDBC Uri can't be empty."));
                 e.setSource(null);
                 return;
             }
             uri = dtls.getHost();
         }
         Runtime.getInstance().setOption("database-uri", uri);
     }
 
 }
