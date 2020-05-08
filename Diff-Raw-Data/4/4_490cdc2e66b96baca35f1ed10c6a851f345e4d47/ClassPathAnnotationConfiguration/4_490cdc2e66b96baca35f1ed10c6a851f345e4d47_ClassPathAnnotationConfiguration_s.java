 package org.eluder.jetty.server.annotations;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import org.eclipse.jetty.annotations.AnnotationConfiguration;
 import org.eclipse.jetty.annotations.AnnotationParser;
 import org.eclipse.jetty.util.PatternMatcher;
 import org.eclipse.jetty.util.log.Log;
 import org.eclipse.jetty.util.log.Logger;
 import org.eclipse.jetty.util.resource.Resource;
 import org.eclipse.jetty.webapp.WebAppContext;
 
 public class ClassPathAnnotationConfiguration extends AnnotationConfiguration {
 
     public static final String CLASSPATH_JAR_PATTERN = "org.eclipse.jetty.server.webapp.ClassPathIncludeJarPattern";
     
     private static final Logger LOG = Log.getLogger(ClassPathAnnotationConfiguration.class);
     
     @Override
     public void parseContainerPath(final WebAppContext context, final AnnotationParser parser) throws Exception {
         final Set<AnnotationParser.Handler> handlers = new HashSet<>();
         handlers.addAll(_discoverableAnnotationHandlers);
         handlers.addAll(_containerInitializerAnnotationHandlers);
         if (_classInheritanceHandler != null) {
             handlers.add(_classInheritanceHandler);
         }
 
         for (URI u : getClassPathUris(context)) {
             final Resource r = Resource.newResource(u);
             //queue it up for scanning if using multithreaded mode
            if (_parserTasks != null)
                 _parserTasks.add(new ParserTask(parser, handlers, r, _webAppClassNameResolver));
         }
     }
 
     @Override
     public void parseWebInfClasses(final WebAppContext context, final AnnotationParser parser) throws Exception {
         // noop
     }
 
     @Override
     public void parseWebInfLib(final WebAppContext context, final AnnotationParser parser) throws Exception {
         // noop
     }
 
     protected final Pattern getJarPattern(final WebAppContext context) {
         String attribute = (String) context.getAttribute(CLASSPATH_JAR_PATTERN);
         return (attribute == null ? null : Pattern.compile(attribute));
     }
     
     protected final boolean isJar(final String resource) {
         return (resource != null && resource.endsWith(".jar"));
     }
     
     private List<URI> getClassPathUris(final WebAppContext context) throws Exception {
         final List<URI> classPathUris = new ArrayList<>();
         final PatternMatcher jarMatcher = new PatternMatcher() {
             @Override
             public void matched(final URI uri) throws Exception {
                 classPathUris.add(uri);
             }
         };
         ClassLoader loader = context.getClassLoader();
         URI[] holder = new URI[1];
         Pattern jarPattern = getJarPattern(context);
         while (loader != null && (loader instanceof URLClassLoader)) {
             URL[] urls = ((URLClassLoader) loader).getURLs();
             if (urls != null) {
                 for (URL u : urls) {
                     try {
                         holder[0] = u.toURI();
                     } catch (URISyntaxException ex) {
                         holder[0] = new URI(u.toString().replaceAll(" ", "%20"));
                     }
                     if (isJar(holder[0].toString())) {
                         jarMatcher.match(jarPattern, holder, false);
                     } else {
                         classPathUris.add(holder[0]);
                     }
                 }
             }
             loader = loader.getParent();
         }
         if (LOG.isDebugEnabled()) {
             LOG.debug("Finding annotatated classes from: " + classPathUris.toString());
         }
         return classPathUris;
     }
 }
