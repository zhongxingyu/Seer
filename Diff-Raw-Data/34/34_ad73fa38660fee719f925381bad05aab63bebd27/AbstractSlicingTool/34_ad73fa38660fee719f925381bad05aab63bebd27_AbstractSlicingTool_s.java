 package org.dawnsci.slicing.api.tool;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.dawb.common.services.ServiceManager;
 import org.dawnsci.slicing.api.system.DimsData;
 import org.dawnsci.slicing.api.system.DimsDataList;
 import org.dawnsci.slicing.api.system.ISliceSystem;
 import org.dawnsci.slicing.api.util.SliceUtils;
 
 import uk.ac.diamond.scisoft.analysis.IAnalysisService;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 
 /**
  * Convenience class for extending to provide a tool.
  * 
  * @author fcp94556
  *
  */
 public abstract class AbstractSlicingTool implements ISlicingTool {
 
 	protected ISliceSystem slicingSystem;
 	protected String       toolId;
 
 	/**
 	 * Does nothing but demilitarize() unless overridden.
 	 */
 	@Override
 	public void dispose() {
 		demilitarize();
 	}
 
 	@Override
 	public String getToolId() {
 		return toolId;
 	}
 
 	@Override
 	public void setToolId(String toolId) {
 		this.toolId = toolId;
 	}
 
 	@Override
 	public ISliceSystem getSlicingSystem() {
 		return slicingSystem;
 	}
 
 	@Override
 	public void setSlicingSystem(ISliceSystem slicingSystem) {
 		this.slicingSystem = slicingSystem;
 	}
 	
 	
 	/**
 	 * Does nothing unless overridden.
 	 */
 	@Override
 	public void demilitarize() {
 		
 	}
 
 	@Override
 	public Object getAdapter(Class clazz) {
         return null;
 	}
 	
 	
 	/**
 	 * May be null. Returns the axes in dimensional order.
 	 * @return
 	 */
 	protected List<IDataset> getNexusAxes() throws Exception {
 		
 		final Map<Integer, String> names = getSlicingSystem().getAxesNames();
 		final DimsDataList           ddl = getSlicingSystem().getDimsDataList();
 		final int[]            dataShape = getSlicingSystem().getData().getLazySet().getShape();
 		
 		final List<IDataset>         ret = new ArrayList<IDataset>(3);
 		for (DimsData dd : ddl.getDimsData()) {
 			
 			IDataset axis = null;
 			try {
 				final String name = names.get(dd.getDimension()+1);
 				axis = SliceUtils.getNexusAxis(getSlicingSystem().getCurrentSlice(), name, false, null);
 			} catch (Throwable e) {
				return null;
 			}
             if (axis==null) {
             	final IAnalysisService service = (IAnalysisService)ServiceManager.getService(IAnalysisService.class);
             	axis = service.arange(dataShape[dd.getDimension()], IAnalysisService.INT);
             }
             ret.add(axis);
 		}
 		return ret;
 	}
 
 }
