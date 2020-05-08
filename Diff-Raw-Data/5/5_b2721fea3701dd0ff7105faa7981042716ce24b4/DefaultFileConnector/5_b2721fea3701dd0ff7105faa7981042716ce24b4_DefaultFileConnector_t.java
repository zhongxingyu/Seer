 package com.neodem.componentConnector.io;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 import nu.xom.Builder;
 import nu.xom.Document;
 import nu.xom.Element;
 import nu.xom.Elements;
 import nu.xom.ParsingException;
 
 import com.neodem.componentConnector.graphics.CrudeConsoleDisplay;
 import com.neodem.componentConnector.graphics.Display;
 import com.neodem.componentConnector.model.Connectable;
 import com.neodem.componentConnector.model.Connection;
 import com.neodem.componentConnector.model.Pin;
 import com.neodem.componentConnector.model.component.Component;
 import com.neodem.componentConnector.model.factory.ConnectableDefinition;
 import com.neodem.componentConnector.model.factory.ConnectableFactory;
 import com.neodem.componentConnector.model.sets.AutoAddComponentSet;
 import com.neodem.componentConnector.model.sets.ComponentSet;
 
 public class DefaultFileConnector implements FileConnector {
 
 	private ConnectableFactory factory;
 
 	public ComponentSet read(File componentsDef, File connectablesDef,
 			File connectionsDef) {
 		loadConnectableFactory(connectablesDef);
 		return loadSet(componentsDef, connectionsDef);
 	}
 
 	protected void loadConnectableFactory(File connectableDefs) {
 		Collection<ConnectableDefinition> defs = loadConnectableDefs(connectableDefs);
 		factory = new ConnectableFactory(defs);
 	}
 
 	protected ComponentSet loadSet(File componentsDef, File connectionsDef) {
 		ComponentSet set = null;
 		try {
 			
 			// for collecting all connectables
 			Map<String, Connectable> components = new HashMap<String, Connectable>();
 			
 			// open the components.xml file
 			Builder builder = new Builder();
 			Document doc = builder.build(componentsDef);
 			Element componentsRoot = doc.getRootElement();
 
 			Element componentParent = componentsRoot.getFirstChildElement("components");
 			
 			int rows = Integer.parseInt(componentParent.getAttributeValue("rows"));
 			int cols = Integer.parseInt(componentParent.getAttributeValue("cols"));
 			boolean autoLocate = Boolean.parseBoolean(componentParent.getAttributeValue("autoLocate"));
 
 			// add components
 			Elements componentElements = componentParent.getChildElements();
 			if(autoLocate) {
 				set = new AutoAddComponentSet(cols, rows);
 				for (int i = 0; i < componentElements.size(); i++) {
 					Element componentElement = componentElements.get(i);
 					String type = componentElement.getAttributeValue("type");
 					String name = componentElement.getAttributeValue("name");
 
 					Component component = (Component) factory.make(type, name);
 					if (component != null) {
 						((AutoAddComponentSet) set).addComponentAtRandomLocation(component);
 						components.put(name, component);
 					}
 				}
 			} else {
 				set = new ComponentSet(cols, rows);
 				for (int i = 0; i < componentElements.size(); i++) {
 					Element componentElement = componentElements.get(i);
 					String type = componentElement.getAttributeValue("type");
 					String name = componentElement.getAttributeValue("name");
 					int row = Integer.parseInt(componentElement.getAttributeValue("row"));
 					int col = Integer.parseInt(componentElement.getAttributeValue("col"));
 					boolean inverted = Boolean.parseBoolean(componentElement.getAttributeValue("inv"));
 
 					Component component = (Component) factory.make(type, name);
 					if (component != null) {
 						component.setxLoc(col);
 						component.setyLoc(row);
 						component.setInverted(inverted);
 						set.addComponent(component);
 						components.put(name, component);
 					}
 				}
 			}
 			
 			// add connectables
 			Element connectableParent = componentsRoot.getFirstChildElement("connectables");
 			Elements connectablesElements = connectableParent.getChildElements();
 			for (int i = 0; i < connectablesElements.size(); i++) {
 				Element componentElement = connectablesElements.get(i);
 				String type = componentElement.getAttributeValue("type");
 				String name = componentElement.getAttributeValue("name");
 
 				Connectable con = factory.make(type, name);
 				components.put(name, con);
 			}
 			
 			doc = builder.build(connectionsDef);
 			Element connectionsRoot = doc.getRootElement();
 
 			// add connections
 			Elements connections = connectionsRoot.getChildElements();
 			for (int i = 0; i < connections.size(); i++) {
 				Element c = connections.get(i);
 				String from = c.getAttributeValue("from");
 				String to = c.getAttributeValue("to");
 				String fromPinLabel = c.getAttributeValue("fromPin");
 				String toPinLabel = c.getAttributeValue("toPin");
 
 				Connectable fromComp = components.get(from);
 				Connectable toComp = components.get(to);
 
 				Collection<Pin> fromPins = fromComp.getPins(fromPinLabel);
 				Collection<Pin> toPins = toComp.getPins(toPinLabel);
 
 				Connection con = new Connection(fromComp, fromPins, toComp, toPins);
 				set.addConnection(con);
 			}
 
 		} catch (ParsingException ex) {
			System.err.println("malformed XML file : " + ex.getMessage());
 		} catch (IOException ex) {
			System.err.println("io error : " + ex.getMessage());
 		}
 		return set;
 	}
 
 	private Collection<ConnectableDefinition> loadConnectableDefs(File connectableDefs) {
 		Collection<ConnectableDefinition> defs = new HashSet<ConnectableDefinition>();
 		try {
 			Builder parser = new Builder();
 			Document doc = parser.build(connectableDefs);
 			Element root = doc.getRootElement();
 
 			Elements definitions = root.getChildElements();
 			for (int i = 0; i < definitions.size(); i++) {
 				Element definition = definitions.get(i);
 				String id = definition.getAttributeValue("id");
 				String pinCount = definition.getAttributeValue("pins");
 				String type = definition.getAttributeValue("type");
 
 				ConnectableDefinition d = new ConnectableDefinition(id, type, Integer.parseInt(pinCount));
 
 				Elements pins = definition.getChildElements();
 				for (int j = 0; j < pins.size(); j++) {
 					Element pinElement = pins.get(j);
 					String pinNumber = pinElement.getAttributeValue("number");
 					String pinName = pinElement.getAttributeValue("name");
 					d.addPin(Integer.parseInt(pinNumber), pinName);
 				}
 
 				defs.add(d);
 			}
 		} catch (ParsingException ex) {
 			System.err.println("Cafe con Leche is malformed today. How embarrassing!");
 		} catch (IOException ex) {
 			System.err.println("Could not connect to Cafe con Leche. The site may be down.");
 		}
 		return defs;
 	}
 
 	public void writeToFile(File file, ComponentSet set) {
 		Display d = new CrudeConsoleDisplay();
 		BufferedWriter out = null;
 		try {
 			out = new BufferedWriter(new FileWriter(file));
 			out.write(d.asString(set));
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				out.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 
 
 }
