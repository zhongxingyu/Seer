 package net.bubbaland.trivia.client;
 
 import java.awt.Frame;
 import java.awt.GridBagConstraints;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Hashtable;
 import java.util.Set;
 
 import javax.swing.JComboBox;
 import javax.swing.JOptionPane;
 import javax.swing.JTextArea;
 import javax.swing.SwingUtilities;
 
 public class AddTabDialog extends TriviaDialogPanel implements ItemListener {
 
 	private static final long		serialVersionUID	= -6388311089354721920L;
 
 	private final JComboBox<String>	tabSelector;
 	private final JTextArea			descriptionLabel;
 	private final TriviaClient		client;
 
 	public AddTabDialog(TriviaFrame panel, TriviaClient client, DnDTabbedPane pane) {
 		super();
 
 		this.client = client;
 
 		Set<String> tabNameSet = client.getTabNames();
 		String[] tabNames = new String[tabNameSet.size()];
 		tabNameSet.toArray(tabNames);
 		Arrays.sort(tabNames, new TabCompare());
 
 		// Set up layout constraints
 		final GridBagConstraints constraints = new GridBagConstraints();
 		constraints.fill = GridBagConstraints.BOTH;
 		constraints.anchor = GridBagConstraints.CENTER;
 		constraints.weightx = 0.0;
 		constraints.weighty = 0.0;
 		constraints.gridx = 0;
 		constraints.gridy = 0;
 
 		this.tabSelector = new JComboBox<String>(tabNames);
 		this.add(this.tabSelector, constraints);
 		this.tabSelector.addItemListener(this);
 
 		constraints.weightx = 1.0;
 		constraints.weighty = 1.0;
 		constraints.gridx = 0;
 		constraints.gridy = 1;
 		this.descriptionLabel = new JTextArea(this.client.getTabDescription(tabNames[0]));
 		this.descriptionLabel.setEditable(false);
 		this.descriptionLabel.setLineWrap(true);
 		this.descriptionLabel.setWrapStyleWord(true);
 		this.add(this.descriptionLabel, constraints);
 
 		Object[] options = { "Add", "Add All", "Cancel" };
 
 		// Display the dialog box
 		this.dialog = new TriviaDialog((Frame) SwingUtilities.getWindowAncestor(pane), "Add tab", this,
 				JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options);
 		this.dialog.setName("Add Tab");
 		this.dialog.setVisible(true);
 
 		// If the OK button was pressed, add the proposed answer to the queue
		if (!( dialog.getValue() instanceof String )) {
			return;
		}
 		final String option = (String) dialog.getValue();
 		final ArrayList<String> newTabs = new ArrayList<String>(0);
 		switch (option) {
 			case "Add": {
 				newTabs.add((String) tabSelector.getSelectedItem());
 				break;
 			}
 			case "Add All":
 				for (String tabName : tabNameSet) {
 					if (!tabName.startsWith("*") && pane.indexOfTab(tabName) == -1) {
 						newTabs.add(tabName);
 					}
 				}
 				break;
			default:
				return;
 		}
 		for (String tabName : newTabs) {
 			if (tabName.startsWith("*")) {
 				tabName = tabName.replaceFirst("\\*", "");
 			}
 			String altName = tabName;
 			int i = 1;
 			while (pane.indexOfTab(altName) > -1) {
 				altName = tabName + " (" + i + ")";
 				i++;
 			}
 			pane.addTab(altName, client.getTab(panel, tabName));
 			final int tabLocation = pane.indexOfTab(altName);
 			pane.setSelectedIndex(tabLocation);
 		}
 
 	}
 
 	@Override
 	public void itemStateChanged(ItemEvent e) {
 		String tabName = (String) this.tabSelector.getSelectedItem();
 		String description = this.client.getTabDescription(tabName);
 		this.descriptionLabel.setText(description);
 	}
 
 	public static class TabCompare implements Comparator<String> {
 
 		final static private Hashtable<String, Integer>	SORT_ORDER;
 		static {
 			SORT_ORDER = new Hashtable<String, Integer>(0);
 			SORT_ORDER.put("Workflow", 0);
 			SORT_ORDER.put("Current", 1);
 			SORT_ORDER.put("History", 2);
 			SORT_ORDER.put("By Round", 3);
 			SORT_ORDER.put("Place Chart", 4);
 			SORT_ORDER.put("Score Chart", 5);
 			SORT_ORDER.put("Cumul. Score Chart", 6);
 			SORT_ORDER.put("Team Comparison", 7);
 			SORT_ORDER.put("*Open Questions", 8);
 			SORT_ORDER.put("*Answer Queue", 9);
 		}
 
 		@Override
 		public int compare(String o1, String o2) {
 			return SORT_ORDER.get(o1).compareTo(SORT_ORDER.get(o2));
 		}
 	}
 
 }
