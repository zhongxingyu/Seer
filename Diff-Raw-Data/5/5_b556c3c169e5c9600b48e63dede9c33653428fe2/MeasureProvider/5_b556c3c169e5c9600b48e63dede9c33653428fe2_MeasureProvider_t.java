 package org.nohope.test.stress;
 
 import org.nohope.test.stress.action.Get;
 import org.nohope.test.stress.action.Invoke;
 
 import java.util.concurrent.ConcurrentMap;
 
 /**
  * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
  * @since 2013-12-29 18:23
  */
 public final class MeasureProvider extends MeasureData {
     private final StressScenario scenario;
     private final ConcurrentMap<String, MultiInvocationStat> map;
 
     protected MeasureProvider(final StressScenario scenario,
                               final int threadId,
                               final int operationNumber,
                               final ConcurrentMap<String, MultiInvocationStat> map) {
         super(threadId, operationNumber);
         this.scenario = scenario;
         this.map = map;
     }
 
    public <T> T invoke(final String name, final Get<T> getter) throws Exception {
         return getStat(name).invoke(getThreadId(), getter);
     }
 
    public void invoke(final String name, final Invoke invoke) throws Exception {
         getStat(name).invoke(getThreadId(), invoke);
     }
 
     private MultiInvocationStat getStat(final String name) {
         map.putIfAbsent(name, new MultiInvocationStat(this.scenario.getResolution(), name));
         return map.get(name);
     }
 }
