 package fm.audiobox.sync;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Arrays;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import fm.audiobox.util.ThreadItem;
 
 public class MD5Converter extends ThreadItem{
 	
 	private File _file = null;
 	private static final int CHUNK = 8192;
 	private String result = null;
 	
 	private static Log log = LogFactory.getLog( MD5Converter.class );
 	
 	public MD5Converter(File file){
 		this._file = file;
 	}
 	
 	
 	@Override
 	protected synchronized void _run() {
 		
 		try {
 			MessageDigest digest = MessageDigest.getInstance("MD5");
 			
 			FileInputStream fis = new FileInputStream( this._file );
 			long file_length = this._file.length();
 			long completed = 0;
 			
 			while ( true ){
 				
 				byte[] bytes = new byte[ CHUNK ];
 				
				int read = -1;


        if ( ( read = fis.read( bytes ) ) <= 0 ) break;
 				
 				if ( read < CHUNK )
 					bytes = Arrays.copyOf( bytes , read);
 				
 				digest.update(bytes , 0 ,read );
 				
 				completed += read;
 				this.getThreadListener().onProgress( this , file_length , completed, file_length - completed, this._file );
 
 
 				if ( read < CHUNK )
 					break;
 			}
 			
 			byte[] bytes = digest.digest();
 			this.result = "";
 			for ( byte _byte : bytes ){
 				this.result += Integer.toHexString(0xFF & _byte);
 			}
 			
 		} catch ( NoSuchAlgorithmException nsae ) {
 			log.error( nsae );
 		} catch ( FileNotFoundException fnfe ) {
 			log.error( fnfe );
 		} catch ( IOException ioe ) {
 			log.error( ioe );
 		}
 		
 	}
 	
 	
 	public synchronized String digest(){
 		this.start();
 		this._run();
 		return this.end();
 	}
 	
 	
 	@Override
 	protected synchronized String end() {
 		return this.result.toLowerCase();
 	}
 
 	@Override
 	protected synchronized void start() {
 		
 	}
 	
 	
 }
