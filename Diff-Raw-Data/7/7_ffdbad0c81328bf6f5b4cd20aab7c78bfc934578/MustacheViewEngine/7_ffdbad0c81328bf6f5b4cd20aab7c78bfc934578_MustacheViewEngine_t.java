 package com.page5of4.mustache;
 
 import java.io.PrintWriter;
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.page5of4.mustache.spring.LayoutAndView;
 import com.samskivert.mustache.Mustache;
 import com.samskivert.mustache.Mustache.TemplateLoader;
 import com.samskivert.mustache.Template;
 
 public class MustacheViewEngine {
    private final ConcurrentHashMap<String, Template> cache = new ConcurrentHashMap<String, Template>();
    private final LayoutViewModelFactory layoutViewModelFactory;
    private final TemplateSourceLoader sourceLoader;
    private boolean sourceCachingEnabled;
 
    @Autowired
    public MustacheViewEngine(LayoutViewModelFactory layoutViewModelFactory, TemplateSourceLoader sourceLoader, HttpServletRequest servletRequest) {
       this.layoutViewModelFactory = layoutViewModelFactory;
       this.sourceLoader = sourceLoader;
    }
 
    public boolean isSourceCachingEnabled() {
       return sourceCachingEnabled;
    }
 
    public void setCachingEnabled(boolean sourceCachingEnabled) {
       this.sourceCachingEnabled = sourceCachingEnabled;
    }
 
    public Template createMustache(String view) {
       String template = getSource(view);
       return createMustache(view, template);
    }
 
    public void render(final String view, final Object scope, final Object parentScope, final PrintWriter writer) {
       try {
          Template template = createMustache(view);
         if(scope == null) {
            template.execute(parentScope, parentScope, writer);
         }
         else {
            template.execute(scope, parentScope, writer);
         }
       }
       catch(Exception e) {
          throw new RuntimeException("Error rendering: " + view, e);
       }
    }
 
    public void render(final LayoutAndView lav, final Map<String, Object> model, final PrintWriter writer) {
       final Object parentBodyScope = layoutViewModelFactory.createLayoutViewModel(model);
       final Object bodyScope = SingleModelAndView.getBodyModel(model);
       if(lav.getLayout() == null) {
          render(lav.getView(), bodyScope, parentBodyScope, writer);
       }
       else {
          LayoutViewModel layoutViewModel = layoutViewModelFactory.createLayoutViewModel(model, new LayoutBodyFunction() {
             @Override
             public String getBody() {
                StringWriter sw = new StringWriter();
                render(lav.getView(), bodyScope, parentBodyScope, new PrintWriter(sw));
                return sw.toString();
             }
          });
          render(lav.getLayout(), layoutViewModel, null, writer);
       }
    }
 
    private Template createMustache(final String view, String template) {
       if(cache.containsKey(view)) {
          return cache.get(view);
       }
       try {
          Template compiled = Mustache.compiler().withLoader(new TemplateLoader() {
             @Override
             public Reader getTemplate(String name) throws Exception {
                return new StringReader(sourceLoader.getPartial(view, name));
             }
          }).compile(template);
          if(isSourceCachingEnabled()) {
             cache.put(view, compiled);
          }
          return compiled;
       }
       catch(Exception e) {
          throw new RuntimeException("Error compiling: " + view, e);
       }
    }
 
    public boolean containsView(String url) {
       return sourceLoader.containsSource(url);
    }
 
    private String getSource(String url) {
       return sourceLoader.getSource(url);
    }
 }
