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
 
 
 package jason.asSyntax;
 
 
 import jason.JasonException;
 import jason.bb.BeliefBase;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 public class PlanLibrary implements Iterable<Plan> {
 
 	/** a MAP from TE to a list of relevant plans */
     Map<PredicateIndicator,List<Plan>> relPlans = new HashMap<PredicateIndicator,List<Plan>>();
 
 	/**
 	 * All plans as defined in the AS code (maintains the order of the plans)
 	 */
 	List<Plan> plans = new ArrayList<Plan>();
 	
 	/** list of plans that have var as TE */
 	List<Plan> varPlans = new ArrayList<Plan>();
 	
 	/** A map from labels to plans */
 	Map<String,Plan> planLabels = new HashMap<String,Plan>();
 	
 	private static int lastPlanLabel = 0;
 
 	private Logger logger = Logger.getLogger(PlanLibrary.class.getName());	
 	
 	/** 
 	 *  Add a new plan based on a String. The source
 	 *  normally is "self" or the agent that sent this plan.
 	 *  If the already has a plan equals to "stPlan", only a
 	 *  new source is added.
      *  
      *   returns the plan added, null if it does not work.
 	 */
 	public Plan add(StringTerm stPlan, Structure tSource) {
 		String sPlan = stPlan.getString();
 		try {
 			// remove quotes \" -> "
             StringBuilder sTemp = new StringBuilder();
 			for (int c=0; c <sPlan.length(); c++) {
 				if (sPlan.charAt(c) != '\\') {
 					sTemp.append(sPlan.charAt(c));
 				}
 			}
			sPlan = sTemp.toString();
 			Plan p = Plan.parse(sPlan);
 			if (p != null) {
     			int i = plans.indexOf(p);
     			if (i < 0) {
     				p.getLabel().addSource(tSource);
     				add(p);
     			} else {
     				p = (Plan) plans.get(i);
     				p.getLabel().addSource(tSource);
     			}
     			return p;
             }
 		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error adding plan "+sPlan,e);
 		}
         return null;
 	}
 
 
 	
     public void add(Plan p) throws JasonException {
         // test p.label
         if (p.getLabel() != null && planLabels.keySet().contains(p.getLabel().getFunctor())) {
             // test if the new plan is equal, in this case, just add a source
             Plan planInPL = get(p.getLabel().getFunctor());
             if (p.equals(planInPL)) {
                 planInPL.getLabel().addSource((Pred) p.getLabel().getSources().get(0));
                 return;
             } else {
                 throw new JasonException("There already is a plan with label " + p.getLabel());
             }
         }
 
         // add label, if necessary
         if (p.getLabel() == null) {
            String l;
            do {
                l = "l__" + (lastPlanLabel++);
            } while (planLabels.keySet().contains(l));
            p.setLabel(new Pred(l));
         }
 
         // add self source
         if (!p.getLabel().hasSource()) {
             p.getLabel().addAnnot(BeliefBase.TSelf);
         }
         planLabels.put(p.getLabel().getFunctor(), p);
 
         if (p.getTriggerEvent().getLiteral().isVar()) {
             varPlans.add(p);
             // add plan p in all entries
             for (List<Plan> lp: relPlans.values()) {
                 lp.add(p);
             }
         } else {
             List<Plan> codesList = relPlans.get(p.getTriggerEvent().getPredicateIndicator());
             if (codesList == null) {
                 codesList = new ArrayList<Plan>();
                 codesList.addAll(varPlans);
                 relPlans.put(p.getTriggerEvent().getPredicateIndicator(), codesList);
             }
             codesList.add(p);
         }
 
         plans.add(p);
     }
 
 	public void addAll(PlanLibrary pl) throws JasonException {
 		for (Plan p: pl) { 
 			add(p);
 		}
 	}
 
 	public void addAll(List<Plan> plans) throws JasonException {
 		for (Plan p: plans) { 
 			add(p);
 		}
 	}
     
 	/** return a plan for a label */
     public Plan get(String label) {
         return planLabels.get(label);
     }
     
     public List<Plan> getPlans() {
     	    return plans;
     }
     
     public Iterator<Plan> iterator() {
     	return plans.iterator();
     }
 
 
     /** 
 	 * Remove a plan represented by the label <i>pLabel</i>.
 	 * In case the plan has many sources, only the plan's source is removed. 
 	 */
 	public boolean removePlan(Structure pLabel, Structure source) {
 		// find the plan
 		Plan p = get(pLabel.getFunctor());
 		if (p != null) {
 			boolean hasSource = p.getLabel().delSource(source);
 
 			// if no source anymore, remove the plan
 			if (hasSource && !p.getLabel().hasSource()) {
 			    remove(pLabel.getFunctor());
 			}
 			return true;
 		}
 		return false;
 	}
 
 	/** remove the plan with label <i>pLabel</i> */
     public Plan remove(String pLabel) {
         Plan p = (Plan) planLabels.remove(pLabel);
 
         // remove it from plans' list
         plans.remove(p);
 
         if (p.getTriggerEvent().getLiteral().isVar()) {
             varPlans.remove(p);
             // remove p from all entries
             for (List<Plan> lp: relPlans.values()) {
                 lp.remove(p);
             }
         } else {
             List<Plan> codesList = relPlans.get(p.getTriggerEvent().getPredicateIndicator());
             codesList.remove(p);
             if (codesList.isEmpty()) {
                 // no more plans for this TE
                 relPlans.remove(p.getTriggerEvent().getPredicateIndicator());
             }
         }
         return p;
     }
 
     public boolean isRelevant(PredicateIndicator pi) {
         	List l = getAllRelevant(pi);
         	return l != null && l.size() > 0;
     }
 
 
     public List<Plan> getAllRelevant(PredicateIndicator pi) {
         	List<Plan> l = relPlans.get(pi);
         	if ((l == null || l.size() == 0) && varPlans.size() > 0) { // no rel plan, try varPlan
         		l = varPlans;
         	}
         	return l;
     }
 
     public static final Trigger TE_IDLE = Trigger.parseTrigger("+!idle");
 
     public List<Plan> getIdlePlans() {
         return relPlans.get(TE_IDLE.getPredicateIndicator());
     }
 
     public Object clone() {
     	PlanLibrary pl = new PlanLibrary();
     	try {
 			pl.addAll(this);
 		} catch (JasonException e) {
 			e.printStackTrace();
 		}
     	return pl;
     }
 
     public String toString() {
         return plans.toString();
     }
     
 	/** get as XML */
 	public Element getAsDOM(Document document) {
 		Element eplans = (Element) document.createElement("plans");
 		String lastFunctor = null;
 		for (Plan p: plans) {
 			String currentFunctor = p.getTriggerEvent().getLiteral().getFunctor();
 			if (lastFunctor != null && !currentFunctor.equals(lastFunctor)) {
 				eplans.appendChild((Element) document.createElement("new-set-of-plans"));
 			}
 			lastFunctor = currentFunctor;
 			eplans.appendChild(p.getAsDOM(document));
 		}
 		return eplans;
 	}
 }
