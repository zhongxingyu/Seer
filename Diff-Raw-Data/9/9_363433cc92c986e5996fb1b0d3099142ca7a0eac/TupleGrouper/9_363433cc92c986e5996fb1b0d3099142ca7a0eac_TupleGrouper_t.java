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
 
 package org.seasr.meandre.components.tools.tuples;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.core.ComponentExecutionException;
 import org.seasr.datatypes.core.BasicDataTypesTools;
 import org.seasr.datatypes.core.Names;
 import org.seasr.datatypes.core.BasicDataTypes.Strings;
 import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
 import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
 import org.seasr.meandre.support.components.sentiment.SentimentSupport;
 import org.seasr.meandre.support.components.tuples.SimpleTuple;
 import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
 
 /**
  *
  * @author Mike Haberman;
  *
  */
 
 @Component(
 		name = "Tuple Grouper",
 		creator = "Mike Haberman",
 		baseURL = "meandre://seasr.org/components/foundry/",
 		firingPolicy = FiringPolicy.all,
 		mode = Mode.compute,
 		rights = Licenses.UofINCSA,
 		tags = "tuple, group",
 		description = "This component groups (frequency counts) consecutive tuples based on window size " ,
 		dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar"}
 )
 public class TupleGrouper extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
             name = Names.PORT_TUPLES,
             description = "set of labelled tuples to be grouped (e.g. startTokenPosition, token, concept)" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
     )
     protected static final String IN_TUPLES = Names.PORT_TUPLES;
 
     @ComponentInput(
             name = Names.PORT_META_TUPLE,
             description = "meta data for tuples to be labeled" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             name = Names.PORT_TUPLES,
             description = "set of grouped tuples" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
     )
     protected static final String OUT_TUPLES = Names.PORT_TUPLES;
 
     @ComponentOutput(
             name = Names.PORT_META_TUPLE,
             description = "meta data for the tuples (windowId, begin, end, concept, count, frequency)" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;
 
 	//----------------------------- PROPERTIES ---------------------------------------------------
 
     @ComponentProperty(
             description = "field name for the key to group on",
             name = "key",
             defaultValue = "concept"
     )
     protected static final String DATA_PROPERTY_KEY_FIELD = "key";
 
     @ComponentProperty(
             description = "field name for the value to use for windowing, must be numeric",
             name = "windowField",
             defaultValue = "tokenStart"
     )
     protected static final String DATA_PROPERTY_WINDOW_FIELD = "windowField";
 
     @ComponentProperty(
             description = "window size, -1 means use dynamic value based on maxWindows",
             name = "windowSize",
             defaultValue = "-1"
     )
     protected static final String DATA_PROPERTY_WINDOW_SIZE = "windowSize";
 
     @ComponentProperty(
             description = "max. number of windows, -1 means use dyanamic value based on windowSize",
             name = "maxWindows",
             defaultValue = "-1"
     )
     protected static final String DATA_PROPERTY_MAX_WINDOWS = "maxWindows";
 
 	//--------------------------------------------------------------------------------------------
 
 
     private String keyField     = "concept";
     private String windowField  = "tokenStart";
 
 
     // TODO: pull this from properties
     private final String posField  = "pos";
 
 
     // tuple field names for the output
     // pos field
     // key field
     public static String WINDOW_FIELD  = "windowId";
     public static String START_FIELD   = "start";       // units based on windowField
     public static String COUNT_FIELD   = "count";
     public static String FREQ_FIELD    = "frequency";
 
 
     long DEFAULT_WINDOW_SIZE = -1;
     long DEFAULT_MAX_WINDOWS = -1;
 
 
    //--------------------------------------------------------------------------------------------
 
 	@Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
 
 		this.keyField    = ccp.getProperty(DATA_PROPERTY_KEY_FIELD);
 		this.windowField = ccp.getProperty(DATA_PROPERTY_WINDOW_FIELD);
 
 		int ws = Integer.parseInt(ccp.getProperty(DATA_PROPERTY_WINDOW_SIZE));
 		int mw = Integer.parseInt(ccp.getProperty(DATA_PROPERTY_MAX_WINDOWS));
 
 		if (ws <= 0) {
 			ws = -1;
 		}
 		if (mw <= 0) {
 			mw = -1;
 		}
 
 		DEFAULT_WINDOW_SIZE = ws;
 		DEFAULT_MAX_WINDOWS = mw;
 	}
 
 	@Override
     public void executeCallBack(ComponentContext cc) throws Exception {
 		Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
 		SimpleTuplePeer inPeer  = new SimpleTuplePeer(inputMeta);
 		SimpleTuplePeer outPeer = new SimpleTuplePeer(new String[]{
 				                 WINDOW_FIELD, START_FIELD, posField,
 							     keyField, COUNT_FIELD, FREQ_FIELD});
 
 		StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);
 		Strings[] in = BasicDataTypesTools.stringsArrayToJavaArray(input);
 
 
 		SimpleTuple tuple    = inPeer.createTuple();
 		SimpleTuple outTuple = outPeer.createTuple();
 
 		int KEY_IDX   = inPeer.getIndexForFieldName(keyField);
 		int START_IDX = inPeer.getIndexForFieldName(windowField);
 		int POS_IDX   = inPeer.getIndexForFieldName(posField);
 
 		if (KEY_IDX == -1){
 			throw new ComponentExecutionException("tuple has no key field " + keyField);
 		}
 		if (START_IDX == -1){
 			throw new ComponentExecutionException("tuple has no window field " + windowField);
 		}
 
 		//
 		// assumes, tuples are in order, so the last tuple has the biggest windowField value
 		//
		long end = 0;
		if (in.length > 0) {
		   tuple.setValues(in[in.length - 1]);
		   end = Long.parseLong(tuple.getValue(START_IDX));
		}else {
			console.warning("NO tuples to process");
		}
 
 		long numberOfWindows = 1;
 		if (DEFAULT_MAX_WINDOWS == -1 && DEFAULT_WINDOW_SIZE == -1) {
 			numberOfWindows = end/in.length; // even split, an average
 		}
 		else if (DEFAULT_MAX_WINDOWS == -1) {
 			numberOfWindows = end/DEFAULT_WINDOW_SIZE;
 		}
 		else if (DEFAULT_WINDOW_SIZE == -1) {
 			numberOfWindows = DEFAULT_MAX_WINDOWS;
 		}
 		else {
 			//
 			// both were specified, window size takes precendence
 			//
 			//
 			numberOfWindows = end/DEFAULT_WINDOW_SIZE;
 		}
 
 		if (numberOfWindows == 0) numberOfWindows++;
 		int windowSize = (int) (end/numberOfWindows);
 
 		console.info("Window size " + windowSize);
 		console.info("Number of windows (best guess) " + numberOfWindows);
 
 		long currentPosition = 0;
 		long total   = 0;          // running sum, used to make frequencies
 		int windowId = 1;
 
 		Map<String,Integer> freqMap = new HashMap<String,Integer>();
 
 		List<Strings> output = new ArrayList<Strings>();
 		for (int i = 0; i < in.length; i++) {
 
 			tuple.setValues(in[i]);
 			String concept = tuple.getValue(KEY_IDX);
 			long start     = Long.parseLong(tuple.getValue(START_IDX));
 
 			String pos = "N.A";
 			if (POS_IDX != -1) {
 				pos = tuple.getValue(POS_IDX);
 			}
 
 			if (concept == null) {
 				console.info("warning, null concept");
 				continue;
 			}
 
 			Integer count = freqMap.get(concept);
 			if (count == null) {
 				count = new Integer(0);
 			}
 			freqMap.put(concept, count + 1);
 			total += 1;
 
 			//
 			// TODO: if previousPOS value != currentPOS value, that
 			// could mark a new window, this needs to implemented
 			// and pushed to properties
 			//
 
 			//
 			// check to see if we have a window's worth of data
 			//
 			if (start - currentPosition >= windowSize || (i + 1 == in.length)) {
 
 				List<Map.Entry<String, Integer>> sortedEntries;
 				sortedEntries = SentimentSupport.sortHashMap(freqMap);
 
 				for (Map.Entry<String,Integer> v : sortedEntries) {
 					count      = v.getValue();
 					String key = v.getKey();
 
 				//Iterator<String> it = freqMap.keySet().iterator();
 				// while(it.hasNext()) {
 					//String key = it.next();
 					//count = freqMap.get(key);
 
 					double f = ((double)count/(double)total) * 100.0;
 					int rf = (int) f;
 
 					outTuple.setValue(WINDOW_FIELD,  Integer.toString(windowId));
 					outTuple.setValue(START_FIELD,   Long.toString(currentPosition));
 					outTuple.setValue(posField,      pos);
 					outTuple.setValue(keyField,      key);
 					outTuple.setValue(COUNT_FIELD,   Integer.toString(count));
 					outTuple.setValue(FREQ_FIELD,    Integer.toString(rf));
 					output.add(outTuple.convert());
 				}
 
 				freqMap.clear();
 				total = 0;
 				currentPosition = start;
 				windowId++;
 
 			}
 		}
 
 		//
 		// push the whole collection, protocol safe
 		//
 		Strings[] results = new Strings[output.size()];
 		output.toArray(results);
 		StringsArray outputSafe = BasicDataTypesTools.javaArrayToStringsArray(results);
 		cc.pushDataComponentToOutput(OUT_TUPLES, outputSafe);
 
 	    //
 		// metaData for this tuple producer
 		//
 	    cc.pushDataComponentToOutput(OUT_META_TUPLE, outPeer.convert());
 	}
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
 
     }
 }
 
 /*
 Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
 String[] meta = DataTypeParser.parseAsString(inputMeta);
 String fields = meta[0];
 DynamicTuplePeer inPeer = new DynamicTuplePeer(fields);
 
 Strings input = (Strings) cc.getDataComponentFromInput(IN_TUPLES);
 String[] tuples = DataTypeParser.parseAsString(input);
 */
