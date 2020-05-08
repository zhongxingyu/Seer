 package com.sun.tools.javac.comp;
 
 import com.sun.tools.javac.code.Symbol;
 import com.sun.tools.javac.code.Type;
 import com.sun.tools.javac.tree.JCTree;
 import com.sun.tools.javac.tree.TreeScanner;
 import com.sun.tools.javac.util.Context;
 import com.sun.tools.javac.util.Log;
 
 import java.util.ArrayList;
 
 public class PropagateFlow extends TreeScanner {
     //every tree has this thing...
     protected static final Context.Key<PropagateFlow> pflowKey =
         new Context.Key<PropagateFlow>();
 
     public static PropagateFlow instance(Context context) {
         PropagateFlow instance = context.get(pflowKey);
         if (instance == null)
             instance = new PropagateFlow(context);
         return instance;
     }
 
     Env<AttrContext> env;
     //private final Names names;
     private final Log log;
     //private final Symtab syms;
     //private final Types types;
     //private final Check chk;
     //private       TreeMaker make;
     //private final Resolve rs;
     //private Env<AttrContext> attrEnv;
     //private       Lint lint;
     //private final boolean allowImprovedRethrowAnalysis;
     //private final boolean allowImprovedCatchAnalysis;
     public PropagateFlow(Context context) {
         //...and this thing.
         //context.put(pflowKey, this);
         log = Log.instance(context);
     }
 
     private ArrayList<Env<AttrContext>> envs;
     public void analysePropagate(ArrayList<Env<AttrContext>> envs) {
         this.envs = envs;
 //        this.env = env;
 //        for (JCTree p: env.toplevel.props) {
 //            buildGraph((JCTree.JCPropagate)p);
 //        }
         //scan(env.tree);
 
                 /* -find out how to add "throws E" to the tree.
                  *   * JCTree$JCMethodDecl.thrown attribute
                  *   we should construct that object
                  * - Figure what structure to store the constructing
                  *   paths. When the path reaches the LHS,
                  *   we should set "thrown" attribute for each node.
                  */
         PathTree paths;
         for(Env<AttrContext> e : envs) {
             if (e.tree.getTag() == JCTree.PROPAGATE) {
                 try {
                     JCTree.JCPropagate p = (JCTree.JCPropagate)e.tree;
                     log.useSource(e.toplevel.sourcefile);
                     process(p);
                 } catch(Exception ex) {
                     ex.printStackTrace();
                     //report unable to build paths
                 }
             }
         }
     }
 
     class PathNode {
         public PathNode parent;
         public JCTree.JCMethodDecl self;
 
         public PathNode(JCTree.JCMethodDecl m, PathNode parent) {
             this.parent = parent;
             this.self = m;
         }
 
         public void setupThrows(JCTree.JCExpression t) {
             if (!alreadyThrows((JCTree.JCIdent)t)) {
                 this.self.thrown = this.self.thrown.append(t);
                 this.self.type.asMethodType().thrown
                         = this.self.type.asMethodType().thrown.append(t.type);
             }
             if (this.parent != null) {
                 this.parent.setupThrows(t);
             }
         }
 
         public boolean alreadyThrows(JCTree.JCIdent t) {
             for(JCTree.JCExpression e : self.thrown) {
                 JCTree.JCIdent i = (JCTree.JCIdent) e;
                 if (i.sym == t.sym) {
                     return true;
                 }
             }
             return false;
         }
     }
     class PathTree {
         public PathNode node = null;
         public JCTree.JCExpression thrown;
         public boolean atLeastOnePathFound;
         PathTree(JCTree.JCMethodDecl m, JCTree.JCExpression thr) {
             setRoot(m);
             this.thrown = thr;
             this.atLeastOnePathFound = false;
         }
 
         void setRoot(JCTree.JCMethodDecl m) {
             this.node = new PathNode(m, this.node);
         }
 
         void setupThrowPath() {
             this.atLeastOnePathFound = true;
             this.node.setupThrows(thrown);
         }
     }
 
     public void process(JCTree.JCPropagate p) {
         JCTree.JCMethodDecl rhsm = lookupMethod(p.rhs);
         JCTree.JCMethodDecl lhsm = lookupMethod(p.lhs);
 
        if (rhsm == null || lhsm == null) {
            //probably there was a semantic error
            //and lhs or rhs are not bound to any methods
            //bail
            return;
        }
         this.currentTree = new PathTree(rhsm, p.thrown);
 
         this.currentTarget = lhsm;
         buildpath(this.currentTree.node);
 
         if (!this.currentTree.atLeastOnePathFound) {
             log.error(p.pos(),
                         "propagate.no.callgraph");
         }
     }
 
     private PathTree currentTree;
     private JCTree.JCMethodDecl currentTarget;
     void buildpath(PathNode node) {
         scan(node.self.body);
     }
 
     public boolean checkRecursion(JCTree.JCMethodDecl m, PathNode node) {
         if (node == null) {
             return false;
         } else if (node.self.type == m.type) {
             return true;
         } else {
             return checkRecursion(m, node.parent);
         }
     }
 
     public void visitApply(JCTree.JCMethodInvocation m) {
         JCTree.JCMethodDecl found = lookupMethod(m);
         if (found == null) {
             //the 'm' method is not in any sources being
             //compiled.
             //lets do nothing.
         } else if (checkRecursion(found, currentTree.node)) {
             //we already went that way...
             //lets do nothing
         } else if (found.sym == this.currentTarget.sym) {
             currentTree.setRoot(found);
             currentTree.setupThrowPath();
         } else {
             PathNode bk = currentTree.node;
             currentTree.setRoot(found);
             buildpath(currentTree.node);
             currentTree.node = bk;
         }
     }
 
     JCTree.JCMethodDecl lookupMethod(JCTree.JCMethodInvocation m) {
         for(Env<AttrContext> e : envs) {
             JCTree.JCFieldAccess f = (JCTree.JCFieldAccess) m.meth;
             if (f.selected.type == e.tree.type) {
                 JCTree.JCClassDecl clazz = (JCTree.JCClassDecl) e.tree;
                 for (JCTree def : clazz.defs) {
                     if (def.getTag() == JCTree.METHODDEF) {
                         JCTree.JCMethodDecl method = (JCTree.JCMethodDecl) def;
                         if (method.sym == f.sym) {
                             return method;
                         }
                     }
                 }
             }
         }
         return null;
     }
 
     JCTree.JCMethodDecl lookupMethod(JCTree.JCPropagateMethod m) {
         for(Env<AttrContext> e : envs) {
             if (m.selector.selected.type == e.tree.type) {
                 //this is the class of the method
                 JCTree.JCClassDecl clazz = (JCTree.JCClassDecl) e.tree;
                 for (JCTree def : clazz.defs) {
                     if (def.getTag() == JCTree.METHODDEF) {
                         JCTree.JCMethodDecl method = (JCTree.JCMethodDecl) def;
                         if (method.sym == m.sym) {
                             return method;
                         }
                     }
                 }
             }
         }
         return null;
     }
 }
