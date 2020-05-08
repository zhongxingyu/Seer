 package org.osgcc.osgcc5.soapydroid.levels;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.InputStream;
 import java.util.List;
 import java.util.Scanner;
 
 import org.osgcc.osgcc5.soapydroid.things.CollidableCow;
 import org.osgcc.osgcc5.soapydroid.things.CollidableEinstein;
 import org.osgcc.osgcc5.soapydroid.things.CollidableIceberg;
 import org.osgcc.osgcc5.soapydroid.things.CollidableRock;
 import org.osgcc.osgcc5.soapydroid.things.CollidableThing;
 import org.osgcc.osgcc5.soapydroid.things.CollidableTree;
 
 
 import android.content.res.Resources;
 import android.util.Log;
 
 import org.osgcc.osgcc5.soapydroid.EinsteinDefenseActivity;
 import org.osgcc.osgcc5.soapydroid.R;
 
 public class LevelData extends LevelInitializer{
 
 	Scanner scanner;
 	public LevelData(List<CollidableThing> invaders,
 			List<CollidableThing> projectilesActive,
 			List<CollidableThing> projectilesInactive) throws FileNotFoundException {
 		super(invaders, projectilesActive, projectilesInactive);
 		// TODO Auto-generated constructor stub
 		InputStream reader = EinsteinDefenseActivity.getTextCache().get(R.raw.leveldata);
 		scanner = new Scanner(reader);
 	}
 
 	@Override
 	public void initializeLists(int level) {
 		
 		int numberOfThings = scanner.nextInt();
 		CollidableThing[] thingsArray = new CollidableThing[numberOfThings];
 		String thingType = "";
 		for(int k = 0; k < thingsArray.length; k++ )
 		{
 			thingType = scanner.next();
 			if(thingType.equals("Tree"))
 				thingsArray[k] = new CollidableTree();
 			else if(thingType.equals("Rock"))
 				thingsArray[k] = new CollidableRock();
 			else if(thingType.equals("Cow"))
 				thingsArray[k] = new CollidableCow();
 			else
 				thingsArray[k] = new CollidableEinstein();
 			
 			thingsArray[k].setX(scanner.nextInt());
 			thingsArray[k].setY(scanner.nextInt());
 			thingsArray[k].setMass(scanner.nextInt());
 			
 			Log.v("EinsteinDefenseActivity", "Type: " + thingType + "\n" +
 											 "x: " + thingsArray[k].getX() + "\n " +
 											 "y: " + thingsArray[k].getY() + "\n " +
 											 "Dx: " + thingsArray[k].getDx() + "\n " +
 											 "Dy: " + thingsArray[k].getDy() + "\n " +
 											 "mass: " + thingsArray[k].getMass() + "\n ");
 			
 			if(thingType.equals("Einstein"))
 				thingsArray[k].setDy(scanner.nextInt());
 			projectilesInactive.add(thingsArray[k]);
 		}
 		
 	}
 	
 	
 
 }
