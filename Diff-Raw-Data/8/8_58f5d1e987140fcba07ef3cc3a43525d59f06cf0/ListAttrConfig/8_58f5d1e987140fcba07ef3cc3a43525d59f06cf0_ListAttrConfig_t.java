 
 package org.paxle.desktop.impl.dialogues.settings;
 
 import javax.swing.JComponent;
 import javax.swing.JList;
 import javax.swing.ListSelectionModel;
 
 import org.osgi.service.metatype.AttributeDefinition;
 import org.paxle.desktop.impl.event.MultipleChangesListener;
 
 class ListAttrConfig extends AbstractMultiAttrConfig {
 	
 	private JList comp;
 	
 	public ListAttrConfig(final AttributeDefinition ad) {
 		super(ad);
 	}
 	
 	@Override
 	protected JComponent createOptionComp(Object value, MultipleChangesListener mcl) {
 		comp = new JList(labels);
 		comp.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 		mcl.addComp2Monitor(comp);
 		return comp;
 	}
 	
 	@Override
 	public Object getValue() {
		Object[] temp = comp.getSelectedValues();
		
		// convert values into proper format
		// XXX: we have a problem here if the required target-value-type is a String
		String[] val = new String[temp==null?0:temp.length];
		System.arraycopy(temp, 0, val, 0, val.length);
		return val;
 	}
 	
 	@Override
 	public void setValue(Object newValue) {
 		comp.setSelectedIndices((int[])newValue);
 	}
 }
