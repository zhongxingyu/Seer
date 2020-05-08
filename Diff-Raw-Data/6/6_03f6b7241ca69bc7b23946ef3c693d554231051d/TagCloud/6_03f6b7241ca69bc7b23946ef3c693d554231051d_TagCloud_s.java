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
 
 package org.seasr.meandre.components.vis.d3;
 
 import java.util.Arrays;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.meandre.annotations.Component;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.core.DataTypeParser;
 import org.seasr.datatypes.core.Names;
 import org.seasr.meandre.components.vis.html.VelocityTemplateToHTML;
 
 /**
  * @author Boris Capitanu
  */
 
 @Component(
         creator = "Boris Capitanu",
         description = "This components uses the D3 library to generate a tag cloud. "+
         "In the properties setting, you can also indicate the rotation for the text, "+
         "for instance, 'rotation=0' means all words are horizontal; "+
         "'rotation=90' means all words are horizontal or vertical. "+
         "References: D3: Data-Driven Documents, Michael Bostock, Vadim Ogievetsky, Jeffrey Heer, "+
         "IEEE Trans. Visualization & Comp. Graphics (Proc. InfoVis), 2011. " +
 		"D3 tag cloud code from http://github.com/jasondavies/d3-cloud.",
         name = "Tag Cloud",
         tags = "#VIS, visualization, d3, tag cloud",
         rights = Licenses.UofINCSA,
         firingPolicy = FiringPolicy.all,
         baseURL = "meandre://seasr.org/components/foundry/",
         dependency = { "velocity-1.7-dep.jar", "d3-v2.8.1.jar", "d3.layout.cloud.jar" },
         resources  = { "TagCloud.vm" }
 )
 public class TagCloud extends AbstractD3CloudLayoutComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
             name = Names.PORT_TOKEN_COUNTS,
             description = "The token counts to use for creating the tag cloud" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.IntegersMap" +
                 "<br>TYPE: java.util.Map<java.lang.String, java.lang.Integer>"
     )
     protected static final String IN_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;
 
     @ComponentInput(
             name = Names.PORT_DOC_TITLE,
             description = "A label to show abovbe the tag cloud" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                 "<br>TYPE: java.lang.String"
     )
     protected static final String IN_LABEL = Names.PORT_DOC_TITLE;
 
    //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             name = Names.PORT_HTML,
             description = "The HTML for the tag cloud" +
             "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_HTML = Names.PORT_HTML;
 
     //------------------------------ PROPERTIES --------------------------------------------------
 
     @ComponentProperty(
             description = "The title for the page",
             name = Names.PROP_TITLE,
             defaultValue = "Tag Cloud"
     )
     protected static final String PROP_TITLE = Names.PROP_TITLE;
 
     private static final String DEFAULT_TEMPLATE = "org/seasr/meandre/components/vis/d3/TagCloud.vm";
     @ComponentProperty(
             description = "The template name",
             name = VelocityTemplateToHTML.PROP_TEMPLATE,
             defaultValue = DEFAULT_TEMPLATE
     )
     protected static final String PROP_TEMPLATE = VelocityTemplateToHTML.PROP_TEMPLATE;
 
     @ComponentProperty(
             defaultValue = "1000",
             description = "Tag cloud width",
             name = Names.PROP_WIDTH
     )
     protected static final String PROP_WIDTH = Names.PROP_WIDTH;
 
     @ComponentProperty(
             defaultValue = "1000",
             description = "Tag cloud height",
             name = Names.PROP_HEIGHT
     )
     protected static final String PROP_HEIGHT = Names.PROP_HEIGHT;
 
     @ComponentProperty(
             defaultValue = "",
             description = "Name of the font to use for the words in the tag cloud",
             name = Names.PROP_FONT_NAME
     )
     protected static final String PROP_FONT_NAME = Names.PROP_FONT_NAME;
 
     @ComponentProperty(
             defaultValue = "150",
             description = "Maximum font size",
             name = Names.PROP_MAX_SIZE
     )
     protected static final String PROP_FONT_MAX_SIZE = Names.PROP_MAX_SIZE;
 
     @ComponentProperty(
             defaultValue = "20",
             description = "Minimum font size",
             name = Names.PROP_MIN_SIZE
     )
     protected static final String PROP_FONT_MIN_SIZE = Names.PROP_MIN_SIZE;
 
     @ComponentProperty(
             defaultValue = "linear",
             description = "The scale to use, one of:<br><ul>" +
             		"<li>linear</li>" +
             		"<li>log</li>" +
             		"<li>sqrt</li>" +
             		"</ul>",
             name = "scale"
     )
     protected static final String PROP_SCALE = "scale";
 
     @ComponentProperty(
             defaultValue = "category20",
             description = "Color palette to use:<br><ul>" +
             		"<li>category10 - ten categorical colors: <font color='#1f77b4'>#1f77b4</font> <font color='#ff7f0e'>#ff7f0e</font> <font color='#2ca02c'>#2ca02c</font> <font color='#d62728'>#d62728</font> <font color='#9467bd'>#9467bd</font> <font color='#8c564b'>#8c564b</font> <font color='#e377c2'>#e377c2</font> <font color='#7f7f7f'>#7f7f7f</font> <font color='#bcbd22'>#bcbd22</font> <font color='#17becf'>#17becf</font></li>" +
             		"<li>category20 - twenty categorical colors: <font color='#1f77b4'>#1f77b4</font> <font color='#aec7e8'>#aec7e8</font> <font color='#ff7f0e'>#ff7f0e</font> <font color='#ffbb78'>#ffbb78</font> <font color='#2ca02c'>#2ca02c</font> <font color='#98df8a'>#98df8a</font> <font color='#d62728'>#d62728</font> <font color='#ff9896'>#ff9896</font> <font color='#9467bd'>#9467bd</font> <font color='#c5b0d5'>#c5b0d5</font> <font color='#8c564b'>#8c564b</font> <font color='#c49c94'>#c49c94</font> <font color='#e377c2'>#e377c2</font> <font color='#f7b6d2'>#f7b6d2</font> <font color='#7f7f7f'>#7f7f7f</font> <font color='#c7c7c7'>#c7c7c7</font> <font color='#bcbd22'>#bcbd22</font> <font color='#dbdb8d'>#dbdb8d</font> <font color='#17becf'>#17becf</font> <font color='#9edae5'>#9edae5</font></li>" +
             		"<li>category20b - twenty categorical colors: <font color='#393b79'>#393b79</font> <font color='#5254a3'>#5254a3</font> <font color='#6b6ecf'>#6b6ecf</font> <font color='#9c9ede'>#9c9ede</font> <font color='#637939'>#637939</font> <font color='#8ca252'>#8ca252</font> <font color='#b5cf6b'>#b5cf6b</font> <font color='#cedb9c'>#cedb9c</font> <font color='#8c6d31'>#8c6d31</font> <font color='#bd9e39'>#bd9e39</font> <font color='#e7ba52'>#e7ba52</font> <font color='#e7cb94'>#e7cb94</font> <font color='#843c39'>#843c39</font> <font color='#ad494a'>#ad494a</font> <font color='#d6616b'>#d6616b</font> <font color='#e7969c'>#e7969c</font> <font color='#7b4173'>#7b4173</font> <font color='#a55194'>#a55194</font> <font color='#ce6dbd'>#ce6dbd</font> <font color='#de9ed6'>#de9ed6</font></li>" +
             		"<li>category20c - twenty categorical colors: <font color='#3182bd'>#3182bd</font> <font color='#6baed6'>#6baed6</font> <font color='#9ecae1'>#9ecae1</font> <font color='#c6dbef'>#c6dbef</font> <font color='#e6550d'>#e6550d</font> <font color='#fd8d3c'>#fd8d3c</font> <font color='#fdae6b'>#fdae6b</font> <font color='#fdd0a2'>#fdd0a2</font> <font color='#31a354'>#31a354</font> <font color='#74c476'>#74c476</font> <font color='#a1d99b'>#a1d99b</font> <font color='#c7e9c0'>#c7e9c0</font> <font color='#756bb1'>#756bb1</font> <font color='#9e9ac8'>#9e9ac8</font> <font color='#bcbddc'>#bcbddc</font> <font color='#dadaeb'>#dadaeb</font> <font color='#636363'>#636363</font> <font color='#969696'>#969696</font> <font color='#bdbdbd'>#bdbdbd</font> <font color='#d9d9d9'>#d9d9d9</font></li>" +
             		"</ul>",
             name = "color_palette"
     )
     protected static final String PROP_COLOR_PALETTE = "color_palette";
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
         super.initializeCallBack(ccp);
 
         context.put("title", getPropertyOrDieTrying(PROP_TITLE, ccp));
         context.put("width", getPropertyOrDieTrying(PROP_WIDTH, ccp));
         context.put("height", getPropertyOrDieTrying(PROP_HEIGHT, ccp));
         context.put("fontMin", getPropertyOrDieTrying(PROP_FONT_MIN_SIZE, ccp));
         context.put("fontMax", getPropertyOrDieTrying(PROP_FONT_MAX_SIZE, ccp));
         context.put("scale", getPropertyOrDieTrying(PROP_SCALE, ccp));
         context.put("colorPalette", getPropertyOrDieTrying(PROP_COLOR_PALETTE, ccp));
 
         String fontName = getPropertyOrDieTrying(PROP_FONT_NAME, true, false, ccp);
         if (fontName.length() == 0) fontName = null;
 
         context.put("fontName", fontName);
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception {
         Map<String, Integer> tokenCounts = DataTypeParser.parseAsStringIntegerMap(cc.getDataComponentFromInput(IN_TOKEN_COUNTS));
         
         String label = "";
         //TODO: cc.getConnectedInputs() appears to contain IN_LABEL when it's not connected??
 		if (Arrays.asList(cc.getConnectedInputs()).contains(IN_LABEL)) {
         		label = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_LABEL))[0];
 		}
         JSONArray words = new JSONArray();
         JSONArray counts = new JSONArray();
 
         for (Map.Entry<String, Integer> entry : tokenCounts.entrySet()) {
             String word = entry.getKey();
             Integer count = entry.getValue();
             
             // this is a spacer
             words.put(word);
             // this is a spacer
             counts.put(count);
         }
 
         JSONObject data = new JSONObject();
         data.put("words", words);
         data.put("counts", counts);
 
         context.put("data", data.toString());
         if (label != "") {
         	context.put("label", label);
//        	int h = Integer.parseInt( (String)context.get("height") );
        	Object h = context.get("height");
        	int hi;
        	if (h instanceof String) hi = Integer.parseInt((String)h);
        	else hi = (Integer)h;
        	context.put("height", hi + 18);
         }
         console.info("Put " + data.toString().length() + " of data with label '"+label+"'");
 
         super.executeCallBack(componentContext);
     }
 }
