 package org.codehaus.xfire.service.binding;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.namespace.QName;
 
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.XFireFactory;
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.fault.FaultSender;
 import org.codehaus.xfire.fault.SoapFaultSerializer;
 import org.codehaus.xfire.fault.XFireFault;
 import org.codehaus.xfire.handler.CustomFaultHandler;
 import org.codehaus.xfire.handler.OutMessageSender;
 import org.codehaus.xfire.service.Binding;
 import org.codehaus.xfire.service.Endpoint;
 import org.codehaus.xfire.service.FaultInfo;
 import org.codehaus.xfire.service.MessageInfo;
 import org.codehaus.xfire.service.MessagePartContainer;
 import org.codehaus.xfire.service.OperationInfo;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.service.ServiceFactory;
 import org.codehaus.xfire.service.ServiceInfo;
 import org.codehaus.xfire.soap.AbstractSoapBinding;
 import org.codehaus.xfire.soap.Soap11Binding;
 import org.codehaus.xfire.soap.Soap12Binding;
 import org.codehaus.xfire.soap.SoapConstants;
 import org.codehaus.xfire.soap.SoapTransport;
 import org.codehaus.xfire.soap.SoapVersion;
 import org.codehaus.xfire.transport.Transport;
 import org.codehaus.xfire.transport.TransportManager;
 import org.codehaus.xfire.transport.http.SoapHttpTransport;
 import org.codehaus.xfire.transport.local.LocalTransport;
 import org.codehaus.xfire.util.ClassLoaderUtils;
 import org.codehaus.xfire.util.MethodComparator;
 import org.codehaus.xfire.util.NamespaceHelper;
 import org.codehaus.xfire.util.ServiceUtils;
 import org.codehaus.xfire.wsdl.ResourceWSDL;
 import org.codehaus.xfire.wsdl11.builder.DefaultWSDLBuilderFactory;
 import org.codehaus.xfire.wsdl11.builder.WSDLBuilderAdapter;
 import org.codehaus.xfire.wsdl11.builder.WSDLBuilderFactory;
 import org.codehaus.xfire.wsdl11.parser.WSDLServiceConfigurator;
 
 /**
  * Creates Services from java objects. This class is meant to be easily overridable
  * so you can customize how your services are created. 
  *
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  * @author <a href="mailto:poutsma@mac.com">Arjen Poutsma</a>
  */
 public class ObjectServiceFactory
         implements ServiceFactory
 {
     public static final String PORT_TYPE = "objectServiceFactory.portType";
     public static final String STYLE = "sobjectServiceFactory.tyle";
     public static final String USE = "objectServiceFactory.use";
     public static final String CREATE_DEFAULT_BINDINGS =  "objectServiceFactory.createDefaultBindings";
     public static final String SOAP11_TRANSPORTS =  "objectServiceFactory.soap11Transports";
     public static final String SOAP12_TRANSPORTS =  "objectServiceFactory.soap12Transports";
 
     private BindingProvider bindingProvider;
     private TransportManager transportManager;
     private String style;
     private String use;
     private Set ignoredClasses = new HashSet();
     private boolean voidOneWay;
     private WSDLBuilderFactory wsdlBuilderFactory = new DefaultWSDLBuilderFactory();
     private boolean customFaultsEnabled = true;
     private boolean bindingCreationEnabled = true;
     
     private Set soap11Transports = new HashSet();
     private Set soap12Transports = new HashSet();
     
     /**
      * Initializes a new instance of the <code>ObjectServiceFactory</code>.
      * Uses the XFireFactory to obtain an instance of the TransportManager.
      */
     public ObjectServiceFactory()
     {
         this(XFireFactory.newInstance().getXFire().getTransportManager());
     }
 
     /**
      * Initializes a new instance of the <code>ObjectServiceFactory</code> with the given transport manager and type
      * mapping registry.
      *
      * @param transportManager the transport manager
      * @param provider         the binding provider
      */
     public ObjectServiceFactory(TransportManager transportManager, BindingProvider provider)
     {
         this(transportManager);
 
         this.bindingProvider = provider;
     }
 
     /**
      * Initializes a new instance of the <code>ObjectServiceFactory</code>.
      */
     public ObjectServiceFactory(TransportManager transportManager)
     {
         this.transportManager = transportManager;
         setStyle(SoapConstants.STYLE_WRAPPED);
         setUse(SoapConstants.USE_LITERAL);
         
         soap11Transports.add(SoapHttpTransport.SOAP11_HTTP_BINDING);
         soap11Transports.add(LocalTransport.BINDING_ID);
         soap12Transports.add(LocalTransport.BINDING_ID);
 
         ignoredClasses.add("java.lang.Object");
         ignoredClasses.add("java.lang.Throwable");
         ignoredClasses.add("org.omg.CORBA_2_3.portable.ObjectImpl");
         ignoredClasses.add("org.omg.CORBA.portable.ObjectImpl");
         ignoredClasses.add("javax.ejb.EJBObject");
         ignoredClasses.add("javax.rmi.CORBA.Stub");
     }
 
     public ObjectServiceFactory(BindingProvider bp)
     {
         this(XFireFactory.newInstance().getXFire().getTransportManager(), bp);
     }
     
     public BindingProvider getBindingProvider()
     {
         if (bindingProvider == null)
         {
             try
             {
                 bindingProvider = (BindingProvider) ClassLoaderUtils
                         .loadClass("org.codehaus.xfire.aegis.AegisBindingProvider", getClass()).newInstance();
             }
             catch (Exception e)
             {
                 throw new XFireRuntimeException("Couldn't find a binding provider!", e);
             }
         }
 
         return bindingProvider;
     }
 
     /**
      * Creates a service via <code>create(Class)</code>. It then configures
      * the bindings and endpoints on the service via the WSDL. 
      */
     public Service create(Class clazz, QName name, URL wsdlUrl, Map properties)
     {
         if (properties == null) properties = new HashMap();
         
         properties.put(CREATE_DEFAULT_BINDINGS, Boolean.FALSE);
         
         Service service = create(clazz, properties);
         service.setName(name);
         
         service.setWSDLWriter(new ResourceWSDL(wsdlUrl));
 
         try
         {
             WSDLServiceConfigurator config = new WSDLServiceConfigurator(service, wsdlUrl, transportManager);
             config.configure();
         }
         catch (Exception e)
         {
             if (e instanceof XFireRuntimeException)
                 throw (XFireRuntimeException) e;
             
             throw new XFireRuntimeException("Couldn't configure service.", e);
         }
         
         
         return service;
     }
     
     /**
      * Creates a service from the specified class. The service name will be the 
      * unqualified class name. The namespace will be based on the package. 
      * The service will use soap version 1.1, wrapped style, and literal use.
      * 
      * @param clazz
      *            The service class used to populate the operations and
      *            parameters. If the class is an interface, then the
      *            implementation class that implements that interface must be
      *            set via {@link Service#setProperty(String, Object)} with the
      *            property key being
      *            {@link org.codehaus.xfire.service.binding.ObjectInvoker#SERVICE_IMPL_CLASS}
      * @return The service.
      */
     public Service create(Class clazz)
     {
         return create(clazz, (Map) null);
     }
 
     /**
      * Creates a service from the specified class. The service name will be the 
      * unqualified class name. The namespace will be based on the package. 
      * The service will use soap version 1.1, wrapped style, and literal use.
      * 
      * @param clazz
      *            The service class used to populate the operations and
      *            parameters. If the class is an interface, then the
      *            implementation class that implements that interface must be
      *            set via {@link Service#setProperty(String, Object)} with the
      *            property key being
      *            {@link org.codehaus.xfire.service.binding.ObjectInvoker#SERVICE_IMPL_CLASS}
      * @return The service.
      */
     public Service create(Class clazz, Map properties)
     {
         return create(clazz, (String) null, (String) null, properties);
     }
 
     protected String makeServiceNameFromClassName(Class clazz)
     {
         return ServiceUtils.makeServiceNameFromClassName(clazz);
     }
 
     /**
      * Creates a service from the specified class, soap version, style and use. The returned service will have a name
      * based on the class name, and a namespace based on the class package.
      * <p/>
      * Some parameters can be <code>null</code>, and will be replaced with sensible defaults if so. See the specific
      * parameters for more info.
      *
      * @param clazz            The service class used to populate the operations and parameters.
      * @param name             The name of the service. If <code>null</code>, a name will be generated from the class
      *                         name.
      * @param namespace        The default namespace of the service. If <code>null</code>, a namespace will be generated
      *                         from the class package.
      * @return The service.
      */
     public Service create(Class clazz, String name, String namespace, Map properties)
     {
         String theName = (name != null) ? name : makeServiceNameFromClassName(clazz);
         String theNamespace = (namespace != null) ? namespace : getTargetNamespace(clazz);
         QName qName = new QName(theNamespace, theName);
         
         SoapVersion theVersion = null;
         String theStyle = null;
         String theUse = null;
         QName portType = null;
         Collection s11Bindings = null;
         Collection s12Bindings = null;
         
         if (properties != null)
         {
             theStyle = (String) properties.get(STYLE);
             theUse = (String) properties.get(USE);
             portType = (QName) properties.get(PORT_TYPE);
             s11Bindings = (List) properties.get(SOAP11_TRANSPORTS);
             s12Bindings = (List) properties.get(SOAP12_TRANSPORTS);
         }
         
         if (theStyle == null) theStyle = style;
         if (theUse == null) theUse = use;
         if (portType == null) portType = new QName(theNamespace, theName + "PortType");
         
         ServiceInfo serviceInfo = new ServiceInfo(portType, clazz);
 
         if (theStyle.equals(SoapConstants.STYLE_WRAPPED))
             serviceInfo.setWrapped(true);
         
         Service endpoint = new Service(serviceInfo);
         endpoint.setName(qName);
         setProperties(endpoint, properties);
 
         endpoint.setInvoker(new ObjectInvoker());
         endpoint.setFaultSerializer(new SoapFaultSerializer());
 
         endpoint.setWSDLWriter(new WSDLBuilderAdapter(getWsdlBuilderFactory(), endpoint, transportManager));
  
         initializeOperations(endpoint, theStyle);
 
         endpoint.setProperty(STYLE, theStyle);
         endpoint.setProperty(USE, theUse);
         
         boolean buildBindings = bindingCreationEnabled;
         if (properties != null && properties.containsKey(CREATE_DEFAULT_BINDINGS))
         {
             buildBindings = ((Boolean) properties.get(CREATE_DEFAULT_BINDINGS)).booleanValue();
         }
         
         if (s11Bindings == null) s11Bindings = new HashSet();
         if (s12Bindings == null) s12Bindings = new HashSet();
         
         if (buildBindings)
         {
             s11Bindings.addAll(getSoap11Transports());
             s12Bindings.addAll(getSoap12Transports());
         }
         
         createBindings(endpoint, s11Bindings, s12Bindings);
         
         try
         {
             BindingProvider provider = getBindingProvider();
             provider.initialize(endpoint);
             endpoint.setBindingProvider(provider);
         }
         catch (Exception e)
         {
             if(e instanceof XFireRuntimeException) throw (XFireRuntimeException)e;
             throw new XFireRuntimeException("Couldn't load provider.", e);
         }
 
         registerHandlers(endpoint);
 
         return endpoint;
     }
 
     protected String getTargetNamespace(Class clazz)
     {
         return NamespaceHelper.makeNamespaceFromClassName(
                 clazz.getName(), "http");
     }
 
     /**
      * Get a list of Transports which are enabled over SOAP 1.1.
      * @return
      */
     public Collection getSoap11Transports()
     {
         return soap11Transports;
     }
     
     public void addSoap11Transport(String id)
     {
         soap11Transports.add(id);
     }
 
     /**
      * Get a list of Transports which are enabled over SOAP 1.2.
      * @return
      */
     public Collection getSoap12Transports()
     {
         return soap12Transports;
     }
     
     public void addSoap12Transport(String id)
     {
         soap12Transports.add(id);
     }
     
     protected void createBindings(Service service, Collection s11, Collection s12)
     {
         QName name = service.getName();
 
         for (Iterator itr = s11.iterator(); itr.hasNext();)
         {
             String bindingId = (String) itr.next();
             Transport t = transportManager.getTransport(bindingId);
 
             if (t instanceof SoapTransport)
             {
                 createSoap11Binding(service, null, bindingId);
             }
             else
             {
                 throw new XFireRuntimeException("Binding " + bindingId + " is not a SoapTransport!");            
             }
         }
         
         for (Iterator itr = s12.iterator(); itr.hasNext();)
         {
             String bindingId = (String) itr.next();
             Transport t = transportManager.getTransport(bindingId);
 
             if (t instanceof SoapTransport)
             {
                 createSoap12Binding(service, null, bindingId);
             }
             else
             {
                 throw new XFireRuntimeException("Binding " + bindingId + " is not a SoapTransport!");            
             }
         }
     }
 
     /**
      * Creates an endpoint for a service.  Additionally it opens a channel for this endpoint
      * as well.
      * 
      * @param service
      * @param name
      * @param url
      * @param binding
      * @return
      * @throws Exception 
      */
     public Endpoint createEndpoint(Service service, QName name, String url, Binding binding) 
         throws Exception
     {
         Endpoint endpoint = service.addEndpoint(name, binding, url);
         
         getTransportManager().getTransport(binding.getBindingId()).createChannel(url);
         
         return endpoint;
     }
     
     /**
      * Create a SOAP 1.2 binding for the specified binding id.
      * 
      * @param service
      * @param bindingName The name of the binding. If null, one will be created.
      * @param bindingId
      * @return
      */
     public Soap12Binding createSoap12Binding(Service service, QName bindingName, String bindingId)
     {
         if (bindingName == null)
         {
             SoapTransport st = (SoapTransport) transportManager.getTransport(bindingId);
             bindingName = new QName(service.getTargetNamespace(), 
                                     service.getSimpleName() + st.getName() + "12Binding");
         }
         
         Soap12Binding binding = new Soap12Binding(bindingName, bindingId, service);
         
         createSoapBinding(service, binding);
            
         return binding;
     }
 
     /**
     * Create a SOAP 1.1 binding for the specified binding id.
     * 
     * @param service
     * @param bindingName The name of the binding. If null, one will be created.
     * @param bindingId
     * @return
     */
     public Soap11Binding createSoap11Binding(Service service, QName bindingName, String bindingId)
     {
         if (bindingName == null)
         {
             SoapTransport st = (SoapTransport) transportManager.getTransport(bindingId);
             bindingName = new QName(service.getTargetNamespace(), 
                                     service.getSimpleName() + st.getName() + "Binding");
         }
 
         Soap11Binding binding = new Soap11Binding(bindingName, bindingId, service);
      
         createSoapBinding(service, binding);
         
         return binding;
     }
 
     protected void createSoapBinding(Service service, AbstractSoapBinding binding)
     {
         ServiceInfo serviceInfo = service.getServiceInfo();
 
         String style = (String) service.getProperty(STYLE);
         String use = (String) service.getProperty(USE);
         binding.setStyle(style);
         binding.setUse(use);
         binding.setSerializer(AbstractSoapBinding.getSerializer(style, use));
         
         // Create SOAP metadata for the binding operation
         for (Iterator itr = serviceInfo.getOperations().iterator(); itr.hasNext();)
         {
             OperationInfo op = (OperationInfo) itr.next();
             
             createBindingOperation(service, binding, op);
         }
     
         service.addBinding(binding);
     }
 
     protected void createBindingOperation(Service service, AbstractSoapBinding binding, OperationInfo op)
     {
         binding.setSoapAction(op, getAction(op));
         createMessageBinding(binding, op.getInputMessage());
         
         if (op.hasOutput())
         {
             createMessageBinding(binding, op.getOutputMessage());
         }
         
         for (Iterator fitr = op.getFaults().iterator(); fitr.hasNext();)
         {
             FaultInfo fault = (FaultInfo) fitr.next();
             
             // we don't support fault headers yet...
         }
     }
 
     private void createMessageBinding(AbstractSoapBinding binding, MessageInfo msg)
     {
         Method method = msg.getOperation().getMethod();
         Class[] paramClasses = method.getParameterTypes();
         boolean isDoc = binding.getStyle().equals(SoapConstants.STYLE_DOCUMENT);
         
         MessagePartContainer parts = binding.getHeaders(msg);
         for (int j = 0; j < paramClasses.length; j++)
         {
             if (!paramClasses[j].equals(MessageContext.class) && isHeader(method, j))
             {
                 final QName q = getInParameterName(binding.getService(), msg.getOperation(), method, j, isDoc);
                 parts.addMessagePart(q, paramClasses[j]).setIndex(j);
             }
         }
     }
 
     protected void registerHandlers(Service service)
     {
         service.addInHandler(new ServiceInvocationHandler());
         service.addOutHandler(new OutMessageSender());
         service.addFaultHandler(new FaultSender());
         service.addFaultHandler(new CustomFaultHandler());
     }
 
     private void setProperties(Service service, Map properties)
     {
         if (properties == null) return;
 
         for (Iterator itr = properties.entrySet().iterator(); itr.hasNext();)
         {
             Map.Entry entry = (Map.Entry) itr.next();
 
             service.setProperty((String) entry.getKey(), entry.getValue());
         }
     }
 
     protected void initializeOperations(Service endpoint, String style)
     {
         final Method[] methods = endpoint.getServiceInfo().getServiceClass().getMethods();
         Arrays.sort(methods, new MethodComparator());
         
         for (int i = 0; i < methods.length; i++)
         {
             final Method method = methods[i];
 
             if (isValidMethod(method))
             {
                 addOperation(endpoint, method, style);
             }
         }
     }
 
     /**
      * Ignore the specified class' declared methods. 
      * This can be used to not expose certain interfaces as a service.
      * By default, the methods specified by the following interfaces/classes are ignored:
      * <li><code>java.lang.Object</code>
      * <li><code>org.omg.CORBA_2_3.portable.ObjectImpl</code>
      * <li><code>org.omg.CORBA.portable.ObjectImpl</code>
      * <li><code>javax.ejb.EJBObject</code>
      * <li><code>javax.ejb.EJBLocalObject</code>
      * <li><code>javax.rmi.CORBA.Stub</code>
      * 
      * @param className the fully qualified class name
      */
     public void addIgnoredMethods(String className)
     {
         ignoredClasses.add(className);
     }
 
     protected boolean isValidMethod(final Method method)
     {
         if(ignoredClasses.contains(method.getDeclaringClass().getName())) return false;
 
         final int modifiers = method.getModifiers();
 
         return Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers);
     }
 
     protected OperationInfo addOperation(Service endpoint, final Method method, String style)
     {
         ServiceInfo service = endpoint.getServiceInfo();
 
         final String opName = getOperationName(service, method);
 
         final OperationInfo op = service.addOperation(opName, method);
 
         final Class[] paramClasses = method.getParameterTypes();
 
         boolean isDoc = style.equals(SoapConstants.STYLE_DOCUMENT);
 
         // Setup the input message
         MessageInfo inMsg = op.createMessage(createInputMessageName(op));
         op.setInputMessage(inMsg);
 
         for (int j = 0; j < paramClasses.length; j++)
         {
             if (!paramClasses[j].equals(MessageContext.class) && !isHeader(method, j))
             {
                 final QName q = getInParameterName(endpoint, op, method, j, isDoc);
                 inMsg.addMessagePart(q, paramClasses[j]).setIndex(j);
             }
         }
 
         String mep = getMEP(method);
         op.setMEP(mep);
         if (hasOutMessage(mep))
         {
             // Setup the output message
             MessageInfo outMsg = op.createMessage(createOutputMessageName(op));
             op.setOutputMessage(outMsg);
 
             final Class returnType = method.getReturnType();
             if (!returnType.isAssignableFrom(void.class) && !isHeader(method, -1))
             {
                 final QName q = getOutParameterName(endpoint, op, method, isDoc);
                 outMsg.addMessagePart(q, method.getReturnType()).setIndex(-1);
             }
         }
 
         if (isCustomFaultsEnabled())
             initializeFaults(endpoint, op);
         
         op.setAsync(isAsync(method));
         
         return op;
     }
 
     protected QName createInputMessageName(final OperationInfo op)
     {
         return new QName(op.getService().getPortType().getNamespaceURI(), op.getName() + "Request");
     }
 
     protected QName createOutputMessageName(final OperationInfo op)
     {
         return new QName(op.getService().getPortType().getNamespaceURI(), op.getName() + "Response");
     }
     
     protected boolean hasOutMessage(String mep)
     {
         if (mep.equals(SoapConstants.MEP_IN)) return false;
         
         return true;
     }
     
     protected void initializeFaults(final Service service, 
                                     final OperationInfo op)
     {
         // Set up the fault messages
         final Class[] exceptionClasses = op.getMethod().getExceptionTypes();
         for (int i = 0; i < exceptionClasses.length; i++)
         {
             Class exClazz = exceptionClasses[i];
             
             // Ignore XFireFaults because they don't need to be declared
             if (exClazz.equals(XFireFault.class) ||
                     exClazz.equals(Exception.class) ||
                     exClazz.equals(RuntimeException.class) ||
                     exClazz.equals(Throwable.class))
             {
                 continue;
             }
             
             addFault(service, op, exClazz);
         }
     }
 
     protected FaultInfo addFault(final Service service, final OperationInfo op, Class exClazz)
     {
         QName name = getFaultName(service, op, exClazz);
         
         FaultInfo info = op.addFault(name.getLocalPart());
         info.addMessagePart(name, exClazz);
         
         return info;
     }
 
     protected QName getFaultName(Service service, OperationInfo o, Class exClazz)
     {
         String name = ServiceUtils.makeServiceNameFromClassName(exClazz);
         return new QName(service.getTargetNamespace(), name);
     }
     
     protected String getAction(OperationInfo op)
     {
         return "";
     }
 
     protected boolean isHeader(Method method, int j)
     {
         return false;
     }
 
     /**
      * Creates a name for the operation from the method name. If an operation with that name
      * already exists, a name is create by appending an integer to the end. I.e. if there is already
      * two methods named <code>doSomething</code>, the first one will have an operation name of
      * "doSomething" and the second "doSomething1".
      * 
      * @param service
      * @param method
      */
     protected String getOperationName(ServiceInfo service, Method method)
     {
         if (service.getOperation(method.getName()) == null)
         {
             return method.getName();
         }
 
         int i = 1;
         while (true)
         {
             String name = method.getName() + i;
             if (service.getOperation(name) == null)
             {
                 return name;
             }
             else
             {
                 i++;
             }
         }
     }
 
     protected String getMEP(final Method method)
     {
         if (isVoidOneWay() && method.getReturnType().equals(void.class))
         {
             return SoapConstants.MEP_IN;
         }
         return SoapConstants.MEP_ROBUST_IN_OUT;
     }
 
     protected boolean isAsync(final Method method)
     {
         return false;
     }
 
     protected QName getInParameterName(final Service endpoint,
                                        final OperationInfo op,
                                        final Method method,
                                        final int paramNumber,
                                        final boolean doc)
     {
         QName suggestion = getBindingProvider().getSuggestedName(endpoint, op, paramNumber);
         
         if (suggestion != null) return suggestion;
         
         String paramName = "";
         if (doc)
             paramName = method.getName();
 
         return new QName(endpoint.getServiceInfo().getPortType().getNamespaceURI(), paramName + "in" + paramNumber);
     }
 
     protected QName getOutParameterName(final Service endpoint, 
                                         final OperationInfo op, 
                                         final Method method, 
                                         final boolean doc)
     {
         QName suggestion = getBindingProvider().getSuggestedName(endpoint, op, -1);
         
         if (suggestion != null) return suggestion;
         
         String outName = "";
         if (doc)
             outName = method.getName();
 
         return new QName(endpoint.getServiceInfo().getPortType().getNamespaceURI(), outName + "out");
     }
 
     public TransportManager getTransportManager()
     {
         return transportManager;
     }
 
     public void setTransportManager(TransportManager transportManager)
     {
         this.transportManager = transportManager;
     }
 
     public void setBindingProvider(BindingProvider bindingProvider)
     {
         this.bindingProvider = bindingProvider;
     }
 
     public String getStyle()
     {
         return style;
     }
 
     public void setStyle(String style)
     {
         this.style = style;
     }
 
     public String getUse()
     {
         return use;
     }
 
     public void setUse(String use)
     {
         this.use = use;
     }
 
     public boolean isVoidOneWay()
     {
         return voidOneWay;
     }
 
     public void setVoidOneWay(boolean voidOneWay)
     {
         this.voidOneWay = voidOneWay;
     }
 
     public WSDLBuilderFactory getWsdlBuilderFactory()
     {
         return wsdlBuilderFactory;
     }
 
     public void setWsdlBuilderFactory(WSDLBuilderFactory wsdlBuilderFactory)
     {
         this.wsdlBuilderFactory = wsdlBuilderFactory;
     }
 
     public boolean isCustomFaultsEnabled()
     {
         return customFaultsEnabled;
     }
 
     public void setCustomFaultsEnabled(boolean customFaultsEnabled)
     {
         this.customFaultsEnabled = customFaultsEnabled;
     }
 
     public boolean isBindingCreationEnabled()
     {
         return bindingCreationEnabled;
     }
 
     public void setBindingCreationEnabled(boolean bindingCreationEnabled)
     {
         this.bindingCreationEnabled = bindingCreationEnabled;
     }
 }
