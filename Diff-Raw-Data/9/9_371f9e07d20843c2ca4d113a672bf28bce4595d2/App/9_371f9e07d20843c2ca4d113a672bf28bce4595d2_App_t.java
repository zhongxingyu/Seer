 package project.senior.app.pormmo;
 
 import com.sun.jna.NativeLibrary;
 import java.io.File;
 import uk.co.caprica.vlcj.runtime.RuntimeUtil;
 
 /**
  * @author John Fisher, Shelby Copeland, Max De Benedetti
  */
 public class App 
 {
   private static final String vlcPath = "\\VideoLAN\\VLC\\";
   private static final int JVM_BIT = Integer.parseInt(System.getProperty(
                                                         "sun.arch.data.model"));
   
   public static void main(String[] args) 
   {
       
     if (System.getProperty("os.name").startsWith("Windows")) 
     {         
      if(System.getenv("ProgramFiles(x86)") != null)
       {
         String osArch = System.getenv("ProgramFiles(x86)");
         //::We now know that PF x86 exists and is not empty
         //::So we will check if VLC is here
         if(new File(osArch + vlcPath).exists())
         {
           //::Now we know that the x86 path exists.
           if(JVM_BIT == 64)
           {
             //::Trying to use a 32bit VLC with a 64 bit JVM
             System.out.println("Please get a 64-bit VLC.");
             System.exit(1);
           }
         }
       }//::End 64 bit vs 32 bit check
       
       
       //::If program files (x86) folder does not exist, use program files.
       //::else use the x86 folder. -- VLC by default installs under x86 folder
       //::for 64-bit machines.
       System.out.println(System.getenv("ProgramFiles") + vlcPath);
 
       //NativeLibrary.addSearchPath("libvlc", osArch + vlcPath);
       NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), 
                                        System.getenv("ProgramFiles") + vlcPath);
     }
         
     UserInterfaceFrame uIF = new UserInterfaceFrame();
   }
 }
