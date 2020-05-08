 package com.ingemark.perftest.plugin.ui;
 
 import static com.ingemark.perftest.plugin.StressTestActivator.INIT_HIST_EVTYPE;
 import static com.ingemark.perftest.plugin.StressTestActivator.RUN_SCRIPT_EVTYPE;
 import static com.ingemark.perftest.plugin.StressTestActivator.STATS_EVTYPE_BASE;
 import static org.eclipse.jface.dialogs.MessageDialog.openError;
 
 import java.util.List;
 
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Scale;
 import org.eclipse.ui.part.ViewPart;
 
 import com.ingemark.perftest.IStressTestServer;
 import com.ingemark.perftest.Stats;
 import com.ingemark.perftest.StressTestServer;
 import com.ingemark.perftest.StressTester;
 
 public class RequestAgeView extends ViewPart
 {
   private static final int MIN_THROTTLE = 50;
   public static RequestAgeView instance;
   public Composite statsParent;
   Process subprocess;
   Scale throttle;
   Display disp;
   IStressTestServer testServer = StressTestServer.NULL;
   Stats stats = new Stats();
 
 
   public void createPartControl(final Composite p) {
     instance = this;
     disp = Display.getDefault();
    p.setLayout(new GridLayout(2, false));
     throttle = new Scale(p, SWT.VERTICAL);
     throttle.setMinimum(MIN_THROTTLE);
     throttle.setMaximum(400);
     throttle.addSelectionListener(new SelectionAdapter() {
       @Override public void widgetSelected(SelectionEvent e) { applyThrottle(); }
     });
     newStatsParent(p);
   }
 
   void newStatsParent(final Composite p) {
     statsParent = new Composite(p, SWT.NONE);
     gridData().grab(true, true).applyTo(statsParent);
     statsParent.setLayout(new GridLayout(2, true));
     statsParent.addListener(RUN_SCRIPT_EVTYPE, new Listener() {
       public void handleEvent(Event event) {
         try {
           shutdown();
           statsParent.dispose();
           newStatsParent(p);
           statsParent.addListener(INIT_HIST_EVTYPE, new Listener() {
             @Override public void handleEvent(Event event) {
               System.out.println("Init histogram");
               throttle.setSelection(MIN_THROTTLE);
               applyThrottle();
               for (int i : (List<Integer>)event.data) {
                 final HistogramViewer histogram = new HistogramViewer(statsParent);
                 gridData().grab(true, true).applyTo(histogram.canvas);
                 statsParent.addListener(STATS_EVTYPE_BASE + i, new Listener() {
                   public void handleEvent(Event e) { histogram.statsUpdate((Stats) e.data); }
                 });
               }
               p.layout(true);
              p.notifyListeners(SWT.Paint, new Event());
           }});
           testServer = new StressTestServer(statsParent);
           subprocess = StressTester.launchTester((String)event.data);
           System.out.println("Subprocess running");
         }
         catch (Throwable t) {
           openError(null, "Stress test init error", String.format(
               "%s: %s", t.getClass().getSimpleName(), t.getMessage()));
         }
       }});
   }
 
   void shutdown() {
     try {
       testServer.shutdown();
       if (subprocess != null) {
         subprocess.destroy();
         subprocess.waitFor();
       }
     } catch (InterruptedException e) { throw new RuntimeException(e); }
   }
 
   static String joinPath(String[] ps) {
     final StringBuilder b = new StringBuilder(128);
     for (String p : ps) b.append(p).append(":");
     return b.toString();
   }
 
   static int pow(int in) { return (int)Math.pow(10, in/100d); }
   static GridDataFactory gridData() { return GridDataFactory.fillDefaults(); }
 
   @Override public void dispose() { shutdown(); }
 
  @Override public void setFocus() { throttle.setFocus(); }
 
   private void applyThrottle() {
     testServer.intensity(pow(throttle.getSelection()));
   }
 }
