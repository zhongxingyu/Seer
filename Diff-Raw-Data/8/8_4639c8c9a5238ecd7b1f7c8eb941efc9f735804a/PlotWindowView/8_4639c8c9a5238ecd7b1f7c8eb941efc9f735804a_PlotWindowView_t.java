 package de.ptb.epics.eve.editor.views;
 
 import java.util.Iterator;
 
 import org.csstudio.swt.xygraph.figures.Trace.TraceType;
 import org.csstudio.swt.xygraph.figures.Trace.PointStyle;
 
 import org.eclipse.jface.preference.ColorFieldEditor;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.ExpandBar;
 import org.eclipse.swt.widgets.ExpandItem;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Spinner;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 
 import de.ptb.epics.eve.data.measuringstation.DetectorChannel;
 import de.ptb.epics.eve.data.measuringstation.MotorAxis;
 import de.ptb.epics.eve.data.scandescription.Axis;
 import de.ptb.epics.eve.data.scandescription.Channel;
 import de.ptb.epics.eve.data.scandescription.PlotWindow;
 import de.ptb.epics.eve.data.scandescription.ScanModul;
 import de.ptb.epics.eve.data.scandescription.YAxis;
 import de.ptb.epics.eve.data.PlotModes;
 import de.ptb.epics.eve.editor.Activator;
 
 /**
  * <code>PlotWindowView</code> contains all configuration parts, corresponding 
  * to plots. It shows the current configuration of a 
  * {@link de.ptb.epics.eve.data.scandescription.PlotWindow} set by 
  * {@link #setPlotWindow(PlotWindow)}.
  * 
  * @author ?
  * @author Marcus Michalsky
  */
 public class PlotWindowView extends ViewPart  {
 
 	/**
 	 * The unique identifier of <code>PlotWindowView</code>.
 	 */
 	public static final String ID = 
 			"de.ptb.epics.eve.editor.views.PlotWindowView";
 
 	// the contents of this view
 	private Composite top = null;
 	
 	// the configurations for the three axis will be expandable
 	private ExpandBar bar = null;
 	
 	// "General" configurations, concerning the x axis
 	private Composite xAxisComposite = null;
 	
 	// configurations for the first y axis are in here
 	private Composite yAxis1Composite = null;
 	
 	// configurations for the second y axis are in here
 	private Composite yAxis2Composite = null;
 	
 	// elements for the "general" composite (x axis)
 	
 	// GUI:  Plot Window ID: {1,2,3,...} x
 	private Label plotWindowIDLabel = null;
 	private Spinner plotWindowIDSpinner = null;
 	private Label plotWindowIDErrorLabel = null;
 	
 	// GUI: Motor Axis: "Select-Box":<motor-name>
 	private Label motorAxisLabel = null;
 	private Combo motorAxisComboBox = null;
 	private Label motorAxisErrorLabel = null;
 	
 	// check box indicating whether the plot should be cleared before
 	private Button preInitWindowCheckBox = null;
 	
 	// GUI: Scale Type: "Select-Box":{linear,log} x
 	private Label scaleTypeLabel = null;
 	private Combo scaleTypeComboBox = null;
 	
 	// end of: elements for the "general composite (x axis)
 	
 	// elements for the first y axis composite
 	
 	// GUI: Detector Channel: "Select-Box":<detector-channels> x
 	private Label yAxis1DetectorChannelLabel = null;
 	private Combo yAxis1DetectorChannelComboBox = null;
 	private Label yAxis1DetectorChannelErrorLabel = null;
 	
 	// GUI: Normalize Channel: "Select-Box":<detector-channels> x
 	private Label yAxis1NormalizeChannelLabel = null;
 	private Combo yAxis1NormalizeChannelComboBox = null;
 	private Label yAxis1NormalizeChannelErrorLabel = null;
 	
 	// GUI: Scale Type: "Select-Box":{linear,log} x
 	private Label yAxis1ScaletypeLabel = null;
 	private Combo yAxis1ScaletypeComboBox = null;
 
 	private ColorFieldEditor yAxis1ColorFieldEditor = null;
 	private Combo yAxis1ColorComboBox = null; 
 	
 	private Label yAxis1LinestyleLabel = null;	 	
 	private Combo yAxis1LinestyleComboBox = null; 
 	
 	private Label yAxis1MarkstyleLabel = null;		
 	private Combo yAxis1MarkstyleComboBox = null;	
 	
 	// end of: elements for the first y axis composite
 	
 	// elements for the second y axis composite
 	
 	// GUI: Detector Channel: "Select-Box":<detector-channels> x
 	private Label yAxis2DetectorChannelLabel = null;
 	private Combo yAxis2DetectorChannelComboBox = null;
 	private Label yAxis2DetectorChannelErrorLabel = null;
 	
 	// GUI: Normalize Channel: "Select-Box":<detector-channels> x
 	private Label yAxis2NormalizeChannelLabel = null;
 	private Combo yAxis2NormalizeChannelComboBox = null;
 	private Label yAxis2NormalizeChannelErrorLabel = null;
 	
 	// GUI: Scale Type: "Select-Box":{linear,log} x
 	private Label yAxis2ScaletypeLabel = null;
 	private Combo yAxis2ScaletypeComboBox = null;
 	
 	private ColorFieldEditor yAxis2ColorFieldEditor = null;		
 	private Combo yAxis2ColorComboBox = null;		
 	
 	private Label yAxis2LinestyleLabel = null;		
 	private Combo yAxis2LinestyleComboBox = null;
 	
 	private Label yAxis2MarkstyleLabel = null;		
 	private Combo yAxis2MarkstyleComboBox = null;	
 		
 	// end of: elements for the second y axis composite	
 	
 	private ExpandItem item0;
 
 	private ExpandItem item1;
 
 	private ExpandItem item2;
 
 	// data attributes:
 	// the plot window, which is shown via the fields
 	private PlotWindow plotWindow;
 	
 	// the y axis available (detector channel / data)
 	private YAxis[] yAxis;
 
 	// the scan modules which contains available motors and detector channels
 	// that could be selected via the select boxes
 	private ScanModul scanModul;
 
 	/**
 	 * Initializes the available input fields and puts them in the view.
 	 * The fields are separated in three groups (motor axis, y axis 1, y axis 2).
 	 * <br><br>
 	 * <b>ViewPart:</b><br>
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void createPartControl(final Composite parent) {
 		
 		parent.setLayout(new FillLayout());
 		
 		// if there is no measuring station loaded, show error message
 		if(Activator.getDefault().getMeasuringStation() == null) {
 			final Label errorLabel = new Label(parent, SWT.NONE);
 			errorLabel.setText("No Measuring Station has been loaded. " +
 							   "Please check Preferences!");
 			return;
 		}
 					
 		// initialize the contents composite with a grid layout
 		top = new Composite(parent, SWT.NONE);
 		top.setLayout(new GridLayout());
 	
 		// we have two y axis
 		this.yAxis = new YAxis[2];
 		
 		// initialize the expand bar with a grid layout
 		this.bar = new ExpandBar(this.top, SWT.V_SCROLL);
 		GridData gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.verticalAlignment = GridData.FILL;
 		this.bar.setLayoutData(gridData);
 		
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 3;
 		
 		/*
 		 * initialize contents of the x axis composite
 		 */
 		
 		// the contents of the first expand item gets a grid layout
 		this.xAxisComposite = new Composite(this.bar, SWT.NONE);
 		this.xAxisComposite.setLayout(gridLayout);
 		
 		// GUI: Plot Window ID:
 		this.plotWindowIDLabel = new Label(this.xAxisComposite, SWT.NONE);
 		this.plotWindowIDLabel.setText("Plot Window ID:");
 		
 		this.plotWindowIDSpinner = new Spinner(this.xAxisComposite, SWT.BORDER);
 		gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		this.plotWindowIDSpinner.setLayoutData(gridData);
 		this.plotWindowIDSpinner.setMinimum(1);
 		
 		// ModifyListener for plotWindowID
 		this.plotWindowIDSpinner.addModifyListener(
 				new plotWindowIDSpinnerModifiedListener());
 		
 		// GUI: red x after the spinner if an error occurs
 		this.plotWindowIDErrorLabel = new Label(this.xAxisComposite, SWT.NONE);
 		this.plotWindowIDErrorLabel.setImage(PlatformUI.getWorkbench().
 														 getSharedImages().
 														 getImage( 
 											ISharedImages.IMG_OBJS_WARN_TSK));
 		
 		// GUI: Motor Axis:
 		this.motorAxisLabel = new Label(this.xAxisComposite, SWT.NONE);
 		this.motorAxisLabel.setText("Motor Axis:");
 		
 		// initialization of the motor axis select box
 		this.motorAxisComboBox = new Combo(this.xAxisComposite, SWT.READ_ONLY);
 		gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		this.motorAxisComboBox.setLayoutData(gridData);
 		
 		// FocusListener for motorAxisComboBox
 		this.motorAxisComboBox.addFocusListener(
 				new MotorAxisComboBoxFocusListener());
 		
 		// ModifyListener for motorAxisCombobox
 		this.motorAxisComboBox.addModifyListener(
 				new MotorAxisComboBoxModifiedListener());
 
 		// GUI: red x after the combo box if an error occurs
 		this.motorAxisErrorLabel = new Label(this.xAxisComposite, SWT.NONE);
 		this.motorAxisErrorLabel.setImage(PlatformUI.getWorkbench().
 													 getSharedImages().
 													 getImage( 
 											ISharedImages.IMG_OBJS_WARN_TSK));
 
 		// check box for pre initialization
 		this.preInitWindowCheckBox = new Button(this.xAxisComposite, SWT.CHECK);
 		gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.horizontalSpan = 3;
 		this.preInitWindowCheckBox.setLayoutData(gridData);
 		this.preInitWindowCheckBox.setText("Preinit Window");
 		
 		// SelectionListener for preInitWindowCheckBox
 		this.preInitWindowCheckBox.addSelectionListener(
 				new PreInitWindowCheckBoxSelectionListener());
 		
 		// GUI: Scale Type:
 		this.scaleTypeLabel = new Label(this.xAxisComposite, SWT.NONE);
 		this.scaleTypeLabel.setText("Scale Type:");
 
 		// select box for the scale type
 		this.scaleTypeComboBox = new Combo(this.xAxisComposite, SWT.READ_ONLY);
 		gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		this.scaleTypeComboBox.setLayoutData(gridData);
 		this.scaleTypeComboBox.setItems(PlotModes.valuesAsString());
 		
 		// SelectionListener of scaleTypeComboBox
 		this.scaleTypeComboBox.addSelectionListener(
 				new ScaleTypeComboBoxSelectionListener());
 
 		/*
 		 *  end of: initialize contents of the x axis composite
 		 */
 		
 		// ********************************************************************
 		
 		/*
 		 * initialize contents of the first y axis composite
 		 */
 		
 		// the contents of the configuration for the first y axis gets a grid
 		this.yAxis1Composite = new Composite(this.bar, SWT.NONE);
 		
 		GridLayout yAxis1GridLayout = new GridLayout();
 		yAxis1GridLayout.numColumns = 3;
 		
 		this.yAxis1Composite.setLayout(yAxis1GridLayout);
 		
 		// GUI: Detector Channel:
 		this.yAxis1DetectorChannelLabel = 
 				new Label(this.yAxis1Composite, SWT.NONE);
 		this.yAxis1DetectorChannelLabel.setText("Detector Channel:");
 		
 		// select box for the detector channel
 		this.yAxis1DetectorChannelComboBox = 
 				new Combo(this.yAxis1Composite, SWT.READ_ONLY);
 		gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		this.yAxis1DetectorChannelComboBox.setLayoutData(gridData);
 
 		// FocusListener for yAxis1DetectorChannelComboBox
 		this.yAxis1DetectorChannelComboBox.addFocusListener(
 				new YAxis1DetectorChannelComboBoxFocusListener());
 
 		// SelectionListener for yAxis1DetectorChannelComboBox
 		this.yAxis1DetectorChannelComboBox.addSelectionListener(
 				new YAxis1DetectorChannelComboBoxSelectionListener());
 		
 		// red x after combo box if an error occurs
 		this.yAxis1DetectorChannelErrorLabel = 
 				new Label(this.yAxis1Composite, SWT.NONE);
 		this.yAxis1DetectorChannelErrorLabel.setImage(
 				PlatformUI.getWorkbench().getSharedImages().
 				getImage(ISharedImages.IMG_OBJS_WARN_TSK));
 		
 		// GUI: Normalize Channel:
 		this.yAxis1NormalizeChannelLabel = 
 				new Label(this.yAxis1Composite, SWT.NONE);
 		this.yAxis1NormalizeChannelLabel.setText("Normalize Channel:");
 		
 		// select box for the normalize channel
 		this.yAxis1NormalizeChannelComboBox = 
 				new Combo(this.yAxis1Composite, SWT.READ_ONLY);
 		gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		this.yAxis1NormalizeChannelComboBox.setLayoutData(gridData);
 		this.yAxis1NormalizeChannelComboBox.addFocusListener(
 				new YAxis1NormalizeChannelComboBoxFocusListener());
 		this.yAxis1NormalizeChannelComboBox.addModifyListener(
 				new YAxis1NormalizeChannelComboBoxModifiedListener());
 		
 		// red x after combo box if an error occurs
 		this.yAxis1NormalizeChannelErrorLabel = 
 				new Label(this.yAxis1Composite, SWT.NONE);
 		this.yAxis1NormalizeChannelErrorLabel.isEnabled(); // get rid of warn
 		
 		// GUI: Color:		
 		Label yAxis1ColorLabel = new Label(yAxis1Composite, SWT.NONE);
 		yAxis1ColorLabel.setText("Color:");
 		
 		// wrapper to fix positioning problems with the grid and field editor
 		Composite yAxis1ColorBoxesWrapper = 
 				new Composite(this.yAxis1Composite, SWT.NONE);
 		
 		GridLayout yAxis1ColorBoxesWrapperGridLayout = new GridLayout();
 		yAxis1ColorBoxesWrapperGridLayout.numColumns = 2;
 		yAxis1ColorBoxesWrapper.setLayout(yAxis1ColorBoxesWrapperGridLayout);
 		
 		gridData = new GridData(GridData.FILL_HORIZONTAL);
 		gridData.grabExcessHorizontalSpace = true;
 		yAxis1ColorBoxesWrapper.setLayoutData(gridData);
 		
 		// the first element of the color composite (the select box)
 		this.yAxis1ColorComboBox = 
 			new Combo(yAxis1ColorBoxesWrapper, SWT.READ_ONLY);
 		gridData.grabExcessHorizontalSpace = true;
 		gridData = new GridData(GridData.FILL_HORIZONTAL);
 		this.yAxis1ColorComboBox.setLayoutData(gridData);
 		
 		String[] yAxis1PredefinedColors = 
 			{"black", "red", "green", "blue", "pink", "purple", "custom..."};
 		this.yAxis1ColorComboBox.setItems(yAxis1PredefinedColors);
 
 		// SelectionListener for Color Combo Box
 		this.yAxis1ColorComboBox.addSelectionListener( 
 				new YAxis1ColorComboBoxSelectionListener());
 		
 		Composite yAxis1ColorFieldWrapper = 
 				new Composite(yAxis1ColorBoxesWrapper, SWT.NONE);
 		
 		GridLayout yAxis1ColorFieldWrapperGridLayout = new GridLayout();
 		yAxis1ColorFieldWrapperGridLayout.numColumns = 2;
 		yAxis1ColorFieldWrapper.setLayout(yAxis1ColorFieldWrapperGridLayout);
 		
 		yAxis1ColorFieldEditor = new ColorFieldEditor(
 				"y axis 1 color selector", "", yAxis1ColorFieldWrapper);
 		yAxis1ColorFieldEditor.setPropertyChangeListener(
 				new YAxis1ColorFieldEditorPropertyChangeListener());
 		// end of wrapper composites for color
 		
 		// ensure grid
 		Label colorDummy1 = new Label(yAxis1Composite, SWT.NONE);
 		colorDummy1.getEnabled(); // dummy call to ged rid of eclipse warning
 		
 		// GUI: Linestyle:
 		this.yAxis1LinestyleLabel = new Label(this.yAxis1Composite, SWT.NONE);
 		this.yAxis1LinestyleLabel.setText("Linestyle:");
 		
 		// select box for the line style
 		this.yAxis1LinestyleComboBox = 
 				new Combo(this.yAxis1Composite, SWT.READ_ONLY);
 		gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		this.yAxis1LinestyleComboBox.setLayoutData(gridData);
 		// use the items available in the plot from css
 		this.yAxis1LinestyleComboBox.setItems(TraceType.stringValues()); 
 		this.yAxis1LinestyleComboBox.addSelectionListener(
 				new YAxis1LineStyleComboBoxSelectionListener());
 
 		// ensure grid
 		Label lineStyleDummy1 = new Label(yAxis1Composite, SWT.NONE);
 		lineStyleDummy1.getEnabled(); // dummy call to ged rid of eclipse warning
 		
 		// GUI: Markstyle:
 		this.yAxis1MarkstyleLabel = new Label(this.yAxis1Composite, SWT.NONE);
 		this.yAxis1MarkstyleLabel.setText("Markstyle:");
 		
 		// select box for the mark style
 		this.yAxis1MarkstyleComboBox = 
 				new Combo(this.yAxis1Composite, SWT.READ_ONLY);
 		gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		this.yAxis1MarkstyleComboBox.setLayoutData(gridData);
 		// set items from the plot of CSS
 		this.yAxis1MarkstyleComboBox.setItems(PointStyle.stringValues()); 
 		// selection listener for the combo box
 		this.yAxis1MarkstyleComboBox.addSelectionListener( 
 				new YAxis1MarkStyleComboBoxSelectionListener());
 		
 		// ensure grid
 		Label markStyleDummy1 = new Label(yAxis1Composite, SWT.NONE);
 		markStyleDummy1.getEnabled(); // dummy call to get rid of eclipse warning
 		
 		// GUI: Scaletype:
 		this.yAxis1ScaletypeLabel = new Label(this.yAxis1Composite, SWT.NONE);
 		this.yAxis1ScaletypeLabel.setText("Scaletype:");
 		
 		// select box for the scale type
 		this.yAxis1ScaletypeComboBox = 
 				new Combo(this.yAxis1Composite, SWT.READ_ONLY);
 		gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		this.yAxis1ScaletypeComboBox.setLayoutData(gridData);
 		this.yAxis1ScaletypeComboBox.setItems(PlotModes.valuesAsString());
 		this.yAxis1ScaletypeComboBox.addSelectionListener( 
 				new YAxis1ScaleTypeComboBoxSelectionListener());
 
 		// ensure grid
 		Label scaleTypeDummy1 = new Label(yAxis1Composite, SWT.NONE);
 		scaleTypeDummy1.getEnabled(); // dummy call to ged rid of eclipse warning	
 		
 		/*
 		 * end of: initialize contents of the first y axis composite
 		 */
 		
 		// ********************************************************************
 		
 		/*
 		 * initialize contents of the second y axis composite	
 		 */
 		
 		this.yAxis2Composite = new Composite(this.bar, SWT.NONE);
 		
 		GridLayout yAxis2GridLayout = new GridLayout();
 		yAxis2GridLayout.numColumns = 3;
 		this.yAxis2Composite.setLayout(yAxis2GridLayout);
 		
 		// GUI: Detector Channel:
 		this.yAxis2DetectorChannelLabel = 
 				new Label(this.yAxis2Composite, SWT.NONE);
 		this.yAxis2DetectorChannelLabel.setText("Detector Channel:");
 		
 		this.yAxis2DetectorChannelComboBox = 
 				new Combo(this.yAxis2Composite, SWT.READ_ONLY);
 		gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		this.yAxis2DetectorChannelComboBox.setLayoutData(gridData);
 		// focus listener for the select box
 		this.yAxis2DetectorChannelComboBox.addFocusListener(
 				new YAxis2DetectorChannelComboBoxFocusListener());
 		// selection listener for the select box
 		this.yAxis2DetectorChannelComboBox.addSelectionListener(
 				new YAxis2DetectorChannelComboBoxSelectionListener());
 		
 		// red x if an error occurs
 		this.yAxis2DetectorChannelErrorLabel = 
 				new Label(this.yAxis2Composite, SWT.NONE);
 		this.yAxis2DetectorChannelErrorLabel.setImage(
 				PlatformUI.getWorkbench().getSharedImages().
 				getImage(ISharedImages.IMG_OBJS_WARN_TSK));
 
 		
 		// GUI: Normalize Channel:
 		this.yAxis2NormalizeChannelLabel = 
 				new Label(this.yAxis2Composite, SWT.NONE);
 		this.yAxis2NormalizeChannelLabel.setText("Normalize Channel:");
 		
 		// select box for the normalize channel
 		this.yAxis2NormalizeChannelComboBox = 
 				new Combo(this.yAxis2Composite, SWT.READ_ONLY);
 		gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		this.yAxis2NormalizeChannelComboBox.setLayoutData(gridData);
 		// focus listener for the select box
 		this.yAxis2NormalizeChannelComboBox.addFocusListener(
 				new YAxis2NormalizeChannelComboBoxFocusListener());
 		// modify listener for the select box
 		this.yAxis2NormalizeChannelComboBox.addModifyListener( 
 				new YAxis2NormalizeChannelComboBoxModifiedListener());
 				
 		// red x if an error occurs
 		this.yAxis2NormalizeChannelErrorLabel = 
 				new Label(this.yAxis2Composite, SWT.NONE);
 		this.yAxis2NormalizeChannelErrorLabel.isEnabled(); // get rid of warn
 		
 		// GUI: Color:
 		Label colorLabel = new Label(yAxis2Composite, SWT.NONE);
 		colorLabel.setText("Color:");
 		
 		// color selection stuff in a wrapper to repair layout problems
 		Composite yAxis2ColorBoxesWrapper = new Composite(yAxis2Composite, SWT.NONE);
 		
 		gridData = new GridData(GridData.FILL_HORIZONTAL);
 		gridData.grabExcessHorizontalSpace = true;
 		yAxis2ColorBoxesWrapper.setLayoutData(gridData);
 		
 		GridLayout yAxis2ColorBoxesWrapperGridLayout = new GridLayout();
 		yAxis2ColorBoxesWrapperGridLayout.numColumns = 2;
 		yAxis2ColorBoxesWrapper.setLayout(yAxis2ColorBoxesWrapperGridLayout);
 		
 		this.yAxis2ColorComboBox = new Combo(yAxis2ColorBoxesWrapper, SWT.READ_ONLY);
 		gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		this.yAxis2ColorComboBox.setLayoutData(gridData);
 		
 		String[] yAxis2PredefinedColors = 
 			{"black", "red", "green", "blue", "pink", "purple", "custom..."};
 		this.yAxis2ColorComboBox.setItems(yAxis2PredefinedColors);
 		this.yAxis2ColorComboBox.addSelectionListener( 
 				new YAxis2ColorComboBoxSelectionListener());
 
 		// wrapper to get rid of the grid
 		Composite colorFieldWrapper = new Composite(yAxis2ColorBoxesWrapper, SWT.NONE);
 			
 		GridLayout colorFieldWrapperGridLayout = new GridLayout();
 		colorFieldWrapperGridLayout.numColumns = 2;
 		colorFieldWrapper.setLayout(colorFieldWrapperGridLayout);
 		
 		yAxis2ColorFieldEditor = new ColorFieldEditor(
 				"color selector 2", "", colorFieldWrapper);		
 		yAxis2ColorFieldEditor.setPropertyChangeListener(
 				new YAxis2ColorFieldEditorPropertyChangeListener());
 		
 		// ensure grid
 		Label colorDummy2 = new Label(yAxis2Composite, SWT.NONE);
 		colorDummy2.getEnabled(); // dummy call to get rid of eclipse warning
 		
 		// GUI: Linestyle:
 		this.yAxis2LinestyleLabel = new Label( this.yAxis2Composite, SWT.NONE);
 		this.yAxis2LinestyleLabel.setText("Linestyle:");
 		
 		this.yAxis2LinestyleComboBox = 
 				new Combo(this.yAxis2Composite, SWT.READ_ONLY);
 		gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		this.yAxis2LinestyleComboBox.setLayoutData(gridData);
 		// set line styles to css trace types for the plot
 		this.yAxis2LinestyleComboBox.setItems(TraceType.stringValues()); 
 			
 		this.yAxis2LinestyleComboBox.addSelectionListener( 
 				new YAxis2LineStyleComboBoxSelectionListener());
 			
 		// ensure grid
 		Label lineStyleDummy2 = new Label(yAxis2Composite, SWT.NONE);
 		lineStyleDummy2.getEnabled(); // dummy call to get rid of eclipse warning
 		
 		// GUI: Markstyle:
 		this.yAxis2MarkstyleLabel = new Label(this.yAxis2Composite, SWT.NONE);
 		this.yAxis2MarkstyleLabel.setText("Markstyle:");
 		
 		// select box for the mark styles
 		this.yAxis2MarkstyleComboBox = 
 				new Combo(this.yAxis2Composite, SWT.READ_ONLY);
 		gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		this.yAxis2MarkstyleComboBox.setLayoutData(gridData);
 		// use css plot point styles as mark styles
 		this.yAxis2MarkstyleComboBox.setItems(PointStyle.stringValues()); 
 		this.yAxis2MarkstyleComboBox.addSelectionListener( 
 				new YAxis2MarkStyleComboBoxSelectionListener());
 
 		// ensure grid
 		Label markStyleDummy2 = new Label(yAxis2Composite, SWT.NONE);
 		markStyleDummy2.getEnabled(); // dummy call to get rid of eclipse warning
 	
 		// GUI: Scalestype:
 		this.yAxis2ScaletypeLabel = new Label(this.yAxis2Composite, SWT.NONE);
 		this.yAxis2ScaletypeLabel.setText("Scaletype:");
 	
 		// select box for the scale type
 		this.yAxis2ScaletypeComboBox = 
 				new Combo(this.yAxis2Composite, SWT.READ_ONLY);
 		gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		this.yAxis2ScaletypeComboBox.setLayoutData(gridData);
 		this.yAxis2ScaletypeComboBox.setItems(PlotModes.valuesAsString());
 		this.yAxis2ScaletypeComboBox.addSelectionListener( 
 				new YAxis2ScaleTypeComboBoxSelectionListener());
 
 		// ensure grid
 		Label scaleTypeDummy2 = new Label(yAxis2Composite, SWT.NONE);
 		scaleTypeDummy2.getEnabled(); // dummy call to get rid of eclipse warning
 
 		/*
 		 * end of: initialize contents of the second y axis composite
 		 */
 		
 		this.item0 = new ExpandItem(this.bar, SWT.NONE, 0);
 		item0.setText("General");
 		item0.setHeight(
 				this.xAxisComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
 		item0.setControl(this.xAxisComposite);
 		item0.setExpanded(true);
 		
 		this.item1 = new ExpandItem(this.bar, SWT.NONE, 0);
 		item1.setText("Y-Axis 1");
 		item1.setHeight(
 				this.yAxis1Composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
 		item1.setControl(this.yAxis1Composite);
 		
 		this.item2 = new ExpandItem(this.bar, SWT.NONE, 0);
 		item2.setText("Y-Axis 2");
 		item2.setHeight(
 				this.yAxis2Composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
 		item2.setControl(this.yAxis2Composite);
 		
 		setPlotWindow(null);
 	} 
 	
 	// ************************************************************************
 	// ********************** end of createPartControl ************************
 	// ************************************************************************
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setFocus() {
 	}
 
 	/**
 	 * Returns the select box for the motor axis.
 	 * 
 	 * @return the select box for the motor axis
 	 */
 	public Combo getMotorAxisComboBox() {
 		return this.motorAxisComboBox;
 	}
 
 	/**
 	 * Returns the select box for the detector channel of the first y axis.
 	 * 
 	 * @return the select box for the detector channel of the first y axis
 	 */
 	public Combo getyAxis1DetectorChannelComboBox() {
 		return this.yAxis1DetectorChannelComboBox;
 	}
 
 	/**
 	 * Returns the select box for the detector channel of the second y axis.
 	 * 
 	 * @return the select box for the detector channel of the second y axis
 	 */
 	public Combo getyAxis2DetectorChannelComboBox() {
 		return this.yAxis2DetectorChannelComboBox;
 	}
 
 	/**
 	 * Returns the select box for the normalize channel of the first y axis.
 	 * 
 	 * @return the select box for the normalize channel of the first y axis
 	 */
 	public Combo getyAxis1NormalizeChannelComboBox() {
 		return this.yAxis1NormalizeChannelComboBox;
 	}
 
 	/**
 	 * Returns the select box for the normalize channel of the second y axis.
 	 * 
 	 * @return the select box for the normalize channel of the second y axis
 	 */
 	public Combo getyAxis2NormalizeChannelComboBox() {
 		return this.yAxis2NormalizeChannelComboBox;
 	}
 	
 	/**
 	 * Returns the <code>PlotWindow</code>.
 	 * 
 	 * @return the <code>PlotWindow</code>
 	 */
 	public PlotWindow getPlotWindow() {
 		return this.plotWindow;
 	}
 	
 	/**
 	 * Sets the <code>PlotWindow</code>.
 	 * 
 	 * @param plotWindow the <code>PlotWindow</code> that should be set.
 	 */
 	public void setPlotWindow(final PlotWindow plotWindow) {
 		
 		if(this.plotWindow != null && plotWindow != null) {
 			
 			if(this.plotWindow.equals(plotWindow)) {
 				// "new" and existing plot are the same->refresh existing window
 				
 				// decide what to do by checking for currently present y axes
 				switch (this.plotWindow.getYAxisAmount()) {
 					
 					case 0: // no y axes currently present, so initialize them
 						this.plotWindow.clearYAxis();
 						this.yAxis[0] = null;
 						this.yAxis[1] = null;
 						break;
 					case 1: // one axis present, is it the first or the second?
 						for(Iterator<YAxis> ityAxis = 
 							plotWindow.getYAxisIterator(); 
 							ityAxis.hasNext();) 
 						{
 							YAxis yAxis = ityAxis.next();
 							
 							if((this.yAxis[0] != null) && 
 									yAxis.equals(this.yAxis[0])) 
 							{ // detector channel 1 is still the same, clear 2nd
 								if (this.yAxis[1] != null) {
 									this.plotWindow.removeYAxis(this.yAxis[1]);
 									this.yAxis[1] = null;
 								}
 							}
 							else if((this.yAxis[1] != null) && 
 									  yAxis.equals(this.yAxis[1])) 
 							{
 								// detector channel 2 is still the same...
 								if (this.yAxis[0] != null) {
 									// remove both and add the 2nd (so that it 
 									// becomes the 1st)
 									this.plotWindow.clearYAxis();
 									this.plotWindow.addYAxis(this.yAxis[1]);
 									this.yAxis[1] = null;
 								}
 							}
 						}
 						break;
 				} // end of switch
 			}
 			else { // "new" plot is really new -> 
 				this.plotWindow.clearYAxis();
 				if(this.yAxis[0] != null) {
 					this.plotWindow.addYAxis(this.yAxis[0]);
 					this.yAxis[0] = null;
 				}
 				if(this.yAxis[1] != null) {
 					this.plotWindow.addYAxis(this.yAxis[1]);
 					this.yAxis[1] = null;
 				}
 			}
 		}
 		
 		// set given plot window as own plot window
 		this.plotWindow = plotWindow;
 
 		if(this.plotWindow != null) {
 			
 			// show controls
 			this.top.setVisible(true);
 			
 			// update contents of the view according to new plot window
 						
 			// index for the y axes
 			int i = 0;
 			
 			for(Iterator<YAxis> it = 
 					this.plotWindow.getYAxisIterator(); it.hasNext(); ++i) {
 				this.yAxis[i] = it.next();
 			}
 
 			// General
 			this.plotWindowIDErrorLabel.setImage(null);
 			
 			this.plotWindowIDSpinner.setSelection(this.plotWindow.getId());
 			this.plotWindowIDSpinner.setEnabled(true);
 			this.motorAxisComboBox.setText(
 					this.plotWindow.getXAxis() != null
 					? plotWindow.getXAxis().getFullIdentifyer()
 					: "");
 			this.motorAxisComboBox.setEnabled(true);
 			this.preInitWindowCheckBox.setSelection(this.plotWindow.isInit());
 			this.preInitWindowCheckBox.setEnabled(true);
 			this.scaleTypeComboBox.setText(
 					PlotModes.modeToString(this.plotWindow.getMode()));
 			this.scaleTypeComboBox.setEnabled(true);
 			
 			// ***************************************************************
 			// ************************ yAxis1 *******************************
 			// ***************************************************************
 			
 			if(this.yAxis[0] != null) {
 				// y axis 1 is present->insert values and enable fields
 				item1.setExpanded(true);
 				this.yAxis1DetectorChannelComboBox.setText(
 						yAxis[0].getDetectorChannel().getFullIdentifyer());
 				this.yAxis1DetectorChannelComboBox.setEnabled(true);
 				
 				if (yAxis[0].getNormalizeChannel() != null)
 						this.yAxis1NormalizeChannelComboBox.setText( 
 							yAxis[0].getNormalizeChannel().getFullIdentifyer());
 				else 
 						this.yAxis1NormalizeChannelComboBox.setText("none");
 				yAxis1NormalizeChannelComboBox.setEnabled(true);
 				
 				// plot related fields...
 				updateColorsAxis(0);
 				yAxis1ColorComboBox.setEnabled(true);
 				yAxis1ColorFieldEditor.getColorSelector().setEnabled(true);
 				
 				yAxis1LinestyleComboBox.setText(
 						yAxis[0].getLinestyle().toString());
 				yAxis1LinestyleComboBox.setEnabled(true);
 				yAxis1MarkstyleComboBox.setText(
 						yAxis[0].getMarkstyle().toString());
 				yAxis1MarkstyleComboBox.setEnabled(true);
 				
 				yAxis1ScaletypeComboBox.setText(
 						PlotModes.modeToString(yAxis[0].getMode()));
 				yAxis1ScaletypeComboBox.setEnabled(true);	
 			} else {
 				// no y axis 1->disable fields
 				item1.setExpanded(false);
 				this.yAxis1DetectorChannelComboBox.setText("none");		
 				//yAxis2DetectorChannelComboBox.setEnabled(false);
 				// Wenn yAxis[0] = 0, muß nachgesehen werden, ob die weiteren
 				// Einstellungen noch zurückgesetzt werden müssen!
 				yAxis1NormalizeChannelComboBox.setEnabled(false);
 				yAxis1ColorComboBox.setEnabled(false);
 				yAxis1ColorFieldEditor.getColorSelector().setEnabled(false);
 				yAxis1LinestyleComboBox.setEnabled(false);
 				yAxis1MarkstyleComboBox.setEnabled(false);
 				yAxis1ScaletypeComboBox.setEnabled(false);
 			}
 			
 			// ***************************************************************
 			// *********************** end of: yAxis1 ************************
 			// ***************************************************************
 			
 			// ***************************************************************
 			// *********************** yAxis2 ********************************
 			// ***************************************************************
 			
 			if(this.yAxis[1] != null) {
 				// y axis 2 is present->insert values and enable fields
 				item2.setExpanded(true);
 				this.yAxis2DetectorChannelComboBox.setText(
 						yAxis[1].getDetectorChannel().getFullIdentifyer());
 				this.yAxis2DetectorChannelComboBox.setEnabled(true);
 				
 				if (yAxis[1].getNormalizeChannel() != null)
 					this.yAxis2NormalizeChannelComboBox.setText(
 							yAxis[1].getNormalizeChannel().getFullIdentifyer());
 				else
 					this.yAxis2NormalizeChannelComboBox.setText("none");
 				yAxis2NormalizeChannelComboBox.setEnabled(true);
 				
 				// plot related fields...
 				updateColorsAxis(1);
 				yAxis2ColorComboBox.setEnabled(true);
 				yAxis2ColorFieldEditor.getColorSelector().setEnabled(true);
 				yAxis2LinestyleComboBox.setText(
 						yAxis[1].getLinestyle().toString());
 				yAxis2LinestyleComboBox.setEnabled(true);
 				yAxis2MarkstyleComboBox.setText(	
 						yAxis[1].getMarkstyle().toString());
 				yAxis2MarkstyleComboBox.setEnabled(true);
 				yAxis2ScaletypeComboBox.setText(
 						PlotModes.modeToString(yAxis[1].getMode()));
 				yAxis2ScaletypeComboBox.setEnabled(true);			
 			}  else {
 				// no y axis 2->disable fields
 				item2.setExpanded(false);
 				this.yAxis2DetectorChannelComboBox.setText("none");
 				//this.yAxis2DetectorChannelComboBox.setEnabled(false);
 				// Wenn yAxis[1] = 0, muß nachgesehen werden, ob die weiteren
 				// Einstellungen noch zurückgesetzt werden müssen!
 				yAxis2NormalizeChannelComboBox.setEnabled(false);
 				yAxis2ColorComboBox.setEnabled(false);
 				yAxis2ColorFieldEditor.getColorSelector().setEnabled(false);
 				yAxis2LinestyleComboBox.setEnabled(false);
 				yAxis2MarkstyleComboBox.setEnabled(false);
 				yAxis2ScaletypeComboBox.setEnabled(false);
 			}
 			
 			// ***************************************************************
 			// *********************** end of: yAxis2 ************************
 			// ***************************************************************
 
 			if ((yAxis[0] == null) && (yAxis[1] == null)) {
 				yAxis1DetectorChannelErrorLabel.setImage( 
 						PlatformUI.getWorkbench().getSharedImages().
 						getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
 				yAxis1DetectorChannelErrorLabel.setToolTipText(
 						"At least one y axis has to be set!");
 				yAxis2DetectorChannelErrorLabel.setImage(
 						PlatformUI.getWorkbench().getSharedImages().
 						getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
 				yAxis2DetectorChannelErrorLabel.setToolTipText(
 						"At least one y axis has to be set!");
 			}
 			else
 			{
 				yAxis1DetectorChannelErrorLabel.setImage(null);
 				yAxis1DetectorChannelErrorLabel.setToolTipText(null);
 				yAxis2DetectorChannelErrorLabel.setImage(null);
 				yAxis2DetectorChannelErrorLabel.setToolTipText(null);
 			}
 			
 			yAxis1DetectorChannelComboBox.setEnabled(true);
 			yAxis2DetectorChannelComboBox.setEnabled(true);
 		} else {
 			// this.plotWindow == null (no plot selected)
 			
 			// hide controls
 			this.top.setVisible(false);			
 			
 			this.yAxis[0] = null;
 			this.yAxis[1] = null;
 			
 			this.plotWindowIDSpinner.setSelection(0);
 			this.plotWindowIDSpinner.setEnabled(false);
 			this.motorAxisComboBox.setEnabled(false);
 			this.preInitWindowCheckBox.setSelection(false);
 			this.preInitWindowCheckBox.setEnabled(false);
 			this.scaleTypeComboBox.setEnabled(false);
 			
 			this.yAxis1DetectorChannelComboBox.setEnabled(false);
 			this.yAxis1NormalizeChannelComboBox.setEnabled(false);
 			this.yAxis1ColorComboBox.setEnabled(false);
 			this.yAxis1ColorFieldEditor.getColorSelector().setEnabled(false);
 			this.yAxis1LinestyleComboBox.setEnabled(false);
 			this.yAxis1MarkstyleComboBox.setEnabled(false);
 			this.yAxis1ScaletypeComboBox.setEnabled(false);
 			this.yAxis2DetectorChannelComboBox.setEnabled(false);
 			this.yAxis2NormalizeChannelComboBox.setEnabled(false);
 			this.yAxis2ColorComboBox.setEnabled(false);
 			this.yAxis2ColorFieldEditor.getColorSelector().setEnabled(false);
 			this.yAxis2LinestyleComboBox.setEnabled(false);
 			this.yAxis2MarkstyleComboBox.setEnabled(false);
 			this.yAxis2ScaletypeComboBox.setEnabled(false);
 		}
 	}
 
 	/**
 	 * Sets the <code>ScanModul</code> of the <code>PlotWindowView</code>, and 
 	 * updates the select boxes to ensure that only available axis and detectors 
 	 * can be selected.
 	 * 
 	 * @param scanModul the <code>ScanModul</code> that should be set.
 	 */
 	public void setScanModul(ScanModul scanModul) {
 
 		if(scanModul != null) {
 			// Es werden nur die Achsen erlaubt die in diesem ScanModul 
 			// verwendet werden.
 			Axis[] cur_axis = scanModul.getAxis();
 			String[] cur_axis_feld = new String[cur_axis.length];
 			for (int i=0; i<cur_axis.length; ++i) {
 				cur_axis_feld[i] = cur_axis[i].getMotorAxis().getFullIdentifyer();
 			}		
 			this.motorAxisComboBox.setItems(cur_axis_feld);
 			this.motorAxisComboBox.add("", 0);	
 		
 			// Es werden nur die Channels erlaubt die in diesem ScanModul 
 			// verwendet werden.
 			Channel[] cur_channel = scanModul.getChannels();
 			String[] cur_ch_feld = new String[cur_channel.length];
 			for (int i=0; i<cur_channel.length; ++i) {
 				cur_ch_feld[i] = cur_channel[i].getDetectorChannel().
 												getFullIdentifyer();
 			}
 			this.yAxis1DetectorChannelComboBox.setItems(cur_ch_feld);
 			this.yAxis1DetectorChannelComboBox.add("none", 0);	
 			this.yAxis1NormalizeChannelComboBox.setItems(cur_ch_feld);
 			this.yAxis1NormalizeChannelComboBox.add("none", 0);	
 			this.yAxis2DetectorChannelComboBox.setItems(cur_ch_feld);
 			this.yAxis2DetectorChannelComboBox.add("none", 0);	
 			this.yAxis2NormalizeChannelComboBox.setItems(cur_ch_feld);
 			this.yAxis2NormalizeChannelComboBox.add("none", 0);	
 		}
 		this.scanModul = scanModul;
 	}
 	
 	/*
 	 * called by setPlotWindow() and listeners to update the color specific 
 	 * widgets of given axis.
 	 */
 	private void updateColorsAxis(int axis)
 	{
 		RGB model_rgb = new RGB(0,0,0);
 		
 		if(axis==0) 
 		{
 			model_rgb = yAxis[0].getColor();
 		}
 		else
 		{
 			for(Iterator<YAxis> ityAxis = 
 					plotWindow.getYAxisIterator(); ityAxis.hasNext();) {
 				YAxis yAxis = ityAxis.next();
 
 				model_rgb = yAxis.getColor();
 			}
 		}
 		
 		
 		// our predefined colors
 		String[] items = {"black", "red", "green", "blue", 
 						  "pink", "purple", "custom..."};
 		
 		if(axis==0) yAxis1ColorComboBox.setItems(items); 
 		if(axis==1) yAxis2ColorComboBox.setItems(items); 
 		
 		// our predefined colors as RGB values...
 		RGB black = new RGB(0,0,0);
 		RGB red = new RGB(255,0,0);
 		RGB green = new RGB(0,128,0);
 		RGB blue = new RGB(0,0,255);
 		RGB pink = new RGB(255,0,255);
 		RGB purple = new RGB(128,0,128);
 		
 		if(model_rgb == null) model_rgb = black;
 		
 		// if the current RGB value equals one of the predefined colors, update 
 		// select box accordingly
 		if(model_rgb.equals(black)) {
 			if(axis==0) yAxis1ColorComboBox.select(0);
 			if(axis==1) yAxis2ColorComboBox.select(0);
 		} else if(model_rgb.equals(red)) {
 			if(axis==0) yAxis1ColorComboBox.select(1);
 			if(axis==1) yAxis2ColorComboBox.select(1);
 		} else if(model_rgb.equals(green)) {
 			if(axis==0) yAxis1ColorComboBox.select(2);
 			if(axis==1) yAxis2ColorComboBox.select(2);
 		} else if(model_rgb.equals(blue)) {
 			if(axis==0) yAxis1ColorComboBox.select(3);
 			if(axis==1) yAxis2ColorComboBox.select(3);
 		} else if(model_rgb.equals(pink)) {
 			if(axis==0) yAxis1ColorComboBox.select(4);
 			if(axis==1) yAxis2ColorComboBox.select(4);
 		} else if(model_rgb.equals(purple)) {
 			if(axis==0) yAxis1ColorComboBox.select(5);
 			if(axis==1) yAxis2ColorComboBox.select(5);
 		} else { // custom...
 			if(axis==0) yAxis1ColorComboBox.select(6);
 			if(axis==1) yAxis2ColorComboBox.select(6);
 		}	
 		
 		// change color of color field editor to current RGB value
 		if(axis==0) 
 			yAxis1ColorFieldEditor.getColorSelector().setColorValue(model_rgb);
 		if(axis==1)
 			yAxis2ColorFieldEditor.getColorSelector().setColorValue(model_rgb);
 	}
 	
 	/*
 	 * called by listeners to update the color field of given axis to match 
 	 * the color selected in the select box.
 	 */
 	private void updateColorField(int axis)
 	{
 		String selected_color_as_text = "";
 		if(axis==0) selected_color_as_text = yAxis1ColorComboBox.getText();
 		if(axis==1) selected_color_as_text = yAxis2ColorComboBox.getText();
 		
 		RGB selected_color = null;
 
 		if(selected_color_as_text == "black") selected_color = new RGB(0,0,0);
 		if(selected_color_as_text == "red" )  selected_color = new RGB(255,0,0);
 		if(selected_color_as_text == "green") selected_color = new RGB(0,128,0);
 		if(selected_color_as_text == "blue")  selected_color = new RGB(0,0,255);
 		if(selected_color_as_text == "pink")  selected_color = new RGB(255,0,255);
 		if(selected_color_as_text == "purple")selected_color = new RGB(128,0,128);
 		if(selected_color_as_text == "custom...")
 		{
 			if(axis==0)	selected_color = 
 				yAxis1ColorFieldEditor.getColorSelector().getColorValue();
 			if(axis==1) selected_color = 
 				yAxis2ColorFieldEditor.getColorSelector().getColorValue();
 		}
 		// just in case...
 		if(selected_color == null) selected_color = new RGB(0,0,0);
 		
 		if(axis==0) yAxis1ColorFieldEditor.getColorSelector().
 											setColorValue(selected_color);
 		if(axis==1) yAxis2ColorFieldEditor.getColorSelector().
 											setColorValue(selected_color);
 	}
 	
 	// ************************************************************************
 	// ************************************************************************
 	// ********************** inner classes for listeners *********************
 	// ************************************************************************
 	// ************************************************************************
 	
 	// Every time something gets changed by the user, it must be "saved" within
 	// the model as well. Therefore Listeners listen to the different entry 
 	// fields.
 	
 	/**
 	 * Modify Listener of <code>plotWindowIDSpinner</code>.
 	 */
 	class plotWindowIDSpinnerModifiedListener implements ModifyListener {	
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void modifyText(ModifyEvent e) {
 			
 			if(plotWindow != null) {
 				// read id from spinner box
 				int newId = plotWindowIDSpinner.getSelection();
 				
 				// get all currently present plot windows
 				PlotWindow[] plotWindows = scanModul.getPlotWindows();
 				
 				// set model id only if it does not already exist
 				boolean setId = true;
 				for (int j=0; j<plotWindows.length; ++j) {	
 					if (newId == plotWindows[j].getId()) {
 						// id does exist -> do not set
 						setId = false; 
 						
 						if (plotWindow.equals(plotWindows[j])) {
 							// id is the one from the plot window itself -> OK
 							// -> show no error
 							plotWindowIDErrorLabel.setImage(null);
 							plotWindowIDErrorLabel.setToolTipText(null);
 						} else {
 							// id already exists -> show error
 							plotWindowIDErrorLabel.setImage( 
 									PlatformUI.getWorkbench().
 											   getSharedImages().
 											   getImage(
 										ISharedImages.IMG_OBJS_ERROR_TSK));
 							plotWindowIDErrorLabel.setToolTipText(
 									"Id could not be changed: it already exists!");
 						}
 					}
 				}
 				
 				// if id does not exist, try to set it
 				if (setId) {
 					try {
 						plotWindow.setId(plotWindowIDSpinner.getSelection());
 						plotWindowIDErrorLabel.setImage(null);
 						plotWindowIDErrorLabel.setToolTipText(null);
 					}
 					catch(final IllegalArgumentException ex) {
 						plotWindowIDErrorLabel.setImage(
 								PlatformUI.getWorkbench().
 										   getSharedImages().
 										   getImage( 
 										ISharedImages.IMG_OBJS_ERROR_TSK));
 						plotWindowIDErrorLabel.setToolTipText(
 														ex.getMessage());
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * <code>FocusListener</code> of <code>motorAxisComboBox</code>.
 	 */
 	class MotorAxisComboBoxFocusListener implements FocusListener {
 		
 		/**
 		 * {@inheritDoc}<br>
 		 * Sets the contents of the select box so that only motor axes 
 		 * used in the scan description are available.
 		 */
 		@Override
 		public void focusGained(FocusEvent e) {
 
 			// get motor axes of the scan module
 			Axis[] cur_axis = scanModul.getAxis();
 			
 			// get current selection of the combo box
 			String aktText = motorAxisComboBox.getText();
 			
 			// get identifiers of all available motor axes
 			String[] cur_feld = new String[cur_axis.length];
 			for (int i=0; i<cur_axis.length; ++i) {
 				cur_feld[i] = cur_axis[i].getMotorAxis().getFullIdentifyer();
 			}
 			// insert names of axes into the combo box
 			motorAxisComboBox.setItems(cur_feld);
 			if (motorAxisComboBox.getItemCount() == 0) {
 				// if there are no entries, create an empty one
 				motorAxisComboBox.add("");
 			}
 			// restore previously selected item
 			motorAxisComboBox.setText(aktText);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void focusLost(FocusEvent e) {
 		}
 	}
 	
 	/**
 	 * <code>ModifyListener</code> of <code>motorAxisComboBox</code>.
 	 */
 	class MotorAxisComboBoxModifiedListener implements ModifyListener {
 		
 		/**
 		 * {@inheritDoc}<br>
 		 * Updates the data model according to the selected motor axis.
 		 */
 		@Override
 		public void modifyText(final ModifyEvent e) {
 			
 			if(motorAxisComboBox.getText().equals("")) {
 				// there is no motor axis selected
 				if (plotWindow != null) 
 				   plotWindow.setXAxis(null); 
 				
 				// a plot without a x axis is senseless -> show error
 				motorAxisErrorLabel.setImage(PlatformUI.getWorkbench().
 														 getSharedImages().
 								getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
 				motorAxisErrorLabel.setToolTipText(
 											"The xAxis must not be empty.");
 			}
 			else {
 				// motor axis is selected (not "") -> set it
 				plotWindow.setXAxis((MotorAxis)Activator.getDefault().
 													getMeasuringStation().
 										getAbstractDeviceByFullIdentifyer(
 											motorAxisComboBox.getText()));
 				// disable error label
 				motorAxisErrorLabel.setImage(null);
 				motorAxisErrorLabel.setToolTipText(null);
 			}
 		}
 	}
 	
 	/**
 	 * <code>SelectionListener</code> of <code>preInitWindowCheckBox</code>.
 	 */
 	class PreInitWindowCheckBoxSelectionListener implements SelectionListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetDefaultSelected(final SelectionEvent e) {
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetSelected(final SelectionEvent e) {
 			// set whether the plot should be initialized or not
 			plotWindow.setInit(preInitWindowCheckBox.getSelection());
 		}
 	}
 	
 	/**
 	 * <code>SelectionListener</code> of <code>scaleTypeComboBox</code>.
 	 */
 	class ScaleTypeComboBoxSelectionListener implements SelectionListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetDefaultSelected(final SelectionEvent e) {
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetSelected(final SelectionEvent e) {
 			// set the plot mode selected
 			plotWindow.setMode(
 					PlotModes.stringToMode(scaleTypeComboBox.getText()));
 		}
 	}
 	
 	// ************************************************************************
 	// *************************** y axis 1 listener **************************
 	// ************************************************************************	
 	
 	/**
 	 * <code>FocusListener</code> of <code>yAxis1DetectorChannelComboBox</code>
 	 */
 	class YAxis1DetectorChannelComboBoxFocusListener implements FocusListener {
 		
 		/**
 		 * {@inheritDoc}<br>
 		 * Sets the contents of the select box so that only detector channels 
 		 * used in the scan description are available.
 		 */
 		@Override
 		public void focusGained(FocusEvent e) {
 			// get channels available in the scan module
 			Channel[] cur_channel = scanModul.getChannels();
 			
 			// save current element of select box			
 			String aktText = yAxis1DetectorChannelComboBox.getText();
 			
 			// get names of available detectors
 			String[] cur_feld = new String[cur_channel.length];
 			for (int i=0; i<cur_channel.length; ++i) {
 				cur_feld[i] = cur_channel[i].getDetectorChannel().
 											 getFullIdentifyer();
 			}
 			
 			// insert elements in select box + "none"
 			yAxis1DetectorChannelComboBox.setItems(cur_feld);
 			yAxis1DetectorChannelComboBox.add("none", 0);
 			
 			// set previously saved element as selected
 			yAxis1DetectorChannelComboBox.setText(aktText);
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void focusLost(FocusEvent e) {
 		}
 	}
 	
 	/**
 	 * <code>SelectionListener</code> of 
 	 * <code>yAxis1DetectorChannelComboBox</code>.
 	 */
 	class YAxis1DetectorChannelComboBoxSelectionListener 
 										implements SelectionListener {
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetDefaultSelected(SelectionEvent e) {
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			
 			// if there is "none" selected or ""...
 			if(yAxis1DetectorChannelComboBox.getText().equals("") || 
 			   yAxis1DetectorChannelComboBox.getText().equals("none")) {
 				if(yAxis[0] != null) {
 					// ... remove the y axis from the plot (if there is any)
 					plotWindow.removeYAxis(yAxis[0]);
 					yAxis[0] = null;
 				}
 				// reset/disable the normalize and scale type select boxes
 				// and the plot related select boxes
 				yAxis1NormalizeChannelComboBox.setText("");
 				yAxis1NormalizeChannelComboBox.setEnabled(false);
 				yAxis1ColorComboBox.setText("");
 				yAxis1ColorComboBox.setEnabled(false);
 				yAxis1ColorFieldEditor.getColorSelector().
 												setColorValue(new RGB(0,0,0));
 				yAxis1ColorFieldEditor.getColorSelector().setEnabled(false);
 				yAxis1LinestyleComboBox.setText("");
 				yAxis1LinestyleComboBox.setEnabled(false);
 				yAxis1MarkstyleComboBox.setText("");
 				yAxis1MarkstyleComboBox.setEnabled(false);
 				yAxis1ScaletypeComboBox.setText("");
 				yAxis1ScaletypeComboBox.setEnabled(false);
 				
 				// first y axis is "none". if the second is too, show error
 				if (yAxis[1] == null) {
 					yAxis1DetectorChannelErrorLabel.setImage( 
 							PlatformUI.getWorkbench().getSharedImages().
 							getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
 					yAxis1DetectorChannelErrorLabel.setToolTipText(
 							"At least one y axis has to be set!");
 					yAxis2DetectorChannelErrorLabel.setImage(
 							PlatformUI.getWorkbench().getSharedImages().
 							getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
 					yAxis2DetectorChannelErrorLabel.setToolTipText(
 							"At least one y axis has to be set!");
 				}
 			} else {
 				// first y axis is NOT "" or "none":
 				
 				// if y axis doesn't exist, create it
 				if(yAxis[0] == null) {
 					yAxis[0] = new YAxis();
 					plotWindow.addYAxis(yAxis[0]);
 				
 					// default values for color, line style and mark style
 					yAxis[0].setColor(new RGB(0,0,255));
 					yAxis[0].setLinestyle(TraceType.SOLID_LINE);
 					yAxis[0].setMarkstyle(PointStyle.NONE);				
 				}
 				
 				// update/enable GUI elements
 				if(yAxis[0].getNormalizeChannel() == null)
 					yAxis1NormalizeChannelComboBox.setText("none");
 				else	
 					yAxis1NormalizeChannelComboBox.setText(
 							yAxis[0].getNormalizeChannel().getFullIdentifyer());
 				yAxis1NormalizeChannelComboBox.setEnabled(true);
 				
 				updateColorsAxis(0);
 				yAxis1ColorComboBox.setEnabled(true);		
 				yAxis1ColorFieldEditor.getColorSelector().setEnabled(true);
 				
 				yAxis1LinestyleComboBox.setText(
 						yAxis[0].getLinestyle().toString());
 				yAxis1LinestyleComboBox.setEnabled(true);
 								
 				yAxis1MarkstyleComboBox.setText(
 						yAxis[0].getMarkstyle().toString());
 				yAxis1MarkstyleComboBox.setEnabled(true);
 								
 				yAxis1ScaletypeComboBox.setText(
 						PlotModes.modeToString(yAxis[0].getMode()));
 				yAxis1ScaletypeComboBox.setEnabled(true);
 						
 				yAxis[0].setDetectorChannel((DetectorChannel)Activator.
 						getDefault().getMeasuringStation().
 						getAbstractDeviceByFullIdentifyer(
 								yAxis1DetectorChannelComboBox.getText()));
 
 				// disable errors (at least one y axis is set)
 				yAxis1DetectorChannelErrorLabel.setImage(null);
 				yAxis1DetectorChannelErrorLabel.setToolTipText(null);
 				yAxis2DetectorChannelErrorLabel.setImage(null);
 				yAxis2DetectorChannelErrorLabel.setToolTipText(null);
 			}
 		}
 	}
 	
 	/**
 	 * <code>FocusListener</code> of <code>yAxis1NormalizeChannelComboBox</code>.
 	 */
 	class YAxis1NormalizeChannelComboBoxFocusListener 
 										implements FocusListener {
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void focusGained(FocusEvent e) {
 			// only channels available in the scan module should be selectable
 			Channel[] cur_channel = scanModul.getChannels();
 			String[] cur_feld = new String[cur_channel.length];
 			
 			// save current contents of the select box
 			String aktText = yAxis1NormalizeChannelComboBox.getText();
 			
 			// get detector names and put them in the select box
 			for (int i=0; i<cur_channel.length; ++i) {
 				cur_feld[i] = 
 					cur_channel[i].getDetectorChannel().getFullIdentifyer();
 			}
			yAxis1NormalizeChannelComboBox.setItems(cur_feld);
 			
 			// add a "none" item
			yAxis1NormalizeChannelComboBox.add("none", 0);
 			
 			// restore previously selected item
			yAxis1NormalizeChannelComboBox.setText(aktText);	
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void focusLost(FocusEvent e) {
 		}	
 	}
 	
 	/**
 	 * <code>SelectionListener</code> of 
 	 * <code>yAxis1NormalizechannelComboBox</code>.
 	 */
 	class YAxis1NormalizeChannelComboBoxModifiedListener
 										implements ModifyListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void modifyText(ModifyEvent e) {
 			if(yAxis1NormalizeChannelComboBox.getText().equals("")) {
 				// nothing
 			} else if(yAxis1NormalizeChannelComboBox.getText().equals("none")) {
 				if (yAxis[0] != null)
 					yAxis[0].clearNormalizeChannel();
 			} else {
 				yAxis[0].setNormalizeChannel(
 						(DetectorChannel)Activator.getDefault().
 						                  getMeasuringStation().
 					        getAbstractDeviceByFullIdentifyer(
 					        		yAxis1NormalizeChannelComboBox.getText()));
 			}
 		}		
 	}
 	
 	/**
 	 * <code>SelectionListener</code> of <code>yAxis1ColorComboBox</code>.
 	 */
 	class YAxis1ColorComboBoxSelectionListener implements SelectionListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetDefaultSelected(final SelectionEvent e) {
 		}
 
 		/**
 		 * {@inheritDoc}<br>
 		 * Sets the selected color in the model and shows it in the 
 		 * adjacent color field editor. 
 		 */
 		@Override
 		public void widgetSelected(final SelectionEvent e) {
 				// set the color in the color field to match the selection
 				updateColorField(0);
 				// save the color to the model
 				yAxis[0].setColor(yAxis1ColorFieldEditor.getColorSelector().
 														 getColorValue());
 				if(yAxis1ColorComboBox.getText().equals("custom..."))
 				{
 					yAxis1ColorFieldEditor.getColorSelector().open();
 				}
 		}
 	}
 	
 	/**
 	 * <code>PropertyChangeListener</code> of <code>colorFieldEditor</code>.
 	 */
 	class YAxis1ColorFieldEditorPropertyChangeListener 
 							implements IPropertyChangeListener {
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void propertyChange(PropertyChangeEvent event) {
 			yAxis1ColorComboBox.select(6);
 			yAxis[0].setColor(
 					yAxis1ColorFieldEditor.getColorSelector().getColorValue());
 		}		
 	}
 	
 	/**
 	 * <code>SelectionListener</code> of <code>yAxis1LineStyleComboBox</code>.
 	 */
 	class YAxis1LineStyleComboBoxSelectionListener 
 											implements SelectionListener {	
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetDefaultSelected(final SelectionEvent e) {		
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetSelected(final SelectionEvent e) {
 			if(yAxis[0] != null) {
 			TraceType[] tracetypes = TraceType.values();
 			for(int i=0;i<tracetypes.length;i++)
 			{
 				if(tracetypes[i].toString().equals(
 						yAxis1LinestyleComboBox.getText()))
 				{
 					yAxis[0].setLinestyle(tracetypes[i]);
 				}
 			}
 			}
 		}
 	}
 	
 	/**
 	 * <code>SelectionListener</code> of <code>yAxis1MarkStyleComboBox</code>.
 	 */
 	class YAxis1MarkStyleComboBoxSelectionListener 
 											implements SelectionListener {
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetDefaultSelected(final SelectionEvent e) {
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetSelected(final SelectionEvent e) {
 			PointStyle[] markstyles = PointStyle.values();
 			for(int i=0;i<markstyles.length;i++)
 			{
 				if(markstyles[i].toString().equals(
 						yAxis1MarkstyleComboBox.getText()))
 				{
 					yAxis[0].setMarkstyle(markstyles[i]);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * <code>SelectionListener</code> of <code>yAxis1ScaleTypeComboBox</code>.
 	 */
 	class YAxis1ScaleTypeComboBoxSelectionListener
 											implements SelectionListener {
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetDefaultSelected(final SelectionEvent e) {
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetSelected(final SelectionEvent e) {
 				yAxis[0].setMode(
 					PlotModes.stringToMode(yAxis1ScaletypeComboBox.getText()));
 		}
 	}
 	
 	// ************************************************************************
 	// ************************************************************************
 	// ********************* Listener of axis 2 *******************************
 	// ************************************************************************
 	// ************************************************************************
 	
 	/**
 	 * <code>FocusListener</code> of <code>yAxis2DetectorChannelComboBox</code>.
 	 */
 	class YAxis2DetectorChannelComboBoxFocusListener implements FocusListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void focusGained(FocusEvent e) {
 			// get channels available in the scan module
 			Channel[] cur_channel = scanModul.getChannels();
 			
 			// save current element of select box
 			String aktText = yAxis2DetectorChannelComboBox.getText();
 			
 			// get names of available detectors
 			String[] cur_feld = new String[cur_channel.length];
 			for (int i=0; i<cur_channel.length; ++i) {
 				cur_feld[i] = cur_channel[i].getDetectorChannel().
 											 getFullIdentifyer();
 			}
 			
 			// insert elements in select box + "none"
 			yAxis2DetectorChannelComboBox.setItems(cur_feld);
 			yAxis2DetectorChannelComboBox.add("none", 0);
 			
 			// set previously saved element as selected
 			yAxis2DetectorChannelComboBox.setText(aktText);	
 		}		
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void focusLost(FocusEvent e) {
 		}
 	}
 	
 	/**
 	 * <code>SelectionListener</code> of 
 	 * <code>yAxis2DetectorChannelComboBox</code>.
 	 */
 	class YAxis2DetectorChannelComboBoxSelectionListener 
 											implements SelectionListener {
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetDefaultSelected(SelectionEvent e) {
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			
 			if(yAxis2DetectorChannelComboBox.getText().equals("") ||
 			   yAxis2DetectorChannelComboBox.getText().equals("none")) {
 				if(yAxis[1] != null) {
 					plotWindow.removeYAxis(yAxis[1]);
 					yAxis[1] = null;
 				}
 				
 				// reset/disable the normalize and scale type select boxes
 				// and the plot related select boxes
 				yAxis2NormalizeChannelComboBox.setText("");
 				yAxis2NormalizeChannelComboBox.setEnabled(false);
 				yAxis2ColorComboBox.setText("");
 				yAxis2ColorComboBox.setEnabled(false);
 				yAxis2ColorFieldEditor.getColorSelector().
 									   setColorValue(new RGB(0,0,0));
 				yAxis2ColorFieldEditor.getColorSelector().setEnabled(false);
 				yAxis2LinestyleComboBox.setText("");
 				yAxis2LinestyleComboBox.setEnabled(false);
 				yAxis2MarkstyleComboBox.setText("");
 				yAxis2MarkstyleComboBox.setEnabled(false);
 				yAxis2ScaletypeComboBox.setText("");
 				yAxis2ScaletypeComboBox.setEnabled(false);
 			
 				if (yAxis[0] == null) {
 					// no axes selected->show error
 					yAxis1DetectorChannelErrorLabel.setImage( 
 							PlatformUI.getWorkbench().getSharedImages().
 								getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
 					yAxis1DetectorChannelErrorLabel.setToolTipText(
 							"At least one y axis has to be set!");
 					yAxis2DetectorChannelErrorLabel.setImage(
 							PlatformUI.getWorkbench().getSharedImages().
 								getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
 					yAxis2DetectorChannelErrorLabel.setToolTipText(
 							"At least one y axis has to be set!");
 				}
 			} else {
 				// select box has value other than "" or "none"...
 				if(yAxis[1] == null) {
 					// a detector channel was selected, but none present yet
 					yAxis[1] = new YAxis();
 					plotWindow.addYAxis(yAxis[1]);
 
 					// default values for color, line style and mark style
 					yAxis[1].setColor(new RGB(0,128,0));
 					yAxis[1].setLinestyle(TraceType.DASH_LINE);
 					yAxis[1].setMarkstyle(PointStyle.NONE);
 				}
 
 				// update/enable GUI elements
 				if(yAxis[1].getNormalizeChannel() == null)
 					yAxis2NormalizeChannelComboBox.setText("none");
 				else 
 					yAxis2NormalizeChannelComboBox.setText(
 							yAxis[1].getNormalizeChannel().getFullIdentifyer());
 				yAxis2NormalizeChannelComboBox.setEnabled(true);
 				
 				updateColorsAxis(1);
 				yAxis2ColorComboBox.setEnabled(true);
 				yAxis2ColorFieldEditor.getColorSelector().setEnabled(true);
 				yAxis2LinestyleComboBox.setText(
 						yAxis[1].getLinestyle().toString());
 				yAxis2LinestyleComboBox.setEnabled(true);
 				yAxis2MarkstyleComboBox.setText(
 						yAxis[1].getMarkstyle().toString());
 				yAxis2MarkstyleComboBox.setEnabled(true);
 				yAxis2ScaletypeComboBox.setText(
 						PlotModes.modeToString( yAxis[1].getMode()));
 				yAxis2ScaletypeComboBox.setEnabled(true);
 						
 				yAxis[1].setDetectorChannel((DetectorChannel)
 						Activator.getDefault().getMeasuringStation().
 							getAbstractDeviceByFullIdentifyer(
 									yAxis2DetectorChannelComboBox.getText()));
 				
 				// disable errors (at least one y axis is set)
 				yAxis1DetectorChannelErrorLabel.setImage(null);
 				yAxis1DetectorChannelErrorLabel.setToolTipText(null);
 				yAxis2DetectorChannelErrorLabel.setImage(null);
 				yAxis2DetectorChannelErrorLabel.setToolTipText(null);
 			}
 		}
 	}
 	
 	/**
 	 * <code>FocusListener</code> of <code>yAxis2NormalizeChannelComboBox</code>.
 	 */
 	class YAxis2NormalizeChannelComboBoxFocusListener implements FocusListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void focusGained(FocusEvent e) {
 			// get channels available in the scan module
 			Channel[] cur_channel = scanModul.getChannels();
 			
 			// save current entry
 			String aktText = yAxis2NormalizeChannelComboBox.getText();
 			
 			// get names and insert them into the select box, add "none"
 			String[] cur_feld = new String[cur_channel.length];
 			for (int i=0; i<cur_channel.length; ++i) {
 				cur_feld[i] = cur_channel[i].getDetectorChannel().
 											 getFullIdentifyer();
 			}
 			yAxis2NormalizeChannelComboBox.setItems(cur_feld);
 			yAxis2NormalizeChannelComboBox.add("none", 0);
 			
 			// set to saved entry
 			yAxis2NormalizeChannelComboBox.setText(aktText);
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void focusLost(FocusEvent e) {
 		}
 	}
 	
 	/**
 	 * <code>ModifyListener</code> of 
 	 * <code>yAxis2NormalizeChannelComboBox</code>.
 	 */
 	class YAxis2NormalizeChannelComboBoxModifiedListener 
 												implements ModifyListener {
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void modifyText(final ModifyEvent e) {
 			if(yAxis2NormalizeChannelComboBox.getText().equals("")) {
 				// nothing
 			} else if(yAxis2NormalizeChannelComboBox.getText().equals("none")) {
 				if(yAxis[1] != null)
 					yAxis[1].clearNormalizeChannel();
 			} else {
 				yAxis[1].setNormalizeChannel((DetectorChannel)
 						Activator.getDefault().getMeasuringStation().
 							getAbstractDeviceByFullIdentifyer(
 									yAxis2NormalizeChannelComboBox.getText()));
 			}
 		}
 	}
 	
 	/**
 	 * <code>SelectionListener</code> of <code>yAxis2ColorComboBox</code>.
 	 */
 	class YAxis2ColorComboBoxSelectionListener implements SelectionListener {
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetDefaultSelected(final SelectionEvent e) {
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetSelected(final SelectionEvent e) {
 			// set the color in the color field to match the selection
 			updateColorField(1);
 			// save the color to the model
 			yAxis[1].setColor(yAxis2ColorFieldEditor.getColorSelector().
 													 getColorValue());
 			if(yAxis2ColorComboBox.getText().equals("custom..."))
 			{
 				yAxis2ColorFieldEditor.getColorSelector().open();
 			}
 		}
 	}
 	
 	/**
 	 * <code>PropertyChangeListener</code> of 
 	 * <code>yAxis2ColorFieldEditor</code>.
 	 */
 	class YAxis2ColorFieldEditorPropertyChangeListener
 										implements IPropertyChangeListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void propertyChange(PropertyChangeEvent event) {
 			yAxis2ColorComboBox.select(6);
 			yAxis[1].setColor(
 					yAxis2ColorFieldEditor.getColorSelector().getColorValue());
 		}
 	}
 	
 	/**
 	 * <code>SelectionListener</code> of <code>yAxis2LineStyleComboBox</code>. 
 	 */
 	class YAxis2LineStyleComboBoxSelectionListener 
 											implements SelectionListener {	
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetDefaultSelected(final SelectionEvent e) {
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetSelected(final SelectionEvent e) {
 			TraceType[] tracetypes = TraceType.values();
 			for(int i=0;i<tracetypes.length;i++)
 			{
 				if(tracetypes[i].toString().equals(
 						yAxis2LinestyleComboBox.getText()))
 				{
 					yAxis[1].setLinestyle(tracetypes[i]);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * <code>SelectionListener</code> of <code>yAxis2MarkStyleComboBox</code>.
 	 */
 	class YAxis2MarkStyleComboBoxSelectionListener 
 											implements SelectionListener {
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetDefaultSelected(final SelectionEvent e) {
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetSelected(final SelectionEvent e) {
 			PointStyle[] markstyles = PointStyle.values();
 			for(int i=0;i<markstyles.length;i++)
 			{
 				if(markstyles[i].toString().equals(
 						yAxis2MarkstyleComboBox.getText()))
 				{
 					yAxis[1].setMarkstyle(markstyles[i]);
 				}
 			}	
 		}
 	}
 
 	/**
 	 * <code>SelectionListener</code> of <code>yAxis2ScaleTypeComboBox</code>.
 	 */
 	class YAxis2ScaleTypeComboBoxSelectionListener
 											implements SelectionListener {
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetDefaultSelected(final SelectionEvent e) {
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetSelected(final SelectionEvent e) {
 			if(yAxis[1] != null) {
 				yAxis[1].setMode(PlotModes.stringToMode(
 						yAxis2ScaletypeComboBox.getText()));
 			}
 		}
 	}
 }
