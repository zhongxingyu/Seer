 package org.dawnsci.plotting.jreality;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.trace.TraceEvent;
 
 import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.roi.IROI;
 
 class PlotterTrace {
 	
 	protected String                 name;
 	protected String                 dataName;
 	protected List<IDataset>         axes;
 	protected List<String>           axesNames;
 	protected JRealityPlotViewer     plotter;
 	protected boolean                active;
 	protected IPlottingSystem plottingSystem;
 	protected IROI                   window;
 
 	public void dispose() {
 		if (axes!=null) axes.clear();
 		axes = null;
 		if (axesNames!=null) axesNames.clear();
 		axesNames = null;
 		plotter = null;
 		plottingSystem=null;
 		window=null;
 	}
 	
 	protected AbstractDataset[] getStack(IDataset... s) {
 		AbstractDataset[] stack = new AbstractDataset[s.length];
		for (int i = 0; i < s.length; i++) stack[i] = (AbstractDataset)s[i];
 		return stack;
 	}
 
 	public PlotterTrace(JRealityPlotViewer plotter2, String name2) {
 		this.plotter = plotter2;
 		this.name    = name2;
 	}
 	public String getName() {
 		return name;
 	}
 
 	public List<IDataset> getAxes() {
 		return axes;
 	}
 
 	public boolean isActive() {
 		return active;
 	}
 
 	protected final void setActive(boolean active) {
 		this.active = active;
 		if (active) {
 			if (plottingSystem!=null) plottingSystem.fireTraceAdded(new TraceEvent(this));
 		}
 	}
 	protected List<AxisValues> createAxisValues() {
 		
 		final AxisValues xAxis = new AxisValues(getLabel(0), axes!=null?(AbstractDataset)axes.get(0):null);
 		final AxisValues yAxis = new AxisValues(getLabel(1), axes!=null?(AbstractDataset)axes.get(1):null);
 		final AxisValues zAxis = new AxisValues(getLabel(2), axes!=null?(AbstractDataset)axes.get(2):null);
 		return Arrays.asList(xAxis, yAxis, zAxis);
 	}
 
 	protected String getLabel(int i) {
 		String label = axesNames!=null ? axesNames.get(i) : null;
 		if  (label==null) label = (axes!=null && axes.get(i)!=null) ? axes.get(i).getName() : null;
 		return label;
 	}
 
 	public List<String> getAxesNames() {
 		return axesNames;
 	}
 
 	public void setAxesNames(List<String> axesNames) {
 		this.axesNames = axesNames;
 	}
 
 	private Object userObject;
 
 	public Object getUserObject() {
 		return userObject;
 	}
 
 	public void setUserObject(Object userObject) {
 		this.userObject = userObject;
 	}
 
 	/**
 	 * True if visible
 	 * @return
 	 */
 	public boolean isVisible() {
 		return isActive();
 	}
 
 	/**
 	 * True if visible
 	 * @return
 	 */
 	public void setVisible(boolean isVisible) {
 		// TODO FIXME What to do to make plots visible/invisible?
 	}
 
 	private boolean isUserTrace=true;
 
 	public boolean isUserTrace() {
 		return isUserTrace;
 	}
 
 	public void setUserTrace(boolean isUserTrace) {
 		this.isUserTrace = isUserTrace;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public IPlottingSystem getPlottingSystem() {
 		return plottingSystem;
 	}
 
 	public void setPlottingSystem(IPlottingSystem plottingSystem) {
 		this.plottingSystem = plottingSystem;
 	}
 
     public IROI getWindow() {
 		return window;
 	}
 
 	public String getDataName() {
 		return dataName;
 	}
 
 	public void setDataName(String dataName) {
 		this.dataName = dataName;
 	}
 
 }
