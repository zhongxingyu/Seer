 package eu32k.ludumdare.ld26.client;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.backends.gwt.GwtApplication;
 import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
 
 import eu32k.ludumdare.ld26.Config;
 import eu32k.ludumdare.ld26.LudumDare26;
 
 public class GwtLauncher extends GwtApplication {
    @Override
    public GwtApplicationConfiguration getConfig() {
       GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(Config.X_RESOLUTION, Config.Y_RESOLUTION);
 
       return cfg;
    }
 
    @Override
    public ApplicationListener getApplicationListener() {
       return new LudumDare26();
    }
 }
