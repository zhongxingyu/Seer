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
 
 package org.seasr.meandre.component.opennlp;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.StringReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.components.abstracts.AbstractExecutableComponent;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextException;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.BasicDataTypesTools;
 import org.seasr.datatypes.BasicDataTypes.Strings;
 import org.seasr.datatypes.BasicDataTypes.StringsArray;
 import org.seasr.meandre.components.tools.Names;
 import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;
 import org.seasr.meandre.support.components.tuples.SimpleTuple;
 import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
 
 
 
 import edu.stanford.nlp.ling.Sentence;
 import edu.stanford.nlp.ling.TaggedWord;
 import edu.stanford.nlp.ling.HasWord;
 import edu.stanford.nlp.tagger.maxent.MaxentTagger;
 
 /**
  * Provides basic pos tagging using the Stanford NLP components.
  *
  * @author Mike Haberman
  */
 
 
 /*  
  * NOTES:   
  * 
 *  there is a problem with the jar files from the stanford NER project
  *  at this time you cannot have BOTH in the classpath:
  *  stanford-postagger.jar and stanford-ner.jar
  *  
  *  the jars contain conflicting classes.
  *  
  *  
  * 
  * Based on stanford-postagger-full-2009-12-24 distribution
  * 
  * 
  */
  /*
   *  NOTES:
   *  this class will unjar the stanfordModels.jar into a directory
   *  the contents of that directory will contain the 3 tagger models
   *  from Stanford's NLP POS facilities:
   *  That jar file was made from the distribution.
   *  
   *  
 	English taggers
 	---------------------------
 	bidirectional-distsim-wsj-0-18.tagger  (SLOW)
 	Trained on WSJ sections 0-18 using a bidirectional architecture and
 	including word shape and distributional similarity features.
 	Penn Treebank tagset.
 	Performance:
 	97.28% correct on WSJ 19-21
 	(90.46% correct on unknown words)
 	
 	left3words-wsj-0-18.tagger            (FASTEST)
 	Trained on WSJ sections 0-18 using the left3words architecture and
 	includes word shape features.  Penn tagset.
 	Performance:
 	96.97% correct on WSJ 19-21
 	(88.85% correct on unknown words)
 	
 	left3words-distsim-wsj-0-18.tagger
 	Trained on WSJ sections 0-18 using the left3words architecture and
 	includes word shape and distributional similarity features. Penn tagset.
 	Performance:
 	97.01% correct on WSJ 19-21
 	(89.81% correct on unknown words)
   *
   */
 
 
 @Component(
 		name = "Stanford Pos Tagger",
 		creator = "Mike Haberman",
 		baseURL = "meandre://seasr.org/components/foundry/",
 		firingPolicy = FiringPolicy.all,
 		mode = Mode.compute,
 		rights = Licenses.UofINCSA,
 		tags = "semantic, tools, text, opennlp, tokenizer, sentences, pos, tagging",
 		description = "This component performs pos tagging using Stanford's NLP facilities",
 		dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar", "stanfordModels.jar", "seasr-commons.jar"}
 )
 public class StanfordPosTagger extends AbstractExecutableComponent {
 
     //------------------------------ PROPERTIES --------------------------------------------------
 
     // Inherited ignoreErrors (PROP_IGNORE_ERRORS) from AbstractExecutableComponent
     // left3words-wsj-0-18.tagger
 	// bidirectional-wsj-0-18.tagger
 	
 	@ComponentProperty(
 			name = "taggerModel",
 			description = "The tagger model to be used ",
 		    defaultValue = "modelsEnglish/left3words-wsj-0-18.tagger"
 		)
 	protected static final String PROP_TAGGER = "taggerModel";
 
 	@ComponentProperty(
 			name = "modelsDir",
 			description = "models directory, if non-empty, skip install",
 		    defaultValue = ""
 		)
 	protected static final String PROP_MODELS_DIR = "modelsDir";
 	
 
 	@ComponentProperty(
 			name = Names.PROP_FILTER_REGEX,
 			description = "optional regular expression to inline filter POS (e.g. JJ|RB)",
 		    defaultValue = ""
 		)
 	protected static final String PROP_POS_FILTER_REGEX = Names.PROP_FILTER_REGEX;
 	
 
 	//--------------------------------------------------------------------------------------------
 
    //------------------------------ INPUTS ------------------------------------------------------
 
 	@ComponentInput(
 			name = Names.PORT_TEXT,
 			description = "The text to be split into sentences"
 	)
 	protected static final String IN_TEXT = Names.PORT_TEXT;
 	
 	
 
 	protected String modelsDir;
 	protected String taggerFile;
     protected String modelJarFile = "stanfordModels.jar";
 	//--------------------------------------------------------------------------------------------
     
     
     //------------------------------ OUTPUTS ------------------------------------------------------
 
 
 	@ComponentOutput(
 			name = Names.PORT_TUPLES,
 			description = "set of tuples: (pos,sentenceId,offset,token)" +
 			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
 	)
 	protected static final String OUT_TUPLES = Names.PORT_TUPLES;
 
 	@ComponentOutput(
 			name = Names.PORT_META_TUPLE,
 			description = "meta data for tuples: (pos,sentenceId,offset,token)" +
 			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
 	)
 	protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;
 	
 	
 	
 
 	SimpleTuplePeer tuplePeer;
 
 	public static final String POS_FIELD         = "pos";
 	public static final String SENTENCE_ID_FIELD = "sentenceId";
 	public static final String TOKEN_START_FIELD = "tokenStart";
 	public static final String TOKEN_FIELD       = "token";
 	
 	int count = 0;
 	int sentenceId = 0;
 	int globalOffset = 0;
 	int startIdx = 0;
 	
 	
 	Pattern pattern = null;
 
     MaxentTagger tagger = null;
 	@Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception 
     {
 		
 		modelsDir = ccp.getProperty(PROP_MODELS_DIR).trim();
 		if (modelsDir.length() == 0)
 		    modelsDir = ccp.getRunDirectory()+File.separator+"stanfordNLP";
 		
 		
 		OpenNLPBaseUtilities.installModelsFromJarFile(modelsDir, modelJarFile, console, getClass());
 		console.fine("Installed models into: " + modelsDir);
 		
 		
 		this.taggerFile = ccp.getProperty(PROP_TAGGER).trim(); // .toLowerCase();
 		tagger = new MaxentTagger(modelsDir + File.separator + taggerFile);
 		
 		
 		sentenceId   = 0;
 		startIdx     = 0;
 		
 		String[] fields =
     		new String[] {POS_FIELD, SENTENCE_ID_FIELD, TOKEN_START_FIELD, TOKEN_FIELD};
 
     	this.tuplePeer = new SimpleTuplePeer(fields);
     	
     	
     	
     	String regex = ccp.getProperty(PROP_POS_FILTER_REGEX).trim();
     	if (regex.length() > 0) {
     		pattern = Pattern.compile(regex);
     		console.info("will filter on " + regex);
     	}
     	else {
     		console.info("No REG EX being used");
     	}
     	
     	
 		
 	}
 	
 
 	
 	@Override
     public void executeCallBack(ComponentContext cc) throws Exception 
     {
 		Strings input = (Strings) cc.getDataComponentFromInput(IN_TEXT);
 		String[] val = BasicDataTypesTools.stringsToStringArray (input);
 		console.info(count++ + " attempt to parse\n" + val[0]);
 		
 		String originalText = val[0];
 		
 		StringReader reader = new StringReader(originalText);
 		List<Sentence<? extends HasWord>> sentences = MaxentTagger.tokenizeText(reader);
 		
 		SimpleTuple tuple   = tuplePeer.createTuple();
 		int POS_IDX         = tuplePeer.getIndexForFieldName(POS_FIELD);
 		int SENTENCE_ID_IDX = tuplePeer.getIndexForFieldName(SENTENCE_ID_FIELD);
 		int TOKEN_START_IDX = tuplePeer.getIndexForFieldName(TOKEN_START_FIELD);
 		int TOKEN_IDX       = tuplePeer.getIndexForFieldName(TOKEN_FIELD);
 		
 		List<Strings> output = new ArrayList<Strings>();
 		for (Sentence<? extends HasWord> sentence : sentences) {
 			
 			//String sText = sentence.toString();
 			//console.info("text is " + sText);
 			
 		      Sentence<TaggedWord> tSentence = MaxentTagger.tagSentence(sentence);
 		      
 		      for (TaggedWord word : tSentence) {
 		    	  
 		    	   String text = word.value();
 		    	   int indexOfLastWord = originalText.indexOf(text, startIdx);
 		    	  
 		    	   tuple.setValue(POS_IDX,         word.tag());
 				   tuple.setValue(SENTENCE_ID_IDX, sentenceId);  // keep this zero based
				   tuple.setValue(TOKEN_START_IDX, indexOfLastWord);
 				   tuple.setValue(TOKEN_IDX,       text);
 
 		    	  
 		    	   // console.info(tuple.toString());
 		    	   
 		    	   startIdx = indexOfLastWord + text.length();
 		    	   
 		    	   if ( pattern == null || pattern.matcher(word.tag()).matches())
 				   {
 		    		   output.add(tuple.convert());
 		    	   }
 		    	  
 		    	   
 		      }
 		      sentenceId++;
 		      //console.info(tSentence.toString(false));
 		}
 		
 		
 		// push the whole collection, protocol safe
 	    Strings[] results = new Strings[output.size()];
 	    output.toArray(results);
 
 	    StringsArray outputSafe = BasicDataTypesTools.javaArrayToStringsArray(results);
 	    cc.pushDataComponentToOutput(OUT_TUPLES, outputSafe);
 
 	    //
 		// metaData for this tuple producer
 		//
 	    Strings metaData = tuplePeer.convert();
 	    cc.pushDataComponentToOutput(OUT_META_TUPLE, metaData);
 
 		
 		
     }
 
 	@Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
 	}
 }
 
