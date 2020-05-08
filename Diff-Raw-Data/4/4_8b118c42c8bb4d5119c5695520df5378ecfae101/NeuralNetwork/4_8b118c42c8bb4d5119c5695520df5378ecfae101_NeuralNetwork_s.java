 package aima.core.learning.neural;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 /**
  * Neural network
  *
  * @author Andrew Brown
  */
 public class NeuralNetwork implements Iterable<Layer>, Serializable {
 
     /**
      * State constants; used by perceptrons to notify state
      */
     public static final byte WAITING = 0;
     public static final byte RECEIVING = 1;
     public static final byte SENDING = 2;
     public static final byte COMPLETE = 3;
     public static final byte TRAINING = 4;
     
     /**
      * Turns on debugging
      */
     public static final boolean DEBUG = false;
     
     /**
      * List of interconnected layers
      */
     public ArrayList<Layer> layers;
 
     /**
      * Constructor
      *
      * @param number_of_layers
      * @param perceptrons_per_layer
      * @param function
      * @param sensitivity
      */
     public NeuralNetwork(int number_of_layers, int perceptrons_per_layer, ActivationFunctionInterface function, double sensitivity) {
         // setup
         this.layers = new ArrayList<Layer>();
         // create layers
         for (int i = 0; i < number_of_layers; i++) {
             this.layers.add(new Layer(perceptrons_per_layer, function, sensitivity));
         }
         // connect layers
         for (int j = 0; j < this.layers.size() - 1; j++) {
             this.layers.get(j).connectTo(this.layers.get(j + 1));
         }
     }
 
     /**
      * Uses the neural network to determine the output for the given input;
      * sends the input, returns the output
      *
      * @param input
      * @return
      * @throws SizeDifferenceException
      * @throws WrongSizeException
      */
     public DataList use(DataList input) throws SizeDifferenceException, WrongSizeException {
         // send input
         this.first().in(input);
         // receive output
         DataList output = this.last().out();
         // return
         return output;
     }
 
     /**
      * Trains a network against a given dataset
      *
      * @param input
      * @param expected_output
      * @return the number of iterations spent training the network
      * @throws SizeDifferenceException
      * @throws WrongSizeException
      */
     public int train(DataList[] inputs, DataList[] outputs, double target_error_rate) throws SizeDifferenceException, WrongSizeException {
         if (inputs.length != outputs.length) {
             throw new SizeDifferenceException("Must have the same number of training inputs and outputs");
         }
         // train until average error rate is below target error rate
         int iterations = 0;
         double average_error_rate = 1.0;
         do {
             iterations++;
             double error_sum = 0.0;
             for (int i = 0; i < inputs.length; i++) {
                 DataList output = this.use(inputs[i]);
                 error_sum += DataList.getErrorRate(output, outputs[i]);
                 // train
                 this.last().backpropagate(outputs[i]);
             }
             // calculate error rate
             average_error_rate = error_sum / inputs.length;
         } while (average_error_rate > target_error_rate);
         // return 
         return iterations;
     }
 
     /**
      * Tests multiple datasets, returning the average error
      *
      * @param inputs
      * @param outputs
      * @return a proportion, average of correct results to total results
      * @throws SizeDifferenceException
      * @throws WrongSizeException
      */
     public double test(DataList[] inputs, DataList[] expected_outputs) throws SizeDifferenceException, WrongSizeException {
         if (inputs.length != expected_outputs.length) {
             throw new SizeDifferenceException("Must have the same number of training inputs and outputs");
         }
         // find average error rate
         double error_sum = 0.0;
         for (int i = 0; i < inputs.length; i++) {
             error_sum += this.test(inputs[i], expected_outputs[i]);
         }
         // calculate error rate
         return error_sum / inputs.length;
     }
 
     /**
      * Test a single dataset for correctness
      *
      * @param input
      * @param expected_output
      * @return a proportion, correct results to total results
      * @throws SizeDifferenceException
      * @throws WrongSizeException
      */
     public double test(DataList input, DataList expected_output) throws SizeDifferenceException, WrongSizeException {
         DataList out = this.use(input);
         return DataList.getErrorRate(out, expected_output);
     }
 
     /**
      * Returns the first layer in this network
      *
      * @return the first layer
      */
     public Layer first() {
         return this.layers.get(0);
     }
 
     /**
      * Returns the last layer in this network
      *
      * @return the last layer
      */
     public Layer last() {
         int end = this.layers.size() - 1;
         return this.layers.get(end);
     }
 
     /**
      * Saves a given neural network to file
      *
      * @param file
      * @param net
      * @throws java.io.IOException
      * @throws java.io.FileNotFoundException
      */
     public static void save(File file, NeuralNetwork net) throws java.io.IOException, java.io.FileNotFoundException {
         ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
         out.writeObject(net);
         out.flush();
         out.close();
     }
 
     /**
      * Loads a network from file
      *
      * @param file
      * @return
      * @throws java.io.IOException
      * @throws java.io.FileNotFoundException
      * @throws ClassNotFoundException
      */
     public static NeuralNetwork load(File file) throws java.io.IOException, java.io.FileNotFoundException, ClassNotFoundException {
         ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
        return (NeuralNetwork) in.readObject();
     }
 
     /**
      * Makes the network Iterable
      *
      * @return
      */
     @Override
     public NeuralNetworkIterator iterator() {
         return new NeuralNetworkIterator();
     }
 
     /**
      * Iterator for the layer
      */
     private class NeuralNetworkIterator implements Iterator<Layer> {
 
         /**
          * Tracks the location in the list
          */
         private int index = 0;
 
         /**
          * Checks whether the list is empty or ended
          *
          * @return
          */
         @Override
         public boolean hasNext() {
             return (this.index < NeuralNetwork.this.layers.size());
         }
 
         /**
          * Returns the next element
          *
          * @return
          */
         @Override
         public Layer next() {
             if (!this.hasNext()) {
                 throw new NoSuchElementException();
             }
             Layer next = NeuralNetwork.this.layers.get(this.index);
             this.index++;
             return next;
         }
 
         /**
          * Removes an element; not supported in this implementation
          */
         @Override
         public void remove() {
             throw new UnsupportedOperationException();
         }
     }
 }
