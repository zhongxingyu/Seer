 /***
  **
  ** This library is free software; you can redistribute it and/or
  ** modify it under the terms of the GNU Lesser General Public
  ** License as published by the Free Software Foundation; either
  ** version 2.1 of the License, or (at your option) any later version.
  **
  ** This library is distributed in the hope that it will be useful,
  ** but WITHOUT ANY WARRANTY; without even the implied warranty of
  ** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  ** Lesser General Public License for more details.
  **
  ** You should have received a copy of the GNU Lesser General Public
  ** License along with this library; if not, write to the Free Software
  ** Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
  **
  **/
 package org.soundboard.server;
 
 import java.util.*;
 import javax.sound.sampled.*;
 import org.soundboard.audio.*;
 import org.soundboard.library.*;
 import org.soundboard.server.command.*;
 import org.soundboard.server.inputservice.*;
 import org.soundboard.util.*;
 
 
 public class Server implements Stoppable {
    
    protected static boolean running = true;
    private String configFileLocation = "./soundboard.properties";
    List<SoundLibrary> libraries = null;
    private static List<InputService> inputServices = null;
    Object monitor = new Object();
    Thread mainThread;
    
    /**
     * Constructor
     */
    public Server() {
       mainThread = Thread.currentThread();
       mainThread.setName("Server");
    }
    
    @Override public boolean isRunning() {
       return running;
    }
    
    @Override public void stopRunning() {
       running = false;
    }
    
    /**
     * set the location of the config file
     */
    public void setConfigFileLocation(String configFileLocation) {
       this.configFileLocation = configFileLocation;
    }
    
    /**
     * Get the Audio Playback Device with the given name.  If the device does not support playback, null is returned
     */
    public Mixer getAudioPlaybackDevice(String deviceName) {
       Mixer.Info[] mixerInfoData = AudioSystem.getMixerInfo();
       try {
          AudioFormat testFormat = new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, (float)11025.0, 8, 1, 1, (float)11025.0, false);
          DataLine.Info info = new DataLine.Info(SourceDataLine.class, testFormat);
          if (mixerInfoData != null) {
             for (Mixer.Info mixerInfo : mixerInfoData) {
                Mixer mixer = AudioSystem.getMixer(mixerInfo);
                if (mixer.isLineSupported(info)) {
                   if (deviceName == null || mixerInfo.getName().equals(deviceName)) {
                      SourceDataLine dataline = null;
                      try {
                         dataline = (SourceDataLine)mixer.getLine(info);
                         dataline.open(testFormat);
                         dataline.start();
                      } catch (Exception e) {
                         if (dataline != null) {
                            try { dataline.close(); } catch (Exception ignored) {}
                         }
                         LoggingService.getInstance().log(" -- Unsupported Audio device: "+ mixerInfo.getName() + " Couldn't get line.info");
                         continue;
                      }
                      LoggingService.getInstance().log("Using Audio device: "+ mixerInfo.getName());
                      return mixer;
                   } else {
                      LoggingService.getInstance().log(" -- Undesired Audio device: "+ mixerInfo.getName());
                   }
                } else {
                   LoggingService.getInstance().log(" -- Unsupported Audio device: "+ mixerInfo.getName());
                }
             }
          }
       } catch (Exception e) {
          LoggingService.getInstance().log("ERROR trying to get Audio device " + e.getMessage());
       }
       LoggingService.getInstance().log("ERROR trying to get Audio device.  Could not find a supported device, or the desired device doesn't work.");
       System.exit(1);
       return null;
    }
    
    /**
     * get an instance of the input service of the given class name
     */
    public static InputService getInputService(String inputServiceClassName) {
       if (inputServices != null) {
          for (InputService service : inputServices) {
             if (service.getClass().getName().equals(inputServiceClassName)) {
                return service;
             }
          }
       }
       return null;
    }
    
    /**
     * setup library
     */
    private void setupLibraries() {
       String[] libraryNames = SoundboardConfiguration.config().getMultiValueProperty(SoundboardConfiguration.LIBRARY, SoundboardConfiguration.NAMES);
       if (libraryNames != null) {
          libraries = new ArrayList<SoundLibrary>();
          for (String libraryName : libraryNames) {
             String libraryFileSource = SoundboardConfiguration.config().getProperty(SoundboardConfiguration.LIBRARY, libraryName, SoundboardConfiguration.SOURCE);
             if (libraryFileSource != null) {
                SoundLibrary library = SoundLibrary.getInstance(libraryName);
                library.registerListener(libraryFileSource);
                libraries.add(library);
             }
          }
       }
       
       //add the default library
       String libraryFileSource = SoundboardConfiguration.config().getProperty(SoundboardConfiguration.LIBRARY, SoundLibrary.DEFAULT_LIBRARY, SoundboardConfiguration.SOURCE);
       if (libraryFileSource != null) {
          SoundLibrary library = SoundLibrary.getInstance();
          library.registerListener(libraryFileSource);
          libraries.add(library);
       }
    }
    
    /**
     * setup library
     */
    private void setupInputServices() {
       String[] inputServiceNames = SoundboardConfiguration.config().getMultiValueProperty(SoundboardConfiguration.INPUT, SoundboardConfiguration.NAMES);
       if (inputServiceNames != null) {
          inputServices = new ArrayList<InputService>();
          for (String serviceName : inputServiceNames) {
             InputService inputService = (InputService)SoundboardConfiguration.config().getClassProperty(SoundboardConfiguration.INPUT, serviceName, SoundboardConfiguration.CLASS_NAME);
             if (inputService != null) {
                System.out.print("Attempting to start input service: " + inputService.getServiceName() + "...");
                boolean startedOk = inputService.initialize();
                if (startedOk) {
                   inputServices.add(inputService);
                   inputService.start();
                   LoggingService.getInstance().registerInputService(inputService);
                   System.out.println("[OK]");
                } else {
                   System.out.println("[FAILED]");
                }
             }
          }
       }
    }
    
    /**
     * run the server
     */
    public void run() {
       Runtime.getRuntime().addShutdownHook(
          new Thread("Shutdown Hook") {
             @Override
             public void run() {
                doShutdown();
             }
          }
       );
       try {
          LoggingService.getInstance().serverLog("Starting Soundboard");
          
          SoundboardConfiguration.create(this.configFileLocation);
          
          LoggingService.getInstance().setupDefence(SoundboardConfiguration.config().getMultiValueProperty(LoggingService.INTERLOPERS), SoundboardConfiguration.config().getMultiValueProperty(LoggingService.FAKE_USERS));
          
          CommandHandler.registerCommands();
          
          //add the shutdown command
         CommandHandler.register("shutdown", new ShutdownCommand(SoundboardConfiguration.config().getProperty(SoundboardConfiguration.SHUTDOWN_PASSWORD)));
          
          //setup and kick off threads
          SoundPlayer player = SoundPlayer.getInstance();
          player.initialize(getAudioPlaybackDevice(SoundboardConfiguration.config().getProperty(SoundboardConfiguration.AUDIO_PLAYBACK_DEVICE)));
          
          setupLibraries();
          
          CronService.startService();
          Karma.getInstance().initialize(SoundboardConfiguration.config());
          
          setupInputServices();
          
          if (!Boolean.FALSE.toString().equals(SoundboardConfiguration.config().getProperty(SoundboardConfiguration.IS_CLIENT))) {
             User user = new User(SoundboardConfiguration.config().getProperty(SoundboardConfiguration.SERVER, SoundboardConfiguration.USERID), getInputService(SoundboardConfiguration.config().getProperty(SoundboardConfiguration.SERVER, SoundboardConfiguration.INPUT)));
             if (!user.isNull()) {
                LoggingService.getInstance().setClient(user, SoundboardConfiguration.config().getProperty(SoundboardConfiguration.SERVER, SoundboardConfiguration.COMMAND));
             }
          }
          LoggingService.getInstance().readObject();
 
          //setup the auto save
          int autosaveInterval = SoundboardConfiguration.config().getIntProperty(SoundboardConfiguration.AUTOSAVE_INTERVAL);
          new AutoSave(autosaveInterval).start();
          try {
             synchronized (monitor) {
                while (running) {
                   monitor.wait();
                }
             }
          } catch (InterruptedException e) {
             //ignore -- this is how it shuts down.
          }
          
       } catch (IllegalStateException e) {
          e.printStackTrace();
       }
    }
    
    protected class AutoSave extends Thread {
       long interval;
       public AutoSave(long interval) {
          this.setName("Auto-Save");
          if (interval < 60000) {
             interval = 0;
          }
          this.interval = interval;
       }
       @Override
       public void run() {
          LoggingService.getInstance().serverLog("Starting auto-save service.");
          boolean first = true;
          try {
             while (this.interval > 0 && isRunning()) {
                if (!first) {
                   LoggingService.getInstance().serverLog("Auto-saving.");
                   LoggingService.getInstance().writeObject();
                   Statistics.writeObject();
                   History.writeObject();
                   CronService.writeObject();
                }
                first = false;
                Thread.sleep(interval);
             }
          } catch (InterruptedException e) {
             LoggingService.getInstance().serverLog("Stopping AutoSave service.");
          }
       }
    }
    
    /**
     * perform the shutdown procedure
     */
    private void doShutdown() {
       LoggingService.getInstance().serverLog("\nShutting down...");
       stopRunning();
       LoggingService.getInstance().writeObject();
       Statistics.writeObject();
       History.writeObject();
       CronService.writeObject();
       
       //stop all active libraries
       if (libraries != null) {
          for (SoundLibrary library : libraries) {
             library.stopActiveLibrary();
          }
       }
       //stop services
       if (inputServices != null) {
          for (InputService inputService : inputServices) {
             inputService.stopRunning();
          }
       }
       try {
          mainThread.interrupt();
       } catch (Exception e) {
          LoggingService.getInstance().serverLog("Error releasing the server: ");
          e.printStackTrace(LoggingService.getInstance().getServerLog());
       }
    }
 
    /**
     * @param args
     */
    public static void main(String[] args) {
       Server server = new Server();
       if (args.length > 0) {
          server.setConfigFileLocation(args[0]);
       }
       server.run();
       System.exit(1);
    }
    
    public class ShutdownCommand extends Command {
       private final String password;
       public ShutdownCommand(String password) {
          this.password = password;
       }
       
       /**
        * get the command description
        */
       @Override public String getDescription() {
          return "Shutdown the soundboard.";
       }
       
       @Override
       public boolean isHttpCommand() {
          return false;
       }
       
       @Override
       public String execute(InputService inputService, String who, String[] args, boolean isCron, boolean respondWithHtml) {
         if (password == null || password.equals(args[0])) {
             doShutdown();
             return "shutting down";
          } else {
             return "Not permitted";
          }
       }
       
    }
    
 }
