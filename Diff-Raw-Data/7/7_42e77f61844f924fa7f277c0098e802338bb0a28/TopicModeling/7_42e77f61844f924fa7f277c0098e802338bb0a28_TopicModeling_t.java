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
 
 package org.seasr.meandre.components.analytics.mallet;
 
 import java.util.Arrays;
 import java.util.Iterator;
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
 import org.meandre.core.ComponentExecutionException;
 import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
 import org.seasr.meandre.support.generic.io.DOMUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import cc.mallet.topics.ParallelTopicModel;
 import cc.mallet.topics.TopicAssignment;
 import cc.mallet.types.Alphabet;
 import cc.mallet.types.FeatureSequence;
 import cc.mallet.types.IDSorter;
 import cc.mallet.types.InstanceList;
 
 /**
  * @author Boris Capitanu
  */
 
 @Component(
         name = "Mallet Topic Modeling",
         creator = "Boris Capitanu",
         baseURL = "meandre://seasr.org/components/foundry/",
         firingPolicy = FiringPolicy.all,
         mode = Mode.compute,
         rights = Licenses.UofINCSA,
         tags = "mallet, topic model",
         description = "This component perform topic analysis in the style of LDA and its variants using Mallet" ,
         dependency = {"protobuf-java-2.2.0.jar"}
 )
 public class TopicModeling extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
             name = "mallet_instance_list",
             description = "The list of machine learning instances" +
                 "<br>TYPE: cc.mallet.types.InstanceList"
     )
     protected static final String IN_INSTANCE_LIST = "mallet_instance_list";
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             name = "doc_topics_xml",
             description = "An XML document containing the processed documents, and for each processed document the set of topics and topic probabilities" +
                 "<br>TYPE: org.w3c.dom.Document"
     )
     protected static final String OUT_DOC_TOPICS_XML = "doc_topics_xml";
 
     @ComponentOutput(
             name = "topic_top_words_xml",
             description = "An XML document containing the topics, and for each topic the set of top words (with weights)" +
                 "<br>TYPE: org.w3c.dom.Document"
     )
     protected static final String OUT_TOPIC_TOP_WORDS_XML = "topic_top_words_xml";
 
     //----------------------------- PROPERTIES ---------------------------------------------------
 
     @ComponentProperty(
             name = "num_topics",
             description = "The number of topics to fit",
             defaultValue = "10"
     )
     protected static final String PROP_NUM_TOPICS = "num_topics";
 
     @ComponentProperty(
             name = "alpha",
             description = "Alpha parameter: smoothing over topic distribution",
             defaultValue = "50.0"
     )
     protected static final String PROP_ALPHA = "alpha";
 
     @ComponentProperty(
             name = "beta",
             description = "Beta parameter: smoothing over unigram distribution",
             defaultValue = "0.01"
     )
     protected static final String PROP_BETA = "beta";
 
     @ComponentProperty(
             name = "num_iterations",
             description = "The number of iterations of Gibbs sampling",
             defaultValue = "1000"
     )
     protected static final String PROP_NUM_ITERATIONS = "num_iterations";
 
     @ComponentProperty(
             name = "random_seed",
             description = "The random seed for the Gibbs sampler.  Default is 0, which will use the clock.",
             defaultValue = "0"
     )
     protected static final String PROP_RANDOM_SEED = "random_seed";
 
     @ComponentProperty(
             name = "num_top_words",
             description = "The number of most probable words to return for each topic after model estimation; use -1 to return all of them",
             defaultValue = "20"
     )
     protected static final String PROP_NUM_TOP_WORDS = "num_top_words";
 
     @ComponentProperty(
             name = "optimize_interval",
             description = "The number of iterations between re-estimating dirichlet hyperparameters",
             defaultValue = "0"
     )
     protected static final String PROP_OPTIMIZE_INTERVAL = "optimize_interval";
 
     @ComponentProperty(
             name = "optimize_burnin",
             description = "The number of iterations to run before first estimating dirichlet hyperparameters",
             defaultValue = "200"
     )
     protected static final String PROP_OPTIMIZE_BURNIN = "optimize_burnin";
 
     @ComponentProperty(
             name = "use_symmetric_alpha",
             description = "Only optimize the concentration parameter of the prior over document-topic distributions. " +
             		"This may reduce the number of very small, poorly estimated topics, but may disperse common words over several topics.",
             defaultValue = "false"
     )
     protected static final String PROP_USE_SYMMETRIC_ALPHA = "use_symmetric_alpha";
 
     @ComponentProperty(
             name = "num_threads",
             description = "The number of threads for parallel training",
             defaultValue = "1"
     )
     protected static final String PROP_NUM_THREADS = "num_threads";
 
     //--------------------------------------------------------------------------------------------
 
 
     protected int _numTopics;
     protected int _numThreads;
     protected int _numIterations;
     protected int _randomSeed;
     protected int _numTopWords;
     protected int _showTopicsInterval;
     protected int _optimizeInterval;
     protected int _optimizeBurnIn;
     protected boolean _useSymmetricAlpha;
     protected double _alpha;
     protected double _beta;
 
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
         _numTopics          = Integer.parseInt(getPropertyOrDieTrying(PROP_NUM_TOPICS, ccp));
         _numThreads         = Integer.parseInt(getPropertyOrDieTrying(PROP_NUM_THREADS, ccp));
         _numIterations      = Integer.parseInt(getPropertyOrDieTrying(PROP_NUM_ITERATIONS, ccp));
         _randomSeed         = Integer.parseInt(getPropertyOrDieTrying(PROP_RANDOM_SEED, ccp));
         _numTopWords        = Integer.parseInt(getPropertyOrDieTrying(PROP_NUM_TOP_WORDS, ccp));
         _optimizeInterval   = Integer.parseInt(getPropertyOrDieTrying(PROP_OPTIMIZE_INTERVAL, ccp));
         _optimizeBurnIn     = Integer.parseInt(getPropertyOrDieTrying(PROP_OPTIMIZE_BURNIN, ccp));
         _useSymmetricAlpha  = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_USE_SYMMETRIC_ALPHA, ccp));
         _alpha              = Double.parseDouble(getPropertyOrDieTrying(PROP_ALPHA, ccp));
         _beta               = Double.parseDouble(getPropertyOrDieTrying(PROP_BETA, ccp));
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception {
         InstanceList training = (InstanceList) cc.getDataComponentFromInput(IN_INSTANCE_LIST);
 
         if (training.size() > 0 && training.get(0) != null) {
             Object data = training.get(0).getData();
             if (! (data instanceof FeatureSequence))
                 throw new ComponentExecutionException("Topic modeling currently only supports feature sequences!");
         } else {
             console.warning("Empty instance list received - nothing to do!");
             return;
         }
 
         ParallelTopicModel topicModel = new ParallelTopicModel (_numTopics, _alpha, _beta);
         if (_randomSeed != 0)
             topicModel.setRandomSeed(_randomSeed);
 
         topicModel.addInstances(training);
 
         topicModel.setTopicDisplay(0, _numTopWords);
         topicModel.setNumIterations(_numIterations);
         topicModel.setOptimizeInterval(_optimizeInterval);
         topicModel.setBurninPeriod(_optimizeBurnIn);
         topicModel.setSymmetricAlpha(_useSymmetricAlpha);
         topicModel.setNumThreads(_numThreads);
 
         topicModel.estimate();
 
         int numTopics = topicModel.getNumTopics();
 
         IDSorter[] sortedTopics = new IDSorter[numTopics];
         for (int topic = 0; topic < numTopics; topic++)
             // Initialize the sorters with dummy values
             sortedTopics[topic] = new IDSorter(topic, topic);
 
         Document topicsDoc = DOMUtils.createNewDocument();
         Element xmlModel = topicsDoc.createElement("model");
         xmlModel.setAttribute("numTopics", Integer.toString(numTopics));
         topicsDoc.appendChild(xmlModel);
 
         int[] topicCounts = new int[numTopics];
         int docNum = 0;
         for (TopicAssignment ta : topicModel.getData()) {
             int[] features = ta.topicSequence.getFeatures();
 
             // Count up the tokens
             for (int i = 0, iMax = features.length; i < iMax; i++)
                 topicCounts[features[i]]++;
 
             // And normalize
             for (int topic = 0; topic < numTopics; topic++)
                 sortedTopics[topic].set(topic, (float) topicCounts[topic] / features.length);
 
            Arrays.fill(topicCounts, 0); // initialize for next round
             Arrays.sort(sortedTopics);
 
             Element xmlTopics = topicsDoc.createElement("topics");
             for (int i = 0, iMax = sortedTopics.length; i < iMax; i++) {
                 Element xmlTopic = topicsDoc.createElement("topic");
                 xmlTopic.setAttribute("id", Integer.toString(sortedTopics[i].getID()));
                xmlTopic.setAttribute("weight", String.format("%.4f", sortedTopics[i].getWeight()));
                 xmlTopics.appendChild(xmlTopic);
             }
 
             Element xmlDoc = topicsDoc.createElement("document");
             xmlDoc.setAttribute("id", Integer.toString(docNum++));
             xmlDoc.setAttribute("name", ta.instance.getName().toString());
 
             Element xmlDocSource = topicsDoc.createElement("source");
             xmlDocSource.appendChild(topicsDoc.createTextNode(ta.instance.getSource().toString()));
             xmlDoc.appendChild(xmlDocSource);
             xmlDoc.appendChild(xmlTopics);
 
             xmlModel.appendChild(xmlDoc);
         }
 
         Alphabet alphabet = topicModel.getAlphabet();
 
         Document topWordsDoc = DOMUtils.createNewDocument();
         Element xmlTopicTopWords = topWordsDoc.createElement("topicTopWords");
         topWordsDoc.appendChild(xmlTopicTopWords);
 
         @SuppressWarnings("rawtypes")
         TreeSet[] topicSortedWords = topicModel.getSortedWords();
         for (int topic = 0; topic < numTopics; topic++) {
             @SuppressWarnings("unchecked")
             TreeSet<IDSorter> sortedWords = topicSortedWords[topic];
             Iterator<IDSorter> iterator = sortedWords.iterator();
 
             Element xmlTopic = topWordsDoc.createElement("topic");
             xmlTopic.setAttribute("id", Integer.toString(topic));
 
             int word = 1;
             while (iterator.hasNext() && (_numTopWords == -1 || word++ < _numTopWords)) {
                 IDSorter info = iterator.next();
 
                 Element xmlWord = topWordsDoc.createElement("word");
                xmlWord.setAttribute("weight", String.format("%s", (int)info.getWeight()));
                 xmlWord.appendChild(topWordsDoc.createTextNode(alphabet.lookupObject(info.getID()).toString()));
 
                 xmlTopic.appendChild(xmlWord);
             }
 
             xmlTopicTopWords.appendChild(xmlTopic);
         }
 
         cc.pushDataComponentToOutput(OUT_DOC_TOPICS_XML, topicsDoc);
         cc.pushDataComponentToOutput(OUT_TOPIC_TOP_WORDS_XML, topWordsDoc);
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 }
