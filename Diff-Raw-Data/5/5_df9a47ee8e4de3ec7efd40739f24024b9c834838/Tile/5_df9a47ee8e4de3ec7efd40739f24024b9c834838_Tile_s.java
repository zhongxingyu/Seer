 package edu.stanford.mobisocial.games.wordplay.tiles;
 
 import org.anddev.andengine.engine.camera.hud.HUD;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.input.touch.TouchEvent;
 
 import android.graphics.Point;
 import android.util.Log;
 import edu.stanford.mobisocial.games.wordplay.WordPlayActivity;
 import edu.stanford.mobisocial.games.wordplay.constants.LetterValues;
 
 public class Tile {
 
 	private char letter;
 	private int points;
 	private Sprite sprite, overlay, active;
 	private int pos;
 	private float lastX, lastY;
 	private int lastSize;
 	WordPlayActivity context;
 	Scene scene;
 	boolean inHud;
 	boolean showMe;
 	boolean draggedFromHud;
 	HUD blankTilePicker;
 	
 	private int xPos, yPos;
 	
 	private Sprite getNewSprite() {
 		return new Sprite(lastX, lastY, context.letterTileRegions.get(Character.valueOf(letter))){
             @Override
             public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                 //3+i*21, 20+j*21
             	float x = pSceneTouchEvent.getX();
             	float y = pSceneTouchEvent.getY();
             	//Log.w("tile", "tile dragged at: " + x + ", " + y);
             	
             	//Log.w("tile", "ontouch");
     			moveToBoard();
     			if (context.mCamera.getZoomFactor() > 1 && draggedFromHud) {
 	    			float centerX = context.mCamera.getCenterX();
 	    			float centerY = context.mCamera.getCenterY();
 
     				float originX = centerX - (context.mCamera.getBoundsWidth()/(context.mCamera.getZoomFactor()*2));
     				float originY = centerY - (context.mCamera.getBoundsHeight()/(context.mCamera.getZoomFactor()*2));
     				
     				x = (x/context.mCamera.getZoomFactor()) + originX;
     				y = (y/context.mCamera.getZoomFactor()) + originY;
     				//Log.w("tile", "adjusted to: " + x + ", " + y);
     			}
     			
     			int tempXPos = (int)(x-3)/21;
         		int tempYPos = (int)(y-23)/21;
         		
     			
             	if(pSceneTouchEvent.isActionUp()){
             		draggedFromHud = false;
             		
             		
             		if (tempXPos >= 0 && tempXPos < 15 && tempYPos >= 0 && tempYPos < 15) {
             			if(context.tileSpaces[tempXPos][tempYPos].letter == '0' && context.tileRack.noOverlaps(pos, tempXPos, tempYPos)) {
 	                        
 	                        Tile.this.setSize(21, true);
 	                        
 	            			//int tempX = ((int) x / 21);
 	            			//int tempY = ((int) y / 21) - 1;
 	                        Tile.this.xPos = tempXPos;
 	                        Tile.this.yPos = tempYPos;
 	                        Log.w("tile", "setting position to " + xPos + ", " + yPos);
 	            			x = tempXPos * 21 + 3;
 	            			y = tempYPos * 21 + 23;
 	            			Tile.this.setPosition(x, y);
 	            			lastX = x;
 	            			lastY = y;
 	            			
 	            			if (points == 0) {
 	            				context.showingPicker = true;
 		            			blankTilePicker = new HUD();
 		            			int j = 0;
 		            			int k = 0;
 		            			for(char let = 'a'; let <= 'z'; let++) {
 		            				//Log.w("tile", "" + let);
 		            				final char tempVal = let;
 		            				Sprite temp = new Sprite(43+(48*j), 60+(48*k), context.letterTileRegions.get(let)){
 		            		            @Override
 		            		            public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
 		                	        		//Log.w("tile", "touched");
 		            		            	if (pSceneTouchEvent.isActionUp()) {
 		            		            		setLetter(tempVal, true);
 		                	        			context.mCamera.setHUD(context.hud);
 
 		    		            				context.showingPicker = false;
 		                	        		}
 		            		            	return true;
 		            		            }
 		            				};
 		            				blankTilePicker.attachChild(temp);
 		            				blankTilePicker.registerTouchArea(temp);
 		            				j++;
 		            				if(j == 5) {
 		            					j = 0;
 		            					k++;
 		            				}
 		            			}
 		            			context.mCamera.setHUD(blankTilePicker);
 	            			}
             			}
             			else {
             				Tile.this.setSize(lastSize, false);
             				if(lastSize == 45) {
             					returnToRack();
             				}
             				else {
                 				Tile.this.setPosition(lastX, lastY);	
             				}
             			}
             		}
             		else if (y >= 338 && y <= 383) {
             			//moveToHud();
             			returnToRack();
             			context.tileRack.insertTileAtPos(scene, Tile.this, ((int)x-3) / 45);
             		}
             		else {
             			returnToRack();
             		}
             		context.removeCrosshair();
             		context.showTentativePoints();
             	}
             	else {
         			Tile.this.setSize(45, false);
             		Tile.this.setPosition(x-25, y-25);
 
             		context.drawCrosshair(tempXPos, tempYPos);
             	}
             	return true;
             }
         };
 	}
 	
 	public Tile(final WordPlayActivity context, final Scene scene, char letter, int i, boolean showme) {
 		//Log.w("tile", "creating tile " + letter);
 		this.letter = letter;
 		this.points = LetterValues.getLetterValue(letter);
 		this.context = context;
 		this.scene = scene;
 		pos = i;
 		lastX = 3+45*pos;
 		lastY = 338;
 		lastSize = 45;
 		inHud = true;
 		showMe = showme;
 		draggedFromHud = true;
 		
 		xPos = -1;
 		yPos = -1;
 		
 		if (showMe) {
 			overlay = new Sprite(lastX, lastY, context.pointTileRegions[points]);
 			active = new Sprite(lastX, lastY, context.activeOverlayRegion);
 			sprite = getNewSprite();
 	        
 	
 	        
 	        setSize(45, false);
 	        
 	        context.hud.attachChild(sprite);
 	        context.hud.registerTouchArea(sprite);
 	        context.hud.attachChild(overlay);
 	        context.hud.setTouchAreaBindingEnabled(true);
 	        scene.setTouchAreaBindingEnabled(true);
             
 		}
 	}
 	
 	public void setSize(int size, boolean setLastSize) {
 		if(!showMe) { return; }
 		sprite.setWidth(size);
 		sprite.setHeight(size);
 		overlay.setWidth(size);
 		overlay.setHeight(size);
 		active.setWidth(size);
 		active.setHeight(size);
 		if (setLastSize) {
 			lastSize = size;
 		}
 	}
 	
 	
 	public boolean overlaps(int testX, int testY) {
 		Log.w("tile", this.xPos + " vs " + testX + " AND " + this.yPos + " vs " + testY);
 		return (testX != -1 && testY != -1 && this.xPos == testX && this.yPos == testY);
 	}
 	
 	public void setLetter(char let, final boolean toBoard) {
 		letter = let;
 		if (!toBoard) {
 			lastX = 3+45*pos;
 			lastY = 338;
 		}
 		context.runOnUpdateThread(new Runnable() {
             @Override
             public void run() {
             	//Log.w("tile", "removing tile");
 		        if (context.hud.getChildIndex(sprite) >= 0) {
 		        	context.hud.detachChild(sprite);
 		        	context.hud.unregisterTouchArea(sprite);
 		        	context.hud.detachChild(overlay);
 		        }
 		        else {
 		        	scene.detachChild(sprite);
 		        	scene.unregisterTouchArea(sprite);
 		        	scene.detachChild(overlay);
 		        	scene.detachChild(active);
 		        	
 		        }
 		        sprite = getNewSprite();
 		        
 		
 		        
 		        setSize(45, false);
 		        
 		        if (toBoard) {
 			        scene.attachChild(sprite);
 			        scene.registerTouchArea(sprite);
 			        scene.attachChild(overlay);
 			        scene.attachChild(active);
 			        scene.setTouchAreaBindingEnabled(true);
 			        moveToBoard();
 			        setSize(21, true);
 		        }
 		        else {
 			        context.hud.attachChild(sprite);
 			        context.hud.registerTouchArea(sprite);
 			        context.hud.attachChild(overlay);
 			        context.hud.setTouchAreaBindingEnabled(true);
 		        	
 		        }
             }
     	});
 	}
 	
 	public void setPosition(float x, float y){
 		if(!showMe) { return; }
 		sprite.setPosition(x, y);
 		overlay.setPosition(x, y);
 		active.setPosition(x, y);
 	}
 	
 	public char getLetter() {
 		if(points == 0) {
 			return Character.toUpperCase(letter);
 		}
 		else {
 			return letter;
 		}
 	}
 	
 	public void setPos(int i) {
 		if(!showMe) { return; }
 		pos = i;
 		if (sprite.getY() >= 338 && sprite.getY() <= 383) {
 			lastX = 4+45*pos;
 			lastY = 338;
 			lastSize = 45;
 			sprite.setPosition(lastX, lastY);
 		}
 		if (inHud){
 			//Log.w("tile", "returning " + letter + " to rack");
 			returnToRack();
 		}
 	}
 	
 	public int getPos() {
 		return pos;
 	}
 	
 	public int getPoints() {
 		return points;
 	}
 	
 	public void returnToRack() {
 		if(!showMe) { return; }
 		draggedFromHud = true;
 		if (points == 0) {
 			setLetter(' ', false);
 		}
 		xPos = -1;
 		yPos = -1;
 		inHud = true;
     	context.runOnUpdateThread(new Runnable() {
             @Override
             public void run() {
                 if (scene.getChildIndex(sprite) >= 0) {
                 	scene.detachChild(sprite);
                 	scene.unregisterTouchArea(sprite);
                 	scene.detachChild(overlay);
                 	scene.detachChild(active);
                 }
 		        if (context.hud.getChildIndex(sprite) < 0) {
 		        	//Log.w("Tile", "attaching " + letter + " to hud");
 		        	context.hud.attachChild(sprite);
 		        	context.hud.registerTouchArea(sprite);
 		        	context.hud.attachChild(overlay);
 		        }
 		        
 		        setSize(45, false);
 		        //Log.w("Tile", scene.getChildIndex(sprite) + "");
 				Tile.this.setPosition(3+45*pos, 338);
 				context.showTentativePoints();
             }
     	});
 	}
 	
 	private void moveToHud() {
 		if(!showMe) { return; }
 		inHud = true;
     	context.runOnUpdateThread(new Runnable() {
             @Override
             public void run() {
                 if (scene.getChildIndex(sprite) >= 0) {
                 	scene.detachChild(sprite);
                 	scene.unregisterTouchArea(sprite);
                 	scene.detachChild(overlay);
                 	scene.detachChild(active);
                 }
 		        if (context.hud.getChildIndex(sprite) < 0) {
 		        	context.hud.attachChild(sprite);
 		        	context.hud.registerTouchArea(sprite);
 		        	context.hud.attachChild(overlay);
 		        }
             }
     	});
 	}
 	
 	private void moveToBoard() {
 		if(!showMe) { return; }
 		inHud = false;
     	context.runOnUpdateThread(new Runnable() {
             @Override
             public void run() {
             	if(inHud) return;
 		        if (context.hud.getChildIndex(sprite) >= 0) {
 		        	context.hud.detachChild(sprite);
 		        	context.hud.unregisterTouchArea(sprite);
 		        	context.hud.detachChild(overlay);
 		        }
 		        if (scene.getChildIndex(sprite) < 0) {
 		        	//Log.w("tile", "attaching " + letter + " to board");
 		        	scene.attachChild(sprite);
 		        	scene.registerTouchArea(sprite);
 		        	scene.attachChild(overlay);
 		        	scene.attachChild(active);
 		        }
             }
     	});
 	}
 	
 	public Sprite getSprite() {
 		return sprite;
 	}
 	
 	public void removeTile() {
 		context.runOnUpdateThread(new Runnable() {
             @Override
             public void run() {
             	//Log.w("tile", "removing tile");
 		        if (context.hud.getChildIndex(sprite) >= 0) {
 		        	context.hud.detachChild(sprite);
 		        	context.hud.unregisterTouchArea(sprite);
 		        	context.hud.detachChild(overlay);
 		        }
 		        else {
 		        	scene.detachChild(sprite);
 		        	scene.unregisterTouchArea(sprite);
 		        	scene.detachChild(overlay);
 		        	scene.detachChild(active);
 		        }
             }
     	});
 	}
 	public void finalizeTile() {
 		if(!showMe) { return; }
     	context.runOnUpdateThread(new Runnable() {
             @Override
             public void run() {
 				scene.unregisterTouchArea(sprite);
 				scene.detachChild(sprite);
             }
     	});
 	}
 	
 	public Point getCoordinates() {
 		if(!showMe) { return null; }
 		if(sprite.getY() > 318) {
 			return null;
 		}
		return new Point(((int) sprite.getX() / 21), ((int) sprite.getY() / 21) - 1);
 	}
	
 }
