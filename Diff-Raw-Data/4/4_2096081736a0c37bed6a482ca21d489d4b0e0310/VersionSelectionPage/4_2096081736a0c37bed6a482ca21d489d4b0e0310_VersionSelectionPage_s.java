 package com.rytong.ui.internal.wizard;
 
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.erlide.core.ErlangPlugin;
 
 import com.rytong.ui.internal.RytongUIMessages;
 
 public class VersionSelectionPage extends WizardPage {
 	private AppFieldData fData;
 	private Text path;
 
 	protected VersionSelectionPage(String pageName, AppFieldData fData) {
 		super(pageName);
 		this.fData = fData;
 	}
 
 	@Override
 	public void createControl(Composite parent) {
 		Composite container = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 2;
 		layout.marginTop = 60;
 		container.setLayout(layout);
 		container.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		Label label = new Label(container, SWT.NONE);
 		label.setText(RytongUIMessages.VersionSelectionPage_label);
 		GridData gd_lbl = new GridData();
 		label.setLayoutData(gd_lbl);
 
 		final Combo versCtrl = new Combo(container, SWT.READ_ONLY);
 		final String[] versions = RytongUIMessages.VersionSelectionPage_versions
 				.split("\\s+");
 		fData.ewpVersion = versions[0];
 		versCtrl.setItems(versions);
 		versCtrl.select(0);
 		versCtrl.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				super.widgetSelected(e);
 				update_data(versions[versCtrl.getSelectionIndex()]);
 			}
 		});
 		GridData gd_vers = new GridData();
 		versCtrl.setLayoutData(gd_vers);
 
 		Label plabel = new Label(container, SWT.NONE);
 		plabel.setText(RytongUIMessages.VersionSelectionPage_plabel);
 		GridData gd_plbl = new GridData();
 		label.setLayoutData(gd_plbl);
 
 		path = new Text(container, SWT.NONE);
		path.setText(ErlangPlugin.ewpPath == null ? "" : ErlangPlugin.ewpPath);
 		path.addModifyListener(new ModifyListener() {
 			@Override
 			public void modifyText(ModifyEvent e) {
 				update_path(path.getText());
 			}
 		});
 		GridData gd_path = new GridData();
 		path.setLayoutData(gd_path);
 
 		setControl(container);
 		setPageComplete(validatePage());
 
 	}
 
 	public void update_data(String ver) {
 		fData.ewpVersion = ver;
 		setPageComplete(validatePage());
 
 		IWizardPage page = this.getNextPage();
 		if (page.getControl() != null)
 			page.dispose();
 	}
 
 	public void update_path(String path) {
 		if (path.endsWith("/")) {
 			fData.ewpPath = path.substring(0, path.length() - 1);
 		} else {
 			fData.ewpPath = path;
 		}
 	}
 	
 	private boolean validatePage() {
 		return true;
 	}
 }
