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
 package org.nuxeo.ide.sdk.ui.actions;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.ui.ISharedImages;
 import org.eclipse.jdt.ui.JavaUI;
 import org.eclipse.jface.viewers.BaseLabelProvider;
 import org.eclipse.jface.viewers.CheckStateChangedEvent;
 import org.eclipse.jface.viewers.CheckboxTreeViewer;
 import org.eclipse.jface.viewers.ICheckStateListener;
 import org.eclipse.jface.viewers.ICheckStateProvider;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.nuxeo.ide.common.UI;
 import org.nuxeo.ide.sdk.NuxeoSDK;
 import org.nuxeo.ide.sdk.index.Dependency;
 import org.nuxeo.ide.sdk.index.DependencyEntry;
 import org.nuxeo.ide.sdk.index.DependencyProvider;
 import org.nuxeo.ide.sdk.model.Artifact;
 import org.nuxeo.ide.sdk.model.PomModel;
 
 /**
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  * 
  */
 public class CollectDependenciesPage extends WizardPage {
 
     protected CheckboxTreeViewer tv;
 
     protected PomModel pom;
 
     public CollectDependenciesPage() {
         super("collectDependencies", "Synchronize POM", null);
     }
 
     @Override
     public void createControl(Composite parent) {
         Composite panel = new Composite(parent, SWT.NONE);
         panel.setLayout(new GridLayout());
         Label label = new Label(panel, SWT.NONE);
         label.setText("Select the dependencies you want to add to the POM:");
         tv = new CheckboxTreeViewer(panel, SWT.V_SCROLL | SWT.H_SCROLL
                 | SWT.BORDER);
         tv.setLabelProvider(new MyLabelProvider());
         tv.setContentProvider(new MyContentProvider());
         tv.setCheckStateProvider(new MyCheckStateProvider());
         GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
         tv.getTree().setLayoutData(gd);
         final Label status = new Label(panel, SWT.NONE);
         gd = new GridData(SWT.FILL, SWT.LEFT, true, false);
         status.setLayoutData(gd);
         status.setText("");
         setControl(panel);
 
         tv.addSelectionChangedListener(new ISelectionChangedListener() {
             @Override
             public void selectionChanged(SelectionChangedEvent event) {
                 String text = "";
                 if (!event.getSelection().isEmpty()) {
                     Object o = ((IStructuredSelection) event.getSelection()).getFirstElement();
                     if (o instanceof DependencyEntry) {
                         text = ((DependencyEntry) o).getLocation();
                     } else if (o instanceof Dependency) {
                         text = ((Dependency) o).getLocation();
                     }
                 }
                 status.setText(text);
             }
         });
 
         tv.addCheckStateListener(new ICheckStateListener() {
             @Override
             public void checkStateChanged(CheckStateChangedEvent event) {
                 if (event.getElement() instanceof DependencyEntry) {
                     List<Dependency> deps = ((DependencyEntry) event.getElement()).getDependencies();
                     for (Dependency dep : deps) {
                         tv.setChecked(dep, event.getChecked());
                     }
                 }
             }
         });
         computeDependencies(tv);
     }
 
     protected DependencyEntry[] removeExistingEntries(IJavaProject project,
             DependencyEntry[] entries) throws Exception {
         pom = PomModel.getPomModel(project.getProject());
         HashSet<Artifact> artifacts = new HashSet<Artifact>(
                 pom.getDependencies());
         ArrayList<DependencyEntry> list = new ArrayList<DependencyEntry>(
                 entries.length);
         for (DependencyEntry entry : entries) {
             if (!artifacts.contains(entry.getArtifact())) {
                 list.add(entry);
             }
         }
         return list.toArray(new DependencyEntry[list.size()]);
     }
 
     protected DependencyEntry[] removeDuplicateEntries(
             DependencyEntry[] entriesToUpdate, DependencyEntry[] entries)
             throws Exception {
         HashSet<Artifact> set = new HashSet<Artifact>(entries.length);
         for (DependencyEntry entry : entries) {
             set.add(entry.getArtifact());
         }
         ArrayList<DependencyEntry> list = new ArrayList<DependencyEntry>(
                 entriesToUpdate.length);
         for (DependencyEntry entry : entriesToUpdate) {
             if (!set.contains(entry.getArtifact())) {
                 list.add(entry);
             }
         }
         if (list.size() == entriesToUpdate.length) {
             return entriesToUpdate;
         }
         return list.toArray(new DependencyEntry[list.size()]);
     }
 
     protected void computeDependencies(CheckboxTreeViewer tv) {
         NuxeoSDK sdk = NuxeoSDK.getDefault();
         if (sdk == null) {
             UI.showError("Failed to collect dependencies: No Nuxeo SDK was configured");
             return;
         }
         try {
             IJavaProject project = ((SyncPomWizard) getWizard()).getSelectedProject();
             Set<Dependency> mainDeps = DependencyProvider.getNonTestDependencies(project);
             DependencyEntry[] mainEntries = sdk.getArtifactIndex().resolve(
                     mainDeps);
             // then remove deps already in pom
             mainEntries = removeExistingEntries(project, mainEntries);
 
             Set<Dependency> testDeps = DependencyProvider.getTestDependencies(project);
            DependencyEntry[] testEntries = sdk.getTestArtifactIndex().resolve(
                     testDeps);
             // remove test dependencies already in main dependencies
             testEntries = removeDuplicateEntries(testEntries, mainEntries);
             // then remove deps already in pom
             testEntries = removeExistingEntries(project, testEntries);
             // add test scope
             for (DependencyEntry entry : testEntries) {
                 entry.getArtifact().setScope("test");
             }
 
             DependencyEntry[] entries = null;
             if (mainEntries.length == 0) {
                 entries = testEntries;
             } else if (testEntries.length == 0) {
                 entries = mainEntries;
             } else {
                 entries = new DependencyEntry[mainEntries.length
                         + testEntries.length];
                 System.arraycopy(mainEntries, 0, entries, 0, mainEntries.length);
                 System.arraycopy(testEntries, 0, entries, mainEntries.length,
                         testEntries.length);
             }
 
             tv.setInput(entries);
         } catch (Exception e) {
             UI.showError("Failed to collect dependencies", e);
         }
     }
 
     public List<Artifact> getCheckedArtifacts() {
         ArrayList<Artifact> result = new ArrayList<Artifact>();
         for (Object o : tv.getCheckedElements()) {
            if (tv.getGrayed(o)) {
                continue;
            }
             if (o instanceof DependencyEntry) {
                 result.add(((DependencyEntry) o).getArtifact());
             }
         }
         return result;
     }
 
     public boolean writePom(IProgressMonitor monitor) throws Exception {
         List<Artifact> artifacts = getCheckedArtifacts();
         if (artifacts.isEmpty()) {
             return false;
         }
         for (Artifact artifact : artifacts) {
             pom.addDependency(artifact);
         }
         String content = pom.toXML();
         ByteArrayInputStream in = new ByteArrayInputStream(
                 content.getBytes("UTF-8"));
         IJavaProject project = ((SyncPomWizard) getWizard()).getSelectedProject();
         IFile file = project.getProject().getFile("pom.xml");
         if (file.exists()) {
             file.setContents(in, true, true, monitor);
         } else {
             file.create(in, true, monitor);
         }
         return true;
     }
 
     static class MyLabelProvider extends BaseLabelProvider implements
             ILabelProvider {
 
         @Override
         public Image getImage(Object element) {
             if (element instanceof DependencyEntry) {
                 return JavaUI.getSharedImages().getImage(
                         ISharedImages.IMG_OBJS_JAR);
             } else {
                 return JavaUI.getSharedImages().getImage(
                         ISharedImages.IMG_OBJS_CFILE);
             }
         }
 
         @Override
         public String getText(Object element) {
             if (element instanceof DependencyEntry) {
                 return ((DependencyEntry) element).getLabel();
             } else if (element instanceof Dependency) {
                 return ((Dependency) element).getName();
             } else if (element instanceof File) {
                 return ((File) element).getName();
             } else if (element instanceof IJavaProject) {
                 return ((IJavaProject) element).getProject().getName();
             }
             return "";
         }
 
         @Override
         public void dispose() {
             super.dispose();
         }
 
     }
 
     static class MyContentProvider implements ITreeContentProvider {
 
         @Override
         public void dispose() {
         }
 
         @Override
         public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
         }
 
         @Override
         public Object[] getElements(Object inputElement) {
             DependencyEntry[] deps = (DependencyEntry[]) inputElement;
             if (deps != null) {
                 return deps;
             }
             return new Object[0];
         }
 
         @Override
         public Object[] getChildren(Object parentElement) {
             if (parentElement instanceof DependencyEntry) {
                 DependencyEntry entry = (DependencyEntry) parentElement;
                 return entry.getDependencies().toArray(
                         new Dependency[entry.size()]);
             }
             return null;
         }
 
         @Override
         public Object getParent(Object element) {
             return null;
         }
 
         @Override
         public boolean hasChildren(Object element) {
             return element instanceof DependencyEntry;
         }
     }
 
     static class MyCheckStateProvider implements ICheckStateProvider {
 
         @Override
         public boolean isChecked(Object element) {
             if (element instanceof DependencyEntry) {
                 return ((DependencyEntry) element).isResolved();
             } else if (element instanceof Dependency) {
                 return true;
             }
             return false;
         }
 
         @Override
         public boolean isGrayed(Object element) {
             if (element instanceof DependencyEntry) {
                 return !((DependencyEntry) element).isResolved();
             }
             return true;
         }
 
     }
 }
