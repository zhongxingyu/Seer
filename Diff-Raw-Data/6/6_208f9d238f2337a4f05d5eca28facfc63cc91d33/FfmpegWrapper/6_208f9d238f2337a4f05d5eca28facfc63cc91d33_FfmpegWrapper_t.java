 package net.mms_projects.tostream;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class FfmpegWrapper extends Thread {
 
     private static final Pattern FRAME_PATTERN = Pattern.compile(
         "frame(\\s*)=(\\s+)(\\d+)", Pattern.CASE_INSENSITIVE
     );
     private static final Pattern FRAME_RATE_PATTERN = Pattern.compile(
         "fps(\\s*)=(\\s+)(\\d+)", Pattern.CASE_INSENSITIVE
     );
 
 	List<EncoderOutputListener> listeners = new ArrayList<EncoderOutputListener>();
 	Process process;
 	InputStream input;
 	BufferedReader reader;
 	Settings settings;
 
 	public FfmpegWrapper(Settings settings) {
 		super();
 		
 		this.settings = settings;
 	}
 	
 	public void addListener(EncoderOutputListener toAdd) {
 		listeners.add(toAdd);
 	}
 
 	public void startEncoder() throws IOException
 	{
 		ProcessBuilder builder = new ProcessBuilder(compileSettings());
 		builder.redirectErrorStream(true);
 		try {
 			process = builder.start();
 			input = process.getInputStream();
 			reader = new BufferedReader(new InputStreamReader(input));
 		} catch (IOException e) {
 			throw e;
 		}
 	}
 
 	public void stopEncoder()
 	{
 		input  = null;
 		reader = null;
 		process.destroy();
 	}
 
 	@Override
 	public void run() {
 		try {
 			while (true) {
 				String line;
 				if (reader != null) {
 					try {
 						while ((line = reader.readLine()) != null) {
 							Matcher matcher;
 							
 							int framerate = 0;
 							int frame     = 0;
 							matcher = FRAME_RATE_PATTERN.matcher(line);
 							if (matcher.find()) {
 								framerate = Integer.parseInt(matcher.group(3));
 							}
 							matcher = FRAME_PATTERN.matcher(line);
 							if (matcher.find()) {
 								frame = Integer.parseInt(matcher.group(3));
 							}
							
 							for (EncoderOutputListener listener : listeners) {
								if (frame != 0) {
									listener.onStatusUpdate(frame, framerate);
								}
 								listener.onOutput(line + "\n");
 							}
 						}
 					} catch (Exception e) {
 						System.out.println("Error: '" + e.getClass() + "' thrown in the FfmpegWrapper.");
 					}
 				}
 				Thread.sleep(500);
 			}
 		} catch (InterruptedException e) {
 			System.out.println("Child interrupted.");
 		}
 		System.out.println("Exiting child thread.");
 	}
 	
 	public List<String> compileSettings()
 	{
 		List<String> command = new ArrayList<String>();
 		command.add("avconv");
 		command.add("-y");
 		command.add("-f");
 		command.add("x11grab");
 		
 		command.add("-i");
 		command.add(":0.0");
 
 		if (settings.framerate > 5) {
 			command.add("-r");
 			command.add(Integer.toString(settings.framerate));
 		}
 		if (!settings.bitrate.isEmpty()) {
 			command.add("-b");
 			command.add(settings.bitrate);
 			command.add("-minrate");
 			command.add(settings.bitrate);
 			command.add("-maxrate");
 			command.add(settings.bitrate);
 			command.add("-bufsize");
 			command.add(settings.bufferSize);
 		}
 		command.add("bla.flv");
 		
 		return command;
 	}
 
 }
