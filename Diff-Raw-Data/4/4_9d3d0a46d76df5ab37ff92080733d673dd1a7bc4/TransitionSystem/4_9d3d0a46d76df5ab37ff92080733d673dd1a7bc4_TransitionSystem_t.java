 // ----------------------------------------------------------------------------
 // Copyright (C) 2003 Rafael H. Bordini, Jomi F. Hubner, et al.
 //
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 //
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 // Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 //
 // To contact the authors:
 // http://www.inf.ufrgs.br/~bordini
 // http://www.das.ufsc.br/~jomi
 //
 //----------------------------------------------------------------------------
 
 package jason.asSemantics;
 
 import jason.JasonException;
 import jason.RevisionFailedException;
 import jason.architecture.AgArch;
 import jason.asSyntax.ASSyntax;
 import jason.asSyntax.Atom;
 import jason.asSyntax.BinaryStructure;
 import jason.asSyntax.InternalActionLiteral;
 import jason.asSyntax.ListTerm;
 import jason.asSyntax.Literal;
 import jason.asSyntax.LiteralImpl;
 import jason.asSyntax.LogicalFormula;
 import jason.asSyntax.NumberTermImpl;
 import jason.asSyntax.Plan;
 import jason.asSyntax.PlanBody;
 import jason.asSyntax.PlanLibrary;
 import jason.asSyntax.StringTermImpl;
 import jason.asSyntax.Structure;
 import jason.asSyntax.Term;
 import jason.asSyntax.Trigger;
 import jason.asSyntax.VarTerm;
 import jason.asSyntax.PlanBody.BodyType;
 import jason.asSyntax.Trigger.TEOperator;
 import jason.asSyntax.Trigger.TEType;
 import jason.asSyntax.parser.ParseException;
 import jason.bb.BeliefBase;
 import jason.runtime.Settings;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class TransitionSystem {
 
     public enum State { StartRC, SelEv, RelPl, ApplPl, SelAppl, FindOp, AddIM, ProcAct, SelInt, ExecInt, ClrInt }
     
     private Logger        logger     = null;
 
     private Agent         ag         = null;
     private AgArch        agArch     = null;
     private Circumstance  C          = null;
     private Settings      setts      = null;
     private State         step       = State.StartRC; // first step of the SOS                                                                                                
     private int           nrcslbr    = Settings.ODefaultNRC; // number of reasoning cycles since last belief revision                                                                                                             
     
     private List<GoalListener>  goalListeners = null;
     
     // both configuration and configuration' point to this
     // object, this is just to make it look more like the SOS
     private TransitionSystem      confP;
     private TransitionSystem      conf;
 
     private Queue<Runnable> taskForBeginOfCycle = new ConcurrentLinkedQueue<Runnable>();
     
     public TransitionSystem(Agent a, Circumstance c, Settings s, AgArch ar) {
         ag     = a;
         C      = c;
         agArch = ar;
 
         if (s == null)
             setts = new Settings();
         else
             setts = s;
 
         if (C == null)
             C = new Circumstance();
         
         // we need to initialise this "aliases"
         conf = confP = this;
 
         nrcslbr = setts.nrcbp(); // to do BR to start with
 
         setLogger(agArch);
         if (setts != null)
             logger.setLevel(setts.logLevel());
         
         if (a != null)
             a.setTS(this);
         
         if (ar != null)
             ar.setTS(this);
     }
 
     public void setLogger(AgArch arch) {
         if (arch != null)
             logger = Logger.getLogger(TransitionSystem.class.getName() + "." + arch.getAgName());
         else
             logger = Logger.getLogger(TransitionSystem.class.getName());
     }
     
     // ---------------------------------------------------------
     //    Goal Listeners support methods
     
     private Map<GoalListener,CircumstanceListener> listenersMap; // map the circumstance listeners created for the goal listeners, used in remove goal listener
     
     /** adds an object that will be notified about events on goals (creation, suspension, ...) */
     public void addGoalListener(final GoalListener gl) {
         if (goalListeners == null) {
             goalListeners = new ArrayList<GoalListener>();
             listenersMap  = new HashMap<GoalListener, CircumstanceListener>();
         } else { 
             // do not install two MetaEventGoalListener
             for (GoalListener g: goalListeners)
                 if (g instanceof GoalListenerForMetaEvents)
                     return;
         }
         
         // we need to add a listener in C to map intention events to goal events
         CircumstanceListener cl = new CircumstanceListener() {
             
             public void intentionDropped(Intention i) {
                 for (IntendedMeans im: i.getIMs()) 
                     if (im.getTrigger().isAddition() && im.getTrigger().isGoal()) 
                         gl.goalFinished(im.getTrigger());                         
             }
             
             public void intentionSuspended(Intention i, String reason) {
                 for (IntendedMeans im: i.getIMs()) 
                     if (im.getTrigger().isAddition() && im.getTrigger().isGoal()) 
                         gl.goalSuspended(im.getTrigger(), reason);                         
             }
             
             public void intentionResumed(Intention i) {
                 for (IntendedMeans im: i.getIMs()) 
                     if (im.getTrigger().isAddition() && im.getTrigger().isGoal()) 
                         gl.goalResumed(im.getTrigger());                         
             }
             
             public void eventAdded(Event e) {
                 if (e.getTrigger().isAddition() && e.getTrigger().isGoal())
                     gl.goalStarted(e);
             }
             
             public void intentionAdded(Intention i) {  }
         };
         C.addEventListener(cl);
         listenersMap.put(gl,cl);
 
         goalListeners.add(gl);
     }
     
     public boolean hasGoalListener() {
         return goalListeners != null && !goalListeners.isEmpty();
     }
     
     public List<GoalListener> getGoalListeners() {
         return goalListeners;
     }
     
     public boolean removeGoalListener(GoalListener gl) {
         CircumstanceListener cl = listenersMap.get(gl);
         if (cl != null) C.removeEventListener(cl);
         return goalListeners.remove(gl);
     }
 
     /** ******************************************************************* */
     /* SEMANTIC RULES */
     /** ******************************************************************* */
     
     private void applySemanticRule() throws JasonException {
         // check the current step in the reasoning cycle
         // only the main parts of the interpretation appear here
         // the individual semantic rules appear below
 
         switch (step) {
         case StartRC:   applyProcMsg(); break;
         case SelEv:     applySelEv(); break;
         case RelPl:     applyRelPl();  break;
         case ApplPl:    applyApplPl(); break;
         case SelAppl:   applySelAppl(); break;
         case FindOp:    applyFindOp(); break;
         case AddIM:     applyAddIM(); break;
         case ProcAct:   applyProcAct(); break;
         case SelInt:    applySelInt(); break;
         case ExecInt:   applyExecInt(); break;
         case ClrInt:    confP.step = State.StartRC;
                         applyClrInt(conf.C.SI);
                         break;
         }
     }
 
     // the semantic rules are referred to in comments in the functions below
 
     private void applyProcMsg() throws JasonException {
         confP.step = State.SelEv;
         if (!conf.C.MB.isEmpty()) {
             Message m = conf.ag.selectMessage(conf.C.MB);
             if (m == null) return;
             
             // get the content, it can be any term (literal, list, number, ...; see ask)
             Term content = null;
             if (m.getPropCont() instanceof Term) {
                 content = (Term)m.getPropCont();
             } else {
                 try {
                     content = ASSyntax.parseTerm(m.getPropCont().toString());
                 } catch (ParseException e) {
                     logger.warning("The content of the message '"+m.getPropCont()+"' is not a term!");
                     return;
                 }
             }
 
             // check if an intention was suspended waiting this message
             Intention intention = null;
             if (m.getInReplyTo() != null) {
                 intention = getC().removePendingIntention(m.getInReplyTo());
             }
             // is it a pending intention?
             if (intention != null) {
                 // unify the message answer with the .send fourth argument.
                 // the send that put the intention in Pending state was
                 // something like
                 //    .send(ag1,askOne, value, X)
                 // if the answer was tell 3, unifies X=3
                 // if the answer was untell 3, unifies X=false
                 Structure send = (Structure)intention.peek().removeCurrentStep();
                 if (m.isUnTell() && send.getTerm(1).toString().equals("askOne")) {
                     content = Literal.LFalse;
                 }
                 if (intention.peek().getUnif().unifies(send.getTerm(3), content)) {
                     getC().resumeIntention(intention);
                 } else {
                     generateGoalDeletion(intention, JasonException.createBasicErrorAnnots("ask_failed", "reply of an ask message ('"+content+"') does not unify with fourth argument of .send ('"+send.getTerm(3)+"')"));
                 }
 
                 // the message is not an ask answer
             } else if (conf.ag.socAcc(m)) {
 
                 // generate an event
                 String sender = m.getSender();
                 if (sender.equals(agArch.getAgName()))
                     sender = "self";
                 Literal received = new LiteralImpl("kqml_received").addTerms(
                         new Atom(sender),
                         new Atom(m.getIlForce()),
                         content,
                         new Atom(m.getMsgId()));
 
                 updateEvents(new Event(new Trigger(TEOperator.add, TEType.achieve, received), Intention.EmptyInt));
             }
         }
     }
 
     private void applySelEv() throws JasonException {
         
         // Rule for atomic, if there is an atomic intention, do not select event
         if (C.hasAtomicIntention()) {
             confP.step = State.ProcAct; // need to go to ProcAct to see if an atomic intention received a feedback action
             return;            
         }
         
         
         if (conf.C.hasEvent()) {
             // Rule for atomic, events from atomic intention has priority
             confP.C.SE = C.removeAtomicEvent();
             if (confP.C.SE != null) {
                 confP.step = State.RelPl;
                 return;
             }
 
             // Rule SelEv1
             confP.C.SE = conf.ag.selectEvent(confP.C.getEvents());
             if (logger.isLoggable(Level.FINE)) logger.fine("Selected event "+confP.C.SE);
             if (confP.C.SE != null) {
                 if (ag.hasCustomSelectOption() || setts.verbose() == 2) // verbose == 2 means debug mode 
                     confP.step = State.RelPl;
                 else 
                     confP.step = State.FindOp;
                 return;
             }
         }
         // Rule SelEv2
         // directly to ProcAct if no event to handle
         confP.step = State.ProcAct;
     }
 
     private void applyRelPl() throws JasonException {
         // get all relevant plans for the selected event
         confP.C.RP = relevantPlans(conf.C.SE.trigger);
 
         // Rule Rel1
         if (confP.C.RP != null || setts.retrieve()) 
             // retrieve is mainly for Coo-AgentSpeak
             confP.step = State.ApplPl;
         else
             applyRelApplPlRule2("relevant");
     }
     
     private void applyApplPl() throws JasonException {
         confP.C.AP = applicablePlans(confP.C.RP);
 
         // Rule Appl1
         if (confP.C.AP != null || setts.retrieve()) 
             // retrieve is mainly for Coo-AgentSpeak
             confP.step = State.SelAppl;
         else
             applyRelApplPlRule2("applicable");
     }
 
     /** generates goal deletion event */
     private void applyRelApplPlRule2(String m) throws JasonException {
         confP.step = State.ProcAct; // default next step
         if (conf.C.SE.trigger.isGoal()) {
             // can't carry on, no relevant/applicable plan.
             String msg = "Found a goal for which there is no "+m+" plan:" + conf.C.SE;
             if (!generateGoalDeletionFromEvent(JasonException.createBasicErrorAnnots("no_"+m, msg))) 
                 logger.warning(msg);                
         } else {
             if (conf.C.SE.isInternal()) {
                 // e.g. belief addition as internal event, just go ahead
                 // but note that the event was relevant, yet it is possible
                 // the programmer just wanted to add the belief and it was
                 // relevant by chance, so just carry on instead of dropping the
                 // intention
                 confP.C.SI = conf.C.SE.intention;
                 updateIntention();
             } else if (setts.requeue()) {  
                 // if external, then needs to check settings
                 confP.C.addEvent(conf.C.SE);
             } else {
                 // current event is external and irrelevant,
                 // discard that event and select another one
                 confP.step = State.SelEv;
             }
         }        
     }
     
 
     private void applySelAppl() throws JasonException {
         // Rule SelAppl
         confP.C.SO = conf.ag.selectOption(confP.C.AP);
         if (confP.C.SO != null) {
             confP.step = State.AddIM;
             if (logger.isLoggable(Level.FINE)) logger.fine("Selected option "+confP.C.SO+" for event "+confP.C.SE);
         } else {
             logger.fine("** selectOption returned null!");
             generateGoalDeletionFromEvent(JasonException.createBasicErrorAnnots("no_option", "selectOption returned null"));
             // can't carry on, no applicable plan.
             confP.step = State.ProcAct;
         }
     }
 
     /**
      * This step is new in Jason 1.1 and replaces the steps RelPl->ApplPl->SelAppl when the user
      * does not customise selectOption. This version does not create the RP and AP lists and thus 
      * optimise the reasoning cycle. It searches for the first option and automatically selects it.
      * 
      * @since 1.1
      */
     private void applyFindOp() throws JasonException {
         confP.step = State.AddIM; // default next step
         
         // get all relevant plans for the selected event
         //Trigger te = (Trigger) conf.C.SE.trigger.clone();
         List<Plan> candidateRPs = conf.ag.pl.getCandidatePlans(conf.C.SE.trigger);
         if (candidateRPs != null) {
             for (Plan pl : candidateRPs) {
                 Unifier relUn = pl.isRelevant(conf.C.SE.trigger);
                 if (relUn != null) { // is relevant
                     LogicalFormula context = pl.getContext();
                     if (context == null) { // context is true
                         confP.C.SO = new Option(pl, relUn);
                         return;
                     } else {
                         Iterator<Unifier> r = context.logicalConsequence(ag, relUn);
                         if (r != null && r.hasNext()) {
                             confP.C.SO = new Option(pl, r.next());
                             return;
                         }
                     } 
                 }
             }
             applyRelApplPlRule2("applicable");   
         } else {
             // problem: no plan
             applyRelApplPlRule2("relevant");   
         }        
     }
     
     private void applyAddIM() throws JasonException {
         // create a new intended means
         IntendedMeans im = new IntendedMeans(conf.C.SO, conf.C.SE.getTrigger());
 
         // Rule ExtEv
         if (conf.C.SE.intention == Intention.EmptyInt) {
             Intention intention = new Intention();
             intention.push(im);
             confP.C.addIntention(intention);
         } else {
             // Rule IntEv
             confP.C.SE.intention.push(im);
             confP.C.addIntention(confP.C.SE.intention);
         }
         confP.step = State.ProcAct;
     }
 
     private void applyProcAct() throws JasonException {
         confP.step = State.SelInt; // default next step
         if (conf.C.hasFeedbackAction()) {
             ActionExec a = conf.ag.selectAction(conf.C.getFeedbackActions());
             if (a != null) {
                 confP.C.SI = a.getIntention();
     
                 // remove the intention from PA (PA has all pending action, including those in FA;
                 // but, if the intention is not in PA, it means that the intention was dropped
                 // and should not return to I)
                 if (C.removePendingAction(confP.C.SI.getId()) != null) {
                     if (a.getResult()) {
                         // add the intention back in I
                         updateIntention();
                         applyClrInt(confP.C.SI);
                         
                         if (hasGoalListener())
                             for (GoalListener gl: getGoalListeners())
                                 for (IntendedMeans im: confP.C.SI.getIMs())
                                     gl.goalResumed(im.getTrigger());
                     } else {
                         String reason = a.getFailureMsg();
                         if (reason == null) reason = "";
                         ListTerm annots = JasonException.createBasicErrorAnnots("action_failed", reason);
                         if (a.getFailureReason() != null) 
                             annots.append(a.getFailureReason());
                        if (!generateGoalDeletion(conf.C.SI, annots)) {
                            C.removeAtomicIntention(); // if (potential) atomic intention is not removed, it will be selected in selInt and runs again
                        }
                     }
                 } else {
                     applyProcAct(); // get next action
                 }
             }
         }
     }
 
     private void applySelInt() throws JasonException {
         confP.step = State.ExecInt; // default next step
 
         // Rule for Atomic Intentions
         confP.C.SI = C.removeAtomicIntention();
         if (confP.C.SI != null) {
             return;
         }
         
         // Rule SelInt1
         if (!conf.C.isAtomicIntentionSuspended() && conf.C.hasIntention()) { // the isAtomicIntentionSuspended is necessary because the atomic intention may be suspended (the above removeAtomicInt returns null in that case)
                                                                              // but no other intention could be selected
             confP.C.SI = conf.ag.selectIntention(conf.C.getIntentions());
             if (confP.C.SI != null) { // the selectIntention function returned null
                 return;             
             }
         }
 
         confP.step = State.StartRC;
     }
 
     @SuppressWarnings("unchecked")
     private void applyExecInt() throws JasonException {
         confP.step = State.ClrInt; // default next step
         
         if (conf.C.SI.isFinished()) {
             return;
         }
         
         // get next formula in the body of the intended means
         // on the top of the selected intention
 
         IntendedMeans im = conf.C.SI.peek();
 
         if (im.isFinished()) { 
             // for empty plans! may need unif, etc
             updateIntention();
             return;
         }
         Unifier     u = im.unif;
         PlanBody    h = im.getCurrentStep();
         
         Term bTerm = h.getBodyTerm();
         // de-var bTerm
         while (bTerm instanceof VarTerm) {
             // check if bTerm is ground
             //if (bTerm.isGround()) {
             if (((VarTerm)bTerm).hasValue()) {
                 bTerm = ((VarTerm)bTerm).getValue();
                 continue; // restart the loop
             }
             
             // h should be 'groundable' (considering the current unifier)
             Term bValue = u.get((VarTerm)bTerm);
             //System.out.println("*** "+bTerm+"="+bValue+"  "+bTerm.isGround()+"  "+u);           
             if (bValue == null) { // the case of !A with A not ground
                 String msg = h.getSrcInfo()+": "+ "Variable '"+bTerm+"' must be ground.";
                 if (!generateGoalDeletion(conf.C.SI, JasonException.createBasicErrorAnnots("body_var_without_value", msg)))
                     logger.log(Level.SEVERE, msg);
                 return;
             }
             if (bValue.isPlanBody()) { 
                 if (h.getBodyType() != BodyType.action) { // the case of ...; A = { !g }; +g; ....
                     String msg = h.getSrcInfo()+": "+ "The operator '"+h.getBodyType()+"' is lost with the variable '"+bTerm+"' unified with a plan body '"+bValue+"'. ";
                     if (!generateGoalDeletion(conf.C.SI, JasonException.createBasicErrorAnnots("body_var_with_op", msg))) 
                         logger.log(Level.SEVERE, msg);
                     return;
                 }
                 h = (PlanBody)bValue;
                 if (h.getPlanSize() > 1) { // the case of A unified with {a;b;c}
                     h.add(im.getCurrentStep().getBodyNext());
                     im.insertAsNextStep(h.getBodyNext());
                 }
                 bTerm = h.getBodyTerm();
             } else {
                 ListTerm annots = ((VarTerm)bTerm).getAnnots();
                 bTerm = bValue;
                 if (bTerm.isLiteral() && annots != null) {
                     bTerm = ((Literal)bTerm).forceFullLiteralImpl();
                     ((Literal)bTerm).addAnnots(annots);
                 }
             }
         }
             
         Literal body  = null;
         if (bTerm instanceof Literal)
             body = (Literal)bTerm;
 
         switch (h.getBodyType()) {
 
         // Rule Action
         case action:
             body = body.copy(); body.apply(u);
             confP.C.A = new ActionExec(body, conf.C.SI);
             break;
 
         case internalAction:
             boolean ok = false;
             List<Term> errorAnnots = null;
             try {
                 InternalAction ia = ((InternalActionLiteral)bTerm).getIA(ag);
                 Term[] terms      = ia.prepareArguments(body, u); // clone and apply args
                 Object oresult    = ia.execute(this, u, terms);
                 if (oresult != null) {
                     ok = oresult instanceof Boolean && (Boolean)oresult;
                     if (!ok && oresult instanceof Iterator) { // ia result is an Iterator
                         Iterator<Unifier> iu = (Iterator<Unifier>)oresult;
                         if (iu.hasNext()) {
                             // change the unifier of the current IM to the first returned by the IA
                             im.unif = iu.next(); 
                             ok = true;
                         }
                     }
                     if (!ok) { // IA returned false
                         errorAnnots = JasonException.createBasicErrorAnnots("ia_failed", ""); 
                     }
                 }
 
                 if (ok && !ia.suspendIntention())
                     updateIntention();
             } catch (JasonException e) {
                 errorAnnots = e.getErrorTerms();
                 if (!generateGoalDeletion(conf.C.SI, errorAnnots))
                     logger.log(Level.SEVERE, body.getErrorMsg()+": "+ e.getMessage());
                 ok = true; // just to not generate the event again
             } catch (Exception e) {
                 if (body == null) 
                     logger.log(Level.SEVERE, "Selected an intention with null body in '"+h+"' and IM "+im, e);
                 else
                     logger.log(Level.SEVERE, body.getErrorMsg()+": "+ e.getMessage(), e);
             }
             if (!ok)
                 generateGoalDeletion(conf.C.SI, errorAnnots);
 
             break;
 
         case constraint:
             Iterator<Unifier> iu = ((LogicalFormula)bTerm).logicalConsequence(ag, u);
             if (iu.hasNext()) {
                 im.unif = iu.next();
                 updateIntention();
             } else {
                 String msg = "Constraint "+h+" was not satisfied ("+h.getSrcInfo()+").";
                 generateGoalDeletion(conf.C.SI, JasonException.createBasicErrorAnnots(new Atom("constraint_failed"), msg));
                 logger.fine(msg);
             }
             break;
 
         // Rule Achieve
         case achieve:
             body = prepareBodyForEvent(body, u);
             Event evt = conf.C.addAchvGoal(body, conf.C.SI);
             confP.step = State.StartRC;
             break;
 
         // Rule Achieve as a New Focus (the !! operator)
         case achieveNF:
             body = prepareBodyForEvent(body, u);
             evt  = conf.C.addAchvGoal(body, Intention.EmptyInt);
             updateIntention();
             break;
 
         // Rule Test
         case test:
             LogicalFormula f = (LogicalFormula)bTerm;
             if (conf.ag.believes(f, u)) {
                 updateIntention();
             } else {
                 boolean fail = true;
                 // generate event when using literal in the test (no events for log. expr. like ?(a & b))
                 if (f.isLiteral() && !(f instanceof BinaryStructure)) { 
                     body = prepareBodyForEvent(body, u);
                     if (body.isLiteral()) { // in case body is a var with content that is not a literal (note the VarTerm pass in the instanceof Literal)
                         Trigger te = new Trigger(TEOperator.add, TEType.test, body);
                         evt = new Event(te, conf.C.SI);
                         if (ag.getPL().hasCandidatePlan(te)) {
                             if (logger.isLoggable(Level.FINE)) logger.fine("Test Goal '" + h + "' failed as simple query. Generating internal event for it: "+te);
                             conf.C.addEvent(evt);
                             confP.step = State.StartRC;
                             fail = false;
                         }
                     }
                 }
                 if (fail) {
                     if (logger.isLoggable(Level.FINE)) logger.fine("Test '"+h+"' failed ("+h.getSrcInfo()+").");
                     generateGoalDeletion(conf.C.SI, JasonException.createBasicErrorAnnots("test_goal_failed", "Failed to test '"+h+"'"));
                 }
             }
             break;
 
             
         case delAddBel: 
             // -+a(1,X) ===> remove a(_,_), add a(1,X)
             // change all vars to anon vars to remove it
             Literal b2 = prepareBodyForEvent(body, u); 
             b2.makeTermsAnnon(); // do not change body (but b2), to not interfere in addBel
             // to delete, create events as external to avoid that
             // remove/add create two events for the same intention
             // (in future releases, creates a two branches for this operator)
 
             try {
                 List<Literal>[] result = ag.brf(null, b2, conf.C.SI); // the intention is not the new focus
                 if (result != null) { // really delete something
                     // generate events
                     updateEvents(result,Intention.EmptyInt);
                 }
             } catch (RevisionFailedException re) {
                 generateGoalDeletion(conf.C.SI, JasonException.createBasicErrorAnnots("belief_revision_failed", "BRF failed for '"+body+"'"));
                 break;
             }
 
             // add the belief, so no break;
             
         // Rule AddBel
         case addBel:
             body = prepareBodyForEvent(body, u);
 
             // calculate focus
             Intention newfocus = Intention.EmptyInt;
             if (setts.sameFocus())
                 newfocus = conf.C.SI;
             
             // call BRF
             try {
                 List<Literal>[] result = ag.brf(body,null,conf.C.SI); // the intention is not the new focus
                 if (result != null) { // really add something
                     // generate events
                     updateEvents(result,newfocus);
                     if (!setts.sameFocus()) {
                         updateIntention();
                     }                    
                 } else {
                     updateIntention();                    
                 }
             } catch (RevisionFailedException re) {
                 generateGoalDeletion(conf.C.SI, null);
             }
             break;
             
         case delBel:
             body = prepareBodyForEvent(body, u);
 
             newfocus = Intention.EmptyInt;
             if (setts.sameFocus())
                 newfocus = conf.C.SI;
 
             // call BRF
             try {
                 List<Literal>[] result = ag.brf(null, body, conf.C.SI); // the intention is not the new focus
                 if (result != null) { // really change something
                     // generate events
                     updateEvents(result,newfocus);
                     if (!setts.sameFocus()) {
                         updateIntention();
                     }                    
                 } else {
                     updateIntention();                    
                 }
             } catch (RevisionFailedException re) {
                 generateGoalDeletion(conf.C.SI, null);
             }            
             break;
         }
     }
     
     // add the self source in the body in case no other source was given
     private Literal prepareBodyForEvent(Literal body, Unifier u) {
         body = body.copy();
         body.apply(u);
         body.makeVarsAnnon(u); // free variables in an event cannot conflict with those in the plan
         body = body.forceFullLiteralImpl();
         if (!body.hasSource()) { // do not add source(self) in case the programmer set the source
             body.addAnnot(BeliefBase.TSelf);
         }
         return body;
     }
 
     public void applyClrInt(Intention i) throws JasonException {
         while (true) { // quit the method by return
             // Rule ClrInt
             if (i == null)
                 return;
             
             if (i.isFinished()) {
                 // intention finished, remove it
                 confP.C.dropIntention(i);
                 //conf.C.SI = null;
                 return;
             }
     
             IntendedMeans im = i.peek();
             if (!im.isFinished()) {
                 // nothing to do
                 return;
             }
     
             // remove the finished IM from the top of the intention
             IntendedMeans topIM = i.pop();
             Trigger topTrigger = topIM.getTrigger();
             Literal topLiteral = topTrigger.getLiteral();
             if (logger.isLoggable(Level.FINE)) logger.fine("Returning from IM "+topIM.getPlan().getLabel()+", te="+topIM.getPlan().getTrigger()+" unif="+topIM.unif);
             
             // produce ^!g[state(finished)] event
             if (topTrigger.getOperator() == TEOperator.add && topTrigger.isGoal()) {
                 if (hasGoalListener())
                     for (GoalListener gl: goalListeners)
                         gl.goalFinished(topTrigger);
             }
             
             // if has finished a failure handling IM ...
             if (im.getTrigger().isGoal() && !im.getTrigger().isAddition() && i.size() > 0) {
                 // needs to get rid of the IM until a goal that
                 // has failure handling. E.g,
                 //   -!b
                 //   +!c
                 //   +!d
                 //   +!b
                 //   +!s: !b; !z
                 // should became
                 //   +!s: !z
                 im = i.peek();
                 if (im.isFinished() || !im.unif.unifies(im.getCurrentStep().getBodyTerm(), topLiteral) || im.getCurrentStep().getBodyTerm() instanceof VarTerm) {
                     im = i.pop(); // +!c above
                 }
                 while (i.size() > 0 &&
                        !im.unif.unifies(im.getTrigger().getLiteral(), topLiteral) &&
                        !im.unif.unifies(im.getCurrentStep().getBodyTerm(), topLiteral)) {
                     im = i.pop();
                 }
             }
             if (!i.isFinished()) {
                 im = i.peek(); // +!s or +?s
                 if (!im.isFinished()) {
                     // removes !b or ?s
                     // unifies the final event with the body that called it
                     topLiteral.apply(topIM.unif);
                     im.unif.unifies(im.removeCurrentStep(), topLiteral);
                 }
             }
         }
     }
 
     /**********************************************/
     /* auxiliary functions for the semantic rules */
     /**********************************************/
 
     public List<Option> relevantPlans(Trigger teP) throws JasonException {
         Trigger te = teP.clone();
         List<Option> rp = null;
         List<Plan> candidateRPs = conf.ag.pl.getCandidatePlans(te);
         if (candidateRPs != null) {
             for (Plan pl : candidateRPs) {
                 Unifier relUn = pl.isRelevant(te);
                 if (relUn != null) {
                     if (rp == null) rp = new LinkedList<Option>();
                     rp.add(new Option(pl, relUn));
                 }
             }
         }
         return rp;
     }
 
     public List<Option> applicablePlans(List<Option> rp) throws JasonException {
         List<Option> ap = null;
         if (rp != null) {
             //ap = new ApplPlanTimeOut().get(rp);
             
             for (Option opt: rp) {
                 LogicalFormula context = opt.getPlan().getContext();
                 if (context == null) { // context is true
                     if (ap == null) ap = new LinkedList<Option>();
                     ap.add(opt);
                 } else {
                     boolean allUnifs = opt.getPlan().isAllUnifs();
                     Iterator<Unifier> r = context.logicalConsequence(ag, opt.getUnifier());
                     if (r != null) {
                         while (r.hasNext()) {
                             opt.setUnifier(r.next());
                             
                             if (ap == null) ap = new LinkedList<Option>();
                             ap.add(opt);
                             
                             if (!allUnifs) break; // returns only the first unification
                             if (r.hasNext()) {
                                 // create a new option for the next loop step
                                 opt = new Option(opt.getPlan(), null);
                             }
                         }
                     }
                 }
             } 
         }
         return ap;
     }
     
     public void updateEvents(List<Literal>[] result, Intention focus) {
         if (result == null) return;
         // create the events
         for (Literal ladd: result[0]) {
             Trigger te = new Trigger(TEOperator.add, TEType.belief, ladd);
             updateEvents(new Event(te, focus));
             focus = Intention.EmptyInt;
         }
         for (Literal lrem: result[1]) {
             Trigger te = new Trigger(TEOperator.del, TEType.belief, lrem);
             updateEvents(new Event(te, focus));
             focus = Intention.EmptyInt;
         }
     }
 
     // only add External Event if it is relevant in respect to the PlanLibrary
     public void updateEvents(Event e) {
         // Note: we have to add events even if they are not relevant to
         // a) allow the user to override selectOption and then provide an "unknown" plan; or then
         // b) create the failure event (it is done by SelRelPlan)
         if (e.isInternal() || C.hasListener() || ag.getPL().hasCandidatePlan(e.trigger)) {
             C.addEvent(e);
             if (logger.isLoggable(Level.FINE)) logger.fine("Added event " + e);
         }
     }
     
     /** remove the top action and requeue the current intention */
     private void updateIntention() {
         if (!conf.C.SI.isFinished()) {
             IntendedMeans im = conf.C.SI.peek();
             im.removeCurrentStep();
             confP.C.addIntention(conf.C.SI);
         } else {
             logger.fine("trying to update a finished intention!");
         }
     }
 
     /** generate a failure event for an intention */
     public boolean generateGoalDeletion(Intention i, List<Term> failAnnots) throws JasonException {
         boolean failEeventGenerated = false;
         IntendedMeans im = i.peek();
         if (im.isGoalAdd()) {
             // notify listener
             if (hasGoalListener())
                 for (GoalListener gl: goalListeners)
                     gl.goalFailed(im.getTrigger());
             
             // produce failure event
             Event failEvent = findEventForFailure(i, im.getTrigger());
             if (failEvent != null) {
                 setDefaultFailureAnnots(failEvent, im.getCurrentStep().getBodyTerm(), failAnnots);
                 confP.C.addEvent(failEvent);
                 failEeventGenerated = true;
                 if (logger.isLoggable(Level.FINE)) logger.fine("Generating goal deletion " + failEvent.getTrigger() + " from goal: " + im.getTrigger());
             } else {
                 logger.warning("No failure event was generated for " + im.getTrigger());
             }
         }
         // if "discard" is set, we are deleting the whole intention!
         // it is simply not going back to 'I' nor anywhere else!
         else if (setts.requeue()) {
             // get the external event (or the one that started
             // the whole focus of attention) and requeue it
             im = i.get(0);
             confP.C.addExternalEv(im.getTrigger());
         } else {
             logger.warning("Could not finish intention: " + i);
         }
         return failEeventGenerated;
     }
 
     // similar to the one above, but for an Event rather than intention
     private boolean generateGoalDeletionFromEvent(List<Term> failAnnots) throws JasonException {
         Event ev = conf.C.SE;
         if (ev == null) {
             logger.warning("** It was not possible to generate a goal deletion event because SE is null! " + conf.C);
             return false;
         }
         
         Trigger tevent = ev.trigger;
         boolean failEeventGenerated = false;
         if (tevent.isAddition() && tevent.isGoal()) {
             // notify listener
             if (hasGoalListener())
                 for (GoalListener gl: goalListeners)
                     gl.goalFailed(tevent);
             
             // produce failure event
             Event failEvent = findEventForFailure(ev.intention, tevent);
             if (failEvent != null) {
                 setDefaultFailureAnnots(failEvent, tevent.getLiteral(), failAnnots);
                 confP.C.addEvent(failEvent);
                 failEeventGenerated = true;
                 //logger.warning("Generating goal deletion " + failEvent.getTrigger() + " from event: " + ev.getTrigger());
             } else {
                 logger.warning("No fail event was generated for " + ev.getTrigger());
             }
         } else if (ev.isInternal()) {
             logger.warning("Could not finish intention:\n" + ev.intention);
         }
         // if "discard" is set, we are deleting the whole intention!
         // it is simply not going back to I nor anywhere else!
         else if (setts.requeue()) {
             confP.C.addEvent(ev);
             logger.warning("Requeing external event: " + ev);
         } else
             logger.warning("Discarding external event: " + ev);
         return failEeventGenerated;
     }
 
     public Event findEventForFailure(Intention i, Trigger tevent) {
         Trigger failTrigger = new Trigger(TEOperator.del, tevent.getType(), tevent.getLiteral());
         if (i != Intention.EmptyInt) {
             ListIterator<IntendedMeans> ii = i.iterator();
             while (!getAg().getPL().hasCandidatePlan(failTrigger) && ii.hasPrevious()) {
                 // TODO: pop IM until +!g or *!g (this TODO is valid only if meta events are pushed on top of the intention)
                 // If *!g is found first, no failure event
                 // - while popping, if some meta event (* > !) is in the stack, stop and simple pop instead of producing an failure event
                 IntendedMeans im = ii.previous();
                 tevent = im.getTrigger();
                 failTrigger = new Trigger(TEOperator.del, tevent.getType(), tevent.getLiteral());
             }
         }
         // if some failure handling plan is found
         if (tevent.isGoal() && tevent.isAddition() && getAg().getPL().hasCandidatePlan(failTrigger)) {
             return new Event(failTrigger.clone(), i);
         }
         return null;
     }
     
     private static final Atom aNOCODE = new Atom("no_code");
     
     /** add default error annotations (error, error_msg, code, code_src, code_line) in the failure event */
     private static void setDefaultFailureAnnots(Event failEvent, Term body, List<Term> failAnnots) {
         // add default failure annots
         if (failAnnots == null)
             failAnnots = JasonException.createBasicErrorAnnots( JasonException.UNKNOW_ERROR, "");
 
         Literal eventLiteral = failEvent.getTrigger().getLiteral();
         eventLiteral.addAnnots(failAnnots);
         
         // add failure annots in the event related to the code source
         Literal bodyterm = aNOCODE;
         Term codesrc     = aNOCODE;
         Term codeline    = aNOCODE;
         if (body != null && body instanceof Literal) {
             bodyterm = (Literal)body;
             if (bodyterm.getSrcInfo() != null) {
                 if (bodyterm.getSrcInfo().getSrcFile() != null)
                     codesrc = new StringTermImpl(bodyterm.getSrcInfo().getSrcFile());
                 codeline = new NumberTermImpl(bodyterm.getSrcInfo().getSrcLine());
             }
         }
 
         // code
         if (eventLiteral.getAnnots("code").isEmpty())
             eventLiteral.addAnnot(ASSyntax.createStructure("code", bodyterm.copy().makeVarsAnnon()));
         
         // ASL source
         if (eventLiteral.getAnnots("code_src").isEmpty())
             eventLiteral.addAnnot(ASSyntax.createStructure("code_src", codesrc));
 
         // line in the source
         if (eventLiteral.getAnnots("code_line").isEmpty())
             eventLiteral.addAnnot(ASSyntax.createStructure("code_line", codeline));
     }
         
     public boolean canSleep() {
         return    (C.isAtomicIntentionSuspended() && conf.C.MB.isEmpty())               
                || (!conf.C.hasEvent() && !conf.C.hasIntention() && 
                    !conf.C.hasFeedbackAction() &&
                    conf.C.MB.isEmpty() && 
                    //taskForBeginOfCycle.isEmpty() &&
                    agArch.canSleep());
     }
 
     /** 
      * Schedule a task to be executed in the begin of the next reasoning cycle. 
      * It is used mostly to change the C only by the TS thread (e.g. by .wait)
      */
     public void runAtBeginOfNextCycle(Runnable r) {
         taskForBeginOfCycle.offer(r);
     }
     
     /**********************************************************************/
     /* MAIN LOOP */
     /**********************************************************************/
     /* infinite loop on one reasoning cycle                               */
     /* plus the other parts of the agent architecture besides             */
     /* the actual transition system of the AS interpreter                 */
     /**********************************************************************/
     public void reasoningCycle() {
         if (logger.isLoggable(Level.FINE)) logger.fine("Start new reasoning cycle");
 
         try {
             C.reset();
 
             // run tasks allocated to be performed in the begin of the cycle
             Runnable r = taskForBeginOfCycle.poll();
             while (r != null) {
                 r.run();
                 r = taskForBeginOfCycle.poll();
             }
             
             if (nrcslbr >= setts.nrcbp()) { 
                 nrcslbr = 0;
                 ag.buf(agArch.perceive());
                 agArch.checkMail();
             }
             nrcslbr++; // counting number of cycles since last belief revision
 
             if (canSleep()) {
                 if (ag.pl.getIdlePlans() != null) {
                     logger.fine("generating idle event");
                     C.addExternalEv(PlanLibrary.TE_IDLE);
                 } else {
                     agArch.sleep();
                     return;
                 }
             }
             
             step = State.StartRC;
             do {
                 if (!agArch.isRunning()) return;
                 applySemanticRule();
             } while (step != State.StartRC);
 
             ActionExec action = C.getAction(); 
             if (action != null) {
                 C.addPendingAction(action);                
                 // We need to send a wrapper for FA to the user so that add method then calls C.addFA (which control atomic things)
                 agArch.act(action, C.getFeedbackActionsWrapper());
             }
 
         } catch (Exception e) {
             logger.log(Level.SEVERE, "*** ERROR in the transition system. "+conf.C+"\nCreating a new C!", e);
             conf.C.create();
         }
     }
 
     // Auxiliary functions
     // (for Internal Actions to be able to access the configuration)
     public Agent getAg() {
         return ag;
     }
 
     public Circumstance getC() {
         return C;
     }
 
     public State getStep() {
         return step;
     }
 
     public Settings getSettings() {
         return setts;
     }
 
     public AgArch getUserAgArch() {
         return agArch;
     }
 
     public Logger getLogger() {
         return logger;
     }
 }
