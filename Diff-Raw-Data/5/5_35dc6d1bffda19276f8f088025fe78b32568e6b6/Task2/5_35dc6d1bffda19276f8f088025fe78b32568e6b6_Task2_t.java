 package ru.kt15.finomen.gui;
 
 import java.awt.Component;
 import java.util.ArrayList;
 
 import org.eclipse.swt.widgets.Composite;
 
 import ru.kt15.finomen.task2.ExplicitDownstream;
 import ru.kt15.finomen.task2.ExplicitUpstream;
 import ru.kt15.finomen.task2.ImplicitDownstream;
 import ru.kt15.finomen.task2.ImplicitUpstream;
 import ru.kt15.finomen.task2.Schema;
 import swing2swt.layout.BorderLayout;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.SWT;
 import swing2swt.layout.BoxLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Spinner;
 import org.eclipse.swt.awt.SWT_AWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.jzy3d.chart.Chart;
 import org.jzy3d.chart.controllers.mouse.camera.CameraMouseController;
 import org.jzy3d.chart.controllers.thread.camera.CameraThreadController;
 import org.jzy3d.colors.Color;
 import org.jzy3d.colors.ColorMapper;
 import org.jzy3d.colors.colormaps.ColorMapHotCold;
 import org.jzy3d.colors.colormaps.ColorMapRainbow;
 import org.jzy3d.maths.Coord3d;
 import org.jzy3d.maths.Range;
 import org.jzy3d.maths.Scale;
 import org.jzy3d.plot3d.builder.Builder;
 import org.jzy3d.plot3d.builder.Mapper;
 import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
 import org.jzy3d.plot3d.primitives.AbstractDrawable;
 import org.jzy3d.plot3d.primitives.MultiColorScatter;
 import org.jzy3d.plot3d.primitives.Scatter;
 import org.jzy3d.plot3d.primitives.Shape;
 import org.jzy3d.plot3d.rendering.canvas.Quality;
 
 public class Task2 extends Composite {
 
 	private Spinner width;
 	private Spinner steps;
 	private Spinner nodes;
 	private Spinner kappa;
 	private Spinner mu;
 	private final Schema[] schemas = new Schema[] {
 			new ExplicitUpstream(),
 			new ExplicitDownstream(),
 			new ImplicitUpstream(),
 			new ImplicitDownstream()
 	};
 	
 	private int schema = 0;
 	private Chart chart;
 	private Combo initialConditions;
 	private AbstractDrawable shape;
 
 	/**
 	 * Create the composite.
 	 * @param parent
 	 * @param style
 	 */
 	public Task2(Composite parent, int style) {
 		super(parent, style);
 		this.chart = chart;
 		setLayout(new BorderLayout(0, 0));
 		
 		Group grpControl = new Group(this, SWT.NONE);
 		grpControl.setText("Control");
 		grpControl.setLayoutData(BorderLayout.WEST);
 		grpControl.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
 		
 		Group grpScheme = new Group(grpControl, SWT.NONE);
 		grpScheme.setText("Scheme");
 		grpScheme.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
 		
 		Button btnExplicitUpstream = new Button(grpScheme, SWT.RADIO);
 		btnExplicitUpstream.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				schema = 0;
 			}
 		});
 		btnExplicitUpstream.setSelection(true);
 		btnExplicitUpstream.setText("Explicit upstream");
 		
 		Button btnExplicitDownstream = new Button(grpScheme, SWT.RADIO);
 		btnExplicitDownstream.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				schema = 1;
 			}
 		});
 		btnExplicitDownstream.setText("Explicit downstream");
 		
 		Button btnImplicitUpstream = new Button(grpScheme, SWT.RADIO);
 		btnImplicitUpstream.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				schema = 2;
 			}
 		});
 		btnImplicitUpstream.setText("Implicit upstream");
 		
 		Button btnImplicitDownstream = new Button(grpScheme, SWT.RADIO);
 		btnImplicitDownstream.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				schema = 3;
 			}
 		});
 		btnImplicitDownstream.setText("Implicit downstream");
 		
 		final Combo presets = new Combo(grpControl, SWT.NONE);
 		presets.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				switch (presets.getSelectionIndex()) {
 				case 0:
 					width.setSelection(10);
 					steps.setSelection(100);
 					nodes.setSelection(20);
 					kappa.setSelection(20);
 					mu.setSelection(0);
 					break;
 				case 1:
 					width.setSelection(40);
 					steps.setSelection(200);
 					nodes.setSelection(200);
 					kappa.setSelection(0);
 					mu.setSelection(10);
 					break;
 				case 2:
 					width.setSelection(40);
 					steps.setSelection(200);
 					nodes.setSelection(200);
 					kappa.setSelection(31);
 					mu.setSelection(0);
 					break;
 				case 3:
 					break;
 				}
 				
 				initialConditions.select(0);
 				
 			}
 		});
 		presets.setItems(new String[] {"Simple", "Front shift", "Front blur", "Implicit upstream only", "\u03BC=0"});
 		presets.setText("<custom>");
 		
 		Group grpInitialConditions = new Group(grpControl, SWT.NONE);
 		grpInitialConditions.setText("Initial conditions");
 		grpInitialConditions.setLayout(new GridLayout(2, false));
 		
 		initialConditions = new Combo(grpInitialConditions, SWT.NONE);
 		initialConditions.setItems(new String[] {"Peak", "Stage left", "Stage right"});
 		initialConditions.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
 		initialConditions.select(0);
 		
 		Label lblWidth = new Label(grpInitialConditions, SWT.NONE);
 		lblWidth.setText("Width");
 		
 		width = new Spinner(grpInitialConditions, SWT.BORDER);
 		width.setSelection(10);
 		width.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
 		
 		Group grpParams = new Group(grpControl, SWT.NONE);
 		grpParams.setText("Params");
 		grpParams.setLayout(new GridLayout(2, false));
 		
 		Label lblNewLabel = new Label(grpParams, SWT.NONE);
 		lblNewLabel.setText("Steps");
 		
 		steps = new Spinner(grpParams, SWT.BORDER);
 		steps.setMaximum(200);
		steps.setSelection(20);
 		GridData gd_steps = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
 		gd_steps.widthHint = 85;
 		steps.setLayoutData(gd_steps);
 		
 		Label lblNodes = new Label(grpParams, SWT.NONE);
 		lblNodes.setText("Nodes");
 		
 		nodes = new Spinner(grpParams, SWT.BORDER);
 		nodes.setMaximum(200);
 		nodes.setSelection(20);
 		nodes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
 		
 		Label label = new Label(grpParams, SWT.NONE);
 		label.setText("\u03F0");
 		
 		kappa = new Spinner(grpParams, SWT.BORDER);
 		kappa.setMaximum(1000);
 		kappa.setSelection(20);
 		kappa.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
 		kappa.setDigits(3);
 		
 		Label label_1 = new Label(grpParams, SWT.NONE);
 		label_1.setText("\u03BC");
 		
 		mu = new Spinner(grpParams, SWT.BORDER);
 		mu.setSelection(1);
 		mu.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
 		mu.setDigits(1);
 		
 		Button btnCalculate = new Button(grpControl, SWT.NONE);
 		btnCalculate.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				solve();
 			}
 		});
 		btnCalculate.setText("Calculate");
 		
 		chart = new Chart(Quality.Advanced, "awt");
 		
 		chart.getAxeLayout().setMainColor(Color.GREEN);
 		chart.getAxeLayout().setXAxeLabel("step");
 		chart.getAxeLayout().setYAxeLabel("x");
 		chart.getAxeLayout().setZAxeLabel("temp");
 		chart.setAxeDisplayed(true);
 		
 		chart.getView().setBackgroundColor(Color.BLACK);
 		
 	    CameraMouseController mouse   = new CameraMouseController();
         
         chart.addController(mouse);
         
         
         CameraThreadController thread = new CameraThreadController();
         mouse.addSlaveThreadController(thread);
         chart.addController(thread);
 
         thread.start();
 		
 		Composite chartComposite = new Composite(this, SWT.EMBEDDED);
 		chartComposite.setLayoutData(BorderLayout.CENTER);
 		java.awt.Frame chartFrame = SWT_AWT.new_Frame(chartComposite);
 		
 		chartFrame.add((Component)chart.getCanvas());		
 		chart.setScale(new Scale(-1, 1));
 
 	}
 
 	private void solve() {
 		int width = this.width.getSelection();
 		int steps = this.steps.getSelection();
 		int nodes = this.steps.getSelection();
 		double kappa = readSpinner(this.kappa);
 		double mu = readSpinner(this.mu);
 		
		System.out.println(kappa + " " + mu);
		
 		Schema schema = schemas[this.schema];
 		double[] startValues = new double[nodes];
 
 		switch (initialConditions.getSelectionIndex()) {
             case 0: // pike
                 for (int i = (nodes - width) / 2;
                      i < (nodes + width) / 2; i++) {
                 	startValues[i] = 1;
                 }
                 break;
             case 1: // stage left
                 for (int i = 0; i < width; i++) {
                 	startValues[i] = 1;
                 }
                 break;
             case 2: // stage right
                 for (int i = nodes - width; i < nodes; i++) {
                 	startValues[i] = 1;
                 }
                 break;
         }
 		
 		final double[][] result = schema.calculate(startValues, kappa, mu, steps);
 		
 		//Coord3d[] coordinates = new Coord3d[result.length * result[0].length];
 		
 		/*for (int i = 0; i < result.length; ++i) {
 			for (int j = 0; j < result[0].length; ++j) {
 				coordinates[i * result[0].length + j] = (new Coord3d(i * 30.0 / result.length, j * 30.0 / result[0].length, result[i][j] / 1e90));
 				//System.out.println(result[i][j]);
 			}
 		}*/
 		
 		if (shape != null) {
 			chart.removeDrawable(shape);
 		}
 		
 		shape = Builder.buildOrthonormal(new OrthonormalGrid(new Range(0, result.length), result.length, new Range(0, result[0].length), result[0].length), new Mapper() {			
 			@Override
 			public double f(double x, double y) {
 				double r = result[Math.min(result.length - 1, (int)x)][Math.min(result[0].length - 1, (int)y)];
 				if (r > 1) {
 					return 1 + r / 1e95;
 				} else if (r < 0) {
 					return -1 + r / 1e95;
 				} else {
 					return r;
 				}
 			}
 		});
 		
 		((Shape)shape).setColorMapper(new ColorMapper(new ColorMapHotCold(), shape.getBounds().getZmin(), shape.getBounds().getZmax()));
 		
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				chart.addDrawable(shape);
 				chart.setScale(new Scale(0, 30));
 				chart.getView().lookToBox(chart.getScene().getGraph().getBounds());
 				chart.getView().shoot();
 			}
 		}).start();
 		
 	}
 
 	@Override
 	protected void checkSubclass() {
 		// Disable the check that prevents subclassing of SWT components
 	}
 	
 	private static double readSpinner(Spinner spinner) {
 		return spinner.getSelection() * 1.0 / Math.pow(10, spinner.getDigits());
 	}
 }
