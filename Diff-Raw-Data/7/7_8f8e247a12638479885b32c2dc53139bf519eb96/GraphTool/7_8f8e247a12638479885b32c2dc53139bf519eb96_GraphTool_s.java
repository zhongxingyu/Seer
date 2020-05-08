 /*
   Copyright (C) 2001 Concord Consortium
 
   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation; either version 2
   of the License, or (at your option) any later version.
 
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
 
   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
 package org.concord.LabBook;
 
 import waba.ui.*;
 import waba.fx.*;
 import waba.io.*;
 import waba.sys.*;
 import waba.util.*;
 import graph.*;
 import extra.ui.*;
 import org.concord.waba.extra.probware.*;
 import org.concord.waba.extra.probware.probs.*;
 import org.concord.waba.extra.event.*;
 
 class TimeBin implements DecoratedValue
 {
     float time = 0f;
 
     public String getLabel(){return "Time";}
 
     public float getValue(){return time;}
     public void setValue(float t){time = t;}
 
     public Color getColor(){return null;}
 
     public float getTime(){return 0f;}
 }
 
 public class GraphTool extends Container
     implements DataListener
 {
     ProbManager pm = null;
     AnnotView lg;
     ToggleButton collectButton;
     TimeBin timeBin = new TimeBin();
 
     int i;
     DigitalDisplay dd;
     //    LabelBuf curVal, curTime;
     Button doneB;
     TextLine convertor = new TextLine("0");
 
     //    Transform startingTrans;
  
     //    int curProbe = 3;
 
     Label bytesRead = new Label("-2");
     Label line2 = new Label("-1");
 
     MainWindow mw = null;
     String units = "";
 
     CCProb curProbe = null;
 
     Bin curBin = null;
     Vector bins = new Vector();
 
     boolean slowUpdate = false;
 
     //    int [] [] pTimes = new int [1000][];
     int [] [] pTimes = null;
 
     LObjDataControl dc = null;
 	Label title1Label, title2Label;
     String title1, title2;
 
     public GraphTool(String t1, String t2, AnnotView av, LObjDataControl dc, 
 					 DigitalDisplay digDisp, int w, int h) 
     {
 		dd = digDisp;
 		dd.addBin(timeBin);
 
 		convertor.maxDigits = 2;
 		this.dc = dc;
 		title1 = t1;
 		title2 = t2;
 
 
 		pm = ProbManager.getProbManager(dc.interfaceId);
 
 		int xPos = 0;
 		int buttonWidth;
        
 
 		this.units = units;
 		lg = av;
 	
 		//	curProbe = probeId;
 
 		convertor.maxDigits = 2;
 		setRect(0,0,w, h);
 
 		String [] names = new String [2];
 		names [0] = "Collect";
 		collectButton = new ToggleButton("Collect", false);
 
 		buttonWidth = h;
 		if(h < 30) buttonWidth = 35;
 
         collectButton.setRect(xPos,0,buttonWidth,h);
 		xPos += buttonWidth+2;
 		add(collectButton);
 
 		title1Label = new Label(title1);
 		title1Label.setRect(xPos, 0, w-xPos-27, h/2);
 		add(title1Label);
 
 		title2Label = new Label(title2);
 		title2Label.setRect(xPos, h/2, w-xPos, h/2);
 		add(title2Label);
 
 		//curVal = new LabelBuf("");
 		//curVal.setRect(xPos,0,w*50/160,h/2);	
 		//curVal.setFont(new Font("Helvetica", 
 		//			Font.BOLD, h*12/20 - (h-20)*8/20));
 		//add(curVal);
 
 		//curTime = new LabelBuf("0.0s");
 		//curTime.setRect(xPos,h/2,w*50/160,h/2);
 		//curTime.setFont(new Font("Helvetica", 
 		//			 Font.BOLD, h*12/20 - (h-20)*8/20));
 		//add(curTime);
 
 		xPos += w*50/160;
 
 		doneB = new Button("Done");
 		doneB.setRect(w-27, 0, 27, h/2);
 		add(doneB);
 
 		curBin = lg.getBin();
 
 		curProbe = dc.getProbe();
 
 		pm.registerProb(curProbe);
 		pm.addDataListenerToProb(curProbe.getName(),this);
 
 		bytesRead.setRect(105, 0, 30, 10);
 		//add(bytesRead);
 	
 		line2.setRect(0, 10, 160, 10);
 		//add(line2);
 
 		mw = MainWindow.getMainWindow();
     }
 
 	public void setTitle(String t1, String t2)
 	{
 		title1Label.setText(t1);
 		title2Label.setText(t2);
 	}
 
     float val= 0f;
     float time = 0f;
 
     int numVals = 0;
 
     public void dataReceived(DataEvent dataEvent)
     {
 		if(dataEvent.type == DataEvent.DATA_READY_TO_START){
 			if(pm.getMode() == CCInterfaceManager.A2D_10_MODE){
 				slowUpdate = true;
 			} else {
 				slowUpdate = false;
 			}
 			numVals = 0;
 			curPtime = 0;
 			return;
 		}
 
 		if(slowUpdate){
 			if(dataEvent.type == DataEvent.DATA_RECEIVED){
 				if(lg.active){
 					int startPTime = Vm.getTimeStamp();
 					if(!curBin.dataReceived(dataEvent)){
 						stop();
 						lg.curView.draw();
 						return;		
 					}
 					dataEvent.pTimes[dataEvent.numPTimes++] = Vm.getTimeStamp() - startPTime;		   
 					savePTimes(dataEvent);
 				}	
 				numVals += dataEvent.numbSamples;
 
 				val = dataEvent.data[dataEvent.dataOffset];
 			} else {
 				int startPTime = Vm.getTimeStamp();
 				if(pTimes != null){
 					pTimes [curPtime] = new int [6];
 					pTimes[curPtime][0] = 1;
 					pTimes[curPtime][1] = startPTime;
 					pTimes[curPtime][2] = numVals;
 				}
 
 				//		if(lg.active){
 				lg.update();
 
 				int newTime = Vm.getTimeStamp();
 				if(pTimes != null){
 					pTimes[curPtime][3] = (newTime - startPTime);		
 				}
 
 				String output1, output2;
 				output1 = Convert.toString(val);
 				output2 = Convert.toString(time);
 
 				startPTime = Vm.getTimeStamp();
 				if(pTimes != null){
 					pTimes[curPtime][4] = (startPTime - newTime);
 				}
 
 				timeBin.setValue(time);
 				dd.update();
 				//		curVal.setText(output1);
 				// curTime.setText(output2);
 
 				if(pTimes != null){
 					pTimes[curPtime][5] = (Vm.getTimeStamp() - startPTime);
 				}
 		
 				numVals = 0;
 				curPtime++;
 			}
 		} else {
 			//	    System.out.println("Data: " + dataEvent.data[0]);
 			if(lg.active){
 				if(!curBin.dataReceived(dataEvent)){
 					stop();
 					lg.curView.draw();
 					return;		
 				}
 				lg.update();
 			}
 
 			timeBin.setValue(dataEvent.getTime());
 			dd.update();
 			// curVal.setText(dataEvent.data[dataEvent.dataOffset] + "");
 			// curTime.setText( + "s");
 		}
     }
     
     public String pTimeText = ""; 
     int curPtime = 0;
 
     public void savePTimes(DataEvent dEvent)
     {
 		if(pTimes != null){
 			pTimes [curPtime] = new int [dEvent.numPTimes + 1];
 			pTimes [curPtime][0] = 0;
 			for(int i=0; i< dEvent.numPTimes; i++){
 				pTimes [curPtime][i+1] = dEvent.pTimes[i];
 			}
 			curPtime++;
 		}
     }
 
     public void setPos(int x, int y)
     {
 		setRect(x,y,width,height);
     }
 
     float oldVal;
 
     public void onExit()
     {
 		pm.stop();
 	
 		if(curProbe != null){
 			pm.unRegisterProb(curProbe);
 		}
 	
 		lg.free();
 	
 		// curVal.free();
 		// curTime.free();
     }
 
     void stop()
     {
 
 		if(lg.active){
 			lg.active = false;
 			curBin = lg.pause();
 		}
 	
 		pm.stop();
 
 		collectButton.setSelected(false);    
 		repaint();
     }
     
     public void onEvent(Event e)
     {
 		float newVal;
 
 		if(e.type == ControlEvent.PRESSED){
 			Control target = (Control)e.target;
 			int index;
 			if(target == collectButton && collectButton.isSelected()){
 				// start
 				if(bins.getCount() == 0){
 					lg.active = true;
 					bins.add(curBin);
 					curBin.time = new Time();
 					curBin.description = dc.getObj(0).name;
 				}
 				slowUpdate = false;
 
 				bytesRead.setText((byte)'C' + "");
 				pTimeText = "";
 				pm.start();
 
 			} else if(target == collectButton && ! collectButton.isSelected()){
 				stop();
 			} else if(target == doneB){
 				// let our parent know we've been done'd
 				postEvent(new ControlEvent(2000, this));
 			}
 		} else if(e.type == 1003){
 			//	    System.out.println("Got 1003");
 			if(lg.lgView.selAnnot != null){
 				timeBin.setValue(lg.lgView.selAnnot.time);
 				// need to make sure lg.lgView.selAnnot has been added to dd
 				dd.update();
 
 				// curVal.setText(convertor.fToString(lg.lgView.selAnnot.value) + units);
 				// curTime.setText(convertor.fToString() + "s");
 			} else {
 				// hack
 				// Need to remove annot bin from dd
 				// set value of timeBin to last time
 				dd.update();
 		
 				// curVal.setText("");
 				// curTime.setText("");
 			}		
 		}  
     }
 
     public void clear()
     {
 		// ask for confirmation
 		stop();
 		lg.reset();
 	
 		// Clear curBin and set time to 0
 		timeBin.setValue(0f);
 		dd.update();
 	
 		// curVal.setText("");
 		// curTime.setText("0.0s");
 		bins = new Vector();	
     }
     
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
