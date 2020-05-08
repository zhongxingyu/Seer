 package posl.engine.core;
 
 import java.io.OutputStream;
 import java.lang.reflect.Method;
 import java.util.logging.Logger;
 
 import posl.engine.annotation.Command;
 import posl.engine.annotation.Primitive;
 import posl.engine.api.IParser;
 
 public class Context {
 	
 	private IParser parser;
 	private Scope scope;
 	
 	private static final String output = "__outputstream";
 	
 	private static final Logger log = Logger.getLogger(Context.class
 			.getName());
 	
 	public Context() {
 		this(new Parser(),new Scope());
 	}
 	
 	
 	public Context(Scope scope) {
 		this(new Parser(),scope);
 	}
 	
 	public Context(IParser parser, Scope scope) {
 		this.parser = parser;
 		this.scope = scope;
 		scope.put(output, System.out);
 	}
 	
 	public void load(Class<?> libraryClass){
 		Method[] methods = libraryClass.getMethods();
 		loadMethods(null, methods);
 	}
 	
 	public void load(Object libraryObject){
 		Method[] methods = libraryObject.getClass().getMethods();
 		loadMethods(libraryObject, methods);
 	}
 
 	public IParser getParser() {
 		return parser;
 	}
 
 
 	public Scope getScope() {
 		return scope;
 	}
 	
 	
 	/**
 	 * Utility function to add a key value pair to the underlying scope
 	 * 
 	 * @param key
 	 * @param value
 	 */
 	public void put(String key,Object value){
 		scope.put(key, value);
 	}
 	
 	private void loadMethods(Object object, Method[] methods) {
 		for (Method method : methods) {
 			boolean isCommand = method.isAnnotationPresent(Command.class);
 			boolean isPrimitive = method.isAnnotationPresent(Primitive.class);
 			if (isCommand || isPrimitive) {
 				String id = null;
 				Object prior = null;
 				if (isCommand){
 					id = method.getAnnotation(Command.class).value();
 				}
 				if (isPrimitive){
 					id = method.getAnnotation(Primitive.class).value();
 				}
 				if (id == null){
 					id = method.getName();
 					log.fine("no id defined, using the method name: "+ id);
 				}
 				if (isCommand) {
 					prior = scope.put(id, new MethodProxy(method, object));
 					log.fine("command : " + id + " has been added");
 				} else {
 					try {
 						prior = scope.put(id, method.invoke(object));
 					} catch (Exception e) {
 						log.warning("error occured when attempting to create primitive: "+id);
 						prior = "";
 					}
 				}
 				if (prior != null){
 					log.warning("adding "+id+" displaced previously assigned object");
 				}
 			}
 		}
 	}
 	
 	public boolean isComplete(){
 		return parser.complete();
 	}
 	
 	public void setOutputStream(OutputStream os){
 		scope.put(output, os);
 	}
 	
 	public void resetParser(){
 		this.parser = new Parser();
 	}
 
 }
