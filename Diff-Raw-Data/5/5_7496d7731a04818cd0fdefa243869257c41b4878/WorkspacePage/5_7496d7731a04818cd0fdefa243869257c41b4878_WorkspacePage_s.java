 package bndtools.editor.pages;
 
 import org.bndtools.core.ui.ExtendedFormEditor;
 import org.bndtools.core.ui.IFormPageFactory;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.ui.forms.editor.FormEditor;
 import org.eclipse.ui.forms.editor.FormPage;
 import org.eclipse.ui.forms.editor.IFormPage;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.ScrolledForm;
 import org.eclipse.ui.forms.widgets.Section;
 
 import bndtools.api.IBndModel;
 import bndtools.editor.workspace.PluginPathPart;
 import bndtools.editor.workspace.PluginsPart;
 import bndtools.editor.workspace.WorkspaceMainPart;
 import bndtools.model.clauses.HeaderClause;
 import bndtools.utils.MessageHyperlinkAdapter;
 
 public class WorkspacePage extends FormPage {
 
     private final IBndModel model;
     private PluginsPart pluginsPart;
 
    public static IFormPageFactory MAIN_FACTORY = new IFormPageFactory() {
         public IFormPage createPage(ExtendedFormEditor editor, IBndModel model, String id) throws IllegalArgumentException {
             return new WorkspacePage(true, editor, model, id, "Workspace");
         }
 
         public boolean supportsMode(Mode mode) {
             return mode == Mode.workspace;
         }
     };
 
    public static IFormPageFactory EXT_FACTORY = new IFormPageFactory() {
         public IFormPage createPage(ExtendedFormEditor editor, IBndModel model, String id) throws IllegalArgumentException {
             return new WorkspacePage(false, editor, model, id, "Workspace");
         }
 
         public boolean supportsMode(Mode mode) {
             return mode == Mode.workspace;
         }
     };
 
     private final boolean mainBuildFile;
 
     private WorkspacePage(boolean mainBuildFile, FormEditor editor, IBndModel model, String id, String title) {
         super(editor, id, title);
         this.mainBuildFile = mainBuildFile;
         this.model = model;
     }
 
     @Override
     protected void createFormContent(IManagedForm managedForm) {
         managedForm.setInput(model);
 
         FormToolkit tk = managedForm.getToolkit();
         ScrolledForm form = managedForm.getForm();
         form.setText("Workspace Config");
         tk.decorateFormHeading(form.getForm());
         form.getForm().addMessageHyperlinkListener(new MessageHyperlinkAdapter(getEditor()));
 
         // Create controls
         Composite body = form.getBody();
 
         WorkspaceMainPart linksPart = new WorkspaceMainPart(mainBuildFile, body, tk, Section.TITLE_BAR | Section.EXPANDED | Section.DESCRIPTION);
         managedForm.addPart(linksPart);
         
         PluginPathPart pluginPathPart = new PluginPathPart(body, tk, Section.TITLE_BAR | Section.EXPANDED | Section.DESCRIPTION | Section.TWISTIE);
         managedForm.addPart(pluginPathPart);
 
         pluginsPart = new PluginsPart(body, tk, Section.TITLE_BAR | Section.EXPANDED | Section.DESCRIPTION | Section.TWISTIE);
         managedForm.addPart(pluginsPart);
 
         // Layout
         GridLayout layout = new GridLayout(1, false);
         body.setLayout(layout);
         
         linksPart.getSection().setLayoutData(PageLayoutUtils.createCollapsed());
         
         pluginPathPart.getSection().setLayoutData(PageLayoutUtils.createExpanded());
         pluginPathPart.getSection().addExpansionListener(new ResizeExpansionAdapter(pluginPathPart.getSection()));
 
         pluginsPart.getSection().setLayoutData(PageLayoutUtils.createExpanded());
         pluginsPart.getSection().addExpansionListener(new ResizeExpansionAdapter(pluginsPart.getSection()));
 
     }
 
     public void setSelectedPlugin(HeaderClause header) {
         pluginsPart.getSelectionProvider().setSelection(new StructuredSelection(header));
     }
 }
