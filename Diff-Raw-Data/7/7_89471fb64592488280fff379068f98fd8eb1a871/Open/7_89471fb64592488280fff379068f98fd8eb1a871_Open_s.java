 package commandproxy.core.commands;
 
 import java.awt.Desktop;
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.util.Map;
 
 import org.json.JSONObject;
 
 import commandproxy.core.Command;
 import commandproxy.core.CommandException;
 import commandproxy.core.Log;
 import commandproxy.core.Proxy;
 
 /**
  * Opens a file according to the application associated with it. 
  * 
  * This is implemented by using: 
  *   on windows: explorer.exe <file> 
  *   on macos x: open <file>
  *   on linux:   not implemented
  */
 public class Open implements Command{
 	static boolean hasDesktopApi;
 	static{
 		// Check for the desktop class 
 		// included in java 1.6+
 		try{
 			Class.forName( "java.awt.Desktop" );
 			hasDesktopApi = Desktop.isDesktopSupported(); 
 		}
 		catch( ClassNotFoundException ex ){
 			hasDesktopApi = false; 
 		}
 	}
 	
 	public JSONObject execute( Map<String, String> parameters ) throws CommandException{
 		String filename = parameters.get( "file" );
 		
 		File file = Proxy.getFile( filename, parameters ); 
 		
 		if( file == null ){
			throw new CommandException( "Paramter file not set!", this ); 
 		}
 		else{
 			boolean isMailto = filename.startsWith("mailto://");
 			boolean isURL = filename.startsWith("http://");
 			
 			try{
 				Log.debug.println( "Opening " + file.getAbsolutePath() ); 
 				
 				// What's our os?
 				boolean opened = false; 
 				
 				if( hasDesktopApi ){
 					try{
 						
 						if (isMailto)
 						{
							Desktop.getDesktop().mail(new URI(filename));
 						}
 						else if (isURL)
 						{
 							Desktop.getDesktop().browse(new URI(filename));
 						}
 						else
 						{
 							if( !file.exists() ){
 								throw new CommandException( "File does not exist: " + file.getAbsolutePath(), this ); 
 							}
 							
 							Desktop.getDesktop().open( file );
 						}
 						
 						opened = true; 
 					}
 					catch( Exception e ){ 
 					}
 				}
 				
 				if( !opened ){
 					if( System.getProperty("os.name" ).equals(  "Mac OS X" ) ){
 						launchMac( file.getAbsolutePath() ); 
 					}
 					else if( System.getProperties().get( "os.name" ).toString().startsWith(  "Windows" ) ){
 						launchWindows( file.getAbsolutePath() ); 
 					}
 				}
 			}
 			catch( IOException e ){
 				e.printStackTrace(); 
 				throw new CommandException( "File could not be opened", this, e ); 
 			} 
 		}
 		
 		return null;
 	}
 
 	public String getName(){
 		return "open"; 
 	}
 	
 	/**
 	 * Launch on mac... 
 	 * @throws IOException 
 	 */
 	private void launchMac( String filename ) throws IOException{
 		Process p = Runtime.getRuntime().exec( new String[]{
 				"/usr/bin/open", 
 				filename
 		} );
 		
 		try{
 			p.waitFor();
 		}
 		catch( InterruptedException e ){
 			e.printStackTrace();
 		} 
 	}
 	
 	/**
 	 * Launch on windows
 	 * @throws IOException 
 	 */
 	private void launchWindows( String filename ) throws IOException{
 		/*Runtime.getRuntime().exec( new String[]{
 				"cmd.exe",
 				"/Q",
 				"/C", 
 				"start", 
 				filename, 
 				"&&",
 				"exit" 
 		} );*/
 		
 		Process p = Runtime.getRuntime().exec( "cmd.exe /C start \"\" \"" + filename + "\"" );
 		
 		try{
 			p.waitFor();
 		}
 		catch( InterruptedException e ){
 			e.printStackTrace();
 		} 
 		
 	}
 
 }
