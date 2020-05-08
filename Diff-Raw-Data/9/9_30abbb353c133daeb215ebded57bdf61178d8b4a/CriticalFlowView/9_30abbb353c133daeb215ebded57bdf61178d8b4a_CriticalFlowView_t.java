 /*******************************************************************************
  * Copyright (c) 2013 Ericsson
  *
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Geneviève Bastien - Initial API and implementation (from ControlFlowView)
  *******************************************************************************/
 
 package org.eclipse.linuxtools.lttng2.kernel.aka.views.criticalflow;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.linuxtools.lttng2.kernel.aka.views.AbstractAKAView;
 import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTimestamp;
 import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
 import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
 import org.eclipse.linuxtools.tmf.core.signal.TmfTraceSelectedSignal;
 import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
 import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
 import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
 import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
 import org.eclipse.linuxtools.tmf.ui.editors.ITmfTraceEditor;
 import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphRangeListener;
 import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphSelectionListener;
 import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphTimeListener;
 import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphRangeUpdateEvent;
 import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphSelectionEvent;
 import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphTimeEvent;
 import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
 import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
 import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.Utils.TimeFormat;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.TreeColumn;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IEditorPart;
 import org.lttng.studio.model.graph.CriticalPathStats;
 import org.lttng.studio.model.graph.DepthFirstCriticalPathBackward;
 import org.lttng.studio.model.graph.ExecEdge;
 import org.lttng.studio.model.graph.ExecGraph;
 import org.lttng.studio.model.graph.ExecVertex;
 import org.lttng.studio.model.graph.Span;
 import org.lttng.studio.model.kernel.SystemModel;
 import org.lttng.studio.model.kernel.Task;
 import org.lttng.studio.reader.handler.IModelKeys;
 
 /**
  * The Control Flow view main object
  *
  */
 public class CriticalFlowView extends AbstractAKAView {
 
     // ------------------------------------------------------------------------
     // Constants
     // ------------------------------------------------------------------------
 
     /**
      * View ID.
      */
     public static final String ID = "org.eclipse.linuxtools.lttng2.kernel.aka.views.criticalflow"; //$NON-NLS-1$
 
     private static final String PROCESS_COLUMN    = Messages.CriticalFlowView_processColumn;
 
 
     private final String[] COLUMN_NAMES = new String[] {
             PROCESS_COLUMN
     };
 
     private final String[] FILTER_COLUMN_NAMES = new String[] {
             PROCESS_COLUMN
     };
     
 	private Task fCurrentTask;
 
 
     /**
      * Redraw state enum
      */
     private enum State { IDLE, BUSY, PENDING }
 
     // ------------------------------------------------------------------------
     // Fields
     // ------------------------------------------------------------------------
 
     // The timegraph combo
     private CriticalFlowCombo fTimeGraphCombo;
 
     // The timegraph entry list
     private ArrayList<CriticalFlowEntry> fEntryList;
 
     // The trace to entry list hash map
     final private HashMap<ITmfTrace, ArrayList<CriticalFlowEntry>> fEntryListMap = new HashMap<ITmfTrace, ArrayList<CriticalFlowEntry>>();
 
     // The trace to build thread hash map
     final private HashMap<ITmfTrace, BuildThread> fBuildThreadMap = new HashMap<ITmfTrace, BuildThread>();
 
     // The start time
     private long fStartTime;
 
     // The end time
     private long fEndTime;
 
     // The display width
     private final int fDisplayWidth;
 
     // The zoom thread
     private ZoomThread fZoomThread;
 
     // The next resource action
     private Action fNextResourceAction;
 
     // The previous resource action
     private Action fPreviousResourceAction;
 
     // The redraw state used to prevent unnecessary queuing of display runnables
     private State fRedrawState = State.IDLE;
 
     // The redraw synchronization object
     final private Object fSyncObj = new Object();
 
     // ------------------------------------------------------------------------
     // Classes
     // ------------------------------------------------------------------------
 
     private class TreeContentProvider implements ITreeContentProvider {
 
         @Override
         public void dispose() {
         }
 
         @Override
         public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
         }
 
         @Override
         public Object[] getElements(Object inputElement) {
             return (ITimeGraphEntry[]) inputElement;
         }
 
         @Override
         public Object[] getChildren(Object parentElement) {
             ITimeGraphEntry entry = (ITimeGraphEntry) parentElement;
             List<? extends ITimeGraphEntry> children = entry.getChildren();
             return children.toArray(new ITimeGraphEntry[children.size()]);
         }
 
         @Override
         public Object getParent(Object element) {
             ITimeGraphEntry entry = (ITimeGraphEntry) element;
             return entry.getParent();
         }
 
         @Override
         public boolean hasChildren(Object element) {
             ITimeGraphEntry entry = (ITimeGraphEntry) element;
             return entry.hasChildren();
         }
 
     }
 
     private class TreeLabelProvider implements ITableLabelProvider {
 
         @Override
         public void addListener(ILabelProviderListener listener) {
         }
 
         @Override
         public void dispose() {
         }
 
         @Override
         public boolean isLabelProperty(Object element, String property) {
             return false;
         }
 
         @Override
         public void removeListener(ILabelProviderListener listener) {
         }
 
         @Override
         public Image getColumnImage(Object element, int columnIndex) {
             return null;
         }
 
         @Override
         public String getColumnText(Object element, int columnIndex) {
             CriticalFlowEntry entry = (CriticalFlowEntry) element;
             if (columnIndex == 0) {
                 return entry.getName();
             } 
             return ""; //$NON-NLS-1$
         }
 
     }
 
     private class BuildThread extends Thread {
         private final ITmfTrace fBuildTrace;
         private final IProgressMonitor fMonitor;
 
         public BuildThread(ITmfTrace trace) {
             super("CriticalFlowView build"); //$NON-NLS-1$
             fBuildTrace = trace;
             fMonitor = new NullProgressMonitor();
         }
 
         @Override
         public void run() {
 //            buildTreeList(fBuildTrace, fMonitor);
 //            synchronized (fBuildThreadMap) {
 //                fBuildThreadMap.remove(this);
 //            }
         }
 
         public void cancel() {
             fMonitor.setCanceled(true);
         }
     }
 
     private class ZoomThread extends Thread {
         private final ArrayList<CriticalFlowEntry> fZoomEntryList;
         private final long fZoomStartTime;
         private final long fZoomEndTime;
         private final long fResolution;
         private final IProgressMonitor fMonitor;
 
         public ZoomThread(ArrayList<CriticalFlowEntry> entryList, long startTime, long endTime) {
             super("ControlFlowView zoom"); //$NON-NLS-1$
             fZoomEntryList = entryList;
             fZoomStartTime = startTime;
             fZoomEndTime = endTime;
             fResolution = Math.max(1, (fZoomEndTime - fZoomStartTime) / fDisplayWidth);
             fMonitor = new NullProgressMonitor();
         }
 
         @Override
         public void run() {
             if (fZoomEntryList == null) {
                 return;
             }
             for (CriticalFlowEntry entry : fZoomEntryList) {
                 if (fMonitor.isCanceled()) {
                     break;
                 }
                 zoom(entry, fMonitor);
             }
         }
 
         private void zoom(CriticalFlowEntry entry, IProgressMonitor monitor) {
             
             List<ITimeEvent> zoomedEventList = getEventList(entry, fZoomStartTime, fZoomEndTime, fResolution);
             if (zoomedEventList != null) {
                 entry.setZoomedEventList(zoomedEventList);
             }
             
             redraw();
             for (CriticalFlowEntry child : entry.getChildren()) {
                 if (fMonitor.isCanceled()) {
                     return;
                 }
                 zoom(child, monitor);
             }
         }
 
         public void cancel() {
             fMonitor.setCanceled(true);
         }
     }
 
     // ------------------------------------------------------------------------
     // Constructors
     // ------------------------------------------------------------------------
 
     /**
      * Constructor
      */
     public CriticalFlowView() {
         super(ID);
         fDisplayWidth = Display.getDefault().getBounds().width;
     }
 
     // ------------------------------------------------------------------------
     // ViewPart
     // ------------------------------------------------------------------------
 
 
 	/* (non-Javadoc)
      * @see org.eclipse.linuxtools.tmf.ui.views.TmfView#createPartControl(org.eclipse.swt.widgets.Composite)
      */
     @Override
     public void createPartControl(Composite parent) {
     	super.createPartControl(parent);
         fTimeGraphCombo = new CriticalFlowCombo(parent, SWT.NONE);
 
         fTimeGraphCombo.setTreeContentProvider(new TreeContentProvider());
 
         fTimeGraphCombo.setTreeLabelProvider(new TreeLabelProvider());
 
         fTimeGraphCombo.setTimeGraphProvider(new CriticalFlowPresentationProvider());
 
         fTimeGraphCombo.setTreeColumns(COLUMN_NAMES);
 
         fTimeGraphCombo.setFilterContentProvider(new TreeContentProvider());
 
         fTimeGraphCombo.setFilterLabelProvider(new TreeLabelProvider());
 
         fTimeGraphCombo.setFilterColumns(FILTER_COLUMN_NAMES);
 
         fTimeGraphCombo.getTimeGraphViewer().addRangeListener(new ITimeGraphRangeListener() {
             @Override
             public void timeRangeUpdated(TimeGraphRangeUpdateEvent event) {
                 final long startTime = event.getStartTime();
                 final long endTime = event.getEndTime();
                 TmfTimeRange range = new TmfTimeRange(new CtfTmfTimestamp(startTime), new CtfTmfTimestamp(endTime));
                 TmfTimestamp time = new CtfTmfTimestamp(fTimeGraphCombo.getTimeGraphViewer().getSelectedTime());
                 broadcast(new TmfRangeSynchSignal(CriticalFlowView.this, range, time));
                 if (fZoomThread != null) {
                     fZoomThread.cancel();
                 }
                 startZoomThread(startTime, endTime);
             }
         });
 
         fTimeGraphCombo.getTimeGraphViewer().addTimeListener(new ITimeGraphTimeListener() {
             @Override
             public void timeSelected(TimeGraphTimeEvent event) {
                 long time = event.getTime();
                 broadcast(new TmfTimeSynchSignal(CriticalFlowView.this, new CtfTmfTimestamp(time)));
             }
         });
 
         fTimeGraphCombo.addSelectionListener(new ITimeGraphSelectionListener() {
             @Override
             public void selectionChanged(TimeGraphSelectionEvent event) {
                 //ITimeGraphEntry selection = event.getSelection();
             }
         });
 
         fTimeGraphCombo.getTimeGraphViewer().setTimeFormat(TimeFormat.CALENDAR);
 
         // View Action Handling
 //        makeActions();
         contributeToActionBars();
 
         IEditorPart editor = getSite().getPage().getActiveEditor();
         if (editor instanceof ITmfTraceEditor) {
             ITmfTrace trace = ((ITmfTraceEditor) editor).getTrace();
             if (trace != null) {
                 traceSelected(new TmfTraceSelectedSignal(this, trace));
             }
         }
 
         // make selection available to other views
         getSite().setSelectionProvider(fTimeGraphCombo.getTreeViewer());
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
      */
     @Override
     public void setFocus() {
         fTimeGraphCombo.setFocus();
     }
 
     // ------------------------------------------------------------------------
     // Internal
     // ------------------------------------------------------------------------
 
     private void buildTreeList(final List<ExecEdge> path, ExecGraph graph) {
     	fStartTime = Long.MAX_VALUE;
         fEndTime = Long.MIN_VALUE;
         
         Map<Object, CriticalFlowEntry> rootList = new LinkedHashMap<Object, CriticalFlowEntry>();
 
         // FIXME: position may change according to sorting in the table view
         int position = 0;
         for (ExecEdge edge : path) {
 			ExecVertex source = graph.getGraph().getEdgeSource(edge);
 			ExecVertex target = graph.getGraph().getEdgeTarget(edge);
 
 			/* Get start and end time */
 			long start = source.getTimestamp();
 			long end = target.getTimestamp();
 			fStartTime = Math.min(fStartTime, start);
             fEndTime = Math.max(fEndTime, end);
 
             Object sourceentry = source.getOwner();
             Object targetentry = target.getOwner();
             
             if (sourceentry == targetentry) {
             	CriticalFlowEntry entry = rootList.get(sourceentry);
             	if (entry == null) {
             		entry = new CriticalFlowEntry(sourceentry.toString(), fStartTime, fEndTime);
             		entry.setPosition(position++);
             		rootList.put(sourceentry,  entry);	
             	}
             	entry.addEvent(new CriticalFlowEvent(rootList.get(sourceentry), start, end-start, 
             			CriticalFlowPresentationProvider.State.CRITICAL));
             }
         }
         
         for (ExecEdge edge : path) {
 			ExecVertex source = graph.getGraph().getEdgeSource(edge);
 			ExecVertex target = graph.getGraph().getEdgeTarget(edge);
 			if (source.getOwner() != target.getOwner()) {
 				CriticalFlowEntry entrySrc = rootList.get(source.getOwner());
 				CriticalFlowEntry entryDst = rootList.get(target.getOwner());
 				entrySrc.addEvent(new CriticalFlowLink(entrySrc, entryDst, source.getTimestamp(), target.getTimestamp()));
 			}
         }
 
         fEntryList = new ArrayList<CriticalFlowEntry>();
         fEntryList.addAll(rootList.values());
         refresh();
 //            Collections.sort(rootList, fCriticalFlowEntryComparator);
 //            synchronized (fEntryListMap) {
 //                fEntryListMap.put(trace, (ArrayList<CriticalFlowEntry>) rootList.clone());
 //            }
 //            if (trace == fTrace) {
 //                refresh();
 //            }
 //        }
         for (CriticalFlowEntry entry : fEntryList) {
             buildStatusEvents(entry);
         }
     }
 
     private void buildStatusEvents( CriticalFlowEntry entry) {
        
         long start = fTrace.getStartTime().getValue();
         long end = fTrace.getEndTime().getValue() + 1;
         long resolution = Math.max(1, (end - start) / fDisplayWidth);
         List<ITimeEvent> eventList = getEventList(entry, entry.getStartTime(), entry.getEndTime(), resolution);
  
         entry.setZoomedEventList(eventList);
 
             redraw();
         
         for (ITimeGraphEntry child : entry.getChildren()) {
 
             buildStatusEvents((CriticalFlowEntry) child);
         }
     }
 
     private static List<ITimeEvent> getEventList(CriticalFlowEntry entry,
             long startTime, long endTime, long resolution) {
 
         final long realStart = Math.max(startTime, entry.getStartTime());
         final long realEnd = Math.min(endTime, entry.getEndTime());
         if (realEnd <= realStart) {
             return null;
         }
         List<ITimeEvent> eventList = null;
         try {
         	Iterator<ITimeEvent> iterator = entry.getTimeEventsIterator();
         	eventList = new ArrayList<ITimeEvent>();
             
         	while (iterator.hasNext()) {
         		
         		ITimeEvent event = iterator.next();
         		/* is event visible */
				if (((event.getTime() >= realStart) && (event.getTime() <= realEnd)) ||
					((event.getTime() + event.getDuration() > realStart) &&
						(event.getTime() + event.getDuration() < realEnd))) {
					eventList.add(event);
				}
         	}
 
         } catch (Exception e) {
         	e.printStackTrace();
         }
         return eventList;
     }
 
     private void refresh() {
         Display.getDefault().asyncExec(new Runnable() {
             @Override
             public void run() {
                 if (fTimeGraphCombo.isDisposed()) {
                     return;
                 }
                 ITimeGraphEntry[] entries = null;
 
                 if (fEntryList == null) {
                     fEntryList = new ArrayList<CriticalFlowEntry>();
                 }
                 entries = fEntryList.toArray(new ITimeGraphEntry[0]);
 
                 fTimeGraphCombo.setInput(entries);
                 fTimeGraphCombo.getTimeGraphViewer().setTimeBounds(fStartTime, fEndTime);
 
                 long timestamp = fTrace == null ? 0 : fTrace.getCurrentTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                 long startTime = fTrace == null ? 0 : fTrace.getCurrentRange().getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                 long endTime = fTrace == null ? 0 : fTrace.getCurrentRange().getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                 startTime = Math.max(startTime, fStartTime);
                 endTime = Math.min(endTime, fEndTime);
                 fTimeGraphCombo.getTimeGraphViewer().setSelectedTime(timestamp, false);
                 fTimeGraphCombo.getTimeGraphViewer().setStartFinishTime(startTime, endTime);
 
                 for (TreeColumn column : fTimeGraphCombo.getTreeViewer().getTree().getColumns()) {
                     column.pack();
                 }
 
                 startZoomThread(startTime, endTime);
             }
         });
     }
 
     private void redraw() {
         synchronized (fSyncObj) {
             if (fRedrawState == State.IDLE) {
                 fRedrawState = State.BUSY;
             } else {
                 fRedrawState = State.PENDING;
                 return;
             }
         }
         Display.getDefault().asyncExec(new Runnable() {
             @Override
             public void run() {
                 if (fTimeGraphCombo.isDisposed()) {
                     return;
                 }
                 fTimeGraphCombo.redraw();
                 fTimeGraphCombo.update();
                 synchronized (fSyncObj) {
                     if (fRedrawState == State.PENDING) {
                         fRedrawState = State.IDLE;
                         redraw();
                     } else {
                         fRedrawState = State.IDLE;
                     }
                 }
             }
         });
     }
 
     private void startZoomThread(long startTime, long endTime) {
         if (fZoomThread != null) {
             fZoomThread.cancel();
         }
         fZoomThread = new ZoomThread(fEntryList, startTime, endTime);
         fZoomThread.start();
     }
 
 //    private void makeActions() {
 //        fPreviousResourceAction = fTimeGraphCombo.getTimeGraphViewer().getPreviousItemAction();
 //        fPreviousResourceAction.setText(Messages.ControlFlowView_previousProcessActionNameText);
 //        fPreviousResourceAction.setToolTipText(Messages.ControlFlowView_previousProcessActionToolTipText);
 //        fNextResourceAction = fTimeGraphCombo.getTimeGraphViewer().getNextItemAction();
 //        fNextResourceAction.setText(Messages.ControlFlowView_nextProcessActionNameText);
 //        fNextResourceAction.setToolTipText(Messages.ControlFlowView_nextProcessActionToolTipText);
 //    }
 
     private void contributeToActionBars() {
         IActionBars bars = getViewSite().getActionBars();
         fillLocalToolBar(bars.getToolBarManager());
     }
 
     private void fillLocalToolBar(IToolBarManager manager) {
         manager.add(fTimeGraphCombo.getShowFilterAction());
         manager.add(fTimeGraphCombo.getTimeGraphViewer().getShowLegendAction());
         manager.add(new Separator());
         manager.add(fTimeGraphCombo.getTimeGraphViewer().getResetScaleAction());
         manager.add(fTimeGraphCombo.getTimeGraphViewer().getPreviousEventAction());
         manager.add(fTimeGraphCombo.getTimeGraphViewer().getNextEventAction());
 //        manager.add(fPreviousResourceAction);
 //        manager.add(fNextResourceAction);
         manager.add(fTimeGraphCombo.getTimeGraphViewer().getZoomInAction());
         manager.add(fTimeGraphCombo.getTimeGraphViewer().getZoomOutAction());
         manager.add(new Separator());
     }
     
 	private void setSpanRoot(Span root, ExecGraph graph, List<ExecEdge> path) {
 
 		buildTreeList(path, graph);
 //		treeViewer.setInput(root);
 //		treeViewer.expandAll();
 	}
     
 	@Override
 	protected void updateDataSafe() {
 		if (registry == null)
 			return;
 		SystemModel system = registry.getModel(IModelKeys.SHARED, SystemModel.class);
 		ExecGraph graph = registry.getModel(IModelKeys.SHARED, ExecGraph.class);
 		if (system == null || graph == null)
 			return;
 		Task task = system.getTask(currentTid);
 		// change input if task has changed
 		if (task != null && task != fCurrentTask) {
 			fCurrentTask = task;
 			computeCriticalPath(graph, task);
 		}
 	}
 
 	private void computeCriticalPath(ExecGraph graph, Task task) {
 		ExecVertex head = graph.getStartVertexOf(task);
 		ExecVertex stop = graph.getEndVertexOf(task);
 		if (!graph.getGraph().containsVertex(head)) {
 			System.err.println("WARNING: head vertex is null for task " + task);
 			return;
 		}
 
 		DepthFirstCriticalPathBackward annotate = new DepthFirstCriticalPathBackward(graph);
 		List<ExecEdge> path = annotate.criticalPath(head, stop);
 		Span root = CriticalPathStats.compile(graph, path);
 		setSpanRoot(root, graph, path);
 	}
 }
