 package org.anddev.andengine.pvb;
 
 import java.util.LinkedList;
 
 import org.anddev.andengine.entity.IEntity;
 import org.anddev.andengine.entity.primitive.Rectangle;
 import org.anddev.andengine.entity.scene.menu.MenuScene;
 import org.anddev.andengine.entity.shape.IShape;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.extra.ExtraScene;
 import org.anddev.andengine.extra.Resource;
 import org.anddev.andengine.input.touch.TouchEvent;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 import org.anddev.andengine.pvb.card.Card;
 import org.anddev.andengine.pvb.card.Flower1;
 import org.anddev.andengine.pvb.card.Flower2;
 import org.anddev.andengine.pvb.singleton.GameData;
 
 import android.util.Log;
 
 public class Game extends ExtraScene {
 
 	private TextureRegion mBack;
 	private TextureRegion mTable;
 	private Card mSelect;
 
 	@Override
 	public void createScene() {
 		this.mBack = Resource.getTexture(1024, 512, "back");
 		this.mTable  = Resource.getTexture(1024, 128, "table");
 		Sprite back = new Sprite(0, 0, this.mBack);
 		Sprite table = new Sprite(0, 0, this.mTable);
 		getChild(BACKGROUND_LAYER).attachChild(back);
 		getChild(BACKGROUND_LAYER).attachChild(table);
 		
 		for (int i = 0; i < 45; i++) {
 			int x = i % 9;
 			int y = (int)(i / 9);
			Rectangle field = new Rectangle(0, 0, 68, 75);
 			field.setColor(0f, 0f, 0f);
			field.setAlpha(0.2f);
			field.setPosition(44 + x * 71,  93 + y * 78);
 			getChild(BACKGROUND_LAYER).attachChild(field);
 			
 			registerTouchArea(field);
 		}
 	}
 
 	@Override
 	public void startScene() {
 		LinkedList<Card> cards = GameData.getInstance().getCards();
 		cards.add(new Flower1(this, 0, 0));
 		cards.add(new Flower2(this, 0, 0));
 		cards.add(new Flower1(this, 0, 0));
 		cards.add(new Flower1(this, 0, 0));
 		cards.add(new Flower2(this, 0, 0));
 		cards.add(new Flower1(this, 0, 0));
 		
 		int start_x = 106;
 		
 		for (int i = 0; i < cards.size(); i++) {
 			Card c = cards.get(i);
 			c.setPosition(start_x + i * 69, 7);
 			getChild(EXTRA_GAME_LAYER).attachChild(c);
 		}
 	}
 
 	@Override
 	public void endScene() {
 		
 	}
 
 	@Override
 	public void manageAreaTouch(ITouchArea pTouchArea) {
 		if (pTouchArea instanceof Card) {
 			Log.i("Game", "card");
 			
 			Card c = (Card) pTouchArea;
 			this.mSelect = c.makeSelect();
 		} else {
 			if (this.mSelect != null && this.mSelect.isReady()) { // aggiungere controllo costo
 				IEntity field = (IEntity) pTouchArea;
 				if (field.getChildCount() == 0) {
 					Log.i("Game", "recharge/object");
 					
 					this.mSelect.startRecharge();
 					
 					IShape obj = this.mSelect.getObject();
 					obj.setPosition(this.mSelect.getWidthScaled() / 2 - obj.getWidthScaled() / 2, this.mSelect.getHeightScaled() / 2 - obj.getHeightScaled() / 2);
 					field.attachChild(obj);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void manageSceneTouch(TouchEvent pSceneTouchEvent) {
 		
 	}
 
 	@Override
 	public MenuScene createMenu() {
 		return null;
 	}
 
 }
