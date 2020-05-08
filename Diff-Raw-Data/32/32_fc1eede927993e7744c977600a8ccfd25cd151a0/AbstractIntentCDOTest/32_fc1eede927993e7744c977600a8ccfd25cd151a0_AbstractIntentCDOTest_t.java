 /*******************************************************************************
  * Copyright (c) 2010, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.client.ui.test.unit.cdo.util;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.mylyn.docs.intent.client.ui.test.util.AbstractIntentUITest;
 import org.eclipse.mylyn.docs.intent.collab.common.repository.IntentRepositoryInitializer;
 import org.eclipse.mylyn.docs.intent.collab.common.repository.IntentRepositoryManager;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.ReadOnlyException;
 import org.eclipse.mylyn.docs.intent.collab.repository.RepositoryConnectionException;
 import org.eclipse.mylyn.docs.intent.test.server.IntentCDORepository;
 
 /**
  * An abstract test class providing API for managing an Intent document stored in a CDO Repository.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class AbstractIntentCDOTest extends AbstractIntentUITest {
 
 	private static final String INTENT_REPOSITORY_NAME = "intent-server";
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.client.ui.test.util.AbstractIntentUITest#setUp()
 	 */
 	@Override
 	protected void setUp() throws Exception {
 		// First step is to launch a CDOServer
 		IntentCDORepository.start(true, INTENT_REPOSITORY_NAME);
 
 		// Otherwise, everything is the same as local
 		super.setUp();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.client.ui.test.util.AbstractIntentUITest#tearDown()
 	 */
 	@Override
 	protected void tearDown() throws Exception {
 		super.tearDown();
 
 		// Stopping the CDOServer
 		IntentCDORepository.stop();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.client.ui.test.util.AbstractIntentUITest#setUpIntentProject(java.lang.String,
 	 *      java.lang.String, boolean)
 	 */
 	@Override
 	protected void setUpIntentProject(String projectName, String intentDocumentPath,
 			boolean listenForRepository) {
 		super.setUpIntentProject(getIntentRepositoryIdentifier(), intentDocumentPath, listenForRepository);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.client.ui.test.util.AbstractIntentUITest#doCreateIntentProject(java.lang.String,
 	 *      java.lang.String)
 	 */
 	@Override
 	protected void doCreateIntentProject(String projectName, String intentDocumentContent)
 			throws CoreException {
 		// Step 1: create the repository
 		try {
 			repository = IntentRepositoryManager.INSTANCE.getRepository(projectName);
 
 			repositoryAdapter = repository.createRepositoryAdapter();
 			repositoryAdapter.openSaveContext();
 			assertNotNull(repository);
 			assertNotNull(repositoryAdapter);
 
 			// Step 2: initialise content
 			IntentRepositoryInitializer.initializeContent(projectName, intentDocumentContent);
 		} catch (RepositoryConnectionException e) {
 			fail("Failed to create CDO Repository '" + projectName + "': " + e.getMessage());
 		} catch (ReadOnlyException e) {
 			fail("Failed to open a Transaction on the CDO Repository '" + projectName + "': "
 					+ e.getMessage());
 		}
 	}
 
 	/**
 	 * Returns the repository identifier associated to the current test.
 	 * 
 	 * @return the repository identifier associated to the current test
 	 */
 	protected final String getIntentRepositoryIdentifier() {
		return "cdo://" + IntentCDORepository.getServerLocation() + "/" + INTENT_REPOSITORY_NAME;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.client.ui.test.util.AbstractIntentUITest#additionalSetUpOperations()
 	 */
 	@Override
 	protected void additionalSetUpOperations() {
 
 	}
 }
