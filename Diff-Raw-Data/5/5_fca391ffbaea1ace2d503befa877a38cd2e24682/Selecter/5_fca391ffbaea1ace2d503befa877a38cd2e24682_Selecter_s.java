 package com.suckless;
 
 public abstract class Selecter extends Command {
 	public enum StateDirection {
 	    LEFT,
 	    RIGHT,
 	    UP,
 	    DOWN,
 	    NONE
 	}
 	
 	public int x,y,maxX,maxY;
 	public StateDirection direction;
 	boolean first = true;
 	
 	Selecter(GameObject inputObject,StateDirection dir) {
 		super(inputObject);
 		direction = dir;
 	}
 	
 	public abstract Command handleSelect(GameObject inputObject);
 	
 	@Override
 	public Command Select(Field[][] states, Player player){
 		switch(direction){
 		case LEFT:	direction = StateDirection.DOWN;
 			break;
 		case RIGHT:	direction = StateDirection.UP;
 			break;
 		case UP:	return handleSelect(selectedObject);
 		case DOWN:	return handleSelect(selectedObject);
 		default:
 			break;
 		}
 		return this;
 	}
 	
 		@Override
 	public Command Update(Field[][] states, Player player){
 			System.out.println(String.format("%d %d", x,y));
 			maxX = states.length;
 			maxY = states[0].length;
 			if(first){
 				if(direction == StateDirection.LEFT){
 					x = maxX;
 				}else{
 					x = 0;
 				}
 				first = false;
 			}
 		switch(direction){
 			case LEFT:	if(x < 0){
 							return null;
 						}
 						else{
 							x = x-1;
 						}			
 				break;
 			case RIGHT:	if(x >= maxX){
 							return null;
 						}
 						else{
 							x = x+1;
 						}
 				break;
			case UP:	if(y <= 0){
 							return null;
 						}
 						else{
 							y = y+1;
 						}
 				break;
			case DOWN:	if(y >= maxY){
 							return null;
 						}
 						else{
 							y = y-1;
 						}
 				break;
 			default:
 				break;
 		}
 		return this;
 	}
 }
