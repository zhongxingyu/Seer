 package de.fraunhofer.esk.openetcs.sysml2b.transformation.wizard;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.PlatformUI;
 
 public class TransformationWizardPage extends WizardPage {
 
 	private Text project;
 	
 	protected TransformationWizardPage(String pageName) {
 		super(pageName);
 		setTitle(pageName);
 		setDescription("Creates B Model");
 	}
 
 	@Override
 	public void createControl(Composite parent) {
 		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "de.fraunhofer.esk.ernest.core.cdt.help_general");
 		
 		final Composite container = new Composite(parent, SWT.NULL);
 		container.setBounds(15, 25, 300, 400);
 		
 		final Label name = new Label(container, SWT.NONE);
 		name.setText("Name of the Project:");
		name.set
 		//name.setBounds(50,50,120,27);
 		
 		project = new Text(container, SWT.BOLD | SWT.BORDER);
 		project.setBounds(180, 50, 170, 27);
 				
 		setControl(container);
 	}
 	
 	@Override
 	public boolean isPageComplete() 	{
 		return true;
 	}
 
 }
