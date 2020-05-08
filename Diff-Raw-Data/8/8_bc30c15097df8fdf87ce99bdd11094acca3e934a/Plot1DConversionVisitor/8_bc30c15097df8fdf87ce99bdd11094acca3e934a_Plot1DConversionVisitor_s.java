 /*
  * Copyright (c) 2012 Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package org.dawb.common.ui.wizard;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.dawb.common.services.conversion.IConversionContext;
 import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
 import org.eclipse.dawnsci.analysis.dataset.impl.AbstractDataset;
 import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
 import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
 import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
 import org.eclipse.dawnsci.plotting.api.PlotType;
 import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
 import org.eclipse.dawnsci.plotting.api.trace.ITrace;
 
 import uk.ac.diamond.scisoft.analysis.io.ASCIIDataHolderSaver;
 import uk.ac.diamond.scisoft.analysis.io.ASCIIDataWithHeadingSaver;
 import uk.ac.diamond.scisoft.analysis.io.DataHolder;
 
 /**
  * TODO FIXME This is not a UI class. Suggest move to data/algorithm plugin like org.dawnsci.persistence perhaps.
  *
  */
 public class Plot1DConversionVisitor extends AbstractPlotConversionVisitor {
 	
 	public Plot1DConversionVisitor(IPlottingSystem system) {
 		super(system);
 		if (system.getPlotType() != PlotType.XY) {
 			throw new IllegalArgumentException("Not a 1D plotting system");
 		}
 	}
 
 	@Override
 	public void visit(IConversionContext context, IDataset slice)
 			throws Exception {
 		
 		if (context.getMonitor()!=null && context.getMonitor().isCancelled()) {
 			throw new Exception(getClass().getSimpleName()+" is cancelled");
 		}
 		
 		final File outFile = new File(context.getOutputPath());
 		if (!outFile.getParentFile().exists()) outFile.getParentFile().mkdirs();
 		
 		Collection<ITrace> traces = system.getTraces(ILineTrace.class);
 		
 		//Determine if x datasets are the same
 		IDataset x = null;
 		boolean xAxisEquivalent = true;
 		
 		for (ITrace trace : traces) {
 			
 			if (trace instanceof ILineTrace) {
 				IDataset currentx = ((ILineTrace)trace).getXData();
 				
 				if (x == null) {
 					x = currentx;
 					continue;
 				}
 				
 				if (!x.equals(currentx)) {
 					xAxisEquivalent = false;
 					break;
 				}
 			}
 		}
 		
 		if (xAxisEquivalent) saveLineTracesAsAscii(context.getOutputPath());
 		else saveLineTracesXDifferent(context.getOutputPath());
 
 	}
 
 	@Override
 	public String getExtension() {
 
 		return "dat";
 	}
 
 	private void saveLineTracesAsAscii(String filename) throws Exception {
 
 		Collection<ITrace> traces = system.getTraces(ILineTrace.class);
 
 		boolean firstTrace = true;
 		List<IDataset> datasets = new ArrayList<IDataset>();
 		List<String> headings = new ArrayList<String>();
 		IDataset data;
 
 		int dtype = 0;
 
 		int i = 0;
 
 		for (ITrace trace : traces ) {
 
 			if (firstTrace) {
 				int ddtype = AbstractDataset.getDType(((ILineTrace)trace).getData());
				data = ((ILineTrace)trace).getXData();
 				int axdtype = AbstractDataset.getDType(data);
 
 				if (ddtype == axdtype) {
 					dtype = ddtype;
 				} else if (ddtype > axdtype) {
 					data = DatasetUtils.cast((Dataset)data, ddtype);
 					dtype = ddtype;
 				} else {
 					dtype = axdtype;
 				}
 
 				data.setShape(data.getShape()[0],1);
 				datasets.add(data);
 				headings.add("x");
 				firstTrace = false;
 			}
 
			data = ((ILineTrace)trace).getData();

 			if (dtype != AbstractDataset.getDType(data)) {
 				data = DatasetUtils.cast((Dataset)data, dtype);
 			}
 
 			data.setShape(data.getShape()[0],1);
 			datasets.add(data);
 			headings.add("dataset_" + i);
 			i++;
 		}
 
 		Dataset allTraces = DatasetUtils.concatenate(datasets.toArray(new IDataset[datasets.size()]), 1);
 
 		ASCIIDataWithHeadingSaver saver = new ASCIIDataWithHeadingSaver(filename);
 		DataHolder dh = new DataHolder();
 		dh.addDataset("AllTraces", allTraces);
 		saver.setHeader("#Traces extracted from Plot");
 		saver.setHeadings(headings);
 
 		saver.saveFile(dh);
 	}
 	
 	private void saveLineTracesXDifferent(String filename) throws Exception {
 		Collection<ITrace> traces = system.getTraces(ILineTrace.class);
 
 		int i = 0;
 		
 		DataHolder dh = new DataHolder();
 		
 		for (ITrace trace : traces ) {
 			
 			IDataset x = ((ILineTrace)trace).getXData();
 			IDataset y = trace.getData();
 			
 			
 			
 			dh.addDataset("Tracex_" + i, x);
 			dh.addDataset("Tracey_y" + i, y);
 			i++;
 		}
 		
 		ASCIIDataHolderSaver saver = new ASCIIDataHolderSaver(filename);
 
 		saver.saveFile(dh);
 
 	}
 
 }
