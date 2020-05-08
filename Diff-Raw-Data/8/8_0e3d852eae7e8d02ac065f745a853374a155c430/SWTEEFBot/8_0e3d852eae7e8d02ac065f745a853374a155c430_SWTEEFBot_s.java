 /*******************************************************************************
  * Copyright (c) 2008, 2010 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.runtime.tests.swtbot.finder;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EEnum;
 import org.eclipse.emf.ecore.EEnumLiteral;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.ui.EMFEditUIPlugin;
 import org.eclipse.emf.eef.runtime.EEFRuntimePlugin;
 import org.eclipse.emf.eef.runtime.tests.exceptions.WidgetInvalidException;
 import org.eclipse.emf.eef.runtime.tests.swtbot.utils.SWTBotUtils;
 import org.eclipse.emf.eef.runtime.tests.utils.EEFTestsResourceUtils;
 import org.eclipse.emf.eef.runtime.tests.utils.UIConstants;
 import org.eclipse.emf.eef.runtime.ui.utils.EEFRuntimeUIMessages;
 import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
 import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
 import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
 import org.eclipse.swtbot.swt.finder.SWTBot;
 import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
 import org.eclipse.swtbot.swt.finder.waits.Conditions;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
 
 /**
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  */
 public class SWTEEFBot extends SWTWorkbenchBot {
 
 	/*****************************************************************************
 	 * * Bot members * *
 	 *****************************************************************************/
 
 	private Resource activeResource = null;
 
 	/**
 	 * @return the URI of the input model
 	 */
 	public String getInputModelURI() {
 		if (activeResource != null)
 			return activeResource.getURI().toString();
 		return null;
 	}
 
 	/**
 	 * @return the active resource
 	 */
 	public Resource getActiveResource() {
 		return activeResource;
 	}
 
 	/*****************************************************************************
 	 * * Bot settings * *
 	 *****************************************************************************/
 
 	public void defineActiveModel(Resource resource) {
 		this.activeResource = resource;
 	}
 
 	public void unloadActiveModel() {
 		this.activeResource.unload();
 	}
 
 	public void reloadActiveModel() throws IOException {
 		this.activeResource.unload();
 		this.activeResource.load(Collections.EMPTY_MAP);
 	}
 
 	/*****************************************************************************
 	 * * Bot operation * *
 	 *****************************************************************************/
 
 	/**
 	 * Get another literal value of the enumeration
 	 */
 	public Object changeEnumLiteralValue(EEnum enumeration, String text) {
 		for (EEnumLiteral eEnumLiteral : enumeration.getELiterals()) {
 			if (!eEnumLiteral.getLiteral().equals(text.toString())) {
 				return eEnumLiteral.getLiteral();
 			}
 		}
 		return text;
 	}
 
 	/**
 	 * Get another reference value of the references list
 	 */
 	public Object changeReferenceValue(List<EObject> eObjectList, EObject value) {
 		for (EObject eobj : eObjectList) {
 			if (!eobj.equals(value)) {
 				return eobj;
 			}
 		}
 		return value;
 	}
 
 	/**
 	 * Get another reference value of the references list
 	 */
 	public Object changeReferenceValue(List<EObject> eObjectList, List values) {
 		for (EObject eobj : eObjectList) {
 			if (!values.contains(eobj)) {
 				return eobj;
 			}
 		}
 		if (!values.isEmpty()) {
 			return values.get(0);
 		}
 		return null;
 	}
 
 	/**
 	 * Must open the file with EEF Reflexive editor -> SWTBot pb with context menu
 	 * @param file
 	 *            the file to open
 	 * @return the editor editing the file
 	 */
 	public SWTBotEditor openFile(IFile file) {
 		SWTBotTree wizardTree = viewByTitle(UIConstants.PACKAGE_EXPLORER_VIEW_NAME).bot().tree();
 		sleep(500);
 		List<IResource> expansionPath = getExpansionPath(file);
 		Iterator<IResource> iter = expansionPath.iterator();
 		if (iter.hasNext()) {
 			String text = getNodeText(iter.next());
 			SWTBotTreeItem treeItem = wizardTree.expandNode(text).expand();
 			while (iter.hasNext()) {
 				text = getNodeText(iter.next());
 				treeItem = treeItem.getNode(text);
 				treeItem.expand();
 			}
 			treeItem.select();
 			menu(UIConstants.NAVIGATE_MENU).menu(UIConstants.OPEN_MENU).click();
 			sleep(500);
 			SWTBotEditor editor = editorByTitle(activeResource.getURI().lastSegment());
 			return editor;
 		}
 		return null;
 	}
 
 	/**
 	 * Open the editor on the input model
 	 * 
 	 * @return the associated editor
 	 */
 	public SWTBotEditor openActiveModel() {
 		IFile file = EEFTestsResourceUtils.workspaceFile(activeResource);
 		return openFile(file);
 	}
 
 	/**
 	 * Prepare the model editing
 	 * 
 	 * @param elementType
 	 *            The EClass of the edited element
 	 * @param element
 	 *            the element to edit
 	 * @return the shell of the opened wizard
 	 */
 	public SWTBotShell prepareBatchEditing(SWTBotEditor editor, EClass elementType, EObject element, String tabName) {
 		SWTBotTreeItem node2 = selectNode(editor, element);
 		node2.doubleClick();
 		cTabItem(tabName).activate();
 		cTabItem(tabName).setFocus();
 		sleep(1000);
 		return shell(elementType.getName());
 	}
 
 	/**
 	 * Prepare the model editing
 	 * 
 	 * @param editor
 	 *            The EClass of the edited element
 	 * @param element
 	 *            the element to edit
 	 * @return the shell of the opened wizard
 	 */
 	public SWTBotView prepareLiveEditing(SWTBotEditor editor, EObject element, String tabName) {
 		SWTBotTreeItem node2 = selectNode(editor, element);
 		node2.select();
 		SWTBotUtils.selectPropertyTabItem(tabName);
 		sleep(1000);
 		return viewByTitle(UIConstants.PROPERTIES_VIEW_NAME);
 	}
 
 	/**
 	 * Select the given element in the given editor
 	 * 
 	 * @param editor
 	 *            the editor where the bot must process
 	 * @param element
 	 *            the element to select
 	 * @return the selected node
 	 */
 	public SWTBotTreeItem selectNode(SWTBotEditor editor, EObject element) {
 		List<Object> expansionPath = getExpansionPath(element);
 		Iterator<Object> iterator = expansionPath.iterator();
 		Object next = null;
 		SWTBotTreeItem node2 = editor.bot().tree().getTreeItem(getInputModelURI());
 		while (iterator.hasNext()) {
 			node2.expand();
 			next = iterator.next();
 			node2 = selectSubNode(node2, next);
 		}
 		return node2;
 	}
 
 	/**
 	 * Select the given element in the given editor
 	 * 
 	 * @param editor
 	 *            the editor where the bot must process
 	 * @param element
 	 *            the element to select
 	 * @return the selected node
 	 */
 	public SWTBotTreeItem selectNode(SWTBotTree tree, EObject element) {
 		List<Object> expansionPath = getExpansionPath(element);
 		Iterator<Object> iterator = expansionPath.iterator();
 		Object next = null;
 		SWTBotTreeItem node2 = tree.getTreeItem(getInputModelURI());
 		while (iterator.hasNext()) {
 			node2.expand();
 			next = iterator.next();
 			node2 = selectSubNode(node2, next);
 		}
 		return node2;
 	}
 
 	/**
 	 * This method save the model and close the editor
 	 * 
 	 * @param editor
 	 *            the modelEditor
 	 */
 	public void finalizeEdition(SWTBotEditor editor) {
 		activateEclipseShell();
 		menu(UIConstants.FILE_MENU).menu(UIConstants.SAVE_MENU).click();
 		sleep(3000);
 		editor.close();
 	}
 
 	/*****************************************************************************
 	 * * EEF features editing testing * *
 	 *****************************************************************************/
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param feature
 	 *            the feature to edit
 	 * @param newValue
 	 *            the new value to set to the feature
 	 */
 	public void editTextFeature(SWTBotShell shell, String feature, Object newValue) {
 		activateShell(shell);
 		sleep(500);
 		editTextWithLabel(feature, newValue);
 		sleep(1000);
 		closeShellWithFinishButton(shell);
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param feature
 	 *            the feature to edit
 	 * @param newValue
 	 *            the new value to set to the feature
 	 */
 	public void editEMFComboViewerFeature(SWTBotShell shell, String feature, Object newValue) {
 		activateShell(shell);
 		sleep(500);
 		editEMFComboViewer(feature, newValue);
 		sleep(1000);
 		closeShellWithFinishButton(shell);
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param i
 	 *            the feature to edit
 	 * @param newValue
 	 *            the new value to set to the feature
 	 */
 	public void editRadioFeature(SWTBotShell shell, int i, Object newValue) {
 		activateShell(shell);
 		sleep(500);
 		editRadio(i, newValue);
 		sleep(1000);
 		closeShellWithFinishButton(shell);
 	}
 
 	/**
 	 * @param feature
 	 * @param newValue
 	 */
 	public void editEMFComboViewer(String feature, Object newValue) {
 		comboBoxWithLabel(feature).setSelection(newValue.toString());
 	}
 
 	/**
 	 * @param feature
 	 * @param newValue
 	 */
 	public void editRadio(int index, Object newValue) {
 		radio(newValue.toString(), index).click();
 	}
 
 	/**
 	 * @param shell
 	 */
 	public void activateShell(SWTBotShell shell) {
 		shell.activate();
 	}
 
 	/**
 	 * @param shell
 	 */
 	public void closeShellWithFinishButton(SWTBotShell shell) {
 		button(UIConstants.FINISH_BUTTON).click();
 		waitUntil(Conditions.shellCloses(shell));
 	}
 
 	/**
 	 * @param feature
 	 * @param newValue
 	 */
 	public void editTextWithLabel(String feature, Object newValue) {
 		textWithLabel(feature).setText(newValue.toString());
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param propertyView
 	 *            the properties view
 	 * @param feature
 	 *            the feature to edit
 	 * @param selectNode
 	 *            the SWTBotTreeItem in the treeview model
 	 */
 	public void editPropertyTextFeature(SWTBotView propertyView, String feature, Object newValue,
 			SWTBotTreeItem selectNode) {
 		SWTBot propertyBot = propertyView.bot();
 		propertyBot.textWithLabel(feature).setFocus();
 		propertyBot.textWithLabel(feature).setText(newValue.toString());
 		SWTBotUtils.pressEnterKey(propertyView.getWidget());
 		selectNode.select();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param feature
 	 *            the feature to edit
 	 * @param newValue
 	 *            the new value to set to the feature
 	 */
 	public void editCheckboxFeature(SWTBotShell shell, String feature) {
 		activateShell(shell);
 		sleep(500);
 		editCheckBox(feature);
 		sleep(1000);
 		closeShellWithFinishButton(shell);
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param feature
 	 *            the feature to edit
 	 * @param newValue
 	 *            the new value to set to the feature
 	 * @throws WidgetInvalidException 
 	 */
 	public void editEObjectFlatComboViewerFeature(SWTBotShell shell, int buttonIndex, int tableIndex) throws WidgetInvalidException {
 		activateShell(shell);
 		sleep(500);
 		editEObjectFlatComboViewer(buttonIndex, tableIndex);
 		sleep(1000);
 		closeShellWithFinishButton(shell);
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param feature
 	 *            the feature to edit
 	 * @param newValue
 	 *            the new value to set to the feature
 	 * @throws WidgetInvalidException 
 	 */
 	public void editFlatReferencesTableFeature(SWTBotShell shell, int buttonIndex, int tableIndex) throws WidgetInvalidException {
 		activateShell(shell);
 		sleep(500);
 		editFlatReferencesTable(buttonIndex, tableIndex);
 		sleep(1000);
 		closeShellWithFinishButton(shell);
 	}
 
 	public void editFlatReferencesTable(int buttonIndex, int tableIndex) throws WidgetInvalidException {
 		button(EEFRuntimeUIMessages.EObjectFlatComboViewer_add_button, buttonIndex).click();
 		try {
 		table().select(tableIndex);
 		button(EMFEditUIPlugin.INSTANCE.getString("_UI_Add_label")).click();
 		} catch (Exception e) {
 			// empty table
 			throw new WidgetInvalidException();
 		}
 		button(UIConstants.OK_BUTTON).click();
 	}
 
 	public void editEObjectFlatComboViewer(int buttonIndex, int tableIndex) throws WidgetInvalidException {
 		button(EEFRuntimeUIMessages.EObjectFlatComboViewer_add_button, buttonIndex).click();
 		try {
 			table().select(tableIndex);
 		} catch (Exception e) {
 			// empty table
 			throw new WidgetInvalidException();
 		}
 		button(UIConstants.OK_BUTTON).click();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param feature
 	 *            the feature to edit
 	 * @param newValue
 	 *            the new value to set to the feature
 	 * @throws WidgetInvalidException 
 	 */
 	public void editReferencesTableFeature(SWTBotShell shell, int buttonIndex, int tableIndex,
 			String buttonLabel) throws WidgetInvalidException {
 		activateShell(shell);
 		sleep(500);
 		editReferencesTable(buttonIndex, tableIndex, buttonLabel);
 		sleep(1000);
 		closeShellWithFinishButton(shell);
 	}
 
 	public void editReferencesTable(int buttonIndex, int tableIndex, String buttonLabel) throws WidgetInvalidException {
 		button(buttonLabel, buttonIndex).click();
 		try {
 		table().select(tableIndex);
 		} catch (Exception e) {
 			// empty table
 			throw new WidgetInvalidException();
 		}
 		button(UIConstants.OK_BUTTON).click();
 	}
 
 	public void removeReferencesTableFeature(SWTBotShell shell, int widgetIndex, int tableIndex,
 			String buttonLabel) throws WidgetInvalidException {
 		activateShell(shell);
 		sleep(500);
 		removeReferencesTable(widgetIndex, tableIndex, buttonLabel);
 		sleep(1000);
 		closeShellWithFinishButton(shell);
 	}
 
 	public void removeReferencesTable(int widgetIndex, int tableIndex, String buttonLabel) throws WidgetInvalidException {
 		try {
 			table(widgetIndex).select(tableIndex);
 			button(buttonLabel, widgetIndex).click();
 		} catch (Exception e) {
 			// empty table
 			throw new WidgetInvalidException();
 		}
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param feature
 	 *            the feature to edit
 	 * @param newValue
 	 *            the new value to set to the feature
 	 */
 	public void editAdvancedEObjectFlatComboViewerFeature(SWTBotShell shell, int buttonIndex, Object value) {
 		activateShell(shell);
 		sleep(500);
 		editAdvancedEObjectFlatComboViewer(buttonIndex, value);
 		sleep(1000);
 		closeShellWithFinishButton(shell);
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param feature
 	 *            the feature to edit
 	 * @param newValue
 	 *            the new value to set to the feature
 	 */
 	public void removeAdvancedEObjectFlatComboViewerFeature(SWTBotShell shell, int buttonIndex) {
 		activateShell(shell);
 		sleep(500);
 		removeAdvancedEObjectFlatComboViewer(buttonIndex);
 		sleep(1000);
 		closeShellWithFinishButton(shell);
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param feature
 	 *            the feature to edit
 	 * @param newValue
 	 *            the new value to set to the feature
 	 * @throws WidgetInvalidException 
 	 */
 	public void removeFlatReferencesTableFeature(SWTBotShell shell, int buttonIndex) throws WidgetInvalidException {
 		activateShell(shell);
 		sleep(500);
 		removeFlatReferencesTable(buttonIndex);
 		sleep(1000);
 		closeShellWithFinishButton(shell);
 	}
 	
 	public void removeFlatReferencesTable(int buttonIndex) throws WidgetInvalidException {
 		button(EEFRuntimeUIMessages.EObjectFlatComboViewer_add_button, buttonIndex).click();
 		try {
 		table(1).select(0);
 		button(EMFEditUIPlugin.INSTANCE.getString("_UI_Remove_label")).click();
 		} catch (Exception e) {
 			// empty table
 			throw new WidgetInvalidException();
 		}
 		button(UIConstants.OK_BUTTON).click();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param feature
 	 *            the feature to edit
 	 * @param newValue
 	 *            the new value to set to the feature
 	 * @throws WidgetInvalidException 
 	 */
 	public void removeEObjectFlatComboViewerFeature(SWTBotShell shell, int buttonIndex) throws WidgetInvalidException {
 		activateShell(shell);
 		sleep(500);
 		removeEObjectFlatComboViewer(buttonIndex);
 		sleep(1000);
 		closeShellWithFinishButton(shell);
 	}
 
 	public void removeEObjectFlatComboViewer(int buttonIndex) throws WidgetInvalidException {
 		button(EEFRuntimeUIMessages.EObjectFlatComboViewer_add_button, buttonIndex).click();
 	try {
 		table().select(0);
 	} catch (Exception e) {
 		// empty table
 		throw new WidgetInvalidException();
 	}
 		button(UIConstants.OK_BUTTON).click();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param feature
 	 *            the feature to edit
 	 * @param newValue
 	 *            the new value to set to the feature
 	 * @throws WidgetInvalidException 
 	 */
 	public void removePropertyFlatReferencesTableFeature(SWTBotView propertyView, int buttonIndex,
 			SWTBotTreeItem selectNode) throws WidgetInvalidException {
 		// SWTBot propertyBot = propertyView.bot();
 		removeEObjectFlatComboViewer(buttonIndex);
 		selectNode.select();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param feature
 	 *            the feature to edit
 	 * @param newValue
 	 *            the new value to set to the feature
 	 * @throws WidgetInvalidException 
 	 */
 	public void removePropertyEObjectFlatComboViewerFeature(SWTBotView propertyView, int buttonIndex,
 			SWTBotTreeItem selectNode) throws WidgetInvalidException {
 		// SWTBot propertyBot = propertyView.bot();
 		removeEObjectFlatComboViewer(buttonIndex);
 		selectNode.select();
 	}
 
 	public void removeAdvancedEObjectFlatComboViewer(int buttonIndex) {
 		buttonWithTooltip(EEFRuntimeUIMessages.AdvancedEObjectFlatComboViewer_remove_tooltip, buttonIndex)
 				.click();
 	}
 
 	public void editAdvancedEObjectFlatComboViewer(int buttonIndex, Object value) {
 		buttonWithTooltip(EEFRuntimeUIMessages.AdvancedEObjectFlatComboViewer_set_tooltip, buttonIndex)
 				.click();
 		cTabItem(EEFRuntimeUIMessages.TabElementTreeSelectionDialog_all_resources_tab_title).activate();
 		cTabItem(EEFRuntimeUIMessages.TabElementTreeSelectionDialog_all_resources_tab_title).setFocus();
 		selectNode(tree(), (EObject)value);
 		button(UIConstants.OK_BUTTON).click();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param feature
 	 *            the feature to edit
 	 * @param newValue
 	 *            the new value to set to the feature
 	 */
 	public void editAdvancedReferencesTableFeature(SWTBotShell shell, int buttonIndex, Object value) {
 		activateShell(shell);
 		sleep(500);
 		editAdvancedReferencesTable(buttonIndex, value);
 		sleep(1000);
 		closeShellWithFinishButton(shell);
 	}
 
 	public void editMultiValuedEditorFeature(SWTBotShell shell, String label, String value) {
 		activateShell(shell);
 		sleep(500);
 		editMultiValuedEditor(label, value);
 		sleep(1000);
 		closeShellWithFinishButton(shell);
 	}
 
 	public void editMultiValuedEditor(String label, List<String> values) {
 		button(label).click();
 		for (String string : values) {
 			text().setText(string);
 			button(EEFRuntimeUIMessages.EObjectFlatComboViewer_add_button).click();
 		}
 		button(UIConstants.OK_BUTTON).click();
 	}
 
 	public void editMultiValuedEditor(String label, String value) {
 		button(label).click();
 		text().setText(value);
		button(EEFRuntimeUIMessages.EObjectFlatComboViewer_add_button).click();
 		button(UIConstants.OK_BUTTON).click();
 	}
 
 	public void editAdvancedReferencesTable(int buttonIndex, Object value) {
 		buttonWithTooltip(EEFRuntimeUIMessages.ReferencesTable_add_tooltip, buttonIndex).click();
 		cTabItem(EEFRuntimeUIMessages.TabElementTreeSelectionDialog_all_resources_tab_title).activate();
 		cTabItem(EEFRuntimeUIMessages.TabElementTreeSelectionDialog_all_resources_tab_title).setFocus();
 		selectNode(tree(), (EObject)value);
 		button(UIConstants.OK_BUTTON).click();
 	}
 
 	public void editAdvancedReferencesTable(int buttonIndex, List values) {
 		for (Object value : values) {
 			editAdvancedReferencesTable(buttonIndex, value);
 		}
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param feature
 	 *            the feature to edit
 	 * @param newValue
 	 *            the new value to set to the feature
 	 * @throws WidgetInvalidException 
 	 */
 	public void removeAdvancedReferencesTableFeature(SWTBotShell shell, int buttonIndex, Object value) throws WidgetInvalidException {
 		activateShell(shell);
 		sleep(500);
 		removeAdvancedReferencesTable(buttonIndex, value);
 		sleep(1000);
 		closeShellWithFinishButton(shell);
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param feature
 	 *            the feature to edit
 	 * @param newValue
 	 *            the new value to set to the feature
 	 * @throws WidgetInvalidException
 	 */
 	public void removeAdvancedTableCompositionFeature(SWTBotShell shell, int buttonIndex)
 			throws WidgetInvalidException {
 		activateShell(shell);
 		sleep(500);
 		removeAdvancedTableComposition(buttonIndex);
 		sleep(1000);
 		closeShellWithFinishButton(shell);
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param feature
 	 *            the feature to edit
 	 * @param newValue
 	 *            the new value to set to the feature
 	 * @throws WidgetInvalidException 
 	 */
 	public void removeTableCompositionFeature(SWTBotShell shell, int buttonIndex, String buttonLabel) throws WidgetInvalidException {
 		activateShell(shell);
 		sleep(500);
 		removeTableComposition(buttonIndex, buttonLabel);
 		sleep(1000);
 		closeShellWithFinishButton(shell);
 	}
 
 	public void removeTableComposition(int buttonIndex, String buttonLabel) throws WidgetInvalidException {
 		try {
 			table(buttonIndex).select(0);
 			buttonWithTooltip(buttonLabel, buttonIndex).click();
 		} catch (Exception e) {
 			// empty table
 			throw new WidgetInvalidException();
 		}
 	}
 
 	public void removeAdvancedTableComposition(int buttonIndex) throws WidgetInvalidException {
 		try {
 			table(buttonIndex).select(0);
 			buttonWithTooltip(EEFRuntimeUIMessages.ReferencesTable_remove_tooltip, buttonIndex).click();
 		} catch (Exception e) {
 			// empty table
 			throw new WidgetInvalidException();
 		}
 	}
 
 	public void removeAdvancedReferencesTable(int buttonIndex, Object value) throws WidgetInvalidException {
 		try {
 		table(buttonIndex).select(0);
 		buttonWithTooltip(EEFRuntimeUIMessages.ReferencesTable_remove_tooltip, buttonIndex).click();
 		} catch (Exception e) {
 			// empty table
 			throw new WidgetInvalidException();
 		}
 	}
 
 	/**
 	 * @param feature
 	 */
 	private void editCheckBox(String feature) {
 		checkBox(feature).click();
 	}
 
 	/**
 	 * @param feature
 	 */
 	public void editCheckBox(String feature, boolean value) {
 		if (value) {
 			checkBox(feature).select();
 		} else {
 			checkBox(feature).deselect();
 		}
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param propertyView
 	 *            the properties view
 	 * @param feature
 	 *            the feature to edit
 	 * @param selectNode
 	 *            the SWTBotTreeItem in the treeview model
 	 */
 	public void editPropertyCheckboxFeature(SWTBotView propertyView, String feature, SWTBotTreeItem selectNode) {
 		SWTBot propertyBot = propertyView.bot();
 		propertyBot.checkBox(feature).click();
 		selectNode.select();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param propertyView
 	 *            the properties view
 	 * @param feature
 	 *            the feature to edit
 	 * @param selectNode
 	 *            the SWTBotTreeItem in the treeview model
 	 */
 	public void editPropertyEMFComboViewerFeature(SWTBotView propertyView, Object feature,
 			SWTBotTreeItem selectNode) {
 		SWTBot propertyBot = propertyView.bot();
 		propertyBot.comboBox().setSelection(feature.toString());
 		selectNode.select();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param feature
 	 *            the feature to edit
 	 * @param newValue
 	 *            the new value to set to the feature
 	 * @throws WidgetInvalidException 
 	 */
 	public void editPropertyFlatReferencesTableFeature(SWTBotView propertyView, int buttonIndex,
 			int tableIndex, SWTBotTreeItem selectNode) throws WidgetInvalidException {
 		// SWTBot propertyBot = propertyView.bot();
 		editFlatReferencesTable(buttonIndex, tableIndex);
 		sleep(1000);
 		selectNode.select();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param feature
 	 *            the feature to edit
 	 * @param newValue
 	 *            the new value to set to the feature
 	 * @throws WidgetInvalidException 
 	 */
 	public void editPropertyEObjectFlatComboViewerFeature(SWTBotView propertyView, int buttonIndex,
 			int tableIndex, SWTBotTreeItem selectNode) throws WidgetInvalidException {
 		// SWTBot propertyBot = propertyView.bot();
 		editEObjectFlatComboViewer(buttonIndex, tableIndex);
 		sleep(1000);
 		selectNode.select();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param feature
 	 *            the feature to edit
 	 * @param newValue
 	 *            the new value to set to the feature
 	 * @throws WidgetInvalidException 
 	 */
 	public void editPropertyReferencesTableFeature(SWTBotView propertyView, int buttonIndex, int tableIndex,
 			String buttonLabel, SWTBotTreeItem selectNode) throws WidgetInvalidException {
 		// SWTBot propertyBot = propertyView.bot();
 		editReferencesTable(buttonIndex, tableIndex, buttonLabel);
 		sleep(1000);
 		selectNode.select();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param feature
 	 *            the feature to edit
 	 * @param newValue
 	 *            the new value to set to the feature
 	 * @throws WidgetInvalidException 
 	 */
 	public void removePropertyReferencesTableFeature(SWTBotView propertyView, int buttonIndex,
 			int tableIndex, String buttonLabel, SWTBotTreeItem selectNode) throws WidgetInvalidException {
 		// SWTBot propertyBot = propertyView.bot();
 		removeReferencesTable(buttonIndex, tableIndex, buttonLabel);
 		sleep(1000);
 		selectNode.select();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param feature
 	 *            the feature to edit
 	 * @param newValue
 	 *            the new value to set to the feature
 	 * @throws WidgetInvalidException 
 	 */
 	public void removePropertyTableCompositionFeature(SWTBotView propertyView, int buttonIndex,
 			String buttonLabel, SWTBotTreeItem selectNode) throws WidgetInvalidException {
 		// SWTBot propertyBot = propertyView.bot();
 		removeTableComposition(buttonIndex, buttonLabel);
 		sleep(1000);
 		selectNode.select();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param feature
 	 *            the feature to edit
 	 * @param newValue
 	 *            the new value to set to the feature
 	 */
 	public void editPropertyAdvancedEObjectFlatComboViewerFeature(SWTBotView propertyView, int buttonIndex,
 			Object value, SWTBotTreeItem selectNode) {
 		// SWTBot propertyBot = propertyView.bot();
 		editAdvancedEObjectFlatComboViewer(buttonIndex, value);
 		selectNode.select();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param feature
 	 *            the feature to edit
 	 * @param newValue
 	 *            the new value to set to the feature
 	 */
 	public void removePropertyAdvancedEObjectFlatComboViewerFeature(SWTBotView propertyView, int buttonIndex,
 			SWTBotTreeItem selectNode) {
 		// SWTBot propertyBot = propertyView.bot();
 		removeAdvancedEObjectFlatComboViewer(buttonIndex);
 		selectNode.select();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param feature
 	 *            the feature to edit
 	 * @param newValue
 	 *            the new value to set to the feature
 	 */
 	public void editPropertyAdvancedReferencesTableFeature(SWTBotView propertyView, int buttonIndex,
 			Object value, SWTBotTreeItem selectNode) {
 		// SWTBot propertyBot = propertyView.bot();
 		editAdvancedReferencesTable(buttonIndex, value);
 		selectNode.select();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param shell
 	 *            the shell of the edited wizard
 	 * @param feature
 	 *            the feature to edit
 	 * @param newValue
 	 *            the new value to set to the feature
 	 * @throws WidgetInvalidException 
 	 */
 	public void removePropertyAdvancedReferencesTableFeature(SWTBotView propertyView, int buttonIndex,
 			Object value, SWTBotTreeItem selectNode) throws WidgetInvalidException {
 		// SWTBot propertyBot = propertyView.bot();
 		removeAdvancedReferencesTable(buttonIndex, value);
 		selectNode.select();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param propertyView
 	 *            the properties view
 	 * @param feature
 	 *            the feature to edit
 	 * @param selectNode
 	 *            the SWTBotTreeItem in the treeview model
 	 */
 	public void editPropertyRadioFeature(SWTBotView propertyView, int index, Object feature,
 			SWTBotTreeItem selectNode) {
 		SWTBot propertyBot = propertyView.bot();
 		propertyBot.radio(feature.toString(), index).click();
 		selectNode.select();
 	}
 
 	/**
 	 * Edit the value of the EEF Wizard to give the <i>feature</i> the value <i>newValue</i>
 	 * 
 	 * @param propertyView
 	 *            the properties view
 	 * @param feature
 	 *            the feature to edit
 	 * @param selectNode
 	 *            the SWTBotTreeItem in the treeview model
 	 */
 	public void editPropertyMultiValuedEditorFeature(SWTBotView propertyView, String label, String value,
 			SWTBotTreeItem selectNode) {
 		// SWTBot propertyBot = propertyView.bot();
 		editMultiValuedEditor(label, value);
 		selectNode.select();
 	}
 
 	public void openPropertiesView() {
 		menu(UIConstants.WINDOW_MENU).menu(UIConstants.SHOW_VIEW_MENU).menu(UIConstants.OTHER_MENU).click();
 
 		SWTBotShell shell = shell(UIConstants.SHOW_VIEW_MENU);
 		activateShell(shell);
 
 		SWTBotTree viewSelectionTree = tree();
 		viewSelectionTree.expandNode(UIConstants.GENERAL_MENU).select(UIConstants.PROPERTIES_VIEW_NAME);
 		button(UIConstants.OK_BUTTON).click();
 		waitUntil(Conditions.shellCloses(shell));
 	}
 
 	public void openJavaPerspective() {
 		menu(UIConstants.WINDOW_MENU).menu(UIConstants.OPEN_PERSPECTIVE_MENU).menu(UIConstants.OTHER_MENU)
 				.click();
 		SWTBotShell openPerspectiveShell = shell(UIConstants.OPEN_PERSPECTIVE_MENU);
 		activateShell(openPerspectiveShell);
 
 		table().select(UIConstants.JAVA_LABEL);
 		button(UIConstants.OK_BUTTON).click();
 	}
 
 	/**
 	 * This method close the welcome page if we use the workspace of test for the first time
 	 */
 	public void closeWelcomePage() {
 		try {
 			viewByTitle(UIConstants.WELCOME_LABEL).close();
 		} catch (WidgetNotFoundException e) {
 			// do nothing
 		}
 	}
 
 	/*****************************************************************************
 	 * * Bot utils * *
 	 *****************************************************************************/
 
 	/**
 	 * Active the main shell (Workbench shell)
 	 */
 	protected void activateEclipseShell() {
 		SWTBotShell[] shells = shells();
 		if (shells.length > 0)
 			// Heuristic : the eclipse shell is the first shell
 			shells[0].activate();
 	}
 
 	/**
 	 * @param modelElement
 	 *            the element to process
 	 * @return a list containing the element to expand
 	 */
 	private List<Object> getExpansionPath(EObject modelElement) {
 		List<Object> result = new ArrayList<Object>();
 		result.add(modelElement);
 		EObject container = modelElement.eContainer();
 		while (container != null) {
 			result.add(0, container);
 			container = container.eContainer();
 		}
 		return result;
 	}
 
 	/**
 	 * @param file
 	 *            the file to process
 	 * @return a list containing the element to expand
 	 */
 	private List<IResource> getExpansionPath(IFile file) {
 		List<IResource> result = new ArrayList<IResource>();
 		result.add(file);
 		IContainer container = file.getParent();
 		while (container != null && !(container instanceof IProject)) {
 			result.add(0, container);
 			container = container.getParent();
 		}
 		if (container instanceof IProject)
 			result.add(0, container);
 		return result;
 	}
 
 	/**
 	 * Return the label of the given IResource
 	 * 
 	 * @param resource
 	 *            the resource to process
 	 * @return the label of the resource
 	 */
 	private String getNodeText(IResource resource) {
 		return resource.getName();
 	}
 
 	/**
 	 * Select the object <code>next</code> as a subnode of the <code>currentNode</code>
 	 * 
 	 * @param currentNode
 	 *            the currentNode
 	 * @param next
 	 *            the subnode to select
 	 * @return the selected node
 	 */
 	private SWTBotTreeItem selectSubNode(SWTBotTreeItem currentNode, Object next) {
 		AdapterFactory adapterFactory = EEFRuntimePlugin.getDefault().getAdapterFactory();
 		IItemLabelProvider labelProvider = (IItemLabelProvider)adapterFactory.adapt(next,
 				IItemLabelProvider.class);
 		String text = labelProvider.getText(next);
 		SWTBotTreeItem node2 = currentNode.getNode(text);
 		node2.select();
 		sleep(500);
 		return node2;
 	}
 }
