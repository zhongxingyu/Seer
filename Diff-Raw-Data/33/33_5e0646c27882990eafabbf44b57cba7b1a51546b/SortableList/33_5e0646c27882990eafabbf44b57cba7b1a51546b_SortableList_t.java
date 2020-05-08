 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package BBK.PiJ01.linkedLists.lists;
 
 import BBK.PiJ01.linkedLists.elements.SortableContainer;
 import BBK.PiJ01.linkedLists.iterators.SwappingIterator;
 
 /**
  *
  * @author Sam Wright <swrigh11@dcs.bbk.ac.uk>
  */
 public class SortableList <T extends Comparable<T>> 
                         extends BaseLinkedList<SortableContainer<T>> {
     
     public void insert(T value) {
         addElement(new SortableContainer<T>(value));
     }
     
     @Override
     protected void cleanDeletedElement(SortableContainer<T> e) {
         e.setNext(null);
         e.setPrev(null);
     }
     
     @Override
     protected SortableContainer<T> getPrevElement(SortableContainer<T> e) {
         if (e.getPrev() == null)
             return null;
         return (SortableContainer<T>) e.getPrev();
     }
     
     @Override
     public SwappingIterator<T> getForwardItr() {
         return new ForwardIterator();
     }
     
     public SwappingIterator<T> getForwardItr(SortableContainer<T> first, SortableContainer<T> last) {
         return new ForwardIterator(first, last);
     }
     
     @Override
     public SwappingIterator<T> getBackwardItr() {
         return new BackwardIterator();
     }
     
     public SwappingIterator<T> getBackwardItr(SortableContainer<T> first, SortableContainer<T> last) {
         return new BackwardIterator(first, last);
     }
     
     protected class ForwardIterator extends SwappingIterator<T> {
         public ForwardIterator(SortableContainer<T> first, SortableContainer<T> last) {
             super(first, last);
         }
         
         public ForwardIterator() {
             super(first_element, last_element);
         }
         
         @Override
        protected SortableContainer<T> getNextElementFromList() {
             return getNextElement(next_element);
         }
        
         @Override
         protected void insertAfterRef(SortableContainer<T> e, SortableContainer<T> ref) {
             insertNextToRef(e, ref);
         }
 
         @Override
         protected void insertBeforeRef(SortableContainer<T> e, SortableContainer<T> ref) {
             insertPrevToRef(e, ref);
         }
     }
     
     protected class BackwardIterator extends SwappingIterator<T> {
         public BackwardIterator(SortableContainer<T> first, SortableContainer<T> last) {
             super(first, last);
         }
         
         public BackwardIterator() {
             super(last_element, first_element);
         }
 
         @Override
        protected SortableContainer<T> getNextElementFromList() {
             return getPrevElement(next_element);
         }
 
         @Override
         protected void insertAfterRef(SortableContainer<T> e, SortableContainer<T> ref) {
             insertPrevToRef(e, ref);
         }
 
         @Override
         protected void insertBeforeRef(SortableContainer<T> e, SortableContainer<T> ref) {
             insertNextToRef(e, ref);
         }
     }
 }
