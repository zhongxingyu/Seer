 package org.codehaus.xfire.wsdl11.builder;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.wsdl.Definition;
 import javax.wsdl.Fault;
 import javax.wsdl.Input;
 import javax.wsdl.Message;
 import javax.wsdl.Output;
 import javax.wsdl.Part;
 import javax.wsdl.Port;
 import javax.wsdl.PortType;
 import javax.wsdl.Types;
 import javax.wsdl.WSDLException;
 import javax.wsdl.extensions.schema.Schema;
 import javax.wsdl.factory.WSDLFactory;
 import javax.xml.namespace.QName;
 
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.service.Binding;
 import org.codehaus.xfire.service.Endpoint;
 import org.codehaus.xfire.service.FaultInfo;
 import org.codehaus.xfire.service.MessageInfo;
 import org.codehaus.xfire.service.MessagePartInfo;
 import org.codehaus.xfire.service.OperationInfo;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.service.binding.ObjectServiceFactory;
 import org.codehaus.xfire.soap.SoapConstants;
 import org.codehaus.xfire.transport.TransportManager;
 import org.codehaus.xfire.wsdl.AbstractWSDL;
 import org.codehaus.xfire.wsdl.SchemaType;
 import org.codehaus.xfire.wsdl.WSDLWriter;
 import org.jdom.Attribute;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.Namespace;
 import org.jdom.output.DOMOutputter;
 
 import com.ibm.wsdl.extensions.schema.SchemaImpl;
 
 /**
  * WSDL
  * 
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  */
 public class WSDLBuilder
     extends org.codehaus.xfire.wsdl.AbstractWSDL
     implements WSDLWriter
 {
     public static final String OVERRIDING_TYPES = "overridingTypes";
 
     private static final QName SCHEMA_QNAME = new QName(SoapConstants.XSD, "schema");
     
     private PortType portType;
 
     private Map wsdlOps = new HashMap();
 
     private TransportManager transportManager;
 
     private Map declaredParameters = new HashMap();
 
     private List extensions;
 
     private Definition def;
 
     
     public WSDLBuilder(Service service, TransportManager transportManager) throws WSDLException
     {
         super(service);
 
         setDefinition(WSDLFactory.newInstance().newDefinition());
         getDefinition().setTargetNamespace(getTargetNamespace());
         
         getDefinition().getExtensionRegistry().registerSerializer(Types.class, 
                                                                   SCHEMA_QNAME,
                                                                   new SchemaSerializer());
                                                     
         this.transportManager = transportManager;
     }
 
     public TransportManager getTransportManager()
     {
         return transportManager;
     }
 
     public void setTransportManager(TransportManager transportManager)
     {
         this.transportManager = transportManager;
     }
 
     protected void writeComplexTypes()
     {
         if (getSchemaTypes().getContentSize() > 0)
         {   
             Element schemaTypes = getSchemaTypes();
             List addNs = schemaTypes.getAdditionalNamespaces();
             for (Iterator itr = addNs.iterator(); itr.hasNext();)
             {
                 Namespace ns = (Namespace) itr.next();
 
                 if (getDefinition().getNamespace(ns.getPrefix()) == null)
                     addNamespace(ns.getPrefix(), ns.getURI());
             }
             
             try
             {
                Types types = getDefinition().getTypes();
                if (types == null)
                {
                    types = getDefinition().createTypes();
                    getDefinition().setTypes(types);
                }
                 
                 List children = schemaTypes.getChildren();
                 while (children.size() > 0)
                 {
                     Element child = (Element) children.get(0);
                     child.detach();
                     
                     if (child.getChildren().size() > 0)
                     {
                         Document inputDoc = new Document(child);
                         org.w3c.dom.Document doc = new DOMOutputter().output(inputDoc);
                         
                         Schema uee = new SchemaImpl();
                         uee.setElement((org.w3c.dom.Element) doc.getDocumentElement());
                         uee.setRequired(Boolean.TRUE);
                         uee.setElementType(SCHEMA_QNAME);
                         types.addExtensibilityElement(uee);
                     }
                 }
             }
             catch (JDOMException e)
             {
                 throw new XFireRuntimeException("Could write schemas to wsdl!", e);
             }
         }
     }
         
     public void write(OutputStream out)
         throws IOException
     {
         try
         {
             initialize();
             
             PortType portType = createAbstractInterface();
 
             createConcreteInterface(portType);
             
             updateImports();
             
             initializeOverrideTypes();
             
             writeComplexTypes();
             
             if (extensions != null)
             {
                 for (Iterator itr = extensions.iterator(); itr.hasNext();)
                 {
                     WSDLBuilderExtension ex = (WSDLBuilderExtension) itr.next();
                     
                     ex.extend(getDefinition(), this);
                 }
             }
             
             javax.wsdl.xml.WSDLWriter writer = WSDLFactory.newInstance().newWSDLWriter();
             writer.writeWSDL(getDefinition(), out);
         }
         catch (WSDLException e)
         {
             throw new XFireRuntimeException("Error creating wsdl", e);
         }
     }
 
 
     private void initializeOverrideTypes()
     {
         List l = (List) getService().getProperty(OVERRIDING_TYPES);
         if (l != null)
         {
             for (Iterator it = l.iterator(); it.hasNext();)
             {
                 SchemaType t = (SchemaType) it.next();
                 addDependency(t);
             }
         }
     }
 
     public PortType createAbstractInterface()
         throws WSDLException
     {
         Service service = getService();
         Definition def = getDefinition();
 
         QName portName = service.getServiceInfo().getPortType();
 
         if (portName == null)
             portName = new QName(getTargetNamespace(), service.getSimpleName() + "PortType");
 
         portType = def.createPortType();
         portType.setQName(portName);
         portType.setUndefined(false);
         def.addPortType(portType);
 
         // Create Abstract operations
         for (Iterator itr = service.getServiceInfo().getOperations().iterator(); itr.hasNext();)
         {
             OperationInfo op = (OperationInfo) itr.next();
 
             // Create input message
             Message req = createInputMessage(op);
             def.addMessage(req);
 
             // Create output message if we have an out MEP
             Message res = null;
             if (op.getMEP().equals(SoapConstants.MEP_ROBUST_IN_OUT))
             {
                 res = createOutputMessage(op);
                 def.addMessage(res);
             }
 
             // Create the fault messages
             List faultMessages = new ArrayList();
             for (Iterator faultItr = op.getFaults().iterator(); faultItr.hasNext();)
             {
                 FaultInfo fault = (FaultInfo) faultItr.next();
                 Fault faultMsg = createFault(op, fault);
                 faultMessages.add(faultMsg);
             }
 
             javax.wsdl.Operation wsdlOp = createOperation(op, req, res, faultMessages);
             wsdlOp.setUndefined(false);
             portType.addOperation(wsdlOp);
 
             wsdlOps.put(op.getName(), wsdlOp);
         }
 
         return portType;
     }
 
     public void createConcreteInterface(PortType portType)
     {
         Service service = getService();
         Definition def = getDefinition();
 
         QName name = service.getName();
 
         // Create a concrete instance for each transport.
         javax.wsdl.Service wsdlService = def.createService();
         wsdlService.setQName(name);
 
         for (Iterator itr = service.getBindings().iterator(); itr.hasNext();)
         {
             Binding binding = (Binding) itr.next();
 
             javax.wsdl.Binding wbinding = binding.createBinding(this, portType);
 
             Port port = binding.createPort(this, wbinding);
             if (port != null)
             {
                 wsdlService.addPort(port);
             }
 
             // Add in user defined endpoints
             Collection endpoints = service.getEndpoints(binding.getName());
             if (endpoints == null)
                 continue;
 
             for (Iterator eitr = endpoints.iterator(); eitr.hasNext();)
             {
                 Endpoint ep = (Endpoint) eitr.next();
 
                 port = binding.createPort(ep, this, wbinding);
                 if (port != null)
                 {
                     wsdlService.addPort(port);
                 }
             }
         }
 
         def.addService(wsdlService);
     }
 
     private Message createOutputMessage(OperationInfo op)
     {
         // response message
         Message res = getDefinition().createMessage();
         res.setQName(new QName(getTargetNamespace(), op.getName() + "Response"));
 
         res.setUndefined(false);
 
         if (getService().getServiceInfo().isWrapped())
             createWrappedOutputParts(res, op);
         else
             createOutputParts(res, op);
 
         return res;
     }
 
     private Message createInputMessage(OperationInfo op)
     {
         Message req = getDefinition().createMessage();
         req.setQName(new QName(getTargetNamespace(), op.getName() + "Request"));
         req.setUndefined(false);
 
         if (getService().getServiceInfo().isWrapped())
             createWrappedInputParts(req, op);
         else
             createInputParts(req, op);
 
         return req;
     }
 
     private Fault createFault(OperationInfo op, FaultInfo faultInfo)
     {
         Message faultMsg = getDefinition().createMessage();
         faultMsg.setQName(new QName(getTargetNamespace(), faultInfo.getName()));
         faultMsg.setUndefined(false);
         getDefinition().addMessage(faultMsg);
 
         Fault fault = getDefinition().createFault();
         fault.setName(faultInfo.getName());
         fault.setMessage(faultMsg);
 
         for (Iterator itr = faultInfo.getMessageParts().iterator(); itr.hasNext();)
         {
             MessagePartInfo info = (MessagePartInfo) itr.next();
 
             String uri = info.getName().getNamespaceURI();
             addNamespace(getNamespacePrefix(uri), uri);
 
             Part part = createPart(info);
             faultMsg.addPart(part);
         }
 
         return fault;
     }
 
     public Part createPart(MessagePartInfo part)
     {
         String style = (String) getService().getProperty(ObjectServiceFactory.STYLE);
         if (style != null && style.equals(SoapConstants.STYLE_RPC))
         {
             return createRpcLitPart(part.getName(), part.getTypeClass(), part.getSchemaType());
         }
         else
         {
             return createDocLitPart(part.getName(), part.getTypeClass(), part.getSchemaType());
         }
     }
 
     /**
      * Creates a wsdl:message part without creating a global schema element.
      * 
      * @param pName
      * @param clazz
      * @param type
      * @return
      */
     public Part createRpcLitPart(QName pName, Class clazz, SchemaType type)
     {
         addDependency(type);
 
         QName schemaTypeName = type.getSchemaType();
 
         Part part = getDefinition().createPart();
         part.setName(pName.getLocalPart());
 
         String prefix = getNamespacePrefix(schemaTypeName.getNamespaceURI());
         addNamespace(prefix, schemaTypeName.getNamespaceURI());
 
         if (!type.isAbstract())
         {
             part.setElementName(schemaTypeName);
         }
         else
         {
 
             part.setTypeName(schemaTypeName);
         }
 
         return part;
     }
 
     /**
      * Creates a wsdl:message part and a global schema element for it if it is
      * abstract.
      * 
      * @param pName
      * @param clazz
      * @param type
      * @return
      */
     public Part createDocLitPart(QName pName, Class clazz, SchemaType type)
     {
         addDependency(type);
 
         QName schemaTypeName = type.getSchemaType();
 
         Part part = getDefinition().createPart();
         part.setName(pName.getLocalPart());
 
         if (!type.isAbstract())
         {
             String prefix = getNamespacePrefix(schemaTypeName.getNamespaceURI());
             addNamespace(prefix, schemaTypeName.getNamespaceURI());
 
             part.setElementName(schemaTypeName);
 
             return part;
         }
 
         SchemaType regdType = (SchemaType) declaredParameters.get(pName);
         if (regdType == null)
         {
             Element schemaEl = createSchemaType(pName.getNamespaceURI());
 
             Element element = new Element("element", XSD_NS);
             schemaEl.addContent(element);
 
             String prefix = getNamespacePrefix(schemaTypeName.getNamespaceURI());
             addNamespace(prefix, schemaTypeName.getNamespaceURI());
 
             if (type.isAbstract())
             {
                 element.setAttribute(new Attribute("name", pName.getLocalPart()));
                 element.setAttribute(new Attribute("type", prefix + ":"
                         + schemaTypeName.getLocalPart()));
             }
 
             declaredParameters.put(pName, type);
         }
         else
         {
             if (!regdType.equals(type))
             {
                 throw new XFireRuntimeException(
                         "Cannot create two schema elements with the same name "
                                 + "and of different types: " + pName);
 
             }
         }
 
         part.setElementName(pName);
 
         return part;
     }
 
     public javax.wsdl.Operation createOperation(OperationInfo op,
                                                 Message req,
                                                 Message res,
                                                 List faultMessages)
     {
         Definition def = getDefinition();
         javax.wsdl.Operation wsdlOp = def.createOperation();
 
         Input input = def.createInput();
         input.setMessage(req);
         input.setName(req.getQName().getLocalPart());
         wsdlOp.setInput(input);
 
         if (res != null)
         {
             Output output = def.createOutput();
             output.setMessage(res);
             output.setName(res.getQName().getLocalPart());
             wsdlOp.setOutput(output);
         }
 
         for (Iterator itr = faultMessages.iterator(); itr.hasNext();)
         {
             wsdlOp.addFault((Fault) itr.next());
         }
 
         wsdlOp.setName(op.getName());
 
         return wsdlOp;
     }
 
     public void createInputParts(Message req, OperationInfo op)
     {
         writeParameters(req, op.getInputMessage().getMessageParts());
     }
 
     public void createOutputParts(Message req, OperationInfo op)
     {
         writeParameters(req, op.getOutputMessage().getMessageParts());
     }
 
     private void writeParameters(Message message, Collection params)
     {
         for (Iterator itr = params.iterator(); itr.hasNext();)
         {
             MessagePartInfo param = (MessagePartInfo) itr.next();
 
             String prefix = getNamespacePrefix(param.getName().getNamespaceURI());
             addNamespace(prefix, param.getName().getNamespaceURI());
             
             addNamespaceImport(getService().getTargetNamespace(), param.getSchemaType()
                     .getSchemaType().getNamespaceURI());
 
             Part part = createPart(param);
 
             message.addPart(part);
         }
     }
 
     protected void createWrappedInputParts(Message req, OperationInfo op)
     {
         Part part = getDefinition().createPart();
 
         QName typeQName = createDocumentType(op.getInputMessage(), part, op.getName());
         part.setName("parameters");
         part.setElementName(typeQName);
 
         req.addPart(part);
     }
 
     protected void createWrappedOutputParts(Message req, OperationInfo op)
     {
         // response message part
         Part part = getDefinition().createPart();
 
         // Document style service
         QName typeQName = createDocumentType(op.getOutputMessage(), part, op.getName() + "Response");
         part.setElementName(typeQName);
         part.setName("parameters");
 
         req.addPart(part);
     }
 
     protected QName createDocumentType(MessageInfo message, Part part, String opName)
     {
         Element element = new Element("element", AbstractWSDL.XSD_NS);
         element.setAttribute(new Attribute("name", opName));
 
         Element complex = new Element("complexType", AbstractWSDL.XSD_NS);
         element.addContent(complex);
 
         if (message.getMessageParts().size() > 0)
         {
             Element sequence = createSequence(complex);
 
             writeParametersSchema(message.getMessageParts(), sequence);
         }
 
         /**
          * Don't create the schema until after we add the types in (via
          * WSDLBuilder.addDependency()) writeParametersSchema.
          */
         Element schemaEl = createSchemaType(getTargetNamespace());
         schemaEl.addContent(element);
 
         return new QName(getTargetNamespace(), opName);
     }
 
     /**
      * @param op
      * @param sequence
      */
     protected void writeParametersSchema(Collection params, Element sequence)
     {
         for (Iterator itr = params.iterator(); itr.hasNext();)
         {
             MessagePartInfo param = (MessagePartInfo) itr.next();
 
             QName pName = param.getName();
             SchemaType type = param.getSchemaType();
 
             addDependency(type);
             QName schemaType = type.getSchemaType();
 
             addNamespaceImport(getService().getTargetNamespace(), schemaType.getNamespaceURI());
 
             String uri = type.getSchemaType().getNamespaceURI();
             String prefix = getNamespacePrefix(uri);
             addNamespace(prefix, uri);
             
             Element element = new Element("element", AbstractWSDL.XSD_NS);
             sequence.addContent(element);
 
             if (type.isAbstract())
             {
                 element.setAttribute(new Attribute("name", pName.getLocalPart()));
 
                 element.setAttribute(new Attribute("type", prefix + ":"
                                 + schemaType.getLocalPart()));
                 
                 if (type.isNillable())
                 {
                     element.setAttribute(new Attribute("nillable", "true"));
                 }
             }
             else
             {
                 element.setAttribute(new Attribute("ref", prefix + ":" + schemaType.getLocalPart()));
             }
 
             element.setAttribute(new Attribute("minOccurs", "1"));
             element.setAttribute(new Attribute("maxOccurs", "1"));
         }
     }
 
     public void addNamespace(String prefix, String uri)
     {
         def.addNamespace(prefix, uri);
 
         super.addNamespace(prefix, uri);
     }
 
     protected Element createSequence(Element complex)
     {
         Element sequence = new Element("sequence", AbstractWSDL.XSD_NS);
         complex.addContent(sequence);
         return sequence;
     }
 
     public List getWSDLBuilderExtensions()
     {
         return extensions;
     }
 
     public void setWSDLBuilderExtensions(List extensions)
     {
         this.extensions = extensions;
     }
 
     public Definition getDefinition()
     {
         return def;
     }
 
     public void setDefinition(Definition definition)
     {
         this.def = definition;
     }
 
 }
