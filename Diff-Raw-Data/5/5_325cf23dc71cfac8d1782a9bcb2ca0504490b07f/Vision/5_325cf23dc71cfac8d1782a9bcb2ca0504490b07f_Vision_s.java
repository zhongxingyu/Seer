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
<<<<<<< HEAD
	public void msgMyNestsReadyForPicture(Nest nest, Part nestPart, Nest nest2, Part nest2Part,Feeder feeder);
=======
 	public void msgMyNestsReadyForPicture(Nest nestOne, Part nestOnePart, Nest nestTwo, Part nestTwoPart, Feeder feeder);
 	public void msgNewNestConfig( ArrayList<Nest> nests);
>>>>>>> Vision now checks that parts match the needed ones
 	
 }
