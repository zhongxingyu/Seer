 /*******************************************************************************
  * Copyright 2011: Matthias Beste, Hannes Bischoff, Lisa Doerner, Victor Guettler, Markus Hattenbach, Tim Herzenstiel, Günter Hesse, Jochen Hülß, Daniel Krauth, Lukas Lochner, Mark Maltring, Sven Mayer, Benedikt Nees, Alexandre Pereira, Patrick Pfaff, Yannick Rödl, Denis Roster, Sebastian Schumacher, Norman Vogel, Simon Weber 
  *
  * Copyright 2010: Anna Aichinger, Damian Berle, Patrick Dahl, Lisa Engelmann, Patrick Groß, Irene Ihl, Timo Klein, Alena Lang, Miriam Leuthold, Lukas Maciolek, Patrick Maisel, Vito Masiello, Moritz Olf, Ruben Reichle, Alexander Rupp, Daniel Schäfer, Simon Waldraff, Matthias Wurdig, Andreas Wußler
  *
  * Copyright 2009: Manuel Bross, Simon Drees, Marco Hammel, Patrick Heinz, Marcel Hockenberger, Marcus Katzor, Edgar Kauz, Anton Kharitonov, Sarah Kuhn, Michael Löckelt, Heiko Metzger, Jacqueline Missikewitz, Marcel Mrose, Steffen Nees, Alexander Roth, Sebastian Scharfenberger, Carsten Scheunemann, Dave Schikora, Alexander Schmalzhaf, Florian Schultze, Klaus Thiele, Patrick Tietze, Robert Vollmer, Norman Weisenburger, Lars Zuckschwerdt
  *
  * Copyright 2008: Camil Bartetzko, Tobias Bierer, Lukas Bretschneider, Johannes Gilbert, Daniel Huser, Christopher Kurschat, Dominik Pfauntsch, Sandra Rath, Daniel Weber
  *
  * This program is free software: you can redistribute it and/or modify it un-der the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FIT-NESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *******************************************************************************/
 package org.bh.gui.swing.comp;
 
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JComboBox;
 import javax.swing.JList;
 import javax.swing.plaf.basic.BasicComboBoxRenderer;
 
import org.bh.data.DTOBranch;
import org.bh.data.DTOBusinessData;
 import org.bh.data.types.IValue;
 import org.bh.gui.CompValueChangeManager;
 import org.bh.gui.IBHModelComponent;
 import org.bh.platform.IPlatformListener;
import org.bh.platform.PlatformController;
 import org.bh.platform.PlatformEvent;
 import org.bh.platform.Services;
 import org.bh.platform.PlatformEvent.Type;
 import org.bh.platform.i18n.ITranslator;
 import org.bh.validation.ValidationRule;
 
 public class BHComboBox extends JComboBox implements IBHModelComponent,
 		ActionListener, IPlatformListener {
 	private static final long serialVersionUID = 3609724364063209645L;
 	static final ITranslator translator = Services.getTranslator();
 	private String key;
 	private String hint;
 	private int lastSelectedIndex = 0;
 	private boolean sorted = false;
 	private Item[] items = new Item[0];
 	final CompValueChangeManager valueChangeManager = new CompValueChangeManager();
 	boolean changeListenerEnabled = true;
 
 	public BHComboBox(Object key, Object[] tooltips) {
 		this.key = key.toString();
 		reloadText();
 		Services.addPlatformListener(this);
 		this.setRenderer(new BHBoxRenderer(tooltips));
 		addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (changeListenerEnabled)
 					valueChangeManager
 							.fireCompValueChangeEvent(BHComboBox.this);
 				
 			}
 			
 		});
 		
 	}
 	public BHComboBox(Object key) {
 		this.key = key.toString();
 		reloadText();
 		Services.addPlatformListener(this);
 		addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (changeListenerEnabled)
 					valueChangeManager
 							.fireCompValueChangeEvent(BHComboBox.this);
 			}
 		});
 	}
 
 	/**
 	 * Use this method to set all available items in the ComboBox.
 	 * We can select entries afterwards via their key or index.
 	 * @param items All available items.
 	 */
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
 			// at this point, it is necessary to trigger a change event because
 			// the first item seems to be selected, but in fact is not saved in
 			// the DTO
 			if (this.getItemCount() > 0)
 				this.setSelectedIndex(0);
 			return;
 		}
 
 		for (int i = 0; i < this.getItemCount(); i++) {
 			Item item = (Item) this.getItemAt(i);
 			if (value.equals(item.getValue())) {
 				changeListenerEnabled = false;
 				this.setSelectedIndex(i);
 				changeListenerEnabled = true;
 				return;
 			}
 		}
 	}
 
 	@Override
 	public String getHint() {
 		return hint;
 	}
 
 	@Override
 	public String getKey() {
 		return key;
 	}
 
 	/**
 	 * Reloads the texts in the combo box. In case the language is changed.
 	 */
 	protected void reloadText() {
 		hint = Services.getTranslator().translate(key, ITranslator.LONG);
 		setToolTipText(hint);
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
			DTOBusinessData businessData = (PlatformController.getInstance()).getBusinessDataDTO();
			
			List<DTOBranch> branchList = businessData.getChildren();
			
			String key1;
			String main;
			String mid;
			String sub;
			
			Iterator<DTOBranch> itr = branchList.iterator();
			while(itr.hasNext()) {
	    		DTOBranch currBranch = itr.next();
	    		main = currBranch.get(DTOBranch.Key.BRANCH_KEY_MAIN_CATEGORY).toString();
	    		mid = currBranch.get(DTOBranch.Key.BRANCH_KEY_MID_CATEGORY).toString();
	    		sub = currBranch.get(DTOBranch.Key.BRANCH_KEY_SUB_CATEGORY).toString();
	    		key1 = main + "." + mid + "." + sub;
	    		if(key1.equals(key)){
	    			return translator.translate(key) + " (" +currBranch.getChildrenSize() + ")";
	    		}
	    		
			}
			
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
 	
 	public int getLastSelectedIndex() {
 		return lastSelectedIndex;
 	}
 
 	@Override
 	public void setSelectedIndex(int arg0) {
 		lastSelectedIndex = getSelectedIndex();
 		super.setSelectedIndex(arg0);
 	}
 
 	@Override
 	public void setSelectedItem(Object arg0) {
 		lastSelectedIndex = getSelectedIndex();
 		super.setSelectedItem(arg0);
 	}
 
 	
 }
 class BHBoxRenderer extends BasicComboBoxRenderer {
 	/**
 	 * Comment for <code>serialVersionUID</code>
 	 */
 	private static final long serialVersionUID = 4422058255837522856L;
 	Object[] tooltip;
 	public BHBoxRenderer(Object[] tooltip){
 		this.tooltip = tooltip;
 	}
     public Component getListCellRendererComponent(JList list, Object value,
         int index, boolean isSelected, boolean cellHasFocus) {
       if (isSelected) {
         setBackground(list.getSelectionBackground());
         setForeground(list.getSelectionForeground());
         if (index > -1) {
         	String text = Services.getTranslator().translate(tooltip[index], ITranslator.LONG);
           list.setToolTipText(text);
           
         }
       } else {
         setBackground(list.getBackground());
         setForeground(list.getForeground());
       }
       setFont(list.getFont());
       setText((value == null) ? "" : value.toString());
       return this;
     }
   }
 
