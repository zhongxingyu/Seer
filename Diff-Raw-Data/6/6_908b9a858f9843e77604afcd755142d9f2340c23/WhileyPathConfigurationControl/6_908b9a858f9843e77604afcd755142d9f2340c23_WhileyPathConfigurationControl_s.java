 package wyclipse.ui.pages;
 
 import java.net.URI;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.dialogs.IDialogPage;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.*;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.ui.dialogs.ContainerSelectionDialog;
 
 import wybs.util.Trie;
 import wyclipse.core.builder.WhileyPath;
 import wyclipse.ui.dialogs.VirtualContainerSelectionDialog;
 import wyclipse.ui.dialogs.NewWhileyPathBuildRuleDialog;
 import wyclipse.ui.util.VirtualContainer;
 import wyclipse.ui.util.WhileyPathViewer;
 import wyclipse.ui.util.WyclipseUI;
 
 /**
  * Provides an abstract class which constructs the standard page for configuring
  * the whileypath. This is used in at least two places:
  * <code>NewWhileyProjectWizard</code> and <code>WhileyPathPropertyPage</code>.
  * 
  * @author David J. Pearce
  * 
  */
 public class WhileyPathConfigurationControl {
 	private Shell shell;
 	private VirtualContainer project;
 	private WhileyPath whileypath;
 	
 	// WhileyPath view + controls
 	private WhileyPathViewer whileyPathViewer;
 	
 	// Default Output Folder Controls
 	private Label defaultOutputFolderLabel;
 	private Text defaultOutputFolderText; 
 	private Button defaultOutputFolderBrowseButton;
 	private Button enableVerificationButton;
 	private Button enableRuntimeAssertionsButton;
 	
 	public WhileyPathConfigurationControl(Shell shell,
 			VirtualContainer project, WhileyPath whileypath) {
 		this.shell = shell;
 		this.whileypath = whileypath;
 		this.project = project;
 	}
 	
 	public WhileyPath getWhileyPath() {
 		return whileypath;
 	}
 
 	public void setProject(VirtualContainer project) {
 		this.project = project;
 	}
 	
 	public void setWhileyPath(WhileyPath whileypath) {
 		this.whileypath = whileypath;
 		this.whileyPathViewer.setInput(whileypath);
 		IPath defaultOutputFolder = whileypath.getDefaultOutputFolder();
 		if(defaultOutputFolder != null) {
 			defaultOutputFolderText.setText(defaultOutputFolder.toString());
 			defaultOutputFolderLabel.setEnabled(true);
 			defaultOutputFolderText.setEnabled(true);
 			defaultOutputFolderBrowseButton.setEnabled(true);
 		} else {
 			defaultOutputFolderLabel.setEnabled(false);
 			defaultOutputFolderText.setEnabled(false);
 			defaultOutputFolderBrowseButton.setEnabled(false);
 			defaultOutputFolderText.setText("");
 		}
 	}
 	
 	public Composite create(Composite parent) {				
 		Composite container = new Composite(parent, SWT.NONE);
 		
 		// =====================================================================
 		// Configure Grid
 		// =====================================================================
 		
 		GridLayout layout = new GridLayout();		
 		layout.numColumns = 3;
 		layout.verticalSpacing = 9;	
 		layout.marginWidth = 20;
 		container.setLayout(layout);
 		
 		// =====================================================================
 		// Middle Section
 		// =====================================================================		
 				
 		// Create viewer which is 2 columns wide and 3 rows deep.
 		whileyPathViewer = createWhileyPathViewer(container, whileypath, 2, 5);						
 		Button addBuildButton = WyclipseUI.createButton(container, "Add Folder...",175);
 		Button addLibraryButton = WyclipseUI.createButton(container, "Add Local Library...",175);
 		Button addExternalLibraryButton = WyclipseUI.createButton(container, "Add External Library...",175);
 		Button editButton = WyclipseUI.createButton(container, "Edit",175);
 		Button removeButton = WyclipseUI.createButton(container, "Remove",175);		
 		
 		addBuildButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				handleAddRule();
 			}
 		});
 		
 		addExternalLibraryButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				handleAddExternalLibrary();
 			}
 		});
 		
 		editButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				handleEditRule();
 			}
 		});
 		
 		removeButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				handleRemoveRule();
 			}
 		});
 		
 		// =====================================================================
 		// Bottom Section
 		// =====================================================================
 		//WyclipseUI.createCheckBox(container, "Enable Folder Specific Settings", 3);
 		
 		Group settings = WyclipseUI.createGroup(container,"Global Settings",SWT.SHADOW_ETCHED_IN, 3, 3);		
 		
 		enableVerificationButton = WyclipseUI.createCheckBox(settings,
 				"Enable Verification", 3);
 		enableRuntimeAssertionsButton = WyclipseUI.createCheckBox(settings,
 				"Enable Runtime Assertions", 3);
 		
 		defaultOutputFolderLabel = WyclipseUI.createLabel(settings, "Output Folder:", 1);		
 		defaultOutputFolderText = WyclipseUI.createText(settings, "", 1);
 		defaultOutputFolderBrowseButton = WyclipseUI.createButton(settings, "Browse...",175);
 		
 		defaultOutputFolderBrowseButton
 				.addSelectionListener(new SelectionAdapter() {
 					public void widgetSelected(SelectionEvent e) {
 						handleBrowseDefaultOutputFolder();
 					}
 				});	
 		
 		enableVerificationButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				handleEnableVerification();
 			}
 		});	
 
 		enableRuntimeAssertionsButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				handleEnableRuntimeAssertions();
 			}
 		});	
 		
 		IPath defaultOutputFolder = whileypath.getDefaultOutputFolder();
 		if(defaultOutputFolder == null) {
 			// No default output folder
 			defaultOutputFolderLabel.setEnabled(false);
 			defaultOutputFolderText.setEnabled(false);
 			defaultOutputFolderBrowseButton.setEnabled(false);
 		} else {
 			defaultOutputFolderText.setText(defaultOutputFolder.toString());
 		}
 		
 		defaultOutputFolderText.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				handleModifyDefaultOutputFolder();
 			}
 		});	
 		
 		container.pack();
 		
 		return container;
 	}			
 	
 	/**
 	 * Make sure that all folders described in actions on the WhileyPath
 	 * actually exist. Also, if the defaultOutputFolder is being used, then
 	 * check that as well.
 	 * 
 	 * @param project
 	 * @param monitor
 	 * @throws CoreException
 	 */
 	public void instantiateWhileyPath(IProject project, IProgressMonitor monitor)
 			throws CoreException {
 		
 		// First, check whether the default output location exists (if applicable)
 		IPath defaultOutputLocation = whileypath.getDefaultOutputFolder();
 		if(defaultOutputLocation != null) {
 			IFolder defaultOutputFolder = project.getFolder(defaultOutputLocation);
 			if (!defaultOutputFolder.exists()) {
 				defaultOutputFolder.create(true, true, monitor);
 			}
 		}
 		
 		// Second, iterate through all the entries, looking for actions which
 		// may have folders that don't yet exist.
 		for (WhileyPath.Entry e : whileypath.getEntries()) {
 			if (e instanceof WhileyPath.BuildRule) {
 				WhileyPath.BuildRule container = (WhileyPath.BuildRule) e;
 				IPath sourceLocation = container.getSourceFolder();
 				IPath outputLocation = container.getOutputFolder();
 
 				// Create the source folder (if it doesn't already exist).
 				IFolder sourceFolder = project.getFolder(sourceLocation);
 				if (!sourceFolder.exists()) {
 					sourceFolder.create(true, true, monitor);
 				}
 
 				// Create the output folder (if applicable and it doesn't
 				// already exist).
 				if (outputLocation != null) {
 					IFolder outputFolder = project.getFolder(outputLocation);
 					if (!outputFolder.exists()) {
 						outputFolder.create(true, true, monitor);
 					}
 				}
 			}
 		}
 	}
 	// ======================================================================
 	// Call Backs
 	// ======================================================================
 
 	/**
 	 * This function is called when the add rule button is pressed.
 	 */
 	protected void handleAddRule() {
 		WhileyPath.BuildRule buildRule = new WhileyPath.BuildRule(new Path(""),
 				"**/*.whiley");
 		NewWhileyPathBuildRuleDialog dialog = new NewWhileyPathBuildRuleDialog(
 				shell, buildRule, project);
 		
 		if (dialog.open() == Window.OK) {
 			whileypath.getEntries().add(buildRule);
 			whileyPathViewer.refresh();
 		}
 	}
 	
 	/**
 	 * This function is called when the add library button is pressed.
 	 */
 	protected void handleAddExternalLibrary() {
 		FileDialog dialog = new FileDialog(shell);
 		String result = dialog.open();
 		if(result != null) {
 			IPath location = new Path(result);
 			whileypath.getEntries().add(new WhileyPath.ExternalLibrary(location, "**/*.wyil"));
 			whileyPathViewer.refresh();
 		}
 	}
 	
 	/**
 	 * This function is called when the edit button is pressed.
 	 */
 	protected void handleEditRule() {
 		WhileyPath.BuildRule buildRule = null;
 		
 		// First, extract the select item (if any)
 		TreeItem[] items = whileyPathViewer.getTree().getSelection();
 		for (TreeItem item : items) {
 			Object data = item.getData();
 			if (data instanceof WhileyPathViewer.PathNode) {			
 				WhileyPathViewer.PathNode pn = (WhileyPathViewer.PathNode) data;
 				if(pn.data instanceof WhileyPath.BuildRule) {
 					buildRule = (WhileyPath.BuildRule) pn.data;
 					break;					
 				}
 
 			}
 		}
 		
 		// Second, open the build rule dialog
 		if (buildRule != null) {
 			NewWhileyPathBuildRuleDialog dialog = new NewWhileyPathBuildRuleDialog(
 					shell, buildRule, project);
 			if (dialog.open() == Window.OK) {
 				whileyPathViewer.refresh();
 			}
 		}
 	}
 	
 	/**
 	 * This function is called when the remove button is pressed.
 	 */
 	protected void handleRemoveRule() {
 		TreeItem[] items = whileyPathViewer.getTree().getSelection();
 		for (TreeItem item : items) {
 			Object data = item.getData();
 			if (data instanceof WhileyPathViewer.PathNode) {
 				WhileyPathViewer.PathNode pn = (WhileyPathViewer.PathNode) data;
 				// Here, data refers to the WhileyPath.Entry object associated
 				// with this path node.
 				whileypath.getEntries().remove(pn.data);
 			}
 		}
 		whileyPathViewer.refresh();
 	}
 	
 	/**
 	 * This function is called when the browse button for the default output
 	 * folder is called.
 	 */
 	protected void handleBrowseDefaultOutputFolder() {
 
 		// At this point, we need to create a special selection (tree?) dialog
 		// based on what we can see in the current project's location. One of
 		// the key problems here is that the current location may not actually
 		// exist! Therefore, we want to show onyl what does exist, and give the
 		// option to create something new (again observing that it's not
 		// actually created yet).
 
 		VirtualContainerSelectionDialog dialog = new VirtualContainerSelectionDialog(
 				shell, project);
 		if (dialog.open() == Window.OK) {
 			IPath path = dialog.getResult();
 			defaultOutputFolderText.setText(path.toString());
 			whileypath.setDefaultOutputFolder(path);
 		}
 	}
 	
 	/**
 	 * This function is called when the text for the default output
 	 * folder is modified.
 	 */
 	protected void handleModifyDefaultOutputFolder() {
 		whileypath.setDefaultOutputFolder(new Path(defaultOutputFolderText
 				.getText()));
 	}
 	
 	/**
 	 * This function is called when the global enable verification toggle is
 	 * toggled.
 	 */
 	protected void handleEnableVerification() {
 		whileypath.setEnableVerification(enableVerificationButton.getSelection());
 	}
 
 	/**
 	 * This function is called when the global enable runtime assertions toggle
 	 * is toggled.
 	 */
 	protected void handleEnableRuntimeAssertions() {
 		whileypath.setEnableRuntimeAssertions(enableRuntimeAssertionsButton
 				.getSelection());
 	}
 	
 	// ======================================================================
 	// Helpers
 	// ======================================================================
 	
 	protected WhileyPathViewer createWhileyPathViewer(Composite container,
 			Object input, int horizontalSpan, int verticalSpan) {
 		WhileyPathViewer viewer = new WhileyPathViewer(container, SWT.VIRTUAL
 				| SWT.BORDER);
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);		
 		gd.horizontalSpan = horizontalSpan;
 		gd.verticalSpan = verticalSpan;
 		viewer.getTree().setLayoutData(gd);
 		viewer.setInput(input);
 		return viewer;
 	}
 }
