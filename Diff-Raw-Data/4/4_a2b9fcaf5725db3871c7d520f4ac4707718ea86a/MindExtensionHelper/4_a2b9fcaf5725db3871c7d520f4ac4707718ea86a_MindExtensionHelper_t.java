 package org.ow2.mindEd.adl.editor.graphic.ui.custom.helpers;
 
 import java.net.URL;
 import java.util.List;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.swt.graphics.Color;
 import org.ow2.mindEd.adl.Annotation;
 import org.ow2.mindEd.adl.AnnotationsList;
 import org.ow2.mindEd.adl.ArchitectureDefinition;
 import org.ow2.mindEd.adl.BindingDefinition;
 import org.ow2.mindEd.adl.Body;
 import org.ow2.mindEd.adl.InterfaceDefinition;
 import org.ow2.mindEd.adl.editor.graphic.ui.custom.figures.IFractalShape;
 import org.ow2.mindEd.adl.editor.graphic.ui.part.MindDiagramEditorPlugin;
 
 public class MindExtensionHelper {
 	
 	protected static final String ANNOTATION_EXTENSION_ID = "org.ow2.mindEd.adl.editor.graphic.ui.mindAnnotation";
 	
 	/**
 	 * @return true if the semantic element has an annotation on it
 	 */
 	public static Boolean isAnnotated(EObject semantic) {
 		Boolean isAnnotated = false;
 		
 		if (semantic instanceof Body) {
 			semantic = ((Body) semantic).getParentComponent();
 		}
 		List<EObject> contents = semantic.eContents();
 		for (EObject content : contents) {
 			if (content instanceof AnnotationsList) {
 				isAnnotated = true;
 			}
 		}
 		return isAnnotated;
 	}
 	
 	/**
 	 * @return true if the semantic element has the given annotation on it
 	 */
 	public static Boolean hasAnnotation(EObject semantic, String annotationName) {
 		Boolean hasAnnotation = false;
 		
 		if (semantic instanceof Body) {
 			semantic = ((Body) semantic).getParentComponent();
 		}
 		List<EObject> contents = semantic.eContents();
 		for (EObject content : contents) {
 			if (content instanceof AnnotationsList) {
 				List<Annotation> annotations = ((AnnotationsList) content).getAnnotations();
 				for (Annotation annotation : annotations) {
 					if ((annotation.getName()).compareTo(annotationName) == 0)
 						hasAnnotation = true;
 				}
 			}
 		}
 		return hasAnnotation;
 	}
 	
 	/**
 	 * 
 	 * @return the root configuration element of the extension
 	 * org.ow2.mindEd.adl.editor.graphic.ui.mindAnnotation
 	 */
 	protected static IConfigurationElement[] getAnnotationExtension() {
 		return Platform.getExtensionRegistry()
 			.getConfigurationElementsFor(ANNOTATION_EXTENSION_ID);
 	}
 	
 	protected static IConfigurationElement getAnnotationExtensionAppearance(EObject semantic) {
 		// Get the extension
 		IConfigurationElement[] config = getAnnotationExtension();
 		
 		try {
 			for (IConfigurationElement element : config) {
 				
 				// Check if there is the given annotation
 				String name = element.getAttribute("annotationName");
 				if (name == "" || !hasAnnotation(semantic, name))
 					continue;
 				
 				IConfigurationElement[] appearances = element.getChildren("Appearance");
 				for (IConfigurationElement appearance : appearances) {
 					// This is the appearance parameters
 					// Check if the element type match
 					IConfigurationElement[] elementTypes = appearance.getChildren("Element");
 					for (IConfigurationElement elementType : elementTypes) {
 						// This is the element types this extension is applicable on
 						String type = elementType.getAttribute("elementType");
 						if (type.equals("Components") &&
 								(semantic instanceof ArchitectureDefinition
 								|| semantic instanceof Body))
 							return appearance;
 						else if (type.equals("Interfaces") &&
 								semantic instanceof InterfaceDefinition)
 							return appearance;
 						else if (type.equals("Bindings") &&
 								semantic instanceof BindingDefinition)
 							return appearance;
 					}
 				}
 			}
 			return null;
 		} catch (Exception e) {
 			MindDiagramEditorPlugin.getInstance().logError("Error in extension mindAnnotation", e);
 		}
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @param semantic
 	 * @return the name (Blue, Red, Light Green, etc) of the color specified for this element in the extension
 	 * org.ow2.mindEd.adl.editor.graphic.ui.mindAnnotation, or null
 	 */
 	public static String getAnnotationExtensionColorName(EObject semantic) {
 		IConfigurationElement extensionElement = getAnnotationExtensionAppearance(semantic);
 		if (extensionElement != null) {
 			return extensionElement.getAttribute("color");
 		}
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @param semantic
 	 * @return the color specified for this element in the extension org.ow2.mindEd.adl.editor.graphic.ui.mindAnnotation, or null
 	 */
 	public static Color getAnnotationExtensionColor(EObject semantic) {
 		String color = getAnnotationExtensionColorName(semantic);
 		if (color != null)
 			return getColorFromString(color);
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @param semantic
 	 * @return the light version of the color specified for this element in the extension
 	 * org.ow2.mindEd.adl.editor.graphic.ui.mindAnnotation, or null
 	 */
 	public static Color getAnnotationExtensionColorLight(EObject semantic) {
 		String color = getAnnotationExtensionColorName(semantic);
 		if (color != null)
 			return getLightColorFromString(color);
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @param semantic
 	 * @return the URL of the icon specified for this element in the extension
 	 * org.ow2.mindEd.adl.editor.graphic.ui.mindAnnotation, or null
 	 */
 	public static URL getExtensionIconURL(EObject semantic) {
 		IConfigurationElement extensionElement = getAnnotationExtensionAppearance(semantic);
 		if (extensionElement != null && !(semantic instanceof Body)) {
 			String icon = extensionElement.getAttribute("icon");
 			String contributor = extensionElement.getContributor().getName();
			if (icon != null && contributor != null)
				return Platform.getBundle(contributor).getResource(icon);
 		}
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @param semantic
 	 * @return the raw path of the icon specified for this element in the extension
 	 * org.ow2.mindEd.adl.editor.graphic.ui.mindAnnotation, or null
 	 */
 	public static String getExtensionIcon(EObject semantic) {
 		IConfigurationElement extensionElement = getAnnotationExtensionAppearance(semantic);
 		if (extensionElement != null) {
 			return extensionElement.getAttribute("icon");
 		}
 		return null;
 	}
 	
 	/**
 	 * @param color the color to return
 	 * @return the color from IFractalShape
 	 */
 	protected static Color getColorFromString(String color) {
 		if (color == null)
 			return null;
 		if (color.equals("Yellow"))
 			return IFractalShape.YELLOW;
 		else if (color.equals("Light Yellow"))
 			return IFractalShape.LIGHT_YELLOW;
 		else if (color.equals("Red"))
 			return IFractalShape.RED;
 		else if (color.equals("Light Red"))
 			return IFractalShape.LIGHT_RED;
 		else if (color.equals("Blue"))
 			return IFractalShape.BLUE;
 		else if (color.equals("Light Blue"))
 			return IFractalShape.LIGHT_BLUE;
 		else if (color.equals("Green"))
 			return IFractalShape.GREEN;
 		else if (color.equals("Light Green"))
 			return IFractalShape.LIGHT_GREEN;
 		else if (color.equals("Purple"))
 			return IFractalShape.PURPLE;
 		else if (color.equals("Light Purple"))
 			return IFractalShape.LIGHT_PURPLE;
 		else if (color.equals("Orange"))
 			return IFractalShape.ORANGE;
 		else if (color.equals("Light Orange"))
 			return IFractalShape.LIGHT_ORANGE;
 		else if (color.equals("Brown"))
 			return IFractalShape.BROWN;
 		else if (color.equals("Light Brown"))
 			return IFractalShape.LIGHT_BROWN;
 		else if (color.equals("Grey"))
 			return IFractalShape.GREY;
 		else if (color.equals("Light Grey"))
 			return IFractalShape.LIGHT_GREY;
 		
 		return null;
 	}
 	
 	
 	
 	/**
 	 * @param color the color to return
 	 * @return the light version of the color from IFractalShape
 	 */
 	protected static Color getLightColorFromString(String color) {
 		if (color == null)
 			return null;
 		if (color.equals("Yellow"))
 			return IFractalShape.LIGHT_YELLOW;
 		else if (color.equals("Light Yellow"))
 			return IFractalShape.LIGHT_YELLOW;
 		else if (color.equals("Red"))
 			return IFractalShape.LIGHT_RED;
 		else if (color.equals("Light Red"))
 			return IFractalShape.LIGHT_RED;
 		else if (color.equals("Blue"))
 			return IFractalShape.LIGHT_BLUE;
 		else if (color.equals("Light Blue"))
 			return IFractalShape.LIGHT_BLUE;
 		else if (color.equals("Green"))
 			return IFractalShape.LIGHT_GREEN;
 		else if (color.equals("Light Green"))
 			return IFractalShape.LIGHT_GREEN;
 		else if (color.equals("Purple"))
 			return IFractalShape.LIGHT_PURPLE;
 		else if (color.equals("Light Purple"))
 			return IFractalShape.LIGHT_PURPLE;
 		else if (color.equals("Orange"))
 			return IFractalShape.LIGHT_ORANGE;
 		else if (color.equals("Light Orange"))
 			return IFractalShape.LIGHT_ORANGE;
 		else if (color.equals("Brown"))
 			return IFractalShape.LIGHT_BROWN;
 		else if (color.equals("Light Brown"))
 			return IFractalShape.LIGHT_BROWN;
 		else if (color.equals("Grey"))
 			return IFractalShape.LIGHT_GREY;
 		else if (color.equals("Light Grey"))
 			return IFractalShape.LIGHT_GREY;
 		
 		return null;
 	}
 
 }
