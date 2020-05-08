 package uk.ac.gla.dcs.tp3.w.algorithm;
 
 import java.util.ArrayList;
 import java.util.Stack;
 
 import uk.ac.gla.dcs.tp3.w.league.DateTime;
 import uk.ac.gla.dcs.tp3.w.league.Division;
 import uk.ac.gla.dcs.tp3.w.league.Match;
 import uk.ac.gla.dcs.tp3.w.league.Team;
 
 /**
  * This class represents the Ford-Fulkerson algorithm and any associated
  * extensions such as the certificate of elimination.
  * 
  * The class creates a graph of the given division and has methods to run
  * through the Ford-Fulkerson algorithm to determine elimination status.
  * 
  * @author Team W
  * @version 1.0
  */
 public class Algorithm {
 
 	private Graph g;
 	private Division d;
 	private static boolean verbose = false;
 
 	/**
 	 * No argument constructor.
 	 */
 	public Algorithm() {
 		this(null);
 	}
 
 	/**
 	 * Parameterised constructor.
 	 * 
 	 * @param Division
 	 *            div
 	 */
 	public Algorithm(Division div) {
 		g = null;
 		d = div;
 	}
 
 	/**
 	 * Returns this algorithms Graph
 	 * 
 	 * @return Graph
 	 */
 	public Graph getG() {
 		return g;
 	}
 
 	/**
 	 * Sets this algorithms Graph
 	 * 
 	 * @param Graph
 	 *            g
 	 */
 	public void setG(Graph g) {
 		this.g = g;
 	}
 
 	/**
 	 * Returns this algorithms Division
 	 * 
 	 * @return Division
 	 */
 	public Division getD() {
 		return d;
 	}
 
 	/**
 	 * Sets this algorithms Division
 	 * 
 	 * @param Division
 	 *            d
 	 */
 	public void setD(Division d) {
 		this.d = d;
 	}
 
 	/**
 	 * Checks whether or not a specified team is eliminated.
 	 * 
 	 * The algorithm is only run if the teams status is currently false.
 	 * 
 	 * @param Team
 	 *            t
 	 * @return boolean
 	 */
 	public boolean isEliminated(Team t) {
 		// If the team is already known to be eliminated, just return true.
 		return t.isEliminated() ? true : fordFulkerson(t);
 	}
 
 	/**
 	 * Sets the verbose flag for the algorithm, useful information is printed on
 	 * System.out
 	 */
 	public void setVerbose() {
 		verbose = true;
 	}
 
 	private boolean fordFulkerson(Team t) {
 		// Create initial graph
 		g = new Graph(d, t);
 		// For each edge in the graph g, set the flow to be 0.
 		for (Vertex v : g.getV())
 			for (AdjListNode n : v.getAdjList())
 				n.setFlow(0);
 		// Cap states the maximum possible flow away from source.
 		int cap = 0;
 		for (AdjListNode a : g.getSource().getAdjList())
 			cap += a.getCapacity();
		// Create initial residual graph for algorithm.
		ResidualGraph residual = new ResidualGraph(g);
 		// Short circuit for end of league
 		if (t.getUpcomingMatches().size() == 0
 				&& t.getPoints() != d.maxPoints()) {
 			t.setTrivial(true);
			return certificateOfElimination(residual, t);
 		}
 		// Naive elimination short circuit
 		if (t.getUpcomingMatches().size() + t.getPoints() < d.maxPoints()) {
 			t.setTrivial(true);
 			return certificateOfElimination(residual, t);
 		}
 		// Path will store the residual path (if it exists)
 		Path path;
 		// Algorithm continues while a residual path exists
 		while ((path = residualPath(residual)) != null) {
 			// Reduce cap by the amount of flow that can be added in this
 			// iteration.
 			cap -= path.getCapacity();
 			// For each node in the path, add/remove the path's capacity from
 			// the edges flow. Add to forward edges, remove from backward edges.
 			int[] pathnodes = path.getPath();
 			for (int j = 1; j < pathnodes.length; j++) {
 				int i = j - 1;
 				if (pathnodes[i] < pathnodes[j]) {
 					for (AdjListNode a : g.getV()[pathnodes[i]].getAdjList())
 						// Forward edge, add flow.
 						if (a.getVertex().getIndex() == pathnodes[j])
 							a.setFlow(a.getFlow() + path.getCapacity());
 				} else {
 					for (AdjListNode a : g.getV()[pathnodes[i]].getAdjList())
 						// Backward edge, remove flow.
 						if (a.getVertex().getIndex() == pathnodes[j])
 							a.setFlow(a.getFlow() - path.getCapacity());
 				}
 			}
 			// Update residual graph based on new original graph's flow data.
 			residual = new ResidualGraph(g);
 		}
 		if (verbose) {
 			int teamToSinkRemain = 0;
 			int minTeamToSink = Integer.MAX_VALUE;
 			int floatOut = 0;
 			for (AdjListNode a : g.getV()[0].getAdjList()) {
 				floatOut += (a.getCapacity());
 			}
 			for (Vertex v : g.getV()) {
 				if (g.getV()[v.getIndex()] instanceof TeamVertex) {
 					TeamVertex TV = (TeamVertex) g.getV()[v.getIndex()];
 					for (AdjListNode a : TV.getAdjList()) {
 						int next = (a.getCapacity() - a.getFlow());
 						teamToSinkRemain += next;
 						if (next < minTeamToSink)
 							minTeamToSink = next;
 					}
 				}
 			}
 			System.out.println("Total From Float: " + floatOut);
 			System.out.println("Total Remaining Games: " + teamToSinkRemain);
 			System.out.println("Min from team to sink: " + minTeamToSink);
 		}
 		// If final flow of graph is saturating, team has not been eliminated,
 		// return false.
 		// Otherwise, team has been eliminated, return true.
 		if (cap != 0)
 			return certificateOfElimination(residual, t);
 		return false;
 	}
 
 	private boolean certificateOfElimination(ResidualGraph residual, Team t) {
 		// Work out which teams are responsible for eliminating team t from the
 		// residual graph.
 		residual.certificateOfEliminationHelper();
 		// Store the teams responsible in an ArrayList with Team t.
 		for (Vertex v : residual.getV()) {
 			if (g.getV()[v.getIndex()] instanceof TeamVertex) {
 				TeamVertex elim = (TeamVertex) g.getV()[v.getIndex()];
 				if (v.getVisited())
 					t.getEliminatedBy().add(elim.getTeam());
 			}
 		}
 		if (verbose) {
 			// Test output from above
 			System.out.println("\n" + t.getName() + " eliminated by:");
 			for (Team elimBy : t.getEliminatedBy())
 				System.out.println(elimBy.getName());
 			System.out.println();
 		}
 		return true;
 	}
 
 	private static Path residualPath(ResidualGraph g) {
 		// Perform BFS from source on graph
 		g.bfs();
 		// Since we'll work out a path from sink to source, it will be added to
 		// a
 		// stack then popped until empty to store path in correct direction.
 		Stack<Integer> backPath = new Stack<Integer>();
 		// Use matrix representation of edge capacities to speed up this method.
 		int[][] matrix = g.getMatrix();
 		int next, current, capacity = Integer.MAX_VALUE;
 		// Start at sink, try to work back up to source (vertex 0)
 		Vertex v[] = g.getV();
 		next = current = g.getSink().getIndex();
 		// Until we either get stuck, or get to the source, keep going.
 		while (true) {
 			// If we're stuck outside the source, we're finished.
 			if (v[next].getPred() == next && next != 0)
 				return null;
 			// Add next node to the path.
 			backPath.add(next);
 			// Ensure current path capacity is at it's lowest possible value
 			if (current != next && matrix[next][current] < capacity)
 				capacity = matrix[next][current];
 			// If we're at the source, one path has been found.
 			if (next == 0)
 				break;
 			// Bump up one step up the path
 			current = next;
 			next = v[next].getPred();
 		}
 		// Pop everything from the stack to make the path in source->sink order.
 		int[] path = new int[backPath.size()];
 		for (int i = 0; i < path.length; i++)
 			path[i] = backPath.pop();
 		return new Path(path, capacity);
 	}
 
 	/**
 	 * Updates the entire Divisions elimination status
 	 * 
 	 * This method sorts the Teams in non-descending order by wins and games
 	 * remaining and then uses binary search to eliminate teams.
 	 * 
 	 * @param Division
 	 *            d
 	 */
 	public void updateDivisionElim() {
 		Team[] teams = d.teamsToArray();
 		// Sorts teams into non-descending order by wins and games remaining.
 		// Bubble sort is sufficient for data size.
 		for (int i = 0; i < teams.length; i++)
 			for (int j = i; j < teams.length; j++)
 				if (teams[i].compareTo(teams[j]) > 0) {
 					Team temp = teams[i];
 					teams[i] = teams[j];
 					teams[j] = temp;
 				}
 		// Determine the highest team that has been eliminated.
 		int lastElim = binaryDetermine(teams, 0, teams.length, -1);
 		// Eliminate this team and all teams below it.
 		// Store array of teams responsible for eliminating the team.
 		// Every team has been eliminated by all teams not-eliminated below it
 		// in table.
 		for (int i = lastElim; i >= 0; i--)
 			teams[i].setEliminated(true);
 	}
 
 	private int binaryDetermine(Team[] T, int s, int e, int highestElim) {
 		// Stop conditions
 		if (e < s || s > e || s < 0 || e > T.length)
 			return highestElim;
 		// Binary search
 		int mid = (s + e) / 2;
 		// If reached end of array...
 		if (mid >= T.length)
 			return highestElim;
 		// If team at middle of range has been eliminated...
 		else if (fordFulkerson(T[mid]))
 			return binaryDetermine(T, mid + 1, e, mid);
 		// If team at middle of range has not been eliminated...
 		else
 			return binaryDetermine(T, s, mid - 1, highestElim);
 	}
 	
 	
 	
 	
 	
 
 /*
 	 * Finds the first Non trivially eliminated team for a division . Sets this
 	 * value in Divion
 	 */
 	public void FirstNonTrivTeamElim() {
 		// mid point
 		int mid = (d.getFixtures().size() / 2);
 		DateTime targetDate = new DateTime(d.getFixtures().get(mid)
 				.getDateTime());
 		boolean singleInstance = false;
 		while (d.getFirstNTTeamElim() ==null) {
 			// set date
 			UpdateMatches(d, targetDate);
 			int nonTrivCount = 0;
 
 			//check bottom team 
 			if (d.getTeams().get(0).isEliminated()) {
 				
 				// look at eliminated teams			
 				for (int y = 0; y < d.getTeams().size(); y++) {
 					
 					if (d.getFirstNTTeamElim() ==null){
 					
 					Team t = d.getTeams().get(y);
 					int TopTeamsPointsCheck = d.maxPoints();
 					int ElimTeamsPointscheck = t.getPoints();					
 					if (t.isEliminated()) {			
 						boolean TeamElimCheck = false ;
 						//get date of elimination 
 						while (t.isEliminated()){
 							
 							TeamElimCheck = t.isEliminated();
 							DateTime td = new DateTime(d.getFixtures().get(mid+1).getDateTime() );
 							mid = mid +1;
 							UpdateMatches (d, td);
 							t.isEliminated();
 						}
 						//bump to elim
 						DateTime elimdate = new DateTime( d.getFixtures().get(mid+1).getDateTime());
 						UpdateMatches (d, elimdate);
 						
 						// Trivial check
 						int pointDiff = (d.maxPoints() - t.getPoints());						
 						
 						int matchesLeft = t.getUpcomingMatches().size();
 						
 						
 						if (pointDiff <= t.getUpcomingMatches().size()
 								&& nonTrivCount <= 1) {
 							d.setFirstNTTeamElim(t);
 							d.setFirstNTTeamElimdate(elimdate);
 							nonTrivCount++;
 
 							// Move to previous date in season
 							if (nonTrivCount > 1) {
 								nonTrivCount = 0;
 								mid = mid + ((d.getFixtures().size() - mid) / 2);
 								
 								targetDate = d.getFixtures().get(mid)
 										.getDateTime();
 							}
 							
 						}//no team found 
 							
 						}
 					}// is eliminated
 				}// each team
 			}
 
 			// move forward in season
 			if (d.getFirstNTTeamElim() == null) {
 				// new later target date
 				mid = (mid / 2);
 				targetDate = d.getFixtures().get(mid).getDateTime();
 			}
 
 			if (nonTrivCount == 1)
 				singleInstance = true;
 
 		} // while single instance
 
 		
 	}
 	
 	// loop through every game played in the current division,
 		// check if date is less than/equal to current date,
 		// if not unplay match
 		private void UpdateMatches(Division d, DateTime dt) {
 			
 				for (Match m : d.getFixtures()) {
 					if (m.getDateTime().before(dt)) {
 						m.playMatch();
 					} else {
 						m.unplayMatch();
 					}
 				}
 				for (Team t : d.getTeams()) {
 					t.setEliminated(false);
 					ArrayList<Team> teams = t.getEliminatedBy();
 					t.getEliminatedBy().removeAll(teams);
 				}
 				(new Algorithm(d)).updateDivisionElim();			
 		
 		}
 	
 	
 	
 	
 	
 
 	
 	
 }
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 
