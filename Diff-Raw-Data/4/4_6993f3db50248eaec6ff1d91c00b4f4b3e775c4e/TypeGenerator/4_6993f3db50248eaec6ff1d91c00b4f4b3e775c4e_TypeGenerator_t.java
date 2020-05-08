 package de.nordakademie.nakjavagenerator.generators;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import javax.annotation.processing.AbstractProcessor;
 import javax.annotation.processing.Filer;
 import javax.annotation.processing.ProcessingEnvironment;
 import javax.annotation.processing.RoundEnvironment;
 import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
 import javax.lang.model.element.AnnotationMirror;
 import javax.lang.model.element.AnnotationValue;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ExecutableElement;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.type.DeclaredType;
 import javax.lang.model.type.TypeMirror;
 import javax.tools.JavaFileObject;
 
@SupportedSourceVersion(SourceVersion.RELEASE_7)
 @SupportedAnnotationTypes(value = { "de.nordakademie.nakjava.server.internal.model.VisibleField" })
 public class TypeGenerator extends AbstractProcessor {
 
 	private Filer filer;
 	private VisibleModelFieldsClass sourceClass;
 
 	@Override
 	public synchronized void init(ProcessingEnvironment processingEnv) {
 		// TODO Auto-generated method stub
 		super.init(processingEnv);
 		filer = processingEnv.getFiler();
 		sourceClass = new VisibleModelFieldsClass();
 	}
 
 	@Override
 	public boolean process(Set<? extends TypeElement> annotations,
 			RoundEnvironment roundEnv) {
 
 		TypeElement annotation;
 
 		// TODO is still a little bit dirty :-)
 		if (annotations.size() == 1) {
 			annotation = annotations.iterator().next();
 
 			for (Element element : roundEnv
 					.getElementsAnnotatedWith(annotation)) {
 
 				String transformatorTypeString = null;
 				List<String> targets = new LinkedList<>();
 
 				for (AnnotationMirror annotationMirror : element
 						.getAnnotationMirrors()) {
 					for (ExecutableElement valueKey : annotationMirror
 							.getElementValues().keySet()) {
 						if (valueKey.getSimpleName().contentEquals("targets")) {
 							List targetValues = ((List) annotationMirror
 									.getElementValues().get(valueKey)
 									.getValue());
 							for (Object target : targetValues) {
 								AnnotationMirror targetAnnotation = (AnnotationMirror) (((AnnotationValue) target)
 										.getValue());
 								for (ExecutableElement targetAnnotationValue : targetAnnotation
 										.getElementValues().keySet()) {
 									if (targetAnnotationValue.getSimpleName()
 											.contentEquals("target")) {
 										targets.add(targetAnnotation
 												.getElementValues().get(
 														targetAnnotationValue)
 												.toString());
 									}
 								}
 							}
 						}
 
 						if (valueKey.getSimpleName().contentEquals(
 								"transformer")) {
 							TypeElement transformer = (TypeElement) ((DeclaredType) annotationMirror
 									.getElementValues().get(valueKey)
 									.getValue()).asElement();
 							for (TypeMirror interfacce : transformer
 									.getInterfaces()) {
 								if (interfacce instanceof DeclaredType) {
 									DeclaredType interfaceType = (DeclaredType) interfacce;
 									if (interfaceType.asElement()
 											.getSimpleName().contentEquals(
 													"Transformator")) {
 										transformatorTypeString = interfaceType
 												.getTypeArguments().get(1)
 												.toString();
 									}
 								}
 							}
 						}
 
 					}
 				}
 
 				for (String target : targets) {
 					sourceClass.addField(new VisibleModelField(
 							transformatorTypeString == null ? element.asType()
 									.toString() : transformatorTypeString,
 							element.getEnclosingElement().getSimpleName()
 									.toString().toUpperCase()
 									+ "_"
 									+ element.getSimpleName().toString()
 											.toUpperCase() + "_" + target,
 							element.getEnclosingElement().asType().toString()
 									+ "." + element.getSimpleName() + "."
 									+ target));
 				}
 
 			}
 
 			finish();
 
 		}
 
 		// TODO Auto-generated method stub
 		return true;
 	}
 
 	private void finish() {
 		try {
 			JavaFileObject file = filer.createSourceFile(
 					"de/nordakademie/nakjava/generated/VisibleModelFields",
 					null);
 			Writer writer = file.openWriter();
 			writer.write(sourceClass.getSourceContent());
 			writer.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
