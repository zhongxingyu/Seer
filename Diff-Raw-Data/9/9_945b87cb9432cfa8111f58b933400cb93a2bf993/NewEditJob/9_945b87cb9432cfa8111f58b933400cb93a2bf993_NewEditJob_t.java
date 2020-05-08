 package com.netxforge.netxstudio.screens.f4;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.eclipse.core.databinding.conversion.Converter;
 import org.eclipse.core.databinding.conversion.IConverter;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.core.databinding.observable.value.IValueChangeListener;
 import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
 import org.eclipse.core.databinding.observable.value.WritableValue;
 import org.eclipse.core.databinding.validation.IValidator;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.databinding.EMFDataBindingContext;
 import org.eclipse.emf.databinding.EMFProperties;
 import org.eclipse.emf.databinding.EMFUpdateValueStrategy;
 import org.eclipse.emf.databinding.IEMFListProperty;
 import org.eclipse.emf.databinding.IEMFValueProperty;
 import org.eclipse.emf.databinding.edit.EMFEditProperties;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.edit.command.AddCommand;
 import org.eclipse.jface.databinding.swt.ISWTObservableValue;
 import org.eclipse.jface.databinding.swt.SWTObservables;
 import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
 import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
 import org.eclipse.jface.databinding.viewers.ViewersObservables;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ComboViewer;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.nebula.widgets.cdatetime.CDT;
 import org.eclipse.nebula.widgets.cdatetime.CDateTime;
 import org.eclipse.nebula.widgets.datechooser.DateChooserCombo;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.forms.widgets.Form;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.Section;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.netxforge.netxstudio.scheduling.Job;
 import com.netxforge.netxstudio.scheduling.JobState;
 import com.netxforge.netxstudio.scheduling.MetricSourceJob;
 import com.netxforge.netxstudio.scheduling.NodeReporterJob;
 import com.netxforge.netxstudio.scheduling.NodeTypeReporterJob;
 import com.netxforge.netxstudio.scheduling.OperatorReporterJob;
 import com.netxforge.netxstudio.scheduling.RFSServiceMonitoringJob;
 import com.netxforge.netxstudio.scheduling.RFSServiceReporterJob;
 import com.netxforge.netxstudio.scheduling.RetentionJob;
 import com.netxforge.netxstudio.scheduling.SchedulingPackage;
 import com.netxforge.netxstudio.screens.AbstractScreen;
 import com.netxforge.netxstudio.screens.CDateTimeObservableValue;
 import com.netxforge.netxstudio.screens.common.util.DateChooserComboObservableValue;
