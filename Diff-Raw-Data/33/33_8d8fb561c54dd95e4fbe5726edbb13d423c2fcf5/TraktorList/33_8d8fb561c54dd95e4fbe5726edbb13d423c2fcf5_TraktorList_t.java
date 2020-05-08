@Writer("Lena Lenz")
 public class TraktorList extends LinkedList {
 	
 	public void append(Traktor t) {
 		super.append(t);
 	}
 	
 	public void prepend(Traktor t) {
 		super.prepend(t);
 	}
 	
 	public Node find(Traktor t) {
 		return super.find(t);
 	}
 	
 	public Traktor remove(Traktor t) {
 		return (Traktor)super.remove(t);
 	}
 
 }
