 package ru.terra.jbrss;
 
 import android.app.Application;
 import org.acra.ACRA;
 import org.acra.ReportingInteractionMode;
 import org.acra.annotation.ReportsCrashes;
 
@ReportsCrashes(formKey = "",mailTo = "jbrss@terranz.ath.cx", mode = ReportingInteractionMode.TOAST, resToastText = R.string.error_caught)
 public class JBRSSApplication extends Application {
 	@Override
 	public void onCreate() {
 		ACRA.init(this);
 		super.onCreate();
 	}
 }
