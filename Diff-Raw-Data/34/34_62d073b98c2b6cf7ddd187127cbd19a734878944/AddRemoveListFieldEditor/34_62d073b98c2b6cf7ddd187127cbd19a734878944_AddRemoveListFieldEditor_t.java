 /* ====================================================================
  *   Copyright 2003-2004 Fabrizio Giustina.
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  * ====================================================================
  */
 package net.sf.commonclipse.preferences;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 import net.sf.commonclipse.CCMessages;
 
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.preference.FieldEditor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 
 /**
  * A field editor for displaying and storing a list of strings. Buttons are provided for adding items to the list and
  * removing items from the list. Implementation from
  * http://www.eclipse.org/articles/Article-Field-Editors/field_editors.html
  * @author fgiust
  * @version $Revision$ ($Author$)
  */
 public class AddRemoveListFieldEditor extends FieldEditor
 {
 
     /**
      * default separator for list items.
      */
     private static final String DEFAULT_SEPARATOR = ";"; //$NON-NLS-1$
 
     /**
      * vertical dialog units per char.
      */
     private static final int VERTICAL_DIALOG_UNITS_PER_CHAR = 8;
 
     /**
      * list height.
      */
     private static final int LIST_HEIGHT_IN_CHARS = 10;
 
     /**
      * list height in units.
      */
     private static final int LIST_HEIGHT_IN_DLUS = LIST_HEIGHT_IN_CHARS * VERTICAL_DIALOG_UNITS_PER_CHAR;
 
     /**
      * The list of items.
      */
     List list;
 
     /**
      * The top-level control for the field editor.
      */
     private Composite top;
 
     /**
      * The text field for inputting new list items.
      */
     private Text textField;
 
     /**
      * The button for adding the contents of the text field to the list.
      */
     private Button add;
 
     /**
      * The button for removing the currently-selected list item.
      */
     private Button remove;
 
     /**
      * The string used to separate list items in a single String representation.
      */
     private String separator = DEFAULT_SEPARATOR;
 
     /**
      * Creates a string field editor of unlimited width. Use the method <code>setTextLimit</code> to limit the text.
      * @param name the name of the preference this field editor works on
      * @param labelText the label text of the field editor
      * @param parent the parent of the field editor's control
      */
     public AddRemoveListFieldEditor(String name, String labelText, Composite parent)
     {
         super(name, labelText, parent);
     }
 
     /**
      * Creates a string field editor of unlimited width. Use the method <code>setTextLimit</code> to limit the text.
      * @param name the name of the preference this field editor works on
      * @param labelText the label text of the field editor
      * @param addButtonText text for the "add" button
      * @param removeButtonText text for the "remove" buttom
      * @param parent the parent of the field editor's control
      */
     public AddRemoveListFieldEditor(
         String name,
         String labelText,
         String addButtonText,
         String removeButtonText,
         Composite parent)
     {
         super(name, labelText, parent);
         setAddButtonText(addButtonText);
         setRemoveButtonText(removeButtonText);
     }
 
     /**
      * @see org.eclipse.jface.preference.FieldEditor#adjustForNumColumns(int)
      */
     @Override
     protected void adjustForNumColumns(int numColumns)
     {
         ((GridData) this.top.getLayoutData()).horizontalSpan = numColumns;
     }
 
     /**
      * @see org.eclipse.jface.preference.FieldEditor#doFillIntoGrid (Composite, int)
      */
     @Override
     protected void doFillIntoGrid(Composite parent, int numColumns)
     {
         this.top = parent;
 
         GridData gd = new GridData(GridData.FILL_HORIZONTAL);
         gd.horizontalSpan = numColumns;
         this.top.setLayoutData(gd);
 
         Label label = getLabelControl(this.top);
         GridData labelData = new GridData();
         labelData.horizontalSpan = numColumns;
         label.setLayoutData(labelData);
 
         this.list = new List(this.top, SWT.BORDER);
 
         // Create a grid data that takes up the extra space in the dialog and spans both columns.
         GridData listData = new GridData(GridData.FILL_HORIZONTAL);
         listData.heightHint = convertVerticalDLUsToPixels(this.list, LIST_HEIGHT_IN_DLUS);
         listData.horizontalSpan = numColumns;
 
         this.list.setLayoutData(listData);
         this.list.addSelectionListener(new SelectionAdapter()
         {
 
             @Override
             public void widgetSelected(SelectionEvent e)
             {
                 selectionChanged();
             }
         });
 
         // Create a composite for the add and remove buttons and the input text field.
         Composite addRemoveGroup = new Composite(this.top, SWT.NONE);
 
         GridData addRemoveData = new GridData(GridData.FILL_HORIZONTAL);
         addRemoveData.horizontalSpan = numColumns;
         addRemoveGroup.setLayoutData(addRemoveData);
 
         GridLayout addRemoveLayout = new GridLayout();
         addRemoveLayout.numColumns = numColumns;
         addRemoveLayout.marginHeight = 0;
         addRemoveLayout.marginWidth = 0;
         addRemoveGroup.setLayout(addRemoveLayout);
 
         // Create a composite for the add and remove buttons.
         Composite buttonGroup = new Composite(addRemoveGroup, SWT.NONE);
         buttonGroup.setLayoutData(new GridData());
 
         GridLayout buttonLayout = new GridLayout();
         buttonLayout.marginHeight = 0;
         buttonLayout.marginWidth = 0;
         buttonGroup.setLayout(buttonLayout);
 
         // Create the add button.
         this.add = new Button(buttonGroup, SWT.NONE);
         this.add.setText(CCMessages.getString("preferences.button.add")); //$NON-NLS-1$
         this.add.addSelectionListener(new SelectionAdapter()
         {
 
             @Override
             public void widgetSelected(SelectionEvent e)
             {
                 add();
             }
         });
         GridData addData = new GridData(GridData.FILL_HORIZONTAL);
         addData.heightHint = convertVerticalDLUsToPixels(this.add, 14);
         addData.widthHint = convertHorizontalDLUsToPixels(this.add, IDialogConstants.BUTTON_WIDTH);
         this.add.setLayoutData(addData);
 
         // Create the remove button.
         this.remove = new Button(buttonGroup, SWT.NONE);
         this.remove.setEnabled(false);
         this.remove.setText(CCMessages.getString("preferences.button.remove")); //$NON-NLS-1$
         this.remove.addSelectionListener(new SelectionAdapter()
         {
 
             @Override
             public void widgetSelected(SelectionEvent e)
             {
                 AddRemoveListFieldEditor.this.list.remove(AddRemoveListFieldEditor.this.list.getSelectionIndex());
                 selectionChanged();
             }
         });
         GridData removeData = new GridData(GridData.FILL_HORIZONTAL);
         removeData.heightHint = convertVerticalDLUsToPixels(this.remove, 14);
         removeData.widthHint = convertHorizontalDLUsToPixels(this.remove, IDialogConstants.BUTTON_WIDTH);
         this.remove.setLayoutData(removeData);
 
         // Create the text field.
         this.textField = new Text(addRemoveGroup, SWT.BORDER);
 
         GridData textData = new GridData(GridData.FILL_HORIZONTAL);
         textData.horizontalSpan = numColumns - 1;
         textData.verticalAlignment = GridData.BEGINNING;
         this.textField.setLayoutData(textData);
     }
 
     /**
      * @see org.eclipse.jface.preference.FieldEditor#doLoad()
      */
     @Override
     protected void doLoad()
     {
         String items = getPreferenceStore().getString(getPreferenceName());
         setList(items);
     }
 
     /**
      * @see org.eclipse.jface.preference.FieldEditor#doLoadDefault()
      */
     @Override
     protected void doLoadDefault()
     {
         String items = getPreferenceStore().getDefaultString(getPreferenceName());
         setList(items);
     }
 
     /**
      * Parses the string into separate list items and adds them to the list.
      * @param items String to be splitted
      */
     private void setList(String items)
     {
         String[] itemArray = parseString(items);
         this.list.setItems(itemArray);
     }
 
     /**
      * @see org.eclipse.jface.preference.FieldEditor#doStore()
      */
     @Override
     protected void doStore()
     {
         String s = createListString(this.list.getItems());
         if (s != null)
         {
 
             getPreferenceStore().setValue(getPreferenceName(), s);
         }
     }
 
     /**
      * @see org.eclipse.jface.preference.FieldEditor#getNumberOfControls()
      */
     @Override
     public int getNumberOfControls()
     {
         // The button composite and the text field.
         return 2;
     }
 
     /**
      * Adds the string in the text field to the list.
      */
     void add()
     {
         String tag = this.textField.getText();
         if (tag != null && tag.length() > 0)
         {
 
             if (!containsOnlyValidChars(tag))
             {
                 MessageDialog.openError(
                     new Shell(),
                     CCMessages.getString("preferences.invalidfieldtitle"), MessageFormat.format(//$NON-NLS-1$
                         CCMessages.getString("preferences.invalidfieldmessage"), //$NON-NLS-1$
                         new Object[]{tag}));
                 return;
             }
 
             this.list.add(tag);
         }
         this.textField.setText(""); //$NON-NLS-1$
     }
 
     /**
      * Checks the the given String doesn't contains invalid chars.
      * @param str input String
      * @return <code>true</code> if the string doesn't contains invalid chars.
      */
     public static boolean containsOnlyValidChars(String str)
     {
 
         char[] invalidChars = new char[]{'(', ')', '[', ']', '{', '}', '.', '/', '\\', '^', '$', '.', '&', '|', '+'};
 
         int strSize = str.length();
         int validSize = invalidChars.length;
         for (int i = 0; i < strSize; i++)
         {
             char ch = str.charAt(i);
             for (int j = 0; j < validSize; j++)
             {
                 if (invalidChars[j] == ch)
                 {
                     return false;
                 }
             }
         }
         return true;
     }
 
     /**
      * Sets the label for the button that adds the contents of the text field to the list.
      * @param text "add" button text
      */
     public void setAddButtonText(String text)
     {
         this.add.setText(text);
     }
 
     /**
      * Sets the label for the button that removes the selected item from the list.
      * @param text "remove" button text
      */
     public void setRemoveButtonText(String text)
     {
         this.remove.setText(text);
     }
 
     /**
      * Sets the string that separates items in the list when the list is stored as a single String in the preference
      * store.
      * @param listSeparator token used as a delimiter when converting the array in a single string
      */
     public void setSeparator(String listSeparator)
     {
         this.separator = listSeparator;
     }
 
     /**
      * Creates the single String representation of the list that is stored in the preference store.
      * @param items String array
      * @return String created adding items elements separated by "separator"
      */
     private String createListString(String[] items)
     {
         StringBuffer path = new StringBuffer();
 
         for (int i = 0; i < items.length; i++)
         {
             path.append(items[i]);
             path.append(this.separator);
         }
         return path.toString();
     }
 
     /**
      * Parses the single String representation of the list into an array of list items.
      * @param stringList String to be splitted in array
      * @return String[] splitted string
      */
     private String[] parseString(String stringList)
     {
         StringTokenizer st = new StringTokenizer(stringList, this.separator);
         java.util.List<String> v = new ArrayList<String>();
         while (st.hasMoreElements())
         {
             v.add(st.nextToken());
         }
        return v.toArray(new String[v.size()]);
     }
 
     /**
      * Sets the enablement of the remove button depending on the selection in the list.
      */
     void selectionChanged()
     {
         int index = this.list.getSelectionIndex();
         this.remove.setEnabled(index >= 0);
     }
 
 }
