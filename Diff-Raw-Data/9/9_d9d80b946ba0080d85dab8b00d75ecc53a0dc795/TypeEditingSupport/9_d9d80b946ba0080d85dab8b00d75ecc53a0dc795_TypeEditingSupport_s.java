 package org.dawnsci.slicing.component;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.dawnsci.common.widgets.celleditor.CComboCellEditor;
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.PlotType;
 import org.dawnsci.slicing.api.system.DimsData;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.ColumnViewer;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 class TypeEditingSupport extends EditingSupport {
 
 	private static final Logger logger = LoggerFactory.getLogger(TypeEditingSupport.class);
 	
 	private CComboCellEditor   typeEditor;
 	private SliceSystemImpl    system;
 
 	public TypeEditingSupport(final SliceSystemImpl system, ColumnViewer viewer) {
 		
 		super(viewer);
 		this.system = system;
 		
 		typeEditor = new CComboCellEditor(((TableViewer)viewer).getTable(), getAxisItems(), SWT.READ_ONLY) {
 			protected int getDoubleClickTimeout() {
 				return 0;
 			}	
 
 			public void activate() {
 				String[] items = getAxisItems();
 				if (!Arrays.equals(this.getCombo().getItems(), items)) {
 					this.getCombo().setItems(items);
 				}
 				super.activate();
 			}
 
 		};
 		final CCombo combo = typeEditor.getCombo();
 		combo.addModifyListener(new ModifyListener() {
 			@Override
 			public void modifyText(ModifyEvent e) {
 				
 				if (!typeEditor.isActivated()) return;
 				final String   value = combo.getText();
 				if ("".equals(value) || "(Slice)".equals(value)) {
 					typeEditor.applyEditorValueAndDeactivate(-1);
 					return; // Bit of a bodge
 				}
 				if ("(Range)".equals(value)) {
 					typeEditor.applyEditorValueAndDeactivate(DimsData.RANGE);
 					return; // Bit of a bodge
 				}
 			}
 		});
 	}
 
 	@Override
 	protected CellEditor getCellEditor(Object element) {			
 		return typeEditor;
 	}
 
 	@Override
 	protected boolean canEdit(Object element) {
 		return true;
 	}
 
 	@Override
 	protected Object getValue(Object element) {
 		final DimsData data = (DimsData)element;
 		int axis = data.getPlotAxis();
 		if (axis<0) {
 			final String[] items = typeEditor.getCombo().getItems();
 			axis = items.length-1; // (Slice)
 		}
 		return axis;
 	}
 
 	@Override
 	protected void setValue(Object element, Object value) {
 		final DimsData data  = (DimsData)((IStructuredSelection)getViewer().getSelection()).getFirstElement();
 		if (data==null) return;
 		int axis = (Integer)value;
 		final Enum sliceType = system.getSliceType();
 		if (sliceType==PlotType.XY && axis!=102) axis = axis>-1 ? 0 : -1;
 		if (axis==data.getPlotAxis()) return;
 		
 		data.setPlotAxis(axis);
 		system.updateAxesChoices();
 		system.update(data, false);
 		system.fireDimensionalListeners();
 	}	
 	
 
 	/**
 	 * TODO Get a better design for this by forcing the
 	 * sliceType to contain a method.
 	 * @return String[] of choices for drop down in table.
 	 */
 	@SuppressWarnings("unused")
 	protected String[] getAxisItems() {
 	
 		List<String> ret = new ArrayList<String>(3);
 
 		Enum sliceType = system.getSliceType();
 		try {
 			final Method dimCountMethod = sliceType.getClass().getMethod("getDimensions");
 			final int dimCount = (Integer)dimCountMethod.invoke(sliceType);
 		
 			switch(dimCount) {
 			case 1:
 				ret.add("X");
 				break;
 			case 2:
 				final IPlottingSystem psystem = system.getPlottingSystem();
				if (system.isReversedImage()) {
					ret.add("Y");
					ret.add("X");
				} else if (psystem!=null && psystem.getPlotType().is1D()) {
 					ret.add("X");
 					ret.add("Y (Many)");
 				} else {
 					ret.add("X");
 					ret.add("Y");
 				}
 				break;
 			case 3:
 				ret.add("X");
 				ret.add("Y");
 				ret.add("Z");
 				break;
 			}
 		} catch (Throwable ne) {
             logger.error("Cannot find the getDimensions method in "+sliceType.getClass());
 			ret.add("X");
 			ret.add("Y");
 			ret.add("Z");
 		}
 
 		final int rank = (system.getLazyDataset()!=null) ? system.getLazyDataset().getRank() : Integer.MAX_VALUE;
 		if (rank>ret.size()) ret.add("(Slice)");
 		if (system.isRangesAllowed()) ret.add("(Range)");
 		return ret.toArray(new String[ret.size()]);
 	}
 
 	public void updateChoices() {
 		typeEditor.setItems(getAxisItems());
 	}
 
 
 }
