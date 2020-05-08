 /**
  * 
  */
 package edu.pw.gis.graph;
 
 import java.text.DecimalFormat;
 import java.util.*;
 
 public class Graph {
 	public List<Edge> edgeList = new ArrayList<Edge>(); // krawedzie
 	public List<Node> nodeList = new ArrayList<Node>(); // wezly
 	public double highestUsage = 0.0; // maksymlane uzycie (f-cja celu)
 
 	public int noNodes; // number of nodes
 
 	public ArrayList<ArrayList<Double>> flowMatrix; // macierz przeplywow,
 													// indeksami sa numery
 													// wezlow
 	public ArrayList<AdjElement> adjList; // listy sasiedztwa dla kazdego
 											// wierzcholka
 	public ArrayList<AdjElement> revertedAdjList; // lista sasiedztwa, w ktorej
 													// kierunki wszystkich
 													// krawedzi sa odwrocone
 
 	public double max_capacity; // pojemnosc najbardziej pojemnej krawedzi w
 								// sieci
 
 	Random rand = new Random();
 
 	public Graph(int noNodes) {
 		this.noNodes = noNodes;
 
 		this.flowMatrix = new ArrayList<ArrayList<Double>>(this.noNodes);
 
 		for (int i = 0; i < this.noNodes; ++i) { // macierz przeplywow --
 													// inicjalizacja rozmiarow
 			this.flowMatrix.add(i, new ArrayList<Double>(this.noNodes));
 			for (int j = 0; j < this.noNodes; ++j) {
 				this.flowMatrix.get(i).add(j, Double.parseDouble("0.0"));
 			}
 		}
 
 		this.adjList = new ArrayList<AdjElement>(this.noNodes);
 		this.revertedAdjList = new ArrayList<AdjElement>(this.noNodes);
 
 		for (int i = 0; i < this.noNodes; ++i) {
 			this.adjList.add(i, new AdjElement());
 			this.revertedAdjList.add(i, new AdjElement());
 		}
 
 	}
 
 	public void addNode(Node n) {
 		this.nodeList.add(n);
 		this.adjList.get(n.id).n = n; // zalozenie, ze przychodza wezly w
 										// kolejnosci ich id
 		this.revertedAdjList.get(n.id).n = n;
 	}
 
 	public void randomWeights(int max_weight, double max_capacity) {
 		int tmp;
 		int j;
 		for (Edge e : edgeList) {
 			for (j = 1; max_weight < max_capacity / j; j = j * 10)
 				; // przeskalowanie wylosowanej wagi tak, aby korespondowala z
 					// pojemnosciami krawedzi
 			tmp = rand.nextInt(max_weight);
			// e.weight = tmp;
 			e.weight = Math.min((int) (tmp * j / e.capacity) + 1, max_weight);
 			// wagi o wiekszych pojemnosciach dostaja mniejsze wagi
 		}
 	}
 
 	public void addEdge(Edge e) {
 		// trzeba dodac krawedz do trzech list
 		this.edgeList.add(e);
 		this.adjList.get(e.source.id).list.add(new NodeEdge(e.target, e));
 		this.revertedAdjList.get(e.target.id).list
 				.add(new NodeEdge(e.source, e));
 	}
 
 	/**
 	 * Wypisuje krawedzie polaczone z wierzcholekim, ich wagi, pojemnosci,
 	 * przeplywy
 	 */
 	public void printAdjList() {
 		DecimalFormat twoDigit = new DecimalFormat("#,##0.00");
 		System.out.println("zrodlo \t cel \t uzycie \t waga \t przeplyw \t pojemnosc");
 		for (AdjElement element : this.adjList) {
 			//System.out.println("Wezel " + element.n.name + " :: ");
 			for (NodeEdge t : element.list) {
 				if (t.e.flowSum > 0) {
 					System.out.print(element.n.name + " \t " +t.n.name + "\t");
 					System.out.println(twoDigit.format(t.e.usage) + "\t" + t.e.weight + " \t "
 							+ twoDigit.format(t.e.flowSum) + " \t " + twoDigit.format(t.e.capacity));
 				}
 			}
 		}
 		System.out.println("");
 	}
 
 	public void printRevertedAdjList() {
 		for (AdjElement element : this.revertedAdjList) {
 			System.out.print("Node " + element.n.name + " outD: "
 					+ element.n.outDegree + " :: ");
 			for (NodeEdge t : element.list) {
 				System.out.println(t.n.name + "(" + t.e.id + "), inTree:"
 						+ t.e.inTree + " ");
 			}
 		}
 		System.out.println("");
 	}
 
 	/**
 	 * Klonowanie grafu. Konieczne jest przepisanie wszystkich wezlwo i krawedzi
 	 * za pomoca metoda addNode i addEdge tak, aby byly zapisane odpowiednio w
 	 * strukturach wewnetrznych grafu
 	 */
 	public Graph clone() {
 		Graph result = new Graph(this.noNodes);
 
 		result.noNodes = this.noNodes;
 
 		result.max_capacity = this.max_capacity;
 
 		result.highestUsage = 0.0;
 
 		for (Node n : this.nodeList) {
 			result.addNode(new Node(n.id, n.draw_x, n.draw_y, n.name));
 		}
 
 		for (Edge e : this.edgeList) {
 			result.addEdge(new Edge(e.id, e.capacity, result.nodeList
 					.get(e.source.id), result.nodeList.get(e.target.id),
 					e.name, e.usage));
 		}
 
 		for (int i = 0; i < result.noNodes; ++i) {
 			ArrayList<Double> row = this.flowMatrix.get(i);
 			for (int j = 0; j < result.noNodes; ++j) {
 				result.flowMatrix.get(i).set(j, row.get(j));
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * Wypisuje macierz przeplywow
 	 */
 	public void printFlowMatrix() {
 		for (ArrayList<Double> a : flowMatrix) {
 			for (Double d : a) {
 				System.out.print(d + "		");
 			}
 			System.out.println();
 		}
 	}
 
 	/**
 	 * Test jedostkowy
 	 */
 	public static void main(String[] args) {
 		Graph g = new Graph(2);
 		Node n = new Node(0, 0, 0, "janek");
 		g.addNode(n);
 		Node n1 = new Node(1, 0, 0, "wiesiek");
 		g.addNode(n1);
 		Edge e = new Edge(0, 100, n, n1, "test");
 		g.addEdge(e);
 		e = new Edge(1, 100, n1, n, "test");
 		g.addEdge(e);
 
 		g.printRevertedAdjList();
 		g.printAdjList();
 		System.out.println(g.nodeList.get(1).name);
 
 		// zmieniamy via edge
 		g.adjList.get(0).list.get(0).n.name = "zmieniony";
 
 		g.printAdjList();
 		System.out.println(g.nodeList.get(1).name);
 
 	}
 
 }
