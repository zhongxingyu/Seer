 package ddth.dasp.hetty.mvc.view;
 
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.lang3.builder.HashCodeBuilder;
 
 import com.google.common.cache.Cache;
 import com.google.common.cache.CacheBuilder;
 
 import ddth.dasp.hetty.message.IRequest;
 import ddth.dasp.hetty.qnt.ITopicPublisher;
 
 public abstract class CacheViewResolver implements IViewResolver {
 
     private boolean enableCache = true;
     private boolean cacheNull = true;
     private Cache<String, IView> cache;
     private long cacheCapacity = 1000;
 
     public boolean isEnableCache() {
         return enableCache;
     }
 
     public void setEnableCache(boolean enableCache) {
         this.enableCache = enableCache;
     }
 
     public boolean isCacheNull() {
         return cacheNull;
     }
 
     public void setCacheNull(boolean cacheNull) {
         this.cacheNull = cacheNull;
     }
 
     public long getCacheCapacity() {
         return cacheCapacity;
     }
 
     public void setCacheCapacity(long cacheCapacity) {
         this.cacheCapacity = cacheCapacity;
     }
 
     public void init() {
         if (enableCache) {
             int concurrencyLevel = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
             CacheBuilder<Object, Object> cacheBuider = CacheBuilder.newBuilder();
             cacheBuider.concurrencyLevel(concurrencyLevel);
             long expireAfterAccess = 3600;
             long expireAfterWrite = 3600;
             cacheBuider.maximumSize(cacheCapacity);
             if (expireAfterAccess > 0) {
                 cacheBuider.expireAfterAccess(expireAfterAccess, TimeUnit.SECONDS);
             } else if (expireAfterWrite > 0) {
                 cacheBuider.expireAfterWrite(expireAfterWrite, TimeUnit.SECONDS);
             } else {
                 cacheBuider.expireAfterAccess(3600, TimeUnit.SECONDS);
             }
             cache = cacheBuider.build();
         }
     }
 
     public void destroy() {
        cache.cleanUp();
         cache = null;

     }
 
     protected String calcCacheKey(String name, Map<String, String> replacements) {
         return String.valueOf(new HashCodeBuilder(19, 81).append(name).append(replacements)
                 .toHashCode());
     }
 
     @Override
     public IView resolveView(String name, Map<String, String> replacements) {
         if (name.startsWith(REDIRECT_VIEW_PREFIX) || name.startsWith(FORWARD_VIEW_PREFIX)) {
             String[] tokens = name.split(":", 2);
             return new RedirectView(tokens[1]);
         }
 
         IView result = null;
         if (cache != null) {
             String cacheKey = calcCacheKey(name, replacements);
             result = cache.getIfPresent(cacheKey);
             if (result == null) {
                 result = createView(name, replacements);
                 if (result != null) {
                     cache.put(cacheKey, result);
                 } else if (cacheNull) {
                     cache.put(cacheKey, new NullView());
                 }
             }
         } else {
             result = createView(name, replacements);
         }
         return result instanceof NullView ? null : result;
     }
 
     protected abstract IView createView(String name, Map<String, String> replacements);
 
     protected static class NullView implements IView {
         @Override
         public void render(IRequest request, Object model, ITopicPublisher topicPublisher,
                 String topicName) throws Exception {
             // empty
         }
     }
 }
