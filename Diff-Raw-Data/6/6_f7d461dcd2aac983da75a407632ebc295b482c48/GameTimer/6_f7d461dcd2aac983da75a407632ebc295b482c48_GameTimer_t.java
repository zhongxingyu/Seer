 package twelve.team;
 
 import java.util.List;
 import java.util.ArrayList;
 
 interface GameTimerListener {
 	public void TimesUp();
 	public void secondDecrease(int timeLeft);
 }
 
 public class GameTimer extends Thread{
 	
 	public GameTimer(long millisec){
 		valid = true;
 		seconds = millisec;
 	}
 	
 	public GameTimer(long millisec, boolean repeatable){
 		valid = true;
 		seconds = millisec;
 		repeat = repeatable;
 	}
 	
 	public void run(){
 		while(!interrupted() && valid){
 			valid = true;
 			t = System.currentTimeMillis() + seconds;
 			secLeft = (int) (seconds/1000);
 			while(t > System.currentTimeMillis()){
 				try {
 					if(secLeft != (int)(t - System.currentTimeMillis()) / 1000){
 						secLeft = (int)(t - System.currentTimeMillis()) / 1000;
 						for(GameTimerListener listener : listeners){
 							listener.secondDecrease(secLeft);
 						}
 					}
 					sleep(50);
 				} catch (InterruptedException e) {
 					
 				}
 			}
 			for(int i=0;i<listeners.size();i++){
 				listeners.get(i).TimesUp();
 			}
 			if(!repeat){
 				valid = false;
 			}
 		}
 	}
 	
 	public int timeLeft(){
 		if(!valid){
 			return 0;
 		}
 		return (int) ((t - System.currentTimeMillis()) / 1000);
 	}
 	
 	public void reset() {
 		t = System.currentTimeMillis() + seconds;
		if(!valid)
			this.run();
 		valid = true;
		
 	}
 	
 	public void setActionListener(GameTimerListener al){
 		listeners.add(al);
 	}
 	
 	boolean valid;
 	boolean repeat;
 	long seconds;
 	long t;
 	int secLeft;
 	List<GameTimerListener> listeners = new ArrayList<GameTimerListener>();
 }
