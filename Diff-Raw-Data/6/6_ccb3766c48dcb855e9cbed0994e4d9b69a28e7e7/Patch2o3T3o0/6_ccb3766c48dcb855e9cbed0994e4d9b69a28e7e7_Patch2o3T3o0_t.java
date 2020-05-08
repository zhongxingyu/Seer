 package de.ptb.epics.eve.data.scandescription.updater.patches;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 
import de.ptb.epics.eve.data.scandescription.ScanModuleTypes;
 import de.ptb.epics.eve.data.scandescription.updater.AbstractModification;
 import de.ptb.epics.eve.data.scandescription.updater.Modification;
 import de.ptb.epics.eve.data.scandescription.updater.Patch;
 import de.ptb.epics.eve.util.data.Version;
 
 /**
  * @author Marcus Michalsky
  * @since 1.18
  */
 public class Patch2o3T3o0 extends Patch {
 	
 	private static Patch2o3T3o0 INSTANCE;
 	
 	private Patch2o3T3o0(Version source, Version target, 
 			List<Modification> modifications) {
 		super(source, target, modifications);
 		modifications.add(new Mod0(this));
 		modifications.add(new Mod1(this));
 		modifications.add(new Mod2(this));
 	}
 	
 	public static Patch2o3T3o0 getInstance() {
 		if (INSTANCE == null) {
 			List<Modification> modifications = new LinkedList<Modification>();
 			
 			INSTANCE = new Patch2o3T3o0(new Version(2, 3), new Version(3, 0), 
 				modifications);
 		}
 		return INSTANCE;
 	}
 	
 	private class Mod0 extends AbstractModification {
 
 		public Mod0(Patch patch) {
 			super(patch, "setting scml version to 3.0");
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void modify(Document document) {
 			Node version = document.getElementsByTagName("version").item(0);
 			if (version.getNodeType() == Node.ELEMENT_NODE) {
 				((Text)version.getFirstChild()).setData("3.0");
 			}
 		}
 		
 	}
 	
 	private class Mod1 extends AbstractModification {
 		
 		public Mod1(Patch patch) {
 			super(patch, "Added storage tag for scan modules.");
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void modify(Document document) {
 			NodeList scanModules = document.getElementsByTagName("scanmodule");
 			for (int i = 0; i < scanModules.getLength(); i++) {
 				Node scanModule = scanModules.item(i);
 				if (scanModule.getNodeType() == Node.ELEMENT_NODE) {
 					int children = scanModule.getChildNodes().getLength();
 					for (int j = 0; j < children; j++) {
 						Node currentChild = scanModule.getChildNodes().item(j);
 						if (currentChild.getNodeName().equals("valuecount")) {
 							Node valueCount = scanModule.getChildNodes().item(j);
 							Element newElement = document.createElement("storage");
 							newElement.setTextContent("default");
 							scanModule.insertBefore(newElement, valueCount);
 							break;
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	private class Mod2 extends AbstractModification {
 		
 		public Mod2(Patch patch) {
 			super(patch, "Replaced scan module type advanced");
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void modify(Document document) {
 			NodeList scanModules = document.getElementsByTagName("scanmodule");
 			Node smType = null;
 			int smAxisTags = 0;
 			int smChannelTags = 0;
 			for (int i = 0; i < scanModules.getLength(); i++) {
 				Node scanModule = scanModules.item(i);
 				if (scanModule.getNodeType() == Node.ELEMENT_NODE) {
 					int children = scanModule.getChildNodes().getLength();
 					smType = null;
 					smAxisTags = 0;
 					smChannelTags = 0;
 					for (int j = 0; j < children; j++) {
 						Node currentChild = scanModule.getChildNodes().item(j);
 						if (currentChild.getNodeName().equals("type")) {
 							smType = scanModule.getChildNodes().item(j);
 						} else if (currentChild.getNodeName().equals("smaxis")) {
 							smAxisTags++;
 						} else if (currentChild.getNodeName().equals("smchannel")) {
 							smChannelTags++;
 						}
 					}
					if (smType.getFirstChild().getNodeValue()
							.equals(ScanModuleTypes.CLASSIC.toString())) {
						continue;
					}
 					if (smAxisTags > 0 && smAxisTags > smChannelTags) {
 						if (smType != null) {
 							smType.getFirstChild().setNodeValue(
 									"save_axis_positions");
 						}
 					} else if (smChannelTags > 0 && smChannelTags > smAxisTags) {
 						smType.getFirstChild().setNodeValue(
 								"save_channel_values");
 					}
 				}
 			}
 		}
 	}
 }
