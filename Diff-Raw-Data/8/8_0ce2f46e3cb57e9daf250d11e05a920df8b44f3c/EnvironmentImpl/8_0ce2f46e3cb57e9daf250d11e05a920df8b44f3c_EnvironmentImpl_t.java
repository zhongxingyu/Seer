 package haw.po.la.cliff;
 
 /**
  * @author Bjoern Kulas, Fenja Harbke
  */
 import java.util.ArrayList;
 import java.util.List;
 
 public class EnvironmentImpl implements Environment {
 
 	private int height, width;
 	private Position startPos,finishPos;
 	private List<Position> cliffList;
 	
 	private double normReward = -1.0;
 	private double cliffReward = -10.0;
 	private double finishReward = 20.0;
 	
 	public EnvironmentImpl(int height, int width, Position startField, Position endField, List<Position> cliffList){
 		this.height = height;
 		this.width = width;
 		this.startPos = startField;
 		this.finishPos = endField;
 		this.cliffList = cliffList;
 	}
 	
 	public EnvironmentImpl(int height, int width){
 		this.height = height;
 		this.width = width;
 		this.startPos = new Position(0,0);
 		this.finishPos = new Position(width-1,0);
 		this.cliffList = new ArrayList<Position>();
 		for(int i = 1; i < width -1; i++){
 			cliffList.add(new Position(i,0));
 		}
 	}
 	
 	public int getHeigth(){	return height;}
 	public int getWidth(){ return width;}
 	public Position getStartPosition(){ return startPos;}
 	public Position getFinishPosition(){ return finishPos;}
 	public List<Position> getCliffPositions(){ return cliffList;}
 	
 	public Pair<Position, Double> nextState(Position pos, Direction dir){
 		Position nextPos = pos;
 		
 		double reward = normReward;
 		
 		// don't move from game ending positions
		if (!isGameEndingPosition(pos)) {//-----??
     		switch (dir){
     			case UP: //System.out.println("UP");
     				if(pos.y()-1 >= 0){ nextPos = new Position(pos.x(), pos.y()-1);}
     				break;
     			case DOWN: //System.out.println("DOWN");
     				if(pos.y()+1 < height){ nextPos = new Position(pos.x(), pos.y()+1);}
     				break;
     			case LEFT: //System.out.println("LEFT");
     				if(pos.x()-1 >=0){ nextPos = new Position(pos.x()-1, pos.y());}
     				break;
     			case RIGHT: //System.out.println("RIGHT");
     				if(pos.x()+1 < width){ nextPos = new Position(pos.x()+1, pos.y());}
     				break;
     		}
	}///-------??
 		
 		if(cliffList.contains(nextPos)){
 			//System.out.println("Run into cliff");
 			reward = cliffReward;
		nextPos = pos;//////////---------??
 		}else if (nextPos.equals(finishPos)){
 			reward = finishReward;
 			//System.out.println("REACHED GOAL!!!");
 		}
 		
 
 		
 		return new Pair<Position, Double>(nextPos,reward);
 	}
 	
 	public boolean isGameEndingPosition(Position pos) {
         return getFinishPosition().equals(pos) || getCliffPositions().contains(pos);
 	}
 	
 	public boolean isRunning(){
 		return true;
 	}
 }
