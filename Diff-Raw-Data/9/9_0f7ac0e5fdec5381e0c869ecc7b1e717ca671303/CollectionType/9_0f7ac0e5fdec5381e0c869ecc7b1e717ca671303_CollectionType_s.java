 /**
  * 
  */
 package ecologylab.serialization.types;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import ecologylab.generic.ReflectionTools;
 import ecologylab.serialization.ElementState;
 import ecologylab.serialization.types.scalar.MappingConstants;
 
 /**
  * Basic cross-platform unit for managing Collection and Map types in S.IM.PL Serialization.
  * 
  * @author andruid
  */
 public class CollectionType extends ElementState
 implements MappingConstants
 {
 	/**
 	 * This is a platform-independent identifier that S.IM.PL uses for the CollectionType.
 	 */
 	@simpl_scalar
 	String			name;
 	
 	@simpl_scalar
 	String			javaName;
 	
 	String			javaSimpleName;
 	
 	Class				javaClass;
 	
 	@simpl_scalar
 	boolean			isMap;
 	
 	@simpl_scalar
 	String			cSharpName;
 	
 	@simpl_scalar
 	String			objCName;
 
 	private static final  HashMap<String, CollectionType>	globalMap = new HashMap<String, CollectionType>();
 	
 	static
 	{
 		new CollectionType(JAVA_ARRAYLIST, ArrayList.class, DOTNET_ARRAYLIST, OBJC_ARRAYLIST, false);
 		
		new CollectionType(JAVA_HASHMAP, ArrayList.class, DOTNET_HASHMAP, OBJC_HASHMAP, true);
		new CollectionType(JAVA_HASHMAPARRAYLIST, ArrayList.class, DOTNET_HASHMAPARRAYLIST, OBJC_HASHMAPARRAYLIST, true);
		new CollectionType(JAVA_SCOPE, ArrayList.class, DOTNET_SCOPE, OBJC_SCOPE, true);
 	}
 	
 	/**
 	 * 
 	 */
 	public CollectionType()
 	{
 	}
 	
 	public CollectionType(String name, Class javaClass, String cSharpName, String objCName, boolean isMap)
 	{
 		this.name				= name;
 		this.javaClass	= javaClass;
 		this.javaName		= javaClass.getName();
 		this.javaSimpleName	= javaClass.getSimpleName();
 		
 		this.cSharpName	= cSharpName;
 		this.objCName		= objCName;
 		this.isMap			= isMap;
 		
 		globalMap.put(name, this);
 	}
 	
 	public Object getInstance()
 	{
 		return ReflectionTools.getInstance(javaClass);
 	}
 	
 	public Collection getCollection()
 	{
 		return isMap ? null : (Collection) getInstance();
 	}
 	
 	public Map getMap()
 	{
 		return isMap ? (Map) getInstance() : null;
 	}
 	
 	public static CollectionType getType(String javaName)
 	{
 		return globalMap.get(javaName);
 	}
 	
 	
 	public String getName()
 	{
 		return name;
 	}
 
 	public String getJavaName()
 	{
 		return javaName;
 	}
 
 	public String getJavaSimpleName()
 	{
 		return javaSimpleName;
 	}
 
 	public Class getJavaClass()
 	{
 		return javaClass;
 	}
 
 	public boolean isMap()
 	{
 		return isMap;
 	}
 
 	public String getcSharpName()
 	{
 		return cSharpName;
 	}
 
 	public String getObjCName()
 	{
 		return objCName;
 	}
 
 }
