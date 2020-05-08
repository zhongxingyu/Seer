 package org.scriptbox.box.controls;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.scriptbox.box.container.BoxContext;
 import org.scriptbox.box.container.Lookup;
 import org.scriptbox.util.common.collection.Iterators;
 import org.scriptbox.util.common.error.Errors;
 import org.scriptbox.util.common.obj.ParameterizedRunnable;
 
 public class BoxServices {
 
	public static Errors<BoxService> start( Collection<BoxService> services, final List arguments ) throws Exception {
 		return Iterators.callAndCollectExceptions( services, new ParameterizedRunnable<BoxService>() {
 			public void run( BoxService service ) throws Exception {
 				service.start( arguments );
 			}
 		} );
 	}
 	
 	public static Errors<BoxService> stop( Collection<BoxService> services ) throws Exception {
 		return Iterators.callAndCollectExceptions( services, new ParameterizedRunnable<BoxService>() {
 			public void run( BoxService service ) throws Exception {
 				service.stop();
 			}
 		} );
 	}
 	public static Errors<BoxService> shutdown( Collection<BoxService> services ) throws Exception {
 		return Iterators.callAndCollectExceptions( services, new ParameterizedRunnable<BoxService>() {
 			public void run( BoxService service ) throws Exception {
 				service.shutdown();
 			}
 		} );
 	}
 }
