 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.constructorsOnlyInvokeFinalMethods, useForLoop, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.disallowReturnMutable, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity, com.instantiations.assist.eclipse.analysis.pathManipulation, explicitThisUsage
 /*******************************************************************************
  * Copyright (c) 2010 Ericsson Research Canada
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class implements an SWT ListEditor extension to select multiple file 
  * paths in preferences pages
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  *******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.editors;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 import org.eclipse.jface.preference.ListEditor;
import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.List;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class FilePathEditor extends ListEditor {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field ADD_BUTTON_REPL_LABEL. (value is ""Add"")
 	 */
 	private static final String ADD_BUTTON_REPL_LABEL = "Add";
 
 	/**
 	 * Invalid List index
 	 */
 	public static final int INVALID_INDEX = -1;
 
 	// ------------------------------------------------------------------------
 	// Member variables
 	// ------------------------------------------------------------------------
 
 	/**
 	 * The last file path, or <code>null if none.
 	 */
 	private String fLastFilePath;
 
 	/**
 	 * The files extensions for the file filter, or <code>null if none.
 	 */
 	private String[] fFileExtensions;
 
 	// ------------------------------------------------------------------------
 	// Constructors
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Creates a new file path field editor
 	 */
 	protected FilePathEditor() { // $codepro.audit.disable emptyMethod
 		//Empty constructor
 	}
 
 	/**
 	 * Creates a file path field editor.
 	 * 
 	 * @param aName
 	 *            - the name of the preference this field editor works on
 	 * @param aLabelText
 	 *            - the label text of the field editor
 	 * @param aFileExtensions
 	 *            - the allowable files extensions to use in filter
 	 * @param aParent
 	 *            - the parent of the field editor's control
 	 */
 	public FilePathEditor(String aName, String aLabelText, String[] aFileExtensions, Composite aParent) {
 		init(aName, aLabelText);
 		fFileExtensions = aFileExtensions.clone();
 		createControl(aParent);
 		getAddButton().setText(ADD_BUTTON_REPL_LABEL);
 	}
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Creates a single string from the given array by separating each string with the appropriate OS-specific path
 	 * separator.
 	 * 
 	 * @param aItems
 	 *            String[]
 	 * @return String
 	 */
 	@Override
 	protected String createList(String[] aItems) {
 		final StringBuffer path = new StringBuffer("");
 
 		for (String item : aItems) {
 			path.append(item);
 			path.append(File.pathSeparator);
 		}
 		return path.toString();
 	}
 
 	/**
 	 * Creates a new file path element by means of a file dialog.
 	 * 
 	 * @return String
 	 */
 	@Override
 	protected String getNewInputObject() {
		final FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
 		dialog.setFilterExtensions(fFileExtensions);
 
 		if (null != fLastFilePath) {
 			if (new File(fLastFilePath).exists()) {
 				dialog.setFilterPath(fLastFilePath);
 			}
 		}
 		String file = dialog.open();
 		if (null != file) {
 			file = file.trim();
 			if (0 == file.length()) {
 				return null;
 			} else if (getList().indexOf(file) != INVALID_INDEX) {
 				return null; //duplicate entries
 			}
 			fLastFilePath = file;
 		}
 		return file;
 	}
 
 	/**
 	 * Method parseString.
 	 * 
 	 * @param aStringList
 	 *            String
 	 * @return String[]
 	 */
 	@Override
 	protected String[] parseString(String aStringList) {
 		final StringTokenizer st = new StringTokenizer(aStringList, File.pathSeparator
 				+ System.getProperty("line.separator"));
 		final ArrayList<String> stringArray = new ArrayList<String>();
 		while (st.hasMoreElements()) {
 			stringArray.add((String) st.nextElement());
 		}
 		return stringArray.toArray(new String[stringArray.size()]);
 	}
 
 	/**
 	 * Method getSelection.
 	 * 
 	 * @return String
 	 */
 	public String getSelection() {
 		List list = getList();
 		if (null != list) {
 			String[] selections = list.getSelection();
 			if (null != selections && 0 < selections.length) {
 				return selections[0];
 			}
 		}
 		return null;
 	}
 }
