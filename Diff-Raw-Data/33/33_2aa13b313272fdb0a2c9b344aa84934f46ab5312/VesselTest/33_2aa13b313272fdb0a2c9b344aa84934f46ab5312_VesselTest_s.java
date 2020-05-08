 package org.skullforge.asteroidpush.arena.entities;
 
 import org.junit.*;
 import org.jmock.*;
 import org.jmock.lib.legacy.ClassImposteriser;
 import org.newdawn.slick.Image;
 import org.skullforge.asteroidpush.arena.entities.Vessel;
 
 public class VesselTest {
    private Mockery context;
    private Vessel testVessel;
    private Image imageMock;
 
    @Before
    public void setUp() throws Exception {
       context = new Mockery() {
          {
             setImposteriser(ClassImposteriser.INSTANCE);
          }
       };
       imageMock = context.mock(Image.class);
       testVessel = new Vessel(imageMock);
    }
 
    @Test
    public void testRendering() throws Exception {
       // Jmock is unable to mock any classes with final methods, so we can not
       // test rendering without building a complete simulation.
    }
 
    @Test
    public void testSpawning() throws Exception {
       // Jmock is unable to mock any classes with final methods, so we can not
       // test spawning without building a complete simulation.
    }
 
    @Test
    public void testUpdating() throws Exception {
      testVessel.update(0);
    }
 
 }
