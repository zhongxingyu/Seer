 /*
  * Copyright 2007 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.prime.yui4jsf.jsfplugin.mojo;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 
 import com.prime.yui4jsf.jsfplugin.digester.Attribute;
 import com.prime.yui4jsf.jsfplugin.digester.Component;
 import com.prime.yui4jsf.jsfplugin.util.FacesMojoUtils;
 
 /**
  * Generates components
  * 
  * @author Latest modification by $Author: cagatay_civici $
  * @version $Revision: 1279 $ $Date: 2008-04-20 13:06:50 +0100 (Sun, 20 Apr 2008) $
  * 
  * @goal generate-components
  */
 public class ComponentMojo extends BaseFacesMojo{
 	
 	public void execute() throws MojoExecutionException, MojoFailureException {
 		getLog().info("Generating Components");
 		
 		try {
 			writeComponents(getComponents());
 			getLog().info("Components Generated successfully");
 			
 			String sourceDirectory =  project.getBuild().getDirectory() +File.separator + "maven-jsf-plugin"
 									+ File.separator + "main" + File.separator + "java"; 
 			project.addCompileSourceRoot(  sourceDirectory );
 		} catch (Exception e) {
 			getLog().info("Error occured in component generation:");
 			getLog().info(e.toString());
 		}
 	}
 
 	private void writeComponents(List components) throws Exception{
 		
 		String outputPath = getCreateOutputDirectory();
 		
 		for (Iterator iterator = components.iterator(); iterator.hasNext();) {
 			Component component = (Component) iterator.next();
 			getLog().info("Generating Component Source for:" + component.getComponentClass());
 			
 			String packagePath = createPackageDirectory(outputPath, component);
 			
 			FileWriter fileWriter = new FileWriter(packagePath + File.separator +  component.getComponentShortName() + ".java");	
 			BufferedWriter writer = new BufferedWriter(fileWriter);
 		
 			writeComponent(writer, component);
 			
 			writer.close();
 			fileWriter.close();
 		}
 	}
 
 	private void writeComponent(BufferedWriter writer, Component component) throws IOException {
 		writeLicense(writer);
 		writePackage(writer, component);
 		writeImports(writer, component);
 		writeClassDeclaration(writer, component);
 		writeComponentProperties(writer, component);
 		writeAttributesDeclarations(writer, component);
 		writeConstructor(writer, component);
 		writeComponentFamily(writer, component);
 		writeAttributes(writer, component);
 		writeSaveState(writer, component);
 		writeRestoreState(writer, component);
 		writeTemplate(writer, component);
 		writeFacesContextGetter(writer);
 		writer.write("}");
 	}
 
 	private void writeImports(BufferedWriter writer, Component component) throws IOException {
 		writer.write("import " + component.getParent() + ";\n");
 		writer.write("import javax.faces.context.FacesContext;\n");
 		writer.write("import javax.faces.el.ValueBinding;\n");
 		writer.write("import javax.faces.context.FacesContext;\n");
 		writer.write("import com.prime.primefaces.ui.util.ComponentUtils;\n");
 		
 		if(hasMethodBinding(component))
 		writer.write("import javax.faces.el.MethodBinding;\n");
 		
 		String templateImports = getTemplateImports(component);
 		
 		if (StringUtils.isNotEmpty(templateImports)) {
 			writer.write(templateImports);
 		}
 		writer.write("\n");
 	}
 	
 	private void writeComponentProperties(BufferedWriter writer, Component component) throws IOException {
 		writer.write("\tpublic static final String COMPONENT_TYPE = \"" + component.getComponentType() + "\";\n");
 		writer.write("\tpublic static final String COMPONENT_FAMILY = \"" + component.getComponentFamily() + "\";\n");
 		
 		if(component.getRendererType() != null)
 			writer.write("\tprivate static final String DEFAULT_RENDERER = \"" + component.getRendererType() + "\";\n");
 		
 		writer.write("\n");
 	}
 	
 	private void writeAttributesDeclarations(BufferedWriter writer, Component component) throws IOException {
 		for (Iterator attributeIterator = component.getAttributes().iterator(); attributeIterator.hasNext();) {
 			Attribute attribute = (Attribute) attributeIterator.next();
 			if(attribute.isIgnored())
 				continue;
 			
 			String returnType = FacesMojoUtils.getWrapperType(attribute.getShortTypeName());
 			writer.write("\tprivate " + returnType + " _" + attribute.getName() + ";\n");
 		}
 		writer.write("\n");
 	}
 	
 	private void writeClassDeclaration(BufferedWriter writer, Component component) throws IOException {
 		writer.write("public class " + component.getComponentShortName() + " extends " + component.getParentShortName() + "{");
 		writer.write("\n\n");
 	}
 	
 	private void writeConstructor(BufferedWriter writer, Component component) throws IOException {
 		writer.write("\tpublic " + component.getComponentShortName() + "() {\n");
 		
 		if(component.getRendererType() != null)
 			writer.write("\t\tsetRendererType(DEFAULT_RENDERER);\n");
 		else
 			writer.write("\t\tsetRendererType(null);\n");
 		
 		writer.write("\t}");
 		writer.write("\n\n");
 	}
 	
 	private void writeComponentFamily(BufferedWriter writer, Component component) throws IOException {
 		writer.write("\tpublic String getFamily() {\n");
 		writer.write("\t\treturn COMPONENT_FAMILY;\n");
 		writer.write("\t}");
 		writer.write("\n\n");
 	}
 	
 	private void writeAttributes(BufferedWriter writer, Component component) throws IOException {
 		for (Iterator attributeIterator = component.getAttributes().iterator(); attributeIterator.hasNext();) {
 			Attribute attribute = (Attribute) attributeIterator.next();
 			if(attribute.isIgnored())
 				continue;
 			
 			if(isMethodBinding(attribute)) {
 				writeMethodBindingAttribute(writer, attribute);
 				
 			} else {
 				//TODO:Make below a method
 				writer.write("\tpublic " + attribute.getShortTypeName() + " " + resolveGetterPrefix(attribute) + attribute.getCapitalizedName() + "() {\n");
 				writer.write("\t\tif(_" + attribute.getName() + " != null )\n");
 				writer.write("\t\t\treturn _" + attribute.getName() + FacesMojoUtils.getPrimitiveMethod(attribute.getShortTypeName()) + ";\n"); 
 				writer.write("\n");
 				
 				writer.write("\t\tValueBinding vb = getValueBinding(\"" + attribute.getName() + "\");\n");
 				writer.write("\t\treturn vb != null ? " + getVBResolverCode(attribute.getType()) + " : " + attribute.getDefaultValue() + ";\n");
 				writer.write("\t}\n");
 				
 				writer.write("\tpublic void set" + attribute.getCapitalizedName() + "(" + attribute.getShortTypeName() + " " + attribute.getName() + "Value) {\n");
 				if(!FacesMojoUtils.shouldWrap(attribute.getType()))
 					writer.write("\t\t_" + attribute.getName() + " = " + attribute.getName() + "Value;\n");
 				else
 					writer.write("\t\t_" + attribute.getName() + " = new " + 
 							FacesMojoUtils.getWrapperType(attribute.getShortTypeName()) + "(" + attribute.getName() + "Value);\n");
 				
 				writer.write("\t}\n\n");
 			}
 		}
 	}
 	
 	private void writeMethodBindingAttribute(BufferedWriter writer, Attribute attribute) throws IOException {
 		writer.write("\tpublic " + attribute.getShortTypeName() + " " + resolveGetterPrefix(attribute) + attribute.getCapitalizedName() + "() {\n");
 		writer.write("\t\t\treturn _" + attribute.getName() + FacesMojoUtils.getPrimitiveMethod(attribute.getShortTypeName()) + ";\n"); 
 		writer.write("\t}\n");
 		
 		writer.write("\tpublic void set" + attribute.getCapitalizedName() + "(" + attribute.getShortTypeName() + " " + attribute.getName() + "Value) {\n");
 		writer.write("\t\t_" + attribute.getName() + " = " + attribute.getName() + "Value;\n");
 		
 		writer.write("\t}\n\n");
 	}
 	
 	private void writeSaveState(BufferedWriter writer, Component component) throws IOException {
 		//ignore id,rendered,binding  
 		int attributesToSave = FacesMojoUtils.getStateAllocationSize(component) + 1;
 		int attributeNo = 1;
 		
 		writer.write("\tpublic Object saveState(FacesContext context) {\n");
 		writer.write("\t\tObject values[] = new Object[" + attributesToSave + "];\n");
 		writer.write("\t\tvalues[0] = super.saveState(context);\n");
 		
 		for (Iterator attributeIterator = component.getAttributes().iterator(); attributeIterator.hasNext();) {
 			Attribute attribute = (Attribute) attributeIterator.next();
 			if(attribute.isIgnored())
 				continue;
 			
 			if(!isMethodBinding(attribute))
 				writer.write("\t\tvalues[" + attributeNo + "] = _" + attribute.getName() +";\n");
 			else
 				writer.write("\t\tvalues[" + attributeNo + "] = saveAttachedState(context, _" + attribute.getName() + ");\n");
 				
 			attributeNo++;
 		}
 		
 		writer.write("\t\treturn ((Object) values);\n");
 		writer.write("\t}\n");
 	}
 	
 	private void writeRestoreState(BufferedWriter writer, Component component) throws IOException {
 		int attributeNo = 1;
 		
 		writer.write("\tpublic void restoreState(FacesContext context, Object state) {\n");
 		writer.write("\t\tObject values[] = (Object[]) state;\n");
 		writer.write("\t\tsuper.restoreState(context, values[0]);\n");
 		
 		for (Iterator attributeIterator = component.getAttributes().iterator(); attributeIterator.hasNext();) {
 			Attribute attribute = (Attribute) attributeIterator.next();
 			if(attribute.isIgnored())
 				continue;
 			
 			if(!isMethodBinding(attribute))
 				writer.write("\t\t_" + attribute.getName() + " = (" + FacesMojoUtils.getWrapperType(attribute.getShortTypeName()) + ") values[" + attributeNo + "];\n");
 			else
 				writer.write("\t\t_" + attribute.getName() + " = (MethodBinding) restoreAttachedState(context, values[" + attributeNo + "]);\n");
 				
 			attributeNo++;
 		}
 		writer.write("\t}\n");
 	}
 	
 	private void writePackage(BufferedWriter writer, Component component) throws IOException {
		writer.write("package com.prime.primefaces.ui.component." + component.getParentPackagePath() + ";\n\n");
 	}
 	
 	private void writeTemplate(BufferedWriter writer, Component component) throws IOException{
 		try {
 			File template = getTemplate(component);
 			FileReader fileReader = new FileReader(template);
 			BufferedReader reader = new BufferedReader(fileReader);
 			String line = null;
 			
 			getLog().info("Writing template for " + component.getComponentShortName());
 			while((line = reader.readLine()) != null) {
 				if (line.startsWith("import ")) continue;
 				writer.write(line);
 				writer.write("\n");
 				
 			}
 		}catch(FileNotFoundException fileNotFoundException) {
 			return;
 		}
 	}
 	
 	private String getTemplateImports(Component component) throws IOException {
 		try {
 			StringBuffer buf = new StringBuffer();
 			File template = getTemplate(component);
 			FileReader fileReader = new FileReader(template);
 			BufferedReader reader = new BufferedReader(fileReader);
 			String line = null;
 			
 			getLog().info("Looking for template imports of " + component.getComponentShortName());
 			while(((line = reader.readLine()) != null) && (line.startsWith("import "))) {
 				buf.append(line).append("\n");
 			}
 			
 			return buf.toString();
 		}catch(FileNotFoundException fileNotFoundException) {
 		}
 		return null;
 	}
 	
 	private String getVBResolverCode(String type) {
 		if(!FacesMojoUtils.shouldWrap(type)) {
 			return "(" + type + ")" + " vb.getValue(getFacesContext())";
 		}else {
 			return "ComponentUtils.get" + FacesMojoUtils.getWrapperType(type) + "Value(getFacesContext(),vb)";
 		}
 	}
 	
 	protected boolean isBoolean(Attribute attribute) {
 		return attribute.getType().equals("boolean");
 	}
 	
 	protected boolean isMethodBinding(Attribute attribute) {
 		return attribute.getType().equals("javax.faces.el.MethodBinding");
 	}
 	
 	protected String resolveGetterPrefix(Attribute attribute) {
 		if(isBoolean(attribute))
 			return "is";
 		else
 			return "get";
 	}
 	
 	protected File getTemplate(Component component) {
 		String templatePath = project.getBasedir() + File.separator + templatesDir;
 		String[] packagePath = component.getPackage().split("\\.");
 		String templateFileName = component.getComponentShortName() + "Template.java";
 		
 		for (int i = 0; i < packagePath.length; i++) {
 			templatePath = templatePath + File.separator + packagePath[i];
 		}
 		
 		return new File(templatePath + File.separator + templateFileName);
 	}
 	
 	protected boolean hasMethodBinding(Component component) {
 		for (Iterator iterator = component.getAttributes().iterator(); iterator.hasNext();) {
 			Attribute attribute = (Attribute) iterator.next();
 			if(attribute.getType().equals("javax.faces.el.MethodBinding"))
 				return true;
 		}
 		
 		return false;
 		
 	}
 }
