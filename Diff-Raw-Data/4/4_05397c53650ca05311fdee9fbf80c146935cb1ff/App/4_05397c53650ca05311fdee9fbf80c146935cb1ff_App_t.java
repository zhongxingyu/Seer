 package project.senior.app.pormmo;
 
import com.sun.jna.NativeLibrary;

 public class App 
 {
     public static void main( String[] args )
     {
        NativeLibrary.addSearchPath("libvlc", "C:\\Program Files\\VideoLAN\\VLC");
         UserInterfaceFrame uIF = new UserInterfaceFrame();
     }
 }
