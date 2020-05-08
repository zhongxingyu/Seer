 package org.codehaus.xfire.client;
 
 import java.util.Collection;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.addressing.AddressingInHandler;
 import org.codehaus.xfire.handler.AbstractHandler;
 import org.codehaus.xfire.handler.Phase;
 
 public class CorrelatorHandler extends AbstractHandler
 {
     private static final Log log = LogFactory.getLog(Client.class);
 
     private Collection calls;
     private Correlator correlator;
     
     public CorrelatorHandler(Collection calls)
     {
         super();
         
         setPhase(Phase.PRE_DISPATCH);
         after(AddressingInHandler.class.getName());
         
         this.calls = calls;
     }
 
     public void invoke(MessageContext context)
         throws Exception
     {
         log.debug("Correlating context with ID " + context.getId());
         
         Invocation invocation = correlator.correlate(context, calls);
         
         if (invocation == null)
         {
             log.info("No correlated invocation was found.");
             return;
         }
         
        
         if (context != invocation.getContext())
         {
             context.getExchange().setOperation(invocation.getContext().getExchange().getOperation());
             context.getExchange().setOutMessage(invocation.getContext().getExchange().getOutMessage());
       
            context.setProperty(Client.CLIENT_MODE, invocation.getContext().getProperty(Client.CLIENT_MODE));
         }
         
         if (invocation != null)
         {
             log.debug("Found correlated context with ID " + context.getId());
             context.getInPipeline().addHandler(new ClientReceiveHandler(invocation));
         }
     }
 
     public Correlator getCorrelator()
     {
         return correlator;
     }
 
     public void setCorrelator(Correlator correlator)
     {
         this.correlator = correlator;
     }
 }
