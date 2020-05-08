 package org.jboss.tools.ws.creation.ui.widgets;
 
 
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.wst.command.internal.env.ui.widgets.SimpleWidgetDataContributor;
 import org.eclipse.wst.command.internal.env.ui.widgets.WidgetDataEvents;
 import org.jboss.tools.ws.creation.core.data.ServiceModel;
 import org.jboss.tools.ws.creation.core.messages.JBossWSCreationCoreMessages;
 
 public class CodeGenConfigWidget extends SimpleWidgetDataContributor {
 	
 	private ServiceModel model;
 	private Button btnRemove;
 	private Button btnUpdateWebxml;
 	private Button btnGenDefaultImpl;
 
 	public CodeGenConfigWidget(ServiceModel model){
 		this.model = model;
 	}
 	
 	public WidgetDataEvents addControls( Composite parent, Listener statusListener){
 		
 		Composite configCom = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout(3, false);		
 		configCom.setLayout(layout);
 		configCom.setLayoutData(new GridData(GridData.FILL_BOTH));
 		
 		//custom package name
 		Label lblCustomPakage = new Label(configCom, SWT.NONE);
 		lblCustomPakage.setText(JBossWSCreationCoreMessages.Label_Custom_Package_Name); //$NON-NLS-1$
 		final Text txtCustomPkgName = new Text(configCom, SWT.BORDER);
 		txtCustomPkgName.setText(model.getCustomPackage());
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalSpan = 2;
 		txtCustomPkgName.setLayoutData(gd);
 		txtCustomPkgName.addModifyListener(new ModifyListener(){
 
 			public void modifyText(ModifyEvent e) {
 				model.setCustomPackage(txtCustomPkgName.getText());
 			}});
 		
 		//target
 		new Label(configCom, SWT.NONE).setText(JBossWSCreationCoreMessages.Label_JaxWS_Target); //$NON-NLS-1$
 		final Combo cbSpec = new Combo(configCom, SWT.BORDER | SWT.READ_ONLY);
 		cbSpec.add(JBossWSCreationCoreMessages.Value_Target_0, 0); //$NON-NLS-1$
 		cbSpec.add(JBossWSCreationCoreMessages.Value_Target_1, 1);		 //$NON-NLS-1$
 		if(JBossWSCreationCoreMessages.Value_Target_0.equals(model.getTarget())){
 			cbSpec.select(0);
 		}
 		else{
 			cbSpec.select(1);
 		}
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalSpan = 2;
 		cbSpec.setLayoutData(gd);
 		cbSpec.addModifyListener(new ModifyListener(){
 
 			public void modifyText(ModifyEvent e) {
 				model.setTarget(cbSpec.getText());
 			}});
 		
 		//catalog file
 		new Label(configCom, SWT.NONE).setText(JBossWSCreationCoreMessages.Label_Catalog_File); //$NON-NLS-1$
 		final Text txtCatlog = new Text(configCom, SWT.BORDER);
 		txtCatlog.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		Button btnCatlog = new Button(configCom, SWT.NONE);
 		btnCatlog.setText(JBossWSCreationCoreMessages.Label_Button_Text_Seletion); //$NON-NLS-1$
 		btnCatlog.addSelectionListener(new SelectionAdapter(){
 			public void widgetSelected(SelectionEvent e) {
 				String fileLocation = new FileDialog(Display.getCurrent().getActiveShell(), SWT.NONE).open();
 				txtCatlog.setText(fileLocation);
 				model.setCatalog(fileLocation);
 			}
 		});
 		
 		//binding files
 		new Label(configCom, SWT.NONE).setText(JBossWSCreationCoreMessages.Label_Binding_File); //$NON-NLS-1$
 		
 		final List bindingList = new List(configCom, SWT.BORDER | SWT.SCROLL_LINE | SWT.V_SCROLL | SWT.H_SCROLL);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.heightHint = Display.getCurrent().getActiveShell().getBounds().height / 4;
 		gd.verticalSpan = 3;
 		bindingList.setLayoutData(gd);
 		loadBindingFiles(bindingList);
 		bindingList.addSelectionListener(new SelectionAdapter(){
 			public void widgetSelected(SelectionEvent e) {
 				if(bindingList.getSelectionIndex() >= 0){
 					btnRemove.setEnabled(true);
 				}else{
 					btnRemove.setEnabled(false);
 				}
 			}
 		});
 		
 
 		Button btnSelect = new Button(configCom, SWT.NONE);
 		btnSelect.setText(JBossWSCreationCoreMessages.Label_Button_Text_Seletion); //$NON-NLS-1$
 		btnSelect.addSelectionListener(new SelectionAdapter(){
 			public void widgetSelected(SelectionEvent e) {
 				
 				String fileLocation = new FileDialog(Display.getCurrent().getActiveShell(), SWT.NONE).open();
 				if(fileLocation != null && 
 						!model.getBindingFiles().contains(fileLocation)){
 					bindingList.add(fileLocation);
 					model.addBindingFile(fileLocation);
 				}
 				
 			}
 		});
 		
 		new Label(configCom, SWT.NONE);
 		btnRemove = new Button(configCom, SWT.NONE);
 		btnRemove.setEnabled(false);
 		btnRemove.setText(JBossWSCreationCoreMessages.Label_Button_Text_Remove);
 		btnRemove.addSelectionListener(new SelectionAdapter(){
 			public void widgetSelected(SelectionEvent e) {
 				model.getBindingFiles().remove(bindingList.getSelectionIndex());
 				bindingList.remove(bindingList.getSelectionIndex());			
 				if(bindingList.getSelectionIndex() == -1){
 					btnRemove.setEnabled(false);
 				}
 			}
 		});
 		
 		btnGenDefaultImpl = new Button(configCom, SWT.CHECK);
 		gd = new GridData();
 		gd.horizontalSpan = 3;
 		btnGenDefaultImpl.setLayoutData(gd);
 		btnGenDefaultImpl.setText(JBossWSCreationCoreMessages.Label_Generate_Impelemtation);
 		btnGenDefaultImpl.setSelection(true);
 		btnGenDefaultImpl.addSelectionListener(new SelectionAdapter(){
 			public void widgetSelected(SelectionEvent e) {
 				model.setGenerateImplementatoin(btnGenDefaultImpl.getSelection());
 				btnUpdateWebxml.setEnabled(btnGenDefaultImpl.getSelection());
 				if(!btnGenDefaultImpl.getSelection()){
 					model.setUpdateWebxml(false);
 				}else{
 					model.setUpdateWebxml(btnUpdateWebxml.getSelection());
 				}
 			}
 		});
 		
 		btnUpdateWebxml = new Button(configCom, SWT.CHECK);
 		gd = new GridData();
 		gd.horizontalSpan = 3;
 		btnUpdateWebxml.setLayoutData(gd);
		btnUpdateWebxml.setText(JBossWSCreationCoreMessages.Label_Upate_Webxml);
 		btnUpdateWebxml.setSelection(true);
 		btnUpdateWebxml.addSelectionListener(new SelectionAdapter(){
 			public void widgetSelected(SelectionEvent e) {
 				model.setUpdateWebxml(btnUpdateWebxml.getSelection());
 			}
 		});
 		
 		return this;
 	}
 	
 	private void loadBindingFiles(List bindingList){
 		for(String fileLocation: model.getBindingFiles()){
 			bindingList.add(fileLocation);
 		}
 	}
 }
