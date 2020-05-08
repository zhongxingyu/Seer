 package org.zend.php.zendserver.deployment.ui.editors;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.ScrolledForm;
 import org.zend.php.zendserver.deployment.core.descriptor.IDescriptorContainer;
 
 public class DeploymentPropertiesPage extends DescriptorEditorPage {
 
 	protected IDescriptorContainer model;
 	private PropertiesTreeSection appdirSection;
 	private PropertiesTreeSection scriptsdirSection;
 
 	public DeploymentPropertiesPage(IDescriptorContainer model,
 			DeploymentDescriptorEditor editor, String id, String title) {
 		super(editor, id, title);
 		this.model = model;
 	}
 
 	@Override
 	public void refresh() {
 		appdirSection.refresh();
 		scriptsdirSection.refresh();
 	}
 
 	@Override
 	public void setActive(boolean active) {
 		if (active) {
 			DeploymentDescriptorEditor editor = ((DeploymentDescriptorEditor) getEditor());
 			IDocument document = editor.getDocumentProvider().getDocument(
 					editor.getPropertiesInput());
 			InputStream stream = new ByteArrayInputStream(document.get().getBytes());
 			try {
 				model.getMappingModel().load(stream);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		super.setActive(active);
 	}
 
 	@Override
 	protected void createFormContent(IManagedForm managedForm) {
 		super.createFormContent(managedForm);

		final ScrolledForm form = managedForm.getForm();
		form.getBody().setLayout(FormLayoutFactory.createFormGridLayout(true, 2));
 		createTreeSections(managedForm);
 	}
 
 	private void createTreeSections(IManagedForm managedForm) {
 		ScrolledForm form = managedForm.getForm();
 		FormToolkit toolkit = managedForm.getToolkit();
 		appdirSection = new AppTreeSection(getEditor(), form.getBody(),
 				toolkit, model);
 		scriptsdirSection = new ScriptsTreeSection(getEditor(), form.getBody(),
 				toolkit, model);
 	}
 }
