 // Copyright 2006-2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.remote.http;
 
 import java.io.Serializable;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.Type;
 
 import org.joe_e.Struct;
 import org.joe_e.Token;
 import org.joe_e.array.ByteArray;
 import org.joe_e.array.ConstArray;
 import org.joe_e.array.PowerlessArray;
 import org.joe_e.charset.URLEncoding;
 import org.joe_e.reflect.Reflection;
 import org.ref_send.promise.Do;
 import org.ref_send.promise.Eventual;
 import org.ref_send.promise.Local;
 import org.ref_send.promise.Log;
 import org.ref_send.promise.Promise;
 import org.ref_send.promise.Receiver;
 import org.ref_send.promise.Vat;
 import org.ref_send.type.Typedef;
 import org.waterken.db.Creator;
 import org.waterken.db.Database;
 import org.waterken.db.Effect;
 import org.waterken.db.Root;
 import org.waterken.db.TransactionMonitor;
 import org.waterken.http.Message;
 import org.waterken.http.Response;
 import org.waterken.http.Server;
 import org.waterken.remote.Messenger;
 import org.waterken.remote.Remote;
 import org.waterken.syntax.BadSyntax;
 import org.waterken.syntax.Exporter;
 import org.waterken.syntax.Importer;
 import org.waterken.syntax.json.JSONSerializer;
 import org.waterken.uri.Header;
 import org.waterken.uri.Query;
 import org.waterken.uri.URI;
 
 /**
  * A web-key interface to a {@link Root}.
  */
 public final class
 HTTP extends Eventual implements Serializable {
     static private final long serialVersionUID = 1L;    
     
     private   final Root root;                          // vat root
     private   final Promise<Outbound> outbound;         // active msg pipelines
     
     private   final Creator creator;                    // sub-vat factory
     private   final Receiver<Effect<Server>> effect;    // tx effect scheduler
 
     private
     HTTP(final Receiver<Promise<?>> enqueue,
          final String here, final Log log, final Receiver<?> destruct,
          final Root root, final Promise<Outbound> outbound) {
         super(new Token(), enqueue, here, log, destruct);
         this.root = root;
         this.outbound = outbound;
         
         creator = root.fetch(null, Database.creator);
         effect = root.fetch(null, Database.effect);
     }
     
     // org.ref_send.promise.Eventual interface
 
     public @Override <R> Vat<R>
     spawn(final String label, final Class<?> maker, final Object... argv) {
         Method make = null;
         try {
             for (final Method m : Reflection.methods(maker)) {
                 if ("make".equals(m.getName()) &&
                         Modifier.isStatic(m.getModifiers())) {
                     make = m;
                     break;
                 }
             }
             if (null == make) { throw new NullPointerException(); }
         } catch (final Exception e) { throw new Error(e); }
         final Class<?> R = make.getReturnType();
         try {
             final Exports http = new Exports(this);
             final ByteArray body = new JSONSerializer().serializeTuple(
                 http.export(),ConstArray.array(make.getGenericParameterTypes()),
                 ConstArray.array(argv)); 
             final PowerlessArray<String> rd = creator.apply(null, here, label,
                 new VatInitializer(make, here, body)).call();
             log.sent(rd.get(0));
             final Importer connect = http.connect();
             final @SuppressWarnings("unchecked") R top =
                 (R)connect.apply(rd.get(1), here, R);
             return new Vat<R>(top,
                 (Receiver<?>)connect.apply(rd.get(2), here, Receiver.class)
             );
         } catch (final Exception e) {
             try {
                 final Promise<R> top =
                     reject(e instanceof BadSyntax ? (Exception)e.getCause() :e); 
                 final Receiver<?> destruct = cast(Receiver.class, null);
                 return new Vat<R>(cast(R, top), destruct);
             } catch (final Exception ee) { throw new Error(ee); }
         }
     }
     
     // org.waterken.remote.http.Exports interface
     
     static protected HTTP.Exports
     make(final Receiver<Promise<?>> enqueue, final Root root, final Log log,
          final Receiver<?> destruct, final Promise<Outbound> outbound) {
         final String here = root.fetch(null, Database.here);
         return new Exports(new HTTP(enqueue,here,log, destruct,root,outbound));
     }
     
     static public final class
     Exports extends Struct implements Messenger, Serializable {
         static private final long serialVersionUID = 1L;
         
         public final HTTP _;
         private final TransactionMonitor tagger;
         
         private
         Exports(final HTTP _) {
             this._ = _;
             tagger = _.root.fetch(null, Database.monitor);
         }
         
         // org.waterken.remote.Messenger interface
 
         public void
         when(final String href,final Remote proxy, final Do<Object,?> observer){
         	if (isPromise(URI.fragment("", href))) {
         		peer(href).when(href, proxy, observer);
         	} else {
                _.when(Void.class, new Inline<Object>(Eventual.cast(
                 			Typedef.raw(Local.parameter(observer)), proxy)),
                 	   observer);
             }
         }
         
         public Object
         invoke(final String href, final Object proxy,
                final Method method, final Object... arg) {
         	if (isPromise(URI.fragment("", href))) {
         		// re-dispatch invocation on resolved value of web-key 	 
                 final ConstArray<?> argv = 	 
                     ConstArray.array(null == arg ? new Object[0] : arg);
                 final Do<Object,Object> invoke = Local.curry(method, argv);
                 return _.when(proxy, invoke);
         	} else {
 	            return peer(href).invoke(href, proxy, method, arg);
         	}
         }
         
         // org.waterken.remote.http.Exports.HTTP interface
         
         private Caller
         peer(final String href) {
             final String peer = URI.resolve(URI.resolve(_.here, href), ".");
             final String peerKey = ".peer-" + URLEncoding.encode(peer);
             Pipeline msgs = _.root.fetch(null, peerKey);
             if (null == msgs) {
                 final SessionInfo s = new SessionMaker(_.root).getFresh();
                 msgs = new Pipeline(peer, s.sessionKey, s.sessionName,
                                     _.effect, _.outbound);
                 _.root.assign(peerKey, msgs);
             }
             return new Caller(_,_.here,getCodebase(), connect(),export(),msgs);
         }
         
         /**
          * Gets the base URL for this URL space.
          */
         public String
         getHere() { return _.here; }
         
         public ClassLoader
         getCodebase() { return _.root.fetch(null, Database.code); }
         
         /**
          * Constructs a reference importer.
          */
         public Importer
         connect() {
             final Importer next = Remote.connect(_, _.local, this, _.here);
             class ImporterX extends Struct implements Importer, Serializable {
                 static private final long serialVersionUID = 1L;
 
                 public Object
                 apply(final String href, final String base,
                                        final Type type) throws Exception {
                     final String URL=null!=base ? URI.resolve(base,href) : href;
                     return Header.equivalent(URI.resolve(URL, "."), _.here) ?
                             reference(URI.fragment("", URL)) :
                         next.apply(URL, null, type);
                 }
             }
             return new ImporterX();
         }
 	     
         /**
          * Constructs a reference exporter.
          */
         public Exporter
         export() {
             class ExporterX extends Struct implements Exporter, Serializable {
                 static private final long serialVersionUID = 1L;
 
                 public String
                 apply(final Object object) {
                     final Promise<?> p = Eventual.ref(object);
                     return href(_.root.export(object, false), 	 
                                 !Fulfilled.isInstance(p) || isPBC(near(p)));
                 }
             }
             return Remote.export(_.local, new ExporterX());
         }
 
         protected String
         getTransactionTag() { return tagger.tag(); }
         
         /**
          * Receives an operation.
          * @param query     request query string
          * @param op        operation to run
          * @return <code>op</code> return value
          */
         protected Message<Response>
         execute(final String query, final NonIdempotent op) {
             final String x = session(query);
             if (null == x) { return ServerSideSession.execute(null, op); }
             final ServerSideSession session = new SessionMaker(_.root).open(x);
             return session.once(window(query), message(query), op);
         }
         
         /**
          * Fetches a message target.
          * @param query web-key argument string
          * @return target reference
          */
         protected Object
         reference(final String query) {
             final String s = subject(query);
             return null==s || s.startsWith(".") ? null : _.root.fetch(null, s);
         }
     }
     
     /*
      * web-key parameters
      * x:   message session secret
      * w:   message window number
      * m:   intra-window message number
      * s:   message target key
      * q:   message query, typically the method name
      * o:   present if web-key is a promise
      */
     
     /**
      * Constructs a live web-key for a POST request.
      * @param href          web-key
      * @param predicate     predicate string, or <code>null</code> if none
      * @param sessionKey    message session key
      * @param window        message window number
      * @param message       intra-window message number
      */
     static protected String
     post(final String href, final String predicate,
          final String sessionKey, final long window, final int message) {
         final StringBuilder r = new StringBuilder();
         if (null != predicate) {
             r.append("?q=");
             r.append(URLEncoding.encode(predicate));
         }
         if (null != sessionKey) {
             r.append(0 == r.length() ? '?' : '&');
             r.append("x=");
             r.append(URLEncoding.encode(sessionKey));
             r.append("&w=");
             r.append(window);
             r.append("&m=");
             r.append(message);
         }
         final String query = URI.query("", href);
         if (!"".equals(query)) {
             r.append(0 == r.length() ? '?' : '&');
             r.append(query);
         }
         final String fragment = URI.fragment("", href);
         if (!"".equals(fragment) && !fragment.startsWith("=")) {
             final int end = fragment.indexOf("&=");
             final String args = -1==end ? fragment : fragment.substring(0, end);
             if (!"".equals(args)) {
                 r.append(0 == r.length() ? '?' : '&');
                 r.append(args);
             }
         }
         return URI.resolve(href, r.toString());
     }
     
     /**
      * Constructs a live web-key for a GET request.
      * @param href      web-key
      * @param predicate predicate string, or <code>null</code> if none
      */
     static protected String
     get(final String href, final String predicate) {
         return post(href, predicate, null, 0, 0);
     }
     
     /**
      * Constructs a web-key.
      * @param subject   target object key
      * @param isPromise Is the target object a promise? 	 
      */
     static private String
     href(final String subject, final boolean isPromise) {
     	return "./#"+(isPromise ? "o=&" : "")+"s="+URLEncoding.encode(subject); 	 
     }
     
     /**
      * Does the given web-key refer to a promise? 	 
      * @param q web-key argument string 	 
      * @return <code>true</code> if a promise, else <code>false</code> 	 
      */ 	 
     static private boolean 	 
     isPromise(final String q) { return null != Query.arg(null, q, "o"); }
 
     /**
      * Extracts the subject key from a web-key.
      * @param query web-key argument string
      * @return corresponding subject key
      */
     static private String
     subject(final String query) { return Query.arg(null, query, "s"); }
     
     /**
      * Extracts the predicate string from a web-key.
      * @param method    HTTP request method
      * @param query     web-key argument string
      * @return corresponding predicate string
      */
     static protected String
     predicate(final String method, final String query) {
         return Query.arg("POST".equals(method) ? "apply" : null, query, "q");
     }
     
     /**
      * Extracts the session key.
      * @param query web-key argument string
      * @return corresponding session key
      */
     static private String
     session(final String query) { return Query.arg(null, query, "x"); }
     
     static private long
     window(final String q) { return Long.parseLong(Query.arg(null, q, "w")); }
     
     static private int
     message(final String q) { return Integer.parseInt(Query.arg("0", q, "m")); }
     
     static private final Class<?> Fulfilled = Eventual.ref(0).getClass();
     
     /**
      * Is the given object pass-by-construction? 	 
      * @param object  candidate object 	 
      * @return <code>true</code> if pass-by-construction, else
      *  	   <code>false</code> 	 
      */ 	 
     static private boolean 	 
     isPBC(final Object object) { 	 
     	final Class<?> type = null != object ? object.getClass() : Void.class; 	 
         return String.class == type || 	 
             Integer.class == type || 	 
             Boolean.class == type || 	 
             Long.class == type || 	 
             Byte.class == type || 	 
             Short.class == type || 	 
             Character.class == type || 	 
             Double.class == type || 	 
             Float.class == type || 	 
             Void.class == type || 	 
             java.math.BigInteger.class == type || 	 
             java.math.BigDecimal.class == type || 	 
             org.ref_send.scope.Scope.class == type || 	 
             org.ref_send.Record.class.isAssignableFrom(type) || 	 
             Throwable.class.isAssignableFrom(type) || 	 
             org.joe_e.array.ConstArray.class.isAssignableFrom(type) || 	 
             org.ref_send.promise.Promise.class.isAssignableFrom(type); 	 
     }
     
     /**
      * Changes the base URI.
      * @param here      current base URI
      * @param export    local exporter
      * @param there     new base URI
      */
     static protected Exporter
     changeBase(final String here, final Exporter export, final String there) {
         class ChangeBase extends Struct implements Exporter, Serializable {
             static private final long serialVersionUID = 1L;
 
             public String
             apply(final Object target) {
                 final String absolute = URI.resolve(here, export.apply(target));
                 return null != there ? URI.relate(there, absolute) : absolute;
             }
         }
         return new ChangeBase();
     }
 }
