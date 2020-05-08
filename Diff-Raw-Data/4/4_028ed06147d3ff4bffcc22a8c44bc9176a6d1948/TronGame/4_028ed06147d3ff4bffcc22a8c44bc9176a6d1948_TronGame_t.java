 package com.tfuruya.tron;
 
 public class TronGame {
     
 	private int numPlayer;
 	
 	public TronGame(int player) {
 		player = numPlayer;
 	}
 	
 	
 	public TronData update(TronData data, TronAction[] action) {
 		
         int x[] = new int[numPlayer];
         int y[] = new int[numPlayer];
         
         for (int i=0; i<numPlayer; i++) {
         	
         	// Initialize x, y
         	x[i] = data.getX()[i];
         	y[i] = data.getY()[i];
         	
         	// If dead, don't move
         	if (data.isDead(i)) continue;
         	
         	// Update to new point according to given action
         	switch (action[i].get()) {
           		case TronAction.LEFT: {
           			x[i] --;
           			break;
           		}
           		case TronAction.UP: {
           			y[i] --;
           			break;
           		}
           		case TronAction.RIGHT: {
           			x[i] ++;
           			break;
           		}
           		case TronAction.DOWN: {
           			y[i] ++;
           			break;
           		}
         	}
           	
         	// evaluate new point
         	if (!data.isValid(x[i], y[i])) {
         		data.setDead(i);
         	}
         }
         
         
         // modify data
         if (!data.isGameOver) {
         	for (int j=0; j<numPlayer; j++) {
        		if (!data.isDead(j)) {
        			data.occupyNewPoint(x[j], y[j], action[j], j);
        		}
             }
         }
         
         return data;
 	}
 
 	
 	// Called at beginning of every game
 	public void reset(TronData data, int player) {
 		numPlayer = player;
 	}
 }
