 /*
  * Copyright (c) 2012, 2013 Hemanta Sapkota.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Hemanta Sapkota (laex.pearl@gmail.com)
  */
 package com.laex.j2objc.preferences;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowData;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.IWorkbenchPropertyPage;
 import org.eclipse.ui.dialogs.ContainerSelectionDialog;
 import org.eclipse.ui.dialogs.ElementListSelectionDialog;
 import org.eclipse.ui.dialogs.PropertyPage;
 import org.eclipse.ui.internal.SharedImages;
 
 import com.laex.j2objc.util.LogUtil;
 import com.laex.j2objc.util.MessageUtil;
 import com.laex.j2objc.util.PropertiesUtil;
import org.eclipse.swt.widgets.Label;
 
 /**
  * The Class ClasspathPropertyPage.
  */
 public class ClasspathPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {
 
     /** The table. */
     private Table table;
 
     /** The selections. */
    private List<String> selections;
 
     /** The table viewer. */
     private TableViewer tableViewer;
 
     private Button btnRemove;
 
     /**
      * The Class TableLabelProvider.
      */
     private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
 
         /*
          * (non-Javadoc)
          * 
          * @see
          * org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java
          * .lang.Object, int)
          */
         public Image getColumnImage(Object element, int columnIndex) {
             if (element.toString().endsWith("jar")) {
                 return new  org.eclipse.jdt.internal.ui.SharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_JAR);
             }
 
             return new SharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see
          * org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.
          * lang.Object, int)
          */
         public String getColumnText(Object element, int columnIndex) {
             if (columnIndex == 0)
                 return (String) element;
 
             return null;
         }
     }
 
     /**
      * The Class ContentProvider.
      */
     private class ContentProvider implements IStructuredContentProvider {
 
         /*
          * (non-Javadoc)
          * 
          * @see
          * org.eclipse.jface.viewers.IStructuredContentProvider#getElements(
          * java.lang.Object)
          */
         public Object[] getElements(Object inputElement) {
             return selections.toArray();
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see org.eclipse.jface.viewers.IContentProvider#dispose()
          */
         public void dispose() {
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see
          * org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse
          * .jface.viewers.Viewer, java.lang.Object, java.lang.Object)
          */
         public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
         }
     }
 
     /**
      * Instantiates a new classpath property page.
      */
     public ClasspathPropertyPage() {
         setDescription("");
         setMessage("J2OBJC Preferences");
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse
      * .swt.widgets.Composite)
      */
     @Override
     public Control createContents(Composite parent) {
         Composite container = new Composite(parent, SWT.NULL);
         container.setLayout(new GridLayout(2, false));
 
         tableViewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
         tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
             @Override
             public void selectionChanged(SelectionChangedEvent event) {
                 onSelectionChanged(event);
             }
         });
         table = tableViewer.getTable();
         table.setLinesVisible(true);
         table.setHeaderVisible(true);
         table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 
         TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
         TableColumn tblclmnName = tableViewerColumn.getColumn();
         tblclmnName.setWidth(257);
         tblclmnName.setText("Name");
         tableViewer.setLabelProvider(new TableLabelProvider());
         tableViewer.setContentProvider(new ContentProvider());
 
         Composite composite = new Composite(container, SWT.NONE);
         composite.setLayout(new RowLayout(SWT.VERTICAL));
         composite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
 
         Button btnAddClasspath = new Button(composite, SWT.NONE);
         btnAddClasspath.setLayoutData(new RowData(88, SWT.DEFAULT));
         btnAddClasspath.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 addClasspath();
             }
         });
         btnAddClasspath.setText("Add");
 
         Button btnAddJar = new Button(composite, SWT.NONE);
         btnAddJar.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 addReferencedClasspath();
             }
         });
         btnAddJar.setLayoutData(new RowData(88, SWT.DEFAULT));
         btnAddJar.setText("Add JAR");
 
         btnRemove = new Button(composite, SWT.NONE);
         btnRemove.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 removeSelection();
             }
         });
         btnRemove.setLayoutData(new RowData(88, SWT.DEFAULT));
         btnRemove.setText("Remove");
         btnRemove.setEnabled(false);
 
         Button btnRemoveAll = new Button(composite, SWT.NONE);
         btnRemoveAll.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 removeAll();
             }
         });
         btnRemoveAll.setText("Remove All");
         
         Label label = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
         label.setLayoutData(new RowData(86, 6));
 
         // load
         try {
             loadClasspaths();
         } catch (CoreException e1) {
             LogUtil.logException(e1);
         }
 
         return container;
     }
 
     protected void addReferencedClasspath() {
         IJavaElement elm = (IJavaElement) getElement();
         try {
             IClasspathEntry[] refClasspath = elm.getJavaProject().getResolvedClasspath(true);
             ElementListSelectionDialog elsd = new ElementListSelectionDialog(getShell(), new LabelProvider());
             elsd.setElements(refClasspath);
 
             int response = elsd.open();
             if (response == ElementListSelectionDialog.CANCEL) {
                 return;
             }
 
             Object[] selected = elsd.getResult();
             for (Object o : selected) {
                 IClasspathEntry entry = (IClasspathEntry) o;
                 selections.add(entry.getPath().makeAbsolute().toOSString());
             }
 
             tableViewer.refresh();
 
         } catch (JavaModelException e) {
             LogUtil.logException(e);
         }
     }
 
     private void onSelectionChanged(SelectionChangedEvent event) {
         if (event.getSelection().isEmpty()) {
             btnRemove.setEnabled(false);
         } else {
             btnRemove.setEnabled(true);
         }
     }
 
     /**
      * Removes the selection.
      */
     protected void removeSelection() {
         IStructuredSelection iss = (IStructuredSelection) tableViewer.getSelection();
         String res = (String) iss.getFirstElement();
         selections.remove(res);
         tableViewer.refresh();
     }
 
     /**
      * Removes the all.
      */
     protected void removeAll() {
         int resp = MessageUtil.messageRemoveItems(getShell());
         if (resp == SWT.OK) {
             selections.clear();
             tableViewer.refresh();
         }
     }
 
     /**
      * Adds the classpath.
      */
     protected void addClasspath() {
         ContainerSelectionDialog csd = new ContainerSelectionDialog(getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
                 "Select a src container as classpath");
         int response = csd.open();
         if (response == ContainerSelectionDialog.CANCEL) {
             return;
         }
 
        selections = new ArrayList<String>();

         for (Object o : csd.getResult()) {
             IPath path = (IPath) o;
             String s = ResourcesPlugin.getWorkspace().getRoot().getFolder(path).getLocation().makeAbsolute().toOSString();
             selections.add(s);
         }
 
         tableViewer.setInput(selections);
         tableViewer.refresh();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.jface.preference.PreferencePage#performOk()
      */
     @Override
     public boolean performOk() {
         IJavaElement javaPrj = (IJavaElement) getElement();
 
         try {
             PropertiesUtil.persistClasspathEntries(javaPrj.getResource(), selections);
         } catch (CoreException e) {
             LogUtil.logException(e);
         }
 
         return super.performOk();
     }
 
     /**
      * Load classpaths.
      * 
      * @throws CoreException
      *             the core exception
      */
     private void loadClasspaths() throws CoreException {
         IJavaElement javaPrj = (IJavaElement) getElement();
         selections = PropertiesUtil.getClasspathEntries(javaPrj.getResource());
 
         tableViewer.setInput(selections);
         tableViewer.refresh();
 
     }
 }
