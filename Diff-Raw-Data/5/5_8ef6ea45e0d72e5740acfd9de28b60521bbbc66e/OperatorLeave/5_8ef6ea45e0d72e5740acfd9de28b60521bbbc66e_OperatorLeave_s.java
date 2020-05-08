 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package operators;
 
 import java.util.ArrayList;
 import blocksworld.Block;
 import blocksworld.State;
 import predicates.PredicateUsedColsNum;
 import predicates.*;
 
 /**
  *
  * @author gaspercat
  */
 public class OperatorLeave extends Operator {
     public OperatorLeave(Block a){
         super(Operator.LEAVE);
         
         // Add preconditions
         pres.add(new PredicateUsedColsNum(PredicateUsedColsNum.N_UNDEFINED));
         pres.add(new PredicateFreeStack());
         pres.add(new PredicatePickedUp(a));
         
         // Add deletions
         rmvs.add(new PredicatePickedUp(a));
         rmvs.add(new PredicateUsedColsNum(PredicateUsedColsNum.N_UNDEFINED));
         
         // Add additions
         adds.add(new PredicateOnTable(a));
         adds.add(new PredicateUsedColsNum(PredicateUsedColsNum.N_INCREASED));
         adds.add(new PredicateFreeArm());
     }
     
     public OperatorLeave(OperatorLeave op){
         super(op);
     }
     
     @Override
     public void instanceValues(Predicate pred, State state){
         if(pred instanceof PredicatePickedUp){
             instanceA(state);
         }else if(pred instanceof PredicateUsedColsNum){
             instanceN(state);
         }
     }
     
     private void instanceA(State state){
         Block val;
         
         // Select value
         // *******************************
         
         Predicate p = state.getPredicate(Predicate.PICKED_UP);
         if(p != null){
             val = p.getA();
         }else{
             ArrayList<Block> blocks = state.getAllBlocks();
             val = blocks.get(rnd.nextInt(blocks.size()));
         }
         
         // Give value to predicates
         // *******************************
         
        pres.get(1).setA(val);
         rmvs.get(0).setA(val);
         adds.get(0).setA(val);
     }
     
     private void instanceN(State state){
         int val = state.getNumColumns();
         
         // Give value to predicates
         // *******************************
         
         pres.get(0).setN(val);
         rmvs.get(1).setN(val);
         adds.get(1).setN(val+1);
     }
     
     @Override
     public Operator clone(){
         OperatorLeave ret = new OperatorLeave(this); 
         return ret;
     }
     
     @Override
     public String toString(){
        Block a = this.pres.get(1).getA();
         return "leave(" + (a == null ? "undef" : a.getName()) + ")";
     }
 }
