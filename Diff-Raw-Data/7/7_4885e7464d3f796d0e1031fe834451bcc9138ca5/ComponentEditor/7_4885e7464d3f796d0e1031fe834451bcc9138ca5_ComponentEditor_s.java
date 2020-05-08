 /*
  * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     bstefanescu
  */
 package org.nuxeo.ide.sdk.server.ui;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jdt.ui.ISharedImages;
 import org.eclipse.jdt.ui.JavaUI;
 import org.eclipse.jface.action.Action;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.program.Program;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Link;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.forms.IFormColors;
 import org.eclipse.ui.forms.events.ExpansionAdapter;
 import org.eclipse.ui.forms.events.ExpansionEvent;
 import org.eclipse.ui.forms.events.HyperlinkAdapter;
 import org.eclipse.ui.forms.events.HyperlinkEvent;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.Hyperlink;
 import org.eclipse.ui.forms.widgets.ImageHyperlink;
 import org.eclipse.ui.forms.widgets.ScrolledForm;
 import org.eclipse.ui.forms.widgets.Section;
 import org.eclipse.ui.part.EditorPart;
 import org.nuxeo.ide.common.UI;
 import org.nuxeo.ide.sdk.NuxeoSDK;
 import org.nuxeo.ide.sdk.comp.ComponentModel;
 import org.nuxeo.ide.sdk.comp.ComponentRef;
 import org.nuxeo.ide.sdk.comp.ExtensionModel;
 import org.nuxeo.ide.sdk.comp.ExtensionPointModel;
 import org.nuxeo.ide.sdk.comp.ServiceModel;
 
 /**
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  * 
  */
 public class ComponentEditor extends EditorPart {
 
     protected ScrolledForm form;
 
     protected FormToolkit toolkit;
 
     protected ComponentModel component;
 
     @Override
     public void doSave(IProgressMonitor monitor) {
     }
 
     @Override
     public void doSaveAs() {
     }
 
     @Override
     public void init(IEditorSite site, IEditorInput input)
             throws PartInitException {
         setSite(site);
         setInput(input);
         ComponentRef ref = (ComponentRef) input.getAdapter(ComponentRef.class);
         NuxeoSDK sdk = NuxeoSDK.getDefault();
         if (sdk == null) {
             throw new PartInitException("Cannot open component "
                     + ref.getName() + ". No NuxeoSDK was defined");
         }
         try {
             component = sdk.getComponentIndexManager().getComponent(ref);
         } catch (Exception e) {
             throw new PartInitException("Failed to find component file: "
                     + ref.getName() + ". Source: " + ref.getSrc(), e);
         }
         if (component == null) {
             throw new PartInitException("Unable to find component file: "
                     + ref.getName() + ". Source: " + ref.getSrc());
         }
     }
 
     @Override
     public boolean isDirty() {
         return false;
     }
 
     @Override
     public boolean isSaveAsAllowed() {
         return false;
     }
 
     protected void createToolbar() {
         Action lookup = new Action() {
             @Override
             public void run() {
                 try {
                     openSourceFile();
                 } catch (Throwable t) {
                     UI.showError("Failed to open the source file", t);
                 }
             }
         };
         lookup.setId(ComponentEditor.class.getName() + "#lookup");
         lookup.setText("Open");
         lookup.setToolTipText("Open component XML definition");
         lookup.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
                 org.eclipse.ui.ISharedImages.IMG_OBJ_FILE));
         form.getToolBarManager().add(lookup);
         form.getToolBarManager().update(true);
         // form.getToolBarManager().add(createExtension);
     }
 
     @Override
     public void createPartControl(Composite parent) {
         // ColumnLayout layout = new ColumnLayout();
         GridLayout layout = new GridLayout();
         layout.verticalSpacing = 10;
 
         toolkit = new FormToolkit(getSite().getShell().getDisplay());
         form = toolkit.createScrolledForm(parent);
         form.setText("Nuxeo Component: " + component.getLabel());
         form.setImage(component.getImage());
         toolkit.decorateFormHeading(form.getForm());
         form.getBody().setLayout(layout);
         createToolbar();
 
         Composite panel = form.getBody();
 
         createHeaderPanel(panel);
 
         Section section;
 
         if (component.getImpl() != null && component.getImpl().length() > 0) {
             section = toolkit.createSection(panel, Section.EXPANDED
                     | Section.TITLE_BAR | Section.DESCRIPTION);
             section.setText("Implementation");
             section.setDescription("Click on the implementation class of this component to open the class in an editor");
             ImageHyperlink link = toolkit.createImageHyperlink(section,
                     SWT.NONE);
             link.setImage(JavaUI.getSharedImages().getImage(
                     ISharedImages.IMG_OBJS_CFILE));
             link.setText(component.getImpl());
             link.addHyperlinkListener(new HyperlinkHandler(component.getImpl()));
             section.setClient(link);
             applyLayout(section);
         }
 
         section = toolkit.createSection(panel, Section.EXPANDED
                 | Section.TITLE_BAR);
         section.setText("Documentation");
         Text label = toolkit.createText(section,
                 DocumentationFormat.format(component.getDocumentation()),
                SWT.READ_ONLY);
 
         section.setClient(label);
         applyLayout(section);
 
         section = toolkit.createSection(panel, Section.EXPANDED
                 | Section.TITLE_BAR | Section.DESCRIPTION);
         section.setText("Provided Services");
         section.setDescription("The list of service classes provided by this component. You can click a service class to open it in the editor.");
         section.setClient(createServiceList(section));
         applyLayout(section);
 
         section = toolkit.createSection(panel, Section.EXPANDED
                 | Section.TITLE_BAR | Section.DESCRIPTION);
         section.setText("Extension Points");
         section.setDescription("The list of extension points exposed by this component. Select an etension point to view its documentation (if any was provided).\nNote that <code>...</code> sections in the documentation contains examples of usage (the <code> tag itself is only a marker).");
         section.setClient(createXPointsTable(section));
         applyLayout(section);
 
         section = toolkit.createSection(panel, Section.EXPANDED
                 | Section.TITLE_BAR | Section.DESCRIPTION);
         section.setText("Extensions");
         section.setDescription("The list of extensions contributed by this component.");
         section.setClient(createExtensionSection(section));
         applyLayout(section);
 
         form.reflow(true);
     }
 
     protected void applyLayout(Control ctrl) {
         GridData gd = new GridData();
         gd.horizontalAlignment = SWT.FILL;
         gd.grabExcessHorizontalSpace = true;
         ctrl.setLayoutData(gd);
     }
 
     protected void createHeaderPanel(Composite parent) {
         Composite panel = toolkit.createComposite(parent);
         applyLayout(panel);
         GridData gd = new GridData();
         panel.setLayout(new GridLayout(2, false));
         Label label = toolkit.createLabel(panel, "Name:", SWT.BOLD);
         label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
         Text text = new Text(panel, SWT.READ_ONLY | SWT.SINGLE);
         text.setText(component.getName());
         // Control value = toolkit.createLabel(panel, component.getName(),
         // SWT.BOLD);
         gd = new GridData();
         gd.grabExcessHorizontalSpace = true;
         gd.horizontalAlignment = SWT.FILL;
         text.setLayoutData(gd);
         label = toolkit.createLabel(panel, "Version:", SWT.BOLD);
         label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
         text = new Text(panel, SWT.READ_ONLY | SWT.SINGLE);
         text.setText(component.getVersion());
         gd = new GridData();
         gd.grabExcessHorizontalSpace = true;
         gd.horizontalAlignment = SWT.FILL;
         text.setLayoutData(gd);
         label = toolkit.createLabel(panel, "Bundle:");
         label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
         text = new Text(panel, SWT.READ_ONLY | SWT.SINGLE);
         text.setText(component.getBundle());
         gd = new GridData();
         gd.grabExcessHorizontalSpace = true;
         gd.horizontalAlignment = SWT.FILL;
         text.setLayoutData(gd);
         if (component.getSrc() != null) {
             label = toolkit.createLabel(panel, "Source:");
             label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
             Hyperlink src = toolkit.createHyperlink(panel, component.getSrc(),
                     SWT.NONE);
             src.addHyperlinkListener(new HyperlinkAdapter() {
                 @Override
                 public void linkActivated(HyperlinkEvent e) {
                     try {
                         openSourceFile();
                     } catch (Throwable t) {
                         UI.showError("Failed to open the source file", t);
                     }
                 }
             });
             gd = new GridData();
             gd.grabExcessHorizontalSpace = true;
             gd.horizontalAlignment = SWT.FILL;
             text.setLayoutData(gd);
         }
     }
 
     protected Control createServiceList(Composite parent) {
         Composite panel = toolkit.createComposite(parent);
         panel.setLayout(new RowLayout(SWT.VERTICAL));
         if (component.getServices() == null
                 || component.getServices().length == 0) {
             toolkit.createLabel(panel, "No services are provided").setForeground(
                     Display.getCurrent().getSystemColor(SWT.COLOR_RED));
             return panel;
         }
         for (final ServiceModel model : component.getServices()) {
             ImageHyperlink link = toolkit.createImageHyperlink(panel, SWT.NONE);
             link.setImage(model.getImage());
             link.setText(model.getName());
             link.addHyperlinkListener(new HyperlinkHandler(model.getName()));
         }
         return panel;
     }
 
     protected Control createXPointsTable(Composite parent) {
         Composite panel = toolkit.createComposite(parent);
         panel.setLayout(new GridLayout());
         applyLayout(panel);
         if (component.getExtensionPoints() == null
                 || component.getExtensionPoints().length == 0) {
             toolkit.createLabel(panel, "No extension points are provided").setForeground(
                     Display.getCurrent().getSystemColor(SWT.COLOR_RED));
             return panel;
         }
         for (ExtensionPointModel model : component.getExtensionPoints()) {
             createXPointEntry(panel, model);
         }
         return panel;
     }
 
     protected void createXPointEntry(Composite parent, ExtensionPointModel model) {
         ExpandableComposite section = toolkit.createExpandableComposite(parent,
                 ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT);
         section.setText(model.getName());
         Composite panel = toolkit.createComposite(section);
         panel.setLayout(new GridLayout(2, false));
         Label label = toolkit.createLabel(panel, "Extension Point Name:");
         label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
         Text text = new Text(panel, SWT.READ_ONLY | SWT.SINGLE);
         text.setText(model.getName());
         applyLayout(text);
         label = toolkit.createLabel(panel, "Contribution Types:");
         label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
         Composite types = toolkit.createComposite(panel);
         types.setLayout(new RowLayout(SWT.VERTICAL));
         for (final String ctype : model.getContributionTypes()) {
             Hyperlink hyperlink = toolkit.createHyperlink(types, ctype,
                     SWT.NONE);
             hyperlink.addHyperlinkListener(new HyperlinkAdapter() {
                 @Override
                 public void linkActivated(HyperlinkEvent e) {
                     new OpenClassAction(ctype).run();
                 }
             });
         }
         applyLayout(types);
         Text docu = toolkit.createText(panel,
                DocumentationFormat.format(model.getDocumentation()), SWT.NONE
                        | SWT.READ_ONLY);
         GridData gd = new GridData();
         gd.horizontalAlignment = SWT.FILL;
         gd.grabExcessHorizontalSpace = true;
         gd.horizontalSpan = 2;
         docu.setLayoutData(gd);
         section.setClient(panel);
         applyLayout(section);
         section.addExpansionListener(new ExpansionAdapter() {
             public void expansionStateChanged(ExpansionEvent e) {
                 form.reflow(true);
             }
         });
     }
 
     protected Composite createExtensionSection(Composite parent) {
         Composite panel = toolkit.createComposite(parent);
         panel.setLayout(new GridLayout());
         applyLayout(panel);
         ExtensionModel[] xts = component.getExtensions();
         if (xts == null || xts.length == 0) {
             toolkit.createLabel(panel, "No contributed extensions").setForeground(
                     Display.getCurrent().getSystemColor(SWT.COLOR_RED));
             return panel;
         }
         for (final ExtensionModel model : xts) {
             Link link = new Link(panel, SWT.WRAP);
             applyLayout(link);
             link.setText("Found contributions to '"
                     + model.getTarget().getName()
                     + "' extension point in component <a>"
                     + model.getTarget().getComponent() + "</a>");
             link.addListener(SWT.Selection, new Listener() {
                 @Override
                 public void handleEvent(Event e) {
                     ComponentRef ref = NuxeoSDK.getDefault().getComponentIndex().getComponent(
                             e.text);
                     if (ref == null) {
                         UI.showError("No such component were found: "
                                 + model.getTarget().getComponent());
                         return;
                     }
                     try {
                         PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
                                 ref, ComponentEditor.class.getName());
                     } catch (PartInitException ee) {
                         // do nothing
                     }
                 }
             });
         }
         return panel;
     }
 
     @Override
     public void setFocus() {
         if (form != null) {
             form.setFocus();
         }
     }
 
     @Override
     public void dispose() {
         super.dispose();
         if (toolkit != null) {
             toolkit.dispose();
             toolkit = null;
         }
         component = null;
     }
 
     protected void openSourceFile() throws IOException {
         NuxeoSDK sdk = NuxeoSDK.getDefault();
         if (sdk == null) {
             UI.showError("No Nuxeo SDK is configured");
             return;
         }
         File file = sdk.getComponentIndexManager().getComponentFile(component);
         if (file == null) {
             UI.showError("Cannot find component file: " + component.getSrc());
         } else {
             Program.launch(file.getAbsolutePath());
         }
     }
 
     public static class HyperlinkHandler extends HyperlinkAdapter {
         public String typeName;
 
         public HyperlinkHandler(String typeName) {
             this.typeName = typeName;
         }
 
         @Override
         public void linkActivated(HyperlinkEvent e) {
             new OpenClassAction(typeName).run();
         }
     }
 
 }
