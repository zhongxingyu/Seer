 package co.mwater.acraplugin;
 
 import android.app.Application;
 import org.acra.*;
 import org.acra.annotation.*;
import co.mwater.clientapp.R;
 
 @ReportsCrashes(formKey="",
 	formUri = "http://api.mwater.co/v3/acra_reports",
 	reportType = org.acra.sender.HttpSender.Type.JSON,
 	mode = ReportingInteractionMode.TOAST,
 	resToastText = R.string.crash_toast_text)
 public class MyApplication extends Application {
   @Override
   public void onCreate() {
     // The following line triggers the initialization of ACRA
     super.onCreate();
     ACRA.init(this);
   }
}	
