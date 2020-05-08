 package com.seitenbau.sicgwaf.generator;
 
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.seitenbau.sicgwaf.component.Component;
 import com.seitenbau.sicgwaf.component.HtmlElementComponent;
 import com.seitenbau.sicgwaf.component.InputComponent;
 
 public class InputComponentGenerator extends HtmlElementComponentGenerator
 {
   public String getClassName(
       String componentName,
       Component rawComponent,
       String targetPackage)
   {
     if (componentName != null)
     {
       return toJavaName(componentName);
     }
     return InputComponent.class.getSimpleName();
   }
   
   public void generate(
       String componentName,
       Component component,
       String targetPackage,
       Map<String, String> filesToWrite)
   {
     super.generate(componentName, component, targetPackage, filesToWrite);
 
     HtmlElementComponent htmlElementCompont = (HtmlElementComponent) component;
     String className = getClassName(componentName, component, targetPackage);
     StringBuilder fileContent = new StringBuilder();
     String rootContent = filesToWrite.get(className);
     
     // remove last "}"
     rootContent = rootContent.substring(0,  rootContent.lastIndexOf("}") -1);
     
     // add import
     int indexOfImport = rootContent.indexOf("\nimport");
     fileContent.append(rootContent.substring(0, indexOfImport)).append("import ")
         .append(HttpServletRequest.class.getName()).append(";\n").append(rootContent.substring(indexOfImport));
 
     fileContent.append("\n  @Override\n");
     fileContent.append("  public void processRequest(HttpServletRequest request)\n");
     fileContent.append("  {\n");
     fileContent.append("    if (request.getParameter(\"" + htmlElementCompont.attributes.get("name") + "\") != null)\n");
     fileContent.append("    {\n");
     fileContent.append("      onSubmit();\n");
     fileContent.append("    }\n");
     fileContent.append("  }\n\n");
     fileContent.append("  public void onSubmit()\n");
     fileContent.append("  {\n");
     fileContent.append("  }\n");
     fileContent.append("}\n");
     
     filesToWrite.put(className, fileContent.toString());
   }
 
   public void generateExtension(
       String componentName,
       Component component,
       String targetPackage,
       Map<String, String> filesToWrite)
   {
     InputComponent inputComponent = (InputComponent) component;
     String className = getClassName(componentName, component, targetPackage);
     String extensionClassName = getExtensionClassName(componentName, component, targetPackage);
     StringBuilder fileContent = new StringBuilder();
     fileContent.append("package ").append(targetPackage).append(";\n\n");
     fileContent.append("\n");
    fileContent.append("import ").append(Component.class.getName()).append(";\n");
    fileContent.append("\n");
     fileContent.append("public class ").append(extensionClassName)
         .append(" extends ").append(className)
         .append("\n");
     fileContent.append("{\n");
    fileContent.append("  public " + extensionClassName + "(Component parent)\n");
    fileContent.append("  {\n");
    fileContent.append("    super(parent);\n");
    fileContent.append("  }\n");
     fileContent.append("}\n");
     filesToWrite.put(extensionClassName, fileContent.toString());
   }
 }
