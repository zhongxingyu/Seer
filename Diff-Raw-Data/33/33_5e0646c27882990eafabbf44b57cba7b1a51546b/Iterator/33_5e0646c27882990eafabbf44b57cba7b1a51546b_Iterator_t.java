 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package BBK.PiJ01.linkedLists.iterators;
 
 import BBK.PiJ01.linkedLists.elements.Element;
 import BBK.PiJ01.linkedLists.elements.ElementInterface;
 import BBK.PiJ01.linkedLists.lists.BaseLinkedList;
 
 /**
  *
  * @author Sam Wright <swrigh11@dcs.bbk.ac.uk>
  */
 public abstract class Iterator <T extends ElementInterface> {
     protected T next_element;
     protected T finishing_element;
     protected T this_element;
     protected T prev_element;
     protected boolean iterating = true;
     
     
     public Iterator(T next_element, T finishing_element) {
         this.next_element = next_element;
         this.finishing_element = finishing_element;
         if (finishing_element == null)
             iterating = false;
     }
     
     public boolean hasNext() {
         return iterating;
     }
     
     public T next() {
         prev_element = this_element;
         this_element = next_element;
        next_element = getNextElementFromList();
         if (this_element == finishing_element)
             iterating = false;
         return this_element;
     }
     
     public void insertAhead(T e) {
         insertAfterRef(e, this_element);
         
         if (this_element == finishing_element) {
             finishing_element = e;
             iterating = true;
         }
         
         next_element = e;
     }
     
     public void insertBehind(T e) {
         insertBeforeRef(e, this_element);
         prev_element = e;
     }
     
    abstract protected T getNextElementFromList();
     abstract protected void insertAfterRef(T e, T ref);
     abstract protected void insertBeforeRef(T e, T ref);
 }
