 package edu.uiuc.immutability.ui;
 
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.ltk.core.refactoring.RefactoringStatus;
 import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 import edu.uiuc.immutability.MakeImmutableRefactoring;
 
 public class MakeImmutableInputPage extends UserInputWizardPage implements
 		IWizardPage {
 
 	Text fNameField;
 
 	public MakeImmutableInputPage(String name) {
 		super(name);
 	}
 
 	public void createControl(Composite parent) {
 		Composite result= new Composite(parent, SWT.NONE);
 
 		setControl(result);
 
 		GridLayout layout= new GridLayout();
 		layout.numColumns= 2;
 		result.setLayout(layout);
 
 		Label label= new Label(result, SWT.NONE);
 		label.setText("&Field name:");
 
 		fNameField = createNameField(result);
 		fNameField.setEditable(false);
 		final MakeImmutableRefactoring refactoring = getConvertToParallelArrayRefactoring();
 		
 		// Class name
 		fNameField.setText(refactoring.getClassName());
 		fNameField.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent event) {
 				handleInputChanged();
 			}
 		});
 
 		fNameField.setFocus();
 		fNameField.selectAll();
 		
 		handleInputChanged();
 	}
 
 	private Text createNameField(Composite result) {
 		Text field= new Text(result, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		field.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		return field;
 	}
 
 	private MakeImmutableRefactoring getConvertToParallelArrayRefactoring() {
 		return (MakeImmutableRefactoring) getRefactoring();
 	}
 
 	void handleInputChanged() {
 		RefactoringStatus status= new RefactoringStatus();
 
 		setPageComplete(!status.hasError());
 		int severity= status.getSeverity();
 		String message= status.getMessageMatchingSeverity(severity);
 		if (severity >= RefactoringStatus.INFO) {
 			setMessage(message, severity);
 		} else {
 			setMessage("", NONE); //$NON-NLS-1$
 		}
 	}
 }
