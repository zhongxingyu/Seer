 package net.dovemq.gen;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jdom.DataConversionException;
 import org.jdom.Element;
 
 abstract class AMQPCompositeType implements AMQPType
 {
     public enum CompoundMetaType {DEFINITION, FRAME_BODIES};
     static CompoundMetaType
     getMetaType(String topLevelElementName)
     {
         if (topLevelElementName.equalsIgnoreCase("frame-bodies"))
         {
             return CompoundMetaType.FRAME_BODIES;
         }
         else
         {
             return CompoundMetaType.DEFINITION;
         }        
     }
 
     AMQPCompositeType(Element xmlElement, CompoundMetaType metaType) throws DataConversionException
     {
         super();
         this.name = xmlElement.getAttributeValue("name");
         this.label = xmlElement.getAttributeValue("label");
         this.provides = xmlElement.getAttributeValue("provides");
         this.source = xmlElement.getAttributeValue("source");
 
         @SuppressWarnings("unchecked")
         List<Element> children = (List<Element>) xmlElement.getChildren();
         Element descriptorElement = null;
         fields = new ArrayList<AMQPCompositeFieldType>();
         for (Element c : children)
         {
             if (c.getName().equalsIgnoreCase("field"))
             {
                 AMQPCompositeFieldType field = new AMQPCompositeFieldType(c);
                 fields.add(field);
             }
             if (c.getName().equalsIgnoreCase("descriptor"))
             {
                 descriptorElement = c;
             }
         }
         this.descriptor = descriptorElement.getAttributeValue("name");
         this.code = descriptorElement.getAttributeValue("code");
         String metaTypeString = "";
         if( metaType == CompoundMetaType.FRAME_BODIES)
         {
             metaTypeString = "Control";
         }
         else
         {
             metaTypeString = "Definition";            
         }
         this.type = String.format("CAMQP%s%s", metaTypeString, Utils.convertToCanonicalName(name));
     }
 
     @Override
 	public void display()
     {
         System.out.println("-----------------------------------------");
         System.out.println("name: " + name);
         System.out.println("type: " + type);        
         System.out.println("label: " + label);
         System.out.println("descriptor: " + descriptor);
         System.out.println("code: " + code);
         for (AMQPCompositeFieldType field : fields)
         {
             System.out.println();
             field.display();
         }
     }    
 
     @Override
 	public final String getName()
     {
         return name;
     }
     
     final String getType()
     {
         return type;
     }
     
     static boolean isCompositeTypeList(Element xmlElement)
     {
         String source = xmlElement.getAttributeValue("source");
         return source.equalsIgnoreCase("list");
     }    
     
     void generateClassFile(String dir) throws IOException
     {
         outputStream.println(String.format("package %s;", AMQPXMLReader.PACKAGE_NAME));
         outputStream.println();
         if (hasMultiple())
         {
             outputStream.println("import java.util.ArrayList;");
             outputStream.println("import java.util.Collection;");
         }
         if (hasSpecifiedType("map"))
         {
             outputStream.println("import java.util.Map;");
             outputStream.println("import java.util.HashMap;");
             outputStream.println("import java.util.Set;");
             outputStream.println("import java.util.Map.Entry;");
         }
         if (hasSpecifiedType("ulong"))
         {
             outputStream.println("import java.math.BigInteger;");
         }
         if (hasSpecifiedType("timestamp"))
         {
             outputStream.println("import java.util.Date;");
         }
         if (hasSpecifiedType("uuid"))
         {
             outputStream.println("import java.util.UUID;");
         }
         if (hasSpecifiedType("binary"))
         {
             outputStream.println("import org.jboss.netty.buffer.ChannelBuffer;");
             outputStream.println();
         }
        outputStream.println("import com.cisco.camqp2.transport.protocol.*;");
         outputStream.println();
 
         outputStream.println("public class " + type);
         outputStream.println("{");
         outputStream.println(Utils.insertTabs(1, String.format("public static final String descriptor = \"%s\";", descriptor)));
         outputStream.println();
 
         generateProviderList();
         generateFieldGettersSetters();
     }
     
     private void
     generateProviderList()
     {
         for (AMQPCompositeFieldType field : fields)
         {
             field.loadProviders();
         }
     }
     private void
     generateFieldGettersSetters()
     {
         for (AMQPCompositeFieldType field : fields)
         {
             String typeName = getJavaTypeName(field.getType());
             String canonicalFieldName = Utils.convertToCanonicalName(field.getName());
             String fieldName = Utils.lowerCapFirstLetter(canonicalFieldName);
             
             if (!field.isRequired())
             {
                 outputStream.println(Utils.insertTabs(1, String.format("private boolean isSet%s = false;", canonicalFieldName)));
                 
                 outputStream.println(Utils.insertTabs(1, String.format("public void setRequired%s(boolean val)", canonicalFieldName)));
                 outputStream.println(Utils.insertTabs(1, "{"));
                 outputStream.println(Utils.insertTabs(2, String.format("isSet%s = val;", canonicalFieldName)));                
                 outputStream.println(Utils.insertTabs(1, "}"));    
                 
                 outputStream.println(Utils.insertTabs(1, String.format("public boolean isSet%s()", canonicalFieldName)));
                 outputStream.println(Utils.insertTabs(1, "{"));
                 outputStream.println(Utils.insertTabs(2, String.format("return isSet%s;", canonicalFieldName)));                
                 outputStream.println(Utils.insertTabs(1, "}"));                           
             }
 
             String typeDeclaration;
             if (AMQPXMLReader.gDefinitionsCompositeTypes.contains(field.getType()))
             {
                 typeDeclaration = String.format("private %s %s = null;", typeName, fieldName);
             }
             else if (isSpecifiedType(field, "map"))
             {
                 typeDeclaration = String.format("private final %s %s = new HashMap<String, String>();", typeName, fieldName);                
             }
             else if (isSpecifiedType(field, "binary"))
             {
                 typeDeclaration = String.format("private %s %s = null;", typeName, fieldName);
             }
             else if (AMQPXMLReader.gRestrictedTypes.contains(field.getType()))
             {
                 String primitiveTypeName = getPrimitiveType(field);
                 typeDeclaration = String.format("private %s %s = %s;", typeName, fieldName, AMQPPrimitiveTypeMappings.getDefaultValue(primitiveTypeName));                
             }
             else
             {
                 if (field.getDefaultValue() == null)
                     typeDeclaration = String.format("private %s %s = %s;", typeName, fieldName, AMQPPrimitiveTypeMappings.getDefaultValue(field.getType()));
                 else
                 {
                     if (typeName.equalsIgnoreCase("BigInteger"))
                         typeDeclaration = String.format("private %s %s = %s;", typeName, fieldName, AMQPPrimitiveTypeMappings.getDefaultValue(field.getType()));
                     else if (typeName.equalsIgnoreCase("Long"))
                         typeDeclaration = String.format("private %s %s = %sL;", typeName, fieldName, field.getDefaultValue());
                     else
                         typeDeclaration = String.format("private %s %s = %s;", typeName, fieldName, field.getDefaultValue());
                 }
             }
             
             if (field.isMultiple())
             {
                 typeDeclaration = String.format("private Collection<%s> %s = new ArrayList<%s>();", typeName, fieldName, typeName);                
             }
             outputStream.println(Utils.insertTabs(1, typeDeclaration));
             
             if (!isSpecifiedType(field, "map"))
             {
                 outputStream.println(Utils.insertTabs(1, String.format("public void %s%s(%s val)", field.isMultiple()? "add" : "set", canonicalFieldName, typeName)));
                 outputStream.println(Utils.insertTabs(1, "{"));
                 
                 if (!field.isRequired())
                 {
                     outputStream.println(Utils.insertTabs(2, String.format("isSet%s = true;", canonicalFieldName)));
                 }
                 
                 if (field.isMultiple())
                 {
                     outputStream.println(Utils.insertTabs(2, String.format("%s.add(val);", fieldName)));
                 }
                 else
                 {
                     outputStream.println(Utils.insertTabs(2, String.format("%s = val;", fieldName)));                    
                 }            
                 outputStream.println(Utils.insertTabs(1, "}"));
                 outputStream.println();                
             }
 
             if (field.isMultiple())
             {
                 outputStream.println(Utils.insertTabs(1, String.format("public Collection<%s> get%s()", typeName, canonicalFieldName)));                
             }
             else
             {
                 outputStream.println(Utils.insertTabs(1, String.format("public %s get%s()", typeName, canonicalFieldName)));
             }
             outputStream.println(Utils.insertTabs(1, "{"));
             outputStream.println(Utils.insertTabs(2, String.format("return %s;", fieldName)));            
             outputStream.println(Utils.insertTabs(1, "}"));
             outputStream.println();
         }
     }
 
     protected abstract void
     generateEncoderSignature();
     
     protected abstract void
     generateEncoder();
     
     protected abstract void
     generateDecoderSignature();
     
     protected abstract void
     generateDecoder();   
 
     static String
     getJavaTypeName(String typeName)
     {
         if (typeName.equalsIgnoreCase("Object")) // REVISIT TODO tejdas
         {
             return typeName;
         }
         else if (AMQPXMLReader.gPrimitiveTypes.contains(typeName))
         {
             AMQPPrimitiveType primType = (AMQPPrimitiveType) AMQPXMLReader.gPrimitiveTypes.getType(typeName);
             return primType.getType();
         }
         else if (AMQPXMLReader.gRestrictedTypes.contains(typeName))
         {
             AMQPRestrictedType restrictedType = (AMQPRestrictedType) AMQPXMLReader.gRestrictedTypes.getType(typeName);
             return AMQPPrimitiveTypeMappings.getJavaType(restrictedType.getType());
         }
         else if (AMQPXMLReader.gDefinitionsCompositeTypes.contains(typeName))
         {
             AMQPCompositeType compoundType = (AMQPCompositeType) AMQPXMLReader.gDefinitionsCompositeTypes.getType(typeName);
             return compoundType.getType();
         }
         System.out.println("REVISIT TODO tejdas: returning null for getJavaTypeName() for typeName: " + typeName);
         return "String";
     }    
     
     private static Boolean
     isSpecifiedType(AMQPCompositeFieldType field, String specifiedType)
     {
         String typeName = field.getType();
         if (typeName.equalsIgnoreCase(specifiedType))
         {
             return true;
         }
         if (AMQPXMLReader.gRestrictedTypes.contains(typeName))
         {
             AMQPRestrictedType restrictedType = (AMQPRestrictedType) AMQPXMLReader.gRestrictedTypes.getType(typeName);
             return restrictedType.getType().equalsIgnoreCase(specifiedType);
         }
         return false;
     }    
     
     private static String
     getPrimitiveType(AMQPCompositeFieldType field)
     {
         String typeName = field.getType();
         if (AMQPXMLReader.gRestrictedTypes.contains(typeName))
         {
             AMQPRestrictedType restrictedType = (AMQPRestrictedType) AMQPXMLReader.gRestrictedTypes.getType(typeName);
             return restrictedType.getType();
         }
         System.out.println("returning null for " + typeName);
         return null;
     }    
     
     private Boolean
     hasSpecifiedType(String specifiedType)
     {
         for (AMQPCompositeFieldType field : fields)
         {
             if (isSpecifiedType(field, specifiedType))
             {
                 return true;
             }
         }
         return false;
     }
     
     private Boolean
     hasMultiple()
     {
         for (AMQPCompositeFieldType field : fields)
         {
             if (field.isMultiple())
             {
                 return true;
             }
         }
         return false;
     }    
 
     private final String name;
     
     private final String source;
     
     @Override
 	public String getSource()
     {
         return source;
     }
 
     private final String provides;    
     
     @Override
 	public String getProvides()
     {
         return provides;
     }
 
     protected final String type;    
 
     private final String label;
 
     private final String descriptor;
 
     private final String code;
     
     protected final List<AMQPCompositeFieldType> fields;
     
     protected PrintWriter outputStream = null;
 }
