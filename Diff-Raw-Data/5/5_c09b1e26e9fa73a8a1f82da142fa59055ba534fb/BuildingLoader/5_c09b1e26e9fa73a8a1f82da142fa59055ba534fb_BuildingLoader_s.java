 package net.lotrcraft.strategycraft.loader;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.Map;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import org.yaml.snakeyaml.Yaml;
 
 import net.lotrcraft.strategycraft.StrategyCraft;
 import net.lotrcraft.strategycraft.buildings.Building;
 import net.lotrcraft.strategycraft.units.Unit;
 
 public class BuildingLoader {
 	private static Yaml yaml = new Yaml();
 
 	@SuppressWarnings("unchecked")
 	public static BuildingDescription loadBuilding(File file)
 			throws IOException, InvalidBuildingConfException {
 
 		String building = null, unit = null, name = null;
 		BuildingDescription d = null;
 		try {
 			JarFile jf = new JarFile(file);
 			JarEntry je = jf.getJarEntry("building.yml");
 
 			Map<String, Object> map = (Map<String, Object>) yaml.load(jf.getInputStream(je));
 
 			building = (String) map.get("building");
 			unit = (String) map.get("unit");
 			name = (String) map.get("name");
 
 			jf.close();
 
 			d = new BuildingDescription(name,
					(Class<? extends Building>) new URLClassLoader(new URL[] { file.toURI().toURL() }, Main.class.getClassLoader())
 							.loadClass(building).cast(Building.class),
					(Class<? extends Unit>) new URLClassLoader(new URL[] { file.toURI().toURL() }, Main.class.getClassLoader()).loadClass(unit)
 							.cast(Unit.class));
 
 		} catch (NullPointerException e) {
 			//Causing problems
 			//TODO: Fix
 			//throw new InvalidBuildingConfException();
 			
 			e.printStackTrace();
 			
 		} catch (ClassNotFoundException e) {
 			throw new InvalidBuildingConfException();
 		}
 		return d;
 	}
 }
