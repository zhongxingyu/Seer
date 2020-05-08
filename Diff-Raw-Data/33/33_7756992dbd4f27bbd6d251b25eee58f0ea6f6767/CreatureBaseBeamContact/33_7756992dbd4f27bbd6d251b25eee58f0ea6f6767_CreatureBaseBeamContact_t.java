 package net.qmat.qmhh.contacts;
 
 import net.qmat.qmhh.models.creatures.CreatureBase;
 import net.qmat.qmhh.models.Hand.Beam;
 import org.jbox2d.dynamics.contacts.Contact;
 
 public class CreatureBaseBeamContact extends ContactLogic {
 	
 	public void beginContact(Contact contact, Object objA, Object objB) {
 		CreatureBase creature = (CreatureBase)objA;
 		if(creature.stage > 0) {
 			// TODO: this will cause trouble when the hand is suddenly removed asynchronously!
 			Beam beam = (Beam)objB;
 			beam.hand.addCreature(creature);
			System.out.println("contact");
 		}
 	}
 	
 	public void endContact(Contact contact, Object objA, Object objB) {
 		CreatureBase creature = (CreatureBase)objA;
 		if(creature.stage > 0) {
 			// TODO: this will cause trouble when the hand is suddenly removed asynchronously!
 			Beam beam = (Beam)objB;
 			beam.hand.removeCreature(creature);
			System.out.println("x");
 		}
 	}
 }
