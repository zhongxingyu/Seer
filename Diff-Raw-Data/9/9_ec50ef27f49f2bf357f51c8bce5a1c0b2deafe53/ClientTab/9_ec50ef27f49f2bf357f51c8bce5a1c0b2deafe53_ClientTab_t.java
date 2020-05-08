 package ee.ut.math.tvt.salessystem.ui.tabs;
 
 import java.awt.Component;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 
 import org.apache.log4j.Logger;
 
 import ee.ut.math.tvt.salessystem.ui.model.SalesSystemModel;
 
 
 public class ClientTab {
 
 	private static final Logger log = Logger.getLogger(ClientTab.class);
 
     private SalesSystemModel model;
     
     public ClientTab(SalesSystemModel model) {
         this.model = model;
     } 
     
     /**
      * The main entry-point method. Creates the tab.
      */
     public Component draw() {
         JPanel panel = new JPanel();
         
         GridBagConstraints gc = getGbConstraints();
         GridBagLayout gb = new GridBagLayout();
         
         panel.setLayout(gb);
         panel.add(drawClientsTable(), gc);
		
         return panel;
     }
 
     
 
     
     private Component drawClientsTable() {
 
         // Create the table 
         JTable table = new JTable(model.getClientTableModel());
         table.getTableHeader().setReorderingAllowed(false);
         JScrollPane scrollPane = new JScrollPane(table);
 
         // Wrap it inside a panel
         JPanel panel = new JPanel();
         panel.setLayout(new GridBagLayout());
         panel.setBorder(BorderFactory.createTitledBorder("Clients"));
         
 		panel.add(scrollPane, getGbConstraints());
         return panel;
     }
 
 
     
     private GridBagConstraints getGbConstraints() {
         GridBagConstraints gc = new GridBagConstraints();
         gc.fill = GridBagConstraints.BOTH;
         gc.gridwidth = GridBagConstraints.REMAINDER;
         gc.weightx = 1.0;
         gc.weighty = 1.0;
         return gc;
     }    
     
 	public void refresh() {
 		log.info("Client tab refresh");
 		this.model.refreshClients();
 	}
 }
