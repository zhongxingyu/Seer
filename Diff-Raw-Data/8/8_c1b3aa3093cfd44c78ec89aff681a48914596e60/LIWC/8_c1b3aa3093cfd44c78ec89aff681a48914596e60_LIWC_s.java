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
 
 package org.seasr.meandre.components.analytics.psychometrics;
 
 import java.util.Hashtable;
 import java.util.Map;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.core.BasicDataTypes.DoublesMap;
 import org.seasr.datatypes.core.BasicDataTypesTools;
 import org.seasr.datatypes.core.DataTypeParser;
 import org.seasr.datatypes.core.Names;
 import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
 
 import drevicko.psycholingua.WordClassDictionary;
 import drevicko.psycholingua.WordCounter.WordClassCount;
 import drevicko.psycholingua.WordCounter.WordClassFloatCount;
 import edu.stanford.nlp.util.StringUtils;
 
 /**
  * This component calculates LIWC scores from token distributions.
  *
  * @author Ian Wood;
  *
  */
 
 @Component(
 		name = "LIWC on Tokens",
 		creator = "Ian Wood",
 		baseURL = "meandre://seasr.org/components/foundry/",
 		firingPolicy = FiringPolicy.all,
 		mode = Mode.compute,
 		rights = Licenses.UofINCSA,
 		tags = "token count, text, convert, LIWC, #ANALYTICS",
 		description = "Given a collection of word frequecies (as doubles), this component calculates " +
 				      "LIWC scores for the stream.",
 		dependency = {"protobuf-java-2.2.0.jar,drevicko-LIWC.jar"}
 )
 public class LIWC extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
 	@ComponentInput(
 			name = Names.PORT_TOKENS,
 			description = "The tokens on which to do LIWC analysis" +
     			 "<br>TYPE: java.lang.String" +
                  "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                  "<br>TYPE: byte[]" +
                  "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                  "<br>TYPE: java.lang.Object"
 	)
 	protected static final String IN_TOKENS = Names.PORT_TOKENS;
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
 	@ComponentOutput(
 	        name = "LIWC_scores",
 	        description = "The calculated LIWC counts." +
                 "<br>TYPE: org.seasr.datatypes.core.BasicDataTypes.IntegersMap"
 	)
     protected static final String OUT_LIWC_SCORES = "LIWC_scores";
 
 	@ComponentOutput(
 	        name = "word_count",
	        description = "The calculated LIWC counts." +
                 "<br>TYPE: org.seasr.datatypes.core.BasicDataTypes.Integers"
 	)
     protected static final String OUT_WORD_COUNT = "word_count";
 
     //------------------------------ PROPERTIES --------------------------------------------------
 
 	@ComponentProperty(
 			name = Names.PROP_FILENAME,
 			description = "The location of the LIWC dictionary file.",
 		    defaultValue = ""
 	)
 	protected static final String PROP_LIWC_DICT = Names.PROP_FILENAME;
 
 	//--------------------------------------------------------------------------------------------
 
 	protected String _dictFileName;
 	private WordClassDictionary dict;
 	
 	//--------------------------------------------------------------------------------------------
 
 	@Override
 	public void initializeCallBack(ComponentContextProperties ccp)
 			throws Exception {
         _dictFileName = getPropertyOrDieTrying(PROP_LIWC_DICT, ccp);
         dict = new WordClassDictionary(_dictFileName);
 	}
 
 	@Override
     public void executeCallBack(ComponentContext cc) throws Exception {
 		String[] tokens = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TOKENS));
         
 		WordClassCount[] LIWC_Values = dict.countClasses(StringUtils.join(tokens));
 //		System.out.print("LIWCOnTokenDoubleValues:");
 		Map<String, Integer> out = new Hashtable<String, Integer>();
 		for (WordClassCount fc : LIWC_Values) {
 			out.put(dict.getClassName(fc.classId),fc.count);
 //			System.out.print(fc);
 		}
 		
 //		System.out.println();
 //		System.out.println(String.format("LIWCOnTokenDoubleValues: found %d classes",out.size()));
		console.fine(String.format("LIWC counter found %d classes from LIWC_Values array of %d",out.size(),LIWC_Values.length));
 		
 		cc.pushDataComponentToOutput(OUT_LIWC_SCORES, BasicDataTypesTools.mapToIntegerMap(out, false));
		cc.pushDataComponentToOutput(OUT_WORD_COUNT, BasicDataTypesTools.integerToIntegers(tokens.length));
 	}
 
 	@Override
 	public void disposeCallBack(ComponentContextProperties ccp)
 			throws Exception {
 		// FIXME Auto-generated method stub
 		
 	}
 }
