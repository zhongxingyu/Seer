 package edu.ualberta.med.biobank.barcodegenerator.views;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.layout.RowLayout;
 
 import edu.ualberta.med.biobank.barcodegenerator.dialogs.StringInputDialog;
 import edu.ualberta.med.biobank.barcodegenerator.template.Template;
 import edu.ualberta.med.biobank.barcodegenerator.template.TemplateStore;
 import edu.ualberta.med.biobank.barcodegenerator.template.presets.cbsr.CBSRTemplate;
 import edu.ualberta.med.biobank.barcodegenerator.trees.ConfigurationTree;
 import edu.ualberta.med.biobank.barcodegenerator.trees.TreeException;
 
 public class TemplateEditorView extends ViewPart {
 
     public static final String ID = "edu.ualberta.med.biobank.barcodegenerator.views.TemplateEditorView";
     private Composite top = null;
     private Group group = null;
     private Composite composite = null;
     private Composite composite1 = null;
     private Composite composite2 = null;
     private Composite composite3 = null;
     private Group group1 = null;
     private Composite composite4 = null;
     private Button deleteButton = null;
     private Button copyButton = null;
     private Button newButton = null;
     private Button helpButton = null;
     private Button cancelButton = null;
     private Button saveButton = null;
     private Composite composite5 = null;
     private Label label = null;
     private Text templateNameText = null;
     private Label label1 = null;
     private Text printerNameText = null;
     private Text jasperFileText = null;
     private Button browseButton = null;
     private List list = null;
     private Group composite6 = null;
     private ConfigurationTree configTree = null;
 
     private Shell shell;
 
     private TemplateStore templateStore;
     private Template templateSelected = null;
 
     boolean templateDirty = false;
 
     // constants
     final private String HELP_URL = "http://www.example.com";
 
     @Override
     public void createPartControl(Composite parent) {
         shell = parent.getShell();
 
        // TODO implement errors for templateStore
        try {
            templateStore = new TemplateStore();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
 
         top = new Composite(parent, SWT.NONE);
         top.setLayout(new GridLayout());
 
         createGroup();
     }
 
     @Override
     public void setFocus() {
     }
 
     /**
      * This method initializes group
      * 
      */
     private void createGroup() {
         GridData gridData = new GridData();
         gridData.horizontalAlignment = GridData.FILL;
         gridData.grabExcessHorizontalSpace = true;
         gridData.grabExcessVerticalSpace = true;
         gridData.verticalAlignment = GridData.FILL;
         group = new Group(top, SWT.NONE);
         group.setText("Templates Editor");
         group.setLayoutData(gridData);
         createComposite();
         group.setLayout(new GridLayout());
         createComposite1();
     }
 
     /**
      * This method initializes composite
      * 
      */
     private void createComposite() {
         GridLayout gridLayout = new GridLayout();
         gridLayout.numColumns = 3;
         GridData gridData1 = new GridData();
         gridData1.horizontalAlignment = GridData.FILL;
         gridData1.grabExcessHorizontalSpace = true;
         gridData1.grabExcessVerticalSpace = true;
         gridData1.verticalAlignment = GridData.FILL;
         composite = new Composite(group, SWT.NONE);
         createComposite2();
         composite.setLayoutData(gridData1);
         composite.setLayout(gridLayout);
         @SuppressWarnings("unused")
         Label filler = new Label(composite, SWT.NONE);
         createComposite3();
     }
 
     /**
      * This method initializes composite1
      * 
      */
     private void createComposite1() {
         GridData gridData2 = new GridData();
         gridData2.horizontalAlignment = GridData.FILL;
         gridData2.grabExcessHorizontalSpace = true;
         gridData2.grabExcessVerticalSpace = false;
         gridData2.verticalAlignment = GridData.CENTER;
         composite1 = new Composite(group, SWT.NONE);
         composite1.setLayoutData(gridData2);
         composite1.setLayout(new FillLayout());
         helpButton = new Button(composite1, SWT.NONE);
         helpButton.setText("Help");
         helpButton.addSelectionListener(helpListener);
 
         @SuppressWarnings("unused")
         Label filler22 = new Label(composite1, SWT.NONE);
         @SuppressWarnings("unused")
         Label filler21 = new Label(composite1, SWT.NONE);
         @SuppressWarnings("unused")
         Label filler2 = new Label(composite1, SWT.NONE);
         cancelButton = new Button(composite1, SWT.NONE);
         cancelButton.setText("Cancel");
         cancelButton.addSelectionListener(cancelListener);
 
         saveButton = new Button(composite1, SWT.NONE);
         saveButton.setText("Save Template");
         saveButton.addSelectionListener(saveAllListener);
     }
 
     /**
      * This method initializes composite2
      * 
      */
     private void createComposite2() {
         GridData gridData3 = new GridData();
         gridData3.horizontalAlignment = GridData.BEGINNING;
         gridData3.grabExcessVerticalSpace = true;
         gridData3.grabExcessHorizontalSpace = false;
         gridData3.verticalAlignment = GridData.FILL;
         composite2 = new Composite(composite, SWT.NONE);
         createGroup1();
         composite2.setLayout(new GridLayout());
         composite2.setLayoutData(gridData3);
         createComposite4();
     }
 
     /**
      * This method initializes composite3
      * 
      */
     private void createComposite3() {
         GridData gridData4 = new GridData();
         gridData4.horizontalAlignment = GridData.FILL;
         gridData4.grabExcessHorizontalSpace = true;
         gridData4.grabExcessVerticalSpace = true;
         gridData4.verticalAlignment = GridData.FILL;
         composite3 = new Composite(composite, SWT.NONE);
         createComposite5();
         composite3.setLayoutData(gridData4);
         createComposite62();
         composite3.setLayout(new GridLayout());
     }
 
     /**
      * This method initializes group1
      * 
      */
     private void createGroup1() {
         GridData gridData6 = new GridData();
         gridData6.grabExcessVerticalSpace = true;
         gridData6.verticalAlignment = GridData.FILL;
         gridData6.grabExcessHorizontalSpace = true;
         gridData6.horizontalAlignment = GridData.FILL;
         FillLayout fillLayout1 = new FillLayout();
         fillLayout1.type = org.eclipse.swt.SWT.VERTICAL;
         group1 = new Group(composite2, SWT.NONE);
         group1.setText("Templates");
         group1.setLayoutData(gridData6);
         group1.setLayout(fillLayout1);
         list = new List(group1, SWT.BORDER | SWT.V_SCROLL);
         list.addSelectionListener(listListener);
         for (String s : templateStore.getTemplateNames())
             list.add(s);
         list.redraw();
 
     }
 
     private SelectionListener listListener = new SelectionListener() {
         @Override
         public void widgetSelected(SelectionEvent e) {
             String[] selectedItems = list.getSelection();
             if (selectedItems.length == 1) {
 
                 if (templateSelected != null) {
 
                     if (templateDirty || configTree.isDirty()) {
 
                         MessageBox messageBox = new MessageBox(shell,
                             SWT.ICON_QUESTION | SWT.YES | SWT.NO);
                         messageBox
                             .setMessage("Template has been modified, do you want to save your changes?");
                         messageBox.setText("Template Editor Saving");
                         int response = messageBox.open();
                         if (response == SWT.YES) {
                             try {
                                 templateStore.updateTemplate(templateSelected);
                             } catch (IOException e1) {
                                 Error(
                                     "Template Save Error",
                                     "Error occured saving template: "
                                         + e1.getMessage());
                             }
                         }
                     }
                 }
 
                 Template t = templateStore.getTemplate(selectedItems[0]);
 
                 setSelectedTemplate(t);
             } else {
                 setSelectedTemplate(null);
             }
         }
 
         @Override
         public void widgetDefaultSelected(SelectionEvent e) {
             widgetSelected(e);
 
         }
     };
 
     private void updateJasperFileText(String selectedName) {
 
         if (templateSelected == null)
             return;
 
         if (!templateSelected.jasperFileDataExists()) {
             jasperFileText.setText("Select a Jasper file.");
             jasperFileText.setBackground(new Color(shell.getDisplay(), 255, 0,
                 0));
         } else {
             if (selectedName == null) {
                 selectedName = "Jasper file loaded";
             }
             jasperFileText.setText(selectedName);
             jasperFileText.setBackground(new Color(shell.getDisplay(), 255,
                 255, 255));
         }
         jasperFileText.redraw();
     }
 
     private void setSelectedTemplate(Template t) {
 
         if (t != null) {
             templateSelected = new CBSRTemplate();
             Template.Clone(t, templateSelected);
 
             templateNameText.setText(t.getName());
             printerNameText.setText(t.getIntendedPrinter());
 
             updateJasperFileText(null);
 
             try {
                 configTree.populateTree(templateSelected.getConfiguration());
             } catch (TreeException e) {
                 Error("Set Templat Error", e.getError());
                 return;
             }
             templateDirty = false;
 
         } else {
             templateSelected = null;
             templateNameText.setText("Select a template.");
             printerNameText.setText("");
             jasperFileText.setText("");
             jasperFileText.setBackground(new Color(shell.getDisplay(), 255,
                 255, 255));
             try {
                 configTree.populateTree(null);
             } catch (TreeException e) {
                 Error("Set Templat Error", e.getError());
                 return;
             }
         }
         templateDirty = false;
     }
 
     /**
      * This method initializes composite4
      * 
      */
     private void createComposite4() {
 
         composite4 = new Composite(composite2, SWT.NONE);
         composite4.setLayout(new RowLayout());
         deleteButton = new Button(composite4, SWT.NONE);
         deleteButton.setText("Delete ");
         deleteButton.addSelectionListener(new SelectionListener() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 if (templateSelected != null) {
                     MessageBox messageBox = new MessageBox(shell,
                         SWT.ICON_QUESTION | SWT.YES | SWT.NO);
                     messageBox.setMessage("Are you sure you want to delete "
                         + templateSelected.getName() + "?");
                     messageBox.setText("Deleting Template");
 
                     int response = messageBox.open();
                     if (response == SWT.YES) {
                         if (templateStore.removeTemplate(templateSelected)) {
                             list.remove(templateSelected.getName());
 
                             if (list.getItemCount() > 0) {
                                 list.deselectAll();
 
                                 int lastItemIndex = list.getItemCount() - 1;
 
                                 if (lastItemIndex >= 0) {
                                     list.select(lastItemIndex);
                                     setSelectedTemplate(templateStore
                                         .getTemplate(list
                                             .getItem(lastItemIndex)));
                                 }
 
                             } else {
                                 setSelectedTemplate(null);
                             }
 
                         } else {
                             Error("Template not in Template Store.",
                                 "Template does not exist, already deleted.");
                             return;
                         }
                     }
                 }
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
 
         copyButton = new Button(composite4, SWT.NONE);
         copyButton.setText("Copy ");
         copyButton.addSelectionListener(new SelectionListener() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 if (templateSelected != null) {
 
                     StringInputDialog dialog = new StringInputDialog(
                         "Cloned Template Name",
                         "What is the name of the cloned template?", shell,
                         SWT.NONE);
                     String cloneName = dialog.open(templateSelected.getName()
                         + " copy");
 
                     if (cloneName != null) {
 
                         Template clone = new CBSRTemplate();
                         Template.Clone(templateSelected, clone);
                         clone.setName(cloneName);
 
                         if (templateStore.addTemplate(clone)) {
                             list.add(clone.getName());
                             list.redraw();
                         } else {
                             Error("Template Exists",
                                 "Duplicate name collision. Your cloned template must have a unique name.");
                             return;
                         }
                     }
                 }
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
 
         newButton = new Button(composite4, SWT.NONE);
         newButton.setText("New");
         newButton.addSelectionListener(new SelectionListener() {
             @Override
             public void widgetSelected(SelectionEvent e) {
 
                 StringInputDialog dialog = new StringInputDialog(
                     "New Template Name",
                     "What is the name of this new template?", shell, SWT.NONE);
                 String newTemplateName = dialog.open(null);
 
                 if (newTemplateName != null) {
                     Template ct = new CBSRTemplate();
                     ct.setJasperFileData(null);
                     ct.setDefaultConfiguration();
                     ct.setName(newTemplateName);
 
                     if (templateStore.addTemplate(ct)) {
                         list.add(ct.getName());
                         list.redraw();
                     } else {
                         Error("Template Exists",
                             "Your new template must have a unique name.");
                     }
                 }
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
     }
 
     /**
      * This method initializes composite5
      * 
      */
     private void createComposite5() {
         GridData gridData11 = new GridData();
         gridData11.horizontalAlignment = GridData.FILL;
         gridData11.grabExcessHorizontalSpace = true;
         gridData11.verticalAlignment = GridData.CENTER;
         GridData gridData8 = new GridData();
         gridData8.horizontalAlignment = GridData.FILL;
         gridData8.grabExcessHorizontalSpace = true;
         gridData8.verticalAlignment = GridData.CENTER;
         GridData gridData7 = new GridData();
         gridData7.grabExcessHorizontalSpace = true;
         gridData7.verticalAlignment = GridData.CENTER;
         gridData7.horizontalAlignment = GridData.FILL;
         GridLayout gridLayout2 = new GridLayout();
         gridLayout2.numColumns = 3;
         composite5 = new Composite(composite3, SWT.NONE);
         composite5.setLayout(gridLayout2);
         composite5.setLayoutData(gridData11);
         label = new Label(composite5, SWT.NONE);
         label.setText("Template Name:");
         templateNameText = new Text(composite5, SWT.BORDER);
         templateNameText.setEditable(false);
         templateNameText.setLayoutData(gridData7);
         @SuppressWarnings("unused")
         Label filler7 = new Label(composite5, SWT.NONE);
         label = new Label(composite5, SWT.NONE);
         label.setText("Intended Printer:");
         printerNameText = new Text(composite5, SWT.BORDER);
         printerNameText.setEditable(true);
         printerNameText.setLayoutData(gridData7);
         printerNameText.addModifyListener(new ModifyListener() {
 
             @Override
             public void modifyText(ModifyEvent e) {
                 if (templateSelected == null || e.widget == null)
                     return;
                 templateSelected.setIntendedPrinter(((Text) e.widget).getText());
                 templateDirty = true;
             }
         });
 
         filler7 = new Label(composite5, SWT.NONE);
         label1 = new Label(composite5, SWT.NONE);
         label1.setText("Jasper File:");
         jasperFileText = new Text(composite5, SWT.BORDER);
         jasperFileText.setEditable(false);
         jasperFileText.setLayoutData(gridData8);
         browseButton = new Button(composite5, SWT.NONE);
         browseButton.setText("Browse...");
         browseButton.addSelectionListener(new SelectionListener() {
             public void widgetSelected(SelectionEvent event) {
                 if (templateSelected == null)
                     return;
 
                 FileDialog fd = new FileDialog(shell, SWT.OPEN);
                 fd.setText("Select Jasper File");
                 String[] filterExt = { "*.jrxml" };
                 fd.setFilterExtensions(filterExt);
                 String selected = fd.open();
                 if (selected != null) {
 
                     File selectedFile = new File(selected);
                     if (!selectedFile.exists()) {
                         Error("Jasper File Non-existant",
                             "Could not find the selected Jasper file.");
                         return;
                     }
                     byte[] jasperFileData;
                     try {
                         jasperFileData = fileToBytes(selectedFile);
                     } catch (IOException e) {
                         Error(
                             "Loading Jasper File",
                             "Could not read the specified jasper file.\n\n"
                                 + e.getMessage());
                         return;
                     }
                     (templateSelected).setJasperFileData(jasperFileData);
                     updateJasperFileText(selected);
                     templateDirty = true;
                 }
             }
 
             public void widgetDefaultSelected(SelectionEvent event) {
                 widgetSelected(event);
             }
         });
     }
 
     public static byte[] fileToBytes(File file) throws IOException {
         InputStream is = new FileInputStream(file);
 
         byte[] bytes = new byte[(int) file.length()];
 
         int offset = 0;
         int readCount = 0;
         while (offset < bytes.length
             && (readCount = is.read(bytes, offset, bytes.length - offset)) >= 0) {
             offset += readCount;
         }
 
         if (offset < bytes.length) {
             throw new IOException("Could not completely read file "
                 + file.getName());
         }
 
         is.close();
         return bytes;
     }
 
     private void createComposite62() {
         GridData gridData10 = new GridData();
         gridData10.horizontalAlignment = GridData.FILL;
         gridData10.grabExcessVerticalSpace = true;
         gridData10.verticalAlignment = GridData.FILL;
         GridData gridData9 = new GridData();
         gridData9.grabExcessHorizontalSpace = true;
         gridData9.verticalAlignment = GridData.FILL;
         gridData9.grabExcessVerticalSpace = true;
         gridData9.widthHint = -1;
         gridData9.horizontalAlignment = GridData.FILL;
         composite6 = new Group(composite3, SWT.NONE);
         composite6.setLayout(new GridLayout());
         composite6.setText("Configuration");
         composite6.setLayoutData(gridData10);
         configTree = new ConfigurationTree(composite6, SWT.NONE);
     }
 
     private void Error(String title, String message) {
         MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
         messageBox.setMessage(message);
         messageBox.setText(title);
         messageBox.open();
     }
 
     private SelectionListener helpListener = new SelectionListener() {
         @Override
         public void widgetSelected(SelectionEvent e) {
             try {
 
                 // TODO test url opening
                 PlatformUI.getWorkbench().getBrowserSupport()
                     .getExternalBrowser().openURL(new URL(HELP_URL));
             } catch (Exception e1) {
                 Error("Open URL Problem",
                     "Could not open help url.\n\n" + e1.getMessage());
                 return;
             }
         }
 
         @Override
         public void widgetDefaultSelected(SelectionEvent e) {
             widgetSelected(e);
         }
     };
 
     private SelectionListener cancelListener = new SelectionListener() {
         @Override
         public void widgetSelected(SelectionEvent e) {
             MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION
                 | SWT.YES | SWT.NO | SWT.CANCEL);
             messageBox
                 .setMessage("Closing Template Editor. Do you want to save your changes?");
             messageBox.setText("Closing Template Editor");
             int response = messageBox.open();
             if (response == SWT.YES) {
                 saveCurrentTemplate();
                 dispose();
             } else if (response == SWT.NO) {
                 dispose();
                 // TODO make close work
                 // TODO prompt user if they close the view via the X.
 
             } else {
                 return;
             }
         }
 
         @Override
         public void widgetDefaultSelected(SelectionEvent e) {
             widgetSelected(e);
         }
     };
 
     private void saveCurrentTemplate() {
 
         try {
             configTree.resetEditor();
         } catch (TreeException e2) {
             Error("Editor Error", "Could not reset editor: " + e2.getError());
             return;
         }
         if (!templateDirty && !configTree.isDirty()) {
             return;
         }
 
         if (templateSelected == null) {
             Error("No Template Selected",
                 "Cannot save template. Please select a template first.");
             return;
         }
 
         try {
             templateStore.updateTemplate(templateSelected);
         } catch (IOException e1) {
             Error("Save Template Error", "Could not save template: " + e1);
             return;
         }
         templateDirty = false;
     }
 
     /**
      * Saves the entire preference store and all the changed template
      * information as a serialized object.
      */
     private SelectionListener saveAllListener = new SelectionListener() {
         @Override
         public void widgetSelected(SelectionEvent e) {
             saveCurrentTemplate();
             MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION
                 | SWT.OK);
             messageBox.setMessage("Template has been successfully saved.");
             messageBox.setText("Template Saved");
             messageBox.open();
 
         }
 
         @Override
         public void widgetDefaultSelected(SelectionEvent e) {
             widgetSelected(e);
 
         }
     };
 
 } // @jve:decl-index=0:visual-constraint="10,10,559,504"
