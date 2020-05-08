 import java.util.Arrays;
 import java.lang.Math;
 public class Target{
 	int[][][] map;
 	
 	public Target(int size){
 		map = new int[size][size][size];
 		for(int i=0 ; i<size ; i++)
 			for(int j=0 ; j<size ; j++)
				for(int k=0 ; k<size ; k++)
					map[i][j][k] = 0;
		//Arrays.fill(map,0);
 	}
 	
 	public void init(){
 		int positionX = (int)Math.abs(map.length*Math.random());
 		int positionY = (int)Math.abs(map.length*Math.random());
 		int positionZ = (int)Math.abs(map.length*Math.random());
 		map[positionX][positionY][positionZ] = 1;
 	}
 	
 	private int[] position(){
 		int size = map.length;
 		for(int i=0 ; i<size ; i++){
 			for(int j=0 ; j<size ; j++){
 				for(int k=0 ; k<size ; k++){
 					if(map[i][j][k]==1){
 						int[] position = {i,j,k};
 						return position;
 					}
 				}
 			}
 		}
 		int[] position = {-1,-1,-1};
 		return position;
 	}
 		
 	public Result fire(int targetX, int targetY, int targetZ){
 		int[] position = this.position();
 		if(targetX>=map.length||targetY>=map.length||targetZ>=map.length
 			||targetX<0||targetY<0||targetZ<0){
 			return Result.OUT_OF_RANGE;
 		}else if(targetX==position[0]&&targetY==position[1]&&targetZ==position[2]){
 			return Result.HIT;
 		}else if(targetX<position[0]){
 			return Result.FAIL_LEFT;
 		}else if(targetX>position[0]){
 			return Result.FAIL_RIGHT;
 		}else if(targetY<position[1]){
 			return Result.FAIL_LOW;
 		}else if(targetY>position[1]){
 			return Result.FAIL_HIGH;
 		}else if(targetZ<position[2]){
 			return Result.FAIL_SHORT;
 		}else {
 		return Result.FAIL_LONG;
 		}
 	}
 }
