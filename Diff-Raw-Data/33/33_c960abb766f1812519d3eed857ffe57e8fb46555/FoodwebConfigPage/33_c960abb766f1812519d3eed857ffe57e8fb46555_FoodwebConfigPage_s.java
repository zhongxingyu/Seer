 package driver.config;
 
 import javax.swing.BoxLayout;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import cwcore.complexParams.ComplexAgentParams;
 import cwcore.complexParams.FoodwebParams;
 
 
 public class FoodwebConfigPage implements ConfigPage {
 
 	private JPanel foodPanel;
 	private MixedValueJTable foodTable;
 
 	public FoodwebConfigPage(ComplexAgentParams[] params) {
 		foodPanel = new JPanel();
 		// tabbedPane.addTab("Agents", panel3);
 
 		foodTable = new MixedValueJTable();
 
 		FoodwebParams[] foodweb = new FoodwebParams[params.length];
 		for (int i = 0; i < params.length; i++) {
 			foodweb[i] = params[i].foodweb;
 		}
 		foodTable.setModel(new ConfigTableModel(foodweb, "Agent "));
 
 		GUI.colorHeaders(foodTable, true);
 
 		// Create the scroll pane and add the table to it.
 		JScrollPane foodScroll = new JScrollPane(foodTable);
 
 		foodPanel.setLayout(new BoxLayout(foodPanel, BoxLayout.X_AXIS));
 		GUI.makeGroupPanel(foodPanel, "Food Parameters");
 		foodPanel.add(foodScroll);
 	}
 
	@Override
 	public JPanel getPanel() {
 		return foodPanel;
 	}
 
	@Override
 	public void validateUI() throws IllegalArgumentException {
 		GUI.updateTable(foodTable);
 	}
 
 }
