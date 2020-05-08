 package org.codehaus.xfire.type.java5;
 
 import java.beans.PropertyDescriptor;
 
 import javax.xml.namespace.QName;
 
 import org.codehaus.xfire.aegis.type.basic.TypeInfo;
 import org.codehaus.xfire.util.NamespaceHelper;
 
 public class AnnotatedTypeInfo
     extends TypeInfo
 {
     public AnnotatedTypeInfo(Class typeClass)
     {
         super(typeClass);
         
         initialize();
     }
 
     protected boolean isAttribute(PropertyDescriptor desc)
     {
         return desc.getReadMethod().isAnnotationPresent(XmlAttribute.class);
     }
 
     protected boolean isElement(PropertyDescriptor desc)
     {
         return desc.getReadMethod().isAnnotationPresent(XmlElement.class);
     }
 
    protected boolean isAnnotatedElement(PropertyDescriptor desc)
    {
        return desc.getReadMethod().isAnnotationPresent(XmlElement.class);
    }
    
     protected QName createQName(PropertyDescriptor desc)
     {
         String name = null;
         String ns = null;
         
         if (isAttribute(desc))
         {
             XmlAttribute att = desc.getReadMethod().getAnnotation(XmlAttribute.class);
             name = att.name();
             ns = att.namespace();
         }
         else if (isElement(desc))
         {
             XmlElement att = desc.getReadMethod().getAnnotation(XmlElement.class);
             name = att.name();
             ns = att.namespace();
         }
         
         if (name == null || name.length() == 0)
             name = desc.getName();
         
         if (ns == null || ns.length() == 0)
             ns = NamespaceHelper.makeNamespaceFromClassName( getTypeClass().getName(), "http");
         
         return new QName(ns, name);
     }
 
     public boolean isNillable(QName name)
     {
         return super.isNillable(name);
     }
 }
