 package test;
 
 import jason.asSemantics.Agent;
 import jason.asSemantics.Intention;
 import jason.asSemantics.Unifier;
 import jason.asSyntax.Atom;
 import jason.asSyntax.DefaultTerm;
 import jason.asSyntax.ListTermImpl;
 import jason.asSyntax.Literal;
 import jason.asSyntax.LogExpr;
 import jason.asSyntax.LogicalFormula;
 import jason.asSyntax.Pred;
 import jason.asSyntax.PredicateIndicator;
 import jason.asSyntax.Structure;
 import jason.asSyntax.Term;
 import jason.asSyntax.VarTerm;
 import jason.bb.BeliefBase;
 import jason.bb.DefaultBeliefBase;
 import jason.bb.JDBCPersistentBB;
 
 import java.util.Iterator;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 /** JUnit test case for syntax package */
 public class BeliefBaseTest extends TestCase {
 
 	protected void setUp() throws Exception {
 		super.setUp();
 	}
 
 	public void testAdd() {
 		Literal l1, l2, l3, l4, l5;
 		BeliefBase bb = new DefaultBeliefBase();
 		
 		l1 = new Literal(true, new Pred("pos"));
 		assertTrue(bb.add(l1));
 		
 		assertFalse(bb.add(new Literal(true, new Pred("pos"))));
         assertEquals(bb.size(),1);
 
 		l2 = new Literal("pos");
 		l2.addAnnot(new Atom("a"));
         //System.out.println(l1.hashCode()+"/"+l2.hashCode());
         //System.out.println(bb+"-"+ bb.contains(l2));
         assertTrue(bb.contains(l2) != null);
         
 		assertTrue(bb.add(l2));
 		// the add should maintain the annots
 		assertEquals(1,l2.getAnnots().size());
 		
 		assertFalse(bb.add(l2));
 		// the add should remove the annots
 		assertEquals(0,l2.getAnnots().size());
 
 		//System.out.println(bb+" "+ bb.contains(l2)+" l2="+l2);
         assertEquals(bb.contains(l2).toString(),"pos[a]");
 		assertEquals(bb.size(),1);
 
 		l3 = new Literal(true, new Pred("pos"));
 		l3.addAnnot(new Structure("b"));
 		l3.addAnnot(BeliefBase.TPercept);
 		assertTrue(bb.add(l3));
 		assertFalse(bb.add(l3));
 		assertEquals(bb.size(),1);
 
 		l3 = new Literal(true, new Pred("pos"));
 		l3.addSource(new Structure("ag1"));
 		assertTrue(bb.add(l3));
 
 		// same as above, must not insert
 		l3 = new Literal(true, new Pred("pos"));
 		l3.addSource(new Atom("ag1"));
 		assertFalse(bb.add(l3));
 		
 		l4 = new Literal(true, new Pred("pos"));
 		l4.addTerm(new Atom("1"));
 		l4.addTerm(new Atom("2"));
 		l4.addAnnot(BeliefBase.TPercept);
 		assertTrue(bb.add(l4));
 
 		l4 = new Literal(true, new Pred("pos"));
 		l4.addTerm(new Atom("1"));
 		l4.addTerm(new Atom("2"));
 		l4.addAnnot(BeliefBase.TPercept);
 		assertFalse(bb.add(l4));
 		assertEquals(bb.size(),2);
 
 		l4 = new Literal(true, new Pred("pos"));
 		l4.addTerm(new Structure("5"));
 		l4.addTerm(new Structure("6"));
 		l4.addAnnot(BeliefBase.TPercept);
 		assertTrue(bb.add(l4));
 
 		l5 = new Literal(true, new Pred("garb"));
 		l5.addTerm(new Structure("r1"));
 		assertTrue(bb.add(l5));
 		
 		//System.out.println("BB="+bb);
 		//System.out.println("Percepts="+bb.getPercepts());
 		assertEquals(iteratorSize(bb.getPercepts()), 3);
 		
 		//Literal lRel1 = new Literal(true, new Pred("pos"));
 		//System.out.println("Rel "+lRel1.getFunctorArity()+"="+bb.getRelevant(lRel1));
 
 		Literal lRel2 = new Literal(true, new Pred("pos"));
 		lRel2.addTerm(new VarTerm("X"));
 		lRel2.addTerm(new VarTerm("Y"));
 		//System.out.println("Rel "+lRel2.getFunctorArity()+"="+bb.getRelevant(lRel2));
 		assertEquals(bb.size(), 4);
 		assertEquals(iteratorSize(bb.iterator()), 4);
 		
 		// remove
 		l5 = new Literal(true, new Pred("garb"));
 		l5.addTerm(new Structure("r1"));
 		assertTrue(bb.remove(l5));
 		assertEquals(bb.getRelevant(l5), null);
 		assertEquals(bb.size(), 3);
 
 		l4 = new Literal(true, new Pred("pos"));
 		l4.addTerm(new Structure("5"));
 		l4.addTerm(new Structure("6"));
 		l4.addAnnot(BeliefBase.TPercept);
 		assertTrue(bb.remove(l4));
 		assertEquals(iteratorSize(bb.getRelevant(l4)), 1);
 		assertEquals(bb.size(), 2);
 		assertEquals(iteratorSize(bb.iterator()), 2);
 
 		//System.out.println("remove grab(r1), pos(5,6)");
 		//System.out.println("BB="+bb);
 		//System.out.println("Percepts="+bb.getPercepts());
 		assertEquals(iteratorSize(bb.getPercepts()), 2);
 	
 		l4 = new Literal(true, new Pred("pos"));
 		l4.addTerm(new Structure("1"));
 		l4.addTerm(new Structure("2"));
 		l4.addAnnot(BeliefBase.TPercept);
 		assertTrue(bb.remove(l4));
 		assertEquals(bb.getRelevant(l4), null);
 		assertEquals(bb.size(), 1);
         assertEquals(iteratorSize(bb.getPercepts()), 1);
 
 		//System.out.println("remove pos(1,2)");
 		//System.out.println("BB="+bb);
 		
 		l2 = new Literal(true, new Pred("pos"));
 		l2.addAnnot(new Structure("a"));
 		assertTrue(bb.contains(l2) != null);
 		assertFalse(bb.contains(l2).hasSubsetAnnot(l2)); //
 		assertTrue(bb.remove(l2));
 
 		l2.addAnnot(new Structure("b"));
 		l2.addAnnot(BeliefBase.TPercept);
 		l2.delAnnot(new Structure("a"));
         assertTrue(l2.hasAnnot(BeliefBase.TPercept));
         Literal l2inBB = ((DefaultBeliefBase)bb).contains(l2);
         assertTrue(l2inBB != null);
         //System.out.println("l2 in BB="+l2inBB);
 		assertTrue(bb.remove(l2));
 		//System.out.println("removed l2 "+l2);
 		//System.out.println("BB="+bb);
 		//System.out.print("Percepts=");
         //Iterator i = bb.getPercepts();
         //while (i.hasNext()) {
         //    System.out.print(i.next()+",");
         //}
 		assertEquals(iteratorSize(bb.getPercepts()), 0);
 		assertEquals(bb.size(), 1);
 		
 		l3 = Literal.parseLiteral("pos[source(ag1)]");
 		assertTrue(bb.remove(l3));
 		
 		//System.out.println("removed "+l3);
 		//System.out.println("BB="+bb);
 		assertEquals(bb.size(), 0);
 		assertEquals(iteratorSize(bb.iterator()), 0);
 	}
 
     public void testAdd2() {
         BeliefBase bb = new DefaultBeliefBase();
         Literal l1 = Literal.parseLiteral("pos[source(ag1)]");
         assertTrue(bb.add(l1));
         Literal l2 = Literal.parseLiteral("pos[source(ag2)]");
         assertTrue(bb.add(l2));
         assertEquals(bb.size(),1);
 
         VarTerm c = new VarTerm("C");
         Unifier u = new Unifier();
         Literal l3 = Literal.parseLiteral("pos");
         u.unifies(c, l3);
         c.apply(u);
         c.addSource(Structure.parse("ag3"));
         assertTrue(c.hasAnnot(DefaultTerm.parse("source(ag3)")));
         Literal inBB = bb.contains(c); 
         assertTrue(inBB != null);
         assertFalse(c.hasSubsetAnnot(inBB));
         assertFalse(c.equals(l1));
         //System.out.println(c+" "+c.getClass().getName());
         assertTrue(c.equalsAsTerm(l1));
         assertTrue(l1.equalsAsTerm(c));
         assertTrue(bb.add(c));
         assertFalse(bb.add(c));
         
         c = new VarTerm("C");
         VarTerm ca = new VarTerm("CA");
         u = new Unifier();
         Literal l4 = Literal.parseLiteral("pos");
         u.unifies(c, l4);
         try {
             new jason.stdlib.add_annot().execute(null, u, new Term[] { c, DefaultTerm.parse("source(ag4)"), ca });
         } catch (Exception e) {
             e.printStackTrace();
         }
         ca.apply(u);
         assertTrue(bb.add(ca));
         assertFalse(bb.add(ca));
 
         //System.out.println(bb);
         assertEquals(bb.size(),1);
         assertEquals(inBB.getAnnots().size(),4);
     }
     
     
 	public void testRemWithList() {
 		Unifier u = new Unifier();
 		BeliefBase bb = new DefaultBeliefBase();
 		Literal s = Literal.parseLiteral("seen(L)");
 		assertTrue(u.unifies(new VarTerm("L"), (Term)ListTermImpl.parseList("[a,b]")));
 		//System.out.println("u="+u);
 		s.apply(u);
 		bb.add(s);
 
 		VarTerm b1 = new VarTerm("B1");
 		u.unifies(b1, Literal.parseLiteral("seen([a,b])"));
 		b1.apply(u);
 		//System.out.println("b1="+b1);
 		//System.out.println("test 1");
 		assertTrue(b1.equalsAsTerm(Literal.parseLiteral("seen([a,b])")));
 		assertTrue(b1.equalsAsTerm(s));
 		assertTrue(bb.remove(b1));
 	}
 	
 	public void testRemWithUnnamedVar() {
 		Agent ag = new Agent();
 		ag.getBB().add(Literal.parseLiteral("pos(2,3)"));
 		Unifier u = new Unifier();
 
 		Literal l = Literal.parseLiteral("pos(_,_)");
 		assertTrue(ag.believes(l, u));
 		l.apply(u);
 		assertEquals(l, Literal.parseLiteral("pos(2,3)"));
 		
 		assertTrue(ag.getBB().remove(l));
 	}
     
     @SuppressWarnings("unused")
     public void testLogCons() {
         Agent ag = new Agent();
         ag.getBB().add(Literal.parseLiteral("a(10)"));
         ag.getBB().add(Literal.parseLiteral("a(20)"));
         ag.getBB().add(Literal.parseLiteral("b(20,10)"));
         ag.getBB().add(Literal.parseLiteral("c(x)"));
         ag.getBB().add(Literal.parseLiteral("c(y)"));
         LogicalFormula texpr;
         
         Iterator<Unifier> iun = Literal.parseLiteral("a(X)").logicalConsequence(ag, new Unifier());
         int c = 0;
         while (iun.hasNext()) {
             iun.next();
             c++;
         }
         assertEquals(c,2);
 
         iun = Literal.parseLiteral("b(X,_)").logicalConsequence(ag, new Unifier());
         assertTrue(iun.hasNext());
         Unifier un = iun.next();
         assertTrue(un.get("X").toString().equals("20"));
         
         
         // test not
         texpr = LogExpr.parseExpr("not a(5)");
         assertTrue(texpr.logicalConsequence(ag, new Unifier()).hasNext());
         texpr = LogExpr.parseExpr("not a(10)");
         assertFalse(texpr.logicalConsequence(ag, new Unifier()).hasNext());
         
         
         // test and
         texpr = LogExpr.parseExpr("a(X) & c(Y) & a(Z)");
         iun = texpr.logicalConsequence(ag, new Unifier());
         c = 0;
         while (iun.hasNext()) {
             Unifier u = iun.next();
             //System.out.println(u);
             c++;
         }
         assertEquals(c,8);
         
         // test or
         texpr = LogExpr.parseExpr("a(X) | c(Y)");
         iun = texpr.logicalConsequence(ag, new Unifier());
         c = 0;
         while (iun.hasNext()) {
             Unifier u = iun.next();
             //System.out.println(u);
             c++;
         }
         assertEquals(c,4);
         
         // test rel
         texpr = LogExpr.parseExpr("a(X) & a(Y) & Y > X");
         iun = texpr.logicalConsequence(ag, new Unifier());
         c = 0;
         while (iun.hasNext()) {
             Unifier u = iun.next();
             c++;
         }
         assertEquals(c,1);
 
         texpr = LogExpr.parseExpr("a(X) & a(Y) & X <= Y");
         iun = texpr.logicalConsequence(ag, new Unifier());
         c = 0;
         while (iun.hasNext()) {
             Unifier u = iun.next();
             c++;
         }
         assertEquals(c,3);
         
         ag.getBB().add(Literal.parseLiteral("k(20,c)"));
         ag.getBB().add(Literal.parseLiteral("k(10,b)"));
         Unifier u = new Unifier();
         assertTrue(ag.believes(Literal.parseLiteral("k(X,c)"), u));
         assertEquals(u.get("X").toString(), "20");
         assertEquals(ag.findBel(Literal.parseLiteral("k(X,c)"), new Unifier()).toString(), "k(20,c)");
     }
     
     public void testPercept1() {
         BeliefBase bb = new DefaultBeliefBase();
         assertTrue(bb.add(Literal.parseLiteral("a[source(percept)]")));
         assertTrue(bb.add(Literal.parseLiteral("a[ag1]")));
         assertEquals(iteratorSize(bb.getPercepts()),1);
 
         // remove annots ag1
         assertTrue(bb.remove(Literal.parseLiteral("a[ag1]")));
         assertEquals(iteratorSize(bb.getPercepts()),1);
         assertTrue(bb.remove(Literal.parseLiteral("a[source(percept)]")));
         assertEquals(bb.size(),0);
         assertEquals(iteratorSize(bb.getPercepts()),0);
 
         // add again and remove percept first
         assertTrue(bb.add(Literal.parseLiteral("a[source(percept)]")));
         assertTrue(bb.add(Literal.parseLiteral("a[ag1]")));
         assertTrue(bb.remove(Literal.parseLiteral("a[source(percept)]")));
         // to remove the only source, should remove all belief
         assertEquals(bb.size(),0);
         assertEquals(iteratorSize(bb.getPercepts()),0);
     }
     
     public void testPercept2() {
         BeliefBase bb = new DefaultBeliefBase();
         assertTrue(bb.add(Literal.parseLiteral("p1[source(percept),source(ag1)]")));
         assertTrue(bb.add(Literal.parseLiteral("p2[source(percept),a1]")));
         assertEquals(iteratorSize(bb.getPercepts()),2);
         
         Iterator<Literal> i = bb.getPercepts();
         Literal l = i.next();
        while (!l.getFunctor().equals("p1")) l = i.next();
         i.remove();
        assertEquals("p1[source(ag1)]",l.toString());
         
         assertEquals(1,iteratorSize(bb.getPercepts()));
         assertEquals(2,bb.size());
         
 
         i = bb.getPercepts();
         l = i.next(); // get the p2
         i.remove();
        assertEquals("p2[a1]",l.toString());
 
         assertEquals(0,iteratorSize(bb.getPercepts()));
         assertEquals(1,bb.size());
     }
     
     
     public void testJDBCBB() {
         BeliefBase bb = new JDBCPersistentBB();
         bb.init(null, new String[] {
                 "org.hsqldb.jdbcDriver",
                 "jdbc:hsqldb:bookstore",
                 "sa",
                 "",
                 "[book(5,book),book_author(2,book_author),author(2,author),test(2,testtable)]"
                 });
         
         bb.abolish(new PredicateIndicator("book",5));
         bb.abolish(new PredicateIndicator("author",2));
         bb.abolish(new PredicateIndicator("book_author",2));
         bb.abolish(new PredicateIndicator("test",2));
         assertEquals(bb.size(),0);
 
         assertTrue(bb.add(Literal.parseLiteral("test(30)")));
         assertEquals(bb.size(),1);
         Literal l;
         
         // add authors
         assertTrue(bb.add(Literal.parseLiteral("author(1,\"Rafael H. Bordini\")")));
         assertFalse(bb.add(Literal.parseLiteral("author(1,\"Rafael H. Bordini\")")));
         assertTrue(bb.add(Literal.parseLiteral("author(2,\"Mehdi Dastani\")")));
         assertTrue(bb.add(Literal.parseLiteral("author(3,\"Jurgen Dix\")")));
         assertTrue(bb.add(Literal.parseLiteral("author(4,\"Amal El Fallah Seghrouchni\")")));
         assertTrue(bb.add(Literal.parseLiteral("author(5,\"Michael Wooldridge\")")));
         assertEquals(bb.size(),6);
         assertEquals(iteratorSize(bb.iterator()),bb.size());
         
         // add books
         l = Literal.parseLiteral("book(1,\"Multi-Agent Programming : Languages, Platforms and Applications\", \"Springer\", 2005, \"0387245685\")");
         assertTrue(bb.add(l));
         assertFalse(bb.add(l));
         // add book authors
         assertTrue(bb.add(Literal.parseLiteral("book_author(1,1)")));
         assertTrue(bb.add(Literal.parseLiteral("book_author(1,2)")));
         assertTrue(bb.add(Literal.parseLiteral("book_author(1,3)")));
         assertTrue(bb.add(Literal.parseLiteral("book_author(1,4)")));
         assertEquals(bb.size(),11);
 
         // add another book
         l = Literal.parseLiteral("book(2,\"Another Multi-Agent Programming : Languages, Platforms and Applications\", \"Springer\", 2005, \"0387245685\")");
         assertTrue(bb.add(l));
 
         l = Literal.parseLiteral("book(3,\"An introduction to multiagent systems\", \"John Wiley & Sons\", 2002, \"\")");
         assertTrue(bb.add(l));
         assertTrue(bb.add(Literal.parseLiteral("book_author(3,5)")));
         assertEquals(bb.size(),14);
 
         // test with legacy table
         ((JDBCPersistentBB)bb).test();
         // test add two records
         assertEquals(bb.size(),16);
         assertTrue(bb.add(Literal.parseLiteral("publisher(10,\"Prentice Hall\")")));
         assertFalse(bb.add(Literal.parseLiteral("publisher(10,\"Prentice Hall\")")));
         assertEquals(bb.size(),17);
 
         // test annots
         l = Literal.parseLiteral("test(t1(a(10),b(20)),[v1,30,\"a vl\"])[annot1,source(carlos)]");
         assertTrue(bb.add(l));
         assertFalse(bb.add(l));
         Literal linbb = ((JDBCPersistentBB)bb).contains(l);
         assertEquals(l.getTerm(0),linbb.getTerm(0));
         assertEquals(l.getTerm(1),linbb.getTerm(1));
         assertTrue(l.equals(linbb));
         l = Literal.parseLiteral("test(t1(a(10),b(20)),[v1,30,\"a vl\"])[annot2]");
         assertTrue(bb.add(l));
         linbb = ((JDBCPersistentBB)bb).contains(l);
         assertEquals(linbb.getAnnots().size(),3);
         l = Literal.parseLiteral("test(t1(a(10),b(20)),[v1,30,\"a vl\"])[annot2]");
         assertFalse(bb.add(l));
         linbb = bb.contains(l);
         assertEquals(linbb.getAnnots().size(),3);
 
         // test negated
         int size = bb.size();
         l = Literal.parseLiteral("test(a,b)");
         assertTrue(bb.add(l));
         l = Literal.parseLiteral("~test(a,b)");
         assertTrue(bb.add(l));
         assertEquals(bb.size(),size+2);
         
         // test get all
         assertEquals(iteratorSize(bb.iterator()),size+2);
 
         //for (Literal l2: bb) {
         //	System.out.println(l2);
         //}
         
         // test remove
         size = bb.size();
         assertTrue(bb.remove(Literal.parseLiteral("test(a,b)")));
         assertFalse(bb.remove(Literal.parseLiteral("test(a,b)")));
         assertTrue(bb.remove(Literal.parseLiteral("publisher(10,\"Prentice Hall\")")));
         l = Literal.parseLiteral("test(t1(a(10),b(20)),[v1,30,\"a vl\"])[annot2]");
         assertTrue(bb.remove(l));
         linbb = bb.contains(l);
         assertEquals(linbb.getAnnots().size(),2);
         assertEquals(bb.size(),size-2);
         
         // test getRelevant
         //Iterator ir = bb.getRelevant(Literal.parseLiteral("book_author(_,_)"));
         //while (ir.hasNext()) {
         //    System.out.println(ir.next());
         //}
         assertEquals(iteratorSize(bb.getRelevant(Literal.parseLiteral("book_author(_,_)"))),5);
         
         bb.stop();
     }
     
     
     @SuppressWarnings("unchecked")
 	public void testBelBRF() {
         Agent ag = new Agent();
         ag.getBB().add(Literal.parseLiteral("a(10)"));
         ag.getBB().add(Literal.parseLiteral("a(20)[a]"));
         ag.getBB().add(Literal.parseLiteral("a(30)[a,b]"));
         ag.getBB().add(Literal.parseLiteral("b(20,10)[source(ag)]"));
         ag.getBB().add(Literal.parseLiteral("c(x)"));
         ag.getBB().add(Literal.parseLiteral("c(y)"));
         ag.getBB().add(Literal.parseLiteral("c(20)"));
 
         assertFalse(ag.believes(Literal.parseLiteral("c(30)"), new Unifier()));
         assertTrue(ag.believes(Literal.parseLiteral("c(20)"), new Unifier()));
         Unifier u = new Unifier();
         assertTrue(ag.believes(Literal.parseLiteral("c(X)"), u));
         assertEquals(u.get("X").toString(),"20");
         
         Literal l = Literal.parseLiteral("c(_)");
         u = new Unifier();
         assertTrue(ag.believes(l, u));
         l.apply(u);
         assertEquals(l.toString(),"c(20)");
         
         assertFalse(ag.believes(Literal.parseLiteral("a(300)"), new Unifier()));
         assertTrue(ag.believes(Literal.parseLiteral("a(30)"), new Unifier()));
         assertTrue(ag.believes(Literal.parseLiteral("a(30)[a]"), new Unifier()));
         assertTrue(ag.believes(Literal.parseLiteral("a(30)[b,a]"), new Unifier()));
         assertFalse(ag.believes(Literal.parseLiteral("a(30)[b,a,c]"), new Unifier()));
 
         l = Literal.parseLiteral("a(X)[A,B|RA]");
         u = new Unifier();
         assertTrue(ag.believes(l,u));
         l.apply(u);
         assertEquals(l.toString(),"a(30)[a,b]");
         assertEquals(u.get("RA").toString(),"[]");
 
         u = new Unifier();
         assertTrue(ag.believes(Literal.parseLiteral("b(X,Y)[source(A)]"), u));
         assertEquals(u.get("X").toString(),"20");
         assertEquals(u.get("Y").toString(),"10");
         assertEquals(u.get("A").toString(),"ag");
         
         List[] rbrf = ag.brf(null, Literal.parseLiteral("c(20)"), Intention.EmptyInt);
         assertTrue(rbrf[0].size() == 0);
         assertTrue(rbrf[1].size() == 1);
         assertEquals(rbrf[1].toString(), "[c(20)]");
 
         rbrf = ag.brf(null, Literal.parseLiteral("c(_)"), Intention.EmptyInt);
         assertTrue(rbrf[1].size() == 1);
         assertEquals(rbrf[1].toString(), "[c(y)]");
 
         // can not remove b without source
         rbrf = ag.brf(null, Literal.parseLiteral("b(_,_)"), Intention.EmptyInt);
         assertEquals(rbrf,null);
 
         // remove b with source
         rbrf = ag.brf(null, Literal.parseLiteral("b(_,_)[source(_)]"), Intention.EmptyInt);
         assertEquals(rbrf[1].toString(), "[b(20,10)[source(ag)]]");
         assertEquals(ag.getBB().size(),4);
         
         ag.abolish(Literal.parseLiteral("a(_)"), null);
         assertEquals(ag.getBB().size(),1);
     }
     
     public void testClone() {
         Agent ag = new Agent();
         ag.getBB().add(1,Literal.parseLiteral("a(10)"));
         ag.getBB().add(1,Literal.parseLiteral("a(20)[a]"));
         ag.getBB().add(1,Literal.parseLiteral("a(30)[a,b]"));
         ag.getBB().add(1,Literal.parseLiteral("c(x)"));
         ag.getBB().add(1,Literal.parseLiteral("c(y)"));
         ag.getBB().add(Literal.parseLiteral("c(20)"));
         BeliefBase c = (BeliefBase)ag.getBB().clone();
         assertEquals(ag.getBB().toString(), c.toString());
     }
     
     @SuppressWarnings("unchecked")
 	private int iteratorSize(Iterator i) {
         int c = 0;
         while (i.hasNext()) {
             i.next();
             c++;
         }
         return c;
     }
     
 }
