 package com.eas.android.libraries.rapidjs.pluginmanager;
 
 import java.util.ArrayList;
 
 import android.webkit.WebView;
 
 import com.eas.android.libraries.rapidjs.plugins.acceleratedCanvas2D.AcceleratedCanvas2DPlugin;
 import com.eas.android.libraries.rapidjs.plugins.accelerometer.AccelerometerPlugin;
 import com.eas.android.libraries.rapidjs.plugins.callback.CallbackPlugin;
import com.eas.android.libraries.rapidjs.plugins.core.CorePlugin;
 
 public class PluginInitializer {
 	public static void init(ArrayList<RapidJSWebViewPlugin> plugins, PluginManager pluginManager, WebView webView, boolean accelerateCanvas){
		plugins.add(new CorePlugin());
 		plugins.add(new CallbackPlugin());
 		if (accelerateCanvas)
 			plugins.add(new AcceleratedCanvas2DPlugin());
 		plugins.add(new AccelerometerPlugin());
 	}
 }
