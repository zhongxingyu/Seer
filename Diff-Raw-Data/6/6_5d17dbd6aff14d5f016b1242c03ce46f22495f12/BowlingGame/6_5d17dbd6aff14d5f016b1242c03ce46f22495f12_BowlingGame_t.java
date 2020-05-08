 package bowling;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 public class BowlingGame {
 
 	private ArrayList<Integer> rolls;
 
 	public BowlingGame() {
 		this.rolls = new ArrayList<Integer>();
 	}
 	
 	public int getScore() {
 		return this.sumAll(this.expandFrames());
 	}
 
 	public void roll(int i) {
 		this.rolls.add(i);
 	}
 
 	private Iterable<Frame> expandFrames() {
 		
 		Collection<Frame> frames = new ArrayList<Frame>();
 		
 		int i = 0;
 		
 		while(frames.size() < 10) {
 			
 			Frame frame = new Frame();
 			
 			frame.first = this.rolls.get(i);
 			frame.second = this.rolls.get(i+1);
 			
			if(frame.isStrike()) {
 				frame.third = this.rolls.get(i+2);
			} else if(frame.isSpare()) {
				frame.third = this.rolls.get(i+2);
				i++;
 			} else {
 				i++;
 			}
 			
 			i++;
 			
 			frames.add(frame);
 		}
 		
 		return frames;
 	}
 
 	private int sumAll(Iterable<Frame> frames) {
 		int result = 0;
 		
 		for(Frame f : frames) {
 			result += f.getScore();
 		}
 		return result;
 	}
 	
 	private static class Frame {
 		public int third = 0;
 		public int first = 0 ;
 		public int second = 0;
 
 		public boolean isStrike() {
 			return first == 10;
 		}
 
 		public boolean isSpare() {
 			return first + second == 10 ;
 		}
 		
 		public int getScore() {
 			return first + second + third;
 		}
 	}
 }
