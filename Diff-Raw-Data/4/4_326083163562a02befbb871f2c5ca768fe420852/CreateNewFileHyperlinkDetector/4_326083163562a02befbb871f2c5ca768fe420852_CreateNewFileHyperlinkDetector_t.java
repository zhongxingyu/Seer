 /*******************************************************************************
  * Copyright (c) 2013 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.ui.internal.text.ext.hyperlink.internal;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.Region;
 import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
 import org.eclipse.jface.text.hyperlink.IHyperlink;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMText;
 import org.jboss.tools.common.text.ext.util.StructuredModelWrapper;
 import org.jboss.tools.common.text.ext.util.Utils;
 import org.jboss.tools.common.text.ext.util.Utils.AttrNodePair;
 import org.jboss.tools.common.web.WebUtils;
 import org.w3c.dom.Document;
 
 public class CreateNewFileHyperlinkDetector extends AbstractHyperlinkDetector {
 
 	public CreateNewFileHyperlinkDetector() {
 	}
 
 	@Override
 	public IHyperlink[] detectHyperlinks(ITextViewer textViewer,
 			IRegion region, boolean canShowMultipleHyperlinks) {
 		List<IHyperlink> links = new ArrayList<IHyperlink>();
 		
 		IFile file = getFile();
 		
 		if(file == null)
 			return null;
 		String extension = file.getFileExtension();
 		
 		if("xml".equals(extension) || //$NON-NLS-1$
 				"htm".equals(extension) || //$NON-NLS-1$
 				"html".equals(extension) || //$NON-NLS-1$
 				"jsp".equals(extension) || //$NON-NLS-1$
 				"xhtml".equals(extension)){ //$NON-NLS-1$
 			StructuredModelWrapper smw = new StructuredModelWrapper();
 			smw.init(textViewer.getDocument());
 			try {
 				Document xmlDocument = smw.getDocument();
 				if (xmlDocument == null)
 					return null;
 				
 				AttrNodePair pair = Utils.findAttrNodePairForOffset(xmlDocument, region.getOffset());
 				
 				if(pair != null){
 					if(pair.getAttribute() != null){
 						ITextRegion textRegion = pair.getAttribute().getValueRegion();
						if(textRegion != null && 
								region.getOffset() >= (pair.getNode().getStartOffset() + textRegion.getStart()) && 
								region.getOffset() <= (pair.getNode().getStartOffset() + textRegion.getEnd())){
 							Region nodeRegion = new Region(pair.getNode().getStartOffset() +textRegion.getStart()+1, textRegion.getLength()-2);
 							String attrValue = pair.getAttribute().getNodeValue();
 							if(validateName(attrValue)){
 								IFile linkFile = getLinkFile(file, attrValue);
 								if(linkFile != null){
 									links.add(new CreateNewFileHyperlink(textViewer.getDocument(), nodeRegion, attrValue, linkFile));
 								}
 							}
 						}
 					}else if(pair.getNode() != null && pair.getNode() instanceof IDOMText){
 						String text = pair.getNode().getNodeValue();
 						int startSpaces = getStartSpaces(text);
 						int endSpaces = getEndSpaces(text);
 						if(region.getOffset() >= (pair.getNode().getStartOffset() + startSpaces) && region.getOffset() <= (pair.getNode().getStartOffset() + (text.length()-endSpaces))){
 							if(validateName(text.trim())){
 								Region nodeRegion = new Region(pair.getNode().getStartOffset()+startSpaces, text.length()-(startSpaces+endSpaces));
 								IFile linkFile = getLinkFile(file, text.trim());
 								if(linkFile != null){
 									links.add(new CreateNewFileHyperlink(textViewer.getDocument(), nodeRegion, text.trim(), linkFile));
 								}
 							}
 						}
 					}
 				}
 			} finally {
 				smw.dispose();
 			}
 		}
 		if (links.size() == 0)
 			return null;
 		return links.toArray(new IHyperlink[links.size()]);
 	}
 	
 	private static int getStartSpaces(String text){
 		for(int index = 0; index < text.length(); index++){
 			char c = text.charAt(index);
 			if(c == ' ' || c == '\n' || c == '\r' || c == '\t'){
 				continue;
 			}
 			return index;
 		}
 		return 0;
 	}
 	
 	private static int getEndSpaces(String text){
 		for(int index = text.length()-1; index >= 0; index--){
 			char c = text.charAt(index);
 			if(c == ' ' || c == '\n' || c == '\r' || c == '\t'){
 				continue;
 			}
 			return text.length()-1-index;
 		}
 		return 0;
 	}
 	
 	private static boolean validateName(String name){
 		String nameLc = name.toLowerCase();
 		if(nameLc.endsWith(".js") || //$NON-NLS-1$
 				nameLc.endsWith(".css") || //$NON-NLS-1$
 			
 				nameLc.endsWith(".jsp") || //$NON-NLS-1$
 				nameLc.endsWith(".htm") || //$NON-NLS-1$
 				nameLc.endsWith(".html") || //$NON-NLS-1$
 				nameLc.endsWith(".xhtml")  //$NON-NLS-1$
 			){
 				try{
 					new URL(name);
 				}catch(MalformedURLException ex){
 					// local file, not remote one
 					return true;
 				}
 				
 		}
 		return false;
 	}
 	
 	private static IFile getLinkFile(IFile baseFile, String name){
 		if(!fileExists(baseFile, name)){
 			IProject project = baseFile.getProject();
 			if(name.startsWith("/")){ //$NON-NLS-1$
 				IPath webContentPath = WebUtils.getFirstWebContentPath(project);
 				if(webContentPath != null){
 					IPath container = webContentPath.segmentCount() > 1 ? webContentPath.removeFirstSegments(1) : project.getFullPath();
 					return project.getFile(container.append(name));
 				}
 				return project.getFile(name);
 			}
 			return project.getFile(baseFile.getFullPath().removeFirstSegments(1).removeLastSegments(1).append(name));
 		}
 		return null;
 	}
 	
 	private static boolean fileExists(IFile baseFile, String name){
 		IProject project = baseFile.getProject();
 		if(name.startsWith("/")){ //$NON-NLS-1$
 			IPath[] webContentPaths = WebUtils.getWebContentPaths(project);
 			for(IPath webContentPath : webContentPaths){
 				if(webContentPath != null){
 					IPath container = webContentPath.segmentCount() > 1 ? webContentPath.removeFirstSegments(1) : project.getFullPath();
 					if(project.getFile(container.append(name)).exists()){
 						return true;
 					}
 				}
 			}
 		}else{
 			return project.getFile(baseFile.getFullPath().removeFirstSegments(1).removeLastSegments(1).append(name)).exists();
 		}
 		return false;
 	}
 	
 	private static IFile getFile(){
 		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
 		if(part != null){
 			IEditorInput input = part.getEditorInput();
 			if(input instanceof FileEditorInput)
 				return ((FileEditorInput)input).getFile();
 		}
 		return null;
 	}
 	
 	
 
 }
