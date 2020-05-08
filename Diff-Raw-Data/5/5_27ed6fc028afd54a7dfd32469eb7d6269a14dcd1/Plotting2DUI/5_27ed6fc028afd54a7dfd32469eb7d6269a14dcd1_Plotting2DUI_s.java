 /*-
  * Copyright 2012 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.rcp.plotting;
 
 import gda.observable.IObserver;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.region.IRegion;
 import org.dawb.common.ui.plot.trace.IImageTrace;
 import org.dawb.common.ui.plot.trace.ITrace;
 import org.eclipse.swt.widgets.Display;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.plotserver.AxisMapBean;
 import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
 import uk.ac.diamond.scisoft.analysis.plotserver.DataSetWithAxisInformation;
 import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
 import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
 import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
 import uk.ac.diamond.scisoft.analysis.roi.LinearROIList;
 import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROIList;
 import uk.ac.diamond.scisoft.analysis.roi.SectorROI;
 import uk.ac.diamond.scisoft.analysis.roi.SectorROIList;
 
 /**
  * Class to create the a 2D/image plotting
  */
 public class Plotting2DUI extends AbstractPlotUI {
 
 	public final static String STATUSITEMID = "uk.ac.diamond.scisoft.analysis.rcp.plotting.Plotting2DUI";
 
 	private AbstractPlottingSystem plottingSystem;
 	private List<IObserver> observers = Collections.synchronizedList(new LinkedList<IObserver>());
 	private PlotWindow plotWindow;
 	private static final Logger logger = LoggerFactory.getLogger(Plotting2DUI.class);
 
 	/**
 	 * @param plotter
 	 */
 	public Plotting2DUI(PlotWindow window, final AbstractPlottingSystem plotter) {
 		this.plotWindow = window;
 		this.plottingSystem = plotter;
 	}
 
 	@Override
 	public void processPlotUpdate(final DataBean dbPlot, boolean isUpdate) {
 		Display.getDefault().syncExec(new Runnable() {
 			@Override
 			public void run() {
 				Collection<DataSetWithAxisInformation> plotData = dbPlot.getData();
 				if (plotData != null) {
 					Iterator<DataSetWithAxisInformation> iter = plotData.iterator();
 					final List<AbstractDataset> yDatasets = Collections
 							.synchronizedList(new LinkedList<AbstractDataset>());
 
 					final AbstractDataset xAxisValues = dbPlot.getAxis(AxisMapBean.XAXIS);
 					final AbstractDataset yAxisValues = dbPlot.getAxis(AxisMapBean.YAXIS);
 					final List<AbstractDataset> axes = Collections.synchronizedList(new LinkedList<AbstractDataset>());
 					
 					String xAxisName = "", yAxisName = "";
 					if(xAxisValues!=null){
 						axes.add(0, xAxisValues);
 						xAxisName = xAxisValues.getName();
 					}
 					if(yAxisValues!=null){
 						axes.add(1, yAxisValues);
 						yAxisName = yAxisValues.getName();
 					}
 
 					while (iter.hasNext()) {
 						DataSetWithAxisInformation dataSetAxis = iter.next();
 						AbstractDataset data = dataSetAxis.getData();
 						yDatasets.add(data);
 					}
 
 					AbstractDataset data = yDatasets.get(0);
 					if (data != null) {
 					
 						final Collection<ITrace> traces = plottingSystem.getTraces();
 						final List<ITrace> traceList =new ArrayList<ITrace>(traces);
 						if (traces != null && traces.size() > 0 
 								&& traceList.size()>0
 								&& traceList.get(0) instanceof IImageTrace) {
 							final IImageTrace image = (IImageTrace) traces.iterator().next();
 							final int[] shape = image.getData() != null ? image.getData().getShape() : null;
 							
 							List<AbstractDataset> currentAxes = image.getAxes(); 
 							String lastXAxisName = "", lastYAxisName = "";
							if(currentAxes!=null)
 								lastXAxisName = currentAxes.get(0).getName();
							if(currentAxes!=null)
 								lastYAxisName = currentAxes.get(1).getName();
 							
 							if (shape != null && Arrays.equals(shape, data.getShape())
 									&& lastXAxisName.equals(xAxisName)
 									&& lastYAxisName.equals(yAxisName)) {
 								if(axes.size()>0)
 									plottingSystem.updatePlot2D(data, axes, null);
 								else
 									plottingSystem.updatePlot2D(data, null, null);
 								logger.debug("Plot 2D updated");
 							} else {
 								plottingSystem.createPlot2D(data, axes, null);
 								logger.debug("Plot 2D created");
 							}
 						}else{
 							if(axes.size()>0)
 								plottingSystem.createPlot2D(data, axes, null);
 							else
 								plottingSystem.createPlot2D(data, null, null);
 							logger.debug("Plot 2D created");
 						}
 						plottingSystem.repaint();
 
 					} else
 						logger.debug("No data to plot");
 				}
 			}
 		});
 	}
 
 	@Override
 	public void addIObserver(IObserver anIObserver) {
 		observers.add(anIObserver);
 	}
 
 	@Override
 	public void deleteIObserver(IObserver anIObserver) {
 		observers.remove(anIObserver);
 	}
 
 	@Override
 	public void deleteIObservers() {
 		observers.clear();
 	}
 	
 	@Override
 	public void processGUIUpdate(GuiBean guiBean) {
 		
 		final Collection<IRegion> regions = plottingSystem.getRegions();
 		final ROIBase currentRoi = (ROIBase)guiBean.get(GuiParameters.ROIDATA);
 
 		logger.debug("There is a guiBean update:"+ guiBean.toString());
 
 		if(currentRoi instanceof LinearROI){
 			final LinearROIList rois = (LinearROIList)guiBean.get(GuiParameters.ROIDATALIST);
 			
 			Display.getDefault().asyncExec(new Runnable(){
 				@Override
 				public void run() {
 					int roiListIdx = 0;
 					for (final IRegion iRegion : regions) {
 				
 						// current region
 						if(plotWindow.currentRoiPair.getName().equals(iRegion.getName())){
 							iRegion.setROI(currentRoi);
 						}
 						//roidatalist regions
 						else{
 							iRegion.setROI(rois.get(roiListIdx));
 							roiListIdx++;
 						}
 					}
 				}
 			});
 		} else if(currentRoi instanceof RectangularROI){
 			final RectangularROIList rois = (RectangularROIList)guiBean.get(GuiParameters.ROIDATALIST);
 			
 			Display.getDefault().asyncExec(new Runnable(){
 				@Override
 				public void run() {
 					int roiListIdx = 0;
 					for (final IRegion iRegion : regions) {
 				
 						// current region
 						if(plotWindow.currentRoiPair.getName().equals(iRegion.getName())){
 							iRegion.setROI(currentRoi);
 						}
 						//roidatalist regions
 						else{
 							iRegion.setROI(rois.get(roiListIdx));
 							roiListIdx++;
 						}
 					}
 				}
 			});
 		} else if(currentRoi instanceof SectorROI){
 			final SectorROIList rois = (SectorROIList)guiBean.get(GuiParameters.ROIDATALIST);
 			
 			Display.getDefault().asyncExec(new Runnable(){
 				@Override
 				public void run() {
 					int roiListIdx = 0;
 					for (final IRegion iRegion : regions) {
 				
 						// current region
 						if(plotWindow.currentRoiPair.getName().equals(iRegion.getName())){
 							iRegion.setROI(currentRoi);
 						}
 						//roidatalist regions
 						else{
 							iRegion.setROI(rois.get(roiListIdx));
 							roiListIdx++;
 						}
 					}
 				}
 			});
 		}
 	}
 }
