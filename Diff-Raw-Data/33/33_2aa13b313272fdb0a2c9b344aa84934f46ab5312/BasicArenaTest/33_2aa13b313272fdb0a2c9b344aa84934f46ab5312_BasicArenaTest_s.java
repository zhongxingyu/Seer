 package org.skullforge.asteroidpush.arena;
 
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.World;
 import org.jmock.*;
 import org.jmock.lib.legacy.ClassImposteriser;
 import org.junit.*;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 
 public class BasicArenaTest {
    Mockery context;
    BasicArena testArena;
    GameContainer containerMock;
    Graphics graphicsMock;
    Viewport viewportMock;
    EntityFactory factoryMock;
    Entity objectMock1;
    Entity objectMock2;
 
    @Before
    public void setUp() throws Exception {
       context = new Mockery() {
          {
             setImposteriser(ClassImposteriser.INSTANCE);
          }
       };
       factoryMock = context.mock(EntityFactory.class, "factory");
       testArena = new BasicArena(factoryMock);
       containerMock = context.mock(GameContainer.class, "container");
       graphicsMock = context.mock(Graphics.class, "graphics");
       viewportMock = context.mock(Viewport.class, "viewport");
       objectMock1 = context.mock(Entity.class, "object1");
       objectMock2 = context.mock(Entity.class, "object2");
    }
 
    @Test
    public void testInit() throws Exception {
       final Vec2 pos = new Vec2(3.0f, 3.0f);
       final Vec2 sceneryPos = new Vec2(0.0f, 0.0f);
       
       context.checking(new Expectations() {
          {
             oneOf(factoryMock).createVessel();
             will(returnValue(objectMock1));
             oneOf(factoryMock).createScenery();
             will(returnValue(objectMock2));
             oneOf(objectMock1).spawn(with(any(World.class)), with(equal(pos)));
             oneOf(objectMock2).spawn(with(any(World.class)), with(equal(sceneryPos)));
          }
       });
 
       testArena.init();
 
       context.assertIsSatisfied();
    }
    
    @Test
    public void testAddingObjects() throws Exception {
       final Vec2 pos1 = new Vec2(2.0f, 2.0f);
       final Vec2 pos2 = new Vec2(3.0f, 3.0f);
      final int expectedDelta = 25;
       context.checking(new Expectations() {
          {
             oneOf(viewportMock).setGraphics(containerMock, graphicsMock);
             oneOf(objectMock1).spawn(with(any(World.class)), with(equal(pos1)));
             oneOf(objectMock2).spawn(with(any(World.class)), with(equal(pos2)));
             oneOf(objectMock1).render(viewportMock);
             oneOf(objectMock2).render(viewportMock);
            oneOf(objectMock1).update(expectedDelta);
            oneOf(objectMock2).update(expectedDelta);
          }
       });
       testArena.addObject(objectMock1, pos1);
       testArena.addObject(objectMock2, pos2);
       testArena.setViewport(viewportMock);
      testArena.update(expectedDelta);
       testArena.render(containerMock, graphicsMock);
       context.assertIsSatisfied();
    }
 
    @Test
    public void testMissingViewport() throws Exception {
       context.checking(new Expectations() {
          {
             oneOf(graphicsMock).drawString("NO VISUAL SIGNAL CONNECTED",
                                            25.0f,
                                            25.0f);
          }
       });
 
       testArena.render(containerMock, graphicsMock);
 
       context.assertIsSatisfied();
    }
 
 }
