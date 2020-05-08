 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.debug.ui.interpreters;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Iterator;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dltk.debug.ui.DLTKDebugUIPlugin;
 import org.eclipse.dltk.debug.ui.IDLTKDebugUIConstants;
import org.eclipse.dltk.launching.EnvironmentVariable;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.launching.IInterpreterInstallType;
 import org.eclipse.dltk.launching.LibraryLocation;
 import org.eclipse.dltk.launching.ScriptRuntime;
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.IBaseLabelProvider;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.Label;
 
import com.sun.corba.se.spi.legacy.connection.GetEndPointInfoAgainException;

 /**
  * Control used to edit the libraries associated with a Interpreter install
  */
 public abstract class AbstractInterpreterLibraryBlock implements
 		SelectionListener, ISelectionChangedListener {
 
 	/**
 	 * Attribute name for the last path used to open a file/directory chooser
 	 * dialog.
 	 */
 	protected static final String LAST_PATH_SETTING = "LAST_PATH_SETTING"; //$NON-NLS-1$
 
 	/**
 	 * the prefix for dialog setting pertaining to this block
 	 */
 	protected static final String DIALOG_SETTINGS_PREFIX = "AbstractInterpreterLibraryBlock"; //$NON-NLS-1$
 
 	protected boolean fInCallback = false;
 	protected IInterpreterInstall fInterpreterInstall;
 	protected IInterpreterInstallType fInterpreterInstallType;
 	protected File fHome;
 
 	// widgets
 	protected LibraryContentProvider fLibraryContentProvider;
 	protected TreeViewer fLibraryViewer;
 	private Button fUpButton;
 	private Button fDownButton;
 	private Button fRemoveButton;
 	private Button fAddButton;
 	protected Button fDefaultButton;
 
 	protected AddScriptInterpreterDialog fDialog;
 
 	protected AbstractInterpreterLibraryBlock(AddScriptInterpreterDialog dialog) {
 		this.fDialog = dialog;
 	}
 
 	/**
 	 * Creates and returns the source lookup control.
 	 * 
 	 * @param parent
 	 *            the parent widget of this control
 	 */
 	public Control createControl(Composite parent) {
 		Font font = parent.getFont();
 
 		Composite comp = new Composite(parent, SWT.NONE);
 		GridLayout topLayout = new GridLayout();
 		topLayout.numColumns = 2;
 		topLayout.marginHeight = 0;
 		topLayout.marginWidth = 0;
 		comp.setLayout(topLayout);
 		GridData gd = new GridData(GridData.FILL_BOTH);
 		comp.setLayoutData(gd);
 
 		fLibraryViewer = new TreeViewer(comp);
 		gd = new GridData(GridData.FILL_BOTH);
 		gd.heightHint = 6;
 		fLibraryViewer.getControl().setLayoutData(gd);
 		fLibraryContentProvider = new LibraryContentProvider();
 		fLibraryViewer.setContentProvider(fLibraryContentProvider);
 		fLibraryViewer.setLabelProvider(getLabelProvider());
 		fLibraryViewer.setInput(this);
 		fLibraryViewer.addSelectionChangedListener(this);
 
 		Composite pathButtonComp = new Composite(comp, SWT.NONE);
 		GridLayout pathButtonLayout = new GridLayout();
 		pathButtonLayout.marginHeight = 0;
 		pathButtonLayout.marginWidth = 0;
 		pathButtonComp.setLayout(pathButtonLayout);
 		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING
 				| GridData.HORIZONTAL_ALIGN_FILL);
 		pathButtonComp.setLayoutData(gd);
 		pathButtonComp.setFont(font);
 
 		fAddButton = createPushButton(pathButtonComp,
 				InterpretersMessages.InterpreterLibraryBlock_7);
 		fAddButton.addSelectionListener(this);
 
 		fRemoveButton = createPushButton(pathButtonComp,
 				InterpretersMessages.InterpreterLibraryBlock_6);
 		fRemoveButton.addSelectionListener(this);
 
 		fUpButton = createPushButton(pathButtonComp,
 				InterpretersMessages.InterpreterLibraryBlock_4);
 		fUpButton.addSelectionListener(this);
 
 		fDownButton = createPushButton(pathButtonComp,
 				InterpretersMessages.InterpreterLibraryBlock_5);
 		fDownButton.addSelectionListener(this);
 
 		fDefaultButton = createPushButton(pathButtonComp,
 				InterpretersMessages.InterpreterLibraryBlock_9);
 		fDefaultButton.addSelectionListener(this);
 
 		return comp;
 	}
 
 	/**
 	 * The "default" button has been toggled
 	 */
 	public void restoreDefaultLibraries() {
 		final LibraryLocation[][] libs = new LibraryLocation[][] { null };
 		final File installLocation = getHomeDirectory();
 		if (installLocation == null) {
 			libs[0] = new LibraryLocation[0];
 		} else {
 			ProgressMonitorDialog dialog = new ProgressMonitorDialog(null);
 			try {
 				dialog.run(false, false, new IRunnableWithProgress() {
 
 					public void run(IProgressMonitor monitor)
 							throws InvocationTargetException,
 							InterruptedException {
 						libs[0] = getInterpreterInstallType()
 								.getDefaultLibraryLocations(installLocation,
 										fDialog.getEnvironmentVariables());
 					}
 
 				});
 			} catch (InvocationTargetException e) {
 				e.printStackTrace();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 
 		}
 		if (libs != null)
 			fLibraryContentProvider.setLibraries(libs[0]);
 		update();
 	}
 
 	/**
 	 * Creates and returns a button
 	 * 
 	 * @param parent
 	 *            parent widget
 	 * @param label
 	 *            label
 	 * @return Button
 	 */
 	protected Button createPushButton(Composite parent, String label) {
 		Button button = new Button(parent, SWT.PUSH);
 		button.setFont(parent.getFont());
 		button.setText(label);
 		setButtonLayoutData(button);
 		return button;
 	}
 
 	/**
 	 * Create some empty space
 	 */
 	protected void createVerticalSpacer(Composite comp, int colSpan) {
 		Label label = new Label(comp, SWT.NONE);
 		GridData gd = new GridData();
 		gd.horizontalSpan = colSpan;
 		label.setLayoutData(gd);
 	}
 
 	/**
 	 * Sets the home directory of the Interpreter Install the user has chosen
 	 */
 	public void setHomeDirectory(File file) {
 		fHome = file;
 	}
 
 	/**
 	 * Returns the home directory
 	 */
 	protected File getHomeDirectory() {
 		return fHome;
 	}
 
 	/**
 	 * Updates buttons and status based on current libraries
 	 */
 	public void update() {
 		updateButtons();
 		IStatus status = Status.OK_STATUS;
 		if (fLibraryContentProvider.getLibraries().length == 0) { // &&
 			// !isDefaultSystemLibrary())
 			// {
 			status = new Status(
 					IStatus.ERROR,
 					DLTKDebugUIPlugin.getUniqueIdentifier(),
 					IDLTKDebugUIConstants.INTERNAL_ERROR,
 					InterpretersMessages.InterpreterLibraryBlock_Libraries_cannot_be_empty__1,
 					null);
 		}
 		LibraryStandin[] standins = fLibraryContentProvider.getStandins();
 		for (int i = 0; i < standins.length; i++) {
 			IStatus st = standins[i].validate();
 			if (!st.isOK()) {
 				status = st;
 				break;
 			}
 		}
 		updateDialogStatus(status);
 	}
 
 	/**
 	 * Saves settings in the given working copy
 	 */
 	public void performApply(IInterpreterInstall install) {
 		if (isDefaultLocations()) {
 			install.setLibraryLocations(null);
 		} else {
 			LibraryLocation[] libs = fLibraryContentProvider.getLibraries();
 			install.setLibraryLocations(libs);
 		}
 	}
 
 	/**
 	 * Determines if the present setup is the default location s for this
 	 * InterpreterEnvironment
 	 * 
 	 * @return true if the current set of locations are the defaults, false
 	 *         otherwise
 	 */
 	protected boolean isDefaultLocations() {
 		LibraryLocation[] libraryLocations = fLibraryContentProvider
 				.getLibraries();
 		IInterpreterInstall install = getInterpreterInstall();
 
 		if (install == null || libraryLocations == null) {
 			return true;
 		}
 		File installLocation = install.getInstallLocation();
 		if (installLocation != null) {
 			LibraryLocation[] def = getInterpreterInstallType()
 					.getDefaultLibraryLocations(installLocation,
 							install.getEnvironmentVariables());
 			if (def.length == libraryLocations.length) {
 				for (int i = 0; i < def.length; i++) {
 					if (!def[i].equals(libraryLocations[i])) {
 						return false;
 					}
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Returns the Interpreter install associated with this library block.
 	 * 
 	 * @return Interpreter install
 	 */
 	protected IInterpreterInstall getInterpreterInstall() {
 		return fInterpreterInstall;
 	}
 
 	/**
 	 * Returns the Interpreter install type associated with this library block.
 	 * 
 	 * @return Interpreter install
 	 */
 	protected IInterpreterInstallType getInterpreterInstallType() {
 		return fInterpreterInstallType;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
 	 */
 	public void widgetSelected(SelectionEvent e) {
 		Object source = e.getSource();
 		if (source == fUpButton) {
 			fLibraryContentProvider.up((IStructuredSelection) fLibraryViewer
 					.getSelection());
 		} else if (source == fDownButton) {
 			fLibraryContentProvider.down((IStructuredSelection) fLibraryViewer
 					.getSelection());
 		} else if (source == fRemoveButton) {
 			fLibraryContentProvider
 					.remove((IStructuredSelection) fLibraryViewer
 							.getSelection());
 		} else if (source == fAddButton) {
 			add((IStructuredSelection) fLibraryViewer.getSelection());
 		} else if (source == fDefaultButton) {
 			restoreDefaultLibraries();
 		}
 		update();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
 	 */
 	public void widgetDefaultSelected(SelectionEvent e) {
 	}
 
 	private void add(IStructuredSelection selection) {
 		LibraryLocation libs = add();
 		if (libs == null)
 			return;
 		fLibraryContentProvider.add(new LibraryLocation[] { libs }, selection);
 		update();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
 	 */
 	public void selectionChanged(SelectionChangedEvent event) {
 		updateButtons();
 	}
 
 	/**
 	 * Refresh the enable/disable state for the buttons.
 	 */
 	private void updateButtons() {
 		IStructuredSelection selection = (IStructuredSelection) fLibraryViewer
 				.getSelection();
 		fRemoveButton.setEnabled(!selection.isEmpty());
 		boolean enableUp = true, enableDown = true;
 		Object[] libraries = fLibraryContentProvider.getElements(null);
 		if (selection.isEmpty() || libraries.length == 0) {
 			enableUp = false;
 			enableDown = false;
 		} else {
 			Object first = libraries[0];
 			Object last = libraries[libraries.length - 1];
 			for (Iterator iter = selection.iterator(); iter.hasNext();) {
 				Object element = iter.next();
 				Object lib;
 				lib = element;
 				if (lib == first) {
 					enableUp = false;
 				}
 				if (lib == last) {
 					enableDown = false;
 				}
 			}
 		}
 		fUpButton.setEnabled(enableUp);
 		fDownButton.setEnabled(enableDown);
 	}
 
 	/**
 	 * Initializes this control based on the settings in the given Interpreter
 	 * install and type.
 	 * 
 	 * @param Interpreter
 	 *            Interpreter or <code>null</code> if none
 	 * @param type
 	 *            type of Interpreter install
 	 */
 
 	public void initializeFrom(IInterpreterInstall Interpreter,
 			IInterpreterInstallType type) {
 		fInterpreterInstall = Interpreter;
 		fInterpreterInstallType = type;
 		if (Interpreter != null) {
 			setHomeDirectory(Interpreter.getInstallLocation());
 			fLibraryContentProvider.setLibraries(ScriptRuntime
 					.getLibraryLocations(getInterpreterInstall()));
 		}
 		update();
 	}
 
 	protected abstract IBaseLabelProvider getLabelProvider();
 
 	protected void updateDialogStatus(IStatus status) {
 		fDialog.setSystemLibraryStatus(status);
 		fDialog.updateStatusLine();
 	}
 
 	protected void setButtonLayoutData(Button button) {
 		fDialog.setButtonLayoutData(button);
 	}
 
 	protected abstract IDialogSettings getDialogSettions();
 
 	protected LibraryLocation add() {
 		IDialogSettings dialogSettings = getDialogSettions();
 		String lastUsedPath = dialogSettings.get(LAST_PATH_SETTING);
 		if (lastUsedPath == null) {
 			lastUsedPath = ""; //$NON-NLS-1$
 		}
 		DirectoryDialog dialog = new DirectoryDialog(fLibraryViewer
 				.getControl().getShell(), SWT.MULTI);
 		dialog.setMessage(InterpretersMessages.InterpreterLibraryBlock_10);
 		dialog.setFilterPath(lastUsedPath);
 		String res = dialog.open();
 		if (res == null) {
 			return null;
 		}
 
 		IPath path = new Path(res);
 		LibraryLocation lib = new LibraryLocation(path.makeAbsolute());
 		dialogSettings.put(LAST_PATH_SETTING, path.toOSString());
 		return lib;
 	}
 
 }
