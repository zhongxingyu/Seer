 package org.acme.example.view;
 
 import gov.nasa.arc.mct.components.AbstractComponent;
 import gov.nasa.arc.mct.components.FeedProvider;
 import gov.nasa.arc.mct.components.Placeholder;
 import gov.nasa.arc.mct.evaluator.api.Evaluator;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JTable;
import javax.swing.ListModel;
 import javax.swing.table.AbstractTableModel;
 
 class MultiColTableModel extends AbstractTableModel {
 	//temp:
 	private static final String _2010_250T21_26_25_762 = "2010-250T21:26:25.762";
 	private static final String _0334154108_25000 = "0334154108.25000";
 	private static final String _22_59_24_448 = "22:59:24.448";
 	private static final String DEQ_C = "deqC";
	private static final String _2 = "2";
 	private static final String ADC_RPAM_A_REU_PRT12 = "ADC RPAM A REU PRT12";
 	private static final String THRM_T_BITBOX1 = "THRM-T-BITBOX1";
 	private static final String ABCD_6789 = "ABCD-6789";
 
	private static final String NONE = "none";
 	//private String[] columnNames; //deprecated
 	private ArrayList<ColumnType> columnList;
 	private AbstractComponent selectedComponent; //is this name confusing because it's the same as in MultiColView?
 	private List<AbstractComponent> childrenList;
 	private MultiColView multiColViewManifestation;
 	private MultiColTable table;
 	private ViewSettings settings;
 	private Map<String, Object> values = new HashMap<String, Object>(); 
 	private Map<String,List<Integer>> componentLocations;
 	
 	private ListenerManager listenerManager = new ListenerManager();
 	
 	public MultiColTableModel(AbstractComponent componentin, MultiColTable table, ViewSettings settings) {
 		this.table = table;
 		this.settings = settings;
 		columnList = settings.getColumnTypes();
 		multiColViewManifestation = table.getMultiColView();
 		selectedComponent = componentin;
 		childrenList = selectedComponent.getComponents();
 		if(childrenList.size()==0) {
 			childrenList = new ArrayList<AbstractComponent>();
 			childrenList.add(selectedComponent);
 		}
 		updateLocations();
 	}
 					
 	public int getColumnCount() { return settings.getNumberOfColumns(); }
 	public int getRowCount()    { return childrenList.size(); }
 	public MultiColTable getMultiColTable() { return table; }
 	public JTable getJTable() { return table.getTable(); }
 	
 	public AbstractComponent getComponentOfRow(int rowIndex) {
 		return childrenList.get(rowIndex);
 	}
 
 	@Override
 	public String getColumnName(int colIndex) {
 		ColumnType colType = settings.getColumnAtIndex(colIndex);
 		return colType.toString();
 	}
 	
 	private void updateLocations() {
 		componentLocations = new HashMap<String,List<Integer>>();
 		for (int row=0; row < getRowCount(); ++row) {
 			AbstractComponent component = childrenList.get(row);
 			if (component != null) {
 				component.addViewManifestation(multiColViewManifestation);
 				List<Integer> locations = componentLocations.get(getKey(component));
 				if (locations == null) {
 					locations = new ArrayList<Integer>();
 					componentLocations.put(getKey(component), locations);
 				}
 				locations.add(row);
 			}
 		}
 	}
 	
 	//TODO: include tableview's CTM's fireCellSettingsChanged?
 
 	/**
 	 * Returns a unique key for a given component. This key is used by the
 	 * code that responds to a feed update to pass along changes to the
 	 * component's value.
 	 * 
 	 * @param component the component for which to determine the key
 	 * @return a unique key for the component
 	 */
 	public String getKey(AbstractComponent component) {
 		AbstractComponent delegate = component;
 		Evaluator e = component.getCapability(Evaluator.class);
 		if (e != null && component.getComponents().size() > 1) {
 			return component.getComponentId();
 		}
 		FeedProvider fp = component.getCapability(FeedProvider.class);
 		if (fp != null) {
 			return fp.getSubscriptionId();
 		}
 		
 		return delegate.getComponentId();
 	}
 	
 	public Object getValueAt(int r, int c) {
 		ColumnType colType = settings.getColumnAtIndex(c);
 		Object cellDatum = null;
 		AbstractComponent cellComponent = childrenList.get(r);
 		switch(colType) {
 		case ID:
 			cellDatum = ABCD_6789; break;
 		case TITLE:
 			cellDatum = THRM_T_BITBOX1; break;
 		case FSW_NAME: 
 			cellDatum = ADC_RPAM_A_REU_PRT12; break;
 		case RAW:
			cellDatum = _2; break;
 		case VALUE:
 			cellDatum = getValueForComponent(cellComponent); break;
 		case UNIT:
 			cellDatum = DEQ_C; break;
 		case ERT:
 			cellDatum = _22_59_24_448; break;
 		case SCLK:
 			cellDatum = _0334154108_25000; break;
 		case SCET:
 			cellDatum = _2010_250T21_26_25_762; break;
 		}
 		if(cellDatum==null) { return "(no data)"; }
 		else                { return cellDatum; }
 	}
 	
 	/**
 	 * Sets the value of an object updated by a data feed. This change
 	 * is propogated to all table cells displaying that object.
 	 * 
 	 * @param id the identifier for the object updated
 	 * @param value the new value to display
 	 */
 	public void setValue(String id, Object value) {
 		values.put(id, value);
 		List<Integer> locations = componentLocations.get(id);
 		if (locations != null) {
 			for (Integer row : locations) {
 				fireTableCellUpdated(row, ColumnType.VALUE.ordinal()); 
 			}
 		}
 	}
 	
 	private Object getValueForComponent(AbstractComponent component) {
 		Object value = values.get(getKey(component));
 		//System.out.println("getValueForComponent ||||||||||||||||||||||||| " + values.size()); //debug
 		if (value == null) {
 			DisplayedValue displayedValue = new DisplayedValue();
 			if (component.getCapability(Placeholder.class) != null) {
 			    displayedValue.setValue(component.getCapability(Placeholder.class).getPlaceholderValue());
 			} else {
 				displayedValue.setValue(component.getDisplayName());
 			}
 			return displayedValue;
 		} else {
 			return value;
 		}
 	}
 	
 	public Class getColumnClass(int c) { return getValueAt(0, c).getClass(); }
 	
 	//copied somewhat blindly from labeledtablemodel.java:
 	/**
 	 * Adds a listener for events fired when the labels have been updated.
 	 * 
 	 * @param listener the label change listener.
 	 */
 	public void addLabelChangeListener(LabelChangeListener listener) {
 		listenerManager.addListener(LabelChangeListener.class, listener);
 	}
 	
 	/**
 	 * Removes the listener for events.
 	 * @param listener the label change listener.
 	 */
 	public void removeLabelChangeListener(LabelChangeListener listener) {
 		listenerManager.removeListener(LabelChangeListener.class, listener);
 	}
 	
 	/**
 	 * Gets the table type.
 	 * @return 2-D TableType 
 	 */
 	public TableType getTableType() {
 		return TableType.TWO_DIMENSIONAL;
 	}
 
 	protected Object getStoredObjectAt(int objectIndex, int attributeIndex) {
 		return getValueAt(objectIndex, attributeIndex);
 	}
 	
 	/**
 	 * Handles an event where the row, column, or cell labels have changed.
 	 * Triggers a structure changed event so that the table will be redrawn
 	 * completely.
 	 */
 	public void fireLabelsChanged() {
 		listenerManager.fireEvent(LabelChangeListener.class, new ListenerNotifier<LabelChangeListener>() {
 			@Override
 			public void notifyEvent(LabelChangeListener listener) {
 				listener.labelsChanged();
 			}
 		});
 	}
 
 	@Override
 	public void fireTableCellUpdated(int row, int column) {
 		super.fireTableCellUpdated(row, column);
 	}
 	
 	//TODO: code for changing this model when components are moved around in MCT component tree, etc
 }
