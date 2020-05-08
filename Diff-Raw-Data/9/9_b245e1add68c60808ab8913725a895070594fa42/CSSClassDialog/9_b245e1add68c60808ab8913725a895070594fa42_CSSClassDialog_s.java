 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.jboss.tools.jst.jsp.outline.cssdialog;
 
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.TitleAreaDialog;
 import org.eclipse.jface.fieldassist.ComboContentAdapter;
 import org.eclipse.jface.fieldassist.ContentProposalAdapter;
 import org.eclipse.jface.fieldassist.IContentProposal;
 import org.eclipse.jface.fieldassist.IContentProposalListener;
 import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.FocusAdapter;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
 import org.eclipse.ui.fieldassist.ContentAssistCommandAdapter;
 import org.eclipse.ui.model.BaseWorkbenchContentProvider;
 import org.eclipse.ui.model.WorkbenchLabelProvider;
 import org.eclipse.ui.progress.UIJob;
 import org.jboss.tools.common.model.ui.widgets.Split;
 import org.jboss.tools.jst.jsp.JspEditorPlugin;
 import org.jboss.tools.jst.jsp.messages.JstUIMessages;
 import org.jboss.tools.jst.jsp.outline.cssdialog.common.CSSModel;
 import org.jboss.tools.jst.jsp.outline.cssdialog.common.Constants;
 import org.jboss.tools.jst.jsp.outline.cssdialog.common.FileExtensionFilter;
 import org.jboss.tools.jst.jsp.outline.cssdialog.common.Util;
 import org.jboss.tools.jst.jsp.outline.cssdialog.events.ChangeStyleEvent;
 import org.jboss.tools.jst.jsp.outline.cssdialog.events.ChangeStyleListener;
 import org.jboss.tools.jst.jsp.outline.cssdialog.events.ManualChangeStyleListener;
 import org.jboss.tools.jst.jsp.outline.cssdialog.events.MessageDialogEvent;
 import org.jboss.tools.jst.jsp.outline.cssdialog.events.MessageDialogListener;
 import org.jboss.tools.jst.jsp.outline.cssdialog.events.StyleAttributes;
 
 /**
  * This dialog represents CSSClass dialog.
  *
  * @author Igor Zhukov (izhukov@exadel.com)
  */
 public class CSSClassDialog extends TitleAreaDialog {
 
 	public static final String ID = "org.jboss.tools.jst.jsp.outline.cssdialog.CSSClassDialog"; //$NON-NLS-1$
 
 	private static String notUsed = "not_used"; //$NON-NLS-1$
     private final static String[] fileExtensions = { Util.CSS_FILE_EXTENTION };
 
     private Composite browserContainer = null;
     private Browser browser = null;
     private Text textBrowser = null;
     private String previewBrowserValue = JstUIMessages.DEFAULT_TEXT_FOR_BROWSER_PREVIEW;
 
     private StyleComposite styleComposite = null;
     private StyleAttributes styleAttributes = null;
     // css file path
     private Text text;
     // css style classes
     private Combo classCombo;
     // combo box content assist
 	private ContentAssistCommandAdapter contentAssistAdapter = null;
     // apply button
     private Button applyButton;
 
     // model is the core of the CSS Class Dialog, it manages style attributes 
     private CSSModel cssModel;
 
     // file is "current" in case of the following priority order:
     // 	1	-	this is selected css file in eclipse project tree
     //	2	-	this is opened and active css file
     private IFile currentFile;
     private String currentClassStyle = null;
 
 	// workbench selection when the wizard was started
 	protected IStructuredSelection selection;
 
 	private boolean styleChanged = false;
 
 	private boolean keyInputSelector = false;
 
 	// Status variables for the possible errors on this page.
 	// 1. timeStatus holds an error if CSS file is not specified
 	private IStatus filePathStatus = null;
 	// 2. holds an error if the destination class style is empty
 	private IStatus classNameStatus = null;
 
 	// an array of subscribed message dialog listener 
     private ArrayList<MessageDialogListener> errorListeners = new ArrayList<MessageDialogListener>();
 
     // parameter indicates if dialog was opened from Wizard
     private final boolean callFromWizard;
 
     // this job is used to correctly process change style class combo text with delay
 	private UIJob uiJob = null;
 	// the job name
 	private String jobName = "Update CSS Composite"; //$NON-NLS-1$
 	// delay for job in milliseconds
 	private int delay = 1000;
 
     /**
      * Constructor.
      *
      * @param parentShell Shell object
      * @param allProject (if allProject is true - browse css file in all projects, else only in current project)
      * @param callFromWizard indicates if CSS dialog is created within Wizard page
      */
     public CSSClassDialog(Shell parentShell, IStructuredSelection selection, boolean callFromWizard) {
         super(parentShell);
         
         setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL);
 
         filePathStatus = new Status(IStatus.ERROR, notUsed, 0, JstUIMessages.CSS_EMPTY_FILE_PATH_MESSAGE, null);
         classNameStatus = new Status(IStatus.ERROR, notUsed, 0, JstUIMessages.CSS_EMPTY_STYLE_CLASS_MESSAGE, null);
 
         styleAttributes = new StyleAttributes();
         this.callFromWizard = callFromWizard;
     	this.selection = selection;
     	init();
     }
 
     /**
      * Initialize method.
      */
     private void init() {
 		if (selection != null && !selection.isEmpty()) {
 			for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
 				Object element = iterator.next();
 				if (element instanceof IResource) {
 					if (element instanceof IFile) {
 						if (((IFile) element).getName().toLowerCase().endsWith(Util.CSS_FILE_EXTENTION)) {
 							currentFile = (IFile)element;
 						}
 					}
 				}
 			}
 		}
 		// if any CSS file is currently opened and has active page status this method should return style class
 		// within this file where cursor is located
 		if (currentFile == null) {
 			currentFile = Util.getActiveCssFile();
 		}
 		currentClassStyle = Util.getActivePageCSSSelectorIfAny();
     }
 
     /**
      * Sets current style class value.
      *
      * @param currentClassStyle String value
      */
     public void setCurrentStyleClass(String currentClassStyle) {
     	this.currentClassStyle = Util.formatStyleClassToCSSView(currentClassStyle);
     }
 
     /**
      * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(Composite)
      */
     @Override
     protected Control createDialogArea(final Composite parent) {
         final Composite composite = (Composite) super.createDialogArea(parent);
         if(composite.getLayoutData()!=null && composite.getLayoutData() instanceof GridData) {
         	((GridData)composite.getLayoutData()).widthHint=500;
         	((GridData)composite.getLayoutData()).heightHint=500;
         }
         final Control control = createDialogComposite(composite); 
         return control;
     }
     
     public Control createDialog(final Composite parent) {
     	return  createDialogArea(parent);
 	}
 
     /**
      * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(Composite)
      */
     @Override
     protected void createButtonsForButtonBar(Composite parent) {
     	super.createButtonsForButtonBar(parent);
     	updateOKButtonState();
 
     }
     private Split split;
     /**
      * Create the dialog itself.
      *
      * @param composite parent window
      * @return eclipse Control object
      */
     private Control createDialogComposite(Composite composite) {
         if (!this.callFromWizard) {
             setTitle(JstUIMessages.CSS_STYLE_CLASS_EDITOR_TITLE);
             setMessage(JstUIMessages.CSS_STYLE_CLASS_EDITOR_DESCRIPTION);
         }
         composite.setLayout(new GridLayout());
         
         // ===============================================================================
         // Create split component that separates dialog on 2 parts
         // ===============================================================================
 		split = new Split(composite, SWT.VERTICAL);
 
         // ===============================================================================
         // Create browser container
         // ===============================================================================
         browserContainer = getCompositeElement(split);
         // create browser component
         createBrowserComponent();
 
         // ===============================================================================
         // Create down splitter container
         // ===============================================================================
         Composite downSplitPane = getCompositeElement(split);
 
         Composite classComposite = new Composite(downSplitPane, SWT.BORDER);
         classComposite.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
         classComposite.setLayout(new GridLayout(3, false));
 
         // ===============================================================================
         // Create component that contains CSS file pass and it's selectors (style classes combo box)
         // ===============================================================================
         createCSSFilePathComponent(classComposite);
         createStyleClassCombo(classComposite);
 
         // ===============================================================================
         // Create style composite component
         // ===============================================================================
         styleComposite = new StyleComposite(downSplitPane, styleAttributes, Constants.EMPTY);
         styleComposite.addManualChangeStyleListener(new ManualChangeStyleListener() {
             public void styleChanged(ChangeStyleEvent event) {
            		styleChanged = true;
            		if (currentClassStyle != null && !currentClassStyle.equals(Constants.EMPTY)
         				&& currentFile != null && !currentFile.equals(Constants.EMPTY)) {
                		applyButton.setEnabled(true);
            		}
             }
         });
 
         // ===============================================================================
         // Create custom button panel
         // ===============================================================================
         createCustomButtonPanel(downSplitPane);
 
         styleAttributes.addChangeStyleListener(new ChangeStyleListener() {
             public void styleChanged(ChangeStyleEvent event) {
             	if (!browser.isDisposed()) {
             		browser.setText(getTextForBrowser());
             	}
             }
         });
 
         if (currentFile != null) {
         	initCSSModel(currentFile, true, true);
         } else if (currentClassStyle != null) {
         	classCombo.setText(currentClassStyle);
     		styleAttributes.setCssSelector(currentClassStyle);
         }
 
     	// add content assist to style COMBO component
 		SimpleContentProposalProvider proposalProvider = new SimpleContentProposalProvider(classCombo.getItems());
 		proposalProvider.setFiltering(true);
 		contentAssistAdapter = new ContentAssistCommandAdapter(
 				classCombo, new ComboContentAdapter(), proposalProvider, null, null, true);
 		contentAssistAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
 		contentAssistAdapter.addContentProposalListener(new IContentProposalListener() {
 			public void proposalAccepted(IContentProposal proposal) {
 				cssStyleClassChanged();
 				applyButton.setEnabled(false);
 				keyInputSelector = false;
 			}
 		});
 
         split.setWeights(new int[]{15, 85});
         split.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, true));
 
         return composite;
     }
 
     private void createCSSFilePathComponent(Composite parent) {
         Label label = new Label(parent, SWT.LEFT);
         label.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
         label.setText(JstUIMessages.CSS_CLASS_DIALOG_FILE_LABEL);
 
         // Text field contains path to the CSS file
         text = new Text(parent, SWT.BORDER);
         text.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
         text.setEditable(false);
         text.addModifyListener(new ModifyListener() {
             public void modifyText(ModifyEvent e) {
     			String cssFile = text.getText().trim();
     			// Initialize a variable with the no error status
 				filePathStatus = new Status(IStatus.ERROR, notUsed, 0, JstUIMessages.CSS_EMPTY_FILE_PATH_MESSAGE, null);
     			if (cssFile != null && !cssFile.equals(Constants.EMPTY)) {
         			filePathStatus = new Status(IStatus.OK, notUsed, 0, JstUIMessages.CSS_STYLE_CLASS_EDITOR_DESCRIPTION, null);
     			}
     			// show corresponding message
     			IStatus status = findMostSevere();
     			notifyListeners(text, status);
     			applyToStatusLine(status);
             }
         });
 
         Button button = new Button(parent, SWT.PUSH);
         button.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
 
         ImageDescriptor imageDesc = JspEditorPlugin.getImageDescriptor(Constants.IMAGE_FOLDERLARGE_FILE_LOCATION);
         Image image = imageDesc.createImage();
         button.setImage(image);
         button.setToolTipText(JstUIMessages.CSS_BROWSE_BUTTON_TOOLTIP);
         button.addDisposeListener(new DisposeListener() {
                 public void widgetDisposed(DisposeEvent e) {
                     Button button = (Button) e.getSource();
                     button.getImage().dispose();
                 }
             });
         button.addSelectionListener(new SelectionAdapter() {
             public void widgetSelected(SelectionEvent event) {
 //    			IResource project = Util.getCurrentProject();
     			IResource project = ResourcesPlugin.getWorkspace().getRoot();
 
     			ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(),
     					new WorkbenchLabelProvider(), new BaseWorkbenchContentProvider());
     			dialog.addFilter(new FileExtensionFilter(fileExtensions));
     			dialog.setTitle(JstUIMessages.CSS_FILE_SELECT_DIALOG_TITLE);
     			dialog.setMessage(JstUIMessages.CSS_FILE_SELECT_DIALOG_LABEL);
     			dialog.setInput(project);
     			dialog.setAllowMultiple(false);
     			dialog.setDoubleClickSelects(false);
     			if (currentFile != null) {
     				dialog.setInitialSelection(currentFile);
     			}
     			dialog.setEmptyListMessage(JstUIMessages.CSS_FILE_SELECT_DIALOG_EMPTY_MESSAGE);
 
     			if (dialog.open() == Window.OK) {
     				IResource res = (IResource) dialog.getFirstResult();
     				// make some important saving actions
     				if (res instanceof IFile) {
     					if (styleChanged && currentFile != null) {
     						MessageBox messageBox = new MessageBox(getParentShell(), SWT.YES | SWT.NO | SWT.ICON_QUESTION);
     						messageBox.setText(JstUIMessages.CSS_SAVE_DIALOG_TITLE);
     						messageBox.setMessage("'" + currentFile + "' " + JstUIMessages.CSS_SAVE_DIALOG_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
     						int result = messageBox.open();
     						if (result == SWT.YES) {
     							saveChanges(true);
     						} else {
     							updateStyleComposite();
     						}
     					}
     		    		styleComposite.revertPreview();
     		    		releaseResources();
 
     					// open new CSS file and initialize dialog
     					boolean useRelativePath = true;
     					if (project instanceof IWorkspaceRoot) {
     						useRelativePath = false;
     					}
     					boolean updateCSSModel = false;
     					if (currentFile != null && !currentFile.equals(Constants.EMPTY)) {
     		            	currentClassStyle = null;
     		            	updateCSSModel = true;
     					}
     					currentFile = (IFile)res;
     					initCSSModel(currentFile, useRelativePath, updateCSSModel);
     					updateOKButtonState();
     					applyButton.setEnabled(false);
             			styleChanged = false;
 
 	                	// update content assist proposals
 	            		SimpleContentProposalProvider proposalProvider =
 	            			(SimpleContentProposalProvider)contentAssistAdapter.getContentProposalProvider();
 	            		proposalProvider.setProposals(classCombo.getItems());
     				}
         		}
             }
         });
     }
 
 	/**
 	 * This method is used to create and initialize style class comboBox component.
 	 *
 	 * @param parent Composite component
 	 */
 	private void createStyleClassCombo(Composite parent) {
         Label label = new Label(parent, SWT.LEFT);
         label.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
         label.setText(JstUIMessages.CSS_CLASS_DIALOG_STYLE_CLASS_LABEL);
 
 		GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
         gridData.horizontalSpan = 2;
 
         classCombo = new Combo(parent, SWT.BORDER);
         classCombo.setLayoutData(gridData);
         classCombo.setEnabled(false);
         // add selection listener
         classCombo.addSelectionListener(new SelectionAdapter() {
         	public void widgetSelected(SelectionEvent e) {
         		if (keyInputSelector) {
         			keyInputSelector = false;
         			if (currentClassStyle != null && classCombo.indexOf(currentClassStyle) != -1) {
         				return;
         			}
         		}
         		keyInputSelector = false;
         	}
         });
         // add key modified listener
         classCombo.addKeyListener(new KeyAdapter() {
 			public void keyReleased(KeyEvent e) {
 				keyInputSelector = true;
 			}
         });
         // this listener is responsible for processing dialog header message events
     	classCombo.addModifyListener(new ModifyListener() {
             public void modifyText(ModifyEvent e) {
     			String cssClass = classCombo.getText().trim();
     			// Initialize a variable with the no error status
 				classNameStatus = new Status(IStatus.ERROR, notUsed, 0, JstUIMessages.CSS_EMPTY_STYLE_CLASS_MESSAGE, null);
     			if (cssClass != null && !cssClass.equals(Constants.EMPTY)) {
         			classNameStatus = new Status(IStatus.OK, notUsed, 0, JstUIMessages.CSS_STYLE_CLASS_EDITOR_DESCRIPTION, null);
     			}
     			// show corresponding message
     			IStatus status = findMostSevere();
     			notifyListeners(classCombo, status);
     			applyToStatusLine(status);
     			// update CSS style cmposite if needed
 				if ((currentClassStyle != null && currentClassStyle.equals(classCombo.getText().trim()))
 						|| (currentClassStyle == null && classCombo.getText().trim().equals(Constants.EMPTY))) {
 					return;
 				}
 				notifyStyleClassChanged();
 				applyButton.setEnabled(false);
             }
         });
 	}
 
 	/**
 	 * This method is invoked to correctly process class style combo modify event.
 	 */
 	private void notifyStyleClassChanged() {
 		Display display = null;
 		if (PlatformUI.isWorkbenchRunning()) {
 			display = PlatformUI.getWorkbench().getDisplay();
 		}
 		if (display != null && (Thread.currentThread() == display.getThread())) {
 			if (uiJob == null) {
 				uiJob = new UIJob(jobName) {
 					@Override
 					public IStatus runInUIThread(IProgressMonitor monitor) {
 						if (monitor.isCanceled()) {
 							return Status.CANCEL_STATUS;
 						}
 						monitor.beginTask(jobName, IProgressMonitor.UNKNOWN);
 
 						// start operation
 						cssStyleClassChanged();
 						// end operation
 
 						monitor.done();
 
 						return Status.OK_STATUS;
 					}
 				};
 			}
 
 			uiJob.setPriority(Job.SHORT);
 			uiJob.schedule(delay);
 
 			return;
 		}
 	}
 
 	/**
 	 * This method is used to create custom button panel.
 	 *
 	 * @param parent Composite component
 	 */
 	private void createCustomButtonPanel(Composite parent) {
         Composite buttonComposite = new Composite(parent, SWT.NONE);
         buttonComposite.setLayoutData(new GridData(GridData.END, GridData.BEGINNING, true, false));
         buttonComposite.setLayout(new GridLayout());
         // add APPLY button
         applyButton = createCustomButton(buttonComposite, JstUIMessages.BUTTON_APPLY);
     	applyButton.setEnabled(false);
         applyButton.setToolTipText(JstUIMessages.CSS_APPLY_CHANGES);
         applyButton.addSelectionListener(new SelectionAdapter() {
             public void widgetSelected(SelectionEvent event) {
             	if (currentClassStyle != null && !currentClassStyle.equals(Constants.EMPTY)) {
            			// update ComboBox element list
            			if (classCombo.indexOf(currentClassStyle) == -1) {
            				classCombo.add(currentClassStyle);
            			}
                 	saveChanges(false);
                 	applyButton.setEnabled(false);
                     styleChanged = false;
             	}
             }
         });
         // add CLEAR button
         Button clearButton = createCustomButton(buttonComposite, JstUIMessages.BUTTON_CLEAR);
         clearButton.setToolTipText(JstUIMessages.CSS_CLEAR_STYLE_SHEET);
         clearButton.addSelectionListener(new SelectionAdapter() {
             public void widgetSelected(SelectionEvent event) {
                 styleComposite.clearStyleComposite(currentClassStyle);
        			styleComposite.updatePreview(currentClassStyle);
        			styleComposite.updateStyle();
             	applyButton.setEnabled(true);
                 styleChanged = true;
             }
         });
 	}
 
 	/**
 	 * This method is used to create custom button.
 	 *
 	 * @param parent Composite component
 	 * @param label Button label value
 	 */
 	protected Button createCustomButton(Composite parent, String label) {
 		// increment the number of columns in the button bar
 		((GridLayout) parent.getLayout()).numColumns++;
 		Button button = new Button(parent, SWT.PUSH);
 		button.setText(label);
 //		setButtonLayoutData(button);
 		return button;
 	}
 
 	/**
 	 * Method is used to correctly process style class change operation.
 	 */
 	private void cssStyleClassChanged() {
 		if (currentFile != null && !currentFile.equals(Constants.EMPTY)) {
 			if (styleChanged && currentClassStyle != null && !currentClassStyle.equals(Constants.EMPTY)) {
 				MessageBox messageBox = new MessageBox(getParentShell(), SWT.YES | SWT.NO | SWT.ICON_QUESTION);
 				messageBox.setText(JstUIMessages.CSS_SAVE_DIALOG_TITLE);
 				messageBox.setMessage("'" + currentFile + "' " + JstUIMessages.CSS_SAVE_DIALOG_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
 				int result = messageBox.open();
 				if (result == SWT.YES) {
            			// update ComboBox element list
            			if (classCombo.indexOf(currentClassStyle) == -1) {
            				classCombo.add(currentClassStyle);
            			}
                 	saveChanges(false);
                 	// update content assist proposals
             		SimpleContentProposalProvider proposalProvider =
             			(SimpleContentProposalProvider)contentAssistAdapter.getContentProposalProvider();
             		proposalProvider.setProposals(classCombo.getItems());
 				} else {
 					// FOR https://jira.jboss.org/jira/browse/JBIDE-3542
 					// cssModel.init(currentFile);
 					// styleComposite.revertPreview();
 				}
 			}
     		// update current class style value
 			currentClassStyle = classCombo.getText().trim();
 			
 			// if new css was added
 			if (classCombo.indexOf(currentClassStyle) == -1) {
 				applyButton.setEnabled(true);
 				classCombo.add(currentClassStyle);
 				styleChanged = true;
 			} else {
 				styleChanged = false;
 			}
     		
     		updateStyleComposite();
     		styleAttributes.setCssSelector(currentClassStyle);
 			styleComposite.updatePreview(currentClassStyle);
     		updateOKButtonState();
 		} else {
 			currentClassStyle = classCombo.getText().trim();
 			styleAttributes.setCssSelector(currentClassStyle);
 			styleChanged = false;
 		}
 		
 	}
 
     /**
      * Initialize CSS model with active opened CSS file.
      *
      * @param file IFile object
      * @param useRelativePathPath
      */
     private void initCSSModel(IFile file, boolean useRelativePathPath, boolean updateCSSModel) {
         if (file != null) {
         	// create CSS Model
         	cssModel = new CSSModel(file);
             classCombo.setEnabled(true);
             classCombo.removeAll();
             // set file path to corresponding text field
             if (useRelativePathPath) {
             	text.setText(file.getProjectRelativePath().toOSString());
             } else {
             	text.setText(file.getFullPath().toOSString());
             }
             
             Point selectionInFile = Util.getSelectionInFile(file);
 
             currentClassStyle = cssModel.getSelectorByPosition(selectionInFile);
             
             // fill in ComboBox component with CSS model selectors
 //            List<Selector> selectors = cssModel.getSelectors();
             List<String> selectors = cssModel.getSelectorLabels();
             int selectedIndex = -1;
             for (int i = 0; i < selectors.size(); i++) {
 //            	Selector value = selectors.get(i);
             	String label = selectors.get(i);
             	classCombo.add(/*value.getValue()*/ label);
             	if (currentClassStyle != null && currentClassStyle.equals(/*value.getValue()*/label)) {
             		selectedIndex = i;
             	}
             }
             if (currentClassStyle != null && selectedIndex == -1) {
             	classCombo.setText(currentClassStyle);
             } else {
             	classCombo.select(selectedIndex);
             }
             classCombo.setToolTipText(cssModel.getCSSText(currentClassStyle));
 
 	        styleComposite.setShowPreviewTab(true);
 	        styleComposite.setCSSModel(cssModel);
             // update style composite component with the values from new CSS file
             if (updateCSSModel) {
             	updateStyleComposite();
             }
             styleComposite.initPreview(cssModel);
         }
     }
 
     /**
      * This method takes affect to OK button when dialog is opened in "dialog" mode and not in "wizard".
      * In case of "wizard" mode OK button is not available.
      */
     private void updateOKButtonState() {
     	Button okButton = getButton(IDialogConstants.OK_ID);
     	if (okButton != null) {
     		if (currentClassStyle == null || currentClassStyle.equals(Constants.EMPTY) || currentFile == null) {
     			okButton.setEnabled(false);
     		} else {
     			okButton.setEnabled(true);
     		}
     	}
     }
 
     /**
      * Method is used to create browser component to display preview HTML.
      */
     private void createBrowserComponent() {
         GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
         browser = new Browser(browserContainer, SWT.BORDER | SWT.MOZILLA);
         browser.setText(getTextForBrowser());
         browser.addMouseListener(new MouseAdapter() {
         	public void mouseDoubleClick(MouseEvent e) {
         		if (e.widget == browser) {
         	        browser.removeMouseListener(this);
         	        browser.dispose();
         	        // create Text area component instead of HTML Browser
         	        GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
 //        	        textBrowser = new Text(browserContainer, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
         	        textBrowser = new Text(browserContainer, SWT.NONE | SWT.H_SCROLL);
         	        textBrowser.setText(previewBrowserValue);
         	        textBrowser.addFocusListener(new FocusAdapter() {
         	        	public void focusLost(FocusEvent e) {
         	        		if (e.widget == textBrowser) {
         	        			String text = textBrowser.getText();
         	        			if (text == null || text.equals(Constants.EMPTY)) {
         	        				previewBrowserValue = JstUIMessages.DEFAULT_TEXT_FOR_BROWSER_PREVIEW;
         	        			} else {
         	        				previewBrowserValue = text;
         	        			}
         	        			textBrowser.dispose();
         	        			// create Browse component instead of text area
         	        			createBrowserComponent();
         	        		}
         	        		browserContainer.layout();
         	        	}
         	        });
         	        textBrowser.setLayoutData(gridData);
         	        textBrowser.setEditable(true);
         	        textBrowser.setFocus();
         		}
         		browserContainer.layout();
         	}
         });
         browser.setLayoutData(gridData);
     }
 
     /**
      * Update style composite component in accordance with the attributes of selected CSS selector.
      */
     private void updateStyleComposite() {
         String style = cssModel.getStyle(currentClassStyle);
         styleComposite.recreateStyleComposite(style, currentClassStyle);
     }
 
     /**
      * Method is used to build html body that is appropriate to browse.
      *
      * @return String html text representation
      */
     private String getTextForBrowser() {
         String styleForSpan = Constants.EMPTY;
         Set<String> keySet = styleAttributes.keySet();
         for (String key : keySet) {
             styleForSpan += (key + Constants.COLON + styleAttributes.getAttribute(key) + Constants.SEMICOLON);
         }
         String html = Constants.OPEN_DIV_TAG + styleForSpan + "\">" + previewBrowserValue + Constants.CLOSE_DIV_TAG; //$NON-NLS-1$
 
         return html;
     }
 
     /**
      * Create container that take up 2 cells and contains fontSizeCombo and extFontSizeCombo elements.
      */
     private Composite getCompositeElement(Composite parent) {
         GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
         GridLayout gridLayoutTmp = new GridLayout();
         gridLayoutTmp.marginHeight = 0;
         gridLayoutTmp.marginWidth = 0;
         Composite classComposite = new Composite(parent, SWT.FILL);
         classComposite.setLayout(gridLayoutTmp);
         classComposite.setLayoutData(gridData);
 
         return classComposite;
     }
     
    public void releaseResources(){
    	
    	cssModel.releaseModel();
    }
 
     /**
      * Method should be called in case of dialog closure operation.
      */
     public void saveChanges(boolean close) {
     	styleComposite.updateStyle();
         cssModel.setCSS(currentClassStyle, styleAttributes);
         cssModel.saveModel();
     }
 
     /**
      * Gets current selected style class value.
      *
      * @return selector name
      */
     public String getSelectorName() {
         return Util.formatCSSSelectorToStyleClassView(currentClassStyle);
     }
 
     /**
      * Method for setting title for dialog
      *
      * @param newShell Shell object
      * @see org.eclipse.jface.window.Window#configureShell(Shell)
      */
     @Override
     protected void configureShell(Shell newShell) {
         super.configureShell(newShell);
         newShell.setText(JstUIMessages.CSS_STYLE_CLASS_EDITOR_TITLE);
     }
 
     /**
      * This method close the dialog.
      *
      * @return true if dialog was closed, false - otherwise
      */
     public boolean closeDialog() {
     	setReturnCode(Window.CANCEL);
     	return close();
     }
 
     /**
      * @see org.eclipse.jface.dialogs.Dialog#close()
      */
     @Override
 	public boolean close() {
 		int code = getReturnCode();
 		switch (code) {
 		case OK:
 			if (styleChanged || classCombo.indexOf(currentClassStyle) == -1) {
 				saveChanges(true);
 			}
 			break;
 		case CANCEL:
 		default:
 			// make some closure operation
 		}
 		releaseResources();
 		return super.close();
 	}
 
     /**
      * Add MessageDialogListener object.
      *
      * @param listener MessageDialogListener object to be added
      */
     public void addMessageDialogListener(MessageDialogListener listener) {
         errorListeners.add(listener);
     }
 
     /**
      * Method is used to notify all subscribed listeners about possible any errors on the page.
      */
     private void notifyListeners(Object source, IStatus operationStatus) {
     	MessageDialogEvent event = new MessageDialogEvent(source, operationStatus);
         for (MessageDialogListener listener : errorListeners) {
             listener.throwMessage(event);
         }
     }
 
     /**
      * Method return the most serious error occurs on the page and that should be displayed.
      *
      * @return IStatus object
      */
 	private IStatus findMostSevere() {
 		if (filePathStatus.matches(IStatus.ERROR)) {
 			return filePathStatus;
 		}
 		if (classNameStatus.matches(IStatus.ERROR)) {
 			return classNameStatus;
 		}
 		if (filePathStatus.getSeverity() > classNameStatus.getSeverity()) {
 			return filePathStatus;
 		}
 		else return classNameStatus;
 	}
 
 	/**
 	 * Applies the status to the status line of a dialog page.
 	 */
 	private void applyToStatusLine(IStatus status) {
 		if (!callFromWizard) {
 			String message= status.getMessage();
 			if (message.length() == 0) {
 				message = null;
 			}
 			switch (status.getSeverity()) {
 				case IStatus.OK:
 					setErrorMessage(null);
 					setMessage(message);
 					break;
 				case IStatus.WARNING:
 					setErrorMessage(null);
 					setMessage(message, WizardPage.WARNING);
 					break;
 				case IStatus.INFO:
 					setErrorMessage(null);
 					setMessage(message, WizardPage.INFORMATION);
 					break;
 				default:
 					setErrorMessage(message);
 					setMessage(null);
 					break;
 			}
 		}
 	}
 	
 }
