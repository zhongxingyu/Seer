 package org.jcryptool.visual.sig.ui.view;
 
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.jcryptool.visual.sig.Messages;
 
 public class SigComposite extends Composite implements PaintListener{
 	private Text txtHash;
 	private Text txtGeneralDescription;
 	private Text txtSignature;
 
 	public SigComposite(Composite parent, int style, SigView view) {
 		super(parent, style);
 		
 		txtHash = new Text(this, SWT.BORDER);
 		txtHash.setEditable(false);
 		txtHash.setText("Hash");
 		txtHash.setBounds(28, 371, 136, 19);
 		
 		Button btnHash = new Button(this, SWT.NONE);
 		btnHash.setBounds(28, 293, 136, 62);
 		btnHash.setText("Hashfunktion");
 		
 		Button btnSignature = new Button(this, SWT.NONE);
 		btnSignature.setBounds(299, 413, 122, 56);
 		btnSignature.setText("Signaturfunktion");
 		
 		txtGeneralDescription = new Text(this, SWT.BORDER);
 		txtGeneralDescription.setEditable(false);
 		txtGeneralDescription.setBounds(10, 10, 666, 45);
 		txtGeneralDescription.setText(Messages.getString("SigComposite.description"));
 		//txtGeneralDescription.setText("SigComposite.description");
 		
 		Group grpDokumentAnEmpfnger = new Group(this, SWT.NONE);
 		grpDokumentAnEmpfnger.setText("Dokument an Empf\u00E4nger");
 		grpDokumentAnEmpfnger.setBounds(441, 288, 212, 203);
 		
 		txtSignature = new Text(grpDokumentAnEmpfnger, SWT.BORDER);
		txtSignature.setBounds(10, 110, 178, 56);
 		
 		Button btnReset = new Button(this, SWT.NONE);
 		btnReset.setBounds(582, 497, 94, 28);
 		btnReset.setText("Reset");
 		
 		TabFolder tabFolder = new TabFolder(this, SWT.NONE);
 		tabFolder.setBounds(208, 103, 445, 148);
 		
 		TabItem tbtmNewItem = new TabItem(tabFolder, SWT.NONE);
 		tbtmNewItem.setText("1");
 		
 		TabItem tbtmNewItem_1 = new TabItem(tabFolder, SWT.NONE);
 		tbtmNewItem_1.setText("2");
 		
 		TabItem tbtmNewItem_2 = new TabItem(tabFolder, SWT.NONE);
 		tbtmNewItem_2.setText("3");
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public void paintControl(PaintEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 }
