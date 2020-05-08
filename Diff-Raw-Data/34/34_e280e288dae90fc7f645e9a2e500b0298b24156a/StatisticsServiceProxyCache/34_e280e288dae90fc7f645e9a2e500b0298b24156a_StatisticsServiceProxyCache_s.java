 package statistics.store.service;
 
 import java.util.Collection;
 import java.util.List;
 import statistics.cache.IResourceCache;
 import statistics.model.indicator.Dimension;
 import statistics.model.indicator.IndicatorValuesFuture;
 import statistics.model.indicator.IndicatorConfiguration;
 import statistics.model.indicator.IndicatorRangeFuture;
 import statistics.utils.StringUtils;
 
 /**
  *
  * @author Andre Matos
  */
 public class StatisticsServiceProxyCache implements IStatisticsServiceProxy {
 
     private IStatisticsServiceProxy proxy;
     private IResourceCache cache;
     private String _key;
 
     public StatisticsServiceProxyCache(IStatisticsServiceProxy proxy, IResourceCache cache) {
         this.proxy = proxy;
         this.cache = cache;
     }
 
     @Override
     public IndicatorValuesFuture getIndicatorValues(IndicatorConfiguration config, List<String> shapeIds) {
        //return proxy.getIndicatorValues(config, shapeIds);

        String rangeKey = "values_" + generateKey(config, shapeIds);
        IndicatorValuesFuture values = null;

        if(cache.hasResource(rangeKey)) {
            return (IndicatorValuesFuture)cache.getResource(rangeKey);
        }

        values = proxy.getIndicatorValues(config, shapeIds);
        cache.addResource(rangeKey, values);

        return values;
     }
 
     @Override
     public IndicatorRangeFuture getIndicatorRange(IndicatorConfiguration config, List<String> shapeIds) {
 
         String rangeKey = "range_" + generateKey(config, null);
         IndicatorRangeFuture range = null;
         
         if(cache.hasResource(rangeKey)) {
             return (IndicatorRangeFuture)cache.getResource(rangeKey);
         }
 
         range = proxy.getIndicatorRange(config, shapeIds);
         cache.addResource(rangeKey, range);
         
         return range;
     }
 
     private String generateKey(IndicatorConfiguration config, List<String> shapeIds) {
 
         StringBuffer builder = new StringBuffer();
         builder.append(config.sourceId);
         builder.append(";");
         builder.append(config.indicatorId);
         builder.append(";");
         builder.append(config.filter);
         builder.append(";");
         builder.append(config.projected);
 
         if(shapeIds != null && shapeIds.size() > 0) {
             builder.append(";");
             java.util.Collections.sort(shapeIds);
             builder.append(StringUtils.join(shapeIds, ","));
         }
 
         return builder.toString();
     }
 
     public void toString(StringBuffer buffer, List<Dimension> dimensions) {
 
         for (Dimension d : dimensions) {
             d.toString(buffer);
         }
     }
 }
