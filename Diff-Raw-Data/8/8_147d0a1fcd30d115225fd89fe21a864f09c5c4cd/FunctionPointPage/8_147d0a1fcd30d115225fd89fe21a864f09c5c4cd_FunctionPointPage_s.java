 package estimation.sizeEstimation;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 
 public class FunctionPointPage extends BaseWizardPage {
 	public FunctionPointPage(){
 		super("功能点估算");
 	}
 
 	@Override
 	protected int getSize() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	protected boolean isEndPage() {
 		return true;
 	}
 
 	@Override
 	public void createControl(Composite parent) {
 		parent.setLayout(new GridLayout(4, false));
 		Label label = new Label(parent, SWT.NONE);
 		
 	}
 
 }
