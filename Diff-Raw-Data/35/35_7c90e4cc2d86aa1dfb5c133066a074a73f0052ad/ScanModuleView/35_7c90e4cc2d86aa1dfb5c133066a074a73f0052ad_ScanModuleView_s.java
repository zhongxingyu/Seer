 package de.ptb.epics.eve.editor.views.scanmoduleview;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.databinding.Binding;
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.UpdateValueStrategy;
 import org.eclipse.core.databinding.beans.BeansObservables;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
 import org.eclipse.jface.databinding.swt.SWTObservables;
 import org.eclipse.jface.databinding.viewers.ViewersObservables;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.custom.CTabItem;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.events.FocusAdapter;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Image;
 
 import de.ptb.epics.eve.data.measuringstation.Event;
 import de.ptb.epics.eve.data.scandescription.ScanModule;
 import de.ptb.epics.eve.data.scandescription.errors.AxisError;
 import de.ptb.epics.eve.data.scandescription.errors.ChannelError;
 import de.ptb.epics.eve.data.scandescription.errors.IModelError;
 import de.ptb.epics.eve.data.scandescription.errors.PlotWindowError;
 import de.ptb.epics.eve.data.scandescription.errors.PositioningError;
 import de.ptb.epics.eve.data.scandescription.errors.PostscanError;
 import de.ptb.epics.eve.data.scandescription.errors.PrescanError;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ControlEventTypes;
 import de.ptb.epics.eve.data.scandescription.updatenotification.IModelUpdateListener;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ModelUpdateEvent;
 import de.ptb.epics.eve.editor.Activator;
 import de.ptb.epics.eve.editor.gef.editparts.ChainEditPart;
 import de.ptb.epics.eve.editor.gef.editparts.ScanModuleEditPart;
 import de.ptb.epics.eve.editor.views.EditorViewPerspectiveListener;
 import de.ptb.epics.eve.editor.views.IEditorView;
 import de.ptb.epics.eve.editor.views.eventcomposite.EventComposite;
 import de.ptb.epics.eve.editor.views.scanmoduleview.detectorchannelcomposite.DetectorChannelComposite;
 import de.ptb.epics.eve.editor.views.scanmoduleview.motoraxiscomposite.MotorAxisComposite;
 import de.ptb.epics.eve.editor.views.scanmoduleview.plotcomposite.PlotComposite;
 import de.ptb.epics.eve.editor.views.scanmoduleview.positioningcomposite.PositioningComposite;
 import de.ptb.epics.eve.editor.views.scanmoduleview.postscancomposite.PostscanComposite;
 import de.ptb.epics.eve.editor.views.scanmoduleview.prescancomposite.PrescanComposite;
 import de.ptb.epics.eve.util.jface.SelectionProviderWrapper;
 import de.ptb.epics.eve.util.swt.TextSelectAllFocusListener;
 import de.ptb.epics.eve.util.swt.TextSelectAllMouseListener;
 
 /**
  * <code>ScanModulView</code> shows the currently selected scan module.
  * 
  * @author ?
  * @author Marcus Michalsky
  * @author Hartmut Scherr
  */
 public class ScanModuleView extends ViewPart implements IEditorView,
 		ISelectionListener, IModelUpdateListener {
 
 	/**
 	 * the id of the <code>ScanModulView</code>.
 	 */
 	public static final String ID = "de.ptb.epics.eve.editor.views.ScanModulView";
 
 	private static Logger logger = Logger.getLogger(ScanModuleView.class);
 
 	// the scan module currently represented by this view
 	private ScanModule currentScanModule;
 
 	private Composite top;
 	private Composite generalComposite;
 	private Composite actionsComposite;
 	private Composite eventsComposite;
 
 	private boolean actionsCompositeMaximized;
 	private boolean eventsCompositeMaximized;
 
 	private Label valueCountLabel;
 	private Text valueCountText;
 
 	private Label triggerDelayLabel;
 	private Text triggerDelayText;
 
 	private Label settleTimeLabel;
 	private Text settleTimeText;
 
 	private Button triggerConfirmAxisCheckBox;
 
 	private Button triggerConfirmChannelCheckBox;
 
 	private CTabFolder actionsTabFolder;
 	private CTabItem motorAxisTab;
 	private CTabItem detectorChannelTab;
 	private CTabItem prescanTab;
 	private CTabItem postscanTab;
 	private CTabItem positioningTab;
 	private CTabItem plotTab;
 
 	private Label actionMaxIcon;
 	private ActionComposite motorAxisComposite;
 	private ActionComposite detectorChannelComposite;
 	private ActionComposite prescanComposite;
 	private ActionComposite postscanComposite;
 	private ActionComposite positioningComposite;
 	private ActionComposite plotComposite;
 
 	/** */
 	public CTabFolder eventsTabFolder;
 	private CTabItem pauseEventsTabItem;
 	private CTabItem redoEventsTabItem;
 	private CTabItem breakEventsTabItem;
 	private CTabItem triggerEventsTabItem;
 
 	private EventsTabFolderSelectionListener eventsTabFolderSelectionListener;
 
 	private Label eventMaxIcon;
 	private EventComposite pauseEventComposite;
 	private EventComposite redoEventComposite;
 	private EventComposite breakEventComposite;
 	private EventComposite triggerEventComposite;
 
 	private Button appendScheduleEventCheckBox;
 	private AppendScheduleEventCheckBoxSelectionListener 
 			appendScheduleEventCheckBoxSelectionListener;
 
 	private SashForm sashForm;
 
 	private Image restoreIcon;
 	private Image maximizeIcon;
 
 	private DataBindingContext context;
 	
 	private ISelectionProvider selectionProvider;
 	
 	private IObservableValue selectionObservable;
 	
 	private IObservableValue valueCountTargetObservable;
 	private IObservableValue valueCountModelObservable;
 	private Binding valueCountBinding;
 	
 	private IObservableValue triggerDelayTargetObservable;
 	private IObservableValue triggerDelayModelObservable;
 	private Binding triggerDelayBinding;
 	
 	private IObservableValue settleTimeTargetObservable;
 	private IObservableValue settleTimeModelObservable;
 	private Binding settleTimeBinding;
 	
 	private IObservableValue axisTriggerTargetObservable;
 	private IObservableValue axisTriggerModelObservable;
 	private Binding axisTriggerBinding;
 	
 	private IObservableValue channelTriggerTargetObservable;
 	private IObservableValue channelTriggerModelObservable;
 	private Binding channelTriggerBinding;
 
 	// the selection service only accepts one selection provider per view,
 	// since we have multiple tabs with tables capable of providing selections,
 	// a wrapper handles them and registers the active one with the global
 	// selection service
 	protected SelectionProviderWrapper selectionProviderWrapper;
 
 	private EditorViewPerspectiveListener editorViewPerspectiveListener;
 
 	private IMemento memento;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void init(IViewSite site, IMemento memento) throws PartInitException {
 		init(site);
 		this.memento = memento;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void createPartControl(final Composite parent) {
 		logger.debug("createPartControl");
 
 		parent.setLayout(new FillLayout());
 
 		if (Activator.getDefault().getMeasuringStation() == null) {
 			final Label errorLabel = new Label(parent, SWT.NONE);
 			errorLabel.setText("No Measuring Station has been loaded. "
 					+ "Please check Preferences!");
 			return;
 		}
 
 		this.restoreIcon = Activator.getDefault().getImageRegistry()
 				.get("RESTORE");
 		this.maximizeIcon = Activator.getDefault().getImageRegistry()
 				.get("MAXIMIZE");
 
 		this.top = new Composite(parent, SWT.NONE);
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.marginWidth = 0;
 		gridLayout.marginHeight = 0;
 		gridLayout.verticalSpacing = 4;
 		this.top.setLayout(gridLayout);
 
 		this.createGeneralComposite(this.top);
 		this.sashForm = new SashForm(this.top, SWT.VERTICAL);
 		this.sashForm.SASH_WIDTH = 4;
 		GridData gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		this.sashForm.setLayoutData(gridData);
 		this.createActionsComposite(this.sashForm);
 		this.createEventComposite(this.sashForm);
 
 		this.actionsCompositeMaximized = false;
 		this.eventsCompositeMaximized = false;
 
 		this.restoreState();
 
 		top.setVisible(false);
 
 		this.bindValues();
 
 		// listen to selection changes (if a scan module is selected, its
 		// attributes are made available for editing)
 		getSite().getWorkbenchWindow().getSelectionService()
 				.addSelectionListener(this);
 
 		selectionProviderWrapper = new SelectionProviderWrapper();
 		getSite().setSelectionProvider(selectionProviderWrapper);
 
 		// reset view if last editor was closed
 		this.editorViewPerspectiveListener = new EditorViewPerspectiveListener(
 				this);
 		PlatformUI.getWorkbench().getActiveWorkbenchWindow()
 				.addPerspectiveListener(this.editorViewPerspectiveListener);
 	} // end of: createPartControl()
 
 	/*
 	 * 
 	 */
 	private void createGeneralComposite(Composite parent) {
 		this.generalComposite = new Composite(parent, SWT.BORDER);
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 2;
 		this.generalComposite.setLayout(gridLayout);
 		GridData gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		this.generalComposite.setLayoutData(gridData);
 
 		// value count
 		this.valueCountLabel = new Label(this.generalComposite, SWT.NONE);
 		this.valueCountLabel.setText("No of Measurements:");
 		this.valueCountLabel.setToolTipText(
 				"Number of Measurements taken for each motor position");
 		this.valueCountText = new Text(this.generalComposite, SWT.BORDER);
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.verticalAlignment = GridData.CENTER;
 		gridData.horizontalIndent = 7;
 		gridData.grabExcessHorizontalSpace = true;
 		this.valueCountText.setLayoutData(gridData);
 		this.valueCountText.addFocusListener(new TextSelectAllFocusListener(
 				this.valueCountText));
 		this.valueCountText.addMouseListener(new TextSelectAllMouseListener(
 				this.valueCountText));
 
 		// Trigger Delay
 		this.triggerDelayLabel = new Label(this.generalComposite, SWT.NONE);
 		this.triggerDelayLabel.setText("Trigger Delay (in s):");
 		this.triggerDelayLabel.setToolTipText("Delay time after positioning");
 		this.triggerDelayText = new Text(this.generalComposite, SWT.BORDER);
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.verticalAlignment = GridData.CENTER;
 		gridData.horizontalIndent = 7;
 		gridData.grabExcessHorizontalSpace = true;
 		this.triggerDelayText.setLayoutData(gridData);
 		this.triggerDelayText.addFocusListener(new TextSelectAllFocusListener(
 				this.triggerDelayText));
 		this.triggerDelayText.addMouseListener(new TextSelectAllMouseListener(
 				this.triggerDelayText));
 
 		// Settle Time
 		this.settleTimeLabel = new Label(this.generalComposite, SWT.NONE);
 		this.settleTimeLabel.setText("Settle Time (in s):");
 		this.settleTimeLabel
 				.setToolTipText("Delay time after first positioning in the scan module");
 		this.settleTimeText = new Text(this.generalComposite, SWT.BORDER);
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.verticalAlignment = GridData.CENTER;
 		gridData.horizontalIndent = 7;
 		gridData.grabExcessHorizontalSpace = true;
 		this.settleTimeText.setLayoutData(gridData);
 		this.settleTimeText.addFocusListener(new TextSelectAllFocusListener(
 				this.settleTimeText));
 		this.settleTimeText.addMouseListener(new TextSelectAllMouseListener(
 				this.settleTimeText));
 
 		// Manual Trigger
 		Label triggerLabel = new Label(this.generalComposite, SWT.NONE);
 		triggerLabel.setText("Manual Trigger:");
 		triggerLabel.setLayoutData(new GridData());
 		Composite triggerCheckBoxes = new Composite(this.generalComposite,
 				SWT.NONE);
 		gridData = new GridData();
 		gridData.horizontalIndent = 7;
 		triggerCheckBoxes.setLayoutData(gridData);
 		triggerCheckBoxes.setLayout(new FillLayout(SWT.HORIZONTAL));
 		this.triggerConfirmAxisCheckBox = new Button(triggerCheckBoxes,
 				SWT.CHECK);
 		this.triggerConfirmAxisCheckBox.setText("Motors");
 		this.triggerConfirmChannelCheckBox = new Button(triggerCheckBoxes,
 				SWT.CHECK);
 		this.triggerConfirmChannelCheckBox.setText("Detectors");
 	}
 
 	/*
 	 * 
 	 */
 	private void createActionsComposite(Composite parent) {
 		this.actionsComposite = new Composite(parent, SWT.BORDER);
 		this.actionsComposite.setLayout(new GridLayout(2, false));
 
 		this.actionMaxIcon = new Label(actionsComposite, SWT.NONE);
 		this.actionMaxIcon.setImage(maximizeIcon);
 		this.actionMaxIcon.addMouseListener(new ActionsMaxIconMouseListener());
 
 		Label actionsLabel = new Label(actionsComposite, SWT.NONE);
 		actionsLabel.setText("Actions:");
 
 		this.actionsTabFolder = new CTabFolder(this.actionsComposite, SWT.FLAT);
 		this.actionsTabFolder.setSimple(false);
 		this.actionsTabFolder.setBorderVisible(true);
 		GridData gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		gridData.horizontalSpan = 2;
 		this.actionsTabFolder.setLayoutData(gridData);
 
 		motorAxisComposite = new MotorAxisComposite(this, actionsTabFolder,
 				SWT.NONE);
 		detectorChannelComposite = new DetectorChannelComposite(this,
 				actionsTabFolder, SWT.NONE);
 		prescanComposite = new PrescanComposite(this, actionsTabFolder,
 				SWT.NONE);
 		postscanComposite = new PostscanComposite(this, actionsTabFolder,
 				SWT.NONE);
 		positioningComposite = new PositioningComposite(this, actionsTabFolder,
 				SWT.NONE);
 		plotComposite = new PlotComposite(this, actionsTabFolder, SWT.NONE);
 
 		this.motorAxisTab = new CTabItem(this.actionsTabFolder, SWT.FLAT);
 		this.motorAxisTab.setText(" Motor Axes ");
 		this.motorAxisTab
 				.setToolTipText("Select motor axes to be used in this scan module");
 		this.motorAxisTab.setControl(this.motorAxisComposite);
 
 		this.detectorChannelTab = new CTabItem(this.actionsTabFolder, SWT.FLAT);
 		this.detectorChannelTab.setText(" Detector Channels ");
 		this.detectorChannelTab
 				.setToolTipText("Select detector channels to be used in this scan module");
 		this.detectorChannelTab.setControl(this.detectorChannelComposite);
 
 		this.prescanTab = new CTabItem(this.actionsTabFolder, SWT.FLAT);
 		this.prescanTab.setText(" Prescan ");
 		this.prescanTab
 				.setToolTipText("Action to do before scan module is started");
 		this.prescanTab.setControl(this.prescanComposite);
 
 		this.postscanTab = new CTabItem(this.actionsTabFolder, SWT.FLAT);
 		this.postscanTab.setText(" Postscan ");
 		this.postscanTab.setToolTipText("Action to do if scan module is done");
 		this.postscanTab.setControl(this.postscanComposite);
 
 		this.positioningTab = new CTabItem(this.actionsTabFolder, SWT.FLAT);
 		this.positioningTab.setText(" Positioning ");
 		this.positioningTab
 				.setToolTipText("Move motor to calculated position after scan module is done");
 		this.positioningTab.setControl(this.positioningComposite);
 
 		this.plotTab = new CTabItem(this.actionsTabFolder, SWT.FLAT);
 		this.plotTab.setText(" Plot ");
 		this.plotTab.setToolTipText("Plot settings for this scan module");
 		this.plotTab.setControl(this.plotComposite);
 	}
 
 	/*
 	 * 
 	 */
 	private void createEventComposite(Composite parent) {
 		this.eventsComposite = new Composite(parent, SWT.BORDER);
 		this.eventsComposite.setLayout(new GridLayout(2, false));
 
 		this.eventMaxIcon = new Label(eventsComposite, SWT.NONE);
 		this.eventMaxIcon.setImage(maximizeIcon);
 		this.eventMaxIcon.addMouseListener(new EventMaxIconMouseListener());
 
 		Label eventLabel = new Label(eventsComposite, SWT.NONE);
 		eventLabel.setText("Events:");
 
 		eventsTabFolder = new CTabFolder(this.eventsComposite, SWT.NONE);
 		this.eventsTabFolder.setSimple(false);
 		this.eventsTabFolder.setBorderVisible(true);
 		this.eventsTabFolderSelectionListener = new EventsTabFolderSelectionListener();
 		eventsTabFolder.addSelectionListener(eventsTabFolderSelectionListener);
 		GridData gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		gridData.horizontalSpan = 2;
 		eventsTabFolder.setLayoutData(gridData);
 
 		pauseEventComposite = new EventComposite(eventsTabFolder, SWT.NONE,
 				ControlEventTypes.PAUSE_EVENT, this);
 		redoEventComposite = new EventComposite(eventsTabFolder, SWT.NONE,
 				ControlEventTypes.CONTROL_EVENT, this);
 		breakEventComposite = new EventComposite(eventsTabFolder, SWT.NONE,
 				ControlEventTypes.CONTROL_EVENT, this);
 		triggerEventComposite = new EventComposite(eventsTabFolder, SWT.NONE,
 				ControlEventTypes.CONTROL_EVENT, this);
 
 		this.pauseEventsTabItem = new CTabItem(eventsTabFolder, SWT.NONE);
 		this.pauseEventsTabItem.setText(" Pause ");
 		this.pauseEventsTabItem
 				.setToolTipText("Configure event to pause and resume this scan module");
 		this.pauseEventsTabItem.setControl(pauseEventComposite);
 		this.redoEventsTabItem = new CTabItem(eventsTabFolder, SWT.NONE);
 		this.redoEventsTabItem.setText(" Redo ");
 		this.redoEventsTabItem
 				.setToolTipText("Repeat the last acquisition, if redo event occurs");
 		this.redoEventsTabItem.setControl(redoEventComposite);
 		this.breakEventsTabItem = new CTabItem(eventsTabFolder, SWT.NONE);
 		this.breakEventsTabItem.setText(" Skip ");
 		this.breakEventsTabItem
 				.setToolTipText("Finish this scan module and continue with next");
 		this.breakEventsTabItem.setControl(breakEventComposite);
 		this.triggerEventsTabItem = new CTabItem(eventsTabFolder, SWT.NONE);
 		this.triggerEventsTabItem.setText(" Trigger ");
 		this.triggerEventsTabItem
 				.setToolTipText("Wait for trigger event before moving to next position");
 		this.triggerEventsTabItem.setControl(triggerEventComposite);
 
 		appendScheduleEventCheckBox = new Button(this.eventsComposite,
 				SWT.CHECK);
 		appendScheduleEventCheckBox.setText("Append Schedule Event");
 		gridData = new GridData();
 		gridData.horizontalSpan = 2;
 		appendScheduleEventCheckBox.setLayoutData(gridData);
 		this.appendScheduleEventCheckBoxSelectionListener = 
 				new AppendScheduleEventCheckBoxSelectionListener();
 		appendScheduleEventCheckBox.addSelectionListener(
 				appendScheduleEventCheckBoxSelectionListener);
 	}
 
 	/*
 	 * 
 	 */
 	private void bindValues() {
 		this.context = new DataBindingContext();
 		
 		this.selectionProvider = new ScanModuleSelectionProvider();
 		this.selectionObservable = ViewersObservables
 				.observeSingleSelection(selectionProvider);
 		
 		this.valueCountTargetObservable = SWTObservables
 				.observeText(this.valueCountText, SWT.Modify);
 		this.valueCountModelObservable = BeansObservables.observeDetailValue(
 				selectionObservable, ScanModule.class,
 				ScanModule.VALUE_COUNT_PROP, Integer.class);
 		UpdateValueStrategy valueCountTargetToModelStrategy = 
 				new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE);
 		valueCountTargetToModelStrategy
 				.setAfterGetValidator(new ValueCountValidator());
 		valueCountTargetToModelStrategy.setConverter(new ValueCountConverter());
 		this.valueCountBinding = context.bindValue(valueCountTargetObservable,
 				valueCountModelObservable, valueCountTargetToModelStrategy,
 				new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE));
 		ControlDecorationSupport.create(this.valueCountBinding, SWT.LEFT);
 		this.valueCountText.addFocusListener(new TextFocusListener(
 				this.valueCountText));
 		
 		this.triggerDelayTargetObservable = SWTObservables.observeText(
 				this.triggerDelayText, SWT.Modify);
 		this.triggerDelayModelObservable = BeansObservables.observeDetailValue(
 				selectionObservable, ScanModule.class, 
 				ScanModule.TRIGGER_DELAY_PROP, Double.class);
 		UpdateValueStrategy triggerDelayTargetToModelStrategy = 
 				new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE);
 		triggerDelayTargetToModelStrategy
 				.setAfterGetValidator(new TriggerDelaySettleTimeValidator("Settle Time"));
 		triggerDelayTargetToModelStrategy
 				.setConverter(new TriggerDelaySettleTimeConverter());
 		UpdateValueStrategy triggerDelayModelToTargetStrategy = 
 				new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE);
 		triggerDelayModelToTargetStrategy
 				.setConverter(new ModelToTargetConverter());
 		this.triggerDelayBinding = context.bindValue(
 				triggerDelayTargetObservable, triggerDelayModelObservable,
 				triggerDelayTargetToModelStrategy,
 				triggerDelayModelToTargetStrategy);
 		ControlDecorationSupport.create(this.triggerDelayBinding, SWT.LEFT);
 		this.triggerDelayText.addFocusListener(new TextFocusListener(
 				this.triggerDelayText));
 		
 		this.settleTimeTargetObservable = SWTObservables.observeText(
 				this.settleTimeText, SWT.Modify);
 		this.settleTimeModelObservable = BeansObservables.observeDetailValue(
 				selectionObservable, ScanModule.class,
 				ScanModule.SETTLE_TIME_PROP, Double.class);
 		UpdateValueStrategy settleTimeTargetToModelStrategy = 
 				new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE);
 		UpdateValueStrategy settleTimeModelToTargetStrategy = 
 				new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE);
 		settleTimeModelToTargetStrategy
 				.setConverter(new ModelToTargetConverter());
 		settleTimeTargetToModelStrategy
 				.setAfterGetValidator(new TriggerDelaySettleTimeValidator(
 						"Settle Time"));
 		settleTimeTargetToModelStrategy
 				.setConverter(new TriggerDelaySettleTimeConverter());
 		this.settleTimeBinding = context.bindValue(settleTimeTargetObservable,
 				settleTimeModelObservable, settleTimeTargetToModelStrategy,
 				settleTimeModelToTargetStrategy);
 		ControlDecorationSupport.create(this.settleTimeBinding, SWT.LEFT);
 		this.settleTimeText.addFocusListener(new TextFocusListener(
 				this.settleTimeText));
 		
 		this.axisTriggerTargetObservable = SWTObservables
 				.observeSelection(this.triggerConfirmAxisCheckBox);
 		this.axisTriggerModelObservable = BeansObservables.observeDetailValue(
 				selectionObservable, ScanModule.class,
 				ScanModule.TRIGGER_CONFIRM_AXIS_PROP, Boolean.class);
 		this.axisTriggerBinding = context.bindValue(
 				axisTriggerTargetObservable, axisTriggerModelObservable,
 				new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE),
 				new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE));
 		this.axisTriggerBinding.getClass();
 		
 		this.channelTriggerTargetObservable = SWTObservables
 				.observeSelection(this.triggerConfirmChannelCheckBox);
 		this.channelTriggerModelObservable = BeansObservables
 				.observeDetailValue(selectionObservable, ScanModule.class,
 						ScanModule.TRIGGER_CONFIRM_CHANNEL_PROP, Boolean.class);
 		this.channelTriggerBinding = context.bindValue(
 				channelTriggerTargetObservable, channelTriggerModelObservable,
 				new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE),
 				new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE));
 		this.channelTriggerBinding.getClass();
 	}
 
 	// ***********************************************************************
 	// ********************** end of create part control *********************
 	// ***********************************************************************
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setFocus() {
 		logger.debug("focus gained -> forward to top composite");
 		this.top.setFocus();
 	}
 
 	/**
 	 * Returns the currently active scan module.
 	 * 
 	 * @return the scan module
 	 */
 	public ScanModule getCurrentScanModule() {
 		return currentScanModule;
 	}
 
 	/*
 	 * Sets the currently active scan module. Called by selectionChanged if the
 	 * current selection is of interest.
 	 */
 	private void setCurrentScanModule(ScanModule currentScanModule) {
 		logger.debug("setCurrentScanModule");
 
 		// if there was a scan module shown before, stop listening to changes
 		if (this.currentScanModule != null) {
 			this.currentScanModule.removeModelUpdateListener(this);
 		}
 
 		// set the new scan module as the current one
 		this.currentScanModule = currentScanModule;
 
 		// tell the action composites about the change
 		this.motorAxisComposite.setScanModule(this.currentScanModule);
 		this.detectorChannelComposite.setScanModule(this.currentScanModule);
 		this.prescanComposite.setScanModule(this.currentScanModule);
 		this.postscanComposite.setScanModule(this.currentScanModule);
 		this.positioningComposite.setScanModule(this.currentScanModule);
 		this.plotComposite.setScanModule(this.currentScanModule);
 
 		// get the selected tab
 		int selection_index = this.actionsTabFolder.getSelectionIndex();
 		if (selection_index == -1) {
 			this.actionsTabFolder.setSelection(0);
 		} else {
 			this.actionsTabFolder.setSelection(selection_index);
 		}
 
 		if (this.currentScanModule != null) {
 			// new scan module
 			this.currentScanModule.addModelUpdateListener(this);
 
 			top.setVisible(true);
 			this.setPartName(this.currentScanModule.getName() + ":"
 					+ this.currentScanModule.getId());
 
 			if (this.eventsTabFolder.getSelection() == null) {
 				this.eventsTabFolder.setSelection(0);
 			}
 
 			this.triggerEventComposite
 					.setControlEventManager(this.currentScanModule
 							.getTriggerControlEventManager());
 			this.breakEventComposite
 					.setControlEventManager(this.currentScanModule
 							.getBreakControlEventManager());
 			this.redoEventComposite
 					.setControlEventManager(this.currentScanModule
 							.getRedoControlEventManager());
 			this.pauseEventComposite
 					.setControlEventManager(this.currentScanModule
 							.getPauseControlEventManager());
 		} else {
 			// no scan module selected -> reset contents
 			selectionProviderWrapper.setSelectionProvider(null);
 			
 			this.setPartName("No Scan Module selected");
 			
 			triggerEventComposite.setControlEventManager(null);
 			breakEventComposite.setControlEventManager(null);
 			redoEventComposite.setControlEventManager(null);
 			pauseEventComposite.setControlEventManager(null);
 			
 			top.setVisible(false);
 		}
 		updateEvent(null);
 	}
 
 	/*
 	 * 
 	 */
 	private void checkForErrors() {
 
 		// check errors in Actions Tab
 		this.motorAxisTab.setImage(null);
 		this.detectorChannelTab.setImage(null);
 		this.prescanTab.setImage(null);
 		this.postscanTab.setImage(null);
 		this.positioningTab.setImage(null);
 		this.plotTab.setImage(null);
 		boolean motorAxisErrors = false;
 		boolean detectorChannelErrors = false;
 		boolean prescanErrors = false;
 		boolean postscanErrors = false;
 		boolean positioningErrors = false;
 		boolean plotWindowErrors = false;
 
 		for (IModelError error : this.currentScanModule.getModelErrors()) {
 			if (error instanceof AxisError) {
 				motorAxisErrors = true;
 			} else if (error instanceof ChannelError) {
 				detectorChannelErrors = true;
 			} else if (error instanceof PrescanError) {
 				prescanErrors = true;
 			} else if (error instanceof PostscanError) {
 				postscanErrors = true;
 			} else if (error instanceof PositioningError) {
 				positioningErrors = true;
 			} else if (error instanceof PlotWindowError) {
 				plotWindowErrors = true;
 			}
 		}
 
 		if (motorAxisErrors) {
 			this.motorAxisTab.setImage(PlatformUI.getWorkbench()
 					.getSharedImages()
 					.getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
 		}
 		if (detectorChannelErrors) {
 			this.detectorChannelTab.setImage(PlatformUI.getWorkbench()
 					.getSharedImages()
 					.getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
 		}
 		if (prescanErrors) {
 			this.prescanTab.setImage(PlatformUI.getWorkbench()
 					.getSharedImages()
 					.getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
 		}
 		if (postscanErrors) {
 			this.postscanTab.setImage(PlatformUI.getWorkbench()
 					.getSharedImages()
 					.getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
 		}
 		if (positioningErrors) {
 			this.positioningTab.setImage(PlatformUI.getWorkbench()
 					.getSharedImages()
 					.getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
 		}
 		if (plotWindowErrors) {
 			this.plotTab.setImage(PlatformUI.getWorkbench().getSharedImages()
 					.getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
 		}
 
 		// check errors in Events Tab
 		this.pauseEventsTabItem.setImage(null);
 		this.redoEventsTabItem.setImage(null);
 		this.breakEventsTabItem.setImage(null);
 		this.triggerEventsTabItem.setImage(null);
 
 		if (this.currentScanModule.getPauseControlEventManager()
 				.getModelErrors().size() > 0) {
 			this.pauseEventsTabItem.setImage(PlatformUI.getWorkbench()
 					.getSharedImages()
 					.getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
 		}
 		if (this.currentScanModule.getRedoControlEventManager()
 				.getModelErrors().size() > 0) {
 			this.redoEventsTabItem.setImage(PlatformUI.getWorkbench()
 					.getSharedImages()
 					.getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
 		}
 		if (this.currentScanModule.getBreakControlEventManager()
 				.getModelErrors().size() > 0) {
 			this.breakEventsTabItem.setImage(PlatformUI.getWorkbench()
 					.getSharedImages()
 					.getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
 		}
 		if (this.currentScanModule.getTriggerControlEventManager()
 				.getModelErrors().size() > 0) {
 			this.triggerEventsTabItem.setImage(PlatformUI.getWorkbench()
 					.getSharedImages()
 					.getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
 		}
 	}
 
 	/**
 	 * Sets the selection provider.
 	 * 
 	 * @param selectionProvider
 	 *            the selection provider that should be set
 	 */
 	public void setSelectionProvider(ISelectionProvider selectionProvider) {
 		this.selectionProviderWrapper.setSelectionProvider(selectionProvider);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
 		logger.debug("selection changed");
 		if (!(selection instanceof IStructuredSelection)
 				|| ((IStructuredSelection) selection).size() == 0) {
 			return;
 		}
 		// at any given time this view can only display options of
 		// one device we take the last (see org.eclipse.gef.SelectionMananger,
 		// primary selection)
 		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
 		Object o = structuredSelection.toList().get(
 				structuredSelection.size() - 1);
 		if (o instanceof ScanModuleEditPart) {
 			// set new ScanModule
 			if (logger.isDebugEnabled()) {
 				logger.debug("ScanModule: "
 						+ ((ScanModuleEditPart) o).getModel().getId()
 						+ " selected.");
 			}
 			final ScanModuleEditPart smep = (ScanModuleEditPart) o;
 			setCurrentScanModule(smep.getModel());
 		} else if (o instanceof ChainEditPart) {
 			// clicking empty space in the editor
 			logger.debug("selection is ScanDescriptionEditPart: " + o);
 			setCurrentScanModule(null);
 		} else {
 			logger.debug("selection other than ScanModule -> ignore: " + o);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void updateEvent(ModelUpdateEvent modelUpdateEvent) {
 		if (this.currentScanModule != null) {
 			// TODO CheckBox for ScheduleIncident Start or End
 			// TODO understand
 			if (currentScanModule.getChain() != null) {
 				Event testEvent = new Event(currentScanModule.getChain()
 						.getId(), currentScanModule.getId(),
 						Event.ScheduleIncident.END);
 				this.appendScheduleEventCheckBox
 						.setSelection(this.currentScanModule.getChain()
 								.getScanDescription()
 								.getEventById(testEvent.getID()) != null);
 			} else {
 				this.appendScheduleEventCheckBox.setSelection(false);
 			}
 			checkForErrors();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void saveState(IMemento memento) {
 		// remember maximized states of sash form
 		memento.putBoolean("actionsCompositeMaximized",
 				this.actionsCompositeMaximized);
 		memento.putBoolean("eventsCompositeMaximized",
 				this.eventsCompositeMaximized);
 		
 		// save composite heights
 		memento.putInteger("actionsCompositeWeight", sashForm.getWeights()[0]);
 		memento.putInteger("eventsCompositeWeight", sashForm.getWeights()[1]);
 		
 		// remember sort state of action composite viewers
 		memento.putInteger("AxesSortState",
 				this.motorAxisComposite.getSortState());
 		memento.putInteger("ChannelSortState",
 				this.detectorChannelComposite.getSortState());
 	}
 
 	/*
 	 * 
 	 */
 	private void restoreState() {
 		if (memento == null) {
 			return;
 		}
 		
 		// restore maximized states
 		this.actionsCompositeMaximized = (memento
 				.getBoolean("actionsCompositeMaximized") == null) ? false
 				: memento.getBoolean("actionsCompositeMaximized");
 		this.eventsCompositeMaximized = (memento
 				.getBoolean("eventsCompositeMaximized") == null) ? false
 				: memento.getBoolean("eventsCompositeMaximized");
 		if (this.actionsCompositeMaximized) {
 			this.actionMaxIcon.setImage(restoreIcon);
 			this.sashForm.setMaximizedControl(actionsComposite);
 		}
 		if (this.eventsCompositeMaximized) {
 			this.eventMaxIcon.setImage(restoreIcon);
 			this.sashForm.setMaximizedControl(eventsComposite);
 		}
 		
 		// restore sash form weights
 		int[] weights = new int[2];
 		weights[0] = (memento.getInteger("actionsCompositeWeight") == null) 
 						? 1 : memento.getInteger("actionsCompositeWeight");
 		weights[1] = (memento.getInteger("eventsCompositeWeight") == null) 
 						? 1 : memento.getInteger("eventsCompositeWeight");
 		sashForm.setWeights(weights);
 		
 		// restore sort state of action composite viewers
 		if (memento.getInteger("AxesSortState") != null) {
 			this.motorAxisComposite.setSortState(memento
 					.getInteger("AxesSortState"));
 		}
 		if (memento.getInteger("ChannelSortState") != null) {
 			this.detectorChannelComposite.setSortState(memento
 					.getInteger("ChannelSortState"));
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void reset() {
 		this.setCurrentScanModule(null);
 	}
 
 	// ************************************************************************
 	// ******************** Listener ******************************************
 	// ************************************************************************
 
 	/**
 	 * @author Marcus Michalsky
 	 * @since 1.8
 	 */
 	private class ActionsMaxIconMouseListener extends MouseAdapter {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void mouseUp(MouseEvent e) {
 			if (actionsCompositeMaximized) {
 				actionMaxIcon.setImage(maximizeIcon);
 				actionMaxIcon.getParent().layout();
 				sashForm.setMaximizedControl(null);
 				actionsCompositeMaximized = false;
 			} else {
 				actionMaxIcon.setImage(restoreIcon);
 				sashForm.setMaximizedControl(actionsComposite);
 				actionsCompositeMaximized = true;
 			}
 		}
 	}
 
 	/**
 	 * @author Marcus Michalsky
 	 * @since 1.8
 	 */
 	private class EventMaxIconMouseListener extends MouseAdapter {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void mouseUp(MouseEvent e) {
 			if (eventsCompositeMaximized) {
 				eventMaxIcon.setImage(maximizeIcon);
 				eventMaxIcon.getParent().layout();
 				sashForm.setMaximizedControl(null);
 				eventsCompositeMaximized = false;
 			} else {
 				eventMaxIcon.setImage(restoreIcon);
 				sashForm.setMaximizedControl(eventsComposite);
 				eventsCompositeMaximized = true;
 			}
 		}
 	}
 
 	/**
 	 * @author Marcus Michalsky
 	 * @since 1.8
 	 */
 	private class TextFocusListener extends FocusAdapter {
 
 		private Text widget;
 
 		/**
 		 * @param widget the widget to observe
 		 */
 		public TextFocusListener(Text widget) {
 			this.widget = widget;
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void focusLost(FocusEvent e) {
 			if (this.widget == valueCountText) {
 				valueCountBinding.updateModelToTarget();
 			} else if (this.widget == triggerDelayText) {
 				triggerDelayBinding.updateModelToTarget();
 			} else if (this.widget == settleTimeText) {
 				settleTimeBinding.updateModelToTarget();
 			}
 		}
 	}
 
 	/**
 	 * {@link org.eclipse.swt.events.SelectionListener} of
 	 * <code>eventsTabFolder</code>.
 	 */
 	private class EventsTabFolderSelectionListener implements SelectionListener {
 
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
 			switch (eventsTabFolder.getSelectionIndex()) {
 			case 0:
 				selectionProviderWrapper
 						.setSelectionProvider(pauseEventComposite
 								.getTableViewer());
 				break;
 			case 1:
 				selectionProviderWrapper
 						.setSelectionProvider(redoEventComposite
 								.getTableViewer());
 				break;
 			case 2:
 				selectionProviderWrapper
 						.setSelectionProvider(breakEventComposite
 								.getTableViewer());
 				break;
 			case 3:
 				selectionProviderWrapper
 						.setSelectionProvider(triggerEventComposite
 								.getTableViewer());
 				break;
 			}
 		}
 	}
 
 	/**
 	 * {@link org.eclipse.swt.events.SelectionListener} of
 	 * <code> appendScheduleEventCheckBox</code>.
 	 */
 	private class AppendScheduleEventCheckBoxSelectionListener implements
 			SelectionListener {
 
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
 			Event scheduleEvent = new Event(currentScanModule.getChain()
 					.getId(), currentScanModule.getId(),
 					Event.ScheduleIncident.END);
 
 			if (appendScheduleEventCheckBox.getSelection()) {
 				if (currentScanModule.getChain().getScanDescription()
 						.getEventById(scheduleEvent.getID()) == null) {
 					currentScanModule.getChain().getScanDescription()
 							.add(scheduleEvent);
 				}
 			} else {
 				Event event = currentScanModule.getChain().getScanDescription()
 						.getEventById(scheduleEvent.getID());
 				if (event != null) {
 					// TODO
 					// check if this event is used by any ControlEvents
 					// and notify them, that we remove the event
 					currentScanModule.getChain().getScanDescription()
 							.remove(event);
 				}
 			}
 		}
 	}
 }
