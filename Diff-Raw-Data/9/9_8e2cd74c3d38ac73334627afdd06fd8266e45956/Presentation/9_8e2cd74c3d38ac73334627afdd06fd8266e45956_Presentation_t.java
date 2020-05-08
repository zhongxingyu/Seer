 package presenter.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class Presentation {
 
 	// ############ STATICS ############
 	private static Presentation instance = null;
 
 	private static void getInstance() {
 		if (null == instance)
 			instance = new Presentation();
 	}
 
 	public static void open(String path) {
 		getInstance();
 	}
 
 	public static void create() {
 		getInstance();
 	}
 
 	public static Slide next() {
 		return get(++instance.index);
 	}
 
 	public static Slide getCurrent() {
 		return get(instance.index);
 	}
 
 	public static Slide previous() {
 		return get(--instance.index);
 	}
 
 	public static Slide get(int index) {
		index = (index % instance.slides.size() + instance.slides.size())
				% instance.slides.size();
 		return instance.slides.get(index);
 	}
 
 	public static List<Slide> getSlides() {
 		return instance.slides;
 	}
 
 	public static PresentationEditor getEditor() {
 		return new PresentationEditor(instance.slides);
 	}
 
 	// ########## NON-STATICS ##########
 	private List<Slide> slides;
 	private int index = 0;
 
 	public Presentation() {
 		slides = new ArrayList<Slide>();
 	}
 }
