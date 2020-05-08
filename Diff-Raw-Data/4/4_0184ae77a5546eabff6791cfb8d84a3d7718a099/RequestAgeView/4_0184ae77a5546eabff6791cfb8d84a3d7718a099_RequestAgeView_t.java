 package com.ingemark.requestage.plugin.ui;
 
 import static com.ingemark.requestage.Message.EXCEPTION;
 import static com.ingemark.requestage.Util.gridData;
 import static com.ingemark.requestage.Util.now;
 import static com.ingemark.requestage.plugin.RequestAgePlugin.EVT_ERROR;
 import static com.ingemark.requestage.plugin.RequestAgePlugin.EVT_INIT_HIST;
 import static com.ingemark.requestage.plugin.RequestAgePlugin.EVT_REPORT;
 import static com.ingemark.requestage.plugin.RequestAgePlugin.EVT_RUN_SCRIPT;
 import static com.ingemark.requestage.plugin.RequestAgePlugin.EVT_STATS;
 import static com.ingemark.requestage.plugin.RequestAgePlugin.averageCharWidth;
 import static com.ingemark.requestage.plugin.RequestAgePlugin.globalEventHub;
 import static com.ingemark.requestage.plugin.RequestAgePlugin.lineHeight;
 import static com.ingemark.requestage.plugin.RequestAgePlugin.requestAgePlugin;
 import static com.ingemark.requestage.plugin.RequestAgePlugin.threeDigitFormat;
 import static com.ingemark.requestage.plugin.ui.HistogramViewer.DESIRED_HEIGHT;
 import static com.ingemark.requestage.plugin.ui.HistogramViewer.minDesiredWidth;
 import static java.lang.Math.max;
 import static java.lang.Math.min;
 import static java.lang.Math.signum;
 import static java.util.concurrent.TimeUnit.MILLISECONDS;
 import static org.slf4j.LoggerFactory.getLogger;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.ControlListener;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Scale;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.ui.part.ViewPart;
 import org.slf4j.Logger;
 
 import com.ingemark.requestage.DialogInfo;
 import com.ingemark.requestage.IStressTestServer;
 import com.ingemark.requestage.InitInfo;
 import com.ingemark.requestage.Message;
 import com.ingemark.requestage.Stats;
 import com.ingemark.requestage.StatsHolder;
 import com.ingemark.requestage.StressTestServer;
 
 public class RequestAgeView extends ViewPart
 {
   static final Logger log = getLogger(RequestAgeView.class);
   private static final Runnable DO_NOTHING = new Runnable() { public void run() {} };
   static Color gridColor;
   public static RequestAgeView requestAgeView;
   public Composite statsParent;
   private volatile IStressTestServer testServer = StressTestServer.NULL;
   volatile History[] histories = {};
   volatile long start;
   private Composite viewParent;
   private Label scriptsRunning;
   private boolean showScriptsRunning;
   private long numbersLastUpdated;
   private ProgressDialog pd;
   private Scale throttle;
   private double throttleScalingFactor;
   private Action stopAction, reportAction;
 
   public void createPartControl(Composite p) {
     this.viewParent = new Composite(p, SWT.NONE);
     final Display disp = p.getDisplay();
     gridColor = new Color(disp, 240, 240, 240);
     final Color colWhite = disp.getSystemColor(SWT.COLOR_WHITE);
     viewParent.setBackground(colWhite);
     requestAgeView = this;
     viewParent.setLayout(new GridLayout(2, false));
     stopAction = new Action() { public void run() {
       pd = new ProgressDialog("Shutting down Stress Test", 15, DO_NOTHING).cancelable(false);
       shutdownAndThen(new Runnable() { public void run() { pd.pm().done(); }});
     }};
     stopAction.setImageDescriptor(requestAgePlugin().imageDescriptor("stop.gif"));
     stopAction.setDisabledImageDescriptor(requestAgePlugin().imageDescriptor("stop_disabled.gif"));
     reportAction = new Action() { public void run() {
       statsParent.notifyListeners(EVT_REPORT, null);
     }};
     reportAction.setImageDescriptor(requestAgePlugin().imageDescriptor("report.gif"));
     reportAction.setEnabled(true);
     stopAction.setEnabled(false);
     final IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();
     toolbar.add(stopAction);
     toolbar.add(reportAction);
     final Composite leftSide = new Composite(viewParent, SWT.NONE);
     leftSide.setLayout(new GridLayout(1, false));
     scriptsRunning = new Label(leftSide, SWT.NONE);
     gridData().minSize(7*averageCharWidth(), lineHeight()).grab(true,false).applyTo(scriptsRunning);
     throttle = new Scale(leftSide, SWT.VERTICAL);
    throttle.setSize(1, 1); // workaround for SWT bug #422446
     throttle.setMinimum(0);
     throttle.setMaximum(100);
     throttle.setBackground(colWhite);
     throttle.addSelectionListener(new SelectionAdapter() {
       @Override public void widgetSelected(SelectionEvent e) { applyThrottle(); }
     });
     newStatsParent();
   }
 
   public History[] histories() { return histories; }
 
   void newStatsParent() {
     scriptsRunning.setText("");
     if (statsParent != null) statsParent.dispose();
     statsParent = new Composite(viewParent, SWT.NONE);
     statsParent.setLayout(new FillLayout());
     gridData().grab(true, true).applyTo(statsParent);
     statsParent.addListener(EVT_RUN_SCRIPT, new Listener() {
       public void handleEvent(final Event event) {
         try {
           pd = new ProgressDialog("Starting Stress Test", 203, new Runnable() {
             @Override public void run() { shutdownAndThen(DO_NOTHING); }
           });
           newStatsParent();
           final TabFolder tabs = new TabFolder(statsParent, SWT.NONE);
           final Composite histsParent = tab(tabs, "request_age"),
                           distsParent = tab(tabs, "resp_time_dist");
           final TabItem cumulativeTab = new TabItem(tabs, SWT.NONE);
           cumulativeTab.setText("resp_time_dist_cumul");
           cumulativeTab.setControl(distsParent);
           statsParent.addListener(EVT_INIT_HIST, new Listener() {
             @Override public void handleEvent(Event event) {
               log.debug("Init histogram");
               start = System.currentTimeMillis();
               final Listener statsListener = new Listener() { public void handleEvent(Event e) {
                   statsParent.notifyListeners(EVT_STATS, e);
                   if (!showScriptsRunning) return;
                   final long now = now();
                   if (now-numbersLastUpdated > MILLISECONDS.toNanos(200)) {
                     numbersLastUpdated = now;
                     scriptsRunning.setText(
                         threeDigitFormat(((StatsHolder)e.data).scriptsRunning, false));
                   }
               }};
               globalEventHub().addListener(EVT_STATS, statsListener);
               statsParent.addDisposeListener(new DisposeListener() {
                 @Override public void widgetDisposed(DisposeEvent e) {
                   globalEventHub().removeListener(EVT_STATS, statsListener);
                 }
               });
               final InitInfo info = (InitInfo)event.data;
               final int size = info.reqNames.length;
               showScriptsRunning = info.showRunningScriptCount;
               throttleScalingFactor = info.maxThrottle/100.0;
               histories = new History[size];
               final HistogramViewer[] hists = new HistogramViewer[size];
               for (int i = 0; i < size; i++) {
                 final HistogramViewer histogram = hists[i] = new HistogramViewer(i, histsParent);
                 gridData().grab(true, true).applyTo(histogram.canvas);
                 final RespDistributionViewer distViewer =
                     new RespDistributionViewer(i, info.reqNames[i], distsParent);
                 gridData().grab(true, true).applyTo(distViewer.chart);
                   tabs.addSelectionListener(new SelectionListener() {
                     @Override public void widgetSelected(SelectionEvent e) {
                       final int ind = tabs.getSelectionIndex();
                       if (ind > 0) distViewer.setCumulative(ind == 2);
                     }
                     @Override public void widgetDefaultSelected(SelectionEvent e) {}
                   });
                 histories[i] = new History(i, info.reqNames[i]);
                 for (Listener l : new Listener[] {histogram, distViewer, histories[i]})
                   statsParent.addListener(EVT_STATS, l);
                 histogram.canvas.addMouseListener(new MouseListener() {
                   @Override public void mouseDoubleClick(MouseEvent e) {
                     testServer.send(new Message(EXCEPTION, histogram.stats.name));
                   }
                   @Override public void mouseUp(MouseEvent e) {}
                   @Override public void mouseDown(MouseEvent e) {}
                 });
               }
               statsParent.addListener(EVT_REPORT, new Listener() {
                 @Override public void handleEvent(Event event) {
                   final List<Stats> statsList = new ArrayList<Stats>();
                   for (HistogramViewer hist : hists) statsList.add(hist.stats);
                   ReportDialog.show(testServer.testName(), statsList);
                 }
               });
               statsParent.addControlListener(new ControlListener() {
                 @Override public void controlResized(ControlEvent e) {
                   final Rectangle bounds = statsParent.getBounds();
                   final int
                     availRows = max(1, bounds.height/DESIRED_HEIGHT),
                     maxCols = size/availRows + (int)signum(size % availRows),
                     desiredCols = max(1, min(maxCols, bounds.width / minDesiredWidth));
                   GridLayout layout = (GridLayout)histsParent.getLayout();
                   if (desiredCols == layout.numColumns) return;
                   layout.numColumns = desiredCols;
                   histsParent.setLayout(layout);
                   layout = (GridLayout)distsParent.getLayout();
                   layout.numColumns = desiredCols;
                   distsParent.setLayout(layout);
                 }
                 @Override public void controlMoved(ControlEvent e) {}
               });
               throttle.setSelection(0);
               applyThrottle();
               viewParent.layout(true);
               histsParent.layout(true);
               distsParent.layout(true);
             }});
           statsParent.addListener(EVT_ERROR, new Listener() {
             @Override public void handleEvent(Event e) {
               stopAction.setEnabled(false);
               if (pd != null) pd.close();
               InfoDialog.show(new DialogInfo("Stress testing error", ((String)e.data)));
             }
           });
           shutdownAndThen(new Runnable() { public void run() {
             testServer = new StressTestServer((String)event.data).progressMonitor(pd.pm());
             testServer.start();
             stopAction.setEnabled(true);
           }});
         }
         catch (Throwable t) {
           if (pd != null) pd.close();
           InfoDialog.show(new DialogInfo("Stress test init error", t));
         }
       }});
   }
 
   private Composite tab(TabFolder tabs, String title) {
     final TabItem tabItem = new TabItem(tabs, SWT.NONE);
     tabItem.setText(title);
     final Composite tabPane = new Composite(tabs, SWT.NONE);
     tabItem.setControl(tabPane);
     final GridLayout gridLayout = new GridLayout(2, false);
     gridLayout.marginHeight = gridLayout.marginWidth = 0;
     tabPane.setLayout(gridLayout);
     tabPane.setBackground(tabPane.getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
     return tabPane;
   }
 
   private void shutdownAndThen(Runnable andThen) {
     testServer.progressMonitor(pd != null? pd.pm() : null);
     stopAction.setEnabled(false);
     final IStressTestServer ts = testServer;
     testServer = StressTestServer.NULL;
     ts.shutdown(andThen);
   }
 
   @Override public void dispose() { shutdownAndThen(DO_NOTHING);}
 
   @Override public void setFocus() { throttle.setFocus(); }
 
   private void applyThrottle() {
     testServer.intensity((int) (throttleScalingFactor * throttle.getSelection()));
   }
 
   public static void main(String[] args) {
     final Display d = Display.getDefault();
     final Shell top = new Shell(d);
     top.setLayout(new RowLayout());
    new Scale(top, SWT.VERTICAL).setSize(1,1);;
     top.pack();
     top.setVisible(true);
     while (!top.isDisposed()) if (!d.readAndDispatch()) d.sleep();
   }
 }
