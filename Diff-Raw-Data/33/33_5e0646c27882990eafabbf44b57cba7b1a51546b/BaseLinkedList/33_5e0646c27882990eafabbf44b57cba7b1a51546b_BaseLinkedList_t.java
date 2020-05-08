 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package BBK.PiJ01.linkedLists.lists;
 
 
 import BBK.PiJ01.linkedLists.elements.Container;
 import BBK.PiJ01.linkedLists.elements.ElementInterface;
 import BBK.PiJ01.linkedLists.iterators.Iterator;
 
 /**
  *
  * @author Sam Wright <swrigh11@dcs.bbk.ac.uk>
  */
 public class BaseLinkedList <T extends ElementInterface> {
     protected T first_element;
     protected T last_element;
     protected int number_of_elements = 0;
     
     
     protected void addElement(T new_element) {
         if (last_element != null) {
             last_element.setNext(new_element);
             last_element = new_element;
             ++number_of_elements;
         } else {
             setAsOnlyElement(new_element);
         }
     }
     
     protected void pushElement(T new_element) {
         if (last_element != null) {
             new_element.setNext(first_element);
             first_element = new_element;
             ++number_of_elements;
         } else {
             setAsOnlyElement(new_element);
         }
     }
     
     protected T popEndElement() {
         T popped = null;
         if (last_element != null) {
             popped = last_element;
             last_element = getPrevElement(last_element);
             if (last_element == null)
                 setAsOnlyElement(null);
             --number_of_elements;
         }
         return popped;
     }
     
     protected T popStartElement() {
         T popped = null;
         if (first_element != null) {
             popped = first_element;
             first_element = getNextElement(first_element);
             if (first_element == null)
                 setAsOnlyElement(null);
             --number_of_elements;
         }
         return popped;
     }
     
     protected void deleteElement(T e) {
         if (first_element == e)
             popStartElement();
         else if (last_element == e)
             popEndElement();
         else if (getPrevElement(e) != null)
             deleteNextElement(getPrevElement(e));
         else {
             System.out.println("Element not in a list.");
             return; 
         }
         cleanDeletedElement(e);
         --number_of_elements;
     }
     
     protected T getNextElement(T e) {
         if (e == null)
             return null;
         return (T) e.getNext();
     }
     
     protected T getPrevElement(T e) {
         T test_element;
         for (Iterator itr = getForwardItr(); itr.hasNext();) {
             test_element = (T) itr.next();
             if (test_element.getNext() == e)
                 return test_element;
         }
         return null;
     }
     
     public void insertNextToRef(T e, T ref) {
         T ref_next = getNextElement(ref);
         ref.setNext(e);
         e.setNext(ref_next);
         if (ref_next == null) {
             last_element = e;
         }
         ++number_of_elements;
     }
     
     public void insertPrevToRef(T e, T ref) {
         T ref_prev = getPrevElement(ref);
         try {
             ref_prev.setNext(e);
         } catch (NullPointerException err) {
             first_element = e;
         }
         e.setNext(ref);
         ++number_of_elements;
     }
     
     protected void cleanDeletedElement(T e) {
         e.setNext(null);
     }
     
     protected void setAsOnlyElement(T e) {
         first_element = e;
         last_element = e;
         number_of_elements = 1;
         if (e != null)
             cleanDeletedElement(e);
     } 
     
     protected void deleteNextElement(T before_deleted) {
         T after_deleted;
         try{
             after_deleted = (T) before_deleted.getNext().getNext();
         } catch (NullPointerException e) {
             after_deleted = null;
         }
         before_deleted.setNext(after_deleted);
     }
     
     public void printUsingItr(Iterator itr) {
         System.out.format("List has %d elements.  Here they are:\n", number_of_elements);
         
         while(itr.hasNext()) {
             System.out.println( "\t" + itr.next().toString() );
         }
         System.out.println("");   
     }
     
     public Iterator getForwardItr() {
         return new ForwardIterator();
     }
     
     public Iterator getBackwardItr() {
         return new BackwardIterator();
     }
     
     protected class ForwardIterator extends Iterator<T> {
         public ForwardIterator() {
             super(first_element, last_element);
         }
         
        protected T getNextElementFromList() {
             return getNextElement((T) next_element);
         }
         
         protected void insertAfterRef(T e, T ref) {
             insertNextToRef(e, ref);
         }
         
         protected void insertBeforeRef(T e, T ref) {
             insertPrevToRef(e, ref);
         }
     }
     
     protected class BackwardIterator extends Iterator<T> {
         public BackwardIterator() {
             super(last_element, first_element);
         }
         
        protected T getNextElementFromList() {
             return getPrevElement((T) next_element);
         }
         
         protected void insertAfterRef(T e, T ref) {
             insertPrevToRef(e, ref);
         }
         
         protected void insertBeforeRef(T e, T ref) {
             insertNextToRef(e, ref);
         }
     }
 }
