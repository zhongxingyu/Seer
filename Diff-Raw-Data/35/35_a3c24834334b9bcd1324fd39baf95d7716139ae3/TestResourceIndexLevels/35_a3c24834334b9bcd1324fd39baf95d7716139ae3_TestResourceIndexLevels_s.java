 package fedora.server.resourceIndex;
 
 import java.io.File;
 
 import fedora.server.storage.types.DigitalObject;
 
 /**
  * @author Edwin Shin
  */
 public class TestResourceIndexLevels extends TestResourceIndex {
     private int m_level;
     private DigitalObject bdef, bmech, dataobject;
     
     protected void setUp() throws Exception {
         super.setUp();
         
         bdef = getFoxmlObject(new File(DEMO_OBJECTS_ROOT_DIR
                 + "/bdefs/demo_ri8.xml"));
         bmech = getFoxmlObject(new File(DEMO_OBJECTS_ROOT_DIR
                 + "/bmechs/demo_ri9.xml"));
         dataobject = getFoxmlObject(new File(
                 DEMO_OBJECTS_ROOT_DIR + "/dataobjects/demo_ri10.xml"));
     }
     
     public void testLevelOff() throws Exception {
        m_ri = new ResourceIndexImpl(ResourceIndex.INDEX_LEVEL_OFF, m_conn, m_cPool, null);
         
         m_ri.addDigitalObject(bdef);
         m_ri.commit();
         assertEquals(0, m_ri.countTriples(null, null, null, 0));
         m_ri.addDigitalObject(bmech);
         m_ri.commit();
         assertEquals(0, m_ri.countTriples(null, null, null, 0));
         m_ri.addDigitalObject(dataobject);
         m_ri.commit();
         assertEquals(0, m_ri.countTriples(null, null, null, 0));
         
         m_ri.modifyDigitalObject(bdef);
         m_ri.commit();
         assertEquals(0, m_ri.countTriples(null, null, null, 0));
         m_ri.modifyDigitalObject(bmech);
         m_ri.commit();
         assertEquals(0, m_ri.countTriples(null, null, null, 0));
         m_ri.modifyDigitalObject(dataobject);
         m_ri.commit();
         assertEquals(0, m_ri.countTriples(null, null, null, 0));
     }
     
     public void testLevelOn() throws Exception {
        m_ri = new ResourceIndexImpl(ResourceIndex.INDEX_LEVEL_ON, m_conn, m_cPool, null);
         m_ri.addDigitalObject(bdef);
         m_ri.commit();
         assertEquals(17, m_ri.countTriples(null, null, null, 0));
         m_ri.addDigitalObject(bmech);
         m_ri.commit();
         assertEquals(39, m_ri.countTriples(null, null, null, 0));
         m_ri.addDigitalObject(dataobject);
         m_ri.commit();
         assertEquals(87, m_ri.countTriples(null, null, null, 0));
         
         m_ri.modifyDigitalObject(bdef);
         m_ri.commit();
         assertEquals(87, m_ri.countTriples(null, null, null, 0));
         m_ri.modifyDigitalObject(bmech);
         m_ri.commit();
         assertEquals(87, m_ri.countTriples(null, null, null, 0));
         m_ri.modifyDigitalObject(dataobject);
         m_ri.commit();
         assertEquals(87, m_ri.countTriples(null, null, null, 0));
     }
     
     public void testLevelWithPermutations() throws Exception {
        m_ri = new ResourceIndexImpl(ResourceIndex.INDEX_LEVEL_PERMUTATIONS, m_conn, m_cPool, null);
         
         m_ri.addDigitalObject(bdef);
         m_ri.commit();
         assertEquals(17, m_ri.countTriples(null, null, null, 0));
         m_ri.addDigitalObject(bmech);
         m_ri.commit();
         assertEquals(39, m_ri.countTriples(null, null, null, 0));
         m_ri.addDigitalObject(dataobject);
         m_ri.commit();
         assertEquals(112, m_ri.countTriples(null, null, null, 0));
         
         m_ri.modifyDigitalObject(bdef);
         m_ri.commit();
         assertEquals(112, m_ri.countTriples(null, null, null, 0));
         m_ri.modifyDigitalObject(bmech);
         m_ri.commit();
         assertEquals(112, m_ri.countTriples(null, null, null, 0));
         m_ri.modifyDigitalObject(dataobject);
         m_ri.commit();
         assertEquals(112, m_ri.countTriples(null, null, null, 0));
     }
 }
