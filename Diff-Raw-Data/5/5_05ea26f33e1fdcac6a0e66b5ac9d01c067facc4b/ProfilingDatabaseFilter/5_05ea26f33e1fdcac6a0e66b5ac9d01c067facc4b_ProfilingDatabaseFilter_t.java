 package com.psddev.dari.db;
 
 import java.io.IOException;
 
 import javax.servlet.FilterChain;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.psddev.dari.util.AbstractFilter;
 import com.psddev.dari.util.HtmlFormatter;
 import com.psddev.dari.util.HtmlWriter;
 import com.psddev.dari.util.Profiler;
 import com.psddev.dari.util.ProfilerFilter;
 import com.psddev.dari.util.StringUtils;
 
 /**
  * Enables {@link ProfilingDatabase} if {@link Profiler} is active
  * on the current HTTP request.
  */
 public class ProfilingDatabaseFilter extends AbstractFilter {
 
     @Override
     protected void doDispatch(
             HttpServletRequest request,
             HttpServletResponse response,
             FilterChain chain)
             throws Exception {
 
         Profiler profiler = Profiler.Static.getThreadProfiler();
 
         if (profiler == null) {
             super.doDispatch(request, response, chain);
 
         } else {
             ProfilingDatabase profiling = new ProfilingDatabase();
             profiling.setDelegate(Database.Static.getDefault());
 
             HtmlWriter resultWriter = ProfilerFilter.Static.getResultWriter(request, response);
             resultWriter.putOverride(Recordable.class, RECORDABLE_FORMATTER);
 
             try {
                 Database.Static.overrideDefault(profiling);
                 super.doDispatch(request, response, chain);
 
             } finally {
                 Database.Static.restoreDefault();
             }
         }
     }
 
     private static final HtmlFormatter<Recordable> RECORDABLE_FORMATTER = new HtmlFormatter<Recordable>() {
 
         @Override
         public void format(HtmlWriter writer, Recordable recordable) throws IOException {
            if (recordable instanceof Query) {
                ((Query<?>) recordable).format(writer);
                return;
            }

             State recordableState = recordable.getState();
             ObjectType type = recordableState.getType();
 
             if (type != null) {
                 writer.html(type.getLabel());
                 writer.html(": ");
             }
 
             writer.start("a", "href", StringUtils.addQueryParameters("/_debug/query",
                     "where", "id = " + recordableState.getId(),
                     "event", "Run"), "target", "query");
                 writer.html(recordableState.getLabel());
             writer.end();
         }
     };
 }
