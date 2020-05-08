 package org.fit.cvut.mvi.cgp;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.fit.cvut.mvi.model.Genome;
 import org.fit.cvut.mvi.model.InnerNode;
 import org.fit.cvut.mvi.model.InputNode;
 import org.fit.cvut.mvi.model.Node;
 import org.fit.cvut.mvi.model.functions.Function;
 
 public class GenomeFactory {
 
     private final List<Function> functions;
 
     public GenomeFactory(List<Function> functions) {
         this.functions = functions;
     }
 
     public Genome createRandomGenome(CGPConfiguration config) {
         List<Node> nodes = new ArrayList<>();
 
         // add input nodes to genome
         for (Function function : config.getInputs()) {
             nodes.add(new InputNode(function));
         }
 
         // add random inner nodes to genome
        for (int column = 0; column < config.getColumns(); column++) {
            for (int row = 0; row < config.getRows(); row++) {
                 Function function = randomFunction();
                 List<Integer> connections = randomConnections(config, function.arity(), column);
                 nodes.add(new InnerNode(function, connections));
             }
         }
 
         return new Genome(nodes, randomOutputs(config));
     }
 
     private Function randomFunction() {
         int randIndex = (int) (Math.random() * functions.size());
         return functions.get(randIndex);
     }
 
     private List<Integer> randomConnections(CGPConfiguration c, int arity, int column) {
         List<Integer> nodes = new ArrayList<>();
 
         for (int i = 0; i < arity; i++) {
             int min, max, randIndex;
 
             // TODO NODES CAN ALSO CONNECT TO INPUTS!!!!
             // TODO CHECK RANDOM CASTING TO INT
             if (column >= c.getLevelsBack()) {
                 min = c.getInputs().size() + (column - c.getLevelsBack()) * c.getRows();
             } else {
                 min = 0;
             }
             max = c.getInputs().size() + column * c.getRows();
             randIndex = min + (int) (Math.random() * (max - min));
 
             nodes.add(randIndex);
         }
 
         return nodes;
     }
 
     private List<Integer> randomOutputs(CGPConfiguration config) {
         List<Integer> nodes = new ArrayList<>();
 
         for (int i = 0; i < config.getOutputs(); i++) {
             int randIndex = (int) (Math.random() * (config.getInputs().size() + config.getRows() * config.getColumns()));
             nodes.add(randIndex);
         }
 
         return nodes;
     }
 
 }
