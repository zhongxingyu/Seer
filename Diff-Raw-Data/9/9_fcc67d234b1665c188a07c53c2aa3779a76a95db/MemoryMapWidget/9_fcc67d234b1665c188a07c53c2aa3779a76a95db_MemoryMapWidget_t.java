 /*******************************************************************************
  * Copyright (c) 2011, 2013 Wind River Systems, Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.internal.debug.ui.commands;
 
 import java.io.IOException;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.layout.PixelConverter;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jface.viewers.ColumnPixelData;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITableColorProvider;
 import org.eclipse.jface.viewers.ITableFontProvider;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.TableLayout;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.tcf.core.ErrorReport;
 import org.eclipse.tcf.internal.debug.launch.TCFLaunchDelegate;
 import org.eclipse.tcf.internal.debug.launch.TCFLaunchDelegate.PathMapRule;
 import org.eclipse.tcf.internal.debug.model.TCFMemoryRegion;
 import org.eclipse.tcf.internal.debug.model.TCFSymFileRef;
 import org.eclipse.tcf.internal.debug.ui.Activator;
 import org.eclipse.tcf.internal.debug.ui.ColorCache;
 import org.eclipse.tcf.internal.debug.ui.ImageCache;
 import org.eclipse.tcf.internal.debug.ui.launch.PathMapRuleDialog;
 import org.eclipse.tcf.internal.debug.ui.model.TCFChildren;
 import org.eclipse.tcf.internal.debug.ui.model.TCFModel;
 import org.eclipse.tcf.internal.debug.ui.model.TCFNode;
 import org.eclipse.tcf.internal.debug.ui.model.TCFNodeExecContext;
 import org.eclipse.tcf.internal.debug.ui.model.TCFNodeLaunch;
 import org.eclipse.tcf.protocol.IChannel;
 import org.eclipse.tcf.protocol.JSON;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.services.IMemory;
 import org.eclipse.tcf.services.IMemoryMap;
 import org.eclipse.tcf.services.IPathMap;
 import org.eclipse.tcf.util.TCFDataCache;
 import org.eclipse.tcf.util.TCFTask;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 
 public class MemoryMapWidget {
 
 
     private static final int
         SIZING_TABLE_WIDTH = 500,
         SIZING_TABLE_HEIGHT = 300;
 
     private static final String[] column_names = {
         "File",
         "Address",
         "Size",
         "Flags",
         "File offset/section",
     };
 
     private TCFModel model;
     private IChannel channel;
     private TCFNode selection;
     private final IMemoryMap.MemoryMapListener listener = new MemoryMapListener();
 
     private Combo ctx_text;
     private Table map_table;
     private TableViewer table_viewer;
     private Runnable update_map_buttons;
     private final Map<String,ArrayList<IMemoryMap.MemoryRegion>> org_maps = new HashMap<String,ArrayList<IMemoryMap.MemoryRegion>>();
     private final Map<String,ArrayList<IMemoryMap.MemoryRegion>> cur_maps = new HashMap<String,ArrayList<IMemoryMap.MemoryRegion>>();
     private final ArrayList<IMemoryMap.MemoryRegion> target_map = new ArrayList<IMemoryMap.MemoryRegion>();
     private final HashMap<String,TCFNodeExecContext> target_map_nodes = new HashMap<String,TCFNodeExecContext>();
     private TCFNodeExecContext selected_mem_map_node;
     private IMemory.MemoryContext mem_ctx;
     private ILaunchConfiguration cfg;
     private final HashSet<String> loaded_files = new HashSet<String>();
     private String selected_mem_map_id;
     private final ArrayList<ModifyListener> modify_listeners = new ArrayList<ModifyListener>();
 
     private Color cError = null;
     private boolean disposed = false;
 
     private final IStructuredContentProvider content_provider = new IStructuredContentProvider() {
 
         public Object[] getElements(Object input) {
             ArrayList<IMemoryMap.MemoryRegion> res = new ArrayList<IMemoryMap.MemoryRegion>();
             ArrayList<IMemoryMap.MemoryRegion> lst = cur_maps.get((String)input);
             if (lst != null) res.addAll(lst);
             res.addAll(target_map);
             return res.toArray();
         }
 
         public void dispose() {
         }
 
         public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
         }
     };
 
     private class MapLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider, ITableFontProvider {
 
         public Image getColumnImage(Object element, int column) {
             return null;
         }
 
         public String getColumnText(Object element, int column) {
             TCFMemoryRegion r = (TCFMemoryRegion)element;
             switch (column) {
             case 0:
                 return r.getFileName();
             case 1:
             case 2:
                 {
                     BigInteger x = column == 1 ? r.addr : r.size;
                     if (x == null) return "";
                     String s = x.toString(16);
                     int sz = 0;
                     if (mem_ctx != null) sz = mem_ctx.getAddressSize() * 2;
                     int l = sz - s.length();
                     if (l < 0) l = 0;
                     if (l > 16) l = 16;
                     return "0x0000000000000000".substring(0, 2 + l) + s;
                 }
             case 3:
                 {
                     int n = r.getFlags();
                     StringBuffer bf = new StringBuffer();
                     if ((n & IMemoryMap.FLAG_READ) != 0) bf.append('r'); else bf.append('-');
                     if ((n & IMemoryMap.FLAG_WRITE) != 0) bf.append('w'); else bf.append('-');
                     if ((n & IMemoryMap.FLAG_EXECUTE) != 0) bf.append('x'); else bf.append('-');
                     return bf.toString();
                 }
             case 4:
                 {
                     Number n = r.getOffset();
                     if (n != null) {
                         BigInteger x = JSON.toBigInteger(n);
                         String s = x.toString(16);
                         int l = 16 - s.length();
                         if (l < 0) l = 0;
                         if (l > 16) l = 16;
                         return "0x0000000000000000".substring(0, 2 + l) + s;
                     }
                     String s = r.getSectionName();
                     if (s != null) return s;
                     return "";
                 }
             }
             return "";
         }
 
         public Color getBackground(Object element, int columnIndex) {
             return map_table.getBackground();
         }
 
         public Color getForeground(Object element, int columnIndex) {
             TCFMemoryRegion r = (TCFMemoryRegion)element;
             if (r.getProperties().get(IMemoryMap.PROP_ID) != null) {
                 String fnm = r.getFileName();
                 if (fnm != null && loaded_files.contains(fnm)) {
                     return map_table.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN);
                 }
                 return map_table.getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE);
             }
 
             String symbolFileInfo = getSymbolFileInfo(r);
             // Set or reset the symbol file error tooltip marker
             TableItem[] items = map_table.getItems();
             for (TableItem item : items) {
                 if (item.getData() != null && item.getData().equals(r)) {
                     item.setData("_TOOLTIP", symbolFileInfo); //$NON-NLS-1$
                 }
             }
             if (symbolFileInfo != null && symbolFileInfo.contains("Symbol file error:") && cError != null) {
                 return cError;
             }
 
             return map_table.getForeground();
         }
 
         /* (non-Javadoc)
          * @see org.eclipse.jface.viewers.ITableFontProvider#getFont(java.lang.Object, int)
          */
         @Override
         public Font getFont(Object element, int columnIndex) {
             switch (columnIndex) {
                 case 1:
                 case 2:
                 case 4:
                     return JFaceResources.getFontRegistry().get(JFaceResources.TEXT_FONT);
             }
             return null;
         }
 
         public String getText(Object element) {
             return element.toString();
         }
     }
 
     private class MemoryMapListener implements IMemoryMap.MemoryMapListener {
 
         /* (non-Javadoc)
          * @see org.eclipse.tcf.services.IMemoryMap.MemoryMapListener#changed(java.lang.String)
          */
         @Override
         public void changed(String context_id) {
             // If the widget is already disposed but the listener is still invoked,
             // remove the listener itself from the memory map service.
             if (disposed) {
                 if (channel != null) {
                     IMemoryMap svc = channel.getRemoteService(IMemoryMap.class);
                     if (svc != null) svc.removeListener(this);
                 }
                 return;
             }
 
             if (mem_ctx != null && mem_ctx.getID() != null && mem_ctx.getID().equals(context_id)) {
                 if (cfg != null && PlatformUI.getWorkbench().getDisplay() != null && !PlatformUI.getWorkbench().getDisplay().isDisposed()) {
                     final ILaunchConfiguration lc = cfg;
                     PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                         @Override
                         public void run() {
                             if (!disposed) loadData(lc);
                         }
                     });
                 }
             }
         }
     }
 
     public MemoryMapWidget(Composite composite, TCFNode node) {
         setTCFNode(node);
         createContextText(composite);
         createMemoryMapTable(composite);
 
         cError = new Color(composite.getDisplay(), ColorCache.rgb_error);
     }
 
     /**
      * Dispose the widget and cleanup the created resources and listeners.
      */
     public void dispose() {
         if (disposed) return;
         disposed = true;
 
         if (cError != null) {
             cError.dispose();
             cError = null;
         }
 
         // Remove the memory map listener
         if (channel != null) {
             // Asynchronous execution. Make a copy of the current channel reference.
             final IChannel c = channel;
             Protocol.invokeLater(new Runnable() {
                 @Override
                 public void run() {
                     IMemoryMap svc = c.getRemoteService(IMemoryMap.class);
                     if (svc != null) svc.removeListener(listener);
                 }
             });
         }
     }
 
     public boolean setTCFNode(TCFNode node) {
         if (node == null && selection == null || node != null && node.equals(selection)) {
             return false;
         }
 
         // Remove the memory map listener from the current channel
         // before setting the variable to the new channel
         if (channel != null && channel.getState() == IChannel.STATE_OPEN) {
             // Asynchronous execution. Make a copy of the current channel reference.
             final IChannel c = channel;
             Protocol.invokeLater(new Runnable() {
                 @Override
                 public void run() {
                     IMemoryMap svc = c.getRemoteService(IMemoryMap.class);
                     if (svc != null) svc.removeListener(listener);
                 }
             });
         }
 
         if (node != null) {
             model = node.getModel();
             channel = node.getChannel();
             selection = node;
 
             // Register the memory map listener
             if (channel != null && channel.getState() == IChannel.STATE_OPEN) {
                 // Asynchronous execution. Make a copy of the current channel reference.
                 final IChannel c = channel;
                 Protocol.invokeLater(new Runnable() {
                     @Override
                     public void run() {
                         IMemoryMap svc = c.getRemoteService(IMemoryMap.class);
                         if (svc != null) svc.addListener(listener);
                     }
                 });
             }
         }
         else {
             model = null;
             channel = null;
             selection = null;
         }
         return true;
     }
 
     public String getMemoryMapID() {
         return selected_mem_map_id;
     }
 
     private void createContextText(Composite parent) {
         Font font = parent.getFont();
         Composite composite = new Composite(parent, SWT.NONE);
         GridLayout layout = new GridLayout(2, false);
         layout.marginHeight = 0;
         layout.marginWidth = 0;
         composite.setFont(font);
         composite.setLayout(layout);
         composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
         Label props_label = new Label(composite, SWT.WRAP);
         props_label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
         props_label.setFont(font);
         props_label.setText("&Debug context:");
 
         ctx_text = new Combo(composite, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
         ctx_text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
         ctx_text.setFont(font);
         ctx_text.addModifyListener(new ModifyListener() {
             public void modifyText(ModifyEvent e) {
                 selected_mem_map_id = ctx_text.getText();
                 selected_mem_map_node = target_map_nodes.get(selected_mem_map_id);
                 loadTargetMemoryMap();
                 table_viewer.setInput(selected_mem_map_id);
                 if (selected_mem_map_id.length() == 0) selected_mem_map_id = null;
                 update_map_buttons.run();
             }
         });
     }
 
     private void createMemoryMapTable(Composite parent) {
         Font font = parent.getFont();
 
         Composite composite = new Composite(parent, SWT.NONE);
         GridLayout layout = new GridLayout(2, false);
         layout.marginHeight = 0;
         layout.marginWidth = 0;
         composite.setFont(font);
         composite.setLayout(layout);
         composite.setLayoutData(new GridData(GridData.FILL_BOTH));
 
         map_table = new Table(composite,
                 SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION |
                 SWT.H_SCROLL | SWT.V_SCROLL);
 
         map_table.setFont(font);
         configureTable(map_table);
 
         map_table.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 IMemoryMap.MemoryRegion r = (IMemoryMap.MemoryRegion)((IStructuredSelection)table_viewer.getSelection()).getFirstElement();
                 if (r == null) return;
                 editRegion(r);
             }
             @Override
             public void widgetSelected(SelectionEvent e) {
                 update_map_buttons.run();
             }
         });
 
         table_viewer = new TableViewer(map_table);
         table_viewer.setUseHashlookup(true);
         table_viewer.setColumnProperties(column_names);
         table_viewer.setContentProvider(content_provider);
         table_viewer.setLabelProvider(new MapLabelProvider());
 
         map_table.pack();
 
         createMapButtons(composite);
     }
 
     protected String getColumnText(int column) {
         if (column < column_names.length && column >= 0)
             return column_names[column];
         return ""; //$NON-NLS-1$
     }
 
     protected void configureTable(final Table table) {
         GridData data = new GridData(GridData.FILL_BOTH);
         data.widthHint = SIZING_TABLE_WIDTH;
         data.heightHint = SIZING_TABLE_HEIGHT;
         table.setLayoutData(data);
 
         final TableColumn colFile = new TableColumn(table, 0);
         colFile.setResizable(true);
         colFile.setAlignment(SWT.LEFT);
         colFile.setText(getColumnText(0));
 
         final TableColumn colAddr = new TableColumn(table, 1);
         colAddr.setResizable(true);
         colAddr.setAlignment(SWT.LEFT);
         colAddr.setText(getColumnText(1));
 
         final TableColumn colSize = new TableColumn(table, 2);
         colSize.setResizable(true);
         colSize.setAlignment(SWT.LEFT);
         colSize.setText(getColumnText(2));
 
         final TableColumn colFlags = new TableColumn(table, 3);
         colFlags.setResizable(true);
         colFlags.setAlignment(SWT.LEFT);
         colFlags.setText(getColumnText(3));
 
         final TableColumn colOffset = new TableColumn(table, 4);
         colOffset.setResizable(true);
         colOffset.setAlignment(SWT.LEFT);
         colOffset.setText(getColumnText(4));
 
 
         TableLayout layout = new TableLayout();
         layout.addColumnData(new ColumnPixelData(150));
         layout.addColumnData(new ColumnPixelData(90));
         layout.addColumnData(new ColumnPixelData(90));
         layout.addColumnData(new ColumnPixelData(50));
         layout.addColumnData(new ColumnPixelData(140));
 
         table.addListener(SWT.Resize, new Listener() {
                 @Override
                 public void handleEvent(Event event) {
                     int width = table.getSize().x - 4 - colAddr.getWidth() - colSize.getWidth() - colFlags.getWidth() - colOffset.getWidth();
                     colFile.setWidth(Math.max(width, 100));
                 }
         });
 
         colFile.addListener(SWT.Resize, new Listener() {
                 @Override
                 public void handleEvent(Event event) {
                     int colWidth = colFile.getWidth();
                     if (colWidth < 100) {
                         event.doit = false;
                         colFile.setWidth(100);
                     }
                 }
         });
 
         Listener listener = new Listener() {
             @Override
             public void handleEvent(Event event) {
                 if (event.widget instanceof TableColumn) {
                     TableColumn col = (TableColumn)event.widget;
                     int colWidth = col.getWidth();
                     if (colWidth < 40) {
                         event.doit = false;
                         col.setWidth(40);
                     }
                 }
             }
         };
         colAddr.addListener(SWT.Resize, listener);
         colSize.addListener(SWT.Resize, listener);
         colFlags.addListener(SWT.Resize, listener);
         colOffset.addListener(SWT.Resize, listener);
 
         // "Symbol File Errors" are displayed as tooltip on the table item.
         // See http://git.eclipse.org/c/platform/eclipse.platform.swt.git/tree/examples/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet125.java.
 
         // Disable native tooltip
         table.setToolTipText ("");
 
         // Implement a "fake" tooltip
         final Listener labelListener = new Listener () {
             public void handleEvent (Event event) {
                 Label label = (Label)event.widget;
                 Shell shell = label.getShell ();
                 switch (event.type) {
                 case SWT.MouseDown:
                     Event e = new Event ();
                     e.item = (TableItem) label.getData ("_TABLEITEM");
                     // Assuming table is single select, set the selection as if
                     // the mouse down event went through to the table
                     table.setSelection (new TableItem [] {(TableItem) e.item});
                     table.notifyListeners (SWT.Selection, e);
                     shell.dispose ();
                     table.setFocus();
                     break;
                 case SWT.MouseExit:
                     shell.dispose ();
                     break;
                 }
             }
         };
 
         Listener tableListener = new Listener () {
             Shell tip = null;
             Label label = null;
             public void handleEvent (Event event) {
                 switch (event.type) {
                 case SWT.Dispose:
                 case SWT.KeyDown:
                 case SWT.MouseMove: {
                     if (tip == null) break;
                     tip.dispose ();
                     tip = null;
                     label = null;
                     break;
                 }
                 case SWT.MouseHover: {
                     TableItem item = table.getItem (new Point (event.x, event.y));
                     if (item != null && item.getData("_TOOLTIP") instanceof String) {
                         if (tip != null  && !tip.isDisposed ()) tip.dispose ();
                         tip = new Shell (table.getShell(), SWT.ON_TOP | SWT.NO_FOCUS | SWT.TOOL);
                         tip.setBackground (table.getDisplay().getSystemColor (SWT.COLOR_INFO_BACKGROUND));
                         FillLayout layout = new FillLayout ();
                         layout.marginWidth = 2;
                         tip.setLayout (layout);
                         label = new Label (tip, SWT.NONE);
                         label.setForeground (table.getDisplay().getSystemColor (SWT.COLOR_INFO_FOREGROUND));
                         label.setBackground (table.getDisplay().getSystemColor (SWT.COLOR_INFO_BACKGROUND));
                         label.setData ("_TABLEITEM", item);
                         label.setText ((String)item.getData("_TOOLTIP"));
                         label.addListener (SWT.MouseExit, labelListener);
                         label.addListener (SWT.MouseDown, labelListener);
                         Point size = tip.computeSize (SWT.DEFAULT, SWT.DEFAULT);
                         Point pt = table.toDisplay (event.x - 20, event.y - size.y);
                         tip.setBounds (pt.x, pt.y, size.x, size.y);
                         tip.setVisible (true);
                     }
                 }
                 }
             }
         };
         table.addListener (SWT.Dispose, tableListener);
         table.addListener (SWT.KeyDown, tableListener);
         table.addListener (SWT.MouseMove, tableListener);
         table.addListener (SWT.MouseHover, tableListener);
 
         table.setLayout(layout);
         table.setHeaderVisible(true);
         table.setLinesVisible(true);
     }
 
     protected final TableViewer getViewer() {
         return table_viewer;
     }
 
     private void createMapButtons(Composite parent) {
         Font font = parent.getFont();
         Composite composite = new Composite(parent, SWT.NONE);
         GridLayout layout = new GridLayout();
         layout.marginHeight = 0;
         layout.marginWidth = 0;
         composite.setFont(font);
         composite.setLayout(layout);
         composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
         Menu menu = new Menu(map_table);
         SelectionAdapter sel_adapter = null;
 
         final Button button_add = new Button(composite, SWT.PUSH);
         button_add.setText(" &Add... ");
         GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
         PixelConverter converter= new PixelConverter(button_add);
         gd.widthHint = converter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
         button_add.setLayoutData(gd);
         button_add.addSelectionListener(sel_adapter = new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 String id = ctx_text.getText();
                 if (id == null || id.length() == 0) return;
                 Map<String,Object> props = new HashMap<String,Object>();
                 Image image = ImageCache.getImage(ImageCache.IMG_MEMORY_MAP);
                 if (new MemoryMapItemDialog(map_table.getShell(), image, props, true).open() == Window.OK) {
                     props.put(IMemoryMap.PROP_ID, id);
                     ArrayList<IMemoryMap.MemoryRegion> lst = cur_maps.get(id);
                     if (lst == null) cur_maps.put(id, lst = new ArrayList<IMemoryMap.MemoryRegion>());
                     lst.add(new TCFMemoryRegion(props));
                     table_viewer.refresh();
                     notifyModifyListeners();
                 }
             }
         });
         final MenuItem item_add = new MenuItem(menu, SWT.PUSH);
         item_add.setText("&Add...");
         item_add.addSelectionListener(sel_adapter);
         item_add.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ADD));
 
         final Button button_edit = new Button(composite, SWT.PUSH);
         button_edit.setText(" E&dit... ");
         button_edit.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
         button_edit.addSelectionListener(sel_adapter = new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 IMemoryMap.MemoryRegion r = (IMemoryMap.MemoryRegion)((IStructuredSelection)table_viewer.getSelection()).getFirstElement();
                 if (r == null) return;
                 editRegion(r);
             }
         });
         final MenuItem item_edit = new MenuItem(menu, SWT.PUSH);
         item_edit.setText("E&dit...");
         item_edit.addSelectionListener(sel_adapter);
 
         final Button button_remove = new Button(composite, SWT.PUSH);
         button_remove.setText(" &Remove ");
         button_remove.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
         button_remove.addSelectionListener(sel_adapter = new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 String id = ctx_text.getText();
                 if (id == null || id.length() == 0) return;
                 IMemoryMap.MemoryRegion r = (IMemoryMap.MemoryRegion)((IStructuredSelection)table_viewer.getSelection()).getFirstElement();
                 if (r == null) return;
                 ArrayList<IMemoryMap.MemoryRegion> lst = cur_maps.get(id);
                 if (lst != null && lst.remove(r)) table_viewer.refresh();
                 notifyModifyListeners();
             }
         });
         final MenuItem item_remove = new MenuItem(menu, SWT.PUSH);
         item_remove.setText("&Remove");
         item_remove.addSelectionListener(sel_adapter);
         item_remove.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ETOOL_DELETE));
 
         final Button button_locate = new Button(composite, SWT.PUSH | SWT.WRAP);
         button_locate.setText(" Locate Symbol File... ");
         GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
         layoutData.widthHint = 50;
         button_locate.setLayoutData(layoutData);
         button_locate.addSelectionListener(sel_adapter = new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 String id = ctx_text.getText();
                 if (id == null || id.length() == 0) return;
                 IMemoryMap.MemoryRegion r = (IMemoryMap.MemoryRegion)((IStructuredSelection)table_viewer.getSelection()).getFirstElement();
                 if (r == null) return;
                 locateSymbolFile(r);
             }
         });
         new MenuItem(menu, SWT.SEPARATOR);
         final MenuItem item_locate = new MenuItem(menu, SWT.PUSH);
         item_locate.setText("Locate Symbol File...");
         item_locate.addSelectionListener(sel_adapter);
 
         map_table.setMenu(menu);
 
         update_map_buttons = new Runnable() {
             public void run() {
                 IMemoryMap.MemoryRegion r = (IMemoryMap.MemoryRegion)((IStructuredSelection)table_viewer.getSelection()).getFirstElement();
                 boolean manual = r != null && r.getProperties().get(IMemoryMap.PROP_ID) != null;
                 button_add.setEnabled(selected_mem_map_id != null);
                 button_edit.setEnabled(r != null);
                 button_remove.setEnabled(manual);
                 item_add.setEnabled(selected_mem_map_id != null);
                 item_edit.setEnabled(r != null);
                 item_remove.setEnabled(manual);
                 String symbolFileInfo = getSymbolFileInfo(r);
                 boolean enabled = symbolFileInfo != null && symbolFileInfo.contains("Symbol file error:")
                                         && r.getFileName() != null && r.getFileName().lastIndexOf('/') != -1;
                 button_locate.setEnabled(enabled);
                 item_locate.setEnabled(enabled);
             }
         };
         update_map_buttons.run();
     }
 
     private void editRegion(IMemoryMap.MemoryRegion r) {
         String id = ctx_text.getText();
         if (id == null || id.length() == 0) return;
         Map<String,Object> props = r.getProperties();
         boolean enable_editing = props.get(IMemoryMap.PROP_ID) != null;
         if (enable_editing) props = new HashMap<String,Object>(props);
         Image image = ImageCache.getImage(ImageCache.IMG_MEMORY_MAP);
         if (new MemoryMapItemDialog(map_table.getShell(), image, props, enable_editing).open() == Window.OK && enable_editing) {
             ArrayList<IMemoryMap.MemoryRegion> lst = cur_maps.get(id);
             if (lst != null) {
                 for (int n = 0; n < lst.size(); n++) {
                     if (lst.get(n) == r) {
                         lst.set(n, new TCFMemoryRegion(props));
                         table_viewer.refresh();
                         notifyModifyListeners();
                     }
                 }
             }
         }
     }
 
     private void locateSymbolFile(IMemoryMap.MemoryRegion r) {
         Assert.isNotNull(r);
 
         Map<String,Object> props = new HashMap<String,Object>(r.getProperties());
         Image image = ImageCache.getImage(ImageCache.IMG_MEMORY_MAP);
 
         // Create the new path map rule
         Map<String, Object> properties = new LinkedHashMap<String, Object>();
         String fileName = (String)props.get(IMemoryMap.PROP_FILE_NAME);
         if (fileName == null || fileName.lastIndexOf('/') == -1) return;
         properties.put(IPathMap.PROP_SOURCE, fileName.lastIndexOf('/') + 1 == fileName.length() ? fileName : fileName.substring(0, fileName.lastIndexOf('/') + 1));
         PathMapRule rule = new PathMapRule(properties);
 
         if (new PathMapRuleDialog(map_table.getShell(), image, rule, true, false).open() == Window.OK) {
             String source = rule.getSource();
             String destination = rule.getDestination();
             if (source != null && source.trim().length() > 0 && destination != null && destination.trim().length() > 0) {
                 if (cfg != null) {
                     try {
                         ILaunchConfigurationWorkingCopy wc = cfg instanceof ILaunchConfigurationWorkingCopy ? (ILaunchConfigurationWorkingCopy)cfg : cfg.getWorkingCopy();
 
                         String s = wc.getAttribute(TCFLaunchDelegate.ATTR_PATH_MAP, ""); //$NON-NLS-1$
                         List<PathMapRule> map = TCFLaunchDelegate.parsePathMapAttribute(s);
 
                         map.add(0, rule);
 
                         StringBuilder bf = new StringBuilder();
                         for (IPathMap.PathMapRule m : map) {
                             bf.append(m.toString());
                         }
                         if (bf.length() == 0)
                             wc.removeAttribute(TCFLaunchDelegate.ATTR_PATH_MAP);
                         else
                             wc.setAttribute(TCFLaunchDelegate.ATTR_PATH_MAP, bf.toString());
 
                         if (wc.isDirty()) wc.doSave();
                     } catch (CoreException e) {
                         Activator.getDefault().getLog().log(e.getStatus());
                     }
                 }
             }
         }
     }
 
     private void readMemoryMapAttribute() {
         cur_maps.clear();
         try {
             new TCFTask<Boolean>() {
                 public void run() {
                     try {
                         TCFLaunchDelegate.getMemMapsAttribute(cur_maps, cfg);
                         done(true);
                     }
                     catch (Exception e) {
                         error(e);
                     }
                 }
             }.get();
         }
         catch (Exception x) {
             Activator.log("Invalid launch cofiguration attribute", x);
         }
     }
 
     private void writeMemoryMapAttribute(ILaunchConfigurationWorkingCopy copy) throws Exception {
         String s = null;
         final ArrayList<Map<String,Object>> lst = new ArrayList<Map<String,Object>>();
         for (ArrayList<IMemoryMap.MemoryRegion> x : cur_maps.values()) {
             for (IMemoryMap.MemoryRegion r : x) lst.add(r.getProperties());
         }
         if (lst.size() > 0) {
             s = new TCFTask<String>() {
                 public void run() {
                     try {
                         done(JSON.toJSON(lst));
                     }
                     catch (IOException e) {
                         error(e);
                     }
                 }
             }.getIO();
         }
         copy.setAttribute(TCFLaunchDelegate.ATTR_MEMORY_MAP, s);
     }
 
     public void loadData(ILaunchConfiguration cfg) {
         this.cfg = cfg;
         cur_maps.clear();
         org_maps.clear();
         loadTargetMemoryNodes();
         readMemoryMapAttribute();
         for (String id : cur_maps.keySet()) {
             org_maps.put(id, new ArrayList<IMemoryMap.MemoryRegion>(cur_maps.get(id)));
         }
         // Update controls
         String map_id = getSelectedMemoryNode();
         HashSet<String> ids = new HashSet<String>(target_map_nodes.keySet());
         if (map_id != null) ids.add(map_id);
         ids.addAll(cur_maps.keySet());
         String[] arr = ids.toArray(new String[ids.size()]);
         Arrays.sort(arr);
         ctx_text.removeAll();
         for (String id : arr) ctx_text.add(id);
         if (map_id == null && arr.length > 0) map_id = arr[0];
         if (map_id == null) map_id = "";
         ctx_text.setText(map_id);
     }
 
     private String getSelectedMemoryNode() {
         if (channel == null || channel.getState() != IChannel.STATE_OPEN) return null;
         try {
             return new TCFTask<String>(channel) {
                 public void run() {
                     TCFDataCache<TCFNodeExecContext> mem_cache = model.searchMemoryContext(selection);
                     if (mem_cache == null) {
                         error(new Exception("Context does not provide memory access"));
                         return;
                     }
                     if (!mem_cache.validate(this)) return;
                     if (mem_cache.getError() != null) {
                         error(mem_cache.getError());
                         return;
                     }
                     String id = null;
                     TCFNodeExecContext mem_node = mem_cache.getData();
                     if (mem_node != null) {
                         TCFDataCache<TCFNodeExecContext> syms_cache = mem_node.getSymbolsNode();
                         if (!syms_cache.validate(this)) return;
                         TCFNodeExecContext syms_node = syms_cache.getData();
                         if (syms_node != null) {
                             TCFDataCache<IMemory.MemoryContext> mem_ctx = syms_node.getMemoryContext();
                             if (!mem_ctx.validate(this)) return;
                             if (mem_ctx.getData() != null) {
                                 if (syms_node.getModel().getLaunch().isMemoryMapPreloadingSupported()) {
                                     TCFDataCache<String> name_cache = syms_node.getFullName();
                                     if (!name_cache.validate(this)) return;
                                     id = name_cache.getData();
                                 }
                                 else {
                                     id = mem_ctx.getData().getName();
                                 }
                                 if (id == null) id = syms_node.getID();
                             }
                         }
                     }
                     done(id);
                 }
             }.get();
         }
         catch (Exception x) {
             if (channel.getState() != IChannel.STATE_OPEN) return null;
             // Don't log error. This is expected if the selected node has no containing memory context
             // Activator.log("Cannot get selected memory node", x);
             return null;
         }
     }
 
     private String getSymbolFileInfo(final IMemoryMap.MemoryRegion r) {
        if (channel == null || channel.getState() != IChannel.STATE_OPEN || r == null || r.getAddress() == null) return null;
         try {
             return new TCFTask<String>(channel) {
                 public void run() {
                     TCFDataCache<TCFNodeExecContext> mem_cache = model.searchMemoryContext(selected_mem_map_node);
                     if (mem_cache == null) {
                         error(new Exception("Context does not provide memory access"));
                         return;
                     }
                     if (!mem_cache.validate(this)) return;
                     if (mem_cache.getError() != null) {
                         error(mem_cache.getError());
                         return;
                     }
                     StringBuilder symbolFileInfo = new StringBuilder();
                     TCFNodeExecContext mem_node = mem_cache.getData();
                     if (mem_node != null) {
                         TCFDataCache<TCFSymFileRef> sym_cache = mem_node.getSymFileInfo(JSON.toBigInteger(r.getAddress()));
                         if (sym_cache != null) {
                             if (!sym_cache.validate(this)) return;
                             TCFSymFileRef sym_data = sym_cache.getData();
                             if (sym_data != null) {
                                 if (sym_data.props != null) {
                                     String sym_file_name = (String)sym_data.props.get("FileName");
                                     if (sym_file_name != null && !sym_file_name.equals(r.getFileName())) symbolFileInfo.append("Symbol file name: ").append(sym_file_name);
 
                                     @SuppressWarnings("unchecked")
                                     Map<String,Object> map = (Map<String,Object>)sym_data.props.get("FileError");
                                     if (map != null) {
                                         if (symbolFileInfo.length() > 0) symbolFileInfo.append("\n");
                                         symbolFileInfo.append("Symbol file error: ").append(TCFModel.getErrorMessage(new ErrorReport("", map), false));
                                     }
                                 }
                                 if (sym_data.error != null) {
                                     symbolFileInfo.append("Symbol file error: ").append(TCFModel.getErrorMessage(sym_data.error, false));
                                }
                             }
                         }
                     }
                     done(symbolFileInfo.length() > 0 ? symbolFileInfo.toString() : null);
                 }
             }.get();
         }
         catch (Exception x) {
             if (channel.getState() != IChannel.STATE_OPEN) return null;
             Activator.log("Cannot get selected symbol file info", x);
             return null;
         }
     }
 
     private void loadTargetMemoryNodes() {
         target_map_nodes.clear();
         if (channel == null || channel.getState() != IChannel.STATE_OPEN) return;
         try {
             new TCFTask<Boolean>(channel) {
                 public void run() {
                     TCFNodeLaunch n = model.getRootNode();
                     if (!collectMemoryNodes(n.getFilteredChildren())) return;
                     done(true);
                 }
                 private boolean collectMemoryNodes(TCFChildren children) {
                     if (!children.validate(this)) return false;
                     Map<String,TCFNode> m = children.getData();
                     if (m != null) {
                         for (TCFNode n : m.values()) {
                             if (n instanceof TCFNodeExecContext) {
                                 TCFNodeExecContext exe = (TCFNodeExecContext)n;
                                 if (!collectMemoryNodes(exe.getChildren())) return false;
                                 TCFDataCache<TCFNodeExecContext> syms_cache = exe.getSymbolsNode();
                                 if (!syms_cache.validate(this)) return false;
                                 TCFNodeExecContext syms_node = syms_cache.getData();
                                 if (syms_node != null) {
                                     TCFDataCache<IMemory.MemoryContext> mem_ctx = syms_node.getMemoryContext();
                                     if (!mem_ctx.validate(this)) return false;
                                     if (mem_ctx.getData() != null) {
                                         String id = null;
                                         if (syms_node.getModel().getLaunch().isMemoryMapPreloadingSupported()) {
                                             TCFDataCache<String> name_cache = syms_node.getFullName();
                                             if (!name_cache.validate(this)) return false;
                                             id = name_cache.getData();
                                         }
                                         else {
                                             id = mem_ctx.getData().getName();
                                         }
                                         if (id == null) id = syms_node.getID();
                                         target_map_nodes.put(id, syms_node);
                                     }
                                 }
                             }
                         }
                     }
                     return true;
                 }
             }.get();
         }
         catch (Exception x) {
             if (channel.getState() != IChannel.STATE_OPEN) return;
             Activator.log("Cannot load target memory context info", x);
         }
     }
 
     private boolean isLocalEntry(IMemoryMap.MemoryRegion r) {
         /* Check if launch configuration contains the entry */
         if (r == null) return false;
         String f0 = r.getFileName();
         BigInteger a0 = JSON.toBigInteger(r.getAddress());
         ArrayList<IMemoryMap.MemoryRegion> map = cur_maps.get(selected_mem_map_id);
         if (map == null) return false;
         for (IMemoryMap.MemoryRegion c : map) {
             String f1 = c.getFileName();
             if (f0 != f1) {
                 if (f0 == null) continue;
                 if (!f0.equals(f1)) continue;
             }
             Number a1 = c.getAddress();
             if (a0 != a1) {
                 if (a0 == null) continue;
             }
             return true;
         }
         return false;
     }
 
     private void loadTargetMemoryMap() {
         loaded_files.clear();
         target_map.clear();
         mem_ctx = null;
         if (channel == null || channel.getState() != IChannel.STATE_OPEN) return;
         try {
             new TCFTask<Boolean>(channel) {
                 public void run() {
                     if (selected_mem_map_node != null && !selected_mem_map_node.isDisposed()) {
                         TCFDataCache<IMemory.MemoryContext> mem_cache = selected_mem_map_node.getMemoryContext();
                         if (!mem_cache.validate(this)) return;
                         if (mem_cache.getError() != null) {
                             error(mem_cache.getError());
                             return;
                         }
                         mem_ctx = mem_cache.getData();
                         TCFDataCache<TCFNodeExecContext.MemoryRegion[]> map_cache = selected_mem_map_node.getMemoryMap();
                         if (!map_cache.validate(this)) return;
                         if (map_cache.getError() != null) {
                             error(map_cache.getError());
                             return;
                         }
                         if (map_cache.getData() != null) {
                             for (TCFNodeExecContext.MemoryRegion m : map_cache.getData()) {
                                 if (isLocalEntry(m.region)) {
                                     String fnm = m.region.getFileName();
                                     if (fnm != null) loaded_files.add(fnm);
                                 }
                                 else {
                                     /* Foreign entry, added by another client or by the target itself */
                                     target_map.add(new TCFMemoryRegion(m.region.getProperties()));
                                 }
                             }
                         }
                     }
                     done(true);
                 }
             }.get();
         }
         catch (Exception x) {
             if (channel.getState() != IChannel.STATE_OPEN) return;
             Activator.log("Cannot load target memory map", x);
         }
     }
 
     public boolean saveData(ILaunchConfigurationWorkingCopy copy) throws Exception {
         boolean loaded_files_ok = true;
         if (selected_mem_map_id != null) {
             ArrayList<IMemoryMap.MemoryRegion> lst = cur_maps.get(selected_mem_map_id);
             if (lst != null) {
                 for (IMemoryMap.MemoryRegion r : lst) {
                     String fnm = r.getFileName();
                     if (fnm != null && !loaded_files.contains(fnm)) loaded_files_ok = false;
                 }
                 if (lst.size() == 0) cur_maps.remove(selected_mem_map_id);
             }
         }
         if (!loaded_files_ok || !org_maps.equals(cur_maps)) {
             writeMemoryMapAttribute(copy);
             return true;
         }
         return false;
     }
 
     public void addModifyListener(ModifyListener l) {
         modify_listeners.add(l);
     }
 
     private void notifyModifyListeners() {
         for (ModifyListener l : modify_listeners) l.modifyText(null);
     }
 }
