 package org.blink.game.model.misc;
 
 import com.jme3.asset.AssetManager;
 import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.queue.RenderQueue.Bucket;
 import com.jme3.renderer.queue.RenderQueue.ShadowMode;
 import com.jme3.scene.Node;
 import org.blink.game.Blink;
 
 /**
  *
  * @author cmessel
  */
 public class Tree extends Node {
 
     public Tree(Blink app, Vector3f position, float scale) {
         super("Tree");
 
         AssetManager assetManager = app.getAssetManager();
 
         Node geom = (Node) assetManager.loadModel("Models/Tree/Tree2.mesh.xml");
         geom.setQueueBucket(Bucket.Opaque);
         geom.setShadowMode(ShadowMode.Cast);
         attachChild(geom);
 
        /*RoundSparkEmitter de = new RoundSparkEmitter(assetManager);
         de.setParticlesPerSec(15);
         de.setStartColor(new ColorRGBA(0f, 1f, 0f, (float) (1.0 / 1)));
         de.setEndColor(new ColorRGBA(0f, 0f, 0, (float) (0.5f / 1)));
         de.emitAllParticles();
 
         de.setLocalTranslation(0, 2f, 0);
 
         attachChild(de);
         * TODO: the spark was buggy so I removed it
         */
 
         setLocalTranslation(position);
         setLocalScale(scale);
 
         CapsuleCollisionShape capsule = new CapsuleCollisionShape(1f, 1f);
         RigidBodyControl ball_phy = new RigidBodyControl(capsule, 0f);
         /** Add physical ball to physics space. */
         geom.addControl(ball_phy);
         app.getBulletAppState().getPhysicsSpace().add(ball_phy);
     }
 }
