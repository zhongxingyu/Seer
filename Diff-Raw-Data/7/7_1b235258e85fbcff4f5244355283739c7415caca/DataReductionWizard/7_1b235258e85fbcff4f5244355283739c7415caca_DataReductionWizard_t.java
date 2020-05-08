 package org.dawnsci.plotting.tools.reduction;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 
 import org.dawb.common.services.ServiceManager;
 import org.dawb.common.services.conversion.IConversionContext;
 import org.dawb.common.services.conversion.IConversionService;
 import org.dawb.common.services.conversion.IConversionVisitor;
 import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
 import org.dawb.common.ui.plot.tools.IDataReductionToolPage;
 import org.dawb.common.ui.slicing.DimsData;
 import org.dawb.common.ui.slicing.DimsDataList;
 import org.dawb.common.ui.util.EclipseUtils;
 import org.dawb.common.util.io.FileUtils;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IExportWizard;
 import org.eclipse.ui.IWorkbench;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 
 public class DataReductionWizard extends Wizard implements IExportWizard {
 
 	public static final String ID = "org.dawb.workbench.plotting.dataReductionExportWizard";
 	
 	private static final Logger logger = LoggerFactory.getLogger(DataReductionWizard.class);
 	
 	
 	private DataReductionWizardPage       sliceConfigurePage;
 	private IConversionService            service;
 	private IConversionContext            context;
 	private IConversionVisitor            visitor;
 
 	private DimsDataList           dimsList;
 	
 	public DataReductionWizard() {
 		super();
 		setWindowTitle("Export Reduced Data");
 		
 		// It's an OSGI service, not required to use ServiceManager
 		try {
 			this.service = (IConversionService)ServiceManager.getService(IConversionService.class);
 		} catch (Exception e) {
 			logger.error("Cannot get conversion service!", e);
 			return;
 		}
 
 	}
 	
 	public void addPages() {
 		
 		sliceConfigurePage = new DataReductionWizardPage("Data Reduction", null, null);
 		sliceConfigurePage.setDescription("This wizard runs '"+visitor.getConversionSchemeName()+"' over a stack of data. Please check the data to slice, "+
                 "confirm the export file and then press 'Finish' to run '"+visitor.getConversionSchemeName()+"' on each slice.");
 
 		addPage(sliceConfigurePage);
 		
 	}
 	
     public boolean canFinish() {
     	 
     	if (!sliceConfigurePage.isContextSet()) {
 			sliceConfigurePage.setDefaltSliceDims(dimsList);
 			sliceConfigurePage.setContext(context);
     	}
     	if (!sliceConfigurePage.isPageValid()) return false;
 		return super.canFinish();
     }
 
 
 	@Override
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 	}
 	
 	@Override
 	public boolean performFinish() {
 		try {
 			getContainer().run(true, true, new IRunnableWithProgress() {
 				@Override
 				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {				 
 				    
 					try {	
 						((ToolConversionVisitor)visitor).setNexusAxes(sliceConfigurePage.getNexusAxes());
 						
						try { // Delete if it exists because some tools can accidentally append.
							final File output = new File(context.getOutputPath());
							if (output.exists()) FileUtils.recursiveDelete(output);
						} catch (Exception ne) {
							logger.error("Cannot delete "+context.getOutputPath(), ne);
						}
						
 						IConversionContext context = sliceConfigurePage.getContext();
 						context.setMonitor(new ProgressMonitorWrapper(monitor));
 						
 						// Bit with the juice
 						monitor.beginTask(visitor.getConversionSchemeName(), context.getWorkSize());
 						monitor.worked(1);
 						service.process(context);
 						
 						EclipseUtils.refreshAndOpen(context.getOutputPath(), true, monitor);
 
 					} catch (final Exception ne) {
 
 						logger.error("Cannot run export process for data reduction from tool '"+visitor.getConversionSchemeName()+"'", ne);
 						Display.getDefault().syncExec(new Runnable() {
 							public void run() {
 								ErrorDialog.openError(Display.getDefault().getActiveShell(),
 										"Data Not Exported", 
 										"Cannot run export process for data reduction from tool "+visitor.getConversionSchemeName()+".\n\nPlease contact your support representative.",
 										new Status(IStatus.WARNING, "org.edna.workbench.actions", ne.getMessage(), ne));
 							}
 						});
 	
 					} 
 
 				}
 			});
 		} catch (Exception ne) {
 			logger.error("Cannot run export process for data reduction from tool "+visitor.getConversionSchemeName(), ne);
 		}
 
 		return true;
 	}				 
 
 	public boolean needsProgressMonitor() {
 		return true;
 	}
 
 	public void setData(IFile file, String h5Path, IDataReductionToolPage tool) {
 		
 		this.context  = service.open(file.getLocation().toOSString());
 		this.visitor  = new ToolConversionVisitor(tool);;
 
 		final String sugFileName = FileUtils.getFileNameNoExtension(file.getName()).replace(' ',  '_')+"_"+tool.getTitle().replace(' ', '_')+".h5";
 		context.setOutputPath(file.getParent().getLocation().toOSString()+File.separator+sugFileName);
 		context.setConversionVisitor(visitor);
 		context.setDatasetName(h5Path);
 		
 	}
 	
 	public void setSlice(final ILazyDataset lazy, final DimsDataList dList) {
 		this.dimsList = dList.clone();
 		for (DimsData dd : dimsList.getDimsData()) {
 			if (dd.isSlice()) {
 				context.addSliceDimension(dd.getDimension(), String.valueOf(dd.getSlice()));
 				if (dd.getSlice()>0) {
 				    dd.setSliceRange(dd.getSlice()+":"+(lazy.getShape()[dd.getDimension()]-1));
 				} else {
 					dd.setSliceRange("all");
 				}
 			    break; // Only one range allowed.
 			} 
 		}
 	}
 
 }
