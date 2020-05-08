 package EDU.Washington.grad.gjb.cassowary;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.cscott.sdr.util.Box;
 import net.cscott.sdr.util.Fraction;
 import net.cscott.sdr.util.Point;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 /** Simple feasibility tests for SDR application. */
 public class BreatheTest {
     /**
      * Test formation expansion.
      * @doc.test
     *  js> BreatheTest.testDiamondBreathing()
      */
     @Test
     public void testDiamondBreathing() throws ExCLError {
         ClSimplexSolver solver = new ClSimplexSolver();
         ClVariable a = new ClVariable(Fraction.ZERO);
         ClVariable b = new ClVariable(Fraction.ZERO);
         ClVariable c = new ClVariable(Fraction.ZERO);
         ClVariable d = new ClVariable(Fraction.ZERO);
         ClVariable e = new ClVariable(Fraction.ZERO);
         ClVariable f = new ClVariable(Fraction.ZERO);
         ClVariable g = new ClVariable(Fraction.ZERO);
         // objective: minimize a-g
         solver.addConstraint(new ClLinearEquation(a, Fraction.ZERO, ClStrength.weak));
         solver.addConstraint(new ClLinearEquation(b, Fraction.ZERO, ClStrength.weak));
         solver.addConstraint(new ClLinearEquation(c, Fraction.ZERO, ClStrength.weak));
         solver.addConstraint(new ClLinearEquation(d, Fraction.ZERO, ClStrength.weak));
         solver.addConstraint(new ClLinearEquation(e, Fraction.ZERO, ClStrength.weak));
         solver.addConstraint(new ClLinearEquation(f, Fraction.ZERO, ClStrength.weak));
         solver.addConstraint(new ClLinearEquation(g, Fraction.ZERO, ClStrength.weak));
         // basic monotonicity constraints (required)
         solver.addConstraint(new ClLinearEquation(a, Fraction.ZERO));
         solver.addConstraint(new ClLinearInequality(b, CL.Op.GEQ, a));
         solver.addConstraint(new ClLinearInequality(c, CL.Op.GEQ, b));
         solver.addConstraint(new ClLinearInequality(d, CL.Op.GEQ, c));
         solver.addConstraint(new ClLinearInequality(e, CL.Op.GEQ, d));
         solver.addConstraint(new ClLinearInequality(f, CL.Op.GEQ, e));
         solver.addConstraint(new ClLinearInequality(g, CL.Op.GEQ, f));
         // make sure each element has enough space (required)
         solver.addConstraint(new ClLinearInequality(CL.Plus(a, Fraction.TWO), CL.Op.LEQ, d));
         solver.addConstraint(new ClLinearInequality(CL.Plus(d, Fraction.TWO), CL.Op.LEQ, g));
         solver.addConstraint(new ClLinearInequality(CL.Plus(b, Fraction.TWO), CL.Op.LEQ, e));
         solver.addConstraint(new ClLinearInequality(CL.Plus(c, Fraction.TWO), CL.Op.LEQ, f));
         // attempt to maintain symmetry (strong)
         solver.addConstraint(new ClLinearEquation(CL.Minus(b, a), CL.Minus(d, c),
                 ClStrength.strong));
         solver.addConstraint(new ClLinearEquation(CL.Minus(e, d), CL.Minus(g, f),
                 ClStrength.strong));
         solver.addConstraint(new ClLinearEquation(CL.Minus(c, b), CL.Minus(e, d),
                 ClStrength.strong));
         solver.addConstraint(new ClLinearEquation(CL.Minus(d, c), CL.Minus(f, e),
                 ClStrength.strong));
         // okay, look at solution
         /*
         System.out.println("a="+a.value().toProperString());
         System.out.println("b="+b.value().toProperString());
         System.out.println("c="+c.value().toProperString());
         System.out.println("d="+d.value().toProperString());
         System.out.println("e="+e.value().toProperString());
         System.out.println("f="+f.value().toProperString());
         System.out.println("g="+g.value().toProperString());
         */
         // automate the check
         Assert.assertEquals("a", Fraction.ZERO, a.value());
         Assert.assertEquals("b", Fraction.valueOf(2, 3), b.value());
         Assert.assertEquals("c", Fraction.valueOf(4, 3), c.value());
         Assert.assertEquals("d", Fraction.TWO, d.value());
         Assert.assertEquals("e", Fraction.valueOf(8, 3), e.value());
         Assert.assertEquals("f", Fraction.valueOf(10, 3), f.value());
         Assert.assertEquals("g", Fraction.valueOf(4), g.value());
     }
 
     /** Feasibility test.
      * We're simulating breathing from this formation:
      * <pre>
      * net.cscott.sdr.calls.Formation[
      *   location={COUPLE 3 GIRL=-2 1/2,1,w, COUPLE 2 GIRL=1 1/2,1,w, COUPLE 3 BOY=-3,0,n,[ROLL_RIGHT], COUPLE 4 BOY=-1,0,s,[ROLL_RIGHT], COUPLE 2 BOY=1,0,n,[ROLL_RIGHT], COUPLE 1 BOY=3,0,s,[ROLL_RIGHT], COUPLE 4 GIRL=-1 1/2,-1,e, COUPLE 1 GIRL=2 1/2,-1,e}
      *   selected=[COUPLE 3 GIRL, COUPLE 2 GIRL, COUPLE 3 BOY, COUPLE 4 BOY, COUPLE 2 BOY, COUPLE 1 BOY, COUPLE 4 GIRL, COUPLE 1 GIRL]
      * ]
      * </pre>
      * But for simplicity, only doing half the formation.
      * @doc.test
     *  js> BreatheTest.testDiamondTrimming()
      */
     @Test
     public void testDiamondTrimming() throws ExCLError {
         /* four dancers:
          * A: [-2,-1, 0,1] n  (center -1,0)
          * B: [0,-1, 2,1]  s  (center  1,0)
          * C: [-1/2,-2, 1 1/2,0] e (center 1/2, -1)
          * D: [-1 1/2,0, 1/2, 2] w (center -1/2, 1)
          *
          * Four variables each: l,b,t,r (left/bottom/top/right)
          */
         ClBranchAndBound solver = new ClBranchAndBound();
 
         Box ab = boxFromStrings(  "-2", "-1", "0", "1" );
         Box bb = boxFromStrings(   "0", "-1", "2", "1" );
         Box cb = boxFromStrings("-1/2", "-2", "1 1/2", "0" );
         Box db = boxFromStrings("-1 1/2", "0", "1/2", "2" );
         // variables, stays, and basic required constraints
         // (left<=right, bottom<=top; regions only shrink)
         ClVariable[] av = makeVars(solver, "a", ab);
         ClVariable[] bv = makeVars(solver, "b", bb);
         ClVariable[] cv = makeVars(solver, "c", cb);
         ClVariable[] dv = makeVars(solver, "d", db);
         // for each pair of overlapping boxes, add constraints to prevent the
         // overlap.
         List<ClBooleanVariable> switches = new ArrayList<ClBooleanVariable>();
         ClBooleanVariable sw;
         // (1) a<->b (no overlap)
         // (2) a<->c (0=ar<cl, 1=ct<ab)
         sw = new ClBooleanVariable(solver);
         solver.addConstraintIf(sw, new ClLinearInequality
                 (av[RIGHT], CL.Op.LEQ, cv[LEFT]));
         solver.addConstraintIfNot(sw, new ClLinearInequality
                 (cv[TOP], CL.Op.LEQ, av[BOTTOM]));
         switches.add(sw);
         // (3) a<->d (0=ar<dl, 1=at<db)
         sw = new ClBooleanVariable(solver);
         solver.addConstraintIf(sw, new ClLinearInequality
                 (av[RIGHT], CL.Op.LEQ, dv[LEFT]));
         solver.addConstraintIfNot(sw, new ClLinearInequality
                 (av[TOP], CL.Op.LEQ, dv[BOTTOM]));
         switches.add(sw);
         // (4) b<->c (0=cr<bl, 1=ct<bb)
         sw = new ClBooleanVariable(solver);
         solver.addConstraintIf(sw, new ClLinearInequality
                 (cv[RIGHT], CL.Op.LEQ, bv[LEFT]));
         solver.addConstraintIfNot(sw, new ClLinearInequality
                 (cv[TOP], CL.Op.LEQ, bv[BOTTOM]));
         switches.add(sw);
         // (5) b<->d (0=dr<bl, 1=bt<db)
         sw = new ClBooleanVariable(solver);
         solver.addConstraintIf(sw, new ClLinearInequality
                 (dv[RIGHT], CL.Op.LEQ, bv[LEFT]));
         solver.addConstraintIfNot(sw, new ClLinearInequality
                 (bv[TOP], CL.Op.LEQ, dv[BOTTOM]));
         switches.add(sw);
         // (6) c<->d (no overlap)
 
         // add handhold constraints
         solver.addConstraint(new ClLinearEquation
                 (new ClLinearExpression(av[RIGHT]),
                  new ClLinearExpression(bv[LEFT]),
                  ClStrength.medium));
 
         // output/check solution!
         solver.solve();
         ab = boxFromVars(av);
         bb = boxFromVars(bv);
         cb = boxFromVars(cv);
         db = boxFromVars(dv);
         System.out.println("Choice vars: "+switches);
         System.out.println("a: "+ab);
         System.out.println("b: "+bb);
         System.out.println("c: "+cb);
         System.out.println("d: "+db);
         // automate the check
         Assert.assertEquals("a", ab, boxFromStrings("-2","-1","0","1"));
         Assert.assertEquals("b", bb, boxFromStrings("0","-1","2","1"));
         Assert.assertEquals("c", cb, boxFromStrings("-1/2","-2","1 1/2","-1"));
         Assert.assertEquals("d", db, boxFromStrings("-1 1/2","1","1/2","2"));
     }
     private static int LEFT=0, BOTTOM=1, RIGHT=2, TOP=3;
     private static Box boxFromStrings(String... points) {
         Fraction[] v = new Fraction[points.length];
         for (int i=0; i<points.length; i++)
             v[i] = Fraction.valueOf(points[i]);
         return new Box(new Point(v[LEFT], v[BOTTOM]), new Point(v[RIGHT], v[TOP]));
     }
     private static ClVariable[] makeVars(ClBranchAndBound s, String prefix, Box b) throws ExCLError {
         ClVariable[] v = new ClVariable[4];
         v[LEFT] = new ClVariable(prefix+"l", b.ll.x);
         v[BOTTOM] = new ClVariable(prefix+"b", b.ll.y);
         v[RIGHT] = new ClVariable(prefix+"r", b.ur.x);
         v[TOP] = new ClVariable(prefix+"t", b.ur.y);
         for (int i=0; i<4; i++)
             s.addConstraint(new ClLinearEquation(v[i], v[i].value(), ClStrength.weak));
         // required constraints: l<r, b<t
         s.addConstraint(new ClLinearInequality(v[LEFT], CL.Op.LEQ, v[RIGHT]));
         s.addConstraint(new ClLinearInequality(v[BOTTOM], CL.Op.LEQ, v[TOP]));
         // ONLY ALLOW SHRINKING REGIONS to resolve overlaps
         // this ensures that overlaps in the solution are a subset of
         // those currently present, which keeps the problem linear
         s.addConstraint(new ClLinearInequality(v[LEFT], CL.Op.GEQ, b.ll.x));
         s.addConstraint(new ClLinearInequality(v[BOTTOM], CL.Op.GEQ, b.ll.y));
         s.addConstraint(new ClLinearInequality(v[RIGHT], CL.Op.LEQ, b.ur.x));
         s.addConstraint(new ClLinearInequality(v[TOP], CL.Op.LEQ, b.ur.y));
         return v;
     }
     private static Box boxFromVars(ClVariable[] vars) {
         return new Box(new Point(vars[LEFT].value(), vars[BOTTOM].value()),
                        new Point(vars[RIGHT].value(), vars[TOP].value()));
     }
 
     public static void main(String[] args) throws ExCLError {
         BreatheTest bt = new BreatheTest();
         bt.testDiamondBreathing();
         bt.testDiamondTrimming();
     }
 }
