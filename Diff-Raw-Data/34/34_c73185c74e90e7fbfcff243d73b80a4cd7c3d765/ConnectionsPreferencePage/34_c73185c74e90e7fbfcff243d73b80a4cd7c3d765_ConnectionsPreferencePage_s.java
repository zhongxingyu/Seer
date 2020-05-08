 /**
  * Copyright (c) 2008 The RCER Development Team.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
  * this entire header must remain intact.
  *
  * $Id$
  */
 package net.sf.rcer.conn.ui.preferences;
 
 import java.text.MessageFormat;
 import java.util.Collection;
 
 import net.sf.rcer.conn.Activator;
 import net.sf.rcer.conn.connections.ConnectionData;
 import net.sf.rcer.conn.connections.ConnectionType;
 import net.sf.rcer.conn.locales.Locale;
 import net.sf.rcer.conn.locales.LocaleRegistry;
 import net.sf.rcer.conn.preferences.PreferencesConnectionManager;
 
 import org.eclipse.core.databinding.AggregateValidationStatus;
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.UpdateValueStrategy;
 import org.eclipse.core.databinding.beans.BeansObservables;
 import org.eclipse.core.databinding.conversion.NumberToStringConverter;
 import org.eclipse.core.databinding.conversion.StringToNumberConverter;
 import org.eclipse.core.databinding.observable.Realm;
 import org.eclipse.core.databinding.observable.list.WritableList;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.core.databinding.validation.IValidator;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.databinding.swt.SWTObservables;
 import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
 import org.eclipse.jface.databinding.viewers.ViewersObservables;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.internal.databinding.provisional.swt.ControlUpdater;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.preference.PreferencePage;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ListViewer;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 
 /**
  * The preference page to edit the connection data.
  * @author vwegert
  *
  */
 @SuppressWarnings("restriction")
 public class ConnectionsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
 
 	// TODO The label in the connection table viewer is not updated when the values are changed.
 	
 	/**
 	 * The connections displayed.
 	 */
 	private WritableList connections;
 
 	/**
 	 * The 'master' elements of the master-detail form.
 	 */
 	private ListViewer connectionViewer;
 	private Button addButton;
 	private Button importButton;
 	private Button removeButton;
 
 	/**
 	 * The common detail elements of the master-detail form.
 	 */
 	private Composite details;
 	private Label descriptionLabel;
 	private Text descriptionText;
 	private Label connectionTypeLabel;
 	private Combo connectionTypeCombo;
 	private Label sidLabel;
 	private Text sidText;
 	private Label errorDisplayLabel;
 
 	/**
 	 * The connection data elements of the master-detail form.
 	 */
 	private Group connectionDataGroup;
 	private Label routerLabel;
 	private Text routerText;
 	private Label applicationServerLabel;
 	private Text applicationServerText;
 	private Label systemNumberLabel;
 	private Text systemNumberText;
 	private Label messageServerLabel;
 	private Text messageServerText; 
 	private Label messageServerPortLabel;
 	private Text messageServerPortText;
 	private Label logonGroupLabel;
 	private Text logonGroupText;
 
 	/**
 	 * The default logon data elements of the master-detail form.
 	 */
 	private Group defaultsDataGroup;
 	private Label defaultClientLabel;
 	private Text defaultClientText;
 	private Label defaultUserLabel;
 	private Text defaultUserText;
 	private Label defaultLocaleLabel;
 	private Combo defaultLocaleCombo;
 
 
 	/**
 	 * A {@link ControlUpdater} that will hide or show controls depending on an {@link IObservableValue}.
 	 * @author vwegert
 	 */
 	@SuppressWarnings("restriction")
 	private class VisibilityUpdater extends ControlUpdater {
 
 		private Composite parent;
 		private Control control;
 		private Control label;
 		private IObservableValue observable;
 		private Object value;
 
 		/**
 		 * Standard constructor.
 		 * @param parent the parent control to re-layout when the visibility changes
 		 * @param control the control to show or hide
 		 * @param label the label of the control (will also be hidden)
 		 * @param dependentObservable the observable whose value will be checked
 		 * @param value the value that causes the control to be visible
 		 */
 		public VisibilityUpdater(Composite parent, Control control, Control label,
 				IObservableValue dependentObservable, Object value) {
 			super(parent);
 			this.parent = parent;
 			this.control = control;
 			this.label = label;
 			this.observable = dependentObservable;
 			this.value = value;
 		}
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.jface.internal.databinding.provisional.swt.ControlUpdater#updateControl()
 		 */
 		@Override
 		protected void updateControl() {
 			Object val = observable.getValue();
 			final boolean excluded = (val == null || !val.equals(value));
 			((GridData)control.getLayoutData()).exclude = excluded;
 			((GridData)label.getLayoutData()).exclude   = excluded;
 			control.setVisible(!excluded);
 			label.setVisible(!excluded);
 			parent.layout(true, true);
 			parent.redraw();
 		}	
 	}
 
 	/**
 	 * A validator that ensures that the value is not empty.
 	 * @author vwegert
 	 */
 	private class NotEmptyValidator implements IValidator {
 
 		private IStatus errorStatus;
 
 		/**
 		 * Standard constructor.
 		 * @param message the error message to return if the value is empty.
 		 */
 		public NotEmptyValidator(String message) {
 			errorStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, message);
 		}
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.core.databinding.validation.IValidator#validate(java.lang.Object)
 		 */
 		public IStatus validate(Object value) {
 			if (value == null) {
 				return errorStatus;
 			}
 			if (value instanceof String) {
 				if (((String) value).equals("")) {
 					return errorStatus;
 				}
 			}
 			return Status.OK_STATUS;
 		}
 	}
 
 	/**
 	 * A validator that ensures that the value is not empty if another field has a certain value.
 	 * @author vwegert
 	 */
 	private class ConditionalNotEmptyValidator implements IValidator {
 
 		private IObservableValue dependentObservable;
 		private Object observableValue;
 		private IStatus errorStatus;
 
 		/**
 		 * Standard constructor.
 		 * @param dependentObservable 
 		 * @param observableValue 
 		 * @param message the error message to return if the value is empty.
 		 */
 		public ConditionalNotEmptyValidator(IObservableValue dependentObservable, Object observableValue, String message) {
 			this.dependentObservable = dependentObservable;
 			this.observableValue = observableValue;
 			this.errorStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, message);
 		}
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.core.databinding.validation.IValidator#validate(java.lang.Object)
 		 */
 		public IStatus validate(Object value) {
 			if (dependentObservable.getValue().equals(observableValue)) {
 				if (value == null) {
 					return errorStatus;
 				}
 				if (value instanceof String) {
 					if (((String) value).equals("")) {
 						return errorStatus;
 					}
 				}
 			}
 			return Status.OK_STATUS;
 		}
 	}
 
 	/**
 	 * A validator that ensures that a string has a certain length.
 	 * @author vwegert
 	 */
 	private class StringLengthValidator implements IValidator {
 
 		private String description;
 		private int minLength;
 		private int maxLength;
 
 		/**
 		 * Standard constructor.
 		 * @param description 
 		 * @param minLength
 		 * @param maxLength
 		 */
 		public StringLengthValidator(String description, int minLength, int maxLength) {
 			super();
 			this.description = description;
 			this.minLength = minLength;
 			this.maxLength = maxLength;
 		}
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.core.databinding.validation.IValidator#validate(java.lang.Object)
 		 */
 		public IStatus validate(Object value) {
 			if (value instanceof String) {
 				String string = (String) value;
 				if (string.length() < minLength) {
 					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
 							MessageFormat.format("{0} must be at least {1} characters.", description, minLength));
 				}
 				if (string.length() > maxLength) {
 					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
 							MessageFormat.format("{0} may not exceed {1} characters.", description, maxLength));
 				}
 				return Status.OK_STATUS;
 			}
 			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Value is not a String");
 		}
 	}
 
 	/**
 	 * A validator that ensures that an integer value is within certain boundaries.
 	 * @author vwegert
 	 */
 	private class IntegerRangeValidator implements IValidator {
 
 		private String description;
 		private int min;
 		private int max;
 
 		/**
 		 * Standard constructor.
 		 * @param description 
 		 * @param min 
 		 * @param max 
 		 */
 		public IntegerRangeValidator(String description, int min, int max) {
 			super();
 			this.description = description;
 			this.min = min;
 			this.max = max;
 		}
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.core.databinding.validation.IValidator#validate(java.lang.Object)
 		 */
 		public IStatus validate(Object value) {
 			if (value instanceof Integer) {
 				Integer intValue = (Integer) value;
 				if ((intValue < min) || (intValue > max)) {
 					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
 							MessageFormat.format("{0} must be between {1} and {2}.", description, min, max)) ;
 				}
 				return Status.OK_STATUS;
 			}
 			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Value is not an Integer");		
 		}
 	}
 
 	/**
 	 * Default constructor.
 	 */
 	public ConnectionsPreferencePage() {
 		super();
 		noDefaultAndApplyButton();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
 	 */
 	public void init(IWorkbench workbench) {
 		// load the connections from the preferences and clone the entire collection 
 		// because we may want to modify the objects without affecting the managed instances
 		// (and we also need an ObservableList)
 		connections = new WritableList();
 		for (ConnectionData conn: PreferencesConnectionManager.getInstance().getConnectionData()) {
 			connections.add(conn.clone());
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
 	 */
 	@Override
 	protected Control createContents(Composite parent) {
 		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
 		GridLayoutFactory.fillDefaults().applyTo(sashForm);
 		createMasterControls(sashForm);
 		createDetailControls(sashForm);
 		bindControls();
 		return sashForm;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.preference.PreferencePage#computeSize()
 	 */
 	@Override
 	public Point computeSize() {
 		Point size = super.computeSize();
 		return new Point((int) (size.x * 1.5), size.y);
 	}
 
 	/**
 	 * Creates the 'master' part of the master-details form.
 	 * @param parent
 	 */
 	private void createMasterControls(Composite parent) {
 		Composite master = new Composite(parent, SWT.NONE);
 		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(master);
 
 		connectionViewer = new ListViewer(master, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
 		GridDataFactory.fillDefaults().grab(true, true).applyTo(connectionViewer.getControl());
 
 		Composite masterButtons = new Composite(master, SWT.NONE);
 		GridDataFactory.fillDefaults().grab(false, true).applyTo(masterButtons);
 		GridLayoutFactory.swtDefaults().applyTo(masterButtons);
 
 		addButton = new Button(masterButtons, SWT.PUSH);
 		GridDataFactory.fillDefaults().applyTo(addButton);
 		addButton.setText("Add...");
 		addButton.addSelectionListener(new SelectionAdapter() {
 			/* (non-Javadoc)
 			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
 			 */
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				ConnectionData connection = new ConnectionData();
 				connections.add(connection);
 				connectionViewer.setSelection(new StructuredSelection(connection));
 			}
 		});
 
 		importButton = new Button(masterButtons, SWT.PUSH);
 		GridDataFactory.fillDefaults().applyTo(importButton);
 		importButton.setText("Import...");
 		importButton.setEnabled(false);
 		// TODO Support import of connection data
 
 		removeButton = new Button(masterButtons, SWT.PUSH);
 		GridDataFactory.fillDefaults().applyTo(removeButton);
 		removeButton.setText("Remove...");
 		removeButton.addSelectionListener(new SelectionAdapter() {
 			/* (non-Javadoc)
 			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
 			 */
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				final ISelection sel = connectionViewer.getSelection();
 				if ((sel != null) && (sel instanceof IStructuredSelection)) {
 					final IStructuredSelection selection = (IStructuredSelection) sel;
 					if (MessageDialog.openConfirm(getShell(), 
 							"Delete Connection", 
 							"Do you want to delete the selected connection?")) {
 						connections.remove(selection.getFirstElement());
 					}
 				}
 			}
 		});
 
 		Label separator = new Label(master, SWT.SEPARATOR | SWT.HORIZONTAL);
 		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(separator);
 	}
 
 	/**
 	 * Creates the 'detail' part of the master-detail form.
 	 * @param parent
 	 */
 	private void createDetailControls(Composite parent) {
 		details = new Composite(parent, SWT.NONE);
 		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true).applyTo(details);
 		createCommonDataControls(details);
 		createConnectionDataControls(details);
 		createDefaultsDataControls(details);
 		createErrorDisplayControls(details);
 	}
 
 	/**
 	 * Creates the common connection data controls.
 	 * @param parent
 	 */
 	private void createCommonDataControls(Composite parent) {
 		Composite common = new Composite(parent, SWT.NONE);
 		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(common);
 		GridLayoutFactory.fillDefaults().numColumns(4).applyTo(common);
 
 		descriptionLabel = new Label(common, SWT.NONE);
 		GridDataFactory.swtDefaults().applyTo(descriptionLabel);
 		descriptionLabel.setText("Description:");
 
 		descriptionText = new Text(common, SWT.SINGLE | SWT.BORDER);
 		GridDataFactory.fillDefaults().span(3, 1).grab(true, false).applyTo(descriptionText);
 
 		connectionTypeLabel = new Label(common, SWT.NONE);
 		GridDataFactory.swtDefaults().applyTo(connectionTypeLabel);
 		connectionTypeLabel.setText("Connection Type:");
 
 		connectionTypeCombo = new Combo(common, SWT.READ_ONLY);
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(connectionTypeCombo);
 		connectionTypeCombo.setItems(ConnectionType.getStrings());
 
 		sidLabel = new Label(common, SWT.NONE);
 		GridDataFactory.swtDefaults().applyTo(sidLabel);
 		sidLabel.setText("System ID:");
 
 		sidText = new Text(common, SWT.SINGLE | SWT.BORDER);
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(sidText);
 	}
 
 	/**
 	 * Creates the connection data group and its contents.
 	 * @param parent
 	 */
 	private void createConnectionDataControls(Composite parent) {
 		connectionDataGroup = new Group(parent, SWT.BORDER);
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(connectionDataGroup);
 		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(connectionDataGroup);
 		connectionDataGroup.setText("Connection Data");
 
 		routerLabel = new Label(connectionDataGroup, SWT.NONE);
 		GridDataFactory.swtDefaults().applyTo(routerLabel);
 		routerLabel.setText("Router:");
 
 		routerText = new Text(connectionDataGroup, SWT.SINGLE | SWT.BORDER);
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(routerText);
 
 		applicationServerLabel = new Label(connectionDataGroup, SWT.NONE);
 		GridDataFactory.swtDefaults().applyTo(applicationServerLabel);
 		applicationServerLabel.setText("Application Server:");
 
 		applicationServerText = new Text(connectionDataGroup, SWT.SINGLE | SWT.BORDER);
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(applicationServerText);
 
 		systemNumberLabel = new Label(connectionDataGroup, SWT.NONE);
 		GridDataFactory.swtDefaults().applyTo(systemNumberLabel);
 		systemNumberLabel.setText("System Number:");
 
 		systemNumberText = new Text(connectionDataGroup, SWT.SINGLE | SWT.BORDER);
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(systemNumberText);
 
 		messageServerLabel = new Label(connectionDataGroup, SWT.NONE);
 		GridDataFactory.swtDefaults().applyTo(messageServerLabel);
 		messageServerLabel.setText("Message Server:");
 
 		messageServerText = new Text(connectionDataGroup, SWT.SINGLE | SWT.BORDER);
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(messageServerText);
 
 		messageServerPortLabel = new Label(connectionDataGroup, SWT.NONE);
 		GridDataFactory.swtDefaults().applyTo(messageServerPortLabel);
 		messageServerPortLabel.setText("Message Server Port:");
 
 		messageServerPortText = new Text(connectionDataGroup, SWT.SINGLE | SWT.BORDER);
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(messageServerPortText);
 
 		logonGroupLabel = new Label(connectionDataGroup, SWT.NONE);
 		GridDataFactory.swtDefaults().applyTo(logonGroupLabel);
 		logonGroupLabel.setText("Logon Group:");
 
 		logonGroupText = new Text(connectionDataGroup, SWT.SINGLE | SWT.BORDER);
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(logonGroupText);
 	}
 
 	/**
 	 * Creates the logon defaults group and its contents.
 	 * @param parent
 	 */
 	private void createDefaultsDataControls(Composite parent) {
 		defaultsDataGroup = new Group(parent, SWT.BORDER);
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(defaultsDataGroup);
 		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(defaultsDataGroup);
 		defaultsDataGroup.setText("Logon Defaults");
 
 		defaultClientLabel = new Label(defaultsDataGroup, SWT.NONE);
 		GridDataFactory.swtDefaults().applyTo(defaultClientLabel);
 		defaultClientLabel.setText("Client:");
 
 		defaultClientText = new Text(defaultsDataGroup, SWT.SINGLE | SWT.BORDER);
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(defaultClientText);
 
 		defaultUserLabel = new Label(defaultsDataGroup, SWT.NONE);
 		GridDataFactory.swtDefaults().applyTo(defaultUserLabel);
 		defaultUserLabel.setText("User:");
 
 		defaultUserText = new Text(defaultsDataGroup, SWT.SINGLE | SWT.BORDER);
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(defaultUserText);
 
 		defaultLocaleLabel = new Label(defaultsDataGroup, SWT.NONE);
 		GridDataFactory.swtDefaults().applyTo(defaultLocaleLabel);
 		defaultLocaleLabel.setText("Locale:");
 
 		defaultLocaleCombo = new Combo(defaultsDataGroup, SWT.READ_ONLY);
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(defaultLocaleCombo);
 		final Collection<String> locales = LocaleRegistry.getInstance().getISOCodes();
 		String[] entries = locales.toArray(new String[locales.size() + 1]);
 		entries[locales.size()] = "";
 		defaultLocaleCombo.setItems(entries);
 	}
 
 	/**
 	 * Creates the error display controls.
 	 * @param parent
 	 */
 	private void createErrorDisplayControls(Composite parent) {
 		errorDisplayLabel = new Label(parent, SWT.WRAP);
 		GridDataFactory.fillDefaults().span(2, 1).applyTo(errorDisplayLabel);
 		errorDisplayLabel.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
 	}
 
 	/**
 	 * Initializes the data binding for the user interface components.
 	 */
 	private void bindControls() {
 
 		UpdateValueStrategy targetToModel;
 
		DataBindingContext context = new DataBindingContext();
 
 		// observe changes in the selection of the connection list
 		IObservableValue selection = ViewersObservables.observeSingleSelection(connectionViewer);
 
 		// bind the description (String, may not be empty)
 		context.bindValue(SWTObservables.observeText(descriptionText, SWT.Modify), 
 				BeansObservables.observeDetailValue(Realm.getDefault(), selection, "description", String.class), 
 				new UpdateValueStrategy().setBeforeSetValidator(new NotEmptyValidator("Description may not be empty.")), 
 				new UpdateValueStrategy());
 
 		// bind the connection type
 		IObservableValue connectionTypeObservable = BeansObservables.observeDetailValue(Realm.getDefault(), 
 				selection, "connectionType", ConnectionType.class);
 		context.bindValue(SWTObservables.observeSelection(connectionTypeCombo), 
 				connectionTypeObservable, 
 				new UpdateValueStrategy().setConverter(new ConnectionType.ToStringConverter()), 
 				new UpdateValueStrategy().setConverter(new ConnectionType.FromStringConverter()));
 
 		// bind the system ID
 		context.bindValue(SWTObservables.observeText(sidText, SWT.Modify), 
 				BeansObservables.observeDetailValue(Realm.getDefault(), selection, "systemID", String.class), 
 				new UpdateValueStrategy().setBeforeSetValidator(new StringLengthValidator("System ID", 3, 3)), 
 				new UpdateValueStrategy());
 
 		// bind the router
 		context.bindValue(SWTObservables.observeText(routerText, SWT.Modify), 
 				BeansObservables.observeDetailValue(Realm.getDefault(), selection, "router", String.class), 
 				new UpdateValueStrategy(), 
 				new UpdateValueStrategy());
 
 		// bind the application server
 		context.bindValue(SWTObservables.observeText(applicationServerText, SWT.Modify), 
 				BeansObservables.observeDetailValue(Realm.getDefault(), selection, "applicationServer", String.class), 
 				new UpdateValueStrategy(), 
 				new UpdateValueStrategy());
 		new VisibilityUpdater(details, applicationServerText, applicationServerLabel,
 				connectionTypeObservable, ConnectionType.DIRECT);
 		// TODO ensure that the app server is required for direct connections
 
 		// bind the system number
 		targetToModel = new UpdateValueStrategy();
 		targetToModel.setConverter(StringToNumberConverter.toInteger(true));
 		// TODO ensure that the system number is required for direct connections
 		//		targetToModel.setAfterGetValidator(new ConditionalNotEmptyValidator(
 		//				connectionTypeObservable, ConnectionType.DIRECT, "System Number must be set for direct connections."));
 		targetToModel.setBeforeSetValidator(new IntegerRangeValidator("System Number", 0, 99));
 		context.bindValue(SWTObservables.observeText(systemNumberText, SWT.Modify), 
 				BeansObservables.observeDetailValue(Realm.getDefault(), selection, "systemNumber", null), 
 				targetToModel, 
 				new UpdateValueStrategy().setConverter(NumberToStringConverter.fromInteger(true)));
 		new VisibilityUpdater(details, systemNumberText, systemNumberLabel, 
 				connectionTypeObservable, ConnectionType.DIRECT);
 
 		// bind the message server 
 		context.bindValue(SWTObservables.observeText(messageServerText, SWT.Modify), 
 				BeansObservables.observeDetailValue(Realm.getDefault(), selection, "messageServer", String.class), 
 				new UpdateValueStrategy(), 
 				new UpdateValueStrategy());
 		new VisibilityUpdater(details, messageServerText, messageServerLabel, 
 				connectionTypeObservable, ConnectionType.LOAD_BALANCED);
 		// TODO ensure that the message server is required for lb connections
 
 		// bind the message server port
 		context.bindValue(SWTObservables.observeText(messageServerPortText, SWT.Modify), 
 				BeansObservables.observeDetailValue(Realm.getDefault(), selection, "messageServerPort", null), 
 				new UpdateValueStrategy().setConverter(StringToNumberConverter.toInteger(true)), 
 				new UpdateValueStrategy().setConverter(NumberToStringConverter.fromInteger(true)));
 		new VisibilityUpdater(details, messageServerPortText, messageServerPortLabel, 
 				connectionTypeObservable, ConnectionType.LOAD_BALANCED);
 		// TODO ensure that the port is required for lb connections
 
 		// bind the logon group 
 		context.bindValue(SWTObservables.observeText(logonGroupText, SWT.Modify), 
 				BeansObservables.observeDetailValue(Realm.getDefault(), selection, "loadBalancingGroup", String.class), 
 				new UpdateValueStrategy(), 
 				new UpdateValueStrategy());
 		new VisibilityUpdater(details, logonGroupText, logonGroupLabel, 
 				connectionTypeObservable, ConnectionType.LOAD_BALANCED);
 		// TODO ensure that the logon group is required for lb connections
 
 		// bind the default client 
 		context.bindValue(SWTObservables.observeText(defaultClientText, SWT.Modify), 
 				BeansObservables.observeDetailValue(Realm.getDefault(), selection, "defaultClient", String.class), 
 				new UpdateValueStrategy(), 
 				new UpdateValueStrategy());
 
 		// bind the default user
 		context.bindValue(SWTObservables.observeText(defaultUserText, SWT.Modify), 
 				BeansObservables.observeDetailValue(Realm.getDefault(), selection, "defaultUser", String.class), 
 				new UpdateValueStrategy(), 
 				new UpdateValueStrategy());
 
 		// bind the default locale 
 		context.bindValue(SWTObservables.observeSelection(defaultLocaleCombo), 
 				BeansObservables.observeDetailValue(Realm.getDefault(), selection, "defaultLocale", Locale.class), 
				new UpdateValueStrategy().setConverter(new Locale.ToStringConverter()), 
				new UpdateValueStrategy().setConverter(new Locale.FromStringConverter(false)));
 
 		// supply the error label with the aggregated status
 		context.bindValue(SWTObservables.observeText(errorDisplayLabel),
 				new AggregateValidationStatus(context.getBindings(), AggregateValidationStatus.MAX_SEVERITY), 
 				null,
 				null);	
 
 		// provide the connection list with input data
 		connectionViewer.setContentProvider(new ObservableListContentProvider());
 		connectionViewer.setInput(connections);
 
 		// select the first entry, if any exist
 		if (connections.size() > 0) {
 			connectionViewer.setSelection(new StructuredSelection(connections.get(0)));
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public boolean performOk() {
 		PreferencesConnectionManager.getInstance().saveConnectionData(connections);
 		return super.performOk();
 	}
 
 }
