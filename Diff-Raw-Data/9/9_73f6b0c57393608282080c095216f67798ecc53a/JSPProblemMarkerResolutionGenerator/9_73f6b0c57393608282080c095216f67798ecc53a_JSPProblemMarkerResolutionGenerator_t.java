 /******************************************************************************* 
  * Copyright (c) 2010 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/
 package org.jboss.tools.jst.web.ui.action;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.TextSelection;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.jface.text.source.Annotation;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.ui.IMarkerResolution;
 import org.eclipse.ui.IMarkerResolutionGenerator2;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.texteditor.DocumentProviderRegistry;
 import org.eclipse.ui.texteditor.IDocumentProvider;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.eclipse.wst.sse.core.internal.provisional.StructuredModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
 import org.eclipse.wst.sse.ui.internal.reconcile.TemporaryAnnotation;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
 import org.jboss.tools.common.model.ui.views.palette.PaletteInsertHelper;
import org.jboss.tools.common.quickfix.IQuickFixGenerator;
 import org.jboss.tools.jst.jsp.jspeditor.dnd.JSPPaletteInsertHelper;
 import org.jboss.tools.jst.jsp.jspeditor.dnd.PaletteTaglibInserter;
 import org.jboss.tools.jst.web.ui.WebUiPlugin;
 
 /**
  * Shows the Marker Resolutions for Unknown tag JSP Problem Marker
  * 
  * @author Daniel Azarov
  *
  */
 public class JSPProblemMarkerResolutionGenerator implements IMarkerResolutionGenerator2, IQuickFixGenerator {
 	
 	private static final String HTML_VALIDATOR_MARKER="org.eclipse.wst.html.core.validationMarker"; //$NON-NLS-1$
 	private static final String JSP_VALIDATOR_MARKER="org.eclipse.jst.jsp.core.validationMarker"; //$NON-NLS-1$
 	
 	private static final String UNKNOWN_TAG = "Unknown tag"; //$NON-NLS-1$
 	
 	public static HashMap<String, String> libs = new HashMap<String, String>();
 	static{
 		libs.put("s", "http://jboss.com/products/seam/taglib");  //$NON-NLS-1$//$NON-NLS-2$
 		libs.put("ui", "http://java.sun.com/jsf/facelets"); //$NON-NLS-1$ //$NON-NLS-2$
 		libs.put("f", "http://java.sun.com/jsf/core"); //$NON-NLS-1$ //$NON-NLS-2$
 		libs.put("h", "http://java.sun.com/jsf/html"); //$NON-NLS-1$ //$NON-NLS-2$
 		libs.put("rich", "http://richfaces.org/rich"); //$NON-NLS-1$ //$NON-NLS-2$
 		libs.put("a4j", "http://richfaces.org/a4j"); //$NON-NLS-1$ //$NON-NLS-2$
 		libs.put("a", "http://richfaces.org/a4j"); //$NON-NLS-1$ //$NON-NLS-2$
 		libs.put("c", "http://java.sun.com/jstl/core"); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	private IFile file;
 	private Properties properties;
 	private String resolutionName;
 	
 	@Override
 	public IMarkerResolution[] getResolutions(IMarker marker) {
 		try{
 			if(isOurCase(marker)){
 				return new IMarkerResolution[] {
 					new AddTLDMarkerResolution(file, resolutionName, properties)
 				};
 			}
 		}catch(CoreException ex){
 			WebUiPlugin.getPluginLog().logError(ex);
 		}
 		return new IMarkerResolution[]{};
 	}
 	
 	private IJavaCompletionProposal isOurCase(Annotation annotation){
 		if(!(annotation instanceof TemporaryAnnotation)){
 			return null;
 		}
 		TemporaryAnnotation ta = (TemporaryAnnotation)annotation;
 		
 		
 		String message = annotation.getText();
 		if(ta.getPosition() == null)
 			return null;
 		
 		final int start = ta.getPosition().getOffset();
 		
 		final int end = ta.getPosition().getOffset()+ta.getPosition().getLength();
 		
 		if(!message.startsWith(UNKNOWN_TAG))
 			return null;
 		
 		String prefix = getPrifix(message);
 		if(prefix == null)
 			return null;
 		
 		if(!libs.containsKey(prefix))
 			return null;
 		
 		Object additionalInfo = ta.getAdditionalFixInfo();
 		if(additionalInfo instanceof IDocument){
 			IStructuredModel model = StructuredModelManager.getModelManager().getModelForRead((IStructuredDocument)additionalInfo);
 			IDOMDocument xmlDocument = (model instanceof IDOMModel) ? ((IDOMModel) model).getDocument() : null;
 			if(xmlDocument != null && xmlDocument.isXMLType()){
 				resolutionName = "xmlns: "+prefix+" = \""+libs.get(prefix)+"\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 			}else{
 				resolutionName = "<%@ taglib uri = \""+libs.get(prefix)+"\" prefix=\""+prefix+"\" %>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 			}
 			
 		}
 		
 		
 		
 		return new AddTLDMarkerResolution(resolutionName, start, end, libs.get(prefix), prefix);
 	}
 	
 	private boolean isOurCase(IMarker marker) throws CoreException{
 		String message = (String)marker.getAttribute(IMarker.MESSAGE);
 		
 		Integer attribute =  ((Integer)marker.getAttribute(IMarker.CHAR_START));
 		if(attribute == null)
 			return false;
 		final int start = attribute.intValue();
 		
 		attribute = ((Integer)marker.getAttribute(IMarker.CHAR_END));
 		if(attribute == null)
 			return false;
 		final int end = attribute.intValue();
 		
 		if(!message.startsWith(UNKNOWN_TAG))
 			return false;
 		
 		String prefix = getPrifix(message);
 		if(prefix == null)
 			return false;
 		
 		if(!libs.containsKey(prefix))
 			return false;
 		
 		file = (IFile)marker.getResource();
 		
 		FileEditorInput input = new FileEditorInput(file);
 		IDocumentProvider provider = DocumentProviderRegistry.getDefault().getDocumentProvider(input);
 		try {
 			provider.connect(input);
 		} catch (CoreException e) {
 			WebUiPlugin.getPluginLog().logError(e);
 		}
 		
 		IDocument document = provider.getDocument(input);
 
 		properties = new Properties();
 		properties.put(JSPPaletteInsertHelper.PROPOPERTY_ADD_TAGLIB, "true"); //$NON-NLS-1$
 		properties.put(PaletteInsertHelper.PROPOPERTY_START_TEXT, ""); //$NON-NLS-1$
 		properties.put(JSPPaletteInsertHelper.PROPOPERTY_TAGLIBRARY_URI, libs.get(prefix));
 		properties.put(JSPPaletteInsertHelper.PROPOPERTY_DEFAULT_PREFIX, prefix);
 		properties.put(PaletteInsertHelper.PROPOPERTY_SELECTION_PROVIDER, new ISelectionProvider() {
 			
 			@Override
 			public void setSelection(ISelection selection) {
 			}
 			
 			@Override
 			public void removeSelectionChangedListener(
 					ISelectionChangedListener listener) {
 			}
 			
 			@Override
 			public ISelection getSelection() {
 				return new TextSelection(start, end-start);
 			}
 			
 			@Override
 			public void addSelectionChangedListener(ISelectionChangedListener listener) {
 			}
 		});
 		
 		
 		Properties p = PaletteTaglibInserter.getPrefixes(document, properties);
 		
 		provider.disconnect(input);
 		
 		if(p.containsValue(prefix))
 			return false;
 		
 		if(marker.getType().equals(HTML_VALIDATOR_MARKER) || marker.isSubtypeOf(HTML_VALIDATOR_MARKER)){
 			resolutionName = "xmlns: "+prefix+" = \""+libs.get(prefix)+"\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		}else if(marker.getType().equals(JSP_VALIDATOR_MARKER) || marker.isSubtypeOf(JSP_VALIDATOR_MARKER)){
 			resolutionName = "<%@ taglib uri = \""+libs.get(prefix)+"\" prefix=\""+prefix+"\" %>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		}
 		
 		return true;
 	}
 	
 	public static String getPrifix(String message){
 		String prefix=""; //$NON-NLS-1$
 		
 		int start = message.indexOf("("); //$NON-NLS-1$
 		if(start < 0)
 			return null;
 		
 		int end = message.indexOf(":", start); //$NON-NLS-1$
 		if(end < 0)
 			return null;
 		
 		prefix = message.substring(start+1, end);
 		
 		return prefix;
 	}
 
 	@Override
 	public boolean hasResolutions(IMarker marker) {
 		try{
 			String message = (String)marker.getAttribute(IMarker.MESSAGE);
 			return message.startsWith(UNKNOWN_TAG);
 		}catch(CoreException ex){
 			WebUiPlugin.getPluginLog().logError(ex);
 		}
 		return false;
 	}
 
 	@Override
 	public boolean hasProposals(Annotation annotation) {
 		String message = annotation.getText();
 		return message.startsWith(UNKNOWN_TAG);
 	}
 
 	@Override
 	public List<IJavaCompletionProposal> getProposals(Annotation annotation) {
 		ArrayList<IJavaCompletionProposal> proposals = new ArrayList<IJavaCompletionProposal>();
 		IJavaCompletionProposal proposal = isOurCase(annotation); 
 		if(proposal != null){
 			proposals.add(proposal);
 		}
 		return proposals;
 	}
 }
