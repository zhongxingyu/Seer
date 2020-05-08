 /*
  * Copyright 2009 Sysmap Solutions Software e Consultoria Ltda.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package br.com.sysmap.crux.gwt.client;
 
 import br.com.sysmap.crux.core.client.declarative.TagAttribute;
 import br.com.sysmap.crux.core.client.declarative.TagAttributes;
import br.com.sysmap.crux.core.rebind.CruxGeneratorException;
import br.com.sysmap.crux.core.rebind.widget.ViewFactoryCreator.SourcePrinter;
 import br.com.sysmap.crux.core.rebind.widget.WidgetCreatorContext;
 
 
 /**
  * Base class for text box based widgets
  * @author Thiago Bustamante
  *
  */
public abstract class TextBoxBaseFactory extends ValueBoxBaseFactory
 {	
 	@Override
 	@TagAttributes({
 		@TagAttribute("value"),
 		@TagAttribute(value="textAlignment", type=TextAlign.class, processor=TextAlignmentProcessor.class)
 	})
	public void processAttributes(SourcePrinter out, WidgetCreatorContext context) throws CruxGeneratorException
 	{
		super.processAttributes(out, context);
 	}
 }
