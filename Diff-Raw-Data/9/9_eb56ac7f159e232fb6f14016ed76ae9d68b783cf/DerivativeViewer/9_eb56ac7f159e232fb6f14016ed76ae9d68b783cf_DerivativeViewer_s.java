 /*
  * Copyright © 2011 Diamond Light Source Ltd.
  * Contact :  ScientificSoftware@diamond.ac.uk
  * 
  * This is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  * 
  * This software is distributed in the hope that it will be useful, but 
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
  * Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along
  * with this software. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.diamond.scisoft.analysis.rcp.plotting.sideplot;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IWorkbenchPartSite;
 import org.eclipse.ui.part.ViewPart;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.SDAPlotter;
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.Maths;
 import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
 import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.AxisValues;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSetPlotter;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.IPlotUI;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.Plot1DUIAdapter;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlotException;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlottingMode;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.AxisMode;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.overlay.Overlay1DConsumer;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.overlay.OverlayProvider;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.tools.AreaSelectEvent;
 import uk.ac.diamond.scisoft.analysis.rcp.views.plot.AbstractPlotView;
 import uk.ac.diamond.scisoft.analysis.rcp.views.plot.PlotBean;
 import uk.ac.diamond.scisoft.analysis.rcp.views.plot.PlotView;
 import uk.ac.diamond.scisoft.analysis.rcp.views.plot.StaticScanPlotView;
 import uk.ac.gda.ui.actions.CheckableActionGroup;
 
 import com.swtdesigner.SWTResourceManager;
 
 /**
  * Simple class which simply plots the derivatives of anything plotted in the plotter.
  * 
  * This class so far tested with 1D editors rather than plot views.
  * 
  * First derivative in 1D is needed by some science.
  * 
  * TODO - change to arbitrary function plotter?
  *      - allow regions? derivative of region rather than whole function.
  *      
  */
 public class DerivativeViewer extends SidePlot implements Overlay1DConsumer, PlotView {
 
     private static final Logger logger = LoggerFactory.getLogger(DerivativeViewer.class);	
 	
 	private DataSetPlotter    plotter;
 	private IPlotUI           plotUI;
 
 	public DerivativeViewer() {
 		super();
 	}
 
 	@Override
 	public Action createSwitchAction(final int index, final IPlotUI plotUI) {
 		this.plotUI = plotUI;
 		Action action = super.createSwitchAction(index, plotUI);
 		action.setText("Derivative Viewer");
 		action.setToolTipText("Show the derivative of the data set(s) plotted in a side plot.");
 		action.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/SidePlot-Derivative.png"));
 
 		return action;
 	}
 
 	/**
 	 * @wbp.parser.entryPoint
 	 */
 	@Override
 	public void createPartControl(Composite parent) {
 		
 		parent.setLayout(new FillLayout());
 		
 		final Composite main = new Composite(parent, SWT.NONE);
 		GridLayout gl_main = new GridLayout(1, false);
 		gl_main.marginWidth = 0;
 		gl_main.marginHeight = 0;
 		gl_main.horizontalSpacing = 0;
 		main.setLayout(gl_main);
 		this.container = main;
 		
 		Composite peakPlotter = new Composite(main, SWT.NONE);
 		peakPlotter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true)); 
 		peakPlotter.setLayout(new FillLayout());
 		
 		plotter = new DataSetPlotter(PlottingMode.ONED, peakPlotter);
 		plotter.setAxisModes(AxisMode.CUSTOM, AxisMode.LINEAR, AxisMode.LINEAR);
 		
 		
 	}
 	
 	
 	// Not too happy about keeping state in fields, perhaps move to bean.
     private int     derivative = 1;
 	
 	@Override
 	public void generateToolActions(IToolBarManager manager) {
 				
  		createDerivativeActions(manager);
 
 		//manager.add(StaticScanPlotView.getOpenStaticPlotAction(this));
 		final IAction showLeg = Plot1DUIAdapter.createShowLegend(plotter);
 		showLeg.setChecked(true);
 		manager.add(showLeg);
 
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		if (plotter != null)
 			plotter.cleanUp();
 		plotter = null;
 	}
 
 	public AbstractDataset getXValues() {
 		
 		final List<AxisValues> xValues = mainPlotter.getXAxisValues();
 		
 		return xValues.get(0).toDataset();
 
 	}
 	
 	public Collection<AbstractDataset> getYValues() {
 		
 		final List<AxisValues> xValues = mainPlotter.getXAxisValues();
 		
 		/**
 		 * These are the y data sets.
 		 */
 		final List<IDataset> ys = mainPlotter.getCurrentDataSets();
 
 		final Collection<AbstractDataset> dervs = new ArrayList<AbstractDataset>(ys.size());
 		for (int i = 0; i < ys.size(); i++) {
 			final AbstractDataset x    = xValues.get(i).toDataset();
 			final AbstractDataset y    = (AbstractDataset)ys.get(i);
 			AbstractDataset derv = (AbstractDataset)ys.get(i);
 			for (int di = 0; di < derivative; di++) { 
 				derv = Maths.derivative(x, derv, derivative);
 			}
 			derv.setName("f"+getTicksFor(derivative)+" {" +y.getName()+" }");
 			dervs.add(derv);
 		}
 		
 		return dervs;
 	}
 	
 	@Override
 	public void processPlotUpdate() {
 		
 		if (mainPlotter==null) {
 		    clearPeakPlotter();
 		    return;
 		}
 		
 		final List<AxisValues> xValues = mainPlotter.getXAxisValues();
 		
 		/**
 		 * These are the y data sets.
 		 */
 		final List<IDataset> ys = mainPlotter.getCurrentDataSets();
 		if (ys==null||ys.isEmpty()) {
 		    clearPeakPlotter();
 		    return;
 		}
 		
 		final Collection<AbstractDataset> dervs = new ArrayList<AbstractDataset>(ys.size());
 		for (int i = 0; i < ys.size(); i++) {
 			final AbstractDataset x    = xValues.get(i).toDataset();
 			final AbstractDataset y    = (AbstractDataset)ys.get(i);
 			AbstractDataset derv = (AbstractDataset)ys.get(i);
 			for (int di = 0; di < derivative; di++) { 
 				derv = Maths.derivative(x, derv, derivative);
 			}
 			derv.setName("f"+getTicksFor(derivative)+" {" +y.getName()+" }");
 			dervs.add(derv);
 		}
 		
 		try {
 			AbstractPlotView.createMultipleLegend(plotter, dervs);
 			plotter.replaceAllPlots(dervs, xValues);
 		} catch (PlotException e) {
 			logger.error("Cannot update plot with "+dervs);
 		}
 		plotter.refresh(false);
 	}
 
 	private String getTicksFor(int size) {
         final StringBuilder buf = new StringBuilder();
         for (int i = 0; i < size; i++) buf.append("'");
         return buf.toString();
 	}
 
 	@Override
 	public void showSidePlot() {
 		processPlotUpdate();
 	}
 	
 	@Override
 	public void registerProvider(OverlayProvider provider) {
 	}
 
 	@Override
 	public void removePrimitives() {
 //		int primNumMax = primitiveIDs.size();
 //		if (primNumMax > 0)
 //			oProvider.unregisterPrimitive(primitiveIDs);
 	}
 	
 	private void clearPeakPlotter() {
 		if (plotter != null) {
 			try {
 				plotter.replaceAllPlots(new ArrayList<IDataset>());
 				removePrimitives();
 			} catch (PlotException e) {
 				logger.warn("The plot could not be cleared in "+getClass().getName());
 			}
 			plotter.refresh(false);
 		}
 	}
 
 	@Override
 	public void unregisterProvider() {
 	}
 
 	@Override
 	public void areaSelected(AreaSelectEvent event) {
 
 		
 	}
 
 
 	///////////////////////////////////////////////////////////////////////////////////////////////
 	/**
 	 * Not currently needed methods:
 	 */
 	@Override
 	public void addToHistory() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void generateMenuActions(IMenuManager manager, IWorkbenchPartSite site) {
  		final DerivativeViewer viewer = this;
  		
  		final Action action = new Action() {
 			@Override
 			public void run() {
 				try {
 					SDAPlotter.plot("Plot 2", viewer.getXValues(), viewer.getYValues().toArray(new AbstractDataset[0]));
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					logger.error("TODO put description of error here", e);
 				}
 			}
 
 		};
 		action.setToolTipText("Send the results of the derivative to a seperate plot view");
 		final org.eclipse.swt.graphics.Image icon = SWTResourceManager.getImage(StaticScanPlotView.class,"/icons/chart_curve_add.png");
 		final ImageDescriptor d = ImageDescriptor.createFromImage(icon);
 		action.setImageDescriptor(d);
 		action.setText("Send derivative to Plot 2");
 		manager.add(action);
 		
 	}
 	@Override
 	public void removeFromHistory() {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public int updateGUI(GuiBean bean) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 	/**
 	 * End not currently needed
 	 */
 	///////////////////////////////////////////////////////////////////////////////////////////////
 
 	@Override
 	public String getPartName() {
 		return getSite().getPart().getTitle();
 	}
 
 	@Override
 	public PlotBean getPlotBean() {
 		final PlotBean bean = new PlotBean();
 		// TODO add data
 		return bean;
 	}
 
 	@Override
 	public IWorkbenchPartSite getSite() {
 		return ((ViewPart)plotUI.getSidePlotView()).getSite();
 	}
 	
 	
 
 	private void createDerivativeActions(IToolBarManager manager) {
 		
 		final CheckableActionGroup group = new CheckableActionGroup();
 		final Action first = new Action("First Derivative", IAction.AS_CHECK_BOX) {
 			@Override
 			public void run() {
 				updatePlotInternal(1);
 			}
 		};
 		group.add(first);
 		first.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/First-Derivative.png"));
 		first.setChecked(true);
 		manager.add(first);
 		
 		
 		final Action second = new Action("Second Derivative", IAction.AS_CHECK_BOX) {
 			@Override
 			public void run() {
 				updatePlotInternal(2);
 			}
 		};
 		second.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/Second-Derivative.png"));
 		group.add(second);
 		manager.add(second);
 		
 		final Action third = new Action("Third Derivative", IAction.AS_CHECK_BOX) {
 			@Override
 			public void run() {
 				updatePlotInternal(3);
 			}
 		};
 		third.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/Third-Derivative.png"));
 		group.add(third);
 		manager.add(third);
 		
 
 	}
 
 	protected void updatePlotInternal(final int derivative) {
 		this.derivative    = derivative;
 		processPlotUpdate();
 	}
 }
