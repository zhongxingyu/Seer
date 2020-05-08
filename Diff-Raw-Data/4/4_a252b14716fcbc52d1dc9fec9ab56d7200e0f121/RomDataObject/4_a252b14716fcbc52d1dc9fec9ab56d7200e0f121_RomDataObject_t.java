 package com.lemoulinstudio.gfa.nb.filetype.rom;
 
 import com.lemoulinstudio.gfa.nb.breakpoint.BoolExpr;
 import com.lemoulinstudio.gfa.nb.breakpoint.ParseException;
 import com.lemoulinstudio.gfa.nb.breakpoint.Parser;
 import com.lemoulinstudio.gfa.core.GfaDevice;
 import com.lemoulinstudio.gfa.core.cpu.Arm7Tdmi;
 import com.lemoulinstudio.gfa.core.time.Time;
 import com.lemoulinstudio.gfa.nb.screen.ScreenTopComponent;
 import com.lemoulinstudio.gfa.nb.screen.ScreenTopComponentFactory;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.EnumMap;
 import java.util.List;
 import java.util.Map;
 import org.openide.cookies.OpenCookie;
 import org.openide.filesystems.FileObject;
 import org.openide.loaders.DataObjectExistsException;
 import org.openide.loaders.MultiDataObject;
 import org.openide.nodes.CookieSet;
 import org.openide.nodes.Node;
 import org.openide.util.Exceptions;
 import org.openide.util.Lookup;
 import org.openide.util.lookup.Lookups;
 import org.openide.windows.TopComponent;
 
 public class RomDataObject extends MultiDataObject {
 
   private class OpenSupport implements OpenCookie {
     public void open() {
       TopComponent tc = getTopComponent();
       tc.open();
       tc.requestActive();
     }
   }
 
   public class Resetable implements Node.Cookie {
     public void reset() {
       if (getGfaDeviceState() == GfaDeviceState.Running)
         stoppable.stop();
 
       setGfaDeviceState(GfaDeviceState.Undefined);
       getGfaDevice().reset(skipBios);
       setGfaDeviceState(GfaDeviceState.Stopped);
     }
   }
 
   public class Runnable implements Node.Cookie {
     public void run() {
       // Set the state to Run.
       setGfaDeviceState(GfaDeviceState.Running);
 
       // Run
       cpuRunner = new StoppableRunner() {
         @Override
         public void run() {
           Arm7Tdmi cpu = getGfaDevice().getCpu();
 
           stopRequested = false;
           try {
             while (!stopRequested)
               cpu.step();
           } catch(Exception e) {
             Exceptions.printStackTrace(e);
           }
 
           // Make sure that we remove the breakpoint.
           getGfaDevice().getMemory().clearListeners();
 
           setGfaDeviceState(GfaDeviceState.Stopped);
           cpuRunner = null;
         }
       };
 
       new Thread(cpuRunner).start();
     }
   }
 
   public class Steppable implements Node.Cookie {
     public void step() {
       setGfaDeviceState(GfaDeviceState.Undefined);
       try {getGfaDevice().getCpu().step();}
       catch (Exception e) {Exceptions.printStackTrace(e);}
       setGfaDeviceState(GfaDeviceState.Stopped);
     }
   }
 
   public class StepBackable implements Node.Cookie {
     public void stepBack() {
       try {
         GfaDevice device = getGfaDevice();
 
         long timeToGoBackTo = Math.max(device.getTime().getTime() - 4, 0L);
 
         // Create the scm expression.
         Parser parser = new Parser();
         final BoolExpr breakpointExpr = parser.parse(
                 String.format("(>= (time) %d)", timeToGoBackTo),
                 device.getMemory(), device.getCpu().getRegisters(), device.getTime());
 
         // Reset
         setGfaDeviceState(GfaDeviceState.Undefined);
         device.reset(skipBios);
 
         Arm7Tdmi cpu = device.getCpu();
         try {
           while (!breakpointExpr.evaluation())
             cpu.step();
         }
         catch(Exception e) {Exceptions.printStackTrace(e);}
 
         // Make sure that we remove the breakpoint.
         getGfaDevice().getMemory().clearListeners();
 
         setGfaDeviceState(GfaDeviceState.Stopped);
       }
       catch (ParseException ex) {
         Exceptions.printStackTrace(ex);
       }
     }
   }
 
   public class StepOverable implements Node.Cookie {
     public void stepOver() {
       Arm7Tdmi cpu = getGfaDevice().getCpu();
       debuggable.debug(String.format("(= pc %d)",
               cpu.PC.get() + cpu.getExecutionState().getInstructionSize()));
     }
   }
 
   public class Debuggable implements Node.Cookie {
     public void debug() {
       debug(breakpoint);
     }
 
     public void debug(String breakpoint) {
       try {
         GfaDevice device = getGfaDevice();
 
         // Create the scm expression.
         Parser parser = new Parser();
         final BoolExpr breakpointExpr = parser.parse(breakpoint,
                 device.getMemory(), device.getCpu().getRegisters(), device.getTime());
 
         // Set the state to Run.
         setGfaDeviceState(GfaDeviceState.Running);
 
         // Run
         cpuRunner = new StoppableRunner() {
           @Override
           public void run() {
             Arm7Tdmi cpu = getGfaDevice().getCpu();
 
             stopRequested = false;
             try {
              do {
                 cpu.step();
              }  while (!stopRequested && !breakpointExpr.evaluation());
             }
             catch(Exception e) {Exceptions.printStackTrace(e);}
 
             // Make sure that we remove the breakpoint.
             getGfaDevice().getMemory().clearListeners();
 
             setGfaDeviceState(GfaDeviceState.Stopped);
             cpuRunner = null;
           }
         };
 
         new Thread(cpuRunner).start();
       }
       catch (ParseException ex) {
         Exceptions.printStackTrace(ex);
       }
     }
   }
 
   public class DebugBackable implements Node.Cookie {
     public void debugBack() {
       try {
         GfaDevice device = getGfaDevice();
         Arm7Tdmi cpu = device.getCpu();
 
         // Create the scm expression.
         Parser parser = new Parser();
         final BoolExpr breakpointExpr = parser.parse(breakpoint,
                 device.getMemory(), device.getCpu().getRegisters(), device.getTime());
         
         setGfaDeviceState(GfaDeviceState.Undefined);
 
         // We note the time now.
         long now = cpu.getTime().getTime();
         
         // Reset
         device.reset(skipBios);
 
         // We are looking for the last time where the condition was true.
         long lastTime = -1;
         Time time = cpu.getTime();
         try {
           while (time.getTime() != now) {
             cpu.step();
             if (breakpointExpr.evaluation())
               lastTime = time.getTime();
             breakpointExpr.clearStatus();
           }
         }
         catch(Exception e) {Exceptions.printStackTrace(e);}
 
         // If we found one, we come back to it.
         if (lastTime != -1) {
           // Reset
           device.reset(skipBios);
 
           while (time.getTime() != lastTime)
             cpu.step();
         }
 
         // Make sure that we remove the breakpoint.
         getGfaDevice().getMemory().clearListeners();
 
         setGfaDeviceState(GfaDeviceState.Stopped);
       }
       catch (ParseException ex) {
         Exceptions.printStackTrace(ex);
       }
     }
   }
 
   public class Stoppable implements Node.Cookie {
     public void stop() {
       StoppableRunner runner = cpuRunner; // avoids concurrency issues.
       if (runner != null)
         runner.requestStop();
     }
   }
 
   public class StoppedState implements Node.Cookie {
     public RomDataObject getRomDataObject() {
       return RomDataObject.this;
     }
   }
   
   public enum GfaDeviceState {
     Undefined,
     Stopped,
     Running
   }
 
   private Resetable     resetable     = new Resetable();
   private DebugBackable debugBackable = new DebugBackable();
   private StepBackable  stepBackable  = new StepBackable();
   private Stoppable     stoppable     = new Stoppable();
   private Steppable     steppable     = new Steppable();
   private StepOverable  stepOverable  = new StepOverable();
   private Debuggable    debuggable    = new Debuggable();
   private Runnable      runnable      = new Runnable();
   
   private StoppedState  stoppedState = new StoppedState();
 
   private String breakpoint = "";
   private boolean skipBios = true;
 
   private GfaDeviceState gfaDeviceState = GfaDeviceState.Undefined;
   private StoppableRunner cpuRunner = null;
   
   private Map<GfaDeviceState, List<Node.Cookie>> stateToCookies =
           new EnumMap<GfaDeviceState, List<Node.Cookie>>(GfaDeviceState.class);
           //new HashMap<GfaDeviceState, List<Node.Cookie>>();
 
   private ScreenTopComponent screenTopComponent;
 
   public RomDataObject(FileObject pf, RomDataLoader loader) throws DataObjectExistsException, IOException {
     super(pf, loader);
 
     CookieSet cookies = getCookieSet();
     cookies.add(this);
     cookies.add(new OpenSupport());
 
     stateToCookies.put(GfaDeviceState.Undefined, Collections.<Node.Cookie>emptyList());
     stateToCookies.put(GfaDeviceState.Stopped, Arrays.<Node.Cookie>asList(stoppedState, resetable, debugBackable, stepBackable, steppable, stepOverable, debuggable, runnable));
     stateToCookies.put(GfaDeviceState.Running, Arrays.<Node.Cookie>asList(resetable, stoppable));
 
     setGfaDeviceState(GfaDeviceState.Undefined);
   }
 
   @Override
   protected Node createNodeDelegate() {
     return new RomDataNode(this, getLookup());
   }
 
   @Override
   public Lookup getLookup() {
     return getCookieSet().getLookup();
   }
 
   private TopComponent getTopComponent() {
     if (screenTopComponent == null) {
       // Todo: Document this hook.
       ScreenTopComponentFactory factory =
               Lookups.forPath("Gfa/ScreenTopComponentFactory")
               .lookup(ScreenTopComponentFactory.class);
       screenTopComponent = factory.createScreenTopComponent(this);
     }
 
     return screenTopComponent;
   }
 
   public GfaDeviceState getGfaDeviceState() {
     return gfaDeviceState;
   }
 
   public final synchronized void setGfaDeviceState(GfaDeviceState gfaDeviceState) {
     List<Node.Cookie> cookiesBefore = stateToCookies.get(this.gfaDeviceState);
     List<Node.Cookie> cookiesAfter = stateToCookies.get(gfaDeviceState);
 
     CookieSet cookieSet = getCookieSet();
 
     for (Node.Cookie cookie : cookiesBefore)
       if (!cookiesAfter.contains(cookie))
         cookieSet.remove(cookie);
     
     this.gfaDeviceState = gfaDeviceState;
 
     for (Node.Cookie cookie : cookiesAfter)
       if (!cookiesBefore.contains(cookie))
         cookieSet.add(cookie);
   }
 
   private GfaDevice gfaDevice;
 
   public boolean isDeviceCreated() {
     return gfaDevice != null;
   }
 
   private synchronized void ensureDeviceCreated() {
     if (gfaDevice == null) {
       // Create the device.
       gfaDevice = new GfaDevice();
 
       // Reset the device.
       gfaDevice.reset(skipBios);
 
       // Load the bios.
       gfaDevice.getMemory().loadBios("roms/bios.gba");
 
       // Load the rom.
       try {gfaDevice.getMemory().loadRom(getPrimaryFile().getInputStream(), getPrimaryFile().getSize());}
       catch (FileNotFoundException e) {}
       catch (OutOfMemoryError oome) {} // tmp fix
 
       // Set the state of the device.
       setGfaDeviceState(GfaDeviceState.Stopped);
     }
   }
 
   public GfaDevice getGfaDevice() {
     ensureDeviceCreated();
     return gfaDevice;
   }
 
   public synchronized void releaseResources() {
     // Stop the device if needed.
     if (getGfaDeviceState() == GfaDeviceState.Running)
       stoppable.stop();
 
     // Set state to undefined, so that no unwanted cookies are left in the lookup.
     setGfaDeviceState(GfaDeviceState.Undefined);
 
     // Release the reference to the device.
     gfaDevice = null;
 
     // Release the reference to the top component.
     screenTopComponent = null;
   }
 
   public String getBreakpoint() {
     return breakpoint;
   }
 
   public void setBreakpoint(String breakpoint) {
     this.breakpoint = breakpoint;
   }
 
   public boolean isSkipBios() {
     return skipBios;
   }
 
   public void setSkipBios(boolean skipBios) {
     this.skipBios = skipBios;
   }
 
 }
