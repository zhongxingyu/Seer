 package no.hild1.utils;
 
import org.json.simple.*;
 import org.json.simple.parser.*;
import java.util.*;
 
 public class Updater {
     public String foo;
     public Updater(String f) {
         this.foo = f;
     }
     public static void main(String[] args) {
         Updater upd = new Updater("Hello World");
         System.out.println(upd.foo);
         String jsonText = "{\"first\": 123, \"second\": [4, 5, 6], \"third\": 789}";
             JSONParser parser = new JSONParser();
             ContainerFactory containerFactory = new ContainerFactory(){
                 public List creatArrayContainer() {
                     return new LinkedList();
                 }
                 public Map createObjectContainer() {
                     return new LinkedHashMap();
                 }
             };
         try {
             Map json = (Map)parser.parse(jsonText, containerFactory);
             Iterator iter = json.entrySet().iterator();
             System.out.println("==iterate result==");
             while(iter.hasNext()){
                 Map.Entry entry = (Map.Entry)iter.next();
                 System.out.println(entry.getKey() + "=>" + entry.getValue());
             }
             System.out.println("==toJSONString()==");
             System.out.println(JSONValue.toJSONString(json));
         }
         catch(ParseException pe){
             System.out.println(pe);
         }
     } 
 } 
