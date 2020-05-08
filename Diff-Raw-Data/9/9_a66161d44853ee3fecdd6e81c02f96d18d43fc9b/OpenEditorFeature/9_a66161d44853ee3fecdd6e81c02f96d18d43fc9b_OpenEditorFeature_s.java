 package com.vectorsf.jvoice.diagram.core.pattern.states;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.ui.URIEditorInput;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.ICustomContext;
 import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.ide.IDE;
 import org.eclipse.xtext.ui.editor.IURIEditorOpener;
 
 import com.isb.bks.ivr.voice.dsl.ui.internal.VoiceDslActivator;
 import com.vectorsf.jvoice.model.operations.CallFlowState;
 import com.vectorsf.jvoice.model.operations.Flow;
 import com.vectorsf.jvoice.model.operations.LocutionState;
 
 public class OpenEditorFeature extends AbstractCustomFeature {
 
 	public OpenEditorFeature(IFeatureProvider fp) {
 		super(fp);
 	}
 
 	@Override
 	public void execute(ICustomContext context) {
 		Object businessObject = getFeatureProvider().getBusinessObjectForPictogramElement(
 				context.getInnerPictogramElement());
 
 		EObject eObject = null;
 		if (businessObject instanceof CallFlowState) {
 			CallFlowState callFlowState = (CallFlowState) businessObject;
 			eObject = callFlowState.getSubflow();
 		} else if (businessObject instanceof LocutionState) {
 			LocutionState locutionState = (LocutionState) businessObject;
 			eObject = locutionState.getLocution();
 		}
 
 		if (eObject != null) {
 			openContainerFile(eObject);
 		}
 
 	}
 
 	private void openContainerFile(EObject eObject) {
 		URI locutionUri = EcoreUtil.getURI(eObject);
 		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 
 		String fileString = locutionUri.toPlatformString(true);
 		try {
 			if (fileString != null) {
 				IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(fileString));
 
				IDE.openEditor(page, file);
 			} else {
 				URIEditorInput input = new URIEditorInput(locutionUri);
 				if (eObject instanceof Flow) {
 					IDE.openEditor(page, input, "org.eclipse.graphiti.ui.editor.DiagramEditorJVoice");
 				} else { // Locution
 					IURIEditorOpener instance = VoiceDslActivator.getInstance()
 							.getInjector(VoiceDslActivator.COM_ISB_BKS_IVR_VOICE_DSL_VOICEDSL)
 							.getInstance(IURIEditorOpener.class);
 					instance.open(locutionUri, false);
 				}
 			}
 		} catch (PartInitException e) {
 		}
 	}
 
 	@Override
 	public boolean hasDoneChanges() {
 		return false;
 	}
 
 	@Override
 	public boolean canExecute(ICustomContext context) {
 		return true;
 	}
 }
