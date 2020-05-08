 package no.utgdev.ctrnngame;
 
 import java.util.Arrays;
 import no.utgdev.ann.core.NeuralLayer;
 import no.utgdev.ann.core.StructuredANN;
 import no.utgdev.ann.core.Synapse;
 import no.utgdev.ann.core.neuron.Neuron;
 import no.utgdev.ann.core.neuron.chain.Chain;
 import no.utgdev.ann.core.neuron.chain.link.BiasLink;
 import no.utgdev.ann.core.neuron.chain.link.CTRNNTransformation;
 import no.utgdev.ann.core.neuron.chain.link.Summation;
 
 /**
  * Hello world!
  *
  */
 public class ANNBuilder {
 
    public static final boolean modifiedTopology = false;
 
     public static StructuredANN build(double[] data) {
         if (modifiedTopology) {
             return modifiedbuild(data);
         } else {
             return nonmodifiedbuild(data);
         }
     }
 
     public static StructuredANN nonmodifiedbuild(double[] data) {
         /**
          * data - must be 34 long data[0..4] == gains for hidden then output
          * data[4..8] == time constants for hidden then output data[8..34] ==
          * weights data[8..18] == weights originating in inputlayer data[18..26]
          * == weights originating in hiddenLayer data[26..30] == weights
          * originating in outputlayer data[30..34] == weights originating in
          * baislayer
          */
         Neuron[] hiddenNeurons = new Neuron[]{
             new Neuron(new Chain(new Summation(), new CTRNNTransformation(data[0], data[4]))),
             new Neuron(new Chain(new Summation(), new CTRNNTransformation(data[1], data[5])))
         };
         Neuron[] outputNeurons = new Neuron[]{
             new Neuron(new Chain(new Summation(), new CTRNNTransformation(data[2], data[6]))),
             new Neuron(new Chain(new Summation(), new CTRNNTransformation(data[3], data[7])))
         };
         Neuron[] inputNeurons = new Neuron[]{
             new Neuron(new Chain(new Summation(), new CTRNNTransformation())),
             new Neuron(new Chain(new Summation(), new CTRNNTransformation())),
             new Neuron(new Chain(new Summation(), new CTRNNTransformation())),
             new Neuron(new Chain(new Summation(), new CTRNNTransformation())),
             new Neuron(new Chain(new Summation(), new CTRNNTransformation()))
         };
         Neuron[] biasNeurons = new Neuron[]{
             new Neuron(new Chain(new BiasLink()))
         };
         NeuralLayer inputLayer = new NeuralLayer(Arrays.asList(inputNeurons));
         NeuralLayer hiddenLayer = new NeuralLayer(Arrays.asList(hiddenNeurons));
         NeuralLayer outputLayer = new NeuralLayer(Arrays.asList(outputNeurons));
         NeuralLayer biasLayer = new NeuralLayer(Arrays.asList(biasNeurons));
 
         addFullConnection(inputLayer, hiddenLayer, Arrays.copyOfRange(data, 8, 18));
         addFullConnection(hiddenLayer, hiddenLayer, Arrays.copyOfRange(data, 18, 22));
         addFullConnection(hiddenLayer, outputLayer, Arrays.copyOfRange(data, 22, 26));
         addFullConnection(outputLayer, outputLayer, Arrays.copyOfRange(data, 26, 30));
         addFullConnection(biasLayer, hiddenLayer, Arrays.copyOfRange(data, 30, 32));
         addFullConnection(biasLayer, outputLayer, Arrays.copyOfRange(data, 32, 34));
 
         StructuredANN ann = new StructuredANN();
         ann.addLayer(NeuralLayer.Type.INPUT, inputLayer);
         ann.addLayer(NeuralLayer.Type.HIDDEN, hiddenLayer);
         ann.addLayer(NeuralLayer.Type.HIDDEN, biasLayer);
         ann.addLayer(NeuralLayer.Type.OUTPUT, outputLayer);
 
         return ann;
     }
 
     private static void addFullConnection(NeuralLayer from, NeuralLayer to, double[] weights) {
         int wCount = 0;
         for (Neuron anFrom : from.getNeurons()) {
             for (Neuron anTo : to.getNeurons()) {
                 Synapse s = new Synapse(anFrom, anTo, weights[wCount++]);
                 anTo.addInputSynapse(s);
             }
         }
     }
 //    public static void main(String[] args) {
 //        double[] data = {
 //            1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
 //            1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
 //            1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
 //            1.0, 1.0, 1.0, 1.0
 //        };
 //        StructuredANN ann = build(data);
 //        double[] input = {1.0, 1.0, 1.0, 1.0, 1.0};
 //        for (int i = 0; i < 10; i++) {
 //            System.out.println(Arrays.toString(ann.update(input)));
 //        }
 //    }
 
     private static StructuredANN modifiedbuild(double[] data) {
         /**
          * data - must be 34 long data[0..4] == gains for hidden then output
          * data[4..8] == time constants for hidden then output data[8..34] ==
          * weights data[8..20] == weights originating in inputlayer data[20..28]
          * == weights originating in hiddenLayer data[28..32] == weights
          * originating in outputlayer data[32..36] == weights originating in
          * baislayer
          */
         Neuron[] hiddenNeurons = new Neuron[]{
             new Neuron(new Chain(new Summation(), new CTRNNTransformation(data[0], data[4]))),
             new Neuron(new Chain(new Summation(), new CTRNNTransformation(data[1], data[5])))
         };
         Neuron[] outputNeurons = new Neuron[]{
             new Neuron(new Chain(new Summation(), new CTRNNTransformation(data[2], data[6]))),
             new Neuron(new Chain(new Summation(), new CTRNNTransformation(data[3], data[7])))
         };
         Neuron[] inputNeurons = new Neuron[]{
             new Neuron(new Chain(new Summation(), new CTRNNTransformation())),
             new Neuron(new Chain(new Summation(), new CTRNNTransformation())),
             new Neuron(new Chain(new Summation(), new CTRNNTransformation())),
             new Neuron(new Chain(new Summation(), new CTRNNTransformation())),
             new Neuron(new Chain(new Summation(), new CTRNNTransformation())),
             new Neuron(new Chain(new Summation(), new CTRNNTransformation())),};
         Neuron[] biasNeurons = new Neuron[]{
             new Neuron(new Chain(new BiasLink()))
         };
         NeuralLayer inputLayer = new NeuralLayer(Arrays.asList(inputNeurons));
         NeuralLayer hiddenLayer = new NeuralLayer(Arrays.asList(hiddenNeurons));
         NeuralLayer outputLayer = new NeuralLayer(Arrays.asList(outputNeurons));
         NeuralLayer biasLayer = new NeuralLayer(Arrays.asList(biasNeurons));
 
         addFullConnection(inputLayer, hiddenLayer, Arrays.copyOfRange(data, 8, 20));
         addFullConnection(hiddenLayer, hiddenLayer, Arrays.copyOfRange(data, 20, 24));
         addFullConnection(hiddenLayer, outputLayer, Arrays.copyOfRange(data, 24, 28));
         addFullConnection(outputLayer, outputLayer, Arrays.copyOfRange(data, 28, 32));
         addFullConnection(biasLayer, hiddenLayer, Arrays.copyOfRange(data, 32, 34));
         addFullConnection(biasLayer, outputLayer, Arrays.copyOfRange(data, 34, 36));
 
         StructuredANN ann = new StructuredANN();
         ann.addLayer(NeuralLayer.Type.INPUT, inputLayer);
         ann.addLayer(NeuralLayer.Type.HIDDEN, hiddenLayer);
         ann.addLayer(NeuralLayer.Type.HIDDEN, biasLayer);
         ann.addLayer(NeuralLayer.Type.OUTPUT, outputLayer);
 
         return ann;
     }
 }
