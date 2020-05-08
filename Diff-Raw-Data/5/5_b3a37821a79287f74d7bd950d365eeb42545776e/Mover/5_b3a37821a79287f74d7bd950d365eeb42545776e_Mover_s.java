 /*
  * Copyright (C) 2009 The Android Open Source Project
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
 package com.estudio.cheke.game.gstb;
 
 import com.estudio.cheke.game.gstb.objects.Buildings;
 import com.estudio.cheke.game.gstb.objects.CanvasSprite;
 import com.estudio.cheke.game.gstb.objects.Horizonte;
 import com.estudio.cheke.game.gstb.objects.Nube;
 import com.estudio.cheke.game.gstb.objects.Pajaro;
 import com.estudio.cheke.game.gstb.objects.Tree;
 import com.estudio.cheke.game.gstb.objects.Velocity;
 
 import android.os.SystemClock;
 
 public class Mover extends Cache implements Runnable {
 	private long mLastTime;
 	private long mtimer;
 	static int TIME_FOR_OBJECT = 1000;
 	public void run() {
 		final long timer =SystemClock.uptimeMillis();
 		final long timeDelta = timer - mLastTime;
 		final float timeDeltaSeconds = mtimer > 0.0f ? timeDelta / 1000.0f : 0.0f;
 		if(mtimer==0){
 			mtimer = timer;
 		}
 		final long timerDelta = timer - mtimer;
 		mLastTime = timer;
 		if (timerDelta > TIME_FOR_OBJECT ){
 			countSec++;
 			mtimer = timer;
 		}
 
 		if (!GstbActivity.pause) {
 			if(nubes!=null&&mountain!=null){
 				int efimero=nubes.length;
 				for(int n=0;n<efimero;n++){
 					nubes[n].move(timeDeltaSeconds);
 				}
 				efimero=pajaro.length;
 				for(int p=0;p<efimero;p++){
 					if(pajaro[p].contacto(canvasArray.x+canvasArray.width, canvasArray.y,canvasArray.height)){
 						nivelPor=0;
 						leveldoned=0;
 					}
 					pajaro[p].move(timeDeltaSeconds);
 				}
 				leveldoned += (125+(Velocity.Speed*15)) * timeDeltaSeconds;
 				barraNivelX=(int) ((leveldoned*width)/10000);
 				nivelPor=leveldoned/100;//*100/10000
 				switch(LevelNum){
 				case 1:
 					moveSun();
 					efimero=mountain.length;
 					for(int m=0;m<efimero;m++){
 						mountain[m].move(timeDeltaSeconds);
 					}
 					efimero=arboles.length;
 					for(int a=0;a<efimero;a++){
 						arboles[a].move(timeDeltaSeconds);
 						if(arboles[a].contacto(canvasArray.x+canvasArray.width, canvasArray.y+canvasArray.height)){
 							nivelPor=0;
 							leveldoned=0;
 						}
 					}
 					if(nivelPor>90){
 						bagUp=moveInt(bagUp,timeDeltaSeconds,true);
 					}
 					break;
 				case 2:
 					Sky();
 					break;
 				case 3:
 					Sea();
 					mar.move(timeDeltaSeconds);
 					if(mar.contacto(canvasArray.x+canvasArray.width, canvasArray.y,canvasArray.height)){
 						nivelPor=0;
 						leveldoned=0;
 					}
 					if(bagUp>0){
 						bagUp=moveInt(bagUp,timeDeltaSeconds,false);
 					}
 					if(nivelPor>70){
 						if(SueloUp>0){
 							SueloUp=moveInt(SueloUp,timeDeltaSeconds,false);
 						}
 						if(nivelPor>95){
 							marUp=moveInt(marUp,timeDeltaSeconds,true);
 						}
 					}
 					break;
 				case 4:
 					City();
 					efimero=arboles.length;
 					for(int a=0;a<efimero;a++){
 						arboles[a].move(timeDeltaSeconds);
 						if(arboles[a].contacto(canvasArray.x+canvasArray.width, canvasArray.y+canvasArray.height)){
 							nivelPor=0;
 							leveldoned=0;
 						}
 					}
 					efimero=builds.length;
 					for(int b=0;b<efimero;b++){
 						builds[b].move(timeDeltaSeconds);
 					}
 					break;
 				}
 				if(!menuFin){
 					canvasArray.gravity(timeDeltaSeconds);
 					canvasArray.move(timeDeltaSeconds);
 					canvasArray.moveX(timeDeltaSeconds);
 				}
 			}
 		}else{//Pause
			if(!loadImages){
 				mtimer=0;
 				LoadBitmaps();
 				loadImages=true;
			}else{
 				int efimero=nubes.length;
 				for(int n=0;n<efimero;n++){
 					nubes[n].move(timeDeltaSeconds);
 				}
 				efimero=mountain.length;
 				for(int m=0;m<efimero;m++){
 					mountain[m].move(timeDeltaSeconds);
 				}
 				efimero=arboles.length;
 				for(int a=0;a<efimero;a++){
 					arboles[a].move(timeDeltaSeconds);
 				}
 			}
 		}
 	}
 	public void LoadBitmaps(){
 		SVG svg = SVGParser.getSVGFromResource(resource, R.raw.bolsa1);
 		mPictures[0]=svg.getPicture();
 		svg = SVGParser.getSVGFromResource(resource, R.raw.bolsa2);
 		mPictures[1]=svg.getPicture();
 		svg = SVGParser.getSVGFromResource(resource, R.raw.bolsa3);
 		mPictures[2]=svg.getPicture();
 		svg = SVGParser.getSVGFromResource(resource, R.raw.bolsa4);
 		mPictures[3]=svg.getPicture();
 		canvasArray=new CanvasSprite();
 		canvasArray.setSize(svg.getLimits());
 		int efimero=mountain.length;
 		for(int m=0;m<efimero;m++){
 			mountain[m]=new Horizonte(m);
 		}
 		svg = SVGParser.getSVGFromResource(resource, R.raw.roque_nublo);
 		mountains[0]=svg.getPicture();
 		mountain[0].setSize(svg.getLimits());
 		svg = SVGParser.getSVGFromResource(resource, R.raw.timanfaya);
 		mountains[1]=svg.getPicture();
 		svg = SVGParser.getSVGFromResource(resource, R.raw.timanfaya,0xFF111111,0xFF222222);
 		mountains[2]=svg.getPicture();
 		for(int m=1;m<efimero;m++){
 			mountain[m].setSize(svg.getLimits());
 		}
 		svg = SVGParser.getSVGFromResource(resource, R.raw.mar);
 		olas[0]=svg.getPicture();
 		mar.setSize(svg.getLimits(),0);
 		svg = SVGParser.getSVGFromResource(resource, R.raw.ola);
 		olas[1]=svg.getPicture();
 		mar.setSize(svg.getLimits(),1);
 		efimero=nubes.length;
 		for(int n=0;n<efimero;n++){
 			if(nubes[n]==null){
 				nubes[n]=new Nube(n);
 			}
 		}
 		efimero=arboles.length;
 		for(int a=0;a<efimero;a++){
 			if(arboles[a]==null){
 				arboles[a]=new Tree();
 			}
 		}
 		efimero=pajaro.length;
 		for(int p=0;p<efimero;p++){
 			if(pajaro[p]==null){
 				pajaro[p]=new Pajaro();
 			}
 		}
 		efimero=builds.length;
 		for(int b=0;b<efimero;b++){
 			if(builds[b]==null){
 				builds[b]=new Buildings();
 			}
 		}
 		svg = SVGParser.getSVGFromResource(resource, R.raw.move);
 		control.setPicture(svg.getPicture());
 		svg = SVGParser.getSVGFromResource(resource, R.raw.play);
 		menu[0]=svg.getPicture();
 		svg = SVGParser.getSVGFromResource(resource, R.raw.exit);
 		menu[1]=svg.getPicture();
 		menuX=(int) svg.getLimits().right;
 		menuY=(int) svg.getLimits().bottom;
 		float multiplier=menuX/(70*wpor);
 		menuX=(int) (70*wpor);
 		menuY=(int) (menuY/multiplier);
 		Cache.loadImages=true;
 	}
 }
