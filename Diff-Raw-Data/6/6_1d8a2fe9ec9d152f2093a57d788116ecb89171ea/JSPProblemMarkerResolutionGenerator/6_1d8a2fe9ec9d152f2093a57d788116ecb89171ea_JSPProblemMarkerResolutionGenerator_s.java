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
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.Position;
 import org.eclipse.jface.text.source.Annotation;
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
 import org.jboss.tools.common.el.core.resolver.ELContext;
 import org.jboss.tools.common.quickfix.IQuickFixGenerator;
 import org.jboss.tools.common.refactoring.MarkerResolutionUtils;
 import org.jboss.tools.common.text.ext.util.StructuredModelWrapper;
 import org.jboss.tools.common.text.ext.util.Utils;
 import org.jboss.tools.jst.web.kb.IKbProject;
 import org.jboss.tools.jst.web.kb.KbProjectFactory;
 import org.jboss.tools.jst.web.kb.PageContextFactory;
 import org.jboss.tools.jst.web.kb.internal.XmlContextImpl;
 import org.jboss.tools.jst.web.kb.internal.taglib.TLDLibrary;
 import org.jboss.tools.jst.web.kb.taglib.INameSpace;
 import org.jboss.tools.jst.web.kb.taglib.ITagLibrary;
 import org.jboss.tools.jst.web.ui.WebUiPlugin;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
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
 	
 	private static final String MISSING_ATTRIBUTE = "Missing required attribute"; //$NON-NLS-1$
 	
 	@Override
 	public IMarkerResolution[] getResolutions(IMarker marker) {
 		try{
 			return findResolutions(marker);
 		}catch(CoreException ex){
 			WebUiPlugin.getPluginLog().logError(ex);
 		}
 		return new IMarkerResolution[]{};
 	}
 	
 	public static boolean validatePrefix(IFile file, int start, String prefix){
 		ELContext context = PageContextFactory.createPageContext(file);
 		if(context instanceof XmlContextImpl){
 			 Map<String, List<INameSpace>> nameSpaces = ((XmlContextImpl) context).getNameSpaces(start);
 			 Iterator<List<INameSpace>> iterator = nameSpaces.values().iterator();
 			 while(iterator.hasNext()){
 				 List<INameSpace> list = iterator.next();
 				 for(INameSpace ns : list){
 					 if(prefix.equals(ns.getPrefix())){
 						 return false;
 					 }
 				 }
 			 }
 		}
 		return true;
 	}
 	
 	public static boolean validateURI(IFile file, int start, String uri){
 //		ELContext context = PageContextFactory.createPageContext(file);
 //		if(context instanceof XmlContextImpl){
 //			 Map<String, List<INameSpace>> nameSpaces = ((XmlContextImpl) context).getNameSpaces(start);
 //			 Iterator<List<INameSpace>> iterator = nameSpaces.values().iterator();
 //			 while(iterator.hasNext()){
 //				 List<INameSpace> list = iterator.next();
 //				 for(INameSpace ns : list){
 //					 if(uri.equals(ns.getURI())){
 //						 return false;
 //					 }
 //				 }
 //			 }
 //		}
 		return true;
 	}
 	
 	private void getAddAttribute(ArrayList<IJavaCompletionProposal> proposals, TemporaryAnnotation ta, String message, int start, int end){
 		String attributeName = getAttributeName(message);
 		
 		
 		IFile file = MarkerResolutionUtils.getFile();
 		if(file == null)
 			return;
 
 		Object additionalInfo = ta.getAdditionalFixInfo();
 		if(additionalInfo instanceof IDocument){
 			IStructuredModel model = StructuredModelManager.getModelManager().getModelForRead((IStructuredDocument)additionalInfo);
 			IDOMDocument xmlDocument = (model instanceof IDOMModel) ? ((IDOMModel) model).getDocument() : null;
 
 			Node n = Utils.findNodeForOffset(xmlDocument, start);
 
 			if (n == null || !(n instanceof Attr))
 				return;
 				
 			Node node = ((Attr)n).getOwnerElement();
 
 			String prefix = node.getPrefix();
 			String tagName = node.getNodeName();
 			
 			proposals.add(new AddAttributeMarkerResolution(file, node, attributeName, start, end));
 			
 		}
 	}
 	
 	private void getAddTLD(ArrayList<IJavaCompletionProposal> proposals, TemporaryAnnotation ta, String message, int start, int end){
 		String prefix = getPrifix(message);
 		if(prefix == null)
 			return;
 		
 		String tagName = getTagName(message);
 		if(tagName == null)
 			return;
 		
 		IFile file = MarkerResolutionUtils.getFile();
 		if(file == null)
 			return;
 		
 		if(!validatePrefix(file, start, prefix)){
 			return;
 		}
 		
 		Object additionalInfo = ta.getAdditionalFixInfo();
 		if(additionalInfo instanceof IDocument){
 			IStructuredModel model = StructuredModelManager.getModelManager().getModelForRead((IStructuredDocument)additionalInfo);
 			IDOMDocument xmlDocument = (model instanceof IDOMModel) ? ((IDOMModel) model).getDocument() : null;
 			
 			IKbProject kbProject = KbProjectFactory.getKbProject(file.getProject(), true);
 			if(kbProject == null){
 				return;
 			}
 			
 			List<ITagLibrary> libraries = kbProject.getAllTagLibraries();
 			ArrayList<String> names = new ArrayList<String>();
 			for(ITagLibrary l : libraries){
 				if(l instanceof TLDLibrary){
 					((TLDLibrary) l).createDefaultNameSpace();
 				}
 				INameSpace ns = l.getDefaultNameSpace();
 				if(ns != null && ns.getPrefix() != null && ns.getPrefix().equals(prefix)){
 					String uri = ns.getURI();
 					String resolutionName = getResolutionName(xmlDocument != null && xmlDocument.isXMLType(), true, prefix, uri);
 					if(resolutionName != null && !names.contains(resolutionName) && l.getComponent(tagName) != null && validateURI(file, start, uri)){
 						proposals.add(new AddTLDMarkerResolution(file, resolutionName, start, end, uri, prefix));
 						names.add(resolutionName);
 					}
 				}
 			}
 			
 			for(ITagLibrary l : libraries){
 				INameSpace ns = l.getDefaultNameSpace();
 				if(ns != null && ns.getPrefix() != null && ns.getPrefix().equals(prefix))
 					continue;
 				
 				String uri = l.getURI();
 				String resolutionName = getResolutionName(xmlDocument != null && xmlDocument.isXMLType(), true, prefix, uri);
 				if(resolutionName != null && !names.contains(resolutionName) && l.getComponent(tagName) != null && validateURI(file, start, uri)){
 					proposals.add(new AddTLDMarkerResolution(file, resolutionName, start, end, uri, prefix));
 					names.add(resolutionName);
 				}
 			}
 			
 		}
 	}
 	
 	private IJavaCompletionProposal[] findProposals(Annotation annotation, Position position){
 		ArrayList<IJavaCompletionProposal> proposals = new ArrayList<IJavaCompletionProposal>();
 		if(!(annotation instanceof TemporaryAnnotation)){
 			return new IJavaCompletionProposal[]{};
 		}
 		TemporaryAnnotation ta = (TemporaryAnnotation)annotation;
 		
 		String message = annotation.getText();
 		if(ta.getPosition() == null)
 			return new IJavaCompletionProposal[]{};
 		
 		final int start = position.getOffset();
 		
 		final int end = position.getOffset()+position.getLength();
 		
 		if(message.startsWith(UNKNOWN_TAG)){
 			getAddTLD(proposals, ta, message, start, end);
 		}else if(message.startsWith(MISSING_ATTRIBUTE)){
 			getAddAttribute(proposals, ta, message, start, end);
 		}
 		return proposals.toArray(new IJavaCompletionProposal[]{});
 	}
 	
 	private String getResolutionName(boolean xml, boolean noXML, String prefix, String uri){
 		if(xml){
 			return "xmlns: "+prefix+" = \""+uri+"\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		}else if(noXML){
 			return "<%@ taglib uri = \""+uri+"\" prefix=\""+prefix+"\" %>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		}
 		return null;
 	}
 	
 	private void getAddAttribute(ArrayList<IMarkerResolution> resolutions, IMarker marker, String message, int start, int end) throws CoreException{
 		String attributeName = getAttributeName(message);
 		
 		IFile file = (IFile)marker.getResource();
 		
 		FileEditorInput input = new FileEditorInput(file);
 		IDocumentProvider provider = DocumentProviderRegistry.getDefault().getDocumentProvider(input);
 		IDocument document = provider.getDocument(input);
 		StructuredModelWrapper smw = new StructuredModelWrapper();
 		try {
 			smw.init(file);
 			Document xmlDocument = smw.getDocument();
 			if (xmlDocument == null) return;
 			
 			Node node = Utils.findNodeForOffset(xmlDocument, start);
 
 			if (node == null) return;
 			
 			String tagName = node.getNodeName();
 			
 			resolutions.add(new AddAttributeMarkerResolution(file, node, attributeName, start, end));
 		} catch (IOException ex) {
 			WebUiPlugin.getPluginLog().logError(ex);
 		} finally {
 			smw.dispose();
 		}
 	}
 	
 	private void getAddTLD(ArrayList<IMarkerResolution> resolutions, IMarker marker, String message, int start, int end) throws CoreException{
 		String prefix = getPrifix(message);
 		if(prefix == null)
 			return;
 		
 		String tagName = getTagName(message);
 		if(tagName == null)
 			return;
 		
 		IFile file = (IFile)marker.getResource();
 		
 		if(!validatePrefix(file, start, prefix)){
 			return;
 		}
 		
 		IKbProject kbProject = KbProjectFactory.getKbProject(file.getProject(), true);
 		
 		List<ITagLibrary> libraries = kbProject.getAllTagLibraries();
 		ArrayList<String> names = new ArrayList<String>();
 		for(ITagLibrary l : libraries){
 			if(l instanceof TLDLibrary){
 				((TLDLibrary) l).createDefaultNameSpace();
 			}
 			INameSpace ns = l.getDefaultNameSpace();
 			if(ns != null && ns.getPrefix() != null && ns.getPrefix().equals(prefix)){
 				String uri = ns.getURI();
 				String resolutionName = getResolutionName(marker.getType().equals(HTML_VALIDATOR_MARKER) || marker.isSubtypeOf(HTML_VALIDATOR_MARKER), marker.getType().equals(JSP_VALIDATOR_MARKER) || marker.isSubtypeOf(JSP_VALIDATOR_MARKER), prefix, uri);
 				if(resolutionName != null && !names.contains(resolutionName) && l.getComponent(tagName) != null && validateURI(file, start, uri)){
 					resolutions.add(new AddTLDMarkerResolution(file, resolutionName, start, end, uri, prefix));
 					names.add(resolutionName);
 				}
 			}
 		}
 		for(ITagLibrary l : libraries){
 			INameSpace ns = l.getDefaultNameSpace();
 			if(ns != null && ns.getPrefix() != null && ns.getPrefix().equals(prefix))
 				continue;
 			
 			String uri = l.getURI();
 			String resolutionName = getResolutionName(marker.getType().equals(HTML_VALIDATOR_MARKER) || marker.isSubtypeOf(HTML_VALIDATOR_MARKER), marker.getType().equals(JSP_VALIDATOR_MARKER) || marker.isSubtypeOf(JSP_VALIDATOR_MARKER), prefix, uri);
 			if(resolutionName != null && !names.contains(resolutionName) && l.getComponent(tagName) != null && validateURI(file, start, uri)){
 				resolutions.add(new AddTLDMarkerResolution(file, resolutionName, start, end, uri, prefix));
 				names.add(resolutionName);
 			}
 		}
 	}
 	
 	private IMarkerResolution[] findResolutions(IMarker marker) throws CoreException{
 		ArrayList<IMarkerResolution> resolutions = new ArrayList<IMarkerResolution>();
 		String message = (String)marker.getAttribute(IMarker.MESSAGE);
 		
 		Integer attribute =  ((Integer)marker.getAttribute(IMarker.CHAR_START));
 		if(attribute == null)
 			return new IMarkerResolution[]{};
 		final int start = attribute.intValue();
 		
 		attribute = ((Integer)marker.getAttribute(IMarker.CHAR_END));
 		if(attribute == null)
 			return new IMarkerResolution[]{};
 		final int end = attribute.intValue();
 		
 		if(message.startsWith(UNKNOWN_TAG)){
 			getAddTLD(resolutions, marker, message, start, end);
 		}else if(message.startsWith(MISSING_ATTRIBUTE)){
 			getAddAttribute(resolutions, marker, message, start, end);
 		}
 
 		return resolutions.toArray(new IMarkerResolution[]{});
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
 
 	public static String getTagName(String message){
 		String tagName=""; //$NON-NLS-1$
 		
 		int start = message.indexOf(":"); //$NON-NLS-1$
 		if(start < 0)
 			return null;
 		
 		int end = message.indexOf(")", start); //$NON-NLS-1$
 		if(end < 0)
 			return null;
 		
 		tagName = message.substring(start+1, end);
 		
 		return tagName;
 	}
 	
 	public static String getAttributeName(String message){
 		String attributeName=""; //$NON-NLS-1$
 		
 		int start = message.indexOf("\""); //$NON-NLS-1$
 		if(start < 0)
 			return null;
 		
 		int end = message.lastIndexOf("\""); //$NON-NLS-1$
 		if(end < 0)
 			return null;
 		
 		attributeName = message.substring(start+1, end);
 		
 		return attributeName;
 	}
 
 	@Override
 	public boolean hasResolutions(IMarker marker) {
 		if(marker.exists()){
 			String message = marker.getAttribute(IMarker.MESSAGE, "");
 			return message.startsWith(UNKNOWN_TAG) || message.startsWith(MISSING_ATTRIBUTE);
 		}
 		return false;
 	}
 
 	@Override
 	public boolean hasProposals(Annotation annotation, Position position) {
 		String message = annotation.getText();
 		return message.startsWith(UNKNOWN_TAG) || message.startsWith(MISSING_ATTRIBUTE);
 	}
 
 	@Override
 	public IJavaCompletionProposal[] getProposals(Annotation annotation, Position position) {
 		return findProposals(annotation, position); 
 	}
 }
