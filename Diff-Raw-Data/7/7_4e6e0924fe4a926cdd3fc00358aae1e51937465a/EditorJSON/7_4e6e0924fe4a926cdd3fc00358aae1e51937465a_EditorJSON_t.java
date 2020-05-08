 package me.tehbeard.BeardAch.dataSource.json.editor;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.annotations.Expose;
 import com.google.gson.reflect.TypeToken;
 import com.google.gson.stream.JsonWriter;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Enumeration;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 import me.tehbeard.BeardAch.BeardAch;
 import me.tehbeard.BeardAch.achievement.rewards.IReward;
 import me.tehbeard.BeardAch.achievement.triggers.ITrigger;
 import me.tehbeard.BeardAch.dataSource.configurable.Configurable;
 
 /**
  * Generates the json file for the achievements editor
  *
  * @author James
  *
  */
 public class EditorJSON {
 
     public List<EditorElement> triggers = new ArrayList<EditorElement>();
     public List<EditorElement> rewards = new ArrayList<EditorElement>();
 
     public class EditorElement {
 
         public List<EditorFormElement> fields = new ArrayList<EditorFormElement>();
         public String type;
         public String name;
     }
 
     /**
      * Structural class for elements of a trigger/reward
      *
      * @author James
      *
      */
     public class EditorFormElement {
 
         public String key;
         public String name;
         public String type;
         public String[] values = null;
     }
 
     public void addTrigger(Class<? extends ITrigger> t) {
         addItem(t, triggers);
     }
 
     public void addReward(Class<? extends IReward> r) {
         addItem(r, rewards);
     }
 
     private void addItem(Class<?> c, List<EditorElement> list) {
 
         EditorElement ee = new EditorElement();
         ee.name = c.getAnnotation(Configurable.class).name();
         ee.type = c.getAnnotation(Configurable.class).tag();
         try {
             for (Field f : c.getDeclaredFields()) {
                 if (!f.isAnnotationPresent(Expose.class)) {
                     continue;
                 }
                 EditorFormElement efe = new EditorFormElement();
                 efe.key = f.getName();
                 efe.name = efe.key;
                 efe.type = "text";
                 EditorField a = f.getAnnotation(EditorField.class);
                 if (a != null) {
                     efe.name = a.alias();
                     efe.type = a.type().toString().toLowerCase();
                     if (a.options().length > 0) {
                         if (a.options().length == 1) {
                             Class<Enum> enumClass = (Class<Enum>) Class.forName(a.options()[0]);
                             Enum[] enums = (Enum[]) enumClass.getMethod("values").invoke(null);
                             String[] options = new String[enums.length];
                             for(int i = 0;i<enums.length; i++){
                                 options[i] = enums[i].name();
                             }
                             efe.values = options;
                         }
                         else
                         {
                             efe.values = a.options();
                         }
                     }
                 }
                 ee.fields.add(efe);
             }
 
             list.add(ee);
         } catch (NoClassDefFoundError e) {
             BeardAch.printCon("Skipping item " + ee.name);
         } catch (ClassNotFoundException ex) {
             Logger.getLogger(EditorJSON.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IllegalArgumentException ex) {
             Logger.getLogger(EditorJSON.class.getName()).log(Level.SEVERE, null, ex);
         } catch (InvocationTargetException ex) {
             Logger.getLogger(EditorJSON.class.getName()).log(Level.SEVERE, null, ex);
         } catch (NoSuchMethodException ex) {
             Logger.getLogger(EditorJSON.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SecurityException ex) {
             Logger.getLogger(EditorJSON.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             Logger.getLogger(EditorJSON.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void write(File file) throws IOException {
         FileWriter fw = new FileWriter(file);
        fw.write("$(function(){initConfig(");
         JsonWriter writer = new JsonWriter(fw);
        writer.setIndent("  ");
         Gson gson = new GsonBuilder().setPrettyPrinting().create();
 
 
         gson.toJson(this, new TypeToken<EditorJSON>() {
         }.getType(), writer);
         writer.flush();
        fw.write(");});");
         fw.flush();
         writer.close();
     }
 }
