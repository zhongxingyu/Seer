 package adp2.implementations;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import adp2.interfaces.*;
 
 public class SimulationImpl implements Simulation{
 
 		Graph graph;	
 		List<Ant> antList = new ArrayList<Ant>();
 		List<List<Integer>> pheromoneUpdateList = new ArrayList<List<Integer>>();		
 
 		int antQuantity;
 		int antsByStep = 0; //Anzahl der Ameisen die pro Step hinzugefuegt werden
 		double antAlpha = 0.5;
 		int startPoint = 1;
 		int pheromoneDecrease = 1;
 		int pheromoneIntensity = 10;
 		
 		int bestDistance = Integer.MAX_VALUE;
 		public List<Integer> bestPath = new ArrayList<Integer>();
 
 		//Ameisen werden alle direkt rein geworfen
 	    public static Simulation valueOf(Graph graph, int antsQuantity) {
 			return new SimulationImpl(graph, antsQuantity);
 		}
 	    
 	    //Ameisen werden in Wellen rein geworfen
 	    public static Simulation valueOf(Graph graph, int antsQuantity, int antsByStep) {
 			return new SimulationImpl(graph, antsQuantity, antsByStep);
 		}
 	    
 	    private SimulationImpl(Graph graph, int antsQuantity) {
 	    	setGraph(graph);
 	    	setAntQuantity(antsQuantity);
 	    	addAnts(antsQuantity);
 	    }
 	
 	    private SimulationImpl(Graph graph, int antsQuantity, int antsByStep) {
 	    	setGraph(graph);
 	    	setAntQuantity(antsQuantity);
 	    	setAntsByStep(antsByStep);
 	    }
 	    
 	    @Override
 	    public void start(){
 	    	long startzeit = System.currentTimeMillis();
 	    	while(System.currentTimeMillis()-startzeit < 2000){ //Abbruch nach 2Sec, weitere Bedingungen kommen
	    		if(antsByStep() != 0){
 	    			if( (antList().size()+antsByStep()) <= antQuantity()){ //Ameisen um antsByStep erhhen
 	    				addAnts(antsByStep());
 	    			} else if(antList().size() < antQuantity()){ // Ameisen um Rest < antsByStep erhhen
 	    				addAnts(antQuantity()-antList().size());
 	    			}
 	    		}
 	    		//System.out.println(antList().size()+" -> "+antList.size());
 	    		int i = 0;
 	    		while (i < antList().size()){
 	    			if (antList().get(i).hasFinished()) {
 	    				if (antList().get(i).weglaenge() < bestDistance()) {
 	    					setBestDistance(antList().get(i).weglaenge());
 	    					setBestPath(antList().get(i).traveledPath().waypoints());
 	    				}
 	    				removeAnt(i); 
 	    			} else {
 		    			if(antList().get(i).getWaitingTime() == 0){ 	//Befindlich auf Knoten
 		    				antList().get(i).step(); 					//Entscheidungsalgorithmus und einen Schritt gehen
 		    				addPheromoneUpdate(antList().get(i).prePosition(),antList().get(i).position(),pheromoneIntensity()); //Pheromonverteilung vorbereiten
 		    			}else{
 		    				antList().get(i).step();
 		    			}
 		    			i++;
 	    			}
 	    		}
 	    		pheromoneDecreament();
 	    		pheromoneIncrement();
 	    	}
 	    }
 		
 
 	    /*Getter and Setter*/
 	    
 	    private void setGraph(Graph graph){
 	    	this.graph = graph;
 	    }
 	    
 	    private void setAntQuantity(int quantity){
 	    	antQuantity = quantity;
 	    }
 	    
 	    private int antQuantity(){
 	    	return antQuantity;
 	    }
 	    
 	    private void setAntsByStep(int quantity){
 	    	antsByStep = quantity;
 	    }
 	    
 	    private int antsByStep(){
 	    	return antsByStep;
 	    }
 	    
 	    private int bestDistance(){
 	    	return bestDistance;
 	    }
 	    
 	    private void setBestDistance(int distance){
 	    	bestDistance = distance;
 	    }
 	    
 	    private List<Integer> bestPath(){
 	    	return bestPath;
 	    }
 	    
 	    private void setBestPath(List<Integer> path){
 	    	bestPath = path;
 	    }
 	    	    
 	    private List<Ant> antList(){
 	    	return antList;
 	    }
 	    	    
 	    private double antAlpha(){
 	    	return antAlpha;
 	    }
 	    
 	    private int startPoint(){
 	    	return startPoint;
 	    }
 	    
 	    private Graph graph(){
 	    	return graph;
 	    }
 	    
 	    private int pheromoneIntensity(){
 	    	return pheromoneIntensity;
 	    }
 	    
 	    private int pheromoneDecrease(){
 	    	return pheromoneDecrease;
 	    }
 	    
 	    private List<List<Integer>> pheromoneUpdateList(){
 	    	return pheromoneUpdateList;
 	    }
 	    
 	    
 	    /*Functions*/
 	    
 	    private void addAnt(){
 	    	antList().add(AntImpl.valueOf(startPoint(), antAlpha(), graph())); /////START POINT?
 	    }
 	    
 	    private void addAnts(int quantity){
 	    	for(int i=1; i <= quantity; i++){
 	    		addAnt();
 	    	}
 	    }
 	    
 	    private void removeAnt(int antIndex){
 	    	antList().remove(antIndex);
 	    }
 	    
 	    private void addPheromoneUpdate(int from,int to, int intensity){
 	    	List<Integer> pheromoneElement = new ArrayList<Integer>();
 	    	pheromoneElement.add(from);
 	    	pheromoneElement.add(to);
 	    	pheromoneElement.add(intensity);
 	    	if (from != to){
 	    		pheromoneUpdateList().add(pheromoneElement);
 	    	}
 	    }
 	    
 	    private void pheromoneIncrement(){
 	    	for(List<Integer> list: pheromoneUpdateList()){
 	    		graph().incrementPheromone(list.get(0), list.get(1),list.get(2));
 	    		graph().incrementPheromone(list.get(1), list.get(0),list.get(2));
 	    	}
 	    }
 	    
 	    private void pheromoneDecreament(){
 	    	graph().decrementPheromone(pheromoneDecrease());
 	    }
 
 		@Override
 		public Map<Path, Integer> frequencyMap() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public Path minPath() {
 			return Values.path(bestPath(), bestDistance());
 		}
 }
