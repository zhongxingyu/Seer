 package org.codehaus.xfire.type.java5;
 
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.aegis.MessageReader;
 import org.codehaus.xfire.aegis.MessageWriter;
 import org.codehaus.xfire.aegis.type.Type;
 import org.codehaus.xfire.fault.XFireFault;
 import org.codehaus.xfire.soap.SoapConstants;
 import org.codehaus.yom.Attribute;
 import org.codehaus.yom.Element;
 
 public class EnumType
     extends Type
 {
     @Override
     public Object readObject(MessageReader reader, MessageContext context)
         throws XFireFault
     {
         String value = reader.getValue();
 
         return Enum.valueOf(getTypeClass(), value);
     }
 
     @Override
     public void writeObject(Object object, MessageWriter writer, MessageContext context)
         throws XFireFault
     {
         writer.writeValue(((Enum) object).toString());
     }
 
     @Override
     public void setTypeClass(Class typeClass)
     {
         if (!typeClass.isEnum())
         {
             throw new XFireRuntimeException("Type class must be an enum.");
         }
         
         super.setTypeClass(typeClass);
     }
 
     @Override
     public void writeSchema(Element root)
     {
         Element simple = new Element(SoapConstants.XSD_PREFIX + ":simpleType",
                                       SoapConstants.XSD);
         simple.addAttribute(new Attribute("name", getSchemaType().getLocalPart()));
         root.appendChild(simple);
         
         Element restriction = new Element(SoapConstants.XSD_PREFIX + ":restriction",
                                      SoapConstants.XSD);
        restriction.addAttribute(new Attribute("base", SoapConstants.XSD_PREFIX + ":string"));
         simple.appendChild(restriction);
         
         Object[] constants = getTypeClass().getEnumConstants();
 
         for (Object constant : constants)
         {
             Element enumeration = new Element(SoapConstants.XSD_PREFIX + ":enumeration",
                                           SoapConstants.XSD);
            enumeration.addAttribute(new Attribute("value", ((Enum) constant).toString()));
             restriction.appendChild(enumeration);
         }
     }
 
     @Override
     public boolean isComplex()
     {
         return true;
     }
 }
