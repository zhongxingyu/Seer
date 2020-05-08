 package au.net.netstorm.boost.demo.spider.instance;
 
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
 import au.net.netstorm.boost.gunge.type.Implementation;
 import au.net.netstorm.boost.gunge.type.ResolvedInstance;
 import au.net.netstorm.boost.gunge.type.UnresolvedInstance;
 
 public final class DefaultPartialInstances implements PartialInstances {
     private static final ThreadLocal LOCAL_MON = new ThreadLocal();
 
     public void clear() {
         Map map = get();
         map.clear();
     }
 
     public boolean exists(Implementation impl) {
         Map map = get();
         return map.containsKey(impl);
     }
 
     public ResolvedInstance get(Implementation impl) {
         if (!exists(impl)) throw new IllegalStateException(impl + " not found");
         Map map = get();
         return (ResolvedInstance) map.get(impl);
     }
 
     public void put(Implementation impl, UnresolvedInstance ref) {
         if (exists(impl)) throw new IllegalStateException("Duplicate insertion of " + impl);
         Map map = get();
         map.put(impl, ref);
     }
 
     public String toString() {
         Map map = get();
         Set keys = map.keySet();
         return "" + keys;
     }
 
     public void remove(Implementation impl) {
         Map map = get();
         map.remove(impl);
     }
 
     private Map get() {
         Map map = (Map) LOCAL_MON.get();
         if (map != null) return map;
         return nuMap();
     }
 
     private Map nuMap() {
         Map map = new LinkedHashMap();
         LOCAL_MON.set(map);
         return get();
     }
 }
