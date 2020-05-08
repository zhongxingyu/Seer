 package apapl.deliberation;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.LinkedList;
 
 import apapl.APLModule;
 import apapl.SubstList;
 import apapl.data.Goal;
 import apapl.data.Prohibition;
 import apapl.data.Term;
 import apapl.plans.Plan;
 import apapl.plans.PlanSeq;
 import apapl.program.BeliefUpdates;
 import apapl.program.Beliefbase;
 import apapl.program.Goalbase;
 import apapl.program.PGrule;
 import apapl.program.PGrulebase;
 import apapl.program.Planbase;
 import apapl.program.Prohibitionbase;
 import apapl.program.Schedule;
 
 public class SchedulePlans implements DeliberationStep {
 
 	public DeliberationResult execute(APLModule module) {
 
 		Planbase planbase = module.getPlanbase();
 		// Planbase atomicplans = module.getAtomicPlanbase();
 		Prohibitionbase prohibitions = module.getProhibitionbase();
 		BeliefUpdates bu = module.getBeliefUpdates();
 
 		schedulePlans(planbase, prohibitions, bu, module.getAtomic());
 		// scheduleAtomicPlans(atomicplans, prohibitions, bu);
 		module.setNewAtomic();
 		
 		return new SchedulePlansResult();
 	}
 
 	private void schedulePlans(Planbase planbase, Prohibitionbase prohibitions,
 			BeliefUpdates bu, PlanSeq atomic) {
 
 		ArrayList<PlanSeq> newAtomic = new ArrayList<PlanSeq>();
 		
 		ArrayList<PlanSeq> newNonAtomic = new ArrayList<PlanSeq>();
 		
 		planbase.sortPlans();
 
 		for (PlanSeq ps : planbase) {
 			
 			//atomic
 			
 			ArrayList<PlanSeq> tempAtomic;
 			
 			if (ps.isAtomic()) {
 				if (atomic == ps) {
 					
 					tempAtomic = newAtomic;
 					
 				} else {
 
 					Date ne = new Date();
 					tempAtomic = new ArrayList<PlanSeq>();
 					
 					for (PlanSeq p : newAtomic) {
 						if (p.getDeadline().getTime() <= ps.getDeadline()
 								.getTime()) {
 							tempAtomic.add(p);
 							ne = new Date(Math.max(new Date(p.getDeadline()
 									.getTime() - p.getExecStart().getTime())
 									.getTime(), ne.getTime()));
 						} else {
 							p.setExecStart(new Date(p.getExecStart().getTime()
 									+ ps.getDuration()));
 							tempAtomic.add(p);
 						}
 					}
 
 					ps.setExecStart(ne);
 				}
 				
				boolean pass = false;
 
 				if (!violatesProhibitions(ps, prohibitions, bu))
 					for (PlanSeq p1 : tempAtomic) {
 						Date now = new Date();
 						long rt = 0;
 
 						for (PlanSeq p2 : tempAtomic) {
 							if (!p2.equals(p1)
 									&& p2.getExecStart().getTime() <= p1
 											.getExecStart().getTime()) {
 								rt += p2.getDeadline().getTime()
 										- p2.getExecStart().getTime();
 							}
 						}
 
 						if (now.getTime() + rt <= p1.getDeadline().getTime()) {
 							pass = false;
 							break;
 						}
 					}
 
 				if (pass)
 					newAtomic.add(ps);
 
 
 		//non atomic
 			} else {
 					if (passNorms(ps, prohibitions, bu)) {						
 						ps.setExecStart(null);
 						newNonAtomic.add(ps);						
 					}
 				}
 
 
 			}
 		
 		planbase.removePlans();
 		
 		for (PlanSeq ps1 : newNonAtomic) {
 			planbase.addPlan(ps1);
 		}
 		for (PlanSeq ps1 : newAtomic) {
 			planbase.addPlan(ps1);
 		}
 		
 		}
 
 
 	public String toString() {
 		return "Schedule Plans";
 	}
 
 	private boolean passNorms(PlanSeq ps, Prohibitionbase prohibitions,
 			BeliefUpdates bu) {
 
 		if (violatesProhibitions(ps, prohibitions, bu))
 			return false;
 
 		Date deadline = ps.getDeadline();
 		if (deadline == null)
 			return true;
 
 		Date started = ps.getExecStart();
 		Date now = new Date();
 		if (started == null) {
 			if (ps.getDuration() + now.getTime() > deadline.getTime())
 				return false;
 		} else {
 			long executed = now.getTime() - started.getTime();
 			if (ps.getDuration() - executed > deadline.getTime()
 					- now.getTime())
 				return false;
 		}
 
 		return true;
 	}
 
 	private boolean violatesProhibitions(PlanSeq ps,
 			Prohibitionbase prohibitions, BeliefUpdates bu) {
 
 		ArrayList<Prohibition> hpp = prohibitions.getHigher(ps.getPriority());
 
 		if (hpp != null) {
 			for (Prohibition p : hpp) {
 				if (p.existIn(ps, bu))
 					return true;
 			}
 		}
 		return false;
 	}
 
 }
