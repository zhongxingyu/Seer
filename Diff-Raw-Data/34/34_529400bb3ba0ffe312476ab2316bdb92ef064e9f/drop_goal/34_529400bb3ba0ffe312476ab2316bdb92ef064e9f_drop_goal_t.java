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
 //----------------------------------------------------------------------------
 
 
 package jason.stdlib;
 
 import jason.JasonException;
 import jason.asSemantics.ActionExec;
 import jason.asSemantics.Circumstance;
 import jason.asSemantics.DefaultInternalAction;
 import jason.asSemantics.Event;
 import jason.asSemantics.Intention;
 import jason.asSemantics.TransitionSystem;
 import jason.asSemantics.Unifier;
 import jason.asSyntax.Literal;
 import jason.asSyntax.Term;
 import jason.asSyntax.Trigger;
 
 /**
   <p>Internal action:
   <b><code>.drop_goal(<i>G</i>,<i>R</i>)</code></b>.
   
   <p>Description: removes the goal <i>G</i> of the agent. <i>G</i> is a goal
   if there is a trigerring event <code>+!G</code> in any plan within an
   intention; just note that intentions can be suspended hence appearing
   in E, PA, or PI as well.
 
   <p>If <i>R</i> is <code>true</code>, the intention where the goal appears is
   updated as if the goal had already been successfuly achieved.  If <i>R</i>
   is <code>false</code>, the intention is updated as if the plan for that goal
   had failed, and an event <code>-!G</code> is generated.
 
   <p>Example:<ul> 
 
   <li> <code>.drop_goal(go(1,3), true)</code>: stops any attempt to achieve
   <code>!go(1,3)</code> as if it had already been achieved.
 
   <li> <code>.drop_goal(go(1,3), false)</code>: stops any attempt to achieve
   <code>!go(1,3)</code> as if it had failed.
 
   </ul>
 
   (Note: this internal action was introduced in a DALT 2006 paper)
 
   @see jason.stdlib.current_intention
   @see jason.stdlib.desire
   @see jason.stdlib.drop_all_desires
   @see jason.stdlib.drop_desire
   @see jason.stdlib.drop_all_intentions
   @see jason.stdlib.drop_intention
   @see jason.stdlib.intend
 
  */
 public class drop_goal extends DefaultInternalAction {
     
     @Override
     public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
         try {
         	args[0].apply(un);
        	args[1].apply(un);
 
            drop(ts, (Literal)args[0], args[1].equals(Literal.LTrue), un);
             return true;
         } catch (ArrayIndexOutOfBoundsException e) {
             throw new JasonException("The internal action 'drop_goal' has not received two arguments.");
         } catch (Exception e) {
             e.printStackTrace();
             throw new JasonException("Error in internal action 'drop_goal': " + e);
         }
     }
     
     public void drop(TransitionSystem ts, Literal l, boolean success, Unifier un) throws JasonException{
         Trigger g = new Trigger(Trigger.TEAdd, Trigger.TEAchvG, l);
         Circumstance C = ts.getC();
         
         for (Intention i: C.getIntentions()) {
             if (dropIntention(i, g, success, ts, un) > 1) {
                 C.removeIntention(i);
             }
         }
         
         // dropping the current intention?
         dropIntention(C.getSelectedIntention(), g, success, ts, un);
             
         // dropping G in Events
         for (Event e: C.getEvents()) {
             Intention i = e.getIntention();
             if (dropIntention(i, g, success, ts, un) > 1) {
                 C.removeEvent(e);
             }
         }
         
         // dropping from Pending Actions
         for (ActionExec a: C.getPendingActions().values()) {
             Intention i = a.getIntention();
             int r = dropIntention(i, g, success, ts, un);
             if (r > 0) { // i was changed
                 C.dropPendingIntention(i); // remove i from PI
                 if (r == 1) { // i must continue running
                     C.addIntention(i); // and put the intention back in I
                 }
             }
         }
         
         // dropping from Pending Intentions
         for (Intention i: C.getPendingIntentions().values()) {
             int r = dropIntention(i, g, success, ts, un);
             if (r > 0) { // i was changed
                 C.dropPendingIntention(i); // remove i from PI
                 if (r == 1) { // intention i must continue running
                     C.addIntention(i); // and put the intention back in I
                 }
             }
         }
     }
     
     private int dropIntention(Intention i, Trigger g, boolean success, TransitionSystem ts, Unifier un) throws JasonException {
         if (i != null && i.dropGoal(g, un)) {
             if (success) {
                 // continue the intention
                 i.peek().removeCurrentStep();
                 ts.applyClrInt(i);
                 return 1;
             } else {
                 // generate failure event
                 Event failEvent = null;
                 if (!i.isFinished()) {
                     failEvent = ts.findEventForFailure(i, i.peek().getTrigger());
                 }
                 if (failEvent != null) {
                     ts.getC().addEvent(failEvent);
                     ts.getLogger().warning("'.drop_goal' is generating a goal deletion event: " + failEvent.getTrigger());
                     return 2;
                 } else {
                     ts.getLogger().warning("'.drop_goal' is removing an intention:\n" + i);
                     return 3;
                 }
             }
         }
         return 0;        
     }
 }
