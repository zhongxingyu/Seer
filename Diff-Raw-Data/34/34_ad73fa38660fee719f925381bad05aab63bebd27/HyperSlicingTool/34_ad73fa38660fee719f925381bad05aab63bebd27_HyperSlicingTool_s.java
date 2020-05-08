 package org.dawnsci.slicing.tools.hyper;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.slicing.api.system.AxisChoiceEvent;
 import org.dawnsci.slicing.api.system.AxisChoiceListener;
 import org.dawnsci.slicing.api.system.DimensionalEvent;
 import org.dawnsci.slicing.api.system.DimensionalListener;
 import org.dawnsci.slicing.api.system.DimsData;
 import org.dawnsci.slicing.api.system.DimsDataList;
 import org.dawnsci.slicing.api.system.SliceSource;
 import org.dawnsci.slicing.api.tool.AbstractSlicingTool;
 import org.eclipse.swt.widgets.Control;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.Slice;
 
 /**
  * This class installs the special Hyper3D slicing tool which
  * replaces the traditional plotting system with an alternative
  * slicer.
  * 
  * @author fcp94556
  *
  */
 public class HyperSlicingTool extends AbstractSlicingTool {
 	
 	private static final Logger logger = LoggerFactory.getLogger(HyperSlicingTool.class);
 
 	private HyperComponent hyperComponent;
 	private Control        originalPlotControl;
 	private HyperType      hyperType = HyperType.Box_Axis;
 
 	private DimensionalListener dimensionalListener;
 	private AxisChoiceListener  axisChoiceListener;
 	
 	public HyperSlicingTool() {
 		this.dimensionalListener = new DimensionalListener() {			
 			@Override
 			public void dimensionsChanged(DimensionalEvent evt) {
 				update();
 			}
 		};
 
 		this.axisChoiceListener = new AxisChoiceListener() {
 			@Override
 			public void axisChoicePerformed(AxisChoiceEvent evt) {
 				update();
 			}
 		};
 	}
 	
 	/**
 	 * We actually install the HyperComponent instead of the plotting system.
 	 */
 	@Override
 	public void militarize() {
 		
 		getSlicingSystem().setSliceType(getSliceType());
 
 		final IPlottingSystem plotSystem = getSlicingSystem().getPlottingSystem();
         if (hyperComponent==null) {
         	hyperComponent = new HyperComponent(plotSystem.getPart());
         	hyperComponent.createControl(plotSystem.getPlotComposite());
         }
         
         originalPlotControl = plotSystem.setControl(hyperComponent.getControl(), false);
         
  		final DimsDataList dimsDataList = getSlicingSystem().getDimsDataList();
 		if (dimsDataList!=null) dimsDataList.setThreeAxesOnly(0, 1, 2);   		
 		
  		getSlicingSystem().refresh();
  		getSlicingSystem().update();
  		
         update(); // Now that the axes are right we can do a hyper slice!
 
 		getSlicingSystem().addDimensionalListener(dimensionalListener);
 		getSlicingSystem().addAxisChoiceListener(axisChoiceListener);
 	}
 
 	/**
 	 * Does nothing unless overridden.
 	 */
 	@Override
 	public void demilitarize() {
 		
 		if (originalPlotControl==null) return;
         final IPlottingSystem plotSystem = getSlicingSystem().getPlottingSystem();
 		plotSystem.setControl(originalPlotControl, true);
 		originalPlotControl = null;
 		
 		if (dimensionalListener!=null) {
 			getSlicingSystem().removeDimensionalListener(dimensionalListener);
 		}
 		if (axisChoiceListener!=null) {
 			getSlicingSystem().removeAxisChoiceListener(axisChoiceListener);
 		}
 
 	}
 
 	private void update() {
 		try {
 			final DimsDataList dimsDataList = getSlicingSystem().getDimsDataList();
 			// Make sure the plot axes count is the same as the current types dimensions
 			if (dimsDataList.getAxisCount()!=hyperType.getDimensions()) return;
           
 			final SliceSource data = getSlicingSystem().getData();
             setData(data.getLazySet(), getAbstractNexusAxes(), getSlices(), getOrder(), hyperType);
         } catch (Exception ne) {
         	logger.error("Cannot set data to HyperComponent!", ne);
         }
 	}
 
 	public void setData(ILazyDataset lazy, List<AbstractDataset> daxes, Slice[] slices, int[] order, HyperType hyperType) {
 		
 		switch (hyperType) {
 		case Box_Axis:
 			hyperComponent.setData(lazy, daxes, slices, order);
 			break;
 		case Line_Line:
 			hyperComponent.setData(lazy, daxes, slices, order,new ArpesMainImageReducer(),new ArpesSideImageReducer());
 			break;
 		case Line_Axis:
 			hyperComponent.setData(lazy, daxes, slices, order,new TraceLineReducer(),new ImageTrapeziumBaselineReducer());
 			break;
 		}
 		
 	}
 	/**
 	 * Currently always gets the dimension order of the 
 	 * @return
 	 */
 	private int[] getOrder() {
 		final DimsDataList dims = getSlicingSystem().getDimsDataList();
 		final int[] ret = new int[3];
 		int dimFound = 0;
 		for (DimsData dd : dims.getDimsData()) {
 			if (dd.isSlice()) continue;
 			ret[dimFound] = dd.getPlotAxis();
 			dimFound++;
 		}
 		return ret;
 	}
 
 	private Slice[] getSlices() {
 		
 		int[] dataShape         = getSlicingSystem().getData().getLazySet().getShape();
 		final DimsDataList dims = getSlicingSystem().getDimsDataList();
 		
 		final Slice[] ret = new Slice[dims.size()];
 		for (int i = 0; i < dims.size(); i++) {
 			DimsData dd = dims.getDimsData(i);			
 			if (dd.isSlice()) {
 				ret[i] = new Slice(dd.getSlice(), dd.getSlice()+1);
 			} else {
 				ret[i] = new Slice(dataShape[dd.getDimension()]);
 			}
 		}
 		return ret;
 	}
 	
 	private List<AbstractDataset> getAbstractNexusAxes() throws Exception {
 		
 		final int[] dataShape    = getSlicingSystem().getData().getLazySet().getShape();
 		List<IDataset> nexusAxes = getNexusAxes();
 		List<IDataset> ia        = null;
 		if (nexusAxes==null || nexusAxes.isEmpty()) {
 			ia = new ArrayList<IDataset>(dataShape.length);
 			for (int i = 0; i < dataShape.length; i++) ia.add(null);
 		} else {
 			ia = new ArrayList<IDataset>(nexusAxes);
 		}
 		while(ia.size()<hyperType.getDimensions()) ia.add(null);
 		
 		final List<AbstractDataset> ret= new ArrayList<AbstractDataset>(ia.size());
 		for (int i = 0; i < ia.size(); i++) {
 			IDataset id = ia.get(i);
			if (id == null) id = AbstractDataset.arange(dataShape[i], AbstractDataset.INT);
 			ret.add((AbstractDataset)id);
 		}
 		return ret;
 	}
 	
 	@Override
 	public void dispose() {
 		demilitarize();
 	}
 
 	@Override
 	public Enum getSliceType() {
 		return hyperType;
 	}
 
 	@Override
 	public Object getAdapter(Class clazz) {
 		if (clazz == HyperComponent.class) {
 			return hyperComponent;
 		}else if (clazz == HyperType.class) {
 			return hyperType;
 		}
 		return super.getAdapter(clazz);
 	}
 }
