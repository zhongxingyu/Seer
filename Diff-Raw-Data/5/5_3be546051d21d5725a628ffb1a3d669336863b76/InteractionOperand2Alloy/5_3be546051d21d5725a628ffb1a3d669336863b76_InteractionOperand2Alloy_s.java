 package uk.ac.bham.cs.sdsts.core.sitra_rules;
 
 import java.util.ArrayList;
 import org.eclipse.uml2.uml.InteractionFragment;
 import org.eclipse.uml2.uml.InteractionOperand;
 import org.eclipse.uml2.uml.Message;
 import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
 import org.eclipse.uml2.uml.internal.impl.InteractionOperandImpl;
 import uk.ac.bham.cs.sdsts.Alloy.AAttr;
 import uk.ac.bham.cs.sdsts.Alloy.ASig;
 import uk.ac.bham.cs.sdsts.core.synthesis.AlloyModel;
 import uk.ac.bham.sitra.Rule;
 import uk.ac.bham.sitra.RuleNotFoundException;
 import uk.ac.bham.sitra.Transformer;
 
 @SuppressWarnings({ "rawtypes", "restriction" })
 public class InteractionOperand2Alloy implements Rule {
 
 	@Override
 	public boolean check(Object source) {
 		if (source instanceof InteractionOperandImpl)
 			return true;
 		else
 			return false;
 	}
 
 	@Override
 	public Object build(Object source, Transformer t) {
 		try {
 			InteractionOperand interactionOperand = (InteractionOperand) source;
 			String currentSD = AlloyModel.getInstance().getSD();
 			String currentSD_ = currentSD + "_";
 			// add abstract for event
 			// one sig abstract event{isbefore: set event}
 			ASig eventAbstract = AlloyModel.getInstance().getSig("EVENT");
 			eventAbstract.set_attr(AAttr.ABSTRACT);
 			eventAbstract.AddField("ISBEFORE", eventAbstract.setOf());	
 			eventAbstract.zone = "abstract";		
 			
 			// add abstract for InteractionOperand
 			// abstract sig INTERACTIONOPERAND {}
 			ASig interactionOperandAbstract = AlloyModel.getInstance().getSig("INTERACTIONOPERAND");
 			interactionOperandAbstract.set_attr(AAttr.ABSTRACT);
 			interactionOperandAbstract.AddField("cov", eventAbstract.setOf());
 			interactionOperandAbstract.zone = "abstract";
 			
 			AlloyModel.getInstance().addFact("//all event can be covered by at most one operand\nfact{all _E:EVENT | lone _OP:INTERACTIONOPERAND | _E in _OP.cov}").zone = "other";
 			
 			// add the interactionOperand
 			// one sig SDid_InteractionName extends INTERACTION {cover1: Message1, cover2: Message2 ...}
 			ASig interactionOperandSig = AlloyModel.getInstance().getSig(currentSD_ + interactionOperand.getName());
 			interactionOperandSig.set_attr(AAttr.ONE);
 			interactionOperandSig.set_parent(interactionOperandAbstract);
 			interactionOperandSig.zone = "operand";
 			
 			// add facts
 			// for every message M(j) after M(i) {
 			ArrayList<Message> messages = new ArrayList<Message>();
 			for (InteractionFragment interactionFragment : interactionOperand.getFragments()) {
 				if(interactionFragment instanceof MessageOccurrenceSpecification){
 					MessageOccurrenceSpecification event = (MessageOccurrenceSpecification) interactionFragment;
 					if(!messages.contains(event.getMessage())){
 						t.transform(event.getMessage());
 						messages.add(event.getMessage());
 					}
 					// event that covered by interaction
					ASig eventSig = AlloyModel.getInstance().getSig(currentSD_ + interactionFragment.getName());
					eventSig.set_name(interactionOperandSig + "_" + event);
 					AlloyModel.getInstance().addFact("%s in %s.cov", eventSig, interactionOperandSig).zone = "cover"; 
 				}
 			}
 			for (int i = 0; i < messages.size() - 1; i++) {
 				Message message1 = messages.get(i);
 				for (int j = i+1; j < messages.size(); j++) {
 					Message message2 = messages.get(j);
 					MessageOccurrenceSpecification m1send = (MessageOccurrenceSpecification) message1.getSendEvent();
 					MessageOccurrenceSpecification m1rec = (MessageOccurrenceSpecification) message1.getReceiveEvent();
 					MessageOccurrenceSpecification m2send = (MessageOccurrenceSpecification) message2.getSendEvent();
 					MessageOccurrenceSpecification m2rec = (MessageOccurrenceSpecification) message2.getReceiveEvent();
 					
 					ASig m1sendASig = AlloyModel.getInstance().getSig(currentSD_ + m1send.getName());
 					ASig m1recASig = AlloyModel.getInstance().getSig(currentSD_ + m1rec.getName());
 					ASig m2sendASig = AlloyModel.getInstance().getSig(currentSD_ + m2send.getName());
 					ASig m2recASig = AlloyModel.getInstance().getSig(currentSD_ + m2rec.getName());
 					
 					if(j == i + 1){
 						AlloyModel.getInstance().addFact("%s in %s.ISBEFORE", m2sendASig, m1sendASig).zone = "order";
 						AlloyModel.getInstance().addFact("%s in %s.ISBEFORE", m2recASig, m1recASig).zone = "order";
 					}
 					AlloyModel.getInstance().addFact("%s !in %s.ISBEFORE", m2sendASig, m1recASig).zone = "order";
 					AlloyModel.getInstance().addFact("%s !in %s.ISBEFORE", m1recASig, m2sendASig).zone = "order";
 				}						
 			}
 			
 			return interactionOperandSig;
 			
 		} catch (RuleNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@Override
 	public void setProperties(Object target, Object source, Transformer t) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
