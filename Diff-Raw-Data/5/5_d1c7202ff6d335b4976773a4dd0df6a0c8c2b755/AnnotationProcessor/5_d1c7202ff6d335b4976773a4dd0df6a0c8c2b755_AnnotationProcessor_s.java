 package de.jaschastarke.maven;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.List;
 import java.util.Set;
 
 import javax.annotation.processing.AbstractProcessor;
 import javax.annotation.processing.ProcessingEnvironment;
 import javax.annotation.processing.RoundEnvironment;
 import javax.annotation.processing.SupportedAnnotationTypes;
 import javax.annotation.processing.SupportedSourceVersion;
 import javax.lang.model.SourceVersion;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.Name;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.util.Elements;
 import javax.tools.FileObject;
 import javax.tools.StandardLocation;
 
 import de.jaschastarke.utils.ClassDescriptorStorage;
 import de.jaschastarke.utils.ClassDescriptorStorage.ClassDescription;
 
 @SupportedAnnotationTypes("de.jaschastarke.maven.ArchiveDocComments")
 @SupportedSourceVersion(SourceVersion.RELEASE_6)
 public class AnnotationProcessor extends AbstractProcessor {
     /**
      * Workaround because processor is called twice by maven-compiler-plugin
      */
     private static boolean alreadyRun = false;
     
     private Elements elementUtils;
     //private Types typeUtils;
 
     @Override
     public synchronized void init(final ProcessingEnvironment processingEnv) {
         super.init(processingEnv);
         elementUtils = processingEnv.getElementUtils();
         //typeUtils = processingEnv.getTypeUtils();
     }
 
     @Override
     public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
         if (alreadyRun)
             return false;
         alreadyRun = true;
         
         ClassDescriptorStorage cds = ClassDescriptorStorage.getInstance();
         
         for (Element elem: roundEnv.getElementsAnnotatedWith(ArchiveDocComments.class)) {
             //ArchiveDocComments annot = elem.getAnnotation(ArchiveDocComments.class);
             
             TypeElement telem = (TypeElement) elem; // Because the Annotation is targeted to Types only
             Name name = elementUtils.getBinaryName(telem);
             
             ClassDescription cd = cds.getClassFor(name.toString());
             
             List<? extends Element> members = elementUtils.getAllMembers(telem);
             for (Element symbol : members) {
                 String descr = elementUtils.getDocComment(symbol);
                 if (descr != null)
                     cd.setElDocComment(symbol.getSimpleName().toString(), descr.trim());
             }
             
             String descr = elementUtils.getDocComment(elem);
             if (descr != null)
                 cd.setDocComment(descr.trim());
         }
 
         try {
             FileObject resource = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/descriptions.jos");
            cds.store(new File(resource.getName()));
             
             // Debugging
             FileObject txtresource = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, "", "META-INF/descriptions.txt");
            File file = new File(txtresource.getName());
             file.getParentFile().mkdirs();
             FileWriter txtfile = new FileWriter(file);
             txtfile.write(cds.toString());
             txtfile.close();
         } catch (IOException e) {
             throw new RuntimeException("Annotation processor failed to write output file", e);
         }
         
         return false;
     }
 
 }
