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
 
 import java.util.Properties;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.TextSelection;
 import org.eclipse.jface.text.contentassist.IContextInformation;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.texteditor.DocumentProviderRegistry;
 import org.eclipse.ui.texteditor.IDocumentProvider;
 import org.jboss.tools.common.model.ui.ModelUIImages;
 import org.jboss.tools.common.model.ui.views.palette.PaletteInsertHelper;
 import org.jboss.tools.common.quickfix.IBaseMarkerResolution;
 import org.jboss.tools.jst.jsp.jspeditor.dnd.JSPPaletteInsertHelper;
 import org.jboss.tools.jst.jsp.jspeditor.dnd.PaletteTaglibInserter;
 import org.jboss.tools.jst.web.ui.Messages;
 import org.jboss.tools.jst.web.ui.WebUiPlugin;
 
 /**
  * The Marker Resolution that adds tag lib declaration to jsp or xhtml file
  * 
  * @author Daniel Azarov
  *
  */
 public class AddTLDMarkerResolution implements IBaseMarkerResolution, IJavaCompletionProposal{
 	private IFile file;
 	
 	private String resolutionName;
 	private int start, end;
 	private String uri, prefix;
 	
 	public AddTLDMarkerResolution(IFile file, String name, int start, int end, String uri, String prefix){
 		this.file = file;
 		this.resolutionName = name;
 		this.start = start;
 		this.end = end;
 		this.uri = uri;
 		this.prefix = prefix;
 	}
 	
 	public AddTLDMarkerResolution(String name, int start, int end, String uri, String prefix){
 		this.resolutionName = name;
 		this.start = start;
 		this.end = end;
 		this.uri = uri;
 		this.prefix = prefix;
 	}
 	
 	private Properties getProperties(){
 		Properties properties = new Properties();
 		properties.put(JSPPaletteInsertHelper.PROPOPERTY_ADD_TAGLIB, "true"); //$NON-NLS-1$
 		properties.put(PaletteInsertHelper.PROPOPERTY_START_TEXT, ""); //$NON-NLS-1$
 		properties.put(JSPPaletteInsertHelper.PROPOPERTY_TAGLIBRARY_URI, uri);
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
 		return properties;
 	}
 
 	@Override
 	public String getLabel() {
 		return NLS.bind(Messages.AddTLDMarkerResolution_Name, resolutionName);
 	}
 
 	@Override
 	public void run(IMarker marker) {
 		FileEditorInput input = new FileEditorInput(file);
 		IDocumentProvider provider = DocumentProviderRegistry.getDefault().getDocumentProvider(input);
 		try {
 			provider.connect(input);
 			
 			boolean dirty = provider.canSaveDocument(input);
 		
 			IDocument document = provider.getDocument(input);
 			
 			PaletteTaglibInserter inserter = new PaletteTaglibInserter();
 			inserter.inserTaglib(document, getProperties());
 			
 			if(!dirty){
 				provider.aboutToChange(input);
 				provider.saveDocument(new NullProgressMonitor(), input, document, true);
 				provider.changed(input);
 			}
 			
 			provider.disconnect(input);
 		}catch(CoreException ex){
 			WebUiPlugin.getPluginLog().logError(ex);
 		}
 	}
 
 	@Override
 	public String getDescription() {
 		return getLabel();
 	}
 
 	@Override
 	public Image getImage() {
		return ModelUIImages.getImageDescriptor(ModelUIImages.TAGLIB_FILE).createImage();
 	}
 
 	@Override
 	public void apply(IDocument document) {
 		
 		Properties properties = getProperties();
 		
 		PaletteTaglibInserter.getPrefixes(document, properties);
 		
 		PaletteTaglibInserter inserter = new PaletteTaglibInserter();
 		inserter.inserTaglib(document, properties);
 	}
 
 	@Override
 	public Point getSelection(IDocument document) {
 		return null;
 	}
 
 	@Override
 	public String getAdditionalProposalInfo() {
 		return getDescription();
 	}
 
 	@Override
 	public String getDisplayString() {
 		return getLabel();
 	}
 
 	@Override
 	public IContextInformation getContextInformation() {
 		return null;
 	}
 
 	@Override
 	public int getRelevance() {
 		return 0;
 	}
 }
