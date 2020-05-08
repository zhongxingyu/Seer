 package com.irr310.common.engine;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.vecmath.Vector3f;
 
 import org.apache.commons.lang3.tuple.ImmutablePair;
 import org.apache.commons.lang3.tuple.Pair;
 
 import com.bulletphysics.collision.broadphase.BroadphaseInterface;
 import com.bulletphysics.collision.broadphase.BroadphasePair;
 import com.bulletphysics.collision.broadphase.BroadphaseProxy;
 import com.bulletphysics.collision.broadphase.DbvtBroadphase;
 import com.bulletphysics.collision.broadphase.DispatcherInfo;
 import com.bulletphysics.collision.broadphase.OverlapFilterCallback;
 import com.bulletphysics.collision.dispatch.CollisionDispatcher;
 import com.bulletphysics.collision.dispatch.CollisionWorld.RayResultCallback;
 import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
 import com.bulletphysics.collision.dispatch.DefaultNearCallback;
 import com.bulletphysics.collision.dispatch.NearCallback;
 import com.bulletphysics.collision.dispatch.CollisionWorld.LocalRayResult;
 import com.bulletphysics.collision.narrowphase.ManifoldPoint;
 import com.bulletphysics.collision.narrowphase.PersistentManifold;
 import com.bulletphysics.collision.shapes.BoxShape;
 import com.bulletphysics.collision.shapes.CollisionShape;
 import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
 import com.bulletphysics.dynamics.DynamicsWorld;
 import com.bulletphysics.dynamics.InternalTickCallback;
 import com.bulletphysics.dynamics.RigidBody;
 import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
 import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
 import com.bulletphysics.dynamics.constraintsolver.Generic6DofConstraint;
 import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
 import com.bulletphysics.linearmath.Clock;
 import com.bulletphysics.linearmath.MatrixUtil;
 import com.bulletphysics.linearmath.MotionState;
 import com.bulletphysics.linearmath.Transform;
 import com.bulletphysics.util.ObjectArrayList;
 import com.irr310.common.Game;
 import com.irr310.common.event.CelestialObjectAddedEvent;
 import com.irr310.common.event.CollisionEvent;
 import com.irr310.common.event.DefaultEngineEventVisitor;
 import com.irr310.common.event.EngineEvent;
 import com.irr310.common.event.PauseEngineEvent;
 import com.irr310.common.event.QuitGameEvent;
 import com.irr310.common.event.StartEngineEvent;
 import com.irr310.common.event.WorldShipAddedEvent;
 import com.irr310.common.tools.TransformMatrix;
 import com.irr310.common.tools.Vect3;
 import com.irr310.common.world.Component;
 import com.irr310.common.world.Link;
 import com.irr310.common.world.Part;
 import com.irr310.common.world.Ship;
 import com.irr310.common.world.Slot;
 import com.irr310.common.world.WorldObject;
 import com.irr310.common.world.capacity.Capacity;
 import com.irr310.common.world.capacity.LinearEngineCapacity;
 import com.irr310.common.world.capacity.WingCapacity;
 import com.irr310.server.Duration;
 
 public class PhysicEngine extends FramerateEngine {
 
     public static final float PI_2 = 1.57079632679489661923f;
 
     // keep the collision shapes, for deletion/cleanup
     private ObjectArrayList<CollisionShape> collisionShapes = new ObjectArrayList<CollisionShape>();
     private BroadphaseInterface broadphase;
     private CollisionDispatcher dispatcher;
     private ConstraintSolver solver;
     private DefaultCollisionConfiguration collisionConfiguration;
     protected Clock clock = new Clock();
 
     // this is the most important class
     protected DynamicsWorld dynamicsWorld = null;
     private Map<Part, RigidBody> partToBodyMap;
 
     private List<Pair<LinearEngineCapacity, RigidBody>> linearEngines;
     private List<Pair<WingCapacity, RigidBody>> wings;
 
     public PhysicEngine() {
         framerate = new Duration(10000000); // 10 ms
 
         linearEngines = new ArrayList<Pair<LinearEngineCapacity, RigidBody>>();
         wings = new ArrayList<Pair<WingCapacity, RigidBody>>();
 
         initPhysics();
     }
 
     @Override
     protected void frame() {
         // Apply forces
 
         // Linear Engines
         for (Pair<LinearEngineCapacity, RigidBody> linearEngine : linearEngines) {
             RigidBody body = linearEngine.getRight();
             Transform t = new Transform();
             body.getWorldTransform(t);
 
             TransformMatrix force = TransformMatrix.identity();
             force.translate(new Vect3(0, linearEngine.getLeft().getCurrentThrust(), 0));
 
             TransformMatrix rotation = new TransformMatrix();
             t.getOpenGLMatrix(rotation.getData());
             rotation.setTranslation(0, 0, 0);
             force.preMultiply(rotation);
 
             body.applyCentralForce(force.getTranslation().toVector3f());
             body.setActivationState(RigidBody.ACTIVE_TAG);
         }
 
         // wings
         for (Pair<WingCapacity, RigidBody> wing : wings) {
             RigidBody body = wing.getRight();
             WingCapacity wingCapacity = wing.getLeft();
 
             // Get Transform
             Transform t = new Transform();
             body.getWorldTransform(t);
             TransformMatrix bodyTransform = TransformMatrix.identity();
             t.getOpenGLMatrix(bodyTransform.getData());
 
             Vect3 breakAxis = wingCapacity.getBreakAxis();
            Vect3 absoluteBreakAxis = breakAxis.rotate(bodyTransform);
 
             Vect3 thrustAxis = wingCapacity.getThrustAxis();
            Vect3 absoluteThrustAxis = thrustAxis.rotate(bodyTransform);
 
             Vector3f lv = new Vector3f();
             body.getLinearVelocity(lv);
             Vect3 velocity = new Vect3(lv);
 
             if (velocity.length() > 0) {
 
                 // Resistance
                 double opposition = velocity.dot(absoluteBreakAxis);
                 body.applyCentralForce(absoluteBreakAxis.multiply(opposition * wingCapacity.getFriction() * -1).toVector3f());
 
                 // Conversion
                 double conversion = velocity.dot(absoluteThrustAxis) / velocity.length();
 
                 body.applyCentralForce(absoluteThrustAxis.multiply(conversion * Math.abs(opposition) * wingCapacity.getFriction()
                         * wingCapacity.getYield()).toVector3f());
             }
             body.setActivationState(RigidBody.ACTIVE_TAG);
 
         }
 
         Game.getInstance().getWorld().lock();
 
         // step the simulation
         if (dynamicsWorld != null) {
             dynamicsWorld.stepSimulation(framerate.getSeconds());
         }
 
         Game.getInstance().getWorld().unlock();
         
     }
 
     public float getDeltaTimeMicroseconds() {
         float dt = clock.getTimeMicroseconds();
         clock.reset();
         return dt;
     }
     
     public RayResultDescriptor rayTest(Vect3 from, Vect3 to) {
         
        System.out.println("begin ray test from "+from+" to "+ to);
         
         dynamicsWorld.rayTest(from.toVector3f(), to.toVector3f(), new RayResultCallback() {
             
             @Override
             public float addSingleResult(LocalRayResult rayResult, boolean normalInWorldSpace) {
                 System.out.println("ray test result !");
                 
                UserData data = (UserData) ((RigidBody) rayResult.collisionObject).getUserPointer();
                System.out.println("hit on "+data.part.getParentObject().getName()+" at "+ rayResult.hitFraction);
                 return 0;
             }
         });
         
         
         System.out.println("end ray test");
         
         
         return null;
     }
 
     public void initPhysics() {
 
         partToBodyMap = new HashMap<Part, RigidBody>();
 
         // collision configuration contains default setup for memory, collision
         // setup
         collisionConfiguration = new DefaultCollisionConfiguration();
 
         // use the default collision dispatcher. For parallel processing you can
         // use a diffent dispatcher (see Extras/BulletMultiThreaded)
         dispatcher = new CollisionDispatcher(collisionConfiguration);
 
         broadphase = new DbvtBroadphase();
 
         
         
         // the default constraint solver. For parallel processing you can use a
         // different solver (see Extras/BulletMultiThreaded)
         SequentialImpulseConstraintSolver sol = new SequentialImpulseConstraintSolver();
         solver = sol;
 
         // TODO: needed for SimpleDynamicsWorld
         // sol.setSolverMode(sol.getSolverMode() &
         // ~SolverMode.SOLVER_CACHE_FRIENDLY.getMask());
 
         dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
 
         dynamicsWorld.setInternalTickCallback(new CollisionDetectionCallback(), null);
         
         // No gravity
         dynamicsWorld.setGravity(new Vector3f(0f, 0f, 0f));
 
         dynamicsWorld.getPairCache().setOverlapFilterCallback(new OverlapFilterCallback() {
 
             
             
             @Override
             public boolean needBroadphaseCollision(BroadphaseProxy proxy0, BroadphaseProxy proxy1) {
 
                 UserData data0 = (UserData) ((RigidBody) proxy0.clientObject).getUserPointer();
                 UserData data1 = (UserData) ((RigidBody) proxy1.clientObject).getUserPointer();
 
                 
 
                 if (data0 != null && data1 != null) {
                     if(data0.ship != null && data0.ship == data1.ship) {
                         return false;
                     }
                 }
                 
                 
                 // System.out.println("valid collision");
                 return true;
             }
         });
         
         
         dispatcher.setNearCallback(new NearCallback() {
             
             DefaultNearCallback defaultNearCallback = new DefaultNearCallback();
             
             @Override
             public void handleCollision(BroadphasePair collisionPair, CollisionDispatcher dispatcher, DispatcherInfo dispatchInfo) {
                 
                 UserData data0 = (UserData) ((RigidBody) collisionPair.pProxy0.clientObject).getUserPointer();
                 UserData data1 = (UserData) ((RigidBody) collisionPair.pProxy1.clientObject).getUserPointer();
                 
                 if(data0.part.getParentObject().isBroken()) {
                     return;
                 }
                 
                 if(data1.part.getParentObject().isBroken()) {
                     return;
                 }
                 
                 defaultNearCallback.handleCollision(collisionPair, dispatcher, dispatchInfo);
             }
         });
         
 
     }
 
     protected void addObject(WorldObject object) {
         for (final Part part : object.getParts()) {
             addPart(part, new UserData());
         }
     }
 
     public void reloadStates() {
         for (Entry<Part, RigidBody> partEntry : partToBodyMap.entrySet()) {
             PartMotionState motionState = (PartMotionState) partEntry.getValue().getMotionState();
             motionState.reload();
         }
     }
 
     protected void addShip(Ship ship, Vect3 position) {
 
         for (Component component : ship.getComponents()) {
             for (final Part part : component.getParts()) {
 
                 if (position != null) {
                     part.getTransform().rotate(component.getShipRotation());
 
                     part.getTransform().translate(position.plus(component.getShipPosition()));
                 }
 
                 UserData userData = new UserData();
                 userData.ship = ship;
                 RigidBody rigidBody = addPart(part, userData);
                 partToBodyMap.put(part, rigidBody);
                 
                 
                 rigidBody.setUserPointer(userData);
             }
 
             for (Capacity capacity : component.getCapacities()) {
                 if (capacity instanceof LinearEngineCapacity) {
                     linearEngines.add(new ImmutablePair<LinearEngineCapacity, RigidBody>((LinearEngineCapacity) capacity,
                                                                                          partToBodyMap.get(component.getFirstPart())));
                 }
 
                 if (capacity instanceof WingCapacity) {
                     wings.add(new ImmutablePair<WingCapacity, RigidBody>((WingCapacity) capacity, partToBodyMap.get(component.getFirstPart())));
                 }
 
             }
         }
 
         for (Link link : ship.getLinks()) {
             addLink(link);
         }
 
     }
 
     protected void addLink(Link link) {
         Slot slot1 = link.getSlot1();
         Slot slot2 = link.getSlot2();
         RigidBody body1 = partToBodyMap.get(slot1.getPart());
         RigidBody body2 = partToBodyMap.get(slot2.getPart());
 
         Vect3 shipRotation1 = slot1.getComponent().getShipRotation();
         Vect3 shipRotation2 = slot2.getComponent().getShipRotation();
 
         Transform localA = new Transform(), localB = new Transform();
         localA.setIdentity();
         Vect3 position1 = slot1.getPosition();
 
         localA.origin.set(position1.x.floatValue(), position1.y.floatValue(), position1.z.floatValue());
 
         MatrixUtil.setEulerZYX(localA.basis,
                                (float) -Math.toRadians(shipRotation1.z),
                                (float) -Math.toRadians(shipRotation1.y),
                                (float) -Math.toRadians(shipRotation1.x));
 
         localB.setIdentity();
         Vect3 position2 = slot2.getPosition();
         localB.origin.set(position2.x.floatValue(), position2.y.floatValue(), position2.z.floatValue());
 
         MatrixUtil.setEulerZYX(localB.basis,
                                (float) -Math.toRadians(shipRotation2.z),
                                (float) -Math.toRadians(shipRotation2.y),
                                (float) -Math.toRadians(shipRotation2.x));
 
         Generic6DofConstraint constraint = new Generic6DofConstraint(body1, body2, localA, localB, false);
         constraint.setLinearLowerLimit(new Vector3f());
         constraint.setLinearUpperLimit(new Vector3f());
 
         constraint.setAngularLowerLimit(new Vector3f(0, 0, 0));
         constraint.setAngularUpperLimit(new Vector3f(0, 0, 0));
 
         dynamicsWorld.addConstraint(constraint, true);
 
     }
 
     protected RigidBody addPart(Part part, UserData userData) {
         // create a few dynamic rigidbodies
         // Re-using the same collision is better for memory usage and
         // performance
 
         CollisionShape colShape = new BoxShape(part.getShape().divide(2).toVector3f());
         // CollisionShape colShape = new SphereShape(1f);
         collisionShapes.add(colShape);
 
         // Create Dynamic Objects
 
         float mass = part.getMass().floatValue() / 10;
 
         // rigidbody is dynamic if and only if mass is non zero, otherwise
         // static
         boolean isDynamic = (mass != 0f);
 
         Vector3f localInertia = new Vector3f(0, 0, 0);
         if (isDynamic) {
             colShape.calculateLocalInertia(mass*10, localInertia);
         }
 
         // TODO rotation
 
         // using motionstate is recommended, it provides interpolation
         // capabilities, and only synchronizes 'active' objects
         PartMotionState myMotionState = new PartMotionState(part);
         RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass, myMotionState, colShape, localInertia);
         RigidBody body = new RigidBody(rbInfo);
         userData.part  = part;
         body.setUserPointer(userData);
         
         myMotionState.setBody(body);
 
         body.setActivationState(RigidBody.ISLAND_SLEEPING);
         body.setDamping(0.1f, 0.8f);
         body.setSleepingThresholds(0.001f, 0.001f);
         // body.setDeactivationTime(deactivationTime)
 
         dynamicsWorld.addRigidBody(body);
 
         body.setLinearVelocity(part.getLinearSpeed().toVector3f());
         body.setAngularVelocity(part.getRotationSpeed().toVector3f());
         body.setActivationState(RigidBody.ACTIVE_TAG);
         body.setCcdMotionThreshold(1f);
         body.setCcdSweptSphereRadius(0.2f);
         return body;
 
     }
 
     private final class CollisionDetectionCallback extends InternalTickCallback {
         @Override
         public void internalTick(DynamicsWorld world, float timeStep) {
             int numManifolds = world.getDispatcher().getNumManifolds();
             for (int i=0;i<numManifolds;i++)
             {
                 PersistentManifold contactManifold =  world.getDispatcher().getManifoldByIndexInternal(i);
                 RigidBody obA = (RigidBody) contactManifold.getBody0();
                 RigidBody obB = (RigidBody) contactManifold.getBody1();
             
                 int numContacts = contactManifold.getNumContacts();
                 for (int j=0;j<numContacts;j++)
                 {
                     ManifoldPoint pt = contactManifold.getContactPoint(j);
                     if (pt.getDistance()<0.f)
                     {
                         Vector3f ptA = new Vector3f();
                         Vector3f ptB = new Vector3f();
                         
                         
                         
                         
                         Vector3f vA = new Vector3f();
                         Vector3f vB = new Vector3f();
                         
                         
                         pt.getPositionWorldOnA(ptA);
                         pt.getPositionWorldOnB(ptB);
                         obA.getLinearVelocity(vA);
                         obB.getLinearVelocity(vB);
                         
                         
                         CollisionDescriptor collisionDescriptor = new CollisionDescriptor();
                         Part partA = ((UserData)obA.getUserPointer()).part;
                         Part partB = ((UserData)obB.getUserPointer()).part;
                         
                         
                         collisionDescriptor.setPartA(partA);
                         collisionDescriptor.setPartB(partB);
                         
                         Vect3 localPositionA = new Vect3(pt.localPointA);
                         Vect3 globalPositionA = new Vect3(ptA);
                         Vect3 localPositionB = new Vect3(pt.localPointB);
                         Vect3 globalPositionB =new Vect3(ptB);
                         
                         Vect3 globalPosition = globalPositionA.plus(globalPositionB).divide(2);
                         
                         collisionDescriptor.setLocalPositionOnA(localPositionA);
                         collisionDescriptor.setLocalPositionOnB(localPositionB);
                         collisionDescriptor.setGlobalPosition(globalPosition);
                         
                         collisionDescriptor.setImpulse(pt.appliedImpulse);
                         
                         Game.getInstance().sendToAll(new CollisionEvent(collisionDescriptor));
                        
                         
                     }
                 }
             }
             
         }
     }
 
     public class UserData {
 
         public Part part = null;
         public Ship ship = null;
 
     }
 
     public class PartMotionState extends MotionState {
 
         private final Part part;
         private RigidBody body;
 
         PartMotionState(Part part) {
             this.part = part;
 
         }
 
         public void reload() {
             Transform transform = new Transform();
             getWorldTransform(transform);
             body.setWorldTransform(transform);
             body.setLinearVelocity(part.getLinearSpeed().toVector3f());
             body.setAngularVelocity(part.getRotationSpeed().toVector3f());
             body.setActivationState(RigidBody.ACTIVE_TAG);
         }
 
         public void setBody(RigidBody body) {
             this.body = body;
         }
 
         @Override
         public Transform getWorldTransform(Transform out) {
             out.setIdentity();
             // out.origin.set(part.getTransform().getTranslation().toVector3d());
             out.setFromOpenGLMatrix(part.getTransform().getData());
             return out;
         }
 
         @Override
         public void setWorldTransform(Transform worldTrans) {
             Vector3f origin = worldTrans.origin;
             part.getTransform().translate(origin.x, origin.y, origin.z);
             worldTrans.getOpenGLMatrix(part.getTransform().getData());
 
             Vector3f lv = new Vector3f();
             body.getLinearVelocity(lv);
             part.getLinearSpeed().set(lv);
 
             Vector3f av = new Vector3f();
             body.getAngularVelocity(av);
             part.getRotationSpeed().set(av);
 
             part.getTransform().fireChanged();
         }
     }
 
     @Override
     protected void processEvent(EngineEvent e) {
         e.accept(new PhysicEngineEventVisitor());
     }
 
     private final class PhysicEngineEventVisitor extends DefaultEngineEventVisitor {
         @Override
         public void visit(QuitGameEvent event) {
             System.out.println("stopping physic engine");
             setRunning(false);
         }
 
         @Override
         public void visit(StartEngineEvent event) {
             pause(false);
         }
 
         @Override
         public void visit(PauseEngineEvent event) {
             pause(true);
 
         }
 
         @Override
         public void visit(CelestialObjectAddedEvent event) {
             addObject(event.getObject());
         }
 
         @Override
         public void visit(WorldShipAddedEvent event) {
             addShip(event.getShip(), event.getPosition());
         }
 
     }
 
     @Override
     protected void init() {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     protected void end() {
         // TODO Auto-generated method stub
 
     }
 
 }
