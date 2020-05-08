 package uk.co.jbothma.protege.protplug.gui;
 
 import javax.swing.event.EventListenerList;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.TableModel;
 
 import org.apache.commons.lang.NotImplementedException;
 
 import uk.co.jbothma.protege.protplug.Project;
 import uk.co.jbothma.protege.protplug.SubclassEvent;
 import uk.co.jbothma.protege.protplug.SubclassEventListener;
 
 public class SubclassRelationCandidateTableModel implements TableModel, SubclassEventListener {
 	private EventListenerList tableModelListenerList;
 	private Project project = null;
 
 	public SubclassRelationCandidateTableModel() {
 		tableModelListenerList = new EventListenerList();
 	}
 	
 	public void setProject(Project project) {
 		this.project = project;
 	}
 
 	@Override
	public void addTableModelListener(TableModelListener listener) {
		tableModelListenerList.add(TableModelListener.class, listener);
 	}
 
 	@Override
 	public Class<?> getColumnClass(int col) {
 		if (col == 0)
 			return String.class;
 		else if (col == 1)
 			return String.class;
 		else
 			throw new IndexOutOfBoundsException();
 	}
 
 	@Override
 	public int getColumnCount() {
 		return 2;
 	}
 
 	@Override
 	public String getColumnName(int col) {
 		if (col == 0)
 			return "Domain";
 		else if (col == 1)
 			return "Range";
 		else
 			throw new IndexOutOfBoundsException();
 	}
 
 	@Override
 	public int getRowCount() {
 		if (project == null)
 			return 0;
 		else
 			return project.getSubclassRelationCandidates().size();
 	}
 
 	@Override
 	public Object getValueAt(int rowIndex, int col) {
 		if (col == 0)
 			return project.getSubclassRelationCandidates().get(rowIndex).getDomain();
 		else if (col == 1)
 			return project.getSubclassRelationCandidates().get(rowIndex).getRange();
 		else
 			throw new IndexOutOfBoundsException();
 	}
 
 	@Override
 	public boolean isCellEditable(int arg0, int arg1) {
 		return false;
 	}
 
 	@Override
	public void removeTableModelListener(TableModelListener listener) {
		tableModelListenerList.remove(TableModelListener.class, listener);
 	}
 
 	@Override
 	public void setValueAt(Object arg0, int arg1, int arg2) {
 		throw new NotImplementedException();
 	}
 
 	@Override
 	public void myEventOccurred(SubclassEvent evt) {
 		Object[] listeners = tableModelListenerList.getListenerList();
 		for (int i = listeners.length-2; i>=0; i-=2) {
 	         if (listeners[i]==TableModelListener.class) {
 	        	 ((TableModelListener)listeners[i+1]).tableChanged(new TableModelEvent(this));
 	         }
 	     }
 	}
 }
