 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
package src.scheduler;
 
 import java.util.Random;
 
 /**
  *
  * @author mateusz
  */
 public class SchedulerHeap implements SchedulerData {
 
     private Job[] heap;
     private int n; // number of elements in heap
     private final int INITIAL_SIZE = 16;
 
     public SchedulerHeap() {
         this.heap = new Job[INITIAL_SIZE];
         for (int i = 0; i < INITIAL_SIZE; i++) {
             this.heap[i] = new Job();
         }
         this.n = 0;
     }
 
     private int getN() {
         return this.n;
     }
 
     private void swap(int i, int j) {
         Job tmp = new Job(heap[i].getId(), heap[i].getPriority());
         heap[i].setId(heap[j].getId());
         heap[i].setPriority(heap[j].getPriority());
         heap[j].setId(tmp.getId());
         heap[j].setPriority(tmp.getPriority());
     }
 
     @Override
     public void add(Job a) {
         //System.out.println("Number of elements: " + this.getN() + ", capacity: " + this.heap.length);
         if (this.n == this.heap.length) {
             Job[] tmp = new Job[2 * n];
             for (int i = n; i < 2 * n; i++) {
                 tmp[i] = new Job();
             }
             System.arraycopy(heap, 0, tmp, 0, n);
             heap = tmp;
         }
 
         for (int i = 0; i < n; i++) {
             if (a.getPriority() == heap[i].getPriority()) // only one element with specific priority
             {
                 return;
             }
         }
 
         heap[n] = a;
         this.heapUp();
         n++;    
     }
 
     private void heapUp() {
         for (int i = n; i > 0; i--) {
             if (heap[i].getPriority() > heap[(i - 1) / 2].getPriority()) {
                 this.swap(i, (i - 1) / 2);
             }
         }
     }
 
     @Override
     public Job remove() {
         if (n == 0)
             return null;
         
         Job removed = heap[0];
 
         heap[0].setId(heap[this.getN()-1].getId());
         heap[0].setPriority(heap[this.getN()-1].getPriority());
         n--;
         heap[this.getN()].reset();
         this.heapDown();
         return removed;
     }
 
     private void heapDown() {
         int i = 0;
         while (2 * i + 1 < n) // dopoki jest chociaz jedno dziecko
         {
             if (2 * i + 2 >= n) // jest tylko lewe dziecko
             {
                 if (heap[i].getPriority() < heap[2 * i + 1].getPriority()) { // jesli dziecko jest wieksze od rodzica
                     swap(i, 2 * i + 1);
                     i = 2 * i + 1;
                 } else {
                     i++;
                 }
             } else // jest oboje dzieci
             if (heap[i].getPriority() < heap[2 * i + 1].getPriority() || heap[i].getPriority() < heap[2 * i + 2].getPriority()) // jedno z dzieci jest wieksze od rodzica
             {
                 if (heap[2 * i + 1].getPriority() > heap[2 * i + 2].getPriority()) {  // lewe jest wieksze od prawego
                     swap(i, 2 * i + 1);
                     i = 2 * i + 1;
                 } else { // prawe jest wieksze od lewego
                     swap(i, 2 * i + 2);
                     i = 2 * i + 2;
                 }
             } else {
                 i++;
             }
         }
     }
     
     @Override
     public boolean isEmpty () {
         return (n == 0 ) ? true : false;
     }
     
     public void writeHeap () {
         System.out.println("Number of elements:\t" + this.n);
         for (Job i : this.heap) {
             if (i.getId() != -1) {
                 System.out.println(i);
             }
         }
     }
 
     @Override
     public void changePriority(int id, int priority) {
         boolean found = false;
         for (Job i : this.heap) {
             if (i.getId() == id) {
                 i.setPriority(priority);
                 found = true;
             }
         }
         
         if (found == false) {
             System.out.println("! Priority not changed. There is no element with such ID!");
             return;
         }
         
         writeHeap();
         heapDown();
     }
 
     public static void main(String[] args) {
         SchedulerHeap jh = new SchedulerHeap();
 
         Random r = new Random();
         for (int i = 0; i < 5; i++) {
             jh.add(new Job(jh.getN() + 1, r.nextInt(1000)));
         }
 
         jh.writeHeap();
         System.out.println("---");
         
         //jh.remove();
         jh.changePriority(1, 1);
         
         jh.writeHeap();
         System.out.println("---");
     }
 }
