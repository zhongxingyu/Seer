 package de.ptb.epics.eve.editor.views.motoraxisview;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 
 import de.ptb.epics.eve.data.PluginTypes;
 import de.ptb.epics.eve.data.measuringstation.PlugIn;
 import de.ptb.epics.eve.data.scandescription.Axis;
 import de.ptb.epics.eve.data.scandescription.PluginController;
 import de.ptb.epics.eve.data.scandescription.ScanModule;
 import de.ptb.epics.eve.data.scandescription.errors.AxisError;
 import de.ptb.epics.eve.data.scandescription.errors.IModelError;
 import de.ptb.epics.eve.editor.Activator;
 
 /**
  * <code>MotorAxisPluginComposite</code> is a composite to input plug in values
  * of the motor axis.
  * 
  * @author Hartmut Scherr
  * @author Marcus Michalsky
  */
 public class PluginComposite extends Composite {
 	
 	// logging 
 	private static Logger logger = 
 		Logger.getLogger(PluginComposite.class.getName());
 	
 	private Label pluginLabel;
 	private Combo pluginCombo;
 	private PluginComboSelectionListener pluginComboSelectionListener;
 	private Label pluginErrorLabel;
 	private PluginControllerComposite pluginControllerComposite;
 	
 	
 	private Axis axis;
 	private ScanModule scanModule;
 	
 	private MotorAxisView motorAxisView;
 	
 	/**
 	 * Constructs a <code>MotorAxisPluginComposite</code>.
 	 * 
 	 * @param parent the parent composite
 	 * @param style the style
 	 * @param parentView the view the composite is contained in
 	 */
 	public PluginComposite(final Composite parent, final int style,
 									final MotorAxisView parentView) {
 		super(parent, style);
 				
 		motorAxisView = parentView;
 		
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 3;
 		this.setLayout(gridLayout);
 		
 		this.pluginLabel = new Label(this, SWT.NONE);
 		
 		this.pluginLabel.setText("Plug-In:");
 		
 		this.pluginCombo = new Combo(this, SWT.READ_ONLY);
 		
 		PlugIn[] plugins = Activator.getDefault().getMeasuringStation().
 									 getPlugins().toArray(new PlugIn[0]);
 		List<String> pluginNames = new ArrayList<String>();
 		for(int i = 0; i < plugins.length; ++i) {
 			if(plugins[i].getType() == PluginTypes.POSITION) {
 				pluginNames.add(plugins[i].getName());
 			}
 		}
 		this.pluginCombo.setItems(pluginNames.toArray(new String[0]));
 		GridData gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		this.pluginCombo.setLayoutData(gridData);
 		pluginComboSelectionListener = new PluginComboSelectionListener();
 		this.pluginCombo.addSelectionListener(pluginComboSelectionListener);
 		
 		this.pluginErrorLabel = new Label(this, SWT.NONE);
 		this.pluginErrorLabel.setImage(PlatformUI.getWorkbench().
 							  getSharedImages().getImage(
 							  ISharedImages.IMG_OBJS_ERROR_TSK));
 		
 		this.pluginControllerComposite = 
 				new PluginControllerComposite(this, SWT.NONE, motorAxisView);
 		gridData = new GridData();
 		gridData.horizontalSpan = 3;
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		this.pluginControllerComposite.setLayoutData(gridData);
 		
 		this.pluginCombo.setEnabled(false);
 		this.pluginControllerComposite.setEnabled(false);
 	}
 
 	/**
 	 * calculate the height to see all entries of this composite
 	 * @return the needed height of Composite to see all entries
 	 */
 	public int getTargetHeight() {
 		return (pluginControllerComposite.getBounds().y + 
 				pluginControllerComposite.getBounds().height + 5);
 	}
 
 	/**
 	 * calculate the width to see all entries of this composite
 	 * @return the needed width of Composite to see all entries
 	 */
 	public int getTargetWidth() {
 		return (pluginControllerComposite.getBounds().x + 
 				pluginControllerComposite.getBounds().width + 5);
 	}
 
 	/**
 	 * 
 	 * @param axis
 	 * @param scanModule
 	 */
 	public void setAxis(final Axis axis, final ScanModule scanModule) {
 		
 		removeListeners();
 		
 		this.axis = axis;
 		this.scanModule = scanModule;
 		
 		if(this.axis != null) {
 			if(this.axis.getPositionPluginController() != null &&
 			   this.axis.getPositionPluginController().getPlugin() != null) {
 				this.pluginCombo.setText(
 					this.axis.getPositionPluginController().getPlugin().getName());
 				checkForErrors();
 			} else {
				this.pluginCombo.setText("");
 			}
 			this.pluginControllerComposite.setPluginController(
 					axis.getPositionPluginController());
 			pluginControllerComposite.setScanModule(scanModule);
 			
 			checkForErrors();
 			
 			this.pluginCombo.setEnabled(true);
 			this.pluginControllerComposite.setEnabled(true);
 		} else { // axis == null -> reset all
			this.pluginCombo.setText("");
 			this.pluginControllerComposite.setPluginController(null);
 			this.pluginCombo.setEnabled(false);
 			this.pluginControllerComposite.setEnabled(false);
 		}
 		addListeners();
 	}
 	
 	/*
 	 * 
 	 */
 	private void checkForErrors()
 	{
 		this.pluginErrorLabel.setImage(null);
 		this.pluginErrorLabel.setToolTipText("");
 		
 		for(IModelError error : axis.getModelErrors()) {
 			if(error instanceof AxisError) {
 				final AxisError axisError = (AxisError)error;
 				
 				switch(axisError.getErrorType()) {
 					case PLUGIN_NOT_SET:
 						this.pluginErrorLabel.setImage(PlatformUI.
 								getWorkbench().getSharedImages().getImage(
 								ISharedImages.IMG_OBJS_ERROR_TSK));
 						this.pluginErrorLabel.setToolTipText("Plugin not set");
 						this.pluginErrorLabel.getParent().layout();
 						break;
 					case PLUGIN_ERROR:
 						this.pluginErrorLabel.setImage(PlatformUI.
 								getWorkbench().getSharedImages().getImage(
 								ISharedImages.IMG_OBJS_ERROR_TSK));
 						this.pluginErrorLabel.setToolTipText("Plugin error");
 						this.pluginErrorLabel.getParent().layout();
 						break;
 				}
 			}
 		}
 	}
 	
 	/*
 	 * 
 	 */
 	private void addListeners() {
 		pluginCombo.addSelectionListener(pluginComboSelectionListener);
 	}
 	
 	/*
 	 * 
 	 */
 	private void removeListeners() {
 		pluginCombo.removeSelectionListener(pluginComboSelectionListener);
 	}
 	
 	/* ********************************************************************* */
 	/* **************************** Listeners ****************************** */
 	/* ********************************************************************* */
 
 	/**
 	 * {@link org.eclipse.swt.events.SelectionListener}<code> of 
 	 * <code>pluginCombo</code>.
 	 */
 	class PluginComboSelectionListener implements SelectionListener {
 
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
 			motorAxisView.suspendModelUpdateListener();
 			
 			if(axis != null) { 
 				PlugIn plugin = Activator.getDefault().getMeasuringStation().
 									getPluginByName(pluginCombo.getText());
 				
 				if(plugin != null)
 					logger.debug("Plugin: " + plugin.getName());
 				else
 					logger.debug("Plugin: null");
 				
 				
 				if(axis.getPositionPluginController() == null)
 				{
 					axis.setPositionPluginController(new PluginController(plugin));
 					logger.debug("filled with default ? " + 
 							axis.getPositionPluginController().isFilledWithDefault());
 				}
 				
 				
 				if(axis.getPositionPluginController().getPlugin() != plugin) {
 					axis.getPositionPluginController().setPlugin(plugin);
 				}
 					pluginControllerComposite.setPluginController(
 							axis.getPositionPluginController());
 					pluginControllerComposite.setScanModule(scanModule);
 			}
 			checkForErrors();
 			motorAxisView.resumeModelUpdateListener();
 		}
 	}
 }
