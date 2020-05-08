  /**
  * This is the main class; the user runs the program from here. In addition, this code contains sample of reading from keyboard 
  * and loading from file (parts of the extention). This class contains many methods other than the main method. Inside the
  * main method the user creates instances by calling the methods. 
  */
 import java.io.*;
 import java.util.*;
 public class Main
 {
 /**
  * This is the main class; the user runs the program from here. In addition, this code contains sample of reading from keyboard 
  * and loading from file (parts of the extention). This class contains many methods other than the main method. Inside the
  * main method the user creates instances by calling the methods. 
  */
  
     public static void main(String []args) throws IOException
     {   // Creating ArrayLists to save created instances fo Artist, Track and Album 
         ArrayList<Artist> artists = new ArrayList<Artist>();
         Artist artist = new Artist();
         ArrayList<Track> tracks = new ArrayList<Track>();
         Track track = new Track();
         ArrayList<Album> albums = new ArrayList<Album>();
         Album album = new Album();
         
         //Reading from the keyboard what is the Required 
         BufferedReader buffr = new BufferedReader(new InputStreamReader(System.in));        
         System.out.println("Please Enter: \n1 for using keyboard input \n2 for loading from a file" ); 
         String str = buffr.readLine();
                 
         // Keyboard Input
         if (Integer.parseInt(str) == 1)
         { 
             // An instance from Artist by calling addArtist() method
             artist = addArtist();
             artists.add(artist);
             
             // An instance from Track by  calling addTrack(Artist artist) method
             track = addTrack(artist);
             tracks.add(track);
             
             // An instance from Album by  calling addAlbum(Artist artist, ArrayList<Track> tracks) method
             album = addAlbum(artist, tracks);
             albums.add(album);
         
             // An instance from Library by  calling AddLibrary(ArrayList<Track> tracks, ArrayList<Album> albums) method
             Library library = new Library();
             library = addLibrary(tracks, albums);
         }
         
         //Sample of loading from a file 
          else if (Integer.parseInt(str) == 2)
         {
             BufferedReader reader = new BufferedReader(new FileReader("MusicLib.txt"));
             String line = reader.readLine();
                   //Reading Datafile
            while (line != null)
            {        
                    artist.setIsSoloist(Boolean.parseBoolean(reader.readLine()));                
                    artist.setName(reader.readLine());
                    track.setTitle(reader.readLine());
                    track.setArtist(artist);
                    track.setDate(reader.readLine());
                    track.setLength(Double.parseDouble(reader.readLine()));
                    track.setRating(Integer.parseInt(reader.readLine()));
                    track.setLocation(reader.readLine());
                    track.setSize(Double.parseDouble(reader.readLine()));
                    line = reader.readLine();
                  
                    System.out.println(artist.getName());
                    System.out.println(track.getTitle());
            }  
            reader.close();
         }            
     }
     
     /**
      * addArtist method used to create an instance from Artist class
      * 
      * @return  will return the created object
      */
     public static Artist addArtist()throws IOException
     {
         BufferedReader buffr = new BufferedReader(new InputStreamReader(System.in));        
         String str = "";
           
             Artist artist = new Artist();                                   
             //Determine whether Solo or Band
             System.out.println("Is this artist Soloist? please enter 1 if YES, and 2 if NO");
             String solo = buffr.readLine();
             if (Integer.parseInt(solo) == 1)
             {
                 artist.setIsSoloist(true);
                 System.out.println("Enter the name of the Soloist Artist");
                 str = buffr.readLine();
                 artist.setName(str);
             }
             else if (Integer.parseInt(solo) == 2)                  
             {      
                 artist.setIsSoloist(false);
                 System.out.println("Enter Band Name");
                 str = buffr.readLine();
                 artist.setName(str);     
                 String m = "1";
                 while (Integer.parseInt(m) == 1)
                 {
                     System.out.println("Enter member Name");
                     str = buffr.readLine();
                     artist.storeMember(str);
                 
                     System.out.println("Do you want to add another member? enter 1 if YES, and 2 if NO");
                     m = buffr.readLine();
                 }
                 
             }
             return artist;
         }
         
     /**
      * addTrack method used to create an instance from Track class
      * 
      * @return  will return the created object
      */
             public static Track addTrack(Artist artist) throws IOException
             {
                 BufferedReader buffr = new BufferedReader(new InputStreamReader(System.in));        
                 String str = "";
                 
                 Track track = new Track();
                 System.out.println("Enter Track Title");
                 str = buffr.readLine();
                 track.setTitle(str);
             
                 track.setArtist(artist);
             
                 System.out.println("Enter Track Date");
                 str = buffr.readLine();
                 track.setDate(str);
             
                 System.out.println("Enter Track Length");
                 str = buffr.readLine();
                 track.setLength(Double.parseDouble(str));
             
                 System.out.println("Enter Track Rating");
                 str = buffr.readLine();
                 track.setRating(Integer.parseInt(str));
             
                 System.out.println("Enter Track location");
                 str = buffr.readLine();
                 track.setLocation(str);
             
                 System.out.println("Enter Track Size");
                 str = buffr.readLine();
                 track.setSize(Double.parseDouble(str));
                         
                 track.setGuestArtist(addArtist());
                 
                 return track;
             }
             
        
     /**
      * addAlbum method used to create an instance from Album class
      * 
      * @return  will return the created object
      */     
             public static Album addAlbum(Artist artist, ArrayList<Track> tracks) throws IOException
             {
                 BufferedReader buffr = new BufferedReader(new InputStreamReader(System.in));        
                 String str = "";
                 
                 Album album = new Album();
                 System.out.println("Enter Album Name ");
                 str = buffr.readLine();
                 album.setAlbumName(str);
                             
                 System.out.println("Enter Album Type ");
                 str = buffr.readLine();
                 album.setAlbumType(str);
                 
                 for (Track t:tracks)
                 {
                     album.addTrack(t);
                 }
                 
                 album.setArtist(artist);
                 
                 return album;
             }         
        
     /**
      * addLibrary method used to create an instance from Library class
      * 
      * @return  will return the created object
      */     
              public static Library addLibrary(ArrayList<Track> t, ArrayList<Album> a) throws IOException
              {
                  Library library = new Library();
                  library.setArrayTrack(t);
                  library.setArrayAlbum(a);
                  
                  return library;
              }   
        }    
