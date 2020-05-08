 package net.bodz.bas.util;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
import net.bodz.bas.collection.map.AbstractVariantLookupMap_MatrixImpl;
 import net.bodz.bas.collection.tree.TreePath;
import net.bodz.bas.util.Nullables;
 
 public class SimpleRequest
        extends AbstractVariantLookupMap_MatrixImpl<String> {
 
     private final Object source;
     private String protocol;
     private TreePath path;
     private String anchor;
 
     private Map<String, Object> map;
 
     public SimpleRequest(Object source, TreePath path) {
         this(source, path, null);
     }
 
     public SimpleRequest(Object source, String url) {
         this(source, null, url);
     }
 
     public SimpleRequest(Object source, TreePath context, String spec) {
         this.map = new HashMap<String, Object>();
         this.source = source;
         parseURL(context, spec);
     }
 
     /**
      * @param source
      * @param protocol
      * @param path
      * @param anchor
      * @param parameters
      *            referenced parameters map, <code>null</code> to construct a new one.
      */
     public SimpleRequest(Object source, String protocol, TreePath path, String anchor, Map<String, Object> parameters) {
         this.map = parameters;
         this.source = source;
         this.protocol = protocol;
         this.path = path;
         this.anchor = anchor;
     }
 
     @Override
     public Set<String> keySet() {
         return map.keySet();
     }
 
     @Override
     public boolean containsKey(String key) {
         return map.containsKey(key);
     }
 
     @Override
     public Object get(String key) {
         return map.get(key);
     }
 
     public Object getSource() {
         return source;
     }
 
     public String getProtocol() {
         return protocol;
     }
 
     public void setProtocol(String protocol) {
         this.protocol = protocol;
     }
 
     public TreePath getPath() {
         return path;
     }
 
     public void setPath(TreePath path) {
         this.path = path;
     }
 
     public String getAnchor() {
         return anchor;
     }
 
     public void setAnchor(String anchor) {
         this.anchor = anchor;
     }
 
     public Map<String, Object> getParameterMap() {
         return map;
     }
 
     protected void parseURL(TreePath context, String spec) {
         String query = null;
 
         if (spec != null) {
             int pos = spec.indexOf("://");
             if (pos != -1) {
                 protocol = spec.substring(0, pos);
                 spec = spec.substring(pos + 3);
             }
 
             pos = spec.indexOf('#');
             if (pos != -1) {
                 this.anchor = spec.substring(pos + 1);
                 spec = spec.substring(0, pos);
             }
 
             pos = spec.indexOf('?');
             if (pos != -1) {
                 query = spec.substring(pos + 1);
                 spec = spec.substring(0, pos);
             }
         }
 
         TreePath path = context;
 
         if (spec != null)
             if (context != null) {
                 TreePath siblingParent = context.getParent();
                 path = new TreePath(siblingParent, spec);
             } else
                 path = new TreePath(spec);
         if (path != null)
             path = path.getCanonical();
         this.path = path;
 
         while (query != null) {
             String pair;
             int amp = query.indexOf('&');
             if (amp == -1) {
                 pair = query;
                 query = null;
             } else {
                 pair = query.substring(0, amp);
                 query = query.substring(amp + 1);
             }
             String key, value;
             int eq = pair.indexOf('=');
             if (eq != -1) {
                 key = pair.substring(0, eq);
                 value = pair.substring(eq + 1);
             } else {
                 key = pair;
                 value = null; // Boolean.TRUE;
             }
             key = _decode(key);
             value = _decode(value);
             map.put(key, value);
         }
     }
 
     static String _decode(String encoded) {
         return encoded;
     }
 
     static String _encode(String value) {
         return String.valueOf(value);
     }
 
     @Override
     public String toString() {
         StringBuffer buf = new StringBuffer();
         if (source != null)
             buf.append(source + "::");
         if (protocol != null)
             buf.append(protocol + "://");
         if (path != null)
             buf.append(path);
         if (!map.isEmpty()) {
             boolean first = false;
             for (Map.Entry<String, Object> e : map.entrySet()) {
                 String key = _encode(e.getKey());
                 String value = _encode(String.valueOf(e.getValue()));
                 buf.append(first ? '?' : '&');
                 buf.append(key + "=" + value);
                 first = false;
             }
         }
         if (anchor != null)
             buf.append('#' + anchor);
         return buf.toString();
     }
 
     @Override
     public int hashCode() {
         int hash = 0xede4f59c;
         if (source != null)
             hash ^= source.hashCode();
         if (protocol != null)
             hash ^= protocol.hashCode();
         if (path != null)
             hash ^= path.hashCode();
         if (anchor != null)
             hash ^= anchor.hashCode();
         if (map.isEmpty())
             hash ^= map.hashCode();
         return hash;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (!(obj instanceof SimpleRequest))
             return false;
         SimpleRequest r = (SimpleRequest) obj;
         if (!Nullables.equals(source, source))
             return false;
         if (!Nullables.equals(protocol, r.protocol))
             return false;
         if (!Nullables.equals(path, r.path))
             return false;
         if (!Nullables.equals(anchor, r.anchor))
             return false;
         if (!Nullables.equals(map, r.map))
             return false;
         return true;
     }
 
 }
