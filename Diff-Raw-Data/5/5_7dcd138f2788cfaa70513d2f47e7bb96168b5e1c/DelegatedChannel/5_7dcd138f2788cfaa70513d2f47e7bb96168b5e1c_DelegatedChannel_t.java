 package org.codehaus.xfire.transport;
 
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.XFireException;
 import org.codehaus.xfire.exchange.InMessage;
 import org.codehaus.xfire.exchange.OutMessage;
 
 /**
  * Wraps another channel so it is easy to add another layer of functionality.
  * 
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  */
 public class DelegatedChannel
     implements Channel
 {
     private Channel channel;
     private Transport transport;
     
     public DelegatedChannel(Transport transport, Channel channel)
     {
         this.transport = transport;
         this.channel = channel;
     }
 
     public void open()
         throws Exception
     {
         channel.open();
     }
 
     public void send(MessageContext context, OutMessage message)
         throws XFireException
     {
         channel.send(context, message);
     }
 
     public void receive(MessageContext context, InMessage message)
     {
         channel.receive(context, message);
     }
 
     public void setEndpoint(ChannelEndpoint receiver)
     {
         channel.setEndpoint(receiver);
     }
 
     public void close()
     {
         channel.close();
     }
 
     public Transport getTransport()
     {
         return transport;
     }
 
     public String getUri()
     {
         return channel.getUri();
     }

    public ChannelEndpoint getEndpoint()
    {
        return channel.getEndpoint();
    }
 }
