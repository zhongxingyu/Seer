 package net.mcforge.API.plugin;
 
 import java.security.InvalidParameterException;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.mcforge.world.Generator;
 
 /**
  * The GeneratorHandler is responsible for
  * managing the server's level generators.
  */
 public class GeneratorHandler {
    private List<Generator> generators = new ArrayList<>();
     
     /**
      * Gets a generator from the list of available generators by its name
      * 
      * @param name -The name to search for
      *            
      * @return The generator if found, otherwise null.
      */
     public Generator findGenerator(String name) {
    	for (int i = 0; i < generators.size(); i++) {
     		Generator g = generators.get(i);
     		if (g.getName().equals(name))
     				return g;
     		else {
         		for (int j = 0; j < g.getShortcuts().length; j++) {
         			if (g.getShortcuts()[j].equalsIgnoreCase(name))
         				return g;
         		}
     		}
     	}
     	return null;
     } 
     
     /**
      * Removes the specified generator from the server
      * 
      * @param g - The generator to remove
      */
     public void removeGenerator(Generator g) {
    	if (generators.contains(g))
    		generators.remove(g);
     }
     
     /**
      * Removes the specified generator from the server
      * 
      * @param name - The name of the generator to remove
      */
     public void removeGenerator(String name) {
    	Generator g = findGenerator(name);
     	if (g == null)
     		throw new InvalidParameterException("The specified generator name for the removeGenerator method is invalid! A generator with that name doesn't exist!");
     	removeGenerator(g);
     }
     
     /**
      * Adds a new generator to the server
      * if it's not already present
      * 
      * @param g - The generator to add
      */
     public void addGenerator(Generator g) {
    	if (!generators.contains(g))
    		generators.add(g);
     }
     
     /**
      * Gets the currently loaded generators
      * 
      * @return A list containing the loaded generators
      */
    public List<Generator> getGenerators() {
    	return generators;
     }
 }
