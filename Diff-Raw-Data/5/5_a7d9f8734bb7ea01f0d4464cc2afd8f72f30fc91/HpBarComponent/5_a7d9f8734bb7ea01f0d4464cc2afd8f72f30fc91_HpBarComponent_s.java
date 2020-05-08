 package com.liongrid.infectosaurus.components;
 
 import com.liongrid.gameengine.Component;
 import com.liongrid.gameengine.DrawableBitmap;
 import com.liongrid.infectosaurus.InfectoGameObject;
 import com.liongrid.infectosaurus.R;
 
 public class HpBarComponent extends Component<InfectoGameObject> {
 	DrawableBitmap mBarBackground;
 	DrawableBitmap mBarForeground;
 	int mHeight = 10;
 	int mWidth = 64;
 	
 	public HpBarComponent() {
 		mBarBackground = new DrawableBitmap
 		(gamePointers.textureLib.allocateTexture(R.drawable.red),mWidth,mHeight);
 		mBarForeground = new DrawableBitmap
 		(gamePointers.textureLib.allocateTexture(R.drawable.green),0,mHeight);
 		
 	}
 	
 	@Override
 	public void update(float dt, InfectoGameObject parent) {
		 mBarForeground.setWidth(
				 Math.round(mWidth*(parent.mHp/(float)parent.mMaxHp)));
 		
 		 
 		 SpriteComponent sprite = (SpriteComponent) parent.findComponentOfType(SpriteComponent.class);
 		 
 		 if(sprite == null) return;
 		 
 		 int spriteHeight = sprite.lastDrawing.getHeight();
 		 
 		 gamePointers.renderSystem.scheduleForDraw(
 				 mBarBackground, parent.pos.x-0.5f*mWidth, parent.pos.y+0.5f*spriteHeight, false);
 		 gamePointers.renderSystem.scheduleForDraw(
 				 mBarForeground,  parent.pos.x-0.5f*mWidth, parent.pos.y+0.5f*spriteHeight, false);
 	}
 
 }
