 package org.codehaus.xfire.aegis.type.java5;
 
 import java.beans.Introspector;
 import java.beans.PropertyDescriptor;
 import java.lang.reflect.Method;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.namespace.QName;
 
 import org.codehaus.xfire.aegis.AbstractXFireAegisTest;
 import org.codehaus.xfire.aegis.type.CustomTypeMapping;
 import org.codehaus.xfire.aegis.type.Type;
 import org.codehaus.xfire.aegis.type.collection.MapType;
 import org.codehaus.xfire.aegis.type.java5.dto.MapDTO;
 import org.codehaus.xfire.aegis.type.java5.dto.MapDTOService;
 import org.codehaus.xfire.service.Service;
 
 public class MapTest
     extends AbstractXFireAegisTest
 {
     private CustomTypeMapping tm;
     private Java5TypeCreator creator;
 
     public void setUp() throws Exception
     {
         super.setUp();
         
         tm = new CustomTypeMapping();
         creator = new Java5TypeCreator();
         tm.setTypeCreator(creator);
     }
 
     public void testType() throws Exception
     {
         Method m = MapService.class.getMethod("getMap", new Class[0]);
         
         Type type = creator.createType(m, -1);
         tm.register(type);
         assertTrue( type instanceof MapType );
         
         MapType mapType = (MapType) type;
         QName keyName = mapType.getKeyName();
         
         type = mapType.getKeyType();
         assertNotNull(type);
         assertTrue(type.getTypeClass().isAssignableFrom(String.class));
         
         type = mapType.getValueType();
         assertNotNull(type);
         assertTrue(type.getTypeClass().isAssignableFrom(Integer.class));
     }
     
     public void testPDType() throws Exception
     {
         PropertyDescriptor pd = 
             Introspector.getBeanInfo(MapDTO.class, Object.class).getPropertyDescriptors()[0];
         Type type = creator.createType(pd);
         tm.register(type);
         assertTrue( type instanceof MapType );
         
         MapType mapType = (MapType) type;
         QName keyName = mapType.getKeyName();
         
         type = mapType.getKeyType();
         assertNotNull(type);
         assertTrue(type.getTypeClass().isAssignableFrom(String.class));
         
         type = mapType.getValueType();
         assertNotNull(type);
         assertTrue(type.getTypeClass().isAssignableFrom(Integer.class));
     }
 
     public void testMapDTO()
     {
         CustomTypeMapping tm = new CustomTypeMapping();
         Java5TypeCreator creator = new Java5TypeCreator();
         tm.setTypeCreator(creator);
         
         Type dto = creator.createType(MapDTO.class);
         Set deps = dto.getDependencies();
         
         Type type = (Type) deps.iterator().next();
         System.out.println(type.getClass().getName());
         assertTrue( type instanceof MapType );
         
         MapType mapType = (MapType) type;
         
         deps = dto.getDependencies();
         assertEquals(1, deps.size());
         
         type = mapType.getKeyType();
         assertNotNull(type);
         assertTrue(type.getTypeClass().isAssignableFrom(String.class));
         
         type = mapType.getValueType();
         assertNotNull(type);
         assertTrue(type.getTypeClass().isAssignableFrom(Integer.class));
     }
     
     public void testMapDTOService() throws Exception
     {
         Service service = getServiceFactory().create(MapDTOService.class);
         getServiceRegistry().register(service);
         
        printNode(invokeService(service.getSimpleName(), "/org/codehaus/xfire/aegis/type/java5/dto/GetDTO.xml"));
     }
     
     public void testMapServiceWSDL() throws Exception
     {
         Service service = getServiceFactory().create(MapDTOService.class);
         getServiceRegistry().register(service);
         
        printNode(getWSDLDocument(service.getSimpleName()));
     }
     
     public class MapService
     {
         public Map<String,Integer> getMap()
         {
         	return null;
         }
         
         public void setMap(Map<String,Integer> strings) {
         	
         }
     }
 }
