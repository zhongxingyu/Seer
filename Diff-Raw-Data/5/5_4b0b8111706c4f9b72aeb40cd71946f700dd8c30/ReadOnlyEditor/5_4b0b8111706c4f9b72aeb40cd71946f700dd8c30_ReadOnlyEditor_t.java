 package com.versionone.taskview.views.editors;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Text;
 
 public class ReadOnlyEditor extends TextEditor {
 
     public ReadOnlyEditor(String propertyName, TreeViewer viewer) {
         super(propertyName, viewer);
     }
 
     @Override
     protected CellEditor createEditor(TreeViewer viewer) {
         return new ReadOnlyCellEditor(viewer.getTree());
     }
 
     @Override
     protected void setValue(Object element, Object value) {
         // Do nothing
     }
    
    @Override
    protected boolean canEdit(Object element) {   
        return true;
    }
 
     /**
      * This is the cell editor used to edit the Task ID cell. It was created
      * based on the TextCellEditor. The objective here is to only have support
      * for Copy, and nothing else.
      * 
      * @author jerry
      */
     static class ReadOnlyCellEditor extends CellEditor {
 
         protected Text text;
 
         public ReadOnlyCellEditor(Composite parent, int style) {
             super(parent, style);
         }
 
         public ReadOnlyCellEditor(Composite parent) {
             this(parent, SWT.SINGLE);
         }
 
         @Override
         protected Control createControl(Composite parent) {
             text = new Text(parent, getStyle());
             text.setFont(parent.getFont());
             text.setBackground(parent.getBackground());
             text.setEditable(false);
             text.setMenu(createMenu());
             return text;
         }
 
         @Override
         protected Object doGetValue() {
             return text.getText();
         }
 
         @Override
         protected void doSetFocus() {
             if (null != text) {
                 text.selectAll();
                 text.setFocus();
             }
         }
 
         @Override
         protected void doSetValue(Object value) {
             Assert.isTrue(value != null && (value instanceof String));
             text.setText((String) value);
         }
 
         private Menu createMenu() {
             Menu rc = new Menu(text);
             MenuItem copyItem = new MenuItem(rc, SWT.PUSH);
             copyItem.setText("Copy");
             copyItem.addSelectionListener(new SelectionListener() {
 
                 public void widgetDefaultSelected(SelectionEvent e) {
                     text.copy();
                 }
 
                 public void widgetSelected(SelectionEvent e) {
                     text.copy();
                 }
             });
             return rc;
         }
     }
 }
