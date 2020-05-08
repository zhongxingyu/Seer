 package com.seitenbau.micgwaf.generator;
 
 import java.util.List;
 
 import com.seitenbau.micgwaf.component.Component;
 
 public abstract class ComponentGenerator
 {
   /**
    * Returns the class name of the component which will represent the parsed component
    * in the generated code. This can either be a generated component, or a component
    * supplied in some Library.
    * Also there may be an extension class containing user modifications to the generating class,
    * in which case the extension class should be used in references instead of this class.
    * 
    * @param component the component to be represented, not null.
    * @param targetPackage the base target package for the generated classes, not null.
    * 
    * @return the class name, not null.
    */
   public abstract JavaClassName getClassName(
       Component component,
       String targetPackage);
 
   /**
    * Returns the class name of the  extension class containing user modifications to the generating class.
    * 
    * @param component the component to be represented, not null.
    * @param targetPackage the base target package for the generated classes, not null.
    * 
    * @return the class name of the extension class. 
    *         May return null if generateExtensionClass() returns null for the component.
    */
   public JavaClassName getExtensionClassName(
       Component component,
       String targetPackage)
   {
     String className = getClassName(component, targetPackage).getSimpleName();
     return new JavaClassName(className + "Extension", targetPackage);
   }
   
   /**
    * Returns whether an extension class should be generated for the component.
    * 
    * @param component the component to check, not null.
    * 
    * @return true if an extension class should be generated, false otherwise.
    */
   public abstract boolean generateExtensionClass(Component component);
   
   /**
    * Returns the class name of the class by which this component can be referenced in generated code.
    * 
    * @param component the component to be represented, not null.
    * @param targetPackage the base target package for the generated classes, not null.
    * 
    * @return the class name of the extension class. 
    *         May return null if generateExtensionClass() returns null for the component.
    */
   public JavaClassName getReferencableClassName(
       Component component,
       String targetPackage)
   {
     if (generateExtensionClass(component))
     {
       return getExtensionClassName(component, targetPackage);
     }
     return getClassName(component, targetPackage);
   }
 
   /**
    * Generates the component class representing the component.
    * 
    * @param component the component for which code should be generated, not null.
    * @param targetPackage the base target package for the generated classes, not null.
    * 
    * @return the code for the generated class, or null if no class should be generated.
    */
   public abstract String generate(
       Component component,
       String targetPackage);
   
   /**
    * Generates the component extension class representing the component.
    * The extension class contains user-specific extensions to the generated class.
    * 
    * @param component the component for which code should be generated, not null.
    * @param targetPackage the base target package for the generated classes, not null.
    * 
    * @return the code for the generated extension class, or null if no extension class should be generated.
    */
   public abstract String generateExtension(
       Component component,
       String targetPackage);
 
   public abstract String generateInitializer(
       String componentField,
       Component component,
       String targetPackage,
       int indent);
   
  public JavaClassName toJavaClassName(String componentId, String packageName)
   {
    String normalizedId = removeLoopPartFromId(componentId);
    String simpleName = normalizedId.substring(0, 1).toUpperCase()
        + normalizedId.substring(1);
     return new JavaClassName(simpleName, packageName);
   }
   
   public String asConstant(String string)
   {
     String result = string.replace("\\", "\\\\");
     result = result.replace("\r", "\\r");
     result = result.replace("\n", "\\n");
     result = result.replace("\"", "\\\"");
     return "\"" + result + "\"";
   }
 
   public void generateFieldOrVariableFromComponent(
       Component component,
       String targetPackage, 
       StringBuilder result,
       String modifier,
       String fieldName,
       int indent)
   {
     String indentString = getIndentString(indent);
     ComponentGenerator generator = Generator.getGenerator(component);
     JavaClassName componentClassName = generator.getReferencableClassName(component, targetPackage);
     result.append(indentString).append(modifier).append(componentClassName.getSimpleName())
         .append(" ").append(fieldName)
         .append(" = new ").append(componentClassName.getSimpleName()).append("(this);\n\n");
     result.append(generator.generateInitializer(fieldName, component, targetPackage, indent));
   }
 
 
   public void generateInitChildren(
       Component component, 
       String targetPackage,
       StringBuilder result,
       String componentField,
       int indent)
   {
     List<? extends Component> children = component.getChildren();
     if (children.isEmpty())
     {
       return;
     }
     String indentString = getIndentString(indent);
     result.append(indentString).append("{\n");
     int counter = 1;
     for (Component child : children)
     {
       String fieldName;
       if (child.id != null)
       {
        fieldName = removeLoopPartFromId(child.id);
       }
       else
       {
         fieldName = getChildName(componentField, counter);
       }
       generateFieldOrVariableFromComponent(child, targetPackage, result, "", fieldName, indent + 2);
       result.append(indentString).append("  ").append(componentField).append(".children.add(")
           .append(fieldName).append(");\n");
 
       counter++;
     }
     result.append(indentString).append("}\n");
   }
   
  public String removeLoopPartFromId(String id)
  {
    if (id == null)
    {
      return null;
    }
    int indexOfColon = id.indexOf(':');
    if (indexOfColon == -1)
    {
      return id;
    }
    return id.substring(0, indexOfColon);
  }
  
   public String getIndentString(int indent)
   {
     StringBuilder result = new StringBuilder(indent);
     for (int i = 0; i < indent; ++i)
     {
       result.append(" ");
     }
     return result.toString();
   }
   
   public String getChildName(String baseName, int counter)
   {
     if (baseName == null || "".equals(baseName))
     {
       return "child" + counter;
     }
     return baseName + "Child" + counter;
   }
   
 
   public String getComponentFieldName(Component component, int componentCounter)
   {
     String componentField;
     if (component != null && component.id != null)
     {
       componentField = component.id;
     }
     else
     {
       componentField= "component" + componentCounter;
     }
     return componentField;
   }
 
 }
