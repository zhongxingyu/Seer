 package com.wideplay.warp.widgets;
 
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.google.inject.Singleton;
 import com.wideplay.warp.widgets.rendering.MvelEvaluatorCompiler;
 import com.wideplay.warp.widgets.routing.PageBook;
 import com.wideplay.warp.widgets.routing.SystemMetrics;
 import net.jcip.annotations.ThreadSafe;
 
 /**
  * Used in the development stage to intercept the real pagebook so we can reload & recompile
  * templates on demand.
  *
  * @author Dhanji R. Prasanna (dhanji@gmail com)
  */
 @ThreadSafe @Singleton
 class DebugModePageBook implements PageBook {
     private final PageBook book;
     private final Provider<TemplateLoader> templateLoader;
     private final WidgetRegistry registry;
     private final SystemMetrics metrics;
 
     @Inject
     public DebugModePageBook(@Production PageBook book, Provider<TemplateLoader> templateLoader,
                              WidgetRegistry registry, SystemMetrics metrics) {
 
         this.book = book;
         this.templateLoader = templateLoader;
         this.registry = registry;
         this.metrics = metrics;
     }
 
     public Page at(String uri, Class<?> myPageClass) {
         return book.at(uri, myPageClass);
     }
 
     public Page get(String uri) {
         final Page page = book.get(uri);
 
         reload(page);
         
         return page;
     }
 
     public Page forName(String name) {
         final Page page = book.forName(name);
 
         //reload template
         reload(page);
 
         return page;
     }
 
     private void reload(Page page) {
 
        //do nothing on the first pass (and skip static resources)
        if (null == page || !metrics.isActive())
             return;
 
         final Class<?> pageClass = page.pageClass();
 
         page.apply(new XmlTemplateCompiler(pageClass, new MvelEvaluatorCompiler(pageClass), registry, book, metrics)
                 .compile(templateLoader.get().load(pageClass)));
     }
 
     public Page embedAs(Class<?> page) {
         return book.embedAs(page);
     }
 }
