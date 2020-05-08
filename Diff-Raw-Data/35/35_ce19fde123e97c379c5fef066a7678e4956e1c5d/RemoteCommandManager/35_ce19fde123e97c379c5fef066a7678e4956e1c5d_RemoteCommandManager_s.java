 package net.fobel.android.remoteduino;
 
 import android.content.Context;
 
 import java.util.*;
 import java.io.IOException;
 import java.io.FileNotFoundException;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import net.fobel.android.Serialization;
 
 
 public class RemoteCommandManager {
 	ArrayList<RemoteCommand> commands;
 	Context context;
 	
 	public RemoteCommandManager(Context c) throws IOException {
 		context = c;
     	FileInputStream fis;
     	try {
     		fis = context.openFileInput("remote_codes.dat");
 			byte[] cmd_bytes = Serialization.read_bytes(fis);
 			commands = (ArrayList<RemoteCommand>) Serialization.deserializeObject(cmd_bytes); 
 			fis.close();
     	} catch(FileNotFoundException e) {
 			commands = new ArrayList<RemoteCommand>();
 			save_remote_codes();
     	}
 	}
 	
 	
 	public void add(RemoteCommand cmd) {
 		commands.add(cmd);
 		try {
 			save_remote_codes();
 		} catch(IOException e) {
 			// Ignore failed save...
 		}
 	}
 	
 	
 	public Collection<RemoteCommand> get_commands() {
 		return Collections.unmodifiableList(commands);
 	}
 	
 	
     void save_remote_codes() throws IOException {
 		byte[] cmd_bytes = Serialization.serializeObject(this.commands);
     	FileOutputStream fos;
 		fos = context.openFileOutput("remote_codes.dat", Context.MODE_PRIVATE);
 		fos.write(cmd_bytes);
 		fos.close();
     }
 }
