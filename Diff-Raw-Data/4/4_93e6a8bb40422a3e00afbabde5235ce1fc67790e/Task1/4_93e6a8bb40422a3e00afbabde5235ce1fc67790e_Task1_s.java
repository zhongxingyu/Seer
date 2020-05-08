 package ru.kt15.finomen.gui;
 
 import java.awt.Component;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.awt.SWT_AWT;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Spinner;
 import org.jzy3d.chart.Chart;
 import org.jzy3d.chart.controllers.mouse.camera.CameraMouseController;
 import org.jzy3d.chart.controllers.thread.camera.CameraThreadController;
 import org.jzy3d.colors.Color;
 import org.jzy3d.colors.ColorMapper;
 import org.jzy3d.colors.colormaps.ColorMapRainbow;
 import org.jzy3d.maths.BoundingBox3d;
 import org.jzy3d.maths.Coord3d;
 import org.jzy3d.maths.Scale;
 import org.jzy3d.plot3d.primitives.AbstractDrawable;
 import org.jzy3d.plot3d.primitives.MultiColorScatter;
 import org.jzy3d.plot3d.primitives.axes.AxeBox;
 import org.jzy3d.plot3d.rendering.canvas.Quality;
 
 import swing2swt.layout.BorderLayout;
 import swing2swt.layout.BoxLayout;
 
 public class Task1 extends Composite {
 
 	private Composite graphsPanel;
 	private Spinner implicitEulerSteps;
 	private Spinner maxTime;
 	private Spinner timeStep;
 	private Spinner startR;
 	private Spinner endR;
 	private Spinner rStep;
 	private ScrolledComposite scroll;
 	private Chart chart;
 	private Composite chartComposite;
 
 	/**
 	 * Create the composite.
 	 * @param parent
 	 * @param style
 	 */
 	public Task1(Composite parent, int style) {
 		super(parent, style);
 		setLayout(new BorderLayout(0, 0));
 		
 		Group grpControl = new Group(this, SWT.NONE);
 		grpControl.setText("Control");
 		grpControl.setLayoutData(BorderLayout.WEST);
 		grpControl.setLayout(new GridLayout(2, false));
 		
 		Label lblStep = new Label(grpControl, SWT.NONE);
 		lblStep.setText("Time step");
 		
 		timeStep = new Spinner(grpControl, SWT.BORDER);
 		timeStep.setMaximum(1000000);
 		timeStep.setSelection(10000);
 		timeStep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
 		timeStep.setDigits(6);
 		
 		Label lblNewLabel = new Label(grpControl, SWT.NONE);
 		lblNewLabel.setText("Max time");
 		
 		maxTime = new Spinner(grpControl, SWT.BORDER);
 		maxTime.setMaximum(10000);
 		maxTime.setSelection(1000);
 		maxTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
 		maxTime.setDigits(2);
 		
 		Label lblExplicitEulerSteps = new Label(grpControl, SWT.NONE);
 		lblExplicitEulerSteps.setText("Implicit euler steps");
 		
 		implicitEulerSteps = new Spinner(grpControl, SWT.BORDER);
 		implicitEulerSteps.setSelection(5);
 		implicitEulerSteps.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
 		
 		Label lblStartR = new Label(grpControl, SWT.NONE);
 		lblStartR.setText("Start r");
 		
 		startR = new Spinner(grpControl, SWT.BORDER);
 		startR.setSelection(17);
 		startR.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
 		
 		Label lblEndR = new Label(grpControl, SWT.NONE);
 		lblEndR.setText("End r");
 		
 		endR = new Spinner(grpControl, SWT.BORDER);
 		endR.setSelection(18);
 		endR.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
 		
 		Label lblRStep = new Label(grpControl, SWT.NONE);
 		lblRStep.setText("r step");
 		
 		rStep = new Spinner(grpControl, SWT.BORDER);
 		rStep.setMaximum(1000);
 		rStep.setSelection(200);
 		rStep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
 		rStep.setDigits(2);
 		
 		Button btnCalculate = new Button(grpControl, SWT.NONE);
 		btnCalculate.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				updateGraphs();
 			}
 		});
 		
 		btnCalculate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
 		btnCalculate.setText("Calculate");
 		
 		new Label(grpControl, SWT.NONE);
 		new Label(grpControl, SWT.NONE);
 		
 		scroll = new ScrolledComposite(this, SWT.V_SCROLL);
 		scroll.setLayoutData(BorderLayout.EAST);
 		//scroll.setLayout(new FillLayout());
 		
 		graphsPanel = new Composite(scroll, SWT.NONE);
 		graphsPanel.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
 	/*	
 		int size = 100000;
 		float x;
 		float y;
 		float z;
 		Coord3d[] points = new Coord3d[size];
 
 		// Create scatter points
 		for(int i=0; i<size; i++){
 		    x = (float)Math.random() - 0.5f;
 		    y = (float)Math.random() - 0.5f;
 		    z = (float)Math.random() - 0.5f;
 		    points[i] = new Coord3d(x, y, z);
 		}       
 
 		// Create a drawable scatter with a colormap
 		final MultiColorScatter scatter = new MultiColorScatter( points, new ColorMapper( new ColorMapRainbow(), -0.5f, 0.5f ) );
 		*/
 		chart = new Chart(Quality.Advanced, "awt");
 		
 		chart.getAxeLayout().setMainColor(Color.GREEN);
 		chart.getAxeLayout().setXAxeLabel("x");
 		chart.getAxeLayout().setYAxeLabel("y");
 		chart.getAxeLayout().setZAxeLabel("z");
 		chart.setAxeDisplayed(true);
 		
 		chart.getView().setBackgroundColor(Color.BLACK);
 		
 	    CameraMouseController mouse   = new CameraMouseController();
         
         chart.addController(mouse);
         
         
         CameraThreadController thread = new CameraThreadController();
         mouse.addSlaveThreadController(thread);
         chart.addController(thread);
 
         thread.start();
 		
 		chartComposite = new Composite(this, SWT.EMBEDDED);
 		chartComposite.setLayoutData(BorderLayout.CENTER);
 		java.awt.Frame chartFrame = SWT_AWT.new_Frame(chartComposite);
 		
 		chartFrame.add((Component)chart.getCanvas());
 		//chart.getScene().add(scatter);
 		
 		chart.setScale(new Scale(-1, 1));
 	}
 
 	@Override
 	protected void checkSubclass() {
 		// Disable the check that prevents subclassing of SWT components
 	}
 	
 	private static double readSpinner(Spinner spinner) {
 		return spinner.getSelection() * 1.0 / Math.pow(10, spinner.getDigits());
 	}
 	
 	private void updateGraphs() {
		for (AbstractDrawable d : chart.getScene().getGraph().getAll()) {
			chart.removeDrawable(d);
		}
 		graphsPanel.dispose();
 		graphsPanel = new Composite(scroll, SWT.NONE);
 		graphsPanel.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
 		
 		final ConcurrentLinkedQueue<GraphWidget> wQueue = new ConcurrentLinkedQueue<>();
 		
 		for (double r = readSpinner(startR); r <= readSpinner(endR); r += readSpinner(rStep)) {
 			GraphWidget w = new GraphWidget(graphsPanel, SWT.NONE, r, chart);
 			wQueue.add(w);
 		}
 		
 		graphsPanel.pack();
 		scroll.setContent(graphsPanel);
 		
 		int threads = Runtime.getRuntime().availableProcessors();
 		
 		final double timeStep = readSpinner(this.timeStep);
 		final double maxTime = readSpinner(this.maxTime);
 		final int eulerImplicitSteps = implicitEulerSteps.getSelection();
 		
 		this.layout();
 		
 		for (int i = 0; i < threads; ++i) {
 			new Thread(new Runnable() {
 				@Override
 				public void run() {		
 					GraphWidget widget;
 					while ((widget= wQueue.poll()) != null) {
 						widget.calculate(timeStep, maxTime, eulerImplicitSteps);
 					}
 				}
 			}).start();
 		}
 		
 	}
 }
