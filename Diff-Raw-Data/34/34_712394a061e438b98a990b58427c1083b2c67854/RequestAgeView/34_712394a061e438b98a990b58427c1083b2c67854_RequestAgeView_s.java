 package com.ingemark.perftest.plugin.ui;
 
 import static com.ingemark.perftest.Message.EXCEPTION;
 import static com.ingemark.perftest.Util.gridData;
 import static com.ingemark.perftest.Util.sneakyThrow;
 import static com.ingemark.perftest.plugin.StressTestActivator.EVT_ERROR;
 import static com.ingemark.perftest.plugin.StressTestActivator.EVT_INIT_HIST;
 import static com.ingemark.perftest.plugin.StressTestActivator.EVT_RUN_SCRIPT;
 import static com.ingemark.perftest.plugin.StressTestActivator.STATS_EVTYPE_BASE;
 import static com.ingemark.perftest.plugin.StressTestActivator.STRESSTEST_VIEW_ID;
 import static com.ingemark.perftest.plugin.StressTestActivator.stressTestPlugin;
 import static org.eclipse.ui.PlatformUI.getWorkbench;
 import static org.slf4j.LoggerFactory.getLogger;
 
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Scale;
 import org.eclipse.ui.part.ViewPart;
 import org.slf4j.Logger;
 
 import com.ingemark.perftest.DialogInfo;
 import com.ingemark.perftest.IStressTestServer;
 import com.ingemark.perftest.Message;
 import com.ingemark.perftest.Stats;
 import com.ingemark.perftest.StressTestServer;
 
 public class RequestAgeView extends ViewPart
 {
   static final Logger log = getLogger(RequestAgeView.class);
   private static final int MIN_THROTTLE = 50;
   public static RequestAgeView instance;
   public Composite statsParent;
   private ProgressDialog pd;
   private Scale throttle;
   private IStressTestServer testServer = StressTestServer.NULL;
   private Action stopAction;
   private Composite viewParent;
 
   public void createPartControl(final Composite p) {
     this.viewParent = p;
     instance = this;
     p.setLayout(new GridLayout(2, false));
     stopAction = new Action() {
       final ImageDescriptor img = stressTestPlugin().imageDescriptor("stop.gif");
       @Override public ImageDescriptor getImageDescriptor() { return img; }
       @Override public void run() { shutdown(); }
     };
     stopAction.setEnabled(false);
     getViewSite().getActionBars().getToolBarManager().add(stopAction);
     throttle = new Scale(p, SWT.VERTICAL);
     throttle.setMinimum(MIN_THROTTLE);
     throttle.setMaximum(400);
     throttle.addSelectionListener(new SelectionAdapter() {
       @Override public void widgetSelected(SelectionEvent e) { applyThrottle(); }
     });
     newStatsParent();
   }
 
   void newStatsParent() {
     if (statsParent != null) statsParent.dispose();
     statsParent = new Composite(viewParent, SWT.NONE);
     gridData().grab(true, true).applyTo(statsParent);
     statsParent.setLayout(new GridLayout(2, false));
     statsParent.addListener(EVT_RUN_SCRIPT, new Listener() {
       public void handleEvent(final Event event) {
         try {
           shutdown();
           statsParent.addListener(EVT_INIT_HIST, new Listener() {
             @Override public void handleEvent(Event event) {
               log.debug("Init histogram");
               throttle.setSelection(MIN_THROTTLE);
               applyThrottle();
               final List<Integer> indices = (List<Integer>)event.data;
               Collections.sort(indices);
               for (int i : indices) {
                 final HistogramViewer histogram = new HistogramViewer(statsParent);
                 gridData().grab(true, true).applyTo(histogram.canvas);
                 statsParent.addListener(STATS_EVTYPE_BASE + i, new Listener() {
                   public void handleEvent(Event e) { histogram.statsUpdate((Stats) e.data); }
                 });
                 histogram.canvas.addMouseListener(new MouseListener() {
                   @Override public void mouseDoubleClick(MouseEvent e) {
                     testServer.send(new Message(EXCEPTION, histogram.stats.name));
                   }
                   @Override public void mouseUp(MouseEvent e) {}
                   @Override public void mouseDown(MouseEvent e) {}
                 });
               }
               viewParent.layout(true);
               viewParent.notifyListeners(SWT.Paint, new Event());
              try {
                getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
                    STRESSTEST_VIEW_ID);
              } catch (CoreException e) { sneakyThrow(e); }
           }});
           statsParent.addListener(EVT_ERROR, new Listener() {
             @Override public void handleEvent(Event e) {
               InfoDialog.show(new DialogInfo("Stress testing error", ((Throwable)e.data)));
             }
           });
           pd = new ProgressDialog(RequestAgeView.this, "Starting Stress Test", 158);
           testServer = new StressTestServer(statsParent, (String)event.data);
           testServer.start(pd.pm());
           stopAction.setEnabled(true);
         }
         catch (Throwable t) {
           InfoDialog.show(new DialogInfo("Stress test init error", t));
         }
       }});
   }
 
   public void shutdown() {
    if (pd != null) pd.close();
     testServer.shutdown();
     testServer = StressTestServer.NULL;
     newStatsParent();
     stopAction.setEnabled(false);
   }
 
   static String joinPath(String[] ps) {
     final StringBuilder b = new StringBuilder(128);
     for (String p : ps) b.append(p).append(":");
     return b.toString();
   }
 
   static int pow(int in) { return (int)Math.pow(10, in/100d); }
   @Override public void dispose() { shutdown(); }
 
   @Override public void setFocus() { throttle.setFocus(); }
 
   private void applyThrottle() {
     testServer.intensity(pow(throttle.getSelection()));
   }
 }
