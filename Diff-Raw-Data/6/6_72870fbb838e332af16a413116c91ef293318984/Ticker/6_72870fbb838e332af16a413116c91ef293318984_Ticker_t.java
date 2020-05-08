 package de.philipphock.java.pechakucha;
 
 
 public class Ticker implements Runnable{
 
 	private boolean stopped=false;
 	
 	private Thread ownThread;
 	
 	private long lastSecond=0;
 	
 	private int every;
 	
 	private enum TickerState{
 		PLAY,PAUSE,STOP;
 	}
 	
 	private TickerState state; 
 	
 	private Presenter p;
 	
 	
 	public Ticker(Presenter p) {
 		this(p,1);
 	}
 	
 	public Ticker(Presenter p, int every) {
 		this.p=p;
 		this.every=every;
 		state=TickerState.PAUSE;
 		ownThread = new Thread(this);
 		ownThread.setDaemon(true);
 		ownThread.start();
 		
 		
 	}
 	
 	public void play(){
 		
 		state = TickerState.PLAY;
 		synchronized (ownThread) {
 			
 		
 			ownThread.notifyAll();
 		}
 		
 		
 	}
 	
 	public void pause(){
 		
 			state = TickerState.PAUSE;	
 	}		
 	
 	
 	public synchronized void stop(){
 		stopped=true;
 	}
 	
 	@Override
 	public void run() {
 		
 		synchronized (ownThread) {
 			
 		
 			while (!stopped){
 				
 				while (state==TickerState.PAUSE){
 					
 					try {
 						ownThread.wait();
 						
 					} catch (InterruptedException e1) {
 						e1.printStackTrace();
 					}				
 				}
 				
 				
				
 				if (System.currentTimeMillis() - lastSecond > 1000*every) {
 					//tick - a second
 					p.tick();
 					lastSecond=System.currentTimeMillis();
 				}
 			}
 		}
 	
 	}
 }
