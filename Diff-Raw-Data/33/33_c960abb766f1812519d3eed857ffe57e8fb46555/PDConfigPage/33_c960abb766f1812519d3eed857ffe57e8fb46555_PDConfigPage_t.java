 package driver.config;
 
 import java.awt.BorderLayout;
 
 import javax.swing.BoxLayout;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 
 import cwcore.complexParams.PDParams;
 
 
 public class PDConfigPage implements ConfigPage {
 
 	private JPanel panelPD;
 	private JTable tablePD;
 
 	public PDConfigPage(PDParams params) {
 		panelPD = new JPanel();
 
 		tablePD = new JTable();
 		tablePD.setModel(new ConfigTableModel(params, "Value"));
 
 		JScrollPane scrollPanePD = new JScrollPane(tablePD);
 
 		panelPD.add(scrollPanePD, BorderLayout.CENTER);
 
 		panelPD.setLayout(new BoxLayout(panelPD, BoxLayout.X_AXIS));
 		GUI.makeGroupPanel(panelPD, "Prisoner's Dilemma Parameters");
 	}
 
 	public JPanel getPanel() {
 		return panelPD;
 	}
 
 	public void validateUI() throws IllegalArgumentException {
 		GUI.updateTable(tablePD);
 	}
 
 }
