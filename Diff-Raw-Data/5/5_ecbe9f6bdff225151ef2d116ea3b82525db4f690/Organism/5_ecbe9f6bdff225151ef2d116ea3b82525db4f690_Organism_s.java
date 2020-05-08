 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package jgpfun;
 
 import jgpfun.world2d.FoodFinder;
 import jgpfun.jgp.OpCode;
 import jgpfun.jgp.EvoVM2;
 import java.security.SecureRandom;
 import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
 import java.util.List;
import java.util.Map;
 import java.util.Random;
 import jgpfun.world2d.Body2d;
 import jgpfun.world2d.WallSense;
 
 /*
  * TODO: Create a loopback sense that represents the differential (ableitung)
  * of an output. Also create an integrator. This should ease temporal
  * memory functions.
  */
 /**
  *
  * @author hansinator
  */
 public class Organism implements Comparable<Organism> {
 
     public static final double maxForce = 10.0;
 
     public static final double maxSpeed = 4.0;
 
     protected static final Random rnd = new SecureRandom();
 
     //protected final int registerCount = 24;
     protected final int registerCount = 32;
 
     protected final EvoVM2 vm;
 
     protected final List<OpCode> program;
 
     public final Body2d[] bodies;
 
     public int food;
 
 
     public Organism(List<OpCode> program, int worldWidth, int worldHeight, FoodFinder foodFinder) {
         this.program = program;
         this.vm = new EvoVM2(registerCount, program.toArray(new OpCode[program.size()]));
         this.food = 0;
 
         bodies = new Body2d[1];
         for (int i = 0; i < bodies.length; i++) {
             bodies[i] = new Body2d(rnd.nextInt(worldWidth), rnd.nextInt(worldHeight), rnd.nextDouble(), foodFinder, new WallSense(worldWidth, worldHeight));
         }
     }
 
 
     public void live() throws Exception {
         double left, right, foodDist;
         double scale = 8192.0;
 
         //write input registers
         int inreg = 0;
         for (Body2d b : bodies) {
             b.food = b.foodFinder.findNearestFood(b.x, b.y);
             foodDist = b.foodFinder.foodDist(b.food, b.x, b.y);
 
             //cached cosdir and scale as int are meant to speed this up
             //vm.regs[inreg++] = (int) (((PrecisionBody2d) b).cosdir * scale);
             vm.regs[inreg++] = (int) (Math.cos(b.dir) * scale);
 
             vm.regs[inreg++] = (int) (((b.food.x - b.x) / foodDist) * scale);
             vm.regs[inreg++] = (int) (((b.food.y - b.y) / foodDist) * scale);
 
             //wallsense
             vm.regs[inreg++] = b.wallSense.lastSenseVal;
         }
 
         vm.run();
 
         //use output values
         for (Body2d b : bodies) {
             //fetch, limit and scale outputs
             left = Math.max(0, Math.min(vm.regs[inreg++], 65535)) / scale;
             right = Math.max(0, Math.min(vm.regs[inreg++], 65535)) / scale;
 
             //move
             b.motor.move(left, right);
 
             //pickup wallsense before coordinates are clipped
             b.wallSense.sense();
         }
     }
 
 
     static Organism randomOrganism(int xmax, int ymax, int progSize, FoodFinder foodFinder) {
         int size = rnd.nextInt(progSize) + 1;
         List<OpCode> program = new ArrayList<OpCode>(size);
 
         for (int i = 0; i < size; i++) {
             program.add(OpCode.randomOpCode(rnd));
         }
 
         return new Organism(program, xmax, ymax, foodFinder);
     }
 
 
     @Override
     public List<OpCode> clone() {
         List<OpCode> p = new ArrayList<OpCode>(program.size());
 
         for (OpCode oc : program) {
             p.add(oc.clone());
         }
 
         return p;
     }
 
 
     @Override
     public int compareTo(Organism o) {
         return new Integer(food).compareTo(o.food);
     }
 
 }
