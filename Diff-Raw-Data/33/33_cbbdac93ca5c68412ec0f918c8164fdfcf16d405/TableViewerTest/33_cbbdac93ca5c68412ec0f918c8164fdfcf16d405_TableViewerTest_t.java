 /**
  *   Copyright 2011 Karl Martens
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *       
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  *
  *   net.karlmartens.ui, is a library of UI widgets
  */
 package net.karlmartens.ui.viewer;
 
 import static net.karlmartens.ui.widget.ClipboardStrategy.OPERATION_COPY;
 import static net.karlmartens.ui.widget.ClipboardStrategy.OPERATION_CUT;
 import static net.karlmartens.ui.widget.ClipboardStrategy.OPERATION_DELETE;
 import static net.karlmartens.ui.widget.ClipboardStrategy.OPERATION_PASTE;
 import static net.karlmartens.ui.widget.ClipboardStrategy.OPERATION_SELECT_ALL;
 import net.karlmartens.platform.util.NumberStringComparator;
 import net.karlmartens.ui.Images;
 import net.karlmartens.ui.dialog.ConfigureColumnsDialog;
 import net.karlmartens.ui.widget.Table;
 
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.ViewerComparator;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.joda.time.LocalDate;
 
 public final class TableViewerTest {
 
   public static void main(String[] args) throws Exception {
     final Image[] images = new Image[] {//
     Images.FILTER.createImage(), //
         Images.COPY.createImage(), //
         Images.CUT.createImage(), //
         Images.PASTE.createImage(), //
     };
 
     final int fixedColumns = 5;
     final Object[][] input = new Object[100][];
     for (int i = 0; i < input.length; i++) {
       input[i] = new Object[20];
       input[i][0] = images[i % 4];
       input[i][1] = "Item " + Integer.toString(i);
       input[i][2] = Boolean.valueOf(i % 3 == 0);
       input[i][3] = TestComboEditingSupport.ITEMS[0 + (i % TestComboEditingSupport.ITEMS.length)];
       input[i][4] = new LocalDate((int) (Math.random() * 50) + 2000,
           (int) (Math.random() * 11) + 1, (int) (Math.random() * 27) + 1);
 
       for (int j = fixedColumns; j < input[i].length; j++) {
         input[i][j] = Integer.toString(Double.valueOf(Math.random() * 10000)
             .intValue());
       }
     }
 
     final Shell shell = new Shell();
     shell.setLayout(GridLayoutFactory.swtDefaults().create());
 
     final Display display = shell.getDisplay();
 
     final Button b = new Button(shell, SWT.PUSH);
     b.setLayoutData(GridDataFactory.swtDefaults().create());
     b.setText("Columns");
 
     final TableViewer viewer = new TableViewer(shell);
     viewer.setContentProvider(new ArrayContentProvider());
     viewer.setComparator(new ViewerComparator(new NumberStringComparator()));
 
     final TableViewerColumn c0 = new TableViewerColumn(viewer, SWT.NONE);
     c0.setLabelProvider(new ColumnLabelProvider() {
       @Override
       public String getText(Object element) {
         return "";
       }
 
       @Override
       public Image getImage(Object element) {
         Object[] arr = (Object[]) element;
         return (Image) arr[0];
       }
     });
     c0.getColumn().setWidth(20);
 
     final TableViewerColumn c1 = new TableViewerColumn(viewer, SWT.NONE);
     c1.setLabelProvider(new TestColumnLabelProvider(1));
    c1.setEditingSupport(new TestTextEditingSupport(viewer, 1, SWT.LEFT));
     c1.getColumn().setText("Name");
     c1.getColumn().setWidth(75);
 
     final TableViewerColumn c2 = new TableViewerColumn(viewer, SWT.CHECK);
     c2.setLabelProvider(new TestColumnLabelProvider(2));
    c2.setEditingSupport(new TestBooleanEditingSupport(viewer, 2));
     c2.getColumn().setText("Active");
     c2.getColumn().setWidth(60);
 
     final TableViewerColumn c3 = new TableViewerColumn(viewer, SWT.LEFT);
     c3.setLabelProvider(new TestColumnLabelProvider(3));
    c3.setEditingSupport(new TestComboEditingSupport(viewer, 3));
     c3.getColumn().setText("Color");
     c3.getColumn().setWidth(60);
 
     final TableViewerColumn c4 = new TableViewerColumn(viewer, SWT.LEFT);
     c4.setLabelProvider(new TestColumnLabelProvider(4));
    c4.setEditingSupport(new TestCalendarComboEditingSupport(viewer, 4));
     c4.getColumn().setText("Date");
     c4.getColumn().setWidth(90);
 
     for (int i = fixedColumns; i < input[0].length; i++) {
       final TableViewerColumn c = new TableViewerColumn(viewer, SWT.RIGHT);
       c.setLabelProvider(new TestColumnLabelProvider(i));
       c.setEditingSupport(new TestTextEditingSupport(viewer, i, SWT.RIGHT));
       c.getColumn().setText("Test " + Integer.toString(i + 1));
       c.getColumn().setWidth(40);
       c.getColumn().setHideable(false);
       c.getColumn().setFilterable(false);
     }
 
     final Table table = viewer.getControl();
     table.setLayoutData(GridDataFactory//
         .fillDefaults()//
         .grab(true, true)//
         .create());
     table.setHeaderVisible(true);
     table.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
     table.setFont(new Font(display, "Arial", 8, SWT.NORMAL));
     table.addColumnSortSupport();
     table.setFixedColumnCount(fixedColumns);
     table.setFixedHeaderColumnCount(1);
     table.setFixedRowCount(3);
 
     final TableViewerClipboardManager clipboardManager = new TableViewerClipboardManager(
         viewer, OPERATION_COPY | OPERATION_CUT | OPERATION_DELETE
             | OPERATION_PASTE | OPERATION_SELECT_ALL);
     clipboardManager.createContextMenu();
 
     viewer.addSelectionChangedListener(new ISelectionChangedListener() {
       @Override
       public void selectionChanged(SelectionChangedEvent event) {
         System.out.println("Selection");
       }
     });
 
     b.addSelectionListener(new SelectionAdapter() {
       @Override
       public void widgetSelected(SelectionEvent e) {
         final ConfigureColumnsDialog dialog = new ConfigureColumnsDialog(shell,
             table, "Title", "header", "message");
         dialog.open();
       }
     });
 
     viewer.setInput(input);
 
     shell.open();
     while (!shell.isDisposed()) {
       if (!display.readAndDispatch()) {
         display.sleep();
       }
     }
   }
 
 }
