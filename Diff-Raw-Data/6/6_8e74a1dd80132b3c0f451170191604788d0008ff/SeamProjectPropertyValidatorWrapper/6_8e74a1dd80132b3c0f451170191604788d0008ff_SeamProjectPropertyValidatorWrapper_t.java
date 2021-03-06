 package org.jboss.tools.seam.core.test.validation;
 
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.wst.validation.internal.core.ValidationException;
 import org.eclipse.wst.validation.internal.provisional.core.IMessage;
 import org.eclipse.wst.validation.internal.provisional.core.IReporter;
 import org.jboss.tools.common.text.ITextSourceReference;
 import org.jboss.tools.jst.web.kb.internal.validation.ContextValidationHelper;
 import org.jboss.tools.jst.web.kb.internal.validation.ValidationErrorManager;
 import org.jboss.tools.jst.web.kb.internal.validation.ValidatorManager;
 import org.jboss.tools.jst.web.kb.validation.IProjectValidationContext;
 import org.jboss.tools.jst.web.kb.validation.IValidatingProjectTree;
 import org.jboss.tools.jst.web.kb.validation.IValidationErrorManager;
 import org.jboss.tools.jst.web.kb.validation.IValidator;
import org.jboss.tools.seam.core.SeamCoreBuilder;
 import org.jboss.tools.seam.internal.core.validation.SeamProjectPropertyValidator;
 import org.jboss.tools.seam.internal.core.validation.SeamValidationErrorManager;
 
 public class SeamProjectPropertyValidatorWrapper extends SeamProjectPropertyValidator implements IValidatorSupport, IValidator, IValidationErrorManager{
 	ValidatorSupport support;
 	SeamValidationErrorManager errorManager;
 	
 	public SeamProjectPropertyValidatorWrapper(IProject project) {
 		this.support = new ValidatorSupport(project,(IValidator)this);
 	}
 
 	public void validate() throws ValidationException {
 		support.validate();
 	}
 
 	public void add(IMarker marker) {
 		support.add(marker);
 	}
 
 	public boolean isMessageCreated(String template, Object[] parameters) {
 		return support.isMessageCreated(template, parameters);
 	}
 
 	public void addFile(IFile o) {
 		support.addFile(o);
 	}
 
 	public List<IMarker> getMarkers() {
 		return support.getMarkers();
 	}
 
 	public void addMessage(org.eclipse.wst.validation.internal.provisional.core.IValidator origin, IMessage message) {
 		support.addMessage(origin, message);
 	}
 
 	public void validate(IFile file) throws ValidationException {
 		support.validate(file);
 	}
 
 	public boolean isMessageCreatedOnLine(String markerTemplate,
 			Object[] parameters, int lineNumber) throws CoreException {
 		return support.isMessageCreatedOnLine(markerTemplate, parameters,
 				lineNumber);
 	}
 
 	public IStatus validate(Set<IFile> changedFiles, IProject project,
 			ContextValidationHelper validationHelper, IProjectValidationContext context, ValidatorManager manager,
 			IReporter reporter) throws ValidationException {
 		errorManager = new SeamValidationErrorManager() {
 			/* (non-Javadoc)
 			 * @see org.jboss.tools.jst.web.kb.internal.validation.ValidationErrorManager#getMarkerOwner()
 			 */
 			protected Class getMarkerOwner() {
 				return SeamProjectPropertyValidatorWrapper.this.getClass();
 			}
 
 			/* (non-Javadoc)
 			 * @see org.jboss.tools.jst.web.kb.internal.validation.ValidationErrorManager#init(org.eclipse.core.resources.IProject, org.jboss.tools.jst.web.kb.internal.validation.ContextValidationHelper, org.eclipse.wst.validation.internal.provisional.core.IValidator, org.eclipse.wst.validation.internal.provisional.core.IReporter)
 			 */
 			public void init(IProject project,
 					ContextValidationHelper validationHelper,
 					IProjectValidationContext validationContext,
 					org.eclipse.wst.validation.internal.provisional.core.IValidator manager, IReporter reporter) {
 				setProject(project);
 				setValidationContext(validationContext);
 				setValidationManager(manager);
 				setReporter(reporter);
 				setMarkerId(SeamValidationErrorManager.MARKED_SEAM_PROJECT_MESSAGE_GROUP);
 			}
 
 			@Override
 			public String getMarkerType() {
 				return ValidationErrorManager.DEFAULT_VALIDATION_MARKER;
 			}
 		};
 		//errorManager.init(project, null, this, reporter);
 
 		return validateInJob(validationHelper, reporter);
 	}
 
 	public IStatus validateAll(IProject project,
 			ContextValidationHelper validationHelper, IProjectValidationContext context, ValidatorManager manager,
 			IReporter reporter) throws ValidationException {
 		return null;
 	}
 
 	public String getId() {
 		return "id";
 	}
 
	public String getBuilderId() {
		return SeamCoreBuilder.BUILDER_ID;
	}

 	public IValidatingProjectTree getValidatingProjects(IProject project) {
 		return null;
 	}
 
 	public boolean shouldValidate(IProject project) {
 		return true;
 	}
 
 	public boolean isEnabled(IProject project) {
 		return true;
 	}
 	
 	protected IValidationErrorManager getTestValidationErrorManager(){
 		return this;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.validation.IValidationErrorManager#init(org.eclipse.core.resources.IProject, org.jboss.tools.jst.web.kb.internal.validation.ContextValidationHelper, org.jboss.tools.jst.web.kb.validation.IProjectValidationContext, org.eclipse.wst.validation.internal.provisional.core.IValidator, org.eclipse.wst.validation.internal.provisional.core.IReporter)
 	 */
 	public void init(
 			IProject project,
 			ContextValidationHelper validationHelper,
 			IProjectValidationContext validationContext,
 			org.eclipse.wst.validation.internal.provisional.core.IValidator manager,
 			IReporter reporter) {
 		ContextValidationHelper vh = new ContextValidationHelper();
 		vh.initialize();
 		errorManager.init(project, vh, validationContext, manager, reporter);
 	}
 
 	public IMarker addError(String message, String preferenceKey,
 			String[] messageArguments, ITextSourceReference location,
 			IResource target) {
 		IMarker marker = errorManager.addError(message, preferenceKey, messageArguments, location, target);
 		support.add(marker);
 		return marker;
 	}
 
 	public IMarker addError(String message, String preferenceKey,
 			String[] messageArguments, IResource target) {
 		IMarker marker = errorManager.addError(message, preferenceKey, messageArguments, target);
 		support.add(marker);
 		return marker;
 	}
 
 	public IMarker addError(String message, String preferenceKey,
 			ITextSourceReference location, IResource target) {
 		IMarker marker = errorManager.addError(message, preferenceKey, location, target);
 		support.add(marker);
 		return marker;
 	}
 
 	public IMarker addError(String message, String preferenceKey,
 			String[] messageArguments, int length, int offset, IResource target) {
 		IMarker marker = errorManager.addError(message, preferenceKey, messageArguments, length, offset, target);
 		support.add(marker);
 		return marker;
 	}
 
 	public IMarker addError(String message, int severity,
 			String[] messageArguments, int lineNumber, int length, int offset, IResource target) {
 		IMarker marker = errorManager.addError(message, severity, messageArguments, lineNumber, length, offset, target);
 		support.add(marker);
 		return marker;
 	}
 
 	public void displaySubtask(String message) {
 		errorManager.displaySubtask(message);
 	}
 
 	public void displaySubtask(String message, String[] messageArguments) {
 		errorManager.displaySubtask(message, messageArguments);
 	}
 
 	public void removeMessagesFromResources(Set<IResource> resources) {
 		errorManager.removeMessagesFromResources(resources);
 	}
 
 	public void removeAllMessagesFromResource(IResource resource) {
 		errorManager.removeAllMessagesFromResource(resource);
 	}
 
 	public IMarker addError(String message, String preferenceKey,
 			String[] messageArguments, int lineNumber, int length, int offset,
 			IResource target) {
 		IMarker marker = errorManager.addError(message, preferenceKey, messageArguments, lineNumber, length, offset, target);
 		support.add(marker);
 		return marker;
 	}
 }
