 package jia;
 
 import jason.asSemantics.DefaultInternalAction;
 import jason.asSemantics.TransitionSystem;
 import jason.asSemantics.Unifier;
 import jason.asSyntax.ASSyntax;
 import jason.asSyntax.ListTerm;
 import jason.asSyntax.ListTermImpl;
 import jason.asSyntax.Term;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.logging.Logger;
 
 import model.Entity;
 import model.graph.Graph;
 import model.graph.Vertex;
 import model.graph.VertexComparator;
 import model.graph.ZoneComparator;
 import arch.CoordinatorArch;
 import arch.WorldModel;
 import env.Percept;
 
 /**
  * Verify which is the team best zone in a determined time and returns a list of
  * agents and positions to increase that zone.
  * </p>
  * Use: jia.agents_coordination(-A,-P); </br>
  * Where: A is the list of agents and P the list of target positions.
  * 
  * @author mafranko
  */
 public class agents_coordination extends DefaultInternalAction {
 
 	private static final long serialVersionUID = -6858228332440013608L;
 
 	static Logger logger = Logger.getLogger(agents_coordination.class.getName());
 
 	private static final VertexComparator comparator = new VertexComparator();
 	private static final ZoneComparator zoneComparator = new ZoneComparator();
 
 	private WorldModel model;
 
 	@Override
 	public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
 		ListTerm positions = new ListTermImpl();
 		ListTerm agents = new ListTermImpl();
 
 		model = ((CoordinatorArch) ts.getUserAgArch()).getModel();
 		Graph graph = model.getGraph();
 
 		model.coloringGraph();
 		List<List<Vertex>> actualZones = graph.getZones();
 		// order zones by vertex value
 		Collections.sort(actualZones, zoneComparator);
 
 		List<List<Vertex>> bestZones =  graph.getBestZones();	// the best zones in the graph
 		if (!bestZones.isEmpty()) {
 			if (bestZones.size() == 2) {
 				ListTerm[] listTerms = coordinateGroup(1, actualZones, bestZones.get(0));
 				agents.addAll(listTerms[0]);
 				positions.addAll(listTerms[1]);
 				listTerms = coordinateGroup(2, actualZones, bestZones.get(1));
 				agents.addAll(listTerms[0]);
 				positions.addAll(listTerms[1]);
 			} else {
 				ListTerm[] listTerms = coordinateGroup(3, actualZones, bestZones.get(0));
 				agents.addAll(listTerms[0]);
 				positions.addAll(listTerms[1]);
 			}
 
 		} else {
 			ListTerm[] listTerms = coordinateGroup(3, actualZones, null);
 			agents.addAll(listTerms[0]);
 			positions.addAll(listTerms[1]);
 		}
 		return un.unifies(terms[0], agents) & un.unifies(terms[1], positions);
 	}
 
 	private ListTerm[] coordinateGroup(int group, List<List<Vertex>> actualZones, List<Vertex> bestZone) {
 		ListTerm positions = new ListTermImpl();
 		ListTerm agents = new ListTermImpl();
 
 		String[] missions;
 		if (group == 1) {
 			missions = new String[] {"mOccupyZone1", "mRepairZone1"};
 		} else if (group == 2) {
 			missions = new String[] {"mOccupyZone2", "mRepairZone2"};
 		} else {
 			missions = new String[] {"mOccupyZone1", "mRepairZone1", "mOccupyZone2", "mRepairZone2"};
 		}
 
 		List<Entity> coworkers = model.getCoworkersWithMission(Arrays.asList(missions));
 
 		Graph graph = model.getGraph();
 
 		List<Vertex> groupZone = null;
 		if (bestZone != null && !bestZone.isEmpty()) {
 			for (List<Vertex> zone : actualZones) {
 				if (graph.hasAtLeastOneVertexOnZone(zone, bestZone)) {
 					groupZone = zone;
 					continue;
 				}
 			}
 		} else if (actualZones != null && !actualZones.isEmpty()) {
 			groupZone = actualZones.get(0);
 		}
 
 		List<Vertex> targets = new ArrayList<Vertex>();
 		if (groupZone == null || groupZone.isEmpty()) {
			targets.addAll(bestZone);
 		} else {
 			if (bestZone != null && !bestZone.isEmpty()) {
 				bestZone.removeAll(groupZone);
 				targets.addAll(bestZone);
 			}
 
 			// zone neighbors
 			List<Vertex> zoneNeighbors = model.getZoneNeighbors(groupZone);
 			// order neighbors by vertex value
 			Collections.sort(zoneNeighbors, comparator);
 
 			targets.addAll(zoneNeighbors);
 		}
 
 		if (targets.isEmpty()) {
 			return new ListTerm[] {agents, positions};
 		}
 
 		for (Entity coworker : coworkers) {
 			if (coworker.getStatus().equals(Percept.STATUS_DISABLED)) {
 				continue;
 			}
 			Vertex target = null;
 			Vertex agentPosition = coworker.getVertex();
 			if (bestZone.contains(agentPosition) && model.isFrontier(agentPosition)) {	// the agent is part of the best zone's frontier
 				List<Entity> agsOnSameVertex = model.getCoworkersOnSameVertex(coworker);
 				if (!agsOnSameVertex.isEmpty()) {	// if there are other agents on the same vertex
 					boolean canMove = true;
 					for (Entity ag : agsOnSameVertex) {
 						if (ag.getId() > coworker.getId()) {
 							canMove = false;	// only the agent with the lower id can move
 							break;
 						}
 					}
 					if (!canMove) {
 						continue;
 					}
 				} else {
 					if (model.hasActiveCoworkersOnNeighbors(coworker)) {	// go to a free neighbor if has two or more coworkers on neighbors 
 						Vertex neighbor = model.getFreeNeighborOutOfZone(agentPosition);
 						if (null != neighbor) {
 							agents.add(ASSyntax.createString(coworker.getName()));
 							positions.add(ASSyntax.createString("vertex" + neighbor.getId()));
 						}
 						continue;
 					} else {
 						continue;	// the agent must not move if he is in the frontier and has no other agent on the same vertex
 					}
 				}
 			}
 
 			if (null != targets && !targets.isEmpty()) {
 //				target = model.closerVertex(agentPosition, targets);
 				target = targets.get(0);
 				if (null != target) {
 					targets.remove(target);
 				}
 			}
 
 			if (null != target) {
 				agents.add(ASSyntax.createString(coworker.getName()));
 				positions.add(ASSyntax.createString("vertex" + target.getId()));
 			}
 		}
 		return new ListTerm[] {agents, positions};
 	}
 }
