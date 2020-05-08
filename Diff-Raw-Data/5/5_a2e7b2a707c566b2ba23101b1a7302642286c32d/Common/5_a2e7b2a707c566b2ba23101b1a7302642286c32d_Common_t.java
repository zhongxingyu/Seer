 package cubetech.common;
 
 import cubetech.gfx.ResourceManager;
 import cubetech.misc.Event;
 import cubetech.misc.ExitException;
 import cubetech.misc.FrameException;
 import cubetech.misc.Ref;
 import cubetech.net.Packet;
 import java.applet.Applet;
 import java.awt.Canvas;
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.EnumSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JOptionPane;
 import org.lwjgl.Sys;
 import org.lwjgl.opengl.Display;
 
 /**
  *
  * @author mads
  */
 public class Common {
     // Constants
     public static final int MAX_GENTITYBITS= 12;
     public static final int MAX_GENTITIES = (1 << MAX_GENTITYBITS);
     public static final int ENTITYNUM_NONE = MAX_GENTITIES - 1;
     public static final int ENTITYNUM_WORLD = MAX_GENTITIES - 2;
     public static final int ENTITYNUM_MAX_NORMAL = MAX_GENTITIES - 2;
     public static final int MAX_PS_EVENTS = 2;
     public static final int EVENT_VALID_MSEC = 300;
     public static final int EV_EVENT_BIT1 = 0x00000100;
     public static final int EV_EVENT_BIT2 = 0x00000200;
     public static final int EV_EVENT_BITS = EV_EVENT_BIT1 | EV_EVENT_BIT2;
     public static final int DEFAULT_GRAVITY = 800;
 
     private static final String DEFAULT_CFG = "config.cfg";
     
 
     // Cvars
     public CVar sv_running; // current server status
     public CVar cl_running; // current client status
     public CVar cl_paused;
     public CVar sv_paused;
     public CVar maxfps; // cap framerate
     public CVar errorMessage; // Will be set when an error occurs
     public CVar developer;
 
     public CVar com_timer; // 1: LWJGLs timer, 2: Javas nano-seconds
     public CVar com_sleepy; // Enables thread sleeping when not running vsync
     
     public CVar com_sleepPrecision; // sleep and yield precision can vary
     public CVar com_yieldPrecision; // for different platforms/computers
 
     private CVar com_unfocused;
     private CVar com_maxfpsUnfocused;
     public CVar com_timescale;
     private CVar com_abnormalExit;
 
     public int frametime; // the time this frame
     public ItemList items = new ItemList();
     
     private int lasttime; // the time last frame
     private int framemsec; // delta time between frames
     private boolean useSysTimer = true; // Controls the current timer. com_timer sets this.
     private Event tempevt = new Event();
 
     
 
     public enum ErrorCode {
         FATAL, // exit the entire game with a popup window
         DROP, // print to console and disconnect from game
         SERVERDISCONNECT, // don't kill server
         DISCONNECT // client disconnected from the server
     }
 
     public void Init() {
         lasttime = Milliseconds();
         // Set up cvars
         maxfps = Ref.cvars.Get("maxfps", "100", EnumSet.of(CVarFlags.ARCHIVE));
         developer = Ref.cvars.Get("developer", "0", EnumSet.of(CVarFlags.ARCHIVE));
         cl_running = Ref.cvars.Get("cl_running", "0", EnumSet.of(CVarFlags.ROM));
         sv_running = Ref.cvars.Get("sv_running", "0", EnumSet.of(CVarFlags.ROM));
         cl_paused = Ref.cvars.Get("cl_paused", "0", EnumSet.of(CVarFlags.ROM));
         sv_paused = Ref.cvars.Get("sv_paused", "0", EnumSet.of(CVarFlags.ROM));
         errorMessage = Ref.cvars.Get("errorMessage", "", EnumSet.of(CVarFlags.ROM));
         com_sleepy = Ref.cvars.Get("com_sleepy", "0", EnumSet.of(CVarFlags.TEMP));
         com_sleepPrecision = Ref.cvars.Get("com_sleepPrecision", "4", EnumSet.of(CVarFlags.TEMP));
         com_yieldPrecision = Ref.cvars.Get("com_yieldPrecision", "1", EnumSet.of(CVarFlags.TEMP));
         com_timer = Ref.cvars.Get("com_timer", "2", EnumSet.of(CVarFlags.TEMP));
         com_unfocused = Ref.cvars.Get("com_unfocused", "0", EnumSet.of(CVarFlags.ROM));
         com_timescale = Ref.cvars.Get("timescale", "1", EnumSet.of(CVarFlags.CHEAT, CVarFlags.SYSTEM_INFO));
         com_maxfpsUnfocused = Ref.cvars.Get("com_maxfpsUnfocused", "30", EnumSet.of(CVarFlags.ARCHIVE));
         com_abnormalExit = Ref.cvars.Get("com_abnormalExit", "0", EnumSet.of(CVarFlags.ROM));
         com_timer.Max = 2;
         com_timer.Min = 1;
         useSysTimer = com_timer.iValue == 1;
         errorMessage.modified = false;
         // Init client and server
         Ref.server.Init();
         Ref.client.Init();
     }
 
     // Where the program starts
     public static void Startup(Canvas parentDisplay, Applet applet) {
         // Init
         try {
             Ref.InitRef();
 
             Ref.glRef.InitWindow(parentDisplay, applet);
             Ref.Input.Init(); // Initialize mouse and keyboard
             Ref.common.Init();
         } catch (Exception ex) {
             String exString = getExceptionString(ex);
             System.out.println("Fatal crash: " + exString);
 
             if(Ref.glRef != null && Ref.glRef.isApplet())
                RunBrowserURL("javascript:handleError(\"CubeTech suffered a fatal crash :(\\r\\nCrashlog: " + exString + "\")");
             else
                 JOptionPane.showMessageDialog(parentDisplay, "CubeTech suffered a fatal crash :(\nCrashlog: " + exString, "Fatal Crash", JOptionPane.ERROR_MESSAGE);
 
             if(Ref.glRef != null)
                 Ref.glRef.Destroy();
             Display.destroy();
             System.exit(-1);
         }
 
         Ref.cvars.modifiedFlags.remove(CVarFlags.ARCHIVE);
         
         // Run
         try {
             while (!Display.isCloseRequested()) {
                 Ref.common.Frame();
             }
         } catch(ExitException ex) {
         }
 
         // Clean up
         try {
             // Allow the client to send a disconnect command, if appropiate
             Ref.common.Error(ErrorCode.DROP, "Client quit");
         } catch (Exception e) {}
 
         Display.destroy();
         System.exit(0);
     }
 
     public void Frame() {
         if(com_timer.modified) {
             useSysTimer = com_timer.iValue == 1;
             lasttime = Milliseconds(); // Different timers might use different timebases
             com_timer.modified = false;
         }
 
 //        WriteConfiguration();
 
         // Cap framerate
         int minMsec;
         if(com_unfocused.iValue == 1 && com_maxfpsUnfocused.iValue > 0) {
             minMsec = 1000 / com_maxfpsUnfocused.iValue;
         } else if(maxfps.iValue > 0) {
             minMsec = 1000 / maxfps.iValue;
         } else
             minMsec = 1;
         
         int msec = minMsec;
         
         boolean sleepy = (com_sleepy.iValue == 1 && Ref.cvars.Find("r_vsync").iValue != 1) || com_unfocused.iValue == 1;
         try {
             do {
                 // Sleepy frees up the CPU when not running vsync.
                 if(sleepy) {
                     int remaining = minMsec - msec;
                     try {
                         if(remaining >= com_sleepPrecision.iValue)
                             Thread.sleep(1);
                         else if(remaining >= com_yieldPrecision.iValue)
                             Thread.sleep(0);
                     } catch (InterruptedException ex) {
                         System.out.println(ex);
                     }
                 }
                 frametime = EventLoop(); // Handle packets while we wait
                 if(lasttime > frametime)
                     lasttime = frametime;
                 msec = frametime - lasttime;
             } while(msec < minMsec);
         
             Ref.commands.Execute(); // Pump commands
             lasttime = frametime;
             framemsec = msec;
 
             msec = ModifyMsec(msec);
             
 
         
             // Server Frame
             Ref.server.Frame(msec);
 
             // Allow server packets to arrive instantly if running server
             EventLoop();
             Ref.commands.Execute(); // Pump commands
 
             // Client Frame
             Ref.client.Frame(msec);
         } catch(FrameException ex) {
             // FrameException is a special RuntimeException that only has
             // one purpose - to exit the current frame.
             return;
         }
     }
 
     public static void LogDebug(String str) {
         if(Ref.common.isDeveloper())
             System.out.println("[D] " + str);
     }
 
     public static void LogDebug(String str, Object... args) {
         if(Ref.common.isDeveloper())
             System.out.println("[D] " + String.format(str, args));
     }
 
     public static void Log(String str) {
         System.out.println(str);
     }
 
     public static void Log(String str, Object... args) {
         System.out.println(String.format(str, args));
     }
 
     public static void RunBrowserURL(String str) {
         if(Ref.glRef != null && Ref.glRef.isApplet())
         try {
             Ref.glRef.getApplet().getAppletContext().showDocument(new URL(str));
         } catch (MalformedURLException ex) {
             Logger.getLogger(Common.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public static String getStackTraceString(StackTraceElement[] elem) {
         StringBuilder bld = new StringBuilder();
         bld.append("Stack Trace:\n");
         for (Object object : elem) {
             bld.append('\t');
             bld.append(object.toString());
             bld.append('\n');
         }
 
         return bld.toString();
     }
 
     public static String getExceptionString(Exception ex) {
         return ex.getLocalizedMessage() + "\n" + getStackTraceString(ex.getStackTrace());
     }
 
     public void HunkClear() {
         Ref.client.ShutdownCGame();
         Ref.server.ShutdownGameProgs();
     }
 
     public void Error(ErrorCode code, String str) {
         Ref.cvars.Set2("errorMessage", str, true);
         String shortStr = str;
         String[] lines = str.split("\n");
         if(lines.length > 5)
             shortStr = lines[0] + '\n' + lines[1] + '\n' + lines[2] + '\n' + lines[3] + '\n' + lines[4];
         System.err.println("Error: " + str);
         if(code == ErrorCode.DISCONNECT || code == ErrorCode.SERVERDISCONNECT) {
             Ref.server.Shutdown("Server disconnected.");
             Ref.client.Disconnect(true);
             Ref.client.FlushMemory();
             throw new FrameException(str);
         } else if(code == ErrorCode.DROP) {
             Ref.server.Shutdown("Server shutdown: " + shortStr);
             Ref.client.Disconnect(true);
             Ref.client.FlushMemory();
             throw new FrameException(str);
         } else {
             // Fatal
             System.out.println("Fatal crash");
             
             if(Ref.glRef != null)
                 Ref.glRef.Destroy();
             
             if(Ref.glRef != null && Ref.glRef.isApplet())
                RunBrowserURL("javascript:handleError(\"CubeTech suffered a fatal crash :(\\r\\nCrashlog: " + str + "\")");
             else {
                 JOptionPane.showMessageDialog(null, "CubeTech suffered a fatal crash :(\nCrashlog: " + str, "Fatal Crash", JOptionPane.ERROR_MESSAGE);
             }
             Shutdown();
         }
         
     }
 
     // Writes the config if anything has changed and not running applet
     public void WriteConfiguration(boolean force) {
         if(Ref.glRef == null || Ref.glRef.isApplet())
             return;
 
         if(!Ref.cvars.modifiedFlags.contains(CVarFlags.ARCHIVE) && !force)
             return;
 
         Ref.cvars.modifiedFlags.remove(CVarFlags.ARCHIVE);
 
         WriteConfigToFile(DEFAULT_CFG);
     }
 
     private int ModifyMsec(int msec) {
         if(com_timescale.fValue != 0) {
             msec = (int)(com_timescale.fValue*msec);
             if(msec < 1)
                 msec = 1;
         }
 
         int clamptime;
         if(sv_running.iValue == 0)
             clamptime = 5000;
         else
             clamptime = 200;
 
         if(msec > clamptime)
             msec = clamptime;
 
         return msec;
     }
 
     private void WriteConfigToFile(String str) {
         StringBuilder dst = new StringBuilder();
         dst.append("// Generated by Cubetech, do not modify\r\n");
         Ref.Input.binds.WriteBinds(dst);
         Ref.cvars.WriteCVars(dst);
         try {
             ResourceManager.SaveStringToFile(str, dst.toString());
         } catch (IOException ex) {
             Common.LogDebug(getExceptionString(ex));
         }
     }
 
     public void Shutdown() {
         throw new ExitException("Shutdown");
     }
 
     public boolean isDeveloper() {
         return developer.iValue == 1;
     }
 
     
 
     // This pumps the network system and hands off packets to the client and server
     // to handle. When there is no more packets, it returns the time.
     public int EventLoop() {
         Event ev;
         
         while(true) {
             Ref.net.PumpNet();
             ev = GetEvent();
 
             if(ev.Type == Event.EventType.NONE)
                 return ev.Time;
 
             switch(ev.Type) {
                 case NONE:
                     break;
                 case PACKET:
                     // packet value2: 0 for server, 1 for client
                     if(ev.Value2 == 0) {
                         if(sv_running.iValue == 1)
                             Ref.server.PacketEvent((Packet)ev.data);
                     } else {
 
                         Ref.client.PacketEvent((Packet)ev.data);
                     }
                     break;
             }
         }
     }
 
     Event GetEvent() {
         // Try to get packet
         Packet packet = Ref.net.GetPacket();
         if(packet != null) {
             // We have a packet!
             return CreateEvent(packet.Time, Event.EventType.PACKET, 0, packet.type==Packet.SourceType.CLIENT?1:0, packet);
         }
 
         // Return an empty event
         Event evt = tempevt;
         evt.Time = Milliseconds();
         evt.Type = Event.EventType.NONE;
         return evt;
     }
     
     
     Event CreateEvent(int time, Event.EventType type, int value, int value2, Object data) {
         Event evt = tempevt;
         if(time == 0)
             time = Milliseconds();
 
         evt.Time = time;
         evt.Type = type;
         evt.Value = value;
         evt.Value2 = value2;
         evt.data = data;
 
         return evt;
     }
 
     // Get current time in milliseconds.
     public int Milliseconds() {
         if(useSysTimer) {
             return (int)((Sys.getTime()*1000)/Sys.getTimerResolution());
         } else {
             return (int)(System.nanoTime() / 1000000);
         }
     }
 
     
 }
