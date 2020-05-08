 //
 // ListWidget.java
 //
 
 package restless.fixbe.view;
 
 import java.awt.BorderLayout;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import restless.fixbe.data.ListRule;
 import restless.fixbe.data.Rule;
 
 /** A widget for a collection of rules (possibly repeated). */
 public class ListWidget extends RuleWidget implements ListSelectionListener {
 
 	private ListRule rule;
 	private boolean tabs;
 
 	private byte[] theData;
 	private int lastBaseOffset;
 	private int lastDefaultSpacing;
 	private int lastIndex;
 
 	private JList list;
 	private List<RuleWidget> widgets = new ArrayList<RuleWidget>();
 
 	public ListWidget(ListRule rule) {
 		this(rule, false);
 	}
 
 	public ListWidget(ListRule rule, boolean tabs) {
 		super(rule);
 		this.rule = rule;
 		this.tabs = tabs;
 		createGUI();
 	}
 
 	@Override
 	public void populate(byte[] data,
 		int baseOffset, int defaultSpacing, int index)
 	{
 		if (index > 0 && rule.getRepetitions() > 1) {
 			throw new IllegalStateException("Cannot nest repeated lists");
 		}
 		theData = data;
 		lastBaseOffset = baseOffset + rule.getOffset();
 		final int spacing = rule.getSpacing();
 		lastDefaultSpacing = spacing == 0 ? defaultSpacing : spacing;
 		lastIndex = index;
 		updateWidgets();
 	}
 
 	public void valueChanged(ListSelectionEvent e) {
 		updateWidgets();
 	}
 
 	private void createGUI() {
 		setLayout(new BorderLayout());
 		if (tabs) createTabbedGUI();
 		else createColumnsGUI();
 	}
 
 	private void createTabbedGUI() {
 		final List<Rule> items = rule.getItems();
 
 		final JTabbedPane rulesPane = new JTabbedPane();
 		for (int i = 0; i < items.size(); i++) {
 			final Rule item = items.get(i);
 			final RuleWidget widget = item.createWidget();
 			widgets.add(widget);
 			rulesPane.addTab(item.getLabel(), widget);
 		}
 		add(rulesPane);
 	}
 
 	private void createColumnsGUI() {
 		final String label = rule.getLabel();
 		final List<Rule> items = rule.getItems();
 		final int reps = rule.getRepetitions();
 		final int columns = rule.getColumns();
 
 		final JPanel rulesPane = new JPanel();
 		rulesPane.setLayout(new BoxLayout(rulesPane, BoxLayout.X_AXIS));
 
 		final List<JPanel> columnPanes = new ArrayList<JPanel>();
 		int col = -1;
 		final int itemsPerColumn = (items.size() + columns - 1) / columns;
 		for (int i = 0; i < items.size(); i++) {
 			final Rule item = items.get(i);
 			if (i % itemsPerColumn == 0) {
 				// begin a new column
 				final JPanel columnPane = new JPanel();
 				columnPane.setLayout(new BoxLayout(columnPane, BoxLayout.Y_AXIS));
 				columnPanes.add(columnPane);
 				rulesPane.add(columnPane);
 				rulesPane.add(Box.createHorizontalStrut(5));
 				col++;
 			}
 			final RuleWidget widget = item.createWidget();
 			widgets.add(widget);
 			columnPanes.get(col).add(widget);
 		}
 		for (JPanel columnPane : columnPanes) {
 			columnPane.add(Box.createVerticalGlue());
 		}
 
 		if (reps > 1) {
 			final String[] itemLabels = new String[reps];
 			for (int i = 0; i < itemLabels.length; i++) {
 				itemLabels[i] = label + " " + (i + 1);
 			}
 			list = new JList(itemLabels);
 			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 			list.setSelectedIndex(0);
 			list.addListSelectionListener(this);
 			final JScrollPane listPane = new JScrollPane(list);
 			final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
 			splitPane.setLeftComponent(listPane);
 			splitPane.setRightComponent(rulesPane);
 			add(splitPane);
 		}
 		else {
 			if (label != null) rulesPane.setBorder(new TitledBorder(label));
 			add(rulesPane);
 		}
 	}
 
 	private void updateWidgets() {
 		int index = list == null ? 0 : list.getSelectedIndex();
 		if (index < 0) return;
 		if (index == 0) index = lastIndex;
 		for (RuleWidget widget : widgets) {
 			widget.populate(theData, lastBaseOffset, lastDefaultSpacing, index);
 		}
 	}
 
 }
