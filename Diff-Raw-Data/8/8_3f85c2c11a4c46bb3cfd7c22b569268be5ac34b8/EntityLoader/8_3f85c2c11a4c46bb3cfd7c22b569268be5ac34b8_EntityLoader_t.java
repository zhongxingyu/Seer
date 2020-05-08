 package entity.util;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.lwjgl.util.vector.Vector3f;
 
 import entity.EntityBuilder;
 import entity.EntityRegistry;
 
 /**
  * This class will load and create EntityBuilders
  * 
  * @author Ichmed
  * 
  */
 public class EntityLoader
 {
 	public static HashMap<String, EntityFound> entitiesFound = new HashMap<>();
 
 	/**
 	 * This method will create a new instance of EntityBuilder containing the
 	 * the values specified in a .entity file. You will have to call a .entlist
 	 * file containing the path to the .emtity file first
 	 * 
 	 * @param name
 	 *            the entity's name
 	 * @return Returns an instance of EntityBuilder if successful and null if
 	 *         not
 	 * @throws IOException
 	 */
 	public static EntityBuilder loadEntity(String name) throws IOException
 	{
 		String path = "";
 		if (doesEntityExist(name))
 		{
 			if (isParentValid(entitiesFound.get(name).getParent())) path = entitiesFound.get(name).getPath();
 			else return null;
 		}
 		else return null;
 
 		File OBJFile = new File(path);
 		BufferedReader reader = new BufferedReader(new FileReader(OBJFile));
 		EntityBuilder e = new EntityBuilder();
 		String line;
 		boolean parentFound = false;
 		while ((line = reader.readLine()) != null)
 		{
 			if (line.startsWith("#"))
 			;
 			else if (line.startsWith("parent "))
 			{
 				String parent = line.split(" ")[1];
 				if (parent.equals("null")) parentFound = true;
 				else if (EntityLoader.doesEntityExist(parent))
 				{
 					EntityRegistry.registerEntityBuilder(EntityLoader.loadEntity(parent));
 					e = EntityRegistry.entries.get(parent).clone();
 					e.parent = parent;
 					parentFound = true;
 				}
 			}
 			else if (!parentFound)
 			{
 				reader.close();
 				return null;
 			}
 			else if (line.startsWith("name "))
 			{
 				e.name = line.split(" ")[1];
 			}
 			else if (line.startsWith("fullName "))
 			{
				e.fullName = line.substring(line.indexOf(" ", 8)).trim();
 			}
 			else if (line.startsWith("physicsType "))
 			{
 				e.physicsType = Integer.valueOf(line.split(" ")[1]);
 			}
 			else if (line.startsWith("collisionType "))
 			{
 				e.collisionType = Integer.valueOf(line.split(" ")[1]);
 			}
 			else if (line.startsWith("invisible "))
 			{
 				e.invisible = Boolean.valueOf(line.split(" ")[1]);
 			}
 			else if (line.startsWith("gravity "))
 			{
 				e.gravity = Boolean.valueOf(line.split(" ")[1]);
 			}
 			else if (line.startsWith("class "))
 			{
 				e.classPath = line.split(" ")[1];
 			}
 			else if (line.startsWith("model "))
 			{
 				e.model = line.split(" ")[1];
 			}
 			else if (line.startsWith("collisionModel "))
 			{
 				e.collisionModel = line.split(" ")[1];
 			}
 			else if (line.startsWith("weight "))
 			{
 				e.weight = Float.valueOf(line.split(" ")[1]);
 			}
 			else if (line.startsWith("balancePoint "))
 			{
 				e.balancePoint = new Vector3f(Float.valueOf(line.split(" ")[1]), Float.valueOf(line.split(" ")[2]), Float.valueOf(line.split(" ")[3]));
 			}
 			else
 			{
 				if (line.startsWith("byte "))
 				{
 					e.customValues.put(line.split(" ")[1], Byte.valueOf(line.split(" ")[2]));
 				}
 				else if (line.startsWith("float "))
 				{
 					e.customValues.put(line.split(" ")[1], Float.valueOf(line.split(" ")[2]));
 				}
 				else if (line.startsWith("integer "))
 				{
 					e.customValues.put(line.split(" ")[1], Integer.valueOf(line.split(" ")[2]));
 				}
 				else if (line.startsWith("boolean "))
 				{
 					e.customValues.put(line.split(" ")[1], Boolean.valueOf(line.split(" ")[2]));
 				}
 				else if (line.startsWith("string "))
 				{
 					e.customValues.put(line.split(" ")[1], line.substring(line.indexOf(" ", 8)).trim());
 				}
 				else if (line.startsWith("file "))
 				{
 					e.customValues.put(line.split(" ")[1], new File(line.split(" ")[2]));
 				}
 			}
 		}
 		reader.close();
 		return e;
 	}
 
 	private static boolean isParentValid(String parent)
 	{
 		if (parent.equals("null")) return true;
 		else if (doesEntityExist(parent)) return isParentValid(entitiesFound.get(parent).getParent());
 		return false;
 	}
 
 	/**
 	 * This method will only work if a .entlist file containing the Entity's
 	 * path was already parsed using findEntities(String path)
 	 * 
 	 * @param name
 	 * @return
 	 */
 	private static boolean doesEntityExist(String name)
 	{
 		return entitiesFound.containsKey(name);
 	}
 
 	/**
 	 * This method will try to find any .entity files specified in a given
 	 * .entlist file and put them into a HashMap for future access
 	 * 
 	 * @param path
 	 *            The path to an .entlist file
 	 * @throws IOException
 	 */
 	public static void findEntities(String path)
 	{
 		try
 		{
 			File entFile = new File(path);
 			BufferedReader reader = new BufferedReader(new FileReader(entFile));
 			String line;
 			List<String> filesToParse = new ArrayList<>();
 			while ((line = reader.readLine()) != null)
 			{
 				filesToParse.add(line);
 			}
 			reader.close();
 
 			for (String s : filesToParse)
 			{
 				findEntity(s);
 			}
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public static void findEntity(String path)
 	{
 		try
 		{
 			File file = new File(path);
 			BufferedReader reader = new BufferedReader(new FileReader(file));
 
 			String line;
 			String name = "", parent = "";
 
 			while ((line = reader.readLine()) != null)
 			{
 				if (line.startsWith("#"))
 				;
 				else if (line.startsWith("parent ")) parent = line.split(" ")[1];
 				else if (line.startsWith("name ")) name = line.split(" ")[1];
 			}
 
 			reader.close();
 
 			if (name != "" && parent != "") entitiesFound.put(name, new EntityFound(parent, name, path));
 			else System.err.println(path + " could not be read properly");
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Used internally for HashMap entries
 	 * 
 	 * @author Ichmed
 	 * 
 	 */
 	public static class EntityFound
 	{
 		private final String parent, name, path;
 
 		public EntityFound(String parent, String name, String path)
 		{
 			super();
 			this.parent = parent;
 			this.name = name;
 			this.path = path;
 		}
 
 		public String getParent()
 		{
 			return parent;
 		}
 
 		public String getName()
 		{
 			return name;
 		}
 
 		public String getPath()
 		{
 			return path;
 		}
 	}
 }
