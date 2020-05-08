 package player;
 
 import grammar.MusicPlayerHeader;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import sound.Song;
 
 /**
  * Main entry point of your application.
  */
 public class Main {
 
     /**
      * Plays the input file using Java MIDI API and displays
      * header information to the standard output stream.
      * 
      * (Your code should not exit the application abnormally using
      * System.exit().)
      * 
      * @param file the name of input abc file
      * @throws FileNotFoundException 
      */
     public static void play(String file) throws FileNotFoundException {
     	File songFile = new File(file);
     	String content = new Scanner(songFile).useDelimiter("\\Z").next();
     	String[] lines = content.split("\\r?\\n");
     	ArrayList<String> header = new ArrayList<String>();
     	ArrayList<String> body = new ArrayList<String>();
     	boolean inHead = true;    	
     	for(String line : lines){
     		if (inHead){
     			header.add(line);
     		}
     		else{
     			body.add(line);
     		}
     		if (line.charAt(0) == 'K'){
     			inHead = false;
     		}
     	}
     	
     	MusicPlayerHeader headPlayer = new MusicPlayerHeader();
    	Song song = headPlayer.parse(join(header,"\n"));
     	System.out.println(song);
     }
 
     public static void main(String[] args) throws FileNotFoundException {
         // CALL play() HERE
    	String file = "sample_abc/waxies_dargle.abc";
     	play(file);
     }
     
     public static String join(ArrayList<String> strings, String separator){
     	StringBuilder newStrings = new StringBuilder();
     	for(String line : strings){
     		newStrings.append(line+separator);
     	}
     	return newStrings.toString();
     }
     
 }
