 //
 // Copyright 2010 GOT5 (GO Tapestry 5)
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 // http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 //
 package org.got5.tapestry5.jquery.services.javascript;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.tapestry5.Asset;
 import org.apache.tapestry5.SymbolConstants;
 import org.apache.tapestry5.func.F;
 import org.apache.tapestry5.func.Mapper;
 import org.apache.tapestry5.internal.TapestryInternalUtils;
 import org.apache.tapestry5.internal.services.javascript.CoreJavaScriptStack;
 import org.apache.tapestry5.ioc.annotations.Symbol;
 import org.apache.tapestry5.services.AssetSource;
 import org.apache.tapestry5.services.ClientInfrastructure;
 import org.apache.tapestry5.services.javascript.JavaScriptStack;
 import org.apache.tapestry5.services.javascript.StylesheetLink;
 import org.got5.tapestry5.jquery.JQuerySymbolConstants;
 import org.got5.tapestry5.jquery.utils.JQueryUtils;
 
 /**
  * Replacement for {@link CoreJavaScriptStack}.
  *
  * @author criedel, GOT5
  */
 public class JQueryJavaScriptStack implements JavaScriptStack {
 	
 	private final ClientInfrastructure clientInfrastructure;
 
     private final boolean productionMode;
     
     private final boolean suppressPrototype;
 
     private final List<Asset> jQueryJsStack;
 
     private final List<StylesheetLink> jQueryCssStack;
     
     private final AssetSource assetSource;
 
     public JQueryJavaScriptStack(ClientInfrastructure clientInfrastructure,
     		
     							 @Symbol(SymbolConstants.PRODUCTION_MODE)
                                  final boolean productionMode,
                                  
                                  @Symbol(JQuerySymbolConstants.SUPPRESS_PROTOTYPE)
                                  final boolean suppressPrototype,
 
                                  final AssetSource assetSource)
     {
     	this.clientInfrastructure = clientInfrastructure;
         this.productionMode = productionMode;
         this.suppressPrototype = suppressPrototype;
         this.assetSource = assetSource;
 
         final Mapper<String, Asset> pathToAsset = new Mapper<String, Asset>()
         {
             @Override
             public Asset map(String path)
             {
                 return assetSource.getExpandedAsset(path);
             }
         };
 
         final Mapper<String, StylesheetLink> pathToStylesheetLink = pathToAsset.combine(JQueryUtils.assetToStylesheetLink);
 
        jQueryCssStack = F.flow("${jquery.ui.default-theme.path}")
                            .map(pathToStylesheetLink)
                            .toList();
 
         if (productionMode) {
 
             jQueryJsStack = F
                 .flow(  //"${tapestry.js.path}",
                         "${jquery.core.path}/jquery-${jquery.version}.min.js",
                         "${jquery.ui.path}/minified/jquery.ui.core.min.js",
                         "${jquery.ui.path}/minified/jquery.ui.position.min.js",
                         "${jquery.ui.path}/minified/jquery.ui.widget.min.js",
                         "${jquery.ui.path}/minified/jquery.effects.core.min.js",
                         "${jquery.ui.path}/minified/jquery.effects.highlight.min.js")
                         //"${tapestry.jquery.path}/tapestry-jquery.js")
             .map(pathToAsset).toList();
 
         } else {
 
         	jQueryJsStack = F
                 .flow(  //"${tapestry.js.path}",
                         "${jquery.core.path}/jquery-${jquery.version}.js",
                         "${jquery.ui.path}/jquery.ui.core.js",
                         "${jquery.ui.path}/jquery.ui.position.js",
                         "${jquery.ui.path}/jquery.ui.widget.js",
                         "${jquery.ui.path}/jquery.effects.core.js",
                         "${jquery.ui.path}/jquery.effects.highlight.js")
                         //,"${tapestry.jquery.path}/tapestry-jquery.js")
             .map(pathToAsset).toList();
 
         }
 
     }
 
     public String getInitialization()
     {
         return productionMode ? null : "Tapestry.DEBUG_ENABLED = true;";
     }
 
     public List<Asset> getJavaScriptLibraries()
     {
     	List<Asset> ret = new ArrayList<Asset>();
     	if(suppressPrototype)
     	{
     		String pathToTapestryJs = "${tapestry.js.path}";
     	    Asset  tapestryJs = this.assetSource.getExpandedAsset(pathToTapestryJs);
     	    ret.add(tapestryJs);
     	
     	    ret.addAll(jQueryJsStack);
     		
     	    String pathToTapestryJqueryJs = "${tapestry.jquery.path}/tapestry-jquery.js";
     	    Asset  tapestryJqueryJs = this.assetSource.getExpandedAsset(pathToTapestryJqueryJs);
     		ret.add(tapestryJqueryJs);
     	}
     	else
     	{
     		ret.addAll(jQueryJsStack);
     		ret.addAll(clientInfrastructure.getJavascriptStack());   	
     		
     		String pathToTapestryJqueryJs = "${tapestry.jquery.path}/jquery-noconflict.js";
    		Asset  tapestryJqueryJs = this.assetSource.getExpandedAsset(pathToTapestryJqueryJs);
     		ret.add(tapestryJqueryJs);
     	}	
  		
  		return ret;
         
     }
 
     public List<StylesheetLink> getStylesheets()
     {
     	List<StylesheetLink> ret = new ArrayList<StylesheetLink>();
     	
     	ret.addAll(jQueryCssStack);
     	if(!suppressPrototype)
     	{
     		List<StylesheetLink> prototypeCssStack = F.flow(clientInfrastructure.getStylesheetStack()).map(TapestryInternalUtils.assetToStylesheetLink)
             .toList();
     		ret.addAll(prototypeCssStack);  
     	}	
  		return ret;
     }
 
     public List<String> getStacks()
     {
         return Collections.emptyList();
     }
 
 }
