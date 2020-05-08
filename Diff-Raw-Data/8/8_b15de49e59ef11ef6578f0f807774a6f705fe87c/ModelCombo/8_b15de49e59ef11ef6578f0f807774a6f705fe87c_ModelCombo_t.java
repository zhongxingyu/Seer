 package data;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.AbstractListModel;
 import javax.swing.ComboBoxModel;
 
 public class ModelCombo extends AbstractListModel implements ComboBoxModel
 {
 	private static final long serialVersionUID = 4011116690928828937L;
 
 	//List<String> list = new ArrayList<String>();
 	List<Object> list = new ArrayList<Object>();
 	Object sel = null;
 	
 	public void setList(List<Object> l) {
 		list = l;
 	}
 	
 	/*
 	public void add(String s) {
 		if (! list.contains(s)) {
 			list.add(s);
 		}
 	}
 	*/
 	
 	public void add(Object o) {
 		if (! list.contains(o)) {
 			list.add(o);
 		}
 	}
 
	public void remove(Object o) {
		list.remove(o);
	}

	public void removeAll(List<?> sl) {
		list.removeAll(sl);
	}
	
 	public Object getElementAt(int index) {
 		return list.get(index);
 	}
 
 	public int getSize() {
 		return list.size();
 	}
 
 	public Object getSelectedItem() {
 		return sel;
 	}
 
 	public void setSelectedItem(Object anItem) {
 		sel = anItem;
 	}
 }
