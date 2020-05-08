 package uk.ac.gla.dcs.tp3.w.algorithm;
 
 import java.util.LinkedList;
 
 import uk.ac.gla.dcs.tp3.w.league.League;
 import uk.ac.gla.dcs.tp3.w.league.Match;
 import uk.ac.gla.dcs.tp3.w.league.Team;
 
 public class Graph {
 
 	private Vertex[] vertices;
 	private int[][] matrix;
 	private Vertex source;
 	private Vertex sink;
 
 	public Graph() {
 		this(null, null);
 	}
 
 	public Graph(League l, Team t) {
 		if (l == null || t == null)
 			return;
 		// Number of team nodes is one less than total number of teams.
 		// The team nodes do not include the team being tested for elimination.
 		int teamTotal = l.getTeams().length;
 		// The r-combination of teamTotal for length 2 is the number of possible
 		// combinations for matches between the list of Teams-{t}.
 		int gameTotal = comb(teamTotal - 1, 2);
 		// Total number of vertices is the number of teams-1 + number of match
 		// pairs + source + sink.
 		vertices = new Vertex[teamTotal + gameTotal + 1];
 		// Set first vertex to be source, and last vertex to be sink.
 		// Create blank vertices for source and sink.
 		source = new Vertex(0);
 		sink = new Vertex(vertices.length - 1);
 		vertices[0] = source;
 		vertices[vertices.length - 1] = sink;
 		// Create vertices for each team node, and make them adjacent to
 		// the sink.
 		Team[] teamsReal = l.getTeams();
 		Team[] teams = new Team[teamsReal.length - 1];
 		// Remove team T from the working list of Teams
 		int pos = 0;
 		for (Team to : teamsReal) {
 			if (!to.equals(t)) {
 				teams[pos] = to;
 				pos++;
 			}
 		}
 		// Create vertex for each team pair and make it adjacent from the
 		// source.
 		// Team[i] is in vertices[vertices.length -2 -i]
 		pos = vertices.length - 2;
 		for (int i = 0; i < teams.length; i++) {
 			vertices[pos] = new TeamVertex(teams[i], pos);
 			vertices[pos].getAdjList().add(
 					new AdjListNode(teams[i].getUpcomingMatches().length,
 							vertices[vertices.length - 1]));
 			pos--;
 		}
 		// Create vertex for each team pair and make it adjacent from the
 		// source.
 		pos = 1;
 		// TODO limit this to something more sensible
 		int infinity = Integer.MAX_VALUE;
 		for (int i = 0; i < teams.length; i++) {
 			for (int j = 1; j < teams.length; j++) {
 				vertices[pos] = new PairVertex(teams[i], teams[j], pos);
 				vertices[pos].getAdjList().add(
 						new AdjListNode(infinity, vertices[vertices.length - 2
 								- i]));
 				vertices[pos].getAdjList().add(
 						new AdjListNode(infinity, vertices[vertices.length - 2
 								- j]));
 				vertices[0].getAdjList().add(new AdjListNode(0, vertices[pos]));
 				pos++;
 			}
 		}
 		// For each match not yet played and not involving t, increment the
 		// capacity of the vertex going from home and away team node->sink and
 		// float->pair node of home and away
 
 		for (Match M : l.getFixtures()) {
			if (!M.isPlayed() && !(M.getAwayTeam().equals(t))
					|| M.getHomeTeam().equals(t)) {
 				Team home = M.getHomeTeam();
 				Team away = M.getAwayTeam();
 				for (int i = vertices.length - 2; i < teams.length; i--) {
 					TeamVertex TV = (TeamVertex) vertices[i];
 					if (TV.getTeam().equals(home)) {
 						vertices[i].getAdjList().peek().incCapacity();
 					} else if (TV.getTeam().equals(away)) {
 						vertices[i].getAdjList().peek().incCapacity();
 					}
 				}
 				for (AdjListNode A : vertices[0].getAdjList()) {
 					PairVertex PV = (PairVertex) A.getVertex();
 					if ((PV.getTeamA().equals(home) && PV.getTeamB().equals(
 							away))
 							|| PV.getTeamA().equals(away)
 							&& PV.getTeamB().equals(home)) {
 						A.incCapacity();
 					}
 				}
 			}
 		}
 
 		// TODO Create the adjacency matrix representation of the graph.
 
 	}
 
 	public Vertex[] getV() {
 		return vertices;
 	}
 
 	public void setV(Vertex[] v) {
 		this.vertices = v;
 	}
 
 	public int[][] getMatrix() {
 		return matrix;
 	}
 
 	public int getSize() {
 		return vertices.length;
 	}
 
 	public void setMatrix(int[][] matrix) {
 		this.matrix = matrix;
 	}
 
 	public Vertex getSource() {
 		return source;
 	}
 
 	public void setSource(Vertex source) {
 		this.source = source;
 	}
 
 	public Vertex getSink() {
 		return sink;
 	}
 
 	public void setSink(Vertex sink) {
 		this.sink = sink;
 	}
 
 	private static int fact(int s) {
 		// For s < 2, the factorial is 1. Otherwise, multiply s by fact(s-1)
 		return (s < 2) ? 1 : s * fact(s - 1);
 	}
 
 	private static int comb(int n, int r) {
 		// r-combination of size n is n!/r!(n-r)!
 		return (fact(n) / (fact(r) * fact(n - r)));
 	}
 
 	/**
 	 * carry out a breadth first search/traversal of the graph
 	 */
 	public void bfs() {
 		// TODO Read over this code, I (GR) just dropped this in here from
 		// bfs-example.
 		for (Vertex v : vertices)
 			v.setVisited(false);
 		LinkedList<Vertex> queue = new LinkedList<Vertex>();
 		for (Vertex v : vertices) {
 			if (!v.getVisited()) {
 				v.setVisited(true);
 				v.setPredecessor(v.getIndex());
 				queue.add(v);
 				while (!queue.isEmpty()) {
 					Vertex u = queue.removeFirst();
 					LinkedList<AdjListNode> list = u.getAdjList();
 					for (AdjListNode node : list) {
 						Vertex w = node.getVertex();
 						if (!w.getVisited()) {
 							w.setVisited(true);
 							w.setPredecessor(u.getIndex());
 							queue.add(w);
 						}
 					}
 				}
 			}
 		}
 	}
 }
