 package aima.core.learning.neural2;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 /**
  *
  * @author andrew
  */
 public class Layer implements Iterable<Perceptron>, Serializable {
 
     /**
      * List of perceptrons in this layer
      */
     public ArrayList<Perceptron> perceptrons;
 
     /**
      * Constructor
      * @param count
      * @param g
      */
     public Layer(int count, ActivationFunction_I g) {
         // setup
         this.perceptrons = new ArrayList<Perceptron>();
         // create perceptrons
         for (int i = 0; i < count; i++) {
             this.perceptrons.add(new Perceptron(g));
         }
     }
 
     /**
      * Connects a layer to another layer; every perceptron is connected to every
      * perceptron in the next layer
      * @param downstream
      */
     public void connectTo(Layer downstream) {
         for (Perceptron a : this.perceptrons) {
             for (Perceptron b : downstream.perceptrons) {
                 a.addOutput(b);
             }
         }
     }
 
     /**
      * Sends initial input data into the network
      * @param input
      * @throws SizeDifferenceException
      */
     public void in(ArrayList<Double> input) throws SizeDifferenceException {
         if (input.size() != this.size()) {
             throw new SizeDifferenceException("DataSet size (" + input.size() + ") and Layer size (" + this.size() + ") do not match");
         }
         // send to perceptrons
         for (int i = 0; i < this.perceptrons.size(); i++) {
             this.perceptrons.get(i).in(input.get(i));
         }
     }
 
     /**
      * Receives final output data from the network
      * @return
      */
     public ArrayList<Double> out() {
         ArrayList<Double> output = new ArrayList<Double>(this.perceptrons.size());
         // wait until all processing is complete
         boolean complete;
         do {
             complete = true;
             for (int i = 0; i < this.perceptrons.size(); i++) {
                 if (!this.perceptrons.get(i).isComplete()) {
                     complete = false;
                 }
             }
         } while (!complete);
         // get values
        for (int i = 0; i < this.perceptrons.size(); i++) {
            output.set(i, this.perceptrons.get(i).result);
         }
         // return
         return output;
     }
 
     /**
      * Returns the number of perceptrons in this layer
      * @return
      */
     public int size() {
         return this.perceptrons.size();
     }
 
     /**
      * Makes the layer Iterable
      * @return
      */
     public LayerIterator iterator() {
         return new LayerIterator();
     }
 
     /**
      * Iterator for the layer
      */
     private class LayerIterator implements Iterator<Perceptron> {
 
         /**
          * Tracks the location in the list
          */
         private int index = 0;
 
         /**
          * Checks whether the list is empty or ended
          * @return
          */
         public boolean hasNext() {
             return (this.index < Layer.this.perceptrons.size());
         }
 
         /**
          * Returns the next element
          * @return
          */
         public Perceptron next() {
             if (!this.hasNext()) {
                 throw new NoSuchElementException();
             }
             Perceptron next = Layer.this.perceptrons.get(this.index);
             this.index++;
             return next;
         }
 
         /**
          * Removes an element; not supported in this implementation
          */
         public void remove() {
             throw new UnsupportedOperationException();
         }
     }
 }
