 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.ui.rcp.dialogs;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.TitleAreaDialog;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.ComboBoxCellEditor;
 import org.eclipse.jface.viewers.ICellModifier;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TextCellEditor;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jubula.client.core.model.IParamDescriptionPO;
 import org.eclipse.jubula.client.core.model.IParameterInterfacePO;
 import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
 import org.eclipse.jubula.client.ui.constants.IconConstants;
 import org.eclipse.jubula.client.ui.rcp.Plugin;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.jubula.client.ui.utils.ErrorHandlingUtil;
 import org.eclipse.jubula.client.ui.utils.LayoutUtil;
 import org.eclipse.jubula.toolkit.common.xml.businessprocess.ComponentBuilder;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.constants.TestDataConstants;
 import org.eclipse.jubula.tools.exception.Assert;
 import org.eclipse.jubula.tools.i18n.CompSystemI18n;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.FocusAdapter;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.events.VerifyListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 
 
 /**
  * @author BREDEX GmbH
  * @created Oct 25, 2007
  */
 @SuppressWarnings("synthetic-access")
 public abstract class AbstractEditParametersDialog extends TitleAreaDialog {
 
     /** 
      * Regex for param name input validation. This allows all letters and 
      * numbers as well as the empty string. It can be used for controlling the
      * input of a text field, but should not be used for final validation, as it
      * allows empty strings. 
      */
     private static final String PARAM_NAME_REGEX = "[a-zA-Z_0-9]*"; //$NON-NLS-1$
 
     /**
      * <code>DEFAULT_COLUMN_WIDTH</code>
      */
     private static final int DEFAULT_COLUMN_WIDTH = 200;
 
     /**
      * <code>INITIAL_SIZE</code> of this Dialog.
      */
     private static final int INITIAL_SIZE = 600;
 
     /** Name of the name-column */
     private static final String NAME_COL_NAME = 
         Messages.EditParametersDialogNameTableColumnName;
     
     /** Name of the type-column */
     private static final String TYPE_COL_NAME = 
         Messages.EditParametersDialogTypeTableColumnName;
     
     /** Constant for an empty Cell entry */
     private static final String EMPTY_ENTRY = StringUtils.EMPTY;
     
     /** Default name of a new added Parameter */
     private static final String DEFAULT_PARAM_NAME = 
         Messages.EditParametersDialogDefaultNewParameterName;
     
     /** Default type of a new added Parameter */
     private static final String DEFAULT_PARAM_TYPE = TestDataConstants.STR;
     
     /**
      * <code>ERROR_COLOR</code>
      */
     private static final int ERROR_COLOR = SWT.COLOR_RED;
     
     /** Constant for the index of the name column of the table */
     private static final int NAME_TABLE_COLUMN = 0;
     
     /** Constant for the index of the type column of the table */
     private static final int TYPE_TABLE_COLUMN = 1;
     
     /**
      * <code>TABLE_COLUMNS</code>
      */
     private static final String[] TABLE_COLUMNS = 
         new String[]{NAME_COL_NAME, TYPE_COL_NAME};
 
     /** The IParameterInterfacePO */
     private IParameterInterfacePO m_paramInterfaceObj;
     
     /** The inner data model of the parameters */
     private List<Parameter> m_parameters = new ArrayList<Parameter>();
     
     /** The TableViewer */
     private TableViewer m_paramTableViewer;
 
     /**
      * @param parentShell
      *            the parent.
      * @param paramIntObj
      *            parameter interface obj the IParameterInterfacePO.
      */
     public AbstractEditParametersDialog(Shell parentShell,
             IParameterInterfacePO paramIntObj) {
         super(parentShell);
         setParamInterfaceObj(paramIntObj);
     }
     
     /**
      * {@inheritDoc}
      */
     protected Point getInitialSize() {
         final Point initialSize = super.getInitialSize();
         initialSize.y = INITIAL_SIZE;
         return initialSize;
     }
     
     /**
      * @author BREDEX GmbH
      * @created Mar 17, 2008
      */
     protected final class SelectionBasedButtonEnabler implements
             ISelectionChangedListener, SelectionListener {
         /** <code>Button</code> */
         private final Button m_button;
 
         /** @param button a Button */
         private SelectionBasedButtonEnabler(Button button) {
             m_button = button;
         }
         
         /** {@inheritDoc} */
         public void selectionChanged(SelectionChangedEvent event) {
             handleSelection();
         }
         
         /** {@inheritDoc} */
         public void widgetDefaultSelected(SelectionEvent e) {
             handleSelection();
         }
 
         /** {@inheritDoc} */
         public void widgetSelected(SelectionEvent e) {
             handleSelection();
         }
         
         /**
          * handle the selection
          */
         private void handleSelection() {
             m_button.setEnabled(generalButtonEnablement()
                     && !getParamTableViewer().getSelection().isEmpty());
         }
     }
 
     /**
      * @return wheter all buttons add, delete, up, down shall be enabled on
      *         selection changed events
      */
     protected boolean generalButtonEnablement() {
         return true;
     }
     
     /**
      * ContentProvider of the ParameterTable
      * @author BREDEX GmbH
      * @created Oct 25, 2007
      */
     private static final class ParamTableContentProvider implements 
         IStructuredContentProvider {
 
         /** {@inheritDoc} */
         public Object[] getElements(Object inputElement) {
             return ((List)inputElement).toArray();
         }
 
         /** {@inheritDoc} */
         public void dispose() {
             // nothing
         }
 
         /** {@inheritDoc} */
         public void inputChanged(Viewer viewer, Object oldInput, 
             Object newInput) {
             // nothing
         }
     }
     
     /**
      * LabelProvider of the ParameterTable
      * 
      * @author BREDEX GmbH
      * @created Oct 25, 2007
      */
     private final class ParamTableLabelProvider implements 
         ITableLabelProvider {
 
         /** {@inheritDoc} */
         public Image getColumnImage(Object element, int columnIndex) {
             if (columnIndex == NAME_TABLE_COLUMN) {
                 final Parameter parameter = (Parameter)element;
                 if (EMPTY_ENTRY.equals(parameter.getName().trim())
                     || EMPTY_ENTRY.equals(parameter.getType().trim())
                     || isDuplicate(parameter)) {
                     
                     return IconConstants.ERROR_IMAGE;
                 } 
             }
             return null;
         }
 
         /** {@inheritDoc} */
         public String getColumnText(Object element, int columnIndex) {
             final Parameter parameter = (Parameter)element;
             final String text = getTableCellText(parameter, columnIndex);
             setCellBackground(parameter);
             return text;
         }
 
 
         /**
          * Sets the background of the cell of the given parameter.
          * @param parameter A Parameter.
          */
         private void setCellBackground(Parameter parameter) {
             
             final Table table = getParamTableViewer().getTable();
             Color rowColor = null; // null sets default color!
             if (EMPTY_ENTRY.equals(parameter.getName().trim())
                 || EMPTY_ENTRY.equals(parameter.getType().trim())
                 || isDuplicate(parameter)) {
                 
                 rowColor = table.getDisplay().getSystemColor(ERROR_COLOR);
             } 
             final Integer[] indices = getIndices(parameter);
             final int itemCount = table.getItemCount();
             for (int index : indices) {
                 if (index < itemCount) {
                     final TableItem item = table.getItem(index);
                     item.setBackground(NAME_TABLE_COLUMN, rowColor);   
                 }
             }
         }
 
         /** {@inheritDoc} */
         public void addListener(ILabelProviderListener listener) {
             // nothing
         }
 
         /** {@inheritDoc} */
         public void dispose() {
             // nothing
         }
 
         /** {@inheritDoc} */
         public boolean isLabelProperty(Object element, String property) {
             return false;
         }
 
         /** {@inheritDoc} */
         public void removeListener(ILabelProviderListener listener) {
             // nothing
         }
         
     }
     
     /**
      * @author BREDEX GmbH
      * @created Oct 26, 2007
      */
     private static final  class I18nComboBoxCellEditor 
         extends ComboBoxCellEditor {
         
         /** The model items (no I18N) */
         private String[] m_modelItems;
         
         /**
          * @param parent the parent control
          * @param items the list of strings for the combo box
          * @param style the style bits
          */
         public I18nComboBoxCellEditor(Composite parent, String[] items, 
             int style) {
             
             super(parent, items, style);
             m_modelItems = items;
             translateModelItems();
         }
 
         /**
          * translates the model items
          */
         protected void translateModelItems() {
             final int itemSize = m_modelItems.length;
             final String[] translatedItems = new String[itemSize];
             int i = 0;
             for (String item : m_modelItems) {                
                 final String translatedItem = CompSystemI18n.getString(item);
                 translatedItems[i] = translatedItem;
                 i++;
             }
             setItems(translatedItems);
         }
         
         /**
          * {@inheritDoc}
          * @return The non-I18N-Value of the current selection.
          */
         protected Object doGetValue() {
             final int selectedIndex = (Integer)super.doGetValue();
             return getModelItems()[selectedIndex];
         }
 
         /**
          * {@inheritDoc}
          */
         protected void doSetValue(Object value) {
             int index = Arrays.asList(getItems()).indexOf(value);
             if (index < 0) {
                 index = 0;
             }
             super.doSetValue(Integer.valueOf(index));
         }
         
         /** 
          * @return The model Items (no I18N)
          */
         public String[] getModelItems() {
             return m_modelItems;
         }
         
     }
     
     
     /**
      * Inner data model for parameter
      * @author BREDEX GmbH
      * @created Oct 25, 2007
      */
     public static final class Parameter {
         
         /** The GUID */
         private String m_guid;
         /** The name */
         private String m_name = StringConstants.EMPTY;
         /** The type */
         private String m_type = StringConstants.EMPTY;
 
         
         /**
          * Constructor to create a new Parameter without GUID
          * @param name The name.
          * @param type The type.
          */
         public Parameter(String name, String type) {
             m_name = name;
             m_type = type;
         }
         
         /**
          * Constructor to create an alreadey existing Parameter with GUID
          * @param guid the GUID.
          * @param name The name.
          * @param type The type.
          */
         public Parameter(String guid, String name, String type) {
             this(name, type);
             m_guid = guid;
         }
 
         /**
          * @return the name or an empty String if no name is set.
          */
         public String getName() {
             return m_name;
         }
 
         /**
          * @param name the name to set
          */
         public void setName(String name) {
             if (name == null) {
                 m_name = EMPTY_ENTRY;
             } else {
                 m_name = name;                
             }
         }
 
         /**
          * @return The type or an empty String if no type is set.
          */
         public String getType() {
             return m_type;
         }
 
         /**
          * @param type The type to set
          */
         public void setType(String type) {
             if (type == null) {
                 m_type = EMPTY_ENTRY;
             } else {                
                 m_type = type;
             }
         }
 
         /**
          * @return The GUID or null if this is a new created Parameter.
          */
         public String getGuid() {
             return m_guid;
         }
         
         /**
          * {@inheritDoc}
          */
         public boolean equals(Object obj) {
             if (this == obj) {
                 return true;
             }
             if (!(obj instanceof Parameter)) {
                 return false;
             }
             final Parameter param = (Parameter)obj;
             
             return m_name.equals(param.m_name);
         }
         
         /**
          * {@inheritDoc}
          */
         public int hashCode() {
             return m_name.hashCode();
         }
         
     }
     
     /**
      * @author BREDEX GmbH
      * @created Oct 25, 2007
      */
     protected abstract class AbstractTableCellModifier 
         implements ICellModifier {
 
         /**
          * {@inheritDoc}
          */
         public abstract boolean canModify(Object element, String property);
 
         /**
          * {@inheritDoc}
          */
         public Object getValue(Object element, String property) {
             final int columnIndex = getColumnIndex(property);
             return getTableCellText((Parameter)element, columnIndex);
         }
 
         /**
          * {@inheritDoc}
          */
         public void modify(Object element, String property, Object value) {
             final TableItem tableItem = (TableItem)element;
             final Parameter parameter = (Parameter)tableItem.getData();
             final int columnIndex = getColumnIndex(property);
             setTableCellText(value, parameter, columnIndex);
         }
     }
     
     /**
      * {@inheritDoc}
      */
     protected Control createDialogArea(Composite parent) {
         final GridLayout gridLayoutParent = new GridLayout();
         gridLayoutParent.numColumns = 1;
         parent.setLayout(gridLayoutParent);
         LayoutUtil.createSeparator(parent);
         final Composite area = new Composite(parent, SWT.NONE);
         final GridLayout areaLayout = new GridLayout();
         area.setLayout(areaLayout);
         final GridData areaGridData = new GridData();
         areaGridData.grabExcessVerticalSpace = true;
         areaGridData.horizontalAlignment = GridData.FILL;
         areaGridData.verticalAlignment = GridData.FILL;
         area.setLayoutData(areaGridData);
 
         createNameField(area);
         LayoutUtil.createSeparator(area);
         createParameterTableArea(area);
         LayoutUtil.createSeparator(area);
         refreshTable();
         // add help id and tell eclipse that it's there
         Plugin.getHelpSystem().setHelp(parent, ContextHelpIds.EDIT_PARAMETERS);
         setHelpAvailable(true);
 
         return area;
     }
 
     /**
      * @param parent the parent
      */
     private void createParameterTableArea(Composite parent) {
         final Label label = new Label(parent, SWT.NONE);
         label.setText(Messages.EditParametersDialogParameters);
         
         createAdditionalWidgetsAtTop(parent);
         
         final Composite tableArea = new Composite(parent, SWT.NONE);
         final GridLayout areaLayout = new GridLayout(2, false);
         tableArea.setLayout(areaLayout);
         final GridData tableAreaGridData = new GridData();
         tableAreaGridData.grabExcessVerticalSpace = true;
         tableAreaGridData.horizontalAlignment = GridData.FILL;
         tableAreaGridData.verticalAlignment = GridData.FILL;
         tableArea.setLayoutData(tableAreaGridData);
         
         createParameterTable(tableArea);
         
         createTableButtons(tableArea);
     }
 
     /**
      * subclasses may override
      * @param parent the parent
      */
     protected void createAdditionalWidgetsAtTop(Composite parent) {
         // subclasses may override
     }
 
     /**
      * @param parent the parent
      */
     protected void createParameterTable(final Composite parent) {
         setParamTableViewer(new TableViewer(parent, 
             SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION));
         createInnerDataModel();
         final GridData paramTableFieldGridData = new GridData();
         paramTableFieldGridData.grabExcessHorizontalSpace = true;
         paramTableFieldGridData.grabExcessVerticalSpace = true;
         paramTableFieldGridData.horizontalAlignment = GridData.FILL;
         paramTableFieldGridData.verticalAlignment = GridData.FILL;
         final Table table = getParamTableViewer().getTable();
         
         for (String columnText : TABLE_COLUMNS) {
             TableColumn column = new TableColumn(table, SWT.NONE);
             column.setText(columnText);
         }
         table.setHeaderVisible(true);
         table.setLinesVisible(true);
         table.setLayoutData(paramTableFieldGridData);
         for (TableColumn col : table.getColumns()) {
             col.setWidth(DEFAULT_COLUMN_WIDTH);
         }
         getParamTableViewer().setColumnProperties(TABLE_COLUMNS);
         getParamTableViewer().setContentProvider(
                 new ParamTableContentProvider());
         getParamTableViewer().setLabelProvider(
                 new ParamTableLabelProvider());
         getParamTableViewer().setInput(m_parameters);
         createTableCellEditors(table);
         getParamTableViewer().setCellModifier(getParamTableCellModifier());
         for (CellEditor cellEditor : getParamTableViewer().getCellEditors()) {
             cellEditor.getControl().addFocusListener(new FocusAdapter() {
                 public void focusLost(FocusEvent e) {
                     checkOkButtonEnablement();
                 }
             });
         }
     }
 
     /**
      * @return a valid cell table modifier
      */
     protected abstract ICellModifier getParamTableCellModifier();
 
     /**
      * Creates the {@link CellEditor}s of the given Table.
      * @param table a Table
      */
     @SuppressWarnings("unchecked")
     private void createTableCellEditors(final Table table) {
         
         final TextCellEditor paramNameTextCellEditor = new TextCellEditor(
             table);
         ((Text)paramNameTextCellEditor.getControl()).addVerifyListener(
                 new VerifyListener() {
                     
                     public void verifyText(VerifyEvent e) {
                         Text txt = (Text)e.widget;
                         final String oldValue = txt.getText();
                         StringBuilder workValue = new StringBuilder(oldValue);
                         workValue.replace(e.start, e.end, e.text);
                         String newValue = workValue.toString();
                         
                         e.doit = Pattern.matches(PARAM_NAME_REGEX, newValue);
                     }
             
                 });
         final Set<String> paramTypes = ComponentBuilder.getInstance()
             .getCompSystem().getDataTypes();
         
         final I18nComboBoxCellEditor i18nParamTypesComboBoxEditor = 
             new I18nComboBoxCellEditor(table, 
                 paramTypes.toArray(new String[paramTypes.size()]), 
                 SWT.READ_ONLY);
         
         getParamTableViewer().setCellEditors(
             new CellEditor[]{paramNameTextCellEditor, 
                 i18nParamTypesComboBoxEditor});
     }
 
     /**
      * Creates the inner data model (m_parameters)
      */
     private void createInnerDataModel() {
         if (!m_parameters.isEmpty()) {
             m_parameters.clear();
         }
         List<IParamDescriptionPO> parameterList = 
             getParamInterfaceObj().getParameterList();
         for (IParamDescriptionPO descr : parameterList) {
             Parameter parameter = new Parameter(descr.getUniqueId(), 
                 descr.getName(), descr.getType());
             m_parameters.add(parameter);
         }
     }
 
     /**
      * @param parent the parent
      */
     private void createTableButtons(Composite parent) {
         final Composite tableButtonArea = new Composite(parent, SWT.NONE);
         final GridLayout areaLayout = new GridLayout(1, true);
         tableButtonArea.setLayout(areaLayout);
         final GridData tableAreaGridData = new GridData();
         tableAreaGridData.grabExcessVerticalSpace = true;
         tableAreaGridData.horizontalAlignment = GridData.FILL;
         tableAreaGridData.verticalAlignment = GridData.FILL;
         tableButtonArea.setLayoutData(tableAreaGridData);
         
         final GridData buttonsGridData = new GridData();
         buttonsGridData.grabExcessHorizontalSpace = true;
         buttonsGridData.horizontalAlignment = GridData.FILL;
         createAddButton(tableButtonArea, buttonsGridData);
         createDeleteButton(tableButtonArea, buttonsGridData);
         LayoutUtil.createSeparator(tableButtonArea);
         createUpButton(tableButtonArea, buttonsGridData);
         createDownButton(tableButtonArea, buttonsGridData);
     }
 
     /**
      * @param parent the parent
      * @param layoutData the LayoutData
      * @return the Delete-Button
      */
     private Button createDeleteButton(Composite parent, GridData layoutData) {
         final Button deleteButton = new Button(parent, SWT.NONE);
         deleteButton.setText(Messages.EditParametersDialogRemove);
         deleteButton.setLayoutData(layoutData);
         deleteButton.addSelectionListener(new SelectionListener() {
             
             public void widgetSelected(SelectionEvent e) {                
                 handleSelection(e);
             }
 
             public void widgetDefaultSelected(SelectionEvent e) {
                 handleSelection(e);
             }
             
             /**
              * Handles the selection
              * @param e SelectionEvent
              */
             private void handleSelection(SelectionEvent e) {
                 removeSelectedParameter();
                 checkOkButtonEnablement();
             }
         });
         final SelectionBasedButtonEnabler buttonEnabler = 
             new SelectionBasedButtonEnabler(deleteButton);
         getParamTableViewer().addSelectionChangedListener(buttonEnabler);
         afterDeleteButtonCreation(deleteButton, buttonEnabler);
         deleteButton.setEnabled(
                 !getParamTableViewer().getSelection().isEmpty());
         return deleteButton;
     }
 
 
     /**
      * subclasses may override
      * @param deleteButton the delete button 
      * @param buttonEnabler the button enabler
      */
     protected void afterDeleteButtonCreation(Button deleteButton,
             SelectionBasedButtonEnabler buttonEnabler) {
         // subclasses may override
     }
     
     /**
      * @param parent the parent
      * @param layoutData the LayoutData
      * @return the Up-Button
      */
     private Button createUpButton(Composite parent, GridData layoutData) {
         final Button upButton = new Button(parent, SWT.NONE);
         upButton.setText(Messages.EditParametersDialogUp);
         upButton.setLayoutData(layoutData);
         upButton.addSelectionListener(new SelectionListener() {
             
             public void widgetSelected(SelectionEvent e) {                
                 handleSelection(e);
             }
 
             public void widgetDefaultSelected(SelectionEvent e) {
                 handleSelection(e);
             }
             
             /**
              * Handles the selection
              * @param e SelectionEvent
              */
             private void handleSelection(SelectionEvent e) {
                 moveParameter(false);
             }
         });
         final SelectionBasedButtonEnabler buttonEnabler = 
             new SelectionBasedButtonEnabler(upButton);
         getParamTableViewer().addSelectionChangedListener(buttonEnabler);
         afterUpButtonCreation(upButton, buttonEnabler);
         upButton.setEnabled(!getParamTableViewer().getSelection().isEmpty());
         return upButton;
     }
     
     /**
      * subclasses may override
      * @param upButton the up button 
      * @param buttonEnabler the button enabler
      */
     protected void afterUpButtonCreation(Button upButton,
             SelectionBasedButtonEnabler buttonEnabler) {
         // subclasses may override
     }
     
     /**
      * @param parent the parent
      * @param layoutData the LayoutData
      * @return the Up-Button
      */
     private Button createDownButton(Composite parent, GridData layoutData) {
         final Button downButton = new Button(parent, SWT.NONE);
         downButton.setText(Messages.EditParametersDialogDown);
         downButton.setLayoutData(layoutData);
         downButton.addSelectionListener(new SelectionListener() {
             
             public void widgetSelected(SelectionEvent e) {                
                 handleSelection(e);
             }
 
             public void widgetDefaultSelected(SelectionEvent e) {
                 handleSelection(e);
             }
             
             /**
              * Handles the selection
              * @param e SelectionEvent
              */
             private void handleSelection(SelectionEvent e) {
                 moveParameter(true);
             }
         });
         final SelectionBasedButtonEnabler buttonEnabler = 
             new SelectionBasedButtonEnabler(downButton);
         getParamTableViewer().addSelectionChangedListener(buttonEnabler);
         afterDownButtonCreation(downButton, buttonEnabler);
         downButton.setEnabled(!getParamTableViewer().getSelection().isEmpty());
         return downButton;
     }
     
     /**
      * subclasses may override
      * @param downButton the down button 
      * @param buttonEnabler the button enabler
      */
     protected void afterDownButtonCreation(Button downButton,
             SelectionBasedButtonEnabler buttonEnabler) {
         // subclasses may override
     }
     
     /**
      * @param parent the parent
      * @param layoutData the LayoutData
      * @return the Add-Button
      */
     private Button createAddButton(Composite parent, GridData layoutData) {
         final Button addButton = new Button(parent, SWT.NONE);
         addButton.setText(Messages.EditParametersDialogAdd);
         addButton.setLayoutData(layoutData);
         addButton.addSelectionListener(new SelectionListener() {
             
             public void widgetSelected(SelectionEvent e) {
                 handleSelection(e);
             }
 
             public void widgetDefaultSelected(SelectionEvent e) {
                 handleSelection(e);
             }
             
             /**
              * Handles the selection
              * @param e SelectionEvent
              */
             private void handleSelection(SelectionEvent e) {
                 addParameter();
                 checkOkButtonEnablement();
             }
         });
         afterAddButtonCreation(addButton);
         return addButton;
     }
     
     /**
      * subclasses may override
      * @param addButton the down button 
      */
     protected void afterAddButtonCreation(Button addButton) {
         // subclasses may override
     }
 
     /**
      * Adds a new Parameter to the table model
      */
     private void addParameter() {
         String newParamName = DEFAULT_PARAM_NAME;
         boolean unique = true;
         int count = 0;
         do {
             for (Parameter p : m_parameters) {
                 unique = true;
                 if (p.getName().equals(newParamName)) {
                     unique = false;
                     newParamName = DEFAULT_PARAM_NAME + (++count);
                     break;
                 }
             }
         } while (!unique);
         
         final Parameter parameter = new Parameter(newParamName, 
             DEFAULT_PARAM_TYPE);
         m_parameters.add(parameter);
         refreshTable();
         selectRowInParameterTable(-1);
         getParamTableViewer().editElement(parameter, 0);
     }
 
     /**
      * selects a row in the parameter table and makes sure all listeners are
      * notified (which doesn't happen if you just call Tabke.setSelection()).
      * @param row valid row number or -1 for last row
      */
     private void selectRowInParameterTable(final int row) {
         final boolean selectLast = (row == -1);
         final Table table = getParamTableViewer().getTable();        
         final int tableItemCount = table.getItemCount();
         int index = row;
         
         if (selectLast) {
             index = tableItemCount - 1;
             if (index == -1) { // no entries in table
                 return;
             }
         }
         if (index > tableItemCount) { // out of bounds
             return;
         }
         final Object selectedObject = getParamTableViewer().getElementAt(index);
         if (selectedObject != null) {
             getParamTableViewer().setSelection(new StructuredSelection(
                     selectedObject));
         }
     }
     
     /**
      * Removes the current selected Parameter from the table model.
      */
     private void removeSelectedParameter() {
         if (!confirmDeleteParam()) {
             return;
         }
         
         final Table table = getParamTableViewer().getTable();
         final int[] selectionIndices = table.getSelectionIndices();
         for (int index : selectionIndices) {
             m_parameters.remove(index);
         }
         if (selectionIndices.length > 0) {
             int index = selectionIndices[selectionIndices.length - 1];
             refreshTable();
             final int tableItemCount = table.getItemCount();
             if (index >= tableItemCount) {            
                 index = -1; // last row     
             }
             selectRowInParameterTable(index);
         }
     }
 
     /**
      * Creates a Question Dialog for deleting a Parameter
      * @return true if the user clicks OK, false otherwise.
      */
     private boolean confirmDeleteParam() {
         final Dialog dialog = ErrorHandlingUtil.createMessageDialog(
                 MessageIDs.Q_CHANGE_INTERFACE_REMOVE_PARAM, null, null,
                 getShell());
         return dialog.getReturnCode() == Window.OK;
     }
 
     /**
      * Creates a Question Dialog for changing a Parameter type.
      * @return true if the user clicks OK, false otherwise.
      */
     private boolean confirmChangeParamType() {
         final Dialog dialog = ErrorHandlingUtil.createMessageDialog(
                 MessageIDs.Q_CHANGE_INTERFACE_CHANGE_PARAM_TYPE, null, null,
                 getShell());
         return dialog.getReturnCode() == Window.OK;
     }
     
     /**
      * @param parent the parent
      */
     private void createNameField(final Composite parent) {
         final Composite nameFieldArea = new Composite(parent, SWT.NONE);
         final GridLayout areaLayout = new GridLayout(2, false);
         nameFieldArea.setLayout(areaLayout);
         final GridData tableAreaGridData = new GridData();
         tableAreaGridData.horizontalAlignment = GridData.FILL;
         nameFieldArea.setLayoutData(tableAreaGridData);
         final Label nameLabel = new Label(nameFieldArea, SWT.NONE);
         nameLabel.setText(getEditedObjectNameString());
         
         final Text nameField = new Text(nameFieldArea, 
                 SWT.SINGLE | SWT.BORDER);
         final GridData nameFieldGridData = new GridData();
         nameFieldGridData.grabExcessHorizontalSpace = true;
         nameFieldGridData.horizontalAlignment = GridData.FILL;
         nameField.setLayoutData(nameFieldGridData);
         nameField.setText(getParamInterfaceObj().getName());
         nameField.setEditable(false);
     }
 
 
     /**
      * @return the name to display for the currently edited object
      */
     protected abstract String getEditedObjectNameString();
 
     /**
      * @return The Parameters.
      */
     public final List<Parameter> getParameters() {
         return m_parameters;
     }
     
     /**
      * Gets the column index of the given column name.
      * @param columnName a column name.
      * @return the column index of the given column name or -1 if no column
      * with the given name was found.
      */
     private int getColumnIndex(String columnName) {
         int index = 0;
         for (String colName : TABLE_COLUMNS) {
             if (colName.equals(columnName)) {
                 return index;
             }
             index++;
         }
         return -1;
     }
     
     /**
      * @param parameter The Parameter object.
      * @param columnIndex the index of the table column.
      * @return the table entry.
      */
     private String getTableCellText(Parameter parameter, int columnIndex) {
         switch (columnIndex) {
             case NAME_TABLE_COLUMN:
                 return parameter.getName();
                 
             case TYPE_TABLE_COLUMN:
                 final String paramType = parameter.getType();
                 return CompSystemI18n.getString(paramType, true);
 
             default:
                 StringBuilder msg = new StringBuilder();
                 msg.append(Messages.ColumnIndex)
                     .append(StringConstants.SPACE)
                     .append(StringConstants.APOSTROPHE)
                     .append(columnIndex)
                     .append(StringConstants.APOSTROPHE)
                     .append(StringConstants.SPACE)
                     .append(Messages.DoesNotExist)
                     .append(StringConstants.EXCLAMATION_MARK);
                     
                 Assert.notReached(msg.toString());
                 break;
         }
         return EMPTY_ENTRY;
     }
     
     /**
      * @param value The value to set.
      * @param parameter The Parameter object.
      * @param columnIndex the column index.
      */
     private void setTableCellText(Object value, Parameter parameter, 
         int columnIndex) {
         
         switch (columnIndex) {
             case NAME_TABLE_COLUMN:
                 changeParameterName(parameter, (String)value);
                 break;
                 
             case TYPE_TABLE_COLUMN:
                 changeParameterType(parameter, (String)value);
                 break;
 
             default:
                 StringBuilder msg = new StringBuilder();
                 msg.append(Messages.ColumnIndex)
                     .append(StringConstants.SPACE)
                     .append(StringConstants.APOSTROPHE)
                     .append(columnIndex)
                     .append(StringConstants.APOSTROPHE)
                     .append(StringConstants.SPACE)
                     .append(Messages.DoesNotExist)
                     .append(StringConstants.EXCLAMATION_MARK);
                 Assert.notReached(msg.toString());
                 break;
         }
         refreshTable();
     }
 
     /**
      * Changes the Parameter Name.
      * 
      * @param parameter
      *            the Parameter which name is to change.
      * @param name
      *            the new name
      */
     private void changeParameterName(Parameter parameter, String name) {
         if (parameter.getName().equals(name)) {
             return;
         }
 
         if (isNewParameter(parameter)) {
             parameter.setName(name);
         } else if (confirmChangeParamName()) {
             parameter.setName(name);
         }
     }
 
     /**
      * optional point to ask the user for permission; subclasses may override
      * 
      * @return true if the param name change is ok
      */
     protected boolean confirmChangeParamName() {
         return true;
     }
     
     /**
      * Changes the Parameter Type.
      * @param parameter the Parameter which type is to change.
      * @param type the type.
      */
     private void changeParameterType(Parameter parameter, String type) {
         if (parameter.getType().equals(type)) {
             return;
         }
         if (isNewParameter(parameter)) {
             parameter.setType(type);
         } else if (confirmChangeParamType()) { // already used Parameter
             final int index = m_parameters.indexOf(parameter);
             final Parameter newParam = new Parameter(parameter.getName(), type);
             m_parameters.remove(parameter);
             m_parameters.add(index, newParam);
         }
     }
     
     /**
      * @param parameter
      *            to check whether it's new
      * @return true if the parameter is a new one
      */
     private boolean isNewParameter(Parameter parameter) {
         return parameter.getGuid() == null;
     }
 
     /**
      * Refreshes the m_paramTableViewer.
      */
     private void refreshTable() {
         getParamTableViewer().refresh();
         final Table table = getParamTableViewer().getTable();
         table.setFocus();
     }
     
     /**
     * @return the OK-Button of this dialog.
      */
     private Button getOkButton() {
         return getButton(IDialogConstants.OK_ID);
     }
 
     /**
      * Checks the enablement of the OK-Button
      */
     private void checkOkButtonEnablement() {
         getOkButton().setEnabled(isParamTableValid());
     }
     
     /**
      * @return true if all entries of the parameter table are valid, 
      * false otherwise.
      */
     private boolean isParamTableValid() {
         for (Parameter param : getParameters()) {
             if (EMPTY_ENTRY.equals(param.getName().trim())) {
                 setErrorMessage(
                         Messages.EditParametersDialogEmptyParamName);
                 return false;
             } else if (EMPTY_ENTRY.equals(param.getType().trim())) {
                 setErrorMessage(
                         Messages.EditParametersDialogEmptyParamName);
                 return false;
             } else if (isDuplicate(param)) {
                 setErrorMessage(
                         NLS.bind(
                                 Messages.EditParametersDialogDuplicateParamName,
                                 param.getName()));
                 return false;
             }
         }
 
         setErrorMessage(null);
         return true;
     }
 
     /**
      * 
      * @param parameter A Parameter.
      * @return true if the given Parameter is a duplicate, false otherwise.
      */
     private boolean isDuplicate(Parameter parameter) {
         int occurences = 0;
         for (Parameter param : m_parameters) {
             if (param.equals(parameter)) {
                 occurences++;
             }
             if (occurences > 1) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Gets all indices of the given Parameter.
      * @param parameter a Parameter
      * @return an Array of indices.
      */
     private Integer[] getIndices(Parameter parameter) {
         final List<Integer> occurenceIndices = new ArrayList<Integer>();
         int index = 0;
         for (Parameter param : m_parameters) {
             if (param.equals(parameter)) {
                 occurenceIndices.add(index);
             }
             index++;
         }
         return occurenceIndices.toArray(new Integer[occurenceIndices.size()]);
     }
     
     /**
      * Moves the selected Parameter up or down
      * 
      * @param down
      *            true moves down, false moves up.
      */
     private void moveParameter(boolean down) {
         if (getParamTableViewer().getSelection() 
                 instanceof IStructuredSelection) {
             final Parameter param = (Parameter)
                     ((IStructuredSelection)getParamTableViewer()
                             .getSelection()).getFirstElement();
             final int paramIdx = m_parameters.indexOf(param);
             final int newIdxDiff = 1;
             final int newIdx = down ? (paramIdx + newIdxDiff)
                     : (paramIdx - newIdxDiff);
             if (newIdx < m_parameters.size() && newIdx > -1) {
                 m_parameters.remove(paramIdx);
                 m_parameters.add(newIdx, param);
                 refreshTable();
             }
         }
     }
 
     /**
      * @param paramTableViewer the paramTableViewer to set
      */
     private void setParamTableViewer(TableViewer paramTableViewer) {
         m_paramTableViewer = paramTableViewer;
     }
 
     /**
      * @return the paramTableViewer
      */
     protected TableViewer getParamTableViewer() {
         return m_paramTableViewer;
     }
 
     /**
      * @param paramInterfaceObj the paramInterfaceObj to set
      */
     protected void setParamInterfaceObj(
             IParameterInterfacePO paramInterfaceObj) {
         m_paramInterfaceObj = paramInterfaceObj;
     }
 
     /**
      * @return the paramInterfaceObj
      */
     protected IParameterInterfacePO getParamInterfaceObj() {
         return m_paramInterfaceObj;
     }
 }
