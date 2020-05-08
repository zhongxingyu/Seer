 package httpServletRequestX.accept.contentType;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Unit test case for {@See AcceptContenTypeList}
  * 
  * @author Marco Reinwarth <marcoreinwarth@gmail.com
  */
 public class AcceptContenTypeListUnitTest {
 
     private static final String           TYPA              = "testtype/A";
     private static final String           TYPB              = "testtype/B";
     private static final String           TYPC              = "testtype/C";
 
     private static final AcceptContenType acceptContenTypeA = new AcceptContenType(TYPA, 1.0f, 1);
     private static final AcceptContenType acceptContenTypeB = new AcceptContenType(TYPB, 1.0f, 1);
     private static final AcceptContenType acceptContenTypeC = new AcceptContenType(TYPC, 1.0f, 1);
 
     private AcceptContenTypeList          acceptContenTypeList;
 
     @Before
     public void setUp() {
         acceptContenTypeList = new AcceptContenTypeList();
         acceptContenTypeList.add(acceptContenTypeA);
         acceptContenTypeList.add(acceptContenTypeB);
         acceptContenTypeList.add(acceptContenTypeC);
     }
 
     @Test
     public void testContainsType() {
         assertTrue(acceptContenTypeList.containsType(TYPA));
         assertTrue(acceptContenTypeList.containsType(TYPB));
         assertTrue(acceptContenTypeList.containsType(TYPC));
         assertFalse(acceptContenTypeList.containsType("some unknown type"));
     }
 }
