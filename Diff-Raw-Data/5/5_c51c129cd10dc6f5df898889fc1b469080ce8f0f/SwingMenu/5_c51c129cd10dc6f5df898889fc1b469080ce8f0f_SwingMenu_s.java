 package com.tacoid.puyopuyo.actors;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Interpolation;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.tacoid.puyopuyo.PuyoPuyo;
 import com.tacoid.puyopuyo.PuyoPuyo.ScreenOrientation;
 
 public class SwingMenu extends Group{
 	private int VIRTUAL_WIDTH;
 	
 	private static final float BUTTON_HEIGHT = 250;
 	
 	private enum State {
 		SHOWING,
 		HIDING,
 		IDLE;
 	};
 	
 	private Map<String,Group> menus;
 	private Group currentGroup;
 	private String currentName;
 
 	private Interpolation interpBush;
 	private Interpolation interpButton;
 	private float timeBush;
 	private float timeButton;
 	private State state = State.IDLE;
 	
 	private boolean switching = false;
 	private String nextMenu;
 	
 	
 	
 	private class BushActor extends Actor {
 		private TextureRegion ForegroundTex;
 
 		public BushActor() {
 			ForegroundTex = new TextureRegion(PuyoPuyo.getInstance().atlasPlank.findRegion("foreground"));
 		}
 		
 		@Override
 		public void draw(SpriteBatch batch, float delta) {
 			boolean keepGoing = false;
 			switch(state) {
 			case HIDING:
 				if(timeBush >= 0.5f) {
 					timeBush-=Gdx.graphics.getDeltaTime()*0.6;
 					keepGoing = true;
 				}
 				
 				if( timeButton >= -0.1f) {
 					timeButton-=Gdx.graphics.getDeltaTime()*1.0;
 					keepGoing = true;
 				}
 				
 				if(!keepGoing) {
 					if(switching) {
 						show(nextMenu);
 					} else {
 						this.touchable =false;
 						this.visible = false;
 						state = State.IDLE;
 					}
 				}
 				break;
 			case SHOWING:
 				if(timeBush <= 1.0f) {
 					timeBush+=Gdx.graphics.getDeltaTime()*0.4;
 					keepGoing = true;
 				}
 				
 				if( timeButton <= 1.0f) {
 					timeButton+=Gdx.graphics.getDeltaTime()*0.5;
 					keepGoing = true;
 				} 
 				if(!keepGoing){
 					state = State.IDLE;
 				}
 				break;
 			case IDLE:
 				break;
 			}
 			
 			currentGroup.y = interpButton.apply(0, BUTTON_HEIGHT, Math.min(timeButton,1.0f));
 			/*
 			for(int i=0; i<currentGroup.size(); i++) {
 				currentList.get(i).y = interpButton.apply(0, BUTTON_HEIGHT, Math.min(timeButton,1.0f));
 			}*/
 			batch.draw(ForegroundTex,0,interpBush.apply(-ForegroundTex.getRegionHeight(),0.0f,Math.min(timeBush,1.0f)));
 		}
 
 		@Override
 		public Actor hit(float arg0, float arg1) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 	}
 	
 	public SwingMenu(ScreenOrientation orientation) {
 		
 		if(orientation == ScreenOrientation.LANDSCAPE) {
 			VIRTUAL_WIDTH = 1280;
 		} else {
 			VIRTUAL_WIDTH = 768;
 		}
 		
 		interpBush = new Interpolation.Pow(2);
 		interpButton = new Interpolation.SwingOut(1.5f);
 		menus = new HashMap<String, Group>();
 	}
 	
 	public void initBegin(String menu) {
 		menus.put(menu, new Group());
 		currentGroup = menus.get(menu);
 		currentName = menu;
 	}
 	
 	public void addButton( Actor actor) {
 		currentGroup.addActor(actor);
 	}
 	
 	public void initEnd() {
 		int size = currentGroup.getActors().size();
 		for(int i=0; i<size; i++) {
 			/*currentList.get(i).x = VIRTUAL_WIDTH*(i+1)/(buttons.size()+1)-128;*/
 			currentGroup.getActors().get(i).x = (i+1)*(VIRTUAL_WIDTH-size*256)/(size+1)+i*256;
 			currentGroup.getActors().get(i).y = 0;
 		//addActor(buttons.get(i));
 		}
 	}
 	public void show(String menu) 
 	{
 		this.clear();
 		currentGroup = menus.get(menu);
 		currentName = menu;
 		
 		this.addActor(currentGroup);
 		this.addActor(new BushActor());
 		
 		state=State.SHOWING;
 		timeBush = 0.5f;
		timeButton = -0.1f;
 		this.touchable =true;
 		this.visible = true;
 		switching = false;
 	}
 	
 	public void hide() {
 		state=State.HIDING;
 	}
 	
 	public void hideInstant() {
 		state=State.HIDING;
 		timeBush = 0.5f;
		timeButton = -0.1f;
 	}
 	
 	/* Change de menu avec animation */
 	public void switchMenuAnimated(String menu) {
 		this.hide();
 		switching = true;
 		nextMenu = menu;
 	}
 	
 	/* Change de menu sans animation */
 	public void switchMenu(String menu) {
 		this.clear();
 		currentGroup = menus.get(menu);
 		
 		this.addActor(currentGroup);
 		this.addActor(new BushActor());
 	}
 	
 	public String getCurrentMenu() {
 		return currentName;
 	}
 	
 
 }
