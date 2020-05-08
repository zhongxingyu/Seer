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
 
 
 import me.tehbeard.BeardAch.achievement.rewards.IReward;
 import me.tehbeard.BeardAch.achievement.triggers.ITrigger;
 import me.tehbeard.BeardAch.dataSource.configurable.Configurable;
import me.tehbeard.BeardStat.BeardStat;
 
 public class EditorJSON {
 
 	public List<EditorElement> triggers = new ArrayList<EditorElement>();
 	public List<EditorElement> rewards = new ArrayList<EditorElement>();
 
 	public class EditorElement{
 
 		public List<EditorFormElement> fields = new ArrayList<EditorFormElement>();
 		public String type;
 		public String name;
 	}
 
 	public class EditorFormElement{
 		public String key;
 		public String name;
 		public String type;
 	}
 
 	public void addTrigger(Class<? extends ITrigger> t){
 		addItem(t,triggers);
 	}
 	public void addReward(Class<? extends IReward> r){
 		addItem(r,rewards);
 	}
 	private void addItem(Class<?> c,List<EditorElement> list){
 		
 		EditorElement ee = new EditorElement();
 		ee.name = c.getAnnotation(Configurable.class).name();
 		ee.type = c.getAnnotation(Configurable.class).tag();
 		try{
 		for(Field f : c.getDeclaredFields()){
 			if(!f.isAnnotationPresent(Expose.class)){continue;}
 			EditorFormElement efe = new EditorFormElement();
 			efe.key = f.getName();
 			efe.name = efe.key;
 			efe.type = "text";
 			EditorField a = f.getAnnotation(EditorField.class);
 			if(a != null){
 				efe.name = a.alias();
 				efe.type = a.type().toString().toLowerCase();
 			}
 			ee.fields.add(efe);
 		}
 
 		list.add(ee);
 		}
 		catch(NoClassDefFoundError e){
			BeardStat.printCon("Skipping item " + ee.name);
 		}
 	}
 
 	public void write(File file) throws IOException{
 		FileWriter fw = new FileWriter(file);
 		fw.write("initConfig(");
 		JsonWriter writer = new JsonWriter(fw);
 		Gson gson = new GsonBuilder().setPrettyPrinting().create();
 		
 		
 		gson.toJson(this,new TypeToken<EditorJSON>(){}.getType(),writer);
 		writer.flush();
 		fw.write(");");
 		fw.flush();
 		writer.close();
 	}
 
 }
