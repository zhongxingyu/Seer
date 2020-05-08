 package com.smallcultfollowing.lathos;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 public class CollectionRenderer
     implements ObjectRenderer
 {
 
     public static class BasePage
         implements Page.Detailed, Page.Titled
     {
         private final Object object;
 
         public BasePage(Object object)
         {
             this.object = object;
         }
 
         @Override
         public void renderTitle(Output out, Link link) throws IOException
         {
             Lathos.reflectiveRenderTitle(object, out, link);
         }
 
         @Override
         public void renderDetails(Output out, Link link) throws IOException
         {
             Lathos.reflectiveRenderDetails(object, out, link);
         }
 
         @Override
         public void renderSummary(Output out, Link link) throws IOException
         {
             Lathos.reflectiveRenderSummary(object, out, link);
         }
 
         @Override
         public Object derefPage(LathosServer server, String link) throws InvalidDeref
         {
             return Lathos.reflectiveDerefPage(object, link);
         }
     }
 
     public static class IterablePage<T>
         extends BasePage
     {
         private final Iterable<? extends T> iterable;
 
         public IterablePage(Iterable<? extends T> iterable)
         {
             super(iterable);
             this.iterable = iterable;
         }
 
         protected Link linkForIndex(Link base, int i, T obj)
         {
             return new IndexLink(base, i);
         }
 
         @Override
         public void renderSummary(Output out, Link link) throws IOException
         {
             out.text("[");
 
             Iterator<? extends T> elems = iterable.iterator();
             for (int i = 0; i < 3 && elems.hasNext(); i++) {
                 T elem = elems.next();
                 if (i > 0)
                     out.text(", ");
                 out.obj(linkForIndex(link, i, elem), elem);
             }
 
             if (elems.hasNext()) {
                 out.a(link);
                 out.text("...");
                 out._a(link);
             }
 
             out.text("]");
         }
 
         @Override
         public void renderDetails(Output out, Link link) throws IOException
         {
             out.ol();
             int i = 0;
             for (T elem : iterable) {
                 out.li();
                 out.obj(linkForIndex(link, i++, elem), elem);
                 out._li();
             }
             out._ol();
         }
 
         @Override
         public Object derefPage(LathosServer server, String link) throws InvalidDeref
         {
             int idx = IndexLink.parseIndexLink(link);
 
             if (iterable instanceof List) {
                 return ((List<?>) iterable).get(idx);
             }
 
             Iterator<?> iter = iterable.iterator();
             while (iter.hasNext()) {
                 Object elem = iter.next();
                 if (idx-- == 0)
                     return elem;
             }
 
             throw InvalidDeref.instance;
         }
     }
 
     public static class MapPage
         extends IterablePage<Map.Entry<?, ?>>
     {
         private final Map<?, ?> map;
 
         public MapPage(Map<?, ?> map)
         {
             super(map.entrySet());
             this.map = map;
         }
 
         private final String entryString(Map.Entry<?, ?> entry)
         {
             Object key = entry.getKey();
             if (key == null)
                 return "null";
             return key.toString();
         }
 
         protected Link linkForIndex(Link base, int i, Map.Entry<?, ?> entry)
         {
             return new RelativeLink(base, entryString(entry));
         }
 
         @Override
         public void renderDetails(Output out, Link link) throws IOException
         {
             out.table();
             int i = 0;
             for (Map.Entry<?, ?> entry : map.entrySet()) {
                 out.tr();
 
                 Link entryLink = linkForIndex(link, i++, entry);
 
                 out.td();
                 out.obj(entryLink, "key", entry.getKey());
                 out._td();
 
                 out.td();
                out.obj(entryLink, "value", entry.getValue());
                 out._td();
 
                 out._tr();
             }
             out._table();
         }
 
         @Override
         public Object derefPage(LathosServer server, String link) throws InvalidDeref
         {
             for (Map.Entry<?, ?> entry : map.entrySet()) {
                 if (entry.getKey().toString().equals(link)) {
                     return entry;
                 }
             }
 
             throw InvalidDeref.instance;
         }
     }
 
     public static class MapEntryPage
         implements Page
     {
         private final Map.Entry<?, ?> entry;
 
         public MapEntryPage(Map.Entry<?, ?> entry)
         {
             this.entry = entry;
         }
 
         @Override
         public void renderSummary(Output out, Link link) throws IOException
         {
             out.text("(");
             out.obj(link, "key", entry.getKey());
             out.code().text(" -> ")._code();
             out.obj(link, "value", entry.getValue());
             out.text(")");
         }
 
         @Override
         public Object derefPage(LathosServer server, String link) throws InvalidDeref
         {
             if (link.equals("key"))
                 return entry.getKey();
             else if (link.equals("value"))
                 return entry.getValue();
             else
                 return null;
         }
     }
 
     @Override
     public Page asPage(LathosServer server, Object obj)
     {
         if (obj instanceof Object[]) {
             Object[] arr = (Object[]) obj;
             return new IterablePage<Object>(Arrays.asList(arr));
         }
 
         if (obj instanceof Iterable<?>) {
             return new IterablePage<Object>((Iterable<?>) obj);
         }
 
         if (obj instanceof Map) {
             Map<?, ?> map = (Map<?, ?>) obj;
             return new MapPage(map);
         }
 
         if (obj instanceof Map.Entry) {
             return new MapEntryPage((Map.Entry<?, ?>) obj);
         }
 
         return null;
     }
 
 }
