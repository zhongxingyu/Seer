 package wwe.preferences;
 
 import org.eclipse.jface.preference.DirectoryFieldEditor;
 import org.eclipse.jface.preference.FieldEditorPreferencePage;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 
 import wwe.Activator;
 
 public class ToolbarPreferencePage extends FieldEditorPreferencePage implements
 		IWorkbenchPreferencePage {
 
	public static final String ID = Messages.ToolbarPreferencePage_0;
 
 	public ToolbarPreferencePage(String title) {
 		super(title, GRID);
 		setTitle(Messages.ToolbarPreferencePage_1);
 
 		setPreferenceStore(Activator.getDefault().getPreferenceStore());
 		setDescription(Messages.ToolbarPreferencePage_2);
 	}
 
 	@Override
 	protected void createFieldEditors() {
 		addField(new DirectoryFieldEditor("PATH", Messages.ToolbarPreferencePage_4, //$NON-NLS-1$
 				getFieldEditorParent()));
 	}
 
 	@Override
 	public void init(IWorkbench workbench) {
 		// TODO Auto-generated method stub
 		
 	}
 }
