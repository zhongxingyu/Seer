 package graph;
 import java.util.*;
 
 import static graph.Graphs.*;
 
 public class AdjMatrix extends AbstractGraph implements Graph{
 	private Map<String, Integer> accessMap = new HashMap<String, Integer>();
 	private double[][] adjMatrix;
 	private int zugriffe;
 
 	private AdjMatrix(String[] pairs, String einleseString){
 			fillAccessMap(pairs);
 			createMatrix(accessMap.size(), pairs);
 			zugriffe = 0;
 			this.einleseString = einleseString;
 	}
 	
 
 	/*
 	 * Fuellt die Matrix mit den im Graphen-String angegebenen Werten
 	 */
 	private void createMatrix(int size, String[] pairs) {
 		adjMatrix = new double[size][size];
 		preFillMatrix(size, Double.POSITIVE_INFINITY);
 		
 		for(String elem : pairs){
 			int firstNode =  accessMap.get(getFirst(elem));
 			int secondNode = accessMap.get(getSecond(elem));
 			if(gewichtung(elem) < adjMatrix[firstNode][secondNode] || adjMatrix[firstNode][secondNode] == 0){
 				adjMatrix[firstNode][secondNode] = gewichtung(elem);
 			}
 			if(trenner(elem).equals("!")){
 				if(gewichtung(elem) < adjMatrix[secondNode][firstNode]){
 					adjMatrix[secondNode][firstNode] = gewichtung(elem);
 				}
 			}
 		}
 	}
 
 	/*
 	 * Fuellt die gesamte Matrix mit 0 auf, um einen Einstiegswert zu haben auf den geprueft werden kann
 	 * 
 	 * 1 als Default-Wert ist nicht moeglich, da die Funktion "createMatrix" sonst nicht wuesste,
 	 * ob ein Feld mit 1 eine Kante mit der Gewichtung 1 ist oder der Default-Wert
 	 */
 	private void preFillMatrix(int size, double val) {
 		for(int i = 0; i < size; i++){
 			for(int j = 0; j < size; j++){
 				adjMatrix[i][j] = val;
 			}
 		}
 	}
 
 	/*
 	 * Fuellt die AccessMap mit den Eckpunkten und den fuer die Matrix dazugehoerigen Indizes:
 	 * z.B. v1->0 / v2->1 etc.
 	 * Somit kann per Eckpunktname auf die Werte in der Matrix zugegriffen werden
 	 */
 	private void fillAccessMap(String[] pairs) {
 		int cnt = 0;
 		for(String elem : pairs){
 			if(!accessMap.containsKey(getFirst(elem))){
 				accessMap.put(getFirst(elem), cnt++);
 			}
 			if(!accessMap.containsKey(getSecond(elem))){
 				accessMap.put(getSecond(elem), cnt++);
 			}
 		}
 	}
 	
 	public static Graph valueOf(String s){
 		String[] pairs = s.split(";");
 		for(String elem : pairs){
 			if(!(checkElem(elem))) return Graphs.nag;
 		}
 		return new AdjMatrix(pairs,s);
 	}
 	
 	
 
 
 
 	public int noOfNodes() {
 		zugriffe++;
 		return accessMap.size();
 	}
 
 	public List<Nachbar> neighbors(String ecke) {
 		ArrayList<Nachbar> result = new ArrayList<Nachbar>();
 		
 		zugriffe++;
 		int edge = accessMap.get(ecke);
 		
 		for(int j = 0; j < accessMap.size(); j++){
 			if(adjMatrix[edge][j] < Double.POSITIVE_INFINITY){
 				// dieses Konstrukt ist notwendig um anhand des Values (Index der Matrix) den Key zu bekommen
 				zugriffe++;
 				for(Map.Entry<String, Integer> entry : accessMap.entrySet()){
 					if(entry.getValue() == j){
 						result.add(nachbar(entry.getKey(),adjMatrix[edge][j]));
 						zugriffe++;
 						break; //break zur optimierung
 					}
 				}
 			}
 		}
 		
 		return result;
 	}
 
 	public Set<String> allNodes() {
 		zugriffe++;
 		return accessMap.keySet();
 	}
 	
 	public String toString(){
 		StringBuffer result = new StringBuffer();
 		
 		for(int i = 0; i < accessMap.size(); i++){
 			// dieses Konstrukt ist notwendig um anhand des Values (Index der Matrix) den Key zu bekommen
 			for(Map.Entry<String, Integer> entry : accessMap.entrySet()){
 				if(entry.getValue() == i){
 					result.append(entry.getKey().toString()+"|");
 					break; //break zur optimierung
 				}
 			}
 			for(int j = 0; j < accessMap.size(); j++){
 				result.append(adjMatrix[i][j]+" | ");
 			}
 			result.append("\n");
 		}
 		return result.toString();
 	}
 	
 	public Map<String,Integer> accessMap(){
 		return this.accessMap;
 	}
 	
 	public int getZugriffe(){
 		return this.zugriffe;
 	}
 	
 	public void setZugriffeNull(){
 		this.zugriffe = 0;
 	}
 
 
 	@Override
 	public Pair<List<String>, Double> dijkstra(String start, String end) {
 		return super.dijkstra(start, end);
 	}
 	
 	public String dijkstraFiFo(String start, String end){
 		return super.dijkstraFiFo(start, end);
 	}
 	
 	private double getMatrixElem(int i, int j){
 		zugriffe++;
 		return this.adjMatrix[i][j];
 	}
 	
 	private void setMatrixElem(int i, int j, double val){
 		zugriffe++;
 		this.adjMatrix[i][j]=val;
 	}
 
 	public Graph[] floydWarshall() {
 		AdjMatrix distance = new AdjMatrix(einleseString.split(";"), einleseString); //init distance-Matrix
 		AdjMatrix v =  new AdjMatrix(einleseString.split(";"), einleseString);//init transitmatrix
 		int size = this.allNodes().size();
 		
 		
 		for(int i = 0 ; i < size; i++){	//init transitmatrix ==> befuellen mit 0	
 			for(int j = 0; j < size; j++){
 				if(v.getMatrixElem(i, j) == Double.POSITIVE_INFINITY){
 					v.setMatrixElem(i, j, 0);
 				}else{
 					v.setMatrixElem(i, j, i);
 				}
 				
 				if(i == j){
 					distance.setMatrixElem(i, j, 0);
 				}
 			}
 		}
 		
 		//eingentliche Berechnung
 		
 		
 		for(int j = 0; j < size; j++){
 //			System.out.println(j + ":");
 //			System.out.println(distance);
 			for(int i = 0; i < size; i++){
 				for(int k = 0; k < size; k++){
 					if(distance.getMatrixElem(i, k) > distance.getMatrixElem(i, j) + distance.getMatrixElem(j, k)){
 						double val = distance.getMatrixElem(i, j) + distance.getMatrixElem(j, k);
 						distance.setMatrixElem(i, k, val); 
 						v.setMatrixElem(i, k, j);
 					}
 				}
 			}
 			
 		}
 		
 		Graph d = (Graph)distance;
 		Graph t = (Graph)v;
 		Graph[] result = {d,t};
 		this.zugriffe += v.getZugriffe();
 		this.zugriffe += distance.getZugriffe();
 		return result;
 	}
 	
 	public void insert(String from, String to, double val){
 		setMatrixElem(accessMap.get(from), accessMap.get(to), val);
 	}
 	
 	public void changeCapacity(String from, String to, double value){
 		if(accessMap.containsKey(from) && accessMap.containsKey(to)){
 			setMatrixElem(accessMap.get(from), accessMap.get(to), value);
 		}
 	}
 	
 	
 	/*Entferne alle Kanten mit einer Geiwchtung von 0
 	* Notwendig fuer ResidualgraphenAlgorithmus,
 	* da in diesem 0 Kanten fuer nicht vorhandene Kanten stehen
 	*/
 	public void deleteZeroEdges(){
 		int size = accessMap.size();
 		
 		for(int i = 0; i < size; i++){
 			for(int j = 0; j < size; j++){
 				if(getMatrixElem(i, j) == 0){
 					setMatrixElem(i, j, Double.POSITIVE_INFINITY);
 				}
 			}
 		}
 	}
 
 	
 	public double weightBetween(String start, String end){
 		return getMatrixElem(accessMap.get(start), accessMap.get(end));
 	}
 
 
 	
 	public List<Pair<String, Double>> edgesReverse(String eckenname) throws IllegalArgumentException{
 		if(!this.allNodes().contains(eckenname)) throw new IllegalArgumentException();
 		
 		List<Pair<String,Double>> result = new ArrayList<Pair<String,Double>>();
 		
 		int internRep = this.accessMap.get(eckenname);
 		for(int i = 0; i < this.accessMap.size(); i++){
 			if(i != internRep){
 				if(!Double.isInfinite(adjMatrix[i][internRep] )){
 					Pair<String,Double> p = new Pair<String,Double>();
 					p.setFirst(keyOf(accessMap, i));
 					p.setSecond(adjMatrix[i][internRep]);
 					result.add(p);
 				}
 			}
 		}
 		
 		return result;
 		
 		
 	}
 	
 	//Hilfsmethode, um Zahlenrepraesentanten der Knoten wieder die jeweiligen Namen zuzuordnen
 	private String keyOf(Map<String,Integer> m ,Integer value){
 		for(Map.Entry<String, Integer> entry : m.entrySet() ){
 			if(entry.getValue().equals(value)){
 				return entry.getKey();
 			}
 		}
 		return "";
 	}
 
 
 	public void allEdgesZero() {
 		this.preFillMatrix(adjMatrix.length, 0.0);
 	}
 
 
 	public void setZugriffe(int i) {
 		this.zugriffe = i;
 		
 	}
 	
 
 
 	public static void main(String args[]){
 		Graph g1 = AdjMatrix.valueOf("v1-v2:8;v2-v3:7;v1-v1:4;v3-v2:2;v1-v4:9");
 		System.out.println(g1.bfs("v1"));
 		System.out.println(g1.noOfNodes());
 		System.out.println(g1.allNodes().toString());
 		
 	}
 	
 	public int eingangsgrad(String ecke){
 		int result = 0;
 		for(Nachbar elem : this.neighbors(ecke)){
			if(adjMatrix[accessMap.get(elem.name())][accessMap.get(ecke)] != Double.POSITIVE_INFINITY){
 				result++;
 			}
 		}
 		return result;
 	}
 	
 	public int ausgangsgrad(String ecke){
 		int result = 0;
 		for(Nachbar elem : this.neighbors(ecke)){
			if(adjMatrix[accessMap.get(ecke)][accessMap.get(elem.name())] != Double.POSITIVE_INFINITY){
 				result++;
 			}
 		}
 		return result;
 	}
 
 }
 
