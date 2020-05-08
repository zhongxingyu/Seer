 /**
  * Copyright (C) 2011 (nick @ objectdefinitions.com)
  *
  * This file is part of JTimeseries.
  *
  * JTimeseries is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * JTimeseries is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with JTimeseries.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.od.jtimeseries.ui.timeseries;
 
 import com.od.jtimeseries.net.httpd.HttpParameterName;
 import com.od.jtimeseries.ui.config.UiTimeSeriesConfig;
 import com.od.jtimeseries.ui.util.ExecuteWeakReferencedCommandTask;
 import com.od.jtimeseries.ui.util.LocalJmxMetrics;
 import com.od.jtimeseries.util.NamedExecutors;
 import com.od.jtimeseries.util.logging.LogMethods;
 import com.od.jtimeseries.util.logging.LogUtils;
 import com.od.jtimeseries.util.time.Time;
 import com.od.jtimeseries.util.time.TimePeriod;
 import swingcommand.BackgroundTask;
 import swingcommand.SwingCommand;
 import swingcommand.Task;
 import swingcommand.TaskListenerAdapter;
 
 import javax.swing.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.*;
 import java.util.concurrent.Executor;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Nick Ebbutt
  * Date: 12-Jan-2009
  * Time: 09:43:39
  *
  * A time series used by the ui, which loads its data from a remote server
  * Property change events should only be fired on the AWT
  */
 public class RemoteHttpTimeSeries extends DefaultUITimeSeries implements ChartSeriesListener {
 
     private static final long STARTUP_TIME = System.currentTimeMillis();
 
     private static final LogMethods logMethods = LogUtils.getLogMethods(RemoteHttpTimeSeries.class);
 
     private final Map<UIPropertiesTimeSeries, String> weakClientSeries = new WeakHashMap<UIPropertiesTimeSeries, String>();
 
     private static ScheduledExecutorService scheduleExecutor = NamedExecutors.newSingleThreadScheduledExecutor("RemoteHttpTimeSeries-Schedule");
     private static Executor seriesLoadExecutor = NamedExecutors.newFixedThreadPool("RemoteHttpTimeSeries-Query", 3);
 
     private static final int MIN_REFRESH_TIME_SECONDS = 10;
 
     private RefreshDataCommand refreshDataCommand = new RefreshDataCommand();
     private volatile ScheduledFuture refreshTask;
     private volatile int displayedChartCount;
     private volatile int failureCount;
 
     //until series is loaded or becomes stale, try again at this frequency
     private static final int NOT_LOADED_REFRESH_TIME_SECONDS = 20;
     //a series starts not 'stale' and remains not stale until a set number of consecutive download failures have occurred
     private static final int MAX_FAILURES_BEFORE_STALE = 2;
 
     private static final int NOT_TICKING_REFRESH_TIME_SECONDS = 900; //15 mins
     private static final int TICKING_FLAG_HOURS_SINCE_LAST_UPDATE = 4; //if no new data for this time a series is considered 'not ticking'
 
 
     RemoteHttpTimeSeries(UiTimeSeriesConfig config) throws MalformedURLException {
         this(config.getId(), config.getDescription(), new URL(config.getTimeSeriesUrl()), Time.seconds(config.getRefreshTimeSeconds()));
         setDisplayName(config.getDisplayName());
     }
 
     RemoteHttpTimeSeries(String id, String description, URL timeSeriesUrl, TimePeriod refreshTime) {
         super(id, description);
         setTimeSeriesURL(timeSeriesUrl);
         setRefreshFrequencySeconds((int) refreshTime.getLengthInMillis() / 1000);
         LocalJmxMetrics.getInstance().getHttpSeriesCount().incrementCount();
     }
 
     public void setRefreshFrequencySeconds(int seconds) {
         super.setRefreshFrequencySeconds(Math.max(seconds, MIN_REFRESH_TIME_SECONDS));
         scheduleRefreshIfDisplayedAndNotStale(false);
     }
 
     public void setStale(boolean stale) {
         super.setStale(stale);
         if (!stale) {
             failureCount = 0;
             scheduleRefreshIfDisplayedAndNotStale(true);
         }
     }
 
     private int calculateRefreshTime() {
         if ( ! isLoaded() ) {
             return NOT_LOADED_REFRESH_TIME_SECONDS;  //request failed, want to try again quite soon
         } else {
             return isTicking() ? getRefreshFrequencySeconds() : NOT_TICKING_REFRESH_TIME_SECONDS;
         }
     }
 
     /**
      * A flag which indicates whether a series in the server is currently 'active' / growing
      * If not, we don't want to hit the server frequently to get new data points
      */
     private void setTickingFlag() {
         long timeSinceUpdate = System.currentTimeMillis() - getLatestTimestamp();
 
        //always treat no values as ticking - this might be a new series, waiting for the first value
        boolean seriesHasNoValues = size() == 0;
 
        //TODO - a better algorithm to determine 'ticking' state, taking into account frequency of updates
        boolean ticking = timeSinceUpdate < Time.hours(TICKING_FLAG_HOURS_SINCE_LAST_UPDATE).getLengthInMillis() || seriesHasNoValues;
         setTicking(ticking);
     }
 
     public void chartSeriesChanged(ChartSeriesEvent e) {
         boolean refreshImmediately = false;
         switch(e.getChartSeriesEventType()) {
             case SERIES_CHART_DISPLAYED:
                 refreshImmediately = true;
                 weakClientSeries.put(e.getSourceSeries(), "UNUSED");
                 break;
             case SERIES_CHART_HIDDEN:
                 weakClientSeries.put(e.getSourceSeries(), "UNUSED");
                 break;
             case SERIES_CHART_DISPOSED:
                 weakClientSeries.remove(e.getSourceSeries());
                 break;
             default:
         }
         scheduleRefreshIfDisplayedAndNotStale(refreshImmediately);
     }
 
     //Cancel any existing task and schedule a new one if series selected
     private synchronized void scheduleRefreshIfDisplayedAndNotStale(boolean immediateRefresh) {
         updateDisplayedChartCount();
 
         if ( refreshTask != null) {
             refreshTask.cancel(false);
         }
 
         if ( displayedChartCount > 0 && ! isStale()) {  //don't refresh if series stale
 
             //A task which doesn't hold a strong reference to this series
             //the series can be collected if no longer referenced elsewhere, even if a refresh is scheduled
             ExecuteWeakReferencedCommandTask runCommandTask = new ExecuteWeakReferencedCommandTask(refreshDataCommand);
 
             if ( immediateRefresh) {
                 scheduleExecutor.execute(runCommandTask);
             }
 
             int refreshTime = calculateRefreshTime();
             refreshTask = scheduleExecutor.schedule(
                 runCommandTask, refreshTime, TimeUnit.SECONDS
             );
         }
     }
 
     private void updateDisplayedChartCount() {
         Set<UIPropertiesTimeSeries> series;
         synchronized(weakClientSeries) {
             series = new HashSet<UIPropertiesTimeSeries>(weakClientSeries.keySet());
         }
 
         int displayedCount = 0;
         for ( UIPropertiesTimeSeries s : series) {
             if ( s.isSelected()) {
                 displayedCount++;
             }
         }
         this.displayedChartCount = displayedCount;
     }
 
     private class RefreshDataCommand extends SwingCommand {
 
         private URL urlForQuery;
 
         public RefreshDataCommand() {
 
             //execute() is called on scheduler thread, the actual execution takes place on
             //seriesLoadExecutor thread pool, so the tasks will get pending() callback immediately,
             //and can start showing progress, but will not get started() until a seriesLoadExecutor
             //thread is free
             setExecutor(seriesLoadExecutor);
 
             //if we exceed the error count when running the load, set the series to stale
             //the user will need to re-enable it to start the load off again
             addTaskListener(new SetStaleOnErrorListener());
             addTaskListener(new RescheduleListener());
             addTaskListener(new SetLoadingListener());
         }
 
         protected Task createTask() {
             return new BackgroundTask() {
 
                 protected void doInBackground() throws Exception {
                     urlForQuery = getUrlWithTimestamp();
                     new DownloadRemoteTimeSeriesDataQuery(RemoteHttpTimeSeries.this, urlForQuery).runQuery();
                 }
 
                 private URL getUrlWithTimestamp() throws MalformedURLException {
                     return new URL(
                         getTimeSeriesURL() + "?" + HttpParameterName.moreRecentThanTimestamp.name() + "=" + getEarliestItemToFetch() + "&" + HttpParameterName.statsOnly + "=" + (displayedChartCount == 0)
                     );
                 }
 
                 //the later of the last current timepoint or the earliest point calculated using max days history
                 private long getEarliestItemToFetch() {
                     return getLatestTimestamp();
                 }
 
                 protected void doInEventThread() throws Exception {
                     failureCount = 0;
                     setLastRefreshTime(new Date());
                     setLoaded(true);
                 }
 
                 public String toString() {
                     return "SeriesLoad for URL " + urlForQuery;
                 }
             };
         }
 
         //perform disconnection if task failed too many times
         private class SetStaleOnErrorListener extends TaskListenerAdapter {
 
             public void error(Task task, Throwable error) {
                 logMethods.logWarning("Failed to load series " + task + " " + error.getClass().getSimpleName());
                 failureCount++;
                 if ( failureCount >= MAX_FAILURES_BEFORE_STALE) {
                     setStale(true);
                 }
             }
         }
 
         private class RescheduleListener extends TaskListenerAdapter {
             public void finished(Task task) {
                 setTickingFlag();
                 scheduleRefreshIfDisplayedAndNotStale(false);
             }
         }
 
         private class SetLoadingListener extends TaskListenerAdapter {
 
             public void pending(Task task) {
                 setLoading(true);
             }
 
             public void finished(Task task) {
                 setLoading(false);
             }
 
         }
     }
 
     public static void updateSummaryStats(UiTimeSeriesConfig config, Properties summaryStats) {
         final RemoteHttpTimeSeries s = RemoteHttpTimeSeriesCollection.getWeakReferencedSeries(config);
         if (s != null) {
             s.updateSummaryStats(summaryStats);
         }
     }
 
     protected void updateSummaryStats(Properties summaryStats) {
         putAllProperties(summaryStats);
 
         //at present all UI times series properties must
         //be set on the swing event thread since jide
         //bean table model expects this
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 setStatsRefreshTime(new Date());
             }
         });
     }
 
     public void finalize() throws Throwable {
         super.finalize();
         LocalJmxMetrics.getInstance().getHttpSeriesCount().decrementCount();
     }
 
 }
