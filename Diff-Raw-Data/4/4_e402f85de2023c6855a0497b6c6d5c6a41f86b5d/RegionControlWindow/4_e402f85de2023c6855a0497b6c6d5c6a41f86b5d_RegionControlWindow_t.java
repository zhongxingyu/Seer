 package org.dawnsci.plotting.tools.window;
 
 import java.util.Collection;
 
 import org.dawb.common.ui.util.DisplayUtils;
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.PlotType;
 import org.dawnsci.plotting.api.region.IRegion;
 import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.dawnsci.plotting.api.trace.ILineStackTrace;
 import org.dawnsci.plotting.api.trace.ISurfaceTrace;
 import org.dawnsci.plotting.api.trace.ITrace;
 import org.dawnsci.plotting.roi.SurfacePlotROI;
 import org.dawnsci.plotting.tools.window.WindowTool.WindowJob;
 import org.dawnsci.plotting.util.PlottingUtils;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Spinner;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.part.IPageSite;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.roi.IROI;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 
 /**
  * Class used to create the composite with spinners to control the Window tool
  * @author wqk87977
  *
  */
 public class RegionControlWindow {
 
 	private static final Logger logger = LoggerFactory.getLogger(RegionControlWindow.class);
 
 	private Composite parent;
 	private Spinner spnStartX;
 	private Spinner spnStartY;
 	private Spinner spnWidth;
 	private Spinner spnHeight;
 	private Button btnOverwriteAspect;
 	private Spinner spnXAspect;
 	private Spinner spnYAspect;
 	private IPlottingSystem windowSystem;
 	private SelectionAdapter selectionListener;
 	private IPlottingSystem plottingSystem;
 	private boolean isOverwriteAspect;
 
 	public RegionControlWindow(Composite parent, 
 			final IPlottingSystem plottingSystem, 
 			final IPlottingSystem windowSystem, 
 			final WindowJob windowJob) {
 		this.parent = parent;
 		this.plottingSystem = plottingSystem;
 		this.windowSystem = windowSystem;
 		this.selectionListener = new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				isOverwriteAspect = btnOverwriteAspect.getSelection();
 
 				int startPosX = spnStartX.getSelection();
 				int startPosY = spnStartY.getSelection();
 				int width = spnWidth.getSelection();
 				int height = spnHeight.getSelection();
 				if (startPosX + width > spnWidth.getMaximum()) {
 					width = spnWidth.getMaximum() - startPosX;
 				}
 				if (startPosY + height > spnHeight.getMaximum()) {
 					height = spnHeight.getMaximum() - startPosY;
 				}
 				IRegion region = windowSystem.getRegion("Window");
 				RectangularROI rroi = new RectangularROI(startPosX, startPosY, width, height, 0);
 
 				if (!e.getSource().equals(btnOverwriteAspect)) {
 					if (region != null)
 						region.setROI(rroi);
 				} else if (e.getSource().equals(btnOverwriteAspect)) {
 					spnXAspect.setEnabled(isOverwriteAspect);
 					spnYAspect.setEnabled(isOverwriteAspect);
 				}
 				SurfacePlotROI sroi = createSurfacePlotROI(width, height, true);
 				windowJob.schedule(sroi);
 			}
 		};
 	}
 
 	/**
 	 * Gets the trace or the first trace if there are more than one.
 	 * @return
 	 */
 	protected ITrace getTrace() {
 		if (plottingSystem == null) return null;
 
 		final Collection<ITrace> traces = plottingSystem.getTraces();
 		if (traces==null || traces.size()==0) return null;
 		return traces.iterator().next();
 	}
 
 	protected ISurfaceTrace getSurfaceTrace() {
 		final ITrace trace = getTrace();
 		return trace instanceof ISurfaceTrace ? (ISurfaceTrace)trace : null;
 	}
 
 	public boolean isOverwriteAspect(){
 		return isOverwriteAspect;
 	}
 
 	public int getXAspectRatio() {
 		return spnXAspect.getSelection();
 	}
 
 	public int getYAspectRatio() {
 		return spnYAspect.getSelection();
 	}
 
 	public Composite createRegionControl(String title, IPageSite site, IViewPart viewPart, ImageDescriptor imageDescriptor) {
 		Composite windowComposite = new Composite(parent, SWT.NONE);
 		windowComposite.setLayout(new FillLayout(SWT.VERTICAL));
 
 		final Action reselect = new Action("Add ROI", imageDescriptor) {
 			public void run() {
 				createSurfaceRegion("Window", false);
 			}
 		};
 		site.getActionBars().getToolBarManager().add(new Separator("org.dawb.workbench.plotting.tools.profile.newProfileGroup"));
 		site.getActionBars().getToolBarManager().insertAfter("org.dawb.workbench.plotting.tools.profile.newProfileGroup", reselect);
 		site.getActionBars().getToolBarManager().add(new Separator("org.dawb.workbench.plotting.tools.profile.newProfileGroupAfter"));
 
 		windowSystem.createPlotPart(windowComposite, title, site.getActionBars(), PlotType.IMAGE, viewPart);
 
 		int xStartPt = 0;
 		int yStartPt = 0;
 		final ITrace trace = getTrace();
 		int xSize = 0, ySize = 0;
		if (trace != null && !(trace instanceof ILineStackTrace) && trace.getData() != null && trace.getData().getShape().length > 1) {
 			xSize = trace.getData().getShape()[1];
 			ySize = trace.getData().getShape()[0];
 		} else {
 			xSize = 1000;
 			ySize = 1000;
 		}
 
 		Composite bottomComposite = new Composite(windowComposite,SWT.NONE | SWT.BORDER);
 		bottomComposite.setLayout(new GridLayout(1, false));
 		bottomComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		Composite spinnersComp = new Composite(bottomComposite, SWT.NONE);
 		spinnersComp.setLayout(new GridLayout(4, false));
 		spinnersComp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
 		Label lblStartX = new Label(spinnersComp, SWT.RIGHT);
 		lblStartX.setText("Start X:");
 		
 		spnStartX = new Spinner(spinnersComp, SWT.BORDER);
 		spnStartX.setMinimum(0);
 		spnStartX.setMaximum(xSize);
 		spnStartX.setSize(62, 18);
 		spnStartX.addSelectionListener(selectionListener);
 
 		Label lblStartY = new Label(spinnersComp, SWT.RIGHT);
 		lblStartY.setText("Start Y:");
 
 		spnStartY = new Spinner(spinnersComp, SWT.BORDER);
 		spnStartY.setMinimum(0);
 		spnStartY.setMaximum(ySize);
 		spnStartY.setSize(62, 18);
 		spnStartY.addSelectionListener(selectionListener);
 
 		Label lblEndX = new Label(spinnersComp, SWT.RIGHT);
 		lblEndX.setText("Width:");
 
 		spnWidth = new Spinner(spinnersComp, SWT.BORDER);
 		spnWidth.setMinimum(0);
 		spnWidth.setMaximum(xSize);
 		spnWidth.setSize(62, 18);
 		spnWidth.addSelectionListener(selectionListener);
 
 		Label lblEndY = new Label(spinnersComp, SWT.RIGHT);
 		lblEndY.setText("Height:");
 
 		spnHeight = new Spinner(spinnersComp, SWT.BORDER);
 		spnHeight.setSize(62, 18);
 		spnHeight.setMinimum(0);
 		spnHeight.setMaximum(ySize);
 		spnHeight.addSelectionListener(selectionListener);
 
 		setSpinnerValues(xStartPt, yStartPt, xSize, ySize);
 
 		Composite aspectComp = new Composite(bottomComposite, SWT.NONE); 
 		aspectComp.setLayout(new GridLayout(4, false));
 		aspectComp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
 		btnOverwriteAspect = new Button(aspectComp,SWT.CHECK);
 		btnOverwriteAspect.setText("Override Aspect-Ratio");
 		btnOverwriteAspect.addSelectionListener(selectionListener);
 
 		spnXAspect = new Spinner(aspectComp,SWT.BORDER);
 		spnXAspect.setEnabled(false);
 		spnXAspect.setMinimum(1);
 		spnXAspect.setMaximum(10);
 		spnXAspect.setSelection(1);
 		spnXAspect.setIncrement(1);
 		spnXAspect.addSelectionListener(selectionListener);
 
 		Label lblDelimiter = new Label(aspectComp,SWT.NONE);
 		lblDelimiter.setText(":");
 
 		spnYAspect = new Spinner(aspectComp,SWT.BORDER);
 		spnYAspect.setEnabled(false);
 		spnYAspect.setMinimum(1);
 		spnYAspect.setMaximum(10);
 		spnYAspect.setSelection(1);
 		spnYAspect.setIncrement(1);
 		spnYAspect.addSelectionListener(selectionListener);
 
 		return windowComposite;
 	}
 
 	public boolean isControlReady() {
 		if (spnStartX == null || spnStartY == null || spnWidth == null
 				|| spnHeight == null || spnXAspect == null || spnYAspect == null
 				|| btnOverwriteAspect == null)
 			return false;
 		return true;
 	}
 
 	public void createSurfaceRegion(String regionName, boolean isDrag) {
 		IRegion region = windowSystem.getRegion(regionName);
 		//create Region
 		try {
 			if (region == null) {
 				region = windowSystem.createRegion(regionName, RegionType.BOX);
 
 				ISurfaceTrace surface = getSurfaceTrace();
 				IROI window = surface != null ? surface.getWindow() : null;
 				if (window == null) {
 					int height = surface.getData().getShape()[0];
 					int width = surface.getData().getShape()[1];
 					SurfacePlotROI sroi = createSurfacePlotROI(width, height, isDrag);
 					region.setROI(sroi);
 				} else {
 					region.setROI(window);
 				}
 
 				windowSystem.addRegion(region);
 			}
 		} catch (Exception e) {
 			logger.debug("Cannot create region for surface!", e);
 		}
 	}
 
 	/**
 	 * 
 	 * @param width
 	 * @param height
 	 * @return
 	 */
 	public SurfacePlotROI createSurfacePlotROI(int width, int height, boolean isDrag) {
 		int xAspectRatio = 0, yAspectRatio = 0, binShape = 1, samplingMode = 0;
 		if (isOverwriteAspect) {
 			xAspectRatio = getXAspectRatio();
 			yAspectRatio = getYAspectRatio();
 		}
 		binShape = PlottingUtils.getBinShape(width, height, isDrag);
 		if (binShape != 1) {
 			// DownsampleMode.MEAN = 2
 			samplingMode = 2;
 		}
 		SurfacePlotROI sroi = new SurfacePlotROI(spnStartX.getSelection(), 
 				spnStartY.getSelection(), 
 				spnStartX.getSelection() + spnWidth.getSelection(), 
 				spnStartY.getSelection() + spnHeight.getSelection(), 
 				samplingMode, samplingMode, 
 				xAspectRatio, 
 				yAspectRatio);
 		sroi.setXBinShape(binShape);
 		sroi.setYBinShape(binShape);
 
 		return sroi;
 	}
 
 	/**
 	 * Set the spinner values
 	 * @param startX start position in x dimension
 	 * @param startY start position in y dimension
 	 * @param width
 	 * @param height
 	 */
 	protected void setSpinnerValues(final int startX, 
 								 final int startY, 
 								 final int width, 
 								 final int height) {
 		DisplayUtils.runInDisplayThread(true, parent, new Runnable() {
 			@Override
 			public void run() {
 				spnStartX.setSelection(startX);
 				spnStartY.setSelection(startY);
 				spnWidth.setSelection(width);
 				spnHeight.setSelection(height);
 			}
 		});
 	}
 
 	public void addSelectionListener() {
 		if (spnStartX != null && !spnStartX.isDisposed())
 			spnStartX.addSelectionListener(selectionListener);
 		if (spnStartY != null && !spnStartY.isDisposed())
 			spnStartY.addSelectionListener(selectionListener);
 		if (spnWidth != null && !spnWidth.isDisposed())
 			spnWidth.addSelectionListener(selectionListener);
 		if (spnHeight != null && !spnHeight.isDisposed())
 			spnHeight.addSelectionListener(selectionListener);
 	}
 
 	public void removeSelectionListener() {
 		if (spnStartX != null && !spnStartX.isDisposed())
 			spnStartX.removeSelectionListener(selectionListener);
 		if (spnStartY != null && !spnStartY.isDisposed())
 			spnStartY.removeSelectionListener(selectionListener);
 		if (spnWidth != null && !spnWidth.isDisposed())
 			spnWidth.removeSelectionListener(selectionListener);
 		if (spnHeight != null && !spnHeight.isDisposed())
 			spnHeight.removeSelectionListener(selectionListener);
 	}
 }
