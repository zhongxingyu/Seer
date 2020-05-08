 package se.chalmers.TowerDefence;
 
 import java.util.ArrayList;
 
 public class Road {
 	
 	private ArrayList<RoadSquare> roadIterator;
 	
 	public Road(GameBoard gameBoard){
 		this.roadIterator = new ArrayList<RoadSquare>();
 		findRoad(gameBoard);
 	}
 	
 	public Road(Road clone){
 		//TODO clone constuctor
 	}
 	
 	private void findRoad(GameBoard gameBoard){
 		int[][] gb = gameBoard.getGameBoard();
 		//TODO better algoritm
 		int x = 0;
     	int y = 1;
     	boolean temp = true;
     	roadIterator.add(new RoadSquare(x,y));
     	while(temp){
     		//*
     		int lastX = roadIterator.get(roadIterator.size()-1).getX();
     		int lastY = roadIterator.get(roadIterator.size()-1).getY();
    		int length = roadIterator.get(roadIterator.size()-1).getLength();
    		if(x+1 < gb.length && gb[x+1][y] == 1 && !(x==lastX/length || y==lastY/length)){
     			roadIterator.add(new RoadSquare(x+1, y));
    			x++;
    		}else if(x - 1 >= 0 && gb[x-1][y] == 1 && !(x==lastX/length || y==lastY/length) ){
     			roadIterator.add(new RoadSquare(x-1, y));
    			x--;
    		}else if(y+1 < gb[x].length && gb[x][y+1] == 1 && !(x==lastX/length || y==lastY/length)){
     			roadIterator.add(new RoadSquare(x, y+1));
    			y++;
    		}else if(y - 1 >= 0 && gb[x][y-1] == 1 && !(x==lastX/length || y==lastY/length)){
    			roadIterator.add(new RoadSquare(x, y-1));
    			y--;
     		}else{
     			temp = false;
     		}
    		System.out.println("Road:adding Roadpiece "+x+" "+y);
     		//*/	
     		/*
     		if(gb[x+1][y] == 1){
 	    		roadIterator.add(new RoadSquare(x+1,y));
 	    		x++;
 	    	}else if(gb[x][y+1]==1){
 	    		roadIterator.add(new RoadSquare(x,y+1));
 	    		y++;
 	    	}else{
 	    		temp=false;
 	    	}
     	}
 	    	//*/
     	}
     	System.out.println(roadIterator);
 	}
 		
 	
 	public RoadSquare getFirst(){
 		return roadIterator.get(0); 
 	}
 	
 	public RoadSquare getNext(RoadSquare tempSquare){
 		for(int i = 0; i < roadIterator.size(); i++){
 //			System.out.println("Road: " + roadIterator.get(i).getX() + "  " + roadIterator.get(i+1).getX());
 			
			if(roadIterator.get(i).equals(tempSquare) && !(getLast().equals(tempSquare))){
 				return roadIterator.get(i+1);
 			}
 		}
 		return getLast();
 	}
 	
 	public RoadSquare getLast(){
 		return roadIterator.get(roadIterator.size()-1);
 	}
 	
 	public void typeToString(){
 		for(RoadSquare RS : roadIterator){
 			System.out.println("Road: " + RS.getX() + "  " + RS.getY());
 		}
 	}
 	
 }
