 package orissatest.application;
 
import com.orissa.application.Application;
 
 public final class MainClass {
 
     public static final void main(String[] args) {

	System.loadLibrary("platform_java_lib");

        Application app = new Application();
	
	System.out.println(app.getMyString());
	System.out.println("App major version = " + app.getMajorVersion());
	System.out.println("App minor version = " + app.getMinorVersion());
     }
 }
