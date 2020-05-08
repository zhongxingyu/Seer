 /* Copyright (c) 2013, Regents of the University of California.  See License.txt for details */
 
 package org.grayleaves.utility;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.util.Properties;
 
 import org.grayleaves.utility.ArrayParameter;
 import org.grayleaves.utility.Constants;
 import org.grayleaves.utility.Input;
 import org.grayleaves.utility.ModelProperties;
 import org.grayleaves.utility.ParameterSpace;
 import org.grayleaves.utility.ParameterSpacePersister;
 import org.grayleaves.utility.PersistenceException;
 import org.grayleaves.utility.Persister;
 import org.grayleaves.utility.PropertiesParameterUpdater;
 import org.grayleaves.utility.RangeParameter;
 import org.grayleaves.utility.SimplePersister;
 import org.grayleaves.utility.StaticParameterUpdater;
 
 
 public class TestingFileBuilder
 {
 	public static String buildPersistentInput() throws Exception
 	{
 		String name = "testingInput.xml"; 
 		File file = new File(name);
 		if (file.exists()) file.delete(); 
 		Persister<Input> persister = new SimplePersister<Input>(); 
 		file = persister.save(new TestingInput(17), name); 
 		return name;
 	}
 	public static String buildParameterSpaceFilenameAndPersist(boolean small) throws Exception
 	{
 		String name = buildParameterSpaceFilename(small); 
 		persistParameterSpace(buildParameterSpace(small), name);
 		return name;
 	}
 	public static String buildParameterSpaceFilename(boolean small) throws Exception, PersistenceException
 	{
 		String name = (small ? "smallParms.xml" : "parms.xml");
 		return name;
 	}
 	public static void persistParameterSpace(ParameterSpace space, String name)
 			throws Exception, PersistenceException
 	{
 		File file = new File(name);
 		if (file.exists()) file.delete(); 
 		space.setFilename(name); 
 		Persister<ParameterSpace> spacePersister = new ParameterSpacePersister<ParameterSpace>();
 		file = spacePersister.save(space, space.getFilename());
 	}
 	public static ParameterSpace buildParameterSpace(boolean small) throws Exception
 	{
 		ParameterSpace space = new ParameterSpace(); 
 		space.setName("test parameter space"); 
 		ModelProperties.setPropertiesForTesting(null);
 		space.loadProperties(buildTestingProperties()); 
 		if (small)
 		{
 			ScenarioSetTest.addSmallParameters(space);
 //				space = ScenarioSetTest.buildSmallParameterSpace(); 
 		}
 		else
 		{
 			space.addParameter(new ArrayParameter<Integer>("String Name", new PropertiesParameterUpdater<Integer>(Integer.class, "integer.property"), new Integer[]{6, 7, 8}));
 			space.addParameter(new ArrayParameter<String>("String Public Name", new StaticParameterUpdater<String>(String.class, "STRING_PARM", "org.grayleaves.utility.TestingBean"), new String[]{"fred", "sam"}));
 			space.addParameter(new RangeParameter<Integer>("Int2 Public Name", new TestingParameterUpdater<Integer>(), 0, 30, 10, 20)); 
 		}
 		return space;
 	}
 	public static String buildParameterSpace() throws Exception
 	{
 		return buildParameterSpaceFilenameAndPersist(false); 
 	}
 	public static String buildTestingProperties() throws Exception
 	{
 		// "org/smartantsgame/utility/testing.properties"
 		String location = "org/grayleaves/utility/testing.properties";
		String name = "bin/"+location; 
 		File file = new File(name);
 		if (file.exists()) file.delete(); 
 		Properties properties = new Properties(); 
 		properties.put("string.property", "someValue");
 		properties.put("integer.property", "2");
 		properties.put("boolean.property", "false");
 		properties.store(new BufferedOutputStream(new FileOutputStream(name)), "some comment");
 		return location; 
 	}
 	public static String getFileContents(String filename) throws Exception
 	{
 		StringBuffer sb = new StringBuffer(); 
 		BufferedReader reader  = new BufferedReader(new FileReader(filename)); 
 		String line = reader.readLine();  
 		while (line != null)
 		{
 			sb.append(line);
 			sb.append("\n"); 
 			line = reader.readLine();
 		}
 		reader.close(); 
 		return sb.toString();
 	}
 	public static void cleanUpDirectory(String path) throws Exception
 	{
 		File dir = new File(path); 
 //		System.out.println("clean up directory "+dir.getCanonicalPath());
 		String[] files = dir.list(); 
 		if (files != null)
 		{
 			File file = null; 
 			for (int i = 0; i < files.length; i++)
 			{
 				file = new File(dir.getCanonicalPath()+Constants.SLASH+files[i]); 
 				if (file.isDirectory()) cleanUpDirectory(file.getCanonicalPath()); 
 				else if (!file.delete()) System.out.println("couldn't clean up file: "+file.getCanonicalPath()); 
 			}
 		}
 		dir.delete();
 	}
 }
