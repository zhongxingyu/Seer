 /*******************************************************************************
  * Copyright (c) 2011 University of Illinois at Urbana-Champaign
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *  
  * Contributors: 
  * University of Illinois at Urbana-Champaign - initial API and implementation
  *******************************************************************************/
 package edu.illinois.parallelism.openmp.refactoring.padscalarvariables;
 
 import java.util.List;
 
 import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
 import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.CheckStateChangedEvent;
 import org.eclipse.jface.viewers.CheckboxTableViewer;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.jface.viewers.ICellModifier;
 import org.eclipse.jface.viewers.ICheckStateListener;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.TableLayout;
 import org.eclipse.jface.viewers.TextCellEditor;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.events.VerifyListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 
 import edu.illinois.parallelism.openmp.refactoring.padscalarvariables.PadScalarVariablesRefactoring.VariableToPadTuple;
 
 /**
  * Creates a simple input page with tables to allow the user to select which
  * variables to pad and to change their padding size.
  * 
  * @author nchen
  * 
  */
 @SuppressWarnings("restriction")
 public class PadScalarVariablesUserInputWizardPage extends UserInputWizardPage {
 
 	private final PadScalarVariablesRefactoring padScalarRefactoring;
 	private CheckboxTableViewer tableViewer;
 	private Table table;
 	private PadScalarVariableContentProvider tableContentProvider;
 
 	public PadScalarVariablesUserInputWizardPage(PadScalarVariablesRefactoring padScalarRefactoring) {
 		super(Messages.PadScalarVariablesUserInputWizardPage_wizardPageTitle);
 		this.padScalarRefactoring = padScalarRefactoring;
 		setPageComplete(false); // Cannot proceed until selection is made
 	}
 
 	private void createButtonComposite(Composite comp) {
 		final Composite btComp = new Composite(comp, SWT.NONE);
 		final FillLayout layout = new FillLayout(SWT.VERTICAL);
 		layout.spacing = 4;
 		btComp.setLayout(layout);
 
 		final Button selectAll = new Button(btComp, SWT.PUSH);
 		selectAll.setText(Messages.PadScalarVariablesUserInputWizardPage_selectAllButtonLabel);
 		selectAll.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				for (final VariableToPadTuple variable : padScalarRefactoring.getVariablesToPad()) {
 					tableViewer.setChecked(variable, true);
 				}
 			}
 
 		});
 
 		final Button deselectAll = new Button(btComp, SWT.PUSH);
 		deselectAll.setText(Messages.PadScalarVariablesUserInputWizardPage_deselectAllButtonLabel);
 		deselectAll.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				for (final VariableToPadTuple variable : padScalarRefactoring.getVariablesToPad()) {
 					tableViewer.setChecked(variable, false);
 				}
 			}
 
 		});
 		GridData gridData = new GridData();
 		gridData.verticalAlignment = SWT.TOP;
 		btComp.setLayoutData(gridData);
 	}
 
 	@Override
 	public void createControl(Composite parent) {
 		final Composite composite = new Composite(parent, SWT.NONE);
 
 		setTitle(Messages.PadScalarVariablesUserInputWizardPage_dialogTitle);
 		setMessage(Messages.PadScalarVariablesUserInputWizardPage_dialogMessage);
 
 		composite.setLayout(new GridLayout(2, false));
 		createTable(composite);
 		createTableViewer();
 		tableViewer.setInput(padScalarRefactoring.getVariablesToPad());
 
 		createButtonComposite(composite);
 
 		setControl(composite);
 		resizeDialog();
 	}
 
 	private void resizeDialog() {
 		final int DIALOG_WIDTH = 600;
 		final int DIALOG_HEIGHT = 400;
 		getShell().setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
 		getShell().layout(true, true);
 	}
 
 	// Set the table column property names
 	private static final String VARIABLE_COLUMN = "Variable";
 	private static final String TYPE_COLUMN = "Type";
 	private static final String BYTE_COLUMN = "Bytes to pad";
 	private static final String SPACER_COLUMN = "";
 
 	// Set column names
 	private String[] columnNames = new String[] {
 			VARIABLE_COLUMN,
 			TYPE_COLUMN,
 			BYTE_COLUMN,
 			SPACER_COLUMN
 	};
 
 	private void createTable(Composite composite) {
 		TableLayout tableLayout = new TableLayout();
 		tableLayout.addColumnData(new ColumnWeightData(30, 50, true));
 		tableLayout.addColumnData(new ColumnWeightData(20, 40, true));
 		tableLayout.addColumnData(new ColumnWeightData(20, 40, true));
 		tableLayout.addColumnData(new ColumnWeightData(10, 10, true));
 
 		table = new Table(composite, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL |
 				SWT.FULL_SELECTION | SWT.CHECK);
 		table.setLayout(tableLayout);
 		table.setLinesVisible(true);
 		table.setHeaderVisible(true);
 
 		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
 		data.heightHint = 100;
 		table.setLayoutData(data);
 
 		TableColumn column = new TableColumn(table, SWT.LEFT, 0);
 		column.setText(VARIABLE_COLUMN);
 		column = new TableColumn(table, SWT.RIGHT, 1);
 		column.setText(TYPE_COLUMN);
 		column = new TableColumn(table, SWT.RIGHT, 2);
 		column.setText(BYTE_COLUMN);
 		column = new TableColumn(table, SWT.RIGHT, 3);
 		column.setText(SPACER_COLUMN);
 
 	}
 
 	private void createTableViewer() {
 		tableViewer = new CheckboxTableViewer(table);
 		tableViewer.setUseHashlookup(true);
 		tableViewer.setColumnProperties(columnNames);
 
 		tableContentProvider = new PadScalarVariableContentProvider();
 		tableViewer.setContentProvider(tableContentProvider);
 		tableViewer.setLabelProvider(new PadScalarVariableLabelProvider());
 		tableViewer.addCheckStateListener(new ICheckStateListener() {
 
 			@Override
 			public void checkStateChanged(CheckStateChangedEvent event) {
 				VariableToPadTuple tuple = (VariableToPadTuple) event.getElement();
 				tuple.setShouldPad(event.getChecked());
 
 				updateNavigation();
 			}
 		});
 
 		tableViewer.setCellEditors(setUpCellEditors());
 		tableViewer.setCellModifier(new BytesToPadCellModifier());
 	}
 
 	private CellEditor[] setUpCellEditors() {
 		// We have 3 real columns and one "pseudo" column for spacing
 		CellEditor[] editors = new CellEditor[columnNames.length];
 		editors[0] = editors[1] = editors[3] = null;
 
 		TextCellEditor byteEditor = new TextCellEditor(table);
 		((Text) byteEditor.getControl()).addVerifyListener(
 
 				new VerifyListener() {
 					public void verifyText(VerifyEvent e) {
 						e.doit = e.text.matches("[1-9][0-9]*");
 					}
 				});
 		editors[2] = byteEditor;
 		return editors;
 	}
 
 	private final class BytesToPadCellModifier implements ICellModifier {
 
 		// These methods are called in this order. First, ask if we "canModify"
 		// the value. If we can, then proceed to "getValue". And if the user
 		// enters a valid text, then proceed to "modify" the old value.
 
 		@Override
 		public boolean canModify(Object element, String property) {
 			// Only allow modification in the bytes column
 			return property.equals(BYTE_COLUMN);
 		}
 
 		@Override
 		public Object getValue(Object element, String property) {
 			if (property.equals(BYTE_COLUMN)) {
 				VariableToPadTuple tuple = (VariableToPadTuple) element;
 				return (new Integer(tuple.getBytesToPad())).toString();
 			}
 			return null;
 		}
 
 		@Override
 		public void modify(Object element, String property, Object value) {
 			TableItem item = (TableItem) element;
 			VariableToPadTuple tuple = (VariableToPadTuple) item.getData();
 
 			if (property.equals(BYTE_COLUMN)) {
 				tuple.setBytesToPad(new Integer(value.toString()));
 			}
 
 			tableViewer.refresh(tuple);
 		}
 
 		// TODO: If a user makes some changes, then select/check that variable
 		// for padding
 	}
 
 	private final class PadScalarVariableContentProvider implements IStructuredContentProvider {
 
 		@Override
 		public Object[] getElements(Object inputElement) {
 			// The inputElement must be of type List<VariableToPadTuple>
 			if (inputElement instanceof List) {
 				final List<?> list = (List<?>) inputElement;
 				return list.toArray();
 			}
 			return null;
 		}
 
 		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		}
 
 		public void dispose() {
 		}
 
 	}
 
 	private final class PadScalarVariableLabelProvider implements ITableLabelProvider {
 
 		private static final int VARIABLE_NAME_COL = 0;
 		private static final int VARIABLE_TYPE_COL = 1;
 		private static final int VARIABLE_BYTES_COL = 2;
 
 		public Image getColumnImage(Object element, int columnIndex) {
 			if (columnIndex == VARIABLE_NAME_COL) {
 				if (element != null) {
 					return CElementImageProvider.getMethodImageDescriptor(ASTAccessVisibility.PUBLIC).createImage();
 				}
 			}
 			return null;
 		}
 
 		@Override
 		public String getColumnText(Object element, int columnIndex) {
 			VariableToPadTuple tuple = (VariableToPadTuple) element;
 			switch (columnIndex) {
 			case VARIABLE_NAME_COL:
 				return tuple.getName().toString();
 			case VARIABLE_TYPE_COL:
 				return "int";
 			case VARIABLE_BYTES_COL:
 				return new Integer(tuple.getBytesToPad()).toString();
 
 			default:
 				return "";
 			}
 		}
 
 		// The following methods are not applicable
 
 		@Override
 		public void addListener(ILabelProviderListener listener) {
 		}
 
 		@Override
 		public void dispose() {
 		}
 
 		@Override
 		public boolean isLabelProperty(Object element, String property) {
 			return false;
 		}
 
 		@Override
 		public void removeListener(ILabelProviderListener listener) {
 		}
 
 	}
 
 	private boolean checkForAtLeastOneSelection() {
 		boolean atLeastOneSelected = false;
 		for (final VariableToPadTuple variable : padScalarRefactoring.getVariablesToPad()) {
 			if (variable.getShouldPad()) {
 				atLeastOneSelected = true;
 				break;
 			}
 		}
 		return atLeastOneSelected;
 	}
 
 	private void updateNavigation() {
 		setPageComplete(checkForAtLeastOneSelection());
 		// TODO: This should not need to be called every time but it needs to be
 		// called at least once when the page has finished being laid out
 		getShell().layout(true, true);
 	}
 
 }
