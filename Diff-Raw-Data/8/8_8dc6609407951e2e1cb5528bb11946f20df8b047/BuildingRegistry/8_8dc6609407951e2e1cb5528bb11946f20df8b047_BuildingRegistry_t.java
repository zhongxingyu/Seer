 package Project.Game.Buildings;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  * Stores and loads the building xml
  * <p/>
  * Gives access to building stats
  */
 public class BuildingRegistry {
 
     HashMap<String, MetaBuilding> buildings;
 
     public BuildingRegistry() {
         buildings = new HashMap<>();
     }
 
     public MetaBuilding getBuilding(String name) {
 
         if (buildings.containsKey(name)) return buildings.get(name);
         throw new IllegalArgumentException("Building not in registry");
     }
 
     public void load() {
         load("Content/Buildings/Buildings.xml");
     }
 
     public void load(String filename) {
         try {
             JAXBContext context = JAXBContext.newInstance(BuildingsWrapper.class);
             Unmarshaller unmarshaller = context.createUnmarshaller();
 
             BuildingsWrapper temp = (BuildingsWrapper) unmarshaller.unmarshal(new File(filename));
 
             for (MetaBuilding building : temp.buildings) {
                 buildings.put(building.name, building);
             }
         } catch (JAXBException e) {
             e.printStackTrace();
         }
     }
 
     public static void main(String[] args) {
 
         BuildingRegistry registry = new BuildingRegistry();
         registry.load("Content/Buildings/Buildings.xml");
 
        System.out.println(registry.getBuilding("Tower").health);
     }
 }
 
 @XmlRootElement(name = "Buildings")
 class BuildingsWrapper {
     @XmlElement(name = "Building")
     ArrayList<MetaBuilding> buildings;
 
 }
