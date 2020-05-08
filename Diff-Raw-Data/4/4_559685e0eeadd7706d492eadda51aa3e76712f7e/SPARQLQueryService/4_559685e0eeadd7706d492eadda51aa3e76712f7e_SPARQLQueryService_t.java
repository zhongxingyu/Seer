 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.ilrt.wf.facets.sparql;
 
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.graph.Triple;
 import com.hp.hpl.jena.query.DataSource;
 import com.hp.hpl.jena.query.DatasetFactory;
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.ResIterator;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.sparql.algebra.Op;
 import com.hp.hpl.jena.sparql.algebra.OpAsQuery;
 import com.hp.hpl.jena.sparql.algebra.TransformBase;
 import com.hp.hpl.jena.sparql.algebra.Transformer;
 import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
 import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
 import com.hp.hpl.jena.sparql.algebra.op.OpGraph;
 import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
 import com.hp.hpl.jena.sparql.algebra.op.OpNull;
 import com.hp.hpl.jena.sparql.algebra.op.OpProject;
 import com.hp.hpl.jena.sparql.algebra.op.OpSlice;
 import com.hp.hpl.jena.sparql.core.BasicPattern;
 import com.hp.hpl.jena.sparql.core.Var;
 import com.hp.hpl.jena.sparql.expr.E_LessThan;
 import com.hp.hpl.jena.sparql.expr.E_LessThanOrEqual;
 import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
 import com.hp.hpl.jena.sparql.expr.E_Regex;
 import com.hp.hpl.jena.sparql.expr.E_Str;
 import com.hp.hpl.jena.sparql.expr.ExprVar;
 import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueNode;
 import java.util.AbstractList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.ilrt.wf.facets.FacetQueryService;
 import org.ilrt.wf.facets.FacetState;
 import org.ilrt.wf.facets.constraints.Constraint;
 import org.ilrt.wf.facets.constraints.RangeConstraint;
 import org.ilrt.wf.facets.constraints.RegexpConstraint;
 import org.ilrt.wf.facets.constraints.UnConstraint;
 import org.ilrt.wf.facets.constraints.ValueConstraint;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author pldms
  */
 public class SPARQLQueryService implements FacetQueryService {
 
     private final static Logger log = LoggerFactory.getLogger(SPARQLQueryService.class);
 
     private final static Var SUBJECT = Var.alloc("s");
 
     private final QEFactory qef;
 
     public SPARQLQueryService(QEFactory qef) {
         this.qef = qef;

        // TODO: Work around. Remove this when ARQ is fixed

        System.setProperty("http.keepAlive", "false");
     }
 
     @Override
     public List<Resource> getRefinements(Resource base, Property prop, boolean isBroader) {
         Tree<Resource> refinements =
                 getHierarchy(base, prop, isBroader, true) ;
         return new TreeChildrenList<Resource>(refinements);
     }
 
     @Override
     public Tree<Resource> getHierarchy(Resource base, Property prop,
             boolean isBroader) {
         return getHierarchy(base, prop, isBroader, false);
     }
 
     /**
      * Common code for refinements and hierarchy. This is a bit confusing, but
      * essentially we use the same basic query, but fill in some values for the
      * only children case (which is anchored at base).
      * @param base Where to begin
      * @param prop Property relating elements in the hierarchy
      * @param isBroader If true go from objects to subjects.
      * @param onlyChildren Only get immediate children of base
      * @return
      */
     protected Tree<Resource> getHierarchy(Resource base, Property prop,
             boolean isBroader, boolean onlyChildren) {
         // If we are only getting immediate children anchor accordingly
         String s = (onlyChildren && !isBroader) ? "<" + base.getURI() + ">" : "?s";
         String o = (onlyChildren && isBroader) ? "<" + base.getURI() + ">" : "?o";
         // Get label for children, according to which will be the child
         String labelFor = (isBroader) ? "?s" : "?o";
 
         Query query = QueryFactory.create(
                 String.format(
                 "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                 "construct " +
                 "{ " +
                 "%1$s <%2$s> %3$s ." +
                 "%4$s rdfs:label ?label . " +
                 "}\n" +
                 "{\n" +
                 "  graph ?g  { %1$s <%2$s> %3$s \n" +
                 "  optional { %4$s rdfs:label ?label } }\n" +
                 "}",
                    s, prop.getURI(), o, labelFor
                 ));
 
         QueryExecution qe = qef.get( query );
 
         Model hierarchy = qe.execConstruct();
         qe.close();
 
         // Make local base, so we don't have to pass model about
         Resource baseL = hierarchy.getResource(base.getURI());
 
         Set<Resource> visited = new HashSet<Resource>(); // avoid loops
         if (isBroader) return makeBroaderTreeStartingAt(baseL, prop, visited);
         else return makeNarrowerTreeStartingAt(baseL, prop, visited);
     }
 
     /**
      * Make a tree starting at base and going up broader relations
      * @param base
      * @param prop
      * @return
      */
     private Tree<Resource> makeBroaderTreeStartingAt(Resource base, Property prop, Set visited) {
         if (visited.contains(base)) throw new RuntimeException("Loop in hierarchy");
         visited.add(base);
         Tree<Resource> t = new Tree<Resource>(base);
         ResIterator r = base.getModel().listSubjectsWithProperty(prop, base);
         while (r.hasNext()) {
             t.addChild( makeBroaderTreeStartingAt(r.next(), prop, visited) );
         }
         r.close();
         return t;
     }
 
     /**
      * Make a tree starting at base and moving down narrower relations
      * @param base
      * @param prop
      * @return
      */
     private Tree<Resource> makeNarrowerTreeStartingAt(Resource base, Property prop, Set visited) {
         if (visited.contains(base)) throw new RuntimeException("Loop in hierarchy");
         visited.add(base);
         Tree<Resource> t = new Tree<Resource>(base);
         StmtIterator r = base.listProperties(prop);
         while (r.hasNext()) {
             t.addChild( makeNarrowerTreeStartingAt(r.next().getResource(), prop, visited) );
         }
         r.close();
         return t;
     }
 
     @Override
     public Map<FacetState, Integer> getCounts(List<? extends FacetState> currentFacetStates) {
         // Inefficient first pass
         Map<FacetState, Integer> counts = new HashMap<FacetState, Integer>();
         for (FacetState state: currentFacetStates) {
             getStateCounts(state, currentFacetStates, counts);
         }
         return counts;
     }
 
     @Override
     public int getCount(List<? extends FacetState> currentFacetStates) {
         return getCount(statesToConstraints(currentFacetStates));
     }
 
     @Override
     public List<Resource> getResults(List<? extends FacetState> currentFacetStates, int offset, int number) {
         // Remember this query. We'll use it later.
         Op opBasic = constraintsToOp(statesToConstraints(currentFacetStates));
         Op op = opBasic;
         op = new OpProject(op, Collections.singletonList(SUBJECT));
         // Apply limit and offset
         op = new OpSlice(op, offset, number);
         Query q = OpAsQuery.asQuery(op);
         q.addDescribeNode(SUBJECT);
         q.setQueryDescribeType();
 
         QueryExecution qe = qef.get(q);
         Model m = qe.execDescribe();
         qe.close();
 
         // We now have a model containing the things we were interested in
         // plus a bunch of info about them. We want to return pointer to those
         // original things (backed by this model), so we execute the same query.
 
         q = OpAsQuery.asQuery(opBasic);
         q.addResultVar(SUBJECT);
         q.setQuerySelectType();
         DataSource ds = DatasetFactory.create();
         ds.addNamedModel("urn:x-ilrt:graph", m);
         qe = QueryExecutionFactory.create(q, ds);
         ResultSet res = qe.execSelect();
 
         List<Resource> results = new LinkedList<Resource>();
         while (res.hasNext()) {
             Resource result = res.next().getResource(SUBJECT.getVarName());
             // We need to reattach to original model. Nasty.
             result = result.isAnon() ?
                 m.createResource(result.getId()) :
                 m.getResource(result.getURI()) ;
             results.add(result);
         }
 
         qe.close();
 
         return results;
     }
 
     protected void getStateCounts(FacetState state,
             List<? extends FacetState> currentFacetStates, Map<FacetState, Integer> counts) {
         // Get contrast state
         List<FacetState> otherStates = new LinkedList<FacetState>(currentFacetStates);
         otherStates.remove(state);
         for (FacetState futureState: state.getRefinements()) {
             counts.put(futureState, getCount(futureState, otherStates));
         }
     }
 
     protected int getCount(FacetState ffs, List<FacetState> otherStates) {
         return getCount(statesToConstraints(otherStates, ffs));
     }
 
     protected int getCount(Collection<Constraint> constraints) {
         Op op = constraintsToOp(constraints);
 
         // just get subject
         op = new OpProject(op, Collections.singletonList(SUBJECT));
 
         Query q = OpAsQuery.asQuery(op);
         QueryExecution qe = qef.get(q);
 
         int count = 0;
         ResultSet r = qe.execSelect();
         while (r.hasNext()) { count++; r.next(); }
 
         qe.close();
         
         return count;
     }
 
     protected Op constraintsToOp(Collection<Constraint> cons) {
         Op op = null;
         for (Constraint con: cons) {
             Op newOp = constraintToOp(con);
             if (newOp instanceof OpNull) continue;
             // We wrap each constraint in a new graph
             // TODO prettify queries. this messes up previous work
             else if (op == null) op = new OpGraph(genVar(), newOp);
             else op = OpJoin.create(op, new OpGraph(genVar(), newOp));
 
         }
 
         // Completelty unconstrained is not permitted
         if (op == null) throw new RuntimeException("Operation is completely unconstrained");
         op = Transformer.transform(new QueryCleaner(), op);
         return op;
     }
 
     protected Op constraintToOp(Constraint constraint) {
         if (constraint instanceof UnConstraint) return OpNull.create();
         else if (constraint instanceof ValueConstraint) {
             return tripleToBGP(SUBJECT,
                     constraint.getProperty().asNode(),
                     ((ValueConstraint) constraint).getValue().asNode()
                     );
         } else if (constraint instanceof RangeConstraint) {
             RangeConstraint rc = (RangeConstraint) constraint;
             Var val = genVar(); // TODO generate
             // create filters
             return OpFilter.filter(
                     new E_LogicalAnd(
                         new E_LessThanOrEqual(
                             NodeValueNode.makeNode(rc.getFrom().asNode()),
                             new ExprVar(val) ),
                         new E_LessThan(
                             new ExprVar(val),
                             NodeValueNode.makeNode(rc.getTo().asNode()) )
                             ),
                     tripleToBGP(SUBJECT, constraint.getProperty().asNode(), val)
                     );
         } else if (constraint instanceof RegexpConstraint) {
             RegexpConstraint rc = (RegexpConstraint) constraint;
             Var val = genVar(); // TODO generate
             return OpFilter.filter(
                     new E_Regex(
                       new E_Str(new ExprVar(val)),
                       rc.getRegexp(),
                       "i"
                       ),
                     tripleToBGP(SUBJECT, constraint.getProperty().asNode(), val)
                     );
         } else throw new RuntimeException("Unknown constraint type");
     }
 
     private Op tripleToBGP(Node s, Node p, Node o) {
         BasicPattern bgp = new BasicPattern();
         bgp.add(Triple.create(s, p, o));
         return new OpBGP(bgp);
     }
 
     private Collection<Constraint> statesToConstraints(Collection<? extends FacetState> states, FacetState... moreStates) {
         Collection<Constraint> cs = new LinkedList<Constraint>();
         for (FacetState s: states) cs.addAll(s.getConstraints());
         for (FacetState s: moreStates) cs.addAll(s.getConstraints());
         return cs;
     }
 
     private int varCount = 0;
     private Var genVar() { varCount++; return Var.alloc("v" + varCount); }
 
     /**
      * This is for cleaning up the query having created it
      */
     private static class QueryCleaner extends TransformBase
     {
         @Override
         public Op transform(OpJoin join, Op left, Op right) {
             if (!(left instanceof OpFilter && right instanceof OpFilter)) return join;
 
             OpFilter leftF = (OpFilter) left;
             OpFilter rightF = (OpFilter) right;
 
             if (!(leftF.getSubOp() instanceof OpBGP &&
                     rightF.getSubOp() instanceof OpBGP)) return join;
 
             ((OpBGP) leftF.getSubOp()).getPattern().addAll(((OpBGP) rightF.getSubOp()).getPattern());
             leftF.getExprs().addAll(rightF.getExprs());
             return leftF;
         }
     }
 
     /**
      * Convenience wrapper to avoid copying
      * @param <T>
      */
     private static class TreeChildrenList<T> extends AbstractList<T> {
         private final Tree<T> tree;
 
         public TreeChildrenList(Tree<T> tree) {
             super();
             this.tree = tree;
         }
 
         @Override
         public T get(int i) {
             return tree.getChildren().get(i).getValue();
         }
 
         @Override
         public int size() {
             return tree.getChildren().size();
         }
 
         @Override
         public Iterator<T> iterator() {
             return new TreeIterator<T>(tree.getChildren().iterator());
         }
 
     }
 
     /**
      * Helper for the above, to avoid thrashing linked list
      *
      * @param <T>
      */
     private static class TreeIterator<T> implements Iterator<T> {
         private Iterator<Tree<T>> it;
 
         public TreeIterator(Iterator<Tree<T>> wrappedIt) {
             this.it = wrappedIt;
         }
 
         @Override
         public boolean hasNext() { return it.hasNext(); }
 
         @Override
         public T next() { return it.next().getValue(); }
 
         @Override
         public void remove() { throw new UnsupportedOperationException("Not supported"); }
     }
 }
