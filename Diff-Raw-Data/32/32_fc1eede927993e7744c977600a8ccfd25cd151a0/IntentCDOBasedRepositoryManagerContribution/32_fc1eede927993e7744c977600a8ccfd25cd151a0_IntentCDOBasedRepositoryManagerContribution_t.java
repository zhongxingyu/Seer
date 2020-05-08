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
 package org.eclipse.mylyn.docs.intent.client.ui.cdo.repository;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.mylyn.docs.intent.collab.cdo.repository.CDOConfig;
 import org.eclipse.mylyn.docs.intent.collab.cdo.repository.CDORepository;
 import org.eclipse.mylyn.docs.intent.collab.common.repository.contribution.IntentRepositoryManagerContribution;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryStructurer;
 import org.eclipse.mylyn.docs.intent.collab.repository.Repository;
 import org.eclipse.mylyn.docs.intent.collab.repository.RepositoryConnectionException;
 import org.eclipse.mylyn.docs.intent.collab.repository.RepositoryCreator;
 import org.eclipse.mylyn.docs.intent.collab.repository.RepositoryRegistry;
 
 /**
  * An {@link IntentRepositoryManagerContribution} allowing to create an Intent Repository from CDO Repository
  * name.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class IntentCDOBasedRepositoryManagerContribution implements IntentRepositoryManagerContribution {
 
 	private static final String SLASH = "/";
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.common.repository.contribution.IntentRepositoryManagerContribution#canCreateRepository(java.lang.String)
 	 */
 	public boolean canCreateRepository(String identifier) {
 		return identifier.startsWith(CDORepository.CDO_REPOSITORY_IDENTIFIER);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.common.repository.contribution.IntentRepositoryManagerContribution#createRepository(java.lang.String)
 	 */
 	public Repository createRepository(String identifier) throws RepositoryConnectionException {
 		// Identifier should respect the following form:
		// cdo://REPOSITORYLOCATION/repoName (e.g. cdo://localhost:2037/repo1)
 		String repositoryType = "org.eclipse.mylyn.docs.intent.collab.cdo.repository";
 		RepositoryCreator repositoryCreator;
 		try {
 			repositoryCreator = RepositoryRegistry.INSTANCE.getRepositoryCreator(repositoryType);
 
 			RepositoryStructurer repositoryStructurer = RepositoryRegistry.INSTANCE
 					.getRepositoryStructurer(repositoryType);
 			if (repositoryCreator == null) {
 				throw new RepositoryConnectionException("Cannot instantiate a repository of type:"
 						+ repositoryType);
 			}
			String identifierWithoutPrefix = identifier.substring(CDORepository.CDO_REPOSITORY_IDENTIFIER
					.length());
 			String[] fragments = identifierWithoutPrefix.split(SLASH);
 			if (fragments.length >= 2) {
 				String repositoryLocation = checkRepositoryLocation(fragments[0]);
 				String repositoryName = checkRepositoryName(fragments[1]);
 				CDOConfig config = new CDOConfig(repositoryLocation, repositoryName);
 				return repositoryCreator.createRepository(config, repositoryStructurer);
 			} else {
 				throw new RepositoryConnectionException(
 						"Invalid identifier for Intent repository '"
 								+ identifier
 								+ "': should be cdo:/REPOSITORY_LOCATION/REPOSITORY_NAME (e.g. cdo:/localhost:2036/repo1)");
 			}
 		} catch (CoreException e) {
 			throw new RepositoryConnectionException(e.getMessage());
 		}
 	}
 
 	/**
 	 * Checks the repository name, and throw a {@link RepositoryConnectionException} if it is not correct.
 	 * 
 	 * @param repositoryName
 	 *            the repositoryName extracted from Intent repository identifier
 	 * @return the repository name if correct
 	 * @throws RepositoryConnectionException
 	 *             if the repository name is not correct
 	 */
 	private String checkRepositoryName(String repositoryName) throws RepositoryConnectionException {
 		if (repositoryName.trim().length() == 0) {
 			throw new RepositoryConnectionException("Invalid Intent repositoy name '" + repositoryName + "'");
 		}
 		return repositoryName;
 	}
 
 	/**
 	 * Checks the repository location, and throw a {@link RepositoryConnectionException} if it is not correct.
 	 * 
 	 * @param repositoryLocation
 	 *            the repository location extracted from Intent repository identifier
 	 * @return the repository location if correct
 	 * @throws RepositoryConnectionException
 	 *             if the repository location is not correct
 	 */
 	private String checkRepositoryLocation(String repositoryLocation) throws RepositoryConnectionException {
 		if (repositoryLocation.split(":").length != 2) {
 			throw new RepositoryConnectionException("Invalid Intent repositoy location '"
 					+ repositoryLocation
 					+ "': should be IP_ADRESS:PORT_NUMBER (e.g. 'localhost:2036', '192.1.2.3:1038')");
 		}
 		return repositoryLocation;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.common.repository.contribution.IntentRepositoryManagerContribution#normalizeIdentifier(java.lang.String)
 	 */
 	public String normalizeIdentifier(String identifier) {
 		String normalizedIdentifier = identifier;
		// If the identifier starts with cdo://
 		if (canCreateRepository(normalizedIdentifier)) {
			// We return cdo://REPOSITORY_NAME, so that we do not have to always give the repository location
			String identifierWithoutPrefix = identifier.substring(CDORepository.CDO_REPOSITORY_IDENTIFIER
					.length());
 			String[] fragments = identifierWithoutPrefix.split(SLASH);
 			if (fragments.length == 1) {
				// If identifier is already cdo://REPOSITORY_NAME
 				normalizedIdentifier = identifier;
 			} else {
				// if identifier is cdo://REPOSITORY_LOCATION/REPOSITORY_NAME[/...]
 				if ((fragments.length == 2) && (fragments[0].contains(":"))) {
 					normalizedIdentifier = CDORepository.CDO_REPOSITORY_IDENTIFIER + fragments[1];
 				} else {
 					if (fragments.length >= 2) {
 						normalizedIdentifier = CDORepository.CDO_REPOSITORY_IDENTIFIER + fragments[0];
 					}
 				}
 			}
 		}
 		return normalizedIdentifier;
 	}
 }
