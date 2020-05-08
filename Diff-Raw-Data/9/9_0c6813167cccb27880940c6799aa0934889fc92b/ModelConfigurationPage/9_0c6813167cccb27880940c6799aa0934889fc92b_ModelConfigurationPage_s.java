 package partitioner.views;
 
 import java.awt.Frame;
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.SwingUtilities;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.ScrolledForm;
 import org.eclipse.ui.forms.widgets.Section;
 import org.eclipse.ui.forms.widgets.TableWrapData;
 import org.eclipse.ui.forms.widgets.TableWrapLayout;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.FrameworkUtil;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.event.Event;
 import org.osgi.service.event.EventAdmin;
 
 import partitioner.models.PartitionerGUIStateModel;
 import plugin.Constants;
 import snapshots.controller.ControllerDelegate;
 import snapshots.views.SnapshotView;
 import snapshots.views.VirtualModelFileInput;
 import ca.ubc.magic.profiler.dist.model.execution.ExecutionFactory.ExecutionCostType;
 import ca.ubc.magic.profiler.dist.model.interaction.InteractionFactory;
 import ca.ubc.magic.profiler.dist.model.interaction.InteractionFactory.InteractionCostType;
 import ca.ubc.magic.profiler.dist.transform.ModuleCoarsenerFactory;
 import ca.ubc.magic.profiler.dist.transform.ModuleCoarsenerFactory.ModuleCoarsenerType;
 import ca.ubc.magic.profiler.partitioning.control.alg.PartitionerFactory;
 import ca.ubc.magic.profiler.partitioning.control.alg.PartitionerFactory.PartitionerType;
 import ca.ubc.magic.profiler.partitioning.view.VisualizePartitioning;
 
 public class
 ModelConfigurationPage
 extends ScrolledForm
 // the following code follows the example provide in 
 // http://www.eclipse.org/articles/Article-Forms/article.html
 // it is my first stab at working with eclipse forms
 {
 	private Section actions_composite;
 	
 	private Label 	profiler_trace_text;
 	
 	private PartitionerGUIStateModel partitioner_gui_state_model;
 	private Text module_exposer_text;
 	private Text host_config_text;
 	
 	private Button mod_exposer_browse_button;
 	private Button host_config_browse;
 	
 	private Button exposure_button;
 	private Button synthetic_node_button;
 	private Button perform_partitioning_button;
 
 	private ControllerDelegate controller;
 	
 	private Combo set_coarsener_combo;
 	
 	private Combo partitioning_algorithm_combo;
 	private Combo interaction_model_combo;
 	private Combo execution_model_combo;
 
 	private Object current_vp_lock;
 
 	private VisualizePartitioning currentVP;
 
 	private Frame frame;
 
 	private ModelCreationEditor model_creation_editor;
 	
 	public void
 	setProfilerTracePath
 	( String path )
 	{
 		this.profiler_trace_text.setText( path );
 	}
 	
 	ModelConfigurationPage
 	( 	Composite parent, 
 		FormToolkit toolkit, 
 		PartitionerGUIStateModel partitioner_gui_state_model, 
 		ControllerDelegate controller,
 		VisualizePartitioning currentVP, 
 		Object current_vp_lock,
 		Frame frame, 
 		ModelCreationEditor model_creation_editor)
 	{
 		super(parent);
 		
 		this.partitioner_gui_state_model
 			= partitioner_gui_state_model;
 		this.controller
 			= controller;
 		
 		this.current_vp_lock
 			= current_vp_lock;
 		
 		this.frame
 			= frame;
 		
 		this.currentVP
 			= currentVP;
 		
 		this.setText(
 			"Configure and Create a Model"
 		);
 		this.model_creation_editor
 			= model_creation_editor;
 		
 		TableWrapLayout layout 
 			= new TableWrapLayout();
 		layout.numColumns = 1;
 		this
 			.getBody().setLayout(layout);
 		
 		Section set_paths_composite
 			= toolkit.createSection(
 				this.getBody(),
 				Section.TITLE_BAR 
 					| Section.EXPANDED 
 					| Section.DESCRIPTION 
 					| Section.TWISTIE
 			);
 		set_paths_composite.setText("Set the File Paths");
 		set_paths_composite.setDescription(
 			"Set the files from which the model shall be built."
 		);
 		
 		Composite set_paths_client
 			= toolkit.createComposite(set_paths_composite);
 		this.initializeSetPathsBarGrid(set_paths_client);
 		this.initializeSetPathsBarWidgets( set_paths_client, toolkit );
 		
 		TableWrapData td 
 			= new TableWrapData(TableWrapData.FILL);
 		set_paths_composite.setLayoutData(td);
 		
 		set_paths_composite.setClient(set_paths_client);
 		
 		Section configure_composite
 			= toolkit.createSection(
 				this.getBody(),
 				Section.TITLE_BAR 
 					|Section.EXPANDED 
 					| Section.DESCRIPTION 
 					| Section.TWISTIE
 			);
 		configure_composite.setText("Configure");
 		configure_composite.setDescription(
 			"Configure the settings for the model."
 		);
 		
 		Composite configure_client
 			= toolkit.createComposite(configure_composite);
 		this.initializeConfigurationGrid(configure_client);
 		this.initializeConfigurationWidgets( configure_client, toolkit );
 		td 
 			= new TableWrapData(TableWrapData.FILL);
 		configure_composite.setLayoutData(td);
 		configure_composite.setClient(configure_client);
 		
 		Section partitioning_composite
 			= toolkit.createSection(
 				this.getBody(),
 				Section.TITLE_BAR 
 					|Section.EXPANDED 
 					| Section.DESCRIPTION 
 					| Section.TWISTIE
 			);
 		partitioning_composite.setText("Partition");
 		partitioning_composite.setDescription(
 			"Configure the cost model and the partitioning algorithm."
 		);
 		
 		Composite partitioning_client
 			= toolkit.createComposite(partitioning_composite);
 		this.initializePartitioningGrid(partitioning_client);
 		this.initializePartitioningWidgets( partitioning_client, toolkit );
 		td 
 			= new TableWrapData(TableWrapData.FILL);
 		partitioning_composite.setLayoutData(td);
 		partitioning_composite.setClient(partitioning_client);
 		
 		this.actions_composite
 			= toolkit.createSection(
 				this.getBody(),
 				Section.TITLE_BAR 
 					| Section.EXPANDED 
 					| Section.DESCRIPTION
 					| Section.TWISTIE
 			);
 		actions_composite.setText("Activate");
 		actions_composite.setDescription(
 			"Activate the model"
 		);
 		
 		Composite actions_client
 			= toolkit.createComposite(actions_composite);
 		td 
 			= new TableWrapData(TableWrapData.FILL);
 		actions_composite.setLayoutData(td);
 		actions_composite.setClient(actions_client);	
 		
 		this.initializeActionsGrid(actions_client);
 		this.initializeActionsWidgets(actions_client, toolkit);
 		
 		this.set_partitioning_widgets_enabled(false);
 		
 		/*
 		final Button preset_module_graph_button
 			= toolkit.createButton(parent, "Preset Module Graph", SWT.CHECK);
 		grid_data 
 			= new GridData(SWT.BEGINNING, SWT.FILL, false, false);
 		grid_data.horizontalSpan = 2;
 		synthetic_node_button.setLayoutData( grid_data );
 		
 		preset_module_graph_button.addSelectionListener(
 			new SelectionAdapter()
 			{
 				@Override
 				public void
 				widgetSelected
 				( SelectionEvent e )
 				{
 					ModelCreationEditor.this.controller.setModelProperty(
 						Constants.GUI_SET_PRESET_MODULE_GRAPH,
 						new Boolean(
 							preset_module_graph_button.getSelection()
 						)
 					);
 				}
 			}
 		);*/
 	}
 	
 	private void 
 	initializeSetPathsBarGrid
 	(Composite parent) 
 	{
 		final GridLayout model_configuration_page_grid_layout
 			= new GridLayout();
 		model_configuration_page_grid_layout.numColumns = 3;
 		parent.setLayout( model_configuration_page_grid_layout );
 	}
 	
 	private void 
 	initializeSetPathsBarWidgets
 	( Composite parent, FormToolkit toolkit  ) 
 	{
 		toolkit.createLabel(parent, "Profiler Trace XML: " );
 		
 		this.profiler_trace_text 
 			= toolkit.createLabel( 
 				parent, 
 				this.partitioner_gui_state_model.getProfilerTracePath()
 			);
 		toolkit.createLabel(parent, "");
 		
 		toolkit.createLabel(parent, "Mod Exposer XML: " );
 		this.module_exposer_text 
 			= toolkit.createText(parent, "");
 		this.module_exposer_text.setEditable( true );
 		this.module_exposer_text.setSize( 
 			150, 
 			this.module_exposer_text.getSize().y
 		);
 		GridData grid_data 
 			= new GridData( SWT.FILL, SWT.FILL, true, false, 1, 1);
 		
 		grid_data.grabExcessHorizontalSpace = true;
 		// hack: will need to fix
 		grid_data.widthHint = 600;
 		this.module_exposer_text.setLayoutData(grid_data);
 		
 		mod_exposer_browse_button 
 			= toolkit.createButton(parent, "Browse", SWT.PUSH);
 		mod_exposer_browse_button.addSelectionListener( 
 			new SelectionAdapter(){
 				public void widgetSelected
 				( SelectionEvent event )
 				{
 					FileDialog file_dialog 
 						= new FileDialog( 
 							PlatformUI.getWorkbench()
 								.getActiveWorkbenchWindow().getShell(), 
 							SWT.OPEN
 						);
 					file_dialog.setText("Select File");
 					file_dialog.setFilterPath( 
 						ModelConfigurationPage.this
 							.profiler_trace_text.getText() 
 					);
 					String selected
 						= file_dialog.open();
 					if(selected != null){
 						ModelConfigurationPage.this
 							.module_exposer_text.setText(selected);
 					}
 				}
 			}
 		);
 		
 		toolkit.createLabel(parent, "Host Config. XML: " );
 		this.host_config_text
 			=  toolkit.createText(parent,"");
 		host_config_text.setEditable( true );
 		grid_data 
 			= new GridData( SWT.FILL, SWT.FILL, true, false, 1, 1);
 		
 		grid_data.grabExcessHorizontalSpace = true;
 		// hack: will need to fix
 		grid_data.widthHint = 600;
 		host_config_text.setLayoutData(grid_data);
 		
 		this.host_config_browse 
 			= toolkit.createButton(parent, "Browse", SWT.PUSH);
 		
 		host_config_browse.addSelectionListener( new SelectionAdapter(){
 			public void widgetSelected( SelectionEvent event ){
 				FileDialog file_dialog 
 					= new FileDialog( 
 						PlatformUI.getWorkbench()
 							.getActiveWorkbenchWindow().getShell(), 
 						SWT.OPEN
 					);
 				file_dialog.setText("Select File");
 				file_dialog.setFilterPath( profiler_trace_text.getText() );
 				String selected
 					= file_dialog.open();
 				if(selected != null){
 					host_config_text.setText(selected);
 				}
 			}
 		});
 	}
 	
 	private void 
 	initializeConfigurationGrid
 	( Composite parent ) 
 	{
 		final GridLayout model_configuration_page_grid_layout
 			= new GridLayout();
 		model_configuration_page_grid_layout.numColumns 
 			= 2;
 		parent.setLayout( model_configuration_page_grid_layout );
 	}
 	
 	private void 
 	initializeConfigurationWidgets
 	( Composite parent, FormToolkit toolkit ) 
 	{
 		toolkit.createLabel(parent, "Coarsener: " );
 
 		this.initialize_coarsener_combo_box(parent);
 		
 		this.exposure_button
 			= toolkit.createButton(
 				parent, 
 				"Activate Module Exposure", 
 				SWT.CHECK
 			);
 		GridData grid_data 
 			= new GridData( SWT.BEGINNING, SWT.FILL, false, false );
 		grid_data.horizontalSpan = 1;
 		exposure_button.setLayoutData(grid_data);
 		
 		exposure_button.addSelectionListener(
 			new SelectionAdapter()
 			{
 				@Override
 				public void
 				widgetSelected
 				( SelectionEvent e )
 				{
 					ModelConfigurationPage.this.controller.setModelProperty(
 						Constants.GUI_SET_MODULE_EXPOSURE, 
 						new Boolean(exposure_button.getSelection())
 					);
 				}
 			}
 		);
 		
 		this.createDummyLabel(parent, toolkit);
 		
 		this.synthetic_node_button
 			= toolkit.createButton(parent, "Add Synthetic Node", SWT.CHECK);
 		grid_data 
 			= new GridData(SWT.BEGINNING, SWT.FILL, false, false);
 		grid_data.horizontalSpan = 1;
 		synthetic_node_button.setLayoutData( grid_data );
 		
 		synthetic_node_button.addSelectionListener(
 			new SelectionAdapter()
 			{
 				@Override
 				public void
 				widgetSelected
 				( SelectionEvent e )
 				{
 					ModelConfigurationPage.this.controller.setModelProperty(
 						Constants.GUI_SET_SYNTHETIC_NODE,
 						new Boolean(synthetic_node_button.getSelection())
 					);
 				}
 			}
 		);
 	}
 	
 	private void
 	initialize_coarsener_combo_box
 	( Composite parent ) 
 	{
 		this.set_coarsener_combo
 			= new Combo(parent, SWT.NONE);
 		
         for( final ModuleCoarsenerType mcType 
         		: ModuleCoarsenerFactory.ModuleCoarsenerType.values())
         {
             set_coarsener_combo.add(mcType.getText());
         }
 		
 		set_coarsener_combo.addSelectionListener( 
 			new SelectionAdapter(){
 				public void 
 				widgetSelected( SelectionEvent se )
 				{
 					ModelConfigurationPage.this.controller.setModelProperty(
 						Constants.GUI_MODULE_COARSENER,
 						ModuleCoarsenerType.fromString(
 							set_coarsener_combo.getText()
 						)
 					);
 				}
 			}
 		);
 		
 		set_coarsener_combo.select(0);
 	}
 	
 	private void 
 	initializePartitioningGrid
 	( Composite parent ) 
 	{
 		final GridLayout model_configuration_page_grid_layout
 			= new GridLayout();
 		model_configuration_page_grid_layout.numColumns 
 			= 2;
 		parent.setLayout( model_configuration_page_grid_layout );
 	}
 
 	private void 
 	initializePartitioningWidgets
 	( Composite parent, FormToolkit toolkit ) 
 	{
 		this.perform_partitioning_button
 			= toolkit.createButton(
 				parent, 
 				"Perform Partitioning", 
 				SWT.CHECK
 			);
 		GridData grid_data 
 			= new GridData( SWT.BEGINNING, SWT.FILL, false, false );
 		grid_data.horizontalSpan = 1;
 		perform_partitioning_button.setLayoutData(grid_data);
 		
 		perform_partitioning_button.addSelectionListener(
 			new SelectionAdapter()
 			{
 				@Override
 				public void
 				widgetSelected
 				( SelectionEvent e )
 				{
 					ModelConfigurationPage.this.controller.setModelProperty(
 						Constants.GUI_PERFORM_PARTITIONING, 
 						new Boolean(
 							ModelConfigurationPage.this
 								.perform_partitioning_button.getSelection()
 						)
 					);
 				}
 			}
 		);
 		
 		this.createDummyLabel(parent, toolkit);
 	
 		toolkit.createLabel(parent, "Execution Cost Model: ");
 		this.initialize_execution_model_combo_box(parent);
 		
 		toolkit.createLabel(parent, "Interaction Cost Model: ");
 		this.initialize_interaction_model_combo_box(parent);
 		
 		toolkit.createLabel(parent, "Partitioning Algorithm");
 		this.initilize_partitioning_algorithm_combo_box(parent);
 	}
 	
 	private Label 
 	createDummyLabel
 	( Composite parent, FormToolkit toolkit ) 
 	{
 		Label return_value = null;
 		
 		if( toolkit != null ){
 			return_value
 				= toolkit.createLabel(parent, "", SWT.NONE);
 			GridData grid_data 
 				= new GridData(SWT.BEGINNING, SWT.FILL, false, false);
 			grid_data.horizontalSpan = 1;
 			return_value.setLayoutData( grid_data );
 		}
 		
 		return return_value;
 	}
 	
 	private void 
 	initilize_partitioning_algorithm_combo_box
 	( Composite parent ) 
 	{
 		this.partitioning_algorithm_combo
 			= new Combo(parent, SWT.NONE);
 		
 	    for( final PartitionerType partitioner_type 
 	    		: PartitionerFactory.PartitionerType.values())
 	    {
 	    	this.partitioning_algorithm_combo.add(
 	    		partitioner_type.getText()
 	    	);
 	    }
 		
 	    this.partitioning_algorithm_combo.addSelectionListener( 
 			new SelectionAdapter(){
 				public void 
 				widgetSelected( SelectionEvent se )
 				{
 					ModelConfigurationPage.this.controller.setModelProperty(
 						Constants.GUI_PARTITIONER_TYPE,
 						PartitionerType.fromString(
 							ModelConfigurationPage.this.
 								partitioning_algorithm_combo.getText()
 						)
 					);
 				}
 			}
 		);
 		
 	    this.partitioning_algorithm_combo.select(0);
 	}
 
 	private void 
 	initialize_interaction_model_combo_box
 	( Composite parent ) 
 	{
 		this.interaction_model_combo
 			= new Combo(parent, SWT.NONE);
 		
 	    for( final InteractionCostType interaction_cost_type 
 	    		: InteractionFactory.InteractionCostType.values())
 	    {
 	    	this.interaction_model_combo.add(
 	    		interaction_cost_type.getText()
 	    	);
 	    }
 		
 	    this.interaction_model_combo.addSelectionListener( 
 			new SelectionAdapter(){
 				public void 
 				widgetSelected( SelectionEvent se )
 				{
 					ModelConfigurationPage.this.controller.setModelProperty(
 						Constants.GUI_INTERACTION_COST,
 						InteractionCostType.fromString(
 							ModelConfigurationPage.this
 								.interaction_model_combo.getText()
 						)
 					);
 				}
 			}
 		);
 	
 	    this.interaction_model_combo.select(0);
 	}
 
 	private void 
 	initialize_execution_model_combo_box
 	( Composite parent ) 
 	{
 		this.execution_model_combo
 			= new Combo(parent, SWT.NONE);
 		
 	    for( final ExecutionCostType execution_cost_type 
 	    		: ExecutionCostType.values())
 	    {
 	    	this.execution_model_combo.add(
 	    		execution_cost_type.getText()
 	    	);
 	    }
 		
 	    this.execution_model_combo.addSelectionListener( 
 			new SelectionAdapter(){
 				public void 
 				widgetSelected( SelectionEvent se )
 				{
 					ModelConfigurationPage.this.controller.setModelProperty(
 						Constants.GUI_EXECUTION_COST,
 						ExecutionCostType.fromString(
 							ModelConfigurationPage.this
 								.execution_model_combo.getText()
 						)
 					);
 				}
 			}
 		);
 	
 	    this.execution_model_combo.select(0);
 	}
 	
 
 	private void 
 	initializeActionsWidgets
 	( Composite parent, FormToolkit toolkit ) 
 	{
 		final Button exposure_button
 			= toolkit.createButton(
 				parent, 
 				"Generate Model", 
 				SWT.PUSH
 			);
 		GridData grid_data 
 			= new GridData(SWT.BEGINNING, SWT.FILL, false, false);
 		grid_data.horizontalSpan = 1;
 		exposure_button.setLayoutData(grid_data);
 		
 		exposure_button.addSelectionListener(
 			new SelectionAdapter()
 			{
 				@Override
 				public void
 				widgetSelected
 				( SelectionEvent e )
 				{
 					ModelConfigurationPage.this.controller.setModelProperty(
 						Constants.GUI_HOST_CONFIGURATION, 
 						ModelConfigurationPage.this.host_config_text.getText()
 					);
 					ModelConfigurationPage.this.controller.setModelProperty(
 						Constants.GUI_MODULE_EXPOSER, 
 						ModelConfigurationPage.this.module_exposer_text.getText()
 					);
 					
 					ModelConfigurationPage.this.host_config_text.setEditable(false);
 					ModelConfigurationPage.this.module_exposer_text.setEditable(false);
 					
 					ModelConfigurationPage.this.setVisualizationAction();
 					ModelConfigurationPage.this.actions_composite.setVisible(false);
 					
 					ModelConfigurationPage.this.synthetic_node_button.setEnabled(false);
 					ModelConfigurationPage.this.exposure_button.setEnabled(false);
 					
 					ModelConfigurationPage.this.mod_exposer_browse_button.setVisible(false);
 					ModelConfigurationPage.this.host_config_browse.setVisible(false);
 					ModelConfigurationPage.this.set_coarsener_combo.setEnabled(false);
 					
 					ModelConfigurationPage.this
 						.perform_partitioning_button.setEnabled(false);
 					ModelConfigurationPage.this.set_partitioning_widgets_enabled(false);
 					
 					ModelConfigurationPage.this.updateModelName();
 				}
 			}
 		);
 	}
 	
 	private void 
 	updateModelName() 
 	{
 		String name_suffix
 			= new SimpleDateFormat("HH:mm:ss")
 				.format( new Date() );
 		String coarsener
 			= ModelConfigurationPage.this.set_coarsener_combo.getText();
 		String new_name
 			= coarsener + "_" + name_suffix;
 		
 		VirtualModelFileInput input
 			= (VirtualModelFileInput) 
 				ModelConfigurationPage.this.model_creation_editor.getEditorInput();
 		
 		input.setSecondaryName(new_name);
 		
 		BundleContext context 
 			= FrameworkUtil.getBundle(
 				ModelConfigurationPage.class
 			).getBundleContext();
         ServiceReference<EventAdmin> ref 
         	= context.getServiceReference(EventAdmin.class);
         EventAdmin eventAdmin 
         	= context.getService(ref);
         Map<String,Object> properties 
         	= new HashMap<String, Object>();
         properties.put("REFRESH", new Boolean(true));
         Event event 
         	= new Event("viewcommunication/syncEvent", properties);
         eventAdmin.sendEvent(event);
         event = new Event("viewcommunication/asyncEvent", properties);
         eventAdmin.postEvent(event);
         
         this.model_creation_editor.updateTitle();
 	}
 
 	void
 	set_partitioning_widgets_enabled
 	( boolean enabled )
 	{
 		this
 			.partitioning_algorithm_combo.setEnabled(enabled);
 		ModelConfigurationPage.this
 			.execution_model_combo.setEnabled(enabled);
 		ModelConfigurationPage.this
 			.interaction_model_combo.setEnabled(enabled);
 	}
 
 	private void 
 	initializeActionsGrid
 	( Composite parent ) 
 	{
 		final GridLayout model_configuration_page_grid_layout
 			= new GridLayout();
 		model_configuration_page_grid_layout.numColumns 
 			= 2;
 		parent.setLayout( model_configuration_page_grid_layout );
 	}
 
 	public void 
 	setModuleExposerPath
 	( String new_value ) 
 	{
 		this.module_exposer_text.setText( new_value );
 	}
 	
 	public void
 	setHostConfigurationPath
 	( String path )
 	{
 		this.host_config_text.setText( path );
 	}
 	
 	public void
 	setVisualizationAction()
 	{
 		// david - make sure we are not in the middle of creating a new model
 		synchronized( this.current_vp_lock){
 			if(this.currentVP != null){
 				return;
 			}
 		}
 		
 		try{ 
 			// for concurrency, we cache the references we will work with
 			final String profiler_trace_path
 				 = this.partitioner_gui_state_model.getProfilerTracePath();
 			final PartitionerGUIStateModel gui_state_model 
 				= this.partitioner_gui_state_model;
 			
 			if(  	profiler_trace_path == null 
 					|| profiler_trace_path.equals("") ) {
 				throw new Exception("No profiler dump data is provided.");   
 			}
 			
            	if( gui_state_model.getHostConfigurationPath()
            			.equals("") ) {
            		throw new Exception ("No host layout is provided.");
            	}
            	
            	gui_state_model.initializeHostModel();
            
            	// reading the input stream for the profiling XML document 
            	// provided to the tool.
            	ModelConfigurationPage.Job job 
            		= new ModelConfigurationPage.Job(
            			profiler_trace_path,
            			gui_state_model
            		);
            	
            	job.setUser(true);
            	job.setPriority(Job.SHORT);
            	job.schedule();
         }catch(Exception e){    
         	e.printStackTrace();
         }
 	}
 	
 	class 
 	Job extends org.eclipse.core.runtime.jobs.Job
 	{
 		private String profiler_trace_path;
 		private PartitionerGUIStateModel gui_state_model;
 		
 		Job
 		( String profiler_trace_path, PartitionerGUIStateModel gui_state_model )
 		{
 			super("Create Model");
 			
 			this.profiler_trace_path
				= profiler_trace_path;
 			this.gui_state_model
 				= gui_state_model;
 		}
 		@Override
 		protected IStatus 
 		run
 		( IProgressMonitor monitor ) 
 		{
 			try {
 				final InputStream in 
 					=  new BufferedInputStream(
 						new FileInputStream(
 							this.profiler_trace_path
 						)
 					);
 				if( monitor.isCanceled()){
 				 	return Status.CANCEL_STATUS;
 				}
 				this.gui_state_model.createModuleModel(in);
 				in.close();
 				this.gui_state_model.finished();
 				visualizeModuleModel(); 
 				
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			return Status.OK_STATUS;
 		}
 		
 		private void 
 		visualizeModuleModel() 
 		{
 			SwingUtilities.invokeLater( new Runnable(){
 				@Override
 				public void
 				run()
 				{
 					synchronized(ModelConfigurationPage.this.current_vp_lock){
 						ModelConfigurationPage.this.currentVP
 							= new VisualizePartitioning( frame );
 
 						ModelConfigurationPage.this.currentVP.drawModules(
 							Job.this.gui_state_model
 								.getModuleModel().getModuleExchangeMap()
 						);  
 						
 						try {
 							ModelConfigurationPage.this.frame.pack();
 							SwingUtilities.updateComponentTreeUI(
 									ModelConfigurationPage.this.frame
 							);
 							
 						} catch (Exception e) {
 							e.printStackTrace();
 						};
 						
 						if(Job.this.gui_state_model.isPartitioningEnabled()){
 							ModelConfigurationPage.this.currentVP
 				            	.setAlgorithm(Job.this.gui_state_model.getAlgorithmString());
 							ModelConfigurationPage.this.currentVP
 				            	.setSolution(Job.this.gui_state_model.getSolution());
 						}
 					}
 				}
 			});
 		}
 	}
 }
