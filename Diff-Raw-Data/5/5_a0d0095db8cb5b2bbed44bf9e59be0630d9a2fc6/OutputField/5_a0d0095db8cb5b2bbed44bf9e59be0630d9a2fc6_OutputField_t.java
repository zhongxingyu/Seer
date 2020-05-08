 package org.cchmc.bmi.snpomics;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.List;
 
 import org.cchmc.bmi.snpomics.annotation.interactive.InteractiveAnnotation;
 import org.cchmc.bmi.snpomics.annotation.interactive.MetaAnnotation;
 import org.cchmc.bmi.snpomics.annotation.reference.ReferenceAnnotation;
 import org.cchmc.bmi.snpomics.exception.SnpomicsException;
 
 public class OutputField {
 
 	public OutputField(Method source) {
 		method = source;
 		annotation = method.getAnnotation(MetaAnnotation.class);
		if (annotation == null)
			throw new SnpomicsException("Method '"+method.getDeclaringClass().getCanonicalName()+"."+method.getName()+
							"' is an OutputField with no MetaAnnotation");
 	}
 	
 	public String getName() {
 		return annotation.name();
 	}
 	
 	public String getDescription() {
 		return annotation.description();
 	}
 	
 	public List<String> getGroups() {
 		return Arrays.asList(annotation.groups());
 	}
 	
 	public List<Class<? extends ReferenceAnnotation>> getReferences() {
 		return Arrays.asList(annotation.ref());
 	}
 	
 	@SuppressWarnings("unchecked")
 	public Class<? extends InteractiveAnnotation> getDeclaringClass() {
 		return (Class<? extends InteractiveAnnotation>) method.getDeclaringClass();
 	}
 	
 	public String getOutput(InteractiveAnnotation annot) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
 		return method.invoke(annot).toString();
 	}
 	
 	public String getInternalName() {
 		return method.getName();
 	}
 	
 	public static boolean isOutputField(Method toCheck) {
 		return toCheck.isAnnotationPresent(MetaAnnotation.class);
 	}
 	
 	private Method method;
 	private MetaAnnotation annotation;
 }
