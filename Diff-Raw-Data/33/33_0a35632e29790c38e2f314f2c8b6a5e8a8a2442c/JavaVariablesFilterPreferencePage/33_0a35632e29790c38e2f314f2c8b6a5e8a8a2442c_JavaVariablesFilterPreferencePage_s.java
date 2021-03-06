 package org.eclipse.jdt.internal.debug.ui;
 
 /**********************************************************************
 Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
 This file is made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html
 **********************************************************************/
 
 import org.eclipse.jdt.internal.debug.ui.actions.JavaVariablesFilterPreferenceAction;
 import org.eclipse.jdt.ui.ISharedImages;
 import org.eclipse.jdt.ui.JavaUI;
 import org.eclipse.jface.preference.BooleanFieldEditor;
 import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.viewers.StructuredViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.help.WorkbenchHelp;
 
 /**
  * A preference page for collecting values that govern how Java variables are
  * presented.  The main part of this pref page is a grid of checkboxes.  The
  * rows represent access types (public, protected, etc.) and the columns
  * represent 'modes' (static, final, etc.).  A check mark means that the
  * corresponding access/mode pair will be shown.
  */
 public class JavaVariablesFilterPreferencePage extends FieldEditorPreferencePage {
 
 	/**
 	 * A boolean field editor that aligns its checkbox in the center of the
 	 * column.  This field editor does NOT support the 'SEPARATE label' mode
 	 * that its superclass supports.
 	 */
 	private class CenterAlignedBooleanFieldEditor extends BooleanFieldEditor {
 		
 		public CenterAlignedBooleanFieldEditor(String name, String label, Composite parent) {
 			super(name, label, parent);
 		}
 
 		/**
 		 * @see org.eclipse.jface.preference.FieldEditor#doFillIntoGrid(org.eclipse.swt.widgets.Composite, int)
 		 */
 		protected void doFillIntoGrid(Composite parent, int numColumns) {
 			String text = getLabelText();
 			Button checkBox = getChangeControl(parent);
 			GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
 			gd.horizontalSpan = numColumns;
 			checkBox.setLayoutData(gd);
 			if (text != null) {
 				checkBox.setText(text);
 			}
 		}
 		
 		/**
 		 * This method essentially serves to provide public access to the
 		 * underlying checkbox button.  This is required so that the +/-
 		 * buttons can do their job.
 		 */
 		public Button getCheckbox(Composite parent) {
 			return getChangeControl(parent);
 		}
 	}
 	
 	/**
 	 * This listener responds to selection events for the +/- buttons at the top
 	 * of each column and the beginning of each row of the filter grid.  The +
 	 * buttons select all of the checkboxes in the corresponding row/column, and
 	 * the - button deselects all of the checkboxes.
 	 */
 	private class PlusMinusButtonListener implements SelectionListener {
 				
 		/**
 		 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
 		 */
 		public void widgetSelected(SelectionEvent evt) {
 			Button button = (Button) evt.getSource();
 			boolean isPlusButton = ((Boolean)button.getData(PLUS_KEY)).booleanValue();
 			boolean isColumnButton = ((Boolean)button.getData(COLUMN_KEY)).booleanValue();
 			int rowCol = ((Integer)button.getData(ROW_COL_KEY)).intValue();
 			if (isColumnButton) {
 				processCol(isPlusButton, rowCol);
 			} else {
 				processRow(isPlusButton, rowCol);
 			}
 		}
 		
 		private void processRow(boolean select, int row) {
 			for (int j = 0; j < JDIDebugUIPlugin.fgAccessModifierNames.length; j++) {
 				CenterAlignedBooleanFieldEditor bfe = fCheckboxes[row][j];
 				bfe.getCheckbox(getFieldEditorParent()).setSelection(select);
 			}
 		}
 		
 		private void processCol(boolean select, int col) {
 			for (int i = 0; i < JDIDebugUIPlugin.fgModeModifierNames.length; i++) {
 				CenterAlignedBooleanFieldEditor bfe = fCheckboxes[i][col];
 				bfe.getCheckbox(getFieldEditorParent()).setSelection(select);
 			}			
 		}
 
 		/**
 		 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
 		 */
 		public void widgetDefaultSelected(SelectionEvent evt) {
 		}
 
 	}
 
 	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
 	
 	private static final int HORIZONTAL_GRID_SPACING = 10;
 	private static final int VERTICAL_GRID_SPACING = 10;
 	
 	private static final String COLUMN_KEY = "column"; //$NON-NLS-1$
 	private static final String PLUS_KEY = "plus"; //$NON-NLS-1$
 	private static final String ROW_COL_KEY = "row_col"; //$NON-NLS-1$
 
 	private StructuredViewer fViewer;
 
 	public static final Image fgPlusSignImage = JavaDebugImages.get(JavaDebugImages.IMG_OBJS_PLUS_SIGN);
 	public static final Image fgMinusSignImage = JavaDebugImages.get(JavaDebugImages.IMG_OBJS_MINUS_SIGN);
 	
 	private static Image[] fgAccessImages;
 	
 	private CenterAlignedBooleanFieldEditor[][] fCheckboxes =
 			new CenterAlignedBooleanFieldEditor[JDIDebugUIPlugin.fgModeModifierNames.length][JDIDebugUIPlugin.fgAccessModifierNames.length];
 	
 	static {
 		fgAccessImages = new Image[JDIDebugUIPlugin.fgAccessModifierNames.length];
 		fgAccessImages[0] = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PUBLIC);
 		fgAccessImages[1] = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_DEFAULT);
 		fgAccessImages[2] = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PROTECTED);
 		fgAccessImages[3] = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PRIVATE);
 		fgAccessImages[4] = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_DEFAULT);		
 	}
 	
 	public JavaVariablesFilterPreferencePage(StructuredViewer viewer) {
 		super(GRID);
 		setViewer(viewer);
 	}
 
 	/**
 	 * @see PreferencePage#createControl(Composite)
 	 */
 	public void createControl(Composite parent) {
 		super.createControl(parent);
 		setTitle(DebugUIMessages.getString("JavaVariablesFilterPreferencePage.Java_Variable_Filter_Preferences_1")); //$NON-NLS-1$
 		setControl(getFieldEditorParent());
 		WorkbenchHelp.setHelp(
 			parent,
 			IJavaDebugHelpContextIds.JAVA_VARIABLES_FILTER_PREFERENCE_PAGE);
 	}
 
 	/**
 	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
 	 */
 	protected void createFieldEditors() {
 		GridData gd;
 		Label label;
 		
 		// Create header images
 		createSpacer(getFieldEditorParent(), 1);
 		for (int i = 0; i < JDIDebugUIPlugin.fgAccessModifierNames.length; i++) {
 			label = new Label(getFieldEditorParent(), SWT.NONE);
 			label.setImage(fgAccessImages[i]);
 			gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
 			label.setLayoutData(gd);
 		}		
 				
 		// Create column headers for the checkbox table
 		createSpacer(getFieldEditorParent(), 1);
 		for (int i = 0; i < JDIDebugUIPlugin.fgAccessModifierNames.length; i++) {
 			label = new Label(getFieldEditorParent(), SWT.NONE);
 			label.setText(JDIDebugUIPlugin.fgAccessModifierNames[i]);
 			gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
 			label.setLayoutData(gd);
 		}
 
 		// Create one listener for all +/- buttons
 		PlusMinusButtonListener buttonListener = new PlusMinusButtonListener();
 		
 		// Create the +/- buttons for the columns
 		createSpacer(getFieldEditorParent(), 1);
 		for (int i = 0; i < JDIDebugUIPlugin.fgAccessModifierNames.length; i++) {
 			Composite buttonComp = createPlusMinusButtons(getFieldEditorParent(), true, buttonListener, i);
 			gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
 			buttonComp.setLayoutData(gd);
 		}
 		
 		// Create the rows of the checkbox table
 		for (int i = 0; i < JDIDebugUIPlugin.fgModeModifierNames.length; i++) {
 			
 			// Create a container for the row label and +/- buttons
 			Composite rowComp = new Composite(getFieldEditorParent(), SWT.NONE);
 			GridLayout rowLayout = new GridLayout();
 			rowLayout.numColumns = 2;
 			rowLayout.marginHeight = 0;
 			rowLayout.marginWidth = 0;
 			rowComp.setLayout(rowLayout);
 			gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
 			rowComp.setLayoutData(gd);
 			
 			// Create the row label
 			label = new Label(rowComp, SWT.NONE);
 			label.setText(JDIDebugUIPlugin.fgModeModifierNames[i]);
 			
 			// Create the +/- buttons
 			Composite buttonComp = createPlusMinusButtons(rowComp, false, buttonListener, i);
 			gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 			buttonComp.setLayoutData(gd);
 			
 			// Create the checkboxes for the row
 			for (int j = 0; j < JDIDebugUIPlugin.fgAccessModifierNames.length; j++) {
 				String prefName = JDIDebugUIPlugin.generateVariableFilterPreferenceName(i, j);
 				CenterAlignedBooleanFieldEditor bfe = new CenterAlignedBooleanFieldEditor(prefName, EMPTY_STRING, getFieldEditorParent());
 				addField(bfe);
 				fCheckboxes[i][j] = bfe;
 			}
 		}	
 		
 		createSpacer(getFieldEditorParent(), JDIDebugUIPlugin.fgAccessModifierNames.length + 1);
 		
 		// Create a group for the 3 primitive display options
 		Group primitiveGroup = new Group(getFieldEditorParent(), SWT.NONE);
 		GridLayout primitiveLayout = new GridLayout();
 		primitiveGroup.setLayout(primitiveLayout);
 		gd = new GridData(GridData.FILL_BOTH);
 		gd.horizontalSpan = JDIDebugUIPlugin.fgAccessModifierNames.length + 1;
 		primitiveGroup.setLayoutData(gd);
 		primitiveGroup.setText(DebugUIMessages.getString("JavaDebugPreferencePage.Primitive_type_display_options_2")); //$NON-NLS-1$
 
 		// Create the 3 primitive display checkboxes
 		BooleanFieldEditor hexCheckbox = new BooleanFieldEditor(IJDIPreferencesConstants.PREF_SHOW_HEX_VALUES, DebugUIMessages.getString("JavaDebugPreferencePage.Display_&hexadecimal_values_(byte,_short,_char,_int,_long)_3"), primitiveGroup);	//$NON-NLS-1$	
 		addField(hexCheckbox);
 		BooleanFieldEditor charCheckbox = new BooleanFieldEditor(IJDIPreferencesConstants.PREF_SHOW_CHAR_VALUES, DebugUIMessages.getString("JavaDebugPreferencePage.Display_ASCII_&character_values_(byte,_short,_int,_long)_4"), primitiveGroup); //$NON-NLS-1$
 		addField(charCheckbox);
 		BooleanFieldEditor unsignedCheckbox = new BooleanFieldEditor(IJDIPreferencesConstants.PREF_SHOW_UNSIGNED_VALUES, DebugUIMessages.getString("JavaDebugPreferencePage.Display_&unsigned_values_(byte)_5"), primitiveGroup); //$NON-NLS-1$
 		addField(unsignedCheckbox);
 
 		createSpacer(getFieldEditorParent(), JDIDebugUIPlugin.fgAccessModifierNames.length + 1);
 	}
 	
 	/**
 	 * Create +/- buttons, put them in a new Composite and return that
 	 * Composite.  
 	 */
 	private Composite createPlusMinusButtons(Composite parent, boolean column, PlusMinusButtonListener buttonListener, int rowCol) {
 		Composite buttonComp = new Composite(parent, SWT.NONE);
 		GridLayout buttonLayout = new GridLayout();
 		buttonLayout.numColumns = 2;
 		buttonLayout.marginHeight = 0;
 		buttonLayout.marginWidth = 5;
 		buttonComp.setLayout(buttonLayout);
 		
 		Boolean columnBoolean = column ? Boolean.TRUE : Boolean.FALSE;
 		Integer rowColInteger = new Integer(rowCol);
 		
 		Button plusButton = new Button(buttonComp, SWT.PUSH);
 		plusButton.setImage(fgPlusSignImage);
 		plusButton.setData(PLUS_KEY, Boolean.TRUE);
 		plusButton.setData(COLUMN_KEY, columnBoolean);
 		plusButton.setData(ROW_COL_KEY, rowColInteger);
 		plusButton.addSelectionListener(buttonListener);
 		
 		Button minusButton = new Button(buttonComp, SWT.PUSH);
 		minusButton.setImage(fgMinusSignImage);
 		minusButton.setData(PLUS_KEY, Boolean.FALSE);
 		minusButton.setData(COLUMN_KEY, columnBoolean);
 		minusButton.setData(ROW_COL_KEY, rowColInteger);
 		minusButton.addSelectionListener(buttonListener);		
 	
 		return buttonComp;
 	}
 
 	/**
 	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
 	 */
 	public boolean performOk() {
 		boolean ok= super.performOk();
 		JDIDebugUIPlugin.getDefault().savePluginPreferences();
 		JavaVariablesFilterPreferenceAction.applyFilterToViewers();
 		return ok;
 	}
 
 	/**
 	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#adjustGridLayout()
 	 */
 	protected void adjustGridLayout() {
 		GridLayout gridLayout = (GridLayout)getFieldEditorParent().getLayout();
 		gridLayout.numColumns = JDIDebugUIPlugin.fgAccessModifierNames.length + 1;
 		gridLayout.horizontalSpacing = HORIZONTAL_GRID_SPACING;
 		gridLayout.verticalSpacing = VERTICAL_GRID_SPACING;
 	}
 
 	private void createSpacer(Composite parent, int numColumns) {
 		Label label = new Label(parent, SWT.NONE);
 		GridData gd = new GridData();
 		gd.horizontalSpan = numColumns;
 		label.setLayoutData(gd);
 	}
	
	/**
	 * By default, ALL filter options are set on.
	 */
	public static void initDefaults(IPreferenceStore store) {
		for (int row = 0; row < JDIDebugUIPlugin.fgModeModifierNames.length; row++) {
			for (int col = 0; col < JDIDebugUIPlugin.fgAccessModifierNames.length; col++) {
				String prefName = JDIDebugUIPlugin.generateVariableFilterPreferenceName(row, col);
				store.setDefault(prefName, true);
			}
		}
	}
 
 	private void setViewer(StructuredViewer viewer) {
 		fViewer = viewer;
 	}
 
 	private StructuredViewer getViewer() {
 		return fViewer;
 	}
 
 }
