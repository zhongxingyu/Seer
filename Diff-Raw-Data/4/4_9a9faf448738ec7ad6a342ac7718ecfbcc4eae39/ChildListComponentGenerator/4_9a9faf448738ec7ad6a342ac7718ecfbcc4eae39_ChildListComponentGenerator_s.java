 package com.seitenbau.sicgwaf.generator;
 
import java.util.HashMap;
 import java.util.Map;
 
 import com.seitenbau.sicgwaf.component.ChildListComponent;
 import com.seitenbau.sicgwaf.component.Component;
 
 public class ChildListComponentGenerator extends ComponentGenerator
 {
   public String getClassName(
       String componentName,
       Component component,
       String targetPackage)
   {
     Component child = component.getChildren().get(0);
     ComponentGenerator delegate = Generator.getGenerator(child);
     return "ChildListComponent<" + delegate.getClassName(child.id, child, targetPackage) + ">";
   }
   
   public String getExtensionClassName(
       String componentName,
       Component component,
       String targetPackage)
   {
     Component child = component.getChildren().get(0);
     ComponentGenerator delegate = Generator.getGenerator(child);
     String delegateClassName = delegate.getExtensionClassName(child.id, child, targetPackage);
     if (delegateClassName == null)
     {
       return null;
     }
     return "ChildListComponent<" + delegateClassName + ">";
   }
   
   public void generate(
         String componentName,
         Component component,
         String targetPackage,
         Map<String, String> filesToWrite)
   {
     throw new UnsupportedOperationException();
   }
   
   public void generateExtension(
         String componentName,
         Component component,
         String targetPackage,
         Map<String, String> filesToWrite)
   {
     Component child = component.getChildren().get(0);
     ComponentGenerator delegate = Generator.getGenerator(child);
     delegate.generateExtension(child.id, component, targetPackage, filesToWrite);
   }
 
   public String generateNewComponent(
       String componentName,
       Component component,
       String targetPackage)
   {
     return null;
   }
 
   public String generateInitializer(
       String componentField,
       Component component,
       String targetPackage,
       int indent,
       Map<String, String> filesToWrite)
   {
     ChildListComponent<?> childListComponent = (ChildListComponent<?>) component;
     String indentString = getIndentString(indent);
     StringBuilder result = new StringBuilder();
     result.append(indentString).append("{\n");
     for (int i = 0; i < childListComponent.children.size(); ++i)
     {
       Component child = childListComponent.children.get(i);
       ComponentGenerator delegate = Generator.getGenerator(child);
      generateFieldFromComponent(child, targetPackage, result, "", componentField + i, 2, filesToWrite);
       // hacky, also creates sub classes
       result.append(delegate.generateInitializer(componentField + i, child, targetPackage, indent + 2, filesToWrite));
       result.append(indentString).append("  ").append(componentField).append(".children.add(")
          .append(componentField).append(i).append(");\n");
       // hacky, also creates sub classes
       delegate.generateExtension(child.id, child, targetPackage, filesToWrite);
     }
     result.append(indentString).append("}\n");
     return result.toString();
   }
 
 }
