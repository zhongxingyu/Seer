 package algorithm;
 
 import java.util.Properties;
 
 public class DiskParameter {
 	int previous;
 	int current;
 	int sequence[];
 	int cylinders;
 
 	public DiskParameter(Properties p) {
 		this.cylinders = Integer.parseInt(p.getProperty("Cylinders"));
 		this.current = Integer.parseInt(p.getProperty("Position.Current"));
 		this.previous = Integer.parseInt(p.getProperty("Position.Previous"));
 		String token[] = p.getProperty("Sequence").split(",");
 		sequence = new int[token.length];
 		for (int i = 0; i < token.length; i++) {
 			sequence[i] = Integer.parseInt(token[i]);
 		}
 	}
 
 	public int getPrevious() {
 		return previous;
 	}
 
 	public void setPrevious(int previous) {
 		this.previous = previous;
 	}
 
 	public int getCurrent() {
 		return current;
 	}
 
 	public void setCurrent(int current) {
 		this.current = current;
 	}
 
 	public int[] getSequence() {
 		return sequence;
 	}
 
 	public void setSequence(int[] sequence) {
 		this.sequence = sequence;
 	}
 
 	public int getCylinders() {
 		return cylinders;
 	}
 
 	public void setCylinders(int cylinders) {
 		this.cylinders = cylinders;
 	}
 
 }
