/*******************************************************************************
 * Copyright (c) Gil Barash - chookapp@yahoo.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gil Barash - initial API and implementation
 *    
 *******************************************************************************/

 package com.chookapp.org.overviewrulerbackground.preferences;
 
 import org.eclipse.jface.preference.*;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 import org.eclipse.ui.IWorkbench;
 
 import com.chookapp.org.overviewrulerbackground.Activator;
 
 /**
  * This class represents a preference page that
  * is contributed to the Preferences dialog. By 
  * subclassing <samp>FieldEditorPreferencePage</samp>, we
  * can use the field support built into JFace that allows
  * us to create a page that is small and knows how to 
  * save, restore and apply itself.
  * <p>
  * This page is used to modify preferences only. They
  * are stored in the preference store that belongs to
  * the main plug-in class. That way, preferences can
  * be accessed directly via the preference store.
  */
 
 public class OverviewRulerBackgroundPref
 	extends FieldEditorPreferencePage
 	implements IWorkbenchPreferencePage {
 
 	FieldEditor _colorFiled;
 	
 	public OverviewRulerBackgroundPref() {
 		super(GRID);
 		setPreferenceStore(Activator.getDefault().getPreferenceStore());
 		setDescription(Messages.OverviewRulerBackgroundPref_Description);
 	}
 	
 	/**
 	 * Creates the field editors. Field editors are abstractions of
 	 * the common GUI blocks needed to manipulate various types
 	 * of preferences. Each field editor knows how to save and
 	 * restore itself.
 	 */
 	public void createFieldEditors() {
 		addField(
 			new BooleanFieldEditor(
 				PreferenceConstants.P_SYS_DEF,
 				Messages.OverviewRulerBackgroundPref_SysDefault,
 				getFieldEditorParent()));
 
 		_colorFiled = new ColorFieldEditor(
 				PreferenceConstants.P_COLOR, 
 				Messages.OverviewRulerBackgroundPref_Color, 
 				getFieldEditorParent());
 
 		addField(_colorFiled);
 		
 		if(getPreferenceStore().getBoolean(PreferenceConstants.P_SYS_DEF))
 			_colorFiled.setEnabled(false, getFieldEditorParent());
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent event) {
 		super.propertyChange(event);
 		
 		setMessage(Messages.OverviewRulerBackgroundPref_Warning, INFORMATION);
 		
 		BooleanFieldEditor source;
 		if( event.getSource() instanceof BooleanFieldEditor )
 			source = (BooleanFieldEditor) event.getSource();
 		else
 			return;
 		
 		if (source.getPreferenceName().equals(PreferenceConstants.P_SYS_DEF))
 		{
 			boolean enableColorFiled = (event.getNewValue() == Boolean.FALSE);
 			_colorFiled.setEnabled(enableColorFiled, getFieldEditorParent());
 		}			
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
 	 */
 	public void init(IWorkbench workbench) {
 	}
 	
 }
