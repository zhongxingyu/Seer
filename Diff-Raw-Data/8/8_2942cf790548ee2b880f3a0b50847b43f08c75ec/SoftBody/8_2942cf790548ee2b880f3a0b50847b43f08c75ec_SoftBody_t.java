 package net.chozabu.gexEdit;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.CircleShape;
 import com.badlogic.gdx.physics.box2d.Fixture;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.Transform;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
 import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
 import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
 import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
 
 public class SoftBody {
 	static float minDis;
 	List<PhysObject> physObject;
 	World world;
 	TextureRegion region;
 	boolean isLoop;
 	
 	PhysObject centreBody;
 	public void draw(SpriteBatch batch) {
         Iterator<PhysObject> it=physObject.iterator();
         while(it.hasNext())
         {
         	PhysObject cObj=(PhysObject)it.next();
         	cObj.draw(batch);
           //System.out.println("Value :"+value);
         }
 		/*for (Iterator<Body> iter = body; iter.hasNext();) {
             Body body = iter.next();
             Transform transform = body.getTransform();
             sprite.setPosition(transform.getPosition().x-sprite.getWidth()/2, transform.getPosition().y-sprite.getHeight()/2);
             sprite.setRotation(transform.getRotation()*MathUtils.radiansToDegrees);
         }*/
         //Transform transform = body.getTransform();
         //sprite.setPosition(transform.getPosition().x-sprite.getWidth()/2, transform.getPosition().y-sprite.getHeight()/2);
         //System.out.println(transform.getRotation()*MathUtils.radiansToDegrees);
 	}
 	public void setup(World pworld, TextureRegion pregion){
 		region = pregion;
 		world = pworld;
 		physObject=new ArrayList<PhysObject>();
 		isLoop = false;
 	}
 	
 	private float lastX, lastY;
 	
 	public void condAddAt(float xP,float yP){
 		float hardRad = 2.f;
 		float xd=xP-lastX;
 		float yd=yP-lastY;
 		float disSqr = xd*xd+yd*yd;
 		if (disSqr>2*2){
 			float dis = (float)Math.sqrt((double)disSqr);
 			xd/=dis;
 			yd/=dis;
 			xP=lastX+xd*hardRad;
 			yP=lastY+yd*hardRad;
 		}
 		if (disSqr>hardRad*hardRad){//TODO unhardcode this
 			queCircle(1, xP, yP);
 		}
 	}
 	
 	public void queCircle(float rad,float x, float y) {
         PhysObject pObj = new PhysObject();
         pObj.setDefCircle(1.f,x,y, region,BodyType.DynamicBody);
         physObject.add(pObj);
 		lastX = x;
 		lastY = y;
         
 	}
 	
 	public void joinCentre(Body ba, Body bb){
 		
 	}
 
 	public void reset(){
 		createFromDef();
 	}
 	public void destroyBody(){
         Iterator<PhysObject> it=physObject.iterator();
         while(it.hasNext())
         {
         	PhysObject cObj=(PhysObject)it.next();
         	cObj.destroyBody(world);
         }
 	}
 	public void dispose(){
         Iterator<PhysObject> it=physObject.iterator();
         while(it.hasNext())
         {
         	PhysObject cObj=(PhysObject)it.next();
         	cObj.dispose();
         }
         
 	}
 	public void createFromDef(){
         Vector2 avgPos = new Vector2(0,0);
         Iterator<PhysObject> it=physObject.iterator();
         List<PhysObject> rems;
         rems = new LinkedList<PhysObject>();
         while(it.hasNext())
         {
         	PhysObject cObj=(PhysObject)it.next();
         	if(cObj.shape == null){
         		rems.add(cObj);
         	}
         }
         it=rems.iterator();
         while(it.hasNext())
         {
         	PhysObject cObj=(PhysObject)it.next();
         		physObject.remove(cObj);
         }
         it=physObject.iterator();
         while(it.hasNext())
         {
         	PhysObject cObj=(PhysObject)it.next();
         	cObj.createFromDef(world);
         	avgPos.add(cObj.body.getPosition());
         	//if (cObj.body == null)
         }
         avgPos.div(physObject.size());
         
         PhysObject lastObj = null;
         //physObject.get(physObject.size()-1);
         //System.out.println(lastObj);
         PhysObject lastObj2 = null;//physObject.get(physObject.size()-2);
         PhysObject lastObj3 = null;//physObject.get(physObject.size()-2);
         if (isLoop){
         	lastObj = physObject.get(physObject.size()-1);
         	lastObj2 = physObject.get(physObject.size()-2);
         	if(physObject.size()>2)
         	lastObj3 = physObject.get(physObject.size()-3);
         }
         /*if (centreBody!=null){
         	centreBody.dispose();
         }
         centreBody = new PhysObject();
         centreBody.createCircle(world, 1, avgPos.x, avgPos.y, region, BodyType.DynamicBody);
         */
         //lastObj2.createFromDef(world);
         it=physObject.iterator();
         int index = 0; int olen = physObject.size();
         while(it.hasNext())
         {
         	PhysObject cObj=(PhysObject)it.next();
         	//if (cObj.body == null)
         	//cObj.createFromDef(world);
         	if (lastObj!=null){
         		//connect neighbouring vertices
         		//DistanceJointDef mjd = new DistanceJointDef();
         		WeldJointDef mjd = new WeldJointDef();
         		mjd.initialize(cObj.body, lastObj.body, cObj.body.getPosition());//,lastObj.body.getPosition());
         		//mjd.dampingRatio=0.f;
         		//mjd.frequencyHz=5f;
         		world.createJoint(mjd);
         		
         		///connect distant sides//
         		PhysObject poR = physObject.get((index+olen/4)%olen);
         		if (poR.body!=cObj.body){
         		DistanceJointDef mjdR = new DistanceJointDef();
         		//PrismaticJointDef mjdR = new PrismaticJointDef();
         		mjdR.initialize(cObj.body, poR.body, cObj.body.getPosition(),poR.body.getPosition());
         		mjdR.dampingRatio=0.5f;
         		mjdR.frequencyHz=5;
         		//Vector2 axis=cObj.body.getPosition().cpy().sub(poR.body.getPosition());
         		//mjdR.initialize(cObj.body, poR.body, cObj.body.getPosition(),axis.nor());
         		world.createJoint(mjdR);
         		}
         		//*/
         		/*/connect centre sides//
         		PhysObject poR = centreBody;
         		DistanceJointDef mjdR = new DistanceJointDef();
         		//PrismaticJointDef mjdR = new PrismaticJointDef();
         		mjdR.initialize(cObj.body, poR.body, cObj.body.getPosition(),poR.body.getPosition());
         		mjdR.dampingRatio=0.5f;
         		mjdR.frequencyHz=5;
         		//Vector2 axis=cObj.body.getPosition().cpy().sub(poR.body.getPosition());
         		//mjdR.initialize(cObj.body, poR.body, cObj.body.getPosition(),axis.nor());
         		world.createJoint(mjdR);//*/
         		
             	if (lastObj2!=null){
             		DistanceJointDef mjd2 = new DistanceJointDef();
             		mjd2.initialize(cObj.body, lastObj2.body, cObj.body.getPosition(),lastObj2.body.getPosition());
             		mjd2.dampingRatio=0.5f;
             		mjd2.frequencyHz=5f;
             		DistanceJoint tj = (DistanceJoint)world.createJoint(mjd2);
                 	if (lastObj3!=null) if(cObj.body != lastObj3.body){
                 		DistanceJointDef mjd3 = new DistanceJointDef();
                 		mjd3.initialize(cObj.body, lastObj3.body, cObj.body.getPosition(),lastObj3.body.getPosition());
                 		mjd3.dampingRatio=0.5f;
                 		mjd3.frequencyHz=5f;
                 		world.createJoint(mjd3);
                 	}
             	}
         	}
         	lastObj3 = lastObj2;
         	lastObj2 = lastObj;
         	lastObj = cObj;
         	
           //System.out.println("Value :"+value);
         	index++;
         }
 		
 	}
 	public void makeLoop() {
 		isLoop = true;
 		
 	}
 	public boolean containsBody(Body checkMe) {
         Iterator<PhysObject> it=physObject.iterator();
         while(it.hasNext())
         {
         	PhysObject cObj=(PhysObject)it.next();
         	if(cObj.body == checkMe){
         		return true;
         	}
         }
 		return false;
 	}
	public void defFromPos() {
        Iterator<PhysObject> it=physObject.iterator();
        while(it.hasNext())
        {
        	PhysObject cObj=(PhysObject)it.next();
        	cObj.defFromPos();
        }
	}
 
 }
