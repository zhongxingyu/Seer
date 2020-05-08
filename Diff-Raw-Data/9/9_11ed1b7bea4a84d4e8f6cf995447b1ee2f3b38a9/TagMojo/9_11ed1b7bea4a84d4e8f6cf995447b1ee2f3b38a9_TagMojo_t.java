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
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 
 import com.prime.yui4jsf.jsfplugin.digester.Attribute;
 import com.prime.yui4jsf.jsfplugin.digester.Component;
 import com.prime.yui4jsf.jsfplugin.util.FacesMojoUtils;
 
 /**
  * Generates jsp tags of components
  * 
  * @author Latest modification by $Author: cagatay_civici $
  * @version $Revision: 1279 $ $Date: 2008-04-20 13:06:50 +0100 (Sun, 20 Apr 2008) $
  * 
  * @goal generate-tags
  */
 public class TagMojo extends BaseFacesMojo {
 
 	public void execute() throws MojoExecutionException, MojoFailureException {
 	
 		getLog().info("Generating Tags");
 		try{
 			createTags(getComponents());
 			getLog().info("Tags Generated");
 		}
 		catch (Exception e) {
 			getLog().error("Error occured in generating tags" + e.getMessage());
 		}
 	}
 
 	private void createTags(List components) throws Exception {
 		String outputPath = getCreateOutputDirectory();
 
 		for (Iterator iterator = components.iterator(); iterator.hasNext();) {
 			Component component = (Component) iterator.next();
 
 			String packagePath = createPackageDirectory(outputPath, component);
 			String tagClassName = getTagClassName(component.getTagClass());
 			
 			FileWriter fileWriter = new FileWriter(packagePath + File.separator + tagClassName + ".java");
 			BufferedWriter writer = new BufferedWriter(fileWriter);
 
 			writeTagFile(writer, component, tagClassName, outputPath);
 		}
 	}
 
 	private void writeTagFile(BufferedWriter writer, Component component, String tagClassName, String outputPath) throws IOException {
 		writeLicense(writer);
 		writePackageImportAndClassDefinition(writer, component, tagClassName);
 		writeProperties(writer, component);
 		writeReleaseMethod(writer, component);
 		writerPropertiesMethod(writer, component);
 		writeComponentAndRenderers(writer, component);
 		writePropertySetters(writer, component.getAttributes());
 		writeFacesContextGetter(writer);
 
 		writer.write("}");
 		writer.close();
 	}
 
 	private void writerPropertiesMethod(BufferedWriter writer, Component component) throws IOException {
 		writer.write("\tprotected void setProperties(UIComponent uicomponent){\n");
 		writer.write("\t\tsuper.setProperties(uicomponent);\n\n");
 
 		for (Iterator iterator = component.getAttributes().iterator(); iterator.hasNext();) {
 			Attribute attribute = (Attribute) iterator.next();
 			if(isIgnored(attribute, uicomponentAttributes))
 				continue;
 			
 			writer.write(getPropertySetterMethod(attribute));
 		}
 		
 		writer.write("\t}\n");
 		writer.write("\n");
 	}
 	
 
 
 	private String getPropertySetterMethod(Attribute attribute) {
 		String method = "\t\tComponentUtils.set";
 		
 		//Handle special attributes like converter, validator, value, valueChangeListener, action, actionListener
 		if(isIgnored(attribute, specialAttributes))
 			method += StringUtils.capitalize(attribute.getName()) + "Property(getFacesContext(), uicomponent, _" + attribute.getName() + ");\n";
 		else {
 			method += FacesMojoUtils.getWrapperType(attribute.getShortTypeName()) +  "Property(getFacesContext(), uicomponent, \"" + 
 					 attribute.getName() +"\", _" + attribute.getName() +" );\n";
 		}
 		
 		return method;
 	}
 
 	private void writeReleaseMethod(BufferedWriter writer, Component component) throws IOException {
 		writer.write("\tpublic void release(){\n");
 		writer.write("\t\tsuper.release();\n");
 		
 		for (Iterator iterator = component.getAttributes().iterator(); iterator.hasNext();) {
 			Attribute attribute = (Attribute) iterator.next();
 			if(isIgnored(attribute, uicomponentAttributes))
 				continue;
 			
 			writer.write("\t\t_" + attribute.getName() +" = null;\n");
 		}
 		
 		writer.write("\t}\n\n");
 	}
 
 	private void writePropertySetters(BufferedWriter writer, Vector attributes) throws IOException {
 		for (Iterator iterator = attributes.iterator(); iterator.hasNext();) {
 			Attribute attribute = (Attribute) iterator.next();
 			if(isIgnored(attribute, uicomponentAttributes))
 				continue;
 			
 			writer.write("\tpublic void set"+ attribute.getName().substring(0,1).toUpperCase() + attribute.getName().substring(1) + "(String value){\n");
 			writer.write("\t\t_"+attribute.getName() + " = value;\n");
 			writer.write("\t}\n\n");
 		}
 	}
 
 	private void writeProperties(BufferedWriter writer, Component component) throws IOException {
 		for (Iterator iterator = component.getAttributes().iterator(); iterator.hasNext();) {
 			Attribute attribute = (Attribute) iterator.next();
 			if(isIgnored(attribute, uicomponentAttributes))
 				continue;
 			
 			writer.write("\tprivate String _" + attribute.getName() +" = null;\n");
 		}
 		writer.write("\n");
 	}
 
 	private void writeComponentAndRenderers(BufferedWriter writer, Component component) throws IOException {
 		writer.write("\tpublic String getComponentType() {\n");
 		writer.write("\t\treturn " + component.getComponentShortName() + ".COMPONENT_TYPE;\n");
 		writer.write("\t}");
 		writer.write("\n\n");
 		
 		writer.write("\tpublic String getRendererType() {\n");
 		if(component.getRendererType() != null)
 			writer.write("\t\treturn \""+ component.getRendererType() +"\";\n");
 		else
 			writer.write("\t\treturn null;\n");
 		
 		writer.write("\t}");
 		writer.write("\n\n");
 	}
 
 	private void writePackageImportAndClassDefinition(BufferedWriter writer, Component component, String tagClassName) throws IOException {
		writer.write("package com.prime.primefaces.ui.component." + component.getParentPackagePath() + ";\n");
 		writer.write("\n");
 
 		writer.write("import javax.faces.webapp.UIComponentELTag;\n");
 		writer.write("import javax.faces.component.UIComponent;\n");
 		writer.write("import javax.faces.context.FacesContext;\n");
 		writer.write("import com.prime.primefaces.ui.util.ComponentUtils;\n");
 		writer.write("\n");
 		
 		writer.write("public class " + tagClassName + " extends UIComponentELTag {\n");
 		writer.write("\n");
 	}
 
 	private String getTagClassName(String dotSeparatedPackageAndTagClass) {
 		int lastIndex = dotSeparatedPackageAndTagClass.lastIndexOf(".");
 		
 		return dotSeparatedPackageAndTagClass.substring(lastIndex+1);
 	}
 	
 	private boolean isIgnored(Attribute attribute, String[] attributes) {
 		return ArrayUtils.contains(attributes, attribute.getName());
 	}
 }
