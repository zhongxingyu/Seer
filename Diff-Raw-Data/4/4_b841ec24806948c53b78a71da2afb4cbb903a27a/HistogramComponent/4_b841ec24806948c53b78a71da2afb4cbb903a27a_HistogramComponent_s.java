 package md.frolov.legume.client.ui.components;
 
 import java.util.Date;
 import java.util.Map;
 
 import com.github.gwtbootstrap.client.ui.Button;
 import com.github.gwtbootstrap.datetimepicker.client.ui.DateTimeBox;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.logical.shared.ResizeEvent;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.place.shared.PlaceController;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.InlineLabel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ResizeLayoutPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.web.bindery.event.shared.EventBus;
 import com.googlecode.gflot.client.DataPoint;
 import com.googlecode.gflot.client.PlotSelectionArea;
 import com.googlecode.gflot.client.Series;
 import com.googlecode.gflot.client.SeriesHandler;
 import com.googlecode.gflot.client.SimplePlot;
 import com.googlecode.gflot.client.event.PlotClickListener;
 import com.googlecode.gflot.client.event.PlotHoverListener;
 import com.googlecode.gflot.client.event.PlotItem;
 import com.googlecode.gflot.client.event.PlotPosition;
 import com.googlecode.gflot.client.event.PlotSelectedListener;
 import com.googlecode.gflot.client.jsni.Plot;
 import com.googlecode.gflot.client.options.*;
 import com.googlecode.gflot.client.options.side.IntegerSideOptions;
 
 import md.frolov.legume.client.Application;
 import md.frolov.legume.client.activities.stream.StreamPlace;
 import md.frolov.legume.client.elastic.ElasticSearchService;
 import md.frolov.legume.client.elastic.api.Callback;
 import md.frolov.legume.client.elastic.api.HistogramInterval;
 import md.frolov.legume.client.elastic.api.HistogramRequest;
 import md.frolov.legume.client.elastic.api.HistogramResponse;
 import md.frolov.legume.client.events.FocusOnDateEvent;
 import md.frolov.legume.client.events.LogMessageHoverEvent;
 import md.frolov.legume.client.events.LogMessageHoverEventHandler;
 import md.frolov.legume.client.events.UpdateSearchQuery;
 import md.frolov.legume.client.events.UpdateSearchQueryHandler;
 import md.frolov.legume.client.gin.WidgetInjector;
 import md.frolov.legume.client.model.Search;
 import md.frolov.legume.client.ui.EventFlowPanel;
 import md.frolov.legume.client.util.ConversionUtils;
 
 /** @author Ivan Frolov (ifrolov@tacitknowledge.com) */
 public class HistogramComponent extends Composite implements UpdateSearchQueryHandler, LogMessageHoverEventHandler
 {
     private static final int MAXIMUM_STEPS = 2000; //TODO constraint to client width?
 
     interface HistogramComponentUiBinder extends UiBinder<Widget, HistogramComponent>
     {
     }
     interface Css extends CssResource {
         String disabled();
         String dateControlsVisible();
     }
 
     private static HistogramComponentUiBinder binder = GWT.create(HistogramComponentUiBinder.class);
 
     private static final DateTimeFormat DATE_LABEL_DTF = DateTimeFormat.getFormat("dd/MM/yyyy HH:mm:ss");
     private static final NumberFormat NUMBER_FORMAT = NumberFormat.getFormat("#,###");
 
     @UiField(provided = true)
     SimplePlot plot;
     @UiField
     ResizeLayoutPanel resizePanel;
     @UiField
     FlowPanel loading;
     @UiField
     FlowPanel error;
     @UiField
     Button zoomIn;
     @UiField
     Button zoomOut;
     @UiField
     Button downloadImage;
     @UiField
     Button trackPosition;
     @UiField
     InlineLabel fromDateLabel;
     @UiField
     Label toDateLabel;
     @UiField
     InlineLabel hitsLabel;
     @UiField
     Button chooseDateButton;
     @UiField
     FlowPanel dateControlsPanel;
 
     @UiField
     Css css;
     @UiField
     Button hideDateButton;
     @UiField
     Button goButton;
     @UiField
     DateTimeBox fromBox;
     @UiField
     DateTimeBox toBox;
     @UiField
     FlowPanel hoverInfo;
     @UiField
     Label countHoverInfo;
     @UiField
     Label dateHoverInfo;
     @UiField
     EventFlowPanel plotPanel;
 
     private EventBus eventBus = WidgetInjector.INSTANCE.eventBus();
     private ElasticSearchService elasticSearchService = WidgetInjector.INSTANCE.elasticSearchService();
     private Application application = WidgetInjector.INSTANCE.application();
     private PlaceController placeController = WidgetInjector.INSTANCE.placeController();
     private ConversionUtils conversionUtils = ConversionUtils.INSTANCE;
 
     private Search currentSearch;
     private Search queuedSearch;
     private boolean inprocess = false;
     private boolean ignoreClickEvent = false;
     private HistogramInterval currentInterval;
 
     public HistogramComponent()
     {
         initPlot();
         initWidget(binder.createAndBindUi(this));
 
         eventBus.addHandler(UpdateSearchQuery.TYPE, this);
         eventBus.addHandler(LogMessageHoverEvent.TYPE, this);
     }
 
     private void initPlot()
     {
         PlotOptions plotOptions = PlotOptions.create();
 
         // add tick formatter to the options
         plotOptions.addYAxisOptions(AxisOptions.create().setTickColor("#fafafa"));
 
         //Styling
         plotOptions.setGridOptions(GridOptions.create().setBorderWidth(IntegerSideOptions.of(0, 0, 1, 0)).setBorderColor("#999")
                 .setClickable(true).setHoverable(true).setAutoHighlight(true));
         plotOptions.setSelectionOptions(SelectionOptions.create().setMode(SelectionOptions.SelectionMode.X).setColor("#ccc"));
         plotOptions.setGlobalSeriesOptions(GlobalSeriesOptions.create().setShadowSize(0).setLineSeriesOptions(
                 LineSeriesOptions.create().setFill(true).setSteps(true).setZero(true).setLineWidth(1)
         ));
 
         //Crosshair
         plotOptions.setCrosshairOptions(CrosshairOptions.create().setColor("hsl(210,60%,70%)").setLineWidth(1).setMode(CrosshairOptions.Mode.X));
 
         // create the plot
         plot = new SimplePlot(plotOptions);
 
         //add listeners
         plot.addSelectedListener(new PlotSelectedListener()
         {
             @Override
             public void onPlotSelected(final PlotSelectionArea area)
             {
                 long from = area.getX().getFrom().longValue();
                 long to = area.getX().getTo().longValue();
 
                 ignoreClickEvent = true;
 
                 Search search = application.getCurrentSearch().clone();
                 search.setFromDate(from);
                 search.setToDate(to);
                 search.setFocusDate(from);
                 WidgetInjector.INSTANCE.placeController().goTo(new StreamPlace(search)); //TODO change this. It might be useful to zoom in/out when in 'terms' activity
             }
         });
 
         plot.addHoverListener(new PlotHoverListener()
         {
             @Override
             public void onPlotHover(final Plot plot, final PlotPosition position, final PlotItem item)
             {
                 if (item != null && item.getDataPoint().getY() >= 0)
                 {
                     String countStr = NUMBER_FORMAT.format(item.getDataPoint().getY()) + currentInterval.getDescription();
                     countHoverInfo.setText(countStr);
                 }
                 else
                 {
                     countHoverInfo.setText("");
                 }
                 if (position != null)
                 {
                     Date theDate = new Date(position.getX().longValue());
                     String dateStr = DATE_LABEL_DTF.format(theDate);
                     dateHoverInfo.setText(dateStr);
                 }
                 else
                 {
                     dateHoverInfo.setText("");
                 }
                 hoverInfo.setVisible(true);
             }
         }, false);
 
         plot.addClickListener(new PlotClickListener()
         {
             @Override
             public void onPlotClick(final Plot plot, final PlotPosition position, final PlotItem item)
             {
                 if(ignoreClickEvent) {
                     ignoreClickEvent = false;
                     return;
                 }
                 long focusDate = position.getX().longValue();
 
                 eventBus.fireEvent(new FocusOnDateEvent(focusDate));
             }
         }, false);
     }
 
     private void updateHistogramWithData(HistogramResponse response)
     {
         plot.getModel().removeAllSeries();
         SeriesHandler handler = plot.getModel().addSeries(Series.of("", "#999"));
 
         long total = 0;
         for (Map.Entry<Long, Long> entry : response.getDateValueMap().entrySet())
         {
 
             handler.add(DataPoint.of(entry.getKey(), entry.getValue()));
             total += entry.getValue();
         }
 
         plot.getPlotOptions().setXAxesOptions(AxesOptions.create().addAxisOptions(TimeSeriesAxisOptions.create()
                 .setTickColor("#eee").setReserveSpace(true)
                 .setTimeZone("browser").setTimeFormat(response.getInterval().getDateTimeFormat())
         ));
 
         currentInterval = response.getInterval();
 
         loading.setVisible(false);
         plot.removeStyleName(css.disabled());
         plot.setVisible(true);
         plot.redraw(true);
 
         plot.clearCrosshair();
         plot.lockCrosshair();
 
 
         hitsLabel.setText(NUMBER_FORMAT.format(total));
     }
 
     @Override
     public void onUpdateSearchQuery(final UpdateSearchQuery event)
     {
         if(!isUpdateRequired(event.getSearchQuery())) {
             return;
         } else {
             currentSearch = event.getSearchQuery();
         }
 
         requestHistogram(event.getSearchQuery());
 
         //update labels
         long fromDate = event.getSearchQuery().getFromDate();
         String fromDateStr;
         if (fromDate == 0)
         {
             fromDateStr = "beginning";
         }
         else
         {
             fromDateStr = DATE_LABEL_DTF.format(new Date(event.getSearchQuery().getRealFromDate()));
         }
 
         long toDate = event.getSearchQuery().getToDate();
         String toDateStr;
         if (toDate == 0)
         {
             toDateStr = "now";
         }
         else
         {
             toDateStr = DATE_LABEL_DTF.format(new Date(event.getSearchQuery().getRealToDate()));
         }
 
         fromDateLabel.setText(fromDateStr);
         toDateLabel.setText(toDateStr);
     }
 
     private boolean isUpdateRequired(Search newSearch) {
         if(currentSearch == null) {
             return true;
         }
         return !(newSearch.getQuery().equals(currentSearch.getQuery())
                         && newSearch.getFromDate() == currentSearch.getFromDate()
                         && newSearch.getToDate() == currentSearch.getToDate()
                 );
     }
 
     @UiHandler("resizePanel")
     public void onPanelResize(final ResizeEvent event)
     {
         plot.setWidth(event.getWidth() - 220);
         plot.redraw();
     }
 
     private void checkQueued() {
         if(queuedSearch != null) {
             requestHistogram(queuedSearch);
             queuedSearch = null;
         }
     }
 
     private void requestHistogram(Search search)
     {
         if (inprocess)
         {
             queuedSearch = search;
             return;
         }
 
         inprocess = true;
         plot.addStyleName(css.disabled());
         hoverInfo.setVisible(false);
         error.setVisible(false);
         loading.setVisible(true);
         hitsLabel.setText("n/a");
 
         HistogramRequest request = new HistogramRequest(search, MAXIMUM_STEPS);
         elasticSearchService.query(request, new Callback<HistogramRequest, HistogramResponse>()
         {
             @Override
             public void onFailure(final Throwable exception)
             {
                 loading.setVisible(false);
                 error.setVisible(true);
                 plot.setVisible(false);
                 inprocess = false;
                 checkQueued();
             }
 
             @Override
             public void onSuccess(final HistogramRequest query, final HistogramResponse response)
             {
                 updateHistogramWithData(response);
                 inprocess = false;
                 checkQueued();
             }
         });
     }
 
     @Override
     public void onLogMessageHover(final LogMessageHoverEvent event)
     {
         if (!trackPosition.isToggled())
         {
             return;
         }
 
         Search search = application.getCurrentSearch();
         long selectionDate = event.getDate();
         long toDate = search.getRealToDate();
         long fromDate = search.getRealFromDate();
 
         boolean update = false;
 
         if (selectionDate > toDate)
         {
             update = true;
             long alltime = toDate - fromDate;
             toDate = selectionDate + alltime / 2;
             long now = new Date().getTime();
             if (toDate > now)
             {
                 toDate = now;
             }
             if(fromDate!=0) {
                 fromDate = toDate - alltime;
             }
         }
         else if (selectionDate < fromDate)
         {
             update = true;
             long alltime = toDate - fromDate;
            fromDate = fromDate - alltime / 2;
            toDate = toDate - alltime / 2;
         }
 
         if (update)
         {
             Search newSearch = search.clone();
             newSearch.setFromDate(fromDate);
             newSearch.setToDate(toDate);
             placeController.goTo(new StreamPlace(newSearch)); //TODO TermsActivity would be interested too
         }
         else
         {
             plot.lockCrosshair(PlotPosition.of(event.getDate(), 0));
         }
     }
 
     @UiHandler("zoomIn")
     public void onZoomIn(final ClickEvent event)
     {
         long from = plot.getPlotOptions().getXAxisOptions().getMinimum().longValue();
         long to = plot.getPlotOptions().getXAxisOptions().getMaximum().longValue();
 
         if (to - from < 100)
         {
             return;
         }
 
         long allTime = to - from;
         long fromDate = from + allTime / 3;
         long toDate = to - allTime / 3;
 
         Search search = application.getCurrentSearch().clone();
         search.setFromDate(fromDate);
         search.setToDate(toDate);
         requestHistogram(search);
     }
 
     @UiHandler("zoomOut")
     public void onZoomOut(final ClickEvent event)
     {
         Search search = application.getCurrentSearch();
         long from = search.getRealFromDate();
         long to = search.getRealToDate();
         long now = new Date().getTime();
 
         long allTime = to - from;
         from = from - allTime / 2;
         to = to + allTime / 2;
 
         if (to > now)
         {
             from = from - (to - now);
             to = now;
         }
         Search newSearch = search.clone();
         newSearch.setFromDate(from);
         newSearch.setToDate(to);
         requestHistogram(newSearch); //TODO probably a new place?
     }
 
     @UiHandler("downloadImage")
     public void onDownloadImage(final ClickEvent event)
     {
         if (plot.isExportAsImageEnabled())
         {
             plot.saveAsImage();
         }
         else
         {
             Window.alert("Sorry. This is not supported in your browser");
         }
     }
 
     @UiHandler("chooseDateButton")
     public void onChooseDateButtonClick(final ClickEvent event)
     {
         Search search = application.getCurrentSearch();
 
         fromBox.setValue(new Date(search.getRealFromDate()));
         toBox.setValue(new Date(search.getRealToDate()));
 
         dateControlsPanel.addStyleName(css.dateControlsVisible());
         hideDateButton.setVisible(true);
         chooseDateButton.setVisible(false);
     }
 
     @UiHandler("hideDateButton")
     public void onHideDateButtonClick(final ClickEvent event)
     {
         dateControlsPanel.removeStyleName(css.dateControlsVisible());
         hideDateButton.setVisible(false);
         chooseDateButton.setVisible(true);
     }
 
     @UiHandler("goButton")
     public void onGoButtonClick(final ClickEvent event)
     {
         Search search = application.getCurrentSearch().clone();
         search.setFromDate(fromBox.getValue().getTime());
         search.setToDate(toBox.getValue().getTime());
 
         placeController.goTo(new StreamPlace(search)); //TODO terms view might want not to be changed
         onHideDateButtonClick(null);
     }
 
     private void submitLastNmins(long mins) {
         Search search = application.getCurrentSearch().clone();
         search.setFromDate(-mins*60000);
         search.setToDate(0);
         search.setFocusDate(0);
 
         placeController.goTo(new StreamPlace(search));
         onHideDateButtonClick(null);
     }
 
     @UiHandler("last15m")
     public void onLast15mClick(ClickEvent event) {
         submitLastNmins(15);
     }
     @UiHandler("last30m")
     public void onLast30mClick(ClickEvent event) {
         submitLastNmins(30);
     }
     @UiHandler("last1h")
     public void onLast1hClick(ClickEvent event) {
         submitLastNmins(60);
     }
     @UiHandler("last2h")
     public void onLast2hClick(ClickEvent event) {
         submitLastNmins(120);
     }
     @UiHandler("last4h")
     public void onLast4hClick(ClickEvent event) {
         submitLastNmins(240);
     }
     @UiHandler("last6h")
     public void onLast6hClick(ClickEvent event) {
         submitLastNmins(360);
     }
     @UiHandler("last12h")
     public void onLast12hClick(ClickEvent event) {
         submitLastNmins(720);
     }
     @UiHandler("last24h")
     public void onLast24hClick(ClickEvent event) {
         submitLastNmins(1440);
     }
     @UiHandler("last2d")
     public void onLast2dClick(ClickEvent event) {
         submitLastNmins(2880);
     }
     @UiHandler("last3d")
     public void onLast3dClick(ClickEvent event) {
         submitLastNmins(4320);
     }
     @UiHandler("last5d")
     public void onLast5dClick(ClickEvent event) {
         submitLastNmins(7200);
     }
     @UiHandler("last7d")
     public void onLast7dClick(ClickEvent event) {
         submitLastNmins(10080);
     }
     @UiHandler("lastAllTime")
     public void onLastAllTimeClick(ClickEvent event) {
         submitLastNmins(0);
     }
 
     @UiHandler("plotPanel")
     public void handleMouseOut(final MouseOutEvent event)
     {
         hoverInfo.setVisible(false);
     }
 
 }
