 /*******************************************************************************
  * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     Matthew Conway - initial API and implementation
  *     IBM Corporation - concepts and ideas taken from Eclipse code
  *     Gunnar Wagenknecht - reworked to Eclipse 3.0 API and code clean-up
  *     Tobias Sodergren - added preferences for job priority
  *******************************************************************************/
 package net.sourceforge.eclipseccase.ui.preferences;
 
 import org.eclipse.jface.util.PropertyChangeEvent;
 
 import org.eclipse.jface.util.IPropertyChangeListener;
 
 import net.sourceforge.eclipseccase.ClearCasePreferences;
 
 import org.eclipse.jface.dialogs.MessageDialog;
 
 import net.sourceforge.eclipseccase.ClearCasePlugin;
 import net.sourceforge.eclipseccase.IClearCasePreferenceConstants;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.preference.*;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 
 /**
  * The main preference page for the Eclipse ClearCase integration.
  */
 public class ClearCasePreferencePage extends FieldEditorPreferencePageWithCategories implements IWorkbenchPreferencePage, IClearCasePreferenceConstants {
 
 	private static final String GENERAL = PreferenceMessages.getString("Preferences.Category.General"); //$NON-NLS-1$
 
 	private static final String SOURCE_MANAGEMENT = PreferenceMessages.getString("Preferences.Category.Source"); //$NON-NLS-1$
 
 	private static final String COMMENTS = PreferenceMessages.getString("Preferences.Category.Comments"); //$NON-NLS-1$
 
 	private static final String[] CATEGORIES = new String[] { GENERAL, SOURCE_MANAGEMENT, COMMENTS };
 
 	static final String[][] ALWAYS_NEVER_PROMPT = new String[][] { { PreferenceMessages.getString("Always"), ALWAYS }, //$NON-NLS-1$
 			{ PreferenceMessages.getString("Never"), NEVER }, //$NON-NLS-1$
 			{ PreferenceMessages.getString("Prompt"), PROMPT } }; //$NON-NLS-1$
 
 	static final String[][] ALWAYS_IF_POSSIBLE_NEVER = new String[][] { { PreferenceMessages.getString("Always"), ALWAYS }, //$NON-NLS-1$
 			{ PreferenceMessages.getString("IfPossible"), IF_POSSIBLE }, //$NON-NLS-1$
 			{ PreferenceMessages.getString("Never"), NEVER } }; //$NON-NLS-1$
 
 	static final String[][] PRIORITIES = new String[][] { { PreferenceMessages.getString("HighPriority"), Integer.toString(Job.LONG) }, //$NON-NLS-1$ 
 			{ PreferenceMessages.getString("DefaultPriority"), Integer.toString(Job.DECORATE) } }; //$NON-NLS-1$
 
 	private RadioGroupFieldEditor reservedCo;
 
 	private BooleanFieldEditor nMaster;
 
 	/**
 	 * Creates a new instance.
 	 */
 	public ClearCasePreferencePage() {
 		setDescription(PreferenceMessages.getString("Preferences.Description")); //$NON-NLS-1$
 
 		// Set the preference store for the preference page.
 		setPreferenceStore(new ClearCasePreferenceStore());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
 	 */
 	public void init(IWorkbench workbench) {
 		// ignore
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors
 	 * ()
 	 */
 	@Override
 	protected void createFieldEditors() {
 
 		// general settings
 		addField(new StringFieldEditor(CLEARCASE_PRIMARY_GROUP, PreferenceMessages.getString("Preferences.General.ClearCasePrimaryGroup"), //$NON-NLS-1$
 				getFieldEditorParent(GENERAL)));
 
 		addField(new BooleanFieldEditor(PREVENT_UNNEEDED_CHILDREN_REFRESH, PreferenceMessages.getString("Preferences.General.RefreshChildren"), //$NON-NLS-1$
 				getFieldEditorParent(GENERAL)));
 
 		//		addField(new BooleanFieldEditor(IGNORE_NEW, PreferenceMessages.getString("Preferences.General.IgnoreNew"), //$NON-NLS-1$
 		// getFieldEditorParent(GENERAL)));
 
 		addField(new BooleanFieldEditor(HIDE_REFRESH_STATE_ACTIVITY, PreferenceMessages.getString("Preferences.General.HideRefreshStateActivity"), //$NON-NLS-1$
 				getFieldEditorParent(GENERAL)));
 
 		//		addField(new BooleanFieldEditor(RECURSIVE, PreferenceMessages.getString("Preferences.General.Recursive"), //$NON-NLS-1$
 		// getFieldEditorParent(GENERAL)));
 
 		//		addField(new BooleanFieldEditor(PRESERVE_TIMES, PreferenceMessages.getString("Preferences.General.PreserveTimes"), //$NON-NLS-1$
 		// getFieldEditorParent(GENERAL)));
 
 		//		addField(new BooleanFieldEditor(TEST_LINKED_PARENT_IN_CLEARCASE, PreferenceMessages.getString("Preferences.General.TestLinkedParentInClearCase"), //$NON-NLS-1$
 		// getFieldEditorParent(GENERAL)));
 
 		//		addField(new RadioGroupFieldEditor(SAVE_DIRTY_EDITORS, PreferenceMessages.getString("Preferences.General.SaveDirtyEditors"), 1, //$NON-NLS-1$
 		// ALWAYS_NEVER_PROMPT, getFieldEditorParent(GENERAL), true));
 
 		addField(new BooleanFieldEditor(USE_CLEARDLG, PreferenceMessages.getString("Preferences.Source.ClearDlg"), //$NON-NLS-1$
 				getFieldEditorParent(GENERAL)));
 
 		addField(new BooleanFieldEditor(FULL_REFRESH, PreferenceMessages.getString("Preferences.Source.FullRefreshOnAssoc"), //$NON-NLS-1$
 				getFieldEditorParent(GENERAL)));
 
 		addField(new RadioGroupFieldEditor(JOB_QUEUE_PRIORITY, PreferenceMessages.getString("Preferences.General.JobQueuePriority"), 1, //$NON-NLS-1$
 				PRIORITIES, getFieldEditorParent(GENERAL), true));
 
 		// general settings
 		addField(new StringFieldEditor(TIMEOUT_GRAPHICAL_TOOLS, PreferenceMessages.getString("Preferences.General.GraphicalTimeout"), //$NON-NLS-1$
 				getFieldEditorParent(GENERAL)));
 
 		addField(new BooleanFieldEditor(GRAPHICAL_EXTERNAL_UPDATE_VIEW, PreferenceMessages.getString("Preferences.General.GraphicalUpdateView"), //$NON-NLS-1$
 				getFieldEditorParent(GENERAL)));
 
 		addField(new BooleanFieldEditor(FORBID_CONFIG_SPEC_MODIFICATION, PreferenceMessages.getString("Preferences.General.ModifyConfigSpec"), //$NON-NLS-1$
 				getFieldEditorParent(GENERAL)));
 
 		//		addField(new BooleanFieldEditor(COMPARE_EXTERNAL, PreferenceMessages.getString("Preferences.General.CompareWithExternalTool"), //$NON-NLS-1$
 		// getFieldEditorParent(GENERAL)));
 
 		// RadioGroupFieldEditor clearcaseLayer = new
 		// RadioGroupFieldEditor(CLEARCASE_API,
 		// "Interface for ClearCase operations",1,
 		// new String[][]{
 		// {"Native - CAL (ClearCase Automation Library)", CLEARCASE_NATIVE},
 		// {"Native - cleartool executable", CLEARCASE_CLEARTOOL},
 		// {"Compatible - ClearDlg executable", CLEARCASE_CLEARDLG}
 		// }
 		// ,getFieldEditorParent(GENERAL),true);
 		// addField(clearcaseLayer);
 
 		// source management
 
 		// addField(new BooleanFieldEditor(CHECKOUT_AUTO, PreferenceMessages
 		// .getString("Preferences.Source.CheckoutAuto"), //$NON-NLS-1$
 		// getFieldEditorParent(SOURCE_MANAGEMENT)));
 
 		addField(new RadioGroupFieldEditor(CHECKOUT_AUTO, PreferenceMessages.getString("Preferences.Source.CheckoutAuto"), //$NON-NLS-1$
 				3, ALWAYS_NEVER_PROMPT, getFieldEditorParent(SOURCE_MANAGEMENT), true));
 
 		addField(new BooleanFieldEditor(AUTO_PARENT_CHECKIN_AFTER_MOVE, PreferenceMessages.getString("Preferences.Source.AutoParentCheckinAfterMove"), getFieldEditorParent(SOURCE_MANAGEMENT)));
 
 		addField(new BooleanFieldEditor(CHECKIN_IDENTICAL, PreferenceMessages.getString("Preferences.Source.CheckinIdentical"), //$NON-NLS-1$
 				getFieldEditorParent(SOURCE_MANAGEMENT)));
 
 		addField(new BooleanFieldEditor(KEEP_CHANGES_AFTER_UNCHECKOUT, PreferenceMessages.getString("Preferences.Source.KeepChangesAfterUncheckout"), //$NON-NLS-1$
 				getFieldEditorParent(SOURCE_MANAGEMENT)));
 
 		addField(new BooleanFieldEditor(ADD_WITH_CHECKIN, PreferenceMessages.getString("Preferences.Source.AddWithCheckin"), //$NON-NLS-1$
 				getFieldEditorParent(SOURCE_MANAGEMENT)));
 
 		addField(new BooleanFieldEditor(CHECKOUT_LATEST, PreferenceMessages.getString("Preferences.Source.CheckoutLatest"), //$NON-NLS-1$
 				getFieldEditorParent(SOURCE_MANAGEMENT)));
 
 		reservedCo = new RadioGroupFieldEditor(IClearCasePreferenceConstants.CHECKOUT_RESERVED, PreferenceMessages.getString("Preferences.Source.CheckoutReserved"), 3, //$NON-NLS-1$ 
 				ALWAYS_IF_POSSIBLE_NEVER, getFieldEditorParent(SOURCE_MANAGEMENT), true);
 
 		addField(reservedCo);
 
 		// Avoid that -nmaster is set when RESERVED checkouts are used.
 		//		nMaster = new BooleanFieldEditor(ADD_WITH_MASTER, PreferenceMessages.getString("Preferences.Source.AddWithMaster"), //$NON-NLS-1$
 		// getFieldEditorParent(SOURCE_MANAGEMENT));
 
 		nMaster = new BooleanFieldEditor(ADD_WITH_MASTER, PreferenceMessages.getString("Preferences.Source.AddWithMaster"), //$NON-NLS-1$
 				getFieldEditorParent(SOURCE_MANAGEMENT)) {
 			protected void valueChanged(boolean oldValue, boolean newValue) {
 				setReservedCheckoutEnabledState(oldValue, newValue);
 			}
 
 		};
 
 		addField(nMaster);
 
 		// comment settings
 
 		addField(new BooleanFieldEditor(SILENT_PREVENT, PreferenceMessages.getString("Preferences.Source.SilentPrevent"), //$NON-NLS-1$
 				getFieldEditorParent(SOURCE_MANAGEMENT)));
 		addField(new StringFieldEditor(PREVENT_CHECKOUT, PreferenceMessages.getString("Preferences.Source.PreventCheckOut"), //$NON-NLS-1$
 				getFieldEditorParent(SOURCE_MANAGEMENT)));
 
 		// comment settings
 
 		addField(new BooleanFieldEditor(COMMENT_ADD, PreferenceMessages.getString("Preferences.Comments.CommentAdd"), //$NON-NLS-1$
 				getFieldEditorParent(COMMENTS)));
 
 		addField(new BooleanFieldEditor(COMMENT_CHECKIN, PreferenceMessages.getString("Preferences.Comments.CommentCheckin"), //$NON-NLS-1$
 				getFieldEditorParent(COMMENTS)));
 
 		addField(new BooleanFieldEditor(COMMENT_CHECKOUT, PreferenceMessages.getString("Preferences.Comments.CommentCheckout"), //$NON-NLS-1$
 				getFieldEditorParent(COMMENTS)));
 
 		addField(new BooleanFieldEditor(COMMENT_CHECKOUT_NEVER_ON_AUTO, PreferenceMessages.getString("Preferences.Comments.CommentCheckoutNeverOnAuto"), //$NON-NLS-1$ 
 				getFieldEditorParent(COMMENTS)));
 
 		addField(new StringFieldEditor(BRANCH_PREFIX, PreferenceMessages.getString("Preferences.Comments.BranchPrefix"), //$NON-NLS-1$
 				getFieldEditorParent(COMMENTS)));
 
 		//		addField(new BooleanFieldEditor(COMMENT_ADD_NEVER_ON_AUTO, PreferenceMessages.getString("Preferences.Comments.CommentAddNeverOnAuto"), //$NON-NLS-1$ 
 		// getFieldEditorParent(COMMENTS)));
 
 		//		addField(new BooleanFieldEditor(COMMENT_ESCAPE, PreferenceMessages.getString("Preferences.Comments.CommentEscapeXml"), //$NON-NLS-1$
 		// getFieldEditorParent(COMMENTS)));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
 	 */
 	@Override
 	public boolean performOk() {
 		if (super.performOk()) {
 			ClearCasePlugin.getDefault().resetClearCase();
 			return true;
 		}
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * net.sourceforge.eclipseccase.ui.preferences.TabFieldEditorPreferencePage
 	 * #getCategories()
 	 */
 	@Override
 	protected String[] getCategories() {
 		return CATEGORIES;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @seenet.sourceforge.eclipseccase.ui.preferences.
 	 * FieldEditorPreferencePageWithCategories#getDescription(java.lang.String)
 	 */
 	@Override
 	protected String getDescription(String category) {
 		// if (GENERAL.equals(category))
 		//			return PreferenceMessages.getString("Preferences.Description.Category.General"); //$NON-NLS-1$
 		// if (SOURCE_MANAGEMENT.equals(category))
 		//			return PreferenceMessages.getString("Preferences.Description.Category.Source"); //$NON-NLS-1$
 		// if (COMMENTS.equals(category))
 		//			return PreferenceMessages.getString("Preferences.Description.Category.Comments"); //$NON-NLS-1$
 		return null;
 	}
 
 	private void setReservedCheckoutEnabledState(boolean oldValue, boolean newValue) {
 		if (newValue && reservedCo.getPreferenceStore().getString(IClearCasePreferenceConstants.CHECKOUT_RESERVED).equals(IClearCasePreferenceConstants.ALWAYS)) {
 
 		} else {
 
 			getPreferenceStore().setValue(IClearCasePreferenceConstants.ADD_WITH_MASTER, newValue);
 			nMaster.store();
 			nMaster.load();
 		}
 
 	}
 
 	/**
 	 * This is used to handle changes in reservedCo, RadioGroupFieldEditor.
 	 * 
 	 * 
 	 * @param event
 	 *            the property change event object describing which property
 	 *            changed and how
 	 */
 	public void propertyChange(PropertyChangeEvent event) {
 		super.propertyChange(event);
 		if (event.getSource().equals(reservedCo)) {
 			setNmasterEnabledState((String) event.getOldValue(), (String) event.getNewValue());
 
 		}
 
 	}
 
 	private void setNmasterEnabledState(String oldValue, String newValue) {
 		
 		if(oldValue.equals(newValue)){
 			//Avoid same value. This happens each time a value is set, Bug in eclipse?
 			return;
 		}
		if (newValue.equals(IClearCasePreferenceConstants.ALWAYS) | newValue.equals(IClearCasePreferenceConstants.IF_POSSIBLE)) {
 
 			nMaster.setEnabled(false, getFieldEditorParent(SOURCE_MANAGEMENT));
 			if (getPreferenceStore().getBoolean(IClearCasePreferenceConstants.ADD_WITH_MASTER) == true) {
 				//load changes value.
 				reservedCo.load();
 				getPreferenceStore().setValue(IClearCasePreferenceConstants.CHECKOUT_RESERVED, oldValue);
 				//move back to old value.
 				reservedCo.load();
 				reservedCo.store();
				MessageDialog.openError(getShell(), "Error", "Since master option is set. \"Reserved Checkouts\" option cannot be set to ALWAYS/IF POSSIBLE since reserved checkouts are not allowed.");
 				nMaster.setEnabled(true, getFieldEditorParent(SOURCE_MANAGEMENT));
 			}
 		} else {
 			nMaster.setEnabled(true, getFieldEditorParent(SOURCE_MANAGEMENT));
 		}
 
 		
 	}
 
 }
