 // Copyright 2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.remote.http;
 
 import java.io.Serializable;
 import java.lang.reflect.Method;
 import java.lang.reflect.TypeVariable;
 
 import org.joe_e.Struct;
 import org.joe_e.charset.URLEncoding;
 import org.ref_send.promise.Rejected;
 import org.ref_send.promise.eventual.Channel;
 import org.ref_send.promise.eventual.Do;
 import org.ref_send.promise.eventual.Eventual;
 import org.ref_send.promise.eventual.Task;
 import org.ref_send.type.Typedef;
 import org.waterken.remote.Exports;
 import org.waterken.remote.Messenger;
 import org.waterken.remote.Remote;
 import org.waterken.remote.Remoting;
 import org.waterken.uri.Authority;
 import org.waterken.uri.Location;
 import org.waterken.uri.URI;
 import org.waterken.vat.Root;
 
 /**
  * Dispatches to the corresponding peer specific messenger.
  */
 public final class
 HTTP extends Struct implements Messenger, Serializable {
     static private final long serialVersionUID = 1L;
 
     private final String scheme;
     private final int standardPort;
     private final Root local;
     
     public
     HTTP(final String scheme, final int standardPort, final Root local) {
         this.scheme = scheme;
         this.standardPort = standardPort;
         this.local = local;
     }
     
     // org.waterken.remote.Messenger interface
 
     /**
      * {@link Do} block parameter type
      */
    static private final TypeVariable<?> DoP = Typedef.name(Do.class, "P");
 
     public <P,R> R
     when(final String URL, final Class<?> R, final Do<P,R> observer) {
         final String src = Exports.src(URL);
         if (null != src) {
             // To ensure when blocks are processed in the same order as
             // enqueued, always ask the source vat for the resolved value.
             final String target = Exports.href(src, Exports.key(URL));
             final String here = (String)local.fetch(null, Root.here);
             if (!src.equalsIgnoreCase(here)) {
                 return message(target).when(target, R, observer);
             }
             Object p;
             try {
                 p = new Exports(local).connect(here).run(Object.class, target);
             } catch (final Exception e) {
                p = new Rejected<Object>(e);
             }
             final Eventual _ = (Eventual)local.fetch(null, Remoting._);
             return _.when((P)p, observer);
         }
         // already a resolved remote reference
         final Eventual _ = (Eventual)local.fetch(null, Remoting._);
         final R r;
         final Do<P,?> forwarder;
         if (void.class == R || Void.class == R) {
             r = null;
             forwarder = observer;
         } else {
             final Channel<R> x = _.defer();
             r = _.cast(R, x.promise);
             forwarder = Eventual.compose(observer, x.resolver);
         }
         final Class<P> P = Typedef.raw(Typedef.value(DoP, observer.getClass()));
         final P a = Remote._(P, local, URL);
         class Fulfill extends Struct implements Task, Serializable {
             static private final long serialVersionUID = 1L;
 
             public void
             run() throws Exception { forwarder.fulfill(a); }
             // AUDIT: call to untrusted application code
         }
         _.enqueue.run(new Fulfill());
         return r;
     }
     
     public Object
     invoke(final String URL, final Object proxy,
            final Method method, final Object... arg) {
         // To ensure invocations are delivered in the same order as enqueued,
         // only send an invocation on a remote reference, or a pipeline
         // promise generated from this vat.
         final String src = Exports.src(URL);
         if (null == src || src.equals(local.fetch(null, Root.here))) {
             return message(URL).invoke(URL, proxy, method, arg);
         }
         final Class<?> R = Typedef.raw(
             Typedef.bound(method.getGenericReturnType(), proxy.getClass()));
         if (void.class == R || Void.class == R) { return null; }
         return new Rejected<Object>(new CannotInvokeRemotePromise())._(R);
     }
     
     private Messenger
     message(final String URL) {
         if (!scheme.equals(URI.scheme("",URL))) {throw new RuntimeException();}
         
         final String authority = URI.authority(URL);
         final String location = Authority.location(authority);
         final String hostname = Location.hostname(location);
         final int port = Location.port(standardPort, location);
         final String peer = scheme + "://" + hostname.toLowerCase() +
                             (standardPort == port ? "" : ":" + port);
         final String peerKey = ".-" + URLEncoding.encode(peer);
         Pipeline msgs = (Pipeline)local.fetch(null, peerKey);
         if (null == msgs) {
             msgs = new Pipeline(peer, local);
             local.link(peerKey, msgs);
         }
         return new Caller(msgs, local);
     }
 }
