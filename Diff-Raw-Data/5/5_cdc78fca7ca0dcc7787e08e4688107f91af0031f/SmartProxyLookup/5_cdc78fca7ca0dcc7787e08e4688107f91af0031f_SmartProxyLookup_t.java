 package com.lemoulinstudio.gfa.nb.util;
 
 import java.lang.ref.Reference;
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Map;
 import java.util.Set;
 import java.util.WeakHashMap;
 import org.openide.util.Lookup;
 import org.openide.util.LookupEvent;
 import org.openide.util.LookupListener;
 
 /**
  * This is my implementation of a Lookup which proxies 1 lookup at a time.
  *
  * It doesn't suffer the design flows and the bugs of the ones provided by
  * the NetBeans platform 11.
  *
  * @author Vincent Cantin
  */
 public class SmartProxyLookup extends Lookup {
 
   private static class ProxyResult<T> extends Result<T> {
 
     private static class DelegateLookupListener implements LookupListener {
 
       private final Reference<ProxyResult> proxyResultRef;
 
       public DelegateLookupListener(ProxyResult proxyResult) {
         this.proxyResultRef = new WeakReference(proxyResult);
       }
 
       public void resultChanged(LookupEvent ev) {
         ProxyResult proxyResult = proxyResultRef.get();
         if (proxyResult != null)
           proxyResult.fireResultChanged();
       }
       
     }
 
     private Template<T> template;
     private Collection<LookupListener> listeners;
     private Result<T> delegateResult;
     private DelegateLookupListener delegateLookupListener;
 
     public ProxyResult(Template<T> template, Lookup delegateLookup) {
       this.template = template;
       delegateResult = delegateLookup.lookup(template);
       delegateLookupListener = new DelegateLookupListener(this);
       delegateResult.addLookupListener(delegateLookupListener);
     }
 
     public void setDelegateLookup(Lookup delegateLookup) {
       setDelegateResult(delegateLookup.lookup(template));
     }
 
     private void setDelegateResult(Result<T> delegateResult) {
       Collection<? extends T> contentBefore = this.delegateResult.allInstances();
       Collection<? extends T> contentAfter = delegateResult.allInstances();
 
       this.delegateResult.removeLookupListener(delegateLookupListener);
       this.delegateResult = delegateResult;
       this.delegateResult.addLookupListener(delegateLookupListener);
 
       if (!contentAfter.containsAll(contentBefore) ||
           !contentBefore.containsAll(contentAfter))
         fireResultChanged();
     }
 
     private synchronized void fireResultChanged() {
       LookupEvent event = new LookupEvent(this);
      if (listeners != null)
        for (LookupListener listener : new ArrayList<LookupListener>(listeners))
          listener.resultChanged(event);
     }
 
     @Override
     public synchronized void addLookupListener(LookupListener listener) {
       if (listeners == null)
         listeners = new ArrayList<LookupListener>();
       
       listeners.add(listener);
     }
 
     @Override
     public synchronized void removeLookupListener(LookupListener listener) {
       if (listeners != null)
         listeners.remove(listener);
     }
 
     @Override
     public Collection<? extends T> allInstances() {
       return delegateResult.allInstances();
     }
 
     @Override
     public Set<Class<? extends T>> allClasses() {
       return delegateResult.allClasses();
     }
 
     @Override
     public Collection<? extends Item<T>> allItems() {
       return delegateResult.allItems();
     }
 
     @Override
     protected void finalize() throws Throwable {
       delegateResult.removeLookupListener(delegateLookupListener);
     }
 
   }
 
   private Lookup delegateLookup;
   private Map<Template<?>, Reference<ProxyResult<?>>> templateToProxyResult;
 
   public SmartProxyLookup() {
     delegateLookup = Lookup.EMPTY;
     templateToProxyResult = new WeakHashMap<Template<?>, Reference<ProxyResult<?>>>();
   }
 
   public Lookup getDelegateLookup() {
     return delegateLookup;
   }
 
   public synchronized void setDelegateLookup(Lookup delegateLookup) {
     this.delegateLookup = delegateLookup;
 
     for (Reference<ProxyResult<?>> proxyResultRef : templateToProxyResult.values()) {
       ProxyResult<?> proxyResult = proxyResultRef.get();
       if (proxyResult != null)
         proxyResult.setDelegateLookup(delegateLookup);
     }
   }
 
   @Override
   public <T> T lookup(Class<T> clazz) {
     return delegateLookup.lookup(clazz);
   }
 
   @Override
   public synchronized <T> Result<T> lookup(Template<T> template) {
     Reference<ProxyResult<?>> proxyResultRef = templateToProxyResult.get(template);
 
     ProxyResult<T> proxyResult = null;
     if (proxyResultRef != null)
       proxyResult = (ProxyResult<T>) proxyResultRef.get();
 
     if (proxyResult == null) {
       proxyResult = new ProxyResult<T>(template, delegateLookup);
       templateToProxyResult.put(template, new WeakReference<ProxyResult<?>>(proxyResult));
     }
     
     return proxyResult;
   }
 
 }
