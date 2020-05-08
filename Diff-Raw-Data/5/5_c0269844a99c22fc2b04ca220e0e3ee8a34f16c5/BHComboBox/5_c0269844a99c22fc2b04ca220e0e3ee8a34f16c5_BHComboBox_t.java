 package org.bh.gui.swing;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Arrays;
 import java.util.Comparator;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JComboBox;
 
 import org.bh.data.types.IValue;
 import org.bh.gui.CompValueChangeManager;
 import org.bh.platform.IPlatformListener;
 import org.bh.platform.PlatformEvent;
 import org.bh.platform.Services;
 import org.bh.platform.PlatformEvent.Type;
 import org.bh.platform.i18n.ITranslator;
 import org.bh.validation.ValidationRule;
 
 
 public class BHComboBox extends JComboBox implements IBHModelComponent, ActionListener, IPlatformListener {
 	private static final long serialVersionUID = 3609724364063209645L;
 	private static final ITranslator translator = Services.getTranslator();
 	private String key;
 	private boolean sorted = false;
 	private Item[] items = new Item[0];
 	private final CompValueChangeManager valueChangeManager = new CompValueChangeManager();
 
 	public BHComboBox(Object key) {
 		this.key = key.toString();
 		addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				valueChangeManager.fireCompValueChangeEvent(BHComboBox.this);
 			}
 		});
 	}
 
 	public void setValueList(Item[] items) {
 		this.items = items;
 		if (sorted) {
 			sortItems();
 		}
 		setModel(new DefaultComboBoxModel(this.items));
 		updateUI();
 	}
 
 	@Override
 	public IValue getValue() {
 		return ((Item) this.getSelectedItem()).getValue();
 	}
 
 	@Override
 	public void setValue(IValue value) {
		if (value == null) {
			this.setSelectedIndex(0);
 			return;
		}
 		
 		for (int i = 0; i < this.getItemCount(); i++) {
 			Item item = (Item) this.getItemAt(i);
 			if (value.equals(item.getValue())) {
 				this.setSelectedIndex(i);
 				return;
 			}
 		}
 	}
 
 	@Override
 	public String getBHHint() {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException(
 				"This method has not been implemented");
 	}
 
 	@Override
 	public String getKey() {
 		return key;
 	}
 
 	protected void reloadText() {
 		this.updateUI();
 	}
 
 	@Override
 	public void platformEvent(PlatformEvent e) {
 		if (e.getEventType() == Type.LOCALE_CHANGED) {
 			reloadText();
 		}
 	}
 
 	@Override
 	public ValidationRule[] getValidationRules() {
 		return new ValidationRule[0];
 	}
 
 	@Override
 	public void setValidationRules(ValidationRule[] validationRules) {
 		// nothing to do
 	}
 
 	public void setSorted(boolean sorted) {
 		this.sorted = sorted;
 		if (sorted) {
 			sortItems();
 		}
 	}
 
 	public boolean isSorted() {
 		return sorted;
 	}
 
 	public void sortItems() {
 		Arrays.sort(items, new Comparator<Item>() {
 			@Override
 			public int compare(Item o1, Item o2) {
 				return o1.toString().compareTo(o2.toString());
 			}
 		});
 	}
 
 	public static class Item {
 		private String key;
 		private IValue value;
 
 		public Item(String key, IValue value) {
 			this.key = key;
 			this.value = value;
 		}
 
 		@Override
 		public String toString() {
 			return translator.translate(key);
 		}
 		
 		public String getKey() {
 			return key;
 		}
 
 		public IValue getValue() {
 			return value;
 		}
 	}
 
 	@Override
 	public CompValueChangeManager getValueChangeManager() {
 		return valueChangeManager;
 	}
 }
