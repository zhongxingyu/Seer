 package org.codehaus.xfire.aegis;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.namespace.QName;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.aegis.stax.ElementReader;
 import org.codehaus.xfire.aegis.stax.ElementWriter;
 import org.codehaus.xfire.aegis.type.DefaultTypeMappingRegistry;
 import org.codehaus.xfire.aegis.type.Type;
 import org.codehaus.xfire.aegis.type.TypeMapping;
 import org.codehaus.xfire.aegis.type.TypeMappingRegistry;
 import org.codehaus.xfire.aegis.type.basic.BeanType;
 import org.codehaus.xfire.aegis.type.basic.ObjectType;
 import org.codehaus.xfire.fault.XFireFault;
 import org.codehaus.xfire.service.MessagePartContainer;
 import org.codehaus.xfire.service.MessagePartInfo;
 import org.codehaus.xfire.service.OperationInfo;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.service.binding.AbstractBindingProvider;
 import org.codehaus.xfire.soap.SoapConstants;
 import org.codehaus.xfire.util.ClassLoaderUtils;
 import org.codehaus.xfire.util.NamespaceHelper;
 import org.codehaus.xfire.wsdl.SchemaType;
 import org.codehaus.xfire.wsdl11.builder.WSDLBuilder;
 
 /**
  * A BindingProvider for the Aegis type system.
  * 
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  */
 public class AegisBindingProvider
     extends AbstractBindingProvider
 {
     public static final Log LOG = LogFactory.getLog(AegisBindingProvider.class);
 
     public static final String CURRENT_MESSAGE_PART = "currentMessagePart";
 
     public static final String TYPE_MAPPING_KEY = "type.mapping";
 
     public static final String ENCODING_URI_KEY = "type.encodingUri";
 
     public static final String WRITE_XSI_TYPE_KEY = "writeXsiType";
 
     public static final String OVERRIDE_TYPES_KEY = "overrideTypesList";
 
     private TypeMappingRegistry registry;
 
     private Map part2type = new HashMap();
 
     public AegisBindingProvider()
     {
         this(new DefaultTypeMappingRegistry(true));
     }
 
     public AegisBindingProvider(TypeMappingRegistry registry)
     {
         this.registry = registry;
     }
 
     public TypeMappingRegistry getTypeMappingRegistry()
     {
         return registry;
     }
 
     public void setTypeMappingRegistry(TypeMappingRegistry registry)
     {
         this.registry = registry;
     }
 
     protected void initializeMessage(Service service, MessagePartContainer container, int type)
     {
         List classes = (List) service.getProperty(OVERRIDE_TYPES_KEY);
 
         if (classes != null)
         {
             List types = new ArrayList();
             TypeMapping tm = getTypeMapping(service);
             for (Iterator it = classes.iterator(); it.hasNext();)
             {
                 String typeName = (String) it.next();
                 Class c;
                 try
                 {
                     c = ClassLoaderUtils.loadClass(typeName, AegisBindingProvider.class);
 
                 }
                 catch (ClassNotFoundException e)
                 {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                     continue;
                 }
                 Type t = tm.getType(c);
                 if (t == null)
                 {
                     t = tm.getTypeCreator().createType(c);
                     tm.register(t);
                 }
                 if (t instanceof BeanType)
                 {
                     BeanType bt = (BeanType) t;
                     bt.getTypeInfo().setExtension(true);
                     types.add(bt);
                 }
             }
             service.setProperty(WSDLBuilder.OVERRIDING_TYPES, types);
         }
         for (Iterator itr = container.getMessageParts().iterator(); itr.hasNext();)
         {
             MessagePartInfo part = (MessagePartInfo) itr.next();
 
             if (part.getSchemaType() == null)
             {
                 part.setSchemaType(getParameterType(getTypeMapping(service), part, type));
             }
         }
     }
 
     public Object readParameter(MessagePartInfo p, XMLStreamReader xsr, MessageContext context)
         throws XFireFault
     {
         Type type = getTypeMapping(context.getService()).getType(xsr.getName());
 
         if (type == null)
             type = (Type) p.getSchemaType();
 
         type = getReadType(xsr, context, type);
 
         MessageReader reader = new ElementReader(xsr);
 
         if (reader.isXsiNil())
         {
             reader.readToEnd();
             return null;
         }
 
         context.setProperty(CURRENT_MESSAGE_PART, p);
         return type.readObject(reader, context);
     }
 
     public static Type getReadType(XMLStreamReader xsr, MessageContext context, Type type)
     {
         String overrideType = xsr.getAttributeValue(SoapConstants.XSI_NS, "type");
         if (overrideType != null)
         {
             QName overrideTypeName = NamespaceHelper.createQName(xsr.getNamespaceContext(),
                                                                  overrideType);
             if (!overrideTypeName.equals(type.getSchemaType()))
             {
                 Type type2 = type.getTypeMapping().getType(overrideTypeName);
                 if (type2 == null)
                 {
                    LOG.info("xsi:type=\"" + overrideTypeName
                             + "\" was specified, but no corresponding Type was registered; defaulting to "
                             + type.getSchemaType());
                 }
                 else
                 {
                     type = type2;
                 }
             }
         }
         return type;
     }
 
     public void writeParameter(MessagePartInfo p,
                                XMLStreamWriter writer,
                                MessageContext context,
                                Object value)
         throws XFireFault
     {
         Type type = (Type) p.getSchemaType();
 
         type = getWriteType(context, value, type);
         MessageWriter mw = new ElementWriter(writer);
 
         if (type.isNillable() && value == null)
         {
             mw.writeXsiNil();
             return;
         }
 
         context.setProperty(CURRENT_MESSAGE_PART, p);
         type.writeObject(value, mw, context);
     }
 
     public static Type getWriteType(MessageContext context, Object value, Type type)
     {
         if (value != null && type != null && type.getTypeClass() != value.getClass()
                 && context.getService() != null)
         {
             List l = (List) context.getService().getProperty(OVERRIDE_TYPES_KEY);
             if (l != null && l.contains(value.getClass().getName()))
             {
                 type = type.getTypeMapping().getType(value.getClass());
             }
         }
         return type;
     }
 
     public QName getSuggestedName(Service service, OperationInfo op, int param)
     {
         TypeMapping tm = getTypeMapping(service);
         QName name = tm.getTypeCreator().getElementName(op.getMethod(), param);
 
         // No mapped name was specified, so if its a complex type use that name
         // instead
         if (name == null)
         {
             Type type = tm.getTypeCreator().createType(op.getMethod(), param);
 
             if (type.isComplex() && !type.isAbstract())
                 name = type.getSchemaType();
         }
 
         return name;
     }
 
     private Type getParameterType(TypeMapping tm, MessagePartInfo param, int paramtype)
     {
         Type type = tm.getType(param.getName());
 
         if (type == null)
         {
             type = (Type) part2type.get(param);
         }
 
         /*
          * if (type == null && tm.isRegistered(param.getTypeClass())) { type =
          * tm.getType(param.getTypeClass()); part2type.put(param, type); }
          */
 
         if (type == null)
         {
             OperationInfo op = param.getContainer().getOperation();
 
             if (paramtype != FAULT_PARAM)
             {
                 /*
                  * Note: we are not registering the type here, because it is an
                  * anonymous type. Potentially there could be many schema types
                  * with this name. For example, there could be many ns:in0
                  * paramters.
                  */
                 type = tm.getTypeCreator().createType(op.getMethod(), param.getIndex());
             }
             else
             {
                 type = tm.getTypeCreator().createType(param.getTypeClass());
             }
 
             type.setTypeMapping(tm);
             part2type.put(param, type);
         }
 
         return type;
     }
 
     public TypeMapping getTypeMapping(Service service)
     {
         TypeMapping tm = (TypeMapping) service.getProperty(TYPE_MAPPING_KEY);
 
         if (tm == null)
             tm = createTypeMapping(service);
 
         return tm;
     }
 
     protected TypeMapping createTypeMapping(Service endpoint)
     {
         // TypeMapping tm =
         // registry.getTypeMapping(endpoint.getTargetNamespace());
         // if (tm != null) return tm;
 
         String encodingStyle = (String) endpoint.getProperty(ENCODING_URI_KEY);
 
         if (encodingStyle == null)
         {
             encodingStyle = SoapConstants.XSD;
         }
 
         endpoint.setProperty(ENCODING_URI_KEY, encodingStyle);
         TypeMapping tm = registry.createTypeMapping(encodingStyle, true);
 
         endpoint.setProperty(TYPE_MAPPING_KEY, tm);
         registry.register(endpoint.getName().getNamespaceURI(), tm);
 
         return tm;
     }
 
     public Class getTypeClass(QName name, Service service)
     {
         TypeMapping tm;
         if (service != null)
             tm = getTypeMapping(service);
         else
             tm = registry.getDefaultTypeMapping();
 
         Type type = tm.getType(name);
 
         if (type == null)
             return null;
 
         return type.getTypeClass();
     }
 
     public SchemaType getSchemaType(QName name, Service service)
     {
         TypeMapping tm;
         if (service != null)
             tm = getTypeMapping(service);
         else
             tm = registry.getDefaultTypeMapping();
 
         Type type = tm.getType(name);
 
         if (type == null)
         {
             ObjectType ot = new ObjectType();
             ot.setTypeMapping(tm);
             ot.setSchemaType(name);
             type = ot;
         }
 
         return type;
     }
 
     public Type getType(Service service, Class clazz)
     {
         TypeMapping tm = getTypeMapping(service);
         Type type = tm.getType(clazz);
 
         if (type == null)
         {
             type = tm.getTypeCreator().createType(clazz);
             tm.register(type);
         }
 
         return type;
     }
 }
