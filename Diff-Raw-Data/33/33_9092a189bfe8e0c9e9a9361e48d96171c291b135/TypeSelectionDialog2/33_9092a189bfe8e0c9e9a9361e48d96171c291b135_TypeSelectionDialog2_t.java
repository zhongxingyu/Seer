 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.ui.dialogs;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.core.runtime.jobs.IJobManager;
 import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.search.IDLTKSearchConstants;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.core.search.NopTypeNameRequestor;
 import org.eclipse.dltk.core.search.SearchEngine;
 import org.eclipse.dltk.core.search.SearchPattern;
 import org.eclipse.dltk.core.search.TypeNameMatch;
 import org.eclipse.dltk.internal.corext.util.Messages;
 import org.eclipse.dltk.internal.corext.util.OpenTypeHistory;
 import org.eclipse.dltk.internal.ui.DLTKUIMessages;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.dltk.ui.IDLTKUILanguageToolkit;
 import org.eclipse.dltk.ui.ScriptElementLabels;
 import org.eclipse.dltk.ui.dialogs.TypeSelectionExtension;
 import org.eclipse.dltk.ui.util.ExceptionHandler;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.operation.IRunnableContext;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.ISelectionStatusValidator;
 import org.eclipse.ui.dialogs.SelectionStatusDialog;
 
 public class TypeSelectionDialog2 extends SelectionStatusDialog {
 
 	private String fTitle;
 
 	private boolean fMultipleSelection;
 	private IRunnableContext fRunnableContext;
 	private IDLTKSearchScope fScope;
 	private int fElementKind;
 
 	private String fInitialFilter;
 	private int fSelectionMode;
 	private ISelectionStatusValidator fValidator;
 	private TypeSelectionComponent fContent;
 	private TypeSelectionExtension fExtension;
 
 	public static final int NONE = TypeSelectionComponent.NONE;
 	public static final int CARET_BEGINNING = TypeSelectionComponent.CARET_BEGINNING;
 	public static final int FULL_SELECTION = TypeSelectionComponent.FULL_SELECTION;
 
 	private static boolean fgFirstTime = true;
 
 	private IDLTKUILanguageToolkit fToolkit;
 
 	private class TitleLabel implements TypeSelectionComponent.ITitleLabel {
 		public void setText(String text) {
 			if (text == null || text.length() == 0) {
 				getShell().setText(fTitle);
 			} else {
 				getShell()
 						.setText(
 								Messages
 										.format(
 												DLTKUIMessages.TypeSelectionDialog2_title_format,
 												new String[] { fTitle, text }));
 			}
 		}
 	}
 
 	public TypeSelectionDialog2(Shell parent, boolean multi,
 			IRunnableContext context, IDLTKSearchScope scope, int elementKinds,
 			IDLTKUILanguageToolkit toolkit) {
 		this(parent, multi, context, scope, elementKinds, null, toolkit);
 	}
 
 	public TypeSelectionDialog2(Shell parent, boolean multi,
 			IRunnableContext context, IDLTKSearchScope scope, int elementKinds,
 			TypeSelectionExtension extension, IDLTKUILanguageToolkit toolkit) {
 		super(parent);
 		setShellStyle(getShellStyle() | SWT.RESIZE);
 		fMultipleSelection = multi;
 		fRunnableContext = context;
 		fScope = scope;
 		fElementKind = elementKinds;
 		fSelectionMode = NONE;
 		fExtension = extension;
 		if (fExtension != null) {
 			fValidator = fExtension.getSelectionValidator();
 		}
 		this.fToolkit = toolkit;
 	}
 
 	public void setFilter(String filter) {
 		setFilter(filter, FULL_SELECTION);
 	}
 
 	public void setFilter(String filter, int selectionMode) {
 		fInitialFilter = filter;
 		fSelectionMode = selectionMode;
 	}
 
 	public void setValidator(ISelectionStatusValidator validator) {
 		fValidator = validator;
 	}
 
 	protected TypeNameMatch[] getSelectedTypes() {
 		if (fContent == null || fContent.isDisposed())
 			return null;
 		return fContent.getSelection();
 	}
 
 	public void create() {
 		super.create();
 		fContent.populate(fSelectionMode);
 		getOkButton().setEnabled(fContent.getSelection().length > 0);
 	}
 
 	protected void configureShell(Shell shell) {
 		super.configureShell(shell);
 		// PlatformUI.getWorkbench().getHelpSystem().setHelp(shell,
 		// IJavaHelpContextIds.TYPE_SELECTION_DIALOG2);
 	}
 
 	protected Control createDialogArea(Composite parent) {
 		Composite area = (Composite) super.createDialogArea(parent);
 		fContent = new TypeSelectionComponent(area, SWT.NONE, getMessage(),
 				fMultipleSelection, fScope, fElementKind, fInitialFilter,
 				new TitleLabel(), fExtension, this.fToolkit);
 		GridData gd = new GridData(GridData.FILL_BOTH);
 		fContent.setLayoutData(gd);
 		fContent.addSelectionListener(new SelectionListener() {
 			public void widgetDefaultSelected(SelectionEvent e) {
 				handleDefaultSelected(fContent.getSelection());
 			}
 
 			public void widgetSelected(SelectionEvent e) {
 				handleWidgetSelected(fContent.getSelection());
 			}
 		});
 		return area;
 	}
 
 	protected void handleDefaultSelected(TypeNameMatch[] selection) {
 		if (selection.length == 0)
 			return;
 		okPressed();
 	}
 
 	protected void handleWidgetSelected(TypeNameMatch[] selection) {
 		IStatus status = null;
 		if (selection.length == 0) {
 			status = new Status(IStatus.ERROR, DLTKUIPlugin.getPluginId(),
 					IStatus.ERROR, "", null); //$NON-NLS-1$
 		} else {
 			if (fValidator != null) {
 				List jElements = new ArrayList();
 				for (int i = 0; i < selection.length; i++) {
 					IType type = selection[i].getType();
 					if (type != null) {
 						jElements.add(type);
 					} else {
 						status = new Status(
 								IStatus.ERROR,
 								DLTKUIPlugin.getPluginId(),
 								IStatus.ERROR,
 								Messages
 										.format(
 												DLTKUIMessages.TypeSelectionDialog_error_type_doesnot_exist,
 												selection[i]
 														.getFullyQualifiedName()),
 								null);
 						break;
 					}
 				}
 				if (status == null) {
 					status = fValidator.validate(jElements.toArray());
 				}
 			} else {
 				status = new Status(IStatus.OK, DLTKUIPlugin.getPluginId(),
 						IStatus.OK, "", null); //$NON-NLS-1$
 			}
 		}
 		updateStatus(status);
 	}
 
 	public int open() {
 		try {
 			ensureConsistency();
 		} catch (InvocationTargetException e) {
 			ExceptionHandler.handle(e,
 					DLTKUIMessages.TypeSelectionDialog_error3Title,
 					DLTKUIMessages.TypeSelectionDialog_error3Message);
 			return CANCEL;
 		} catch (InterruptedException e) {
 			// cancelled by user
 			return CANCEL;
 		}
 		if (fInitialFilter == null) {
 			IWorkbenchWindow window = DLTKUIPlugin.getActiveWorkbenchWindow();
 			if (window != null) {
 				ISelection selection = window.getSelectionService()
 						.getSelection();
 				if (selection instanceof ITextSelection) {
 					String text = ((ITextSelection) selection).getText();
					setInitialFilter(text);
				}else if (selection instanceof IStructuredSelection) {
					Object object = ((IStructuredSelection) selection).getFirstElement();
					if (object instanceof IModelElement) {
						IModelElement element = (IModelElement) object;
						String text = element.getElementName();
						setInitialFilter(text);
 					}
 				}
 			}
 		}
 		return super.open();
 	}
 
	private void setInitialFilter(String text) {
		if (text != null) {
			text = text.trim();
			if (text.length() > 0 /*
								 * TODO: Add validate source type
								 * call
								 */) {
				setFilter(text);
			}
		}
	}

 	public boolean close() {
 		boolean result;
 		try {
 			if (getReturnCode() == OK) {
 				OpenTypeHistory.getInstance(this.fToolkit).save();
 			}
 		} finally {
 			result = super.close();
 		}
 		return result;
 	}
 
 	public void setTitle(String title) {
 		super.setTitle(title);
 		fTitle = title;
 	}
 
 	protected void computeResult() {
 		TypeNameMatch[] selected = fContent.getSelection();
 		if (selected == null || selected.length == 0) {
 			setResult(null);
 			return;
 		}
 
 		// If the scope is null then it got computed by the type selection
 		// component.
 		if (fScope == null) {
 			fScope = fContent.getScope();
 		}
 
 		OpenTypeHistory history = OpenTypeHistory.getInstance(this.fToolkit);
 		List result = new ArrayList(selected.length);
 		for (int i = 0; i < selected.length; i++) {
 			TypeNameMatch typeInfo = selected[i];
 			IType type = typeInfo.getType();
 			if (!type.exists()) {
 				ISourceModule module = type.getSourceModule();
 				if (module.exists()) {
 					result.add(module);
 				} else {
 					String title = DLTKUIMessages.TypeSelectionDialog_errorTitle;
 					IProjectFragment root = typeInfo.getProjectFragment();
 					ScriptElementLabels labels = this.fToolkit
 							.getScriptElementLabels();
 					String containerName = labels.getElementLabel(root,
 							ScriptElementLabels.ROOT_QUALIFIED);
 					String message = Messages.format(
 							DLTKUIMessages.TypeSelectionDialog_dialogMessage,
 							new String[] { typeInfo.getFullyQualifiedName(),
 									containerName });
 					MessageDialog.openError(getShell(), title, message);
 					history.remove(typeInfo);
 					setResult(null);
 				}
 			} else {
 				history.accessed(typeInfo);
 				result.add(type);
 			}
 		}
 		setResult(result);
 	}
 
 	private void ensureConsistency() throws InvocationTargetException,
 			InterruptedException {
 		// we only have to ensure history consistency here since the search
 		// engine
 		// takes care of working copies.
 		class ConsistencyRunnable implements IRunnableWithProgress {
 			public void run(IProgressMonitor monitor)
 					throws InvocationTargetException, InterruptedException {
 				if (fgFirstTime) {
 					// Join the initialize after load job.
 					IJobManager manager = Job.getJobManager();
 					manager.join(DLTKUIPlugin.PLUGIN_ID, monitor);
 				}
 				OpenTypeHistory history = OpenTypeHistory.getInstance(fToolkit);
 				if (fgFirstTime || history.isEmpty()) {
 					monitor
 							.beginTask(
 									DLTKUIMessages.TypeSelectionDialog_progress_consistency,
 									100);
 					if (history.needConsistencyCheck()) {
 						refreshSearchIndices(new SubProgressMonitor(monitor, 90));
 						history.checkConsistency(new SubProgressMonitor(
 								monitor, 10));
 					} else {
 						refreshSearchIndices(monitor);
 					}
 					monitor.done();
 					fgFirstTime = false;
 				} else {
 					history.checkConsistency(monitor);
 				}
 			}
 
 			public boolean needsExecution() {
 				OpenTypeHistory history = OpenTypeHistory.getInstance(fToolkit);
 				return fgFirstTime || history.isEmpty()
 						|| history.needConsistencyCheck();
 			}
 
 			private void refreshSearchIndices(IProgressMonitor monitor)
 					throws InvocationTargetException {
 				try {
 					new SearchEngine().searchAllTypeNames(
 							null,
 							0,
 							// make sure we search a concrete name. This is
 							// faster according to Kent
 							"_______________".toCharArray(), //$NON-NLS-1$
 							SearchPattern.R_EXACT_MATCH
 									| SearchPattern.R_CASE_SENSITIVE,
 							IDLTKSearchConstants.FIELD, SearchEngine
 									.createWorkspaceScope(fToolkit
 											.getCoreToolkit()),
 							new NopTypeNameRequestor(),
 							IDLTKSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
 							monitor);
 				} catch (ModelException e) {
 					throw new InvocationTargetException(e);
 				}
 			}
 		}
 		ConsistencyRunnable runnable = new ConsistencyRunnable();
 		if (!runnable.needsExecution())
 			return;
 		IRunnableContext context = fRunnableContext != null ? fRunnableContext
 				: PlatformUI.getWorkbench().getProgressService();
 		context.run(true, true, runnable);
 	}
 }
