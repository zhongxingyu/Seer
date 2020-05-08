 package spaceappschallenge.moonville.factories;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 import spaceappschallenge.moonville.R;
 import spaceappschallenge.moonville.businessmodels.Building;
 import spaceappschallenge.moonville.managers.ApplicationService;
 import spaceappschallenge.moonville.xml_parsers.BuildingXMLParser;
 import android.content.Context;
 import android.util.Log;
 
 //singleton class
 public class Buildings
 {
 	private static Buildings instance;
 	protected static Context context;
 
 	//a list of all possible buildings
 	private ArrayList<Building> allBuildings;
 	
 	//a list of buildings that the player can build
 	private ArrayList<Building> availableBuildings;
 	
 	protected InputStream inputStream = null;
 
 	
 	private Buildings()
 	{	
 		this.allBuildings = new ArrayList<Building>();
 		
 		this.availableBuildings = new ArrayList<Building>();
 
 		Buildings.context = ApplicationService.getInstance().getApplicationContext();
 		
 		Log.i("Buildings","initializing all buildings");
		initAllBuildings();
 		
 	}
 	
	protected void initAllBuildings(){
 		inputStream = context.getResources().openRawResource(R.raw.buildings);
 
 		try {
 			BuildingXMLParser xmlParser = new BuildingXMLParser(inputStream);
 			try {
 				Log.i("Buildings","parsing.....");
 				this.allBuildings = xmlParser.parse();
 				Log.i("Buildings","printing.....");
 				printAllBuildings();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				Log.e("Buildings","There was problem while parsing the xml file");
 				//e.printStackTrace();
 			}
 		} catch (XmlPullParserException e) {
 			// TODO Auto-generated catch block
 			Log.e("Buildings","XMLParser could not be instantiated");
 			//e.printStackTrace();
 		}
 	}
 	
 	public void printAllBuildings(){
 		for(int i=0;i<this.allBuildings.size();i++){
 			Building building = this.allBuildings.get(i);
 			
 			Log.i("Buildings",building.getName());
 			ArrayList<Building> buildings = building.getRequiredBuildings();
 			Log.i("LENGTH","size:"+buildings.size());
 			for(Building rbuilding: buildings){
 				Log.i("Buildings rb",rbuilding.getName());
 			}
 			/*
 			ArrayList<Resource> resources= building.getRequiredResources();
 			Log.i("LENGTH","reqd resource size:"+resources.size());
 			for(Resource rResource:resources){
 				Log.i("Buildings rr",rResource.getName());
 			}
 			*/
 			Log.i("Buildings","object: "+building);
 		}
 	}
 	
 	
 	public static Buildings getInstance()
 	{
 		//TODO: when loading existing game, this factory should be replaced by the saved one, not create a new one
 		
 		if( Buildings.instance == null )
 		{
 			Buildings.instance = new Buildings();
 		}
 		
 		return Buildings.instance;
 	}
 
 
 	//returns a building object according to its name
 	public Building getBuilding( String name )
 	{
 		Building foundBuilding = null;
 		
 		for( Building building : this.allBuildings )
 		{
 			if( building.getName().equals( name ) )
 			{
 				foundBuilding = building;
 				break;
 			}
 		}
 		
 		return foundBuilding;
 	}
 
 
 	//returns all available buildings
 	public ArrayList<Building> getAllBuildings()
 	{
 		return this.allBuildings;
 	}
 }
