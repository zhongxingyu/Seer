 /*******************************************************************************
  * Copyright (c) 2012 Emanuele Tamponi.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Emanuele Tamponi - initial API and implementation
  ******************************************************************************/
 package game.configuration;
 
 import com.thoughtworks.xstream.converters.Converter;
 import com.thoughtworks.xstream.converters.MarshallingContext;
 import com.thoughtworks.xstream.converters.UnmarshallingContext;
 import com.thoughtworks.xstream.io.HierarchicalStreamReader;
 import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
 
 class ConfigurationConverter implements Converter {
 	
 	private ClassLoader classLoader = getClass().getClassLoader();
 
 	@Override
 	public boolean canConvert(Class type) {
 		return Configurable.class.isAssignableFrom(type);
 	}
 	
 	public void setClassLoader(ClassLoader loader) {
 		this.classLoader = loader;
 	}
 
 	@Override
 	public void marshal(Object o, HierarchicalStreamWriter writer,
 			MarshallingContext context) {
 		Configurable object = (Configurable)o;
 		
 		for (String optionName: object.getAllOptionNames()) {
 			if (object.isOmittedFromConfiguration(optionName))
 				continue;
 			Object option = object.getOption(optionName);
 			if (option != null) {
 				if (optionName.matches("^\\d+$"))
 					writer.startNode("item"+optionName);
 				else
 					writer.startNode(optionName);
 				if (option.getClass() != object.getOptionType(optionName))
 					writer.addAttribute("class", option.getClass().getName());
 				context.convertAnother(option);
 				writer.endNode();
 			}
 		}
 	}
 
 	@Override
 	public Object unmarshal(HierarchicalStreamReader reader,
 			UnmarshallingContext context) {
 		Configurable object = null;
 		
 		try {
 			object = (Configurable)(context.currentObject() != null ? context.currentObject() 
 																	: context.getRequiredType().newInstance());
 			while(reader.hasMoreChildren()) {
 				reader.moveDown();
 				String optionName = reader.getNodeName();
 				if (optionName.matches("^item\\d+$"))
 					optionName = optionName.substring(4);
 				String className = reader.getAttribute("class");
 				Class optionType = className != null ? classLoader.loadClass(className) : object.getOptionType(optionName);
 				if (optionName.matches("^\\d+$"))
 					object.setOption("add", context.convertAnother(object, optionType));
 				else
 					object.setOption(optionName, context.convertAnother(object, optionType)/*, false, null*/);
 				reader.moveUp();
 			}
 		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
 			e.printStackTrace();
 		}
 		return object;
 	}
 
 }
