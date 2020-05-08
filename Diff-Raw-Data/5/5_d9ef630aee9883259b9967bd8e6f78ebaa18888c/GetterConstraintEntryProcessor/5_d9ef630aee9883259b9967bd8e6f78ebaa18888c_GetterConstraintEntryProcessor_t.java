 package org.dejava.component.i18n.source.processor.impl;
 
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import javax.lang.model.element.AnnotationMirror;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ElementKind;
 import javax.lang.model.element.TypeElement;
 import javax.validation.Constraint;
 
 import org.dejava.component.i18n.source.processor.MessageSourceEntryProcessor;
 import org.dejava.component.reflection.MethodMirror;
 
 /**
  * Processes a class and retrieves entries for getter constraints.
  */
 public class GetterConstraintEntryProcessor implements MessageSourceEntryProcessor {
 
 	/**
 	 * @see org.dejava.component.i18n.source.processor.MessageSourceEntryProcessor#processClass(javax.lang.model.element.TypeElement,
 	 *      javax.lang.model.element.TypeElement)
 	 */
 	@Override
 	public Set<String> processClass(final TypeElement originalClass, final TypeElement currentClass) {
 		// Creates an entry set.
 		final Set<String> entries = new LinkedHashSet<>();
 		// For each enclosed elements of the class.
 		for (final Element currentClassElement : currentClass.getEnclosedElements()) {
			// If the element is a method.
 			if (currentClassElement.getKind() == ElementKind.METHOD) {
 				// If the name is from a getter.
 				if (MethodMirror.isGetter(currentClassElement.getSimpleName().toString())) {
 					// For each annotation in the field.
 					for (final AnnotationMirror currentAnnotation : currentClassElement
 							.getAnnotationMirrors()) {
 						// If the annotation is a constraint.
						if (currentAnnotation.getAnnotationType().asElement().getAnnotation(Constraint.class) != null) {
 							// Adds the current field annotation name to the entry set.
 							entries.add(originalClass.getSimpleName().toString()
 									+ '.'
 									+ MethodMirror.getFieldName(currentClassElement.getSimpleName()
 											.toString()) + "."
 									+ currentAnnotation.getAnnotationType().asElement().getSimpleName());
 						}
 					}
 				}
 			}
 		}
 		// Returns he retrieved entries.
 		return entries;
 	}
 }
