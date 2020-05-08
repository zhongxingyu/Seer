 package states;
 
 import java.util.Random;
 
 import essentials.Option;
 
 public class Secede {
 	private State[] state = new State[48];
 	private Random rand = new Random();
 	public Secede(){
 		for(int i = 0; i < 48; i++)
 			state[i] = StateHelper.getState(i);
 	}
 	public State getLeavingState(){
		int normalFactor = Option.getRawOption(0) != null ? Integer.parseInt(Option.getRawOption(1)) : 1;
 		int[] timesComeUp = new int[48];
 		for(int i = 0; i < normalFactor; i++)
 			timesComeUp[getOneLeavingState()]++;
		int maxTimes = Integer.MIN_VALUE;
		int maxTimesIndex = timesComeUp.length;
 		for(int i = 0; i < timesComeUp.length; i++)
 			if(timesComeUp[i] > maxTimes){
 				maxTimes = timesComeUp[i];
 				maxTimesIndex = i;
 			}
 		return state[maxTimesIndex];
 	}
 	private int getOneLeavingState(){
 		boolean[] leave = new boolean[48];
 		boolean done = false;
 		int index = 48;
 		while(!done){
 			for(int i = 0; i < 48; i++)
 				if(rand.nextInt(state[i].getSecedeProbability()) == 0)
 					leave[i] = true;
 			for(int i = 0; i < 48; i++)
 				if(leave[i])
 					done = true;
 		}
 		for(int i = 0; i < 48; i++)
 			if(leave[i]){
 				if(index < 48){
 					if(state[i].getSecedeProbability() > state[index].getSecedeProbability())
 						index = i;
 				}
 				else
 					index = i;
 			}
 		return index;
 	}
 }
