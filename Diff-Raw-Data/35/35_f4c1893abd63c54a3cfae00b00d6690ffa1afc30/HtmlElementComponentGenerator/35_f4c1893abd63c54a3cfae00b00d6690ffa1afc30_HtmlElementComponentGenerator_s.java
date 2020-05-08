 package com.seitenbau.micgwaf.generator;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import com.seitenbau.micgwaf.component.ChildListComponent;
 import com.seitenbau.micgwaf.component.Component;
 import com.seitenbau.micgwaf.component.HtmlElementComponent;
 import com.seitenbau.micgwaf.component.SnippetComponent;
 import com.seitenbau.micgwaf.component.SnippetListComponent;
 
 public class HtmlElementComponentGenerator extends ComponentGenerator
 {
   @Override
   public JavaClassName getClassName(
       Component component,
       String targetPackage)
   {
     return toJavaClassName(component.id, targetPackage);
   }
   
   @Override
   public boolean generateExtensionClass(Component component)
   {
     if (component.generationParameters != null)
     {
       if (component.generationParameters.generateExtensionClass != null)
       {
         return component.generationParameters.generateExtensionClass;
       }
       return false;
     }
     return true;
   }
   
   @Override
   public String generate(
         Component component,
         String targetPackage)
   {
     HtmlElementComponent htmlElementCompont = (HtmlElementComponent) component;
    String className = getClassName(component, targetPackage).getSimpleName();
     StringBuilder fileContent = new StringBuilder();
     fileContent.append("package ").append(targetPackage).append(";\n\n");
     fileContent.append("import ").append(Component.class.getName()).append(";\n");
     fileContent.append("import ").append(HtmlElementComponent.class.getName()).append(";\n");
     fileContent.append("import ").append(SnippetComponent.class.getName()).append(";\n");
     fileContent.append("import ").append(ChildListComponent.class.getName()).append(";\n");
     fileContent.append("import ").append(IOException.class.getName()).append(";\n");
     fileContent.append("import ").append(Writer.class.getName()).append(";\n");
     fileContent.append("import ").append(List.class.getName()).append(";\n");
     fileContent.append("import ").append(ArrayList.class.getName()).append(";\n");
     fileContent.append("\n");
     fileContent.append("public class ").append(className)
         .append(" extends ").append(HtmlElementComponent.class.getSimpleName())
         .append("\n");
     fileContent.append("{\n\n");
     fileContent.append("  public " + className + "(Component parent)");
     fileContent.append("  {\n");
     fileContent.append("    super(parent);\n");
     if (htmlElementCompont.renderChildren == false)
     {
       fileContent.append("    renderChildren = false;\n");
     }
     if (htmlElementCompont.renderSelf == false)
     {
       fileContent.append("    renderSelf = false;\n");
     }
     fileContent.append("  }\n");
     int componentCounter = 1;
     for (Component child : htmlElementCompont.children)
     {
       if (child instanceof SnippetListComponent)
       {
         SnippetListComponent snippetListChild = (SnippetListComponent) child;
         for (SnippetListComponent.ComponentPart part : snippetListChild.parts)
         {
           String componentField = getComponentFieldName(part.component, componentCounter);
           if (part.component != null)
           {
             generateFieldOrVariableFromComponent(part.component, targetPackage, fileContent, "public ", componentField, 2);
           }
           else
           {
             fileContent.append("  public ").append(SnippetComponent.class.getSimpleName()).append(" ")
                     .append(componentField).append(" = new ").append(SnippetComponent.class.getSimpleName())
                     .append("(null, ").append(asConstant(part.htmlSnippet)).append(", this);\n\n");
           }
           componentCounter++;                      
         }
         continue;
       }
       String componentField = getComponentFieldName(child, componentCounter);
       generateFieldOrVariableFromComponent(child, targetPackage, fileContent, "public ", componentField, 2);
       componentCounter++;          
     }
     fileContent.append("  ").append("{\n");
     fileContent.append("    ").append("elementName = \"").append(htmlElementCompont.elementName)
         .append("\";\n");
     fileContent.append("    ").append("id = \"").append(removeLoopPart(htmlElementCompont.id)).append("\";\n");
     for (Map.Entry<String, String> attributeEnty : htmlElementCompont.attributes.entrySet())
     {
       fileContent.append("    ").append("attributes.put(\"").append(attributeEnty.getKey())
           .append("\", \"").append(attributeEnty.getValue()).append("\");\n");
     }
     fileContent.append("  ").append("}\n");
 
     fileContent.append("  @Override\n");
     fileContent.append("  public List<Component> getChildren()\n");
     fileContent.append("  {\n");
     fileContent.append("    List<Component> result = new ArrayList<>();\n");
     componentCounter = 1;
     for (Component child : htmlElementCompont.children)
     {
       if (child instanceof SnippetListComponent)
       {
         SnippetListComponent snippetListChild = (SnippetListComponent) child;
         for (SnippetListComponent.ComponentPart part : snippetListChild.parts)
         {
           String componentField = getComponentFieldName(part.component, componentCounter);
           fileContent.append("    result.add(").append(componentField).append(");\n");
           componentCounter++;
         }
         continue;
       }
       String componentField = getComponentFieldName(child, componentCounter);
       fileContent.append("    result.add(").append(componentField).append(");\n");
       componentCounter++;
     }
     fileContent.append("    return result;\n");
     fileContent.append("  }\n");
     generateConvenienceMethods(htmlElementCompont, fileContent);
     fileContent.append("}\n");
     return fileContent.toString();
   }
   
   protected void generateConvenienceMethods(
       HtmlElementComponent htmlElementComponent, 
       StringBuilder stringBuilder)
   {
     if (htmlElementComponent.children.size() == 1)
     {
       Component child = htmlElementComponent.children.get(0);
       String componentField = getComponentFieldName(child, 1);
       if (child instanceof SnippetListComponent)
       {
         SnippetListComponent snippetListComponent = (SnippetListComponent) child;
         if (snippetListComponent.parts.size() == 1)
         {
           SnippetListComponent.ComponentPart part = snippetListComponent.parts.get(0);
           if (part.htmlSnippet != null)
           {
             if (part.htmlSnippet.contains("<"))
             {
               stringBuilder.append("\n").append("  public String getInnerContent()\n")
                   .append("  {\n")
                   .append("    return ").append(componentField).append(".snippet;\n")
                   .append("  }\n");
               stringBuilder.append("\n").append("  public void setInnerContent(String innerContent)\n")
                   .append("  {\n")
                   .append("    ").append(componentField).append(".snippet = innerContent;\n")
                   .append("  }\n");
             }
             else
             {
               stringBuilder.append("\n").append("  public String getTextContent()\n")
                   .append("  {\n")
                   .append("    return ").append(componentField).append(".snippet;\n")
                   .append("  }\n");
               stringBuilder.append("\n").append("  public void setTextContent(String text)\n")
                   .append("  {\n")
                   .append("    ").append(componentField).append(".snippet = text;\n")
                   .append("  }\n");
             }
           }
         }
       }
     }
   }
   
   @Override
   public String generateExtension(
       Component component,
       String targetPackage)
   {
     String className = getClassName(component, targetPackage).getSimpleName();
     String extensionClassName = getExtensionClassName(component, targetPackage).getSimpleName();
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
     return fileContent.toString();
   }
 
   @Override
   public String generateInitializer(
       String componentField,
       Component component,
       String targetPackage,
       int indent)
   {
     return "";
     // TODO following code should be not necessary as initialisation is done in generation of class
 //    String indentString = getIndentString(indent);
 //    HtmlElementComponent htmlElementCompont = (HtmlElementComponent) component;
 //    StringBuilder result = new StringBuilder();
 //    result.append(indentString).append("{\n");
 //    result.append(indentString).append("  ").append(componentField)
 //        .append(".elementName = \"").append(htmlElementCompont.elementName).append("\";\n");
 //    result.append(indentString).append("  ").append(componentField)
 //        .append(".id = \"").append(htmlElementCompont.id).append("\";\n");
 //    for (Map.Entry<String, String> attributeEnty : htmlElementCompont.attributes.entrySet())
 //    {
 //      result.append(indentString).append("  ").append(componentField)
 //          .append(".attributes.put(\"").append(attributeEnty.getKey())
 //          .append("\", \"").append(attributeEnty.getValue()).append("\");\n");
 //    }
 //    generateInitChildren(component, targetPackage, result, componentField, indent + 2);
 //    result.append(indentString).append("}\n");
 //    return result.toString();
   }
 }
