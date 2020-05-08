 package cs437.som;
 
 import java.io.*;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.regex.Pattern;
 
 /**
  * Reads a self-organizing map from a file.
  */
 public class FileReader {
     private static final Pattern COLON_SPLIT = Pattern.compile(":");
     private TrainableSelfOrganizingMap tsom = null;
 
     private FileReader(File input) throws IOException {
         BufferedReader isr = new BufferedReader(
                 new InputStreamReader(new FileInputStream(input)));
         String[] kv = COLON_SPLIT.split(isr.readLine());
         if (kv.length != 2) {
             throw new SOMError(
                     "Input file is malformed: first line must be a map type statement.");
         }
 
         String className = "cs437.som.network." + kv[1].trim();
         try {
             Class<?> mapType = Class.forName(className);
             Method readMethod = mapType.getMethod("read", BufferedReader.class);
             tsom = (TrainableSelfOrganizingMap) readMethod.invoke(mapType, isr);
         } catch (ClassNotFoundException e) {
             throw new SOMError("Map type " + className + " cannot be found.");
         } catch (NoSuchMethodException e) {
             throw new SOMError("Map type " + className + " cannot be loaded from a file.");
         } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new SOMError("Internal error.");
         } catch (IllegalAccessException e) {
            throw new SOMError("Internal error.");
         }
     }
 
     public static TrainableSelfOrganizingMap read(File file)
             throws IOException {
         FileReader fileReader = new FileReader(file);
         return fileReader.tsom;
     }
 
     public static TrainableSelfOrganizingMap read(String filename)
             throws IOException {
         File file = new File(filename);
         return read(file);
     }
 
     public static void main(String[] args) throws IOException {
         FileReader.read("basic.som");
     }
 
     @Override
     public String toString() {
         return "FileReader";
     }
 }
