 package org.dawnsci.spectrum.ui.file;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.trace.ITrace;
 import org.dawnsci.spectrum.ui.utils.DatasetManager;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 
 public class SpectrumFile extends AbstractSpectrumFile implements ISpectrumFile {
 	
 	private String path;
 	private DatasetManager dsManager;
 	
 	
 	private static Logger logger = LoggerFactory.getLogger(SpectrumFile.class);
 	
 	private SpectrumFile(String path, DatasetManager dsmanager, IPlottingSystem system) {
 		this.path = path;
 		this.dsManager = dsmanager;
 		this.yDatasetNames = new ArrayList<String>();
 		this.system = system;
 	}
 	
 	public static SpectrumFile create(String path, IPlottingSystem system) {
 		
 		DatasetManager dsManager = DatasetManager.create(path);
 		if (dsManager == null) return null;
 		return new SpectrumFile(path, dsManager,system);
 	}
 	
 	public String getName() {
 		File file = new File(path);
 		return file.getName();
 	}
 	
 	public String getLongName() {
 		return path;
 	}
 	
 	public String getPath() {
 		return path;
 	}
 	
 	public Collection<String> getDataNames() {
 		return dsManager.getDatasetNames();
 	}
 	
 	public boolean contains(String datasetName) {
 		for (String name : dsManager.getDatasetNames()) {
 			if (datasetName.equals(name)) return true;
 		}
 		
 		return false;
 	}
 	
 	public String getxDatasetName() {
 		return xDatasetName;
 	}
 	
 	public IDataset getxDataset() {
 		return getDataset(xDatasetName);
 	}
 	
 	public IDataset getDataset(String name) {
 		try {
 			IDataset x =  LoaderFactory.getDataSet(path, name, null);
 			if (x == null) return null;
 			x.setName(name);
 			return x;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 	
 	public List<IDataset> getyDatasets() {
 		List<IDataset> sets = new ArrayList<IDataset>();
 		
 		for (String name : yDatasetNames) {
 			try {
 				IDataset set = LoaderFactory.getDataSet(path, name, null);
 				if (set != null) {
 					set.setName(name);
 					sets.add(set);
 				}
 			} catch (Exception e) {
 				logger.error(e.getMessage());
 			}
 		}
 		
 		return sets;
 	}
 	
 	public void plotAll() {
 		
 		Job job = new Job("Plot all") {
 			
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				
 				IDataset x = null;
 				
 				if (useAxisDataset) x = getxDataset();
 				
 				List<IDataset> list = getyDatasets();
 				for (IDataset ds : list) {
 					
 					if (ds.getRank() != 1) {
 						ds = reduceTo1D(x, ds);
 					}
 					ds.setName(getTraceName(ds.getName()));
 				}
 				
 				List<ITrace> traces = system.updatePlot1D(x, list, null);
 				
 				for (int i = 0; i < traces.size();i++) {
 					traceMap.put(yDatasetNames.get(i), traces.get(i));
 				}
 				
 				return Status.OK_STATUS;
 			}
 		};
 		job.setRule(mutex);
 		job.schedule();
 	}
 	
 	protected void addToPlot(final String name) {
 		
 		if (traceMap.containsKey(name)) return;
 		
 		Job job = new Job("Add to plot") {
 			
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				IDataset set = null;
 				
 				IDataset x = null;
 				if (useAxisDataset) x = getxDataset();
 				
 				try {
 					set = LoaderFactory.getDataSet(path, name, null);
 					if (set != null) {
 						if (set.getRank() != 1) set = reduceTo1D(x, set);
 						set.setName(getTraceName(name));
 					}
 				} catch (Exception e) {
 					logger.error(e.getMessage());
 					return Status.CANCEL_STATUS;
 				}
 				
 				
 				
 				if (set != null) {
 					List<ITrace> traces = system.updatePlot1D(x, Arrays.asList(new IDataset[] {set}), null);
 					traceMap.put(name, traces.get(0));
 				}
 				return Status.OK_STATUS;
 			}
 		};
 		job.setRule(mutex);
 		job.schedule();
 		
 	}
 
 	@Override
 	public List<String> getMatchingDatasets(int size) {
 		return dsManager.getAllowedDatasets(size);
 	}
 
 	@Override
 	public List<String> getPossibleAxisNames() {
 		return dsManager.getPossibleAxisDatasets();
 	}
 
 	@Override
 	protected String getTraceName(String name) {
 		File file = new File(path);
 		return file.getName() + " : " + name;
 	}
 
 	@Override
 	public boolean canBeSaved() {
 		return false;
 	}
 	
 	
 }
 
