 /* lander
  * Copyright 2012 Axel Isaksson
  * 
  * Main class setting up simulation
  */
 
 import java.lang.Throwable;
 import java.util.Random;
 import java.awt.Color;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 class ShipAdapter implements AIAdapter {
 	//AIAdapter interface methods
 	//This data comes from suggested table from the exercise specifications
 	public int calcHeightIndex(Ship ship, Background background) {
 		int h=background.getGroundY()-ship.getY()-ship.getH();
 		if(h>100) return 7;
 		if(h>50) return 6;
 		if(h>25) return 5;
 		return h/5;
 	}
 	
 	public int calcSpeedIndex(Ship ship) {
 		int s=ship.getSpeed();
 		if(s<-30) return 0;
 		if(s<-20) return 1;
 		if(s<-10) return 2;
 		if(s<-1) return 3;
 		if(s<1) return 4;
 		if(s<10) return 5;
 		if(s<20) return 6;
 		return 7;
 	}
 	
 	public int calcFuelIndex(Ship ship) {
 		int f=ship.getFuel();
 		return f>70?7:f/10;
 	}
 }
 
 public class Lander implements ActionListener {
 	Timer timer;
 	Random rnd=new Random(System.currentTimeMillis());
 	LanderPanel panel;
 	Background background;
 	Ship ship;
 	ShipAI ai;
 	Particle explosion[];
 	
 	//Semaphore for pausing logic during a finished run to display explosion, etc..
 	//Certainly not the best way of doing it but it works well enough
 	int sem=0;
 	
 	int count=0, successCount=0, failFuelCount=0;
 	
 	public Lander(int iterations) {
 		background=new Background(640, 480);
 		explosion=new Particle[100];
 		panel=new LanderPanel(background, explosion);
 		ai=new ShipAI(8, ship, background, new ShipAdapter());
 		respawnShip();
 		
 		//Run initial iterations if specified
 		while(count<iterations)
 			actionPerformed(null);
 		
 		//Main loop timer at 16 frames
 		timer=new Timer(1000/16, this);
 		timer.setRepeats(true);
 		
 		//Set up window
 		JFrame frame=new JFrame("lander");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setSize(640, 480);
 		frame.setResizable(false);
 		frame.getContentPane().add(panel);
 		frame.pack();
 		frame.setVisible(true);
 		timer.start();
 	}
 	
 	public void actionPerformed(ActionEvent e) {
 		//Main loop, updates panel view, runs engine
 		if(e!=null)
 			panel.repaint();
 			
 		//If we have no ship, make an explosion and restart simulation
 		if(ship==null) {
 			if(e==null) {
 				//Unless we run without graphics, in which case we just restart the simulation
 				respawnShip();
 				return;
 			}
 			for(int i=0; i<explosion.length; i++)
 				explosion[i].move();
 			if(sem++==10)
 				respawnShip();
 			return;
 		}
 		
 		if(ai.runEngine())
 			ship.motorOn();
 		else
 			ship.motorOff();
 		ship.move();
 		panel.updateInstuments();
 		
 		//Check if simulation should end
 		if(ship.getY()+ship.getH()>=background.getGroundY()||ship.getFuel()<=0) {
 			boolean success;
 			//Check if out of fuel or crashing with high speed
 			if(ship.getFuel()<=0&&ship.getY()+ship.getH()<background.getGroundY()) {
 				failFuelCount++;
 				success=false;
 				panel.setShip(null);
 			} else if(ship.getSpeed()>10) {
 				success=false;
 				panel.setShip(null);
			} else
 				success=true;
 			
 			//Make AI learn from this run
 			ai.learn(success);
 			//Update statistics
 			count++;
 			if(success)
 				successCount++;
 			panel.updateStatistics(count, successCount, failFuelCount);
 			
 			//Create particles with random speed for explosion
 			for(int i=0; i<explosion.length; i++) {
 				Color c=new Color(rnd.nextInt(64)+128, rnd.nextInt(256), 0);
 				explosion[i]=new Particle(ship.getX()+ship.getW()/2, ship.getY()+ship.getH()/2, rnd.nextDouble()*16-8, rnd.nextDouble()*16-8, c);
 			}
 			
 			ship.motorOff();
 			ship=null;
 		}
 	}
 	
 	public void respawnShip() {
 		//Restart simulation with a new ship
 		sem=0;
 		ship=new Ship(640/2-32/2, background.getGroundY()-64-100-rnd.nextInt(successCount>count/2?300:100), 32, 64, 75+rnd.nextInt(50), rnd.nextInt(10)-5);
 		panel.setShip(ship);
 		ai.setShip(ship);
 	}
 	
 	public static void main(String args[]) {
 		int iterations;
 		
 		//Parse command line arguments
 		try {
 			if(args[0].equals("-h")||args[0].equals("--help")) {
 				System.out.println("lander - moon landing simulator with AI");
 				System.out.println("Axel Isaksson 2012");
 				System.out.println("Usage:");
 				System.out.println("	lander		- run lander");
 				System.out.println("	lander	[n]	- run lander with n initial iterations");
 				return;
 			}
 			iterations=new Integer(args[0].replace("-", ""));
 		} catch(Throwable e) {
 			iterations=0;
 		}
 		
 		new Lander(iterations);
 	}
 }
 
