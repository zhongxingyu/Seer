 package com.buildcoolrobots.games.pgcgame.Screens;
 
 import me.pagekite.glen3b.gjlib.ExtendedLabel;
 import me.pagekite.glen3b.gjlib.ExtendedSprite;
 import me.pagekite.glen3b.gjlib.SimpleSprite;
 import me.pagekite.glen3b.gjlib.SpriteManager;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.buildcoolrobots.games.pgcgame.CoreTypes.BaseScreen;
 import com.buildcoolrobots.games.pgcgame.CoreTypes.DPad;
 import com.buildcoolrobots.games.pgcgame.CoreTypes.Enums.DPadDirection;
 import com.buildcoolrobots.games.pgcgame.CoreTypes.Enums.GameImage;
 import com.buildcoolrobots.games.pgcgame.CoreTypes.Enums.ScreenType;
 import com.buildcoolrobots.games.pgcgame.CoreTypes.Enums.ShipTypes;
 
 public class GameScreen extends BaseScreen {
 
 	ExtendedSprite Ship;
 	DPad Dpad;
 	ExtendedLabel xy;
 	String coor = "0,0";
 	public GameScreen(SpriteManager allSprites, SpriteBatch spriteBatch, ScreenType screenType) {
 		super(allSprites, spriteBatch, screenType);
 		
 	
 		
 		Ship = new ExtendedSprite(ShipTypes.PLAYERSHIP.GameTexture());
 		Ship.setPosition(100, Gdx.graphics.getHeight()/2 - Ship.getHeight()/2);
 		Dpad = new DPad();
 		
 		xy = new ExtendedLabel(coor , GameImage.DEBUGFONT.ImageText());
 		xy.setPosition(Gdx.graphics.getWidth() - 150, 50);
 		//xy.setFontScale(2,2);
 		
 		allSprites.add(xy);
 		allSprites.add(Ship);
 		allSprites.add((SimpleSprite) Dpad);
 
 	}
 	
 	public void update(float deltaTime) {
 		super.update(deltaTime);
 
 		if (Gdx.input.isTouched()) {
 			coor = "" + Gdx.input.getX() + "," + (Gdx.graphics.getHeight() - Gdx.input.getY());
 			xy.setText(coor + "\ndir: " + Dpad.shipDirection.toString());
 			
 			if (!SettingsScreen.leftyMode) {
 				Dpad.setPosition(0, 0);
 			} else {
 				Dpad.setPosition(Gdx.graphics.getWidth() - Dpad.getWidth(), 0);
 			}
 			
 	        
 		}
 		
 		switch(DPad.shipDirection){
 		case NONE:{
 			Ship.xSpeed = 0;
 			Ship.ySpeed = 0;
 			break;
 		}
 		case NORTH:{
         	Ship.ySpeed = 5;
         	break;
         }
 		case NORTHEAST:{
 			Ship.xSpeed = 5;
 			Ship.ySpeed = 5;
 			break;
 		}
 		case EAST:{
         	Ship.xSpeed = 5;
         	break;
         }
 		case SOUTHEAST:{
 			Ship.xSpeed = 5;
 			Ship.ySpeed = -5;
 			break;
 		}
 		case SOUTH:{
 			Ship.ySpeed = -5;
 			break;
 		}
 		case SOUTHWEST:{
 			Ship.xSpeed = -5;
 			Ship.ySpeed = -5;
 			break;
 		}
 		case WEST:{
 			Ship.xSpeed = -5;
 			break;
 		}
 		case NORTHWEST:{
 			Ship.xSpeed = -5;
 			Ship.ySpeed = 5;
 			break;
 		}
 		}
 	}
 }
