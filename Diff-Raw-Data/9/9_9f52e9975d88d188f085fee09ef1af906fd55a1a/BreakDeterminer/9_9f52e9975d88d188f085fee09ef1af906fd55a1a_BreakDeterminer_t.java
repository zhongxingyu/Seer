 package com.daylenyang.tab;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.daylenyang.graph.Graph;
 import com.daylenyang.graph.Node;
 
 /**
  * Chooses the teams that will break given a list of tied teams.
  * 
  * @author daylenyang
  * 
  */
 public class BreakDeterminer implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 8801451296919417811L;
 	private List<Team> tiedTeams;
 	private List<Round> rounds;
 	private List<Team> tiedTeamsToBreak;
 	private Map<Student, List<Double>> speakerPoints;
 
 	private Map<Team, List<Team>> mapOfBeatTeamsForEachTeam;
 
 	private Graph graph;
 
 	public BreakDeterminer(List<Team> tiedTeams, int numToPick,
 			List<Round> rounds, Map<Student, List<Double>> speakerPoints) {
 
 		this.tiedTeams = tiedTeams;
 		this.rounds = rounds;
 		tiedTeamsToBreak = new ArrayList<Team>();
 		this.speakerPoints = speakerPoints;
 
 		mapOfBeatTeamsForEachTeam = new HashMap<Team, List<Team>>();
 		buildMapOfBeatTeamsForEachTeam();
 
 		graph = new Graph();
 
 		addTeamsToGraph();
 		addEdgesToGraph();
 
 		selectTeamsViaHeadToHead(graph, numToPick, tiedTeamsToBreak);
 	}
 
 	private void addTeamsToGraph() {
 		for (Team t : tiedTeams) {
 			Node n = new Node(t);
 			graph.addNode(n);
 		}
 	}
 
 	private void addEdgesToGraph() {
 		for (Team t : tiedTeams) {
 			// Find the team in the graph
 			Node n = graph.getNodeForData(t);
 			Set<Team> teamsThisTeamBeat = new HashSet<Team>(tiedTeams);
 			if (mapOfBeatTeamsForEachTeam.get(t) != null)
 				teamsThisTeamBeat.retainAll(mapOfBeatTeamsForEachTeam.get(t));
 			else
 				teamsThisTeamBeat.clear();
 			for (Team beatenTeam : teamsThisTeamBeat) {
 				Node beatenNode = graph.getNodeForData(beatenTeam);
 				graph.addEdge(n, beatenNode);
 			}
 		}
 	}
 
 	private void buildMapOfBeatTeamsForEachTeam() {
 		for (Round r : rounds) {
 			for (Pair p : r.getPairs()) {
 				Team winningTeam;
 				Team losingTeam;
 				if (p.getAffBallots() > p.getNegBallots()) {
 					// aff wins
 					winningTeam = p.getAffTeam();
 					losingTeam = p.getNegTeam();
 				} else if (p.getNegBallots() > p.getAffBallots()) {
 					// neg wins
 					winningTeam = p.getNegTeam();
 					losingTeam = p.getAffTeam();
 				} else {
 					continue;
 				}
 				// is winning team in the map?
 				if (mapOfBeatTeamsForEachTeam.containsKey(winningTeam)) {
 					// yes, just add the losing team
 					mapOfBeatTeamsForEachTeam.get(winningTeam).add(losingTeam);
 				} else {
 					// no, create the list and add it
 					ArrayList<Team> losingTeamWrapper = new ArrayList<Team>();
 					losingTeamWrapper.add(losingTeam);
 					mapOfBeatTeamsForEachTeam.put(winningTeam,
 							losingTeamWrapper);
 				}
 
 			}
 		}
 	}
 
 	private boolean selectTeamsViaHeadToHead(Graph graph, int numToPick,
 			List<Team> tiedTeamsToBreak) {
 
 		// Count number of dominant and island teams
 		int numDominantAndIslandTeams = graph.getStrongNodes().size()
 				+ graph.getIslandNodes().size();
 
 		if (numDominantAndIslandTeams == numToPick) {
 			// wow, what a coincidence! we are done
 			for (Node n : graph.getStrongNodes())
 				tiedTeamsToBreak.add((Team) n.getData());
 			for (Node n : graph.getIslandNodes())
 				tiedTeamsToBreak.add((Team) n.getData());
 			return true;
 		} else if (numDominantAndIslandTeams > numToPick) {
 			// take them all and proceed to the next stage
 			return selectTeamsViaSpeakerPoints(graph, numToPick,
 					tiedTeamsToBreak);
 		} else {
 			// not enough. we need to run this again recursively

			// it's important that we remove island nodes first. if we remove
			// strong nodes first, then potentially that would create more
			// island nodes
			for (Node n : graph.getIslandNodes()) {
 				tiedTeamsToBreak.add((Team) n.getData());
 				graph.removeNode(n);
 				numToPick--;
 			}
 
			for (Node n : graph.getStrongNodes()) {
 				tiedTeamsToBreak.add((Team) n.getData());
 				graph.removeNode(n);
 				numToPick--;
 			}
 
 			return selectTeamsViaHeadToHead(graph, numToPick, tiedTeamsToBreak);
 		}
 	}
 
 	private boolean selectTeamsViaSpeakerPoints(Graph graph, int numToPick,
 			List<Team> tiedTeamsToBreak) {
 		List<TeamPlusSpeakerPoints> teamsToConsider = new ArrayList<TeamPlusSpeakerPoints>();
 
 		for (Node n : graph.getStrongNodes()) {
 			Team t = (Team) n.getData();
 			teamsToConsider.add(new TeamPlusSpeakerPoints(t,
 					getTotalPointsForTeam(t)));
 		}
 
 		for (Node n : graph.getIslandNodes()) {
 			Team t = (Team) n.getData();
 			teamsToConsider.add(new TeamPlusSpeakerPoints(t,
 					getTotalPointsForTeam(t)));
 		}
 
 		Collections.sort(teamsToConsider);
 		Collections.reverse(teamsToConsider);
 
 		for (int i = 0; i < numToPick; i++) {
 			tiedTeamsToBreak.add(teamsToConsider.get(i).t);
 		}
 
 		return true;
 
 	}
 
 	private double getTotalPointsForTeam(Team t) {
 		if (t instanceof NullTeam)
 			return 0;
 		return getTotalForList(speakerPoints.get(t.getStudents()[0]))
 				+ getTotalForList(speakerPoints.get(t.getStudents()[1]));
 	}
 
 	private double getTotalForList(List<Double> list) {
 		double total = 0;
 		for (Double d : list) {
 			total += d;
 		}
 		return total;
 	}
 
 	public static class TeamPlusSpeakerPoints implements
 			Comparable<TeamPlusSpeakerPoints> {
 
 		Team t;
 		double points;
 
 		public TeamPlusSpeakerPoints(Team t, double points) {
 			this.t = t;
 			this.points = points;
 		}
 
 		@Override
 		public int compareTo(TeamPlusSpeakerPoints otherTeam) {
 			if (points > otherTeam.points) {
 				return 1;
 			} else if (points < otherTeam.points) {
 				return -1;
 			} else {
 				return 0;
 			}
 		}
 
 		@Override
 		public String toString() {
 			return t.toString() + "=" + points;
 		}
 
 	}
 
 	public List<Team> getTiedTeamsToBreak() {
 		return tiedTeamsToBreak;
 	}
 
 }
