 package com.seitenbau.micgwaf.parser.contenthandler;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 
 import com.seitenbau.micgwaf.component.ChildListComponent;
 import com.seitenbau.micgwaf.component.Component;
 import com.seitenbau.micgwaf.component.FormComponent;
 import com.seitenbau.micgwaf.component.HtmlElementComponent;
 import com.seitenbau.micgwaf.component.InputComponent;
 import com.seitenbau.micgwaf.util.Constants;
 
 public class HtmlElementContentHandler extends ContentHandler
 {
   public static final String ID_ATTR = "id";
   
   public static final Set<String> inputElements = new HashSet<>();
   
   public static final Set<String> formElements = new HashSet<>();
 
   static
   {
     inputElements.add("input");
     inputElements.add("button");
     formElements.add("form");
   }
   
   public String elementName;
 
   public String id;
   
   public boolean multiple = false;
   
   public Map<String, String> attributeValues = new LinkedHashMap<>();
   
   public List<Component> children = new ArrayList<>();
   
   public boolean rendered = true;
 
   @Override
   public void startElement(
         String uri,
         String localName,
         String qName, 
         Attributes attributes) 
       throws SAXException 
   {
     elementName = localName;
     for (int i = 0; i < attributes.getLength(); ++i)
     {
       String attributeUri = attributes.getURI(i);
       String attributeName = attributes.getLocalName(i);
       if ("".equals(attributeName)) 
       {
         attributeName = attributes.getQName(i);
       }
       String value = attributes.getValue(i);
       if (attributeName.startsWith("xmlns:") && Constants.XML_NAMESPACE.equals(value))
       {
         // do not output definition of our own namespace
         continue;
       }
       if (Constants.XML_NAMESPACE.equals(attributeUri) && ID_ATTR.equals(attributeName))
       {
         id = value;
       }
       else if (Constants.XML_NAMESPACE.equals(attributeUri) 
           && ContentHandlerRegistry.MULTIPLE_ATTR.equals(attributeName))
       {
         multiple = true;
       }
       else if (Constants.XML_NAMESPACE.equals(attributeUri) 
           && ContentHandlerRegistry.DEFAULT_RENDERED_ATTR.equals(attributeName))
       {
         rendered = Boolean.parseBoolean(value);
       }
       else if (attributeUri == null || "".equals(attributeUri))
       {
         attributeValues.put(attributeName, value);
       }
     }
     if (id == null || "".equals(id.trim()))
     {
       throw new SAXException("Attribute " + Constants.XML_NAMESPACE + ":"+ HtmlElementContentHandler.ID_ATTR 
           + " is required on element " + qName);
     }
   }
   
   
   @Override
   public void child(Component child)
   {
     children.add(child);
   }
 
   @Override
   public Component finished() throws SAXException
   {
     HtmlElementComponent htmlElementComponent;
     if (inputElements.contains(elementName))
     {
       htmlElementComponent = new InputComponent(elementName, id, null);
     }
     else if (formElements.contains(elementName))
     {
       htmlElementComponent = new FormComponent(elementName, id, null);
     }
     else
     {
       htmlElementComponent = new HtmlElementComponent(elementName, id, null);
     }
     htmlElementComponent.attributes.putAll(attributeValues);
     htmlElementComponent.children.addAll(children);
     if (inputElements.contains(elementName) && attributeValues.get("name") == null)
     {
       htmlElementComponent.attributes.put("name", id);
     }
     htmlElementComponent.setRender(rendered);
     if (multiple)
     {
       Component result = new ChildListComponent<HtmlElementComponent>(
           null,
           null, 
           htmlElementComponent);
       htmlElementComponent.parent = result;
       return result;
     }
     return htmlElementComponent;
   }
 }
