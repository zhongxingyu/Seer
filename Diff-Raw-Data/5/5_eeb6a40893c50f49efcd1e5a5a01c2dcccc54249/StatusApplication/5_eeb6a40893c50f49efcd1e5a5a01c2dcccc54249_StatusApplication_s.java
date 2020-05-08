 package be.mira.adastra3.server.website.status;
 
 import be.mira.adastra3.server.website.data.DeferredExecution;
 import be.mira.adastra3.server.website.data.NetworkDetail;
 import be.mira.adastra3.server.website.data.NetworkItem;
 import be.mira.adastra3.server.website.data.NetworkModel;
 import be.mira.adastra3.server.website.data.TreeItem;
 import eu.webtoolkit.jwt.SelectionBehavior;
 import eu.webtoolkit.jwt.SelectionMode;
 import eu.webtoolkit.jwt.Signal;
 import eu.webtoolkit.jwt.WApplication;
 import eu.webtoolkit.jwt.WContainerWidget;
 import eu.webtoolkit.jwt.WEnvironment;
 import eu.webtoolkit.jwt.WGridLayout;
 import eu.webtoolkit.jwt.WLabel;
 import eu.webtoolkit.jwt.WLength;
 import eu.webtoolkit.jwt.WModelIndex;
 import eu.webtoolkit.jwt.WTabWidget;
 import eu.webtoolkit.jwt.WTextArea;
 import eu.webtoolkit.jwt.WTimer;
 import eu.webtoolkit.jwt.WTreeView;
 import java.util.SortedSet;
 import org.apache.log4j.AppenderSkeleton;
 import org.apache.log4j.Logger;
 import org.apache.log4j.SimpleLayout;
 import org.apache.log4j.spi.LoggingEvent;
 
 /*
  * A simple hello world application class which demonstrates how to react
  * to events, read input, and give feed-back.
  */
 public class StatusApplication extends WApplication {
     //
     // Data members
     //
     
     WTimer mTimer;
     
     NetworkModel mNetworkModel;
     WTreeView mNetworkView;
     NetworkDetail mNetworkDetail;
     LogAppender mLogAppender;
     
     
     //
     // Construction and destruction
     //
     
     public StatusApplication(WEnvironment iEnvironment) {
         super(iEnvironment);
         
         setCssTheme("polished");        
         setTitle("Status page");        
         createUI();
         
         // Schedule deferred executions
         mTimer = new WTimer(getRoot());
         mTimer.setInterval(1000);
         mTimer.timeout().addListener(this, new Signal.Listener() {
             @Override
             public void trigger() {
                 for (DeferredExecution tDeferree : DeferredExecution.DEFERREES) {
                     tDeferree.execute();
                 }
                 DeferredExecution.DEFERREES.clear();
             }            
         });
         mTimer.start();
     }
     
     @Override
    protected void finalize() throws Throwable {
        super.finalize();
         mNetworkModel.detach();
         
         Logger tLogger = Logger.getRootLogger();
         tLogger.removeAppender(mLogAppender);
     }
     
     
     //
     // UI creation
     //
     
     private void createUI() {        
         // Tabs
         WTabWidget tTabs = new WTabWidget(getRoot());
         tTabs.addTab(createNetwork(), "Network");
         tTabs.addTab(createRepository(), "Repository");
         tTabs.addTab(createLog(), "Log");
     }
     
     private WContainerWidget createNetwork() {
         // Layout
         WContainerWidget tNetwork = new WContainerWidget();
         WGridLayout tNetworkLayout = new WGridLayout();
         tNetworkLayout.setColumnResizable(0);
         tNetworkLayout.setRowStretch(0, 1);
         tNetworkLayout.setColumnStretch(1, 1);
         tNetwork.setLayout(tNetworkLayout);
         
         // Network view
         mNetworkModel = new NetworkModel();
         mNetworkModel.attach();
         mNetworkView = new WTreeView(tNetwork);
         mNetworkView.setModel(mNetworkModel);
         mNetworkView.selectionChanged().addListener(this, new SelectionChanged());
         mNetworkView.setSelectionMode(SelectionMode.SingleSelection);
         mNetworkView.setSelectionBehavior(SelectionBehavior.SelectRows);
         mNetworkView.expandToDepth(2);
         mNetworkView.resize(new WLength(400), new WLength(750));   // FIXME
         tNetworkLayout.addWidget(mNetworkView, 0, 0);
         
         // Network detail
         mNetworkDetail = new NetworkDetail(tNetwork);
         tNetworkLayout.addWidget(mNetworkDetail, 0, 1);
         
         return tNetwork;        
     }
     
     private WContainerWidget createRepository() {
         // Layout
         WContainerWidget tRepository = new WContainerWidget();
         
         // Repository view
         tRepository.addWidget(new WLabel("WIP"));
         
         return tRepository;
     }
     
     private WContainerWidget createLog() {
         // Layout
         WContainerWidget tLog = new WContainerWidget();
         
         // Text area
         WTextArea tLogText = new WTextArea(tLog);
         tLogText.setReadOnly(true);
         tLogText.setSelectable(true);
         
         // Setup logging
         Logger tLogger = Logger.getRootLogger();
         mLogAppender = new LogAppender(tLogText);
         mLogAppender.setLayout(new SimpleLayout());
         tLogger.addAppender(mLogAppender);
         
         return tLog;
     }
     
     
     //
     // UI events
     //
     
     private class SelectionChanged implements Signal.Listener {
         @Override
         public void trigger() {
             // Get the network item
             SortedSet<WModelIndex> tSelected = mNetworkView.getSelectedIndexes();
             NetworkItem tNetworkItem = null;
             if (tSelected.size() == 1) {
                 TreeItem tItem = mNetworkModel.getItem(tSelected.first());
                 if (tItem instanceof NetworkItem)
                     tNetworkItem = (NetworkItem) tItem;
             }
             mNetworkDetail.showDetail(tNetworkItem);
         }        
     }
     
     
     //
     // Subclasses
     //
     
     private class LogAppender extends AppenderSkeleton {
         private WTextArea mLogText;
         
         public LogAppender(WTextArea iLogText) {
             super();
             mLogText = iLogText;
             mLogText.setText("");
         }
         
         @Override
         public void close() {
             mLogText.setText("");
         }
 
         @Override
         public boolean requiresLayout() {
             return true;
         }
         
         @Override
         protected void append(LoggingEvent event) {
             if (event != null) {
                 DeferredExecution.DEFERREES.add(new DeferredExecution() {
                     String mMessage;
                     public DeferredExecution construct(String iMessage) {
                         mMessage = iMessage;
                         return this;
                     }
                     @Override
                     public void execute() {
                         mLogText.setText( mLogText.getText() + mMessage);
                     }
                 }.construct(getLayout().format(event)));
             }
         }
     }
 }
