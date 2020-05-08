 package org.codehaus.xfire.addressing;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.exchange.AbstractMessage;
 import org.codehaus.xfire.exchange.InMessage;
 import org.codehaus.xfire.exchange.MessageExchange;
 import org.codehaus.xfire.exchange.OutMessage;
 import org.codehaus.xfire.fault.XFireFault;
 import org.codehaus.xfire.handler.AbstractHandler;
 import org.codehaus.xfire.handler.Phase;
 import org.codehaus.xfire.service.OperationInfo;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.transport.Channel;
 import org.codehaus.xfire.transport.Transport;
 import org.codehaus.xfire.transport.dead.DeadLetterTransport;
 import org.jdom.Attribute;
 import org.jdom.Element;
 
 public class AddressingInHandler
     extends AbstractHandler
 {
    //private static RandomGUID guidGenerator = new RandomGUID(false);
 
     public static final Object ADRESSING_HEADERS = "xfire-ws-adressing-headers";
 
     public static final Object ADRESSING_FACTORY = "xfire-ws-adressing-factory";
 
     private List factories = new ArrayList();
 
     public AddressingInHandler()
     {
         super();
         setPhase(Phase.PRE_DISPATCH);
         createFactories();
     }
 
     public void createFactories()
     {
         factories.add(new AddressingHeadersFactory200508());
         factories.add(new AddressingHeadersFactory200408());
     }
 
     public List getFactories()
     {
         return factories;
     }
 
     public void invoke(MessageContext context)
         throws Exception
     {
         for (Iterator itr = factories.iterator(); itr.hasNext();)
         {
             AddressingHeadersFactory factory = (AddressingHeadersFactory) itr.next();
 
             InMessage msg = context.getInMessage();
             Element header = msg.getHeader();
 
             if (header != null && factory.hasHeaders(header))
             {
 
                 AddressingHeaders headers = null;
                 try
                 {
                     headers = factory.createHeaders(header);
                     msg.setProperty(ADRESSING_HEADERS, headers);
                     msg.setProperty(ADRESSING_FACTORY, factory);
 
                     context.setId(headers.getRelatesTo());
 
                     // Dispatch the service
                     Service service = getService(headers, context);
                     if (service != null)
                     {
                         context.setService(service);
 
                     }
                     else
                     {
                         // wsa:To can be not specified, so use service found by
                         // url
                         service = context.getService();
                     }
 
                     // Dispatch the Exchange and operation
                     OperationInfo op = context.getExchange().getOperation();
                     AddressingOperationInfo aop = AddressingOperationInfo.getOperationByInAction(service.getServiceInfo(), headers
                                 .getAction());
                     
                     // Check the client side case
                     if (aop == null)
                     {
                         aop = AddressingOperationInfo.getOperationByOutAction(service.getServiceInfo(), 
                                                                               headers.getAction());
                         if (aop != null) 
                         {
                             context.setId(headers.getRelatesTo());
                             return;
                         }
                     }
                     
                     if (aop == null)
                     {
                         throw new XFireFault("Action '" + headers.getAction() + "' was not found for service "
                                 + headers.getTo(), XFireFault.SENDER);
                     }
 
                     MessageExchange exchange = context.getExchange();
                     exchange.setOperation(aop.getOperationInfo());
 
                     EndpointReference faultTo = headers.getFaultTo();
                     OutMessage faultMsg = null;
                     if (faultTo != null)
                     {
                         faultMsg = processEPR(context, faultTo, aop, headers, factory);
                     }
                     else
                     {
                         faultMsg = createDefaultMessage(context, aop, headers, factory);
                     }
                     exchange.setFaultMessage(faultMsg);
 
                     EndpointReference replyTo = headers.getReplyTo();
                     OutMessage outMessage = null;
                     if (replyTo != null)
                     {
                         outMessage = processEPR(context, replyTo, aop, headers, factory);
                     }
                     else
                     {
                         outMessage = createDefaultMessage(context, aop, headers, factory);
                     }
                     exchange.setOutMessage(outMessage);
                 }
                 catch (XFireFault fault)
                 {
                     /* If this happens we've most likely received some invalid
                      * WS-Addressing headers, so lets try to make the best of it.
                      */
                     AbstractMessage faultMsg = context.getExchange().getFaultMessage();
                     AddressingHeaders outHeaders = (AddressingHeaders) faultMsg.getProperty(ADRESSING_HEADERS);
                     if (outHeaders == null)
                     {
                         outHeaders = new AddressingHeaders();
                         
                         if (headers != null)
                             outHeaders.setRelatesTo(headers.getMessageID());
                         
                         outHeaders.setAction(WSAConstants.WSA_200508_FAULT_ACTION);
                         faultMsg.setProperty(ADRESSING_HEADERS, outHeaders);
                         faultMsg.setProperty(ADRESSING_FACTORY, factory);
                     }
                     throw fault;
                 }
             }
         }
     }
 
     private OutMessage createDefaultMessage(MessageContext context,
                                             AddressingOperationInfo aoi,
                                             AddressingHeaders inHeaders,
                                             AddressingHeadersFactory factory)
     {
         OutMessage outMessage = context.getOutMessage();
 
         AddressingHeaders headers = new AddressingHeaders();
         headers.setTo(factory.getAnonymousUri());
         headers.setRelatesTo(inHeaders.getMessageID());
         
         headers.setAction(aoi.getOutAction());
         outMessage.setProperty(ADRESSING_HEADERS, headers);
         outMessage.setProperty(ADRESSING_FACTORY, factory);
 
         return outMessage;
     }
 
     /**
      * @param factory
      * @param addr
      * @return
      */
     private boolean isNoneAddress(AddressingHeadersFactory factory, String addr)
     {
         return factory.getNoneUri() != null && factory.getNoneUri().equals(addr);
     }
 
     /**
      * @param context
      * @param epr
      * @param aoi
      * @param inHeaders
      * @param factory
      * @return
      * @throws XFireFault
      * @throws Exception
      */
     protected OutMessage processEPR(MessageContext context,
                                     EndpointReference epr,
                                     AddressingOperationInfo aoi,
                                     AddressingHeaders inHeaders,
                                     AddressingHeadersFactory factory)
         throws XFireFault, Exception
     {
         String addr = epr.getAddress();
         OutMessage outMessage = null;
 
         boolean isFault = epr.getName().equals(WSAConstants.WSA_FAULT_TO);
         Transport t = null;
         Channel c = null;
         
         if (addr == null)
         {
             throw new XFireFault("Invalid ReplyTo address.", XFireFault.SENDER);
         }
         
         if (addr.equals(factory.getAnonymousUri()))
         {
             outMessage = new OutMessage(Channel.BACKCHANNEL_URI);
             c = context.getInMessage().getChannel();
             t = c.getTransport();
         }
         else
         {
             if (isNoneAddress(factory, addr))
             {
                 t = new DeadLetterTransport();
                 outMessage = new OutMessage(addr);
                 c = t.createChannel();
             }
             else
             {
                 outMessage = new OutMessage(addr);
                 t = context.getXFire().getTransportManager().getTransportForUri(addr);
                 c = t.createChannel();
             }
         }
         
         outMessage.setChannel(c);
         outMessage.setSoapVersion(context.getExchange().getInMessage().getSoapVersion());
 
         if (t == null)
         {
             throw new XFireFault("URL was not recognized: " + addr, XFireFault.SENDER);
         }
 
         AddressingHeaders headers = new AddressingHeaders();
         // Fault can have only refrenceParameters
         if (!isFault)
         {
             headers.setTo(addr);
             headers.setAction(aoi.getOutAction());
         }
         else
         {
             headers.setAction(WSAConstants.WSA_200508_FAULT_ACTION);
         }
        //headers.setMessageID("urn:uuid:" + guidGenerator.toString());
        headers.setMessageID("urn:uuid:" +new RandomGUID(false).toString());
         headers.setRelatesTo(inHeaders.getMessageID());
 
         Element refParam = epr.getReferenceParametersElement();
         if (refParam != null)
         {
             List refs = refParam.cloneContent();
 
             List params = new ArrayList();
             for (int i = 0; i < refs.size(); i++)
             {
                 if (refs.get(i) instanceof Element)
                 {
                     Element e = (Element) refs.get(i);
                     e.setAttribute(new Attribute(WSAConstants.WSA_IS_REF_PARAMETER, "true", epr.getNamespace()));
                     params.add(e);
                 }
 
             }
             headers.setReferenceParameters(params);
         }
 
         outMessage.setProperty(ADRESSING_HEADERS, headers);
         outMessage.setProperty(ADRESSING_FACTORY, factory);
 
         return outMessage;
     }
 
     protected Service getService(AddressingHeaders headers, MessageContext context)
     {
         String serviceName = null;
 
         if (headers.getTo() != null)
         {
             int i = headers.getTo().lastIndexOf('/');
             serviceName = headers.getTo().substring(i + 1);
         }
 
         if (serviceName == null)
         {
             return null;
         }
 
         return context.getXFire().getServiceRegistry().getService(serviceName);
     }
 
 }
