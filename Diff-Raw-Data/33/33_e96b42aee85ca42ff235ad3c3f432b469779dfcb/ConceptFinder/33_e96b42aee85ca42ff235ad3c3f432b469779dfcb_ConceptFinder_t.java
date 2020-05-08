 /**
  *
  * University of Illinois/NCSA
  * Open Source License
  *
  * Copyright (c) 2008, NCSA.  All rights reserved.
  *
  * Developed by:
  * The Automated Learning Group
  * University of Illinois at Urbana-Champaign
  * http://www.seasr.org
  *
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal with the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject
  * to the following conditions:
  *
  * Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimers.
  *
  * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimers in
  * the documentation and/or other materials provided with the distribution.
  *
  * Neither the names of The Automated Learning Group, University of
  * Illinois at Urbana-Champaign, nor the names of its contributors may
  * be used to endorse or promote products derived from this Software
  * without specific prior written permission.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
  * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
  * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
  *
  */
 
 package org.seasr.meandre.components.sentiment;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.core.ComponentExecutionException;
 import org.seasr.datatypes.core.BasicDataTypes.Strings;
 import org.seasr.datatypes.core.Names;
 import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
 import org.seasr.meandre.support.components.apps.sentiment.PathMetric;
 import org.seasr.meandre.support.components.apps.sentiment.PathMetricFinder;
 import org.seasr.meandre.support.components.tuples.SimpleTuple;
 import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
 
 /**
  * @author Boris Capitanu
  */
 
 @Component(
         name = "Concept Finder",
         creator = "Boris Capitanu",
         baseURL = "meandre://seasr.org/components/foundry/",
         firingPolicy = FiringPolicy.all,
         mode = Mode.compute,
         rights = Licenses.UofINCSA,
         tags = "sentiment, concept",
         description = "This component connects to the SynNet service and computes the best concept " +
         		      "for the input token based on the defined concept rules" ,
         dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar"}
 )
 public class ConceptFinder extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
             name = Names.PORT_TUPLE,
             description = "The tuple" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String IN_TUPLE = Names.PORT_TUPLE;
 
     @ComponentInput(
             name = Names.PORT_META_TUPLE,
             description = "The meta data for the tuple(s)" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             name = Names.PORT_TUPLE,
             description = "The tuple augmented with the concept information" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_TUPLE = Names.PORT_TUPLE;
 
     @ComponentOutput(
             name = Names.PORT_META_TUPLE,
             description = "The meta data for the augmented tuple (same as input, plus concept, seed, pathLength)" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;
 
     @ComponentOutput(
             name = "no_concept_tuple",
             description = "The original tuple, if no concept could be computed" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_NO_CONCEPT = "no_concept_tuple";
 
     @ComponentOutput(
             name = "no_concept_meta",
             description = "The meta data for the no_concept_tuple" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_NO_CONCEPT_META = "no_concept_meta";
 
     //----------------------------- PROPERTIES ---------------------------------------------------
 
     @ComponentProperty(
             description = "SynNet service URL (include http://)",
             name = "synnet_host",
             defaultValue = "http://services.seasr.org/synnet/"
     )
     protected static final String PROP_SYNNET_HOST = "synnet_host";
 
     @ComponentProperty(
             description = "The concept rules (Example: <i>love={lovable};anger={hateful,angry}</i>)",
             name = "concepts",
             defaultValue = ""
     )
     protected static final String PROP_CONCEPTS = "concepts";
 
     @ComponentProperty(
             description = "The attribute holding the word whose concept should be computed",
             name = "key",
             defaultValue = "token"
     )
     protected static final String PROP_KEY = "key";
 
     //--------------------------------------------------------------------------------------------
 
 
     protected static final String CONCEPT = "concept";
     protected static final String SEED = "seed";
     protected static final String PATH_LENGTH = "pathLength";
 
     protected String _key;
     protected PathMetricFinder _pathMetricFinder;
     protected Map<String, String> _conceptSeedMap;
 
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
         _key = getPropertyOrDieTrying(PROP_KEY, ccp);
 
         String concepts = getPropertyOrDieTrying(PROP_CONCEPTS, ccp);
         _conceptSeedMap = parseConcepts(concepts);
 
         console.fine(String.format("Using seeds: %s", _conceptSeedMap.keySet()));
 
         String synnetHost = getPropertyOrDieTrying(PROP_SYNNET_HOST, ccp);
         _pathMetricFinder = new PathMetricFinder(synnetHost);
        _pathMetricFinder.setLogger(console);
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception {
         Strings inTuple = (Strings) cc.getDataComponentFromInput(IN_TUPLE);
         Strings inMetaTuple = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
 
         console.finest(String.format("input: meta='%s' tuple='%s'", inMetaTuple.toString(), inTuple.toString()));
 
         SimpleTuplePeer inPeer  = new SimpleTuplePeer(inMetaTuple);
         SimpleTuplePeer outPeer = new SimpleTuplePeer(inPeer, new String[] { CONCEPT, SEED, PATH_LENGTH });
 
         int KEY_IDX = inPeer.getIndexForFieldName(_key);
         if (KEY_IDX == -1)
             throw new ComponentExecutionException(String.format("Incoming tuple has no attributed named '%s'", _key));
 
         SimpleTuple tuple    = inPeer.createTuple();
         SimpleTuple outTuple = outPeer.createTuple();
 
         tuple.setValues(inTuple);
         outTuple.setValue(tuple);
 
         String token = tuple.getValue(KEY_IDX);
         Set<String> seeds = _conceptSeedMap.keySet();
 
         console.fine(String.format("Attempting to find concept information for '%s'", token));
         List<PathMetric> allPathMetrics = _pathMetricFinder.getAllMetric(token, new ArrayList<String>(seeds));
         if (allPathMetrics == null)
             throw new ComponentExecutionException(String.format("SynNet service down (token='%s' seeds='%s')", token, seeds));
 
         PathMetric pathMetric = _pathMetricFinder.getBestMetric(allPathMetrics);
         if (pathMetric == null) {
             // No concept found
             console.fine("No concept found for: " + token);
             outTuple.setValue(CONCEPT, "");
             outTuple.setValue(SEED, "");
             outTuple.setValue(PATH_LENGTH, "");
 
             cc.pushDataComponentToOutput(OUT_NO_CONCEPT, outTuple.convert());
             cc.pushDataComponentToOutput(OUT_NO_CONCEPT_META, outPeer.convert());
             return;
         }
 
         String concept = _conceptSeedMap.get(pathMetric.end);
         console.fine(String.format("Token '%s' maps to seed '%s' which corresponds to concept '%s' (pathLength: %d)",
                 token, pathMetric.end, concept, pathMetric.depthFound));
 
         outTuple.setValue(CONCEPT, concept);
         outTuple.setValue(SEED, pathMetric.end);
         outTuple.setValue(PATH_LENGTH, Integer.toString(pathMetric.depthFound));
 
         cc.pushDataComponentToOutput(OUT_TUPLE, outTuple.convert());
         cc.pushDataComponentToOutput(OUT_META_TUPLE, outPeer.convert());
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
         _conceptSeedMap = null;
         _pathMetricFinder = null;
     }
 
     //--------------------------------------------------------------------------------------------
 
     private Map<String, String> parseConcepts(String concepts) {
         Map<String, String> conceptSeedMap = new HashMap<String, String>();
 
         StringTokenizer tokenizer = new StringTokenizer(concepts, ";");
         while (tokenizer.hasMoreTokens()) {
             String conceptSeed = tokenizer.nextToken();
             String[] parts = conceptSeed.split("=");
             String concept = parts[0].trim();
             String[] seeds = parts[1].replaceAll("[}{]", "").split(",");
             for (String seed : seeds)
                 conceptSeedMap.put(seed.trim(), concept);
         }
 
         return conceptSeedMap;
     }
 }
