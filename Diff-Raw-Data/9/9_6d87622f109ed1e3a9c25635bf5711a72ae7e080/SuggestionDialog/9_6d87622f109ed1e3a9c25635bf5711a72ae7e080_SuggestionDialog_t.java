 package org.eclipse.emf.refactor.smells.eraser.ui;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.ENamedElement;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EOperation;
 import org.eclipse.emf.refactor.refactoring.core.Refactoring;
 import org.eclipse.emf.refactor.smells.core.ModelSmell;
 import org.eclipse.emf.refactor.smells.eraser.managers.EraseManager;
 import org.eclipse.emf.refactor.smells.eraser.utils.SetSorter;
 import org.eclipse.emf.refactor.smells.runtime.core.EObjectGroup;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.TableEditor;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 
 /**
  * Dialog class for providing the user with suggestions for applicable refactorings based on a given
  * smell occurence.
  * 
  * @author Matthias Burhenne
  *
  */
 
 public class SuggestionDialog extends Dialog {
 	
 	private static final String APPLICABLE_REFACTORINGS_TAB_LABEL = "Applicable Refactorings";
 	private static final String SUGGESTED_REFACTORINGS_TAB_LABEL = "Suggested Refactorings";
 	private static final String CONTEXT_OBJECTS = "contextObjects";
 	private static final String SELECTED_CONTEXT_OBJECT = "selectedContextObject";
 	private static final String POSSIBLE_SMELLS_COLUMNL_LABEL = "Possible Smells";
 	private static final String DESCRIPTION_COLUMN_LABEL = "Description";
 	private static final String CONTEXT_COLUMN_LABEL = "Context";
 	private static final String REFACTORING_NAME_COLUMN_LABEL = "Refactoring";
 	
 	private static final String NO_MODEL_ELEMENT_SELECTED_ERROR_MESSAGE = "No model element was selected. In order to perform the\n" + 
 																			"selected refactoring a context object is needed.\n" +
 																			"Please select it via the combo box in column " + CONTEXT_COLUMN_LABEL + "!";
 	private static final String NO_REFACTORING_SELECTED_ERROR_MESSAGE = "No refactoring was selected. Please select a refactoring\n" +
 																			"by selecting one of the table rows!";
 	private static final String NO_CONTEXT_ELEMENT_SELECTED_ERROR_TITLE = "No model element selected";
 	private static final String NO_REFACTORING_SELECTED_ERROR_MESSAGE_TITLE = "No refactoring selected";
 	
 	private static final int CONTEXT_COLUMN_INDEX = 1;
 	private static final String REFACTORING_TABLE_ITEM_DATA = "refactoring";
 	private static final String CONTEXT_OBJECTS_COMBO = "contextObjectsCombo";
 	private final Map<Refactoring, Set<ModelSmell>> relationMap;
 	private final EObjectGroup eObjects;
 	private final Map<Refactoring, Set<EObject>> dynamicallyCalculatedRefactorings;
 	
 	private Refactoring selectedRefactoring = null;
 	private EObject selectedContextObject = null;
 	private TableItem selectedItem = null;
 	private Table staticTable;
 	private Table dynamicTable;
 	
 	public SuggestionDialog(Shell parentShell, Map<Refactoring, Set<ModelSmell>> relationMap, EObjectGroup eObjects) {
 		super(parentShell);
 		this.relationMap = relationMap;
 		this.eObjects = eObjects;
 		this.dynamicallyCalculatedRefactorings = EraseManager.getApplicableRefactoringsDynamically(eObjects);
 		
 	}
 	
 	protected void configureShell(Shell shell) {
 		super.configureShell(shell);
 	    shell.setText("Refactoring Quick Fix Dialog");
 	}
 	
 	@Override
 	protected Control createContents(Composite parent) {
 		
 		parent.setLayout(new GridLayout(1, true));
 		
 		
 		TabFolder tabs = new TabFolder(parent, SWT.NULL);
 		GridData gridData = new GridData(GridData.FILL_BOTH);
 		gridData.horizontalAlignment = GridData.FILL;
 		tabs.setLayoutData(gridData);
 		
 		TabItem staticDefinitionsTab = new TabItem(tabs, SWT.NULL);
 		staticDefinitionsTab.setText(SUGGESTED_REFACTORINGS_TAB_LABEL);
 		Composite staticComposite = new Composite(tabs, SWT.NULL);
 		staticComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		staticDefinitionsTab.setControl(staticComposite);
 		GridLayout staticLayout = new GridLayout();
 		staticComposite.setLayout(staticLayout);
 		
 		staticTable = new Table(staticComposite, SWT.SINGLE
 				| SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL);
 		createRelationTableColumns(staticTable);
 		
 		try {
 			fillStaticRelationsTable(staticTable);
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		
 		staticTable.setHeaderVisible(true);
 		staticTable.setLinesVisible(true);
 		
 		TabItem dynamicRelationsTab = new TabItem(tabs, SWT.NULL);
 		dynamicRelationsTab.setText(APPLICABLE_REFACTORINGS_TAB_LABEL);
 		Composite dynamicComposite = new Composite(tabs, SWT.NULL);
 		dynamicComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		dynamicRelationsTab.setControl(dynamicComposite);
 		GridLayout dynamicLayout = new GridLayout();
 		dynamicComposite.setLayout(dynamicLayout);
 		
 		dynamicTable = new Table(dynamicComposite, SWT.SINGLE
 				| SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL);
 		createRelationTableColumns(dynamicTable);
 		
 		fillDynamicRelationsTable(dynamicallyCalculatedRefactorings, dynamicTable);
 		
 		dynamicTable.setHeaderVisible(true);
 		dynamicTable.setLinesVisible(true);
 		
 		return super.createContents(parent);
 	}
 	
 	/*
 	 * fills the table contained in the tab for applicable refactorings
 	 */
 	private void fillDynamicRelationsTable(
 			Map<Refactoring, Set<EObject>> dynamicMap,
 			Table dynamicTable) {
 		for(Refactoring refactoring : SetSorter.sortRefactoringSet(dynamicMap.keySet())){
 				String causedSmells = buildCausedSmellsTableEntry(refactoring);
 				TableItem tableItem = new TableItem(dynamicTable, SWT.LEFT);
 				tableItem.setText(0, refactoring.getName());
 				tableItem.setText(1, refactoring.getNamespaceUri());
 				tableItem.setText(2, refactoring.getId());
 				tableItem.setText(3, causedSmells);
 
 				Combo contextObjectsCombo = new Combo(dynamicTable, SWT.LEFT);
 				for(EObject contextObject : dynamicMap.get(refactoring)){
 					String label = getEObjectLabel(contextObject);
 					contextObjectsCombo.add(label);
 					contextObjectsCombo.setData(label, contextObject);
 				}
 				tableItem.setData(REFACTORING_TABLE_ITEM_DATA, refactoring);
 				tableItem.setData(CONTEXT_OBJECTS_COMBO, contextObjectsCombo);
 				
 				ArrayList<EObject> contextObjects = new ArrayList<EObject>();
 				contextObjects.addAll(dynamicMap.get(refactoring));
 				tableItem.setData(CONTEXT_OBJECTS, contextObjects);
 		}
 	}
 
 	/*
 	 * Fills the table for refactorings suggested in the tab containing the suggested refactorings
 	 */
 	private void fillStaticRelationsTable(Table table) throws ClassNotFoundException {
 		Refactoring[] refactorings = {};
 		refactorings = relationMap.keySet().toArray(refactorings);
 		Arrays.sort(refactorings);
 		for(Refactoring refactoring : refactorings){
 			String smells = buildCausedSmellsTableEntry(refactoring);
 			ArrayList<EObject> contextObjects = new ArrayList<EObject>();
 			for(EObject eObject : eObjects.getEObjects()){
 				if(EraseManager.passesInitialCheck(eObject, refactoring)){
 					contextObjects.add(eObject);
 				}
 			}		
 			TableItem tableItem = new TableItem(table, SWT.LEFT);
 			tableItem.setText(0, refactoring.getName());
 			tableItem.setText(1, refactoring.getNamespaceUri());
 			tableItem.setText(2, refactoring.getId());
 			tableItem.setText(3, smells);
 					
 			
 			Combo contextObjectsCombo = new Combo(table, SWT.LEFT);
 			for(EObject contextObject : contextObjects){
 				String label = getEObjectLabel(contextObject);
 				contextObjectsCombo.add(label);
 				contextObjectsCombo.setData(label, contextObject);
 			}
 			tableItem.setData(REFACTORING_TABLE_ITEM_DATA, refactoring);
 			tableItem.setData(CONTEXT_OBJECTS_COMBO, contextObjectsCombo);
 			tableItem.setData(CONTEXT_OBJECTS, contextObjects);
 		}
 		
 	}
 
 	/*
 	 * Builds a String for the table column containing the names of possibly caused smells
 	 */
 	private String buildCausedSmellsTableEntry(Refactoring refactoring) {
 		String smells = "";
 		
 		Set<ModelSmell> currentSet = EraseManager.getCausedModelSmells(refactoring);
 		if(currentSet != null){
 			for(ModelSmell smell : currentSet){
 				smells += smell.getName() + ", ";
 			}
 			if(smells.endsWith(", ") && smells.length() > 2){
 				smells = smells.substring(0, smells.length()-2);
 			}
 		}
 		return smells;
 	}
 
 	/*
 	 * Adds the columns to the tables inside the two tabs
 	 */
 	private void createRelationTableColumns(Table table) {
 		//Column 1:
 		TableColumn refactoringNameColumn = new TableColumn(table, SWT.LEFT);
 		refactoringNameColumn.setText(REFACTORING_NAME_COLUMN_LABEL);
 		refactoringNameColumn.setWidth(250);
 		
 		//Column 2:
 		TableColumn refactoringContextColumn = new TableColumn(table, SWT.LEFT);
 		refactoringContextColumn.setText(CONTEXT_COLUMN_LABEL);
 		refactoringContextColumn.setWidth(150);
 		
 		//Column 3:
 		TableColumn refactoringDescriptionColumn = new TableColumn(table, SWT.LEFT);
 		refactoringDescriptionColumn.setText(DESCRIPTION_COLUMN_LABEL);
 		refactoringDescriptionColumn.setWidth(300);
 		
 		//Column 4:
 		TableColumn causedSmellsColumn = new TableColumn(table, SWT.LEFT);
 		causedSmellsColumn.setText(POSSIBLE_SMELLS_COLUMNL_LABEL);
 		causedSmellsColumn.setWidth(300);
 		
 		final TableEditor tableEditor = new TableEditor(table);
 		final Table editorTable = table;
 		tableEditor.horizontalAlignment = SWT.LEFT;
 		tableEditor.grabHorizontal = true;
 		tableEditor.minimumWidth = refactoringContextColumn.getWidth();
 		
 		table.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				TableItem item = (TableItem) e.item;
 			    if (item == null)
 			       	return;
 			    String oldSelectedText = item.getText(CONTEXT_COLUMN_INDEX);
 			    Combo oldEditor = (Combo)tableEditor.getEditor();
 			    if(oldEditor != null && selectedItem != null){
 			    	String oldLabel = oldEditor.getText();
 				    selectedItem.setText(CONTEXT_COLUMN_INDEX, oldLabel);
 			    	oldEditor.dispose();
 			    }
 			    selectedItem = item;
 			    Combo newEditor = new Combo(editorTable, SWT.NONE);
 			    @SuppressWarnings("unchecked")
 				List<EObject> contextObjects = (List<EObject>)item.getData(CONTEXT_OBJECTS);
 			    for(EObject contextObject : contextObjects){
 			    	String label = getEObjectLabel(contextObject);
 			    	newEditor.setData(label, contextObject);
 			    	newEditor.add(label);
 			    }
 			    for(int i = 0; i < newEditor.getItemCount(); i++){
 			    	if(newEditor.getItem(i).equals(oldSelectedText)){
 			    		newEditor.select(i);
 			    		selectedItem.setData(SELECTED_CONTEXT_OBJECT, newEditor.getData("" + i));
 			    	}
 			    }
 			    newEditor.addModifyListener(new ModifyListener() {
 					
 					@Override
 					public void modifyText(ModifyEvent e) {
 						Combo me = (Combo)e.widget;
 						int index = me.getSelectionIndex();
 						String key = me.getItem(index);
 						EObject contextObject = (EObject)me.getData(key);
 						selectedItem.setData(SELECTED_CONTEXT_OBJECT, contextObject);		
 					}
 				});
 			    newEditor.setFocus();
 			    tableEditor.setEditor(newEditor, item, CONTEXT_COLUMN_INDEX);
 			}
 		});		
 	}
 	
 	/**
 	 * Getter for the selected refactoring. To be used after okPressed() has been called.
 	 * Will return null beforehand.
 	 * @return - the Refactoring selected by the user or null
 	 */
 	public Refactoring getSelectedRefactoring(){
 		return selectedRefactoring;
 	}
 	
 	/**
 	 * Getter for the selected context object. To be used after okPressed() has been called.
 	 * Will return null beforehand.
 	 * @return - the EObject selected by the user or null
 	 */
 	public EObject getSelectedContextObject(){
 		return selectedContextObject;
 	}
 
 	@Override
 	protected void okPressed() {
 		try{
 			if(selectedItem != null){
 				selectedContextObject = (EObject) selectedItem.getData(SELECTED_CONTEXT_OBJECT); //selectedEObject;
 				selectedRefactoring = (Refactoring)selectedItem.getData(REFACTORING_TABLE_ITEM_DATA);
 			}
 		}catch(NullPointerException ex){
 			selectedContextObject = null;
 			selectedRefactoring = null;
 		}
 		
 		if(selectedRefactoring == null){
 			org.eclipse.jface.dialogs.
 			MessageDialog.openError(
 				getShell(),
 				NO_REFACTORING_SELECTED_ERROR_MESSAGE_TITLE,
 				NO_REFACTORING_SELECTED_ERROR_MESSAGE);
 			return;
 		}
 		
 		if(selectedContextObject == null){
 			org.eclipse.jface.dialogs.
 			MessageDialog.openError(
 				getShell(),
 				NO_CONTEXT_ELEMENT_SELECTED_ERROR_TITLE,
 				NO_MODEL_ELEMENT_SELECTED_ERROR_MESSAGE);
 			return;
 		}
 		super.okPressed();
 	}
 	
 	/*
 	 * Builds the label used in the ComboBoxes for selecting the context object.
 	 * The content of the label depends on the type of object and the available fields.
 	 */
 	private String getEObjectLabel(EObject eObject){
 		String name = null;
 		String label = null;
 		String id = null;
 		for (EAttribute attribute : eObject.eClass().getEAllAttributes()) {
			if (attribute.getName().equalsIgnoreCase("name")) {
 				name = (String) eObject.eGet(attribute);
 			}
 		}
 		for(EOperation operation : eObject.eClass().getEAllOperations()){
 			try{
 				if(operation.getName().equals("getName") && name == null)
 					name = (String)eObject.eInvoke(operation, null);
 				if(operation.getName().equals("getLabel") && label == null)
 					label = (String)eObject.eInvoke(operation, null);
 				if(operation.getName().equals("getID") && id == null)
 					id = (String)eObject.eInvoke(operation, null);
 				if(operation.getName().equals("getId") && id == null)
 					id = (String)eObject.eInvoke(operation, null);
 				} catch (Exception e) {
 				} 
 			}
 		
 		if(name != null && !name.isEmpty())
 			return name;
 		if(label != null && !label.isEmpty())
 			return label;
 		if(id != null && !id.isEmpty())
 			return id;
 		
 		if(eObject instanceof ENamedElement)
 			if(((ENamedElement) eObject).getName() != null && !((ENamedElement) eObject).getName().equals(""))
 				return ((ENamedElement)eObject).getName();
 			else
 				return eObject.toString();
 		else
 			return eObject.toString();
 		
 	}
 	
 	
 
 }
