 package org.cytoscape.data.reader.kgml;
 
 import giny.view.NodeView;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import org.cytoscape.data.reader.kgml.generated.Entry;
 import org.cytoscape.data.reader.kgml.generated.Graphics;
 import org.cytoscape.data.reader.kgml.generated.Pathway;
 import org.cytoscape.data.reader.kgml.generated.Product;
 import org.cytoscape.data.reader.kgml.generated.Reaction;
 import org.cytoscape.data.reader.kgml.generated.Relation;
 import org.cytoscape.data.reader.kgml.generated.Substrate;
 import org.cytoscape.data.reader.kgml.generated.Subtype;
 
 import cytoscape.CyEdge;
 import cytoscape.CyNetwork;
 import cytoscape.CyNode;
 import cytoscape.Cytoscape;
 import cytoscape.data.CyAttributes;
 import cytoscape.data.Semantics;
 import cytoscape.view.CyNetworkView;
 import cytoscape.visual.ArrowShape;
 import cytoscape.visual.EdgeAppearanceCalculator;
 import cytoscape.visual.GlobalAppearanceCalculator;
 import cytoscape.visual.LineStyle;
 import cytoscape.visual.NodeAppearanceCalculator;
 import cytoscape.visual.NodeShape;
 import cytoscape.visual.VisualPropertyType;
 import cytoscape.visual.VisualStyle;
 import cytoscape.visual.calculators.BasicCalculator;
 import cytoscape.visual.calculators.Calculator;
 import cytoscape.visual.mappings.DiscreteMapping;
 import cytoscape.visual.mappings.ObjectMapping;
 import cytoscape.visual.mappings.PassThroughMapping;
 
 public class PathwayMapper {
 
 	private final Pathway pathway;
 	private final String pathwayName;
 
 	private int[] nodeIdx;
 	private int[] edgeIdx;
 
 	private static final String KEGG_NAME = "KEGG.name";
 	private static final String KEGG_ENTRY = "KEGG.entry";
 	private static final String KEGG_LABEL = "KEGG.label";
 	private static final String KEGG_RELATION = "KEGG.relation";
 	private static final String KEGG_REACTION = "KEGG.reaction";
 	private static final String KEGG_LINK = "KEGG.link";
 	private static final String KEGG_TYPE = "KEGG.type";
 	private static final String KEGG_COLOR = "KEGG.color";
 
 	public PathwayMapper(final Pathway pathway) {
 		this.pathway = pathway;
 		this.pathwayName = pathway.getName();
 	}
 
 	public void doMapping() {
 		mapNode();
 		final List<CyEdge> relationEdges = mapRelationEdge();
 		final List<CyEdge> reactionEdges = mapReactionEdge();
 
 		edgeIdx = new int[relationEdges.size() + reactionEdges.size()];
 		int idx = 0;
 
 		for (CyEdge edge : reactionEdges) {
 			edgeIdx[idx] = edge.getRootGraphIndex();
 			idx++;
 		}
 
 		for (CyEdge edge : relationEdges) {
 			edgeIdx[idx] = edge.getRootGraphIndex();
 			idx++;
 		}
 	}
 
 	private final Map<String, Entry> entryMap = new HashMap<String, Entry>();
 	private final Map<String, Entry> edgeEntryMap = new HashMap<String, Entry>();
 
 	final Map<String, CyNode> nodeMap = new HashMap<String, CyNode>();
 
 	final Map<String, CyNode> id2cpdMap = new HashMap<String, CyNode>();
 	final Map<String, List<Entry>> cpdDataMap = new HashMap<String, List<Entry>>();
 	final Map<CyNode, Entry> geneDataMap = new HashMap<CyNode, Entry>();
 	private final Map<CyNode, String> entry2reaction = new HashMap<CyNode, String>();
 
 	private void mapNode() {
 
 		final String pathwayID = pathway.getName();
 		final String pathway_entryID = pathway.getNumber();
 		final List<Entry> components = pathway.getEntry();
 
 		final CyAttributes nodeAttr = Cytoscape.getNodeAttributes();
 
 		Pattern titleNodePattern = Pattern.compile("TITLE:.*");
 
 		for (final Entry comp : components) {
 			for (Graphics grap : comp.getGraphics()) {
 				if (!titleNodePattern.matcher(grap.getName()).matches()) {
 					if (!grap.getType().equals(KEGGShape.LINE.getTag())) {
 						CyNode node = Cytoscape.getCyNode(pathwayID + "-"
 								+ comp.getId(), true);
 						nodeAttr.setAttribute(node.getIdentifier(), KEGG_NAME,
 								comp.getName());
 						if (comp.getLink() != null)
 							nodeAttr.setAttribute(node.getIdentifier(),
 									KEGG_LINK, comp.getLink());
 						nodeAttr.setAttribute(node.getIdentifier(), KEGG_ENTRY,
 								comp.getType());
 
 						final String reaction = comp.getReaction();
 
 						// Save reaction
 						if (reaction != null) {
 							entry2reaction.put(node, reaction);
 							nodeAttr.setAttribute(node.getIdentifier(),
 									"KEGG.reaction", reaction);
 						}
 
 						// final Graphics graphics = comp.getGraphics();
 						if (grap != null && grap.getName() != null) {
 							nodeAttr.setAttribute(node.getIdentifier(),
 									KEGG_LABEL, grap.getName());
 							if (pathway_entryID.equals("01100")) {
 								nodeAttr.setAttribute(node.getIdentifier(),
 										KEGG_COLOR, grap.getBgcolor());
 							}
 						}
 
 						nodeMap.put(comp.getId(), node);
 						entryMap.put(comp.getId(), comp);
 
 						if (comp.getType().equals(
 								KEGGEntryType.COMPOUND.getTag())) {
 							id2cpdMap.put(comp.getId(), node);
 							List<Entry> current = cpdDataMap
 									.get(comp.getName());
 
 							if (current != null) {
 								current.add(comp);
 							} else {
 								current = new ArrayList<Entry>();
 								current.add(comp);
 							}
 							cpdDataMap.put(comp.getName(), current);
 						} else if (comp.getType().equals(
 								KEGGEntryType.GENE.getTag())
 								|| comp.getType().equals(
 										KEGGEntryType.ORTHOLOG.getTag())) {
 							geneDataMap.put(node, comp);
 						}
 					}
 					// If the pathway is "global metabolism map", put the entry
 					// to entryMap even in "line" graphics.
 					if (pathway_entryID.equals("01100")) {
 						if (grap.getType().equals(KEGGShape.LINE.getTag())) {
 							edgeEntryMap.put(comp.getId(), comp);
 						}
 					}
 				}
 			}
 		}
 
 		nodeIdx = new int[nodeMap.values().size()];
 		int idx = 0;
 		for (CyNode node : nodeMap.values()) {
 			nodeIdx[idx] = node.getRootGraphIndex();
 			idx++;
 		}
 	}
 
 	private List<CyEdge> mapRelationEdge() {
 
 		final List<Relation> relations = pathway.getRelation();
 		final List<CyEdge> edges = new ArrayList<CyEdge>();
 
 		final CyAttributes edgeAttr = Cytoscape.getEdgeAttributes();
 
 		for (Relation rel : relations) {
 
 			final String type = rel.getType();
 
 			if (rel.getType().equals(KEGGRelationType.MAPLINK.getTag())) {
 				final List<Subtype> subs = rel.getSubtype();
 				if (entryMap.get(rel.getEntry1()).getType().equals(
 						KEGGEntryType.MAP.getTag())) {
 					CyNode maplinkNode = nodeMap.get(rel.getEntry1());
 
 					for (Subtype sub : subs) {
 						CyNode cpdNode = nodeMap.get(sub.getValue());
 
 						//System.out.println(maplinkNode.getIdentifier());
 						//System.out.println(cpdNode.getIdentifier() + "\n\n");
 
 						CyEdge edge2 = Cytoscape.getCyEdge(maplinkNode,
 								cpdNode, Semantics.INTERACTION, type, true,
 								true);
 						edges.add(edge2);
 						edgeAttr.setAttribute(edge2.getIdentifier(),
 								KEGG_RELATION, type);
 					}
 				} else {
 					CyNode maplinkNode = nodeMap.get(rel.getEntry2());
 
 					for (Subtype sub : subs) {
 						CyNode cpdNode = nodeMap.get(sub.getValue());
 
 						//System.out.println(maplinkNode.getIdentifier());
 						//System.out.println(cpdNode.getIdentifier() + "\n\n");
 
 						CyEdge edge2 = Cytoscape.getCyEdge(maplinkNode,
 								cpdNode, Semantics.INTERACTION, type, true,
 								true);
 						edges.add(edge2);
 						edgeAttr.setAttribute(edge2.getIdentifier(),
 								KEGG_RELATION, type);
 					}
 				}
 			}
 		}
 
 		return edges;
 
 	}
 
 	private List<CyEdge> mapReactionEdge() {
 
 		final String pathwayID = pathway.getName();
 		final String pathway_entryID = pathway.getNumber();
 		final List<Reaction> reactions = pathway.getReaction();
 		final List<CyEdge> edges = new ArrayList<CyEdge>();
 
 		CyAttributes edgeAttr = Cytoscape.getEdgeAttributes();
 
 		if (pathway_entryID.equals("01100")) {
 			for (Reaction rea : reactions) {
 				Entry rea_entry = edgeEntryMap.get(rea.getId());
 				for (Graphics grap : rea_entry.getGraphics()) {
 					for (Substrate sub : rea.getSubstrate()) {
 						CyNode subNode = nodeMap.get(sub.getId());
 						for (Product pro : rea.getProduct()) {
 							CyNode proNode = nodeMap.get(pro.getId());
 							CyEdge edge = Cytoscape.getCyEdge(subNode, proNode,
 									Semantics.INTERACTION, "cc", true);
 							edges.add(edge);
 							edgeAttr.setAttribute(edge.getIdentifier(),
 									KEGG_NAME, rea_entry.getName());
 							edgeAttr.setAttribute(edge.getIdentifier(),
 									KEGG_REACTION, rea_entry.getReaction());
 							edgeAttr.setAttribute(edge.getIdentifier(),
 									KEGG_TYPE, rea_entry.getType());
 							edgeAttr.setAttribute(edge.getIdentifier(),
 									KEGG_LINK, rea_entry.getLink());
 							edgeAttr.setAttribute(edge.getIdentifier(),
 									KEGG_COLOR, grap.getFgcolor());
 						}
 					}
 				}
 			}
 		} else {
 			for (Reaction rea : reactions) {
 				CyNode reaNode = nodeMap.get(rea.getId());
 				//System.out.println(rea.getId());
 				//System.out.println(reaNode.getIdentifier());
 
 				if (rea.getType().equals("irreversible")) {
 					for (Substrate sub : rea.getSubstrate()) {
 						CyNode subNode = nodeMap.get(sub.getId());
 						CyEdge edge = Cytoscape.getCyEdge(subNode, reaNode,
 								Semantics.INTERACTION, "cr", true, true);
 						edges.add(edge);
 						edgeAttr.setAttribute(edge.getIdentifier(), KEGG_NAME,
 								rea.getName());
 						edgeAttr.setAttribute(edge.getIdentifier(),
 								KEGG_REACTION, rea.getType());
 					}
 					for (Product pro : rea.getProduct()) {
 						CyNode proNode = nodeMap.get(pro.getId());
 						CyEdge edge = Cytoscape.getCyEdge(reaNode, proNode,
 								Semantics.INTERACTION, "rc", true, true);
 						edges.add(edge);
 						edgeAttr.setAttribute(edge.getIdentifier(), KEGG_NAME,
 								rea.getName());
 						edgeAttr.setAttribute(edge.getIdentifier(),
 								KEGG_REACTION, rea.getType());
 					}
 
 				} else if (rea.getType().equals("reversible")) {
 					for (Substrate sub : rea.getSubstrate()) {
 						//System.out.println(sub.getId());
 						CyNode subNode = nodeMap.get(sub.getId());
 						//System.out.println(subNode.getIdentifier());
 
 						CyEdge proEdge = Cytoscape.getCyEdge(reaNode, subNode,
 								Semantics.INTERACTION, "rc", true, true);
 						edges.add(proEdge);
 						edgeAttr.setAttribute(proEdge.getIdentifier(),
 								KEGG_NAME, rea.getName());
 						edgeAttr.setAttribute(proEdge.getIdentifier(),
 								KEGG_REACTION, rea.getType());
 					}
 					for (Product pro : rea.getProduct()) {
 						CyNode proNode = nodeMap.get(pro.getId());
 
 						CyEdge proEdge = Cytoscape.getCyEdge(reaNode, proNode,
 								Semantics.INTERACTION, "rc", true, true);
 						edges.add(proEdge);
 						edgeAttr.setAttribute(proEdge.getIdentifier(),
 								KEGG_NAME, rea.getName());
 						edgeAttr.setAttribute(proEdge.getIdentifier(),
 								KEGG_REACTION, rea.getType());
 					}
 				}
 			}
 		}
 
 		return edges;
 
 	}
 
 	protected void updateView(final CyNetwork network) {
 
 		final String vsName = "KEGG: " + network.getTitle() + " ("
 				+ pathwayName + ")";
 		final CyNetworkView view = Cytoscape.getNetworkView(network
 				.getIdentifier());
 		final CyAttributes nodeAttr = Cytoscape.getNodeAttributes();
 
 		if (Cytoscape.getVisualMappingManager().getCalculatorCatalog()
 				.getVisualStyleNames().contains(vsName)) {
 			// Visual Style already exists.
 			// Apply it and exit.
 
 			final VisualStyle targetStyle = Cytoscape.getVisualMappingManager()
 					.getCalculatorCatalog().getVisualStyle(vsName);
 			Cytoscape.getVisualMappingManager().setVisualStyle(targetStyle);
 			view.setVisualStyle(targetStyle.getName());
 
 			Cytoscape.getVisualMappingManager().setNetworkView(view);
 			view.redrawGraph(false, true);
 			return;
 		}
 
 		final VisualStyle defStyle = new VisualStyle(vsName);
 		final String pathway_entryID = pathway.getNumber();
 
 		NodeAppearanceCalculator nac = defStyle.getNodeAppearanceCalculator();
 		EdgeAppearanceCalculator eac = defStyle.getEdgeAppearanceCalculator();
 		GlobalAppearanceCalculator gac = defStyle
 				.getGlobalAppearanceCalculator();
 
 		// Default values
 		final Color nodeColor = Color.WHITE;
 		final Color edgeColor = Color.BLACK;
 		final Color nodeLineColor = new Color(20, 20, 20);
 		final Color nodeLabelColor = new Color(30, 30, 30);
 
 		final Color geneNodeColor = new Color(153, 255, 153);
 
 		final Font nodeLabelFont = new Font("SansSerif", 7, Font.PLAIN);
 
 		gac.setDefaultBackgroundColor(Color.white);
 
 		final PassThroughMapping m = new PassThroughMapping("", KEGG_LABEL);
 
 		final Calculator nodeLabelMappingCalc = new BasicCalculator(vsName
 				+ "-" + "NodeLabelMapping", m, VisualPropertyType.NODE_LABEL);
 
 		nac.setCalculator(nodeLabelMappingCalc);
 
 		nac.setNodeSizeLocked(false);
 
 		nac.getDefaultAppearance().set(VisualPropertyType.NODE_FILL_COLOR,
 				nodeColor);
 		nac.getDefaultAppearance().set(VisualPropertyType.NODE_SHAPE,
 				NodeShape.ROUND_RECT);
 
 		nac.getDefaultAppearance().set(VisualPropertyType.NODE_BORDER_COLOR,
 				nodeLineColor);
 		nac.getDefaultAppearance().set(VisualPropertyType.NODE_LINE_WIDTH, 1);
 
 		nac.getDefaultAppearance().set(VisualPropertyType.NODE_LABEL_COLOR,
 				nodeLabelColor);
 		nac.getDefaultAppearance().set(VisualPropertyType.NODE_FONT_FACE,
 				nodeLabelFont);
 		nac.getDefaultAppearance().set(VisualPropertyType.NODE_FONT_SIZE, 6);
 
 		// Default Edge appr
 		eac.getDefaultAppearance().set(VisualPropertyType.EDGE_TGTARROW_SHAPE,
 				ArrowShape.DELTA);
 		final DiscreteMapping edgeLineStyle = new DiscreteMapping(
 				LineStyle.SOLID, KEGG_RELATION, ObjectMapping.EDGE_MAPPING);
 		final Calculator edgeLineStyleCalc = new BasicCalculator(vsName + "-"
 				+ "EdgeLineStyleMapping", edgeLineStyle,
 				VisualPropertyType.EDGE_LINE_STYLE);
 		edgeLineStyle.putMapValue(KEGGRelationType.MAPLINK.getTag(),
 				LineStyle.LONG_DASH);
 		eac.setCalculator(edgeLineStyleCalc);
 
 		final DiscreteMapping edgeTgtarrowShape = new DiscreteMapping(
 				ArrowShape.DELTA, Semantics.INTERACTION,
 				ObjectMapping.EDGE_MAPPING);
 		final Calculator edgeTgtarrowShapeCalc = new BasicCalculator(vsName
 				+ "-" + "EdgeTgtarrowStyleMapping", edgeTgtarrowShape,
 				VisualPropertyType.EDGE_TGTARROW_SHAPE);
 
 		edgeTgtarrowShape.putMapValue("cr", ArrowShape.NONE);
 		edgeTgtarrowShape.putMapValue("maplink", ArrowShape.NONE);
 		if (pathway_entryID.equals("01100")) {
 			edgeTgtarrowShape.putMapValue("cc", ArrowShape.NONE);
 		}
 
 		eac.setCalculator(edgeTgtarrowShapeCalc);
 
 		final DiscreteMapping nodeShape = new DiscreteMapping(NodeShape.RECT,
 				KEGG_ENTRY, ObjectMapping.NODE_MAPPING);
 		final Calculator nodeShapeCalc = new BasicCalculator(vsName + "-"
 				+ "NodeShapeMapping", nodeShape, VisualPropertyType.NODE_SHAPE);
 		nodeShape.putMapValue(KEGGEntryType.MAP.getTag(), NodeShape.ROUND_RECT);
 		nodeShape.putMapValue(KEGGEntryType.GENE.getTag(), NodeShape.RECT);
 		nodeShape.putMapValue(KEGGEntryType.ORTHOLOG.getTag(), NodeShape.RECT);
 		nodeShape.putMapValue(KEGGEntryType.COMPOUND.getTag(),
 				NodeShape.ELLIPSE);
 		nac.setCalculator(nodeShapeCalc);
 
 		if (pathway_entryID.equals("01100")) {
 			final DiscreteMapping nodeColorMap = new DiscreteMapping(nodeColor,
 					KEGG_COLOR, ObjectMapping.NODE_MAPPING);
 			final Calculator nodeColorCalc = new BasicCalculator(vsName + "-"
 					+ "NodeColorMapping", nodeColorMap,
 					VisualPropertyType.NODE_FILL_COLOR);
 
 			final DiscreteMapping edgeColorMap = new DiscreteMapping(edgeColor,
 					KEGG_COLOR, ObjectMapping.EDGE_MAPPING);
 			final Calculator edgeColorCalc = new BasicCalculator(vsName + "-"
 					+ "EdgeColorMapping", edgeColorMap,
 					VisualPropertyType.EDGE_COLOR);
 
 			for (String key : nodeMap.keySet()) {
 				for (Graphics nodeGraphics : entryMap.get(key).getGraphics()) {
 					if (!nodeGraphics.getBgcolor().equals("none")) {
 						Color c = Color.decode(nodeGraphics.getBgcolor());
 						nodeColorMap.putMapValue(nodeGraphics.getBgcolor(), c);
 						edgeColorMap.putMapValue(nodeGraphics.getBgcolor(), c);
 					}
 				}
 			}
 
 			final DiscreteMapping edgeWidthMap = new DiscreteMapping(3,
 					Semantics.INTERACTION, ObjectMapping.EDGE_MAPPING);
 			final Calculator edgeWidthCalc = new BasicCalculator(vsName + "-"
 					+ "EdgeWidthMapping", edgeWidthMap,
 					VisualPropertyType.EDGE_LINE_WIDTH);
 			edgeWidthMap.putMapValue("cc", 3);
 
 			nac.setCalculator(nodeColorCalc);
			nac.setCalculator(edgeColorCalc);
			nac.setCalculator(edgeWidthCalc);
 
 		} else {
 			final DiscreteMapping nodeColorMap = new DiscreteMapping(nodeColor,
 					KEGG_ENTRY, ObjectMapping.NODE_MAPPING);
 			final Calculator nodeColorCalc = new BasicCalculator(vsName + "-"
 					+ "NodeColorMapping", nodeColorMap,
 					VisualPropertyType.NODE_FILL_COLOR);
 			nodeColorMap
 					.putMapValue(KEGGEntryType.GENE.getTag(), geneNodeColor);
 			nac.setCalculator(nodeColorCalc);
 		}
 
 		final DiscreteMapping nodeBorderColorMap = new DiscreteMapping(
 				nodeColor, KEGG_ENTRY, ObjectMapping.NODE_MAPPING);
 		final Calculator nodeBorderColorCalc = new BasicCalculator(vsName + "-"
 				+ "NodeBorderColorMapping", nodeBorderColorMap,
 				VisualPropertyType.NODE_BORDER_COLOR);
 		nodeBorderColorMap.putMapValue(KEGGEntryType.MAP.getTag(), Color.BLUE);
 		nac.setCalculator(nodeBorderColorCalc);
 
 		final DiscreteMapping nodeWidth = new DiscreteMapping(30, "ID",
 				ObjectMapping.NODE_MAPPING);
 		final Calculator nodeWidthCalc = new BasicCalculator(vsName + "-"
 				+ "NodeWidthMapping", nodeWidth, VisualPropertyType.NODE_WIDTH);
 		final DiscreteMapping nodeHeight = new DiscreteMapping(30, "ID",
 				ObjectMapping.NODE_MAPPING);
 		final Calculator nodeHeightCalc = new BasicCalculator(vsName + "-"
 				+ "NodeHeightMapping", nodeHeight,
 				VisualPropertyType.NODE_HEIGHT);
 
 		nac.setCalculator(nodeHeightCalc);
 		nac.setCalculator(nodeWidthCalc);
 
 		nodeWidth.setControllingAttributeName("ID", null, false);
 		nodeHeight.setControllingAttributeName("ID", null, false);
 
 		for (String key : nodeMap.keySet()) {
 
 			for (Graphics nodeGraphics : entryMap.get(key).getGraphics()) {
 				if (KEGGShape.getShape(nodeGraphics.getType()) != -1) {
 					final String nodeID = nodeMap.get(key).getIdentifier();
 
 //					System.out.println(nodeID);
 //					System.out.println(key);
 //					System.out.println(nodeMap.size());
 //					System.out.println(nodeMap.get(key).getIdentifier());
 //					System.out.println(nodeMap.get(key).toString());
 //					System.out.println(view.toString());
 
 					final NodeView nv = view.getNodeView(nodeMap.get(key));
 					if(nv == null)
 						continue;
 
 					nv.setXPosition(Double.parseDouble(nodeGraphics.getX()));
 					nv.setYPosition(Double.parseDouble(nodeGraphics.getY()));
 
 					final double w = Double
 							.parseDouble(nodeGraphics.getWidth());
 					nodeAttr.setAttribute(nodeID, "KEGG.nodeWidth", w);
 
 					nodeWidth.putMapValue(nodeID, w);
 
 					final double h = Double.parseDouble(nodeGraphics
 							.getHeight());
 					nodeAttr.setAttribute(nodeID, "KEGG.nodeHeight", h);
 
 					nodeHeight.putMapValue(nodeID, h);
 
 					nv.setShape(KEGGShape.getShape(nodeGraphics.getType()));
 				}
 			}
 		}
 
 		Cytoscape.getVisualMappingManager().getCalculatorCatalog()
 				.addVisualStyle(defStyle);
 		Cytoscape.getVisualMappingManager().setVisualStyle(defStyle);
 		view.setVisualStyle(defStyle.getName());
 
 		Cytoscape.getVisualMappingManager().setNetworkView(view);
 		view.redrawGraph(false, true);
 
 	}
 
 	public int[] getNodeIdx() {
 		return nodeIdx;
 	}
 
 	public int[] getEdgeIdx() {
 		return edgeIdx;
 	}
 
 }
