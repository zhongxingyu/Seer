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
 
 package org.seasr.meandre.components.tools.tuples;
 
import java.io.BufferedReader;
 
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.HashMap;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.components.abstracts.AbstractExecutableComponent;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.BasicDataTypesTools;
 import org.seasr.datatypes.BasicDataTypes.Strings;
 import org.seasr.datatypes.BasicDataTypes.StringsArray;
 import org.seasr.meandre.components.tools.Names;
 
 
 import org.seasr.meandre.support.components.tuples.SimpleTuple;
 import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
 import org.seasr.meandre.support.components.tuples.TupleUtilities;
 import org.seasr.meandre.support.generic.io.HttpUtils;
 
 
 /**
  * This component uses the google ajas/REST api for doing searches (web only)
  *
  * @author Mike Haberman
  *
  */
 
 @Component(
 		name = "Google Search To Tuple",
 		creator = "Mike Haberman",
 		baseURL = "meandre://seasr.org/components/foundry/",
 		firingPolicy = FiringPolicy.all,
 		mode = Mode.compute,
 		rights = Licenses.UofINCSA,
 		tags = "tuple, tools, text, filter",
 		description = "This component does a google search using the google search api ",
 		dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar"}
 )
 public class GoogleSearchToTuple extends AbstractExecutableComponent {
 
 	//------------------------------ OUTPUTS -----------------------------------------------------
 
 	@ComponentOutput(
 			name = Names.PORT_TUPLES,
 			description = "tuples (set based on search results)" +
 			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
 	)
 	protected static final String OUT_TUPLES = Names.PORT_TUPLES;
 
 	@ComponentOutput(
 			name = Names.PORT_META_TUPLE,
 			description = "meta data for the tuple (url, title, content)" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
 	)
 	protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;
 
 	//----------------------------- PROPERTIES ---------------------------------------------------
 
 	@ComponentProperty(
 			name = "query",
 			description = "what to query for",
 		    defaultValue = ""
 	)
 	protected static final String PROP_QUERY = "query";
 
 	//--------------------------------------------------------------------------------------------
 
 	SimpleTuplePeer tuplePeer;
 
 	//--------------------------------------------------------------------------------------------
     String url;
     Map<String,String> columnMap = new HashMap<String,String>();
 	@Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception 
     {
 	    String query = ccp.getProperty(PROP_QUERY).trim();
 	    query = URLEncoder.encode(query, "UTF-8");
 	    
 	    StringBuilder location = new StringBuilder();
 	    location.append("http://ajax.googleapis.com/ajax/services/search/web?v=1.0");
 	    location.append("&start=").append(0); // TODO, add a paging property ?
 	    location.append("&rsz=large");  // small == 4 results, large == 8 results
 	    location.append("&q=").append(query);
 	    	
 	    url = location.toString();
 	    console.info("reading location " + url);
 	    String fields[] = {"url","title", "content"};
 		tuplePeer = new SimpleTuplePeer(fields);
 		
 		columnMap.put("titleNoFormatting", "title");
 		
 	}
 
 	@Override
     public void executeCallBack(ComponentContext cc) throws Exception 
     {
 		
 		String json = HttpUtils.doGET(url, null);
 		 
 		List<SimpleTuple> tuples = jsonToTuples(json, tuplePeer, columnMap);	 
 		List<Strings> output = new ArrayList<Strings>();
 		for (int i = 0; i < tuples.size(); i++) {
 			SimpleTuple tuple = tuples.get(i);
 			output.add(tuple.convert());
 		}
 		
 		Strings[] results = new Strings[output.size()];
 		output.toArray(results);
 		StringsArray outputSafe = BasicDataTypesTools.javaArrayToStringsArray(results);
 		cc.pushDataComponentToOutput(OUT_TUPLES, outputSafe);
 
 	    //
 		// metaData for this tuple producer
 		//
 	    cc.pushDataComponentToOutput(OUT_META_TUPLE, tuplePeer.convert());
 
 	}
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     }
     
     public List<SimpleTuple> jsonToTuples(String jsonData, 
     		                              SimpleTuplePeer peer,
     		                              Map<String,String> columnMap) 
     throws Exception
     {
     	JSONObject json   = new JSONObject(jsonData);
     	JSONObject res    = json.getJSONObject("responseData");
     	JSONArray results = res.getJSONArray("results");
     	ArrayList<SimpleTuple> out = new ArrayList<SimpleTuple>();
     	
     	
     	int urlIdx     = TupleUtilities.getFieldIndexFromName(peer, "url",               columnMap);
     	int titleIdx   = TupleUtilities.getFieldIndexFromName(peer, "titleNoFormatting", columnMap);
     	int contentIdx = TupleUtilities.getFieldIndexFromName(peer, "content",           columnMap);
     	
     	
     	int size = results.length();
 
 		for (int i = 0; i < size;i++) {
 			
 			SimpleTuple tuple = peer.createTuple();
 			JSONObject fields = results.getJSONObject(i);
 			
 			String url      = fields.getString("url");
             String title    = fields.getString("titleNoFormatting");
             String content  = fields.getString("content");
             
             tuple.setValue(urlIdx,     url);
             tuple.setValue(titleIdx,   title);
             tuple.setValue(contentIdx, content);
             
             out.add(tuple); 
 		}
 
     	return out;	
     }
 }
