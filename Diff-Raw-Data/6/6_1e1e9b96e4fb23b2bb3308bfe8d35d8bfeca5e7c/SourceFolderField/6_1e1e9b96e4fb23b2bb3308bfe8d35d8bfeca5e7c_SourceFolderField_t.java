 package org.ow2.mindEd.ide.ui.wizards;
 
 import java.util.List;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.jface.dialogs.IDialogPage;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
 import org.eclipse.ui.dialogs.ISelectionStatusValidator;
 import org.ow2.mindEd.ide.core.MindIdeCore;
 import org.ow2.mindEd.ide.core.MindModel;
 import org.ow2.mindEd.ide.model.MindObject;
 import org.ow2.mindEd.ide.model.MindProject;
 import org.ow2.mindEd.ide.model.MindRootSrc;
 import org.ow2.mindEd.ide.model.MindideFactory;
 import org.ow2.mindEd.ide.ui.Activator;
 import org.ow2.mindEd.ide.ui.views.ListAndAdapterFactoryContentProvider;
 
 public class SourceFolderField {
 	private Text _sourceFolderText;
 	private MindRootSrc _srcFolder;
 	private MindModel _model;
 	private ComposedAdapterFactory _adapterFactory;
 	private AdapterFactoryLabelProvider _renderer;
 
 	public SourceFolderField(
 			MindModel model, ComposedAdapterFactory adapterFactory,
 			AdapterFactoryLabelProvider renderer) {
 		super();
 		_model = model;
 		_adapterFactory = adapterFactory;
 		_renderer = renderer;
 	}
 
 	/**
 	 * @see IDialogPage#createControl(Composite)
 	 */
 	public void createControl(Composite container, final PageUdapteStatus pus) {
 		Label label = new Label(container, SWT.NULL);
 		label.setText(Messages.ComponentNewWizardPage_src_field_label);
 
 		_sourceFolderText = new Text(container, SWT.BORDER | SWT.SINGLE);
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		_sourceFolderText.setLayoutData(gd);
 		_sourceFolderText.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				pus.dialogChanged();
 			}
 		});
 		Button button = new Button(container, SWT.PUSH);
 		button.setText(Messages.ComponentNewWizardPage_src_field_browser);
 		button.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
				MindRootSrc s = handleSelect(_model.getAllProject(), Messages.ComponentNewWizardPage_src_field_title, _sourceFolderText);
				if (s != null) {	
					_srcFolder = s;
					_sourceFolderText.setText(_srcFolder.getFullpath());
				}
 			}
 		});
 	}
 	
 	public void init(MindRootSrc rs) {
 		if (rs == null)
 			return;
 		_srcFolder = rs;
 		_sourceFolderText.setText(_srcFolder.getFullpath());
 	}
 
 	
 	/**
 	 * @return true if error
 	 */
 
 	public boolean dialogChanged(PageUdapteStatus pus) {
 		IResource container = ResourcesPlugin.getWorkspace().getRoot()
 				.findMember(new Path(getSourceFolderName()));
 		
 		if (getSourceFolderName().length() == 0) {
 			pus.updateErrorStatus(Messages.ComponentNewWizardPage_error_msg_source_folder_must_be_specified);
 			return true;
 		}
 		if (container == null
 				|| (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0) {
 			pus.updateErrorStatus(Messages.ComponentNewWizardPage_error_msg_source_folder_must_exist);
 			return true;
 		}
 		if (!container.isAccessible()) {
 			pus.updateErrorStatus(Messages.ComponentNewWizardPage_error_msg_source_folder_must_be_writable);
 			return true;
 		}
 		
 		MindObject mobj = MindIdeCore.get(container);
 		if (mobj != null) {
 			if (!(mobj instanceof MindRootSrc)) {
 				pus.updateErrorStatus(Messages.ComponentNewWizardPage_error_msg_source_folder_must_valid);
 				return true;
 			}
 			_srcFolder = (MindRootSrc) mobj;
 		} else {
 			_srcFolder = MindideFactory.eINSTANCE.createMindRootSrc();
 			_srcFolder.setFullpath(container.getFullPath().toPortableString());
 		}
 		
 		return false;
 	}
 
 	
 
 	public String getSourceFolderName() {
 		return _sourceFolderText.getText();
 	}
 	
 	protected MindRootSrc handleSelect(List<MindProject> input, String message, Text control) {
 		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(this._sourceFolderText.getShell(), _renderer, 
 				new ListAndAdapterFactoryContentProvider (_adapterFactory));
 		dialog.setInput(input.toArray());
 		dialog.setMessage(message);
 		dialog.setEmptyListMessage(message);
 		dialog.addFilter(new ViewerFilter() {
 			
 			@Override
 			public boolean select(Viewer viewer, Object parentElement, Object element) {
 				if (element instanceof MindProject || element instanceof MindRootSrc)
 					return true;
 				return false;
 			}
 		});
 		dialog.setValidator(new ISelectionStatusValidator() {
 			
 			public IStatus validate(Object[] selection) {
 				if (selection == null || selection.length != 1)
 					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, Messages.ComponentNewWizardPage_error_msg_choose_one_source_folder, null);
 				Object o = selection[0];
 				if (o instanceof MindRootSrc) {
 					return Status.OK_STATUS;
 				}
 				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, Messages.ComponentNewWizardPage_error_msg_choose_a_source_folder, null);
 			}
 		});
 		if (dialog.open() == Window.OK) {
 			MindRootSrc v = (MindRootSrc) dialog.getFirstResult();
 			control.setText(v.getFullpath());
 			return v;
 		}
 		return null;
 	}
 
 	public MindRootSrc getSourceFolder() {
 		return _srcFolder;
 	}
 }
