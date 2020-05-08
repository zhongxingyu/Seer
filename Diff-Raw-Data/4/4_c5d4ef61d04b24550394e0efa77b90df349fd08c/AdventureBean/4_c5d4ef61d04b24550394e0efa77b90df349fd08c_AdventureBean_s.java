 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package beans;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 /**
  *
  * @author xbmc
  */
 public class AdventureBean {
     public static final List<String[]> ADVENTURE_LIST;
     
     static {
         ADVENTURE_LIST = new ArrayList();
         try {
             InputStream adventureStream = AdventureBean.class.getClassLoader().getResourceAsStream("adventures.txt");
             BufferedReader br = new BufferedReader(new InputStreamReader(adventureStream, "UTF-8"));
             String line = br.readLine();
 
             while (line != null) {
                 String[] splitLine = line.split("\t");
                 if(splitLine.length>=4){
                     String group = splitLine[0];
                     String location = splitLine[3];
                     String action = splitLine[1];
                     if(action.contains("adventure=")){
                         String snarfblat = action.replace("adventure=", "");
                         String[] adventure = {snarfblat,group+": "+location};
                         ADVENTURE_LIST.add(adventure);
                     }
                 }
                 line = br.readLine();
             }
         } catch(Exception e){
             System.out.println(e);
         }
         Collections.sort(ADVENTURE_LIST, new AdventureComparator());
     }
     
     public static String getAutomaticAdventures(){
         String automaticAdventure =
             "<center>"+
             "<form method=\"post\" action=\"adventure.php\">"+
             "<select name=\"snarfblat\">";
         for(String[] adventure : ADVENTURE_LIST){
             String adventureNumber = adventure[0];
             String adventureName = adventure[1];
            if(adventureName.length()>40){
                adventureName=adventureName.substring(0, 37)+"...";
             }
             automaticAdventure+="<option value=\""+adventureNumber+"\">"+adventureName+"</option>";
         }
         automaticAdventure+=
             "</select><br/>"+
             "Times: <input type=\"text\" name=\"times\"><br/>"+
             "<input type=\"submit\" value=\"Adventure\" name=\"adventure\">"+
             "</center>";
         return automaticAdventure;
     }
     
     static class AdventureComparator implements Comparator<String[]> {
         @Override
         public int compare(String[] a1, String[] a2) {
             return a1[1].compareTo(a2[1]);
         }
     }
 }
