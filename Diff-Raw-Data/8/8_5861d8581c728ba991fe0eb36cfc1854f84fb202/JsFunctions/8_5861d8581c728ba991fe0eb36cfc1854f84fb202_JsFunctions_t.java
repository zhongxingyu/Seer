 package com.ingemark.requestage.script;
 
 import static com.ingemark.requestage.Util.sneakyThrow;
 import static com.ingemark.requestage.script.JdomBuilder.jdomBuilder;
 import static com.ingemark.requestage.script.JsScope.JS_LOGGER_NAME;
 import static com.ingemark.requestage.script.UrlBuilder.urlBuilder;
 import static java.util.Arrays.asList;
 import static java.util.Collections.newSetFromMap;
 import static org.jdom2.Namespace.getNamespace;
 import static org.jdom2.filter.Filters.fpassthrough;
 import static org.jdom2.output.Format.getPrettyFormat;
 import static org.mozilla.javascript.Context.getCurrentContext;
 import static org.mozilla.javascript.ScriptRuntime.constructError;
 import static org.slf4j.LoggerFactory.getLogger;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.regex.Pattern;
 
 import org.jdom2.Content;
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.Namespace;
 import org.jdom2.input.StAXStreamBuilder;
 import org.jdom2.output.XMLOutputter;
 import org.jdom2.xpath.XPathExpression;
 import org.jdom2.xpath.XPathFactory;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Function;
 import org.mozilla.javascript.NativeJSON;
 import org.mozilla.javascript.NativeJavaObject;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
 import org.mozilla.javascript.Undefined;
 import org.slf4j.Logger;
 
 import com.fasterxml.aalto.in.ByteSourceBootstrapper;
 import com.fasterxml.aalto.in.CharSourceBootstrapper;
 import com.fasterxml.aalto.in.ReaderConfig;
 import com.fasterxml.aalto.stax.StreamReaderImpl;
 import com.ning.http.client.Response;
 
 public class JsFunctions {
   private static final int COMPILED_EXPR_CACHE_LIMIT = 256;
   public static final String[] JS_METHODS = new String[] {
     "require","nsdecl","ns","xml","parseXml","prettyXml","xpath","regex","url","spy","infoSpy"
   };
   private static Logger jsLogger = getLogger(JS_LOGGER_NAME);
   private static final ReaderConfig readerCfg = new ReaderConfig();
   static { readerCfg.configureForSpeed(); }
   private static final Map<String, Namespace> nsmap = concHashMap();
   private static final Map<String, XPathExpression<Object>> xpathmap = concHashMap();
   private static final Map<String, Pattern> regexmap = concHashMap();
   private static final Set<String> filesAlreadyRequired =
       newSetFromMap(new ConcurrentHashMap<String,Boolean>());
 
   public static Object require(Context _1, Scriptable scope, Object[] args, Function _3)
   throws Exception
   {
     final String fname = args[0].toString();
     final JsScope jsScope = (JsScope) ((NativeJavaObject)
         ScriptableObject.getProperty(scope, "jsScope")).unwrap();
     File f = new File(fname);
     if (!f.isAbsolute()) f = new File(jsScope.scriptBase, fname);
     final String path = f.getCanonicalPath();
     if (filesAlreadyRequired.add(path)) {
       jsScope.evaluateFile(path);
       return true;
     }
     return false;
   }
 
   public static Namespace nsdecl(String prefix, String url) {
     final Namespace ns = ns(prefix, url);
     nsmap.put(prefix, ns);
     return ns;
   }
   public static Namespace ns(String prefix, Object _uri ) {
     final String uri = cast(_uri, String.class);
     return uri != null? getNamespace(prefix, uri) : nsmap.get(prefix);
   }
   public static Scriptable xml(Context _1, Scriptable scope, Object[] args, Function _3) {
     final Object root = cast(args[0], Object.class);
     if (root == null) throw new NullPointerException("First argument is null/undefined");
     final String name = cast(root, String.class);
     final Namespace ns = args.length > 1? cast(args[1], Namespace.class) : null;
     if (name != null) return jdomBuilder(scope, name, ns);
     final Element el = cast(root, Element.class);
     if (el != null) return jdomBuilder(scope, el);
     final Document doc = cast(el, Document.class);
     if (doc != null) return jdomBuilder(scope, doc);
     throw constructError("TypeError", "Got " + root.getClass().getName() +
         "; supporting Document, Element,  or String");
   }
   public static Document parseXml(Object in) {
     try {
       in = cast(in, Object.class);
       if (in == null) throw new NullPointerException("Argument is null/undefined");
       final Response r = cast(in, Response.class);
       final InputStream is = r != null? r.getResponseBodyAsStream() : cast(in, InputStream.class);
       final String s = is == null? cast(in, String.class) : null;
       if (is == null && s == null) throw new IllegalArgumentException("Got " +
           in.getClass().getName() + "; supporting HTTP Response, InputStream, or String");
       return new StAXStreamBuilder().build(StreamReaderImpl.construct(is != null?
           ByteSourceBootstrapper.construct(readerCfg, is)
           : CharSourceBootstrapper.construct(readerCfg, new StringReader(s))));
     } catch (Exception e) { return sneakyThrow(e); }
   }
   public static Object prettyXml(Object input) {
     final Object o = cast(input, Object.class);
     if (o == null) return "<!--undefined-->";
     final boolean parseable =
         o instanceof Response || o instanceof String || o instanceof InputStream;
     if (!(parseable || isJdom(o)))
       throw constructError("TypeError",
           "Got " + o.getClass().getName() + ". Supporting HTTP Response, String, " +
       		"InputStream, JdomBuilder, or JDOM2 Document/Content");
     return new Object() {
       Object in = o;
       String out;
       @Override public synchronized String toString() {
         if (out != null) return out;
         final Object jdom = parseable? parseXml(in)
                  : in instanceof JdomBuilder? ((JdomBuilder)in).topNode()
                  : in;
         final XMLOutputter xo = new XMLOutputter(getPrettyFormat());
         final StringWriter w = new StringWriter(256);
         try {
           if (jdom instanceof Document) xo.output((Document)jdom, w);
           else xo.output(asList((Content)jdom), w);
         } catch (IOException e) { return sneakyThrow(e); }
         in = null;
         return out = w.toString();
       }};
   }
   public static XPathExpression xpath(String expr) {
     XPathExpression x = xpathmap.get(expr);
     if (x == null) {
       x = XPathFactory.instance().compile(expr, fpassthrough(), null, nsmap.values());
       if (xpathmap.size() < COMPILED_EXPR_CACHE_LIMIT) xpathmap.put(expr, x);
     }
     return x;
   }
   public static Pattern regex(String regex) {
     Pattern p = regexmap.get(regex);
     if (p == null) {
       p = Pattern.compile(regex);
       if (regexmap.size() < COMPILED_EXPR_CACHE_LIMIT) regexmap.put(regex, p);
     }
     return p;
   }
   public static Object url(Context _1, Scriptable scope, Object[] args, Function _3) {
     return urlBuilder(scope, args[0].toString());
   }
 
   private static Object spy0(boolean debug, Object[] args) {
     if (args.length == 0) return null;
     final Object ret = args[args.length-1];
     if (debug && !jsLogger.isDebugEnabled()) return ret;
    if (args.length == 1) {
      final String msg = logArg(args[0]).toString();
      if (debug) jsLogger.debug(msg); else jsLogger.info(msg);
      return ret;
    }
     final Object[] logArgs = new Object[args.length-1];
     final int start = args.length-1;
     for (int i = start; i < args.length; i++) logArgs[i-start] = logArg(args[i]);
     if (debug) jsLogger.debug(args[0].toString(), logArgs);
     else jsLogger.info(args[0].toString(), logArgs);
     return ret;
   }
   public static Object spy(Context _1, Scriptable _2, Object[] args, Function _3) {
     return spy0(true, args);
   }
   public static Object infoSpy(Context _1, Scriptable _2, Object[] args, Function _3) {
     return spy0(false, args);
   }
   private static Object logArg(Object in) {
     if (in instanceof NativeJavaObject) in = ((NativeJavaObject)in).unwrap();
     return in instanceof Scriptable? NativeJSON.stringify(getCurrentContext(),
       ((Scriptable)in).getParentScope(), in, null, " ")
     : isJdom(in)? prettyXml(in)
     : in;
   }
 
   private static boolean isJdom(final Object o) {
     return o instanceof Document || o instanceof Content || o instanceof JdomBuilder;
   }
 
   private static <T> T cast(Object o, Class<T> c) {
     if (o == null || o == Undefined.instance) return null;
     if (o instanceof NativeJavaObject) o = ((NativeJavaObject)o).unwrap();
     return c.isInstance(o)? (T) o : null;
   }
 
   private static <K,V> Map<K,V> concHashMap() { return new ConcurrentHashMap<K,V>(); }
 }
