 package djDemo;
 
 import dj.*;
 import djExceptions.ID3Exception;
 import java.io.*;
 
 public class example {
 
 	public static void main(String[] args) throws FileNotFoundException {
 		
		list(new File("/media/Shared/Music/good music/"));
 	}
 	
 	public static void printInfo(File file)
 	{
 		ID3TagBody tagBody = new ID3TagBody(file);
 		
 		try{
 			tagBody.parse(); 
 		}catch(ID3Exception e){
			System.out.println( file.getAbsolutePath() );
 			e.printStackTrace();
 		}
 		//doesn't have to be in try/catch block; all exceptions thrown are Runtime.
 		
 		ID3Frame titleFrame = tagBody.getFrame("TIT2");
 		ID3Frame albumFrame = tagBody.getFrame("TALB");
 		//returns null if not found
 		
 		String songTitle = "<none>";
 		String songAlbum = "<none>";
 		
 		if(titleFrame != null)
 			songTitle = titleFrame.getText();
 		
 		if(albumFrame != null)
 			songAlbum = albumFrame.getText();
 		
		//System.out.println( songTitle + " -on- " + songAlbum );
 		
 		try{
 			tagBody.close();
 		}catch(ID3Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	public static void list( File f ) throws FileNotFoundException
 	{		
 		if(f.isFile() && f.getName().endsWith(".mp3"))
 			printInfo(f);
 		
 		else if(f.isDirectory())
 			for(File cur: f.listFiles())
 				list(cur);
 	}
 }
