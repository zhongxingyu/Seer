 
 
 import java.io.File;
 import java.util.ArrayList;
 
 import org.citygml4j.model.citygml.building.Building;
 
 
 /**
  * 7-3-2012
  * Vandaag packages 6 gemaakt. Ook interior shell werkt nu goed, vanaf versie 8 van de poly files
  * Responsible for the overall process; voorlopig want wanneer ik GUI klas erbij
  * maak komt daar mogelijk een deel van de regie te liggen.
  * @author kooijmanj1
  *
  */
 public class VReaderWriter {
 	
 	private String destinationName;
 	private VInputFile input;
 	private VOutputFile output;
 	private File sourceFile;
 	private File destinationFolder;
 	private int shellNr = 1;
 	private int buildingNr = 1;
 	
 	public VReaderWriter(File sourceFile, File destinationFolder){
 		this.sourceFile = sourceFile;
 		this.destinationFolder = destinationFolder;
 	}
 
 	public void organizeConversion() throws Exception{
 		input = new VInputFile(sourceFile);
 		VStringStore stringStore = new VStringStore();
 		ArrayList<Building> buildingList = input.readAllBuildings();	
 		for (Building building : buildingList){
 			String buildingId = building.getId();
       // System.out.println("BuildingId: " + buildingId);
 			VConstruct<Building> construct = new VConstruct<Building>();
 			construct.store(building);
 			construct.setStringStore(stringStore);
 			construct.organize();
 			for (String str :stringStore.getShellStrings() ){	
        // System.out.println("Raw string begin\n" + str + "\n Raw string end");
 				// find position of INTERIOR_ or EXTERIOR_INDICATOR
 				int endIndex = str.indexOf("EX");
 				String where = ".0";
 				if (endIndex == -1){ // meaning: there is no "EX" in str, so it is not an exterior shell
 					endIndex = str.indexOf("IN");
 					where = ".1";
 				}
 				
 				// split of  part of str for file name
 				String helpStr = str.substring(2,endIndex);//+2 verwijderd
        // destinationName = destinationFolder.getPath() +"/"+ "shell-" + shellNr + "_" + helpStr + where + ".poly";
        destinationName = destinationFolder.getPath() +"/"+ helpStr + where + ".poly";
 				
 				//Take away INTERIOR_ or EXTERIOR_INDICATOR from ID
 				String beginStr = str.substring(0, endIndex);
 				String endStr = str.substring(endIndex + 2);
 				str = beginStr + endStr;
         // System.out.println(destinationName);
         // System.out.println(str);
 				output = new VOutputFile(new File(destinationName));
 				output.writeBuilding(str);
 				//
 				shellNr++;
 			}
 			buildingNr++;
 			stringStore.clear();
       // System.out.println("Number of buildings written to disk: " + (buildingNr-1) );
       // System.out.println("Number of shells: " + (shellNr-1));
 		}
 	}
 	
 	public int getNumberOfBuildings(){
 		return buildingNr - 1;
 	}
 	
 	public int getNumberOfShells(){
 		return shellNr - 1;
 	}
 }
