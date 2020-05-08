 package org.ivan.simple.game.level;
 
 import org.ivan.simple.game.motion.Motion;
 import org.ivan.simple.game.motion.MotionType;
 import org.ivan.simple.game.hero.Sprite;
 
 import android.graphics.Canvas;
 
 public class Platform {
 	private PlatformType type = PlatformType.NONE;
 	private Sprite sprite = null;
 	private int currentStatus = 0;
 	private static int switchHelper = 0;
 
 	public Platform(PlatformType type) {
 		if(type == null) return;
 		this.type = type;
 		switch(type) {
 		case SIMPLE:
 			sprite = new Sprite("platform_h/simple_platform.png", 4, 8);
 			break;
 		case SIMPLE_V:
 			sprite  = new Sprite("platform_v/simple_platform_v.png", 1, 1);
 			break;
 		case REDUCE:
 			sprite = new Sprite("platform_h/reduce_platform.png", 4, 8);
 			break;
 		case ANGLE_RIGHT:
 			sprite = new Sprite("platform_h/angle_platform_right.png", 1, 8);
 			break;
 		case ANGLE_LEFT:
 			sprite = new Sprite("platform_h/angle_platform_left.png", 1, 8);
 			break;
 		case THROW_OUT_RIGHT:
 			sprite = new Sprite("platform_h/throw_out_platform_right.png", 1, 8);
 			break;
 		case THROW_OUT_LEFT:
 			sprite = new Sprite("platform_h/throw_out_platform_left.png", 1, 8);
 			break;	
 		case TRAMPOLINE:
 			sprite = new Sprite("platform_h/trampoline_platform.png",1,8);
 			break;
 		case ELECTRO:
 			sprite = new Sprite("platform_h/electro_platform.png",1,4);
 			sprite.setAnimating(true);
 			break;
 		case SPRING:
 			sprite = new Sprite("platform_v/spring_platform.png",2,16);
 			break;
 		case SPIKE:
 			sprite = new Sprite("platform_h/spike.png",1,8);
 			sprite.setAnimating(true);
 			break;
 		case SPIKE_V:
 			sprite = new Sprite("platform_v/spike_v.png", 1, 8);
 			sprite.setAnimating(true);
 			break;
         case SPIKE_UP:
             sprite = new Sprite("platform_h/spike_up.png", 1, 8);
             sprite.setAnimating(true);
             break;
 		case WIN:
 			sprite = new Sprite("platform_h/win_platform.png", 1, 8);
 			sprite.setAnimating(true);
 			break;
 		case TELEPORT_L_V:
 			sprite = new Sprite("platform_v/teleport_l_v.png", 1, 16);
 			sprite.setAnimating(true);
 			break;
 		case TELEPORT_R_V:
 			sprite = new Sprite("platform_v/teleport_r_v.png", 1, 16);
 			sprite.setAnimating(true);
 			break;
 		case SLICK:
 			sprite = new Sprite("platform_h/slick.png", 1, 1);
 			break;
 		case SLOPE:
 			sprite = new Sprite("platform_h/slope.png", 3, 1);
 			break;
 		case ONE_WAY_LEFT:
 			sprite = new Sprite("platform_v/one_way_right.png", 1, 16);
 			break;
 		case ONE_WAY_RIGHT:
 			sprite = new Sprite("platform_v/one_way_left.png", 1, 16);
 			break;
 		case ONE_WAY_DOWN:
 			sprite = new Sprite("platform_h/one_way_down.png", 1, 16);
 			break;
 		case ONE_WAY_UP:
 			sprite = new Sprite("platform_h/one_way_up.png", 1, 16);
 			break;
 		case SWITCH:
 			sprite = new Sprite("platform_v/switch_platform.png", 4, 8, switchHelper);
 			currentStatus = switchHelper;
 			switchHelper = (switchHelper + 1) % 4;
 			break;
 		case UNLOCK:
 			sprite = new Sprite("platform_v/unlock_platform.png", 2, 8);
             sprite.setAnimating(true);
 			break;
 		case STRING:
 			sprite = new Sprite("platform_h/string_platform.png", 2, 16);
 			break;
 		case LIMIT:
 			sprite = new Sprite("platform_v/limit_way.png", 4, 8);
 			break;
 		case BRICK:
 			sprite = new Sprite("platform_h/brick.png", 4, 1);
 			break;
 		case BRICK_V:
 			sprite = new Sprite("platform_v/brick_v.png", 4, 1);
 			break;
 		case GLUE:
 			sprite = new Sprite("platform_h/glue.png", 1, 1);
 			break;
 		case GLUE_V:
 			sprite = new Sprite("platform_v/glue_v.png", 1, 1);
 			break;
 		case TELEPORT:
 			sprite = new Sprite("platform_h/teleport.png", 1, 16);
 			sprite.setAnimating(true);
 			break;
 		case INVISIBLE:
 			sprite = new Sprite("platform_h/invisible_platform.png", 2, 8);
 			break;
 		case TRANSPARENT:
 			sprite = new Sprite("platform_h/transparent_platform.png", 1, 8);
 			break;
 		case TRANSPARENT_V:
 			sprite = new Sprite("platform_v/transparent_platform_v.png", 1, 8);
 			break;
 		case WAY_UP_DOWN:
 			sprite = new Sprite("platform_h/way_up_down.png", 2, 16);
 			break;
 		case CLOUD:
 			sprite = new Sprite("platform_h/cloud.png", 1, 1);
 			break;
 		case NONE:
 			break;
 		}
 	}
 	
 	public PlatformType getType() {
 		return type;
 	}
 	
 	public void updateFloor(Motion motion, Motion prevMotion) {
 		MotionType mt = motion.getType();
 		MotionType prevMt = prevMotion.getType();
 		// CLOUD
 		if(type == PlatformType.NONE) {
 			if(!mt.isCLOUD() && prevMt.isCLOUD()) {
 				type = PlatformType.CLOUD;
 				sprite = new Sprite("platform_h/cloud.png", 1, 1);
 			}
 			return;
 		}
 		if(sprite == null || 
 				mt == MotionType.MAGNET || 
 				mt == MotionType.BEAT_ROOF || 
 				mt == MotionType.THROW_LEFT && motion.getStage() != 0 || 
 				mt == MotionType.THROW_RIGHT && motion.getStage() != 0 || 
 				mt == MotionType.JUMP && motion.getStage() != 0 && type != PlatformType.TRAMPOLINE ||
 				mt == MotionType.FLY_LEFT && motion.getStage() != 0 ||
 				mt == MotionType.FLY_RIGHT && motion.getStage() != 0 ||
 				mt == MotionType.TP_LEFT && prevMt == MotionType.FLY_LEFT ||
 				mt == MotionType.TP_RIGHT && prevMt == MotionType.FLY_RIGHT ||
 				mt == MotionType.TP_LR && prevMt == MotionType.FLY_LEFT ||
 				mt == MotionType.TP_RL && prevMt == MotionType.FLY_RIGHT ||
				mt == MotionType.JUMP_LEFT_WALL && (prevMt == MotionType.FLY_LEFT || prevMt == MotionType.THROW_LEFT) ||
				mt == MotionType.JUMP_RIGHT_WALL && (prevMt == MotionType.FLY_RIGHT || prevMt == MotionType.THROW_RIGHT) ||
 				mt == MotionType.FLY_RIGHT && prevMt == MotionType.FLY_LEFT && prevMotion.getStage() != 0 ||
 				mt == MotionType.FLY_LEFT && prevMt == MotionType.FLY_RIGHT && prevMotion.getStage() != 0) {
 			return;
 		}
 		if(type == PlatformType.REDUCE) {
 			if(currentStatus<3) {
 				currentStatus++;
 				sprite.playOnce(true);
 			} else if(currentStatus<4){
 				currentStatus++;
 				sprite.playOnce(true);
 				type = PlatformType.NONE;
 			}
 			return;
 		}
 		if(type == PlatformType.ANGLE_LEFT) {
 			sprite.playOnce();
 			return;
 		}
 		if(type == PlatformType.ANGLE_RIGHT) {
 			sprite.playOnce();
 			return;
 		}
 		if(type == PlatformType.THROW_OUT_LEFT) {
 			sprite.playOnce();
 			return;
 		}
 		if(type == PlatformType.THROW_OUT_RIGHT) {
 			sprite.playOnce();
 			return;
 		}
 		if(type == PlatformType.TRAMPOLINE) {
 			sprite.playOnce();
 			return;
 		}
 		if(type == PlatformType.SLOPE) {
 			switch(mt) {
 			case JUMP_LEFT:
 			case JUMP_LEFT_WALL:
 				currentStatus = 1;
 				sprite.changeSet(1);
 				break;
 			case JUMP_RIGHT:
 			case JUMP_RIGHT_WALL:
 				currentStatus = 2;
 				sprite.changeSet(2);
 				break;
 			default:
 				break;
 			}
 			return;
 		}
 		if(type == PlatformType.SIMPLE) {
 			switch(mt) {
 	//		case STEP_LEFT:
 	//			sprite.setAnimating(sprite.changeSet(1));
 	//			sprite.playOnce = true;
 	//			break;
 	//		case STEP_RIGHT:
 	//			sprite.setAnimating(sprite.changeSet(2));
 	//			sprite.playOnce = true;
 	//			break;
 			case JUMP_LEFT:
 				sprite.changeSet(1);
 				sprite.playOnce();
 				break;
 			case JUMP_RIGHT:
 				sprite.changeSet(2);
 				sprite.playOnce();
 				break;
 			case JUMP:
 			default:
 				sprite.changeSet(0);
 				sprite.playOnce();
 				break;
 			}
 			return;
 		}
 		if(type == PlatformType.ONE_WAY_DOWN && mt == MotionType.FALL) {
 			sprite.playOnce();
 			return;
 		}
 		if(type == PlatformType.WAY_UP_DOWN && mt == MotionType.FALL) {
 			sprite.changeSet(1);
 			sprite.playOnce();
 			return;
 		}
 		if(type == PlatformType.STRING) {
 			if(mt == MotionType.STAY) {
 				sprite.changeSet(1);
 				sprite.playOnce();
 				type = PlatformType.NONE;
 			} else if(mt == MotionType.JUMP) {
 				sprite.playOnce();
 			}
 			return;
 		}
 		if(type == PlatformType.INVISIBLE) {
             if(currentStatus == 0) {
                 sprite.playOnce(true);
                 currentStatus = 1;
             } else {
                 sprite.changeSet(1);
                 sprite.playOnce();
             }
 			return;
 		}
 		if(type == PlatformType.TRANSPARENT) {
 			sprite.playOnce();
 			type = PlatformType.NONE;
 			return;
 		}
 		// CLOUD
 		if(type == PlatformType.CLOUD) {
 			if(mt.isCLOUD()) {
 				type = PlatformType.NONE;
 				sprite = null;
 			}
 			return;
 		}
 	}
 	
 	public void updateRoof(Motion motion) {
 		MotionType mt = motion.getType();
 		int stage = motion.getStage();
 		if(mt == MotionType.BEAT_ROOF) {
 			if(type == PlatformType.SIMPLE) {
 				sprite.changeSet(3);
 				sprite.playOnce();
 			}
 			if(type == PlatformType.BRICK) {
 				if(currentStatus < 3) {
 					currentStatus++;
 					sprite.changeSet(currentStatus);
 				} else {
 					sprite = null;
 					type = PlatformType.NONE;
 				}
 			}
 			if(type == PlatformType.INVISIBLE) {
 				sprite.playOnce(currentStatus == 0);
 				currentStatus = 1;
 			}
 		}
 		if(mt == MotionType.JUMP && stage != 0 && type == PlatformType.ONE_WAY_UP) {
 			sprite.playOnce();
 		}
 		if(mt == MotionType.JUMP && stage != 0 && type == PlatformType.WAY_UP_DOWN) {
 			sprite.changeSet(0);
 			sprite.playOnce();
 		}
 		
 		if(mt == MotionType.JUMP && stage != 0 && type == PlatformType.TRANSPARENT) {
 			sprite.playOnce();
 			type = PlatformType.NONE;
 		}
 	}
 	
 	public void highlightSpring(Motion prevMotion) {
 		MotionType prevMt = prevMotion.getType();
 		if(type == PlatformType.SPRING) {
 			switch(prevMt) {
 			case JUMP:
 			case FLY_LEFT:
 			case FLY_RIGHT:
 			case THROW_LEFT:
 			case THROW_RIGHT:
 			case TP:
 				sprite.playOnce(0);
 				break;
 			default:
 				sprite.playOnce(10);
 				break;
 			}
 		}
 	}
 	
 	public void updateLeftWall(Motion motion, Motion prevMotion) {
 		MotionType mt = motion.getType();
 		int stage = motion.getStage();
 		MotionType prevMt = prevMotion.getType();
 		if(mt == MotionType.JUMP_LEFT ||
 				mt == MotionType.FLY_LEFT && stage != 0 ||
 				mt == MotionType.THROW_LEFT ||
 				mt == MotionType.JUMP_LEFT_WALL) {
 			if(type == PlatformType.ONE_WAY_LEFT) {
 				sprite.playOnce();
 			}
 			if(type == PlatformType.LIMIT && currentStatus < 3) {
 				currentStatus = currentStatus + 1;
 				sprite.playOnce(true);
 			}
 			if(type == PlatformType.TRANSPARENT_V) {
 				sprite.playOnce();
 				type = PlatformType.NONE;
 			}
 			if(type == PlatformType.SWITCH) {
 				currentStatus = (currentStatus + 1) % 4;
 				sprite.changeSet(currentStatus);
 				sprite.goToFrame(1);
 				switch(prevMt) {
 				case JUMP:
 				case FLY_LEFT:
 				case THROW_LEFT:
 				case TP:
 					sprite.playOnce(0);
 					break;
 				default:
 					sprite.playOnce(10);
 					break;
 				}
 			}
 			if(type == PlatformType.BRICK_V) {
 				if(currentStatus < 3) {
 					currentStatus++;
 					sprite.playOnce(10, true);
 				} else {
 					sprite.playOnce(10, true);
 					type = PlatformType.NONE;
 				}
 			}
 		}
 	}
 	
 	public void updateRightWall(Motion motion, Motion prevMotion) {
 		MotionType mt = motion.getType();
 		int stage = motion.getStage();
 		MotionType prevMt = prevMotion.getType();
 		if(mt == MotionType.JUMP_RIGHT ||
 				mt == MotionType.FLY_RIGHT && stage != 0 ||
 				mt == MotionType.THROW_RIGHT ||
 				mt == MotionType.JUMP_RIGHT_WALL) {
 			if(type == PlatformType.ONE_WAY_RIGHT) {
 				sprite.playOnce();
 			}
 			if(type == PlatformType.LIMIT && currentStatus < 3) {
 				currentStatus = currentStatus + 1;
 				sprite.playOnce(true);
 			}
 			if(type == PlatformType.TRANSPARENT_V) {
 				sprite.playOnce();
 				type = PlatformType.NONE;
 			}
 			if(type == PlatformType.SWITCH) {
 				currentStatus = (currentStatus + 1) % 4;
 				sprite.changeSet(currentStatus);
 				sprite.goToFrame(1);
 				switch(prevMt) {
 				case JUMP:
 				case FLY_RIGHT:
 				case THROW_RIGHT:
 				case TP:
 					sprite.playOnce(0);
 					break;
 				default:
 					sprite.playOnce(10);
 					break;
 				}
 			}
 			if(type == PlatformType.BRICK_V) {
 				if(currentStatus < 3) {
 					currentStatus++;
 					sprite.playOnce(10, true);
 				} else {
 					sprite.playOnce(10, true);
 					type = PlatformType.NONE;
 				}
 			}
 		}
 	}
 	
 	public void unlock() {
         setStatus(1);
         sprite.playOnce();
 		type = PlatformType.NONE;
 	}
 	
 	public int getStatus() {
 		return currentStatus;
 	}
 
     public void setStatus(int currentStatus) {
         this.currentStatus = currentStatus;
         sprite.changeSet(currentStatus);
     }
 
     public void onDraw(Canvas canvas, int x, int y, boolean update) {
 		if(sprite != null) {
 			if(type == PlatformType.NONE && !sprite.isAnimatingOrDelayed()) {
 				sprite = null;
 			} else {
 				sprite.onDraw(canvas, x, y, update);
 			}
 		}
 	}
 }
