 /**
  * 
  */
 package org.tomale.id.documents.management.preferences;
 
 import java.util.ArrayList;
 
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.PreferencePage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 import org.tomale.id.documents.management.Activator;
 import org.tomale.id.documents.management.DocumentStoreConfigurationElement;
 
 /**
  * @author ferd
  *
  */
 public class DocumentManagementPreferencePage extends PreferencePage implements
 		IWorkbenchPreferencePage {
 
 	Combo _stores;
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
 	 */
 	@Override
 	protected Control createContents(Composite parent) {
 		Composite comp = new Composite(parent,SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 2;
 		comp.setLayout(layout);
 		
 		Label l;
 		
 		l = new Label(comp, SWT.NO_FOCUS);
 		l.setText("Document Store Provider");
 		l.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
 		
 		_stores = new Combo(comp,SWT.DROP_DOWN | SWT.READ_ONLY);
 		_stores.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
 		initDocumentStoreTypes();
 		
 		return comp;
 	}
 	
 	private void initDocumentStoreTypes(){
 		ArrayList<DocumentStoreConfigurationElement> elements = Activator.getDocumentStoreTypes();
 		for(DocumentStoreConfigurationElement element : elements){
 			_stores.add(element.getName());
 			_stores.setData(element.getName(), element);
 		}
 		
 		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
 		String selectedStore = store.getString(PreferenceConstants.DOC_STORE_PROVIDER);
 		
 		if(!selectedStore.isEmpty()){
			_stores.setText(selectedStore);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
 	 */
 	@Override
 	public void init(IWorkbench workbench) {
 
 	}
 
 	@Override
 	public boolean performOk() {
 
 		savePreferences();
 		
 		return super.performOk();
 	}
 
 	private void savePreferences(){
 		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
 		if(!_stores.getText().isEmpty()){
 			String id = ((DocumentStoreConfigurationElement) _stores.getData(_stores.getText())).getId();
 			store.setValue(PreferenceConstants.DOC_STORE_PROVIDER, id);
 		}
 	}
 
 	
 }
