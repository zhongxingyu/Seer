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
 
 package org.seasr.meandre.components.transform.xml;
 
 import java.util.Arrays;
 
 import org.apache.velocity.VelocityContext;
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.components.abstracts.AbstractExecutableComponent;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextException;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.core.ComponentExecutionException;
 import org.seasr.datatypes.BasicDataTypesTools;
 import org.seasr.meandre.components.tools.Names;
 import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;
 import org.seasr.meandre.support.generic.html.VelocityTemplateService;
 
 /**
  *
  * @author Lily Dong
  * @author Boris Capitanu
  *
  */
 
 @Component(
         name = "Date Filter",
         creator = "Lily Dong",
         baseURL = "meandre://seasr.org/components/foundry/",
         firingPolicy = FiringPolicy.all,
         mode = Mode.compute,
         rights = Licenses.UofINCSA,
         tags = "date, xsl, filter",
         description = "This component generates an xsl template to filter an xml file and include only " +
                       "the dates between the minimum and maximum year. This is used to filter the Simile xml file that is generated.",
         dependency = { "protobuf-java-2.2.0.jar", "velocity-1.6.2-dep.jar" },
         resources = { "DateFilter.vm" }
 )
 public class DateFilter extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
             name = Names.PORT_MIN_VALUE,
             description = "The minimum year in input document." +
                 "<br>TYPE: java.lang.Integer" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Integers" +
                 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String IN_MIN_YEAR = Names.PORT_MIN_VALUE;
 
     @ComponentInput(
             name = Names.PORT_MAX_VALUE,
             description = "The maximum year in input document." +
                 "<br>TYPE: java.lang.Integer" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Integers" +
                 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String IN_MAX_YEAR = Names.PORT_MAX_VALUE;
 
     // ------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             name = Names.PORT_XSL,
             description = "The XSL template for filtering dates." +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_XSL = Names.PORT_XSL;
 
     @ComponentOutput(
             name = Names.PORT_MIN_VALUE,
             description = "The minimum year." +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_MIN_YEAR = Names.PORT_MIN_VALUE;
 
     @ComponentOutput(
             name = Names.PORT_MAX_VALUE,
             description = "The maximum year." +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_MAX_YEAR = Names.PORT_MAX_VALUE;
 
     // ------------------------------ PROPERTIES --------------------------------------------------
 
     @ComponentProperty(
             name = Names.PROP_MIN_VALUE,
             description = "The minimum year cutoff value.",
             defaultValue = "1600"
     )
     protected static final String PROP_MIN_VALUE = Names.PROP_MIN_VALUE;
 
     @ComponentProperty(
             name = Names.PROP_MAX_VALUE,
             description = "The maximum year cutoff value.",
             defaultValue = "1800"
     )
     protected static final String PROP_MAX_VALUE = Names.PROP_MAX_VALUE;
 
     //--------------------------------------------------------------------------------------------
 
 
     private static final String DEFAULT_TEMPLATE = "org/seasr/meandre/components/transform/xml/DateFilter.vm";
     private static final VelocityTemplateService velocity = VelocityTemplateService.getInstance();
 
     /** The min year and max year to use */
     private int propMinYear, propMaxYear;
 
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
         propMinYear = Integer.parseInt(ccp.getProperty(PROP_MIN_VALUE));
         propMaxYear = Integer.parseInt(ccp.getProperty(PROP_MAX_VALUE));
 
         if (propMinYear > propMaxYear)
             throw new ComponentContextException(
                    String.format("%s > %s: Minimum value should be smaller or equal to max value",
                             PROP_MIN_VALUE, PROP_MAX_VALUE));
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception {
 
         Integer[] inputMin = DataTypeParser.parseAsInteger(cc.getDataComponentFromInput(IN_MIN_YEAR));
         Integer[] inputMax = DataTypeParser.parseAsInteger(cc.getDataComponentFromInput(IN_MAX_YEAR));
 
         if (inputMin.length != inputMax.length)
             throw new ComponentExecutionException("Inputs are not properly balanced. Both inputs should contain the same number of elements.");
 
         for (int i = 0, iMax = inputMin.length; i < iMax; i++) {
             int inMinYear = inputMin[i];
             int inMaxYear = inputMax[i];
 
             if (inMinYear > inMaxYear)
                console.warning(String.format("%s > %s: Input minimum value should be smaller or equal to input max value",
                         IN_MIN_YEAR, IN_MAX_YEAR));
 
             int minYear = Math.max(inMinYear, propMinYear);
             int maxYear = Math.min(inMaxYear, propMaxYear);
 
             VelocityContext context = velocity.getNewContext();
 
             context.put("min_year", minYear);
             context.put("max_year", maxYear);
 
             String xsl = velocity.generateOutput(context, DEFAULT_TEMPLATE);
 
             cc.pushDataComponentToOutput(OUT_MIN_YEAR, BasicDataTypesTools.integerToIntegers(minYear));
             cc.pushDataComponentToOutput(OUT_MAX_YEAR, BasicDataTypesTools.integerToIntegers(maxYear));
             cc.pushDataComponentToOutput(OUT_XSL, BasicDataTypesTools.stringToStrings(xsl));
         }
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     protected void handleStreamInitiators() throws Exception {
         if (!inputPortsWithInitiators.containsAll(Arrays.asList(new String[] { IN_MIN_YEAR, IN_MAX_YEAR })))
             console.severe("Unbalanced stream delimiter received - the delimiters should arrive on all ports at the same time when FiringPolicy = ALL");
 
         componentContext.pushDataComponentToOutput(OUT_MIN_YEAR, componentContext.getDataComponentFromInput(IN_MIN_YEAR));
         componentContext.pushDataComponentToOutput(OUT_MAX_YEAR, componentContext.getDataComponentFromInput(IN_MAX_YEAR));
     }
 
     @Override
     protected void handleStreamTerminators() throws Exception {
         if (!inputPortsWithTerminators.containsAll(Arrays.asList(new String[] { IN_MIN_YEAR, IN_MAX_YEAR })))
             console.severe("Unbalanced stream delimiter received - the delimiters should arrive on all ports at the same time when FiringPolicy = ALL");
 
         componentContext.pushDataComponentToOutput(OUT_MIN_YEAR, componentContext.getDataComponentFromInput(IN_MIN_YEAR));
         componentContext.pushDataComponentToOutput(OUT_MAX_YEAR, componentContext.getDataComponentFromInput(IN_MAX_YEAR));
     }
 
 }
