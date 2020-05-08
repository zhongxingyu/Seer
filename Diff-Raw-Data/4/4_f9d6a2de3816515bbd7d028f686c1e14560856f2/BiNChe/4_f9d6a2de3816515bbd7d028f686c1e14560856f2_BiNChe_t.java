 /*
  * Copyright (c) 2012, Stephan Beisken. All rights reserved.
  *
  * This file is part of BiNChe.
  *
  * BiNChe is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * BiNChe is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with BiNChe. If not, see <http://www.gnu.org/licenses/>.
  */
 package net.sourceforge.metware.binche;
 
 import BiNGO.BingoParameters;
 import BiNGO.interfaces.CalculateCorrectionTask;
 import BiNGO.interfaces.CalculateTestTask;
 import BiNGO.methods.BingoAlgorithm;
 import BiNGO.methods.saddlesum.SaddleSumTestCalculate;
 import BiNGO.parser.AnnotationParser;
 import BiNGO.parser.ChEBIAnnotationParser;
 import cytoscape.data.annotation.Ontology;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.*;
 
 public class BiNChe {
 
     private static final Logger LOGGER = Logger.getLogger(BiNChe.class);
     private BingoParameters params;
     private BingoAlgorithm algorithm;
     private final String NONE = BingoAlgorithm.NONE;
     /**
      * We aim to replace these data containers with data inside the nodes
      */
     private Map<Integer, Double> testMap;
     private Map<String, String> correctionMap = null;
     /*
      private Map<Integer, String> mapSmallX = null;
      private Map<Integer, String> mapSmallN = null;
      private Map<Integer, String> mapBigX = null;
      private Map<Integer, String> mapBigN = null;
      */
     private Map<Integer, Double> pValueMap; // how is this not redundant to one of the previous ones?
     /**
      * to be replaced by:
      */
     private List<BiNChENode> enrichmentNodes;
     private Map<String, Set<String>> classifiedEntities = null;
 
     public void execute() {
 
         LOGGER.log(Level.INFO, "Parsing annotation ...");
 
         AnnotationParser annParser = new ChEBIAnnotationParser(params, new HashSet<String>());
         annParser.run();
 
         if (annParser.getStatus()) {
             params.setAnnotation(annParser.getAnnotation());
             params.setOntology(annParser.getOntology());
             params.setAlias(annParser.getAlias());
             this.setAllNodes(); // this sets all nodes in the params.
 
             if (annParser.getOrphans()) {
                 System.err.println("WARNING : Some category labels in the annotation file" + "\n"
                         + "are not defined in the ontology. Please check the compatibility of" + "\n"
                         + "these files. For now, these labels will be ignored and calculations" + "\n"
                         + "will proceed. " + annParser.getSynHashSize());
             }
             //only way to set status true is to pass annotation parse step
             params.setStatus(true);
         } else {
             params.setStatus(false);
         }
 
         algorithm = new BingoAlgorithm(params);
         LOGGER.log(Level.INFO, "Calculating distribution ...");
         CalculateTestTask test = algorithm.calculate_distribution();
         test.run();
         testMap = test.getTestMap();
         System.out.println("Got result size before correction: " + testMap.size());
 
         LOGGER.log(Level.INFO, "Calculating corrections ...");
         CalculateCorrectionTask correction = algorithm.calculate_corrections(testMap);
 
         enrichmentNodes = new ArrayList<BiNChENode>();
         if ((correction != null) && (!params.getTest().equals(NONE))) {
             correction.run();
             correctionMap = correction.getCorrectionMap();
             pValueMap = new HashMap<Integer, Double>();
             for (String id : correctionMap.keySet()) {
                 Integer idAsInteger = Integer.valueOf(id);
                 Double corrAsDouble = Double.valueOf(correctionMap.get(id));
                 pValueMap.put(idAsInteger, corrAsDouble);
                 enrichmentNodes.add(
                         new BiNChENode(testMap.get(idAsInteger), corrAsDouble,
                         test.getMapBigX().get(idAsInteger),
                         test.getMapBigN().get(idAsInteger),
                         test.getMapSmallX().get(idAsInteger),
                         test.getMapSmallN().get(idAsInteger), id));
             }
         } else if (correction == null) {
             pValueMap = testMap;
             for (Integer idAsInteger : testMap.keySet()) {
                 String id = idAsInteger + "";
                //Double corrAsDouble = Double.valueOf(correctionMap.get(id));
                //pValueMap.put(idAsInteger, corrAsDouble);
                 enrichmentNodes.add(
                         new BiNChENode(testMap.get(idAsInteger), null,
                         test.getMapBigX().get(idAsInteger),
                         test.getMapBigN().get(idAsInteger),
                         test.getMapSmallX().get(idAsInteger),
                         test.getMapSmallN().get(idAsInteger), id));
             }
         }
         System.out.println("Got result size after correction/weighted test: " + enrichmentNodes.size());
 
         // these hashMaps contain the results, where the Keys are the different categories (ie. a ChEBI entry or a
         // GeneOntology element). These results are after the test we then need to retrieve the corrections from the
         // correction object.
                 /*
          mapSmallX = test.getMapSmallX();
          mapSmallN = test.getMapSmallN();
          mapBigX = test.getMapBigX();
          mapBigN = test.getMapBigN();
          */
 
         LOGGER.log(Level.INFO, "Computing elements ...");
         this.computeElementsPerCategory();
 
     }
 
     /* **********************************
      START - custom methods
      ************************************* */
     /**
      * Provides a map with the corrected p-values, or the p-values if no correction was applied.
      *
      * @return
      * @deprecated use instead {@link #getEnrichedNodes() } and then obtain the corrected p-value from each element in
      * the resulting list of {@link BiNChENode}.
      */
     @Deprecated
     public Map<Integer, Double> getPValueMap() {
 
         return pValueMap;
     }
 
     public Map<String, Set<String>> getClassifiedEntities() {
 
         return classifiedEntities;
     }
 
     public Ontology getOntology() {
 
         return params.getOntology();
     }
 
     public Set<String> getInputNodes() {
 
         return params.getSelectedNodes();
     }
 
     /**
      * Get the enriched nodes, where each node can provide its p-value, corrected p-value, fold, and % of sample.
      *
      * @return list of BiNChENode objects
      */
     public List<BiNChENode> getEnrichedNodes() {
         return this.enrichmentNodes;
     }
 
     /* **********************************
      END - custom methods
      ************************************* */
     
     /**
      * Provides the original p-value for a particular node/category identifier.
      *
      * @return
      * @deprecated use instead {@link #getEnrichedNodes() } and then obtain the p-value for the element in
      * the resulting list of {@link BiNChENode}.
      */
     @Deprecated
     public Double getPValueForCategory(Integer categoryID) {
 
         return testMap.get(categoryID);
     }
 
     public Set<String> getElementsInCategory(Integer categoryID) {
 
         return this.classifiedEntities.get(categoryID + "");
     }
 
     @Deprecated
     public Double getCorrectedPValueForCategory(Integer categoryID) {
 
         return Double.parseDouble(correctionMap.get(categoryID + ""));
     }
 
     @Deprecated
     public Set<Integer> getCategories() {
 
         return testMap.keySet();
     }
 
     private void computeElementsPerCategory() {
 
         this.classifiedEntities = new HashMap<String, Set<String>>();
         Iterator it2 = params.getSelectedNodes().iterator();
         while (it2.hasNext()) {
             String name = it2.next() + "";
             Set tmp = params.getAlias().get(name);
             if (tmp != null) {
                 Iterator it = tmp.iterator();
                 while (it.hasNext()) {
                     // this is the limiting step to move identifiers to string or a more general object.
                     int[] nodeClassifications = params.getAnnotation().getClassifications(it.next() + "");
                     for (int k = 0; k < nodeClassifications.length; k++) {
                         String cat = new Integer(nodeClassifications[k]).toString();
                         if (!classifiedEntities.containsKey(cat)) {
                             HashSet catset = new HashSet();
                             classifiedEntities.put(cat, catset);
                         }
                         ((HashSet) classifiedEntities.get(cat)).add(name);
                     }
                 }
             }
         }
     }
 
     public void loadDesiredElementsForEnrichmentFromFile(String fileName) throws IOException {
 
         BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(fileName)));
 
         String container = new String();
         HashSet<String> inputNodes = new HashSet<String>();
         HashMap<String, Double> inputWeights = new HashMap<String, Double>();
 
         String line;
         while ((line = br.readLine()) != null) {
             container += line;
 
             String content[] = new String[line.split("\\s+").length];
             content = line.split("\\s+");
 
             inputNodes.add(content[0]);
             if (content.length == 2) {
                 String name[] = new String[content[0].split(":").length];
                 name = content[0].split(":");
 
                 inputWeights.put(content[0], Double.parseDouble(content[1]));
             }
         }
         this.params.setTextInput(container);
         this.params.setSelectedNodes(inputNodes);
 
         this.params.setWeights(inputWeights);
     }
 
     /**
      * Alternative method to be used by the web-app, because the chebi ids (and weights, if present) input by the user
      * will be passed in the form of a HashMap
      */
     public void loadDesiredElementsForEnrichmentFromInput(Map<String, String> input) throws IOException {
 
         String container = new String();
         Set<String> inputNodes = new HashSet<String>();
         Map<String, Double> inputWeights = new HashMap<String, Double>();
 
         for (String chebiId : input.keySet()) {
             container += chebiId.concat(input.get(chebiId));
 
             inputNodes.add(chebiId);
             String name[] = new String[chebiId.split(":").length];
             name = chebiId.split(":");
 
             Double weight = Double.valueOf(input.get(chebiId));
 
             inputWeights.put(chebiId, weight);
             //System.out.println("Got weight " + weight + " for chebiId " + chebiId);
         }
         this.params.setTextInput(container);
         this.params.setSelectedNodes(inputNodes);
 
         this.params.setWeights(inputWeights);
     }
 
     public void setAllNodes() {
 
         String[] nodes = params.getAnnotation().getNames();
         // HashSet for storing the canonical names
         HashSet canonicalNameVector = new HashSet();
         for (int i = 0; i < nodes.length; i++) {
             if (nodes[i] != null && (nodes[i].length() != 0)) {
                 canonicalNameVector.add(nodes[i].toUpperCase());
             }
         }
 
         params.setAllNodes(canonicalNameVector);
     }
 
     public void setParameters(BingoParameters parameters) {
 
         this.params = parameters;
     }
 }
