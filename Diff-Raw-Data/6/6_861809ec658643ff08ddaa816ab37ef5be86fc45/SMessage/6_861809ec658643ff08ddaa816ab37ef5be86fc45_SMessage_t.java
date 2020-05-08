 package org.correttouml.uml2zot.semantics.sequencediagram;
 
 import org.correttouml.uml.diagrams.classdiagram.Clock;
 import org.correttouml.uml.diagrams.sequencediagram.Message;
 import org.correttouml.uml.diagrams.sequencediagram.MessageParameter;
 import org.correttouml.uml.diagrams.sequencediagram.MessageType;
 import org.correttouml.uml2zot.semantics.events.SClockTickEvent;
 import org.correttouml.uml2zot.semantics.util.bool.Iff;
import org.correttouml.uml2zot.semantics.util.bool.Not;
 import org.correttouml.uml2zot.semantics.util.bool.Or;
 import org.correttouml.uml2zot.semantics.util.trio.Predicate;
 import org.correttouml.uml2zot.semantics.util.trio.Since;
import org.correttouml.uml2zot.semantics.util.trio.Since_ei;
 
 
 public class SMessage {
 
 	private Message mades_message;
 
 	public SMessage(Message m) {
 		this.mades_message=m;
 	}
 	
 	public Predicate getPredicate(){
 		return new Predicate("MESSAGE"+this.mades_message.getUMLId().replace("-", "_"));
 	}
 
 	public String getSemantics(){
         String sem = "";
         
         Predicate message=this.getPredicate();
         Predicate message_start=new SMessageStart(this.mades_message.getMessageStartEvent()).getPredicate();
         Predicate message_end=new SMessageEnd(this.mades_message.getMessageEndEvent()).getPredicate();
         
         //Message predicate definition
        sem = sem + new Iff(message, new Or(message_start, new Since_ei(new Not(message_end), message_start))) + "\n";
         
         //Message parameter semantics
         for (MessageParameter mp : this.mades_message.getParameters()) {
         	sem=sem+new SMessageParameter(mp).getSemantics(mades_message.getMessageEndEvent().getLifeline().getObject())+"\n";
         }
 
         //Message start and message end holds at the same time instant
         if(this.mades_message.getMessageType()==MessageType.INSTANTANEOUS){
             sem = sem + new Iff(message_start, message_end) + "\n";
         }else if(this.mades_message.getMessageType()==MessageType.RECURSIVE){
             //Nothing to do
         }else if(this.mades_message.getMessageType()==MessageType.DELAYED){
         	//TODO: Semantics for delayed messages
         }
         
         //Timed event semantics
         if(this.mades_message.hasTimedEventStereotype()){
         	Clock c=mades_message.getAssociatedClock();
         	sem = sem + new Iff(message_start, new SClockTickEvent(c.getClockTickEvent()).getPredicate()) + "\n";
         }
 
         return sem;
 	}
 }
