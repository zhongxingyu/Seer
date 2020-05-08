 package org.ivan.simple.hero;
 
 import org.ivan.simple.ImageProvider;
 import org.ivan.simple.MotionType;
 import org.ivan.simple.R;
 
 import android.graphics.Canvas;
 
 public class Hero {
 	/**
 	 * Save prev motion after set changed. Prev motion used to get proper animation.
 	 * For example, if prev motion was STEP_LEFT and next motion will be STAY,
 	 * Panda schould turn 90 degrees right in air while jumping on place.
 	 */
 	private MotionType prevMotion = MotionType.NONE;
 	private Sprite sprite = new Sprite(ImageProvider.getBitmap(R.drawable.panda_sprite), 17, 16);
 	
 	public Hero() {
 	}
 	
 	public Sprite getSprite() {
 		return sprite;
 	}
 	
 	/**
 	 * Check if hero is in control state: ready for begin new motion type.
 	 * Used each game loop iteration to know is it time to process user controls
 	 * and achieve new motion type.
 	 * More often there is control state when next frame is first frame of animation.   
 	 * @return
 	 */
 	public boolean isInControlState() {
 		if(sprite.currentSet == 12) return sprite.currentFrame == 8;
 		if(sprite.currentSet == 0 ||
 				sprite.currentSet == 7 ||
				sprite.currentSet == 8 ||
				sprite.currentSet == 15 ||
				sprite.currentSet == 16) {
 			return sprite.currentFrame == 0;
 		}
 		return sprite.currentFrame % 8 == 0;
 	}
 	
 	/**
 	 * Change hero behavior (animation) depending on motion type.
 	 * Used after new motion type is obtained. 
 	 * @param mt
 	 */
 	public void changeSet(MotionType mt) {
 		switch (mt) {
 		case STAY:
 			if(prevMotion == MotionType.STEP_LEFT || prevMotion == MotionType.JUMP_LEFT) {
 				sprite.changeSet(7);
 			} else if(prevMotion == MotionType.STEP_RIGHT || prevMotion == MotionType.JUMP_RIGHT) {
 				sprite.changeSet(8);
 			} else {
 				sprite.changeSet(0);
 			}
 			break;
 		case FALL:
 			if(Math.random() > 0.5) {
 				sprite.changeSet(6);
 			} else {
 				sprite.changeSet(11);
 			}
 			break;
 		case STEP_LEFT:
 			if(prevMotion == mt) {
 				sprite.changeSet(3);
 			} else {
 				sprite.changeSet(4);
 			}
 			break;
 		case JUMP_LEFT:
 			sprite.changeSet(10);
 			break;
 		case STEP_RIGHT:
 			if(prevMotion == mt) {
 				sprite.changeSet(1);
 			} else {
 				sprite.changeSet(2);
 			}
 			break;
 		case JUMP_RIGHT:
 			sprite.changeSet(9);
 			break;
 		case PRE_JUMP:
 			sprite.changeSet(12);
 			break;
 		case STEP_LEFT_WALL:
 			sprite.changeSet(16);
 			break;
 		case STEP_RIGHT_WALL:
 			sprite.changeSet(15);
 			break;
 		default:
 			sprite.changeSet(5);
 			break;
 		}
 		prevMotion = mt;
 	}
 	
 	public void onDraw(Canvas canvas, int x, int y) {
 		sprite.onDraw(canvas, x - sprite.getWidth() / 2, y - sprite.getHeight() / 2);
 	}
 }
