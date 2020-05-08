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
 
 package org.seasr.meandre.components.tools.text.normalize.porter;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.core.BasicDataTypes;
 import org.seasr.datatypes.core.DataTypeParser;
 import org.seasr.datatypes.core.Names;
 import org.seasr.datatypes.core.exceptions.UnsupportedDataTypeException;
 import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;
 
 /**
  * @author Lily Dong
  */
 
 @Component(
         creator = "Lily Dong",
 		description = "Constructs a dictionary mapping the stemmed words back to " +
 		              "the actual words in the original document, so for the output map, " +
		              "the stemmed words are keys and the actual words are values. " +
		              "If several words have the same stem, the shortest word is choosen " +
 		              "as the representative. ",
         firingPolicy = FiringPolicy.all,
 		name = "Dictionary Of Stemming",
		tags = "stem, dictionary",
 		rights = Licenses.UofINCSA,
 		baseURL="meandre://seasr.org/components/foundry/"
 )
 public class DictionaryOfStemming extends AbstractStreamingExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
 	@ComponentInput(
 			name = Names.PORT_STEMMED_WORDS,
 			description = "The stemmed words" +
                 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                 "<br>TYPE: byte[]" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                 "<br>TYPE: java.lang.Object"
 	)
 	protected static final String IN_STEMMED_WORDS = Names.PORT_STEMMED_WORDS;
 
 	@ComponentInput(
 			name = Names.PORT_WORDS,
 			description = "The original words" +
                 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                 "<br>TYPE: byte[]" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                 "<br>TYPE: java.lang.Object"
 	)
 	protected static final String IN_ORIGINAL_WORDS = Names.PORT_WORDS;
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
 	@ComponentOutput(
 			name = Names.PORT_DICTIONARY,
 			description = "The output dictionary" +
 			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsMap"
 	)
 	protected static final String OUT_DICTIONARY = Names.PORT_DICTIONARY;
 
     //--------------------------------------------------------------------------------------------
 
 
 	/** Store mapping between token and word */
 	private Map<String, String> _tokenMap;
 
 	private boolean _isStreaming;
 
 
     //--------------------------------------------------------------------------------------------
 
 	@Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
 	    super.initializeCallBack(ccp);
 
 		_tokenMap = new HashMap<String, String>();
 		_isStreaming = false;
 	}
 
 	@Override
     public void executeCallBack(ComponentContext cc) throws Exception {
 		String[] originalWords = null;
 		String[] stemmedWords = null;
 
 		try {
 			originalWords = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_ORIGINAL_WORDS));
             stemmedWords = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_STEMMED_WORDS));
         }
         catch (UnsupportedDataTypeException e) {
             if (ignoreErrors)
                 console.warning("processTokens: UnsupportedDataTypeException ignored - input data was not in the correct format");
             else
                 throw e;
         }
 
         for (int i=0; i<originalWords.length; i++ ) {
         	String key = stemmedWords[i];
         	String str = originalWords[i];
         	if (_tokenMap.containsKey(key)) { //take the shorter one
         		String value = _tokenMap.get(key);
         		value = (value.length()>str.length())? str: value;
         		_tokenMap.put(key, value);
         	} else
         		_tokenMap.put(key, str);
         }
 
         if (!_isStreaming) {
         	org.seasr.datatypes.core.BasicDataTypes.StringsMap.Builder mres = BasicDataTypes.StringsMap.newBuilder();
             Set<String> set = _tokenMap.keySet();
             for (String s : set ) {
             	org.seasr.datatypes.core.BasicDataTypes.Strings.Builder sres = BasicDataTypes.Strings.newBuilder();
             	sres.addValue(_tokenMap.get(s));
             	mres.addKey(s);
     			mres.addValue(sres.build());
             }
 
             componentContext.pushDataComponentToOutput(OUT_DICTIONARY, mres.build());
 
             _tokenMap.clear();
         }
 	}
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
         if (_tokenMap != null) {
             _tokenMap.clear();
             _tokenMap = null;
         }
     }
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public boolean isAccumulator() {
         return true;
     }
 
 	@Override
 	public void startStream() throws Exception {
 		if (_isStreaming)
 		    throw new UnsupportedOperationException("Cannot process multiple streams at the same time!");
 
 		_isStreaming = true;
 	}
 
 	@Override
 	public void endStream() throws Exception {
 		if (!_isStreaming)
 	            throw new Exception("Received StreamTerminator without receiving StreamInitiator");
 
 		if (!_tokenMap.isEmpty()) {
 			org.seasr.datatypes.core.BasicDataTypes.StringsMap.Builder mres = BasicDataTypes.StringsMap.newBuilder();
 	        Set<String> set = _tokenMap.keySet();
 	        for (String s : set ) {
 	        	org.seasr.datatypes.core.BasicDataTypes.Strings.Builder sres = BasicDataTypes.Strings.newBuilder();
 	        	sres.addValue(_tokenMap.get(s));
 	        	mres.addKey(s);
 				mres.addValue(sres.build());
 	        }
 
 	        componentContext.pushDataComponentToOutput(OUT_DICTIONARY, mres.build());
 
 	        _tokenMap.clear();
 		}
 
         _isStreaming = false;
 	}
 }
