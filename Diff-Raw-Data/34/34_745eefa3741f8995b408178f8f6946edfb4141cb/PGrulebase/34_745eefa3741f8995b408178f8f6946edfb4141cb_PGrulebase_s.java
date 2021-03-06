 package apapl.program;
 
 import apapl.APLModule;
 import apapl.Parser;
 import apapl.plans.ChunkPlan;
 import apapl.plans.Plan;
 import apapl.plans.PlanSeq;
 import apapl.program.PGrule;
 import apapl.data.Goal;
 import apapl.data.Prohibition;
 import apapl.data.Term;
 import apapl.data.Query;
 import apapl.program.Rule;
 import apapl.data.True;
 import java.util.ArrayList;
 import java.util.Date;
 
 import apapl.SubstList;
 import javax.swing.JComponent;
 import javax.swing.JTextArea;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import java.awt.GridLayout;
 import java.awt.BorderLayout;
 import java.awt.Color;
 
 public class PGrulebase extends Rulebase<PGrule>
 {
 	
 	/**
 	 * Equal to 
 	 * {@link apapl.program.PGrulebase#generatePlans(goalbase,beliefbase,planbase,atomicplans,false)}
 	 * @param atomicplans 
 	 * @param prohibitions 
 	 */
	public ArrayList<PlanSeq> generatePlans(Goalbase goalbase, Beliefbase beliefbase, Planbase planbase, Planbase atomicplans, Prohibitionbase prohibitions, BeliefUpdates bu)
 	{
 		return generatePlans(goalbase,beliefbase,planbase,prohibitions,bu,false);
 	}
 	
 	/**
 	 * NEVER CALLED
 	 * Equal to
 	 * {@link apapl.program.PGrulebase#generatePlans(goalbase,beliefbase,planbase,atomicplans,true)}
 	 */
 	public ArrayList<PlanSeq> generatePlan(Goalbase goalbase, Beliefbase beliefbase, Planbase planbase)
 	{
 		return generatePlans(goalbase,beliefbase,planbase,null,null,true);
 	}
 	
 	/**
 	 * Returns a list of plans that can be generated by applying the PG rules. A PG-rule
 	 * generates a plan if the head matches some goal and if the guard is satisfied by the
 	 * beliefs and if the module is not already working on a plan that has been generated
 	 * by the same rule for the same goal. In generating plans, each PG-rule is tried only 
 	 * once.
 	 * 
 	 * @param goalbase goalbase that is needed to select a rule
 	 * @param beliefbase beliefbase that is needed to select a rule
 	 * @param planbase planbase that is needed to check whether a rule may be selected
 	 * @param prohibitions 
 	 * @param onlyone if true, only one plan will be generated
 	 * @return an list containing one or more plans that can be generate with the PG rules
 	 */
 	public ArrayList<PlanSeq> generatePlans(Goalbase goalbase, Beliefbase beliefbase, Planbase planbase, Prohibitionbase prohibitions, BeliefUpdates bu, boolean onlyone)
 	{
 		ArrayList<PlanSeq> plans = new ArrayList<PlanSeq>();
 				
 		// for each rule
 		for (PGrule pgrule : rules)
 		{ // if it is a reactive rule, try to match the guard with the beliefs
 		  if (pgrule.getHead() instanceof True)
 		  { SubstList<Term> theta = new SubstList<Term>();
 				PlanSeq p = tryRule(pgrule.clone(),pgrule,theta,beliefbase,planbase,null);
 				if (p!=null)
 				{ 
					//plans.add(p);
 					Object atomic = p.getPlans().getFirst();
 					if (atomic instanceof ChunkPlan)
 					{
 						atomic = (ChunkPlan) atomic;
 						((ChunkPlan) atomic).toPlanSeq();
 						//atomicplans.addPlan((PlanSeq) atomic);
 						plans.add((PlanSeq)atomic);
 					}
 					else
 					{
 					plans.add(p);
 				    planbase.addPlan(p);
 					
 					}
 					if (onlyone) return plans;
 				}
 			}
 			// if it is not a reactive rule, try to match the head with the goals
 			else for (Goal goal : goalbase.sorted())
 			{ boolean ruleApplied = false;
 			  ArrayList<SubstList<Term>> substs;
 				PGrule variant = pgrule.getVariant(goal.getVariables());
 				substs = goal.possibleSubstitutions(variant.getHead());
 				
 				// for all possible substitutions of the head of the rule, try to match
 				// it with the guard of the rule and check if the module is not already
 				// working on a plan for the same goal
 				for (SubstList<Term> theta : substs)
 				{ PlanSeq p = tryRule(variant,pgrule,theta,beliefbase,planbase,goal);
 					if (p!=null)
 					{ 
 					  ruleApplied = true;
					  //plans.add(p);
 					  
 					  
 					  Object atomic = p.getPlans().getFirst();
 						if (atomic instanceof ChunkPlan)
 						{
 							atomic = (ChunkPlan) atomic;
 							((ChunkPlan) atomic).toPlanSeq();
 							//atomicplans.addPlan((PlanSeq) atomic);
 							plans.add((PlanSeq)atomic);
 						}
 						else
 						{
 					    
 					    planbase.addPlan(p);
 					    plans.add(p);
 						}
 					  
 					  
 					  if (onlyone) return plans;
 						break;
 					}
 				}
 
 				if( ruleApplied ) break; 
 			}
 		}
 		return plans;
 	}
 			
 	
 	/**
 	 * Tries to apply a PG-rule given substitution theta.
 	 * 
 	 * @param pgrule the PGrule to be applied
 	 * @param theta the substitutions that are needed to match the rule
 	 * @param beliefs belief base
 	 * @param plans plan base
 	 * @return the body of the rule with theta applied to it or null if this rule 
 	 *   cannot be applied
 	 */
 	private PlanSeq tryRule(PGrule variant, PGrule pgrule, SubstList<Term> theta, Beliefbase beliefs, Planbase planbase, Goal goal)
 	{
 		variant.applySubstitution(theta);
 		Query goalquery = variant.getHead();
 		Query beliefquery = variant.getGuard();
 		SubstList<Term> goaltheta = clone(theta);
 		// Goal is still a goal of the module
 		if (!beliefs.doQuery(goalquery,theta)||goalquery instanceof True)
 		// Guard is satisfied
 		if (beliefs.doQuery(beliefquery,theta))
 		{ PlanSeq p = variant.getBody();
 			p.applySubstitution(theta);
 			p.setActivationRule(pgrule);
 			p.setActivationGoal(goaltheta);
 			p.setActivationSubstitution(theta);
 
 			p.setDuration(pgrule.getDuration());
 			if (goal != null)
 			{
 				p.setDeadline(goal.getDeadline());
 				p.setPriority(goal.getPriority());
 			}
 			// For the special case we are dealing with a reactive rule (head is True)
 			// it should not be the case that the module is working on an instance of
 			// the same rule
 			if( goalquery instanceof True && !planbase.ruleOccurs( p.getActivationRule() ) )
 			{ return p;
 			}
 			// Otherwise, the module might not be already working on a plan that was
 			// generated by the same rule for this goal
 			else if (!planbase.sameRuleActiveForSameGoal( pgrule, theta ) )
 			{ return p;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Clones a substitution.
 	 * 
 	 * @note this is not the place for this function. It should be defined in SubstList.
 	 * 
 	 * @param theta the substitution to be cloned.
 	 * @return the cloned substitution
 	 */
 	private SubstList<Term> clone(SubstList<Term> theta)
 	{
 		SubstList<Term> theta2 = new SubstList<Term>();
 		theta2.putAll(theta);
 		return theta2;
 	}
 	
 	/**
 	 * @return  clone of the PG rulebase
 	 */
 	public PGrulebase clone()
 	{
 		PGrulebase b = new PGrulebase(); 
 		b.setRules(getRules());
 		return b;
 	}
 	
 }
