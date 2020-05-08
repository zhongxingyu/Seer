 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package org.dawb.workbench.ui.editors;
 
 import java.util.Collection;
 
 import org.dawb.common.ui.editors.EditorExtensionFactory;
 import org.dawb.common.ui.plot.tool.IToolPageSystem;
 import org.dawb.common.ui.util.EclipseUtils;
 import org.dawb.common.ui.views.HeaderTablePage;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IReusableEditor;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.MultiPageEditorPart;
 import org.eclipse.ui.part.Page;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 public class ImageEditor extends MultiPageEditorPart implements IReusableEditor  {
 
 	public static final String ID = "org.dawb.workbench.editors.ImageEditor"; //$NON-NLS-1$
 
 	private static final Logger logger = LoggerFactory.getLogger(ImageEditor.class);
 
 	private PlotImageEditor plotImageEditor;
 
 
 	
 	@Override
 	public void init(IEditorSite site, IEditorInput input) throws PartInitException{
         super.init(site, input);
 	    setPartName(input.getName());
     }
 	
 	public void setInput(IEditorInput input) {
 		super.setInput(input);
 		if (plotImageEditor!=null) plotImageEditor.setInput(input);
		try{ 
		    setPartName(input.getName());
		} catch (Exception ignored) {
			// Input maybe invalid but we do not treat this as a failure if the above methods already worked.
		}
 	}
 	/**
 	 * It might be necessary to show the tree editor on the first page.
 	 * A property can be introduced to change the page order if this is required.
 	 */
 	@Override
 	protected void createPages() {
 		try {
 			
 			int index = 0;
 			
 			try {
 				Collection<IEditorPart> extensions = EditorExtensionFactory.getEditors(this);
 				if (extensions!=null && extensions.size()>0) {
 					for (IEditorPart iEditorPart : extensions) {
 						addPage(index, iEditorPart,  getEditorInput());
 						setPageText(index, iEditorPart.getTitle());
 						index++;
 					}
 				}
 			} catch (Exception e) {
 				logger.error("Cannot read editor extensions!", e);
 			}
 
 			this.plotImageEditor = new PlotImageEditor();
 			addPage(index, plotImageEditor,       getEditorInput());
 			setPageText(index, "Image");
 			index++;
 			
 			if (System.getProperty("org.dawb.editor.ascii.hide.diamond.image.editor")==null) {
 				final uk.ac.diamond.scisoft.analysis.rcp.editors.ImageEditor im = new uk.ac.diamond.scisoft.analysis.rcp.editors.ImageEditor();
 				addPage(index, im,       getEditorInput());
 				setPageText(index, "Info");
 			}
 
 			final int infoIndex = index;
 			getSite().getShell().getDisplay().asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					if (EclipseUtils.getPage().findView("uk.ac.diamond.scisoft.analysis.rcp.views.DatasetInspectorView")!=null &&
 							getPageCount()>=2) {
 						setActivePage(infoIndex);
 					}
 
 				}
 			});
 
 		} catch (PartInitException e) {
 			logger.error("Cannot initiate "+getClass().getName()+"!", e);
 		}
 	}
 
 	/** 
 	 * No Save
 	 */
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 		if (getActiveEditor().isDirty()) {
 			getActiveEditor().doSave(monitor);
 		}
 	}
 
 	/** 
 	 * No Save
 	 */
 	@Override
 	public void doSaveAs() {
 		if (getActiveEditor().isDirty()) {
 			getActiveEditor().doSaveAs();
 		}
 	}
 
 	/** 
 	 * We are not saving this class
 	 */
 	@Override
 	public boolean isSaveAsAllowed() {
 		return false;
 	}
 
     public Object getAdapter(final Class clazz) {
 		
     	// TODO FIXME for IContentProvider return a Page which shows the value
     	// of plotted data. Bascially the same as the CSVPage.
     	
 		if (clazz == Page.class) {
 			return new HeaderTablePage(EclipseUtils.getFilePath(getEditorInput()));
 		} else if (clazz == IToolPageSystem.class) {
 			return plotImageEditor.getPlottingSystem();
 		}
 		
 		return super.getAdapter(clazz);
 	}
 
 	public String toString(){
 		if (getEditorInput()!=null) return getEditorInput().getName();
 		return super.toString();
 	}
 
 	public PlotImageEditor getPlotImageEditor() {
 		return plotImageEditor;
 	}
 }
