 /*******************************************************************************
  * Copyright (c) 2008, 2009 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.runtime.tests;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.common.command.BasicCommandStack;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.compare.diff.metamodel.DiffGroup;
 import org.eclipse.emf.compare.diff.metamodel.DiffModel;
 import org.eclipse.emf.compare.diff.service.DiffService;
 import org.eclipse.emf.compare.match.metamodel.MatchModel;
 import org.eclipse.emf.compare.match.service.MatchService;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
 import org.eclipse.emf.eef.runtime.EEFRuntimePlugin;
import org.eclipse.emf.eef.runtime.impl.utils.EEFUtils;
 import org.eclipse.emf.eef.runtime.tests.swtbot.finder.SWTEEFBot;
 import org.eclipse.emf.eef.runtime.tests.utils.EEFTestsResourceUtils;
 import org.eclipse.swtbot.eclipse.finder.SWTBotEclipseTestCase;
 import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
 import org.junit.Before;
 
 /**
  * @author <a href="mailto:nathalie.lepine@obeo.fr">Nathalie Lepine</a>
  */
 public abstract class SWTBotEEFTestCase extends SWTBotEclipseTestCase {
 
 	/**
 	 * An instance of SWTEclipseBot.
 	 * 
 	 * @since 1.1
 	 */
 	protected SWTEEFBot bot = new SWTEEFBot();
 
 	/*****************************************************************************
 	 * * Test members * *
 	 *****************************************************************************/
 
 	/**
 	 * the model editor
 	 */
 	protected SWTBotEditor editor;
 	
 	/**
 	 * The ResourceSet where to operate
 	 */
 	protected AdapterFactoryEditingDomain editingDomain = new AdapterFactoryEditingDomain(EEFRuntimePlugin.getDefault().getAdapterFactory(), new BasicCommandStack());
 	
 	/**
 	 * The test project
 	 */
 	private IProject testProject;
 	
 	/**
 	 * The workspace folder containing the input model 
 	 */
 	private IFolder modelFolder;
 	
 	/**
 	 * The expectedModel 
 	 */
 	protected Resource expectedModel;
 
 	/**
 	 * @return The project that contains models for tests 
 	 */
 	protected abstract String getTestsProjectName();
 	
 	/**
 	 *  @return The folder that contains the input models for tests 
 	 */
 	protected abstract String getInputModelFolder();
 	
 	/**
 	 *  @return The folder that contains the expected models for tests 
 	 */
 	protected abstract String getExpectedModelFolder();
 	
 	/**
 	 * @return The input model
 	 */
 	protected abstract String getInputModelName();
 	
 	/**
 	 * T@return he expected model
 	 */
 	protected abstract String getExpectedModelName();
 	
 	/**
 	 * 
 	 * @return the import models folder
 	 */
 	protected abstract String getImportModelsFolder();
 
 	/*****************************************************************************
 	 * * Tests specialization * *
 	 *****************************************************************************/
 	
 	/**
 	 * Process to initialize the workspace for the tests
 	 * 
 	 * @throws CoreException
 	 *             an error occurred during the tests initialization
 	 * @throws IOException
 	 *             an error occurred during the tests initialization
 	 */
 	protected void initWorkspaceForTests() throws CoreException, IOException {
 		List<String> names = new ArrayList<String>();
 		names.add(getInputModelFolder());
 		testProject = EEFTestsResourceUtils.createTestProject(getTestsProjectName(), names);
 		modelFolder = testProject.getFolder(getInputModelFolder());
 	}
 
 	/**
 	 * Import the input model
 	 * @throws CoreException error during model import
 	 * @throws IOException error during model import
 	 */
 	protected void initializeInputModel() throws CoreException, IOException  {
 		EEFTestsResourceUtils.importModel(getTestsProjectName(), getImportModelsFolder() + "/" + getInputModelFolder() + "/" + getInputModelName(), modelFolder);
 		URI fileURI = URI.createPlatformResourceURI(getTestsProjectName() + "/" + getInputModelFolder() + "/" + getInputModelName(), true);
 		Resource activeResource = editingDomain.getResourceSet().getResource(fileURI, true);
 		bot.defineActiveModel(activeResource);
 	}
 	
 	/**
 	 * Delete the test models
 	 * @throws CoreException error during model deleting
 	 */
 	protected void deleteModels() throws CoreException {
 		IFile inputFile = EEFTestsResourceUtils.workspaceFile(bot.getActiveResource());
 		bot.unloadActiveModel();
 		NullProgressMonitor monitor = new NullProgressMonitor();
 		inputFile.delete(true, true, monitor);
 		IFile expectedFile = EEFTestsResourceUtils.workspaceFile(expectedModel);
 		expectedModel.unload();
 		expectedFile.delete(true, true, monitor);
 		testProject.refreshLocal(IResource.DEPTH_INFINITE, monitor);
 	}
 	
 	protected void createExpectedModel() {
 		URI fileURI = URI.createPlatformResourceURI(getTestsProjectName() + "/" + getExpectedModelFolder() + "/" + getExpectedModelName(), true);
 		expectedModel = editingDomain.getResourceSet().createResource(fileURI);
 		expectedModel.getContents().addAll(EcoreUtil.copyAll(bot.getActiveResource().getContents()));
 	}
 
 	/*****************************************************************************
 	 * * Test lifecycle * *
 	 *****************************************************************************/
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		super.setUp();
 		bot.closeWelcomePage();
 		// openJavaPerspective();
 		bot.openPropertiesView();
 		initWorkspaceForTests();
 	}
 
 	/*****************************************************************************
 	 * * Utils methods * *
 	 *****************************************************************************/
 
 	/**
 	 * @param the
 	 *            model to compare to the bot active model
 	 * @return the comparison model
 	 * @throws Exception
 	 *             something wrong during comparison
 	 */
 	private DiffModel compareToActiveModel(Resource expectedModel) throws Exception {
 		bot.reloadActiveModel();
 		Map<String, Object> options = new HashMap<String, Object>();
 		options.put(org.eclipse.emf.compare.match.MatchOptions.OPTION_IGNORE_XMI_ID, Boolean.TRUE);
 		MatchModel match = MatchService.doResourceMatch(bot.getActiveResource(), expectedModel, options);
 		DiffModel diff = DiffService.doDiff(match);
 		return diff;
 	}
 
 	/**
 	 * Check if the bot's active model is the same that the given model
 	 * 
 	 * @param expectedModel
 	 *            the model to compare
 	 */
 	public void assertExpectedModelReached(Resource expectedModel) {
 		try {
 			DiffModel compareToActiveModel = compareToActiveModel(expectedModel);
			List<EObject> diffList = EEFUtils.asEObjectList(compareToActiveModel.eAllContents());
 			List<EObject> result = filterAbnormalDiffElement(diffList);
 			assertEquals("The active model isn't the same that the expected model", result.size(), 0);
 		} catch (Exception e) {
 			// How to do that ???
 			assertTrue("Error during model comparison", false);
 		}
 	}
 
 	/**
 	 * TODO: check this with the EMF Compare team
 	 * 
 	 * @param diffList
 	 *            the list to filter
 	 * @return the list of "good" diff
 	 */
 	private List<EObject> filterAbnormalDiffElement(List<EObject> diffList) {
 		List<EObject> result = new ArrayList<EObject>();
 		for (EObject object : diffList) {
 			if (!(object instanceof DiffGroup) || (((DiffGroup)object).eContents().size() > 0))
 				result.add(object);
 		}
 		return result;
 	}
 
 }
