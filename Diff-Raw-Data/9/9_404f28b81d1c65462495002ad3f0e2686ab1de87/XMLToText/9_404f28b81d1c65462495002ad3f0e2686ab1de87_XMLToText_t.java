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
 
 package org.seasr.meandre.components.tools.xml;
 
 import java.util.Properties;
 import java.util.logging.Level;
 
 import javax.xml.transform.OutputKeys;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.components.abstracts.AbstractExecutableComponent;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.BasicDataTypesTools;
 import org.seasr.meandre.components.tools.Names;
 import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;
 import org.seasr.meandre.support.generic.io.DOMUtils;
 import org.w3c.dom.Document;
 
 /**
  * Converts XML into text document
  *
  * @author Xavier Llor&agrave;
  * @author Boris Capitanu
  *
  */
 
 @Component(
 		name = "XML To Text",
 		creator = "Xavier Llora",
 		baseURL = "meandre://seasr.org/components/foundry/",
 		firingPolicy = FiringPolicy.all,
 		mode = Mode.compute,
 		rights = Licenses.UofINCSA,
 		tags = "xml, io, text",
 		description = "This component write a XML in text form and generates it text form. " +
 				      "The XML document to convert is received in its input. The component outputs the text " +
 				      "generated. A property allows to control the behaviour of the component in " +
 				      "front of an IO error, allowing to continue pushing and empty model or " +
 				      "throwing and exception forcing the finalization of the flow execution.",
 		dependency = {"protobuf-java-2.2.0.jar"}
 )
 public class XMLToText extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
 	@ComponentInput(
 			name = Names.PORT_XML,
 			description = "The XML containing the XML to be read" +
                 "<br>TYPE: org.w3c.dom.Document" +
                 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
 	)
 	protected static final String IN_XML = Names.PORT_XML;
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
 	@ComponentOutput(
 			name = Names.PORT_TEXT,
 			description = "The text containing the XML read" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
 	)
 	protected static final String OUT_TEXT = Names.PORT_TEXT;
 
     //------------------------------ PROPERTIES --------------------------------------------------
 
     // Inherited ignoreErrors (PROP_IGNORE_ERRORS) from AbstractExecutableComponent
 
     @ComponentProperty(
             name=Names.PROP_ENCODING,
             description = "The encoding to use on the outputed text.",
             defaultValue = "UTF-8"
     )
     protected static final String PROP_ENCODING = Names.PROP_ENCODING;
 
 	//--------------------------------------------------------------------------------------------
 
 
 	private Properties outputProps;
 
 
 	//--------------------------------------------------------------------------------------------
 
 	@Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
 		outputProps = new Properties();
 		outputProps.put(OutputKeys.INDENT, "yes");
 		outputProps.put(OutputKeys.ENCODING, ccp.getProperty(PROP_ENCODING));
 	}
 
 	@Override
     public void executeCallBack(ComponentContext cc) throws Exception {
 	    String sXml = "";
 
 	    try {
	    	Object xml = cc.getDataComponentFromInput(IN_XML);
	    	if (xml == null) {
	    		console.info("NO PROPERTY "+ IN_XML);
	    	}
            Document doc = DataTypeParser.parseAsDomDocument(xml);
             sXml = DOMUtils.getString(doc, outputProps);
         }
         catch (Exception e) {
            console.log(Level.WARNING, "XMLToText " + e.getMessage(), e);
 
             if (ignoreErrors)
                 sXml = "";
             else
                 throw e;
         }
 
         cc.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.stringToStrings(sXml));
 	}
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     protected void handleStreamInitiators() throws Exception {
         componentContext.pushDataComponentToOutput(OUT_TEXT,
                 componentContext.getDataComponentFromInput(IN_XML));
     }
 
     @Override
     protected void handleStreamTerminators() throws Exception {
         componentContext.pushDataComponentToOutput(OUT_TEXT,
                 componentContext.getDataComponentFromInput(IN_XML));
     }
 }
