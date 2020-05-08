 package org.dawb.common.ui.wizard;
 
 import java.io.File;
 
import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
 
 /**
  * A wizard page for choosing any file in a project.
  * 
  * @author fcp94556
  *
  */
 public class NewFileChoosePage extends WizardNewFileCreationPage {
 
 	private boolean overwrite = false;
 	private Button overwriteBtn;
 	
 	public NewFileChoosePage(String pageName, IStructuredSelection selection) {
 		super(pageName, selection);
 	}
 
 	protected void createAdvancedControls(Composite parent) {
 		this.overwriteBtn = new Button(parent, SWT.CHECK);
 		overwriteBtn.setSelection(overwrite);
 		
         overwriteBtn.setText("Overwrite the file if it exists");
         overwriteBtn.addSelectionListener(new SelectionAdapter() {
         	public void widgetSelected(SelectionEvent e) {
                	overwrite = overwriteBtn.getSelection();
                	setErrorMessage(null);
                	validatePage();
         	}       	
  		});
 		
         final Label score = new Label(parent, SWT.HORIZONTAL|SWT.SEPARATOR);
         score.setLayoutData(new GridData(SWT.FILL , SWT.CENTER, true, false));
 		super.createAdvancedControls(parent);
 	}
 	
 	protected boolean validatePage() {
 
 		boolean ok = super.validatePage();
 		if (!ok && overwrite) { // check if not ok because 
 			                    // might not because because existing
 			                    // in that case overwrite may have been chosen.
 			try {
 				final File file = getFile();
 				if (!file.isDirectory() && file.exists() && overwrite) {
 					if (file.canWrite()) {
 						setErrorMessage(null);
 					    ok = true;
 						setPageComplete(true);
 					} else {
 						setErrorMessage("'"+getFileName()+"' is read only can cannot be overwritten.");
 						setPageComplete(false);
 					}
 				}
 			} catch (Throwable ne) {
 				// Ignored because ok is already false.
 			}
 		}
 		if (!ok) setPageComplete(false);
 		return ok;
 	}
 
 	public File getFile() {
		final IFile ifile = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(getContainerFullPath()+"/"+getFileName()));
		if (ifile!=null) return new File(ifile.getLocation().toOSString());
 		final String dir  = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString()+getContainerFullPath().toOSString();
 		return new File(dir, getFileName());
 	}
 	
 	public void setFileName(String name) {
 		super.setFileName(name);
 		if (getContainerFullPath()!=null && name!=null) {
 			overwrite = true;
 			if (overwriteBtn!=null) {
 				overwriteBtn.setSelection(true);
 				validatePage();
 			}
 		}
 	}
 }
