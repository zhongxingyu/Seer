 package org.openntf.xsp.annotations.processor;
 
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.annotation.processing.Filer;
 import javax.annotation.processing.Messager;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.element.VariableElement;
 import javax.tools.Diagnostic.Kind;
 import javax.tools.FileObject;
 
 import org.openntf.xsp.annotations.XspComponent;
 import org.openntf.xsp.annotations.XspDojoRenderer;
 import org.openntf.xsp.annotations.XspProperty;
 import org.openntf.xsp.annotations.processor.ComponentProcessor.PropertyInfo;
 
 /**
  * Generates a renderer code based on {@link XspComponent} and {@link XspDojoRenderer} annotations.
  * @author Mariusz Jakubowski
  *
  */
 public class DojoRendererSourceGenerator extends AbstractGenerator {
 	private String baseSimpleName;
 	private String newSimpleName;
 	private String componentSimpleName;
 
 	private List<PropertyInfo> fields;
 	
 	private StringBuilder source;
 	private String pkg;
 	private XspDojoRenderer dojoAnnotation;
 
 	public DojoRendererSourceGenerator(Filer filer, Messager messager) {		
 		super(filer, messager);
 	}
 
 	@Override
 	public void start() {
 	}
 
 	@Override
 	public void newComponent(TypeElement element, XspComponent annotation) {
 		dojoAnnotation = element.getAnnotation(XspDojoRenderer.class);
 		if (dojoAnnotation == null)
 			return;
 
 		//messager.printMessage(Kind.NOTE, "dojoann " + dojoAnnotation);
 		//messager.printMessage(Kind.NOTE, "dojoannclass " + dojoAnnotation.baseClass());
 		baseSimpleName = dojoAnnotation.baseClass(); 		
 		newSimpleName = element.getSimpleName().toString() + "Renderer";
 		String newFullName = element.getQualifiedName().toString() + "Renderer";
 		componentSimpleName = element.getSimpleName().toString() + "Impl";
 
 		pkg = newFullName.substring(0, newFullName.indexOf(newSimpleName) - 1);
 		
 		source = new StringBuilder();
 		header();
 		module(dojoAnnotation);
 		defaultDojoType(dojoAnnotation);
 		tagName(dojoAnnotation);
 		css(dojoAnnotation);
         fields = new ArrayList<PropertyInfo>();
 	}
 	
 	/**
 	 * Generates header.
 	 */
 	private void header() {
 		source.append("package " + pkg + ";\n\n");
 		source.append("import java.io.IOException;\n" +
 			"import java.util.Map;\n" +
 			"import javax.faces.context.FacesContext;\n" +
 			"import com.ibm.xsp.dojo.FacesDojoComponent;\n" +
 			"import com.ibm.xsp.extlib.renderkit.dojo.DojoRendererUtil;\n" +
 			"import com.ibm.xsp.resource.Resource;\n" +
 			"import com.ibm.xsp.resource.StyleSheetResource;\n" + 
 			"import com.ibm.xsp.resource.DojoModuleResource;\n\n");
 		source.append("public class " + newSimpleName + " extends " + baseSimpleName + " {\n\n");
 	}
 	
 	/**
 	 * Generates getDefaultDojoModule method.
 	 * @param dojoAnnotation
 	 */
 	private void module(XspDojoRenderer dojoAnnotation) {
 		if ("".equals(dojoAnnotation.dojoModule()))
 			return;
 		source.append("private static final DojoModuleResource module = " +
 				"new DojoModuleResource(\"" + dojoAnnotation.dojoModule() + "\");\n");
 		source.append("@Override protected DojoModuleResource getDefaultDojoModule(" +
 				"FacesContext context, FacesDojoComponent component) {\n" +
 				"return  module;\n" +
 				"}\n\n");
 	}
 	
 	/**
 	 * Generates getDefaultDojoType method.
 	 * @param dojoAnnotation
 	 */
 	private void defaultDojoType(XspDojoRenderer dojoAnnotation) {
 		if ("".equals(dojoAnnotation.defaultDojoType()))
 			return;
 		source.append("@Override protected String getDefaultDojoType(" +
 			"FacesContext context, FacesDojoComponent component) {\n" +
 			"return \"" + dojoAnnotation.defaultDojoType() + "\";\n" +
 			"}\n\n");
 	}
 	
 	/**
 	 * Generates getTagName method.
 	 * @param dojoAnnotation
 	 */
 	private void tagName(XspDojoRenderer dojoAnnotation) {
 		if ("".equals(dojoAnnotation.tagName()))
 			return;
 		source.append("@Override protected String getTagName() {\n" +
 			"return \"" + dojoAnnotation.tagName() + "\";\n" +
 			"}\n\n");
 	}
 	
 	/**
 	 * Generates getExtraResources method.
 	 * @param dojoAnnotation
 	 */
 	private void css(XspDojoRenderer dojoAnnotation) {
 		String[] css = dojoAnnotation.css();
 		if (css.length == 0)
 			return;
 		for (int i=0; i<css.length; i++) {
 			source.append("private static final StyleSheetResource css" + Integer.toString(i) +
 					" = new StyleSheetResource(\"" + css[i] + "\");\n");
 		}
 		source.append("private static final Resource[] resources = new Resource[] {\n");
 		for (int i=0; i<css.length; i++) {
 			source.append("css" + Integer.toString(i));
 			if (i<css.length-1)
 				source.append(", ");
 			source.append("\n");
 		}
 		source.append("};\n\n");
 		source.append("@Override protected Resource[] getExtraResources(" +
 				"FacesContext context, FacesDojoComponent component) {\n" +
 				"return resources;\n" +
 				"}\n\n");
 	}
 
 	@Override
 	public void newProperty(VariableElement field, XspProperty annProp) {
 		if (annProp.dontGenerateCode())
 			return;
 		if (dojoAnnotation == null)
 			return;
		PropertyInfo propInfo = ComponentProcessor.javaTypeToXspType(field);
		fields.add(propInfo);
 	}
 	
 	/**
 	 * Generates initDojoAttributes method.
 	 * For all properties the DojoRendererUtil.addDojoHtmlAttributes method is called.
 	 */
 	private void initDojoAttributes() {
 		source.append("@Override protected void initDojoAttributes(" +
 				"FacesContext context, FacesDojoComponent dojoComponent, " +
 				"Map<String, String> attrs) throws IOException {\n" +
 				"super.initDojoAttributes(context, dojoComponent, attrs);\n" +
 				"if (dojoComponent instanceof " + componentSimpleName + " ) {\n" +
 				componentSimpleName + " c = (" + componentSimpleName + ") dojoComponent;\n");
 	    for (int i=0; i<fields.size(); i++) {
	    	PropertyInfo propInfo = fields.get(i);
	    	String prefix = "boolean".equals(propInfo.xspType) ? "is" : "get";
	    	String defaultValue = propInfo.annotation.defaultValue();
 	    	if (!"".equals(defaultValue)) {
 		    	source.append("DojoRendererUtil.addDojoHtmlAttributes(attrs, \"" + 
		    			propInfo.fieldName + "\", c." + prefix + propInfo.fieldUName + "(), " +
 		    			defaultValue + ");\n");
 	    	} else {
 		    	source.append("DojoRendererUtil.addDojoHtmlAttributes(attrs, \"" + 
		    			propInfo.fieldName + "\", c." + prefix + propInfo.fieldUName + "());\n");
 	    	}
 	    }
 		source.append("}\n}\n");
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * Writes renderer source file.
 	 */
 	@Override
 	public void endComponent(TypeElement element) throws Exception {
 		if (dojoAnnotation == null)
 			return;
 		initDojoAttributes();
         source.append("}\n");
 		String fname = pkg.replace('.', '/') + '/' + newSimpleName; 
 		messager.printMessage(Kind.NOTE, fname);
 		FileObject srcFile = filer.createSourceFile(fname, element);
 		messager.printMessage(Kind.NOTE, "creating " + srcFile.toUri());
 		Writer writer = srcFile.openWriter();
 		writer.write(source.toString());
 		writer.close();
 	}
 
 	@Override
 	public void end() throws Exception {
 	}
 
 }
