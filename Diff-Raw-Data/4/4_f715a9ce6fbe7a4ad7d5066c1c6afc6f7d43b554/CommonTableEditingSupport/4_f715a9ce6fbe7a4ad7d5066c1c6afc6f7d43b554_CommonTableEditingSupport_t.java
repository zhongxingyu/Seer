 package de.ptb.epics.eve.viewer.views.deviceinspectorview;
 
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.ComboBoxCellEditor;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
 
 /**
  * <code>CommonTableEditingSupport</code> is the 
  * {@link org.eclipse.jface.viewers.EditingSupport} for the table viewers 
  * defined in 
  * {@link de.ptb.epics.eve.viewer.views.deviceinspectorview.DeviceInspectorView}.
  * 
  * @author ?
  * @author Marcus Michalsky
  */
 public class CommonTableEditingSupport extends EditingSupport {
 	
 	// the table viewer the editing support belongs to
 	private TableViewer viewer;
 	
 	// the column name
 	private String column;
 	
 	/**
 	 * Constructs a <code>CommonTableEditingSupport</code>.
 	 * 
 	 * @param viewer the table viewer the editing support is based on
 	 * @param column the column of the table the support is based on
 	 */
 	public CommonTableEditingSupport(TableViewer viewer, String column) {
 		super(viewer);
 		this.viewer = viewer;
 		this.column = column;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected boolean canEdit(Object element) {
 		// TODO do not execute stuff in here
 		// this function should only determine if a column is editable
 		if (column.equals("remove")) {
 			((CommonTableContentProvider)viewer.getInput()).removeElement(element);
 		} else if (column.equals("trigger")) {
 			((CommonTableElement) element).trigger();
 		} else if (column.equals("stop")) {
 			((CommonTableElement) element).stop();
 		} else if (column.equals("tweakforward")) {
 			((CommonTableElement) element).tweak(true);
 		} else if (column.equals("tweakreverse")) {
 			((CommonTableElement) element).tweak(false);
 		} else {
 			CommonTableElement ctb = (CommonTableElement) element;
 			if (!ctb.isReadonly(column) && ctb.isConnected(column))return true;
 		}
 		return false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected CellEditor getCellEditor(Object element) {
 		CommonTableElement ctb = (CommonTableElement) element;
 		
 		if (ctb.getCellEditor(column) == null) {
 			if (ctb.isDiscrete(column)) {
 				ctb.setCellEditor(new ComboBoxCellEditor(viewer.getTable(), 
						ctb.getSelectStrings(column), SWT.READ_ONLY) {
 					@Override protected void focusLost() {
 						if(isActivated()) {
 							fireCancelEditor();
 						}
 						deactivate();
 					}
 				}, column);
 			} else {
 				TextCellEditor textCellEditor = 
 						new TextCellEditor(viewer.getTable()) {
 					@Override protected void focusLost() {
 						if (isActivated()) {
 							fireCancelEditor();
 						}
 						deactivate();
 					}
 				};
 				ctb.setCellEditor(textCellEditor, column);
 			}
 		}
 		return ctb.getCellEditor(column);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected Object getValue(Object element) {
 		CommonTableElement ctb = (CommonTableElement) element;
 		CellEditor ceditor = ctb.getCellEditor(column);
 		if (ceditor instanceof ComboBoxCellEditor) {
 			int count = 0;
 			String currentVal = ctb.getValue(column);
 			for (String selection : ctb.getSelectStrings(column)) {
 				if (selection.startsWith(currentVal)) return count;
 				++count;
 			}
 			return 0;
 		}
 		return ctb.getValue(column);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void setValue(Object element, Object value) {
 		CommonTableElement ctb = (CommonTableElement) element;
 		ctb.setValue(value, column);
 	}
 }
