 package gsingh.learnkirtan.ui.shabadeditor.tableeditor;
 
 import javax.swing.event.UndoableEditEvent;
 import javax.swing.event.UndoableEditListener;
 import javax.swing.table.DefaultTableModel;
 
 public class UndoTableModel extends DefaultTableModel {
 
 	public void setValueAt(Object value, int row, int column, boolean undoable) {
 		UndoableEditListener listeners[] = getListeners(UndoableEditListener.class);
 
 		if (undoable == false || listeners == null) {
 			super.setValueAt(value, row, column);
 			return;
 		}
 
 		Object oldValue = getValueAt(row, column);

		if (oldValue == null && value.equals("")) {
			return;
		}

 		super.setValueAt(value, row, column);
 		CellEdit cellEdit = new CellEdit(this, oldValue, value, row, column);
 		UndoableEditEvent editEvent = new UndoableEditEvent(this, cellEdit);
 		for (UndoableEditListener listener : listeners)
 			listener.undoableEditHappened(editEvent);
 	}
 
 	@Override
 	public void setValueAt(Object value, int row, int column) {
 		setValueAt(value, row, column, true);
 	}
 
 	public void addUndoableEditListener(UndoableEditListener listener) {
 		listenerList.add(UndoableEditListener.class, listener);
 	}
 }
