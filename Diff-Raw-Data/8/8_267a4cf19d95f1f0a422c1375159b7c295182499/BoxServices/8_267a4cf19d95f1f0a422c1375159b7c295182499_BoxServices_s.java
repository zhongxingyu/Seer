 package org.scriptbox.box.controls;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.scriptbox.box.container.BoxContext;
 import org.scriptbox.box.container.Lookup;
 import org.scriptbox.util.common.collection.Iterators;
 import org.scriptbox.util.common.error.Errors;
 import org.scriptbox.util.common.obj.ParameterizedRunnable;
 
 public class BoxServices {
 
	public static List getArguments() {
		Lookup lookup = BoxContext.getCurrentContext().getBeans();
		return lookup.get( "box.services.arguments", List.class );
	}
	
	public static Errors<BoxService> start( BoxContext context, Collection<BoxService> services, final List arguments ) throws Exception {
		context.getBeans().put( "box.services.arguments", arguments );
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
