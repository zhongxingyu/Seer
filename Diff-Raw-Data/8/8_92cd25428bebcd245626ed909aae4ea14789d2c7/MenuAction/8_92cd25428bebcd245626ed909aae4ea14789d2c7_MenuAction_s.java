 #set( $symbol_pound = '#' )
 #set( $symbol_dollar = '$' )
 #set( $symbol_escape = '\' )
 package ${package}.internal;
 
 import java.awt.event.ActionEvent;
 
 import javax.swing.JOptionPane;
 
 import org.cytoscape.application.swing.AbstractCyAction;
 
 
 /**
  * Creates a new menu item under Apps menu section.
  *
  */
 public class MenuAction extends AbstractCyAction {
 
 	public MenuAction(CyApplicationManager cyApplicationManager, final String menuTitle) {
 		
 		super(menuTitle, cyApplicationManager, null, null);
 		setPreferredMenu("Apps");
 		
 	}
 
 	public void actionPerformed(ActionEvent e) {
 
 		// Write your own function here.
 		JOptionPane.showMessageDialog(null, "Hello Cytoscape World!");
 		
 	}
 }
