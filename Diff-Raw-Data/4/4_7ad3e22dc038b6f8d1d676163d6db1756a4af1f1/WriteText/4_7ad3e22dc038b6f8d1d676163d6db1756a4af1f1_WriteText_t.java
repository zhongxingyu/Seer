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
 
 package org.seasr.meandre.components.tools.text.io;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.net.MalformedURLException;
import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
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
 import org.meandre.core.ComponentContextException;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.core.ComponentExecutionException;
 import org.meandre.core.system.components.ext.StreamDelimiter;
 import org.seasr.datatypes.BasicDataTypesTools;
 import org.seasr.meandre.components.tools.Names;
 import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;
 
 /**
  * Writes a text to a location
  *
  * @author Boris Capitanu
  */
 
 @Component(
 		name = "Write Text",
 		creator = "Boris Capitanu",
 		baseURL = "meandre://seasr.org/components/foundry/",
 		firingPolicy = FiringPolicy.all,
 		mode = Mode.compute,
 		rights = Licenses.UofINCSA,
 		tags = "semantic, io, read, text",
 		description = "This component writes text to a file.",
 		dependency = {"protobuf-java-2.2.0.jar"}
 )
 public class WriteText extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
 	@ComponentInput(
 			name = Names.PORT_LOCATION,
 			description = "The URL or file name specifying where the text will be written" +
                 "<br>TYPE: java.net.URI" +
                 "<br>TYPE: java.net.URL" +
                 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
 	)
 	protected static final String IN_LOCATION = Names.PORT_LOCATION;
 
 	@ComponentInput(
 			name = Names.PORT_TEXT,
 			description = "The text to write" +
                 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                 "<br>TYPE: byte[]" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                 "<br>TYPE: java.lang.Object"
 	)
 	protected static final String IN_TEXT = Names.PORT_TEXT;
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
 	@ComponentOutput(
 			name = Names.PORT_LOCATION,
 			description = "The URL or file name containing the written text" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
 	)
 	protected static final String OUT_LOCATION = Names.PORT_LOCATION;
 
 	@ComponentOutput(
 			name = Names.PORT_TEXT,
 			description = "The text written" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
 	)
 	protected static final String OUT_TEXT= Names.PORT_TEXT;
 
     //------------------------------ PROPERTIES --------------------------------------------------
 
     @ComponentProperty(
             name = Names.PROP_DEFAULT_FOLDER,
             description = "The folder to write to for relatively specified locations; " +
             		"absolute locations will be respected. This value can be specified relative " +
             		"to the published_resources folder",
             defaultValue = ""
     )
     protected static final String PROP_DEFAULT_FOLDER = Names.PROP_DEFAULT_FOLDER;
 
     @ComponentProperty(
             name = Names.PROP_APPEND_TIMESTAMP,
             description = "Append the current timestamp to the file specified in the location?",
             defaultValue = "false"
     )
     protected static final String PROP_APPEND_TIMESTAMP = Names.PROP_APPEND_TIMESTAMP;
 
 	//--------------------------------------------------------------------------------------------
 
 
     private String defaultFolder;
     private boolean appendTimestamp;
 
 
     //--------------------------------------------------------------------------------------------
 
 	@Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
 	    defaultFolder = ccp.getProperty(PROP_DEFAULT_FOLDER).trim();
 	    if (defaultFolder.length() == 0)
 	        defaultFolder = ccp.getPublicResourcesDirectory();
 	    else
 	        if (!defaultFolder.startsWith(File.separator))
 	            defaultFolder = new File(ccp.getPublicResourcesDirectory(), defaultFolder).getAbsolutePath();
 
 	    console.fine("Default folder set to: " + defaultFolder);
 
 	    appendTimestamp = Boolean.parseBoolean(ccp.getProperty(PROP_APPEND_TIMESTAMP));
 	}
 
 	@Override
     public void executeCallBack(ComponentContext cc) throws Exception {
 	    String[] inputLocation = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_LOCATION));
 	    String[] inputText = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TEXT));
 	    if (inputLocation.length > 1 || inputText.length > 1)
 	        throw new Exception("Cannot process multiple inputs per execute()");
 
 	    String location = inputLocation[0].trim();
 	    String text = inputText[0];
 
 	    File file = getLocation(location, defaultFolder);
 	    File parentDir = file.getParentFile();
 
 	    if (!parentDir.exists()) {
 	        if (parentDir.mkdirs())
 	            console.finer("Created directory: " + parentDir);
 	    } else
 	        if (!parentDir.isDirectory())
 	            throw new IOException(parentDir.toString() + " must be a directory!");
 
 	    if (appendTimestamp) {
 	        String name = file.getName();
 	        String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
 
 	        int pos = name.lastIndexOf(".");
 	        if (pos < 0)
 	            name += "_" + timestamp;
 	        else
 	            name = String.format("%s_%s%s", name.substring(0, pos), timestamp, name.substring(pos));
 
 	        file = new File(parentDir, name);
 	    }
 
 	    console.fine(String.format("Writing file %s", file));
 
 	    // Write the text to file
 	    Writer writer = new FileWriter(file);
 	    writer.write(text);
 	    writer.close();
 
 	    String publicResourcesDir = new File(cc.getPublicResourcesDirectory()).getAbsolutePath();
 	    if (!publicResourcesDir.endsWith(File.separator)) publicResourcesDir += File.separator;
 
 	    if (file.getAbsolutePath().startsWith(publicResourcesDir)) {
 	        String publicLoc = file.getAbsolutePath().substring(publicResourcesDir.length());
 	        URL outputURL = new URL(cc.getProxyWebUIUrl(true), "/public/resources/" + publicLoc);
 	        console.info("File accessible at: " + outputURL);
 	    }
 
 		cc.pushDataComponentToOutput(OUT_LOCATION, BasicDataTypesTools.stringToStrings(file.toString()));
 		cc.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.stringToStrings(text));
 	}
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     protected void handleStreamInitiators() throws Exception {
         pushDelimiters(
                 componentContext.getDataComponentFromInput(IN_LOCATION),
                 componentContext.getDataComponentFromInput(IN_TEXT));
     }
 
     @Override
     protected void handleStreamTerminators() throws Exception {
         pushDelimiters(
                 componentContext.getDataComponentFromInput(IN_LOCATION),
                 componentContext.getDataComponentFromInput(IN_TEXT));
     }
 
     //--------------------------------------------------------------------------------------------
 
 	/**
 	 * Pushes the obtained delimiters
 	 *
 	 * @param objLoc The location delimiter
 	 * @param objDoc The document delimiter
 	 * @throws Exception Push failed
 	 */
 	private void pushDelimiters(Object objLoc, Object objDoc) throws Exception {
 		if ( objLoc instanceof StreamDelimiter &&  objDoc instanceof StreamDelimiter)  {
 			componentContext.pushDataComponentToOutput(OUT_LOCATION, objLoc);
 			componentContext.pushDataComponentToOutput(OUT_TEXT, objDoc);
 		}
 		else
 			pushMissalignedDelimiters(objLoc, objDoc);
 	}
 
 	/**
 	 * Push the delimiters to the outputs as needed.
 	 *
 	 * @param objLoc The location delimiter
 	 * @param objDoc The document delimiter
 	 * @throws ComponentContextException Push failed
 	 */
 	private void pushMissalignedDelimiters(Object objLoc, Object objDoc) throws ComponentExecutionException {
 	    console.warning("Missaligned delimiters received");
 
 		if ( objLoc instanceof StreamDelimiter ) {
 		    try {
                 StreamDelimiter clone = ComponentUtils.cloneStreamDelimiter((StreamDelimiter) objLoc);
                 componentContext.pushDataComponentToOutput(OUT_LOCATION, objLoc);
                 componentContext.pushDataComponentToOutput(OUT_TEXT, clone);
             }
             catch (Exception e) {
                 throw new ComponentExecutionException(e);
             }
 		}
 		else {
 		    try {
                 StreamDelimiter clone = ComponentUtils.cloneStreamDelimiter((StreamDelimiter) objDoc);
                 componentContext.pushDataComponentToOutput(OUT_LOCATION, clone);
                 componentContext.pushDataComponentToOutput(OUT_TEXT, objDoc);
             }
             catch (Exception e) {
                 throw new ComponentExecutionException(e);
             }
 		}
 	}
 
     //--------------------------------------------------------------------------------------------
 
 	/**
 	 * Gets a file reference to the location specified
 	 *
 	 * @param location The location; can be a full file:/// URL, or an absolute or relative pathname
 	 * @param defaultFolder The folder to use as base for relatively specified pathnames, or null to use current folder
 	 * @return The File reference to the location
 	 * @throws MalformedURLException
 	 * @throws URISyntaxException
 	 */
     protected File getLocation(String location, String defaultFolder) throws MalformedURLException, URISyntaxException {
         // Check if the location is a fully-specified URL
         URL locationURL;
         try {
            locationURL = new URI(location).toURL();
         }
         catch (IllegalArgumentException e) {
             // Not a fully-specified URL, check if absolute location
             if (location.startsWith(File.separator))
                 locationURL = new File(location).toURI().toURL();
             else
                 // Relative location
                 locationURL = new File(defaultFolder, location).toURI().toURL();
         }
 
         return new File(locationURL.toURI());
     }
 }
