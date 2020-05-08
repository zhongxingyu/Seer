 import processing.core.PApplet;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Scanner;
 
 // Class for managing the parsing of the text file
 public class Parser {
        
         //HashMap for storing all dialog.
         //      Key:    (String) S# + E# + CharacterName
         //      Value:  (String) Dialog
         public static HashMap<String, String> ALL_DATA = new HashMap<String, String>();
        
         //ArrayList for storing all characters.
         public static ArrayList<Character> ALL_CHARACTERS = new ArrayList<Character>();
        
         //6 ArrayLists (1 Per Season)
         //Each hold episode information via Episode objects.
         public static ArrayList<Episode> LIST_S1 = new ArrayList<Episode>();
         public static ArrayList<Episode> LIST_S2 = new ArrayList<Episode>();
         public static ArrayList<Episode> LIST_S3 = new ArrayList<Episode>();
         public static ArrayList<Episode> LIST_S4 = new ArrayList<Episode>();
         public static ArrayList<Episode> LIST_S5 = new ArrayList<Episode>();
         public static ArrayList<Episode> LIST_S6 = new ArrayList<Episode>(); 
         
         //1 Arraylist that contains all the season
         public static ArrayList<Episode> LIST_ALL = new ArrayList<Episode>(); 
        
         int episode;    //Current episode number
         int season;             //Current season number
         String hashKey; //HashMap key based on episode and season
         Episode ep;             //Current object to be added to arraylist
        
        
         //Character names to ignore
         ArrayList<String> backgroundChars = new ArrayList<String>();
 
         public void parseBackgroundChars(){
         	//Constructor simply loads unacceptable character names into backgroundChars from file names.txt
         	Scanner scan;    //Scanner for reading file
         	
         	try {
         		scan = new Scanner(new FileReader("data/names.txt"));   //Initialize scanner with file.
 	        } catch (FileNotFoundException e) {
 	                e.printStackTrace();
 	                return;
 	        }
 
 	        //Scan all dialog
 	        while (scan.hasNextLine()) {    //Executes if a line of text exists
 	                backgroundChars.add(scan.nextLine());
 	        }
 	        
 	        scan.close();
         }
 
         public void parseFile(String fileName) {
             Scanner scan = null;    //Scanner for reading file
             ep = null;
            
             try {
                     scan = new Scanner(new FileReader(fileName));   //Initialize scanner with file.
             } catch (FileNotFoundException e) {
                     e.printStackTrace();
                     return;
             }
            
             //Scan first line
             storeLineTitle(scan.nextLine());        //Store title of episode
            
             //Scan all dialog
             while (scan.hasNextLine()) {    //Executes if a line of text exists
                     storeLine(scan.nextLine());     //Sends line to method storeLine()
             }
            
             scan.close();
             updateCharacterEpisodes();      //Add episode to appropriate Character objects.
             updateArray();  //Add episode to appropriate array list
         }
        
         //Sends all Futurama transcripts through the method parseFile()
         public void parseAllTranscripts() {
         	String path;
         	
             //Season 1  	
             parseFile("data/transcripts/SEASON1/S1E1.txt"); //Series 1
             parseFile("data/transcripts/SEASON1/S1E2.txt");
             parseFile("data/transcripts/SEASON1/S1E3.txt");
             parseFile("data/transcripts/SEASON1/S1E4.txt");
             parseFile("data/transcripts/SEASON1/S1E5.txt");
             parseFile("data/transcripts/SEASON1/S1E6.txt");
             parseFile("data/transcripts/SEASON1/S1E7.txt");
             parseFile("data/transcripts/SEASON1/S1E8.txt");
             parseFile("data/transcripts/SEASON1/S1E9.txt");
             parseFile("data/transcripts/SEASON1/S2E1.txt");	//Series 2
             parseFile("data/transcripts/SEASON1/S2E2.txt");
             parseFile("data/transcripts/SEASON1/S2E3.txt");
             parseFile("data/transcripts/SEASON1/S2E4.txt");
            
             //Season 2
             path = "data/transcripts/SEASON2/S2E";	//Series 2, continued
             for(int x=5; x<21; ++x) { //Build path name dynamically.
             	parseFile(path + x + ".txt");	
             }
             parseFile("data/transcripts/SEASON2/S3E1.txt"); //Series 3
             parseFile("data/transcripts/SEASON2/S3E2.txt");
             parseFile("data/transcripts/SEASON2/S3E3.txt");
             
             //Season 3
             path = "data/transcripts/SEASON3/S3E";	//Series 3, continued
             for(int y=4; y<16; ++y) {
             	parseFile(path + y + ".txt");
             }
             
             path = "data/transcripts/SEASON3/S4E";	//Series 4
             for(int z=1; z<12; ++z) {
             	if(z != 4 && z != 5) {	//Season 3 does not include series 4.4 or 4.5
             		parseFile(path + z + ".txt");
             	}
             }
             parseFile("data/transcripts/SEASON3/S5E3.txt");
             
             
             //Season 4
             parseFile("data/transcripts/SEASON4/S4E4.txt");
             parseFile("data/transcripts/SEASON4/S4E5.txt");
             parseFile("data/transcripts/SEASON4/S4E12.txt");
             
             path = "data/transcripts/SEASON4/S5E";
             for(int a=1; a<17; ++a) {
             	if(a != 3) {	//Season 4 does include series 5.3
             		parseFile(path + a + ".txt");
             	}
             }
             
             //Season 5
             path = "data/transcripts/SEASON5/S6E";
             for(int b=1; b<17; ++b) {
             	parseFile(path + b + ".txt");
             }
             
             //Season 6
             path = "data/transcripts/SEASON6/S7E";
             for(int c=1; c<14; ++c) {
             	parseFile(path + c + ".txt");
             }
             
             path = "data/transcripts/SEASON6/S8E";
             for(int d=1; d<14; ++d) {
             	parseFile(path + d + ".txt");
             }
             
             
             //Aggregate all seasons
             LIST_ALL.addAll(LIST_S1);
             LIST_ALL.addAll(LIST_S2);
             LIST_ALL.addAll(LIST_S3);
             LIST_ALL.addAll(LIST_S4);
             LIST_ALL.addAll(LIST_S5);
             LIST_ALL.addAll(LIST_S6);
             
             /*for(int n=0; n<LIST_ALL.size(); ++n) {
             	Episode temp = LIST_ALL.get(n);
             	System.out.println( temp.getSeason() + ": " + temp.getEpisode());
             }*/
         }
        
         //Add episode to appropriate ArrayList
         private void updateArray() {
             switch(season) {
             case 1: LIST_S1.add(ep); return;
             case 2: LIST_S2.add(ep); return;
             case 3: LIST_S3.add(ep); return;
             case 4: LIST_S4.add(ep); return;
             case 5: LIST_S5.add(ep); return;
             case 6: LIST_S6.add(ep); return;
             }
         }
        
         //Store Line of dialog
         private void storeLine(String line) {
                 //Split line so character name and dialog are seperated.
                 String[] splitLine = line.split("\t", 2);
                 
                 if(splitLine.length<2) { return; }
                
                 String character = splitLine[0].toLowerCase();  //Store character name
                 character = updateName(character);	//Check to see if name needs to be changed (eg. "inez" is also = "mrs. wong")
                 
                 String fullKey = hashKey + character;           //Full hash map key includes character name.
                 String value = ALL_DATA.get(fullKey);           //Get current episode dialog for this character
                
                 if(value == null) {
                         value = "";
                 }
                 value += splitLine[1] + "\n";   //Append dialog to any other episode dialog
                 ALL_DATA.put(fullKey, value);
                
                 //Add character to episode (unless is a background character)
                 if(!isBackgroundCharacter(character)){
                         ep.addCharacter(character);
                        
                         //Add character to ArrayList ALL_CHARACTERS (unless already added)
                         if(!isInAllCharacters(character)){
                                 Character chr = new Character(character);      //make a new Character object
                                 ALL_CHARACTERS.add(chr);        //Add Character object to ArrayList
                         }                      
                 }
                
         }
        
         //Store Episode Title
         //Called only for first line when .txt file is opened
         private void storeLineTitle(String line) {
                 //Split line so season #, episode #, episode title are seperated
                String[] splitLine = line.split("\t", 3);
                
                 season = Integer.parseInt(splitLine[0]);        //Store season #
                 episode = Integer.parseInt(splitLine[1]);       //Store episode #
                
                 ep = new Episode(episode, season, splitLine[2]);
                 hashKey = "S" + season + "E" + episode;         //Store current hash map key prefix
         }
        
         //Returns true if character (name String c) is not a reoccurring character.
     //  This does not find all of the non-reocurring characters, but gets rid of the obvious ones.
     private boolean isBackgroundCharacter(String c) {
        
         //Parse every background character name
         for(int x=0; x<backgroundChars.size(); ++x){
                 if(c.startsWith(backgroundChars.get(x))){
                         return true;
                 }              
         }
        
         return false;
     }
        
     //Returns true if character (name String c) is already in ArrayList ALL_CHARATCTERS
     private boolean isInAllCharacters(String c) {
         for(int x=0; x<ALL_CHARACTERS.size(); ++x){
                 if(c.equals(ALL_CHARACTERS.get(x).getName())){
                         return true;
                 }
         }
         return false;
     }
    
     //Add current episode to Character objects' episode lists
     private void updateCharacterEpisodes() {
         //Retrieve all chracters added to the current episode
         ArrayList<String> chars = ep.getChars();        
        
         //Parse characters from current episode
         for(int x=0; x<chars.size(); ++x) {
                 for(int y=0; y<ALL_CHARACTERS.size(); ++y) {
                         if(chars.get(x).equals(ALL_CHARACTERS.get(y).getName())) {
                                 ALL_CHARACTERS.get(y).addEpisode(season, episode);
                                 //System.out.println(chars.get(x) + "\tep# " + episode);
                         }
                 }
                
         }
        
     }
    
     //Parse entire list of Characters (ALL_CHARACTERS).
     //Removes any Characters only in 1 episode.
     //  Note: Meant to be run only ONCE after all episode transcripts are parsed.
     public void filterCharacters() {
         for(int x=0; x<ALL_CHARACTERS.size(); ++x) {
                 if(ALL_CHARACTERS.get(x).getTotalEpisodes() < 2) {
                         ALL_CHARACTERS.remove(x);
                         --x;    //prevents x from incrementing since items have been shifted to the left
                 }              
         }      
     }
    
     //Returns character name with changes, if needed.
     //Allows for one Character object per Character, 
     //	even if they go by multiple names
    public String updateName(String c) {
 	   if(c.charAt(c.length()-1) == ' ') {
		   c = c.substring(0,c.length()-1);	//Remove extra whitespace character if needed.
 	   }
 	   
 	   if(c.equals("inez")){ //Inez is Mrs. Wong's first name
 		   return "mrs. wong";
 	   }
 	   
 	   if(c.equals("leo")){ //Leo is Mr. Wong's first name
 		   return "mr. wong";
 	   }
 	   
 	   if(c.equals("zapp")) {	//Changing Zapp to his full name
 		   return "zapp brannigan";
 	   }
 	   
 	   if(c.equals("farsnworth")){	//This one was likely a typo
 		   return "farnsworth";
 	   }
 	   
 	   if(c.equals("richard nixon's head")) {	//Switch these two arround if you'd rather it the other way, 
 		   c = "nixon";									//but I had to pick one :)
 	   }
 	   
 	   if(c.equals("joey")) {
 		   return "joey mouspad";
 	   }
 	   
 	   return c;
    }
    
 
 }
 
