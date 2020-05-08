 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package uk.co.mtford.jalp.abduction;
 
 import org.apache.log4j.Logger;
 import uk.co.mtford.jalp.abduction.logic.instance.DenialInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.constraints.ConstraintInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.constraints.IConstraintInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.equalities.IEqualityInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.PredicateInstance;
 
 import java.util.LinkedList;
 import java.util.List;
 
 
 /**
  * An ASystem store of collectables.
  *
  * @author mtford
  */
 public class Store implements Cloneable {
 
     private static final Logger LOGGER = Logger.getLogger(Store.class);
 
     public List<PredicateInstance> abducibles;
     public List<DenialInstance> denials;
     public List<IEqualityInstance> equalities;
     public List<IConstraintInstance> constraints;
 
 
     public Store() {
         abducibles = new LinkedList<PredicateInstance>();
         denials = new LinkedList<DenialInstance>();
         equalities = new LinkedList<IEqualityInstance>();
         constraints = new LinkedList<IConstraintInstance>();
     }
 
     public Store shallowClone() {
         Store store = new Store();
         store.abducibles.addAll(abducibles);
         store.denials.addAll(denials);
         store.equalities.addAll(equalities);
         return store;
     }
 
 }
