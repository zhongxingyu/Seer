 package statistics.store.service;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.List;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.xml.namespace.QName;
 import org.datacontract.schemas._2004._07.providerdatacontracts.ArrayOfDimensionFilter;
 import org.datacontract.schemas._2004._07.providerdatacontracts.IndicatorValueRange;
 import org.datacontract.schemas._2004._07.statisticsproxyservicedefenitions.IndicatorValues;
 import org.tempuri.DefaultStatisticsProxyImpl;
 import org.tempuri.IStatisticsProxyService;
 import statistics.model.indicator.Dimension;
 import statistics.model.indicator.GeographicDimension;
 import statistics.model.indicator.IndicatorConfiguration;
 import statistics.model.indicator.IndicatorValue;
 import statistics.model.indicator.IndicatorRange;
 import statistics.store.service.extensions.ArrayOfDimensionFilterExtension;
 import statistics.store.service.extensions.ArrayOfIndicatorValueExtension;
 import statistics.store.service.extensions.IndicatorValueRangeExtension;
 import statistics.utils.concurrent.ManualResetEvent;
 
 /**
  *
  * @author Andre Matos
  */
 public class StatisticsServiceProxyImpl implements IStatisticsServiceProxy {
 
     protected static final QName qname = new QName("http://tempuri.org/", "DefaultStatisticsProxyImpl");
     protected final ThreadPoolExecutor _threadPool;
 
     protected volatile IndicatorConfiguration _config;
     protected volatile List<String> _shapeIds;
     protected volatile URL _serviceURL;
 
     // service responses
     private volatile IndicatorValues indicatorValuesResponse;
     private volatile IndicatorValueRange indicatorValueRange;
     private volatile DefaultStatisticsProxyImpl proxy;
 
     // service requesters
     private RequestValues valuesRequester;
     private RequestValuesRange valuesRangeRequester;
 
     // response lockers
     private volatile ManualResetEvent valuesEvent = new ManualResetEvent(false);
     private volatile ManualResetEvent valuesRangeEvent = new ManualResetEvent(false);
 
     private class RequestValues implements Runnable {
         @Override
         public void run() {
             IStatisticsProxyService service = getProxyInstance();
             indicatorValuesResponse = service.getIndicatorValues(
                                             _config.sourceId,
                                             _config.indicatorId,
                                             getFilterDimensions(),
                                             getProjectedDimensions()
                                       );
             valuesEvent.set();
         }
     }
     
     private class RequestValuesRange implements Runnable {
         @Override
         public void run() {
 
             IStatisticsProxyService service = getProxyInstance();
             indicatorValueRange = service.getIndicatorValuesRange(
                                         _config.sourceId,
                                         _config.indicatorId,
                                         getFilterDimensions(),
                                         getProjectedDimensions(),
                                         getShapeLevel()
                                    );
             valuesRangeEvent.set();
         }
     }
 
     private synchronized IStatisticsProxyService getProxyInstance() {
         // configure the endpoint
         if(proxy == null)
             proxy = new DefaultStatisticsProxyImpl(_serviceURL, qname);
         
         return proxy.getBasicHttpBindingIStatisticsProxyService();
     }
 
     private ArrayOfDimensionFilter getFilterDimensions() {
 
         ArrayOfDimensionFilterExtension dimensionFilter = new ArrayOfDimensionFilterExtension();
         for (Dimension dimension : _config.dimensions)
             if(! (dimension instanceof GeographicDimension))
                 dimensionFilter.addDimension(dimension);
 
         return dimensionFilter;
     }
 
     private ArrayOfDimensionFilter getProjectedDimensions() {
         
         GeographicDimension dimension = _config.getGeographicDimension();
         ArrayOfDimensionFilterExtension dimensionFilter = new ArrayOfDimensionFilterExtension();
         dimensionFilter.addDimension(dimension.id, _shapeIds);
 
         return dimensionFilter;
     }
 
     private String getShapeLevel() {
         return _config.getGeographicDimension().shapeLevel;
     }
 
     public StatisticsServiceProxyImpl(String serviceURL, ThreadPoolExecutor threadpool) 
             throws MalformedURLException {
         
         _threadPool = threadpool;
         _serviceURL = new URL(serviceURL);
         valuesRequester = new RequestValues();
         valuesRangeRequester = new RequestValuesRange();
     }
 
     public StatisticsServiceProxyImpl(
             String serviceURL, ThreadPoolExecutor threadpool,
             IndicatorConfiguration configuration, List<String> shapeIds) throws MalformedURLException {
 
         this(serviceURL, threadpool);
         requestIndicatorValues(configuration, shapeIds);
     }
 
     @Override
     public void requestIndicatorValues(IndicatorConfiguration config, List<String> shapeIds) throws IllegalArgumentException {
 
         if(_config == null && _shapeIds == null){
             _config = config;
             _shapeIds = shapeIds;
 
             if(_shapeIds == null || _shapeIds.size() == 0) {
                 valuesRangeEvent.set();
                 valuesEvent.set();
             } else {
                 valuesRangeEvent.reset();
                 valuesEvent.reset();
 
                 _threadPool.execute(valuesRequester);
                 _threadPool.execute(valuesRangeRequester);
             }
         }else
             throw new IllegalArgumentException("This class has been initialized");
     }
 
     @Override
     public List<IndicatorValue> getIndicatorValue(String shapeId) {
         try {
             valuesEvent.waitOne();
         } catch (InterruptedException ex) {
             Logger.getLogger(StatisticsServiceProxyImpl.class.getName()).log(Level.SEVERE, "Thread was interrupted while waiting for values", ex);
             return null;
         }
 
         if(indicatorValuesResponse == null || indicatorValuesResponse.getValues().getValue() == null)
             return null;
 
 
         List<statistics.model.indicator.IndicatorValue> values = ArrayOfIndicatorValueExtension.getIndicatorValues (
                 indicatorValuesResponse.getValues().getValue(),
                 _config.getGeographicDimension().id,
                 shapeId);
         
         if(values == null || values.size() == 0) return null;
         return values;
     }
 
     @Override
     public IndicatorRange getIndicatorRange() {
         try {
            valuesRangeEvent.waitOne();
         } catch (InterruptedException ex) {
             Logger.getLogger(StatisticsServiceProxyImpl.class.getName()).log(Level.SEVERE, "Thread was interrupted while waiting for values", ex);
             return null;
         }
 
         if(indicatorValueRange == null || indicatorValueRange.getMaximum() == null || indicatorValueRange.getMaximum().isNil())
             return null;
 
         return IndicatorValueRangeExtension.toIndicatorRange(indicatorValueRange);
     }
 }
