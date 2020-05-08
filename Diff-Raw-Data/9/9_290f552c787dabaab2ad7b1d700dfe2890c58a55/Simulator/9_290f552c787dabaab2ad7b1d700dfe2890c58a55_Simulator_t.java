 package prototype;
 
 import java.util.ArrayList;
 import javax.vecmath.Vector3d;
 import com.github.hiuprocon.pve.core.*;
 import com.github.hiuprocon.pve.obj.*;
 
 public class Simulator implements CollisionListener {
     PVEWorld w;
     PVEObject ground;
     PVEObject wall1;
     PVEObject wall2;
     PVEObject wall3;
     PVEObject wall4;
     PVEObject wall5;
     PVEObject wall6;
     PVEObject bar1;
     PVEObject bar2;
     PVEObject slope1;
     PVEObject slope2;
     Lifter lifter1;
     Lifter lifter2;
     PVEObject switch1;
     PVEObject switch2;
     PVEObject goal1;
     PVEObject goal2;
     Obstacle obstacle1;
     Obstacle obstacle2;
     CarA carA1;
     CarA carA2;
     CarB carB;
     ArrayList<Jewel> jewels = new ArrayList<Jewel>();
     SimulatorGUI gui;
 
     public Simulator() throws Exception {
         w = new PVEWorld(PVEWorld.A3CANVAS);
         gui = new SimulatorGUI(w.getMainCanvas());
         w.addCollisionListener(this);
         w.resume();
 
         ground = new BoxObj(Type.STATIC, 0, new Vector3d(250, 1, 100),
                 "x-res:///res/prototype/Ground.wrl");
         ground.setUserData("ground");
         ground.setLocRev(0, -0.5, 0, 0, 0, 0);
         w.add(ground);
 
         wall1 = new BoxObj(Type.STATIC, 0, new Vector3d(5, 5, 100),
                 "x-res:///res/Box.wrl");
         wall1.setUserData("wall1");
         wall1.setLocRev(-25, 2.5, 0, 0, 0, 0);
         w.add(wall1);
 
         wall2 = new BoxObj(Type.STATIC, 0, new Vector3d(5, 5, 100),
                 "x-res:///res/Box.wrl");
         wall2.setUserData("wall2");
         wall2.setLocRev(25, 2.5, 0, 0, 0, 0);
         w.add(wall2);
 
         wall3 = new BoxObj(Type.STATIC, 0, new Vector3d(250, 5, 1),
                 "x-res:///res/prototype/WireBox.wrl");
         wall3.setUserData("wall3");
         wall3.setLocRev(0, 2.5, -50.5, 0, 0, 0);
         w.add(wall3);
 
         wall4 = new BoxObj(Type.STATIC, 0, new Vector3d(250, 5, 1),
                 "x-res:///res/prototype/WireBox.wrl");
         wall4.setUserData("wall4");
         wall4.setLocRev(0, 2.5, 50.5, 0, 0, 0);
         w.add(wall4);
 
         wall5 = new BoxObj(Type.STATIC, 0, new Vector3d(1, 5, 100),
                 "x-res:///res/prototype/WireBox.wrl");
         wall5.setUserData("wall5");
         wall5.setLocRev(-125.5, 2.5, 0, 0, 0, 0);
         w.add(wall5);
 
         wall6 = new BoxObj(Type.STATIC, 0, new Vector3d(1, 5, 100),
                 "x-res:///res/prototype/WireBox.wrl");
         wall6.setUserData("wall6");
         wall6.setLocRev(125.5, 2.5, 0, 0, 0, 0);
         w.add(wall6);
 
         bar1 = new CylinderObj(Type.STATIC, 0, 100, 0.1, "x-res:///res/Cylinder.wrl");
         bar1.setUserData("bar1");
         bar1.setLocRev(-23, 6, 0, 90, 0, 0);
         w.add(bar1);
 
         bar2 = new CylinderObj(Type.STATIC, 0, 100, 0.1, "x-res:///res/Cylinder.wrl");
         bar2.setUserData("bar2");
         bar2.setLocRev( 23, 6, 0, 90, 0, 0);
         w.add(bar2);
 
         slope1 = new SlopeObj(10, 1, 10);
         slope1.setUserData("slope1");
         slope1.setLocRev(-42.5, 0.5, 0, 0, -90, 0);
         w.add(slope1);
 
         slope2 = new SlopeObj(10, 1, 10);
         slope2.setUserData("slope2");
         slope2.setLocRev(42.5, 0.5, 0, 0, 90, 0);
         w.add(slope2);
 
         lifter1 = new Lifter();
         lifter1.setUserData("elevator1");
        //lifter1.setLocRev(-32.5, 0.5, 0, 0, 0, 0);
        lifter1.setLocRev(-32.5, -0.5, 0, 0, 0, 0);
         w.add(lifter1);
 
         lifter2 = new Lifter();
         lifter2.setUserData("elevator2");
        //lifter2.setLocRev(32.5, 0.5, 0, 0, 0, 0);
        lifter2.setLocRev(32.5, -0.5, 0, 0, 0, 0);
         w.add(lifter2);
 
         switch1 = new BoxObj(Type.GHOST, 1, new Vector3d(10, 1, 10),
                 "x-res:///res/prototype/Switch.wrl");
         switch1.setUserData("switch1");
         switch1.setLocRev(-17.5, 0.5, 10, 0, 0, 0);
         w.add(switch1);
 
         switch2 = new BoxObj(Type.GHOST, 1, new Vector3d(10, 1, 10),
                 "x-res:///res/prototype/Switch.wrl");
         switch2.setUserData("switch2");
         switch2.setLocRev(17.5, 0.5, -10, 0, 0, 0);
         w.add(switch2);
 
         goal1 = new BoxObj(Type.GHOST, 1, new Vector3d(20, 1, 10),
                 "x-res:///res/prototype/Goal.wrl");
         goal1.setUserData("goal1");
         goal1.setLocRev(0, 0.5, 45, 0, 0, 0);
         w.add(goal1);
 
         goal2 = new BoxObj(Type.GHOST, 1, new Vector3d(20, 1, 10),
                 "x-res:///res/prototype/Goal.wrl");
         goal2.setUserData("goal2");
         goal2.setLocRev(0, 0.5, -45, 0, 0, 0);
         w.add(goal2);
 
         obstacle1 = new Obstacle(1,new Vector3d(-50, 1.5, 0), new Vector3d(-0.3,
                 0, 0.3));
         obstacle1.setUserData("obstacle1");
         w.add(obstacle1);
 
         obstacle2 = new Obstacle(2,new Vector3d( 50, 1.5, 0), new Vector3d(0.4,
                 0, -0.5));
         obstacle2.setUserData("obstacle2");
         w.add(obstacle2);
 
         carA1 = new CarA(this, 10000);
         carA1.setUserData("carA1");
         carA1.setLocRev(-80, 0, 0, 0, 90, 0);
         w.add(carA1);
         gui.setCarA1(carA1);
 
         carA2 = new CarA(this, 20000);
         carA2.setUserData("carA2");
         carA2.setLocRev(80, 0, 0, 0, -90, 0);
         w.add(carA2);
         gui.setCarA2(carA2);
 
         carB = new CarB(this, 30000);
         carB.setUserData("carB");
         carB.setLocRev(0, 0, 0, 0, 90, 0);
         w.add(carB);
         gui.setCarB(carB);
 
         for (int i = 0; i < 10; i++) {
             Jewel j = new Jewel();
             j.setUserData("jA1." + i);
             double x = 70 * Math.random() - 50 - 80;
             double z = 70 * Math.random() - 50;
             j.setLocRev(x, 2, z, 0, 0, 0);
             w.add(j);
             jewels.add(j);
         }
         for (int i = 0; i < 10; i++) {
             Jewel j = new Jewel();
             j.setUserData("jA2." + i);
             double x = 70 * Math.random() - 50 + 80;
             double z = 70 * Math.random() - 50;
             j.setLocRev(x, 2, z, 0, 0, 0);
             w.add(j);
             jewels.add(j);
         }
     }
 
     @Override
     public void collided(PVEPart a, PVEPart b) {
         PVEObject aa = a.getObject();
         PVEObject bb = b.getObject();
         if (aa == ground || bb == ground)
             return;
         // System.out.println("collided "+(String)(aa.getUserData())+":"+
         // ((String)bb.getUserData()));
         if (aa == switch1 && bb == carB)
             lifter1.setUp();
         if (aa == carB && bb == switch1)
             lifter1.setUp();
         if (aa == switch2 && bb == carB)
             lifter2.setUp();
         if (aa == carB && bb == switch2)
             lifter2.setUp();
         if ((aa == goal1 || aa == goal2) && bb instanceof Jewel) {
             gui.appendText("goal!");
             w.del(bb);
         }
         if (aa instanceof Jewel && (bb == goal1 || bb == goal2)) {
             gui.appendText("goal!");
             w.del(aa);
         }
 
     }
     String searchJewels() {
         String s = ""+jewels.size();
         for (Jewel j:jewels) {
             Vector3d v = j.getLoc();
             s = s +" "+j.getUserData().toString()+" "+v.x+" "+v.y+" "+v.z;
         }
         return s;
     }
 
     public static void main(String args[]) throws Exception {
         new Simulator();
     }
 }
