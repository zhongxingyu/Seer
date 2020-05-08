 package gov.nih.nci.security.authorization.attributeLevel;
 
 import gov.nih.nci.security.AuthorizationManager;
 import gov.nih.nci.security.util.FileLoader;
 import gov.nih.nci.security.util.StringUtilities;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import net.sf.ehcache.Cache;
 import net.sf.ehcache.CacheException;
 import net.sf.ehcache.CacheManager;
 import net.sf.ehcache.Element;
 
 import org.hibernate.SessionFactory;
 import org.springframework.dao.DataRetrievalFailureException;
 
 
 public class UserClassAttributeMapCache
 {
 	//private static HashMap cache = new HashMap();
 	private static Cache cache=null;
 	
 	private static void initializeCache()
 	{
 		URL url=null;
 		FileLoader fileLoader = FileLoader.getInstance();
 		try
 		{
 			url = fileLoader.getFileAsURL("csm.ehcache.xml");
 		}
 		catch (Exception e)
 		{
 			url = null;
 		}
 		
 		CacheManager.create(url);
 		cache = CacheManager.getInstance().getCache("gov.nih.nci.security.attributelevel.ClassAttributeMap");
 		
 	}
 	
 	
 	public static List getAttributeMap(String userName, String className)
 	{
 		if(cache==null) initializeCache();
 		
 		List<String> strClassAttributeMapList=null;
 		List<ClassAttributeMap> classAttributeMapList =  (List<ClassAttributeMap>)getClassAttributeMapListFromCache(userName);
 		if(null!=classAttributeMapList && !classAttributeMapList.isEmpty())
 		{
 			strClassAttributeMapList = new ArrayList<String>();
 			Iterator it = classAttributeMapList.iterator();
 			while(it.hasNext())
 			{
 				ClassAttributeMap classAttributeMap = (ClassAttributeMap)it.next();
				if(null!= classAttributeMap.getAttributes() && !classAttributeMap.getAttributes().isEmpty()) 
 					strClassAttributeMapList.addAll(classAttributeMap.getAttributes());
 			}
 		}
 		return strClassAttributeMapList;
 	}
 	
 
 	@SuppressWarnings("unchecked")
 	public static List getAttributeMapForGroup(String[] groupNames, String className)
 	{	
 		List<String> classAttributeMapForGroups =new ArrayList<String>();		
 		for(int i=0;i<groupNames.length;i++)
 		{
 			String groupName = groupNames[i];
 			if(!StringUtilities.isBlank(groupName))
 			{
 				List<ClassAttributeMap> gcamList =  (List<ClassAttributeMap>)getClassAttributeMapListFromCache(groupName);
 				if(null!=gcamList && !gcamList.isEmpty())
 				{
 					Iterator it = gcamList.iterator();
 					while(it.hasNext())
 					{
 						ClassAttributeMap gcam = (ClassAttributeMap)it.next();
 						if(null!= gcam.getAttributes() && !gcam.getAttributes().isEmpty()) 
 							classAttributeMapForGroups.addAll(gcam.getAttributes());
 					}
 				}
 			}
 		}	
 		if(classAttributeMapForGroups.isEmpty()) 
 			return null;
 		
 		return classAttributeMapForGroups;	
 	}
 	
 
 	public static void setAttributeMap(String userName, SessionFactory sessionFactory, AuthorizationManager authorizationManager)
 	{
 		if(cache==null) initializeCache();
 		
 		String privilegeName = "READ";
 		Map map = sessionFactory.getAllClassMetadata();
 		Set set = map.keySet();
 		ArrayList list = new ArrayList(set);
 
 		List<ClassAttributeMap> classAttributeMapList = new ArrayList<ClassAttributeMap>();
 			
 		Iterator iterator = list.iterator();
 		while (iterator.hasNext())
 		{
 			String className = (String)iterator.next();
 			ClassAttributeMap classAttributeMap=null;
 			if(!StringUtilities.isBlank(className))
 			{
 				classAttributeMap = new ClassAttributeMap(className); 
 			}
 			List  attributeList = authorizationManager.getAttributeMap(userName, className, privilegeName);
 			if (null!= attributeList && attributeList.size() != 0)
 			{
 				if(classAttributeMap!=null)
 				{
 					classAttributeMap.setAttributes(attributeList);
 				}
 			}
 			if(classAttributeMap!=null) classAttributeMapList.add(classAttributeMap);
 		}
 		
 		putClassAttributeMapInCache(userName,classAttributeMapList);	
 		
 	}
 	
 	public static void setAttributeMapForGroup(String[] groupNames, SessionFactory sessionFactory, AuthorizationManager authorizationManager)
 	{		
 		if(cache==null) initializeCache();
 				
 		String privilegeName = "READ";
 		Map map = sessionFactory.getAllClassMetadata();
 		Set set = map.keySet();
 		ArrayList list = new ArrayList(set);
 		
 		for(int i=0;i<groupNames.length;i++)
 		{
 			List<ClassAttributeMap> classAttributeMapList = new ArrayList<ClassAttributeMap>();
 			
 			String groupName = groupNames[i];
 			
 			Iterator iterator = list.iterator();
 			while (iterator.hasNext())
 			{
 				String className = (String)iterator.next();
 				ClassAttributeMap classAttributeMap=null;
 				if(!StringUtilities.isBlank(className))
 				{
 					classAttributeMap = new ClassAttributeMap(className); 
 				}
 				List  attributeList = authorizationManager.getAttributeMapForGroup(groupName, className, privilegeName);
 				if (null!= attributeList && attributeList.size() != 0)
 				{
 					if(classAttributeMap!=null)
 					{
 						classAttributeMap.setAttributes(attributeList);
 					}
 				}
 				if(classAttributeMap!=null) classAttributeMapList.add(classAttributeMap);
 			}
 			putClassAttributeMapInCache(groupName,classAttributeMapList);
 			
 		}
 	}
 	
 	public static void removeAttributeMap(String userName)
 	{
 		cache.remove(userName);
 	}
 	
 	private static List<ClassAttributeMap> getClassAttributeMapListFromCache(String userOrGroupName) 
 	{
 		Element element = null;
 
 		try {
 			element = cache.get(userOrGroupName);
 		} catch (CacheException cacheException) {
 			throw new DataRetrievalFailureException("Cache failure: "
 					+ cacheException.getMessage());
 		}
 
 		if (element == null) {
 			return null;
 		} else {
 			return (List<ClassAttributeMap>) element.getValue();
 		}
 	}
 	
 	private static void putClassAttributeMapInCache(String userOrGroupName, List<ClassAttributeMap> groupClassAttributeMapList) 
 	{
 		ArrayList arrayList = new ArrayList(groupClassAttributeMapList);
 		Element element = new Element(userOrGroupName, arrayList);
 		cache.put(element);
 	}
 	
 }
