 // Copyright 2002-2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.remote;
 
 import java.io.Serializable;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.lang.reflect.Type;
 
 import org.joe_e.Struct;
 import org.joe_e.Token;
 import org.joe_e.reflect.Proxies;
 import org.joe_e.reflect.Reflection;
 import org.ref_send.promise.Deferred;
 import org.ref_send.promise.Do;
 import org.ref_send.promise.Eventual;
 import org.ref_send.type.Typedef;
 import org.waterken.syntax.Exporter;
 import org.waterken.syntax.Importer;
 import org.waterken.uri.URI;
 
 /**
  * A remote reference.
  */
 public final class
 Remote extends Deferred<Object> {
     static private final long serialVersionUID = 1L;
 
     /**
      * network message sender
      */
     private final Messenger messenger;
 
     /**
      * relative URL string for message target
      */
     private final String href;
 
     private
     Remote(final Eventual _, final Token deferred,
            final Messenger messenger, final String href) {
         super(_, deferred);
         if (null == messenger) { throw new NullPointerException(); }
         if (null == href) { throw new NullPointerException(); }
         this.messenger = messenger;
         this.href = href;
     }
     
     /**
      * Constructs a remote reference importer.
      * @param _         corresponding eventual operator
      * @param deferred  {@link Deferred} permission
      * @param messenger network message sender
      * @param here      URL for local vat
      */
     static public Importer
     connect(final Eventual _, final Token deferred,
             final Messenger messenger, final String here) {
         class ImporterX extends Struct implements Importer, Serializable {
             static private final long serialVersionUID = 1L;
             
             public Object
             run(final String href, final String base, final Type type) {
                 final String url = null!=base ? URI.resolve(base, href) : href;
                 return _.cast(Typedef.raw(type),
                     new Remote(_, deferred, messenger, URI.relate(here,url)));
             }
         }
         return new ImporterX();
     }
     
     /**
      * Constructs a remote reference exporter.
      * @param deferred  {@link Deferred} permission
      * @param next      next exporter to try
      */
     static public Exporter
     export(final Token deferred, final Exporter next) {
         class ExporterX extends Struct implements Exporter, Serializable {
             static private final long serialVersionUID = 1L;
             
             public String
             run(final Object target) {
                 final Object handler = target instanceof Proxy
                     ? Proxies.getHandler((Proxy)target) : target;
                 if (handler instanceof Remote) {
                     final Remote x = (Remote)handler;
                     if (Deferred.trusted(deferred, x)) { return x.href; }
                 }
                 return next.run(target);
             }
         }
         return new ExporterX();
     }
     
     // java.lang.Object interface
 
     /**
      * Is the given object the same?
      * @param x The compared to object.
      * @return true if the same, else false.
      */
     public boolean
     equals(final Object x) {
         return x instanceof Remote &&
                href.equals(((Remote)x).href) &&
                messenger.equals(((Remote)x).messenger) &&
                _.equals(((Remote)x)._);
     }
     
     /**
      * Calculates the hash code.
      */
     public int
     hashCode() { return 0x4E307E4F; }
 
     // org.ref_send.promise.Promise interface
 
     /**
      * @return <code>this</code>
      */
     public Object
     call() { return this; }
     
     // java.lang.reflect.InvocationHandler interface
 
     public @Override Object
     invoke(final Object proxy, final Method method,
            final Object[] arg) throws Exception {
         if (Object.class == method.getDeclaringClass()) {
             if ("equals".equals(method.getName())) {
                 return arg[0] instanceof Proxy &&
                     proxy.getClass() == arg[0].getClass() &&
                     equals(Proxies.getHandler((Proxy)arg[0]));
             } else {
                 return Reflection.invoke(method, this, arg);
             }
         }
         try {
             return messenger.invoke(href, proxy, method, arg);
         } catch (final Exception e) { throw new Error(e); }
     }
     
    // org.ref_send.promise.Deferred interface
 
     public void
     when(final Do<Object,?> observer) { messenger.when(href, this, observer); }
 }
