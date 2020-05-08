 //----------------------------------------------------------------------------
 // Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
 // 
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 // 
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 // 
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 // 
 // To contact the authors:
 // http://www.dur.ac.uk/r.bordini
 // http://www.inf.furb.br/~jomi
 //
 //----------------------------------------------------------------------------
 
 
 package jason.asSemantics;
 
 
 import jason.asSyntax.Literal;
 import jason.asSyntax.Trigger;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Logger;
 
 public final class BDIlogic {
 
 	static private Logger logger = Logger.getLogger(BDIlogic.class.getName());
 
     public static final boolean Bel(TransitionSystem ts, Literal l) {
         return ts.ag.believes(l,new Unifier()) != null;
     }
 
     /** 
      * Checks if <i>l</i> is a desire: <i>l</i> is a desire either 
      * if there is an event with +!l as triggering event or it is an Intention. 
      */
     public static final boolean Des(TransitionSystem ts, Literal l, Unifier un) {
     	Trigger teFromL = new Trigger(Trigger.TEAdd,Trigger.TEAchvG,l);
     	//logger.log(Level.SEVERE,"Des: "+l+un+ts.C.E+ts.C.I);
 
 		// need to check the slected event in this cycle!!! (already removed from E)
 		if(ts.C.SE!=null) {
 			Trigger t = ts.C.SE.trigger;
 			if (ts.C.SE.intention!=Intention.EmptyInt) {
 				t = (Trigger)t.clone();
 				((IntendedMeans)ts.C.SE.intention.peek()).unif.apply(t.getLiteral());
 			}
 			//logger.log(Level.SEVERE,"Des: "+t+" unif "+teFromL);
         	if(un.unifies(t,teFromL)) {
         		return true;
         	}
 		}
 
 		for(Iterator i=ts.C.getEvents().iterator(); i.hasNext(); ) {
             Event ei = (Event)i.next();
             Trigger t = (Trigger)ei.trigger;
             if (ei.intention!=Intention.EmptyInt) {
                 t = (Trigger)t.clone();
                 ((IntendedMeans)ei.intention.peek()).unif.apply(t.getLiteral());
             }
             //logger.log(Level.SEVERE,"Des: "+t+" unif "+teFromL);
             if(un.unifies(t,teFromL)) {
                 return true;
             }
         }
 
         return Int(ts,l,un); // Int subset Des (see formal definitions)
     }
 
     /** 
      * Checks if <i>l</i> is an intention: <i>l</i> is an intention
      * if there is a trigerring event +!l in any plan within an intention;
      * just note that intentions can be suspended and appear in E or PA as well.
      */
     public static final boolean Int(TransitionSystem ts, Literal l, Unifier un) {
         Trigger g = new Trigger(Trigger.TEAdd, Trigger.TEAchvG,l);
         //logger.log(Level.SEVERE,"Entering Int: "+ts.C.I);
 
         // need to check the intention in the slected event in this cycle!!! (already removed from E)
         if (ts.C.SE!=null) {
         	//logger.log(Level.SEVERE,"Int: "+g+" unif "+ts.C.SE);
 			if (ts.C.SE.intention!=null)
 				if (ts.C.SE.intention.hasTrigger(g,un))
 					return true;
         }
 
         // need to check the slected intention in this cycle!!!
         if (ts.C.SI!=null) {
         	//logger.log(Level.SEVERE,"Int: "+g+" unif "+ts.C.SI);
         	if (ts.C.SI.hasTrigger(g,un))
         		return true;
         }
 
         // intention may be suspended in E
         for(Iterator i=ts.C.getEvents().iterator(); i.hasNext(); ) {
         	//logger.log(Level.SEVERE,"Int: "+g+" unif "+ts.C.SI);
             Event t = (Event)i.next(); 
             if (t.intention != null && t.intention.hasTrigger(g,un))
                 return true;
         }
         
         // intention may be suspended in PA! (in the new semantics)
         if (ts.C.hasPendingAction()) {
             for(Iterator i=ts.C.getPendingActions().values().iterator(); i.hasNext(); ) {
             	//logger.log(Level.SEVERE,"Int: "+g+" unif "+ts.C.SI);
                 Object o = i.next();
                 Intention intention = null;
                 try {
                     intention = ((ActionExec)o).getIntention();
                 } catch (Exception e) {
                     intention = (Intention)o;
                 } 
                 if (intention.hasTrigger(g,un))
                     return true;
             }
         }
         synchronized (ts.C.getIntentions()) {
             for(Iterator i=ts.C.getIntentions().iterator(); i.hasNext(); ) {
             	//logger.log(Level.SEVERE,"Int: "+g+" unif "+ts.C.SI);
         		if (((Intention)i.next()).hasTrigger(g,un))
         			return true;
             }
         }
 
         return false;
     }
 
     // Changing the Agent's Circumstance!!!
 
     /**
      * This changes the agent's circumstance. Currently what it does
      * is simply to change all +!l to -!l in events which would give
      * true for Des(l) in the whole set of events.
      * IMPORTANT: unlike Des() this only alters literals explicitly
      * desired (rather than intended), that is, it does NOT consider
      * intentions. You should use both dropDes() AND dropInt() to
      * remove all desires and intentions of l.
      */
     public static final void dropDes(TransitionSystem ts, Literal l, Unifier un) {
         Event e = new Event(new Trigger(Trigger.TEAdd,Trigger.TEAchvG,l),Intention.EmptyInt);
         for(Iterator i=ts.C.getEvents().iterator(); i.hasNext(); ) {
             Event ei = (Event)i.next();
             Trigger t = (Trigger)ei.trigger;
             if (ei.intention!=Intention.EmptyInt) {
                 t = (Trigger)t.clone();
                 ((IntendedMeans)ei.intention.peek()).unif.apply(t.getLiteral());
             }
             if(un.unifies(t,e.trigger)) {
                 t.setTrigType(Trigger.TEDel); // Just changing "+!g" to "-!g" !!!
             }
         }
     }
 
     /**
      * This changes the agent's circumstance. It removes an intention
      * from I, E or PA and use that intention in a new event that is
      * added to E with triggering event -!l. This is EXPERIMENTAL, in
      * particular for intentions suspended in PA, this is bound to
      * create problems at the moment.
      */
     public static final void dropInt(TransitionSystem ts, Literal l, Unifier un) {
         Trigger g = new Trigger(Trigger.TEAdd,Trigger.TEAchvG,l);
 
         synchronized (ts.C.getIntentions()) {
             for(Iterator j=ts.C.getIntentions().iterator(); j.hasNext(); ) {
             	Intention i = (Intention) j.next();
                 if (i.hasTrigger(g,un)) {
                    j.remove();
                    addDropEvent(g,ts);
                    // TODO: what if we have 2 intentions?
                    // 2 identical events are generated!
                    // idem for E/PA
                    // Maybe do not use g, but a copy of g with un.apply
                 }
             }
         }
         
         // intention may be suspended in E
         List dropped = new LinkedList();
         for(Iterator j=ts.C.getEvents().iterator(); j.hasNext(); ) {
         	Intention i = ((Event)j.next()).intention;
             if (i != null && i.hasTrigger(g,un)) {
                 j.remove();
                dropped.add(g);
             }
         }
         // we must add the events only after removing (add events while removing cause a loop)
         Iterator id = dropped.iterator();
         while (id.hasNext()) {
            addDropEvent((Trigger)id.next(), ts);
         }
         
         // intention may be suspended in PA! (in the new semantics)
         if (ts.C.hasPendingAction()) {
             for(Iterator j=ts.C.getPendingActions().values().iterator(); j.hasNext(); ) {
                 // PA may contain ActionExec or Intention
                 Object o = j.next();
             	Intention i = null;
                 try {
                     i = ((ActionExec)o).getIntention();
                 } catch (Exception e) {
                     i = (Intention)o;
                 } 
         		// TODO CAREFUL: The semantics for this isn't well defined yet.
         		// The goal deletion on top of the intention will not get to know
         		// the result of the action, as it is removed from the PA set!
                	// If left in PA, the action won't be the the top of
                	// the stack (that might cause problems?)
                 if (i.hasTrigger(g,un)) {
                     j.remove();
                    addDropEvent(g,ts);
                 }
             }
         }
     }
 
    private static void addDropEvent(Trigger g, TransitionSystem ts) {
        Trigger ng = (Trigger) g.clone();
        ng.setTrigType(Trigger.TEDel);
        if (ts.getAg().getPS().isRelevant(ng)) {
            ts.C.addEvent(new Event(ng, Intention.EmptyInt));
        }
    }
    
     /**
      * This changes the agent's circumstance by simply emptying
      * the whole set of events (E). IMPORTANT: note that this
      * is different from droping one desires, in which case a
      * goal deletion event is generated.
      */
     public static final void dropAllDes(TransitionSystem ts) {
         ts.C.clearEvents();
     }
 
     /**
      * This changes the agent's circumstance by simply emptying
      * the whole set of intentions (I). IMPORTANT: note that this
      * is different from droping one intention, in which case a
      * goal deletion event is generated.
      */
     public static final void dropAllInt(TransitionSystem ts) {
         ts.C.clearIntentions();
     }
 
 }
