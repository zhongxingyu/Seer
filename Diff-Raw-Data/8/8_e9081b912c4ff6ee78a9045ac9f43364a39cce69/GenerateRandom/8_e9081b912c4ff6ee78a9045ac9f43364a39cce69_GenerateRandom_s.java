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
 
 package org.seasr.meandre.components.tools.basic;
 
 import java.util.Random;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.core.ComponentExecutionException;
 import org.seasr.datatypes.core.BasicDataTypesTools;
 import org.seasr.datatypes.core.Names;
 import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
 
 /**
  * @author Boris Capitanu
  */
 
 @Component(
         name = "Generate Random",
         creator = "Boris Capitanu",
         baseURL = "meandre://seasr.org/components/foundry/",
         firingPolicy = FiringPolicy.all,
         mode = Mode.compute,
         rights = Licenses.UofINCSA,
         tags = "tools, random",
         description = "This component generates either a random string or a random number, depending on how it's properties are set.",
         dependency = { "protobuf-java-2.2.0.jar"}
 )
 public class GenerateRandom extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
             name = Names.PORT_TRIGGER,
             description = "Trigger indicating when new random data should be generated" +
                           "<br>TYPE: java.lang.Object"
     )
     protected static final String IN_TRIGGER = Names.PORT_TRIGGER;
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             name = "random",
             description = "The random data" +
                           "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                           "<br>OR" +
                           "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Integers"
     )
     protected static final String OUT_RANDOM = "random";
 
     @ComponentOutput(
             name = Names.PORT_TRIGGER,
             description = "The original trigger received on the input" +
                           "<br>TYPE: java.lang.Object"
     )
     protected static final String OUT_TRIGGER = Names.PORT_TRIGGER;
 
     //----------------------------- PROPERTIES ---------------------------------------------------
 
     @ComponentProperty(
             name = "type",
             description = "The type of random data to generate (0 = Integer, 1 = String)",
             defaultValue = "0"
     )
     protected static final String PROP_TYPE = "type";
 
     @ComponentProperty(
             name = "min",
             description = "For Integer random data, the lowest boundary for the generated random number (inclusive)",
             defaultValue = "0"
     )
     protected static final String PROP_MIN = "min";
 
     @ComponentProperty(
             name = "max",
             description = "For Integer random data, the highest boundary for the generated random number (exclusive)",
             defaultValue = "100"
     )
     protected static final String PROP_MAX = "max";
 
     @ComponentProperty(
             name = "allowed_chars",
             description = "For String random data, the set of characters from which the random string will be generated",
             defaultValue = "abcdefghijklmnopqrstuvwxyz0123456789"
     )
     protected static final String PROP_ALLOWED_CHARS = "allowed_chars";
 
     @ComponentProperty(
             name = "random_length",
            description = "For String random data, the length of the random string to be generated",
             defaultValue = "8"
     )
     protected static final String PROP_RAND_LENGTH = "random_length";
 
     @ComponentProperty(
             name = "string_prefix",
            description = "For Integer random data, the highest boundary for the generated random number",
             defaultValue = ""
     )
     protected static final String PROP_STR_PREFIX = "string_prefix";
 
     @ComponentProperty(
             name = "string_suffix",
            description = "For Integer random data, the highest boundary for the generated random number",
             defaultValue = ""
     )
     protected static final String PROP_STR_SUFFIX = "string_suffix";
 
     //--------------------------------------------------------------------------------------------
 
 
     protected static final Random _random = new Random();
 
     protected int _type;
     protected int _min;
     protected int _max;
     protected int _randLength;
     protected String _allowedChars;
     protected String _prefix;
     protected String _suffix;
 
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
         _type = Integer.parseInt(getPropertyOrDieTrying(PROP_TYPE, ccp));
         _min = Integer.parseInt(getPropertyOrDieTrying(PROP_MIN, ccp));
         _max = Integer.parseInt(getPropertyOrDieTrying(PROP_MAX, ccp));
         _randLength = Integer.parseInt(getPropertyOrDieTrying(PROP_RAND_LENGTH, ccp));
         _allowedChars = getPropertyOrDieTrying(PROP_ALLOWED_CHARS, ccp);
         _prefix = getPropertyOrDieTrying(PROP_STR_PREFIX, true, false, ccp);
         _suffix = getPropertyOrDieTrying(PROP_STR_SUFFIX, true, false, ccp);
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception {
         Object result;
 
         switch (_type) {
             case 0:
                 int range = _max - _min;
                 int num = _min + _random.nextInt(range);
                 result = BasicDataTypesTools.integerToIntegers(num);
                 break;
 
             case 1:
                 StringBuilder sb = new StringBuilder();
                 sb.append(_prefix);
                 for (int i = 0, len = _allowedChars.length(); i < _randLength; i++)
                     sb.append(_allowedChars.charAt(_random.nextInt(len)));
                 sb.append(_suffix);
                 result = BasicDataTypesTools.stringToStrings(sb.toString());
                 break;
 
             default:
                 throw new ComponentExecutionException("Invalid property value '" + _type + "' for property '" + PROP_TYPE + "'");
         }
 
         cc.pushDataComponentToOutput(OUT_RANDOM, result);
         cc.pushDataComponentToOutput(OUT_TRIGGER, cc.getDataComponentFromInput(IN_TRIGGER));
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 }
