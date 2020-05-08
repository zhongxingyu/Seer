 package org.codehaus.xfire.client;
 
 import java.util.List;
 
 import javax.xml.stream.XMLStreamReader;
 
 import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.exchange.MessageExchange;
 import org.codehaus.xfire.exchange.OutMessage;
 import org.codehaus.xfire.fault.XFireFault;
 import org.codehaus.xfire.handler.HandlerPipeline;
 import org.codehaus.xfire.service.Binding;
 import org.codehaus.xfire.service.OperationInfo;
 import org.codehaus.xfire.service.binding.ServiceInvocationHandler;
 import org.codehaus.xfire.soap.Soap11;
 import org.codehaus.xfire.soap.Soap11Binding;
 import org.codehaus.xfire.soap.Soap12;
 import org.codehaus.xfire.soap.Soap12Binding;
 
 public class Invocation
 {
     static final String CONTEXT_KEY = "client.call";
     
     private Object[] response;
     private MessageContext context;
     private Exception fault;
     private Client client;
     
     public Invocation(Client client)
     {
         this.client = client;
     }
 
     public MessageContext getContext()
     {
         return context;
     }
 
     
     
     Object[] invoke(OperationInfo op, OutMessage msg) throws Exception
     {
     	try
         {
             
             
             msg.setChannel(client.getOutChannel());
             
             // TODO this should probably be in a seperate handler.
             // We'll have to address this when we add REST support.
             Binding binding = client.getBinding();
             if (binding instanceof Soap11Binding)
                 msg.setSoapVersion(Soap11.getInstance());
             else if (binding instanceof Soap12Binding)
                 msg.setSoapVersion(Soap12.getInstance());
             
             context = new MessageContext();
             context.setService(client.getService());
             context.setXFire(client.getXFire());
             context.setBinding(binding);
             context.setProperty(Client.CLIENT_MODE, Boolean.TRUE);
             context.setClient(client);
             context.setProperty(CONTEXT_KEY, this);
             
             MessageExchange exchange = new MessageExchange(context);
             exchange.setOperation(op);
             exchange.setOutMessage(msg);
             context.setCurrentMessage(msg);
             
             HandlerPipeline outPipe = new HandlerPipeline(client.getXFire().getOutPhases());
             outPipe.addHandlers(client.getXFire().getOutHandlers());
             outPipe.addHandlers(client.getOutHandlers());
             outPipe.addHandlers(client.getTransport().getOutHandlers());
             
             context.setOutPipeline(outPipe);
 
             ServiceInvocationHandler.writeHeaders(context, null);
             
             outPipe.invoke(context);
         }
         catch (Exception e1)
         {
             throw XFireFault.createFault(e1);
         }
         
         waitForResponse();
 
         if (fault != null)
         {
             Exception localFault = fault;
             fault = null;
             throw localFault;
         }
         
         Object[] localResponse = response;
         response = null;
 
         return localResponse;
 
     	
     }
     
     Object[] invoke(OperationInfo op, XMLStreamReader reader) throws Exception{
     	OutMessage msg = new OutMessage(client.getUrl());
     	msg.setSerializer(new RawDataSerializer(reader));
     	return invoke(op,msg);
     }
     
     
     Object[] invoke(OperationInfo op, Object[] params) throws Exception
     {
     	OutMessage msg = new OutMessage(client.getUrl());
     	msg.setBody(params);
     	return invoke(op,msg);
     }
     
     /**
      * Waits for a response from the service.
      */
     protected void waitForResponse()
     {
         /**
          * If this is an asynchronous channel, we'll need to sleep() and wait
          * for a response. Channels such as HTTP will have the response set
          * by the time we get to this point.
          */
         if (!client.getOutChannel().isAsync() || 
                 response != null ||
                 fault != null || 
                 !context.getExchange().getOperation().hasOutput())
         {
             return;
         }
         
         int count = 0;
         while (response == null && fault == null && count < client.getTimeout())
         {
             try
             {
                 Thread.sleep(50);
                 count += 50;
             }
             catch (InterruptedException e)
             {
                 break;
             }
         }
        
        if(response == null && fault == null)
            throw new XFireRuntimeException("Invocation timeout when waiting for resonse.");
     }
 
     public void receive(Object response)
     {
         this.response = ((List) response).toArray();
     }
 
     public void receiveFault(Exception fault)
     {
         this.fault = fault;
     }
 }
