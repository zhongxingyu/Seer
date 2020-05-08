 package org.codehaus.xfire.aegis.type.basic;
 
 import java.lang.reflect.Method;
 import java.math.BigDecimal;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 
 import javax.xml.namespace.QName;
 
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.aegis.AbstractXFireAegisTest;
 import org.codehaus.xfire.aegis.type.CustomTypeMapping;
 import org.codehaus.xfire.aegis.type.DefaultTypeCreator;
 import org.codehaus.xfire.aegis.type.DefaultTypeMappingRegistry;
 import org.codehaus.xfire.aegis.type.Type;
 import org.codehaus.xfire.aegis.type.XMLTypeCreator;
 import org.codehaus.xfire.aegis.type.collection.CollectionType;
 
 public class DescriptorTest
     extends AbstractXFireAegisTest
 {
     CustomTypeMapping tm;
 
     protected void setUp()
         throws Exception
     {
         super.setUp();
 
         tm = new CustomTypeMapping();
         XMLTypeCreator creator = new XMLTypeCreator();
         creator.setNextCreator(new DefaultTypeCreator());
         tm.setTypeCreator(creator);
     }
 
     public void testMapping() throws Exception
     {
         tm.setEncodingStyleURI("urn:xfire:bean");
 
         Type type = tm.getTypeCreator().createType(MyBean.class);
         BeanTypeInfo info = ((BeanType) type).getTypeInfo();
 
         Iterator elItr = info.getElements();
         assertTrue(elItr.hasNext());
         QName el = (QName) elItr.next();
         assertEquals("urn:xfire:bean", el.getNamespaceURI());
         assertEquals("Prop1", el.getLocalPart());
 
         Iterator attItr = info.getAttributes();
         assertTrue(attItr.hasNext());
         QName att = (QName) attItr.next();
         assertEquals("urn:xfire:bean", att.getNamespaceURI());
         assertEquals("Prop2", att.getLocalPart());
     }
 
     public void testMapping2() throws Exception
     {
         tm.setEncodingStyleURI("urn:xfire:bean2");
 
         Type type = tm.getTypeCreator().createType(MyBean.class);
         BeanTypeInfo info = ((BeanType) type).getTypeInfo();
 
         Iterator elItr = info.getElements();
         assertTrue(elItr.hasNext());
         QName el = (QName) elItr.next();
         assertEquals("urn:xfire:bean2", el.getNamespaceURI());
         assertEquals("Prop1", el.getLocalPart());
 
         assertTrue(elItr.hasNext());
         QName el2 = (QName) elItr.next();
         assertEquals("urn:xfire:bean2", el2.getNamespaceURI());
         assertEquals("Prop2", el2.getLocalPart());
     }
 
     public void testListHolder() throws Exception
     {
         tm.setEncodingStyleURI("urn:xfire:bean");
 
         Type type = tm.getTypeCreator().createType(ListHolderBean.class);
         BeanTypeInfo info = ((BeanType) type).getTypeInfo();
 
         Iterator elItr = info.getElements();
         assertTrue(elItr.hasNext());
         QName el = (QName) elItr.next();
         assertEquals("urn:xfire:bean", el.getNamespaceURI());
         assertEquals("Beans", el.getLocalPart());
 
         Type beanList = info.getType(el);
         assertTrue( beanList instanceof CollectionType );
     }
 
     public void testListHolderNoName() throws Exception
     {
         tm.setEncodingStyleURI("urn:xfire:bean2");
 
         Type type = tm.getTypeCreator().createType(ListHolderBean.class);
         BeanTypeInfo info = ((BeanType) type).getTypeInfo();
 
         Iterator elItr = info.getElements();
         assertTrue(elItr.hasNext());
         QName el = (QName) elItr.next();
         assertEquals("urn:xfire:bean2", el.getNamespaceURI());
         assertEquals("beans", el.getLocalPart());
 
         Type beanList = info.getType(el);
         assertTrue( beanList instanceof CollectionType );
     }
 
     public void testDefaultName() throws Exception
     {
         tm.setEncodingStyleURI("urn:xfire:bean4");
 
         Type type = tm.getTypeCreator().createType(MyBean.class);
         BeanTypeInfo info = ((BeanType) type).getTypeInfo();
 
         Iterator attItr = info.getAttributes();
         assertTrue(attItr.hasNext());
         QName el = (QName) attItr.next();
         assertEquals("urn:xfire:bean4", el.getNamespaceURI());
         assertEquals("prop2", el.getLocalPart());
     }
 
     public void testSimpleXMLMapping() throws Exception
     {
         XMLTypeCreator creator = new XMLTypeCreator();
         creator.setNextCreator(new DefaultTypeCreator());
         tm = new CustomTypeMapping(new DefaultTypeMappingRegistry().createDefaultMappings());
         creator.setTypeMapping(tm);
         Type type = creator.createType(MyService1.class.getDeclaredMethod("getCollection", new Class[0]), -1);
         assertTrue("type is not a collection", type instanceof CollectionType);
         assertEquals("unexpected collection type", Double.class, ((CollectionType)type).getComponentType().getTypeClass());
         try
         {
             creator.createType(MyService1.class.getDeclaredMethod("getUnmapped", new Class[]{java.util.List.class}), 0);
             fail("Unmapped collection did not throw an exception");
         }
         catch(XFireRuntimeException ex)
         {
         }
        
        assertEquals(new QName(tm.getEncodingStyleURI(), "doubles"), type.getSchemaType());
     }
 
     public void testBestMatch() throws Exception
     {
         XMLTypeCreator creator = new XMLTypeCreator();
         creator.setNextCreator(new DefaultTypeCreator());
         tm = new CustomTypeMapping(new DefaultTypeMappingRegistry().createDefaultMappings());
         creator.setTypeMapping(tm);
         Method method = MyService1.class.getDeclaredMethod("getCollection", new Class[0]);
         Type type = creator.createType(method, -1);
         assertTrue("type is not a collection", type instanceof CollectionType);
         assertEquals("unexpected collection return type for method " + method, Double.class, ((CollectionType)type).getComponentType().getTypeClass());
         
         method = MyService1.class.getDeclaredMethod("getCollection", new Class[]{Integer.TYPE});
         type = creator.createType(method, -1);
         assertEquals("unexpected collection return type for method " + method, Float.class, ((CollectionType)type).getComponentType().getTypeClass());
         
        type = creator.createType(method, 0);
        // assertEquals(new QName("urn:foo:bar", "int"), type.getSchemaType());
        
         method = MyService1.class.getDeclaredMethod("getCollectionForValues", new Class[]{Integer.TYPE, Collection.class});
         type = creator.createType(method, -1);
         assertEquals("unexpected collection return type for method " + method, Calendar.class, ((CollectionType)type).getComponentType().getTypeClass());
         
         method = MyService1.class.getDeclaredMethod("getCollectionForValues", new Class[]{String.class, Collection.class});
         type = creator.createType(method, -1);
         assertEquals("unexpected collection return type for method " + method, BigDecimal.class, ((CollectionType)type).getComponentType().getTypeClass());
         type = creator.createType(method, 1);
         assertEquals("unexpected collection parameter type for method " + method, Date.class, ((CollectionType)type).getComponentType().getTypeClass());
         try
         {
             creator.createType(method, 2);
             fail("Expected exception when requesting type for non-existent parameter index");
         }
         catch(Exception ex)
         {}
     }
     
     public void testMapping5() throws Exception
     {
         tm.setEncodingStyleURI("urn:xfire:bean5");
 
         Type type = tm.getTypeCreator().createType(MyBean.class);
         BeanTypeInfo info = ((BeanType) type).getTypeInfo();
 
         Iterator elItr = info.getElements();
         assertFalse(elItr.hasNext());
 
         Iterator attItr = info.getAttributes();
         assertFalse(attItr.hasNext());
     }
 }
