 /*******************************************************************************
   * Copyright (c) 2010 Red Hat, Inc.
   * Distributed under license by Red Hat, Inc. All rights reserved.
   * This program is made available under the terms of the
   * Eclipse Public License v1.0 which accompanies this distribution,
   * and is available at http://www.eclipse.org/legal/epl-v10.html
   *
   * Contributors:
   *     Red Hat, Inc. - initial API and implementation
   ******************************************************************************/
 package org.jboss.tools.jst.web.kb.refactoring;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.internal.ui.text.FastJavaPartitionScanner;
 import org.eclipse.jdt.ui.text.IJavaPartitions;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.TextSelection;
 import org.eclipse.jface.text.rules.IToken;
 import org.eclipse.jface.text.rules.Token;
 import org.eclipse.wst.sse.core.StructuredModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.IModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
 import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
 import org.jboss.tools.common.el.core.ELCorePlugin;
 import org.jboss.tools.common.model.project.ProjectHome;
 import org.jboss.tools.common.model.util.EclipseResourceUtil;
 import org.jboss.tools.common.util.FileUtil;
 import org.jboss.tools.jst.web.kb.WebKbPlugin;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class SearchUtil {
 	public static final int JAVA_FILES = 1 << 1;
 	public static final int XML_FILES = 1 << 2;
 	public static final int PROPERTY_FILES = 1 << 3;
 	public static final int EVERYWHERE = JAVA_FILES|XML_FILES|PROPERTY_FILES;
 	
 	
 	private static final String JAVA_EXT = "java"; //$NON-NLS-1$
 	private static final String XML_EXT = "xml"; //$NON-NLS-1$
 	private static final String XHTML_EXT = "xhtml"; //$NON-NLS-1$
 	private static final String JSP_EXT = "jsp"; //$NON-NLS-1$
 	private static final String PROPERTIES_EXT = "properties"; //$NON-NLS-1$
 	
 	private int fileSet;
 	private String nodeName=null;
 	private String attributeName=null;
 	private SearchResult result = null;
 	private FileResult lastResult=null;
 	
 	private String searchString;
 	
 	public SearchUtil(int fileSet, String searchString){
 		this.fileSet = fileSet;
 		this.searchString = searchString;
 		result = new SearchResult();
 	}
 	
 	private boolean isSearchInJavaFiles () {
 		if ((fileSet & JAVA_FILES) != 0)
 			return true;
 		return false;
 	}
 	
 	private boolean isSearchInXMLFiles () {
 		if ((fileSet & XML_FILES) != 0)
 			return true;
 		return false;
 	}
 	
 	private boolean isSearchInPropertyFiles () {
 		if ((fileSet & PROPERTY_FILES) != 0)
 			return true;
 		return false;
 	}
 	
 	public SearchResult searchInNodeAttribute(IProject project, String nodeName, String attributeName){
 		this.nodeName = nodeName;
 		this.attributeName = attributeName;
 		return search(project);
 	}
 	
 	public SearchResult search(IProject project){
 		
 		if(isSearchInJavaFiles()){
 			IJavaProject javaProject = EclipseResourceUtil.getJavaProject(project);
 			
 			// searching java, xml and property files in source folders
 			if(javaProject != null){
 				for(IResource resource : EclipseResourceUtil.getJavaSourceRoots(project)){
 					if(resource instanceof IFolder)
 						scanForJava((IFolder) resource);
 					else if(resource instanceof IFile)
 						scanForJava((IFile) resource);
 				}
 			}
 		}
 		
 		// searching jsp, xhtml and xml files in WebContent folders
 		if(isSearchInXMLFiles()){
 			if(getViewFolder(project) != null)
 				scan(getViewFolder(project));
 			else
 				scan(project);
 		}
 		
 		return result;
 	}
 	
 	protected IContainer getViewFolder(IProject project){
 		IPath path = ProjectHome.getFirstWebContentPath(project);
 		
 		if(path != null)
			return project.getFolder(path.removeFirstSegments(1));
 		
 		return null;
 	}
 
 	
 	private boolean isFileCorrect(IFile file){
 		if(!file.isSynchronized(IResource.DEPTH_ZERO)){
 			return false;
 		}else if(file.isPhantom()){
 			return false;
 		}else if(file.isReadOnly()){
 			return false;
 		}
 		return true;
 	}
 
 	
 	private void scan(IContainer container){
 		try{
 			for(IResource resource : container.members()){
 				if(resource instanceof IFolder)
 					scan((IFolder) resource);
 				else if(resource instanceof IFile)
 					scan((IFile) resource);
 			}
 		}catch(CoreException ex){
 			ELCorePlugin.getDefault().logError(ex);
 		}
 	}
 	
 	private void scanForJava(IContainer container){
 		try{
 			for(IResource resource : container.members()){
 				if(resource instanceof IFolder)
 					scanForJava((IFolder) resource);
 				else if(resource instanceof IFile)
 					scanForJava((IFile) resource);
 			}
 		}catch(CoreException ex){
 			ELCorePlugin.getDefault().logError(ex);
 		}
 	}
 	
 	private void scan(IFile file){
 		if(isFileCorrect(file)) {
 			String fileContent=null;
 			try {
 				fileContent = FileUtil.readStream(file);
 			} catch (CoreException e) {
 				ELCorePlugin.getDefault().logError(e);
 			}
 			String ext = file.getFileExtension();			
 			if(XHTML_EXT.equalsIgnoreCase(ext) 
 				|| JSP_EXT.equalsIgnoreCase(ext)) {
 				scanInDOM(file, fileContent);
 			}
 		}
 	}
 	
 	private void scanForJava(IFile file){
 		if(isFileCorrect(file)) {
 			String fileContent=null;
 			try {
 				fileContent = FileUtil.readStream(file);
 			} catch (CoreException e) {
 				ELCorePlugin.getDefault().logError(e);
 			}
 			String ext = file.getFileExtension();			
 			if(JAVA_EXT.equalsIgnoreCase(ext)) {
 				scanInJava(file, fileContent);
 			}
 		}
 	}
 
 	
 	private boolean scanInDOM(IFile file, String content){
 		IModelManager manager = StructuredModelManager.getModelManager();
 		if(manager == null) {
 			return false;
 		}
 		IStructuredModel model = null;		
 		try {
 			model = manager.getModelForRead(file);
 			if (model instanceof IDOMModel) {
 				IDOMModel domModel = (IDOMModel) model;
 				IDOMDocument document = domModel.getDocument();
 				return scanChildNodes(file, document);
 			}
 		} catch (CoreException e) {
 			WebKbPlugin.getDefault().logError(e);
         } catch (IOException e) {
         	WebKbPlugin.getDefault().logError(e);
 		} finally {
 			if (model != null) {
 				model.releaseFromRead();
 			}
 		}
 		return false;
 	}
 	
 	private boolean scanChildNodes(IFile file, Node parent) {
 		boolean status = false;
 		
 		if(parent == null)
 			return false;
 		
 		NodeList children = parent.getChildNodes();
 		for(int i=0; i<children.getLength(); i++) {
 			Node curentValidatedNode = children.item(i);
 			if(Node.ELEMENT_NODE == curentValidatedNode.getNodeType()) {
 				if(nodeName == null || curentValidatedNode.getNodeName().endsWith(nodeName))
 					status = scanNodeContent(file, ((IDOMNode)curentValidatedNode).getFirstStructuredDocumentRegion(), DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE);
 				if(status)
 					return status;
 			}/* else if(Node.TEXT_NODE == curentValidatedNode.getNodeType()) {
 				status = scanNodeContent(file, ((IDOMNode)curentValidatedNode).getFirstStructuredDocumentRegion(), DOMRegionContext.XML_CONTENT);
 				if(status)
 					return status;
 			}*/
 			status = scanChildNodes(file, curentValidatedNode);
 			if(status)
 				return status;
 		}
 		return false;
 	}
 	
 	private boolean scanInJava(IFile file, String content){
 		try {
 			FastJavaPartitionScanner scaner = new FastJavaPartitionScanner();
 			Document document = new Document(content);
 			scaner.setRange(document, 0, document.getLength());
 			IToken token = scaner.nextToken();
 			while(token!=null && token!=Token.EOF) {
 				if(IJavaPartitions.JAVA_STRING.equals(token.getData())) {
 					int length = scaner.getTokenLength();
 					int offset = scaner.getTokenOffset();
 					String value = document.get(offset, length);
 					if(searchString.equals(value)){
 						if(lastResult == null || !lastResult.getFile().equals(file)){
 							lastResult = new FileResult(file);
 							result.getEntries().add(lastResult);
 						}
 						
 						lastResult.addPosition(length);
 					}
 				}
 				token = scaner.nextToken();
 			}
 		} catch (BadLocationException e) {
 			WebKbPlugin.getDefault().logError(e);
 		}
 		return false;
 	}
 	
 	private boolean scanNodeContent(IFile file, IStructuredDocumentRegion node, String regionType) {
 		boolean status = false;
 		
 		if(node == null)
 			return false;
 		
 		ITextRegionList regions = node.getRegions();
 		for(int i=0; i<regions.size(); i++) {
 			String tempSearchString = searchString;
 			int delta = 0;
 			ITextRegion region = regions.get(i);
 			if(region.getType() == regionType) {
 				String text = node.getFullText(region);
 				if(text.startsWith("\"")){
 					tempSearchString = "\""+tempSearchString;
 					delta = 1;
 				}
 				if(text.startsWith(tempSearchString)){
 					if(lastResult == null || !lastResult.getFile().equals(file)){
 						lastResult = new FileResult(file);
 						result.getEntries().add(lastResult);
 					}
 					
 					lastResult.addPosition(node.getStartOffset()+region.getStart()+delta);
 				}
 			}
 		}
 		return false;
 	}
 
 
 	
 	public class SearchResult{
 		private List<FileResult> entries;
 		
 		public SearchResult(){
 			entries = new ArrayList<FileResult>();
 		}
 		
 		public List<FileResult> getEntries(){
 			return entries;
 		}
 	}
 	
 	public class FileResult{
 		private IFile file;
 		private List<Integer> positions;
 		
 		public FileResult(IFile file){
 			this.file = file;
 			positions = new ArrayList<Integer>();
 		}
 		
 		public void addPosition(int position){
 			positions.add(new Integer(position));
 		}
 		
 		public IFile getFile(){
 			return file;
 		}
 		
 		public int[] getPositions(){
 			Integer[] integerArray = (Integer[])positions.toArray(new Integer[positions.size()]);
 			
 			int[] intArray = new int[positions.size()];
 			int index = 0;
 			for(Integer position : positions){
 				intArray[index++] = position.intValue(); 
 			}
 			return intArray;
 		}
 	}
 	
 }
