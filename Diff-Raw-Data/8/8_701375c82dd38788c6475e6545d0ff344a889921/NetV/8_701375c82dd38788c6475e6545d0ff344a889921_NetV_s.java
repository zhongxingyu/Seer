 package org.Giraffe;
 import android.content.Context;
 
 //ADDED BY CROLL
 public class NetV extends Enemy
 {
 	public NetV(Context context, long time, float width, float height) {
 		super(context, time, width, height);
 		cancollide=true;
 		
 		x1=modelToViewX(110, width);
 		x2=modelToViewX(160, width);
 		y1=modelToViewY(70, height);
 		y2=modelToViewY(118, height);
 		
		speed=modelToViewX(-7, width)*(width/800f);
 		
 		this.hitBox.add(new HitBox("netv", x1, y1, x2, y2,true));
 		this.image=context.getResources().getDrawable(
                 R.drawable.netv);
 	}	
 	public void move() {
 		y1=moveDown(y1, (int)speed, 5);
 		y2=moveDown(y2, (int)speed, 5);
 		this.hitBox.get(0).changePosition(x1, y1, x2, y2);
 	}
 
 	@Override
 	public void collided(HitBox thisHitBox, HitBox otherHitBox) {
 		if(otherHitBox.toString().equals("killbox")){
 			image=context.getResources().getDrawable(R.drawable.kapow2);
 			SoundManager.playSound(4);
 			
 			this.cancollide=false;
 			collidedWithGiraffe=true;
 			delayOfTime=System.currentTimeMillis()+0;
 			delayObstacleImage(delayOfTime);
 			
 		}else if(otherHitBox.toString().equals("head")||(otherHitBox.toString().equals("body"))){
 			image=context.getResources().getDrawable(R.drawable.kapow);
 			SoundManager.playSound(3);
 			this.cancollide=false;
 			collidedWithGiraffe=true;
 			delayOfTime=System.currentTimeMillis()+0;
 			delayObstacleImage(delayOfTime);
 		}
 		
 	}
 	
 	public boolean canCollide() {
 		if(collidedWithGiraffe){
 			delayObstacleImage(delayOfTime);
 		}
 		return cancollide;
 	}
 	public String toString(){
 		return "netv";
 	}
 
 
 }
