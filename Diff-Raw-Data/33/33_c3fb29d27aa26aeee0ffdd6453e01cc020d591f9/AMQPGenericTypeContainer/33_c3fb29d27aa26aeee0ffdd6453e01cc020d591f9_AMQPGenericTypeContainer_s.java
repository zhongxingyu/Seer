 package net.dovemq.gen;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.jdom.DataConversionException;
 
 class AMQPGenericTypeContainer
 {
     AMQPGenericTypeContainer()
     {
         super();
         amqpTypes = new HashMap<String, AMQPType>();
     }
 
     void
     display()
     {
         Collection<AMQPType> vals = amqpTypes.values();
         for (AMQPType val : vals)
         {
             System.out.println("-------------------------------------");
             val.display();
             System.out.println();
         }
     }
     
     boolean
     contains(String name)
     {
         return (null != amqpTypes.get(name));
     }
 
     String
     getProviderSourceType(String provides)
     {
         Collection<AMQPType> items = amqpTypes.values();
         for (AMQPType item : items)
         {
             if ((item.getProvides() != null) &&
                 (item.getProvides().equalsIgnoreCase(provides)))
             {
                 // REVISIT TODO tejdas
                 return item.getName();
                 //return item.getSource();
             }
         }
         System.out.println("returning null for: " + provides);
         return null;
     }
     
     void
     loadAllProvidersFor(String consumer, Collection<String> providerList)
     {
         Collection<AMQPType> items = amqpTypes.values();
         for (AMQPType item : items)
         {
             if ((item.getProvides() != null) &&
                 (item.getProvides().equalsIgnoreCase(consumer)))
             {
                 if (!providerList.contains(item.getName()))
                 {
                     providerList.add(item.getName());  
                 }
             }
         }
     }    
     
     AMQPType
     getType(String name)
     {
         return amqpTypes.get(name);
     }
     
     void
     putType(String typeName, AMQPType type)
     {
         amqpTypes.put(typeName, type);        
     }
 
     private final Map<String, AMQPType> amqpTypes;
     
     void
     generateConstants(String dir) throws IOException, DataConversionException
     {
         Collection<AMQPType> vals = amqpTypes.values();
         String fileName = String.format("%s/%s.java", dir, "CAMQPConstants");
         PrintWriter outputStream = new PrintWriter(new FileWriter(fileName, false));
         try
         {
             outputStream.println(String.format("package %s;", AMQPXMLReader.PACKAGE_NAME));
             outputStream.println();
             outputStream.println("public class CAMQPConstants");
             outputStream.println("{");
             for (AMQPType val : vals)
             {
                 AMQPRestrictedType restrictedVal = (AMQPRestrictedType) val;
                 restrictedVal.generateConstants(outputStream);
                 outputStream.println();
             }
             outputStream.println("}");                  
         }
         finally
         {
             outputStream.close();
         }
     }
     
     void
     generateFormatCodeConstants(String dir) throws IOException, DataConversionException
     {
         Collection<AMQPType> vals = amqpTypes.values();
         String fileName = String.format("%s/%s.java", dir, "CAMQPFormatCodes");
         PrintWriter outputStream = new PrintWriter(new FileWriter(fileName, false));
         try
         {
             outputStream.println(String.format("package %s;", AMQPXMLReader.PACKAGE_NAME));
             outputStream.println();
             outputStream.println("public class CAMQPFormatCodes");
             outputStream.println("{");            
             for (AMQPType val : vals)
             {
                 AMQPPrimitiveType primitiveVal = (AMQPPrimitiveType) val;
                 primitiveVal.generateFormatcodeConstants(outputStream);
             }
             outputStream.println("}");                  
         }
         finally
         {
             outputStream.close();
         }
     }
     
     void
     generateAmqpTypeEnums(String dir) throws IOException, DataConversionException
     {
         Collection<AMQPType> vals = amqpTypes.values();
         String fileName = String.format("%s/%s.java", dir, "CAMQPTypes");
         PrintWriter outputStream = new PrintWriter(new FileWriter(fileName, false));
         try
         {
             outputStream.println(String.format("package %s;", AMQPXMLReader.PACKAGE_NAME));
            outputStream.println("import com.cisco.camqp2.transport.protocol.Width;");
             outputStream.println();
             outputStream.println("public enum CAMQPTypes");
             outputStream.println("{");            
             for (AMQPType val : vals)
             {
                 AMQPPrimitiveType primitiveVal = (AMQPPrimitiveType) val;
                 primitiveVal.generateAmqpTypeEnums(outputStream);
             }
             outputStream.println(Utils.insertTabs(1, ";"));
             outputStream.println(Utils.insertTabs(1, "private final int formatCode;"));
             outputStream.println(Utils.insertTabs(1, "private final String typeName;"));      
             outputStream.println(Utils.insertTabs(1, "private final Width width;"));
             outputStream.println(Utils.insertTabs(1, "CAMQPTypes(int formatCode, String typeName, Width width)"));
             outputStream.println(Utils.insertTabs(1, "{"));
             outputStream.println(Utils.insertTabs(2, "this.formatCode = formatCode;"));
             outputStream.println(Utils.insertTabs(2, "this.typeName = typeName;"));
             outputStream.println(Utils.insertTabs(2, "this.width = width;"));
             outputStream.println(Utils.insertTabs(1, "}"));
             outputStream.println(Utils.insertTabs(1, "protected int formatCode()"));
             outputStream.println(Utils.insertTabs(1, "{"));
             outputStream.println(Utils.insertTabs(2, "return formatCode;"));
             outputStream.println(Utils.insertTabs(1, "}"));
             outputStream.println(Utils.insertTabs(1, "protected String typeName()"));
             outputStream.println(Utils.insertTabs(1, "{"));
             outputStream.println(Utils.insertTabs(2, "return typeName;"));
             outputStream.println(Utils.insertTabs(1, "}"));
             outputStream.println(Utils.insertTabs(1, "protected Width width()"));
             outputStream.println(Utils.insertTabs(1, "{"));
             outputStream.println(Utils.insertTabs(2, "return width;"));
             outputStream.println(Utils.insertTabs(1, "}"));
             
             outputStream.println("}");                  
         }
         finally
         {
             outputStream.close();
         }
     }
     
     void
     generateClassFile(String dir)
     {
         Collection<AMQPType> vals = amqpTypes.values();
         for (AMQPType val : vals)
         {
             try
             {
                 ((AMQPCompositeType) val).generateClassFile(dir);
             } catch (IOException e)
             {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }        
     }
     
     void
     generateClassFile(String dir, String typeName)
     {
         Collection<AMQPType> vals = amqpTypes.values();
         for (AMQPType val : vals)
         {
             if (val.getName().equalsIgnoreCase(typeName))
             {
                 try
                 {
                     ((AMQPCompositeType) val).generateClassFile(dir);
                 }
                 catch (IOException e)
                 {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
                 break;
             }
         }        
     }
 }
