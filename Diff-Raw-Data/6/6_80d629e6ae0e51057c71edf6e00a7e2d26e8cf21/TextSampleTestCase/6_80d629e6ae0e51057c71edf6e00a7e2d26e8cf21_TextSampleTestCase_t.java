 /*******************************************************************************
  * Copyright (c) 2009 - 2010 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.eefnr.tests.junit;
 
 import java.io.IOException;
import java.net.URL;
 import java.util.Collections;
 
 import org.eclipse.emf.common.command.CompoundCommand;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.command.SetCommand;
 import org.eclipse.emf.eef.eefnr.EefnrPackage;
 import org.eclipse.emf.eef.eefnr.parts.EefnrViewsRepository;
 import org.eclipse.emf.eef.runtime.tests.SWTBotEEFTestCase;
 import org.eclipse.emf.eef.runtime.tests.exceptions.InputModelInvalidException;
 import org.eclipse.emf.eef.runtime.tests.utils.EEFTestsModelsUtils;
 import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.keyboard.KeyboardStrategy;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences.KeyboardLayoutDetector;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
 /**
  * TestCase for TextSample
  * @author <a href="mailto:nathalie.lepine@obeo.fr">Nathalie Lepine</a>
  */
 public class TextSampleTestCase extends SWTBotEEFTestCase {
 
 	/**
 	 * The EClass of the type to edit
 	 */
 	private EClass textSampleMetaClass = EefnrPackage.eINSTANCE.getTextSample();
 
 	/**
 	 * The type to edit
 	 */
 	private EObject textSample;
 	/**
 	 * Updated value of the feature
 	 */
 	private static final String UPDATED_VALUE = "value2";
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.tests.SWTBotEEFTestCase#getExpectedModelName()
 	 */
 	protected String getExpectedModelName() {
 		return "expected.eefnr";
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.tests.SWTBotEEFTestCase#getInputModelFolder()
 	 */
 	protected String getInputModelFolder() {
 		return "input";
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.tests.SWTBotEEFTestCase#getInputModelName()
 	 */
 	protected String getInputModelName() {
 		return "input.eefnr";
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.tests.SWTBotEEFTestCase#getTestsProjectName()
 	 */
 	protected String getTestsProjectName() {
 		return "org.eclipse.emf.eef.tests.nonreg.junit";
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.tests.SWTBotEEFTestCase#getExpectedModelFolder()
 	 */
 	protected String getExpectedModelFolder() {
 		// The project that contains models for tests
 		return "expected";
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.tests.SWTBotEEFTestCase#getImportModelsFolder()
 	 */
 	protected String getImportModelsFolder() {
 		return  "models";
 	}
 	/**
 	 * Create the expected model from the input model
 	 * @throws InputModelInvalidException error during expected model initialization
 	 * @throws IOException error during expected model serialization
 	 */
 	protected void initializeExpectedModelForTextSampleTextRequiredProperty() throws InputModelInvalidException, IOException {
 		// Create the expected model content by applying the attempted command on a copy of the input model content
 		createExpectedModel();
 		EObject textSample = EEFTestsModelsUtils.getFirstInstanceOf(expectedModel, textSampleMetaClass);
 		if (textSample == null)
 			throw new InputModelInvalidException(textSampleMetaClass.getName());
 		CompoundCommand cc = new CompoundCommand();
 				cc.append(SetCommand.create(editingDomain, textSample, EefnrPackage.eINSTANCE.getTextSample_TextRequiredProperty(), UPDATED_VALUE));
 		editingDomain.getCommandStack().execute(cc);
 		expectedModel.save(Collections.EMPTY_MAP);
 	}
 	/**
 	 * Test the editor properties :
 	 * - init the input model
 	 * - calculate the expected model
 	 * - initialize the model editor
 	 * - change the properties in the editor properties
 	 * - compare the expected and the real model : if they are equals the test pass
 	 * - delete the models
 	 */
 	public void testEditTextSampleTextRequiredProperty() throws Exception {
 		
 		// Import the input model
 		initializeInputModel();
 		
 		textSample = EEFTestsModelsUtils.getFirstInstanceOf(bot.getActiveResource(), textSampleMetaClass);
 		if (textSample == null)
 			throw new InputModelInvalidException(textSampleMetaClass.getName());
 	
 		// Create the expected model
 		initializeExpectedModelForTextSampleTextRequiredProperty();
 		
 		// Open the input model with the treeview editor
 		SWTBotEditor modelEditor = bot.openActiveModel();
 		
 		// Open the EEF wizard (by double click) to edit the TextSample element
 		EObject firstInstanceOf = EEFTestsModelsUtils.getFirstInstanceOf(bot.getActiveResource(), textSampleMetaClass);
 		if (firstInstanceOf == null)
 			throw new InputModelInvalidException(textSampleMetaClass.getName());
 		
 		SWTBotShell wizardShell = bot.prepareBatchEditing(modelEditor, textSampleMetaClass, firstInstanceOf, "Base");
 		// Change value of the textRequiredProperty feature of the TextSample element 
 				bot.editTextFeature(wizardShell, EefnrViewsRepository.TextSample.textRequiredProperty, UPDATED_VALUE);
 		
 		// Save the changement
 		bot.finalizeEdition(modelEditor);
 		
 		// Compare real model with expected model
 		assertExpectedModelReached(expectedModel);
 		
 		// Delete the input model
 		deleteModels();
 	
 	}
 	/**
 	 * Create the expected model from the input model
 	 * @throws InputModelInvalidException error during expected model initialization
 	 * @throws IOException error during expected model serialization
 	 */
 	protected void initializeExpectedModelForTextSampleTextOptionalProperty() throws InputModelInvalidException, IOException {
 		// Create the expected model content by applying the attempted command on a copy of the input model content
 		createExpectedModel();
 		EObject textSample = EEFTestsModelsUtils.getFirstInstanceOf(expectedModel, textSampleMetaClass);
 		if (textSample == null)
 			throw new InputModelInvalidException(textSampleMetaClass.getName());
 		CompoundCommand cc = new CompoundCommand();
 				cc.append(SetCommand.create(editingDomain, textSample, EefnrPackage.eINSTANCE.getTextSample_TextOptionalProperty(), UPDATED_VALUE));
 		editingDomain.getCommandStack().execute(cc);
 		expectedModel.save(Collections.EMPTY_MAP);
 	}
 	/**
 	 * Test the editor properties :
 	 * - init the input model
 	 * - calculate the expected model
 	 * - initialize the model editor
 	 * - change the properties in the editor properties
 	 * - compare the expected and the real model : if they are equals the test pass
 	 * - delete the models
 	 */
 	public void testEditTextSampleTextOptionalProperty() throws Exception {
 		
 		// Import the input model
 		initializeInputModel();
 		
 		textSample = EEFTestsModelsUtils.getFirstInstanceOf(bot.getActiveResource(), textSampleMetaClass);
 		if (textSample == null)
 			throw new InputModelInvalidException(textSampleMetaClass.getName());
 	
 		// Create the expected model
 		initializeExpectedModelForTextSampleTextOptionalProperty();
 		
 		// Open the input model with the treeview editor
 		SWTBotEditor modelEditor = bot.openActiveModel();
 		
 		// Open the EEF wizard (by double click) to edit the TextSample element
 		EObject firstInstanceOf = EEFTestsModelsUtils.getFirstInstanceOf(bot.getActiveResource(), textSampleMetaClass);
 		if (firstInstanceOf == null)
 			throw new InputModelInvalidException(textSampleMetaClass.getName());
 		
 		SWTBotShell wizardShell = bot.prepareBatchEditing(modelEditor, textSampleMetaClass, firstInstanceOf, "Base");
 		
 		// Change value of the textOptionalProperty feature of the TextSample element 
 				bot.editTextFeature(wizardShell, EefnrViewsRepository.TextSample.textOptionalProperty, UPDATED_VALUE);
 		
 		// Save the changement
 		bot.finalizeEdition(modelEditor);
 		
 		// Compare real model with expected model
 		assertExpectedModelReached(expectedModel);
 		
 		// Delete the input model
 		deleteModels();
 	
 	}
 
 
 
 
 
 
 }
