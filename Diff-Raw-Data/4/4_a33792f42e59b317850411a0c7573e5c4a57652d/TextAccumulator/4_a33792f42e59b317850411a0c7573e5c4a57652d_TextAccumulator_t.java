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
 
 package org.seasr.meandre.components.tools.basic;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextException;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.core.system.components.ext.StreamInitiator;
 import org.meandre.core.system.components.ext.StreamTerminator;
 import org.seasr.datatypes.core.BasicDataTypesTools;
 import org.seasr.datatypes.core.DataTypeParser;
 import org.seasr.datatypes.core.Names;
 import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
 
 /**
  * @author Boris Capitanu
  */
 
 @Component(
         name = "Text Accumulator",
         creator = "Boris Capitanu",
         baseURL = "meandre://seasr.org/components/foundry/",
         firingPolicy = FiringPolicy.all,
         mode = Mode.compute,
         rights = Licenses.UofINCSA,
         tags = "text, accumulator, string, concatenate",
         description = "This component accumulates multiple text values and pushes them out " +
         		"as a single concatenated value.",
         dependency = {"protobuf-java-2.2.0.jar"}
 )
 public class TextAccumulator extends AbstractExecutableComponent {
 
     // ------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
             name = Names.PORT_TEXT,
             description = "The text" +
                 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                 "<br>TYPE: byte[]" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                 "<br>TYPE: java.lang.Object"
     )
     protected static final String IN_TEXT = Names.PORT_TEXT;
 
     // ------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             name = Names.PORT_TEXT,
             description = "The concatenated text" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_TEXT = Names.PORT_TEXT;
 
     //----------------------------- PROPERTIES ---------------------------------------------------
 
     @ComponentProperty(
             description = "The separator to insert between the text values",
             name = Names.PROP_SEPARATOR,
             defaultValue = " "
     )
     protected static final String PROP_SEPARATOR = Names.PROP_SEPARATOR;
 
     //--------------------------------------------------------------------------------------------
 
 
     protected String _separator;
     protected StringBuilder _accumulator = new StringBuilder();
     protected boolean _gotInitiator;
 
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
         String separator = getPropertyOrDieTrying(PROP_SEPARATOR, false, true, ccp);
        _separator = separator.replaceAll("\\t", "\t").replaceAll("\\n", "\n").replaceAll("\\r", "\r");
         console.fine(String.format("Separator set to '%s' (surrounding single quotes added for readability)", _separator));
 
         _gotInitiator = false;
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception {
         Object input = cc.getDataComponentFromInput(IN_TEXT);
         for (String text : DataTypeParser.parseAsString(input))
             _accumulator.append(_separator).append(text);
 
         if (!_gotInitiator)
             pushConcatenatedTextAndReset();
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
         _accumulator = null;
     }
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     protected void handleStreamInitiators() throws Exception {
         if (_gotInitiator)
             console.severe("Duplicate StreamInitiator received!");
 
         _gotInitiator = true;
     }
 
     @Override
     protected void handleStreamTerminators() throws Exception {
         if (!_gotInitiator)
             console.severe("Got StreamTerminator without StreamInitiator!");
 
         if (_accumulator.length() > 0) {
             componentContext.pushDataComponentToOutput(OUT_TEXT, new StreamInitiator());
             pushConcatenatedTextAndReset();
             componentContext.pushDataComponentToOutput(OUT_TEXT, new StreamTerminator());
         }
 
         _gotInitiator = false;
     }
 
     //--------------------------------------------------------------------------------------------
 
     private void pushConcatenatedTextAndReset() throws ComponentContextException {
         componentContext.pushDataComponentToOutput(OUT_TEXT,
                 BasicDataTypesTools.stringToStrings(_accumulator.substring(_separator.length())));
         _accumulator = new StringBuilder();
     }
 }
