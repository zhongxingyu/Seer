 package com.seitenbau.micgwaf.generator;
 
 import com.seitenbau.micgwaf.component.ChildListComponent;
 import com.seitenbau.micgwaf.component.Component;
 
 public class ChildListComponentGenerator extends ComponentGenerator
 {
   @Override
   public JavaClassName getClassName(
       Component component,
       String targetPackage)
   {
     Component child = component.getChildren().get(0);
     ComponentGenerator delegate = Generator.getGenerator(child);
     return new JavaClassName(
         "ChildListComponent<" + delegate.getClassName(child, targetPackage).getSimpleName() + ">",
         ChildListComponent.class.getPackage().getName());
   }
   
   @Override
   public JavaClassName getExtensionClassName(
       Component component,
       String targetPackage)
   {
     Component child = component.getChildren().get(0);
     ComponentGenerator delegate = Generator.getGenerator(child);
     String delegateClassName = delegate.getExtensionClassName(child, targetPackage).getSimpleName();
     if (delegateClassName == null)
     {
       return null;
     }
     return  new JavaClassName(
         "ChildListComponent<" + delegateClassName + ">",
         ChildListComponent.class.getPackage().getName()); 
   }
   
   @Override
  public JavaClassName getReferencableClassName(
      Component component,
      String targetPackage)
  {
    return getExtensionClassName(component, targetPackage);
  }
  
  @Override
   public String generate(
         Component component,
         String targetPackage)
   {
     return null;
   }
   
   @Override
   public String generateExtension(
         Component component,
         String targetPackage)
   {
     return null;
   }
 
   @Override
   public String generateInitializer(
       String componentField,
       Component component,
       String targetPackage,
       int indent)
   {
     ChildListComponent<?> childListComponent = (ChildListComponent<?>) component;
     String indentString = getIndentString(indent);
     StringBuilder result = new StringBuilder();
     result.append(indentString).append("{\n");
     for (int i = 0; i < childListComponent.children.size(); ++i)
     {
       Component child = childListComponent.children.get(i);
       ComponentGenerator delegate = Generator.getGenerator(child);
       generateFieldOrVariableFromComponent(child, targetPackage, result, "", componentField + i, 4);
       result.append(delegate.generateInitializer(componentField + i, child, targetPackage, indent + 2));
       result.append(indentString).append("  ").append(componentField).append(".children.add(")
          .append(componentField).append(i).append(");\n");
     }
     result.append(indentString).append("}\n");
     return result.toString();
   }
 
   @Override
   public boolean generateExtensionClass(Component component)
   {
     return false;
   }
 
 }
