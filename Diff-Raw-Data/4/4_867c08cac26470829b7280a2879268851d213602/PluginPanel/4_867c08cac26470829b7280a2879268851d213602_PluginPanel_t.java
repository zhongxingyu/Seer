 package evaluation.simulator.gui.customElements;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import javax.swing.JComboBox;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import evaluation.simulator.annotations.simulationProperty.SimProp;
 import evaluation.simulator.gui.customElements.accordion.AccordionEntry;
 import evaluation.simulator.gui.pluginRegistry.SimPropRegistry;
 
 public class PluginPanel extends JScrollPane {
 
 	private JPanel _panel;
 	HashMap<String, JComboBox<String>> pluginListsMap = new HashMap<>();
 
 	SimPropRegistry simPropRegistry;
 
 	public PluginPanel() {
 		this.initPanel();
 	}
 
 	private List<SimProp> getSections() {
 		List<SimProp> list = new LinkedList<SimProp>();
 
 		Set<Entry<String, SimProp>> e = this.simPropRegistry.getAllSimProps();
 
 		Iterator<Entry<String, SimProp>> iter = e.iterator();
 		while (iter.hasNext()) {
 			Entry<?, ?> entry = iter.next();
 			list.add((SimProp) entry.getValue());
 		}
 
 		return list;
 	}
 
 	private void initPanel() {
 		// Start Layout
 		this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.getVerticalScrollBar().setUnitIncrement(16);
 
 		this._panel = new JPanel();
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		GridBagConstraints gridBagConstraints = new GridBagConstraints();
 		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
 		gridBagConstraints.anchor = GridBagConstraints.NORTH;
 		gridBagConstraints.weightx = 1;
		gridBagConstraints.weighty = 0;
 		gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
 		gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
 		gridBagLayout.setConstraints(this._panel, gridBagConstraints);
 		this._panel.setLayout(gridBagLayout);
 
 		this.setViewportView(this._panel);
 
 		// End Layout
 
 		this.simPropRegistry = SimPropRegistry.getInstance();
 
 		Map<String, String>[] PluginLayerMap = this.simPropRegistry
 				.getPlugIns();
 
 		String[] levelStrings[] = {
 				PluginLayerMap[0].keySet().toArray(
 						new String[PluginLayerMap[0].size()]),
 				PluginLayerMap[1].keySet().toArray(
 						new String[PluginLayerMap[1].size()]),
 				PluginLayerMap[2].keySet().toArray(
 						new String[PluginLayerMap[2].size()]),
 				PluginLayerMap[3].keySet().toArray(
 						new String[PluginLayerMap[3].size()]),
 				PluginLayerMap[4].keySet().toArray(
 						new String[PluginLayerMap[4].size()]),
 				PluginLayerMap[5].keySet().toArray(
 						new String[PluginLayerMap[5].size()]) };
 
 		JComboBox<String> plugInLevel1List = new JComboBox<String>(
 				levelStrings[0]);
 
 		this.pluginListsMap.put("clientSendStyle", plugInLevel1List);
 		JComboBox<String> plugInLevel2List = new JComboBox<String>(
 				levelStrings[1]);
 		this.pluginListsMap.put("delayBox", plugInLevel2List);
 		JComboBox<String> plugInLevel3List = new JComboBox<String>(
 				levelStrings[2]);
 		this.pluginListsMap.put("mixSendStyle", plugInLevel3List);
 		JComboBox<String> plugInLevel4List = new JComboBox<String>(
 				levelStrings[3]);
 		this.pluginListsMap.put("outputStrategy", plugInLevel4List);
 		JComboBox<String> plugInLevel5List = new JComboBox<String>(
 				levelStrings[4]);
 		this.pluginListsMap.put("plotType", plugInLevel5List);
 		JComboBox<String> plugInLevel6List = new JComboBox<String>(
 				levelStrings[5]);
 		this.pluginListsMap.put("trafficSource", plugInLevel6List);
 
 		AccordionEntry accordionElement;
 
 		List<SimProp> listofAllSections = this.getSections();
 		Set<String> listOfAllCategories = new HashSet<String>();
 
 		Iterator<SimProp> iter = listofAllSections.iterator();
 		while (iter.hasNext()) {
 			String category = iter.next().getPluginLayer();
 			listOfAllCategories.add(category);
 		}
 
 		// Select the categories
 		Iterator<String> iter2 = listOfAllCategories.iterator();
 		while (iter2.hasNext()) {
 			String category = iter2.next();
 
 			// Create the list of all elements in a category
 			List<SimProp> listOfAllSectionsInACategory = new LinkedList<SimProp>();
 			Iterator<SimProp> iter1 = listofAllSections.iterator();
 			while (iter1.hasNext()) {
 				SimProp propToCheck = iter1.next();
 				if (propToCheck.getPluginLayer().equals(category)) {
 					listOfAllSectionsInACategory.add(propToCheck);
 				}
 			}
 
 			accordionElement = new AccordionEntry(category,
 					listOfAllSectionsInACategory,
 					this.pluginListsMap.get(category));
 			this._panel.add(accordionElement, gridBagConstraints);
 		}
 
 		gridBagConstraints.weighty = 1;
 
 		// Spring element to push all other elements to the top
 		// needed for alignment
 		JPanel jp = new JPanel();
 		this._panel.add(jp, gridBagConstraints);
 
 		this.setVisible(true);
 	}
 }
