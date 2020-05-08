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
 
 package org.meandre.components.rdf.zotero;
 
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.components.abstracts.AbstractExecutableComponent;
 import org.meandre.components.utils.ComponentUtils;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.core.system.components.ext.StreamInitiator;
 import org.meandre.core.system.components.ext.StreamTerminator;
 import org.seasr.meandre.components.tools.Names;
 import org.seasr.meandre.support.io.ModelUtils;
 import org.seasr.meandre.support.parsers.DataTypeParser;
 import org.seasr.meandre.support.zotero.ZoteroUtils;
 
 import com.hp.hpl.jena.rdf.model.Model;
 
 /**
 * This class extracts the list of urls per entry from a Zotero RDF
 * For each Zotero item, we have an output for the url, and title. We output
 * a message on the "PORT_NO_DATA" if there were no urls extracted.
  *
  * @author Xavier Llor&agrave;
  * @author Loretta Auvil
  * @author Boris Capitanu
  */
 
 @Component(
 		creator = "Xavier Llora",
 		description = "Extract the urls for each of the entry of a Zotero RDF",
 		name = "Zotero URL Extractor",
		tags = "zotero, url",
 		rights = Licenses.UofINCSA,
 		mode = Mode.compute,
 		firingPolicy = FiringPolicy.all,
 		baseURL = "meandre://seasr.org/components/zotero/",
 		dependency = {"protobuf-java-2.0.3.jar"}
 )
 public class ZoteroURLExtractor extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
 	@ComponentInput(
 			description = "A map object containing the key elements of the request and the associated values",
 			name = Names.PORT_REQUEST_DATA
 	)
 	protected static final String IN_REQUEST = Names.PORT_REQUEST_DATA;
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
 	@ComponentOutput(
 			description = "Item location",
 			name = Names.PORT_LOCATION
 	)
 	protected static final String OUT_ITEM_LOCATION = Names.PORT_LOCATION;
 
 	@ComponentOutput(
 			description = "Item title",
 			name = Names.PORT_TEXT
 	)
 	protected static final String OUT_ITEM_TITLE = Names.PORT_TEXT;
 
 	@ComponentOutput(
 			description = "No data to display.",
 			name = Names.PORT_NO_DATA
 	)
 	public final static String OUT_NO_DATA = Names.PORT_NO_DATA;
 
     //------------------------------ PROPERTIES --------------------------------------------------
 
     @ComponentProperty(
             name = Names.PROP_WRAP_STREAM,
             description = "Should the pushed message be wrapped as a stream. ",
             defaultValue = "true"
     )
     protected static final String PROP_WRAP_STREAM = Names.PROP_WRAP_STREAM;
 
     //--------------------------------------------------------------------------------------------
 
 
     private boolean bWrapped;
     private int itemCount;
 
     //--------------------------------------------------------------------------------------------
 
 	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
 	    bWrapped = Boolean.parseBoolean(ccp.getProperty(PROP_WRAP_STREAM));
 	}
 
 	public void executeCallBack(ComponentContext cc) throws Exception {
 		Map<String,byte[]> map = DataTypeParser.parseAsStringByteArrayMap(cc.getDataComponentFromInput(IN_REQUEST));
 
 		itemCount = 0;
 
 		for ( String sKey:map.keySet() ) {
 		    int count = 0;
 			Map<String, String> mapURLs = null;
 			try {
 				Model model = ModelUtils.getModel(map.get(sKey), "meandre://specialUri");
 				mapURLs = ZoteroUtils.extractURLs(model);
 				count = mapURLs.size();
 				itemCount += count;
 			} catch (Exception e) {
 			    console.log(Level.WARNING, "Error in data format", e);
 				cc.pushDataComponentToOutput(OUT_NO_DATA, "Error in data format. "+e.getMessage());
 				return;
 			}
 
 			if (count == 0)
 			    continue;
 
 			if (bWrapped)
 			    pushInitiator(sKey);
 
 			for (Entry<String, String> item : mapURLs.entrySet()) {
 			    String sURI = item.getKey();
 			    String sTitle = item.getValue();
 			    console.fine("[ uri = '" + sURI + "' title = '" + sTitle + "' ]");
 
 			    cc.pushDataComponentToOutput(OUT_ITEM_LOCATION, sURI);
 	            cc.pushDataComponentToOutput(OUT_ITEM_TITLE, sTitle);
 			}
 
 			if (bWrapped)
 			    pushTerminator(sKey);
 		}
 
         if (itemCount == 0)
             cc.pushDataComponentToOutput(OUT_NO_DATA,
                     "Your items contained no URL information. Check to see that the URL attribute contains a valid url.");
 	}
 
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 
     //--------------------------------------------------------------------------------------------
 
     /**
      * Pushes an initiator.
      *
      * @param sDoc The document being processed
      * @throws Exception Something went wrong when pushing
      */
     private void pushInitiator(String sDoc) throws Exception {
         console.fine("Pushing " + StreamInitiator.class.getSimpleName());
 
         StreamInitiator si = new StreamInitiator();
         si.put(OUT_ITEM_TITLE, sDoc);
 
         componentContext.pushDataComponentToOutput(OUT_ITEM_LOCATION, si);
         componentContext.pushDataComponentToOutput(OUT_ITEM_TITLE,
                 ComponentUtils.cloneStreamDelimiter(si));
     }
 
     /**
      * Pushes a terminator.
      *
      * @param sDoc The document being processed
      * @throws Exception Something went wrong when pushing
      */
     private void pushTerminator(String sDoc) throws Exception {
         console.fine("Pushing " + StreamTerminator.class.getSimpleName());
 
         StreamTerminator st = new StreamTerminator();
         st.put(OUT_ITEM_TITLE, sDoc);
 
         componentContext.pushDataComponentToOutput(OUT_ITEM_LOCATION, st);
         componentContext.pushDataComponentToOutput(OUT_ITEM_TITLE,
                 ComponentUtils.cloneStreamDelimiter(st));
     }
 }
