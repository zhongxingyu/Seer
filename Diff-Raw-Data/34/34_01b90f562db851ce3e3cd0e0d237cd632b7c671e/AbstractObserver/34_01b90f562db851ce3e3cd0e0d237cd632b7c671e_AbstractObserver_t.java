 package tutorials.slickout.dda.observer;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observer;
 import tutorials.slickout.dda.Adaptation;
 
 public abstract class AbstractObserver implements Observer {
 
 	protected List<Adaptation> adaptations;
 	
 	public AbstractObserver(){
 		adaptations = new ArrayList<Adaptation>();
 	}
 	
 	public List<Adaptation> getAdaptations(){
 		return adaptations;
 	}
 	
}
