 package com.ingemark.perftest.plugin.ui;
 
 import static com.ingemark.perftest.plugin.StressTestActivator.RUN_SCRIPT_EVTYPE;
 import static com.ingemark.perftest.plugin.StressTestActivator.STATS_EVTYPE_BASE;
 import static org.eclipse.jface.dialogs.MessageDialog.openError;
 
 import java.io.InputStream;
 
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
 
 import com.ingemark.perftest.IStressTester;
 import com.ingemark.perftest.RequestProvider;
 import com.ingemark.perftest.Script;
 import com.ingemark.perftest.Stats;
 import com.ingemark.perftest.StressTester;
 import com.ingemark.perftest.script.Parser;
 
 public class RequestAgeView extends ViewPart
 {
   private static final int MIN_THROTTLE = 70;
   public static Composite statsParent;
   Scale throttle;
   Display disp;
   IStressTester stressTester = StressTester.NULL;
   Stats stats = new Stats();
 
 
   public void createPartControl(final Composite p) {
     disp = Display.getDefault();
     final GridLayout l = new GridLayout(2, false);
     p.setLayout(l);
     throttle = new Scale(p, SWT.VERTICAL);
     throttle.setMinimum(MIN_THROTTLE);
     throttle.setMaximum(330);
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
         final Script script = new Parser((InputStream) event.data).parse();
         stressTester.shutdown();
         statsParent.dispose();
         newStatsParent(p);
         for (RequestProvider rp : script.testReqs) {
           final HistogramViewer histogram = new HistogramViewer(statsParent);
           gridData().grab(true, true).applyTo(histogram.canvas);
           statsParent.addListener(STATS_EVTYPE_BASE + rp.liveStats.index, new Listener() {
             public void handleEvent(Event event) { histogram.statsUpdate((Stats) event.data); }
           });
         }
         p.layout(true);
         try {
           stressTester = new StressTester(statsParent, script);
           throttle.setSelection(MIN_THROTTLE);
          applyThrottle();
           stressTester.runTest();
         }
         catch (Throwable t) {
           openError(null, "Stress test init error", String.format(
               "%s: %s", t.getClass().getSimpleName(), t.getMessage()));
         }
       }});
   }
 
   static int pow(int in) { return (int)Math.pow(10, in/100d); }
   static GridDataFactory gridData() { return GridDataFactory.fillDefaults(); }
 
   @Override public void dispose() { stressTester.shutdown(); }
   @Override public void setFocus() { }
 
   private void applyThrottle() {
     stressTester.setIntensity(pow(throttle.getSelection()));
   }
 }
