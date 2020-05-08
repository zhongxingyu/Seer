 #set( $symbol_pound = '#' )
 #set( $symbol_dollar = '$' )
 #set( $symbol_escape = '\' )
 package ${package}.internal;
 
 import java.awt.event.ActionEvent;
 
 import javax.swing.JOptionPane;
 
 import org.cytoscape.application.swing.AbstractCyAction;
 import org.cytoscape.application.CyApplicationManager;
 
 
 /**
  * Creates a new menu item under Apps menu section.
  *
  */
 public class MenuAction extends AbstractCyAction {
 
 	public MenuAction(final CyApplicationManager applicationManager, final String menuTitle) {
		super(menuTitle, applicationManager);
 		setPreferredMenu("Apps");
 	}
 
 	public void actionPerformed(ActionEvent e) {
 
 		// Write your own function here.
 		JOptionPane.showMessageDialog(null, "Hello Cytoscape World!");
 		
 	}
 }
