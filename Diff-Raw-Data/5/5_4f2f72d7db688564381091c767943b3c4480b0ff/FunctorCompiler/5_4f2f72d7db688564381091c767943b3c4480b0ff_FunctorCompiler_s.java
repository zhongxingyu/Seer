 package com.codereligion.hammock.compiler;
 
 import com.codereligion.hammock.compiler.model.Type;
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import com.google.common.cache.LoadingCache;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.io.Resources;
 import freemarker.cache.URLTemplateLoader;
 import freemarker.template.Configuration;
 import freemarker.template.DefaultObjectWrapper;
 import freemarker.template.Template;
 import freemarker.template.TemplateException;
 import freemarker.template.TemplateExceptionHandler;
 import freemarker.template.Version;
 
 import javax.annotation.processing.AbstractProcessor;
 import javax.annotation.processing.Filer;
 import javax.annotation.processing.RoundEnvironment;
 import javax.annotation.processing.SupportedAnnotationTypes;
 import javax.lang.model.SourceVersion;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ElementKind;
 import javax.lang.model.element.PackageElement;
 import javax.lang.model.element.TypeElement;
 import javax.tools.Diagnostic.Kind;
 import javax.tools.JavaFileObject;
 import java.io.IOException;
 import java.io.Writer;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ExecutionException;
 
 import static com.google.common.base.Predicates.not;
 import static com.google.common.collect.Iterables.removeIf;
 
 @SupportedAnnotationTypes("com.codereligion.hammock.Functor")
 public class FunctorCompiler extends AbstractProcessor {
 
     private final CacheLoader<TypeElement, Type> loader = new CacheLoader<TypeElement, Type>() {
 
         @Override
         public Type load(TypeElement element) throws Exception {
             return new Type(element);
         }
 
     };
 
     @Override
     public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
         boolean claimed = false;
 
         final LoadingCache<TypeElement, Type> cache = CacheBuilder.newBuilder().build(loader);
 
         for (TypeElement annotation : annotations) {
             for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                 try {
                     parse(element, cache);
                     claimed = true;
                 } catch (UnsupportedUsageException e) {
                     error(e.getElement(), e.getMessage());
                 }
             }
         }
 
         write(compact(cache));
 
         return claimed;
     }
 
     private void parse(Element element, Function<TypeElement, Type> storage) throws UnsupportedUsageException {
         final ElementKind kind = element.getKind();
 
         final Map<ElementKind, Parser> parsers = ImmutableMap.<ElementKind, Parser>of(
                 ElementKind.METHOD, new MethodParser(processingEnv)
         );
 
         final Parser parser = parsers.get(kind);
 
         if (parser == null) {
             throw new UnsupportedUsageException(element, "unsupported usage");
         }
 
         parser.check(element);
         parser.parse(element, storage);
     }
 
     private List<Type> compact(LoadingCache<TypeElement, Type> cache) {
        for (Type type : cache.asMap().values()) {
             Type current = type;
 
             while (true) {
                 final Element element = current.getElement().getEnclosingElement();
 
                 if (element instanceof PackageElement) {
                     break;
                 }
 
                 final Type parent;
 
                 try {
                     parent = cache.get((TypeElement) element);
                 } catch (ExecutionException e) {
                     throw new AssertionError(e);
                 }
 
                 parent.getTypes().add(current);
                 current = parent;
             }
         }
 
         final List<Type> types = new ArrayList<>(cache.asMap().values());
         removeIf(types, not(TopLevel.INSTANCE));
         return types;
     }
 
     private void write(Collection<Type> types) {
         final Thread thread = Thread.currentThread();
         final ClassLoader original = thread.getContextClassLoader();
 
         try {
             final ClassLoader loader = FunctorCompiler.class.getClassLoader();
             thread.setContextClassLoader(loader);
 
             //final MustacheFactory factory = new DefaultMustacheFactory();
             //final Mustache mustache = factory.compile("templates/template.mustache");
 
             final Configuration config = new Configuration();
 
             config.setTemplateLoader(new URLTemplateLoader() {
                 @Override
                 protected URL getURL(String name) {
                     return Resources.getResource(name);
                 }
             });
 
             config.setObjectWrapper(new DefaultObjectWrapper());
             config.setDefaultEncoding("UTF-8");
             config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
             config.setIncompatibleImprovements(new Version(2, 3, 20));  // FreeMarker 2.3.20
             config.setLocalizedLookup(false);
 
             final Template template = config.getTemplate("templates/template.ftl");
 
             for (Type type : types) {
                 final Filer filer = processingEnv.getFiler();
                 final JavaFileObject file = filer.createSourceFile(type.getName().getQualified());
 
                 try (Writer writer = file.openWriter()) {
                     //mustache.execute(writer, type).flush();
                     template.process(type, writer);
                 }
             }
         } catch (IOException | TemplateException e) {
             throw new IllegalArgumentException(e);
         } finally {
             thread.setContextClassLoader(original);
         }
     }
 
     private void error(Element element, String message) {
         processingEnv.getMessager().printMessage(Kind.ERROR, message, element);
     }
 
     @Override
     public SourceVersion getSupportedSourceVersion() {
         return SourceVersion.latestSupported();
     }
 
     private enum TopLevel implements Predicate<Type> {
 
         INSTANCE;
 
         @Override
         public boolean apply(Type input) {
             return input.getElement().getEnclosingElement() instanceof PackageElement;
         }
 
     }
 
 }
