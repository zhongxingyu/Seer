 package linewars.parser;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Scanner;
 
 public class Parser {
 
 	private HashMap<String, Value> values;
 	private ConfigFile configFile;
 	private int startLine;
 	private int endLine;
 	
 	public Parser(ConfigFile cFile) throws InvalidConfigFileException
 	{
 		configFile = cFile;
 		startLine = cFile.nextLineNumber();
 		values = new HashMap<String, Value>();
 		
 		while(configFile.hasNextLine())
 		{
 			Scanner line = new Scanner(configFile.nextLine());
 			String key = line.next().toLowerCase();
 			
 			//allow comments
 			if(key.charAt(0) == '#')
 				continue;
 			
 			if(key.equals("}"))
 				break;
 			
 			if(!line.hasNext() || !line.next().equals("=") || !line.hasNext())
 				throw new InvalidConfigFileException(configFile.getURI() + "ERROR LINE " + (configFile.nextLineNumber() - 1) + 
 						": is not a valid config file.");
 			
 			String value = line.nextLine().trim();
 			
 			values.put(key, new Value(value));	
 		}
 		
 		endLine = configFile.nextLineNumber() - 1;
 	}
 	
 	public String getStringValue(ParserKeys key) throws NoSuchKeyException
 	{
 		checkKey(key);
 		return values.get(key).value;
 	}
 	
 	public double getNumericValue(ParserKeys key) throws NoSuchKeyException
 	{
 		checkKey(key);
 		return Double.parseDouble(values.get(key).value);
 	}
 	
 	public String[] getList(ParserKeys key) throws NoSuchKeyException
 	{
 		checkKey(key);
 		
 		ArrayList<String> list = new ArrayList<String>();
 		Scanner s = new Scanner(values.get(key).value);
 		s.useDelimiter(",");
 		
 		while(s.hasNext())
 			list.add(s.next());
 		
 		return list.toArray(new String[0]);
 	}
 	
 	public Parser getParser(ParserKeys key) throws NoSuchKeyException
 	{
 		checkKey(key);
 		return values.get(key).parser;
 	}
 	
 	@Deprecated
 	public String getStringValue(String key) throws NoSuchKeyException
 	{
 		checkKey(key);
 		return values.get(key).value;
 	}
 	
 	@Deprecated
 	public double getNumericValue(String key) throws NoSuchKeyException
 	{
 		checkKey(key);
 		return Double.parseDouble(values.get(key).value);
 	}
 	
 	@Deprecated
 	public String[] getList(String key) throws NoSuchKeyException
 	{
 		checkKey(key);
 		
 		ArrayList<String> list = new ArrayList<String>();
 		Scanner s = new Scanner(values.get(key).value);
 		s.useDelimiter(",");
 		
 		while(s.hasNext())
 			list.add(s.next());
 		
 		return list.toArray(new String[0]);
 	}
 	
 	@Deprecated
 	public Parser getParser(String key) throws NoSuchKeyException
 	{
 		checkKey(key);
 		return values.get(key).parser;
 	}
 	
 	public ConfigFile getConfigFile()
 	{
 		return configFile;
 	}
 	
 	private void checkKey(String key) throws NoSuchKeyException
 	{
 		if(!values.containsKey(key))
 			throw new NoSuchKeyException("The key \"" + key + "\" is not contained in the config file " + configFile.getURI() + 
 					" from line " + startLine + " to " + endLine);
 	}
 	
 	private void checkKey(ParserKeys key) throws NoSuchKeyException
 	{
 		if(!values.containsKey(key))
 			throw new NoSuchKeyException("The key \"" + key + "\" is not contained in the config file " + configFile.getURI() + 
 					" from line " + startLine + " to " + endLine);
 	}
 	
 	private class Value {
 		String value = null;
 		Parser parser = null;
 		public Value(String data) throws InvalidConfigFileException
 		{
 			if(data.equals("{"))
 				parser = new Parser(configFile);
 			else
 				value = data;
 		}
 	}
 	
 	public static class InvalidConfigFileException extends Exception {
 		private static final long serialVersionUID = 8024603342014298051L;
 
 		public InvalidConfigFileException(String s) {
 			super(s);
 		}
 	}
 	
 	public static class NoSuchKeyException extends RuntimeException {
 		private static final long serialVersionUID = -3402614354747155750L;
 
 		public NoSuchKeyException(String s) {
 			super(s);
 		}
 	}
 	
 }
