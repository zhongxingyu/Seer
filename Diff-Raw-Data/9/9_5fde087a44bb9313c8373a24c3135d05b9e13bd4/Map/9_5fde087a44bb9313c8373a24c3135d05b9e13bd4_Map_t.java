 package antColony;
 
 import java.util.ArrayList;
 
 public class Map {
 	
 	Cell[][] map;
 	ArrayList<Ant> antList;
 	ArrayList<Ant> finishedAnts;
 	int antMax;
 	int width;
 	int height;
 	int ANT_COUNTER;//counts how many ants have entered the system
 	int BEST_ANT_MOVEMENTS;//current least number of movements an ant has taken to reach a food source
 	int specialAntMult;//the multiplier for the number of pheromones a special ant will leave compared to a regular ant
 	int pheromoneLvl_global;
 	GUI mapGUI;
 	Cell start_global;
 	
 	//CONSTRUCTORS
 	public Map(int width_in, int height_in,int foodSource_in){//width=elements in each array, height=# of arrays
 		System.out.println("ANT MADE IT IN THIS MANY MOVES: ");
 		mapGUI=new GUI(height_in,width_in);//GUI
 		specialAntMult=1;
 		ANT_COUNTER=1;
 		BEST_ANT_MOVEMENTS=1000000000;
 		width=width_in;
 		height=height_in;
 		antList=new ArrayList<Ant>();
 		finishedAnts=new ArrayList<Ant>();
 		map=new Cell[height][width];
 		Cell[] tempArray=new Cell[width];
 		for(int i=0;i<map.length;i++){
 			tempArray=new Cell[width];
 			for(int j=0;j<map[i].length;j++){
 				tempArray[j]=new Cell(j,i);
 			}
 			map[i]=tempArray;
 		}
 		int foodNum=0;
 		if(width*height<foodSource_in){
 			System.out.println("REMAKE MAP, TOO MANY FOOD SOURCES SPECIFIED!");
 		}else{
 			Cell tempCell=map[height/8][width/8];//getRandomCell();//TEST
 			Cell foodCell = tempCell;
 			while(foodNum<foodSource_in){
 				mapGUI.makeFood(tempCell.getLocation()[1], tempCell.getLocation()[0]);//GUI
 				if(tempCell.getType()==0){
 					foodNum++;
 					tempCell.makeFoodSource();
 				}
 			}
 			
 			for(int i=0;i<width*height/5;i++){
 				tempCell=getRandomCell();
 				if(tempCell.getType()==0 && tempCell.isNextTo(foodCell.getLocation()) == false){
 					tempCell.makeObsticle();
 					mapGUI.makeObsticle(tempCell.getLocation()[1],tempCell.getLocation()[0]);
 				}	
 				
 			}
 		}
 	}
 	
 	//GET_METHODS
 	public Cell getRandomCell(){
 		return map[(int) (Math.random()*(height-1))][(int) (Math.random()*(width-1))];
 	}
 	public Cell getCell(int x, int y){
 		return map[y][x];
 	}
 	public int getAntCounter(){
 		return ANT_COUNTER;
 	}
 	public int getBestAntMovements(){
 		return BEST_ANT_MOVEMENTS;
 	}
 	//CLEAR_ALL
 	public void resetAll(){
 		for(int i=0;i<map.length;i++){
 			for(int j=0;j<map[i].length;j++){
 				map[i][j].setPheromone(1);
 			}
 		}
 		antList.clear();
 		finishedAnts.clear();
 		ANT_COUNTER=1;
 		BEST_ANT_MOVEMENTS=1000000000;
 	}
 	
 	//RUN_THIS_BITCH_METHODS
 	public void antColony(int antMax_in,int antRelease_in,Cell start,int pheromoneLvl_in,int antColonySize_in,int specialAntMult_in,int delay_in){
 		start_global= start;
 		mapGUI.makeStart(start.getLocation()[1], start.getLocation()[0]);//GUI
 		specialAntMult=specialAntMult_in;
 		antMax=antMax_in;//number of ants in the system
 		pheromoneLvl_global = pheromoneLvl_in;
 		
 		int antRelease=antRelease_in;//how many moves do you want to go through before you release another ant
 		int releaseCounter=0;
 		int finishedAntNum=0;//counts the number of ants who have completed their journey
 		
 		for(int i=0;i<antColonySize_in;i++){
 			Ant tempAnt= new Ant(start);
 			antList.add(tempAnt);
 		}
 		
 		if(antMax>=1){
 			//Ant tempAnt= new Ant(start);
 			//antList.add(tempAnt);
 			while(finishedAntNum<=antMax){
 				
 				if(finishedAnts.isEmpty()){
 				//releases new ants into map based on counter specified
 				
 				/*
 				releaseCounter++;
 				if(releaseCounter>antRelease && antList.size()+finishedAnts.size()<antColonySize_in){
 					ANT_COUNTER++;
 					antList.add(new Ant(start));
 					releaseCounter=0;
 				}
 				*/
 				try {
 					Thread.sleep(delay_in);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				moveAnts();//moves all active ants (adds the number of finished ants to the "finishedAnts" counter)
 				mapGUI.makeStart(start.getLocation()[1], start.getLocation()[0]);//GUI
 				if(finishedAntNum<=antMax){
 					decayPheromones();
 					pheromoneLvl_global = BEST_ANT_MOVEMENTS;
 					pheromoneLvl_in=pheromoneLvl_global;
 					finishedAntNum+=releasePheromones(pheromoneLvl_in);
 				}
 				}else{
 					
 					pheromoneLvl_global = BEST_ANT_MOVEMENTS;
 					pheromoneLvl_in=pheromoneLvl_global;
 					releaseAll(pheromoneLvl_in);
 					mapGUI.incFinished();
 					mapGUI.setFinished(BEST_ANT_MOVEMENTS);
 					finishedAntNum++;
 					for(int i=0;i<antColonySize_in;i++){
 						Ant tempAnt= new Ant(start);
 						antList.add(tempAnt);
 					}
 				
 				}
 
 			}
 		}else{
 			System.out.println("One or more ants must be chosen to be in system!");
 		}
 		
 	}
 	//HELPER_METHODS
 	private void moveAnts(){//moves ants, returns an int which is the number of ants who have found a food source
 		int[] lastMove = new int[2];
 		int[] currentLoc = new int[2];
 		ArrayList<Cell> possibleMoves=new ArrayList<Cell>();
 		ArrayList<Ant> tempFinishedAnts=new ArrayList<Ant>();
 		ArrayList<Ant> tempList=new ArrayList<Ant>();
 		double[] chooseMoveList=null;
 		int totalPheromones=0;
 		//***MIGHT NEED TO CHECK WHICH ONES ARE Y-AXIS AND X-AXIS IN MAP AND IN "lastMove[]"***//
 		for(Ant ant:antList){
 			//previous direction "lastMove[]" = current position - position before previous e.g. [1,1]-[0,0]=[1,1] (came from southWest)
 			currentLoc[0]=ant.getCurrent().getLocation()[0];
 			currentLoc[1]=ant.getCurrent().getLocation()[1];
 			if(ant.getPath().size()<2){
 				possibleMoves.add(map[currentLoc[1]+1][currentLoc[0]]);//N
 				possibleMoves.add(map[currentLoc[1]-1][currentLoc[0]]);//S
 				possibleMoves.add(map[currentLoc[1]][currentLoc[0]-1]);//W
 				possibleMoves.add(map[currentLoc[1]][currentLoc[0]+1]);//E
 				possibleMoves.add(map[currentLoc[1]-1][currentLoc[0]-1]);//SW
 				possibleMoves.add(map[currentLoc[1]-1][currentLoc[0]+1]);//SE
 				possibleMoves.add(map[currentLoc[1]+1][currentLoc[0]-1]);//NW
 				possibleMoves.add(map[currentLoc[1]+1][currentLoc[0]+1]);//NE
 			}else{
 				lastMove[0]=ant.getCurrent().getLocation()[0]-ant.getPreviousCurrent().getLocation()[0];
 				lastMove[1]=ant.getCurrent().getLocation()[1]-ant.getPreviousCurrent().getLocation()[1];
 				//if proceeding in the same direction will put the ant out of bounds...
 				if(currentLoc[0]+lastMove[0]<0 || currentLoc[1]+lastMove[1]<0 || currentLoc[0]+lastMove[0]>width-1 || currentLoc[1]+lastMove[1]>height-1){
 					//PSUEDO_CODE
 						//ignore the middle case and look at the side cases, choose between them. If none are acceptable then reverse direction.
 					//END_PSUEDO_CODE
 					possibleMoves.add(map[currentLoc[1]-lastMove[1]][currentLoc[0]-lastMove[0]]);
 					possibleMoves.add(map[currentLoc[1]-lastMove[1]][currentLoc[0]-lastMove[0]]);
 					possibleMoves.add(map[currentLoc[1]-lastMove[1]][currentLoc[0]-lastMove[0]]);
 					//PSUEDO-REPLACE WITH THIS POLICY ->ignore the middle case and look at the side cases, choose between them. If none are acceptable then reverse direction.
 				}else{
 					//populate possible moves with 3 adjacent cells in direction you are going in if they are on the ap
 					//randomly select one with pheromones taken into concideration
 					if(lastMove[0]==0){//if the last move came from up or down
 						possibleMoves.add(map[currentLoc[1]+lastMove[1]][currentLoc[0]]);//continueing along the same direction
 						possibleMoves.add(map[currentLoc[1]+lastMove[1]][currentLoc[0]+1]);//changing the x-coordinate cell left
 						possibleMoves.add(map[currentLoc[1]+lastMove[1]][currentLoc[0]-1]);//changing the x-coordinate cell right
 					}else if(lastMove[1]==0){//if the last move came from left or right
 						possibleMoves.add(map[currentLoc[1]][currentLoc[0]+lastMove[0]]);//continueing along the same direction
 						possibleMoves.add(map[currentLoc[1]+1][currentLoc[0]+lastMove[0]]);//changing the x-coordinate cell left
 						possibleMoves.add(map[currentLoc[1]-1][currentLoc[0]+lastMove[0]]);//changing the x-coordinate cell right
 					}else{//if the last move came from a diagonal
 						possibleMoves.add(map[currentLoc[1]+lastMove[1]][currentLoc[0]+lastMove[0]]);//continueing along the same diagonal
 						possibleMoves.add(map[currentLoc[1]][currentLoc[0]+lastMove[0]]);//changing the x-coordinate cell
 						possibleMoves.add(map[currentLoc[1]+lastMove[1]][currentLoc[0]]);//changing the y-coordinate cell
 					}
 				}
 			}
 			
 			chooseMoveList=new double[possibleMoves.size()];
 			totalPheromones=0;
 			for(int i=0;i<possibleMoves.size();i++){
 				totalPheromones+=possibleMoves.get(i).getPheromone();
 				chooseMoveList[i]=totalPheromones;
 			}
 
 			double pheromoneChooser=Math.random()*totalPheromones;
 			
 			Cell chosenCell=possibleMoves.get((int) Math.floor(Math.random()*possibleMoves.size()));//idk why there was an issue here at one point....
 			
 			double maxVal=1000000;
 			for(int i=0;i<possibleMoves.size();i++){
 				chooseMoveList[i]= chooseMoveList[i]-pheromoneChooser;
 				if(chooseMoveList[i]>=0 && chooseMoveList[i]<maxVal){
 					chosenCell=possibleMoves.get(i);
 					maxVal=chooseMoveList[i];
 				}
 			}
 			if(chosenCell.getType()!=2){
 				ant.move(chosenCell);
 				if(ant.getCurrent().getType()==0){
 					mapGUI.putAnt(ant.getCurrent().getLocation()[1],ant.getCurrent().getLocation()[0]);
 					mapGUI.removeAnt(ant.getPreviousCurrent().getLocation()[1],ant.getPreviousCurrent().getLocation()[0]);
 				}
 			}else{
 				ant.move(map[currentLoc[1]-lastMove[1]][currentLoc[0]-lastMove[0]]);
 				mapGUI.removeAnt(ant.getPreviousCurrent().getLocation()[1],ant.getPreviousCurrent().getLocation()[0]);
 				mapGUI.putAnt(ant.getCurrent().getLocation()[1],ant.getCurrent().getLocation()[0]);
 				//tempList.add(ant);
 			}
 			
 			
 			
 			//ant.move(possibleMoves.get((int) (Math.random()*possibleMoves.size())));
 			//TEST_MOVE//
 			//ant.move(possibleMoves.get((int) (Math.random()*possibleMoves.size())));
 			
 			//checks if ant is at a food source
 			if(ant.getCurrent().getType()==1){
 				tempFinishedAnts.add(ant);
 			}
 			possibleMoves.clear();
 		}
 		for(Ant bestAnt: tempFinishedAnts){
 			if(bestAnt.getPath().size()<=BEST_ANT_MOVEMENTS){
 				BEST_ANT_MOVEMENTS=bestAnt.getPath().size();
 				bestAnt.makeSpecial(specialAntMult);
 			}
 		}
 		for(int i=0;i<tempList.size();i++){
 			Ant tempAnt= new Ant(start_global);
 			antList.add(tempAnt);
 		}
 		antList.removeAll(tempList);
 		tempList.clear();
 		antList.removeAll(tempFinishedAnts);
 		finishedAnts.addAll(tempFinishedAnts);
 	}
 	private void decayPheromones(){
 		for(int i=0;i<map.length;i++){
 			for(int j=0;j<map[i].length;j++){
 				if(map[i][j].getPheromone()>1){
					if(map[i][j].getPheromone()>BEST_ANT_MOVEMENTS*2){
						mapGUI.setPheromone(i, j,BEST_ANT_MOVEMENTS*2);
						map[i][j].setPheromone(BEST_ANT_MOVEMENTS*2);//decreases pheromones of all cells by 1 if it has at >1 pheromone in it
					}else{
						mapGUI.decPheromone(i, j);
						map[i][j].decPheromone(1);//decreases pheromones of all cells by 1 if it has at >1 pheromone in it
					}
 				}
 			}
 		}
 	}
 	private int releasePheromones(int pheromoneLvl){
 		int done=0;//# of finished ants
 		ArrayList<Ant> tempFinishedAnts=new ArrayList<Ant>();
 		for(Ant ant:finishedAnts){
 			if(ant.getPath().isEmpty()){
 				tempFinishedAnts.add(ant);
 				mapGUI.incFinished();
 				done++;
 			}else{
 				if(ant.getPath().get(ant.getPath().size()-1).getPheromone()<BEST_ANT_MOVEMENTS){
 					ant.getPath().get(ant.getPath().size()-1).incPheromone(pheromoneLvl*ant.getSpecial());
 					if(ant.getPath().get(ant.getPath().size()-1).getType()==0){
 //						mapGUI.backTrack(ant.getPath().get(ant.getPath().size()-1).getLocation()[1],ant.getPath().get(ant.getPath().size()-1).getLocation()[0]);
 					}
 					mapGUI.incPheromone(ant.getPath().get(ant.getPath().size()-1).getLocation()[1],ant.getPath().get(ant.getPath().size()-1).getLocation()[0], pheromoneLvl*ant.getSpecial());
 				}
 				ant.getPath().remove(ant.getPath().get(ant.getPath().size()-1));
 			}
 		}
 		finishedAnts.removeAll(tempFinishedAnts);
 		return done;
 	}
 	private void releaseAll(int pheromoneLvl){
 		Ant ant=finishedAnts.get(0);
 		System.out.println(ant.getPath().size());
 			while(!ant.getPath().isEmpty()){
 				if(ant.getPath().get(ant.getPath().size()-1).getPheromone()<BEST_ANT_MOVEMENTS*2){
 					ant.getPath().get(ant.getPath().size()-1).incPheromone(pheromoneLvl*ant.getSpecial());
 					if(ant.getPath().get(ant.getPath().size()-1).getType()==0){
 //						mapGUI.backTrack(ant.getPath().get(ant.getPath().size()-1).getLocation()[1],ant.getPath().get(ant.getPath().size()-1).getLocation()[0]);
 					}
 					mapGUI.incPheromone(ant.getPath().get(ant.getPath().size()-1).getLocation()[1],ant.getPath().get(ant.getPath().size()-1).getLocation()[0], pheromoneLvl*ant.getSpecial());
 				
 				}else{
 					if(ant.getPath().get(ant.getPath().size()-1).getType()==0){
 //						mapGUI.backTrack(ant.getPath().get(ant.getPath().size()-1).getLocation()[1],ant.getPath().get(ant.getPath().size()-1).getLocation()[0]);
 					}
 					mapGUI.setPheromone(ant.getPath().get(ant.getPath().size()-1).getLocation()[1],ant.getPath().get(ant.getPath().size()-1).getLocation()[0], BEST_ANT_MOVEMENTS*2);
 					ant.getPath().get(ant.getPath().size()-1).setPheromone(BEST_ANT_MOVEMENTS*2);
 				}
 					ant.getPath().remove(ant.getPath().get(ant.getPath().size()-1));
 			}
 			for(Ant allAnts:antList){
 				if(allAnts.getCurrent().getType()==0){
 					mapGUI.removeAnt(allAnts.getCurrent().getLocation()[1], allAnts.getCurrent().getLocation()[0]);
 				}
 			}
 		antList.clear();
 		finishedAnts.clear();
 	}
 }
