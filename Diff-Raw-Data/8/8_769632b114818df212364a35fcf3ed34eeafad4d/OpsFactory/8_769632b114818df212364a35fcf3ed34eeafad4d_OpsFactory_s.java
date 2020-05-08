 package ops;
 
 
 import ops.commands.*;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.charset.Charset;
 import java.util.*;
 
 
 public class OpsFactory
 {
  static Map<String, Command> _registeredCommands = new HashMap<String, Command>();

   public static Map<String, Command> getDefaultRegistry()
   {
     Map<String, Command> registry = new HashMap<String, Command>();
     registry.put("remove", new remove());
     registry.put("write", new write());
     registry.put("bind", new bind());
     registry.put("halt", new halt());
     registry.put("make", new make());
     registry.put("modify", new modify());
     return registry;
   }
 
   public static OPS create(Map<String, Command> registry, String opsFile)
   {
     if (opsFile == null || opsFile.isEmpty())
     {
       throw new IllegalArgumentException("opsFile is null or empty");
     }
 
     File file = new File(opsFile);
     if (!file.exists())
     {
       throw new IllegalArgumentException("opsFile does not exist: " + opsFile);
     }
 
     return create(registry, file);
   }
 
   public static OPS create(Map<String, Command> registry, File opsFile)
   {
     OPS ops = null;
 
     try
     {
       ops = processOPSFile(registry, opsFile);
       return ops;
     }
     catch (Exception e)
     {
       e.printStackTrace();
     }
 
     return ops;
   }
 
   private static Command getCommand(Map<String, Command> registry, String name)
   {
    if (!_registeredCommands.containsKey(name))
     {
       throw new IllegalArgumentException(String.format("command %s not found", name));
     }
    return _registeredCommands.get(name);
   }
 
   private static OPS processOPSFile(Map<String, Command> registry, File file)
       throws Exception
   {
     OPS ops = new OPS();
 
     JSONObject obj = readJSONFile(file.getCanonicalPath());
     if (obj.has("name"))
     {
       System.out.println("processing ops: " + obj.getString("name"));
     }
     if (!obj.has("ops"))
     {
       throw new IllegalArgumentException("missing ops section");
     }
 
     JSONArray arr = obj.getJSONArray("ops");
 
     for (int i = 0; i < arr.length(); i++)
     {
       JSONArray statement = arr.getJSONArray(i);
       if (statement.length() < 2)
       {
         System.out.println("malformed statement: " + statement);
       }
 
       String cmd = statement.getString(0);
       if (cmd.equals("literalize"))
       {
         String recordName = statement.getString(1);
         Map<String, Object> values = new HashMap<String, Object>();
         for (int j = 2; j < statement.length(); j++)
         {
           Object field = statement.get(j);
           if (field instanceof String)
           {
             values.put(field.toString(), null);
           }
           else if (field instanceof JSONObject)
           {
             JSONObject fieldObj = (JSONObject)field;
             Iterator<String> keys = fieldObj.keys();
             while (keys.hasNext())
             {
               String key = keys.next();
               values.put(key, fieldObj.get(key));
             }
           }
         }
 
         ops.literalize(new MemoryElement(recordName, values));
       }
       else if (cmd.equals("make"))
       {
         String recordName = statement.getString(1);
         Map<String, Object> values = new HashMap<String, Object>();
         for (int j = 2; j < statement.length(); j += 2)
         {
           values.put(statement.getString(j), statement.get(j+1));
         }
         ops.make(new MemoryElement(recordName, values));
       }
       else if (cmd.equals("p"))
       {
         String productionName = statement.getString(1);
 
         List<QueryElement> query = new ArrayList<QueryElement>();
         JSONArray list = statement.getJSONArray(2);
         for (int j = 0; j < list.length(); j++)
         {
           JSONArray matcher = list.getJSONArray(j);
           String recordName = matcher.getString(0);
           Object[] values = sublist(1, matcher);
           query.add(new QueryElement(recordName, values));
         }
 
         List<ProductionSpec> productions = new ArrayList<ProductionSpec>();
         list = statement.getJSONArray(3);
         for (int j = 0; j < list.length(); j++)
         {
           JSONArray production = list.getJSONArray(j);
           String commandName = production.getString(0);
           Command command = getCommand(registry, commandName);
           Object[] params = sublist(1, production);
           productions.add(new ProductionSpec(command, params));
         }
 
         ops.addRule(new Rule(productionName, query, productions));
       }
     }
 
     return ops;
   }
 
   private static Object[] sublist(int startIdx, JSONArray arr)
       throws JSONException
   {
     Object[] objects = new Object[arr.length() - startIdx];
     for (int i = startIdx, j = 0; i < arr.length(); i++, j++)
     {
       objects[j] = arr.get(i);
     }
     return objects;
   }
 
   public static JSONObject readJSONFile(String path)
       throws IOException, JSONException
   {
     return new JSONObject(readFile(path));
   }
 
   // http://stackoverflow.com/questions/326390/how-to-create-a-java-string-from-the-contents-of-a-file
   public static String readFile(String path)
       throws IOException
   {
     FileInputStream stream = new FileInputStream(new File(path));
     try
     {
       FileChannel fc = stream.getChannel();
       MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
       /* Instead of using default, pass in a decoder. */
       return Charset.defaultCharset().decode(bb).toString();
     }
     finally
     {
       stream.close();
     }
   }
 
   public static String getPwd()
   {
     File file = new File(".");
     try
     {
       return file.getCanonicalPath();
     }
     catch (IOException e)
     {
       e.printStackTrace();
     }
     return "";
   }
 }
