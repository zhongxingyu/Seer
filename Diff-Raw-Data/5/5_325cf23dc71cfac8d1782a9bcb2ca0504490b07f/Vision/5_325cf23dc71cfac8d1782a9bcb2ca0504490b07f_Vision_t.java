 package factory.interfaces;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import factory.Kit;
 import factory.KitConfig;
 import factory.Part;
 public interface Vision {
 
 	public void msgAnalyzeKitAtInspection(Kit kit);
 
 	
 	 //Alfonso, which of these should we use?
 	public void msgVisionClearForPictureInNests(Nest nest, Nest nest2);
 	public void msgMyNestsReadyForPicture(Nest nestOne, Part nestOnePart, Nest nestTwo, Part nestTwoPart, Feeder feeder);
 	public void msgNewNestConfig( ArrayList<Nest> nests);
 	
 }
