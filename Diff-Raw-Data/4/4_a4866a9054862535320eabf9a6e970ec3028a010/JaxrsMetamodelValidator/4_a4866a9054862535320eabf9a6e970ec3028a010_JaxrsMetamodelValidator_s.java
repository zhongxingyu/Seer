 /******************************************************************************* 
  * Copyright (c) 2008 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Xavier Coulon - Initial API and implementation 
  ******************************************************************************/
 package org.jboss.tools.ws.jaxrs.core.internal.metamodel.validation;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.wst.validation.internal.core.ValidationException;
 import org.eclipse.wst.validation.internal.provisional.core.IReporter;
 import org.eclipse.wst.validation.internal.provisional.core.IValidationContext;
 import org.jboss.tools.common.validation.ContextValidationHelper;
 import org.jboss.tools.common.validation.EditorValidationContext;
 import org.jboss.tools.common.validation.IAsYouTypeValidator;
 import org.jboss.tools.common.validation.IPreferenceInfo;
 import org.jboss.tools.common.validation.IProjectValidationContext;
 import org.jboss.tools.common.validation.IValidatingProjectTree;
 import org.jboss.tools.common.validation.IValidator;
 import org.jboss.tools.common.validation.PreferenceInfoManager;
 import org.jboss.tools.common.validation.TempMarkerManager;
 import org.jboss.tools.common.validation.ValidatorManager;
 import org.jboss.tools.common.validation.internal.SimpleValidatingProjectTree;
 import org.jboss.tools.ws.jaxrs.core.JBossJaxrsCorePlugin;
 import org.jboss.tools.ws.jaxrs.core.configuration.ProjectNatureUtils;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.builder.JaxrsMetamodelBuilder;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsHttpMethod;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsJavaApplication;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsMetamodel;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsResource;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsWebxmlApplication;
 import org.jboss.tools.ws.jaxrs.core.internal.utils.Logger;
 import org.jboss.tools.ws.jaxrs.core.metamodel.IJaxrsApplication;
 import org.jboss.tools.ws.jaxrs.core.metamodel.IJaxrsElement;
 import org.jboss.tools.ws.jaxrs.core.metamodel.JaxrsMetamodelLocator;
 import org.jboss.tools.ws.jaxrs.core.preferences.JaxrsPreferences;
 
 /**
  * JAX-RS Metamodel Validator. Relies on delegates to validate each category of element.
  * 
  * @author Xavier Coulon
  * 
  */
 @SuppressWarnings("restriction")
 public class JaxrsMetamodelValidator extends TempMarkerManager implements IValidator, IAsYouTypeValidator {
 
 	private static final String PREFERENCE_PAGE_ID = "org.jboss.tools.ws.jaxrs.ui";
 
 	private static final String PROPERTY_PAGE_ID = "org.jboss.tools.ws.jaxrs.ui";
 
 	/** The JAX-RS Validator ID. */
 	public static final String ID = "org.jboss.tools.ws.jaxrs.JaxrsMetamodelValidator"; //$NON-NLS-1$
 
 	/** The custom 'JAX-RS Problem' marker type. */
 	public static final String JAXRS_PROBLEM_TYPE = "org.jboss.tools.ws.jaxrs.metamodelMarker";
 
 	private static final String BUNDLE_NAME = JaxrsMetamodelValidator.class.getPackage().getName() + ".messages";
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.validation.IValidator#isEnabled(org.eclipse.core.resources.IProject)
 	 */
 	public boolean isEnabled(IProject project) {
 		return JaxrsPreferences.isValidationEnabled(project);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.validation.IValidator#shouldValidate(org.eclipse .core.resources.IProject)
 	 */
 	public boolean shouldValidate(IProject project) {
 		try {
 			return project.isAccessible() && project.hasNature(ProjectNatureUtils.JAXRS_NATURE_ID)
 					&& isEnabled(project);
 		} catch (CoreException e) {
 			Logger.error("Failed to check if JAX-RS validation is required for project '" + project.getName() + "'", e);
 		}
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.validation.IValidator#validate(java.util.Set,
 	 * org.eclipse.core.resources.IProject, org.jboss.tools.jst.web.kb.internal.validation.ContextValidationHelper,
 	 * org.jboss.tools.jst.web.kb.validation.IProjectValidationContext,
 	 * org.jboss.tools.jst.web.kb.internal.validation.ValidatorManager,
 	 * org.eclipse.wst.validation.internal.provisional.core.IReporter)
 	 */
 	public IStatus validate(Set<IFile> changedFiles, IProject project, ContextValidationHelper validationHelper,
 			IProjectValidationContext context, ValidatorManager manager, IReporter reporter) throws ValidationException {
 		Logger.debug("*** Validating project {} after files {} changed... ***", project.getName(),
 				changedFiles.toString());
 		init(project, validationHelper, context, manager, reporter);
 		setAsYouTypeValidation(false);
 		try {
 			if (!changedFiles.isEmpty()) {
 				final JaxrsMetamodel jaxrsMetamodel = JaxrsMetamodelLocator.get(project);
 				final Set<IResource> allResources = completeValidationSet(jaxrsMetamodel, changedFiles.toArray(new IFile[changedFiles.size()]));
 				for (IResource changedResource : allResources) {
 					validate(reporter, changedResource, jaxrsMetamodel);
 				}
 			}
 			// trigger a full validation instead
 			else {
 				validateAll(project, validationHelper, context, manager, reporter);
 			}
 		} catch (CoreException e) {
 			Logger.error("Failed to validate changed files " + changedFiles + " in project " + project, e);
 		} finally {
 			Logger.debug("Validation done.");
 		}
 		return Status.OK_STATUS;
 	}
 
 	/**
 	 * @param jaxrsMetamodel
 	 * @param objects
 	 * @return
 	 */
 	private Set<IResource> completeValidationSet(JaxrsMetamodel jaxrsMetamodel, final IFile... changedResources) {
 		final Set<IResource> resources = new HashSet<IResource>();
 		for(IResource changedResource : changedResources) {
 			resources.add(changedResource);
 			if(jaxrsMetamodel.getApplication(changedResource) != null) {
 				Logger.debug("Adding all applications and project in the set of files to validate...");
 				for(IJaxrsApplication application : jaxrsMetamodel.getAllApplications()) {
 					resources.add(application.getResource());
 				}
 				resources.add(jaxrsMetamodel.getProject());
 			}
 		}
 		return resources;
 	}
 
 	/**
 	 * @param reporter
 	 * @param changedResource
 	 * @throws CoreException
 	 */
 	private void validate(final IReporter reporter, final IResource changedResource, final JaxrsMetamodel jaxrsMetamodel) {
 		if (reporter.isCancelled() || !changedResource.isAccessible()) {
 			return;
 		}
 		displaySubtask(JaxrsValidationMessages.VALIDATING_RESOURCE,
 				new String[] { changedResource.getProject().getName(), changedResource.getName() });
 		try {
 			if (jaxrsMetamodel != null) {
 				List<IJaxrsElement> elements = jaxrsMetamodel.getElements(changedResource);
 				for (IJaxrsElement element : elements) {
 					validate(element);
 				}
 				
 			}
 		} catch (CoreException e) {
 			Logger.error("Failed to validate the resource change", e);
 		}
 	}
 
 	@Override
 	public void validate(org.eclipse.wst.validation.internal.provisional.core.IValidator validatorManager,
 			IProject rootProject, Collection<IRegion> dirtyRegions, IValidationContext helper, IReporter reporter,
 			EditorValidationContext validationContext, IProjectValidationContext projectContext, IFile changedFile) {
 		Logger.debug("*** Validating project {} after file {} changed... ***", changedFile.getProject().getName(),
 				changedFile.getFullPath());
 		ContextValidationHelper validationHelper = new ContextValidationHelper();
 		validationHelper.setProject(rootProject);
 		validationHelper.setValidationContextManager(validationContext);
 		init(rootProject, validationHelper, projectContext, validatorManager, reporter);
 		setAsYouTypeValidation(false);
 		this.document = validationContext.getDocument();
 		displaySubtask(JaxrsValidationMessages.VALIDATING_RESOURCE,
 				new String[] { changedFile.getProject().getName(), changedFile.getName() });
 		try {
 			final JaxrsMetamodel jaxrsMetamodel = JaxrsMetamodelLocator.get(changedFile.getProject());
 			final Set<IResource> allResources = completeValidationSet(jaxrsMetamodel, changedFile);
 			for (IResource changedResource : allResources) {
 				validate(reporter, changedResource, jaxrsMetamodel);
 			}
 		} catch (CoreException e) {
 			Logger.error("Failed to validate changed file " + changedFile.getName() + " in project " + changedFile.getProject(), e);
 		} finally {
 			Logger.debug("Validation done.");
 		}
 	}
 
 	@Override
 	public IStatus validateAll(IProject project, ContextValidationHelper validationHelper,
 			IProjectValidationContext validationContext, ValidatorManager manager, IReporter reporter)
 			throws ValidationException {
 		Logger.debug("*** Validating all files in project {} ***", project.getName());
 		init(project, validationHelper, validationContext, manager, reporter);
 		setAsYouTypeValidation(false);
 		displaySubtask(JaxrsValidationMessages.VALIDATING_PROJECT, new String[] { project.getName() });
 		try {
 			final JaxrsMetamodel jaxrsMetamodel = JaxrsMetamodelLocator.get(project);
 			if (jaxrsMetamodel != null) {
 				for (IJaxrsElement element : jaxrsMetamodel.getAllElements()) {
 					validate(element);
 				}
 				validate(jaxrsMetamodel);
 			}
 		} catch (CoreException e) {
 			Logger.error("Failed to validate project '", e);
 		} finally {
 			Logger.debug("Validation done.");
 		}
 
 		return Status.OK_STATUS;
 	}
 
 	/**
 	 * Uses the appropriate validator to validate the given JAX-RS element, or does nothing if no validator could be
 	 * found.
 	 * 
 	 * @param element
 	 * @throws CoreException
 	 */
 	@SuppressWarnings("incomplete-switch")
 	private void validate(IJaxrsElement element) throws CoreException {
 		Logger.debug("Validating element {}", element);
 		switch (element.getElementCategory()) {
 		case METAMODEL:
 			new JaxrsMetamodelValidatorDelegate(this, (JaxrsMetamodel)element).validate();
 			break;
 		case APPLICATION:
 			switch (element.getElementKind()) {
 			case APPLICATION_JAVA:
 				new JaxrsJavaApplicationValidatorDelegate(this, (JaxrsJavaApplication) element).validate();
 				break;
 			case APPLICATION_WEBXML:
 				new JaxrsWebxmlApplicationValidatorDelegate(this, (JaxrsWebxmlApplication) element).validate();
 				break;
 			}
 			break;
 		case HTTP_METHOD:
 			new JaxrsHttpMethodValidatorDelegate(this, (JaxrsHttpMethod) element).validate();
 			break;
 		case PROVIDER:
 			break;
 		case RESOURCE:
 			// this validator delegate also deals with ResourceMethods and ResourceFields
 			new JaxrsResourceValidatorDelegate(this, (JaxrsResource) element).validate();
 			break;
 		default:
 			// skipping other categories of elements at this validator level. (see above)
 			break;
 		}
 	}
 
 	@Override
 	protected String getMessageBundleName() {
 		return BUNDLE_NAME;
 	}
 
 	@Override
 	protected String getPreference(IProject project, String preferenceKey) {
 		return JaxrsPreferences.getInstance().getProjectPreference(project, preferenceKey);
 	}
 
 	public int getMaxNumberOfMarkersPerFile(IProject project) {
 		return JaxrsPreferences.getMaxNumberOfProblemMarkersPerFile(project);
 	}
 
 	@Override
 	public String getId() {
 		return ID;
 	}
 
 	@Override
 	public String getBuilderId() {
 		return JaxrsMetamodelBuilder.BUILDER_ID;
 	}
 
 	@Override
 	public IValidatingProjectTree getValidatingProjects(IProject project) {
 		return new SimpleValidatingProjectTree(project);
 	}
 
 	@Override
 	public void registerPreferenceInfo() {
 		PreferenceInfoManager.register(getProblemType(), new JaxrsPreferenceInfo());
 	}
 	
 	class JaxrsPreferenceInfo implements IPreferenceInfo{
 
 		@Override
 		public String getPreferencePageId() {
 			return PREFERENCE_PAGE_ID;
 		}
 
 		@Override
 		public String getPropertyPageId() {
 			return PROPERTY_PAGE_ID;
 		}
 
 		@Override
 		public String getPluginId() {
 			return JBossJaxrsCorePlugin.PLUGIN_ID;
 		}
 		
 	}
 
 }
