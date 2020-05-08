 class Individual {
 	double[] values;
 	double evaluationScore;
 
 	double mutationParameter = 0.05; //aanpasbaar
 	int numDimensions = 10;
 
 	public Individual () {
 		double[] values = new double[numDimensions];
 	}
 	
 	public void initialize(){
 		//intialiseer de values met random getallen
 	}
 	
 	public void initialize(int i){
 		this.evaluationScore = i;
 	}
 
 	public Individual createOffspring (Individual i) {
 		//child.values = mutate(crossover(i.values));
 		return new Individual();
 	}
 
 	private double[] crossover (double[] d) { //one of the parent values
 		int crossoverpoint = randomWithRange(1,9); //bepaald welk stuk van welke ouder
 		double[] child = new double[numDimensions];
		for(int i=0; i== crossoverpoint; i++){ 			//ouder 1
 			child[i] = this.values[i];
 		}
		for(int i= crossoverpoint + 1; i==numDimensions; i++){ //ouder 2
			child[i]=d[i];
 		}
 		return child;
 	}
 	
 	private int randomWithRange(int min, int max){ 
 		 int range = (max - min) + 1;     
 		 return (int)(Math.random() * range) + min;
 	}
 
 	private double[] mutate (double[] d) {	//child values
 		return new double[1];
 	}
 
 }
