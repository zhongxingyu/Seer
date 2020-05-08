 package au.net.netstorm.boost.spider.core;
 
 import au.net.netstorm.boost.demo.spider.newer.ResolvedThings;
 import au.net.netstorm.boost.spider.onion.layer.closure.TryCatchFinally;
 
 public final class SpiderTryCatchFinally implements TryCatchFinally {
     ResolvedThings resolvedThings;
 
     public SpiderTryCatchFinally(ResolvedThings resolvedThings) {
         this.resolvedThings = resolvedThings;
     }
 
    public void in() {
         resolvedThings.clear();
     }
 
    public void out() {
         resolvedThings.clear();
     }
 }
