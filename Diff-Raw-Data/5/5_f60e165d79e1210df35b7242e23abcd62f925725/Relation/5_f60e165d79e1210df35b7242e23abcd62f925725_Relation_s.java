 package fr.vergne.data;
 
 public class Relation extends Concept {
 
 	private Concept start;
 	private Concept end;
 
 	public Concept getStart() {
 		return start;
 	}
 
 	public void setStart(Concept start) {
 		if (start == null) {
			throw new RuntimeException("You must give a start element");
 		} else {
 			this.start = start;
 		}
 	}
 
 	public Concept getEnd() {
 		return end;
 	}
 
 	public void setEnd(Concept end) {
 		if (end == null) {
			throw new RuntimeException("You must give an end element");
 		} else {
 			this.end = end;
 		}
 	}
 }
