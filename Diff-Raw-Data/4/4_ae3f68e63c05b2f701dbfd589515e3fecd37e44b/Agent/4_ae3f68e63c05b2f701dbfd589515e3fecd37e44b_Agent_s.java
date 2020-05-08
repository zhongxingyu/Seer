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
 // http://www.dur.ac.uk/r.bordini
 // http://www.inf.furb.br/~jomi
 //
 //----------------------------------------------------------------------------
 
 package jason.asSemantics;
 
 import jason.JasonException;
 import jason.architecture.AgArch;
 import jason.asSyntax.Literal;
 import jason.asSyntax.PlanLibrary;
 import jason.asSyntax.Structure;
 import jason.asSyntax.Trigger;
 import jason.asSyntax.parser.ParseException;
 import jason.asSyntax.parser.as2j;
 import jason.bb.BeliefBase;
 import jason.bb.DefaultBeliefBase;
 import jason.runtime.Settings;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
  * The Agent class has the belief base and plan library of an
  * AgentSpeak agent. It also implements the default selection
  * functions of the AgentSpeak semantics.
  */
 public class Agent {
 
     // Members
     protected BeliefBase fBB = new DefaultBeliefBase();
 
     protected PlanLibrary fPL = new PlanLibrary();
 
     private Map<String, InternalAction> internalActions = new HashMap<String, InternalAction>();
 
     protected TransitionSystem fTS = null;
 
     protected String aslSource = null;
 
     private Logger logger = Logger.getLogger(Agent.class.getName());
 
     /** Creates the TS of this agent, parses its AS source, and sets its Settings */
     public TransitionSystem initAg(AgArch arch, BeliefBase bb, String asSrc, Settings stts) throws JasonException {
         // set the agent
         try {
             setLogger(arch);
             logger.setLevel(stts.logLevel());
 
             if (bb != null) {
                 this.fBB = bb;
             }
 
             setTS(new TransitionSystem(this, new Circumstance(), stts, arch));
 
             this.aslSource = asSrc;
             parseAS(asSrc);
             // kqml Plans at the end of the ag PS
             parseAS(JasonException.class.getResource("/asl/kqmlPlans.asl"));
 
             return fTS;
         } catch (Exception e) {
             logger.log(Level.SEVERE, "Error creating the agent class!", e);
             throw new JasonException("Error creating the agent class! - " + e);
         }
     }
 
     public void setLogger(AgArch arch) {
         if (arch != null) {
             logger = Logger.getLogger(Agent.class.getName() + "." + arch.getAgName());
         }
     }
 
     public Logger getLogger() {
         return logger;
     }
 
     /** Returns the .asl file source used to create this agent */
     public String getASLSource() {
         return aslSource;
     }
 
     /** Adds beliefs and plans form an URL */
     public boolean parseAS(URL asURL) {
         try {
             parseAS(asURL.openStream());
             logger.fine("as2j: AgentSpeak program '" + asURL + "' parsed successfully!");
             return true;
         } catch (IOException e) {
             logger.log(Level.SEVERE, "as2j: the AgentSpeak source file was not found", e);
         } catch (ParseException e) {
             logger.log(Level.SEVERE, "as2j: error parsing \"" + asURL + "\"", e);
         }
         return false;
     }
 
     /** Adds beliefs and plans form a file */
     public boolean parseAS(String asFileName) {
         try {
             parseAS(new FileInputStream(asFileName));
             logger.fine("as2j: AgentSpeak program '" + asFileName + "' parsed successfully!");
             return true;
         } catch (FileNotFoundException e) {
             logger.log(Level.SEVERE, "as2j: the AgentSpeak source file was not found", e);
         } catch (ParseException e) {
             logger.log(Level.SEVERE, "as2j: error parsing \"" + asFileName + "\"", e);
         }
         return false;
     }
 
     void parseAS(InputStream asIn) throws ParseException {
         as2j parser = new as2j(asIn);
         parser.agent(this);
     }
 
     public InternalAction getIA(Structure action) throws Exception {
         String iaName = action.getFunctor();
         if (iaName.charAt(0) == '.')
             iaName = "jason.stdlib" + iaName;
         InternalAction objIA = internalActions.get(iaName);
         if (objIA == null) {
             objIA = (InternalAction) Class.forName(iaName).newInstance();
             internalActions.put(iaName, objIA);
         }
         return objIA;
     }
 
     /**
      * Follows the default implementation for the agent's message acceptance
      * relation and selection functions
      */
     public boolean socAcc(Message m) {
         return true;
     }
 
     public Event selectEvent(Queue<Event> events) {
         // make sure the selected Event is removed from evList
         return events.poll();
     }
 
     public Option selectOption(List<Option> options) {
         if (options.size() > 0) {
             return options.remove(0);
         } else {
             return null;
         }
     }
 
     public Intention selectIntention(Queue<Intention> intentions) {
         // make sure the selected Intention is removed from intList AND
         // make sure no intention will "starve"!!!
         return intentions.poll();
     }
 
     public Message selectMessage(Queue<Message> messages) {
         // make sure the selected Message is removed from msgList
         return messages.poll();
     }
 
     public ActionExec selectAction(List<ActionExec> actList) {
         // make sure the selected Action is removed from actList
         return actList.remove(0);
     }
 
     /** TS Initialisation (called by the AgArch) */
     public void setTS(TransitionSystem ts) {
         this.fTS = ts;
     }
 
     public TransitionSystem getTS() {
         return fTS;
     }
 
     // Accessing the agent's belief base and plans
     public BeliefBase getBB() {
         return fBB;
     }
 
     public PlanLibrary getPL() {
         return fPL;
     }
 
     /** Belief Update Function: adds/removes perceptions into belief base */
     public void buf(List<Literal> percepts) {
         if (percepts == null) {
             return;
         }
 
         // deleting percepts in the BB that is not perceived anymore
         Iterator<Literal> perceptsInBB = getBB().getPercepts();
         while (perceptsInBB.hasNext()) { 
             Literal l = perceptsInBB.next();
 
             // could not use percepts.contains(l), since equalsAsTerm must be
             // used (to ignore annotations)
             boolean wasPerceived = false;
             for (Literal t : percepts) {
                 // if percept t is already in BB
                 if (l.equalsAsTerm(t) && l.negated() == t.negated()) {
                     wasPerceived = true;
                     break;
                 }
             }
             if (!wasPerceived) {
 	            if (fBB.remove(l)) {
 	                fTS.updateEvents(new Event(new Trigger(Trigger.TEDel,Trigger.TEBel, l), Intention.EmptyInt));
 	            }
             }
         }
 
         // addBel only adds a belief when appropriate
         // checking all percepts for new beliefs
         for (Literal lp : percepts) {
             try {
                 lp = (Literal) lp.clone();
                 lp.addAnnot(BeliefBase.TPercept);
                 if (getBB().add(lp)) {
                     Trigger te = new Trigger(Trigger.TEAdd, Trigger.TEBel, lp);
                     fTS.updateEvents(new Event(te, Intention.EmptyInt));
                 }
             } catch (Exception e) {
                 logger.log(Level.SEVERE, "Error adding percetion " + lp, e);
             }
         }
     }
 
     /**
      * Returns true if BB contains the literal (using unification to test).
      * The unifier <i>un</i> is updated by the method.
      */
     public boolean believes(Literal bel, Unifier un) {
         try {
             Iterator<Unifier> iun = bel.logicalConsequence(this, un);
             if (iun != null && iun.hasNext()) {
                 un.compose(iun.next());
                 return true;
             }
         } catch (Exception e) {
             logger.log(Level.SEVERE, "Error in believe("+bel+","+un+").",e);
         }
         return false;
     }
 
     /**
      * Find a literal in BB using unification to test.
      * 
      * Returns the belief as it is in BB, e.g. findBel(a(_),...) 
      * may returns a(10)[source(ag)].
      *   
      * The unifier <i>un</i> is updated by the method.
      */
     public Literal findBel(Literal bel, Unifier un) {
         Iterator<Literal> relB = fBB.getRelevant(bel);
         if (relB != null) {
             while (relB.hasNext()) {
                 Literal b = relB.next();
 
                 // recall that order is important because of annotations!
                 if (!b.isRule() && un.unifies(bel, b)) {
                     return b;
                 }
             }
         } 
         return null;
     }
 
     
     /**
      * This function should revise the belief base with the given literal to
      * add, to remove, and the current intention that triggered the operation.
      * 
      * <p>In its return, List[0] has the list of actual additions to
      * the belief base, and List[1] has the list of actual deletions;
      * this is used to generate the appropriate internal events. If
      * nothing change, returns null.
      */
     @SuppressWarnings("unchecked")
     public List<Literal>[] brf(Literal beliefToAdd, Literal beliefToDel,  Intention i) {
         // This class does not implement belief revision! It
         // is supposed that a subclass will do it.
         // It simply add/del the belief.
 
         List<Literal>[] result = null;
 
         if (beliefToAdd != null) {
         	if (logger.isLoggable(Level.FINE)) logger.fine("adding " + beliefToAdd);
             
             if (getBB().add(beliefToAdd)) {
             	result = new List[2];
                 result[0] = Collections.singletonList(beliefToAdd);
                 result[1] = Collections.emptyList();
             }
         }
 
         if (beliefToDel != null) {
             Unifier u = null;
             try {
                 u = i.peek().unif; // get from current intention
             } catch (Exception e) {
                 u = new Unifier();
             }
 
             if (logger.isLoggable(Level.FINE)) logger.fine("doing brf for " + beliefToDel + " in BB=" + believes(beliefToDel, u));
             
             if (believes(beliefToDel, u)) {
             	beliefToDel.apply(u);
                 if (getBB().remove(beliefToDel)) {
                     if (logger.isLoggable(Level.FINE)) logger.fine("Removed:" + beliefToDel);
                     if (result == null) {
                     	result = new List[2];
                         result[0] = Collections.emptyList();
                     }
                     result[1] = Collections.singletonList(beliefToDel);
                 }
             }
 
         }
         return result;
     }
 
     /**
      * Adds <i>bel</i> in belief base (calling brf) and generates the
      * events. If <i>bel</i> has no source, add
      * <code>source(self)</code>. (the belief is not cloned!)
      */
     public boolean addBel(Literal bel) {
         if (!bel.hasSource()) {
             bel.addAnnot(BeliefBase.TSelf);
         }
         List<Literal>[] result = brf(bel, null, Intention.EmptyInt);
         if (result != null && fTS != null) {
             fTS.updateEvents(result, Intention.EmptyInt);
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * If the agent believes in <i>bel</i>, removes it (calling brf)
      * and generate the event.
      */
     public boolean delBel(Literal bel) {
         List<Literal>[] result = brf(null, bel, Intention.EmptyInt);
         if (result != null && fTS != null) {
             fTS.updateEvents(result, Intention.EmptyInt);
             return true;
         } else {
             return false;
         }
     }
     
     /** Removes all occurrences of <i>bel</i> in BB. 
         If <i>un</i> is null, an empty Unifier is used. 
      */
     public void abolish(Literal bel, Unifier un) {
         List<Literal> toDel = new ArrayList<Literal>();
         
         Iterator<Literal> il = getBB().getRelevant(bel);
         if (il != null) {
             while (il.hasNext()) {
                 Literal inBB = il.next();
                 if (!inBB.isRule()) {
                     // need to clone unifier since it is changed in previous iteration
                     Unifier unC = (un == null ? new Unifier() : (Unifier)un.clone());
                     if (unC.unifies(bel, inBB)) {
                         toDel.add(inBB);
                     }
                 }
             }
         }
         
         for (Literal l: toDel) {
             delBel(l);
         }
     }
 
     static DocumentBuilder builder = null;
 
     /** Gets the agent "mind" (Beliefs, plans and circumstance) as XML */
     public Document getAgState() {
         if (builder == null) {
             try {
                 builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
             } catch (Exception e) {
                 logger.log(Level.SEVERE, "Error creating XML builder\n");
                 return null;
             }
         }
         Document document = builder.newDocument();
         Element ag = getAsDOM(document);
         document.appendChild(ag);
 
         ag.appendChild(fTS.getC().getAsDOM(document));
         return document;
     }
 
     /** Gets the agent "mind" as XML */
     public Element getAsDOM(Document document) {
         Element ag = (Element) document.createElement("agent");
         ag.setAttribute("name", fTS.getUserAgArch().getAgName());
         ag.setAttribute("cycle", ""+fTS.getUserAgArch().getCycleNumber());
 
         ag.appendChild(fBB.getAsDOM(document));
         // ag.appendChild(ps.getAsDOM(document));
         return ag;
     }
 
     /** Gets the agent program (Beliefs and plans) as XML */
     public Document getAgProgram() {
         if (builder == null) {
             try {
                 builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
             } catch (Exception e) {
                 logger.log(Level.SEVERE, "Error creating XML builder\n");
                 return null;
             }
         }
         Document document = builder.newDocument();
         Element ag = (Element) document.createElement("agent");
         ag.appendChild(fBB.getAsDOM(document));
         ag.appendChild(fPL.getAsDOM(document));
         document.appendChild(ag);
 
         return document;
     }
 
 }
