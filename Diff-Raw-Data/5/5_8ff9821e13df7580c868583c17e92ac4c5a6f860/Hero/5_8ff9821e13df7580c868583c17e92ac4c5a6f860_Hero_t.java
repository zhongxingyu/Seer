 package org.ivan.simple.game.hero;
 
 import org.ivan.simple.game.motion.Motion;
 import org.ivan.simple.game.motion.MotionType;
 import org.ivan.simple.game.level.LevelCell;
 import org.ivan.simple.game.level.PlatformType;
 
 import android.graphics.Canvas;
 
 public class Hero {
 	/**
 	 * Save prev motion after set changed. Prev motion used to get proper animation.
 	 * For example, if prev motion was STEP_LEFT and next motion will be STAY,
 	 * Panda schould turn 90 degrees right in air while jumping on place.
 	 */
 	private boolean finishingState = false;
 	private LevelCell prevCell;
 	private Sprite activeSprite;
 	private TPSprite tpSprite;
 	private SpriteSet sprites;
 	public int x;
 	public int y;
 	private int prevX;
 	private int prevY;
 	public final HeroModel model;
 	
 	public Hero(HeroModel model) {
 		this.model = model;
 		sprites = SpriteSet.getPandaSprites();
 		activeSprite = sprites.getSprite("stay");
 		tpSprite = sprites.getTPSprite("stepleft_tp");
 	}
 	
 	public boolean isFinishing() {
 		return finishingState;
 	}
 	
 	/* properly 'was', 'has been'? */
 	public boolean isFinishingMotionEnded(/*Motion prevMotion*/) {
 		if(activeSprite.getFrame() == 0 ||
 				(model.finishingMotion.getType() == MotionType.FLY_LEFT && activeSprite.getFrame() == 4) ||
 				(model.finishingMotion.getType() == MotionType.FLY_RIGHT && activeSprite.getFrame() == 4)) {
 			finishingState = false;
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	/**
 	 * Check if hero is in control state: ready for begin new motion type.
 	 * Used each game loop iteration to know is it time to process user controls
 	 * and achieve new motion type.
 	 * More often there is control state when next frame is first frame of animation.   
 	 * @return
 	 */
 	public boolean isInControlState() {
 		if((model.finishingMotion.getType() == MotionType.FLY_LEFT  ||
 				model.finishingMotion.getType() == MotionType.FLY_RIGHT) &&
 				finishingState) {
 			return activeSprite.getFrame() == 4;
 		}
 		if(model.currentMotion.getType() == MotionType.FALL_BLANSH) {
 			return activeSprite.getFrame() % 8 == 0;
 		}
 		if(prevCell != null && prevCell.getFloor().getType() == PlatformType.GLUE) {
 			return activeSprite.getFrame() % 8 == 0;
 		}
 		return activeSprite.getFrame() == 0 && tpSprite.getFrame() == 0;
 	}
 	
 	/**
 	 * Change hero behavior (animation) depending on motion type.
 	 * Used after new motion type is obtained. 
 	 * Goal is to play start/end animations of motions.
 	 * @param prevCell
 	 */
 	public void finishPrevMotion(LevelCell prevCell) {
 		this.prevCell = prevCell;
 		prevX = x;
 		prevY = y;
 //		model.finishingMotion = prevMotion;
 //		model.currentMotion = newMotion;
 		if (model.finishingMotion.getChildMotion().isFinishing()) {
 			finishingState = true;
 			switch(model.finishingMotion.getChildMotion().getType()) {
 			case MAGNET:
 				activeSprite = sprites.getSprite("postmagnet");
 //				activeSprite.changeSet(17);
 				break;
 			case STICK_LEFT:
 				activeSprite = sprites.getSprite("poststickleft");
 //				activeSprite.changeSet(30);
 				break;
 			case STICK_RIGHT:
 				activeSprite = sprites.getSprite("poststickright");
 //				activeSprite.changeSet(33);
 				break;
 			case FLY_LEFT:
 				// skip finishing fall down after FLY if finish because wall
 				if(model.currentMotion.getType() == MotionType.JUMP_LEFT_WALL || 
 					model.currentMotion.getType() == MotionType.STICK_LEFT ||
 					model.currentMotion.getType() == MotionType.FLY_RIGHT) {
 					finishingState = false;
 				} else {
 					activeSprite = sprites.getSprite("fallfly");
 					activeSprite.changeSet(0);
 //					activeSprite.changeSet(26);
 				}
 				break;
 			case FLY_RIGHT:
 				// skip finishing fall down after FLY if finish because wall
 				if(model.currentMotion.getType() == MotionType.JUMP_RIGHT_WALL || 
 					model.currentMotion.getType() == MotionType.STICK_RIGHT ||
 					model.currentMotion.getType() == MotionType.FLY_LEFT) {
 					finishingState = false;
 				} else {
 					activeSprite = sprites.getSprite("fallfly");
 					activeSprite.changeSet(0);
 //					activeSprite.changeSet(26);
 				}
 				break;
 			default:
 				break;
 			}
 		} else {
 			finishingState = false;
 		}
 	}
 	
 	/**
 	 * Begins main animation, after finish/start animations became complete
 	 */
 	public void switchToCurrentMotion() {
 		pickActiveSprite(model.currentMotion.getType());
 		MotionType mt = model.currentMotion.getType();
 		int curStage = model.currentMotion.getStage();
 		MotionType prevMt = model.finishingMotion.getChildMotion().getType();
 //		if(prevMt == MotionType.TP_LEFT || prevMt == MotionType.TP_RIGHT) {
 //			prevMt = finishingMotion.getChildMotion().getType();
 //			prevStage = finishingMotion.getChildMotion().getStage();
 //		}
 //		if(mt == MotionType.TP_LEFT || mt == MotionType.TP_RIGHT) {
 //			prevMt = currentMotion.getChildMotion().getType();
 //			prevStage = currentMotion.getChildMotion().getStage();
 //		}
 		switch (mt) {
 		case STAY:
 			if(prevCell.getFloor().getType() == PlatformType.GLUE){
 				activeSprite = sprites.getSprite("glue");
 //				activeSprite.changeSet(0);
 			} else if(prevMt == MotionType.THROW_LEFT || 
 					prevMt == MotionType.JUMP_LEFT ||
 					prevMt == MotionType.TP_LEFT) {
 				activeSprite = sprites.getSprite("stay");
 //				activeSprite.changeSet(0);
 			} else if(prevMt == MotionType.THROW_RIGHT || 
 					prevMt == MotionType.JUMP_RIGHT ||
 					prevMt == MotionType.TP_RIGHT) {
 				activeSprite = sprites.getSprite("stay");
 //				activeSprite.changeSet(0);
 			} else {
 				activeSprite = sprites.getSprite("stay");
 //				activeSprite.changeSet(0);
 			}
 			break;
 		case FALL:
 			if(Math.random() > 0.5) {
 				activeSprite = sprites.getSprite("fall");
 //				activeSprite.changeSet(5);
 			} else {
 				activeSprite = sprites.getSprite("fall2");
 //				activeSprite.changeSet(6);
 			}
 			break;
 		case FALL_BLANSH:
 //			if(curStage == 0) {
 				activeSprite = sprites.getSprite("fallblansh");
 //				activeSprite.changeSet(1);
 //			}
 			break;
 		case JUMP_LEFT:
 			if(prevMt == MotionType.JUMP || prevMt == MotionType.TP) {
 				activeSprite = sprites.getSprite("jumpleft");
 //				activeSprite.changeSet(8);
 			} else if(prevCell.getFloor().getType() == PlatformType.SLICK) {
 				if(prevMt != mt && prevMt != MotionType.JUMP_RIGHT_WALL) {
 					activeSprite = sprites.getSprite("startslickleft");
 				} else {
 					activeSprite = sprites.getSprite("slickleft");
 				}
 			} else if(prevMt == mt || 
 					prevMt == MotionType.THROW_LEFT ||
 					prevMt == MotionType.TP_LEFT) {
 				activeSprite = sprites.getSprite("stepleft");
 //				activeSprite.changeSet(2);
             } else if(prevMt.isCLOUD()) {
                 activeSprite = sprites.getSprite("cloud_out_left");
 			} else {
 				activeSprite = sprites.getSprite("stepleft");
 //				activeSprite.changeSet(3);
 			}
 			break;
 		case JUMP_RIGHT:
 			if(prevMt == MotionType.JUMP || prevMt == MotionType.TP) {
 				activeSprite = sprites.getSprite("jumpright");
 //				activeSprite.changeSet(7);
 			} else if(prevCell.getFloor().getType() == PlatformType.SLICK) {
 				if(prevMt != mt && prevMt != MotionType.JUMP_LEFT_WALL) {
 					activeSprite = sprites.getSprite("startslickright");
 				} else {
 					activeSprite = sprites.getSprite("slickright");
 				}
 			} else if(prevMt == mt || 
 					prevMt == MotionType.THROW_RIGHT ||
 					prevMt == MotionType.TP_RIGHT) {
 				activeSprite = sprites.getSprite("stepright");
 //				activeSprite.changeSet(0);
             } else if(prevMt.isCLOUD()) {
                 activeSprite = sprites.getSprite("cloud_out_right");
 			} else {
 				activeSprite = sprites.getSprite("stepright");
 //				activeSprite.changeSet(1);
 			}
 			break;
 		case JUMP:
 			if(curStage != 0) {
 				activeSprite = sprites.getSprite("jump");
 //				activeSprite.changeSet(4);
 			} else {
 				activeSprite = sprites.getSprite("prejump");
 //				activeSprite.changeSet(9);
 			}
 			break;
 		case THROW_LEFT:
 			if(curStage == 1) {
 				activeSprite = sprites.getSprite("throwleft2");
 //				activeSprite.changeSet(24);
 			} else if(prevMt == MotionType.JUMP_LEFT || prevMt == MotionType.THROW_LEFT) {
 				activeSprite = sprites.getSprite("throwleft1");
 //				activeSprite.changeSet(21);
 			} else {
 				activeSprite = sprites.getSprite("throwleft1");
 //				activeSprite.changeSet(20);
 			}
 			break;
 		case THROW_RIGHT:
 			if(curStage == 1) {
 				activeSprite = sprites.getSprite("throwright2");
 //				activeSprite.changeSet(25);
 			} else if(prevMt == MotionType.JUMP_RIGHT || prevMt == MotionType.THROW_RIGHT) {
 				activeSprite = sprites.getSprite("throwright1");
 //				activeSprite.changeSet(19);
 			} else {
 				activeSprite = sprites.getSprite("throwright1");
 //				activeSprite.changeSet(18);
 			}
 			break;
 		case JUMP_LEFT_WALL:
 			switch(prevMt) {
 			case JUMP:
 			case THROW_LEFT:
 			case FLY_LEFT:
 			case TP:
 //			case JUMP_RIGHT_WALL: 
 				activeSprite = sprites.getSprite("jumpleftwall");
 //				activeSprite.changeSet(12);
 				break;
 			default:
 				if(prevCell.getFloor().getType() == PlatformType.THROW_OUT_LEFT) {
 					activeSprite = sprites.getSprite("jumpleftwall");
 //					activeSprite.changeSet(12);
 				} else if(prevCell.getFloor().getType() == PlatformType.SLICK) {
 					activeSprite = sprites.getSprite("slickleftwall");
 				} else {
 					activeSprite = sprites.getSprite("stepleftwall");
 //					activeSprite.changeSet(3);
 				}
 				break;
 			}
 			break;
 		case JUMP_RIGHT_WALL:
 			switch(prevMt) {
 			case JUMP:
 			case THROW_RIGHT:
 			case FLY_RIGHT:
 			case TP:
 //			case JUMP_LEFT_WALL:
 				activeSprite = sprites.getSprite("jumprightwall");
 //				activeSprite.changeSet(11);
 				break;
 			default:
 				if(prevCell.getFloor().getType() == PlatformType.THROW_OUT_RIGHT) {
 					activeSprite = sprites.getSprite("jumprightwall");
 //					activeSprite.changeSet(11);
 				} else if(prevCell.getFloor().getType() == PlatformType.SLICK) {
 					activeSprite = sprites.getSprite("slickrightwall");
 				} else {
 					activeSprite = sprites.getSprite("steprightwall");
 //					activeSprite.changeSet(2);
 				}
 				break;
 			}
 			break;	
 		case BEAT_ROOF:
 			activeSprite = sprites.getSprite("beatroof");
 //			activeSprite.changeSet(10);
 			break;
 		case MAGNET:
 			if(curStage == 0) {
 				activeSprite = sprites.getSprite("premagnet");
 //				activeSprite.changeSet(13);
 			} else {
 				activeSprite = sprites.getSprite("magnet");
 //				activeSprite.changeSet(14);
 			}
 			break;
 		case FLY_LEFT:
 			if(prevMt == mt || prevMt == MotionType.TP_LEFT) {
 				activeSprite = sprites.getSprite("flyleft");
 //				activeSprite.changeSet(22);
 			} else if(prevMt == MotionType.FLY_RIGHT || prevMt == MotionType.THROW_RIGHT) {
 				activeSprite = sprites.getSprite("jumprightwall");
 			} else if(prevMt == MotionType.JUMP ||
 					prevMt == MotionType.TP || 
 					prevCell.getFloor().getType() == PlatformType.THROW_OUT_RIGHT) {
 				activeSprite = sprites.getSprite("beginflyleft8");
 //				activeSprite.changeSet(11);
 			} else {
 				activeSprite = sprites.getSprite("beginflyleft");
 //				activeSprite.changeSet(7);
 			}
 			break;
 		case FLY_RIGHT:
 			if(prevMt == mt || prevMt == MotionType.TP_RIGHT) {
 				activeSprite = sprites.getSprite("flyright");
 //				activeSprite.changeSet(23);
 			} else if(prevMt == MotionType.FLY_LEFT || prevMt == MotionType.THROW_LEFT ) {
 				activeSprite = sprites.getSprite("jumpleftwall");
 			} else if(prevMt == MotionType.JUMP ||
 					prevMt == MotionType.TP ||
 					prevCell.getFloor().getType() == PlatformType.THROW_OUT_LEFT) {
 				activeSprite = sprites.getSprite("beginflyright8");
 //				activeSprite.changeSet(12);
 			} else {
 				activeSprite = sprites.getSprite("beginflyright");
 //				activeSprite.changeSet(6);
 			}
 			break;
 		case TP_LEFT:
 			MotionType childMt = model.currentMotion.getChildMotion().getType();
 			int childStage = model.currentMotion.getChildMotion().getStage();
 			if(childMt == MotionType.JUMP_LEFT) {
 				if(prevMt == MotionType.JUMP) {
 					tpSprite = sprites.getTPSprite("jumpleft_tp");
 //					activeSprite = sprites.getSprite("jumpleft_tp");
 				} else if(prevCell.getFloor().getType() == PlatformType.SLICK) {
 					if(prevMt != mt && prevMt != MotionType.JUMP_RIGHT_WALL) {
 						tpSprite = sprites.getTPSprite("startslickleft_tp");
 //						activeSprite = sprites.getSprite("startslickleft_tp");
 					} else {
 						tpSprite = sprites.getTPSprite("slickleft_tp");
 //						activeSprite = sprites.getSprite("slickleft_tp");
 					}
 				} else if(prevMt == MotionType.JUMP_LEFT || prevMt == MotionType.THROW_LEFT) {
 					tpSprite = sprites.getTPSprite("stepleft_tp");
 //					activeSprite = sprites.getSprite("stepleft_tp");
 				} else {
 					tpSprite = sprites.getTPSprite("stepleft_tp");
 //					activeSprite = sprites.getSprite("stepleft_tp");
 				}
 			} else if(childMt == MotionType.THROW_LEFT && childStage == 0) {
 				if(prevMt == MotionType.THROW_LEFT || prevMt == MotionType.JUMP_LEFT) {
 					tpSprite = sprites.getTPSprite("throwleft1_tp");
 //					activeSprite = sprites.getSprite("throwleft1_tp");
 				} else {
 					tpSprite = sprites.getTPSprite("throwleft1_tp");
 //					activeSprite = sprites.getSprite("throwleft1_tp");
 				}
 			} else if(childMt == MotionType.THROW_LEFT) {
 				tpSprite = sprites.getTPSprite("throwleft2_tp");
 //				activeSprite = sprites.getSprite("throwleft2_tp");
 			} else if(childMt == MotionType.FLY_LEFT) {
 				tpSprite = sprites.getTPSprite("flyleft_tp");
 //				activeSprite = sprites.getSprite("flyleft_tp");
 			} else {
 				tpSprite = sprites.getTPSprite("stepleft_tp");
 //				activeSprite = sprites.getSprite("stepleft_tp");
 			}
 			break;
 		case TP_RIGHT:
 			MotionType childMt1 = model.currentMotion.getChildMotion().getType();
 			int childStage1 = model.currentMotion.getChildMotion().getStage();
 			if(childMt1 == MotionType.JUMP_RIGHT) {
 				if(prevMt == MotionType.JUMP) {
 					tpSprite = sprites.getTPSprite("jumpright_tp");
 //					activeSprite = sprites.getSprite("jumpright_tp");
 				} else if(prevCell.getFloor().getType() == PlatformType.SLICK) {
 					if(prevMt != mt && prevMt != MotionType.JUMP_LEFT_WALL) {
 						tpSprite = sprites.getTPSprite("startslickright_tp");
 //						activeSprite = sprites.getSprite("startslickright_tp");
 					} else {
 						tpSprite = sprites.getTPSprite("slickright_tp");
 //						activeSprite = sprites.getSprite("slickright_tp");
 					}
 				} else if(prevMt == MotionType.JUMP_RIGHT || prevMt == MotionType.THROW_RIGHT) {
 					tpSprite = sprites.getTPSprite("stepright_tp");
 //					activeSprite = sprites.getSprite("stepright_tp");
 				} else {
 					tpSprite = sprites.getTPSprite("stepright_tp");
 //					activeSprite = sprites.getSprite("stepright_tp");
 				}
 			} else if(childMt1 == MotionType.THROW_RIGHT && childStage1 == 0) {
 				if(prevMt == MotionType.THROW_RIGHT || prevMt == MotionType.JUMP_RIGHT) {
 					tpSprite = sprites.getTPSprite("throwright1_tp");
 //					activeSprite = sprites.getSprite("throwright1_tp"); 
 				} else {
 					tpSprite = sprites.getTPSprite("throwright1_tp");
 //					activeSprite = sprites.getSprite("throwright1_tp"); 
 				}
 			} else if(childMt1 == MotionType.THROW_RIGHT) {
 				tpSprite = sprites.getTPSprite("throwright2_tp");
 //				activeSprite = sprites.getSprite("throwright2_tp");
 			} else if(childMt1 == MotionType.FLY_RIGHT) {
 				tpSprite = sprites.getTPSprite("flyright_tp");
 //				activeSprite = sprites.getSprite("flyright_tp");
 			} else {
 				tpSprite = sprites.getTPSprite("stepright_tp");
 //				activeSprite = sprites.getSprite("stepright_tp");
 			}
 			break;
 		case STICK_LEFT:
 			if(curStage == 0) {
                 if(prevMt == MotionType.JUMP) {
				    activeSprite = sprites.getSprite("prestickleftjump");
                 } else {
                    activeSprite = sprites.getSprite("prestickleft");
                 }
 //				activeSprite.changeSet(28);
 			} else {
 				activeSprite = sprites.getSprite("stickleft");
 //				activeSprite.changeSet(29);
 			}
 			break;
 		case STICK_RIGHT:
 			if(curStage == 0) {
                 if(prevMt == MotionType.JUMP) {
                     activeSprite = sprites.getSprite("prestickrightjump");
                 } else {
 				    activeSprite = sprites.getSprite("prestickright");
                 }
 //				activeSprite.changeSet(31);
 			} else {
 				activeSprite = sprites.getSprite("stickright");
 //				activeSprite.changeSet(32);
 			}
 			break;
 		case TP:
 			if(curStage == 0) {
 				activeSprite = sprites.getSprite("fallinto");
 //				activeSprite.changeSet(34);
 			} else {
 				activeSprite = sprites.getSprite("flyout");
 //				activeSprite.changeSet(35);
 			}
 			break;
 		case CLOUD_IDLE:
             if(prevMt.isCLOUD()) {
 			    activeSprite = sprites.getSprite("cloud");
             } else {
                 activeSprite = sprites.getSprite("cloud_prepare");
             }
 			break;
 		case CLOUD_LEFT:
             activeSprite = sprites.getSprite("cloud_left");
             break;
 		case CLOUD_RIGHT:
             activeSprite = sprites.getSprite("cloud_right");
             break;
 		case CLOUD_UP:
             activeSprite = sprites.getSprite("cloud_up");
             break;
 		case CLOUD_DOWN:
 			activeSprite = sprites.getSprite("cloud_down");
 			break;
 		default:
 			activeSprite = sprites.getSprite("glue");
 //			activeSprite.changeSet(0);
 			break;
 		}
 	}
 
 	
 	/**
 	 * Used to get proper bitmap for motion
 	 * @param mt
 	 */
 	private void pickActiveSprite(MotionType mt) {
 		switch(mt) {
 		case NONE:
 		case STAY:
 		case FALL_BLANSH:
 			activeSprite = sprites.getSprite("fallblansh");
 			break;
 		case TP_LEFT:
 		case TP_RIGHT:
 //			activeSprite = tpSprite;
 			break;
 		default:
 //			activeSprite = sprite8;
 			break;
 		}
 	}
 	
 	/**
 	 * Draw proper hero animation frame by center coordinates
 	 * @param canvas
 	 */
 	public void onDraw(Canvas canvas, boolean update) {
 		if(!finishingState && (model.currentMotion.getType() == MotionType.TP_LEFT || model.currentMotion.getType() == MotionType.TP_RIGHT)) {
 			tpSprite.onDraw(canvas, prevX, prevY, x, y, update);
 		} else {
 			activeSprite.onDraw(canvas, x, y, update);
 		}
 	}
 	
 	/**
 	 * Real motion type used to get proper hero speed on start/finish/main animations
 	 * @return
 	 */
 	public Motion getRealMotion() {
 		if(finishingState) {
 			return new Motion(MotionType.NONE);
 		} else {
 			return model.currentMotion;
 		}
 	}
 	
 	/**
 	 * Play loose animation
 	 */
 	public boolean playLoseAnimation() {
 //		activeSprite = sprites.getSprite("fall");
         activeSprite = sprites.getSprite("detonate");
         activeSprite.setPlayOnce(true);
         if(!activeSprite.isAnimatingOrDelayed()) {
             return false;
         }
         return true;
 	}
 	
 	/**
 	 * Play win animation
 	 * @return is sprite animating?
 	 */
 	public boolean playWinAnimation() {
 		activeSprite = sprites.getSprite("fallinto");
 		activeSprite.setPlayOnce(true);
 		if(!activeSprite.isAnimatingOrDelayed()) {
 			activeSprite.goToFrame(7);
 			return false;
 		}
 		return true;
 	}
 }
