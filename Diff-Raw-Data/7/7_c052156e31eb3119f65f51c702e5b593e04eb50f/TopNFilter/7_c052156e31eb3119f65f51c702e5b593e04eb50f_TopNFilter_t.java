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
 
 package org.seasr.meandre.components.analytics.text.filters;
 
 import java.util.Comparator;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.components.abstracts.AbstractExecutableComponent;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.BasicDataTypesTools;
 import org.seasr.meandre.components.tools.Names;
 import org.seasr.meandre.support.parsers.DataTypeParser;
 
 /**
  * @author Lily Dong
  * @author Boris Capitanu
  *
  */
 
 @Component(
         creator = "Lily Dong",
         description = "Inputs a Map<String, Integer> and filters " +
                       "words with lower word counts.",
         name = "Top N Filter",
         tags = "word, filter",
         rights = Licenses.UofINCSA,
         baseURL = "meandre://seasr.org/components/tools/",
         dependency = {"protobuf-java-2.0.3.jar"}
 )
 public class TopNFilter extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
             description = "Token counts",
             name = Names.PORT_TOKEN_COUNTS
     )
     protected static final String IN_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             description = "Filtered token counts",
             name = Names.PORT_TOKEN_COUNTS
     )
     protected static final String OUT_FILTERED_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;
 
     //------------------------------ PROPERTIES --------------------------------------------------
 
     @ComponentProperty(
             defaultValue = "100",
             description = "This property sets the maximum number of keys to be outputed",
             name = Names.PROP_N_TOP_TOKENS
     )
     protected static final String PROP_UPPER_LIMIT = Names.PROP_N_TOP_TOKENS;
 
     //--------------------------------------------------------------------------------------------
 
 
    private int _upperLimit;
 
 
     //--------------------------------------------------------------------------------------------
 
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _upperLimit = Integer.parseInt(ccp.getProperty(PROP_UPPER_LIMIT));
     }
 
     public void executeCallBack(ComponentContext cc) throws Exception {
         Map<String, Integer> inputMap = DataTypeParser.parseAsStringIntegerMap(cc.getDataComponentFromInput(IN_TOKEN_COUNTS));
 
         Map<String, Integer> outputMap = inputMap;
 
        int upperLimit = _upperLimit;
         if (inputMap.size() > upperLimit) {
             byValueComparator bvc = new byValueComparator(inputMap);
             TreeMap<String, Integer> sortedMap = new TreeMap<String, Integer>(bvc);
             sortedMap.putAll(inputMap);
             outputMap = new Hashtable<String, Integer>();
             while (upperLimit > 0) {
                 String key = sortedMap.firstKey();
                 Integer value = (Integer)sortedMap.get(key);
                 outputMap.put(key, value);
                 sortedMap.remove(key);
                 --upperLimit;
             }
         }
 
         console.fine(String.format("Filter results:%ninput_tokens=%s%noutput_tokens=%s", inputMap.size(), outputMap.size()));
         cc.pushDataComponentToOutput(OUT_FILTERED_TOKEN_COUNTS, BasicDataTypesTools.mapToIntegerMap(outputMap, false));
     }
 
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 
     //--------------------------------------------------------------------------------------------
 
     @SuppressWarnings("unchecked")
     class byValueComparator implements Comparator<String> {
         Map base_map;
 
         public byValueComparator(Map base_map) {
             this.base_map = base_map;
         }
 
         public int compare(String arg0, String arg1) {
             int result = ((Integer)base_map.get(arg1)).compareTo(
                     (Integer)base_map.get(arg0));
             if(result == 0)
                 result = arg1.compareTo(arg0);
             return result;
         }
     }
 }
