 package com.estudio.cheke.game.gstb.objects;
 /*
  * Created by Estudio Cheke, creative purpose.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 import com.estudio.cheke.game.gstb.Cache;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Picture;
 import android.graphics.Rect;
 
 public class Pajaro extends Object{
 	private int currentFrame=0; //current frame being played
 	private static int waitDelay=15; // delay before the next frame
 	private boolean down=false;
 	private int downI=0;
 	static Picture[] pajaro=new Picture[3];
 	Rect dst=new Rect();
 	public Pajaro(){
 		if(pajaro[0]==null){
 			pajaro=new Picture[3];
 		}
 		makeBitmaps();
 	}
 	public void makeBitmaps(){
 		width=(int) (12*Cache.wpor);
 		height=(int) (9*Cache.hpor);
 		int CentroX=(int) (6*Cache.wpor);
 		int CentroY=(int) (6*Cache.hpor);
 		int[] alaIX={(int) Cache.wpor,(int) Cache.wpor,(int) (4*Cache.wpor)};
 		int[] alaDX={(int) (10*Cache.wpor),(int) (10*Cache.wpor),(int) (8*Cache.wpor)};
 		int[] alaIY={(int) (4*Cache.wpor),(int) Cache.wpor,(int) Cache.wpor};
 		int[] alaDY={(int) (4*Cache.wpor),(int) Cache.wpor,(int) Cache.wpor};
 		Paint paint=new Paint();
 		paint.setFlags(1);
 		if(pajaro[0]==null){
 			for(int a=0;a<pajaro.length;a++){
 				//Bitmap pajaroB = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
				Canvas canvas = new Canvas();
				canvas=pajaro[a].beginRecording(width, height);
 				paint.setColor(Color.WHITE);
 				paint.setStrokeWidth(Cache.wpor);
 				canvas.drawLine(alaIX[a], alaIY[a], CentroX, CentroY, paint);
 				canvas.drawLine(CentroX, CentroY, alaDX[a], alaDY[a], paint);
 				canvas.drawCircle(CentroX, CentroY, Cache.wpor*2, paint);
 				paint.setColor(Color.BLACK);
 				paint.setStrokeWidth(Cache.wpor/2);
 				canvas.drawLine(alaIX[a], alaIY[a], CentroX, CentroY, paint);
 				canvas.drawLine(CentroX, CentroY, alaDX[a], alaDY[a], paint);
 				canvas.drawCircle(CentroX, CentroY, Cache.wpor, paint);
 				//pajaro[a]=pajaroB;
 				pajaro[a].endRecording();
 			}
 		}
 	}
 	public void draw(Canvas canvas) {
 		//canvas.drawBitmap(pajaro[currentFrame], x, y + height+downI+Cache.bagUp, null);
 		dst.set(x, y + height+Cache.bagUp+downI, x+width, y + height+Cache.bagUp+height+downI);
 		canvas.drawPicture(pajaro[currentFrame], dst);
 		update();
 	}
 	public void move(float timeDeltaSeconds){ 
 		moveBasic(timeDeltaSeconds);
 		if(downI<100&&!down){
 			downI++;
 		}else if(!down){
 			down=true;
 		}else if(down&&downI>0){
 			downI--;
 		}else if(downI<=0){
 			down=false;
 		}
 		if(x<-150){
 			x=(int) (canvaswidth*((10*Math.random())+1));
 			if(Cache.LevelNum!=2){
 				y=(int) (y>Cache.hpor*15-Cache.bagUp||y<Cache.hpor*6-Cache.bagUp?Cache.hpor*6-Cache.bagUp:y);
 				y=(int) (y+((Cache.hpor*9)*Math.random()));
 			}else if(Cache.LevelNum==2){
 				y=(int) (y>Cache.hpor*66-Cache.bagUp||y<Cache.hpor*6-Cache.bagUp?Cache.hpor*6-Cache.bagUp:y);
 				y=(int) (y+((Cache.hpor*60)*Math.random()));
 			}
 		}
 	}
 	public boolean contacto(int right,int yB, int hB){
 		boolean touch=false;
 		if(right>=x&&right<=x+width){
 			int yhB=yB+hB;
 			int yy=y+height+downI+Cache.bagUp;
 			int yh=yy+height;
 			//        arriba              debajo               dentro
 			if( (yhB>=yy&&yhB<=yh) || (yB>=yy&&yB<=yh) || (yB>=yy&&yhB<=yh) ){
 				touch=true;
 			}
 		}
 		return touch;
 	}
 	public void update(){
 		if(waitDelay==0){//if done waiting
 			//set current frame back to the first because looping is possible
 			if(currentFrame == pajaro.length-1){
 				currentFrame=0;
 			}else{	
 				currentFrame++; //go to next frame
 				waitDelay = 15; //set delaytime for the next frame
 			}
 		}else{
 			waitDelay--; //wait for delay to expire
 		}
 	}
 }
