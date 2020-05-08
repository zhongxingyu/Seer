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
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.InputStream;
 
 import org.dawb.common.ui.plot.IPlottingSystem;
 import org.dawb.common.ui.plot.IPlottingSystemSelection;
 import org.dawb.common.ui.plot.PlotType;
 import org.dawb.common.ui.plot.tool.IToolPageSystem;
 import org.dawb.common.ui.slicing.ISlicablePlottingPart;
 import org.dawb.common.ui.slicing.SliceComponent;
 import org.dawb.common.ui.util.EclipseUtils;
 import org.dawb.common.util.io.FileUtils;
 import org.dawb.workbench.ui.editors.slicing.ZipUtils;
 import org.dawb.workbench.ui.views.PlotDataPage;
 import org.eclipse.core.filesystem.EFS;
 import org.eclipse.core.filesystem.IFileStore;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.editors.text.TextEditor;
 import org.eclipse.ui.ide.FileStoreEditorInput;
 import org.eclipse.ui.part.MultiPageEditorPart;
 import org.eclipse.ui.part.Page;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 
 
 public class ZipEditor extends MultiPageEditorPart implements ISlicablePlottingPart /**, IReusableEditor TODO Fix this **/, IPlottingSystemSelection {
 
 	public static final String ID = "org.dawb.workbench.editor.ZipEditor"; //$NON-NLS-1$
 
 	private static final Logger logger = LoggerFactory.getLogger(ZipEditor.class);
 
 	private CSVDataEditor dataEditor;
 
 	private PlotDataEditor dataSetEditor;
 	
 	@Override
 	public void init(IEditorSite site, IEditorInput input) throws PartInitException{
         super.init(site, input);
 	    setPartName(input.getName());
     }
 	
 	@Override
 	public void setInput(final IEditorInput input) {
 		super.setInput(input);
 		setPartName(input.getName());
 		for (int i = 0; i < getPageCount(); i++) removePage(i);
 		createPages();
 	}
 	
 	/**
 	 * It might be necessary to show the tree editor on the first page.
 	 * A property can be introduced to change the page order if this is required.
 	 */
 	@Override
 	protected void createPages() {
 		try {
 
 			// For 1D we are like an ascii editor and for 2D we are like an
 			// image editor for each image in the zip
 			if (isOneDZipFile()) {
 				this.dataSetEditor = new PlotDataEditor(true, PlotType.IMAGE);
 				addPage(0, dataSetEditor,    getUnzippedEditorInput());
 				setPageText(0, "Plot");
 
 				addPage(1, new TextEditor(), getUnzippedEditorInput());
 				setPageText(1, "Text");
 
 				this.dataEditor = new CSVDataEditor();
 				dataEditor.setDataProvider(dataSetEditor);
 				addPage(2, dataEditor,       getUnzippedEditorInput());
 				setPageText(2, "Data");
 				addPageChangedListener(dataEditor);
 				
 			} else {
 				final IEditorInput[] images = getImageEditorInput();
 				for (int i = 0; i < images.length; i++) {
 					addPage(i, new ImageEditor(), images[i]);
 					setPageText(i, "Image "+(i+1));				
 				}
 			}
  
 
 		} catch (Exception e) {
 			logger.error("Cannot initiate "+getClass().getName()+"!", e);
 		}
 	}
 	
 	public void dispose() {
 		if (dataEditor!=null) removePageChangedListener(dataEditor);
 		dataEditor = null;
 		super.dispose();
 	}
 
 	private IEditorInput getUnzippedEditorInput() throws Exception {
 		
         final InputStream in  = ZipUtils.getStreamForFile(EclipseUtils.getFile(getEditorInput()));
         final String fileName = getEditorInput().getName();
         final String zipExt   = FileUtils.getFileExtension(fileName);
         // This might not work, depending on how file is named.
         final String origExt  = FileUtils.getFileExtension(fileName.substring(0,fileName.length()-zipExt.length()-1));
        
         final File      file = File.createTempFile(fileName, "."+origExt);
         file.deleteOnExit();
         FileUtils.write(new BufferedInputStream(in), file);
        
 		final IFileStore externalFile = EFS.getLocalFileSystem().fromLocalFile(file);
 		return new FileStoreEditorInput(externalFile);
 	}
 	
 	/**
 	 * Currently does not support multiple files in zip!
 	 * @return
 	 */
 	private IEditorInput[] getImageEditorInput()  throws Exception {
 		return new IEditorInput[]{getUnzippedEditorInput()};
 	}
 
 	private boolean isOneDZipFile() {
 		
 		if (getEditorInput()==null) return false;
 		final String name = getEditorInput().getName();
 		if (name.toLowerCase().endsWith(".dat.gz"))  return true;
 		if (name.toLowerCase().endsWith(".dat.bz2")) return true;
 		if (name.toLowerCase().endsWith(".dat.zip")) return true;
 		if (name.toLowerCase().endsWith(".srs.gz"))  return true;
 		if (name.toLowerCase().endsWith(".srs.bz2")) return true;
 		if (name.toLowerCase().endsWith(".srs.zip")) return true;
 		return false;
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
 
 	@Override
 	public PlotDataComponent getDataSetComponent() {
 		return ((PlotDataEditor)getEditor(0)).getDataSetComponent();
 	}
 	@Override
 	public SliceComponent getSliceComponent() {
 		return  ((PlotDataEditor)getEditor(0)).getSliceComponent();
 	}
 	
 	@Override
 	public void setActivePage(final int ipage) {
 		super.setActivePage(ipage);
 	}
 
 	@Override
 	public IEditorPart getActiveEditor() {
 		return super.getActiveEditor();
 	}
 
 	public IPlottingSystem getPlotWindow() {
 		return ((PlotDataEditor)getEditor(0)).getPlotWindow();
 	}
 	public PlotDataEditor getDataSetEditor() {
 		return dataSetEditor;
 	}
     public Object getAdapter(@SuppressWarnings("rawtypes") final Class clazz) {
 		
 		if (clazz == Page.class) {
 			final PlotDataEditor      ed  = getDataSetEditor();
 			return new PlotDataPage(ed);
 		} else if (clazz == IToolPageSystem.class) {
			if (dataSetEditor!=null) return dataSetEditor.getPlottingSystem();
			for (int i = 0; i < getPageCount(); i++) {
				final Object ret = getEditor(i).getAdapter(clazz);
				if (ret!=null && ret instanceof IToolPageSystem) return ret;
			}
 		}
 		
 		return super.getAdapter(clazz);
 	}
     
 
 	@Override
 	public AbstractDataset setDatasetSelected(String name, boolean clearOthers) {
 		return getDataSetComponent().setDatasetSelected(name, clearOthers);
 	}
 
 	@Override
 	public void setAll1DSelected(boolean overide) {
 		getDataSetComponent().setAll1DSelected(overide);
 	}
 
 }
