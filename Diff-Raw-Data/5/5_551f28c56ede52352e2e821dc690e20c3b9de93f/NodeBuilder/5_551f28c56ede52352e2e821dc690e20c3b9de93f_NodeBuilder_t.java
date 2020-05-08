 
 package net.slreynolds.ds.model.builder;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.List;
 
 
 import net.slreynolds.ds.model.BuildException;
 import net.slreynolds.ds.model.BuilderOptions;
 import net.slreynolds.ds.model.Graph;
 import net.slreynolds.ds.model.GraphPoint;
 import net.slreynolds.ds.model.Named;
 import net.slreynolds.ds.model.NamedIDGenerator;
 import net.slreynolds.ds.model.Node;
 import net.slreynolds.ds.model.NodeArray;
 import net.slreynolds.ds.util.NodeUtil;
 import net.slreynolds.ds.util.Pair;
 
 /**
  *
  */
 public class NodeBuilder  {
   
     
 	final public static GraphPoint buildNode(Object o, Graph graph,  GraphBuildContext context, int nestingLevel) throws BuildException {
 
 		if (o == null) {
 			System.err.printf("Won't build a null\n");
 			return null;
 		}
 		
 		final Map<String,Object> options = context.getOptions();
 		final boolean showSystemHash =
 				(options.containsKey(BuilderOptions.SHOW_SYSHASH) ? 
 						(Boolean)options.get(BuilderOptions.SHOW_SYSHASH)  : BuilderOptions.DEFAULT_SHOW_SYSHASH );
 		final int MAX_NESTING = 
 				(options.containsKey(BuilderOptions.MAX_NESTING) ? 
 						(Integer)options.get(BuilderOptions.MAX_NESTING)  : BuilderOptions.DEFAULT_MAX_NESTING );
 		final int MAX_ARRAY_LENGTH = 
 				(options.containsKey(BuilderOptions.MAX_ARRAY_LENGTH) ? 
 						(Integer)options.get(BuilderOptions.MAX_ARRAY_LENGTH)  : BuilderOptions.DEFAULT_MAX_ARRAY_LENGTH );
 		final int generation = (Integer)options.get(BuilderOptions.GENERATION);
 		
 		try {
 			final String classname = getClassName(o);
 			final String packageNameOfInstance = classNameToPackage(o.getClass().getName());
 			if (o.getClass().isArray()) {
 				int length = getArrayLength(o);
 				if (length > MAX_ARRAY_LENGTH) {
 					// TODO This message should have a name for the array. Else it is highly ambiguous
 					// and confusing to the user.
 					System.out.printf("Truncating array type %s of length %d to %d\n",
 							classname,length,MAX_ARRAY_LENGTH);
 					length = MAX_ARRAY_LENGTH;
 				}
 				NodeArray array = new NodeArray(NamedIDGenerator.next()," ",NodeArray.ArrayType.NODE,length, generation); 
 				array.putAttr(Named.CLASS,classname);
 				final boolean inlineValues = shouldInlineArrayValues(o,options);
 				for(int i = 0; i < length; i++) {
 					Node node = (Node)array.get(i);
 					Object val = getArrayValue(o,i);
 					if (val == null || inlineValues) {
 						node.putAttr(Named.VALUE,val);  
 					}
 					else if (nestingLevel < MAX_NESTING) {
 						enqueueNode(context, nestingLevel, node," ", val);
 					}
 					else {
 						// TODO this log message should really have a name for the object instead of just a type. 
 						// this message is really ambiguous!
 						String reason = "";
 						if (nestingLevel >= MAX_NESTING) {
 							reason = String.format("nestingLevel %s exceeds MAX_NESTING %d");
 						}
 						System.out.printf("Not following array %s:%s because %s.\n",
 								o.getClass(),reason);
 					}
 				}
 				if (showSystemHash)
 					NodeUtil.addSystemHash(array, o);
 				context.addPoint(o,array);
 				return array;
 			}
 			else {
 				Node node = new Node(NamedIDGenerator.next(),generation);
 				// TODO try to get generic info?
 				node.putAttr(Named.CLASS, classname); 
 				if (showSystemHash) {
 					NodeUtil.addSystemHash(node, o);
 				}
 				Class<?> clazz = o.getClass();
 
 				// TODO logic embodied by only exporting fields in this package should be injectable by users
 				while (clazz != null && classNameToPackage(clazz.getName()).equals(packageNameOfInstance)) {
 					Field[] fields = clazz.getDeclaredFields();
 
 					for (Field field : fields) {
 						
 						field.setAccessible(true);
 						
 						final String fieldName = field.getName();
 						Object fieldValue = null;
 						try {
 							fieldValue = field.get(o);
 							if (fieldValue == null) continue;
 						} 
 						catch (IllegalArgumentException e) {
 							throw new BuildException("Error accessing field " + classname + "." + fieldName ,e);
 						} 
 						catch (IllegalAccessException e) {
 							throw new BuildException("Error accessing field " + classname + "." + fieldName ,e);	
 						}    		
 
 						Pair<Boolean,List<String>> followAsPair = shouldFollowField(nestingLevel, MAX_NESTING, o, field);
 						if (!followAsPair.first()) {
 							List<String> reasons = followAsPair.second();
 							if (reasons.size() > 0) {
 								StringBuilder reason = new StringBuilder();
 								for (int i = 0; i < (reasons.size()- 1);i++) {
 									reason.append(reasons.get(i));
 									reason.append(", ");
 								}
 								reason.append(reasons.get(reasons.size()-1));
 								System.out.printf("Not following field %s:%s because %s.\n",
 										fieldName,fieldValue.getClass(),reason);
 							}
 							continue;
 						}
 						
 						if (shouldInlineField(o,field,options)) {
 							node.putAttr(fieldName,fieldValue);
 						}
						else {
							enqueueNode(context, nestingLevel, node, fieldName, fieldValue);
						}
 						
 					}
 					clazz = clazz.getSuperclass();
 				}
 				context.addPoint(o,node);
 				return node;
 			}
 		}
 		catch (BuildException be) {
 			be.printStackTrace();
 			throw be;
 		}
 		catch (NullPointerException npe) {
 			npe.printStackTrace();
 			throw npe;
 		}
 		catch (Throwable t) {
 			t.printStackTrace();
 			throw new RuntimeException(t);
 		}
 	}
 	
 	// TODO the logic embodied in shouldFollowField should be injectable by users
 	private static Pair<Boolean,List<String>> shouldFollowField(int nestingLevel, int MAX_NESTING, Object o, Field field) {
 		boolean shouldFollow = true;
 		List<String> reasons = new ArrayList<String>();
 		
     	if (Modifier.isStatic(field.getModifiers())) {
     		shouldFollow = false;
     		/* don't blaber about static fields, too many of them */
     		return new Pair<Boolean,List<String>>(shouldFollow,reasons);
     	}
     	
 		/* Note: Scala compiler puts some important fields as synthetic,
 		 * so don't dare skip them
 		 */
 		if (nestingLevel >= MAX_NESTING) {
     		shouldFollow = false;
     		reasons.add(String.format("nestingLevel %d exceeds MAX_NESTING %d",
     				nestingLevel,MAX_NESTING));
 		}
 		
     	if (field.getName().equals("serialVersionUID")) {
     		shouldFollow = false;
     		reasons.add("field named serialVersionUID");
     	}
     	
     	if (field.getName().equals("serialPersistentFields")) {
     		shouldFollow = false;
     		reasons.add("field named serialPersistentFields");
     	}
     	
 
 
 		// Do not follow scala.collection.parallel.*TaskSupport else we
 		// get into a big tangle that GraphViz chokes on
 		if (o.getClass().getName().endsWith("TaskSupport")) {
 			shouldFollow = false;
 			reasons.add("Parent object is a TaskSupport");
 		}	
 		
 		// Similarly, following a Class is not interesting in this context
 		if (o.getClass().getName().equals("java.lang.Class")) {
 			shouldFollow = false;
 			reasons.add("Parent object is a java.lang.Class");
 		}	
 		
     	if (field.getName().equals("_meta")) {
     		shouldFollow = false;
     		reasons.add("field named _meta");
     	}
 		return new Pair<Boolean,List<String>>(shouldFollow,reasons);
 	}
 
 	private static void enqueueNode(GraphBuildContext context, int nestingLevel, GraphPoint fromNode,
 			final String edgeName, Object value) {
 		if (context.hasPoint(value)) {
 			final EdgeSuspension edgeSusp = new EdgeSuspension(fromNode,context.getPoint(value),edgeName);
 			context.enqueueEdgeToBuild(edgeSusp);
 		}
 		else {
 			final GraphPointSuspension pointSusp = new GraphPointSuspension(nestingLevel+1,value);
 			context.enqueuePointToBuild(pointSusp);
 			final EdgeSuspension edgeSusp = new EdgeSuspension(fromNode,value,edgeName);
 			context.enqueueEdgeToBuild(edgeSusp);
 		}
 	}
     
     private static int getArrayLength(Object ar) throws BuildException {
     	try {
     		return Array.getLength(ar);
 		} 
     	catch (Exception e) {
 			throw new BuildException("Error getting length on "+ ar.toString(),e);
 		} 
     	
     }
     
     private static Object getArrayValue(Object ar, int i) throws BuildException {
     	try {
     		return Array.get(ar, i);
 		} 
     	catch (Exception e) {
 			throw new BuildException("Error getting value[" + i + "] on " + ar.toString(),e);
 		} 
     }
     
     
     private static boolean shouldInlineField(Object o, Field field, Map<String,Object> options) {
     	return shouldInLineType(field.getType(),options);
     }
     
     private static boolean shouldInlineArrayValues(Object o, Map<String,Object> options) {
     	if (!o.getClass().isArray()) {
     		throw new IllegalArgumentException("first argument to this method must be an array");
     	}
     	Class<?> contentType = o.getClass().getComponentType();
     	
     	if (shouldInLineType(contentType,options))
     		return true;
     	boolean shouldInLine = true;
     	final int N = Array.getLength(o);
     	for (int i = 0; i < N; i++) {
     		Object val = Array.get(o, i);
     		if (val == null) continue;
     		shouldInLine = shouldInLine && shouldInLineType(val.getClass(),options);
     	}
     	return shouldInLine;
     }
     
     private static boolean shouldInLineType(Class<?> clazz,Map<String,Object> options) {
     	if (clazz.equals(String.class)) {
     		boolean inlineString = BuilderOptions.DEFAULT_INLINE_STRINGS;
     		if (options.containsKey(BuilderOptions.INLINE_STRINGS)) {
     			inlineString = (Boolean)options.get(BuilderOptions.INLINE_STRINGS);
     		}
     		return inlineString;
     	}
     	if (clazz.isPrimitive()) return true;
 		if (isNumberObject( clazz, options))  {
 			boolean inlineNumbers = BuilderOptions.DEFAULT_INLINE_NUMBERS;
 			if (options.containsKey(BuilderOptions.INLINE_NUMBERS)) {
 				inlineNumbers = (Boolean)options.get(BuilderOptions.INLINE_NUMBERS);
 			}
 			return inlineNumbers;
 		}
     	return false;
     }
     
     private static boolean isNumberObject(Class<?> clazz,Map<String,Object> options) {
     	if (clazz.equals(Byte.class)) return true;
     	if (clazz.equals(Short.class)) return true;
     	if (clazz.equals(Integer.class)) return true;
     	if (clazz.equals(Long.class)) return true; 
     	if (clazz.equals(Float.class)) return true;
     	if (clazz.equals(Double.class)) return true;
     	return false;
     }
     
     private static boolean shouldSkip(Object o,Field field) {
     	if (field.getName().equals("serialVersionUID"))
     		return true;
     	if (field.getName().equals("serialPersistentFields"))
     		return true;
     	if (Modifier.isStatic(field.getModifiers()))
     		return true;
     	return false;
     }
     
     private static String getClassName(Object o) {
     	return o.getClass().getSimpleName(); // TODO fails if inner class
     }
     
     private static String classNameToPackage(String classname) {
     	int lastdot = classname.lastIndexOf('.');
     	if (lastdot < 0) return "no-package";
     	return classname.substring(0,lastdot);
     }
 
 }
