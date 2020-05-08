 package cs239;
 
 import syntaxtree.*;
 import visitor.*;
 import java.util.*;
 
 public class MayExecuteInParallelVisitor extends DepthFirstVoidVisitor {
 
     private HashMap<INode, Mol> statements;
 
     public MayExecuteInParallelVisitor() {
         statements = new HashMap<INode, Mol>();
     }
 
     private static HashSet<LabelPair> symCross(HashSet<INode> l0, HashSet<INode> l1) {
         HashSet<LabelPair> res = new HashSet<LabelPair>();
         for (Iterator<INode> i = l0.iterator(); i.hasNext(); ) {
             INode n0 = i.next();
             for (Iterator<INode> j = l1.iterator(); j.hasNext(); ) {
                 INode n1 = i.next();
                 res.add(new LabelPair(n0, n1));
             }
         }
         return res;
     }
 
     public void printOutput() {
         for (Mol mol : statements.values()) {
             for (Iterator<LabelPair> i = mol.m.iterator(); i.hasNext(); ) {
                 LabelPair p = i.next();
                 System.out.println(p.left.toString() + " " + p.right.toString());
             }
         }
     }
 
     public void visit(final Expression n) {
         super.visit(n);
 
         Mol current = new Mol();
 
         // expressions not previously being recorded
         current.l.add(n.f0.choice);
 
         statements.put(n, current);
     }
 
 	public void visit(final Statement n) {
         super.visit(n);
 
         Mol current;
         switch (n.f0.which) {
             case 1:
                 // async
             case 3:
                 // finish
             case 5:
                 // loop
             case 10:
                 // while
                 current = statements.get(n.f0.choice);
                current.l.add(n);
                 break;
             default:
                 // previously unrecorded statement
                 current = new Mol();
                 current.l.add(n.f0.choice);
                 break;
         }
 
         statements.put(n, current);
 	}
 
     /*
      * f0: 'async'
      * f1: '('
      * f2: Expression
      * f3: ')'
      * f4: Block
      */
 	public void visit(final AsyncStatement n) {
         super.visit(n);
         
         Mol current = new Mol();
         Mol tmp = new Mol();
 
         Mol expression = statements.get(n.f2);
         Mol block = statements.get(n.f4);
 
         tmp.m.addAll(block.m);
         tmp.o.addAll(block.o);
         tmp.l.addAll(block.l);
 
         tmp.m.addAll(expression.m);
         tmp.o.addAll(expression.o);
         tmp.l.addAll(expression.l);
 
         tmp.m.addAll(symCross(expression.o, block.l));
         tmp.l.add(n);
 
         current.m.addAll(tmp.m);
         current.o.addAll(tmp.l);
         current.l.addAll(tmp.l);
 
         statements.put(n, current);
 	}
 
     /*
      * f0: '{'
      * f1: BlockStatement
      * f2: '}'
      */
 	public void visit(final Block n) {
         super.visit(n);
 
         // need to aggregate all blockStatement under block
         Mol current = new Mol();
         for (Iterator<INode> i = n.f1.nodes.iterator(); i.hasNext(); ) {
             INode node = i.next();
             Mol mol = statements.get(node);
 
             current.m.addAll(mol.m);
             current.o.addAll(mol.o);
             current.l.addAll(mol.l);
             current.m.addAll(symCross(current.o, mol.l));
         }
         statements.put(n, current);
 	}
 
     /*
      * f0: final var declaration |
      *      updateable var declaration |
      *      statement
      */
 	public void visit(final BlockStatement n) {
         super.visit(n);
 
         Mol current;
         switch (n.f0.which) {
             case 3:
                 // statement
                 current = statements.get(n.f0.choice);
                current.l.add(n);
                 break;
             default:
                 current = new Mol();
                 current.l.add(n);
                 break;
         }
 
         statements.put(n, current);
 	}
 
     /*
      * f0: 'finish'
      * f1: Statement
      */
 	public void visit(final FinishStatement n) {
         super.visit(n);
 
         Mol current = new Mol();
         Mol statement = statements.get(n.f1);
 
         // finish s: M, empty, L
         current.m.addAll(statement.m);
         current.l.addAll(statement.l);
         current.l.add(n);
 
         statements.put(n, current);
 	}
 
     /*
      * f0: 'if'
      * f1: '('
      * f2: Expression
      * f3: ')'
      * f4: Statement
      * f5: ElseClause?
      */
 	public void visit(final IfStatement n) {
 		n.f0.accept(this);
 		n.f1.accept(this);
 		n.f2.accept(this);
 		n.f3.accept(this);
 		n.f4.accept(this);
 		n.f5.accept(this);
 	}
 
     /*
      * f0: 'for'
      * f1: '('
      * f2: PointType
      * f3: ExplodedSpecification
      * f4: ':'
      * f5: Expression
      * f6: ');
      * f7: Statement
      */
 	public void visit(final LoopStatement n) {
         super.visit(n);
 
         Mol current = new Mol();
         Mol statement = statements.get(n.f7);
         Mol expression = statements.get(n.f5);
 
         current.m.addAll(expression.m);
         current.o.addAll(expression.o);
         current.l.addAll(expression.l);
 
         current.m.addAll(statement.m);
         current.o.addAll(statement.o);
         current.l.addAll(statement.l);
 
         current.m.addAll(symCross(statement.o, expression.l));
         current.m.addAll(symCross(current.o, current.l));
         current.l.add(n);
 
         statements.put(n, current);
 	}
 
     /*
      * f0: Expression
      * f1: ';'
      */
 	public void visit(final PostfixStatement n) {
 		n.f0.accept(this);
 		n.f1.accept(this);
 	}
 
     /*
      * f0: 'System.out.println'
      * f1: '('
      * f2: Expression
      * f3: ')'
      * f4: ';'
      */
 	public void visit(final PrintlnStatement n) {
 		n.f0.accept(this);
 		n.f1.accept(this);
 		n.f2.accept(this);
 		n.f3.accept(this);
 		n.f4.accept(this);
 	}
 
     /*
      * f0: 'return'
      * f1: Expression
      * f2: ';'
      */
 	public void visit(final ReturnStatement n) {
 		n.f0.accept(this);
 		n.f1.accept(this);
 		n.f2.accept(this);
 	}
 
     /*
      * f0: 'throw'
      * f1: 'new'
      * f2: 'RuntimeException'
      * f3: '('
      * f4: Expression
      * f5: ')'
      * f6: ';'
      */
 	public void visit(final ThrowStatement n) {
 		n.f0.accept(this);
 		n.f1.accept(this);
 		n.f2.accept(this);
 		n.f3.accept(this);
 		n.f4.accept(this);
 		n.f5.accept(this);
 		n.f6.accept(this);
 	}
 
     /*
      * f0: 'while'
      * f1: '('
      * f2: Expression
      * f3: ')'
      * f4: Statement
      */
 	public void visit(final WhileStatement n) {
         super.visit(n);
 
         Mol current = new Mol();
         Mol statement = statements.get(n.f4);
         Mol expression = statements.get(n.f2);
 
         // do expression and statement
         current.m.addAll(statement.m);
         current.m.addAll(expression.m);
         current.m.addAll(symCross(expression.o, statement.l));
         current.o.addAll(statement.o);
         current.o.addAll(expression.o);
         current.l.addAll(statement.l);
         current.l.addAll(expression.l);
         // do loop
         current.m.addAll(symCross(current.o, current.l));
         current.l.add(n);
 
         statements.put(n, current);
 	}
 
 }
