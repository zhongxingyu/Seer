 package rhogenwizard.preferences;
 
 
 import org.eclipse.jface.preference.StringFieldEditor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.IWorkbench;
 
 import rhogenwizard.Activator;
 import rhogenwizard.rhohub.IRhoHubSetting;
 import rhogenwizard.sdk.task.rhohub.TokenTask;
 
 public class PreferencesPageRhoHub extends BasePreferencePage 
 {
    PreferenceInitializer m_pInit = null;
     
     public PreferencesPageRhoHub() 
     {
         super(GRID);
         setPreferenceStore(Activator.getDefault().getPreferenceStore());
         setDescription("");
     }
     
     @Override
     public boolean performOk()
     {
         boolean ret = super.performOk();
 
         try 
         {
             m_pInit.savePreferences();
         } 
         catch (Exception e) 
         {
             e.printStackTrace();
         }
 
         return ret;
     }
 
     public void createFieldEditors() 
     {
         checkRhodesSdk();
        
        addField(new StringFieldEditor(IRhoHubSetting.rhoHubUrl, 
                "&RhoHub API Endpoint (advanced):", getFieldEditorParent()));
                
        addField(new StringFieldEditor(IRhoHubSetting.rhoHubToken, 
                "&API Token:", getFieldEditorParent()));
        
        addField(new StringFieldEditor(IRhoHubSetting.rhoHubProxy, 
                "&HTTP proxy:", getFieldEditorParent()));
     }

     @Override
 	protected Control createContents(Composite parent) 
     {
     	StringFieldEditor editor = null;
    		
     	Composite top = new Composite(parent, SWT.LEFT);
     	
     	editor = new StringFieldEditor(IRhoHubSetting.rhoHubUrl, "&RhoHub API Endpoint (advanced):", top);
    	editor.setPreferenceStore(getPreferenceStore());
     	editor.setPreferencePage(this);
     	editor.load();
     	
     	editor = new StringFieldEditor(IRhoHubSetting.rhoHubToken, "&API Token:", top);
    	editor.setPreferenceStore(getPreferenceStore());
     	editor.setPreferencePage(this);
     	editor.load();
     	
     	editor = new StringFieldEditor(IRhoHubSetting.rhoHubProxy, "&HTTP proxy:", top);
    	editor.setPreferenceStore(getPreferenceStore());
     	editor.setPreferencePage(this);
     	editor.load();
     	    	
     	top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
     	
         GridData checkBoxAligment = new GridData();
         checkBoxAligment.horizontalAlignment = GridData.FILL;
         checkBoxAligment.horizontalSpan = 3;
         
 		Button clearButton = new Button(top, SWT.NONE);
 		clearButton.setText("Clear token");
 		clearButton.setLayoutData(checkBoxAligment);
     	clearButton.addSelectionListener(new SelectionListener() 
     	{
 			@Override
 			public void widgetSelected(SelectionEvent e) 
 			{
 				TokenTask.clearToken(m_pInit.getRhodesPath());
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 				// TODO Auto-generated method stub				
 			}			
 		});
     	
 		return top;
 	}
 
 	public void init(IWorkbench workbench) 
     {
         m_pInit = PreferenceInitializer.getInstance();
     }   
 }