import com.netxforge.netxstudio.screens.common.util.ValidationService;
 import com.netxforge.netxstudio.screens.editing.selector.IDataScreenInjection;
 import com.netxforge.netxstudio.screens.editing.selector.ScreenUtil;
 import com.netxforge.netxstudio.screens.internal.ScreensActivator;
 
 public class NewEditJob extends AbstractScreen implements IDataScreenInjection {
 
 	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
 	private Text txtJobName;
 	private Text txtOccurences;
 	private Table table;
 	private Job job;
 	// private Job original;
 	private Resource owner;
 	private ComboViewer comboViewerOn;
 	private ComboViewer comboViewerEvery;
 
 	private static final Object[] ComboStartInput = new Object[] { "Week",
 			"Day", "Hour" };
 
 	private CDateTime cdateTimeStartTime;
 	private DateChooserCombo dateChooserStartsOn;
 	private DateChooserCombo dateChooserEndsOn;
 	private Form frmNewJob;
 	private Button btnOn;
 	private Button btnAfter;
 	private Button btnNever;
 	private TableViewer occurencesTableViewer;
 
 	private EMFDataBindingContext bindingContext;
 	private WritableValue comboViewerOnWritableValue;
 	private WritableValue comboViewerEveryWritableValue;
 	private WritableValue cdateTimeStartTimeWritableValue;
 	private WritableValue dateChooserStartsOnWritableValue;
 	private WritableValue dateChooserEndsOnWritableValue;
 	private WritableValue btnOnWritableValue;
 	private WritableValue btnAfterWritableValue;
 	private WritableValue btnNeverWritableValue;
 	private WritableValue txtOccurencesWritableValue;
 	private Button btnActive;
 	private String[] jobTypes;
 	private IViewerObservableValue comboViewerOnObserveSingleSelection;
 	private IViewerObservableValue comboViewerEveryObserveSingleSelection;
 	private ISWTObservableValue comboViewerEveryObserveText;
 	private CDateTimeObservableValue comboObserveStartTime;
 	private DateChooserComboObservableValue dateChooseObserveStartDate;
 	private DateChooserComboObservableValue dateChooseObserveEndDate;
 	private ISWTObservableValue endOnObservable;
 	private ISWTObservableValue endOccurencesObservable;
 	private ISWTObservableValue endNeverObservable;
 	private ISWTObservableValue occurenceObservable;
 	private Table tableJobObjects;
 	private TableViewer tableViewerJobObjects;
 
 	public NewEditJob(Composite parent, int style) {
 		super(parent, style);
 
 		addDisposeListener(new DisposeListener() {
 			public void widgetDisposed(DisposeEvent e) {
 				toolkit.dispose();
 				validationService.dispose();
 				validationService.removeValidationListener(NewEditJob.this);
 			}
 		});
 		toolkit.adapt(this);
 		toolkit.paintBordersFor(this);
 //		buildUI();
 	}
 
 	private void buildUI() {
 		setLayout(new FillLayout(SWT.HORIZONTAL));
 
 		frmNewJob = toolkit.createForm(this);
 		frmNewJob.setSeparatorVisible(true);
 		toolkit.paintBordersFor(frmNewJob);
 		frmNewJob.getBody().setLayout(new FormLayout());
 
 		Section sctnDetails = toolkit.createSection(frmNewJob.getBody(),
 				Section.EXPANDED | Section.TITLE_BAR);
 		FormData fd_sctnDetails = new FormData();
 		fd_sctnDetails.top = new FormAttachment(0, 12);
 		fd_sctnDetails.left = new FormAttachment(0, 12);
 		sctnDetails.setLayoutData(fd_sctnDetails);
 		toolkit.paintBordersFor(sctnDetails);
 		sctnDetails.setText("Job Details");
 
 		Composite compositeDetails = toolkit.createComposite(sctnDetails,
 				SWT.NONE);
 		toolkit.paintBordersFor(compositeDetails);
 		sctnDetails.setClient(compositeDetails);
 		compositeDetails.setLayout(new GridLayout(2, false));
 
 		btnActive = toolkit.createButton(compositeDetails, "Active", SWT.CHECK);
 		btnActive.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
 				false, 2, 1));
 
 		Label lblJobName = toolkit.createLabel(compositeDetails, "Name:",
 				SWT.NONE);
 		lblJobName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
 				false, 1, 1));
 		lblJobName.setAlignment(SWT.RIGHT);
 
 		txtJobName = toolkit.createText(compositeDetails, "New Text", SWT.NONE);
 		txtJobName.setText("");
 		GridData gd_txtJobName = new GridData(SWT.FILL, SWT.CENTER, false,
 				false, 1, 1);
 		gd_txtJobName.widthHint = 100;
 		txtJobName.setLayoutData(gd_txtJobName);
 
 		// SECTION START OF JOB
 
 		Section sctnRecurrence = toolkit.createSection(frmNewJob.getBody(),
 				Section.EXPANDED | Section.TITLE_BAR);
 		fd_sctnDetails.right = new FormAttachment(sctnRecurrence, -6);
 		FormData fd_sctnRecurrence = new FormData();
 		fd_sctnRecurrence.right = new FormAttachment(100, -12);
 		fd_sctnRecurrence.left = new FormAttachment(0, 295);
 		fd_sctnRecurrence.bottom = new FormAttachment(0, 129);
 		fd_sctnRecurrence.top = new FormAttachment(0, 12);
 		sctnRecurrence.setLayoutData(fd_sctnRecurrence);
 		toolkit.paintBordersFor(sctnRecurrence);
 		sctnRecurrence.setText("Start");
 
 		Composite compositeRecurrence = toolkit.createComposite(sctnRecurrence,
 				SWT.NONE);
 		toolkit.paintBordersFor(compositeRecurrence);
 		sctnRecurrence.setClient(compositeRecurrence);
 		compositeRecurrence.setLayout(new GridLayout(6, false));
 
 		Label lblOn = toolkit.createLabel(compositeRecurrence, "On:", SWT.NONE);
 		lblOn.setAlignment(SWT.RIGHT);
 		GridData gd_lblOn = new GridData(SWT.RIGHT, SWT.CENTER, false, false,
 				1, 1);
 		gd_lblOn.widthHint = 70;
 		lblOn.setLayoutData(gd_lblOn);
 
 		comboViewerOn = new ComboViewer(compositeRecurrence, SWT.NONE);
 		Combo comboOn = comboViewerOn.getCombo();
 		GridData gd_comboOn = new GridData(SWT.FILL, SWT.CENTER, false, false,
 				3, 1);
 		gd_comboOn.widthHint = 150;
 		comboOn.setLayoutData(gd_comboOn);
 		toolkit.paintBordersFor(comboOn);
 
 		Label lblAt = toolkit.createLabel(compositeRecurrence, "At:", SWT.NONE);
 		lblAt.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
 				1, 1));
 
 		cdateTimeStartTime = new CDateTime(compositeRecurrence, CDT.BORDER
 				| CDT.CLOCK_24_HOUR | CDT.DROP_DOWN);
 		GridData gd_cdateTimeStartTime = new GridData(SWT.FILL, SWT.CENTER,
 				false, false, 1, 1);
 		gd_cdateTimeStartTime.widthHint = 150;
 		cdateTimeStartTime.setLayoutData(gd_cdateTimeStartTime);
 		cdateTimeStartTime.setPattern("HH:mm");
 
 		toolkit.adapt(cdateTimeStartTime);
 		toolkit.paintBordersFor(cdateTimeStartTime);
 
 		Label lblEvery = toolkit.createLabel(compositeRecurrence, "Every:",
 				SWT.NONE);
 		lblEvery.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
 				false, 1, 1));
 		lblEvery.setAlignment(SWT.RIGHT);
 
 		comboViewerEvery = new ComboViewer(compositeRecurrence, SWT.NONE);
 		Combo comboEvery = comboViewerEvery.getCombo();
 		GridData gd_comboEvery = new GridData(SWT.FILL, SWT.CENTER, false,
 				false, 3, 1);
 		gd_comboEvery.widthHint = 150;
 		comboEvery.setLayoutData(gd_comboEvery);
 		toolkit.paintBordersFor(comboEvery);
 		new Label(compositeRecurrence, SWT.NONE);
 		new Label(compositeRecurrence, SWT.NONE);
 
 		Label lblStartsOn = toolkit.createLabel(compositeRecurrence, "From:",
 				SWT.NONE);
 		GridData gd_lblStartsOn = new GridData(SWT.RIGHT, SWT.CENTER, false,
 				false, 1, 1);
 		gd_lblStartsOn.widthHint = 70;
 		lblStartsOn.setLayoutData(gd_lblStartsOn);
 		lblStartsOn.setAlignment(SWT.RIGHT);
 
 		dateChooserStartsOn = new DateChooserCombo(compositeRecurrence,
 				SWT.BORDER | SWT.FLAT);
 		GridData gd_dateChooserStartsOn = new GridData(SWT.LEFT, SWT.CENTER,
 				false, false, 3, 1);
 		gd_dateChooserStartsOn.heightHint = 19;
 		gd_dateChooserStartsOn.widthHint = 150;
 		dateChooserStartsOn.setLayoutData(gd_dateChooserStartsOn);
 		toolkit.adapt(dateChooserStartsOn);
 		toolkit.paintBordersFor(dateChooserStartsOn);
 		new Label(compositeRecurrence, SWT.NONE);
 		new Label(compositeRecurrence, SWT.NONE);
 
 		// SECTION END OF JOB
 
 		Section sctnJobEnd = toolkit.createSection(frmNewJob.getBody(),
 				Section.EXPANDED | Section.TITLE_BAR);
 		FormData fd_sctnJobEnd = new FormData();
 		fd_sctnJobEnd.top = new FormAttachment(sctnRecurrence, 12);
 		fd_sctnJobEnd.left = new FormAttachment(0, 295);
 		fd_sctnJobEnd.right = new FormAttachment(100, -12);
 		sctnJobEnd.setLayoutData(fd_sctnJobEnd);
 		toolkit.paintBordersFor(sctnJobEnd);
 		sctnJobEnd.setText("End");
 		sctnJobEnd.setExpanded(true);
 
 		Composite compositeEndsOn = toolkit.createComposite(sctnJobEnd,
 				SWT.NONE);
 		toolkit.paintBordersFor(compositeEndsOn);
 		sctnJobEnd.setClient(compositeEndsOn);
 		compositeEndsOn.setLayout(new GridLayout(3, false));
 
 		btnOn = toolkit.createButton(compositeEndsOn, "On: ", SWT.RADIO);
 		// btnOn.addSelectionListener(new SelectionAdapter() {
 		// @Override
 		// public void widgetSelected(SelectionEvent e) {
 		//
 		// }
 		// });
 
 		GridData gd_btnOn = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1,
 				1);
 		gd_btnOn.widthHint = 70;
 		btnOn.setLayoutData(gd_btnOn);
 
 		dateChooserEndsOn = new DateChooserCombo(compositeEndsOn, SWT.BORDER
 				| SWT.FLAT);
 		GridData gd_dateChooserEndsOn = new GridData(SWT.LEFT, SWT.CENTER,
 				false, false, 2, 1);
 		gd_dateChooserEndsOn.heightHint = 19;
 		gd_dateChooserEndsOn.widthHint = 150;
 		dateChooserEndsOn.setLayoutData(gd_dateChooserEndsOn);
 		toolkit.adapt(dateChooserEndsOn);
 		toolkit.paintBordersFor(dateChooserEndsOn);
 
 		btnAfter = toolkit.createButton(compositeEndsOn, "After:", SWT.RADIO);
 
 		txtOccurences = toolkit.createText(compositeEndsOn, "New Text",
 				SWT.NONE);
 		GridData gd_txtOccurences = new GridData(SWT.FILL, SWT.CENTER, false,
 				false, 1, 1);
 		gd_txtOccurences.widthHint = 15;
 		txtOccurences.setLayoutData(gd_txtOccurences);
 		txtOccurences.setText("");
 
 		toolkit.createLabel(compositeEndsOn, "Repeats", SWT.NONE);
 
 		btnNever = toolkit.createButton(compositeEndsOn, "Never", SWT.RADIO);
 		btnNever.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false,
 				2, 1));
 		new Label(compositeEndsOn, SWT.NONE);
 		btnNever.setSelection(true);
 
 		// SECTION SUMMARY
 
 		Section sctnSummary = toolkit.createSection(frmNewJob.getBody(),
 				Section.EXPANDED | Section.TITLE_BAR);
 		fd_sctnDetails.bottom = new FormAttachment(sctnSummary, -12);
 		fd_sctnJobEnd.bottom = new FormAttachment(sctnSummary, -12);
 		FormData fd_sctnSummary = new FormData();
 		fd_sctnSummary.top = new FormAttachment(0, 260);
 		fd_sctnSummary.bottom = new FormAttachment(100, -12);
 		fd_sctnSummary.left = new FormAttachment(0, 12);
 		fd_sctnSummary.right = new FormAttachment(100, -12);
 		sctnSummary.setLayoutData(fd_sctnSummary);
 		toolkit.paintBordersFor(sctnSummary);
 		sctnSummary.setText("Summary");
 
 		Composite composite_3 = toolkit.createComposite(sctnSummary, SWT.NONE);
 		toolkit.paintBordersFor(composite_3);
 		sctnSummary.setClient(composite_3);
 		composite_3.setLayout(new GridLayout(1, false));
 
 		occurencesTableViewer = new TableViewer(composite_3, SWT.BORDER
 				| SWT.FULL_SELECTION);
 		table = occurencesTableViewer.getTable();
 		table.setLinesVisible(true);
 		table.setHeaderVisible(true);
 		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 4));
 		toolkit.paintBordersFor(table);
 
 		TableViewerColumn tableViewerColumn = new TableViewerColumn(
 				occurencesTableViewer, SWT.NONE);
 		TableColumn tblclmnDate = tableViewerColumn.getColumn();
 		tblclmnDate.setWidth(100);
 		tblclmnDate.setText("Date");
 
 		TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(
 				occurencesTableViewer, SWT.NONE);
 		TableColumn tblclmnTime = tableViewerColumn_1.getColumn();
 		tblclmnTime.setWidth(100);
 		tblclmnTime.setText("Time");
 
 		occurencesTableViewer.setContentProvider(new ArrayContentProvider());
 		occurencesTableViewer.setLabelProvider(new OccurenceLabelProvider());
 
 		tableViewerJobObjects = new TableViewer(compositeDetails, SWT.BORDER
 				| SWT.FULL_SELECTION);
 		tableJobObjects = tableViewerJobObjects.getTable();
 		tableJobObjects.setHeaderVisible(true);
 		tableJobObjects.setLinesVisible(true);
 		tableJobObjects.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
 				true, 2, 1));
 		toolkit.paintBordersFor(tableJobObjects);
 
 		TableViewerColumn tableViewerColumn_2 = new TableViewerColumn(
 				tableViewerJobObjects, SWT.NONE);
 		TableColumn tblclmnObject = tableViewerColumn_2.getColumn();
 		tblclmnObject.setWidth(135);
 		tblclmnObject.setText("Object");
 
 		TableViewerColumn tableViewerColumn_3 = new TableViewerColumn(
 				tableViewerJobObjects, SWT.NONE);
 		TableColumn tblclmnId = tableViewerColumn_3.getColumn();
 		tblclmnId.setWidth(120);
 		tblclmnId.setText("ID");
 	}
 
 	/**
 	 * Shows date occurences.
 	 * 
 	 * @author Christophe Bouhier
 	 */
 	class OccurenceLabelProvider extends LabelProvider implements
 			ITableLabelProvider {
 
 		public Image getColumnImage(Object element, int columnIndex) {
 			return null;
 		}
 
 		public String getColumnText(Object element, int columnIndex) {
 			Date d = (Date) element;
 			switch (columnIndex) {
 			case 0: {
 				return modelUtils.date(d);
 			}
 			case 1: {
 				return modelUtils.time(d);
 			}
 			}
 
 			return null;
 		}
 
 	}
 
 	public EMFDataBindingContext initDataBindings_() {
 		EMFDataBindingContext bindingContext = new EMFDataBindingContext();
 
 		// JOB_STATE
 		EMFUpdateValueStrategy jobStateModelToTargetStrategy = new EMFUpdateValueStrategy();
 		jobStateModelToTargetStrategy.setConverter(new IConverter() {
 
 			public Object getFromType() {
 				return JobState.class;
 			}
 
 			public Object getToType() {
 				return Boolean.class;
 			}
 
 			public Object convert(Object fromObject) {
 				JobState current = (JobState) fromObject;
 				if (current.getValue() == JobState.ACTIVE_VALUE) {
 					return Boolean.TRUE;
 				} else {
 					return Boolean.FALSE;
 				}
 			}
 
 		});
 
 		EMFUpdateValueStrategy jobStateTargetToModelStrategy = new EMFUpdateValueStrategy();
 		jobStateTargetToModelStrategy.setConverter(new IConverter() {
 
 			public Object getFromType() {
 				return Boolean.class;
 			}
 
 			public Object getToType() {
 				return JobState.class;
 			}
 
 			public Object convert(Object fromObject) {
 				Boolean current = (Boolean) fromObject;
 				if (current) {
 					return JobState.ACTIVE;
 				} else {
 					return JobState.IN_ACTIVE;
 				}
 			}
 		});
 
 		IObservableValue jobStateObservable = SWTObservables
 				.observeSelection(btnActive);
 		IEMFValueProperty jobStateProperty = EMFEditProperties.value(
 				editingService.getEditingDomain(),
 				SchedulingPackage.Literals.JOB__JOB_STATE);
 		bindingContext.bindValue(jobStateObservable,
 				jobStateProperty.observe(job), jobStateTargetToModelStrategy,
 				jobStateModelToTargetStrategy);
 
 		// JOB_NAME
		EMFUpdateValueStrategy nameStrategy = ValidationService.getStrategyfactory()
				.strategyBeforeSetStringNotEmpty("Name is required");
 
 		IObservableValue textObserveJobName = SWTObservables
 				.observeDelayedValue(400,
 						SWTObservables.observeText(this.txtJobName, SWT.Modify));
 
 		IEMFValueProperty textJobNameValue = EMFProperties
 				.value(SchedulingPackage.Literals.JOB__NAME);
 		bindingContext.bindValue(textObserveJobName,
 				textJobNameValue.observe(job), nameStrategy, null);
 
 		// ////////////////////////////////////////////////////////
 		// WRITABLE OBSERVABLES
 		// All these widgets, do not directly through the model and are
 		// therefore bound
 		// separately for each sync direction through a WritableValue
 		// ////////////////////////////////////////////////////////
 
 		comboViewerOn.setContentProvider(new ArrayContentProvider());
 		comboViewerOn.setLabelProvider(new LabelProvider() {
 
 			@Override
 			public String getText(Object element) {
 				return modelUtils.weekDay((Integer) element);
 			}
 
 		});
 		comboViewerOn.setInput(modelUtils.weekDaysAsInteger().toArray());
 
 		comboViewerEvery.setContentProvider(new ArrayContentProvider());
 		comboViewerEvery.setInput(ComboStartInput);
 
 		// ////////////////////////////
 		// OBSERVABLES THROUGH AN AGGREGATOR
 		// //////////////////////////////
 
 		comboViewerOnWritableValue = new WritableValue();
 		comboViewerEveryWritableValue = new WritableValue();
 		cdateTimeStartTimeWritableValue = new WritableValue();
 		dateChooserStartsOnWritableValue = new WritableValue();
 		dateChooserEndsOnWritableValue = new WritableValue();
 		btnOnWritableValue = new WritableValue();
 		btnAfterWritableValue = new WritableValue();
 		btnNeverWritableValue = new WritableValue();
 		txtOccurencesWritableValue = new WritableValue();
 
 		comboViewerOnObserveSingleSelection = ViewersObservables
 				.observeSingleSelection(comboViewerOn);
 		comboViewerEveryObserveSingleSelection = ViewersObservables
 				.observeSingleSelection(comboViewerEvery);
 		comboViewerEveryObserveText = SWTObservables
 				.observeText(comboViewerEvery.getCombo());
 		comboObserveStartTime = new CDateTimeObservableValue(
 				this.cdateTimeStartTime);
 		dateChooseObserveStartDate = new DateChooserComboObservableValue(
 				dateChooserStartsOn, SWT.Modify);
 		dateChooseObserveEndDate = new DateChooserComboObservableValue(
 				dateChooserEndsOn, SWT.Modify);
 		endOnObservable = SWTObservables.observeSelection(btnOn);
 		endOccurencesObservable = SWTObservables.observeSelection(btnAfter);
 		endNeverObservable = SWTObservables.observeSelection(btnNever);
 		occurenceObservable = SWTObservables.observeDelayedValue(400,
 				SWTObservables.observeText(this.txtOccurences, SWT.Modify));
 
 		// ////////////////////////////
 		// OPPOSITE BINDING
 		// ///////////////////////////
 
 		bindingContext.bindValue(comboViewerOnObserveSingleSelection,
 				comboViewerOnWritableValue, null, null);
 		bindingContext.bindValue(comboViewerEveryObserveSingleSelection,
 				comboViewerEveryWritableValue, null, null);
 		bindingContext.bindValue(comboObserveStartTime,
 				cdateTimeStartTimeWritableValue, null, null);
 		bindingContext.bindValue(dateChooseObserveStartDate,
 				dateChooserStartsOnWritableValue, null, null);
 		bindingContext.bindValue(dateChooseObserveEndDate,
 				dateChooserEndsOnWritableValue, null, null);
 
 		bindingContext.bindValue(endNeverObservable, btnNeverWritableValue,
 				null, null);
 		bindingContext.bindValue(endOccurencesObservable,
 				btnAfterWritableValue, null, null);
 		bindingContext.bindValue(endOnObservable, btnOnWritableValue, null,
 				null);
 
 		EMFUpdateValueStrategy occurencesModelToTargetStrategy = new EMFUpdateValueStrategy();
 		occurencesModelToTargetStrategy.setConverter(new IConverter() {
 
 			public Object getFromType() {
 				return Integer.class;
 			}
 
 			public Object getToType() {
 				return String.class;
 			}
 
 			public Object convert(Object fromObject) {
 				return ((Integer) fromObject).toString();
 			}
 
 		});
 
 		EMFUpdateValueStrategy occurencesTargetToModelStrategy = new EMFUpdateValueStrategy();
 		occurencesTargetToModelStrategy.setConverter(new IConverter() {
 
 			public Object getFromType() {
 				return String.class;
 			}
 
 			public Object getToType() {
 				return Integer.class;
 			}
 
 			public Object convert(Object fromObject) {
 				try {
 
 					String from = (String) fromObject;
 					if (from.length() == 0) {
 						return 0;
 					}
 					return new Integer(from);
 				} catch (NumberFormatException nfe) {
 					nfe.printStackTrace();
 				}
 				return 0;
 			}
 
 		});
 
 		bindingContext.bindValue(occurenceObservable,
 				this.txtOccurencesWritableValue,
 				occurencesTargetToModelStrategy,
 				occurencesModelToTargetStrategy);
 
 		// /////////////////////////////////////////////////
 		// ACTUAL MODEL BINDING
 		// ////////////////////////////////////////////////
 
 		// The following binding is indirect through a series of writableValues.
 		// / The writables, which will be deduced from various widgets by our
 		// aggregator.
 		IObservableValue startTimeWritableValue = new WritableValue();
 		IObservableValue endTimeWritableValue = new WritableValue();
 		IObservableValue intervalWritableValue = new WritableValue();
 		IObservableValue repeatWritableValue = new WritableValue();
 
 		// The aggregator.
 		JobInfoAggregate aggregate = new JobInfoAggregate(
 				startTimeWritableValue, endTimeWritableValue,
 				intervalWritableValue, repeatWritableValue);
 
 		IEMFValueProperty startTimeProperty = EMFEditProperties.value(
 				editingService.getEditingDomain(),
 				SchedulingPackage.Literals.JOB__START_TIME);
 
 		IEMFValueProperty endTimeProperty = EMFEditProperties.value(
 				editingService.getEditingDomain(),
 				SchedulingPackage.Literals.JOB__END_TIME);
 
 		IEMFValueProperty intervalProperty = EMFEditProperties.value(
 				editingService.getEditingDomain(),
 				SchedulingPackage.Literals.JOB__INTERVAL);
 
 		IEMFValueProperty repeatProperty = EMFEditProperties.value(
 				editingService.getEditingDomain(),
 				SchedulingPackage.Literals.JOB__REPEAT);
 
 		// TODO, add converters and validators.
 
 		IObservableValue startTimeObservableValue = startTimeProperty
 				.observe(job);
 		IObservableValue endTimeObservableValue = endTimeProperty.observe(job);
 		IObservableValue intervalObservableValue = intervalProperty
 				.observe(job);
 		IObservableValue repeatObservableValue = repeatProperty.observe(job);
 
 		// //////////////////////
 		// BIND OUR WRITABLES.
 		// ////////////////////
 
 		EMFUpdateValueStrategy targetToModelStrategy = new EMFUpdateValueStrategy();
 		targetToModelStrategy.setConverter(new DateToXMLConverter());
 
 		bindingContext.bindValue(startTimeWritableValue,
 				startTimeObservableValue, targetToModelStrategy, null);
 		bindingContext.bindValue(endTimeWritableValue, endTimeObservableValue,
 				targetToModelStrategy, null);
 		bindingContext.bindValue(intervalWritableValue,
 				intervalObservableValue, targetToModelStrategy, null);
 		bindingContext.bindValue(repeatWritableValue, repeatObservableValue,
 				targetToModelStrategy, null);
 
 		// Set initial values of the widgets. , without having the aggregator
 		// activated yet.
 		this.setInitial(startTimeObservableValue.getValue(),
 				endTimeObservableValue.getValue(),
 				repeatObservableValue.getValue(),
 				intervalObservableValue.getValue());
 
 		// Set the initial values of the aggregator.
 		aggregate.setInitialValues(job);
 		this.enableAggregate(aggregate);
 
 		// bindingContext.updateTargets();
 
 		return bindingContext;
 	}
 
 	private void bindJobObjects() {
 
 		ObservableListContentProvider listContentProvider = new ObservableListContentProvider();
 		tableViewerJobObjects.setContentProvider(listContentProvider);
 
 		tableViewerJobObjects.setLabelProvider(new JobObjectsLabelProvider());
 
 		IEMFListProperty list = null;
 
 		if (job instanceof MetricSourceJob) {
 			list = EMFProperties
 					.list(SchedulingPackage.Literals.METRIC_SOURCE_JOB__METRIC_SOURCES);
 		} else if (job instanceof NodeReporterJob) {
 			list = EMFProperties
 					.list(SchedulingPackage.Literals.NODE_REPORTER_JOB__NODE);
 		} else if (job instanceof NodeTypeReporterJob) {
 			list = EMFProperties
 					.list(SchedulingPackage.Literals.NODE_TYPE_REPORTER_JOB__NODE_TYPE);
 		} else if (job instanceof OperatorReporterJob) {
 			list = EMFProperties
 					.list(SchedulingPackage.Literals.OPERATOR_REPORTER_JOB__OPERATOR);
 		} else if (job instanceof RFSServiceMonitoringJob) {
 			list = EMFProperties
 					.list(SchedulingPackage.Literals.RFS_SERVICE_MONITORING_JOB__RFS_SERVICE);
 		} else if (job instanceof RFSServiceReporterJob) {
 			list = EMFProperties
 					.list(SchedulingPackage.Literals.RFS_SERVICE_REPORTER_JOB__RFS_SERVICE);
 		}
 
 		if (list != null) {
 			tableViewerJobObjects.setInput(list.observe(job));
 		}
 
 	}
 
 	private void enableAggregate(JobInfoAggregate aggregate) {
 
 		comboViewerOnObserveSingleSelection.addValueChangeListener(aggregate);
 		comboViewerEveryObserveSingleSelection
 				.addValueChangeListener(aggregate);
 		comboViewerEveryObserveText.addValueChangeListener(aggregate);
 		comboObserveStartTime.addValueChangeListener(aggregate);
 		dateChooseObserveStartDate.addValueChangeListener(aggregate);
 		dateChooseObserveEndDate.addValueChangeListener(aggregate);
 		endOnObservable.addValueChangeListener(aggregate);
 		endOccurencesObservable.addValueChangeListener(aggregate);
 		endNeverObservable.addValueChangeListener(aggregate);
 		occurenceObservable.addValueChangeListener(aggregate);
 
 	}
 
 	private void setInitial(Object startTimeValue, Object endTimeValue,
 			Object repeatValue, Object intervalValue) {
 		// Decompose the object values to the UI settings.
 		if (startTimeValue != null) {
 			Date startDate = modelUtils
 					.fromXMLDate((XMLGregorianCalendar) startTimeValue);
 			int weekday = modelUtils.weekDay(startDate);
 			comboViewerOnWritableValue.setValue(weekday);
 			// The start time can be directly set.
 			cdateTimeStartTimeWritableValue.setValue(startDate);
 			// The start date can be directly set.
 			dateChooserStartsOnWritableValue.setValue(startDate);
 		}
 
 		if (repeatValue != null && ((Integer) repeatValue) != 0) {
 			// We have a repeat. So this could be.
 			btnAfterWritableValue.setValue(true);
 			txtOccurencesWritableValue.setValue(repeatValue);
 		} else {
 			if (endTimeValue != null) {
 				Date endDate = modelUtils
 						.fromXMLDate((XMLGregorianCalendar) endTimeValue);
 				btnOnWritableValue.setValue(true);
 				// Derive if we should set the enddate, or the number of
 				// occurences,
 				dateChooserEndsOnWritableValue.setValue(endDate);
 
 			} else {
 				// There is no end date.
 				btnNeverWritableValue.setValue(true);
 			}
 		}
 
 		if (intervalValue != null) {
 			String asString = modelUtils.fromSeconds((Integer) intervalValue);
 
 			List<Object> startlist = Lists.newArrayList(ComboStartInput);
 			if (startlist.contains(asString)) {
 				comboViewerEveryWritableValue.setValue(asString);
 			} else {
 				// we update the list.
 				startlist.add(asString);
 				comboViewerEvery.setInput(startlist.toArray());
 				comboViewerEveryWritableValue.setValue(asString);
 			}
 
 		}
 
 		// See if we can find a week or month repeat from the interval.
 		// Week or Month => comboViewerEveryWritableValue.setValue(value);
 
 	}
 
 	/**
 	 * We try to aggregate all info from widgets, and fill the writable
 	 * observables corresponding to: <code>JOB__START_TIME</code>
 	 * 
 	 * 
 	 * @author Christophe Bouhier
 	 * 
 	 */
 	private class JobInfoAggregate implements IValueChangeListener, IValidator {
 
 		private IObservableValue startTimeObservable;
 		private IObservableValue endTimeObservable;
 		private IObservableValue intervalObservable;
 		private IObservableValue repeatObservable;
 
 		private Date startDate = null;
 		private Date endDate = null;
 		private int repeat = -1;
 		private String interval = null;
 
 		JobInfoAggregate(IObservableValue startTime, IObservableValue endTime,
 				IObservableValue interval, IObservableValue repeat) {
 			this.startTimeObservable = startTime;
 			this.endTimeObservable = endTime;
 			this.intervalObservable = interval;
 			this.repeatObservable = repeat;
 		}
 
 		public void setInitialValues(Job j) {
 
 			if (j.eIsSet(SchedulingPackage.Literals.JOB__START_TIME)) {
 				startDate = modelUtils.fromXMLDate(j.getStartTime());
 			}
 			if (j.eIsSet(SchedulingPackage.Literals.JOB__END_TIME)) {
 				endDate = modelUtils.fromXMLDate(j.getEndTime());
 			}
 			if (j.eIsSet(SchedulingPackage.Literals.JOB__INTERVAL)) {
 				// interval = j.getInterval();
 			}
 			if (j.eIsSet(SchedulingPackage.Literals.JOB__REPEAT)) {
 				repeat = j.getRepeat();
 			}
 
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * org.eclipse.core.databinding.observable.value.IValueChangeListener
 		 * #handleValueChange
 		 * (org.eclipse.core.databinding.observable.value.ValueChangeEvent)
 		 */
 		public void handleValueChange(ValueChangeEvent event) {
 			System.out.println(event.getObservable());
 
 			Object newValue = event.diff.getNewValue();
 			System.out.println("New Value:" + event.diff.getNewValue());
 
 			if (event.getObservable() instanceof ISWTObservableValue) {
 				Control control = (Control) ((ISWTObservableValue) event
 						.getObservable()).getWidget();
 				System.out.println(control);
 				if (control.equals(btnOn)) {
 					// read the end date value
 					endDate = dateChooserEndsOn.getValue();
 					repeat = -1;
 				}
 				if (control.equals(btnAfter)) {
 					endDate = null;
 					try {
 						repeat = new Integer(txtOccurences.getText());
 					} catch (NumberFormatException nfe) {
 						nfe.printStackTrace();
 					}
 				}
 				if (control.equals(btnNever)) {
 					endDate = null;
 					repeat = -1;
 				}
 				if (control.equals(txtOccurences)) {
 					try {
 						repeat = new Integer((String) newValue);
 					} catch (NumberFormatException nfe) {
 						nfe.printStackTrace();
 					}
 				}
 				if (control.equals(comboViewerEvery.getCombo())) {
 					interval = (String) newValue;
 				}
 			}
 
 			if (event.getObservable() instanceof CDateTimeObservableValue) {
 				Date dateWithTime = (Date) newValue;
 
 				if (dateWithTime == null) {
 					// It could be that clear has been pressed on the widget,
 					// so we reset the time to the current time.
 					dateWithTime = modelUtils.todayAndNow();
 				}
 				startDate = modelUtils.mergeTimeIntoDate(startDate,
 						dateWithTime);
 			}
 
 			if (event.getObservable() instanceof DateChooserComboObservableValue) {
 				DateChooserCombo dc = ((DateChooserComboObservableValue) event
 						.getObservable()).getDateChooserCombo();
 
 				if (dc.equals(dateChooserEndsOn)) {
 					Date newEndDate = (Date) newValue;
 					this.endDate = newEndDate;
 				}
 
 				if (dc.equals(dateChooserStartsOn)) {
 					// So we have a start day, and time, now we have date, we
 					// keep
 					// the day and time, and merge with the new date.
 					// NOTE: As we override the date set by this widget.
 					// This will perhaps confuse the end-user.
 					Date newStartDate = (Date) newValue;
 					startDate = modelUtils.mergeDateIntoTime(startDate,
 							newStartDate);
 				}
 			}
 
 			if (event.getObservable() instanceof IViewerObservableValue) {
 				Viewer viewer = (Viewer) ((IViewerObservableValue) event
 						.getObservable()).getViewer();
 				System.out.println(viewer);
 
 				if (viewer.equals(comboViewerOn)) {
 					int dayOfWeek = (Integer) newValue;
 
 					startDate = modelUtils.mergeDayIntoDate(startDate,
 							dayOfWeek);
 
 				}
 				if (viewer.equals(comboViewerEvery)) {
 					interval = (String) newValue;
 				}
 			}
 			allSet();
 		}
 
 		private boolean allSet() {
 			// Calculate the new writablevalues.
 
 			if (startDate != null) {
 				System.out.println(startDate);
 				startTimeObservable.setValue(startDate);
 			}
 
 			if (endDate != null) {
 				endTimeObservable.setValue(endDate);
 			}
 
 			if (interval != null) {
 
 				int inSeconds = modelUtils.inSeconds(interval);
 				System.out.println(" Second interpreter = " + inSeconds);
 				// refuse to set a -1 value, as the string could be unvalid.
 				if (inSeconds != -1) {
 					intervalObservable.setValue(inSeconds);
 				}
 
 			}
 
 			if (repeat != -1) {
 				repeatObservable.setValue(repeat); // Job holds repear,
 			}
 			if (repeat == -1) {
 				repeatObservable.setValue(0); // Job holds repear,
 			}
 			// It would be nicer to observe the job for this.
 			List<Date> occurences = NewEditJob.this.generateOccurences(job);
 			occurencesTableViewer.setInput(occurences.toArray());
 
 			return true;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * org.eclipse.core.databinding.validation.IValidator#validate(java.
 		 * lang.Object)
 		 */
 		public IStatus validate(Object value) {
 
 			// Should we care about the value, we use the observable.
 			if (allSet()) {
 				return Status.OK_STATUS;
 			}
 			return new Status(IStatus.WARNING, ScreensActivator.PLUGIN_ID,
 					"Please enter correct job scheduling data");
 		}
 	}
 
 	class DateToXMLConverter extends Converter {
 		public DateToXMLConverter() {
 			super(Date.class, XMLGregorianCalendar.class);
 		}
 
 		public Object convert(Object fromObject) {
 			if (fromObject instanceof Date) {
 				return modelUtils.toXMLDate((Date) fromObject);
 			}
 			return fromObject;
 		}
 	}
 
 	public void injectData(Object owner, Object object) {
 
 		if (owner instanceof Resource) {
 			this.owner = (Resource) owner;
 		}
 		if (object != null && object instanceof Job) {
 			if (ScreenUtil.isEditOperation(this.getOperation())) {
 				// Job copy = EcoreUtil.copy((Job) object);
 				// job = copy;
 				job = (Job) object;
 			} else if (ScreenUtil.isNewOperation(getOperation())) {
 				job = (Job) object;
 			}
 		}
 
 		buildUI();
 		bindingContext = initDataBindings_();
 
 		jobTypes = new String[] { "Metric Import", "Monitoring",
 				"Data retention", "Service Reporting", "Operator Reporting",
 				"Node Reporting" };
 		int type = 0;
 		if (job instanceof MetricSourceJob) {
 			type = 0;
 		}
 		if (job instanceof RFSServiceMonitoringJob) {
 			type = 1;
 		}
 		if (job instanceof RetentionJob) {
 			type = 2;
 		}
 		if (job instanceof RFSServiceReporterJob) {
 			type = 3;
 		}
 		if (job instanceof OperatorReporterJob) {
 			type = 4;
 		}
 		if (job instanceof NodeReporterJob) {
 			type = 5;
 		}
 
 		// Doesn't apply to all jobs..
 		this.bindJobObjects();
 
 		
 		frmNewJob.setText(this.getOperationText() + jobTypes[type] + " Job");
 
 		if (!ScreenUtil.isReadOnlyOperation(getOperation())) {
 			validationService.registerBindingContext(bindingContext);
 			validationService.addValidationListener(this);
 		}
 
 		List<Date> occurences = NewEditJob.this.generateOccurences(job);
 		occurencesTableViewer.setInput(occurences.toArray());
 
 	}
 
 	public void addData() {
 		if (ScreenUtil.isNewOperation(getOperation()) && owner != null) {
 			// If new, we have been operating on an object not added yet.
 			Command c = new AddCommand(editingService.getEditingDomain(),
 					owner.getContents(), job);
 			editingService.getEditingDomain().getCommandStack().execute(c);
 		} else if (ScreenUtil.isEditOperation(getOperation())) {
 			// If edit, we have been operating on a copy of the object, so we
 			// have to replace. However if our original object is invalid, this
 			// will
 			// cause invalidity, so the action will not occure in case the
 			// original is
 			// invalid, and we should cancel the action and warn the user.
 			if (job.cdoInvalid()) {
 				MessageDialog
 						.openWarning(Display.getDefault().getActiveShell(),
 								"Conflict",
 								"There is a conflict with another user. Your changes can't be saved.");
 				return;
 			}
 
 			// Command c = new ReplaceCommand(editingService.getEditingDomain(),
 			// owner.getContents(), original, job);
 			// editingService.getEditingDomain().getCommandStack().execute(c);
 
 			// Databinding would have occured on the object, not through the
 			// editing
 			// domain, so no point in creating a command. It won't be undoable.
 
 		}
 		// After our edit, we shall be dirty only if using the editing domain.
 		if (editingService.isDirty()) {
 			editingService.doSave(new NullProgressMonitor());
 		}
 	}
 
 	private List<Date> generateOccurences(Job job) {
 		return ImmutableList.copyOf(modelUtils.occurences(
 				modelUtils.fromXMLDate(job.getStartTime()),
 				job.getEndTime() != null ? modelUtils.fromXMLDate(job
 						.getEndTime()) : null, job.getInterval(), job
 						.getRepeat()));
 	}
 
 	public Viewer getViewer() {
 		// We don't have a specific viewer.
 		return null;
 	}
 
 	@Override
 	public boolean isValid() {
 		return validationService.isValid();
 	}
 
 	public Form getScreenForm() {
 		return frmNewJob;
 	}
 
 	public void disposeData() {
 		// N/A
 
 	}
 
 	public String getScreenName() {
 		return "Job";
 	}
 
 }
