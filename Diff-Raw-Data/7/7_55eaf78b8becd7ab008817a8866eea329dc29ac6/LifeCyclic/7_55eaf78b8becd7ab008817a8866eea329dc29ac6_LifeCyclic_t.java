 package life;
 
 import java.util.ArrayList;
 /**
  * classe qui determine un cycle non-stable
  *
  */
 public class LifeCyclic extends LIFE {
 	private ArrayList<ArrayList<Cellule>> history;
 	private Integer current;
 	private Integer periode;
 	
 	public LifeCyclic(ArrayList<Cellule> raw, ArrayList<ArrayList<Cellule>> history) {
 		super(raw);
 		this.history = history;
 		this.current = 0;
 		this.periode = history.size();
 	}
 
 	public LIFE next(){
 		current = (current+1) % periode;
 		raw = history.get(current);
 		return this;
 	}
 	
 }
