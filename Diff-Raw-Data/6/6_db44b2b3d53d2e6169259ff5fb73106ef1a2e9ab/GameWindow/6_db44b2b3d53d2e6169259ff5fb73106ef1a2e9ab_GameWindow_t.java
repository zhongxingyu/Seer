 /* 
  * Copyright 2012 Bryan Wyatt
  * 
  * This file is part of BRGE.
  *  
  * BRGE is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * BRGE is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with BRGE.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.brwyatt.brge.graphics;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Toolkit;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.awt.image.BufferStrategy;
 import java.util.ArrayList;
 
 import javax.swing.JFrame;
 
 import net.brwyatt.brge.BRGE;
 
 
 public class GameWindow extends JFrame implements WindowListener, Runnable, KeyListener {
 	private static final long serialVersionUID = -506949053075590082L;
 	private boolean drawFPS=false;
 	private long lastFrame=0;
 	private int FPS=0;
 	private int sleeptime=10;
 	private ScreenObjects screenObjects;
 
 	private ArrayList<Integer> fpsHist;
 	private ArrayList<Integer> sleepHist;
 
 	public GameWindow(ScreenObjects so){
 		super(BRGE.getGameTitle());
 		
 		init(so,false);
 	}
 	public GameWindow(ScreenObjects so, boolean drawFPS){
 		super(BRGE.getGameTitle());
 		
 		init(so,drawFPS);
 	}
 	
 	@SuppressWarnings("static-access")
 	private void init(ScreenObjects so, boolean drawFPS){
 		screenObjects=so;
		this.drawFPS=drawFPS;
 		
 		addWindowListener(this);
 		addKeyListener(this);
 		setResizable(false);
 		//setUndecorated(true);
 		setSize(BRGE.getWidth(),BRGE.getHeight());
 		
 		fpsHist=new ArrayList<Integer>();
 		sleepHist=new ArrayList<Integer>();
 		
 		Dimension screen=Toolkit.getDefaultToolkit().getScreenSize();
 		setLocation((int)((screen.getWidth()/2)-(getWidth()/2)), (int)((screen.getHeight()/2)-(getHeight()/2)));
 		
 		setVisible(true);
 
 		try {
 			Thread.currentThread().sleep(10);
 		} catch (InterruptedException e) {
 		}
 		
 		createBufferStrategy(2);
 		
 		try {
 			Thread.currentThread().sleep(10);
 		} catch (InterruptedException e) {
 		}
 	}
 	
 	private void drawToFrame(){
 		BufferStrategy bf=getBufferStrategy();
 		Graphics g=null;
 		
 		try{
 			g=bf.getDrawGraphics(); //get buffer object
 			
 			g.setColor(Color.white);
 			g.fillRect(0, 0, getWidth(), getHeight()); //blank the image
 			
 			screenObjects.draw(g); //draw all objects
 			
 			g.setColor(Color.lightGray);
 			g.drawRect(0, 0, getWidth()-1, getHeight()-1);
 			
 			//calculate and lock framerates
 			long thisFrame=System.currentTimeMillis(); //get the current time
 			long r=(thisFrame-lastFrame);
 			if(r<=0){
 				r=1;
 			}
 			FPS=(int)(1000/r); //calculate FPS
 			if(drawFPS){	
 				g.setColor(Color.red);
				g.drawString("FPS:   "+FPS, 5, BRGE.getHeight()-20);
				g.drawString("Delay: "+sleeptime, 5, BRGE.getHeight()-8);
 			}
 			lastFrame=thisFrame; // set last frame time to this frame's time for the next time it is called
 			
 		}catch(Exception e){
 			e.printStackTrace();
 		}finally{
 			g.dispose(); //clean up
 		}
 		
 		bf.show(); //draw to screen
 		
 		Toolkit.getDefaultToolkit().sync(); //force redraw
 	}
 
 	@SuppressWarnings("static-access")
 	public void run() {
 		lastFrame=System.currentTimeMillis();
 		while(true){
 			drawToFrame();
 			
 			fpsHist.add(FPS);
 			sleepHist.add(sleeptime);
 			while(fpsHist.size()>240){
 				fpsHist.remove(0);
 				sleepHist.remove(0);
 			}
 			FPS=sleeptime=0;
 			for(int i=0;i<fpsHist.size();i++){
 				FPS+=fpsHist.get(i);
 				sleeptime+=sleepHist.get(i);
 			}
 			FPS/=fpsHist.size();//average fps over 60 frames
 			sleeptime/=sleepHist.size(); //average delay over 60 frames
 			
 			sleeptime+=(int)(16.66667-(1/(FPS/1000.0))); //lock framerate to 60/62FPS
 			if(sleeptime<0){
 				sleeptime=0;
 			}
 			try {
 				Thread.currentThread().sleep(sleeptime);
 			} catch (InterruptedException e) {
 			}
 		}
 	}
 	
 	public void toggleFPS(){
 		drawFPS=!drawFPS;
 	}
 	
 	//WindowListener Methods
 	public void windowActivated(WindowEvent e) {
 	}
 	public void windowClosed(WindowEvent e) {
 	}
 	public void windowClosing(WindowEvent e) {
 		System.exit(0);
 	}
 	public void windowDeactivated(WindowEvent e) {
 	}
 	public void windowDeiconified(WindowEvent e) {
 	}
 	public void windowIconified(WindowEvent e) {
 	}
 	public void windowOpened(WindowEvent e) {
 	}
 	
 	//KeyListener methods
 	public void keyPressed(KeyEvent e) {
 		BRGE.keyPressed(e.getKeyCode());
 	}
 	public void keyReleased(KeyEvent e) {
 		BRGE.keyReleased(e.getKeyCode());
 	}
 	public void keyTyped(KeyEvent e) {
 		BRGE.keyTyped(e.getKeyCode());
 	}
 }
