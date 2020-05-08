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
 
 package org.seasr.meandre.components.transform.text;
 
 import net.sf.json.util.JSONStringer;
 import net.sf.json.util.JSONBuilder;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.components.abstracts.AbstractExecutableComponent;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.BasicDataTypesTools;
 import org.seasr.datatypes.BasicDataTypes.IntegersMap;
 import org.seasr.meandre.components.tools.Names;
 
 /**
  * @author Lily Dong
  */
 
 @Component(
         creator = "Lily Dong",
         description = "Converts token count to JSON.",
         name = "Token Count To JSON",
         tags = "token, count, JSON, convert",
         firingPolicy = FiringPolicy.any,
         rights = Licenses.UofINCSA,
         baseURL = "meandre://seasr.org/components/foundry/",
         dependency = {"protobuf-java-2.2.0.jar"}
 )
 
 public class TokenCountToJSON extends AbstractExecutableComponent {
        //------------------------------ INPUTS ------------------------------
 
     @ComponentInput(
             description = "The token counts",
             name = Names.PORT_TOKEN_COUNTS
     )
     protected static final String IN_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;
 
    //------------------------------ OUTPUTS ------------------------------
 
     @ComponentOutput(
             description = "Output JSON object.",
            name = Names.PROP_JSON
     )
    protected static final String OUT_JSON = Names.PROP_JSON;
 
     //--------------------------------------------------------------------
 
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 
     public void executeCallBack(ComponentContext cc) throws Exception {
        JSONStringer myString = new JSONStringer();
        JSONBuilder myBuilder = myString.object();
 
        IntegersMap im = (IntegersMap)cc.getDataComponentFromInput(IN_TOKEN_COUNTS);
        for (int i = 0; i < im.getValueCount(); i++) {
                String key = im.getKey(i);
                int count = im.getValue(i).getValue(0);
             myBuilder.key(key).value(count);
            }
        myBuilder.endObject();
 
        cc.pushDataComponentToOutput(OUT_JSON,
           BasicDataTypesTools.stringToStrings(myString.toString()));
     }
 
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 }
 
