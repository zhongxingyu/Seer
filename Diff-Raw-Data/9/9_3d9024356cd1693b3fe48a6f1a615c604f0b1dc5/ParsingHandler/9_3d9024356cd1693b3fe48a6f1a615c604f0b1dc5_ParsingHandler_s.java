 package ch.compass.gonzoproxy.relay.parser;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.TreeMap;
 
 import org.yaml.snakeyaml.Yaml;
 
 import ch.compass.gonzoproxy.model.Packet;
 
 public class ParsingHandler {
 
 	private static final String TEMPLATE_FOLDER = "templates/";
 
 	private ArrayList<PacketTemplate> templates = new ArrayList<PacketTemplate>();
 
 	private ParsingUnit parsingUnit = new ParsingUnit();
 	private TemplateValidator templateValidator = new TemplateValidator();
 
 	public ParsingHandler() {
 		loadTemplates();
 	}
 
 	public void tryParse(Packet processingPacket) {
 		if (!parseByTemplate(processingPacket))
 			parseByDefault(processingPacket);
 	}
 
	private void loadTemplates() {
 		File[] templateFiles = locateTemplateFiles();
 		for (int i = 0; i < templateFiles.length; i++) {
 			try (InputStream fileInput = new FileInputStream(templateFiles[i])) {
 				Yaml beanLoader = new Yaml();
 				PacketTemplate template = beanLoader.loadAs(fileInput,
 						PacketTemplate.class);
 				templates.add(template);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private File[] locateTemplateFiles() {
 		File folder = new File(TEMPLATE_FOLDER);
 		File[] templateFiles = folder.listFiles(new FilenameFilter() {
 
 			@Override
 			public boolean accept(File dir, String filename) {
 				return filename.endsWith(".apdu");
 			}
 		});
 
 		return templateFiles;
 	}
 
 	private void parseByDefault(Packet processingPacket) {
 		parsingUnit.parseByDefault(processingPacket);
 	}
 
 	private boolean parseByTemplate(Packet processingPacket) {
 		TreeMap<Integer, PacketTemplate> matchingTemplates = new TreeMap<Integer, PacketTemplate>();
 
 		for (PacketTemplate template : templates) {
 			if (templateValidator.accept(template, processingPacket)) {
 				matchingTemplates.put(template.getFields().size(), template);
 			}
 		}
 		
 		if(matchingTemplates.size() > 0) {
 			PacketTemplate bestMatchingTemplate = matchingTemplates.lastEntry().getValue();
 			return parsingUnit.parseBy(bestMatchingTemplate, processingPacket);
 		}
 		return false;
 	}
 }
