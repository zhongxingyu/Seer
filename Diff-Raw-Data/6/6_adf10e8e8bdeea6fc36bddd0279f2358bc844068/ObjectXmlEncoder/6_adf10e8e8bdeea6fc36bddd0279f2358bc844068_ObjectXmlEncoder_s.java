 package org.otherobjects.cms.io;
 
 import java.beans.PropertyEditor;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.lang.reflect.InvocationTargetException;
 import java.util.List;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.XMLWriter;
 import org.otherobjects.cms.OtherObjectsException;
 import org.otherobjects.cms.model.BaseNode;
 import org.otherobjects.cms.model.Editable;
 import org.otherobjects.cms.tools.BeanTool;
 import org.otherobjects.cms.types.PropertyDef;
 import org.otherobjects.cms.types.PropertyDefImpl;
 import org.otherobjects.cms.types.TypeDef;
 import org.otherobjects.cms.types.annotation.PropertyType;
 
 public class ObjectXmlEncoder
 {
     public Document encode(Object item, TypeDef typeDef)
     {
         Document document = DocumentHelper.createDocument();
         Element objectsElement = document.addElement("objects");
         Element element = objectsElement.addElement("object");
         addObject(element, item, typeDef);
         try
         {
             StringWriter sw = new StringWriter();
             OutputFormat outformat = OutputFormat.createPrettyPrint();
             outformat.setEncoding("UTF-8");
             XMLWriter writer = new XMLWriter(sw, outformat);
             writer.write(document);
             writer.flush();
             System.out.println(sw);
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
         return document;
     }
 
     public void addObject(Element element, Object item, TypeDef typeDef)
     {
         try
         {
             element.addAttribute("id", ((Editable) item).getEditableId());
             element.addAttribute("type", typeDef.getName());
             if(item instanceof BaseNode)
                 element.addAttribute("path", ((BaseNode)item).getJcrPath());
             for (PropertyDef prop : typeDef.getProperties())
             {
                 addProperty(element, item, (PropertyDefImpl) prop);
             }
         }
         catch (Exception e)
         {
             throw new OtherObjectsException("Error encoding object.", e);
         }
     }
 
     private void addProperty(Element element, Object item, PropertyDefImpl prop) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
     {
 
         if (prop.getType().equals(PropertyType.LIST.value()))
         {
             addListProperty(element, (PropertyDefImpl) prop, item);
         }
         else if (prop.getType().equals(PropertyType.REFERENCE.value()))
         {
             addReferenceProperty(element, (PropertyDefImpl) prop, item);
         }
         else if (prop.getType().equals(PropertyType.COMPONENT.value()))
         {
             addComponentProperty(element, (PropertyDefImpl) prop, item);
         }
         else
         {
             addSimpleProperty(element, (PropertyDefImpl) prop, item);
         }
     }
 
     @SuppressWarnings("unchecked")
     private void addListProperty(Element element, PropertyDefImpl property, Object item) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
     {
         String name = property.getName();
         String path = property.getPropertyPath();
         List<Object> list = (List<Object>) BeanTool.getPropertyValue(item, path);
         Element prop = element.addElement("list");
         if (list == null)
         {
             // Handle nulls
             prop.addAttribute("name", name);
             prop.addAttribute("null", "true");
         }
         else
         {
             prop.addAttribute("name", name);
             
             for (int counter = 0; counter<list.size(); counter++)
             {
                 PropertyDefImpl pd = new PropertyDefImpl();
                 pd.setName(property.getName() + "[" + counter + "]");
                 pd.setType(property.getCollectionElementType());
                 pd.setParentTypeDef(property.getParentTypeDef());
                 pd.setRelatedType(property.getRelatedType());
                 addProperty(prop, item, pd);
             }
         }
     }
 
     private void addComponentProperty(Element element, PropertyDefImpl property, Object item) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
     {
         String name = property.getName();
         String path = property.getPropertyPath();
         Object value = BeanTool.getPropertyValue(item, path);
 
         Element prop = element.addElement("property");
         if (value == null)
         {
             // Handle nulls
             prop.addAttribute("name", name);
             prop.addAttribute("null", "true");
         }
         else
         {
             prop.addAttribute("name", name);
             Element el = prop.addElement("object");
             addObject(el, value, property.getRelatedTypeDef());
         }
         // TODO Auto-generated method stub
 
     }
 
     private void addReferenceProperty(Element element, PropertyDefImpl property, Object item) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
     {
         String name = property.getName();
         String path = property.getPropertyPath();
         Object value = BeanTool.getPropertyValue(item, path);
 
         Element prop = element.addElement("property");
         if (value == null)
         {
             // Handle nulls
             prop.addAttribute("name", name);
             prop.addAttribute("null", "true");
         }
         else
         {
             prop.addAttribute("name", name);
             prop.addAttribute("ref", ((Editable) value).getEditableId());
         }
     }
 
     private void addSimpleProperty(Element root, PropertyDefImpl property, Object object) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
     {
         String name = property.getName();
         String path = property.getPropertyPath();
         Object value = BeanTool.getPropertyValue(object, path);
 
         Element prop = root.addElement("property");
         if (value == null)
         {
             // Handle nulls
             prop.addAttribute("name", name);
             prop.addAttribute("null", "true");
         }
         else
         {
             PropertyEditor propertyEditor = property.getPropertyEditor();
             propertyEditor.setValue(value);
             prop.addAttribute("name", name);
             prop.addAttribute("value", propertyEditor.getAsText());
         }
     }
 }
