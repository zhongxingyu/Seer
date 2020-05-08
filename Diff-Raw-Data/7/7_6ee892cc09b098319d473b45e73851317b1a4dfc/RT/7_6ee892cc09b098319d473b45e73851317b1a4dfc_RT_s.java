 package li.rudin.rt.jjst;
 
 import li.rudin.jjst.api.Javascript;
import li.rudin.jjst.api.JavascriptCollection;
 import li.rudin.jjst.api.Native;
 import li.rudin.jjst.api.UnwrapMethod;
 import li.rudin.jjst.core.api.JSObject;
 import li.rudin.jjst.lib.knockout.Observable;
 
 @Native("RT")
@JavascriptCollection({
	@Javascript("/META-INF/resources/rt-api/js/rt.js"),
	@Javascript("/META-INF/resources/rt-api/js/rt-observable.js")
})
 public class RT
 {
 	
 	public RT(String url){}
 
 	public native void on(String name, @UnwrapMethod("onEvent") RTListener listener);
 	
 	public native <T> Observable<T> observable(String changeId);
 	//TODO: observable array
 	
 	/**
 	 * Listener interface
 	 * @author user
 	 *
 	 */
 	public interface RTListener
 	{
 		/**
 		 * Called on incoming event
 		 * @param data
 		 */
 		void onEvent(JSObject data);
 	}
 	
 }
