 package de.ptb.epics.eve.editor.views.detectorchannelview;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.databinding.Binding;
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.UpdateValueStrategy;
 import org.eclipse.core.databinding.beans.BeansObservables;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
 import org.eclipse.jface.databinding.swt.SWTObservables;
 import org.eclipse.jface.databinding.viewers.ViewersObservables;
 import org.eclipse.jface.fieldassist.ControlDecoration;
 import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.custom.CTabItem;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.ExpandEvent;
 import org.eclipse.swt.events.ExpandListener;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.ExpandBar;
 import org.eclipse.swt.widgets.ExpandItem;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.layout.GridData;
 
 import de.ptb.epics.eve.data.measuringstation.Event;
 import de.ptb.epics.eve.data.scandescription.Channel;
 import de.ptb.epics.eve.data.scandescription.ScanModule;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ControlEventTypes;
 import de.ptb.epics.eve.data.scandescription.updatenotification.IModelUpdateListener;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ModelUpdateEvent;
 import de.ptb.epics.eve.editor.Activator;
 import de.ptb.epics.eve.editor.gef.editparts.ChainEditPart;
import de.ptb.epics.eve.editor.gef.editparts.ScanDescriptionEditPart;
 import de.ptb.epics.eve.editor.gef.editparts.ScanModuleEditPart;
 import de.ptb.epics.eve.editor.views.EditorViewPerspectiveListener;
 import de.ptb.epics.eve.editor.views.IEditorView;
 import de.ptb.epics.eve.editor.views.eventcomposite.EventComposite;
 import de.ptb.epics.eve.util.swt.TextSelectAllFocusListener;
 import de.ptb.epics.eve.util.swt.TextSelectAllMouseListener;
 
 /**
  * <code>DetectorChannelView</code> shows attributes of a
  * {@link de.ptb.epics.eve.data.measuringstation.DetectorChannel} and allows
  * modification.
  * 
  * @author Hartmut Scherr
  * @author Marcus Michalsky
  */
 public class DetectorChannelView extends ViewPart implements IEditorView,
 		ISelectionListener, PropertyChangeListener, IModelUpdateListener {
 
 	/**
 	 * the unique identifier of the view.
 	 */
 	public static final String ID = "de.ptb.epics.eve.editor.views.DetectorChannelView";
 
 	// logging
 	private static final Logger LOGGER = Logger
 			.getLogger(DetectorChannelView.class.getName());
 
 	// *******************************************************************
 	// ********************** underlying model ***************************
 	// *******************************************************************
 
 	// the detector channel containing the information that is shown and
 	// allowed for editing.
 	private Channel currentChannel;
 
 	private ScanModule scanModule;
 
 	private Channel[] availableDetectorChannels;
 
 	// *******************************************************************
 	// ****************** end of: underlying model ***********************
 	// *******************************************************************
 
 	private ScrolledComposite sc = null;
 	private Composite top = null;
 
 	private Label averageLabel;
 	private Text averageText;
 	private ControlDecoration averageTextDisabledControlDecoration;
 
 	private Label maxDeviationLabel;
 	private Text maxDeviationText;
 
 	private Label minimumLabel;
 	private Text minimumText;
 
 	private Label maxAttemptsLabel;
 	private Text maxAttemptsText;
 
 	private Label normalizeChannelLabel;
 	private Combo normalizeChannelCombo;
 	private NormalizeChannelComboSelectionListener normalizeChannelComboSelectionListener;
 
 	private Button deferredCheckBox;
 
 	private DataBindingContext context;
 
 	private ChannelSelectionProvider selectionProvider;
 
 	private IObservableValue selectionObservable;
 
 	private Binding averageBinding;
 	private IObservableValue averageTargetObservable;
 	private IObservableValue averageModelObservable;
 
 	private Binding maxDeviationBinding;
 	private IObservableValue maxDeviationTargetObservable;
 	private IObservableValue maxDeviationModelObservable;
 
 	private Binding minimumBinding;
 	private IObservableValue minimumTargetObservable;
 	private IObservableValue minimumModelObservable;
 
 	private Binding maxAttemptsBinding;
 	private IObservableValue maxAttemptsTargetObservable;
 	private IObservableValue maxAttemptsModelObservable;
 
 	private IObservableValue deferredTriggerTargetObservable;
 	private IObservableValue deferredTriggerModelObservable;
 
 	private ExpandBar bar;
 	private ExpandItem eventExpandItem;
 
 	/** */
 	public CTabFolder eventsTabFolder;
 	private EventComposite redoEventComposite;
 	private Composite eventComposite;
 
 	private CTabItem redoEventTabItem;
 
 	private Button detectorReadyEventCheckBox;
 	private DetectorReadyEventCheckBoxSelectionListener detectorReadyEventCheckBoxSelectionListener;
 
 	private Image infoImage;
 
 	private EditorViewPerspectiveListener editorViewPerspectiveListener;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void createPartControl(final Composite parent) {
 		parent.setLayout(new FillLayout());
 
 		if (Activator.getDefault().getMeasuringStation() == null) {
 			final Label errorLabel = new Label(parent, SWT.NONE);
 			errorLabel.setText("No Measuring Station has been loaded. "
 					+ "Please check Preferences!");
 			return;
 		}
 
 		this.infoImage = FieldDecorationRegistry.getDefault()
 				.getFieldDecoration(FieldDecorationRegistry.DEC_INFORMATION)
 				.getImage();
 
 		this.sc = new ScrolledComposite(parent, SWT.V_SCROLL);
 
 		this.top = new Composite(sc, SWT.NONE);
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 2;
 		this.top.setLayout(gridLayout);
 
 		this.sc.setExpandHorizontal(true);
 		this.sc.setExpandVertical(true);
 		this.sc.setContent(this.top);
 
 		// GUI: Average: <TextBox> x
 		this.averageLabel = new Label(this.top, SWT.NONE);
 		this.averageLabel.setText("Average:");
 		GridData gridData;
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		this.averageLabel.setLayoutData(gridData);
 
 		this.averageText = new Text(this.top, SWT.BORDER);
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.horizontalIndent = 7;
 		gridData.grabExcessHorizontalSpace = true;
 		this.averageText.setLayoutData(gridData);
 		this.averageText.addFocusListener(new TextSelectAllFocusListener(
 				this.averageText));
 		this.averageText.addMouseListener(new TextSelectAllMouseListener(
 				this.averageText));
 		this.averageText.addFocusListener(new TextFocusListener(
 				this.averageText));
 
 		this.averageTextDisabledControlDecoration = new ControlDecoration(
 				this.averageText, SWT.LEFT);
 		this.averageTextDisabledControlDecoration.setDescriptionText("");
 		this.averageTextDisabledControlDecoration.setImage(infoImage);
 		this.averageTextDisabledControlDecoration.hide();
 
 		// GUI: Max. Deviation (%): <TextBox> x
 		this.maxDeviationLabel = new Label(this.top, SWT.NONE);
 		this.maxDeviationLabel.setText("Max. Deviation (%):");
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		this.maxDeviationLabel.setLayoutData(gridData);
 
 		this.maxDeviationText = new Text(this.top, SWT.BORDER);
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.horizontalIndent = 7;
 		gridData.grabExcessHorizontalSpace = true;
 		this.maxDeviationText.setLayoutData(gridData);
 		this.maxDeviationText.addFocusListener(new TextSelectAllFocusListener(
 				this.maxDeviationText));
 		this.maxDeviationText.addMouseListener(new TextSelectAllMouseListener(
 				this.maxDeviationText));
 		this.maxDeviationText.addFocusListener(new TextFocusListener(
 				this.maxDeviationText));
 
 		// GUI: Minimum: <TextBox> x
 		this.minimumLabel = new Label(this.top, SWT.NONE);
 		this.minimumLabel.setText("Minimum:");
 		this.minimumLabel
 				.setToolTipText("for values < minimum no deviation check");
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		this.minimumLabel.setLayoutData(gridData);
 
 		this.minimumText = new Text(this.top, SWT.BORDER);
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.horizontalIndent = 7;
 		gridData.grabExcessHorizontalSpace = true;
 		this.minimumText.setLayoutData(gridData);
 		this.minimumText.addFocusListener(new TextSelectAllFocusListener(
 				this.minimumText));
 		this.minimumText.addMouseListener(new TextSelectAllMouseListener(
 				this.minimumText));
 		this.minimumText.addFocusListener(new TextFocusListener(
 				this.minimumText));
 
 		// GUI: Max. Attempts: <TextBox> x
 		this.maxAttemptsLabel = new Label(this.top, SWT.NONE);
 		this.maxAttemptsLabel.setText("Max. Attempts:");
 		this.maxAttemptsLabel
 				.setToolTipText("Maximum attemps to calculate deviation:");
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		this.maxAttemptsLabel.setLayoutData(gridData);
 
 		this.maxAttemptsText = new Text(this.top, SWT.BORDER);
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.horizontalIndent = 7;
 		gridData.grabExcessHorizontalSpace = true;
 		this.maxAttemptsText.setLayoutData(gridData);
 		this.maxAttemptsText.addFocusListener(new TextSelectAllFocusListener(
 				this.maxAttemptsText));
 		this.maxAttemptsText.addMouseListener(new TextSelectAllMouseListener(
 				this.maxAttemptsText));
 		this.maxAttemptsText.addFocusListener(new TextFocusListener(
 				this.maxAttemptsText));
 
 		//
 		this.normalizeChannelLabel = new Label(this.top, SWT.NONE);
 		this.normalizeChannelLabel.setText("Normalize Channel:");
 		this.normalizeChannelCombo = new Combo(this.top, SWT.BORDER
 				| SWT.READ_ONLY);
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.horizontalIndent = 7;
 		gridData.grabExcessHorizontalSpace = true;
 		this.normalizeChannelCombo.setLayoutData(gridData);
 		this.normalizeChannelComboSelectionListener = new NormalizeChannelComboSelectionListener();
 		this.normalizeChannelCombo
 				.addSelectionListener(normalizeChannelComboSelectionListener);
 
 		this.deferredCheckBox = new Button(this.top, SWT.CHECK);
 		this.deferredCheckBox.setText("Deferred Trigger");
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.horizontalSpan = 2;
 		gridData.grabExcessHorizontalSpace = true;
 		this.deferredCheckBox.setLayoutData(gridData);
 
 		// Expand Bar
 		this.bar = new ExpandBar(this.top, SWT.NONE);
 		gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.horizontalSpan = 2;
 		this.bar.setLayoutData(gridData);
 
 		this.bar.addExpandListener(new BarExpandListener());
 
 		// Event Section
 		this.eventComposite = new Composite(this.bar, SWT.NONE);
 		gridLayout = new GridLayout();
 		this.eventComposite.setLayout(gridLayout);
 
 		this.detectorReadyEventCheckBox = new Button(this.eventComposite,
 				SWT.CHECK);
 		this.detectorReadyEventCheckBox.setText("Send Detector Ready Event");
 		this.detectorReadyEventCheckBox
 				.setToolTipText("Mark to send detector ready event if channel is ready");
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		this.detectorReadyEventCheckBox.setLayoutData(gridData);
 		this.detectorReadyEventCheckBoxSelectionListener = new DetectorReadyEventCheckBoxSelectionListener();
 		this.detectorReadyEventCheckBox
 				.addSelectionListener(detectorReadyEventCheckBoxSelectionListener);
 
 		// Event Options Tab
 		eventsTabFolder = new CTabFolder(this.eventComposite, SWT.FLAT);
 		this.eventsTabFolder.setSimple(false);
 		this.eventsTabFolder.setBorderVisible(true);
 		gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.horizontalAlignment = GridData.FILL;
 		eventsTabFolder.setLayoutData(gridData);
 
 		redoEventComposite = new EventComposite(eventsTabFolder, SWT.NONE,
 				ControlEventTypes.CONTROL_EVENT, this);
 
 		this.redoEventTabItem = new CTabItem(eventsTabFolder, SWT.FLAT);
 		this.redoEventTabItem.setText("Redo");
 		this.redoEventTabItem.setToolTipText("Repeat the current reading "
 				+ "of the channel, if redo event occurs");
 		this.redoEventTabItem.setControl(redoEventComposite);
 
 		// expand item (Events)
 		this.eventExpandItem = new ExpandItem(this.bar, SWT.NONE, 0);
 		this.eventExpandItem.setText("Event options");
 		this.eventExpandItem.setHeight(this.eventComposite.computeSize(
 				SWT.DEFAULT, SWT.DEFAULT).y);
 		this.eventExpandItem.setControl(this.eventComposite);
 
 		this.sc.setMinSize(this.top.computeSize(SWT.DEFAULT, SWT.DEFAULT));
 
 		top.setVisible(false);
 
 		// set the table viewer of the event composite as selection provider
 		// (used by add/delete event commands)
 		this.getSite().setSelectionProvider(
 				this.redoEventComposite.getTableViewer());
 
 		// listen to selection changes (if a detector channel is selected, its
 		// attributes are made available for editing)
 		getSite().getWorkbenchWindow().getSelectionService()
 				.addSelectionListener(this);
 
 		// reset view if last editor was closed
 		this.editorViewPerspectiveListener = new EditorViewPerspectiveListener(
 				this);
 		PlatformUI.getWorkbench().getActiveWorkbenchWindow()
 				.addPerspectiveListener(this.editorViewPerspectiveListener);
 
 		this.bindValues();
 	}
 
 	// ************************************************************************
 	// ********************** end of createPartControl ************************
 	// ************************************************************************
 
 	/*
 	 * 
 	 */
 	private void bindValues() {
 		this.context = new DataBindingContext();
 
 		this.selectionProvider = new ChannelSelectionProvider();
 		this.selectionObservable = ViewersObservables
 				.observeSingleSelection(selectionProvider);
 
 		this.averageTargetObservable = SWTObservables.observeText(
 				this.averageText, SWT.Modify);
 		this.averageModelObservable = BeansObservables.observeDetailValue(
 				selectionObservable, Channel.class, "averageCount",
 				Integer.class);
 		UpdateValueStrategy averageTargetToModelStrategy = new UpdateValueStrategy(
 				UpdateValueStrategy.POLICY_UPDATE);
 		averageTargetToModelStrategy
 				.setAfterGetValidator(new AverageTargetToModelValidator());
 		averageTargetToModelStrategy
 				.setConverter(new AverageTargetToModelConverter());
 		this.averageBinding = context.bindValue(averageTargetObservable,
 				averageModelObservable, averageTargetToModelStrategy,
 				new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE));
 		ControlDecorationSupport.create(this.averageBinding, SWT.LEFT);
 
 		this.maxDeviationTargetObservable = SWTObservables.observeText(
 				this.maxDeviationText, SWT.Modify);
 		this.maxDeviationModelObservable = BeansObservables.observeDetailValue(
 				selectionObservable, Channel.class, "maxDeviation",
 				Double.class);
 		UpdateValueStrategy maxDeviationTargetToModelStrategy = new UpdateValueStrategy(
 				UpdateValueStrategy.POLICY_UPDATE);
 		maxDeviationTargetToModelStrategy
 				.setAfterGetValidator(new MaxDeviationTargetToModelValidator());
 		maxDeviationTargetToModelStrategy
 				.setConverter(new DoubleTargetToModelConverter());
 		UpdateValueStrategy maxDeviationModelToTargetStrategy = new UpdateValueStrategy(
 				UpdateValueStrategy.POLICY_UPDATE);
 		maxDeviationModelToTargetStrategy
 				.setConverter(new DoubleModelToTargetConverter());
 		this.maxDeviationBinding = context.bindValue(
 				maxDeviationTargetObservable, maxDeviationModelObservable,
 				maxDeviationTargetToModelStrategy,
 				maxDeviationModelToTargetStrategy);
 		ControlDecorationSupport.create(this.maxDeviationBinding, SWT.LEFT);
 
 		this.minimumTargetObservable = SWTObservables.observeText(
 				this.minimumText, SWT.Modify);
 		this.minimumModelObservable = BeansObservables.observeDetailValue(
 				selectionObservable, Channel.class, "minimum", Double.class);
 		UpdateValueStrategy minimumTargetToModelStrategy = new UpdateValueStrategy(
 				UpdateValueStrategy.POLICY_UPDATE);
 		minimumTargetToModelStrategy
 				.setAfterGetValidator(new MinimumTargetToModelValidator());
 		minimumTargetToModelStrategy
 				.setConverter(new DoubleTargetToModelConverter());
 		UpdateValueStrategy minimumModelToTargetStrategy = new UpdateValueStrategy(
 				UpdateValueStrategy.POLICY_UPDATE);
 		minimumModelToTargetStrategy
 				.setConverter(new DoubleModelToTargetConverter());
 		this.minimumBinding = context.bindValue(minimumTargetObservable,
 				minimumModelObservable, minimumTargetToModelStrategy,
 				minimumModelToTargetStrategy);
 		ControlDecorationSupport.create(this.minimumBinding, SWT.LEFT);
 
 		this.maxAttemptsTargetObservable = SWTObservables.observeText(
 				this.maxAttemptsText, SWT.Modify);
 		this.maxAttemptsModelObservable = BeansObservables.observeDetailValue(
 				selectionObservable, Channel.class, "maxAttempts",
 				Integer.class);
 		UpdateValueStrategy maxAttemptsTargetToModelStrategy = new UpdateValueStrategy(
 				UpdateValueStrategy.POLICY_UPDATE);
 		maxAttemptsTargetToModelStrategy
 				.setAfterGetValidator(new MaxAttemptsTargetToModelValidator());
 		maxAttemptsTargetToModelStrategy
 				.setConverter(new MaxAttemptsTargetToModelConverter());
 		UpdateValueStrategy maxAttemptsModelToTargetStrategy = new UpdateValueStrategy(
 				UpdateValueStrategy.POLICY_UPDATE);
 		maxAttemptsModelToTargetStrategy
 				.setConverter(new MaxAttemptsModelToTargetConverter());
 		this.maxAttemptsBinding = context.bindValue(
 				maxAttemptsTargetObservable, maxAttemptsModelObservable,
 				maxAttemptsTargetToModelStrategy,
 				maxAttemptsModelToTargetStrategy);
 		ControlDecorationSupport.create(this.maxAttemptsBinding, SWT.LEFT);
 
 		this.deferredTriggerTargetObservable = SWTObservables
 				.observeSelection(this.deferredCheckBox);
 		this.deferredTriggerModelObservable = BeansObservables
 				.observeDetailValue(selectionObservable, Channel.class,
 						"deferred", Boolean.class);
 		context.bindValue(deferredTriggerTargetObservable,
 				deferredTriggerModelObservable, new UpdateValueStrategy(
 						UpdateValueStrategy.POLICY_UPDATE),
 				new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setFocus() {
 		LOGGER.debug("got focus -> forward to top composite");
 		this.top.setFocus();
 	}
 
 	/**
 	 * Returns the {@link de.ptb.epics.eve.data.scandescription.Channel}
 	 * currently shown by this view.
 	 * 
 	 * @return the {@link de.ptb.epics.eve.data.scandescription.Channel}
 	 *         currently shown
 	 */
 	public Channel getCurrentChannel() {
 		return this.currentChannel;
 	}
 
 	/**
 	 * Sets the {@link de.ptb.epics.eve.data.scandescription.Channel} (the
 	 * underlying model whose contents is presented by this view).
 	 * 
 	 * @param channel
 	 *            the {@link de.ptb.epics.eve.data.scandescription.Channel} that
 	 *            should be set
 	 */
 	private void setChannel(final Channel channel) {
 		if (channel != null) {
 			LOGGER.debug("set channel ("
 					+ channel.getAbstractDevice().getFullIdentifyer() + ")");
 		} else {
 			LOGGER.debug("set channel (null)");
 		}
 		if (this.currentChannel != null) {
 			this.currentChannel.removeModelUpdateListener(this);
 			this.scanModule.removePropertyChangeListener("removeChannel", this);
 		}
 		// update the underlying model to the new one
 		this.currentChannel = channel;
 		this.scanModule = null;
 
 		if (this.currentChannel != null) {
 			this.currentChannel.addModelUpdateListener(this);
 			this.scanModule = this.currentChannel.getScanModule();
 			this.scanModule.addPropertyChangeListener("removeChannel", this);
 		}
 		updateEvent(null);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void reset() {
 		this.setChannel(null);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
 		LOGGER.debug("selection changed");
 
 		if (selection instanceof IStructuredSelection) {
 			if (((IStructuredSelection) selection).size() == 0) {
 				return;
 			}
 			// since at any given time this view can only display the attributes
 			// of one detector channel, we take the first element of the
 			// selection
 			Object o = ((IStructuredSelection) selection).toList().get(0);
 			if (o instanceof Channel) {
 				// set new Channel
 				if (LOGGER.isDebugEnabled()) {
 					LOGGER.debug("Channel: "
 							+ ((Channel) o).getDetectorChannel()
 									.getFullIdentifyer() + " selected.");
 				}
 				setChannel((Channel) o);
 			} else if (o instanceof ScanModuleEditPart) {
 				// ScanModule was selected
 				if (LOGGER.isDebugEnabled()) {
 					LOGGER.debug("selection is ScanModuleEditPart: " + o);
 					LOGGER.debug("ScanModule: "
 							+ ((ScanModuleEditPart) o).getModel().getId()
 							+ " selected.");
 				}
 
 				if (this.scanModule != null
 						&& !this.scanModule.equals(((ScanModuleEditPart) o)
 								.getModel())) {
 					setChannel(null);
 				}
 			} else if (o instanceof ChainEditPart) {
 				LOGGER.debug("selection is ChainEditPart: " + o);
 				setChannel(null);
			} else if (o instanceof ScanDescriptionEditPart) {
				LOGGER.debug("selection is ScanDescriptionEditPart: " + o);
				setChannel(null);
 			} else {
 				LOGGER.debug("unknown selection -> ignore");
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void propertyChange(PropertyChangeEvent e) {
 		if (e.getOldValue().equals(currentChannel)) {
 			// current Axis will be removed
 			setChannel(null);
 		}
 	}
 
 	/*
 	 * 
 	 */
 	private void checkForErrors() {
 		if (this.currentChannel.getRedoControlEventManager().getModelErrors()
 				.size() > 0) {
 			this.redoEventTabItem.setImage(PlatformUI.getWorkbench()
 					.getSharedImages()
 					.getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
 		} else {
 			this.redoEventTabItem.setImage(null);
 		}
 	}
 
 	/*
 	 * 
 	 */
 	private void addListeners() {
 		normalizeChannelCombo
 				.addSelectionListener(normalizeChannelComboSelectionListener);
 
 		detectorReadyEventCheckBox
 				.addSelectionListener(detectorReadyEventCheckBoxSelectionListener);
 	}
 
 	/*
 	 * 
 	 */
 	private void removeListeners() {
 		normalizeChannelCombo
 				.removeSelectionListener(normalizeChannelComboSelectionListener);
 
 		detectorReadyEventCheckBox
 				.removeSelectionListener(detectorReadyEventCheckBoxSelectionListener);
 	}
 
 	private void suspendModelUpdateListener() {
 		this.currentChannel.removeModelUpdateListener(this);
 	}
 
 	private void resumeModelUpdateListener() {
 		this.currentChannel.addModelUpdateListener(this);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void updateEvent(ModelUpdateEvent modelUpdateEvent) {
 		removeListeners();
 
 		if (this.currentChannel != null) {
 			// current channel set -> update widgets
 			top.setVisible(true);
 
 			// set the view title
 			this.setPartName(currentChannel.getAbstractDevice().getName());
 
 			// fill combo box
 			List<Channel> channels = new ArrayList<Channel>();
 			for (Channel ch : this.scanModule.getChannels()) {
 				if (ch.getNormalizeChannel() != null) {
 					continue;
 				}
 				if (ch.getDetectorChannel()
 						.getID()
 						.equals(this.currentChannel.getDetectorChannel()
 								.getID())) {
 					continue;
 				}
 				channels.add(ch);
 			}
 			this.availableDetectorChannels = channels.toArray(new Channel[0]);
 			String[] detectorItems = new String[availableDetectorChannels.length];
 			for (int i = 0; i < availableDetectorChannels.length; ++i) {
 				detectorItems[i] = availableDetectorChannels[i]
 						.getDetectorChannel().getName();
 			}
 			this.normalizeChannelCombo.setItems(detectorItems);
 			this.normalizeChannelCombo.add("none");
 
 			if (this.currentChannel.getNormalizeChannel() != null) {
 				this.normalizeChannelCombo.setText(this.currentChannel
 						.getNormalizeChannel().getName());
 			}
 
 			// set detector ready event check box
 			this.detectorReadyEventCheckBox.setSelection(this.currentChannel
 					.getDetectorReadyEvent() != null);
 
 			this.redoEventComposite.setControlEventManager(this.currentChannel
 					.getRedoControlEventManager());
 
 			this.sc.setMinSize(this.top.computeSize(SWT.DEFAULT, SWT.DEFAULT));
 
 			checkForErrors();
 
 			this.averageLabel.setEnabled(true);
 			this.averageText.setEnabled(true);
 			this.maxDeviationLabel.setEnabled(true);
 			this.maxDeviationText.setEnabled(true);
 			this.minimumLabel.setEnabled(true);
 			this.minimumText.setEnabled(true);
 			this.maxAttemptsLabel.setEnabled(true);
 			this.maxAttemptsText.setEnabled(true);
 			this.normalizeChannelLabel.setEnabled(true);
 			this.normalizeChannelCombo.setEnabled(true);
 			this.bar.setEnabled(true);
 			this.detectorReadyEventCheckBox.setEnabled(true);
 			this.eventsTabFolder.setEnabled(true);
 			this.redoEventComposite.getTableViewer().getTable()
 					.setEnabled(true);
 
 			this.averageTextDisabledControlDecoration.hide();
 
 			// disable fields if channel is used as a normalize channel
 			for (Channel ch : this.scanModule.getChannels()) {
 				if (ch.getNormalizeChannel() == null) {
 					continue;
 				}
 				if (ch.getNormalizeChannel()
 						.getID()
 						.equals(this.currentChannel.getDetectorChannel()
 								.getID())) {
 					String message = this.currentChannel.getDetectorChannel()
 							.getName()
 							+ " is used as normalize channel for "
 							+ ch.getDetectorChannel().getName();
 					this.averageTextDisabledControlDecoration
 							.setDescriptionText(message);
 					this.averageTextDisabledControlDecoration.show();
 
 					this.averageLabel.setEnabled(false);
 					this.averageText.setEnabled(false);
 					this.maxDeviationLabel.setEnabled(false);
 					this.maxDeviationText.setEnabled(false);
 					this.minimumLabel.setEnabled(false);
 					this.minimumText.setEnabled(false);
 					this.maxAttemptsLabel.setEnabled(false);
 					this.maxAttemptsText.setEnabled(false);
 					this.normalizeChannelLabel.setEnabled(false);
 					this.normalizeChannelCombo.setEnabled(false);
 					this.bar.setEnabled(false);
 					this.detectorReadyEventCheckBox.setEnabled(false);
 
 					this.eventsTabFolder.setEnabled(false);
 					this.redoEventComposite.getTableViewer().getTable()
 							.setEnabled(false);
 				}
 			}
 		} else {
 			// this.currentChannel == null (no channel selected)
 			this.setPartName("No Detector Channel selected");
 
 			this.detectorReadyEventCheckBox.setSelection(false);
 
 			this.redoEventComposite.setControlEventManager(null);
 
 			top.setVisible(false);
 		}
 		// re-enable listeners
 		addListeners();
 	}
 
 	/* ********************************************************************* */
 	/* ******************************* Listeners *************************** */
 	/* ********************************************************************* */
 
 	/**
 	 * @author Marcus Michalsky
 	 * @since 1.8
 	 */
 	private class TextFocusListener implements FocusListener {
 
 		private Text widget;
 
 		/**
 		 * @param widget
 		 *            the widget to observe
 		 */
 		public TextFocusListener(Text widget) {
 			this.widget = widget;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void focusGained(FocusEvent e) {
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void focusLost(FocusEvent e) {
 			if (widget == averageText) {
 				averageText.setSelection(0, 0);
 				averageBinding.updateModelToTarget();
 			} else if (widget == maxDeviationText) {
 				maxDeviationText.setSelection(0, 0);
 				maxDeviationBinding.updateModelToTarget();
 			} else if (widget == minimumText) {
 				minimumText.setSelection(0, 0);
 				minimumBinding.updateModelToTarget();
 			} else if (widget == maxAttemptsText) {
 				maxAttemptsText.setSelection(0, 0);
 				maxAttemptsBinding.updateModelToTarget();
 			}
 		}
 	}
 
 	/**
 	 * {@link org.eclipse.swt.events.SelectionListener} of
 	 * normalizeChannelCombo.
 	 * 
 	 * @author Marcus Michalsky
 	 * @since 1.2
 	 */
 	private class NormalizeChannelComboSelectionListener implements
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
 			suspendModelUpdateListener();
 			if (normalizeChannelCombo.getText().equals("none")) {
 				currentChannel.setNormalizeChannel(null);
 				normalizeChannelCombo.deselectAll();
 			} else {
 				Channel normCh = availableDetectorChannels[normalizeChannelCombo
 						.getSelectionIndex()];
 				normCh.reset();
 				currentChannel.setNormalizeChannel(normCh.getDetectorChannel());
 			}
 			resumeModelUpdateListener();
 		}
 	}
 
 	/**
 	 * {@link org.eclipse.swt.events.SelectionListener} of
 	 * <code>detectorReadyEventCheckBox</code>.
 	 */
 	private class DetectorReadyEventCheckBoxSelectionListener implements
 			SelectionListener {
 
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
 			LOGGER.debug("send detector ready event modified");
 			// we create an event and add it to the list if selected
 			// or remove the event with same id from the list if deselected
 			Event detReadyEvent = new Event(currentChannel.getAbstractDevice()
 					.getID(), currentChannel.getAbstractDevice().getParent()
 					.getName(), currentChannel.getAbstractDevice().getName(),
 					currentChannel.getScanModule().getChain().getId(),
 					currentChannel.getScanModule().getId());
 
 			if (detectorReadyEventCheckBox.getSelection()) {
 				currentChannel.getScanModule().getChain().getScanDescription()
 						.add(detReadyEvent);
 				currentChannel.setDetectorReadyEvent(detReadyEvent);
 			} else {
 				currentChannel.getScanModule().getChain().getScanDescription()
 						.removeEventById(detReadyEvent.getID());
 				currentChannel.setDetectorReadyEvent(null);
 			}
 		}
 	}
 
 	/**
 	 * {@link org.eclipse.swt.events.ExpandListener} of bar.
 	 * 
 	 * @author Marcus Michalsky
 	 * @since 1.4
 	 */
 	private class BarExpandListener implements ExpandListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void itemCollapsed(ExpandEvent e) {
 			LOGGER.debug("collapse");
 			Point topPoint = top.computeSize(SWT.DEFAULT, SWT.DEFAULT);
 			Point eventPoint = eventComposite.computeSize(SWT.DEFAULT,
 					SWT.DEFAULT);
 			sc.setMinSize(topPoint.x, topPoint.y - eventPoint.y);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void itemExpanded(ExpandEvent e) {
 			LOGGER.debug("expand");
 			Point topPoint = top.computeSize(SWT.DEFAULT, SWT.DEFAULT);
 			Point eventPoint = eventComposite.computeSize(SWT.DEFAULT,
 					SWT.DEFAULT);
 			sc.setMinSize(topPoint.x, topPoint.y + eventPoint.y);
 		}
 	}
 }
