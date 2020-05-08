 /**
  * University of Illinois/NCSA
  * Open Source License
  *
  * Copyright (c) 2008, Board of Trustees-University of Illinois.
  * All rights reserved.
  *
  * Developed by:
  *
  * Automated Learning Group
  * National Center for Supercomputing Applications
  * http://www.seasr.org
  *
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to
  * deal with the Software without restriction, including without limitation the
  * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
  * sell copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  *  * Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimers.
  *
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimers in the
  *    documentation and/or other materials provided with the distribution.
  *
  *  * Neither the names of Automated Learning Group, The National Center for
  *    Supercomputing Applications, or University of Illinois, nor the names of
  *    its contributors may be used to endorse or promote products derived from
  *    this Software without specific prior written permission.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
  * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
  * WITH THE SOFTWARE.
  */
 
 package org.seasr.meandre.components.analytics.mallet;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.TreeSet;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.core.system.components.ext.StreamDelimiter;
 import org.meandre.core.system.components.ext.StreamInitiator;
 import org.meandre.core.system.components.ext.StreamTerminator;
 import org.seasr.datatypes.core.BasicDataTypes.Strings;
 import org.seasr.datatypes.core.BasicDataTypesTools;
 import org.seasr.datatypes.core.Names;
 import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;
 import cc.mallet.topics.ParallelTopicModel;
 import cc.mallet.types.Alphabet;
 import cc.mallet.types.IDSorter;
 
 /**
  * @author Ian Wood
  */
 
 @Component(
         name = "Topic Model Topics To Doubles",
         creator = "Ian Wood",
         baseURL = "meandre://seasr.org/components/foundry/",
         firingPolicy = FiringPolicy.all,
         mode = Mode.compute,
         rights = Licenses.UofINCSA,
         tags = "#TRANSFORM, mallet, topic model, xml",
         description = "This component outputs inferred topic-word distributions for a given topic index from a topic model." ,
         dependency = {"protobuf-java-2.2.0.jar"}
 )
 public class TopicModelToDoublesMaps extends AbstractStreamingExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
             name = "topic_model",
             description = "The topic model" +
                 "<br>TYPE: cc.mallet.topics.ParallelTopicModel"
     )
     protected static final String IN_TOPIC_MODEL = "topic_model";
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             name = "topic_word_distributions",
             description = "A stream containing the topic-word distributions" +
                 "<br>TYPE: org.seasr.datatypes.core.BasicDataTypes.DoublesMap"
     )
     protected static final String OUT_TOPIC_DISTRIBUTIONS = "topic_word_distributions";
 
     @ComponentOutput(
             name = "topic_id",
             description = "A stream containing the topic id's" +
                 "<br>TYPE: org.seasr.datatypes.core.BasicDataTypes.Strings"
     )
     protected static final String OUT_TOPIC_ID = "topic_id";
 
     @ComponentOutput(
             name = "topic_size",
             description = "A stream containing the total token count for each topic" +
                "<br>TYPE: org.seasr.datatypes.core.BasicDataTypes.Strings"
     )
     protected static final String OUT_TOPIC_SIZE = "topic_size";
 
     @ComponentOutput(
             name = "topic_model",
             description = "The topic model (same as input)" +
                 "<br>TYPE: cc.mallet.topics.ParallelTopicModel"
     )
     protected static final String OUT_TOPIC_MODEL = "topic_model";
 
     //----------------------------- PROPERTIES ---------------------------------------------------
 
     @ComponentProperty(
             name = Names.PROP_WRAP_STREAM,
             description = "Should the output be wrapped as a stream?",
             defaultValue = "true"
     )
     protected static final String PROP_WRAP_STREAM = Names.PROP_WRAP_STREAM;
 
     //--------------------------------------------------------------------------------------------
 
 
     protected boolean _wrapStream;
 
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
 	    super.initializeCallBack(ccp);
 
         _wrapStream = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_WRAP_STREAM, ccp));
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception {
         ParallelTopicModel topicModel = (ParallelTopicModel) cc.getDataComponentFromInput(IN_TOPIC_MODEL);
 
 		if (_wrapStream) {
 		    StreamDelimiter sd = new StreamInitiator(streamId);
 		    cc.pushDataComponentToOutput(OUT_TOPIC_MODEL, sd);
             cc.pushDataComponentToOutput(OUT_TOPIC_ID, sd);
             cc.pushDataComponentToOutput(OUT_TOPIC_SIZE, sd);
 		    cc.pushDataComponentToOutput(OUT_TOPIC_DISTRIBUTIONS, sd);
 		}
 
         int numTopics = topicModel.getNumTopics();
         Alphabet alphabet = topicModel.getAlphabet();
 
         Map<String,Double> topicWordDistribution = new HashMap<String,Double>();
 
         ArrayList<TreeSet<IDSorter>> topicSortedWords = topicModel.getSortedWords();
         for (int topic = 0; topic < numTopics; topic++) {
             TreeSet<IDSorter> sortedWords = topicSortedWords.get(topic);
             Iterator<IDSorter> iterator = sortedWords.iterator();
             Double topicTotalCount = 0d;
             
             topicWordDistribution.clear();
             while (iterator.hasNext()) {
                 IDSorter info = iterator.next();
                 topicTotalCount += info.getWeight();
                 Double oldValue = topicWordDistribution.put(alphabet.lookupObject(info.getID()).toString(),(double)info.getWeight());
                 if (oldValue != null) console.warning(String.format("Duplicate word %s with weights %d and %d in topic model!?",alphabet.lookupObject(info.getID()).toString(),oldValue,info.getWeight()));
             }
             
             if (topicTotalCount != topicModel.tokensPerTopic[topic]) 
             	System.out.println(String.format("TopicModelToDoublesMaps: topic %d has count %d but %d mallet tokens!!",topic,topicTotalCount,topicModel.tokensPerTopic[topic]));
            
             for (String word : topicWordDistribution.keySet())
             	topicWordDistribution.put(word,topicWordDistribution.get(word)/topicTotalCount);
             
 //            System.out.println(String.format("TopicModelToDoublesMaps: for topic %d found %d words",topic,topicWordDistribution.size()));
             console.fine(String.format("for topic %d found %d words",topic,topicWordDistribution.size()));
             
             cc.pushDataComponentToOutput(OUT_TOPIC_ID, intToStrings(topic));
             cc.pushDataComponentToOutput(OUT_TOPIC_SIZE, BasicDataTypesTools.doubleToDoubles(topicTotalCount));
             cc.pushDataComponentToOutput(OUT_TOPIC_DISTRIBUTIONS, BasicDataTypesTools.mapToDoubleMap(topicWordDistribution, false));
         }
         
         cc.pushDataComponentToOutput(OUT_TOPIC_MODEL, topicModel);
 
 		if (_wrapStream) {
 		    StreamDelimiter sd = new StreamTerminator(streamId);
 		    cc.pushDataComponentToOutput(OUT_TOPIC_MODEL, sd);
             cc.pushDataComponentToOutput(OUT_TOPIC_ID, sd);
             cc.pushDataComponentToOutput(OUT_TOPIC_SIZE, sd);
 		    cc.pushDataComponentToOutput(OUT_TOPIC_DISTRIBUTIONS, sd);
 		}
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 
 	// --------------------------------------------------------------------------------------------
     
 	@Override
 	public boolean isAccumulator() {
 	    return false;
 	}    
 	
 	public Strings intToStrings(int value) {
         Strings.Builder s = Strings.newBuilder();
         s.addValue(Integer.toString(value));
 
         return s.build();
     }
 
 }
