 package org.codehaus.xfire.service.binding;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.xml.namespace.QName;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.exchange.InMessage;
 import org.codehaus.xfire.exchange.MessageExchange;
 import org.codehaus.xfire.exchange.OutMessage;
 import org.codehaus.xfire.fault.XFireFault;
 import org.codehaus.xfire.handler.AbstractHandler;
 import org.codehaus.xfire.handler.Phase;
 import org.codehaus.xfire.service.MessageHeaderInfo;
 import org.codehaus.xfire.service.MessageInfo;
 import org.codehaus.xfire.service.MessagePartInfo;
 import org.codehaus.xfire.service.OperationInfo;
 import org.codehaus.xfire.util.STAXUtils;
 import org.codehaus.xfire.util.stax.DepthXMLStreamReader;
 
 public abstract class AbstractBinding
     extends AbstractHandler
     implements ObjectBinding, Cloneable
 {
     private static final Log logger = LogFactory.getLog(AbstractBinding.class.getName());
 
     public static final String OPERATION_KEY = "xfire.operation";
     
     public static final String RESPONSE_VALUE = "xfire.java.response";
 
     public static final String RESPONSE_PIPE = "xfire.java.responsePipe";
 
     private String style;
     private Invoker invoker;
     private BindingProvider bindingProvider;
     private boolean clientModeOn = false;
 
     public String getPhase()
     {
         return Phase.SERVICE;
     }
 
     public void setOperation(OperationInfo operation, MessageContext context)
     {
         MessageExchange exchange = context.createMessageExchange(operation);
         
         context.setExchange(exchange);
     }
 
     public void invoke(final MessageContext context)
         throws Exception
     {
         try
         {
             // Read in the parameters...
             final List params = (List) context.getInMessage().getBody();
 
             // Don't read the operation in until after reading. Otherwise
             // it won't work for document style services.
             final OperationInfo operation = context.getExchange().getOperation();
 
             // read in the headers
             final List headerInfos = operation.getInputMessage().getMessageHeaders();
             for (Iterator itr = headerInfos.iterator(); itr.hasNext();)
             {
                 MessageHeaderInfo header = (MessageHeaderInfo) itr.next();
                 BindingProvider provider = context.getService().getBinding().getBindingProvider();
                 params.add(header.getIndex(), provider.readHeader(header, context));
             }
 
             final Invoker invoker = getInvoker();
             
             // invoke the service method...
             if (!operation.isAsync())
             {
                 sendMessage(context, params, operation, invoker);
             }
             else
             {
                 Runnable runnable = new Runnable() 
                 {
                     public void run() 
                     {
                         try
                         {
                             sendMessage(context, params, operation, invoker);
                         }
                         catch (Exception e)
                         {
                             XFireFault fault = XFireFault.createFault(e);
                             
                             context.getInPipeline().handleFault(fault, context);
                         }
                     }
                 };
                 
                 Thread opthread = new Thread(runnable);
                 opthread.start();
             }
         }
         catch (XFireRuntimeException e)
         {
             logger.warn("Error invoking service.", e);
            throw new XFireFault("Error invoking service.", e, XFireFault.SENDER);
         }
     }
 
     protected void sendMessage(final MessageContext context,
                                final List params,
                                final OperationInfo operation,
                                final Invoker invoker)
         throws Exception
     {
         final Object value = invoker.invoke(operation.getMethod(),
                                             params.toArray(),
                                             context);
 
         if (context.getExchange().hasOutMessage())
         {
             OutMessage outMsg = context.getExchange().getOutMessage();
             outMsg.setBody(new Object[] {value});
             outMsg.setSerializer(context.getService().getBinding());
             context.getOutPipeline().invoke(context);
         }
     }
 
     protected void nextEvent(XMLStreamReader dr)
     {
         try
         {
             dr.next();
         }
         catch (XMLStreamException e)
         {
             throw new XFireRuntimeException("Couldn't parse stream.", e);
         }
     }
 
     protected OperationInfo findOperation(Collection operations, List parameters)
     {
         for ( Iterator itr = operations.iterator(); itr.hasNext(); )
         {
             OperationInfo o = (OperationInfo) itr.next();
             List messageParts = o.getInputMessage().getMessageParts();
             if ( messageParts.size() == parameters.size() )
             {
                 if (checkParameters(messageParts, parameters))
                     return o;
             }
         }
         
         return null;
     }
 
     private boolean checkParameters(List messageParts, List parameters)
     {
         Iterator messagePartIterator = messageParts.iterator();
         for (Iterator parameterIterator = parameters.iterator(); parameterIterator.hasNext();)
         {
             Object param = parameterIterator.next();
             MessagePartInfo mpi = (MessagePartInfo) messagePartIterator.next();
             
             if (!mpi.getTypeClass().isAssignableFrom(param.getClass()))
             {
                 if (!param.getClass().isPrimitive() && mpi.getTypeClass().isPrimitive())
                 {
                     return checkPrimitiveMatch(mpi.getTypeClass(), param.getClass());
                 }
                 else
                 {
                     return false;
                 }
             }
         }
         return true;
     }
 
     private boolean checkPrimitiveMatch(Class clazz, Class typeClass)
     {
         if ((typeClass == Integer.class && clazz == int.class) ||
                 (typeClass == Double.class && clazz == double.class) ||
                 (typeClass == Long.class && clazz == long.class) ||
                 (typeClass == Float.class && clazz == float.class) ||
                 (typeClass == Short.class && clazz == short.class) ||
                 (typeClass == Boolean.class && clazz == boolean.class) ||
                 (typeClass == Byte.class && clazz == byte.class))
             return true;
         
         return false;
     }
 
     protected MessagePartInfo findMessagePart(Collection operations, QName name)
     {
         for ( Iterator itr = operations.iterator(); itr.hasNext(); )
         {
             OperationInfo op = (OperationInfo) itr.next();
             MessageInfo msgInfo = null;
             if (isClientModeOn())
             {
                 msgInfo = op.getOutputMessage();
             }
             else
             {
                 msgInfo = op.getInputMessage();
             }
 
             MessagePartInfo p = msgInfo.getMessagePart(name);
 
             if ( p != null )
                 return p;
         }
         return null;
     }
 
     protected void read(InMessage inMessage, MessageContext context, Collection operations)
         throws XFireFault
     {
         List parameters = new ArrayList();
         OperationInfo opInfo = context.getExchange().getOperation();
         
         DepthXMLStreamReader dr = new DepthXMLStreamReader(context.getInMessage().getXMLStreamReader());
         while (STAXUtils.toNextElement(dr))
         {
             MessagePartInfo p;
             if (opInfo != null && isClientModeOn())
             {
                 p = opInfo.getOutputMessage().getMessagePart(dr.getName());
             }
             else if (opInfo != null && !isClientModeOn())
             {
                 p = opInfo.getInputMessage().getMessagePart(dr.getName());
             }
             else
             {
                 p = findMessagePart(operations, dr.getName());
             }
             
             if (p == null)
             {
                 throw new XFireFault("Parameter " + dr.getName() + " does not exist!", 
                                      XFireFault.SENDER);
             }
 
             parameters.add( getBindingProvider().readParameter(p, dr, context) );
         }
 
         if (opInfo == null && !isClientModeOn())
         {
             opInfo = findOperation(operations, parameters);
 
             if (opInfo == null)
             {
                 StringBuffer sb = new StringBuffer("Could not find appropriate operation for request ");
                 //we know we have at least one operation, right?
                 sb.append(((OperationInfo)operations.iterator().next()).getName());
                 sb.append('(');
                 for(Iterator iterator = parameters.iterator(); iterator.hasNext();)
                 {
                     sb.append(iterator.next().getClass().getName());
                     if(iterator.hasNext())
                     {
                         sb.append(", ");
                     }
                 }
                 sb.append(") in service '");
                 sb.append(context.getService().getName());
                 sb.append('\'');
                 throw new XFireFault(sb.toString(), XFireFault.SENDER);
             }
             
             setOperation(opInfo, context);
         }
         
         context.getInMessage().setBody(parameters);
     }
     
     public Invoker getInvoker()
     {
         return invoker;
     }
  
     public void setInvoker(Invoker invoker)
     {
         this.invoker = invoker;
     }
     
     public String getStyle()
     {
         return style;
     }
     
     protected void setStyle(String style)
     {
         this.style = style;
     }
 
     public BindingProvider getBindingProvider()
     {
         return bindingProvider;
     }
 
     public void setBindingProvider(BindingProvider bindingProvider)
     {
         this.bindingProvider = bindingProvider;
     }
 
     public boolean isClientModeOn()
     {
         return clientModeOn;
     }
 
     public void setClientModeOn(boolean clientModeOn)
     {
         this.clientModeOn = clientModeOn;
     }
     
     public abstract Object clone();
 }
