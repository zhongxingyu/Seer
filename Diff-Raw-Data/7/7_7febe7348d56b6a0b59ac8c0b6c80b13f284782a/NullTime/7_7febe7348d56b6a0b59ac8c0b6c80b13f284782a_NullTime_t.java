 package sort;
 
 public class NullTime extends Time {
 	
 	public NullTime() {
 		super(0);
 	}
 	
 	@Override
 	public String toString() {
 		return "--.--.--";
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		return true;
 	}
	
	@Override
	public int compareTo(Time time){
		return 1;
	}
	
 	@Override
 	public Time difference(Time t) {
 		return t;
 	}
 }
