 /*******************************************************************************
  * Copyright (c) 2013 Xilinx, Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Xilinx - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.internal.debug.ui.profiler;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.debug.ui.DebugUITools;
 import org.eclipse.debug.ui.IDebugUIConstants;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.tcf.internal.debug.launch.TCFSourceLookupParticipant;
 import org.eclipse.tcf.internal.debug.model.TCFFunctionRef;
 import org.eclipse.tcf.internal.debug.model.TCFLaunch;
 import org.eclipse.tcf.internal.debug.model.TCFSourceRef;
 import org.eclipse.tcf.internal.debug.ui.Activator;
 import org.eclipse.tcf.internal.debug.ui.ImageCache;
 import org.eclipse.tcf.internal.debug.ui.model.TCFModel;
 import org.eclipse.tcf.internal.debug.ui.model.TCFModelManager;
 import org.eclipse.tcf.internal.debug.ui.model.TCFNode;
 import org.eclipse.tcf.internal.debug.ui.model.TCFNodeExecContext;
 import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.protocol.JSON;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.services.ILineNumbers;
 import org.eclipse.tcf.services.IProfiler;
 import org.eclipse.tcf.services.ISymbols;
 import org.eclipse.tcf.util.TCFDataCache;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.part.ViewPart;
 
 public class ProfilerView extends ViewPart {
 
     private static final long UPDATE_DELAY = 4000;
 
     private static class ProfileSample {
         int cnt;
         final BigInteger[] trace;
 
         ProfileSample(int cnt, BigInteger[] trace) {
             this.cnt = cnt;
             this.trace = trace;
         }
     }
 
     @SuppressWarnings("serial")
     private static class ProfileData extends HashMap<BigInteger,List<ProfileSample>> {
         final String ctx;
 
         boolean stopped;
         boolean unsupported;
         int sample_count;
 
         ProfileData(String ctx) {
             this.ctx = ctx;
         }
     }
 
     private static class ProfileEntry {
         BigInteger addr;
         String name;
         String file_full;
         String file_base;
         int line;
         int count;
     }
 
     private final Map<TCFModel,Map<String,ProfileData>> data =
             new HashMap<TCFModel,Map<String,ProfileData>>();
 
     private final ISelectionListener selection_listener = new ISelectionListener() {
         @Override
         public void selectionChanged(IWorkbenchPart part, ISelection s) {
             if (s instanceof IStructuredSelection) {
                 final Object obj = ((IStructuredSelection)s).getFirstElement();
                 Protocol.invokeLater(new Runnable() {
                     @Override
                     public void run() {
                         if (obj instanceof TCFNode) {
                             selection = (TCFNode)obj;
                         }
                         else {
                             selection = null;
                         }
                         updateView();
                     }
                 });
             }
         }
     };
 
     private final TCFModelManager.ModelManagerListener launch_listener = new TCFModelManager.ModelManagerListener() {
         @Override
         public void onConnected(TCFLaunch launch, TCFModel model) {
         }
 
         @Override
         public void onDisconnected(TCFLaunch launch, TCFModel model) {
             data.remove(model);
             updateView();
         }
     };
 
     private final Runnable read_data = new Runnable() {
         final Set<IToken> cmds = new HashSet<IToken>();
         @Override
         public void run() {
             if (disposed) {
                 data.clear();
                 return;
             }
             for (Map.Entry<TCFModel,Map<String,ProfileData>> e : data.entrySet()) {
                 TCFModel model = e.getKey();
                 IProfiler profiler = model.getChannel().getRemoteService(IProfiler.class);
                 if (profiler == null) continue;
                 Map<String,ProfileData> ctx_map = e.getValue();
                 for (final ProfileData p : ctx_map.values()) {
                     if (p.stopped) continue;
                     cmds.add(profiler.read(p.ctx, new IProfiler.DoneRead() {
                         @Override
                         public void doneRead(IToken token, Exception error, Map<String,Object>[] data) {
                             cmds.remove(token);
                             if (cmds.size() == 0) {
                                 Protocol.invokeLater(UPDATE_DELAY, read_data);
                                 updateView();
                             }
                             if (error != null) {
                                 Activator.log(error);
                             }
                             else if (!disposed && data != null) {
                                 p.unsupported = data.length == 0;
                                 if (data.length > 0) {
                                     for (Map<String,Object> m : data) addSamples(p, m);
                                 }
                             }
                         }
                     }));
                 }
             }
             if (cmds.size() == 0) {
                 Protocol.invokeLater(UPDATE_DELAY, this);
                 updateView();
             }
         }
     };
 
     private class SampleComparator implements Comparator<ProfileEntry> {
         final int sorting;
         SampleComparator(int sorting) {
             this.sorting = sorting;
         }
         @Override
         public int compare(ProfileEntry x, ProfileEntry y) {
             int r = 0;
             switch (sorting) {
             case 0:
                 r = x.addr.compareTo(y.addr);
                 break;
             case 1:
                 break;
             case 2:
                 if (x.name == y.name) break;
                 if (x.name == null) return -1;
                 if (y.name == null) return +1;
                 r = x.name.compareTo(y.name);
                 break;
             case 3:
                 if (x.file_base == y.file_base) break;
                 if (x.file_base == null) return -1;
                 if (y.file_base == null) return +1;
                 r = x.file_base.compareTo(y.file_base);
                 break;
             }
             if (r != 0) return r;
             if (x.count > y.count) return -1;
             if (x.count < y.count) return +1;
             return 0;
         }
     };
 
     private class Update implements Runnable {
         final TCFNode selection;
         final int sorting;
         final List<ProfileEntry> lst = new ArrayList<ProfileEntry>();
         final Map<BigInteger,String> funcs = new HashMap<BigInteger,String>();
         final TCFNodeExecContext node;
         final ProfileData prof_data;
         final List<ProfileSample>[] samples;
         final ISymbols symbols;
         final ILineNumbers line_numbers;
         boolean done;
         int pos;
         @SuppressWarnings("unchecked")
         Update() {
             assert Protocol.isDispatchThread();
             selection = ProfilerView.this.selection;
             sorting = ProfilerView.this.sorting;
             ProfileData p = null;
             if (selection != null) {
                 Map<String,ProfileData> m = data.get(selection.getModel());
                 if (m != null) p = m.get(selection.getID());
             }
             prof_data = p;
             if (p == null) {
                 node = null;
                 samples = null;
                 symbols = null;
                 line_numbers = null;
             }
             else {
                 node = (TCFNodeExecContext)selection;
                 symbols = node.getChannel().getRemoteService(ISymbols.class);
                 line_numbers = node.getChannel().getRemoteService(ILineNumbers.class);
                 samples = p.values().toArray(new List[p.values().size()]);
             }
         }
 
         @Override
         public void run() {
             if (done) return;
             if (last_update != this) return;
             if (samples != null && pos < samples.length) {
                 if (node.isDisposed()) {
                     lst.clear();
                 }
                 else {
                     while (pos < samples.length) {
                         List<ProfileSample> s = samples[pos];
                         int count = 0;
                         BigInteger addr = null;
                         for (ProfileSample x : s) {
                             if (addr == null) addr = x.trace[0];
                             count += x.cnt;
                         }
                         if (addr != null) {
                             String func_name = null;
                             String file_full = null;
                             String file_base = null;
                             int line = 0;
                             if (symbols != null) {
                                 String func_id = funcs.get(addr);
                                 if (func_id == null) {
                                     func_id = "";
                                     TCFDataCache<TCFFunctionRef> func_cache = node.getFuncInfo(addr);
                                     if (func_cache != null) {
                                         if (!func_cache.validate(this)) return;
                                         TCFFunctionRef func_data = func_cache.getData();
                                         if (func_data != null && func_data.symbol_id != null) {
                                             func_id = func_data.symbol_id;
                                         }
                                     }
                                     funcs.put(addr, func_id);
                                 }
                                 if (func_id.length() > 0) {
                                     TCFDataCache<ISymbols.Symbol> sym_cache = node.getModel().getSymbolInfoCache(func_id);
                                     if (!sym_cache.validate(this)) return;
                                     ISymbols.Symbol sym_data = sym_cache.getData();
                                     if (sym_data != null && sym_data.getName() != null) {
                                         func_name = sym_data.getName();
                                     }
                                 }
                             }
                             if (line_numbers != null) {
                                 TCFDataCache<TCFSourceRef> line_cache = node.getLineInfo(addr);
                                 if (line_cache != null) {
                                     if (!line_cache.validate(this)) return;
                                     TCFSourceRef line_data = line_cache.getData();
                                     if (line_data != null && line_data.area != null) {
                                         file_full = TCFSourceLookupParticipant.toFileName(line_data.area);
                                         if (file_full != null) {
                                             file_base = file_full;
                                             int i = file_base.lastIndexOf('/');
                                             int j = file_base.lastIndexOf('\\');
                                             if (i > j) file_base = file_base.substring(i + 1);
                                             if (i < j) file_base = file_base.substring(j + 1);
                                             line = line_data.area.start_line;
                                         }
                                     }
                                 }
                             }
                             ProfileEntry e = new ProfileEntry();
                             e.addr = addr;
                             e.count = count;
                             e.name = func_name;
                             e.file_full = file_full;
                             e.file_base = file_base;
                             e.line = line;
                             lst.add(e);
                         }
                         pos++;
                     }
                 }
             }
             done = true;
             final ProfileEntry[] entries = lst.toArray(new ProfileEntry[lst.size()]);
             Arrays.sort(entries, new SampleComparator(sorting));
             final boolean enable_start =
                     (selection instanceof TCFNodeExecContext) &&
                     selection.getChannel().getRemoteService(IProfiler.class) != null;
             final boolean enable_stop = node != null && !prof_data.stopped;
             final boolean stopped = node != null && prof_data.stopped;
             final boolean running = node != null && !stopped;
             final boolean unsupported = node != null && prof_data.unsupported;
             final int sample_count = prof_data == null ? 0 : prof_data.sample_count;
             parent.getDisplay().asyncExec(new Runnable() {
                 @Override
                 public void run() {
                     action_start.setEnabled(enable_start);
                     action_stop.setEnabled(enable_stop);
                     profile_node = node;
                     profile = entries;
                     viewer.setInput(entries);
                     if (!enable_start) {
                         status.setText("Selected context does not support profiling");
                     }
                     else if (unsupported) {
                         status.setText("No suitable profiler found for selected context");
                     }
                     else if (stopped) {
                         status.setText("Profiler stopped. Press 'Start' button to restart profiling");
                     }
                     else if (running) {
                         status.setText("Profiler runnning. " + sample_count + " samples");
                     }
                     else {
                         status.setText("Idle. Press 'Start' button to start profiling");
                     }
                 }
             });
         }
     }
 
     private class ProfileContentProvider implements IStructuredContentProvider  {
 
         public Object[] getElements(Object input) {
             return profile;
         }
 
         public void inputChanged(Viewer viewer, Object old_input, Object new_input) {
         }
 
         public void dispose() {
         }
     }
 
     private class ProfileLabelProvider extends LabelProvider implements ITableLabelProvider {
 
         public Image getColumnImage(Object element, int column) {
             return null;
         }
 
         public String getColumnText(Object element, int column) {
             ProfileEntry e = (ProfileEntry)element;
             switch (column) {
             case 0: return toHex(e.addr);
             case 1: return Integer.toString(e.count);
             case 2: return e.name;
             case 3: return e.file_base;
             case 4: return e.line == 0 ? null : Integer.toString(e.line);
             }
             return null;
         }
 
         private String toHex(BigInteger n) {
             String s = n.toString(16);
             if (s.length() >= 8) return s;
             return "00000000".substring(s.length()) + s;
         }
     }
 
     private final Action action_start = new Action("Start", ImageCache.getImageDescriptor(ImageCache.IMG_THREAD_RUNNNIG)) {
         @Override
         public void run() {
             Protocol.invokeLater(new Runnable() {
                 @Override
                 public void run() {
                     if (selection == null) return;
                     start(selection);
                 }
             });
         }
     };
 
     private final Action action_stop = new Action("Stop", ImageCache.getImageDescriptor(ImageCache.IMG_THREAD_SUSPENDED)) {
         @Override
         public void run() {
             Protocol.invokeLater(new Runnable() {
                 @Override
                 public void run() {
                     if (selection == null) return;
                     stop(selection);
                 }
             });
         }
     };
 
     private boolean disposed;
     private boolean launch_listener_ok;
     private TCFNode selection;
     private int sorting;
     private Update last_update;
     private Composite parent;
     private Label status;
     private TableViewer viewer;
     private ProfileEntry[] profile;
     private TCFNode profile_node;
 
     private static final String[] column_ids = {
         "Address",
         "Count",
         "Function",
         "File",
         "Line"
     };
 
     private static final int[] column_size = {
         80,
         60,
         250,
         250,
         60
     };
 
     @Override
     public void createPartControl(Composite parent) {
         this.parent = parent;
 
         Font font = parent.getFont();
         Composite composite = new Composite(parent, SWT.NONE);
         GridLayout layout = new GridLayout(1, false);
         composite.setFont(font);
         composite.setLayout(layout);
         composite.setLayoutData(new GridData(GridData.FILL_BOTH));
         status = new Label(composite, SWT.NONE);
         status.setFont(font);
         status.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
         viewer = new TableViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
         final Table table = viewer.getTable();
         table.setLayoutData(new GridData(GridData.FILL_BOTH));
         table.setHeaderVisible(true);
         table.setLinesVisible(true);
         table.setFont(font);
         viewer.setContentProvider(new ProfileContentProvider());
         viewer.setLabelProvider(new ProfileLabelProvider());
         viewer.setColumnProperties(column_ids);
 
         for (int i = 0; i < column_ids.length; i++) {
             final int n = i;
             final TableColumn c = new TableColumn(table, SWT.NONE, i);
             c.setText(column_ids[i]);
             c.setWidth(column_size[i]);
             if (i != 4) {
                 c.addSelectionListener(new SelectionListener() {
                     @Override
                     public void widgetSelected(SelectionEvent e) {
                         table.setSortDirection(SWT.DOWN);
                         table.setSortColumn(c);
                         sorting = n;
                         Protocol.invokeLater(new Runnable() {
                             @Override
                             public void run() {
                                 updateView();
                             }
                         });
                     }
                     @Override
                     public void widgetDefaultSelected(SelectionEvent e) {
                     }
                 });
             }
             if (i == 1) {
                 table.setSortDirection(SWT.DOWN);
                 table.setSortColumn(c);
                 sorting = n;
             }
         }
 
         table.addSelectionListener(new SelectionListener() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 if (profile_node == null) return;
                 if (e.item == null) return;
                 ProfileEntry pe = (ProfileEntry)viewer.getElementAt(table.indexOf((TableItem)e.item));
                 if (pe.file_full == null) return;
                 profile_node.getModel().displaySource(profile_node.getID(), pe.file_full, pe.line);
             }
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
             }
         });
 
         action_start.setEnabled(false);
         action_stop.setEnabled(false);
         IActionBars action_bars = getViewSite().getActionBars();
         IToolBarManager tool_bar = action_bars.getToolBarManager();
         tool_bar.add(action_start);
         tool_bar.add(action_stop);
 
         IWorkbenchWindow window = getSite().getWorkbenchWindow();
         window.getSelectionService().addSelectionListener(
                 IDebugUIConstants.ID_DEBUG_VIEW, selection_listener);
         ISelection active_context = DebugUITools.getDebugContextManager()
                 .getContextService(window).getActiveContext();
         selection_listener.selectionChanged(null, active_context);
 
         Protocol.invokeLater(read_data);
     }
 
     @Override
     public void setFocus() {
         viewer.getControl().setFocus();
     }
 
     public void start(TCFNode node) {
         assert Protocol.isDispatchThread();
         if (!launch_listener_ok) {
             TCFModelManager.getModelManager().addListener(launch_listener);
             launch_listener_ok = true;
         }
         Map<String,Object> params = new HashMap<String,Object>();
         params.put(IProfiler.PARAM_FRAME_CNT, 4);
         configure(node, params);
     }
 
     public void stop(TCFNode node) {
         assert Protocol.isDispatchThread();
         Map<String,Object> params = new HashMap<String,Object>();
         configure(node, params);
     }
 
     private void configure(final TCFNode node, final Map<String,Object> params) {
         IProfiler profiler = node.getChannel().getRemoteService(IProfiler.class);
         if (profiler != null) {
             profiler.configure(node.getID(), params, new IProfiler.DoneConfigure() {
                 @Override
                 public void doneConfigure(IToken token, final Exception error) {
                     if (error == null) {
                         Map<String,ProfileData> m = data.get(node.getModel());
                         if (params.size() == 0) {
                             ProfileData d = null;
                             if (m != null) d = m.get(node.getID());
                             if (d != null) d.stopped = true;
                         }
                         else {
                             ProfileData d = new ProfileData(node.getID());
                             if (m == null) data.put(node.getModel(), m = new HashMap<String,ProfileData>());
                             m.put(d.ctx, d);
                         }
                         if (selection == node) updateView();
                     }
                     else {
                         parent.getDisplay().asyncExec(new Runnable() {
                             @Override
                             public void run() {
                                 MessageBox mb = new MessageBox(parent.getShell(), SWT.ICON_ERROR | SWT.OK);
                                 mb.setText("Cannot start profiling");
                                 mb.setMessage(TCFModel.getErrorMessage(error, true));
                                 mb.open();
                             }
                         });
                     }
                 }
             });
         }
         updateView();
     }
 
     private void addSamples(ProfileData p, Map<String,Object> props) {
         int size = 4;
         boolean big_endian = false;
         byte[] data = null;
         if (props != null) {
             Number n = (Number)props.get(IProfiler.PROP_ADDR_SIZE);
             if (n != null) size = n.intValue();
             Boolean b = (Boolean)props.get(IProfiler.PROP_BIG_ENDIAN);
             if (b != null) big_endian = b.booleanValue();
            data = JSON.toByteArray(props.get(IProfiler.PROP_DATA));
         }
         if (data == null) return;
         int pos = 0;
         byte[] buf = new byte[size + 1];
         BigInteger[] trace = new BigInteger[16];
         for (;;) {
             int c = -1;
             int l = -1;
             int i = 0;
             while (pos + size <= data.length) {
                 for (int j = 0; j < size; j++) {
                     buf[big_endian ? j + 1: size - j] = data[pos++];
                 }
                 if (i >= trace.length) continue;
                 BigInteger a = new BigInteger(buf);
                 if (c < 0) {
                     /* Count */
                     c = a.intValue();
                 }
                 else if (l < 0) {
                     /* Trace length */
                     l = a.intValue();
                 }
                 else {
                     /* Trace addresses */
                     trace[i++] = a;
                     if (i == l) break;
                 }
             }
             if (l < 0) break;
             if (i > 0) addSample(p, trace, i, c);
         }
     }
 
     private void addSample(ProfileData p, BigInteger[] trace, int len, int cnt) {
         p.sample_count += cnt;
         List<ProfileSample> lp = p.get(trace[0]);
         if (lp != null) {
             for (ProfileSample s : lp) {
                 int i = 0;
                 while (i < len && trace[i].equals(s.trace[i])) i++;
                 if (i == len) {
                     s.cnt += cnt;
                     return;
                 }
             }
         }
         else {
             p.put(trace[0], lp = new ArrayList<ProfileSample>());
         }
         BigInteger[] t = new BigInteger[len];
         for (int i = 0; i < len; i++) t[i] = trace[i];
         lp.add(new ProfileSample(cnt, t));
     }
 
     private void updateView() {
         assert Protocol.isDispatchThread();
         last_update = new Update();
         Protocol.invokeLater(last_update);
     }
 
     @Override
     public void dispose() {
         disposed = true;
         Protocol.invokeAndWait(new Runnable() {
             @Override
             public void run() {
                 TCFModelManager.getModelManager().addListener(launch_listener);
             }
         });
         getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(
                 IDebugUIConstants.ID_DEBUG_VIEW, selection_listener);
         super.dispose();
     }
 }
