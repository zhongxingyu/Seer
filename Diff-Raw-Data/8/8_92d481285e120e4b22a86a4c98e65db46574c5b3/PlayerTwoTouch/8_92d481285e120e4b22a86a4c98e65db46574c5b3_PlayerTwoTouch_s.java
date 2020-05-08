 package org.ssg.Cambridge;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.geom.Polygon;
 import org.newdawn.slick.geom.Transform;
 
 import paulscode.sound.SoundSystem;
 import paulscode.sound.SoundSystemConfig;
 
 public class PlayerTwoTouch extends Player{
 
 	float DEFAULTKICK;
 	float EXTRAKICK;
 	float[] ballPos, prevBallPos;
 	Ball ball;
 	
 	//flag used for knocking the ball out of the locked zone, true when player can't lock ball
 	boolean lockCoolDown;
 	
 	//only used during lock
 	float angle;//Atan2 returns from pi to -pi
 	float angleTarget;
 	//float angle2;//Versions of angle and angleTarget respecified from 0 to 2pi
 	float prevAngleTarget;
 	float curveFactor;
 	float[] prevVel;
 	int rotateDir;
 	
 	//only used during lock for prediction
 	Ball predictor;
 	int predictionCount;
 	int PREDICTIONCOUNT = 300;
 	float[] predictionPos, predictionVel, predictionCurveAcc;
 	float predictionVelMag, predictionCurveMag;
 	float predictionVDelta, predictionDelta;
 	float predictionTempX, predictionTempY;
 	
 	float[] predictionKickFloat;
 	float[] predictionSpinFloat;
 	
 	public PlayerTwoTouch(int n, int tN, float[] consts, int[] f, int[] c, CambridgeController c1, float[] p, int[] xyL, Color se, SoundSystem ss, String sn, Image slc, Ball b, Ball pb) {
 		super(n, tN, consts, f, c, c1, p, xyL, se, ss, sn, slc);
 		
 		poly = new Polygon(new float[]{0,0,-PLAYERSIZE/3, -PLAYERSIZE/2, PLAYERSIZE*2/3, 0, -PLAYERSIZE/3, PLAYERSIZE/2});
 		
 		DEFAULTKICK = NORMALKICK;
 		EXTRAKICK = POWERKICK;
 		POWERKICK = 0;
 
 		angleTarget = 0;
 		prevAngleTarget = 0;
 		angle = 0;
 		
 		prevVel = new float[2];
 		rotateDir = 1;
 		
 		ball = b;
 		ballPos = new float[2];
 		prevBallPos = new float[2];
 		
 		predictor = pb;
 		
 		MAXPOWER = 10;
 		
 		curveFactor = 5;
 		
 		lockCoolDown = false;
 		
 		predictionCount = 0;
 		
 		predictionPos = new float[2];
 		predictionVel = new float[2];
 		predictionCurveAcc = new float[2];
 		
 		predictionVelMag = 0f;
 		predictionCurveMag = 0f;
 		predictionVDelta = 0f;
 		predictionDelta = 0f;
 		predictionTempX = 0f;
 		predictionTempY = 0f;
 		
 		predictionKickFloat = new float[2];
 		predictionSpinFloat = new float[2];
 	}
 	
 	@Override
 	public void drawPlayer(Graphics g){
 		g.setColor(getColor());
 		g.setLineWidth(2f);
 		g.translate(pos[0],pos[1]);
 		tempf = (float)Math.atan2(curve[1], curve[0]);
 		tempArr[0] = mag(curve)*(float)(Math.cos(tempf)*Math.sin(theta)-Math.cos(theta)*Math.sin(tempf));
 		poly = (Polygon) poly.transform(Transform.createRotateTransform(theta+tempArr[0]));
 		g.draw(poly);
 		poly = (Polygon) poly.transform(Transform.createRotateTransform(-theta-tempArr[0]));
 		g.translate(-pos[0], -pos[1]);
 		g.setLineWidth(5f);
 			
 	}
 	
 	@Override
 	public void drawPowerCircle(Graphics g){
 		//Draw power circle
 		if(ball.locked(playerNum)){
 			g.setColor(getColor(mag(vel)/velMag+.5f));
 			g.drawOval(ballPos[0]-KICKRANGE-power/2f, ballPos[1]-KICKRANGE-power/2f, (KICKRANGE+power/2f)*2, (KICKRANGE+power/2f)*2);
 			g.setColor(Color.white);
 			drawBallPrediction(g);
 		}else if(isPower()){
 			g.setColor(getColor3());
 			g.drawOval(getX()-getKickRange()/2f-getPower()/2f, getY()-getKickRange()/2f-getPower()/2f, getKickRange()+getPower(), getKickRange()+getPower());
 			g.setColor(Color.white);
 		}
 	}	
 
 	public void drawBallPrediction(Graphics g){
 		predictor.setPos(ballPos[0], ballPos[1]);
 		
 		predictionKickFloat[0] = (prevBallPos[0]-getX());
 		predictionKickFloat[1] = (prevBallPos[1]-getY());
 
 		unit(predictionKickFloat);
 		if(sameDir(getVel()[0], predictionKickFloat[0])){
 			predictionKickFloat[0] += getKick()[0];
 		}
 		if(sameDir(getVel()[1], predictionKickFloat[1])){
 			predictionKickFloat[1] += getKick()[1];
 		}
 		unit(predictionKickFloat);
 		tempf = Math.abs(dot(getVel(), predictionKickFloat)); 
 		predictor.setVel(new float[]{predictionKickFloat[0], predictionKickFloat[1]}, .2f+VELMAG*tempf*kickStrength());
 
 		predictionSpinFloat = normal(getCurve(), predictionKickFloat);
 		predictor.setCurve(predictionSpinFloat, mag(predictionSpinFloat)*curveStrength());
 		
 //		System.out.println("Predicted kickfloat:"+predictionKickFloat[0]+", "+predictionKickFloat[1]);
 //		System.out.println("Predicted spinfloat:"+tempBrr[0]+", "+tempBrr[1]);
 		
 //		System.out.println("Predicted:"+predictor.curveMag+", "+predictor.curveAcc[0]+", "+predictor.curveAcc[1]);
 		
 		for(int i=0; i<PREDICTIONCOUNT && (predictor.getX()>0 && predictor.getX()<field[0] && predictor.getY()>0 && predictor.getY()<field[1]); i++){
 			predictor.update(6f);
 			
 			if (i%10 == 0 && i > 0) {
 				g.setColor(getColor(1f-(float)i/(float)PREDICTIONCOUNT));
 				g.fillRect(predictor.getX()-3, predictor.getY()-3, 6, 6);
 			}
 			
 		}
 		
 	}
 	
 	@Override
 	public void update(float delta){
 		
 		if (c.exists()) {
 			
 			prevVel[0] = vel[0];
 			prevVel[1] = vel[1];
 			
 			pollController(delta);
 			
 			if (c.getAction()){
 				if(!buttonPressed){
 					activatePower();
 					buttonPressed = true;
 				}
 			}else if(buttonPressed){
 					powerKeyReleased();
 					buttonPressed = false;
 			}
 		
 		}else{
 			
 			prevVel[0] = vel[0];
 			prevVel[1] = vel[1];
 			
 			if(ball.locked(playerNum)){
 				tempArr[0] = (float)Math.atan2(vel[1],  vel[0]);
 				tempArr[1] = (float)Math.atan2(curve[1], curve[0]);
 				
 				//Rotate around with left and right
 				if(left&&!right){
 					tempf = tempArr[0]+delta/240f;
 					vel[0] = (float)Math.cos(tempf);
 					vel[1] = (float)Math.sin(tempf);
 					tempf = tempArr[1]+delta/240f;
 					curve[0] = (float)Math.cos(tempf);
 					curve[1] = (float)Math.sin(tempf);
 				}else if(!left && right){
 					tempf = tempArr[0]-delta/240f;
 					vel[0] = (float)Math.cos(tempf);
 					vel[1] = (float)Math.sin(tempf);
 					tempf = tempArr[1]-delta/240f;
 					curve[0] = (float)Math.cos(tempf);
 					curve[1] = (float)Math.sin(tempf);
 				}
 				
 				//Set curve with up and down
 				if(up && !down){
 					if(vel[0]<0){
 						if(tempArr[0]<0)
 							tempArr[0]+=(float)Math.PI*2f;
 						if(tempArr[1]<0)
 							tempArr[1]+=(float)Math.PI*2f;
 						tempf = tempArr[1]+delta/240f;
 						if(tempf>tempArr[0]+(float)Math.PI/2f)
 							tempf = tempArr[0]+(float)Math.PI/2f;
 					}else{
 						tempf = tempArr[1]-delta/240f;
 						if(tempf < tempArr[0]-(float)Math.PI/2f)
 							tempf = tempArr[0]-(float)Math.PI/2f;
 					}
 					curve[0] = (float)Math.cos(tempf);
 					curve[1] = (float)Math.sin(tempf);
 				}else if(!up && down){
 					if(vel[0]<0){
 						if(tempArr[0]<0)
 							tempArr[0]+=(float)Math.PI*2f;
 						if(tempArr[1]<0)
 							tempArr[1]+=(float)Math.PI*2f;
 						tempf = tempArr[1]-delta/240f;
 						if(tempf<tempArr[0]-(float)Math.PI/2f)
 							tempf = tempArr[0]-(float)Math.PI/2f;
 					}else{
 						tempf = tempArr[1]+delta/240f;
 						if(tempf> tempArr[0]+(float)Math.PI/2f)
 							tempf = tempArr[0]+(float)Math.PI/2f;
 					}
 					curve[0] = (float)Math.cos(tempf);
 					curve[1] = (float)Math.sin(tempf);
 				}
 
 			}else{
 				pollKeys(delta);
 			}
 			
 			if(buttonPressed){
 				activatePower();
 				buttonPressed = false;
 			}
 			if(buttonReleased){
 				powerKeyReleased();
 				buttonReleased = false;
 			}
 			
 		}
 		
 //		predictionVDelta = delta;
 //		predictionDelta = delta;
 		
 		//For if the ball gets knocked out of your hands
 		if(dist(pos[0],pos[1],ball.getX(),ball.getY())>=KICKRANGE/2f+3f+velMag*delta+1f ){
 			ball.setLocked(playerNum, false);
 			lockCoolDown = false;
 			NORMALKICK = DEFAULTKICK;
 		}
 		
 		//System.out.println(ball.getVel()[0]+", "+ball.getVel()[1]+": "+ball.getVelMag());
 		
 		//Entering Lock
 		if(!lockCoolDown && power>0 && !ball.locked(playerNum) && dist(pos[0],pos[1],ball.getX(),ball.getY())<KICKRANGE/2f && !ball.scored()){
 			ball.clearLocked();
 			ball.setLocked(playerNum, true);
 //			ball.setCanBeKicked(playerNum, true);
 			ball.setLastKicker(teamNum);
 			ball.setVel(new float[]{ball.getVelX(),ball.getVelY()},ball.getVelMag()/4f);
 			ball.slowDown(0, ball.getVelMag()/24f, 0);
 			//Sets vel to be relative position to ball, so you don't jump on contact
 			tempArr[0] = ball.getX()-pos[0];
 			tempArr[1] = ball.getY()-pos[1];
 			angle = (float)Math.atan2(tempArr[1], tempArr[0]);
 			angleTarget = (float)Math.atan2(tempArr[1], tempArr[0]);
 			NORMALKICK = EXTRAKICK;
 			mySoundSystem.quickPlay( true, slowMo?"TwoTouchLockOnSlow.ogg":"TwoTouchLockOn.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 		}
 		
 		ballPos[0] = ball.getX();
 		ballPos[1] = ball.getY();
 		prevBallPos[0] = ball.getPrevX();
 		prevBallPos[1] = ball.getPrevY();
 		
 		if(!ball.locked(playerNum)){
 			updatePos(delta);
 		}else if(power>0){
 			
 			angle = (float)Math.atan2(ball.getY()-pos[1], ball.getX()-pos[0]);
 //			System.out.println(angle/2/Math.PI*360);
 			if(mag(vel)!=0){
 				unit(vel);	
 				angleTarget = (float)Math.atan2(vel[1],vel[0]);
 			}
 
 //Joystick rotation based rotation
 			
 			prevAngleTarget = (float)Math.atan2(prevVel[1], prevVel[0]);
 		
 //			System.out.println(angle/2/Math.PI*360+"->  "+angleTarget/2/Math.PI*360);
 
 			if(angleDist(angleTarget, prevAngleTarget) > .1f){//If you've moved the stick a non negligible amount
 				tempf = (float)(Math.cos(angleTarget)*Math.sin(prevAngleTarget)-Math.cos(prevAngleTarget)*Math.sin(angleTarget));
 				tempf/= -Math.abs(tempf);
 				rotateDir = (int)tempf;
 			}
 			
 			tempf = delta/80f;//The step interval
 			if(angle != angleTarget){
 				if(angleDist(angle, angleTarget)>.1f){//If it's actual turning and not a microscopic slip of the finger
 					if(rotateDir > 0){
 						angle+=tempf;
 					}else if(rotateDir < 0){
 						angle-=tempf;
 					}
 				}else{
 					angle = angleTarget;
 				}
 			}
 				
 //End rotation code
 			
 			pos[0] = ballPos[0]-(float)Math.cos(angle)*(KICKRANGE/2+2);
 			pos[1] = ballPos[1]-(float)Math.sin(angle)*(KICKRANGE/2+2);
 			ball.setVel(new float[]{(float)Math.cos(angle), (float)Math.sin(angle)}, 0);
 			
 			if(pos[0]<xyLimit[0]+KICKRANGE/2f){
 				tempf = pos[0];
 				shiftX(xyLimit[0]+KICKRANGE/2f-tempf);
 				ball.shiftX(xyLimit[0]+KICKRANGE/2f-tempf);
 			}
 			if(pos[0]>xyLimit[1]-KICKRANGE/2f){
 				tempf = pos[0];
 				shiftX(xyLimit[1]-KICKRANGE/2f-tempf);
 				ball.shiftX(xyLimit[1]-KICKRANGE/2f-tempf);
 			}
 			if(pos[1]<xyLimit[2]+KICKRANGE/2f){
 				tempf = pos[1];
 				shiftY(xyLimit[2]+KICKRANGE/2f-tempf);
 				ball.shiftY(xyLimit[2]+KICKRANGE/2f-tempf);
 			}
 			if(pos[1]>xyLimit[3]-KICKRANGE/2f){
 				tempf = pos[1];
 				shiftY(xyLimit[3]-KICKRANGE/2f-tempf);
 				ball.shiftY(xyLimit[3]-KICKRANGE/2f-tempf);
 			}
 
 //			//If that rotation made you out of bounds, roll it back//No longer used: now you get shoved away from wall if you rotate into it
 //			if(pos[0]-KICKRANGE/2<xyLimit[0] || pos[0]+KICKRANGE/2>xyLimit[1] || pos[1]-KICKRANGE/2<xyLimit[2] || pos[1]+KICKRANGE/2>xyLimit[3]){
 //				angle = tempf;
 //				vel[0] = (float)Math.cos(angle);
 //				vel[1] = (float)Math.sin(angle);
 //			}
 			
 			//Keeps angle between -pi and pi for next round of calculations
 			if(angle>(float)Math.PI)
 				angle-=(float)Math.PI*2f;
 			if(angle<-(float)Math.PI)
 				angle+=(float)Math.PI*2f;
 			
 		}
 		
 		updateCounters(delta);
 		
 		if(ball.locked(playerNum)){
 			theta = angleTarget;
 		}else {
 			theta+= omega*delta/15f;
 			if(theta>(float)Math.PI*2f) theta-=(float)Math.PI*2f;
 		}
		System.out.println(isKicking());
 	}
 	
 	@Override
 	public void setLockCoolDown(boolean l) {
 		lockCoolDown = l;
 	}
 
 	@Override
 	public void activatePower(){
 		power = MAXPOWER;
 		velMag = VELMAG * 0.5f;
 		mySoundSystem.quickPlay( true, slowMo?"TwoTouchActivateSlow.ogg":"TwoTouchActivate.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 		if(!c.exists()){
 			curve[0] = 0;
 			curve[1] = 0;
 		}
 	}
 	
 	@Override
 	public void powerKeyReleased(){
 		power = 0;
 		velMag = VELMAG;
 		if(ball.locked(playerNum)){//teleport the ball in range for the flash kick
 			ball.setPos(pos[0]+(float)Math.cos(angle)*(KICKRANGE/2f-3f), pos[1]+(float)Math.sin(angle)*(KICKRANGE/2f-3f));
 			kickingCoolDown = 0;
 		}
 	}
 	
 	@Override
 	public void setPower(){
 	
 	}
 
 	//Used to add velocity component of player to kick
 	//In power, TwoTouch will not move the ball
 	@Override
 	public float[] getKick(){
 		if(power>0)
 			return new float[]{0,0};
 		return vel;
 	}
 	
 	@Override
 	public float[] getCurve() {
 		if(c.exists() || ball.locked(playerNum))
 			return curve;
 		return zeroes;
 	}
 	
 	@Override
 	public float kickStrength(){
 		if(NORMALKICK == EXTRAKICK){
 			if(mag(vel)>0)
 				return EXTRAKICK;
 			else
 				return DEFAULTKICK;
 		}else if(power>0){
 			return POWERKICK;
 		}if(mag(vel)==0){
 			return .5f;
 		}else{
 			return NORMALKICK;
 		}
 	}
 
 	@Override
 	public float curveStrength(){
 		return 3f;
 	}
 	
 	@Override
 	public boolean isKicking() {
 		if(power>0)
 			return false;
 		return kickingCoolDown == 0;
 	}
 	
 	//I just kicked (power or regular kick) the ball now what
 	@Override
 	public void setKicking(Ball b){
 		if(power>0 && !ball.locked(playerNum)){
 			//Do nothing
 		}else{
 //			b.setCanBeKicked(playerNum, false);
 			kickingCoolDown = KICKCOOLDOWN;
 			if(ball.locked(playerNum)){
 				ball.setLocked(playerNum, false);
 				NORMALKICK = DEFAULTKICK;
 				if(mag(vel)>0)
 					lastKickAlpha = 1f;
 			}
 		}
 	}
 
 	//Return if the kick should flash and make the power kick sound
 	@Override
 	public boolean flashKick(){
 		return ball.locked(playerNum) && mag(vel)>0;
 	}
 
 	@Override
 	public void setLastKick(float bx, float by, float px, float py, float lka){//ball pos, player pos, was it a power kick
 		lastKickBallPos[0] = bx;
 		lastKickBallPos[1] = by;
 		lastKickPos[0] = px;
 		lastKickPos[1] = py;
 		//TwoTouch doesn't have the last kick alpha
 	}
 
 	@Override
 	public float slowMoFactor(){
 		if(ball.locked(playerNum)){
 			return 6f;
 		}else{
 			return 1f;
 		}
 	}
 	
 }
