 /*
  * Copyright (c) 2012 Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package org.dawb.common.ui.wizard.persistence;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 import org.dawb.common.services.IPersistenceService;
 import org.dawb.common.services.IPersistentFile;
 import org.dawb.common.services.ServiceManager;
 import org.dawb.common.ui.Activator;
 import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
 import org.dawb.common.ui.util.EclipseUtils;
 import org.dawb.common.ui.wizard.CheckWizardPage;
 import org.dawb.common.ui.wizard.ResourceChoosePage;
 import org.dawb.common.util.io.FileUtils;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
 import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunctionService;
 import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
 import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
 import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
 import org.eclipse.dawnsci.analysis.api.metadata.IMetadata;
 import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
 import org.eclipse.dawnsci.analysis.api.roi.IROI;
 import org.eclipse.dawnsci.analysis.dataset.impl.BooleanDataset;
 import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
 import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
 import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
 import org.eclipse.dawnsci.plotting.api.region.IRegion;
 import org.eclipse.dawnsci.plotting.api.region.IRegionService;
 import org.eclipse.dawnsci.plotting.api.tool.IToolPage;
 import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
 import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
 import org.eclipse.dawnsci.plotting.api.trace.ITrace;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IImportWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PlatformUI;
 
 import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionMetadataUtils;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 
 /**
  * 
  * @author Matthew Gerring
  *
  */
 public class PersistenceImportWizard extends AbstractPersistenceWizard implements IImportWizard {
 
 	public static final String ID = "org.dawnsci.plotting.importMask";
 	private ResourceChoosePage fcp;
 	private CheckWizardPage options;
 
 	public PersistenceImportWizard() {
 		
 		setWindowTitle("Import");
 		
 		this.fcp = new ResourceChoosePage("Import File", null, null);
 		fcp.setDescription("Choose the file (*.nxs or *.msk) to import.");
 		fcp.setOverwriteVisible(false);
 		addPage(fcp);
 		
 		this.options = new CheckWizardPage("Import Options", createDefaultOptions());
 		options.setStringValues("Mask", Arrays.asList(""));
 		options.setDescription("Please choose things to import.");
 		addPage(options);
 		
 	}
 	
 	private static String lastStaticPath;
 	
 	public void createPageControls(Composite pageContainer) {
 		super.createPageControls(pageContainer);
 		
 		if (lastStaticPath==null) {
 			try {
 			    final IFile file = EclipseUtils.getSelectedFile();
 			    if (file!=null) {
 			    	lastStaticPath = file.getLocation().toOSString();
 			    }
 			} catch (Throwable ne) {
 				// Nowt
 			}
 		}
 
 		if (lastStaticPath!=null) {
 			fcp.setPath(lastStaticPath);
 		}
 	}
 	
 	public boolean canFinish() {
 		COMPLETE_TEST: if (fcp.isPageComplete()) {
 			final String absolutePath = fcp.getAbsoluteFilePath();
 			if (absolutePath==null) break COMPLETE_TEST;
 			options.setOptionEnabled(PersistWizardConstants.ORIGINAL_DATA, false);
 			options.setOptionEnabled(PersistWizardConstants.MASK,          false);
 			options.setOptionEnabled(PersistWizardConstants.REGIONS,       false);
 			options.setOptionEnabled(PersistWizardConstants.FUNCTIONS,       false);
 			options.setOptionEnabled(PersistWizardConstants.DIFF_META,       false);
 			final File   file         = new File(absolutePath);
 			if (file.exists())  {
 				final String ext = FileUtils.getFileExtension(file);
 				if (ext!=null) {
 					if ("msk".equals(ext.toLowerCase())){
 						options.setStringValue(PersistWizardConstants.MASK, null);
 						options.setOptionEnabled(PersistWizardConstants.MASK,true);
 
 					} else if ("nxs".equals(ext.toLowerCase())) {
 
 						IPersistentFile     pf=null;
 
 						try {
 							IPersistenceService service = (IPersistenceService)ServiceManager.getService(IPersistenceService.class);
 							pf    = service.getPersistentFile(file.getAbsolutePath());
 
 							if (pf.containsMask()) {
 								final List<String>  names = pf.getMaskNames(null);
 								if (names!=null && !names.isEmpty()) {
 									options.setOptionEnabled(PersistWizardConstants.MASK, true);
 									options.setStringValues(PersistWizardConstants.MASK, names);
 								}
 							} else {
 								options.setStringValue(PersistWizardConstants.MASK, null);
 							}
 
 							if (pf.containsRegion()) {
 								final List<String>  regions = pf.getROINames(null);
 								if (regions!=null && !regions.isEmpty()) {
 									options.setOptionEnabled(PersistWizardConstants.REGIONS, true);
 								}
 							}
 
 							if (pf.containsFunction()) {
 								final List<String>  functions = pf.getFunctionNames(null);
 								if (functions!=null && !functions.isEmpty()) {
 									options.setOptionEnabled(PersistWizardConstants.FUNCTIONS, true);
 								}
 							}
 
 							if (pf.containsDiffractionMetadata()) {
 								options.setOptionEnabled(PersistWizardConstants.DIFF_META, true);
 							}
 
 						} catch (Throwable ne) {
 							logger.error("Cannot read persistence file at "+file);
 						} finally {
 							if (pf!=null) pf.close();
 						}
 
 					}
 				}
 
 
 			}
 		}
 	return super.canFinish();
 	}
 
 	@Override
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public boolean performFinish() {
 		 String absolutePath = null;
 		 try {
 			 absolutePath   = fcp.getAbsoluteFilePath();
 			 			 
 			 final IWorkbenchPart  part   = EclipseUtils.getPage().getActivePart();
 			 final IPlottingSystem system = (IPlottingSystem)part.getAdapter(IPlottingSystem.class);
 			 final IFunctionService funcService = (IFunctionService)part.getAdapter(IFunctionService.class);
 
 			 final String finalPath = absolutePath;
 			 getContainer().run(true, true, new IRunnableWithProgress() {
 
 				 @Override
 				 public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 					 
 					 try {
 						 if (finalPath.toLowerCase().endsWith(".msk")) {
 							 createFit2DMask(finalPath, system, monitor);
 						 } else {
 							 createDawnMask(finalPath, system, monitor, funcService);
 						 }
 						 
 					 } catch (Exception e) {
 						 throw new InvocationTargetException(e);
 					 }
 
 				 }
 			 });
 		 } catch (Throwable ne) {
 			 if (ne instanceof InvocationTargetException && ((InvocationTargetException)ne).getCause()!=null){
 				 ne = ((InvocationTargetException)ne).getCause();
 			 }
 			 String message = null;
 			 if (absolutePath!=null) {
 				 message = "Cannot import from '"+absolutePath+"' ";
 			 } else {
 				 message = "Cannot import file.";
 			 }
			 logger.error("Cannot export mask file!", ne);
		     ErrorDialog.openError(Display.getDefault().getActiveShell(), "Export failure", message, new Status(IStatus.WARNING, "org.dawb.common.ui", ne.getMessage(), ne));
 		     return true;
 		 }
 		 
 		 lastStaticPath = absolutePath;
 		 
 		 return true;
 	}
 
 	protected void createFit2DMask(String filePath, IPlottingSystem system, IProgressMonitor monitor) throws Exception {
 		
 		final IDataHolder     holder = LoaderFactory.getData(filePath, new ProgressMonitorWrapper(monitor));
 		final Dataset mask   = DatasetUtils.cast(holder.getDataset(0), Dataset.BOOL);
 		final ITrace          trace  = system.getTraces().iterator().next();
 		
 		if (mask!=null && trace!=null && trace instanceof IImageTrace) {
 			Display.getDefault().syncExec(new Runnable() {
 				public void run() {
 					IImageTrace image = (IImageTrace)trace;
 					image.setMask(mask);
 				}
 			});
 		}
 	}
 
 	protected void createDawnMask(final String filePath, final IPlottingSystem system, final IProgressMonitor monitor, final IFunctionService funcService) throws Exception{
 		 
 		IPersistentFile file = null;
 		try {
 			IPersistenceService service = (IPersistenceService)ServiceManager.getService(IPersistenceService.class);
 			file    = service.getPersistentFile(filePath);
 
 			final IMonitor mon = new ProgressMonitorWrapper(monitor);
 
 			// Save things.
 			ITrace trace  = system.getTraces().iterator().next();
 			if (options.is(PersistWizardConstants.ORIGINAL_DATA)) {
 				// Not needed can open file directly
 			}
 
 			if (options.is(PersistWizardConstants.MASK) && trace instanceof IImageTrace) {
 				final IImageTrace image = (IImageTrace)trace;
 				String name = options.getString("Mask"); //TODO drop down of available masks.
 				if (name == null) name = file.getMaskNames(null).get(0);
 				final BooleanDataset mask = (BooleanDataset)file.getMask(name, mon);
 				if (mask!=null)  {
 					Display.getDefault().syncExec(new Runnable() {
 						public void run() {
 							// we set the maskDataset on the masking tool
 							final IToolPageSystem tsystem = (IToolPageSystem)system.getAdapter(IToolPageSystem.class);
 							final IToolPage       tool    = tsystem.getActiveTool();
 							if (tool != null && tool.getToolId().equals("org.dawb.workbench.plotting.tools.maskingTool")) {
 								tool.setToolData(mask);
 							}
 							// we set the mask on the image trace
 							image.setMask(mask);
 						}
 					});
 				}
 			}
 
 			final IPersistentFile finalFile = file;
 			if (options.is(PersistWizardConstants.REGIONS)) {
 				final Map<String, IROI> rois = file.getROIs(mon);
 				if (rois!=null && !rois.isEmpty()) {
 					
 					final IRegionService rservice = (IRegionService)Activator.getService(IRegionService.class);
 					for (final String roiName : rois.keySet()) {
 						final IROI roi = rois.get(roiName);
 						Display.getDefault().syncExec(new Runnable() {
 							public void run() {
 								try {
 									IRegion region = null;
 									if (system.getRegion(roiName)!=null) {
 										region = system.getRegion(roiName);
 										region.setROI(roi);
 									} else {
 										region = system.createRegion(roiName, rservice.forROI(roi));
 										region.setROI(roi);
 										system.addRegion(region);
 									}
 									if (region!=null) {
 										String uObject = finalFile.getRegionAttribute(roiName, "User Object");
 										if (uObject!=null) region.setUserObject(uObject); // Makes a string out of
 										// it but gives a clue.
 									}
 								} catch (Throwable e) {
 									logger.error("Cannot create/import region "+roiName, e);
 								}
 							}
 						});
 					}
 				}
 			}
 			
 			if (options.is(PersistWizardConstants.DIFF_META) && trace instanceof IImageTrace) {
 				//check loader service and overwrite if not null
 				//check image and overwrite if none in service
 				ILoaderService loaderService = (ILoaderService)PlatformUI.getWorkbench().getService(ILoaderService.class);
 
 				final IDiffractionMetadata fileMeta = file.getDiffractionMetadata(mon);
 
 				final IDiffractionMetadata lockedmeta = loaderService.getLockedDiffractionMetaData();
 				final IImageTrace image = (IImageTrace)trace;
 				Display.getDefault().syncExec(new Runnable() {
 					public void run() {
 						if (lockedmeta != null) {
 							DiffractionMetadataUtils.copyNewOverOld(fileMeta, lockedmeta);
 						} else if (image.getData() != null && ((Dataset)image.getData()).getMetadata()!= null){
 							//Should only need to copy over here, not replace
 							IMetadata meta = ((Dataset)image.getData()).getMetadata();
 							if (meta instanceof IDiffractionMetadata) {
 								DiffractionMetadataUtils.copyNewOverOld(fileMeta, (IDiffractionMetadata)meta);
 							}
 						}
 					}
 				});
 			}
 			
 			if (options.is(PersistWizardConstants.FUNCTIONS)) {
 				if (funcService != null) {
 					final Map<String, IFunction> functions = file.getFunctions(mon);
 					if (functions != null) {
 						Display.getDefault().syncExec(new Runnable() {
 							public void run() {
 								funcService.setFunctions(functions);
 							}
 						});
 					}
 				}
 			}
 
 		} finally {
 			if (file!=null) file.close();
 		}
 		
 	}
 
 }
