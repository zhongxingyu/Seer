 package ch.tkuhn.lagravis;
 
 import java.awt.Color;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import org.gephi.data.attributes.api.AttributeColumn;
 import org.gephi.data.attributes.api.AttributeController;
 import org.gephi.data.attributes.api.AttributeModel;
 import org.gephi.data.attributes.api.AttributeTable;
 import org.gephi.data.attributes.api.AttributeType;
 import org.gephi.graph.api.GraphController;
 import org.gephi.graph.api.GraphModel;
 import org.gephi.io.exporter.api.ExportController;
 import org.gephi.io.exporter.preview.PNGExporter;
 import org.gephi.io.importer.api.Container;
 import org.gephi.io.importer.api.ImportController;
 import org.gephi.io.processor.plugin.DefaultProcessor;
 import org.gephi.layout.plugin.openord.OpenOrdLayout;
 import org.gephi.layout.plugin.openord.OpenOrdLayoutBuilder;
 import org.gephi.partition.api.Part;
 import org.gephi.partition.api.Partition;
 import org.gephi.partition.api.PartitionController;
 import org.gephi.partition.plugin.NodeColorTransformer;
 import org.gephi.preview.api.PreviewController;
 import org.gephi.preview.api.PreviewModel;
 import org.gephi.preview.api.PreviewProperties;
 import org.gephi.preview.api.PreviewProperty;
 import org.gephi.preview.types.EdgeColor;
 import org.gephi.project.api.ProjectController;
 import org.gephi.project.api.Workspace;
 import org.openide.util.Lookup;
 
 public class Lagravis {
 
 	private static Properties defaultProperties;
 
 	static {
 		defaultProperties = new Properties();
 		InputStream in = Lagravis.class.getResourceAsStream("default.properties");
 		try {
 			defaultProperties.load(in);
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	private Properties properties = new Properties();
 	private File dir;
 
 	public static void main(String[] args) throws IOException {
 		if (args.length != 1) {
 			throw new IllegalArgumentException("Exactly one argument expected: properties file");
 		}
 		Lagravis lagravis = new Lagravis(new File(args[0]));
 		lagravis.run();
 	}
 
 	public Lagravis(Properties properties, File dir) {
 		this.properties = properties;
 		this.dir = dir;
 	}
 
 	public Lagravis(File propertiesFile) throws IOException {
 		properties = new Properties();
 		properties.load(new FileInputStream(propertiesFile));
 		dir = propertiesFile.getParentFile();
 	}
 
 	@SuppressWarnings("rawtypes")
 	public void run() {
 		File inputFile = new File(dir, getProperty("input-file"));
 
 		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
 		pc.newProject();
 		Workspace ws = pc.getCurrentWorkspace();
 		ImportController imp = Lookup.getDefault().lookup(ImportController.class);
 		Container c = null;
 		try {
 			c = imp.importFile(inputFile);
 		} catch (FileNotFoundException ex) {
 			ex.printStackTrace();
 		}
 		imp.process(c, new DefaultProcessor(), ws);
 		GraphModel gm = Lookup.getDefault().lookup(GraphController.class).getModel();
 
 		PreviewModel model = Lookup.getDefault().lookup(PreviewController.class).getModel();
 		PreviewProperties props = model.getProperties();
 		props.putValue(PreviewProperty.EDGE_CURVED, false);
 		props.putValue(PreviewProperty.ARROW_SIZE, 0);
 		props.putValue(PreviewProperty.NODE_BORDER_WIDTH, 0);
 		Color edgeColor = Color.decode(getProperty("edge-color"));
 		props.putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(edgeColor));
 		props.putValue(PreviewProperty.EDGE_OPACITY, new Float(getProperty("edge-opacity")));
 		props.putValue(PreviewProperty.EDGE_THICKNESS, new Float(getProperty("edge-thickness")));
 		props.putValue(PreviewProperty.NODE_OPACITY, new Float(getProperty("node-opacity")));
 
 		OpenOrdLayoutBuilder b = new OpenOrdLayoutBuilder();
 		OpenOrdLayout layout = (OpenOrdLayout) b.buildLayout();
 		layout.resetPropertiesValues();
 		layout.setLiquidStage(new Integer(getProperty("liquid-stage")));
 		layout.setExpansionStage(new Integer(getProperty("expansion-stage")));
 		layout.setCooldownStage(new Integer(getProperty("cooldown-stage")));
 		layout.setCrunchStage(new Integer(getProperty("crunch-stage")));
 		layout.setSimmerStage(new Integer(getProperty("simmer-stage")));
 		layout.setGraphModel(gm);
 		layout.initAlgo();
 		while (layout.canAlgo()) {
 			layout.goAlgo();
 		}
 		layout.endAlgo();
 
 		AttributeModel attributeModelLocal = Lookup.getDefault().lookup(AttributeController.class).getModel();
 		AttributeTable nodeTable = attributeModelLocal.getNodeTable();
 		AttributeColumn col;
 		String typeCol = getProperty("type-column");
 		col = nodeTable.getColumn(typeCol);
 		if (col == null) {
 			col = nodeTable.addColumn(typeCol, AttributeType.STRING);
 		}
 		PartitionController partitionController = Lookup.getDefault().lookup(PartitionController.class);
 		Partition p = partitionController.buildPartition(col, gm.getGraph());
 		NodeColorTransformer transform = new NodeColorTransformer();
 		Color[] defaultColors = new Color[] {
 			Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW, Color.PINK, Color.MAGENTA, Color.ORANGE, Color.CYAN
 		};
 		Map<String,Color> colorMap = new HashMap<>();
 		for (String s : getProperty("node-colors").split(",")) {
 			if (s.isEmpty()) continue;
 			Color color = Color.decode(s.replaceFirst("^.*(#......)$", "$1"));
 			colorMap.put(s.replaceFirst("^(.*)#......$", "$1"), color);
 		}
 
 		int i = 0;
 		System.out.println("Color codes:");
 		for (Part part : p.getParts()) {
 			String v;
 			if (part.getValue() == null) {
 				v = "";
 			} else {
 				v = part.getValue().toString();
 			}
 			Color color = defaultColors[i];
 			if (colorMap.containsKey(v)) {
 				color = colorMap.get(v);
 			}
 			transform.getMap().put(part.getValue(), color);
 			System.out.println(String.format("#%06X", (0xFFFFFF & color.getRGB())) + " " + v);
 			i = (i + 1) % defaultColors.length;
 		}
 		partitionController.transform(p, transform);
 
 		ExportController ec = Lookup.getDefault().lookup(ExportController.class);
 		String outputName = getProperty("output-file");
 		if (outputName.isEmpty()) {
 			outputName = inputFile.getName().replaceFirst("[.][^.]+$", "");
 			if (outputName.isEmpty()) outputName = "out";
 		}
		File parent = inputFile.getAbsoluteFile().getParentFile();
 
 		for (String s : getProperty("output-formats").split(",")) {
 			if (s.isEmpty()) continue;
 			if (s.equals("pdf")) {
 				try {
					ec.exportFile(new File(parent, outputName + ".pdf"));
 				} catch (IOException ex) {
 					ex.printStackTrace();
 				}
 			} else if (s.matches("png[0-9]*")) {
 				int size = 1000;
 				if (s.length() > 3) {
 					size = new Integer(s.substring(3));
 				}
 				PNGExporter pngexp = (PNGExporter) ec.getExporter("png");
 				pngexp.setHeight(size);
 				pngexp.setWidth(size);
 				pngexp.setWorkspace(ws);
 				try {
					ec.exportFile(new File(parent, outputName + ".png"), pngexp);
 				} catch (IOException ex) {
 					ex.printStackTrace();
 				}
 			}
 		}
 
 	}
 
 	private String getProperty(String key) {
 		String value = properties.getProperty(key);
 		if (value != null) return value;
 		return defaultProperties.getProperty(key);
 	}
 
 }
