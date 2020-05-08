 package ru.terra.jbrss;
 
 import android.app.Application;
 import org.acra.ACRA;
 import org.acra.ReportingInteractionMode;
 import org.acra.annotation.ReportsCrashes;
 
@ReportsCrashes(formKey = "dHNOa2ljTHF2dnpmNjBycm9HOExNU2c6MA", mode = ReportingInteractionMode.TOAST, resToastText = R.string.error_caught)
 public class JBRSSApplication extends Application {
 	@Override
 	public void onCreate() {
 		ACRA.init(this);
 		super.onCreate();
 	}
 }
