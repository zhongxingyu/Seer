 /*
  * Copyright 2004-2006 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.dolteng.eclipse.wizard;
 
 import java.util.Arrays;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.JavaConventions;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.launching.IVMInstall;
 import org.eclipse.jdt.launching.IVMInstall2;
 import org.eclipse.jdt.launching.IVMInstallType;
 import org.eclipse.jdt.launching.JavaRuntime;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
 import org.seasar.dolteng.eclipse.nls.Labels;
 import org.seasar.dolteng.eclipse.nls.Messages;
 import org.seasar.dolteng.eclipse.template.ProjectBuildConfigResolver;
 import org.seasar.dolteng.eclipse.template.ProjectBuildConfigResolver.ProjectDisplay;
 import org.seasar.framework.util.ArrayMap;
 import org.seasar.framework.util.StringUtil;
 
 /**
  * @author taichi
  * 
  */
 public class ChuraProjectWizardPage extends WizardNewProjectCreationPage {
 
     private Text rootPkgName;
 
     private Combo projectType;
 
     private ArrayMap selectedProjectTypes = null;
 
     private ArrayMap projectMap = new ArrayMap();
 
     private ArrayMap tigerProjects = new ArrayMap();
 
     private Button useDefaultJre;
 
     private Button selectJre;
 
     private Combo enableJres;
 
     private ArrayMap jres = new ArrayMap();
 
     private ProjectBuildConfigResolver resolver = new ProjectBuildConfigResolver();
 
     /**
      * @param pageName
      */
     public ChuraProjectWizardPage() {
         super("ChuraProjectWizard");
         setTitle(Labels.WIZARD_CHURA_PROJECT_TITLE);
         setDescription(Messages.CHURA_PROJECT_DESCRIPTION);
 
         resolver.initialize();
 
         setUpProjects(projectMap, "1.4");
         setUpProjects(tigerProjects, "1.5");
 
         String version = getDefaultJavaVersion();
        if (version.startsWith(JavaCore.VERSION_1_5)) {
             selectedProjectTypes = tigerProjects;
         } else {
             selectedProjectTypes = projectMap;
         }
     }
 
     private void setUpProjects(Map m, String jre) {
         ProjectDisplay[] projects = resolver.getProjects(jre);
         Arrays.sort(projects);
         for (int i = 0; i < projects.length; i++) {
             m.put(projects[i].name, projects[i].id);
         }
     }
 
     /**
      * @return
      */
     private String getDefaultJavaVersion() {
         String version = JavaCore.getOption(JavaCore.COMPILER_COMPLIANCE);
         IVMInstall vm = JavaRuntime.getDefaultVMInstall();
         if (vm instanceof IVMInstall2) {
             IVMInstall2 vm2 = (IVMInstall2) vm;
             version = vm2.getJavaVersion();
         }
         return version;
     }
 
     public void createControl(Composite parent) {
         super.createControl(parent);
         Composite composite = (Composite) getControl();
         createRootPackage(composite);
         createJreContainer(composite);
         createProjectType(composite);
     }
 
     private void createRootPackage(Composite parent) {
         Composite composite = new Composite(parent, SWT.NONE);
         GridLayout layout = new GridLayout();
         layout.numColumns = 2;
         composite.setLayout(layout);
         composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
         Label label = new Label(composite, SWT.NONE);
         label.setText(Labels.WIZARD_PAGE_CHURA_ROOT_PACKAGE);
         label.setFont(parent.getFont());
 
         this.rootPkgName = new Text(composite, SWT.BORDER);
         GridData data = new GridData(GridData.FILL_HORIZONTAL);
         data.widthHint = 250;
         this.rootPkgName.setLayoutData(data);
         this.rootPkgName.setFont(parent.getFont());
         this.rootPkgName.addListener(SWT.Modify, new Listener() {
             public void handleEvent(Event event) {
                 boolean is = validatePage();
                 if (is == false) {
                     setErrorMessage(validateRootPackageName());
                 }
                 setPageComplete(is);
             }
         });
     }
 
     private void createJreContainer(Composite parent) {
         GridData data = new GridData(GridData.FILL_HORIZONTAL);
         Group group = new Group(parent, SWT.NONE);
         group.setLayout(new GridLayout(2, false));
         group.setText(Labels.WIZARD_PAGE_CHURA_JRE_CONTAINER);
         group.setLayoutData(data);
 
         data = new GridData(GridData.FILL_BOTH);
         useDefaultJre = new Button(group, SWT.RADIO);
         useDefaultJre.setSelection(true);
         data.horizontalSpan = 2;
         useDefaultJre.setLayoutData(data);
         useDefaultJre.setText(Labels.bind(
                 Labels.WIZARD_PAGE_CHURA_USE_DEFAULT_JRE,
                 getDefaultJavaVersion()));
         useDefaultJre.addSelectionListener(new SelectionAdapter() {
             public void widgetSelected(SelectionEvent e) {
                 enableJres.setEnabled(false);
                 selectJre(ChuraProjectWizardPage.this, JavaCore
                         .getOption(JavaCore.COMPILER_COMPLIANCE));
             }
         });
 
         data = new GridData();
         selectJre = new Button(group, SWT.RADIO);
         selectJre.setLayoutData(data);
         selectJre.setText("");
         selectJre.addSelectionListener(new SelectionAdapter() {
             public void widgetSelected(SelectionEvent e) {
                 enableJres.setEnabled(true);
                 enableJres.select(0);
                 selectJre(ChuraProjectWizardPage.this);
             }
         });
 
         data = new GridData();
         enableJres = new Combo(group, SWT.BORDER | SWT.READ_ONLY);
         enableJres.setLayoutData(data);
 
         IVMInstallType[] types = JavaRuntime.getVMInstallTypes();
         for (int i = 0; i < types.length; i++) {
             IVMInstall[] installs = types[i].getVMInstalls();
             for (int j = 0; j < installs.length; j++) {
                 if (installs[j] instanceof IVMInstall2) {
                     IVMInstall2 vm2 = (IVMInstall2) installs[j];
                     StringBuffer stb = new StringBuffer();
                     stb.append(installs[j].getName());
                     stb.append(" (");
                     stb.append(vm2.getJavaVersion());
                     stb.append(")");
                     jres.put(stb.toString(), vm2);
                 }
             }
         }
         String[] ary = new String[jres.size()];
         for (int i = 0; i < jres.size(); i++) {
             ary[i] = jres.getKey(i).toString();
         }
         enableJres.setItems(ary);
         enableJres.addSelectionListener(new SelectionAdapter() {
             public void widgetSelected(SelectionEvent e) {
                 selectJre(ChuraProjectWizardPage.this);
             }
         });
         enableJres.setEnabled(false);
     }
 
     private static void selectJre(ChuraProjectWizardPage page) {
         IVMInstall2 vm = (IVMInstall2) page.jres.get(page.enableJres.getText());
         selectJre(page, vm.getJavaVersion());
     }
 
     private static void selectJre(ChuraProjectWizardPage page, String version) {
        if (version.startsWith(JavaCore.VERSION_1_5)) {
             page.selectedProjectTypes = page.tigerProjects;
         } else {
             page.selectedProjectTypes = page.projectMap;
         }
         page.projectType.setItems(page.getProjectTypes());
         page.projectType.select(0);
     }
 
     private void createProjectType(Composite parent) {
         Composite composite = new Composite(parent, SWT.NONE);
         GridLayout layout = new GridLayout();
         layout.numColumns = 2;
         composite.setLayout(layout);
         composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
         Label label = new Label(composite, SWT.NONE);
         label.setText(Labels.WIZARD_PAGE_CHURA_TYPE_SELECTION);
         label.setFont(parent.getFont());
 
         this.projectType = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
         this.projectType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
         this.projectType.setItems(getProjectTypes());
         this.projectType.select(0);
         this.projectType.pack();
     }
 
     private String[] getProjectTypes() {
         String[] ary = new String[selectedProjectTypes.size()];
         for (int i = 0; i < ary.length; i++) {
             ary[i] = selectedProjectTypes.getKey(i).toString();
         }
         return ary;
     }
 
     protected boolean validatePage() {
         return super.validatePage() ? StringUtil
                 .isEmpty(validateRootPackageName()) : false;
     }
 
     protected String validateRootPackageName() {
         String name = getRootPackageName();
         if (StringUtil.isEmpty(name)) {
             return Messages.PACKAGE_NAME_IS_EMPTY;
         }
         IStatus val = JavaConventions.validatePackageName(name);
         if (val.getSeverity() == IStatus.ERROR
                 || val.getSeverity() == IStatus.WARNING) {
             return NLS.bind(Messages.INVALID_PACKAGE_NAME, val.getMessage());
         }
         return null;
     }
 
     public String getRootPackageName() {
         if (rootPkgName == null) {
             return "";
         }
         return rootPkgName.getText();
     }
 
     public String getRootPackagePath() {
         return getRootPackageName().replace('.', '/');
     }
 
     public String getProjectTypeKey() {
         return (String) selectedProjectTypes.get(this.projectType.getText());
     }
 
     public String getJREContainer() {
         IPath path = new Path(JavaRuntime.JRE_CONTAINER);
         if (selectJre.getSelection()) {
             IVMInstall vm = (IVMInstall) jres.get(enableJres.getText());
             path = path.append(vm.getVMInstallType().getId());
             path = path.append(vm.getName());
         }
         return path.toString();
     }
 
     public ProjectBuildConfigResolver getResolver() {
         return this.resolver;
     }
 
 }
