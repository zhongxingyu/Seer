 package org.dawnsci.spectrum.ui.file;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.trace.ITrace;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 
 
 public class SpectrumInMemory extends AbstractSpectrumFile implements ISpectrumFile {
 	
 	Map<String,IDataset> datasets;
 	private String name;
 	private String longName;
 	
 	public SpectrumInMemory(String longName, String name,IDataset xDataset ,Collection<IDataset> yDatasets, IPlottingSystem system) {
 		this.system = system;
 		this.name = name;
 		this.longName = longName;
 		datasets = new HashMap<String, IDataset>();
 		yDatasetNames = new ArrayList<String>(yDatasets.size());
 		useAxisDataset = false;
 		
 		if (xDataset != null) {
 			useAxisDataset = true;
 			String dsName = xDataset.getName();
 			if (dsName == null) dsName = "xData";
 			datasets.put(dsName, xDataset);
 			xDatasetName = dsName;
 		}
 		
 		//TODO make more robust to the same dataset names
 		int i = 0;
 		for (IDataset dataset : yDatasets) {
 			String dsName = dataset.getName();
 			if (dsName == null) dsName = "yData" + i;
 			datasets.put(dsName, dataset);
 			yDatasetNames.add(dsName);
 		}
 		
 	}
 
 	@Override
 	public String getName() {
 		// TODO Auto-generated method stub
 		return name;
 	}
 	
 	@Override
 	public Collection<String> getDataNames() {
 		
 		return getDatasetNames();
 	}
 	
 	@Override
 	public IDataset getDataset(String name) {
 		return datasets.get(name);
 	}
 
 	@Override
 	public IDataset getxDataset() {
 		return datasets.get(xDatasetName);
 	}
 
 	@Override
 	public List<IDataset> getyDatasets() {
 		List<IDataset> sets = new ArrayList<IDataset>();
 		for (String name : yDatasetNames) {
 			sets.add(datasets.get(name));
 		}
 		return sets;
 	}
 
 	
 
 	@Override
 	public String getxDatasetName() {
 		// TODO Auto-generated method stub
 		return xDatasetName;
 	}
 
 	@Override
 	public boolean contains(String datasetName) {
 		for (String name : datasets.keySet()) {
 			if (datasetName.equals(name)) return true;
 		}
 		
 		return false;
 	}
 	
 	public void plotAll() {
 		//not slow, doesnt need to be a job but the mutex keeps the plotting order
		if (!showPlot) return;
 		Job job = new Job("Plot all") {
 
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 
 				IDataset x = null;
 				if (useAxisDataset) x = getxDataset();
 
 				List<IDataset> list = getyDatasets();
 				List<IDataset> copy = new ArrayList<IDataset>(list.size());
 				List<String> names = new ArrayList<String>(list.size());
 
 				for (IDataset ds : list) copy.add(ds);
 
 				for (int i= 0; i < copy.size(); i++) {
 					names.add( copy.get(i).getName());
 					if (copy.get(i).getRank() != 1) {
 						copy.set(i,reduceTo1D(x, copy.get(i)));
 					}
 					copy.get(i).setName(getTraceName(copy.get(i).getName()));
 				}
 
 				List<ITrace> traces = system.updatePlot1D(x, getyDatasets(), null);
 
 				for (int i = 0; i < traces.size();i++) {
 					traceMap.put(yDatasetNames.get(i), traces.get(i));
 				}
 				for (int i= 0; i < copy.size(); i++) {
 					list.get(i).setName(names.get(i));
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
 				IDataset x = null;
 				if (useAxisDataset) x = getxDataset();
 				IDataset set = datasets.get(name);
 				String oldName = set.getName();
 				if (set.getRank() != 1) set = reduceTo1D(x, set);
 				set.setName(getTraceName(set.getName()));
 
 				if (set != null) {
 					List<ITrace> traces = system.updatePlot1D(x, Arrays.asList(new IDataset[] {set}), null);
 					traceMap.put(name, traces.get(0));
 				}
 
 				set.setName(oldName);
 				return Status.OK_STATUS;
 			}
 		};
 		job.setRule(mutex);
 		job.schedule();
 
 	}
 	
 	@Override
 	public String getLongName() {
 		return longName;
 	}
 
 	@Override
 	public List<String> getPossibleAxisNames() {
 		
 		return getDatasetNames();
 	}
 
 	@Override
 	public List<String> getMatchingDatasets(int size) {
 		// TODO Auto-generated method stub
 		return getDatasetNames();
 	}
 	
 	private List<String> getDatasetNames() {
 		List<String> col = new ArrayList<String>(datasets.size());
 		
 		for (String key : datasets.keySet()) {
 			col.add(key);
 		}
 		
 		return col;
 	}
 
 	@Override
 	protected String getTraceName(String name) {
 		// TODO Auto-generated method stub
 		return this.name + " : " + name;
 	}
 
 	@Override
 	public boolean canBeSaved() {
 		return true;
 	}
 }
